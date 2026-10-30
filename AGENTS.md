# AGENTS.md — AI Agent Operational Standards and Best Practices Guide

> **Authoritative Specification & Operating Guide for AI Coding Agents**  
> *Compliant with the Linux Foundation Agentic AI Foundation (AAIF) Open Standard for Machine-Readable Repository Instructions (`AGENTS.md`).*

---

## 📑 Table of Contents

- [1. Executive Overview and the Agent Prime Directive](#1-executive-overview-and-the-agent-prime-directive)
  - [1.1 Purpose and Audience](#11-purpose-and-audience)
  - [1.2 The Agent Prime Directive](#12-the-agent-prime-directive)
  - [1.3 Core Operating Principles](#13-core-operating-principles)
- [2. Scope, Precedence and Configuration Hierarchy](#2-scope-precedence-and-configuration-hierarchy)
  - [2.1 The AAIF Open Standard Specification](#21-the-aaif-open-standard-specification)
  - [2.2 Proximity Resolution: Closest-File-Wins](#22-proximity-resolution-closest-file-wins)
  - [2.3 Repository Configuration Precedence Ladder](#23-repository-configuration-precedence-ladder)
  - [2.4 Cross-Document Alignment: AGENTS, GEMINI, and ARCHITECTURE](#24-cross-document-alignment-agents-gemini-and-architecture)
- [3. The 5-Stage Agent Execution Lifecycle](#3-the-5-stage-agent-execution-lifecycle)
  - [3.1 Stage 1: Reconnaissance and Context Acquisition](#31-stage-1-reconnaissance-and-context-acquisition)
  - [3.2 Stage 2: Task Planning and Decomposition](#32-stage-2-task-planning-and-decomposition)
  - [3.3 Stage 3: Surgical Execution and Minimal Diffs](#33-stage-3-surgical-execution-and-minimal-diffs)
  - [3.4 Stage 4: Deterministic Verification and Validation](#34-stage-4-deterministic-verification-and-validation)
  - [3.5 Stage 5: Manifest Audit and Delivery](#35-stage-5-manifest-audit-and-delivery)
- [4. Tool Calling Discipline and Command Protocol](#4-tool-calling-discipline-and-command-protocol)
  - [4.1 Read Operations and Search Hierarchy](#41-read-operations-and-search-hierarchy)
  - [4.2 Write and Modification Operations](#42-write-and-modification-operations)
  - [4.3 Shell and Command Execution Boundaries](#43-shell-and-command-execution-boundaries)
  - [4.4 Tool Error Handling and Idempotency](#44-tool-error-handling-and-idempotency)
- [5. Code Modification Standards and Diff Hygiene](#5-code-modification-standards-and-diff-hygiene)
  - [5.1 The Minimal Diff Rule](#51-the-minimal-diff-rule)
  - [5.2 Style, Idioms and Architectural Compliance](#52-style-idioms-and-architectural-compliance)
  - [5.3 Dependency Hygiene and External Libraries](#53-dependency-hygiene-and-external-libraries)
- [6. Testing, Verification and Quality Assurance](#6-testing-verification-and-quality-assurance)
  - [6.1 The Claim vs Proof Invariant](#61-the-claim-vs-proof-invariant)
  - [6.2 The 4-Tier Verification Ladder](#62-the-4-tier-verification-ladder)
  - [6.3 Hypothesis Invalidation Testing: Red-Green-Refactor](#63-hypothesis-invalidation-testing-red-green-refactor)
  - [6.4 Project Verification Command Matrix](#64-project-verification-command-matrix)
- [7. Error Recovery, Root-Cause Analysis and Safe Rollback](#7-error-recovery-root-cause-analysis-and-safe-rollback)
  - [7.1 The Rule of Three: Breaking Churn Loops](#71-the-rule-of-three-breaking-churn-loops)
  - [7.2 Root-Cause Analysis (RCA) Protocol](#72-root-cause-analysis-rca-protocol)
  - [7.3 Safe Rollback Procedures](#73-safe-rollback-procedures)
- [8. Security, Secret Hygiene and Safety Guardrails](#8-security-secret-hygiene-and-safety-guardrails)
  - [8.1 Absolute Never-Rules: Hard Guardrails](#81-absolute-never-rules-hard-guardrails)
  - [8.2 Secret and Credential Leakage Prevention](#82-secret-and-credential-leakage-prevention)
  - [8.3 Environmental and Network Isolation](#83-environmental-and-network-isolation)
- [9. Developer-Agent Interaction and Communication Protocols](#9-developer-agent-interaction-and-communication-protocols)
  - [9.1 Autonomous Action vs Escalation Thresholds](#91-autonomous-action-vs-escalation-thresholds)
  - [9.2 Progress Tracking and State Heartbeats](#92-progress-tracking-and-state-heartbeats)
  - [9.3 Structured Delivery and Handoff Format](#93-structured-delivery-and-handoff-format)
- [10. Operational Checklists and Quick Reference](#10-operational-checklists-and-quick-reference)
  - [10.1 Pre-Flight Checklist](#101-pre-flight-checklist)
  - [10.2 Modification Checklist](#102-modification-checklist)
  - [10.3 Verification and Done Checklist](#103-verification-and-done-checklist)

---

## 1. Executive Overview and the Agent Prime Directive

### 1.1 Purpose and Audience
This document establishes the binding operational standards, behavioral boundaries, and quality requirements for AI coding agents interacting with this repository. It applies universally to all autonomous, semi-autonomous, and interactive agent runtimes—including but not limited to Cursor, Claude Code, OpenAI Codex, Google Jules/Gemini, GitHub Copilot, Aider, OpenHands, SWE-bench runners, and custom multi-agent orchestration harnesses.

Human developers and agent orchestrators should treat this document as the machine-readable foundation for repository governance, error recovery, and verification rigor.

### 1.2 The Agent Prime Directive
Every agent operating in this codebase must adhere to the single unifying directive:

> **"First, preserve system integrity. Second, minimize blast radius. Third, prove every assertion with deterministic evidence."**

If an action threatens repository stability, touches files outside the declared objective, or cannot be substantiated by verified test executions, that action is strictly forbidden.

### 1.3 Core Operating Principles

| Principle | Meaning & Enforcement |
|:---|:---|
| **Humility over Assumption** | Never invent project patterns, frameworks, or interfaces out of thin air. Always inspect existing codebase conventions, build files, and architectural documentation before generating code. |
| **Surgical Precision** | Restrict diffs exclusively to lines necessary to achieve the task. Unsolicited refactoring, stylistic re-formatting, and "drive-by" cleanup are considered defects. |
| **Claim vs Proof Invariant** | Unverified claims are false by definition. Stating *"The feature is complete and working"* without providing verifiable exit codes (`exit code 0`) and output logs is a protocol violation. |
| **Non-Destructive Idempotency** | All file edits and tool calls must be reproducible, reversible, and non-destructive. Never leave the working tree in a broken or corrupted state upon task termination. |

---

## 2. Scope, Precedence and Configuration Hierarchy

### 2.1 The AAIF Open Standard Specification
This file adheres to the open standard established under the **Linux Foundation's Agentic AI Foundation (AAIF)** for machine-readable repository guides (`AGENTS.md`). Under this standard:
- `AGENTS.md` is formatted as clean, platform-neutral CommonMark.
- It is located at the root of the repository as the primary entry point for AI agents.
- It prioritizes high-impact operational invariants near the top of the file to exploit the primacy effect in large language model attention heads.

### 2.2 Proximity Resolution: Closest-File-Wins
In modular repositories or monorepos, instruction scoping follows the **Proximity Resolution Rule**:
1. **Root Guide (`/AGENTS.md`)**: Establishes global repository defaults, universal safety guardrails, git workflows, and cross-cutting verification protocols.
2. **Subtree Guide (`path/to/module/AGENTS.md`)**: Provides localized domain rules, module-specific test commands, package conventions, or specialized dependencies.
3. **Precedence Invariant**: In case of direct contradiction between root and subtree guides, the **closest file to the modified code wins**, provided it does not violate global security or non-destructive invariants.

```
/ (Repository Root)
├── AGENTS.md                   <-- Universal baseline (All agents read this first)
├── pom.xml                     <-- Global Maven build definition
├── src/
│   ├── main/
│   │   ├── java/.../core/      <-- Governed by root AGENTS.md + ARCHITECTURE.md
│   │   └── java/.../adapter/   <-- Governed by root AGENTS.md + ARCHITECTURE.md
│   └── test/
```

### 2.3 Repository Configuration Precedence Ladder
When executing instructions, agents must resolve conflicting directives according to the following strict descending hierarchy:

1. **Direct User / Developer Session Prompt**  
   *Highest priority.* Explicit user instructions for the current session override general repository advice, provided they do not breach hard security guardrails (Section 8.1).
2. **Subdirectory `AGENTS.md`**  
   Module-level guidelines and subsystem-specific architectural requirements.
3. **Root `AGENTS.md` (This Document)**  
   The authoritative default baseline for repository conventions, tool discipline, and verification.
4. **Vendor / Runtime Tool Overlays**  
   Files such as `GEMINI.md`, `CLAUDE.md`, or `.cursorrules` that configure model parameters, context window handling, and platform-specific IDE shortcuts.
5. **Base LLM Training Prior**  
   *Lowest priority.* General knowledge should always defer to repository code, configuration files, and explicit documentation.

### 2.4 Cross-Document Alignment: AGENTS, GEMINI, and ARCHITECTURE
This repository maintains three specialized documentation pillars. Agents must respect their distinct boundaries:
- **`AGENTS.md` (This File)**: The universal, vendor-agnostic operational standard for agent behavior, tool execution, minimal diffs, verification ladders, and error recovery.
- **`ARCHITECTURE.md`**: The technical reference for software design (Hexagonal Architecture / Ports & Adapters, domain core isolation, and package dependencies). Agents must verify their changes against `ARCHITECTURE.md`.
- **`GEMINI.md`**: Model-specific runtime configurations, context management strategies, prompt conventions, and CLI flags tailored for Google Gemini / Antigravity agents.

---

## 3. The 5-Stage Agent Execution Lifecycle

To prevent context drift, hallucination loops, and unverified diffs, all agent tasks must proceed through five distinct, deterministic phases.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     THE 5-STAGE AGENT LIFECYCLE                         │
└─────────────────────────────────────────────────────────────────────────┘
  1. RECONNAISSANCE     2. PLANNING           3. SURGICAL EXECUTION
  ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
  │ • Read-only     │   │ • Atomic steps  │   │ • Minimal diff  │
  │ • Locate code   │──►│ • Define tests  │──►│ • Match style   │
  │ • Inspect specs │   │ • Map risks     │   │ • No side-refac │
  └─────────────────┘   └─────────────────┘   └─────────────────┘
                                                       │
                        4. VERIFICATION                ▼
                        ┌─────────────────┐   5. MANIFEST DELIVERY
                        │ • 4-tier ladder │   ┌─────────────────┐
                        │ • Compile/Build │──►│ • Git status    │
                        │ • Test proof    │   │ • Audit diff    │
                        │ • Zero failures │   │ • Handoff report│
                        └─────────────────┘   └─────────────────┘
```

### 3.1 Stage 1: Reconnaissance and Context Acquisition
- **Post-Dispatch Posture**: Maintain an absolute read-only posture. Never modify a file during initial exploration.
- **Entry Point Identification**: Inspect the primary build descriptor (`pom.xml`), existing configuration files, and package layouts before inspecting business code.
- **Bounded Exploration**:
  - Prefer targeted index searches (`grep_search`, `find_by_name`) over exhaustive directory scans.
  - Bound file reads using line-number slices (`StartLine`, `EndLine`). Avoid reading entire 1,000+ line files into working context.
  - Never view generated binary files, compiled class files, or build artifacts (`target/`, `.git/`).

### 3.2 Stage 2: Task Planning and Decomposition
- **Step Breakdown**: Decompose complex tasks into atomic, sequentially verifiable operations. Each subtask must possess a distinct verification condition.
- **Invariant Formulation**: Identify critical system invariants (e.g., domain entities must not depend on database annotations; existing public APIs must remain backward-compatible).
- **Hypothesis Definition**: If resolving a bug, formulate an explicit hypothesis explaining the root cause and describe how a test will disprove or prove it.
- **Rollback Preparation**: Note the initial git commit or file state to enable instant rollback if execution diverges from plan.

### 3.3 Stage 3: Surgical Execution and Minimal Diffs
- **Hunk-Based Editing**: Use precise replacement tools (`replace_file_content`) specifying unique target text blocks. Avoid whole-file replacements.
- **Context Preservation**: Read the immediate target file lines directly before editing.
- **Boundary Adherence**: Modify only the code necessary to satisfy the prompt. Never reformat untouched lines, clean up unrelated imports, or modify comments outside the change hunk.

### 3.4 Stage 4: Deterministic Verification and Validation
- **Execution of Verification**: Run the project build and test suite via non-destructive shell execution.
- **Evidence Collection**: Capture exact terminal outputs, exit codes, and test summaries (e.g., `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0`).
- **Regression Detection**: Verify both the changed module and any downstream dependent modules.

### 3.5 Stage 5: Manifest Audit and Delivery
- **Working Tree Inspection**: Run `git status` or inspect modified file manifests. Ensure no unintended edits, temporary test logs, or untracked scratch files remain.
- **Structured Delivery**: Generate a concise completion report referencing modified files, verification evidence, and operational handoff details.

---

## 4. Tool Calling Discipline and Command Protocol

### 4.1 Read Operations and Search Hierarchy
Agents must optimize context token usage and avoid terminal truncation by following the strict search-and-read sequence:

```
[Target Discovery] ──► 1. Exact Symbol / Pattern Search (grep_search)
                            │
                            ▼
                       2. File Path Matching (find_by_name)
                            │
                            ▼
                       3. Bounded File Slice Read (view_file with StartLine/EndLine)
```

- **Line Bounding Rule**: Always specify `StartLine` and `EndLine` with ranges under 150 lines when inspecting code. Broad file dumps saturate context and degrade reasoning.
- **Exclusion Filters**: Always exclude `/target`, `/.git`, `node_modules`, and binary directories from searches.

### 4.2 Write and Modification Operations
- **Atomic Replacements**: All modifications must use localized string replacements (`replace_file_content`). Provide sufficient context lines before and after the modification chunk to guarantee uniqueness.
- **Whole-File Replacement Ban**: Overwriting entire files via `write_to_file` with `Overwrite=true` is prohibited on existing code files. Whole-file creation is restricted to brand-new files.
- **Pre-Read Verification**: An agent must never modify a file without having viewed its current contents during the current task session.

### 4.3 Shell and Command Execution Boundaries

#### ❌ Forbidden Commands (Immediate Disqualification)
The following commands and patterns are strictly forbidden in any agent shell invocation:

```bash
# Destructive filesystem operations
rm -rf /
rm -rf ~
rm -rf *
del /f /s /q C:\*

# Destructive git commands that erase history or uncommitted work
git reset --hard
git clean -fdx
git checkout .
git restore .
git push --force
git push origin master --force
git push origin main --force

# Uncontrolled background / remote execution
curl ... | sh
wget ... | bash
nohup ... & (without managed task lifecycle and timeout)
```

#### ✅ Permitted Command Protocol
- Read-only diagnostics: `git status`, `git diff`, `git log -n 5`.
- Standard project build and test tooling (e.g., Maven, Gradle, npm, Python).
- Commands must be executed with appropriate timeouts (e.g., 5000–10000 ms synchronous wait) and non-daemon flags unless explicitly orchestrating a long-running service.

### 4.4 Tool Error Handling and Idempotency
- **Inspect, Don't Blind-Retry**: If a tool call fails (e.g., `TargetContent not found` or `range error`), re-read the relevant lines of the file to see if previous edits changed line numbering or formatting.
- **The Idempotency Rule**: Never call the exact same tool with the exact same failed arguments twice. Each successive call must incorporate diagnostic adjustments based on the previous error message.

---

## 5. Code Modification Standards and Diff Hygiene

### 5.1 The Minimal Diff Rule
Every line in a git diff carries operational risk and cognitive load for human reviewers. Agents must follow the **Minimal Diff Rule**:
- **Proportionality**: A one-line bug fix should produce a one-line diff (or small surrounding hunk). A 50-line diff for a trivial defect is an immediate failure.
- **No Drive-by Refactoring**: Do not reformat code, alter indentation conventions (e.g. converting 4 spaces to 2 spaces), reorder class members, or rewrite existing comments.
- **Import Hygiene**: Add only the specific imports required for new functionality. Do not optimize, reorder, or wildcard (`import foo.*`) untouched imports.
- **Blame Preservation**: Preserve git blame history on existing code whenever possible.

### 5.2 Style, Idioms and Architectural Compliance
- **Local Consistency**: Mirror the conventions of surrounding code. If the file uses CamelCase, explicit types, or specific annotation styles, conform strictly.
- **Architectural Boundary Adherence**: If the repository implements Hexagonal Architecture (as detailed in `ARCHITECTURE.md`):
  1. **Domain Core**: Must remain completely free of framework imports (e.g. Spring, JPA, Hibernate), database annotations, and external network clients.
  2. **Inbound/Driving Ports**: Define application interfaces for use cases.
  3. **Outbound/Driven Ports**: Define SPIs for external dependencies (repositories, mail services, notification adapters).
  4. **Adapters**: Reside in secondary packages and implement outbound ports or invoke inbound ports.

### 5.3 Dependency Hygiene and External Libraries
- **No Unauthorized Dependencies**: An agent must never add a new third-party dependency to `pom.xml`, `build.gradle`, or `package.json` without explicit user permission.
- **License Compliance**: If adding a dependency is approved, verify that it uses an enterprise-permissive open-source license (Apache 2.0, MIT, BSD-3-Clause). Viral copyleft licenses (GPL, AGPL) are prohibited.
- **CVE Invariant**: Check that the requested library version has zero critical or high Common Vulnerabilities and Exposures (CVEs).

---

## 6. Testing, Verification and Quality Assurance

### 6.1 The Claim vs Proof Invariant
Agents frequently generate optimistic completion summaries when their changes fail compilation or break existing tests. To eliminate false-positive declarations:

```
┌────────────────────────────────────────────────────────────────────────┐
│                      THE CLAIM VS PROOF INVARIANT                      │
├────────────────────────────────────────────────────────────────────────┤
│  CLAIM: "I fixed the NullPointerException in EmailService."            │
│  STATUS: INADMISSIBLE WITHOUT EXPLICIT TERMINAL EXECUTION OUTPUT       │
│                                                                        │
│  PROOF:                                                                │
│  $ mvn test -Dtest=EmailServiceTest                                    │
│  [INFO] Running ir.bahmanheydarinejad.service.EmailServiceTest         │
│  [INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time: 0.812s │
│  [INFO] BUILD SUCCESS (Exit Code 0)                                    │
│  STATUS: VERIFIED AND ADMISSIBLE                                       │
└────────────────────────────────────────────────────────────────────────┘
```

### 6.2 The 4-Tier Verification Ladder
All code modifications must climb the 4-tier verification ladder in order. Skipping tiers is not permitted:

```
[Tier 4: Full Regression Suite]   ──► mvn test (entire suite green, 0 regressions)
               ▲
[Tier 3: Targeted Unit Tests]     ──► mvn test -Dtest=ModifiedClassTest
               ▲
[Tier 2: Build & Compilation]     ──► mvn clean compile (exit code 0, zero errors)
               ▲
[Tier 1: Syntax & Static Check]   ──► Formatting, imports, static analysis clean
```

1. **Tier 1: Syntax & Static Analysis**: Verify code syntax, balance brackets, ensure valid typing, and eliminate missing imports.
2. **Tier 2: Build & Compilation**: Compile the project with all modified and generated files. Must produce `BUILD SUCCESS` with exit code 0.
3. **Tier 3: Targeted Unit Verification**: Run unit and integration tests specifically exercising the modified class or method.
4. **Tier 4: Full Regression Suite**: Run the complete test suite across the repository to verify that no downstream consumers or sibling modules were broken.

### 6.3 Hypothesis Invalidation Testing: Red-Green-Refactor
When fixing a reported bug:
1. **Red (Reproduce)**: Write or run a targeted test that isolates the bug. Confirm that the test fails before the code fix is applied.
2. **Green (Fix)**: Apply the minimal code modification. Re-run the test. Confirm that the test now passes with exit code 0.
3. **Refactor & Check**: Verify that all other existing tests continue to pass without regression.

### 6.4 Project Verification Command Matrix
Reference commands for this repository (Maven / Java Spring Boot) and standard equivalents across ecosystems:

| Operation | Maven (Current Repository) | Gradle Equivalent | npm / TypeScript Equivalent | Python (pytest/uv) Equivalent |
|:---|:---|:---|:---|:---|
| **Clean & Compile** | `mvn clean compile` | `./gradlew compileJava` | `npm run build` | `python -m py_compile ...` |
| **Targeted Test** | `mvn test -Dtest=<TestName>` | `./gradlew test --tests <TestName>` | `npm test -- -t "<TestName>"` | `pytest -k <test_name>` |
| **Full Test Suite** | `mvn test` | `./gradlew test` | `npm test` | `pytest` |
| **Package / Artifact** | `mvn package -DskipTests` | `./gradlew assemble` | `npm pack` | `python -m build` |
| **Dependency Audit** | `mvn dependency:tree` | `./gradlew dependencies` | `npm audit` | `pip list` / `uv pip tree` |

---

## 7. Error Recovery, Root-Cause Analysis and Safe Rollback

### 7.1 The Rule of Three: Breaking Churn Loops
Agents trapped in failure loops tend to churn code randomly, making increasingly reckless modifications. To prevent this destructive behavior, agents must enforce **The Rule of Three**:

```
[Test / Build Failure Detected]
             │
             ▼
      [Attempt 1: Formulate RCA Hypothesis & Apply Surgical Fix]
             │
             ├─► Passes? ──► Proceed to Verification (Success)
             ▼
          Fails?
             │
             ▼
      [Attempt 2: Re-read logs, diagnose secondary issue & adjust]
             │
             ├─► Passes? ──► Proceed to Verification (Success)
             ▼
          Fails?
             │
             ▼
   ╔══════════════════════════════════════════════════════════════╗
   ║ 🛑 STOP! THE RULE OF THREE ACTIVATED                         ║
   ║ • Cease all write operations immediately.                    ║
   ║ • Roll back all attempted changes to clean git state.        ║
   ║ • Return to Stage 1 (Reconnaissance) or escalate to human.   ║
   ╚══════════════════════════════════════════════════════════════╝
```

### 7.2 Root-Cause Analysis (RCA) Protocol
When a compiler or test failure occurs, agents must conduct a structured RCA:
- **Trace the Stack**: Locate the exact throwing file, line number, and exception class. Do not guess based on method names.
- **No Symptom Masking**:
  - Never wrap failing code in a generic `catch (Exception e) {}` block that silently swallows errors.
  - Never add broad `null` checks (`if (x != null)`) that obscure invalid system states or violate invariants.
  - Never comment out, disable (`@Disabled`, `@Ignore`), or delete existing tests to force a build to pass.
- **Five-Why Diagnostic**: Determine why the state was invalid before attempting a code fix.

### 7.3 Safe Rollback Procedures
If an agent realizes an approach is architecturally invalid or fails twice under the Rule of Three:
1. Identify all files modified during the failed attempt.
2. Revert individual modified files cleanly:
   ```bash
   git checkout -- src/main/java/.../FailingClass.java
   ```
3. Verify that the repository has returned to its initial, pre-modification clean state before formulating a new strategy.

---

## 8. Security, Secret Hygiene and Safety Guardrails

### 8.1 Absolute Never-Rules: Hard Guardrails
The following actions represent critical safety violations and will result in immediate termination of the agent's task:
1. **Never commit, print, echo, or log secrets**: Including API tokens, private keys, database passwords, OAuth secrets, and `.env` contents.
2. **Never disable security controls**: Disabling authentication filters, bypassing CSRF protection, adding wildcard `permitAll()` to Spring Security, or turning off SSL/TLS verification (`trustAllCerts`) to pass tests is strictly forbidden.
3. **Never place application code in metadata folders**: Source code, tests, and business logic belong in `src/`. The `.agents/` and `.agents/teamwork/` directories are reserved exclusively for agent operational metadata.
4. **Never alter remote repository pointers**: Modifying remote URLs, force-pushing, or attempting network operations outside authorized endpoints.

### 8.2 Secret and Credential Leakage Prevention
Agents must sanitize all outputs and inspect diffs for unintended credential leaks. Never introduce tokens matching common credential signatures:

```regex
# Secret Detection Patterns
(?i)api[_-]?key\s*[:=]\s*['"][A-Za-z0-9_\-]{16,}['"]
(?i)password\s*[:=]\s*['"][^'"]+['"]
(?i)bearer\s+[A-Za-z0-9_\-\.]{20,}
(?i)aws[_-]?(secret|access)[_-]?key
-----BEGIN (RSA |EC |DSA |OPENSSH )?PRIVATE KEY-----
```

Use mock strings (`"test-dummy-api-key"`, `"placeholder-password"`) or environment variable references in test cases.

### 8.3 Environmental and Network Isolation
- **Workspace Confinement**: Agents must operate exclusively inside the active repository working directory. Reading or writing outside the workspace (e.g. `C:\Windows`, `~/.ssh`, `~/.bashrc`, user profile folders) is strictly disallowed.
- **Environment Cleanliness**: Do not modify system environment variables, modify system-wide package managers, or install untracked global tools.

---

## 9. Developer-Agent Interaction and Communication Protocols

### 9.1 Autonomous Action vs Escalation Thresholds
Agents must understand when to act autonomously and when to pause execution to ask the developer for clarification:

```
┌──────────────────────────────────────┬──────────────────────────────────────┐
│       ACT AUTONOMOUSLY               │        ESCALATE TO DEVELOPER         │
├──────────────────────────────────────┼──────────────────────────────────────┤
│ • Implementing documented use cases  │ • Ambiguous or conflicting specs     │
│ • Fixing localized unit test bugs    │ • Proposed database schema changes   │
│ • Writing missing test suites        │ • Introducing new 3rd-party libraries│
│ • Refactoring within class boundary  │ • Breaking changes to public APIs    │
│ • Conforming code to ARCHITECTURE.md │ • Security policy alterations        │
└──────────────────────────────────────┴──────────────────────────────────────┘
```

### 9.2 Progress Tracking and State Heartbeats
For long-running or multi-step tasks, agents must maintain persistent progress tracking:
- Maintain an active `progress.md` containing milestones, current phase, and timestamps.
- Update progress heartbeats every meaningful step to enable human developers and orchestrators to detect liveness and avoid stalled execution.

### 9.3 Structured Delivery and Handoff Format
Upon task completion, agents must submit a structured report containing the five standard handoff components:
1. **Observation**: Concrete, verifiable facts (exact file paths, modified line numbers, verbatim test execution logs).
2. **Logic Chain**: Step-by-step reasoning demonstrating how the changes fulfill the initial requirements.
3. **Caveats**: Edge cases, assumptions made, or areas outside the scope that require human review.
4. **Conclusion**: Concise statement of final state.
5. **Verification Method**: The exact terminal commands a developer can run to verify the solution independently.

---

## 10. Operational Checklists and Quick Reference

### 10.1 Pre-Flight Checklist
Copy and verify this checklist before writing any code:

```markdown
- [ ] Task objective and acceptance criteria clearly understood.
- [ ] Active workspace confirmed as repository root.
- [ ] Relevant documentation consulted (`pom.xml`, `ARCHITECTURE.md`, `AGENTS.md`).
- [ ] Working tree status clean (`git status` checked).
- [ ] Baseline build and tests verified (`mvn clean compile`).
- [ ] Targeted search conducted (`grep_search` / `find_by_name`).
- [ ] Target file contents inspected with bounded line read (`view_file`).
- [ ] Explicit 5-stage plan formulated before mutating files.
```

### 10.2 Modification Checklist
Copy and verify this checklist during code modification:

```markdown
- [ ] Minimal Diff Rule followed (zero unsolicited refactoring or formatting changes).
- [ ] Surrounding code style, indentation, and naming conventions strictly matched.
- [ ] Atomic hunk replacement tool used (`replace_file_content`).
- [ ] Existing comments and untouched methods preserved verbatim.
- [ ] Architectural boundaries respected (Hexagonal Architecture ports/adapters intact).
- [ ] No unauthorized dependencies added to `pom.xml`.
- [ ] No hardcoded secrets, passwords, or credentials introduced.
- [ ] Source files placed in `src/`, not in agent metadata directories.
```

### 10.3 Verification and Done Checklist
Copy and verify this checklist before declaring task completion:

```markdown
- [ ] Tier 1: Syntax and static analysis verified clean.
- [ ] Tier 2: Compilation succeeds (`mvn clean compile` returns exit code 0).
- [ ] Tier 3: Targeted unit tests pass (`mvn test -Dtest=...`).
- [ ] Tier 4: Full regression test suite passes (`mvn test` reports 0 failures, 0 errors).
- [ ] Rule of Three obeyed (no churn loops or blind retries).
- [ ] Working tree audited (`git status` shows only intended file changes).
- [ ] Scratch files and temporary artifacts removed.
- [ ] Structured delivery report generated with verbatim verification proof.
```

---
*End of `AGENTS.md` — AI Agent Operational Standards and Best Practices Guide.*
