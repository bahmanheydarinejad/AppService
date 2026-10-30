# BRIEFING — 2026-10-29T21:30:00Z

## Mission
Investigate and specify comprehensive requirements, architecture, structure, and operational best practices for R2: Agent Best Practices Guide (AGENTS.md).

## 🔒 My Identity
- Archetype: teamwork_explorer
- Roles: explorer, survey
- Working directory: c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\explorer_survey_2\
- Original parent: e8a6ca15-9ae0-4a99-991a-e54d257388f2
- Milestone: Survey R2 - Agent Best Practices Guide (AGENTS.md) specification

## 🔒 Key Constraints
- Read-only investigation — do NOT implement project files directly
- Deliver report to handoff.md in working directory
- Do NOT modify codebase or place source/test/data in .agents/teamwork/
- Never create AGENTS.md or GEMINI.md in .agents/teamwork/

## Current Parent
- Conversation ID: e8a6ca15-9ae0-4a99-991a-e54d257388f2
- Updated: 2026-10-29T21:30:00Z

## Investigation State
- **Explored paths**:
  - `c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\ORIGINAL_REQUEST.md` (R2 requirements, lines 15-16, 31-34)
  - `c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\explorer_survey_2\DISPATCH.md` (Role scope, lines 11-20)
  - `c:\Users\parse-it\Documents\Git\AppService\pom.xml` (Maven / Spring Boot project context)
  - Open industry standards for `AGENTS.md` (Linux Foundation / AAIF, Cursor, Claude Code, OpenAI Codex, Aider)
- **Key findings**:
  - `AGENTS.md` must be a vendor-neutral, command-first "README for machines" adopting plain markdown, proximity resolution ("closest-file-wins"), and high-priority guardrail placement.
  - Specified a complete 5-stage task lifecycle: Reconnaissance -> Plan & Decompose -> Surgical Execution -> Deterministic Verification -> Delivery Audit.
  - Formulated explicit tool calling boundaries, forbidding destructive commands (`rm -rf`, `git reset --hard`, `git push -f`) and whole-file overwrites in favor of localized chunk edits.
  - Codified the "Minimal Diff Rule", style preservation, and dependency hygiene.
  - Established a 4-tier verification ladder (Lint -> Compile -> Unit Tests -> Regression Suite) enforcing the invariant: "Claims without execution output are invalid."
  - Formalized diagnostic error recovery via "The Rule of Three" to break hallucination loops.
  - Outlined a 10-section operational blueprint with actionable checklists and command matrix for the future writer/implementer.
- **Unexplored areas**: None for R2 survey; comprehensive blueprint complete and ready for execution phase.

## Key Decisions Made
- Structured `handoff.md` with complete 5-component protocol (Observation, Logic Chain, Caveats, Conclusion, Verification Method).
- Strictly separated universal agent rules (`AGENTS.md`) from Gemini-specific configuration (`GEMINI.md`) and architectural patterns (`ARCHITECTURE.md`).
- Documented Java/Maven command reference while maintaining general language/framework applicability.

## Artifact Index
- `c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\explorer_survey_2\BRIEFING.md` — Persistent working memory
- `c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\explorer_survey_2\progress.md` — Liveness heartbeat and progress tracking
- `c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\explorer_survey_2\handoff.md` — Final 5-component handoff report and AGENTS.md implementation blueprint
