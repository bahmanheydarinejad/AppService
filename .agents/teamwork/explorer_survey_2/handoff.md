# Handoff Report — Explorer Survey 2: Specification for R2 (AGENTS.md)

**Role**: Explorer Survey 2 (`teamwork_explorer`)  
**Mission**: Investigate and specify comprehensive requirements, architecture, structure, and operational best practices for **R2: Agent Best Practices Guide (`AGENTS.md`)**.  
**Date**: 2026-10-29T21:28:00Z  

---

## 1. Observation

### 1.1 Direct Prompt and Requirement Quotes
From `c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\ORIGINAL_REQUEST.md`:
* **Requirement R2 (Lines 15–16)**:
  > `### R2. Agent Best Practices Guide (AGENTS.md)`  
  > `Create AGENTS.md establishing default best practices for AI agents, including core agent workflows, instructions, project interaction guidelines, and standards for task execution.`
* **Acceptance Criteria for AGENTS.md (Lines 31–34)**:
  > `### AGENTS.md Structure and Content`  
  > `- [ ] AGENTS.md exists in c:\Users\parse-it\Documents\Git\AppService.`  
  > `- [ ] Documents default best practices, behavioral guidelines, and operational standards for AI agents.`

From `c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\explorer_survey_2\DISPATCH.md`:
* **Task Scope & Details (Lines 11–20)**:
  > `1. Core agent workflows, instructions, project interaction guidelines, and standards for task execution.`  
  > `2. Standard AI agent operational best practices:`  
  > `   - Tool calling discipline, error recovery, verification, and non-destructive operations.`  
  > `   - Code modification standards (minimal diffs, formatting preservation, regression prevention).`  
  > `   - Task planning, decomposition, and state tracking.`  
  > `   - Communication protocols, reporting findings, asking clarifying questions.`  
  > `   - Security, safety constraints, avoiding secret leakage, dependency hygiene.`  
  > `3. Industry standards for AGENTS.md (e.g. agent instructions format, system prompts, operational rules).`  
  > `4. Section layout, practical examples, checklists, and guidelines for developer-agent interaction.`

### 1.2 Repository and Environment Context
* **Root Directory**: `c:\Users\parse-it\Documents\Git\AppService`
* **Existing Project Files**:
  - `pom.xml`: Maven build configuration (Java Spring Boot 2.1.4.RELEASE).
  - `src/main`, `src/test`: Java source trees.
  - `.agents/teamwork/`: Contains agent orchestration state, dispatch logs, and explorer folders (`explorer_survey_1`, `explorer_survey_2`, `explorer_survey_3`, `orchestrator`, `sentinel`, `interviewer`).
* **Sibling Tasks & Deliverable Boundaries**:
  - `R1: ARCHITECTURE.md` (Explored by `explorer_survey_1`): General hexagonal architecture guide, ports & adapters, dependency inversion rule, Mermaid diagrams, callout blocks.
  - `R2: AGENTS.md` (This task): Universal, vendor-neutral agent best practices, operational workflows, tool discipline, minimal diffs, verification protocols, security boundaries, and interaction guidelines.
  - `R3: GEMINI.md` (Explored by `explorer_survey_3`): Gemini-specific models, context windows, Antigravity CLI/environment configurations, system instructions, and tool calling parameter conventions.

### 1.3 Open Industry Standards for `AGENTS.md`
* Stewarded under the **Linux Foundation's Agentic AI Foundation (AAIF)**, `AGENTS.md` is the emerging vendor-neutral "README for machines" adopted across leading AI coding tools (OpenAI Codex, Cursor, Google Jules, Claude Code, Aider, OpenHands, GitHub Copilot).
* **Key specification conventions**:
  1. **Plain Markdown**: Standard Markdown (`.md`), free from rigid or vendor-proprietary schema locks.
  2. **Proximity Resolution ("Closest-File-Wins")**: Root-level `AGENTS.md` establishes repository-wide baselines; nested `AGENTS.md` in subdirectories provide localized overrides.
  3. **High-Priority Placement (Primacy Effect)**: Critical operational invariants, safety guardrails, and non-destructive boundaries must reside near the top because LLMs heavily weight initial tokens in the context window.
  4. **Actionable & Command-Oriented**: Concrete execution commands (build, test, lint) over vague prose.

