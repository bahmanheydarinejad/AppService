#!/usr/bin/env python3
"""
tests/test_docs_e2e.py

Automated Opaque-Box E2E Test Suite for:
  - ARCHITECTURE.md (Hexagonal Architecture Guide)
  - AGENTS.md (Universal AI Agent Guidelines & Standards)
  - GEMINI.md (Gemini Model Family Specific Instructions)

Validates all acceptance criteria from ORIGINAL_REQUEST.md and PROJECT.md:
  - Tier 1: Artifact Existence & Basic Non-Emptiness
  - Tier 2: Syntactic, Formatting, Anchor Link Integrity & Visuals
  - Tier 3: Structural & Conceptual Technical Depth
  - Tier 4: Real-World Completeness, Quality Invariants & Cross-Referencing

Uses strictly Python standard library modules (unittest, pathlib, re, urllib.parse).
"""

import os
import re
import unittest
from pathlib import Path
from typing import Dict, List, Set, Tuple
from urllib.parse import unquote


ROOT_DIR = Path(__file__).resolve().parent.parent
ARCH_FILE = ROOT_DIR / "ARCHITECTURE.md"
AGENTS_FILE = ROOT_DIR / "AGENTS.md"
GEMINI_FILE = ROOT_DIR / "GEMINI.md"


def generate_gfm_slug(heading_text: str) -> str:
    """
    Generates a GitHub-Flavored Markdown (GFM) anchor slug from raw heading text.
    Rules:
      1. Strip markdown links [text](url) -> text, inline code `code` -> code, formatting bold/italic.
      2. Downcase text.
      3. Strip emojis and non-alphanumeric/non-space/non-hyphen characters.
      4. Convert spaces and consecutive whitespace to a single hyphen.
      5. Strip leading/trailing hyphens.
    """
    # 1. Strip markdown images and links: ![alt](url) -> alt, [text](url) -> text
    cleaned = re.sub(r'!\[([^\]]*)\]\([^)]*\)', r'\1', heading_text)
    cleaned = re.sub(r'\[([^\]]*)\]\([^)]*\)', r'\1', cleaned)
    # Strip inline code formatting `foo` -> foo
    cleaned = re.sub(r'`([^`]+)`', r'\1', cleaned)
    # Strip bold / italics / strikethrough: **foo**, *foo*, __foo__, _foo_, ~~foo~~
    cleaned = re.sub(r'[*_~]', '', cleaned)
    # 2. Downcase
    cleaned = cleaned.lower()
    # 3. Strip HTML tags if any <span ...>...</span> -> ...
    cleaned = re.sub(r'<[^>]+>', '', cleaned)
    # 4. Remove all characters except alphanumeric, whitespace, and hyphens (GFM rule)
    cleaned = re.sub(r'[^\w\s-]', '', cleaned)
    # 5. Convert whitespace sequences to a single hyphen
    cleaned = re.sub(r'[\s]+', '-', cleaned)
    # 6. Strip leading and trailing hyphens
    cleaned = cleaned.strip('-')
    return cleaned


def parse_markdown_headings(content: str) -> List[Tuple[int, str, str]]:
    """
    Parses all Markdown headings from content.
    Returns a list of (level, raw_title, slug).
    Handles duplicate heading disambiguation (-1, -2, ...) according to GFM.
    """
    headings: List[Tuple[int, str, str]] = []
    slug_counts: Dict[str, int] = {}
    in_code_fence = False

    for line in content.splitlines():
        trimmed = line.strip()
        # Toggle code block fences
        if trimmed.startswith("```"):
            in_code_fence = not in_code_fence
            continue
        if in_code_fence:
            continue

        match = re.match(r'^(#{1,6})\s+(.+)$', line)
        if match:
            level = len(match.group(1))
            raw_title = match.group(2).strip()
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

    return headings


def extract_explicit_anchors(content: str) -> Set[str]:
    """Extracts explicit HTML anchor targets like <a id="slug"> or <a name="slug">."""
    pattern = r'<a\s+(?:id|name)=["\']([^"\']+)["\']'
    return set(re.findall(pattern, content, re.IGNORECASE))


