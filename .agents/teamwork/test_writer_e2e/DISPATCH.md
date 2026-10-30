# Dispatch for E2E Test Writer

## Identity
- Role: E2E Test Suite Creator
- Type: teamwork_test_writer
- Working directory: c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\test_writer_e2e\

## Task Objective
Design and implement an automated, opaque-box E2E test suite that validates all acceptance criteria from `c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\ORIGINAL_REQUEST.md` for the deliverables: `ARCHITECTURE.md`, `AGENTS.md`, and `GEMINI.md`.

## Deliverables
1. `c:\Users\parse-it\Documents\Git\AppService\TEST_INFRA.md` describing the test methodology (Tiers 1-4) and coverage matrix.
2. An executable Python test script, e.g. `c:\Users\parse-it\Documents\Git\AppService\tests\test_docs_e2e.py`, that verifies:
   - File existence of `ARCHITECTURE.md`, `AGENTS.md`, and `GEMINI.md` in `c:\Users\parse-it\Documents\Git\AppService\`.
   - **TOC Anchor Integrity**: Parses the Table of Contents in `ARCHITECTURE.md` (and any other files with TOCs), extracts all `[Anchor Text](#slug)` links, and verifies that every single slug matches an actual heading slug in the document, and verifies no broken links exist.
   - **Hexagonal Architecture Concepts**: Verifies presence of Core Domain / Entities, Primary/Driving Ports, Secondary/Driven Ports, Driving & Driven Adapters, and the Dependency Inversion Rule.
   - **Visual Elements in ARCHITECTURE.md**: Verifies presence of valid Mermaid diagrams (````mermaid ... ````), Markdown callouts (`> [!NOTE]`, `> [!TIP]`, `> [!WARNING]`, etc.), and badges/emojis.
   - **General Guide Design**: Verifies document is general software architecture guide, not repository-specific.
   - **AGENTS.md Requirements**: Verifies presence of AI agent best practices, execution lifecycle, tool discipline, minimal diff rule, verification ladders, safety guardrails, and checklists.
   - **GEMINI.md Requirements**: Verifies Gemini-specific model guidance (1.5 Pro, 1.5 Flash, 2.0 Flash/Thinking), context caching, environment configurations, system instructions, and tool calling/MCP.
   - Tiered test structure (Tier 1: Feature presence, Tier 2: Boundary/formatting/link exactness, Tier 3: Cross-referencing, Tier 4: Real-world completeness).
3. Execute the test script using `python tests\test_docs_e2e.py` (it may initially fail if files are not yet created, which verifies Red-Green-Refactor).
4. `c:\Users\parse-it\Documents\Git\AppService\TEST_READY.md` containing the runner command, expected exit code (0), and coverage summary.
5. Write your report to `c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\test_writer_e2e\handoff.md`.


## 2026-10-29T21:28:20Z
You are the E2E Test Suite Creator (teamwork_test_writer).
Your working directory is: c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\test_writer_e2e\
Read your dispatch at: c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\test_writer_e2e\DISPATCH.md
Read the user request at: c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\ORIGINAL_REQUEST.md

Design and implement an automated opaque-box E2E test suite in Python:
1. Create c:\Users\parse-it\Documents\Git\AppService\TEST_INFRA.md
2. Create c:\Users\parse-it\Documents\Git\AppService\tests\test_docs_e2e.py testing all acceptance criteria:
   - File existence of ARCHITECTURE.md, AGENTS.md, GEMINI.md
   - Table of Contents integrity: verify every [Text](#slug) link matches an actual header in ARCHITECTURE.md, AGENTS.md, GEMINI.md
   - Hexagonal concepts presence (Core Domain, Primary Ports, Secondary Ports, Adapters, Dependency Inversion)
   - Mermaid diagrams presence
   - Markdown callouts and badges/emojis presence
   - General architectural guide verification
   - AGENTS.md standards and GEMINI.md standards verification
3. Execute the test runner (via terminal python command) to verify initial test behavior.
4. Publish c:\Users\parse-it\Documents\Git\AppService\TEST_READY.md
5. Write your report to c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\test_writer_e2e\handoff.md and send a completion message.