---

## 2. Logic Chain

```
[Observation 1.1: User Request R2 & Acceptance Criteria]
   │
   ├─► Step 1: Establish Document Identity & Delineation
   │     • Differentiate universal standards (AGENTS.md) from vendor-specific configs (GEMINI.md)
   │     • Differentiate operational agent standards (AGENTS.md) from software domain architecture (ARCHITECTURE.md)
   │
   ├─► Step 2: Define the 5-Stage Agent Operational Lifecycle
   │     • Address context drift and hallucination via structured progression:
   │       Reconnaissance ──► Plan & Decompose ──► Surgical Execution ──► Verification ──► Delivery Audit
   │
   ├─► Step 3: Codify Tool Calling Discipline & Operational Safety
   │     • Invariant: "Never execute destructive, unrecoverable, or uninspected commands"
   │     • Targeted search/view over broad full-file dumps
   │     • Idempotency and error diagnosis instead of blind loop retries
   │
   ├─► Step 4: Codify Code Modification Hygiene & The Minimal Diff Principle
   │     • Hunk-based editing over whole-file rewrites to minimize token waste and regression risks
   │     • Strict preservation of existing formatting, architectural boundaries, and naming patterns
   │
   ├─► Step 5: Engineer Deterministic Verification Protocols
   │     • "Claims without execution output are invalid"
   │     • Multi-stage verification: Syntax/Lint ──► Compile/Build ──► Unit Tests ──► Regression Suite
   │     • Invalidation testing (demonstrating pre-fix failure and post-fix pass)
   │
   ├─► Step 6: Standardize Error Recovery & Diagnostic Protocols
   │     • "The Rule of Three": Max 2 failed fix attempts before stepping back to re-evaluate assumptions
   │     • Safe rollback mechanisms ensuring clean repository git state
   │
   ├─► Step 7: Formulate Security, Privacy & Safety Guardrails
   │     • Zero credential leakage, environment variable sanitization, strict workspace boundary enforcement
   │
   └─► Step 8: Design Section Blueprint, Quick-Reference Matrices, and Checklists
         • Produce an authoritative, exhaustive specification for AGENTS.md ready for immediate authoring
```

* **Step 1: Scope Delineation**: `AGENTS.md` must be universally applicable to any AI agent interacting with the codebase (Cursor, Claude, Copilot, Gemini, Aider, custom autonomous loops). Specific runtime parameters or API features unique to Google Gemini models must remain in `GEMINI.md`.
* **Step 2: Mitigating Agent Failure Modes**: Real-world AI coding failures stem from premature editing, context window pollution, excessive modifications (scope creep), lack of independent verification, and unhandled errors. A structured 5-stage lifecycle eliminates these root causes.
* **Step 3: Tool & Command Discipline**: Autonomous agents can unintentionally corrupt repositories via destructive git commands (`git reset --hard`, `git push -f`) or shell operations (`rm -rf`). Explicitly blacklisting destructive commands and establishing read-before-write protocols ensures repository safety.
* **Step 4: Minimal Diff Principle**: AI agents frequently introduce gratuitous diffs (whitespace changes, import reordering, unrelated refactoring). Enforcing surgical, hunk-based modifications preserves git blame history, simplifies human review, and avoids unintended regressions.
* **Step 5: Rigorous Verification**: Agents often report task completion prematurely based solely on absence of tool errors. The specification must require verifiable terminal proof (build and test commands must return exit code 0).
* **Step 6: Diagnostic Discipline**: When tests fail, agents often enter "hallucination loops," randomly tweaking code. Requiring Root-Cause Analysis (RCA) and enforcing the "Rule of Three" breaks failure spirals.

---

