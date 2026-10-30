# Project: Hexagonal Architecture Guide and Agent Guidelines (ARCHITECTURE.md, AGENTS.md, GEMINI.md)

## Architecture
- Deliverables are three comprehensive, industry-standard Markdown documents in the project root `c:\Users\parse-it\Documents\Git\AppService`:
  1. `ARCHITECTURE.md`: General Hexagonal Architecture (Ports and Adapters) guide for software developers, complete with TOC, working anchor links, Mermaid diagrams, visual callouts, badges, emojis, polyglot patterns, and testing strategies.
  2. `AGENTS.md`: AI Agent Operational Standards and Best Practices Guide complying with the Linux Foundation AAIF open standard, defining workflows, tool calling discipline, minimal diff rule, verification ladders, error recovery, safety guardrails, and checklists.
  3. `GEMINI.md`: Gemini-specific agent instructions, model routing matrix, context window caching mechanics, environment configurations, system instruction schemas, tool calling/MCP standards, and prompt conventions.

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | ARCHITECTURE.md Table of Contents | Working markdown links referencing all section headings | M1 | ORIGINAL_REQUEST §R1 |
| 2 | Hexagonal Core Concepts | Core domain, entities, value objects, domain services, events | M1 | ORIGINAL_REQUEST §R1 |
| 3 | Ports & Adapters Architecture | Primary/driving vs secondary/driven ports and adapters | M1 | ORIGINAL_REQUEST §R1 |
| 4 | Dependency Inversion Rule | Domain isolation and inward dependency flow | M1 | ORIGINAL_REQUEST §R1 |
| 5 | Mermaid Diagrams | Topology and runtime sequence diagrams for hexagonal flow | M1 | ORIGINAL_REQUEST §R1 |
| 6 | Visual Styling & Badges | Markdown callouts (> [!NOTE], etc.), badges, emojis | M1 | ORIGINAL_REQUEST §R1 |
| 7 | General Guide Design | Polyglot patterns, anti-patterns, migration guide | M1 | ORIGINAL_REQUEST §R1 |
| 8 | AGENTS.md Standards | AAIF-aligned agent standards, prime directive, lifecycle | M2 | ORIGINAL_REQUEST §R2 |
| 9 | Agent Tool Calling Discipline | Search hierarchy, atomic edits, shell command boundaries | M2 | ORIGINAL_REQUEST §R2 |
| 10 | Code Modification & Minimal Diff | Diff hygiene, regression prevention, style preservation | M2 | ORIGINAL_REQUEST §R2 |
| 11 | Verification Ladder & QA | Claim vs proof invariant, 4-tier verification ladder | M2 | ORIGINAL_REQUEST §R2 |
| 12 | Error Recovery & Safe Rollback | Rule of three, RCA protocol, safe git rollback | M2 | ORIGINAL_REQUEST §R2 |
| 13 | Security Guardrails & Checklists | Hard guardrails, secret hygiene, 3 copy-paste checklists | M2 | ORIGINAL_REQUEST §R2 |
| 14 | GEMINI.md Model Routing | Gemini 1.5 Pro/Flash, 2.0 Flash/Thinking model matrix | M3 | ORIGINAL_REQUEST §R3 |
| 15 | Context Window & Caching | 1M/2M mechanics, context sandwich, GenAI CachedContent | M3 | ORIGINAL_REQUEST §R3 |
| 16 | Environment & Safety Config | GenerationConfig, safety settings overrides (BLOCK_NONE) | M3 | ORIGINAL_REQUEST §R3 |
| 17 | Tool Calling & MCP Integration | OpenAPI schemas, execution modes, MCP integration | M3 | ORIGINAL_REQUEST §R3 |
| 18 | Prompt Conventions & Multimodal | System instructions, thinking tokens, multimodal tokens | M3 | ORIGINAL_REQUEST §R3 |
| 19 | Comprehensive Acceptance & E2E Validation | Anchor link validity, file existence, acceptance criteria check | M4 | ORIGINAL_REQUEST Acceptance Criteria |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | Hexagonal Architecture Guide | Create c:\Users\parse-it\Documents\Git\AppService\ARCHITECTURE.md | none | DONE (1,218 lines, 55 TOC links, 3 Mermaid diagrams) |
| 2 | Agent Best Practices Guide | Create c:\Users\parse-it\Documents\Git\AppService\AGENTS.md | none | DONE (485 lines, 45 TOC links, AAIF aligned) |
| 3 | Gemini Agent Instructions | Create c:\Users\parse-it\Documents\Git\AppService\GEMINI.md | none | DONE (812 lines, 43 TOC links, 8 JSON schemas) |
| 4 | Final E2E Verification & Audit | Validate all files, anchor links, acceptance criteria | M1, M2, M3 | DONE (42/42 tests pass, Clean Audit, 4 Approvals) |

## Code Layout
- `c:\Users\parse-it\Documents\Git\AppService\ARCHITECTURE.md` (Hexagonal Architecture Guide - Complete)
- `c:\Users\parse-it\Documents\Git\AppService\AGENTS.md` (Agent Best Practices Guide - Complete)
- `c:\Users\parse-it\Documents\Git\AppService\GEMINI.md` (Gemini Agent Instructions - Complete)
- `c:\Users\parse-it\Documents\Git\AppService\tests\test_docs_e2e.py` (E2E Test Suite - 24 tests passing)
- `c:\Users\parse-it\Documents\Git\AppService\tests\test_docs_adversarial.py` (Adversarial Suite - 10 tests passing)
- `c:\Users\parse-it\Documents\Git\AppService\tests\test_adversarial_challenger2.py` (Challenger 2 Suite - 8 tests passing)
- `c:\Users\parse-it\Documents\Git\AppService\TEST_INFRA.md` & `TEST_READY.md` (E2E Test Architecture - Complete)

## Interface Contracts
- Cross-referencing between AGENTS.md, GEMINI.md, and ARCHITECTURE.md for domain boundary rules and universal agent baselines.
