import re
import urllib.parse
import pathlib
import sys

def check_toc(filename):
    p = pathlib.Path(filename)
    if not p.exists():
        print(f"ERROR: {filename} does not exist")
        return False
    text = p.read_text(encoding='utf-8')
    headings = []
    slug_counts = {}
    in_fence = False
    for line in text.splitlines():
        trimmed = line.strip()
        if trimmed.startswith("```"):
            in_fence = not in_fence
            continue
        if in_fence:
            continue
        m = re.match(r'^(#{1,6})\s+(.+)$', line)
        if m:
            raw = m.group(2).strip()
            cleaned = re.sub(r'!\[([^\]]*)\]\([^)]*\)', r'\1', raw)
            cleaned = re.sub(r'\[([^\]]*)\]\([^)]*\)', r'\1', cleaned)
            cleaned = re.sub(r'`([^`]+)`', r'\1', cleaned)
            cleaned = re.sub(r'[*_~]', '', cleaned)
            cleaned = cleaned.lower()
            cleaned = re.sub(r'<[^>]+>', '', cleaned)
            cleaned = re.sub(r'[^\w\s-]', '', cleaned)
            cleaned = re.sub(r'[\s]+', '-', cleaned).strip('-')
            cnt = slug_counts.get(cleaned, 0)
            slug = cleaned if cnt == 0 else f"{cleaned}-{cnt}"
            slug_counts[cleaned] = cnt + 1
            headings.append(slug)
    
    explicit = set(re.findall(r'<a\s+(?:id|name)=["\']([^"\']+)["\']', text, re.I))
    all_targets = set(headings) | explicit

    lines = text.splitlines()
    in_toc = False
    in_fence = False
    toc_links = []
    for line in lines:
        trimmed = line.strip()
        if trimmed.startswith("```"):
            in_fence = not in_fence
            continue
        if in_fence:
            continue
        if ('table of contents' in trimmed.lower() and trimmed.startswith('#')) or 'id="table-of-contents"' in trimmed.lower():
            in_toc = True
            continue
        if in_toc:
            if trimmed.startswith('---') and len(toc_links) >= 5:
                break
            if re.match(r'^#{1,2}\s+', trimmed) and 'table of contents' not in trimmed.lower():
                break
            for text_link, slug in re.findall(r'\[([^\]]+)\]\(#([^\)]+)\)', trimmed):
                toc_links.append((text_link.strip(), urllib.parse.unquote(slug).strip()))

    broken = [(t, s) for t, s in toc_links if s not in all_targets]
    print(f"{filename}: {len(toc_links)} TOC links extracted, {len(broken)} broken.")
    if broken:
        for t, s in broken:
            print(f"  BROKEN: [{t}](#{s})")
        return False
    return True

all_clean = True
for f in ["ARCHITECTURE.md", "AGENTS.md", "GEMINI.md"]:
    ok = check_toc(f)
    if not ok:
        all_clean = False

print(f"OVERALL TOC RESULT: {'ALL PASS' if all_clean else 'FAIL'}")