## 3. Caveats

1. **Framework-Agnostic vs Project-Specific Commands**:
   - `AppService` is a Java Maven/Spring Boot repository. However, `AGENTS.md` must serve as both a universal Agent Best Practices Guide for software development and provide clear, adaptable command examples (Maven, Gradle, npm, Python uv/poetry, Cargo, Go). The specification provides both universal rules and concrete Java/Maven mappings.
2. **Interactive vs Autonomous Agents**:
   - Some agents operate interactively with a developer in an IDE (e.g. Cursor, Copilot Chat), while others execute asynchronously in background loops (e.g. Devin, SWE-bench runners, subagent orchestrators). The guide must provide operational guidelines applicable to both modes, specifying communication thresholds for both.
3. **Proximity and Inheritance**:
   - While this specification targets the root `AGENTS.md`, it must clearly explain the proximity rule for multi-module or monorepo configurations so sub-packages can extend or override root rules.

---

## 4. Conclusion & Complete Implementation Blueprint for `AGENTS.md`

The future implementer must construct `c:\Users\parse-it\Documents\Git\AppService\AGENTS.md` following the detailed blueprint below. The document must be exhaustive, professional, beautifully formatted, and immediately operational.

### 4.1 Document Architecture & Section Blueprint

```markdown
# AGENTS.md — AI Agent Operational Standards & Best Practices Guide

## 📑 Table of Contents
- [1. Executive Overview & The Agent Prime Directive](#1-executive-overview--the-agent-prime-directive)
- [2. Scope, Precedence & Configuration Hierarchy](#2-scope-precedence--configuration-hierarchy)
- [3. The 5-Stage Agent Execution Lifecycle](#3-the-5-stage-agent-execution-lifecycle)
  - [3.1 Phase 1: Reconnaissance & Context Acquisition](#31-phase-1-reconnaissance--context-acquisition)
  - [3.2 Phase 2: Task Planning & Decomposition](#32-phase-2-task-planning--decomposition)
  - [3.3 Phase 3: Surgical Execution & Minimal Diffs](#33-phase-3-surgical-execution--minimal-diffs)
  - [3.4 Phase 4: Deterministic Verification & Validation](#34-phase-4-deterministic-verification--validation)
  - [3.5 Phase 5: Manifest Audit & Delivery](#35-phase-5-manifest-audit--delivery)
- [4. Tool Calling Discipline & Command Protocol](#4-tool-calling-discipline--command-protocol)
  - [4.1 Read Operations & Search Hierarchy](#41-read-operations--search-hierarchy)
  - [4.2 Write & Modification Operations](#42-write--modification-operations)
  - [4.3 Shell & Command Execution Boundaries](#43-shell--command-execution-boundaries)
  - [4.4 Tool Error Handling & Idempotency](#44-tool-error-handling--idempotency)
- [5. Code Modification Standards & Diff Hygiene](#5-code-modification-standards--diff-hygiene)
  - [5.1 The Minimal Diff Rule](#51-the-minimal-diff-rule)
  - [5.2 Style, Idioms & Architectural Compliance](#52-style-idioms--architectural-compliance)
  - [5.3 Dependency Hygiene & External Libraries](#53-dependency-hygiene--external-libraries)
- [6. Testing, Verification & Quality Assurance](#6-testing-verification--quality-assurance)
  - [6.1 The "Claim vs Proof" Invariant](#61-the-claim-vs-proof-invariant)
  - [6.2 The 4-Tier Verification Pipeline](#62-the-4-tier-verification-pipeline)
  - [6.3 Hypothesis Invalidation Testing](#63-hypothesis-invalidation-testing)
  - [6.4 Project Verification Command Matrix](#64-project-verification-command-matrix)
- [7. Error Recovery, Root-Cause Analysis & Safe Rollback](#7-error-recovery-root-cause-analysis--safe-rollback)
  - [7.1 The Rule of Three (Breaking Churn Loops)](#71-the-rule-of-three-breaking-churn-loops)
  - [7.2 Root-Cause Analysis (RCA) Protocol](#72-root-cause-analysis-rca-protocol)
  - [7.3 Safe Rollback Procedures](#73-safe-rollback-procedures)
- [8. Security, Secret Hygiene & Safety Guardrails](#8-security-secret-hygiene--safety-guardrails)
  - [8.1 Absolute Never-Rules (Hard Guardrails)](#81-absolute-never-rules-hard-guardrails)
  - [8.2 Secret & Credential Leakage Prevention](#82-secret--credential-leakage-prevention)
  - [8.3 Environmental & Network Isolation](#83-environmental--network-isolation)
- [9. Developer-Agent Interaction & Communication Protocols](#9-developer-agent-interaction--communication-protocols)
  - [9.1 Autonomous Action vs Escalation Thresholds](#91-autonomous-action-vs-escalation-thresholds)
  - [9.2 Progress Tracking & State Heartbeats](#92-progress-tracking--state-heartbeats)
  - [9.3 Structured Delivery & Handoff Format](#93-structured-delivery--handoff-format)
- [10. Operational Checklists & Quick Reference](#10-operational-checklists--quick-reference)
  - [10.1 Pre-Flight Checklist](#101-pre-flight-checklist)
  - [10.2 Modification Checklist](#102-modification-checklist)
  - [10.3 Verification & Done Checklist](#103-verification--done-checklist)
```

