#!/usr/bin/env python3
"""
tests/test_docs_adversarial.py

Comprehensive Adversarial Stress Test Suite for:
  - ARCHITECTURE.md (Hexagonal Architecture Guide)
  - AGENTS.md (Universal AI Agent Guidelines & Standards)
  - GEMINI.md (Gemini Model Family Specific Instructions)

Features exhaustive empirical checks for:
  1. File non-emptiness & length thresholds (>300 lines each).
  2. CommonMark code fence balance (supporting nested fences e.g. ````markdown).
  3. Table of Contents link resolution across ALL THREE documents.
  4. Exhaustive internal body anchor link resolution.
  5. Strict RFC 8259 JSON parsing of all ```json blocks.
  6. Deep Mermaid syntax validation (flowcharts, sequence diagrams, subgraphs, direction, classes).
  7. Cross-document reference integrity between ARCHITECTURE.md, AGENTS.md, and GEMINI.md.
  8. Heading slug uniqueness (GFM disambiguation).
  9. Markdown table column parity between header and delimiter rows.
 10. Heading coverage (ensuring all major H2/H3 headings are indexed in TOC).
"""

import json
import os
import re
import unittest
from pathlib import Path
from typing import Dict, List, Set, Tuple
from urllib.parse import unquote

ROOT_DIR = Path(__file__).resolve().parent.parent
DOC_FILES = {
    "ARCHITECTURE.md": ROOT_DIR / "ARCHITECTURE.md",
    "AGENTS.md": ROOT_DIR / "AGENTS.md",
    "GEMINI.md": ROOT_DIR / "GEMINI.md",
}


def generate_gfm_slug(heading_text: str) -> str:
    """
    Generates a GitHub-Flavored Markdown (GFM) anchor slug from raw heading text.
    GFM Algorithm:
      1. Strip markdown images and links: ![alt](url) -> alt, [text](url) -> text.
      2. Strip inline code `code` -> code.
      3. Strip formatting tags (*, _, ~, <tag>).
      4. Downcase.
      5. Remove characters that are NOT alphanumeric, whitespace, underscores, or hyphens.
      6. Convert each whitespace sequence to a single hyphen.
      7. Strip leading and trailing hyphens.
    """
    cleaned = re.sub(r'!\[([^\]]*)\]\([^)]*\)', r'\1', heading_text)
    cleaned = re.sub(r'\[([^\]]*)\]\([^)]*\)', r'\1', cleaned)
    cleaned = re.sub(r'`([^`]+)`', r'\1', cleaned)
    cleaned = re.sub(r'[*_~]', '', cleaned)
    cleaned = re.sub(r'<[^>]+>', '', cleaned)
    cleaned = cleaned.lower()
    cleaned = re.sub(r'[^\w\s-]', '', cleaned)
    cleaned = re.sub(r'[\s]+', '-', cleaned)
    cleaned = cleaned.strip('-')
    return cleaned


def parse_headings_and_anchors(content: str) -> Tuple[List[Tuple[int, str, str]], Set[str]]:
    """
    Parses headings (with GFM duplicate suffix -1, -2, etc.) and explicit HTML anchors.
    Properly ignores headings inside code fences.
    """
    headings: List[Tuple[int, str, str]] = []
    slug_counts: Dict[str, int] = {}
    explicit_anchors: Set[str] = set()

    lines = content.splitlines()
    in_fence = False
    fence_char = ''
    fence_len = 0

    for line in lines:
        m_fence = re.match(r'^(?:\s{0,3})(`{3,}|~{3,})(.*)$', line)
        if not in_fence:
            if m_fence:
                in_fence = True
                fence_char = m_fence.group(1)[0]
                fence_len = len(m_fence.group(1))
                continue
        else:
            if m_fence and m_fence.group(1)[0] == fence_char and len(m_fence.group(1)) >= fence_len and not m_fence.group(2).strip():
                in_fence = False
            continue

        # Extract explicit HTML anchor targets: <a id="..." or <a name="..."
        for m in re.finditer(r'<a\s+(?:id|name)=["\']([^"\']+)["\']', line, re.IGNORECASE):
            explicit_anchors.add(m.group(1).strip())

        # Extract markdown headings: #...
        m_head = re.match(r'^(#{1,6})\s+(.+)$', line)
        if m_head:
            level = len(m_head.group(1))
            raw_title = m_head.group(2).strip()
            base_slug = generate_gfm_slug(raw_title)
            if not base_slug:
                continue

            count = slug_counts.get(base_slug, 0)
            if count == 0:
                final_slug = base_slug
            else:
                final_slug = f"{base_slug}-{count}"
            slug_counts[base_slug] = count + 1

            headings.append((level, raw_title, final_slug))

    return headings, explicit_anchors