def extract_toc_links(content: str) -> List[Tuple[str, str]]:
    """
    Extracts all [Anchor Text](#slug) links declared within the Table of Contents section.
    Returns a list of (anchor_text, slug).
    """
    lines = content.splitlines()
    in_toc = False
    in_code_fence = False
    toc_links: List[Tuple[str, str]] = []

    for line in lines:
        trimmed = line.strip()
        if trimmed.startswith("```"):
            in_code_fence = not in_code_fence
            continue
        if in_code_fence:
            continue

        # Detect TOC heading (e.g. ## Table of Contents or ## 📑 Table of Contents or <a id="table-of-contents">)
        if (re.search(r'Table of Contents|Contents|TOC', trimmed, re.IGNORECASE) and trimmed.startswith('#')) or 'id="table-of-contents"' in trimmed.lower():
            in_toc = True
            continue

        # If inside TOC, look for termination: next section heading or divider
        if in_toc:
            if trimmed.startswith("---") and len(toc_links) >= 5:
                break
            if re.match(r'^#{1,2}\s+', trimmed) and not re.search(r'Table of Contents|Contents|TOC', trimmed, re.IGNORECASE):
                break

            # Find all markdown anchor links [text](#slug)
            matches = re.findall(r'\[([^\]]+)\]\(#([^\)]+)\)', trimmed)
            for text, slug in matches:
                # Unquote URL encoding if present (e.g. %20)
                clean_slug = unquote(slug).strip()
                toc_links.append((text.strip(), clean_slug))

    return toc_links


def extract_mermaid_blocks(content: str) -> List[str]:
    """Extracts contents of all ```mermaid ... ``` code blocks."""
    pattern = r'```mermaid\s*\n(.*?)\n```'
    return re.findall(pattern, content, re.DOTALL)


def extract_callouts(content: str) -> List[Tuple[str, str]]:
    """Extracts GitHub-style callouts/alerts (> [!NOTE], > [!TIP], etc.)."""
    pattern = r'>\s*\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\](?:\s*\n((?:>.*(?:\n|$))*))?'
    return re.findall(pattern, content, re.IGNORECASE)


