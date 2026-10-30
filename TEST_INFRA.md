# Test Infrastructure & E2E Validation Strategy

## 1. Executive Summary

This document specifies the end-to-end (E2E) testing methodology, architectural test harness design, test tiering hierarchy, and feature coverage matrix for validating the architectural and AI agent documentation deliverables within `AppService`:
- `ARCHITECTURE.md`: General Hexagonal Architecture (Ports and Adapters) Guide and Reference.
- `AGENTS.md`: Universal AI Agent Operational Standards and Best Practices (AAIF-aligned).
- `GEMINI.md`: Gemini Model Family Specific Instructions, Tooling, and Configuration Reference.

The validation harness is designed as an **automated, opaque-box, zero-external-dependency E2E test suite** implemented in Python (`tests/test_docs_e2e.py`) utilizing Python's built-in `unittest` framework. It treats the Markdown deliverables as observable artifacts, asserting their structural integrity, link correctness, technical depth, and requirement conformance without relying on internal generator state.

---

## 2. Test Architecture & Principles

### 2.1 Opaque-Box Validation Principles
1. **Observable Artifact Inspection**: Tests inspect only the published deliverables in the project root (`ARCHITECTURE.md`, `AGENTS.md`, `GEMINI.md`).
2. **Deterministic Expected Output**: All assertions derive directly from `ORIGINAL_REQUEST.md` and `PROJECT.md`.
3. **Zero-Dependency Portability**: Built strictly on the Python standard library (`unittest`, `re`, `pathlib`, `urllib.parse`, `typing`) to run natively on any development machine or CI/CD pipeline without external package installations.
4. **Adversarial & Boundary Verification**: Heading slug parsers and TOC extractors strictly mimic GitHub Flavored Markdown (GFM) anchor generation to catch broken links, trailing whitespace issues, duplicate heading collisions, and non-alphanumeric punctuation stripping.