def extract_all_toc_links(content: str) -> List[Tuple[str, str, int]]:
    """
    Extracts all (anchor_text, slug, line_number) within the Table of Contents section.
    """
    lines = content.splitlines()
    in_toc = False
    in_fence = False
    fence_char = ''
    fence_len = 0
    toc_links: List[Tuple[str, str, int]] = []

    for idx, line in enumerate(lines, 1):
        trimmed = line.strip()
        m_fence = re.match(r'^(?:\s{0,3})(`{3,}|~{3,})(.*)$', line)
        if not in_fence:
            if m_fence:
                in_fence = True
                fence_char = m_fence.group(1)[0]
                fence_len = len(m_fence.group(1))
                continue
        else:
            if m_fence and m_fence.group(1)[0] == fence_char and len(m_fence.group(1)) >= fence_len and not m_fence.group(2).strip():
                in_fence = False
            continue

        if (re.search(r'Table of Contents|Contents|TOC', trimmed, re.IGNORECASE) and trimmed.startswith('#')) or 'id="table-of-contents"' in trimmed.lower():
            in_toc = True
            continue

        if in_toc:
            if trimmed.startswith("---") and len(toc_links) >= 3:
                break
            if re.match(r'^#{1,2}\s+', trimmed) and not re.search(r'Table of Contents|Contents|TOC', trimmed, re.IGNORECASE):
                break

            matches = re.finditer(r'\[([^\]]+)\]\(#([^\)]+)\)', trimmed)
            for m in matches:
                text = m.group(1).strip()
                slug = unquote(m.group(2)).strip()
                toc_links.append((text, slug, idx))

    return toc_links


def extract_commonmark_code_blocks(content: str) -> List[Tuple[str, str, int, int]]:
    """
    Extracts code blocks following CommonMark fence rules (supporting nested 4+ backtick fences).
    Returns list of (info_string, code_content, start_line, end_line).
    """
    lines = content.splitlines()
    in_fence = False
    fence_char = ''
    fence_len = 0
    fence_start = 0
    fence_info = ''
    current_lines: List[str] = []
    blocks: List[Tuple[str, str, int, int]] = []

    for idx, line in enumerate(lines, 1):
        m = re.match(r'^(?:\s{0,3})(`{3,}|~{3,})(.*)$', line)
        if not in_fence:
            if m:
                in_fence = True
                fence_char = m.group(1)[0]
                fence_len = len(m.group(1))
                fence_info = m.group(2).strip()
                fence_start = idx
                current_lines = []
        else:
            if m and m.group(1)[0] == fence_char and len(m.group(1)) >= fence_len and not m.group(2).strip():
                in_fence = False
                blocks.append((fence_info, "\n".join(current_lines), fence_start, idx))
            else:
                current_lines.append(line)

    return blocks


