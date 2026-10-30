#!/usr/bin/env python3
"""
Independent Forensic Victory Audit Verifier
Executed by victory_auditor to independently verify:
- ARCHITECTURE.md, AGENTS.md, GEMINI.md
- TOC link integrity (100% resolution)
- All required concepts from ORIGINAL_REQUEST.md
- No facade or dummy placeholder patterns
- Clean forensic verdict
"""

import sys
import os
import re
import json
from pathlib import Path

ROOT = Path(r"c:\Users\parse-it\Documents\Git\AppService")
ARCH_FILE = ROOT / "ARCHITECTURE.md"
AGENTS_FILE = ROOT / "AGENTS.md"
GEMINI_FILE = ROOT / "GEMINI.md"

def gfm_slug(text):
    # Strip markdown images and links
    cleaned = re.sub(r'!\[([^\]]*)\]\([^)]*\)', r'\1', text)
    cleaned = re.sub(r'\[([^\]]*)\]\([^)]*\)', r'\1', cleaned)
    cleaned = re.sub(r'`([^`]+)`', r'\1', cleaned)
    cleaned = re.sub(r'[*_~]', '', cleaned)
    cleaned = re.sub(r'<[^>]+>', '', cleaned)
    cleaned = cleaned.lower()
    cleaned = re.sub(r'[^\w\s-]', '', cleaned)
    cleaned = re.sub(r'[\s]+', '-', cleaned)
    return cleaned.strip('-')

def audit_document(file_path, name):
    print(f"\n--- Auditing {name} ---")
    if not file_path.exists():
        print(f"FAIL: File {name} does not exist!")
        return False
    
    content = file_path.read_text(encoding="utf-8")
    lines = content.splitlines()
    size = len(content.encode("utf-8"))
    line_count = len(lines)
    print(f"Size: {size} bytes, Lines: {line_count}")

    if size < 5000:
        print(f"FAIL: {name} is suspiciously small ({size} bytes)")
        return False

    # Check for placeholder strings (TODO, FIXME, TBD, XXX)
    placeholders = []
    for idx, line in enumerate(lines, 1):
        # Allow mentions inside explanations of what NOT to do or regex
        if re.search(r'\b(TODO|FIXME|XXX)\b', line) and not ("TODO" in line and ("re.search" in line or "placeholder" in line or "anti-pattern" in line)):
            placeholders.append((idx, line.strip()))
    if placeholders:
        print(f"WARNING/FAIL: Found placeholders in {name}: {placeholders[:5]}")
        return False
    else:
        print("PASS: No suspicious placeholders (TODO/FIXME/XXX) found.")

    # Parse headings and explicit anchors
    headings = []
    explicit_anchors = set(re.findall(r'<a\s+(?:id|name)=["\']([^"\']+)["\']', content, re.IGNORECASE))
    slug_counts = {}
    in_fence = False

    for line in lines:
        if line.strip().startswith("```"):
            in_fence = not in_fence
            continue
        if in_fence:
            continue
        m = re.match(r'^(#{1,6})\s+(.+)$', line)
        if m:
            raw_title = m.group(2).strip()
            base = gfm_slug(raw_title)
            if not base:
                continue
            cnt = slug_counts.get(base, 0)
            final_slug = base if cnt == 0 else f"{base}-{cnt}"
            slug_counts[base] = cnt + 1
            headings.append((m.group(1), raw_title, final_slug))

    valid_targets = set(slug for _, _, slug in headings) | explicit_anchors
    print(f"Total Headings: {len(headings)}, Total Valid Anchors: {len(valid_targets)}")

    # Extract TOC links
    toc_links = []
    in_toc = False
    in_fence = False
    for line in lines:
        if line.strip().startswith("```"):
            in_fence = not in_fence
            continue
        if in_fence:
            continue
        if re.search(r'Table of Contents|Contents|TOC', line, re.IGNORECASE) and line.strip().startswith('#'):
            in_toc = True
            continue
        if in_toc:
            if line.strip().startswith("---") and len(toc_links) >= 5:
                break
            if re.match(r'^#{1,2}\s+', line) and not re.search(r'Table of Contents|Contents|TOC', line, re.IGNORECASE):
                break
            matches = re.findall(r'\[([^\]]+)\]\(#([^\)]+)\)', line)
            for text, slug in matches:
                toc_links.append((text.strip(), slug.strip()))

    print(f"Extracted {len(toc_links)} TOC links.")
    if len(toc_links) < 10:
        print(f"FAIL: Insufficient TOC links ({len(toc_links)})")
        return False

    broken = []
    for text, slug in toc_links:
        if slug not in valid_targets:
            broken.append((text, slug))

    if broken:
        print(f"FAIL: {len(broken)} broken TOC links in {name}:")
        for t, s in broken:
            print(f"  - [{t}](#{s})")
        return False
    else:
        print(f"PASS: 100% of TOC links ({len(toc_links)}/{len(toc_links)}) resolve cleanly!")

    return True