---

### 4.2 Detailed Section Specifications & Content Guidance

#### Section 1: Executive Overview & The Agent Prime Directive
* **Purpose**: Establish universal behavioral foundation for all AI agents (human-in-the-loop and autonomous).
* **The Agent Prime Directive**:
  > **"First, preserve system integrity. Second, minimize blast radius. Third, prove every assertion with deterministic evidence."**
* **Core Principles**:
  1. *Humility over Assumption*: If uncertain of repository conventions, inspect existing code before inventing new patterns.
  2. *Surgical Precision*: Never touch code outside the immediate scope of the approved task.
  3. *Zero Unverified Claims*: A task is never complete until the test suite passes with exit code 0.

#### Section 2: Scope, Precedence & Configuration Hierarchy
* **The AAIF Open Standard**: Explain compliance with the Linux Foundation Agentic AI Foundation `AGENTS.md` standard.
* **Proximity Rule**: 
  - Root `AGENTS.md` establishes baseline rules for the entire repository.
  - Subdirectory `AGENTS.md` (e.g. `src/modules/billing/AGENTS.md`) overrides or extends root rules for that subtree.
* **Configuration Precedence**:
  1. User Direct Prompt / Session Instruction (highest priority, subject to safety invariants).
  2. Subdirectory `AGENTS.md` (localized domain rules).
  3. Root `AGENTS.md` (general repository standards).
  4. Vendor/Tool Overlays (e.g. `GEMINI.md`, `CLAUDE.md`, `.cursorrules` for platform-specific capabilities).
  5. Default Model Prior (lowest priority).

#### Section 3: The 5-Stage Agent Execution Lifecycle
Provide an end-to-end operational model for task execution:
1. **Stage 1: Reconnaissance & Context Acquisition (Read-Only)**
   - Locate entry points, configuration files (`pom.xml`, build configs, docs).
   - Trace existing architectural patterns before writing a single line.
   - Restrict token usage by reading targeted slices (`view_file` with line bounds) rather than dumping 1,000-line files into context.
2. **Stage 2: Task Planning & Decomposition**
   - For multi-step tasks, generate an explicit, human-readable execution plan.
   - Decompose complex goals into atomic, verifiable milestones.
   - Identify potential failure modes and define rollback points before mutating files.
3. **Stage 3: Surgical Execution (Minimal Diff)**
   - Apply changes via localized chunk replacements (`replace_file_content` or hunk patches).
   - Adhere strictly to existing coding style, indentation, naming conventions, and package structures.
   - Touch only the minimum lines required to fulfill the requirement.