def validate_mermaid_syntax(diagram: str) -> List[str]:
    """
    Deep grammar validation of Mermaid diagrams:
      - Valid diagram type declaration
      - Valid flowchart orientation (TD, TB, BT, RL, LR)
      - Balanced brackets: (), [], {}, [()]
      - Balanced subgraphs: 'subgraph' vs 'end'
      - Valid classDef and class directives
      - Sequence diagram actor/participant definitions and arrows
    """
    errors: List[str] = []
    lines = [l.strip() for l in diagram.strip().splitlines() if l.strip() and not l.strip().startswith("%%")]
    if not lines:
        return ["Empty mermaid diagram"]

    first_line = lines[0]
    valid_types = [
        "graph", "flowchart", "sequencediagram", "classdiagram", "statediagram",
        "statediagram-v2", "erdiagram", "journey", "gantt", "pie", "requirementdiagram",
        "gitgraph", "c4context", "mindmap", "timeline"
    ]
    diag_type = first_line.split()[0].lower()
    if diag_type not in valid_types:
        errors.append(f"Unknown mermaid diagram type '{diag_type}' on line: '{first_line}'")

    if diag_type in ["graph", "flowchart"]:
        parts = first_line.split()
        if len(parts) >= 2:
            direction = parts[1].upper()
            if direction not in ["TD", "TB", "BT", "RL", "LR"]:
                errors.append(f"Invalid flowchart direction '{direction}' in '{first_line}'")
        else:
            errors.append(f"Missing direction in flowchart header '{first_line}'")

        subgraphs = []
        for line_no, l in enumerate(lines[1:], 2):
            if l.startswith("subgraph"):
                subgraphs.append(l)
            elif l == "end":
                if not subgraphs:
                    errors.append(f"'end' without matching 'subgraph' on line {line_no}")
                else:
                    subgraphs.pop()
            elif l.startswith("direction"):
                d = l.split()[1] if len(l.split()) > 1 else ""
                if d not in ["TB", "TD", "BT", "LR", "RL"]:
                    errors.append(f"Invalid direction '{d}' on line {line_no}")
            elif l.startswith("classDef"):
                if not re.match(r'classDef\s+\w+\s+[^;]+;?', l):
                    errors.append(f"Invalid classDef syntax on line {line_no}: {l}")
            elif l.startswith("class "):
                if not re.match(r'class\s+[\w,]+\s+\w+;?', l):
                    errors.append(f"Invalid class syntax on line {line_no}: {l}")
            else:
                no_quotes = re.sub(r'"[^"]*"', '', l)
                for open_b, close_b in [('(', ')'), ('[', ']'), ('{', '}')]:
                    if no_quotes.count(open_b) != no_quotes.count(close_b):
                        errors.append(f"Unbalanced '{open_b}{close_b}' on line {line_no}: {l}")

        if subgraphs:
            errors.append(f"Unclosed subgraphs at EOF: {subgraphs}")

    elif diag_type == "sequencediagram":
        for line_no, l in enumerate(lines[1:], 2):
            if l == "autonumber":
                continue
            if l.startswith("actor") or l.startswith("participant"):
                if not re.match(r'(?:actor|participant)\s+(\w+)\s*(?:as\s+.+)?', l):
                    errors.append(f"Invalid participant/actor definition on line {line_no}: {l}")
            elif l.startswith("Note over"):
                if not re.match(r'Note\s+over\s+[^:]+:.+', l):
                    errors.append(f"Invalid Note definition on line {line_no}: {l}")
            elif any(arrow in l for arrow in ["->>", "-->>", "->", "-->"]):
                if not re.match(r'(\w+)\s*(?:->>|-->>|->|-->)\s*(\w+)\s*:\s*.+', l):
                    errors.append(f"Invalid message sequence on line {line_no}: {l}")

    return errors