### 2.2 GitHub Markdown Anchor Slug Algorithm
To ensure 100% link integrity between Table of Contents `[Anchor Text](#slug)` links and Markdown headings, the test suite implements the authoritative GFM slugification rules:
1. Strip leading `#` hashes, blockquotes, and whitespace from the heading line.
2. Strip markdown syntax (bold `**`, italics `*`, inline code `` ` ``, markdown links `[...]`).
3. Convert characters to lowercase.
4. Strip all punctuation characters matching `[^\w\s-]` (including emojis, periods, commas, colons, brackets, quotes, question marks, exclamation marks).
5. Replace whitespace sequences with a single hyphen `-`.
6. Maintain an occurrence counter for duplicate heading slugs (`slug`, `slug-1`, `slug-2`, ...).

---

## 3. Four-Tier Test Verification Hierarchy

The test suite is structured into four progressive tiers:

```
+-----------------------------------------------------------+
| Tier 4: Real-World Completeness & Quality Invariants     |
|         Polyglot patterns, neutrality, copy-paste checklists|
+-----------------------------------------------------------+
| Tier 3: Structural & Conceptual Technical Depth           |
|         Hexagonal concepts, AAIF agent lifecycle, Gemini  |
+-----------------------------------------------------------+
| Tier 2: Syntactic, Formatting & Link Exactness            |
|         TOC anchor validity, Mermaid syntax, callouts     |
+-----------------------------------------------------------+
| Tier 1: Artifact Existence & Basic Non-Emptiness          |
|         File presence, minimum byte counts (>1KB)         |
+-----------------------------------------------------------+
```

### 3.1 Tier 1: Artifact Existence & Basic Non-Emptiness
- **Scope**: Asserts that `ARCHITECTURE.md`, `AGENTS.md`, and `GEMINI.md` exist in the root directory.
- **Invariants**: Files must not only exist but must have substantial length (> 1,000 bytes each) to prevent empty or placeholder files from passing.

### 3.2 Tier 2: Syntactic, Formatting & Link Exactness
- **TOC Anchor Integrity**:
  - Locates the Table of Contents in `ARCHITECTURE.md` (and any other document providing a TOC).
  - Extracts every link of the form `[Link Text](#anchor-slug)`.
  - Parses all document headings (`#`, `##`, `###`, etc.) and generates GFM-compliant slugs.
  - Verifies that every single TOC anchor slug resolves to an existing heading in that document with zero orphan or broken links.
  - Verifies minimum TOC link density (>= 10 entries for a comprehensive guide).
- **Mermaid Diagrams**:
  - Verifies presence of code fences with `mermaid` language identifier.
  - Checks diagram types (`graph`, `flowchart`, `sequenceDiagram`) and directional flow declarations (`TD`, `LR`, `TB`).
  - Verifies boundary and component definitions representing hexagonal layers.
- **Visual Elements & GitHub Callouts**:
  - Verifies presence of standard GitHub alert blockquotes (`> [!NOTE]`, `> [!TIP]`, `> [!WARNING]`, `> [!IMPORTANT]`, `> [!CAUTION]`).
  - Verifies presence of visual cues including badges (`[![...](...)]`) and contextual emojis (e.g., 🏛️, 🔌, 📦, ⚙️, 🛡️, 🚀, 🧪).

### 3.3 Tier 3: Structural & Conceptual Technical Depth
- **Hexagonal Architecture Core Concepts** (`ARCHITECTURE.md`):
  - **Core Domain**: Entities, Value Objects, Domain Services, Domain Events, Ubiquitous Language.
  - **Primary / Driving Ports**: Inbound interfaces, application entry points, use case controllers.
  - **Secondary / Driven Ports**: Outbound interfaces, persistence SPIs, notification interfaces.
  - **Adapters**: Driving adapters (REST, CLI, GraphQL, WebSockets) and Driven adapters (PostgreSQL, Kafka, S3, external HTTP clients).
  - **Dependency Inversion Rule**: Explicit rule that dependencies point inward toward the core domain; domain has zero dependencies on external frameworks or databases.
- **AI Agent Best Practices & Operational Standards** (`AGENTS.md`):
  - **AAIF Standard**: Open standard alignment and agent prime directives.
  - **Execution Lifecycle**: Phased workflow (Explore / Discover -> Plan -> Act / Implement -> Verify / QA).
  - **Tool Calling Discipline**: Search hierarchy (glob -> grep -> view), atomic edits, controlled command execution.
  - **Code Modification & Minimal Diff**: Regression prevention, style preservation, single-change focus.
  - **Verification Ladder**: Claim vs. proof invariant, multi-level verification (compile -> unit -> integration -> e2e).
  - **Error Recovery & Rollback**: Safe git rollback instructions, Root Cause Analysis (RCA), Rule of Three for repeated errors.
  - **Safety Guardrails & Checklists**: Secret hygiene, command safety guardrails, copy-paste operational checklists.
- **Gemini-Specific Model Routing & Features** (`GEMINI.md`):
  - **Model Routing Matrix**: Clear guidance comparing Gemini 1.5 Pro, Gemini 1.5 Flash, and Gemini 2.0 Flash / Thinking, detailing token capacity, reasoning abilities, and target use cases.
  - **Context Window & Caching**: Mechanics for 1M/2M token context windows, Context Caching API (`CachedContent`), cache TTL, context sandwich prompt optimization.
  - **Environment Configuration**: GenerationConfig parameters (`temperature`, `top_p`, `top_k`, `max_output_tokens`), safety settings configuration (`BLOCK_NONE`, threshold overrides).
  - **Tool Calling & MCP**: Function calling conventions, OpenAPI schemas, Model Context Protocol (MCP) server integration.
  - **Prompt Conventions & Multimodal**: System instructions formatting, thinking token budgets, multimodal token considerations.

### 3.4 Tier 4: Real-World Completeness & Quality Invariants
- **General Architecture Guide Neutrality**:
  - Confirms `ARCHITECTURE.md` is an educational, general architectural guide for software developers and not locked to a specific repository or framework.
  - Verifies polyglot implementation patterns and language-agnostic concepts.
  - Checks for architectural anti-patterns and migration guidelines.
- **Operational Actionability**:
  - Asserts AGENTS.md contains actionable, copy-pasteable checklists for pre-flight, implementation, and verification.
- **Cross-Document Cohesion**:
  - Asserts cross-referencing between guides where domain boundaries or agent baselines connect.

---

## 4. Coverage Matrix

| Requirement ID | Project Feature | Target Document | Test Class & Method | Tier | Acceptance Criteria Source |
|---|---|---|---|---|---|
| R1.1 | Feature 19: File Existence | `ARCHITECTURE.md` | `TestDocsE2E.test_tier1_architecture_file_exists` | 1 | ORIGINAL_REQUEST §Acceptance Criteria |
| R2.1 | Feature 19: File Existence | `AGENTS.md` | `TestDocsE2E.test_tier1_agents_file_exists` | 1 | ORIGINAL_REQUEST §Acceptance Criteria |
| R3.1 | Feature 19: File Existence | `GEMINI.md` | `TestDocsE2E.test_tier1_gemini_file_exists` | 1 | ORIGINAL_REQUEST §Acceptance Criteria |
| R1.2 | Feature 1: TOC Integrity | `ARCHITECTURE.md` | `TestDocsE2E.test_tier2_architecture_toc_anchor_integrity` | 2 | ORIGINAL_REQUEST §R1 |
| R2.8 | Feature 1: TOC Integrity | `AGENTS.md` | `TestDocsE2E.test_tier2_agents_toc_anchor_integrity` | 2 | ORIGINAL_REQUEST §R2 |
| R3.7 | Feature 1: TOC Integrity | `GEMINI.md` | `TestDocsE2E.test_tier2_gemini_toc_anchor_integrity` | 2 | ORIGINAL_REQUEST §R3 |
| R1.3 | Feature 5: Mermaid Diagrams | `ARCHITECTURE.md` | `TestDocsE2E.test_tier2_architecture_mermaid_diagrams` | 2 | ORIGINAL_REQUEST §R1 |
| R1.4 | Feature 6: Visual Callouts & Badges | `ARCHITECTURE.md` | `TestDocsE2E.test_tier2_architecture_visual_callouts_and_badges` | 2 | ORIGINAL_REQUEST §R1 |
| R1.5 | Feature 2: Core Domain & Entities | `ARCHITECTURE.md` | `TestDocsE2E.test_tier3_architecture_core_domain_concepts` | 3 | ORIGINAL_REQUEST §R1 |
| R1.6 | Feature 3: Ports & Adapters | `ARCHITECTURE.md` | `TestDocsE2E.test_tier3_architecture_ports_and_adapters` | 3 | ORIGINAL_REQUEST §R1 |
| R1.7 | Feature 4: Dependency Inversion Rule | `ARCHITECTURE.md` | `TestDocsE2E.test_tier3_architecture_dependency_inversion_rule` | 3 | ORIGINAL_REQUEST §R1 |
| R1.8 | Feature 7: General Guide Neutrality | `ARCHITECTURE.md` | `TestDocsE2E.test_tier4_architecture_general_guide_design` | 4 | ORIGINAL_REQUEST §R1 |
| R2.2 | Feature 8: AGENTS Standards & AAIF | `AGENTS.md` | `TestDocsE2E.test_tier3_agents_standards_and_lifecycle` | 3 | ORIGINAL_REQUEST §R2 |
| R2.3 | Feature 9: Tool Calling Discipline | `AGENTS.md` | `TestDocsE2E.test_tier3_agents_tool_calling_discipline` | 3 | ORIGINAL_REQUEST §R2 |
| R2.4 | Feature 10: Minimal Diff & Hygiene | `AGENTS.md` | `TestDocsE2E.test_tier3_agents_minimal_diff_rule` | 3 | ORIGINAL_REQUEST §R2 |
| R2.5 | Feature 11: Verification Ladder & QA | `AGENTS.md` | `TestDocsE2E.test_tier3_agents_verification_ladder` | 3 | ORIGINAL_REQUEST §R2 |
| R2.6 | Feature 12: Error Recovery & Rollback | `AGENTS.md` | `TestDocsE2E.test_tier3_agents_error_recovery_and_rollback` | 3 | ORIGINAL_REQUEST §R2 |
| R2.7 | Feature 13: Safety Guardrails & Checklists | `AGENTS.md` | `TestDocsE2E.test_tier4_agents_safety_and_checklists` | 4 | ORIGINAL_REQUEST §R2 |
| R3.2 | Feature 14: GEMINI Model Routing | `GEMINI.md` | `TestDocsE2E.test_tier3_gemini_model_routing_matrix` | 3 | ORIGINAL_REQUEST §R3 |
| R3.3 | Feature 15: Context Window & Caching | `GEMINI.md` | `TestDocsE2E.test_tier3_gemini_context_caching_mechanics` | 3 | ORIGINAL_REQUEST §R3 |
| R3.4 | Feature 16: Environment & Safety Config | `GEMINI.md` | `TestDocsE2E.test_tier3_gemini_environment_configurations` | 3 | ORIGINAL_REQUEST §R3 |
| R3.5 | Feature 17: Tool Calling & MCP | `GEMINI.md` | `TestDocsE2E.test_tier3_gemini_tool_calling_and_mcp` | 3 | ORIGINAL_REQUEST §R3 |
| R3.6 | Feature 18: Prompt Conventions & System Inst | `GEMINI.md` | `TestDocsE2E.test_tier3_gemini_prompt_conventions` | 3 | ORIGINAL_REQUEST §R3 |
| R-X | Cross-Document Interoperability | All | `TestDocsE2E.test_tier4_cross_document_referencing` | 4 | PROJECT §Interface Contracts |

---

## 5. Execution Protocol & Verification Commands

### 5.1 Prerequisites
- Python 3.8+ (no pip modules required).

### 5.2 Test Execution Command
Run the test suite directly from the project root:
```bash
python tests/test_docs_e2e.py
```
Or with verbose output:
```bash
python -m unittest tests/test_docs_e2e.py -v
```

### 5.3 Exit Codes & Output
- **Exit Code 0**: All acceptance criteria satisfied, all files exist, all TOC anchor links resolve, all architectural & agent requirements verified.
- **Non-zero Exit Code**: One or more assertions failed. Detailed failure messages pinpoint the exact file, missing concept, or broken anchor slug.