4. **Stage 4: Deterministic Verification & Validation**
   - Execute static analysis/linting.
   - Execute project build/compilation.
   - Run relevant unit tests and integration tests.
   - Perform regression checks across unchanged modules.
5. **Stage 5: Manifest Audit & Delivery**
   - Inspect `git status` / modified file manifest. Verify no unintended files or untracked temporary files exist.
   - Deliver concise, evidence-backed summary of changes, test outputs, and any remaining caveats.

#### Section 4: Tool Calling Discipline & Command Protocol
* **Search & Read Protocol**:
  - Prefer index-based searches (`grep_search` / `ripgrep`, `find_by_name`) with specific queries over recursive directory sweeps.
  - Always bound file reads (`StartLine`, `EndLine`). Avoid reading binary files or bloated bundle/build artifacts (`target/`, `node_modules/`, `.git/`).
* **Write Protocol**:
  - Use atomic replacements (`replace_file_content`) specifying unique `TargetContent`.
  - Avoid whole-file overwrites (`write_to_file` with `Overwrite=true`) unless creating a new file from scratch.
  - Never modify files outside the workspace directory.
* **Shell Command Execution Rules**:
  - Strictly forbidden commands:
    * `rm -rf /`, `rm -rf ~`, or unconstrained recursive deletions.
    * `git reset --hard`, `git clean -fdx`, `git checkout .` (without explicit human approval).
    * `git push --force`, `git push` to protected branches (`main`, `master`).
    * Background processes left unmonitored without timeouts.
    * Arbitrary remote script execution (`curl ... | sh`, `wget ... | bash`).
* **Tool Error Handling & Idempotency**:
  - If a tool fails with syntax/range error, inspect the failure message, re-read the target file lines, and adjust parameters.
  - Never repeat an identical failed tool call more than once.

#### Section 5: Code Modification Standards & Diff Hygiene
* **The Minimal Diff Rule**:
  - Diff line count must be directly proportional to the task. If fixing a 1-line null-pointer bug requires touching 50 lines, the approach is rejected.
  - Do NOT reformat adjacent code, adjust unrelated imports, rename variables out of scope, or alter whitespace conventions.
* **Preserving Existing Architectural Boundaries**:
  - Comply with repository architectural conventions (e.g., Hexagonal Architecture documented in `ARCHITECTURE.md`):
    * Core domain classes must never import adapter/infrastructure classes.
    * Primary/driving ports define application use cases.
    * Secondary/driven ports define outbound dependencies.
* **Dependency Hygiene**:
  - Never introduce new external libraries or update major framework versions in `pom.xml` without explicit user instruction.
  - When dependencies are approved, ensure compatible licenses (MIT, Apache 2.0, BSD) and zero known CVEs.

#### Section 6: Testing, Verification & Quality Assurance
* **The "Claim vs Proof" Invariant**:
  - Statements such as *"The bug is fixed and tests pass"* without corresponding command execution logs are strictly inadmissible.
* **The 4-Tier Verification Ladder**:
  1. *Tier 1: Syntax & Lint*: Verify code formatting, import cleanliness, and absence of syntax errors.
  2. *Tier 2: Build & Compilation*: Verify target builds cleanly with zero errors (`mvn clean compile`).
  3. *Tier 3: Targeted Unit Verification*: Execute test suite for modified classes (`mvn test -Dtest=MyServiceTest`).
  4. *Tier 4: Regression Suite*: Run the full test suite (`mvn test`) to guarantee zero collateral breakage.
* **Hypothesis Invalidation (Red-Green-Refactor)**:
  - When fixing a reported defect, ensure a test exists that fails in the presence of the defect, and passes after applying the surgical fix.
* **Project Command Matrix**: Provide a reference table of exact commands for the repository:
  | Action | Command | Expected Output / Success Criteria |
  |---|---|---|
  | Clean & Compile | `mvn clean compile` | `BUILD SUCCESS`, exit code 0 |
  | Run All Tests | `mvn test` | `BUILD SUCCESS`, 0 failures, 0 errors |
  | Run Single Test Class | `mvn test -Dtest=<TestClassName>` | `BUILD SUCCESS`, exit code 0 |
  | Package JAR | `mvn package -DskipTests` | `BUILD SUCCESS`, generated artifact in `target/` |

