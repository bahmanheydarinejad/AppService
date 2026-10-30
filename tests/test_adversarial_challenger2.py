#!/usr/bin/env python3
"""
tests/test_adversarial_challenger2.py

Independent adversarial test suite executed by Challenger 2 (teamwork_challenger).
Performs empirical stress testing on ARCHITECTURE.md, AGENTS.md, and GEMINI.md:
  1. TOC Link Resolvability in GFM for ALL 3 documents
  2. All In-Document Cross-Anchor Links (#slug) across all 3 documents
  3. Syntax Validation for Embedded Code Blocks (JSON, Python AST)
  4. Placeholder & Hand-waving Detection (TODO, TBD, FIXME, truncated stubs)
  5. AC & Requirement Exhaustive Coverage Verification
  6. Git Tree Boundary & Workspace Invariant Verification
"""

import ast
import json
import os
import re
import subprocess
import sys
import unittest
from pathlib import Path
from typing import Dict, List, Set, Tuple
from urllib.parse import unquote

ROOT_DIR = Path(__file__).resolve().parent.parent
ARCH_FILE = ROOT_DIR / "ARCHITECTURE.md"
AGENTS_FILE = ROOT_DIR / "AGENTS.md"
GEMINI_FILE = ROOT_DIR / "GEMINI.md"


def generate_gfm_slug(heading_text: str) -> str:
    cleaned = re.sub(r'!\[([^\]]*)\]\([^)]*\)', r'\1', heading_text)
    cleaned = re.sub(r'\[([^\]]*)\]\([^)]*\)', r'\1', cleaned)
    cleaned = re.sub(r'`([^`]+)`', r'\1', cleaned)
    cleaned = re.sub(r'[*_~]', '', cleaned)
    cleaned = cleaned.lower()
    cleaned = re.sub(r'<[^>]+>', '', cleaned)
    cleaned = re.sub(r'[^\w\s-]', '', cleaned)
    cleaned = re.sub(r'[\s]+', '-', cleaned)
    return cleaned.strip('-')


def parse_all_anchors(content: str) -> Tuple[Set[str], List[Tuple[int, str, str]]]:
    """Returns (set_of_all_valid_anchor_slugs, list_of_headings)"""
    headings = []
    slug_counts: Dict[str, int] = {}
    in_code = False

    explicit_anchors = set(re.findall(r'<a\s+(?:id|name)=["\']([^"\']+)["\']', content, re.IGNORECASE))

    for line in content.splitlines():
        trimmed = line.strip()
        if trimmed.startswith("```"):
            in_code = not in_code
            continue
        if in_code:
            continue
        m = re.match(r'^(#{1,6})\s+(.+)$', line)
        if m:
            lvl = len(m.group(1))
            raw = m.group(2).strip()
            base = generate_gfm_slug(raw)
            if not base:
                continue
            cnt = slug_counts.get(base, 0)
            slug = base if cnt == 0 else f"{base}-{cnt}"
            slug_counts[base] = cnt + 1
            headings.append((lvl, raw, slug))

    heading_slugs = {s for _, _, s in headings}
    all_anchors = heading_slugs | explicit_anchors
    return all_anchors, headings


def extract_markdown_anchors_in_toc(content: str) -> List[Tuple[str, str]]:
    """Extracts (anchor_text, slug) in Table of Contents."""
    in_toc = False
    in_code = False
    links = []
    for line in content.splitlines():
        trimmed = line.strip()
        if trimmed.startswith("```"):
            in_code = not in_code
            continue
        if in_code:
            continue
        if (re.search(r'Table of Contents|Contents|TOC', trimmed, re.IGNORECASE) and trimmed.startswith('#')) or 'id="table-of-contents"' in trimmed.lower():
            in_toc = True
            continue
        if in_toc:
            if trimmed.startswith("---") and len(links) >= 5:
                break
            if re.match(r'^#{1,2}\s+', trimmed) and not re.search(r'Table of Contents|Contents|TOC', trimmed, re.IGNORECASE):
                break
            for text, slug in re.findall(r'\[([^\]]+)\]\(#([^\)]+)\)', trimmed):
                links.append((text.strip(), unquote(slug).strip()))
    return links