def audit_architecture_specifics():
    print("\n--- Auditing ARCHITECTURE.md Specific Requirements ---")
    content = ARCH_FILE.read_text(encoding="utf-8")

    # 1. Mermaid diagrams
    mermaids = re.findall(r'```mermaid\s*\n(.*?)\n```', content, re.DOTALL)
    print(f"Mermaid blocks found: {len(mermaids)}")
    if len(mermaids) < 1:
        print("FAIL: No Mermaid diagram found!")
        return False
    for idx, m in enumerate(mermaids, 1):
        first_line = m.strip().splitlines()[0] if m.strip() else ""
        print(f"  Diagram {idx}: declaration '{first_line}', length {len(m)} chars")

    # 2. Key concepts
    concepts = {
        "Primary / Driving Ports": ["primary port", "driving port", "inbound port"],
        "Secondary / Driven Ports": ["secondary port", "driven port", "outbound port"],
        "Adapters (Driving & Driven)": ["driving adapter", "driven adapter", "inbound adapter", "outbound adapter"],
        "Core Domain / Entities": ["core domain", "entity", "entities", "value object"],
        "Dependency Inversion Rule": ["dependency inversion", "dependencies point inward", "inward dependency"]
    }
    for c_name, terms in concepts.items():
        found = any(term in content.lower() for term in terms)
        if not found:
            print(f"FAIL: Missing concept: {c_name}")
            return False
        print(f"PASS: Found concept '{c_name}'")

    # 3. Callouts & Badges
    callouts = re.findall(r'>\s*\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\]', content, re.IGNORECASE)
    print(f"Markdown callouts found: {len(callouts)} (types: {set(callouts)})")
    if len(callouts) < 2:
        print("FAIL: Insufficient callouts")
        return False

    has_badges = bool(re.search(r'img\.shields\.io', content))
    has_emojis = bool(re.search(r'[\U0001F300-\U0001FAFF\u2600-\u26FF\u2700-\u27BF]', content))
    print(f"Badges present: {has_badges}, Emojis present: {has_emojis}")
    if not (has_badges and has_emojis):
        print("FAIL: Missing badges or emojis")
        return False

    return True

def audit_agents_specifics():
    print("\n--- Auditing AGENTS.md Specific Requirements ---")
    content = AGENTS_FILE.read_text(encoding="utf-8")
    
    # Check best practices, operational standards, lifecycle, checklists
    standards = ["prime directive", "lifecycle", "tool calling", "minimal diff", "verification ladder", "error recovery"]
    for std in standards:
        if std not in content.lower():
            print(f"FAIL: Missing standard in AGENTS.md: {std}")
            return False
        print(f"PASS: Found standard '{std}'")

    checklists = re.findall(r'-\s*\[\s*\]\s+(.+)', content)
    print(f"Actionable checklist items found: {len(checklists)}")
    if len(checklists) < 3:
        print("FAIL: Insufficient checklist items")
        return False

    return True

def audit_gemini_specifics():
    print("\n--- Auditing GEMINI.md Specific Requirements ---")
    content = GEMINI_FILE.read_text(encoding="utf-8")

    topics = [
        ("Gemini Models", ["gemini-1.5-pro", "gemini-1.5-flash", "gemini-2.0-flash"]),
        ("Context Caching", ["cachedcontent", "context caching", "ttl"]),
        ("Environment Config", ["generationconfig", "safety", "block_none"]),
        ("Tool Calling & MCP", ["mcp", "model context protocol", "function calling"])
    ]
    for topic, terms in topics:
        found = any(term in content.lower() for term in terms)
        if not found:
            print(f"FAIL: Missing topic in GEMINI.md: {topic}")
            return False
        print(f"PASS: Found topic '{topic}'")

    # Validate JSON blocks using CommonMark fence extraction
    def extract_cm_blocks(c):
        lines = c.splitlines()
        in_fence = False
        fence_char = ''
        fence_len = 0
        fence_info = ''
        current = []
        blks = []
        for line in lines:
            m = re.match(r'^(?:\s{0,3})(`{3,}|~{3,})(.*)$', line)
            if not in_fence:
                if m:
                    in_fence = True
                    fence_char = m.group(1)[0]
                    fence_len = len(m.group(1))
                    fence_info = m.group(2).strip()
                    current = []
            else:
                if m and m.group(1)[0] == fence_char and len(m.group(1)) >= fence_len and not m.group(2).strip():
                    in_fence = False
                    blks.append((fence_info, "\n".join(current)))
                else:
                    current.append(line)
        return blks

    cm_blocks = extract_cm_blocks(content)
    json_blocks = [code for info, code in cm_blocks if info.lower() == "json"]
    print(f"JSON blocks found in GEMINI.md: {len(json_blocks)}")
    for i, jb in enumerate(json_blocks, 1):
        try:
            json.loads(jb)
        except Exception as e:
            print(f"FAIL: Invalid JSON in block {i}: {e}")
            return False
    print("PASS: All JSON blocks in GEMINI.md are valid RFC 8259 JSON!")
    return True

def main():
    print("==================================================")
    print("INDEPENDENT VICTORY AUDIT EXECUTION")
    print("==================================================")
    
    docs_ok = all([
        audit_document(ARCH_FILE, "ARCHITECTURE.md"),
        audit_document(AGENTS_FILE, "AGENTS.md"),
        audit_document(GEMINI_FILE, "GEMINI.md")
    ])
    
    arch_ok = audit_architecture_specifics()
    agents_ok = audit_agents_specifics()
    gemini_ok = audit_gemini_specifics()

    if docs_ok and arch_ok and agents_ok and gemini_ok:
        print("\n==================================================")
        print("INDEPENDENT AUDIT VERDICT: ALL CHECKS PASSED")
        print("==================================================")
        sys.exit(0)
    else:
        print("\n==================================================")
        print("INDEPENDENT AUDIT VERDICT: FAILED CHECKS DETECTED")
        print("==================================================")
        sys.exit(1)

if __name__ == "__main__":
    main()