class TestDocsE2E(unittest.TestCase):
    """Automated opaque-box end-to-end tests for architectural and agent documentation."""

    # =========================================================================
    # TIER 1: ARTIFACT EXISTENCE & NON-EMPTINESS
    # =========================================================================

    def test_tier1_architecture_file_exists(self):
        """Verify ARCHITECTURE.md exists and is non-empty (>1000 bytes)."""
        self.assertTrue(ARCH_FILE.exists(), f"ARCHITECTURE.md not found at {ARCH_FILE}")
        size = ARCH_FILE.stat().st_size
        self.assertGreaterEqual(
            size, 1000,
            f"ARCHITECTURE.md is too short ({size} bytes). Minimum 1000 bytes expected for a comprehensive guide."
        )

    def test_tier1_agents_file_exists(self):
        """Verify AGENTS.md exists and is non-empty (>1000 bytes)."""
        self.assertTrue(AGENTS_FILE.exists(), f"AGENTS.md not found at {AGENTS_FILE}")
        size = AGENTS_FILE.stat().st_size
        self.assertGreaterEqual(
            size, 1000,
            f"AGENTS.md is too short ({size} bytes). Minimum 1000 bytes expected."
        )

    def test_tier1_gemini_file_exists(self):
        """Verify GEMINI.md exists and is non-empty (>1000 bytes)."""
        self.assertTrue(GEMINI_FILE.exists(), f"GEMINI.md not found at {GEMINI_FILE}")
        size = GEMINI_FILE.stat().st_size
        self.assertGreaterEqual(
            size, 1000,
            f"GEMINI.md is too short ({size} bytes). Minimum 1000 bytes expected."
        )

    # =========================================================================
    # TIER 2: SYNTACTIC, FORMATTING, TOC ANCHORS & VISUAL ELEMENTS
    # =========================================================================

    def _assert_document_toc_anchor_integrity(self, file_path: Path, doc_name: str, min_links: int = 10):
        """Helper to verify Table of Contents link integrity against actual heading slugs."""
        self.assertTrue(file_path.exists(), f"Missing {file_path}")
        content = file_path.read_text(encoding="utf-8")

        headings = parse_markdown_headings(content)
        self.assertGreaterEqual(
            len(headings), 5,
            f"{doc_name} contains only {len(headings)} headings; comprehensive guide required."
        )

        heading_slugs: Set[str] = {slug for _, _, slug in headings}
        explicit_anchors: Set[str] = extract_explicit_anchors(content)
        all_valid_anchors = heading_slugs | explicit_anchors

        toc_links = extract_toc_links(content)
        self.assertGreaterEqual(
            len(toc_links), min_links,
            f"Table of Contents in {doc_name} has only {len(toc_links)} links. Expected >= {min_links}."
        )

        broken_links: List[Tuple[str, str]] = []
        for text, slug in toc_links:
            # Anchor slugs must match generated heading slugs or explicit HTML anchor tags
            if slug not in all_valid_anchors:
                broken_links.append((text, slug))

        self.assertEqual(
            len(broken_links), 0,
            f"Found {len(broken_links)} broken TOC anchor link(s) in {doc_name}:\n"
            + "\n".join(f"  - [{text}](#{slug}) -> Target '#{slug}' does not match any document heading or anchor" for text, slug in broken_links)
            + f"\n\nAvailable heading slugs ({len(heading_slugs)} total):\n  "
            + "\n  ".join(sorted(heading_slugs))
        )

    def test_tier2_architecture_toc_anchor_integrity(self):
        """Verify Table of Contents integrity in ARCHITECTURE.md."""
        self._assert_document_toc_anchor_integrity(ARCH_FILE, "ARCHITECTURE.md", min_links=10)

    def test_tier2_agents_toc_anchor_integrity(self):
        """Verify Table of Contents integrity in AGENTS.md."""
        self._assert_document_toc_anchor_integrity(AGENTS_FILE, "AGENTS.md", min_links=10)

    def test_tier2_gemini_toc_anchor_integrity(self):
        """Verify Table of Contents integrity in GEMINI.md."""
        self._assert_document_toc_anchor_integrity(GEMINI_FILE, "GEMINI.md", min_links=10)

    def test_tier2_architecture_mermaid_diagrams(self):
        """
        Verify ARCHITECTURE.md includes at least one valid Mermaid diagram illustrating
        hexagonal architecture boundaries and data/dependency flow.
        """
        self.assertTrue(ARCH_FILE.exists(), f"Missing {ARCH_FILE}")
        content = ARCH_FILE.read_text(encoding="utf-8")

        mermaid_blocks = extract_mermaid_blocks(content)
        self.assertGreaterEqual(
            len(mermaid_blocks), 1,
            "ARCHITECTURE.md must contain at least one Mermaid diagram (```mermaid ... ```)."
        )

        # Verify that diagrams illustrate hexagonal components / boundaries
        combined_diagrams = "\n".join(mermaid_blocks).lower()
        has_boundaries = any(
            token in combined_diagrams
            for token in ["port", "adapter", "core", "domain", "driving", "driven", "inbound", "outbound"]
        )
        self.assertTrue(
            has_boundaries,
            "Mermaid diagram in ARCHITECTURE.md must illustrate hexagonal boundaries/components "
            "(e.g. Ports, Adapters, Domain Core, Inbound/Outbound flow)."
        )

    def test_tier2_architecture_visual_callouts_and_badges(self):
        """
        Verify ARCHITECTURE.md uses GitHub-style Markdown callouts (> [!NOTE], etc.)
        and visual badges or contextual emojis.
        """
        self.assertTrue(ARCH_FILE.exists(), f"Missing {ARCH_FILE}")
        content = ARCH_FILE.read_text(encoding="utf-8")

        callouts = extract_callouts(content)
        self.assertGreaterEqual(
            len(callouts), 2,
            f"ARCHITECTURE.md should use at least 2 Markdown callouts (> [!NOTE], > [!TIP], > [!WARNING], etc.). Found {len(callouts)}."
        )

        # Check for emojis or badges (Shields.io, etc.)
        has_badges = bool(re.search(r'!\[.*?\]\(https?://img\.shields\.io/.*?\)', content))
        has_emojis = bool(re.search(r'[\U0001F300-\U0001FAFF\u2600-\u26FF\u2700-\u27BF]', content))

        self.assertTrue(
            has_badges or has_emojis,
            "ARCHITECTURE.md must incorporate visual styling elements (badges or emojis) across major sections."
        )

    # =========================================================================
    # TIER 3: STRUCTURAL & CONCEPTUAL TECHNICAL DEPTH
    # =========================================================================

    def test_tier3_architecture_core_domain_concepts(self):
        """
        Verify ARCHITECTURE.md covers Core Domain, Entities, Value Objects,
        Domain Services, and Domain Events.
        """
        self.assertTrue(ARCH_FILE.exists(), f"Missing {ARCH_FILE}")
        content = ARCH_FILE.read_text(encoding="utf-8").lower()

        required_concepts = {
            "core domain / entities": ["core domain", "entity", "entities"],
            "value objects": ["value object", "value objects"],
            "domain services / events": ["domain service", "domain event"],
        }
        missing = []
        for concept_name, tokens in required_concepts.items():
            if not any(token in content for token in tokens):
                missing.append(concept_name)

        self.assertEqual(
            len(missing), 0,
            f"ARCHITECTURE.md is missing critical domain concepts: {', '.join(missing)}"
        )

    def test_tier3_architecture_ports_and_adapters(self):
        """
        Verify ARCHITECTURE.md explicitly covers:
        - Primary / Driving Ports
        - Secondary / Driven Ports
        - Driving / Inbound Adapters
        - Driven / Outbound Adapters
        """
        self.assertTrue(ARCH_FILE.exists(), f"Missing {ARCH_FILE}")
        content = ARCH_FILE.read_text(encoding="utf-8").lower()

        checks = {
            "primary/driving ports": ["primary port", "driving port", "inbound port"],
            "secondary/driven ports": ["secondary port", "driven port", "outbound port"],
            "driving adapters": ["driving adapter", "inbound adapter"],
            "driven adapters": ["driven adapter", "outbound adapter"],
        }
        missing = []
        for layer, terms in checks.items():
            if not any(term in content for term in terms):
                missing.append(layer)

        self.assertEqual(
            len(missing), 0,
            f"ARCHITECTURE.md is missing ports/adapters coverage: {', '.join(missing)}"
        )

    def test_tier3_architecture_dependency_inversion_rule(self):
        """
        Verify ARCHITECTURE.md documents the Dependency Inversion Rule and
        the inward dependency rule (outer layers depend on inner domain; domain has zero outward dependencies).
        """
        self.assertTrue(ARCH_FILE.exists(), f"Missing {ARCH_FILE}")
        content = ARCH_FILE.read_text(encoding="utf-8").lower()

        has_dep_rule = any(
            phrase in content
            for phrase in [
                "dependency inversion",
                "dependency rule",
                "dependencies point inward",
                "inward dependency",
                "inversion of control",
            ]
        )
        self.assertTrue(
            has_dep_rule,
            "ARCHITECTURE.md must document the Dependency Inversion Rule / Inward Dependency Rule."
        )

    def test_tier3_agents_standards_and_lifecycle(self):
        """
        Verify AGENTS.md establishes operational standards, AAIF alignment,
        and agent execution lifecycle (explore, plan, act, verify).
        """
        self.assertTrue(AGENTS_FILE.exists(), f"Missing {AGENTS_FILE}")
        content = AGENTS_FILE.read_text(encoding="utf-8").lower()

        lifecycle_terms = ["lifecycle", "workflow", "plan", "verify", "explore"]
        matched_lifecycle = sum(1 for term in lifecycle_terms if term in content)
        self.assertGreaterEqual(
            matched_lifecycle, 3,
            "AGENTS.md must document core agent lifecycle phases (explore, plan, act, verify)."
        )

        has_standards = any(
            term in content
            for term in ["best practices", "operational standards", "behavioral guidelines", "aaif", "open standard"]
        )
        self.assertTrue(
            has_standards,
            "AGENTS.md must establish agent operational standards / best practices."
        )

    def test_tier3_agents_tool_calling_discipline(self):
        """
        Verify AGENTS.md defines tool calling discipline, search hierarchies,
        and safe shell command boundaries.
        """
        self.assertTrue(AGENTS_FILE.exists(), f"Missing {AGENTS_FILE}")
        content = AGENTS_FILE.read_text(encoding="utf-8").lower()

        has_tool_discipline = any(
            term in content
            for term in ["tool calling", "tool discipline", "search hierarchy", "atomic edit", "tool guidelines"]
        )
        self.assertTrue(
            has_tool_discipline,
            "AGENTS.md must define tool calling discipline and execution guidelines."
        )

    def test_tier3_agents_minimal_diff_rule(self):
        """
        Verify AGENTS.md defines the minimal diff rule and regression prevention practices.
        """
        self.assertTrue(AGENTS_FILE.exists(), f"Missing {AGENTS_FILE}")
        content = AGENTS_FILE.read_text(encoding="utf-8").lower()

        has_diff_rule = any(
            phrase in content
            for phrase in ["minimal diff", "diff hygiene", "scoped edit", "avoid unnecessary changes", "preserve existing"]
        )
        self.assertTrue(
            has_diff_rule,
            "AGENTS.md must document minimal diff rules and change scope preservation."
        )

    def test_tier3_agents_verification_ladder(self):
        """
        Verify AGENTS.md defines the verification ladder and claim-vs-proof invariants.
        """
        self.assertTrue(AGENTS_FILE.exists(), f"Missing {AGENTS_FILE}")
        content = AGENTS_FILE.read_text(encoding="utf-8").lower()

        has_verification = any(
            phrase in content
            for phrase in ["verification ladder", "claim vs proof", "never claim without proof", "verification method", "qa"]
        )
        self.assertTrue(
            has_verification,
            "AGENTS.md must document the verification ladder and evidence-based completion invariant."
        )

    def test_tier3_agents_error_recovery_and_rollback(self):
        """
        Verify AGENTS.md defines error recovery protocols, root cause analysis (RCA),
        and safe git rollback procedures.
        """
        self.assertTrue(AGENTS_FILE.exists(), f"Missing {AGENTS_FILE}")
        content = AGENTS_FILE.read_text(encoding="utf-8").lower()

        has_recovery = any(
            phrase in content
            for phrase in ["error recovery", "rollback", "root cause", "rca", "rule of three"]
        )
        self.assertTrue(
            has_recovery,
            "AGENTS.md must document error recovery protocols and safe rollback strategies."
        )

    def test_tier3_gemini_model_routing_matrix(self):
        """
        Verify GEMINI.md provides a model routing matrix covering:
        - Gemini 1.5 Pro
        - Gemini 1.5 Flash
        - Gemini 2.0 Flash / Thinking
        """
        self.assertTrue(GEMINI_FILE.exists(), f"Missing {GEMINI_FILE}")
        content = GEMINI_FILE.read_text(encoding="utf-8")

        models = ["1.5 Pro", "1.5 Flash"]
        has_2_0 = "2.0" in content or "Thinking" in content
        self.assertTrue(has_2_0, "GEMINI.md must mention Gemini 2.0 (Flash / Thinking).")

        for model in models:
            self.assertIn(
                model, content,
                f"GEMINI.md model routing matrix must reference Gemini {model}."
            )

    def test_tier3_gemini_context_caching_mechanics(self):
        """
        Verify GEMINI.md details context window mechanics (1M/2M tokens)
        and Context Caching API (CachedContent, TTL, context sandwich).
        """
        self.assertTrue(GEMINI_FILE.exists(), f"Missing {GEMINI_FILE}")
        content = GEMINI_FILE.read_text(encoding="utf-8").lower()

        has_cache_concepts = any(
            token in content
            for token in ["context caching", "cachedcontent", "cache ttl", "1m", "2m", "million token"]
        )
        self.assertTrue(
            has_cache_concepts,
            "GEMINI.md must detail context window capacity (1M/2M) and context caching mechanics."
        )

    def test_tier3_gemini_environment_configurations(self):
        """
        Verify GEMINI.md documents environment configurations, GenerationConfig,
        and safety settings overrides.
        """
        self.assertTrue(GEMINI_FILE.exists(), f"Missing {GEMINI_FILE}")
        content = GEMINI_FILE.read_text(encoding="utf-8")

        has_gen_config = any(
            token in content
            for token in ["GenerationConfig", "temperature", "max_output_tokens", "safety_settings", "BLOCK_NONE"]
        )
        self.assertTrue(
            has_gen_config,
            "GEMINI.md must document GenerationConfig, safety settings, or environment configuration parameters."
        )

    def test_tier3_gemini_tool_calling_and_mcp(self):
        """
        Verify GEMINI.md specifies function/tool calling schemas and
        Model Context Protocol (MCP) server integration.
        """
        self.assertTrue(GEMINI_FILE.exists(), f"Missing {GEMINI_FILE}")
        content = GEMINI_FILE.read_text(encoding="utf-8")

        has_tooling = any(
            token in content
            for token in ["MCP", "Model Context Protocol", "Function Calling", "function_declarations", "tool_config"]
        )
        self.assertTrue(
            has_tooling,
            "GEMINI.md must specify tool calling schemas / Model Context Protocol (MCP) integration."
        )

    def test_tier3_gemini_prompt_conventions(self):
        """
        Verify GEMINI.md covers prompt conventions, system instructions,
        and thinking/multimodal considerations.
        """
        self.assertTrue(GEMINI_FILE.exists(), f"Missing {GEMINI_FILE}")
        content = GEMINI_FILE.read_text(encoding="utf-8").lower()

        has_conventions = any(
            token in content
            for token in ["system instruction", "system_instruction", "thinking", "multimodal", "prompt convention"]
        )
        self.assertTrue(
            has_conventions,
            "GEMINI.md must cover system instructions, thinking token guidance, or prompt conventions."
        )

    # =========================================================================
    # TIER 4: REAL-WORLD COMPLETENESS, NEUTRALITY & CROSS-REFERENCING
    # =========================================================================

    def test_tier4_architecture_general_guide_design(self):
        """
        Verify ARCHITECTURE.md is a general software architecture guide
        not tied to a single repository:
        - Covers polyglot/multi-language implementations or general software concepts.
        - Discusses architectural trade-offs, anti-patterns, or migration strategies.
        """
        self.assertTrue(ARCH_FILE.exists(), f"Missing {ARCH_FILE}")
        content = ARCH_FILE.read_text(encoding="utf-8").lower()

        # Should discuss general testing or anti-patterns or trade-offs
        has_general_concepts = any(
            term in content
            for term in ["anti-pattern", "trade-off", "testing strategy", "migration", "polyglot", "best practice"]
        )
        self.assertTrue(
            has_general_concepts,
            "ARCHITECTURE.md must serve as a comprehensive general guide covering trade-offs, anti-patterns, or testing strategies."
        )

    def test_tier4_agents_safety_and_checklists(self):
        """
        Verify AGENTS.md includes actionable copy-paste checklists and safety guardrails.
        """
        self.assertTrue(AGENTS_FILE.exists(), f"Missing {AGENTS_FILE}")
        content = AGENTS_FILE.read_text(encoding="utf-8")

        # Must have markdown checkbox checklists: - [ ]
        checklist_items = re.findall(r'-\s*\[\s*\]\s+(.+)', content)
        self.assertGreaterEqual(
            len(checklist_items), 3,
            f"AGENTS.md must include actionable copy-paste checklists with at least 3 checkbox items. Found {len(checklist_items)}."
        )

    def test_tier4_cross_document_referencing(self):
        """
        Verify cross-referencing and cohesion across ARCHITECTURE.md, AGENTS.md, and GEMINI.md:
        - AGENTS.md or GEMINI.md should cross-reference architectural or agent baselines.
        """
        self.assertTrue(AGENTS_FILE.exists(), f"Missing {AGENTS_FILE}")
        self.assertTrue(GEMINI_FILE.exists(), f"Missing {GEMINI_FILE}")

        agents_content = AGENTS_FILE.read_text(encoding="utf-8")
        gemini_content = GEMINI_FILE.read_text(encoding="utf-8")

        has_cross_ref = (
            "ARCHITECTURE.md" in agents_content
            or "ARCHITECTURE.md" in gemini_content
            or "AGENTS.md" in gemini_content
            or "GEMINI.md" in agents_content
        )
        self.assertTrue(
            has_cross_ref,
            "Documentation suite should exhibit cross-referencing between ARCHITECTURE.md, AGENTS.md, and GEMINI.md."
        )


if __name__ == "__main__":
    unittest.main(verbosity=2)