class TestDocsAdversarial(unittest.TestCase):
    """Adversarial stress test suite for documentation invariants."""

    def test_adv_all_files_exist_and_sizable(self):
        """Stress verify that all 3 files exist and have substantial content (>300 lines each)."""
        for doc_name, file_path in DOC_FILES.items():
            with self.subTest(file=doc_name):
                self.assertTrue(file_path.exists(), f"File does not exist: {file_path}")
                content = file_path.read_text(encoding="utf-8")
                lines = content.splitlines()
                self.assertGreaterEqual(
                    len(lines), 300,
                    f"{doc_name} is too short ({len(lines)} lines). Expected >= 300 lines."
                )

    def test_adv_commonmark_code_fences_closure(self):
        """
        Verify no document has unclosed markdown code fences according to CommonMark spec.
        Accurately handles nested fences (e.g. ````markdown containing ``` blocks).
        """
        for doc_name, file_path in DOC_FILES.items():
            with self.subTest(file=doc_name):
                content = file_path.read_text(encoding="utf-8")
                lines = content.splitlines()
                in_fence = False
                fence_char = ''
                fence_len = 0
                for line in lines:
                    m = re.match(r'^(?:\s{0,3})(`{3,}|~{3,})(.*)$', line)
                    if not in_fence:
                        if m:
                            in_fence = True
                            fence_char = m.group(1)[0]
                            fence_len = len(m.group(1))
                    else:
                        if m and m.group(1)[0] == fence_char and len(m.group(1)) >= fence_len and not m.group(2).strip():
                            in_fence = False
                self.assertFalse(
                    in_fence,
                    f"{doc_name} has an unclosed code fence at EOF!"
                )

    def test_adv_toc_link_resolution_all_documents(self):
        """
        Adversarial check: EVERY document that defines a TOC must have 100% resolvable anchor links.
        Validates ARCHITECTURE.md, AGENTS.md, AND GEMINI.md.
        """
        for doc_name, file_path in DOC_FILES.items():
            with self.subTest(file=doc_name):
                content = file_path.read_text(encoding="utf-8")
                headings, explicit_anchors = parse_headings_and_anchors(content)
                heading_slugs = {slug for _, _, slug in headings}
                all_valid_anchors = heading_slugs | explicit_anchors

                toc_links = extract_all_toc_links(content)
                self.assertGreaterEqual(
                    len(toc_links), 5,
                    f"{doc_name} should have at least 5 TOC links. Found {len(toc_links)}."
                )

                broken_links = []
                for text, slug, line_num in toc_links:
                    if slug not in all_valid_anchors:
                        broken_links.append((line_num, text, slug))

                self.assertEqual(
                    len(broken_links), 0,
                    f"Found {len(broken_links)} broken TOC links in {doc_name}:\n"
                    + "\n".join(f"  Line {ln}: [{txt}](#{sl}) -> Anchor '#{sl}' not found in document" for ln, txt, sl in broken_links)
                    + f"\nTotal valid headings/anchors: {len(all_valid_anchors)}"
                )

    def test_adv_all_internal_anchor_links_in_body(self):
        """
        Verify that EVERY internal markdown link [text](#anchor) anywhere in the body
        of ARCHITECTURE.md, AGENTS.md, and GEMINI.md resolves to an existing heading or explicit anchor.
        """
        for doc_name, file_path in DOC_FILES.items():
            with self.subTest(file=doc_name):
                content = file_path.read_text(encoding="utf-8")
                headings, explicit_anchors = parse_headings_and_anchors(content)
                all_valid_anchors = {slug for _, _, slug in headings} | explicit_anchors

                lines = content.splitlines()
                in_fence = False
                fence_char = ''
                fence_len = 0
                broken_links = []

                for idx, line in enumerate(lines, 1):
                    m_fence = re.match(r'^(?:\s{0,3})(`{3,}|~{3,})(.*)$', line)
                    if not in_fence:
                        if m_fence:
                            in_fence = True
                            fence_char = m_fence.group(1)[0]
                            fence_len = len(m_fence.group(1))
                            continue
                    else:
                        if m_fence and m_fence.group(1)[0] == fence_char and len(m_fence.group(1)) >= fence_len and not m_fence.group(2).strip():
                            in_fence = False
                        continue

                    for m in re.finditer(r'\[([^\]]+)\]\(#([^\)]+)\)', line):
                        text = m.group(1).strip()
                        slug = unquote(m.group(2)).strip()
                        if slug and slug not in all_valid_anchors:
                            broken_links.append((idx, text, slug))

                self.assertEqual(
                    len(broken_links), 0,
                    f"Found {len(broken_links)} broken internal anchor links in {doc_name}:\n"
                    + "\n".join(f"  Line {ln}: [{txt}](#{sl})" for ln, txt, sl in broken_links)
                )

    def test_adv_json_code_blocks_validity(self):
        """
        Verifies all ```json blocks across ARCHITECTURE.md, AGENTS.md, and GEMINI.md
        are strictly valid JSON parseable by standard library json.loads.
        """
        total_json_tested = 0
        for doc_name, file_path in DOC_FILES.items():
            content = file_path.read_text(encoding="utf-8")
            blocks = extract_commonmark_code_blocks(content)
            json_blocks = [(code, s) for info, code, s, _ in blocks if info.lower() == "json"]

            for code, s in json_blocks:
                total_json_tested += 1
                with self.subTest(file=doc_name, line=s):
                    try:
                        parsed = json.loads(code)
                        self.assertIsNotNone(parsed)
                    except json.JSONDecodeError as e:
                        self.fail(
                            f"Strict JSON parsing failed in {doc_name} at line {s}:\n"
                            f"Error: {e}\n"
                            f"Snippet:\n{code[:300]}..."
                        )

        self.assertGreaterEqual(
            total_json_tested, 5,
            f"Expected at least 5 JSON blocks across the documentation suite. Found {total_json_tested}."
        )

    def test_adv_mermaid_diagrams_deep_syntax(self):
        """
        Deep validation of all ```mermaid diagrams across ARCHITECTURE.md, AGENTS.md, and GEMINI.md:
        Checks flowcharts, sequence diagrams, subgraphs, direction statements, node declarations,
        and bracket matching.
        """
        total_mermaid_blocks = 0
        for doc_name, file_path in DOC_FILES.items():
            content = file_path.read_text(encoding="utf-8")
            blocks = extract_commonmark_code_blocks(content)
            mermaid_blocks = [(code, s) for info, code, s, _ in blocks if info.lower() == "mermaid"]

            for code, s in mermaid_blocks:
                total_mermaid_blocks += 1
                with self.subTest(file=doc_name, line=s):
                    errors = validate_mermaid_syntax(code)
                    self.assertEqual(
                        len(errors), 0,
                        f"Mermaid syntax error in {doc_name} at line {s}:\n"
                        + "\n".join(f"  - {err}" for err in errors)
                        + f"\nDiagram text:\n{code[:300]}"
                    )

        self.assertGreaterEqual(
            total_mermaid_blocks, 3,
            f"Expected at least 3 Mermaid diagrams across documentation. Found {total_mermaid_blocks}."
        )

    def test_adv_internal_cross_document_links(self):
        """
        Verify that all cross-references between the 3 files (e.g. [text](AGENTS.md#anchor)
        or [text](ARCHITECTURE.md#anchor)) point to valid files and valid anchors.
        """
        link_pattern = r'\[([^\]]+)\]\((ARCHITECTURE\.md|AGENTS\.md|GEMINI\.md)(#[^\)]+)?\)'

        for doc_name, file_path in DOC_FILES.items():
            content = file_path.read_text(encoding="utf-8")
            lines = content.splitlines()
            in_fence = False
            fence_char = ''
            fence_len = 0

            for idx, line in enumerate(lines, 1):
                m_fence = re.match(r'^(?:\s{0,3})(`{3,}|~{3,})(.*)$', line)
                if not in_fence:
                    if m_fence:
                        in_fence = True
                        fence_char = m_fence.group(1)[0]
                        fence_len = len(m_fence.group(1))
                        continue
                else:
                    if m_fence and m_fence.group(1)[0] == fence_char and len(m_fence.group(1)) >= fence_len and not m_fence.group(2).strip():
                        in_fence = False
                    continue

                for match in re.finditer(link_pattern, line):
                    text, target_file, anchor = match.groups()
                    target_path = ROOT_DIR / target_file
                    self.assertTrue(
                        target_path.exists(),
                        f"Broken file link in {doc_name}:{idx} -> {target_file}"
                    )
                    if anchor:
                        clean_slug = anchor.lstrip("#")
                        target_content = target_path.read_text(encoding="utf-8")
                        target_headings, target_explicit = parse_headings_and_anchors(target_content)
                        target_anchors = {slug for _, _, slug in target_headings} | target_explicit
                        self.assertIn(
                            clean_slug, target_anchors,
                            f"Broken cross-doc anchor in {doc_name}:{idx} -> [{text}]({target_file}{anchor}): "
                            f"Anchor '{clean_slug}' not found in {target_file}"
                        )

    def test_adv_gfm_heading_slug_uniqueness(self):
        """Verify that all heading slugs are properly disambiguated without collisions."""
        for doc_name, file_path in DOC_FILES.items():
            with self.subTest(file=doc_name):
                content = file_path.read_text(encoding="utf-8")
                headings, _ = parse_headings_and_anchors(content)
                slugs = [slug for _, _, slug in headings]
                unique_slugs = set(slugs)
                self.assertEqual(
                    len(slugs), len(unique_slugs),
                    f"{doc_name} has {len(slugs) - len(unique_slugs)} heading slug collision(s)!"
                )

    def test_adv_table_formatting_integrity(self):
        """Verify that all markdown tables have matching column counts in header and separator lines."""
        for doc_name, file_path in DOC_FILES.items():
            content = file_path.read_text(encoding="utf-8")
            lines = content.splitlines()
            in_fence = False
            fence_char = ''
            fence_len = 0
            for idx, line in enumerate(lines, 1):
                m_fence = re.match(r'^(?:\s{0,3})(`{3,}|~{3,})(.*)$', line)
                if not in_fence:
                    if m_fence:
                        in_fence = True
                        fence_char = m_fence.group(1)[0]
                        fence_len = len(m_fence.group(1))
                        continue
                else:
                    if m_fence and m_fence.group(1)[0] == fence_char and len(m_fence.group(1)) >= fence_len and not m_fence.group(2).strip():
                        in_fence = False
                    continue

                if re.match(r'^\s*\|?\s*:?-+:?\s*(\|\s*:?-+:?\s*)+\|?\s*$', line):
                    prev_line = lines[idx - 2].strip() if idx >= 2 else ""
                    if prev_line and "|" in prev_line:
                        prev_cols = [c for c in prev_line.split("|") if c.strip() != ""]
                        sep_cols = [c for c in line.split("|") if c.strip() != ""]
                        self.assertEqual(
                            len(prev_cols), len(sep_cols),
                            f"Table column mismatch in {doc_name} at line {idx}:\n"
                            f"Header ({len(prev_cols)} cols): {prev_line}\n"
                            f"Separator ({len(sep_cols)} cols): {line}"
                        )

    def test_adv_major_headings_indexed_in_toc(self):
        """
        Verify that 100% of major H2 and H3 sections (excluding the document title
        and the Table of Contents header itself) are indexed in the Table of Contents.
        """
        for doc_name, file_path in DOC_FILES.items():
            with self.subTest(file=doc_name):
                content = file_path.read_text(encoding="utf-8")
                tocs = extract_all_toc_links(content)
                toc_slugs = {s for _, s, _ in tocs}
                headings, _ = parse_headings_and_anchors(content)

                missing = [
                    (lvl, title, s) for lvl, title, s in headings
                    if lvl in [2, 3]
                    and not re.search(r'Table of Contents|Contents|TOC', title, re.IGNORECASE)
                    and s not in toc_slugs
                ]
                self.assertEqual(
                    len(missing), 0,
                    f"{doc_name} has {len(missing)} major H2/H3 headings missing from its Table of Contents:\n"
                    + "\n".join(f"  Level {lvl}: '{title}' (#{s})" for lvl, title, s in missing)
                )


if __name__ == "__main__":
    unittest.main(verbosity=2)