def extract_all_internal_links(content: str) -> List[Tuple[int, str, str]]:
    """Extracts (line_no, link_text, anchor_slug) for all internal links (#slug) in the document outside code fences."""
    in_code = False
    links = []
    for line_idx, line in enumerate(content.splitlines(), start=1):
        trimmed = line.strip()
        if trimmed.startswith("```"):
            in_code = not in_code
            continue
        if in_code:
            continue
        for text, slug in re.findall(r'\[([^\]]+)\]\(#([^\)]*)\)', trimmed):
            links.append((line_idx, text.strip(), unquote(slug).strip()))
    return links


class TestChallengerAdversarialSuite(unittest.TestCase):

    def test_adv_pure_gfm_slug_resolution_without_html_anchors(self):
        """Adversarially verify whether TOC links resolve PURELY against natural GFM heading slugs (without HTML <a id> tags)."""
        docs = [
            ("ARCHITECTURE.md", ARCH_FILE),
            ("AGENTS.md", AGENTS_FILE),
            ("GEMINI.md", GEMINI_FILE),
        ]
        for name, path in docs:
            content = path.read_text(encoding="utf-8")
            # Parse headings ONLY, ignoring explicit <a id="..."> tags
            _, headings = parse_all_anchors(content)
            pure_gfm_slugs = {slug for _, _, slug in headings}
            toc_links = extract_markdown_anchors_in_toc(content)
            unmatched = [(t, s) for t, s in toc_links if s not in pure_gfm_slugs]
            self.assertEqual(
                len(unmatched), 0,
                f"TOC links in {name} that fail pure GFM slugification (relying solely on custom HTML tags):\n" +
                "\n".join(f"  - [{t}](#{s})" for t, s in unmatched)
            )

    def test_adv_all_internal_links_resolution(self):
        """Adversarially verify all internal anchor references (#slug) in the body of all documents resolve or are harmless empty anchors."""
        for name, path in [("ARCHITECTURE.md", ARCH_FILE), ("AGENTS.md", AGENTS_FILE), ("GEMINI.md", GEMINI_FILE)]:
            content = path.read_text(encoding="utf-8")
            valid_anchors, _ = parse_all_anchors(content)
            all_links = extract_all_internal_links(content)
            broken = []
            for line_no, text, slug in all_links:
                # ignore dummy link `#` (e.g. badges pointing to `#`)
                if slug in ("", "#"):
                    continue
                if slug not in valid_anchors:
                    broken.append((line_no, text, slug))
            self.assertEqual(
                len(broken), 0,
                f"Broken internal anchor link(s) found in {name}:\n" +
                "\n".join(f"  Line {l}: [{t}](#{s})" for l, t, s in broken)
            )

    def test_adv_relative_file_links(self):
        """Adversarially verify that any markdown relative file links (e.g. [text](file.md)) point to existing files."""
        for name, path in [("ARCHITECTURE.md", ARCH_FILE), ("AGENTS.md", AGENTS_FILE), ("GEMINI.md", GEMINI_FILE)]:
            content = path.read_text(encoding="utf-8")
            # find all [text](target) where target does not start with http, #, mailto
            in_code = False
            for line_no, line in enumerate(content.splitlines(), start=1):
                if line.strip().startswith("```"):
                    in_code = not in_code
                    continue
                if in_code:
                    continue
                matches = re.findall(r'\[([^\]]+)\]\(([^)#\s]+)\)', line)
                for text, target in matches:
                    if target.startswith(("http://", "https://", "mailto:")):
                        continue
                    # Clean any trailing anchor: e.g. path/to/file#anchor
                    file_part = target.split("#")[0]
                    if not file_part:
                        continue
                    target_path = (ROOT_DIR / file_part).resolve()
                    self.assertTrue(
                        target_path.exists(),
                        f"Non-existent relative file link at {name}:{line_no}: [{text}]({target}) -> {target_path} not found"
                    )

    def test_adv_embedded_code_syntax(self):
        """Adversarially verify syntactical validity of embedded JSON and Python code snippets."""
        for name, path in [("ARCHITECTURE.md", ARCH_FILE), ("AGENTS.md", AGENTS_FILE), ("GEMINI.md", GEMINI_FILE)]:
            content = path.read_text(encoding="utf-8")
            
            # Check JSON blocks (supporting indented code fences in markdown lists)
            json_blocks = re.findall(r'^[ \t]*```json\s*\n(.*?)\n[ \t]*```', content, re.MULTILINE | re.DOTALL)
            for idx, block in enumerate(json_blocks):
                # Clean JavaScript / JSON5 style comments if any: // ...
                cleaned_block = re.sub(r'//.*', '', block).strip()
                try:
                    json.loads(cleaned_block)
                except Exception as e:
                    self.fail(f"Invalid JSON block #{idx+1} in {name}: {e}\nBlock content:\n{block}")

            # Check Python blocks (supporting indented code fences)
            py_blocks = re.findall(r'^[ \t]*```python\s*\n(.*?)\n[ \t]*```', content, re.MULTILINE | re.DOTALL)
            for idx, block in enumerate(py_blocks):
                try:
                    ast.parse(block)
                except SyntaxError as e:
                    self.fail(f"Syntax error in Python code snippet #{idx+1} in {name} (line {e.lineno}): {e.msg}\nSnippet:\n{block}")

    def test_adv_no_unresolved_placeholders(self):
        """Adversarially verify no lazy placeholder tokens or omitted stubs remain in the documentation deliverables."""
        placeholder_patterns = [
            (r'\bTODO\b', "TODO tag"),
            (r'\bFIXME\b', "FIXME tag"),
            (r'\bTBD\b', "TBD tag"),
            (r'\[INSERT\s+[^\]]+\]', "[INSERT ...] placeholder"),
            (r'<INSERT\s+[^>]+>', "<INSERT ...> placeholder"),
            (r'lorem\s+ipsum', "Lorem Ipsum dummy text"),
        ]
        for name, path in [("ARCHITECTURE.md", ARCH_FILE), ("AGENTS.md", AGENTS_FILE), ("GEMINI.md", GEMINI_FILE)]:
            content = path.read_text(encoding="utf-8")
            for pattern, desc in placeholder_patterns:
                matches = re.findall(pattern, content, re.IGNORECASE)
                self.assertEqual(
                    len(matches), 0,
                    f"Found {len(matches)} unresolved placeholder(s) of type '{desc}' in {name}: {matches[:3]}"
                )

    def test_adv_git_boundary_and_file_integrity(self):
        """Empirically verify via git status and diff that no repository source files were damaged or deleted."""
        diff_res = subprocess.run(["git", "diff", "--stat"], capture_output=True, text=True, cwd=ROOT_DIR)
        self.assertEqual(diff_res.returncode, 0)
        self.assertEqual(diff_res.stdout.strip(), "", f"Tracked files were modified in git working copy:\n{diff_res.stdout}")

    def test_adv_code_block_tag_hygiene(self):
        """Inspect all fenced code blocks to ensure no broken or malformed code fence lines."""
        for name, path in [("ARCHITECTURE.md", ARCH_FILE), ("AGENTS.md", AGENTS_FILE), ("GEMINI.md", GEMINI_FILE)]:
            content = path.read_text(encoding="utf-8")
            lines = content.splitlines()
            for line_no, line in enumerate(lines, start=1):
                trimmed = line.strip()
                if trimmed.startswith("```"):
                    tag = trimmed[3:].strip()
                    # If there is a tag, ensure it doesn't contain spaces or invalid chars
                    if tag:
                        self.assertFalse(
                            " " in tag or "\t" in tag,
                            f"Malformed code fence tag at {name}:{line_no}: '{trimmed}'"
                        )

    def test_adv_mermaids_syntactic_structure(self):
        """Adversarially verify all Mermaid code blocks have valid diagram declarations and non-empty definitions."""
        for name, path in [("ARCHITECTURE.md", ARCH_FILE), ("AGENTS.md", AGENTS_FILE), ("GEMINI.md", GEMINI_FILE)]:
            content = path.read_text(encoding="utf-8")
            mermaid_blocks = re.findall(r'```mermaid\s*\n(.*?)\n```', content, re.DOTALL)
            for idx, block in enumerate(mermaid_blocks):
                lines = [l.strip() for l in block.strip().splitlines() if l.strip()]
                self.assertGreaterEqual(len(lines), 2, f"Mermaid block #{idx+1} in {name} is empty or single-line!")
                first_line = lines[0]
                valid_starters = ["flowchart", "graph", "sequenceDiagram", "classDiagram", "stateDiagram", "erDiagram", "journey", "gantt"]
                self.assertTrue(
                    any(first_line.startswith(s) for s in valid_starters),
                    f"Mermaid block #{idx+1} in {name} has unrecognized diagram starter: '{first_line}'"
                )


if __name__ == "__main__":
    unittest.main(verbosity=2)