#### Section 7: Error Recovery, Root-Cause Analysis & Safe Rollback
* **The Rule of Three**:
  - If an agent's code modification produces a compilation error or test failure, the agent may attempt a diagnostic fix.
  - If the second fix attempt also fails, the agent MUST immediately stop modifying code.
  - Return to Stage 1 (Reconnaissance): re-read stack traces, verify environmental assumptions, or ask the developer for guidance.
* **Root-Cause Analysis (RCA) Protocol**:
  - Never patch the symptom (e.g., wrapping code in broad `try { ... } catch (Exception e) {}`).
  - Trace the exact line of exception, evaluate input parameters, check state invariants, and resolve the underlying root defect.
* **Safe Rollback**:
  - If a task path proves unviable, discard local changes cleanly (`git checkout -- <file>` or restore from backup) before attempting an alternate design.

#### Section 8: Security, Secret Hygiene & Safety Guardrails
* **Absolute Never-Rules**:
  1. Never display, print, echo, or log API keys, database credentials, passwords, JWT secrets, or `.env` files.
  2. Never commit secrets to git.
  3. Never disable security controls (e.g., adding `permitAll()` to Spring Security, disabling TLS certificate checks, or bypassing authentication filters) to make a test pass.
  4. Never place application code or tests into internal agent metadata directories (e.g., `.agents/teamwork/`).
* **Environmental & Workspace Boundaries**:
  - Do not read or write outside the project root (`c:\Users\parse-it\Documents\Git\AppService`).
  - Do not modify user profile files (`~/.bashrc`, `~/.profile`, environment variables).

#### Section 9: Developer-Agent Interaction & Communication Protocols
* **Autonomous Execution vs Escalation**:
  - *Act Autonomously*: Clear requirements, localized bug fixes, adding tests, standard refactoring within established patterns.
  - *Escalate / Ask Clarification*: Ambiguous business logic, conflicting requirements, proposed database schema migrations, introducing third-party dependencies, breaking changes to public APIs.
* **Communication Format**:
  - Concise, factual, structured.
  - Use markdown tables, code blocks with line numbers, and clear status summaries.
  - Always separate observations (verified facts) from interpretations (hypotheses).

#### Section 10: Operational Checklists & Quick Reference
Provide three actionable markdown checklists that human developers and agents can copy/paste:
1. **Pre-Flight Checklist** (Clean git status, dependencies verified, problem reproduced).
2. **Modification Checklist** (Minimal diff verified, style matched, no secrets touched, architectural rules respected).
3. **Verification & Completion Checklist** (Build successful, tests green, `git status` audited, clear summary delivered).

---

## 5. Verification Method

To verify the quality and completeness of this specification and the resulting `AGENTS.md`:

1. **Acceptance Criteria Verification**:
   - Check that `AGENTS.md` is specified to exist directly in the project root: `c:\Users\parse-it\Documents\Git\AppService\AGENTS.md`.
   - Verify that all operational standards (workflows, instructions, project interaction guidelines, task execution standards, tool use discipline, testing/verification, error handling, safety, and formatting conventions) are fully detailed with concrete blueprints.
2. **Boundary & Non-Duplication Check**:
   - Verify `AGENTS.md` does not duplicate Gemini-specific configuration (which belongs in `GEMINI.md`).
   - Verify `AGENTS.md` does not replace `ARCHITECTURE.md` (which covers hexagonal domain architecture), but instead provides cross-references to respect architectural boundaries.
3. **Invalidation Conditions**:
   - The specification would be invalidated if it prescribed destructive tool execution, advocated whole-file overwrites as standard practice, permitted unverified task declarations, or omitted the emerging Linux Foundation AAIF open standard.
