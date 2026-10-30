# Handoff Report: R3 — Gemini-Specific Agent Instructions (GEMINI.md) Specification

**Author**: Explorer Survey 3 (`teamwork_explorer`)  
**Target Milestone**: R3 — Gemini-Specific Agent Instructions (`GEMINI.md`)  
**Recipient**: Orchestrator (`e8a6ca15-9ae0-4a99-991a-e54d257388f2`) & Downstream Implementation Worker  
**Date**: 2026-10-29T21:26:00Z  

---

## 1. Observation

### 1.1 Direct Repository and Environment Observations
1. **Authoritative Task Requirement**:
   In `c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\ORIGINAL_REQUEST.md`, lines 18-19 and lines 35-38 state:
   > "### R3. Gemini-Specific Agent Instructions (GEMINI.md)
   > Create a separate GEMINI.md specifying Gemini-specific agent instructions, environment configurations, and prompt conventions.
   > ...
   > ### GEMINI.md Structure and Content
   > - [ ] GEMINI.md exists in c:\Users\parse-it\Documents\Git\AppService.
   > - [ ] Documents instructions, conventions, and configurations specific to Gemini agents."

2. **Dispatch Directives for Survey 3**:
   In `c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\explorer_survey_3\DISPATCH.md`, lines 8-19 specify:
   > "Investigate and specify the comprehensive requirements, structure, and best practices for **R3: Gemini-Specific Agent Instructions (GEMINI.md)**...
   > Key Scope & Details to Explore:
   > 1. Gemini-specific agent instructions, environment configurations, and prompt conventions:
   >    - Google Gemini models family capabilities (e.g. Gemini 1.5 Pro / Flash, 2.0 / Flash / Thinking, large context window handling).
   >    - Gemini CLI / Antigravity / API environment configurations and tool calling conventions (function calling, system instructions, safety settings).
   >    - Prompt conventions: structure, role definition, formatting, few-shot examples, reasoning chains, structured output formats.
   >    - Long context window best practices (retrieval vs in-context stuffing, needle-in-a-haystack optimization).
   >    - Multimodal inputs, code execution, search groundings, and tool ecosystem integration.
   >    - Project-level GEMINI.md configuration standards: how Gemini agents interpret GEMINI.md rules, overrides, and environment flags."

3. **Complementary Scope Division (AGENTS.md vs GEMINI.md)**:
   In `c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\explorer_survey_2\DISPATCH.md`, lines 8-20, `AGENTS.md` is scoped to vendor-neutral agent standards (general workflows, communication protocols, non-destructive editing, general safety, planning). Thus, `GEMINI.md` must focus specifically on **Google Gemini architectural capabilities, Gemini API/CLI/Antigravity runtime configurations, native function calling, multimodal tokenization, context caching, safety threshold suppression for developers, and Gemini-native prompt conventions**.

4. **Host Runtime Environment**:
   Inspection of `c:\Users\parse-it\.gemini` confirms active Antigravity agent tooling:
   - `c:\Users\parse-it\.gemini\config\config.json` configures `artifactReviewMode: ARTIFACT_REVIEW_MODE_TURBO`, `autoExecutionPolicy: CASCADE_COMMANDS_AUTO_EXECUTION_EAGER`, and terminal sandbox settings.
   - `c:\Users\parse-it\.gemini\config\mcp_config.json` exposes MCP servers (e.g. `chrome-devtools-mcp`).
   - Project root is `c:\Users\parse-it\Documents\Git\AppService` (Java Maven repository with Spring Boot).

5. **Structural Isolation Constraint**:
   Per Agent Workflow Rules:
   > "⚠️ `.agents/teamwork/` holds only agent metadata (plans, progress, handoffs). NEVER place source code, tests, or data files here, and never name a file `AGENTS.md` or `GEMINI.md` — those filenames are loaded as always-on agent rules."
   The deliverable `GEMINI.md` must be created in the repository root (`c:\Users\parse-it\Documents\Git\AppService\GEMINI.md`), while this survey output resides in `c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\explorer_survey_3\handoff.md`.

---

## 2. Logic Chain

### Step 1: Architectural Rationale for a Dedicated `GEMINI.md`
- *Premise (Observation 1 & 3)*: Generic agent instructions (`AGENTS.md`) define model-agnostic principles (e.g., "verify before committing", "do not delete user files"). However, LLMs have distinct architectures, token limits, prompt caching models, attention biases, and function calling conventions.
- *Reasoning*: Google Gemini models possess distinct attributes unmatched by traditional architectures:
  1. Ultra-long context windows (1 Million to 2 Million tokens).
  2. First-class API-level Context Caching (`CachedContent`).
  3. Dedicated architectural `systemInstruction` parameters separate from the conversational KV cache.
  4. Specific Function Calling schemas and calling modes (`AUTO`, `ANY`, `NONE`, `allowedFunctionNames`).
  5. Built-in groundings (Google Search Grounding, native Code Execution sandbox).
  6. Native multimodal tokenization across text, image, audio, and video.
  7. Strict safety filter heuristics requiring explicit developer thresholds (`BLOCK_NONE` or `BLOCK_ONLY_HIGH` for code/security analysis).
- *Deduction*: `GEMINI.md` serves as the authoritative, project-level operational instruction manual specifically tuned to harness Gemini's strengths and mitigate its failure modes.

### Step 2: Gemini Model Family Taxonomy & Workload Routing
- *Premise (Observation 2)*: Developers interacting with Gemini must select the optimal model variant based on latency, reasoning depth, context length, and token cost.
- *Analysis of Model Variants*:
  - **Gemini 1.5 Pro**:
    - Context: 2,097,152 (2M) input tokens; 8,192 max output tokens.
    - MoE architecture with deep cross-document reasoning.
    - Target Tasks: Large-scale repository architecture audits, cross-module refactoring, multi-file dependency analysis, deep architectural review.
  - **Gemini 1.5 Flash**:
    - Context: 1,048,576 (1M) input tokens; 8,192 max output tokens.
    - Lightweight, sub-second TTFT, high generation velocity (>100 tokens/sec).
    - Target Tasks: Surgical edits, localized unit test writing, grep/log triage, quick doc generation, repetitive code linting.
  - **Gemini 1.5 Flash-8B**:
    - Ultra-fast 8B model for high-frequency classification, lightweight text extraction, and micro-checks.
  - **Gemini 2.0 Flash**:
    - Next-generation multimodal frontier model (1M context) with native tool use optimization, low latency streaming, and agentic workflows.
  - **Gemini 2.0 Flash Thinking (Experimental)**:
    - Integrated native Chain-of-Thought reasoning tokens.
    - Target Tasks: Algorithmic complexity, concurrency debugging, race condition analysis, complex mathematical/state machine logic.
  - **Gemini 2.0 Pro (Experimental)**:
    - Frontier flagship model for state-of-the-art software engineering, complex system redesign, and advanced multi-turn problem solving.

### Step 3: Context Window Management & Optimization Mechanics
- *Premise (Observation 2)*: A 1M-2M context window changes developer workflows, but naive usage causes attention degradation ("Lost in the Middle") or excessive latency.
- *Analysis & Best Practices*:
  1. **Needle-in-a-Haystack (NIAH) & Attention Distribution**:
     - Although Gemini retains >99% recall across 1M+ tokens, transformer attention exhibits a U-shaped curve: information at the beginning (primacy) and end (recency) receives the strongest attention weights.
     - **The Context Sandwich Pattern**:
       - *Top*: `systemInstruction` (immutable rules, role identity, constraints, tool rules).
       - *Middle*: Reference code, full repository file dumps, external documentation, API schemas.
       - *Bottom*: Immediate task instructions, latest tool results, and a "Recency Anchor" reiterating critical boundaries.
  2. **Context Caching (`CachedContent` API)**:
     - Minimum cacheable payload: 32,768 tokens.
     - Cost savings: Up to 75% reduction on input token pricing.
     - Latency: TTFT drops from ~15s to <1.5s for large codebases.
     - Guideline: Repositories with stable codebases and schemas should leverage context caching with TTLs (e.g., 1 hour to 24 hours).
  3. **In-Context Stuffing vs Surgical Tool Search**:
     - *Stuffing*: Ideal when the task requires global reasoning across all classes (e.g. "Identify all circular dependencies across the entire project").
     - *Surgical Search (`grep_search`, `find_by_name`, `view_file`)*: Preferred for localized fixes to conserve quota, reduce latency, and prevent context dilution.
  4. **Context Hygiene**:
     - Prune raw build outputs (e.g. `target/`, `node_modules/`, compiler byte arrays).
     - Truncate repetitive tool responses (e.g. capping file search at top 50 matches).

### Step 4: Tool Calling & Function Calling Architecture
- *Premise (Observation 2 & 4)*: Gemini relies on OpenAPI 3.0 / JSON Schema function declarations to interact with the environment.
- *Analysis & Requirements*:
  1. **Calling Modes (`toolConfig`)**:
     - `AUTO`: Default; model decides whether to speak or call tools.
     - `ANY`: Forces the model to call at least one tool before responding (essential for enforcing an "investigate-before-answering" workflow).
     - `NONE`: Restricts the model to natural language.
     - `allowedFunctionNames`: Limits callable tools to a strict whitelist for sensitive phases.
  2. **Parallel Tool Calling**:
     - Gemini 1.5 Pro and 2.0 natively issue multiple tool calls in a single response turn (e.g., inspecting 3 related source files simultaneously).
     - Agent runtime must support batched execution and return corresponding `functionResponse` items mapped by name and call ID.
  3. **Built-in Grounding Tools**:
     - `google_search_retrieval`: Grounding responses against real-time web search for latest package versions, CVE details, or API docs.
     - `code_execution`: Google-hosted Python execution sandbox for math, data transformation, and algorithmic validation.
  4. **Model Context Protocol (MCP)**:
     - Seamless integration with MCP servers (e.g. `chrome-devtools-mcp`, filesystem, database adapters).
     - Distinguish between **eager tools** (declared directly in system function declarations) and **lazy tools** (`call_mcp_tool`, `list_resources`) to prevent schema token bloat.

### Step 5: Environment Configurations & Safety Threshold Management
- *Premise (Observation 2 & 4)*: Coding agents frequently encounter code snippets, system commands, or penetration testing scripts that trigger overzealous safety filters unless configured correctly.
- *Analysis*:
  - **Generation Configuration**:
    - Coding / Refactoring: `temperature: 0.0` - `0.2`, `topP: 0.95`, `topK: 40`.
    - Architecture / Brainstorming: `temperature: 0.5` - `0.7`.
    - Structured Output: Set `responseMimeType: "application/json"` and provide `responseSchema`.
  - **Safety Threshold Calibration**:
    - Harm categories: `HARM_CATEGORY_HARASSMENT`, `HARM_CATEGORY_HATE_SPEECH`, `HARM_CATEGORY_SEXUALLY_EXPLICIT`, `HARM_CATEGORY_DANGEROUS_CONTENT`, `HARM_CATEGORY_CIVIC_INTEGRITY`.
    - Default API settings will block security audits, shell commands (`rm -rf`, `curl`, `chmod`), and network code as "Dangerous Content".
    - **Crucial Rule**: For developer coding agents in trusted workspaces, `safetySettings` must set `HarmBlockThreshold.BLOCK_NONE` (or `BLOCK_ONLY_HIGH`) to ensure uninterrupted execution without false-positive `finishReason: SAFETY` refusals.

### Step 6: Prompt Conventions & Instruction Engineering
- *Premise (Observation 2)*: Gemini models exhibit distinct prompt response dynamics compared to other LLMs.
- *Key Directives*:
  1. **Privileged System Instruction Channel**: Always pass core repository guidelines via the API's `systemInstruction` parameter, not as a user message turn.
  2. **Role & Identity Definition**: Specify archetype, operational capabilities, tool permissions, and OS context (e.g., Windows powershell vs Linux bash).
  3. **Chain-of-Thought (CoT)**:
     - For Gemini 1.5: Enforce explicit `<thought>...</thought>` scratchpads before tool calling or code output.
     - For Gemini 2.0 Flash Thinking: Leverage native reasoning tokens via `thinking_config`.
  4. **Directness & Tone**: Strictly suppress conversational preamble ("Sure, I can help!"). Direct code/patch output only.
  5. **Negative Boundaries**: Pair every affirmative rule with a hard negative ("Do X; Never do Y").

### Step 7: Multimodal Capabilities in Developer Workflows
- *Premise (Observation 2)*: Gemini is natively multimodal (text, images, audio, video, PDF).
- *Developer Applications*:
  - **Visual Bug Reproduction**: Ingest screenshot or video recording of a UI glitch, compare against design spec (Figma screenshot), and pinpoint CSS/DOM mismatches.
  - **Architecture Diagram Ingestion**: Feed PNG/SVG/whiteboard diagrams and instruct Gemini to synthesize structured Mermaid diagrams or domain entity definitions.
  - **Datasheet & Specification Parsing**: Directly ingest PDF datasheets or IEEE/RFC specifications without pre-OCR conversion.
  - **Token Calculation Rules**: Image tiles consume ~258 tokens per crop; video at 1 FPS consumes ~258 tokens/second; audio consumes ~32 tokens/second.

### Step 8: Complete Structural Specification for `GEMINI.md`
- To enable the downstream implementation worker to create a world-class `GEMINI.md`, this report details the exact section outline, content requirements, configuration snippets, and best practices.

---

## 3. Caveats

1. **Host Environment Context**: The repository contains a Java/Spring Boot codebase (`AppService`), but `ORIGINAL_REQUEST.md` specifies that the architectural and agent guides must provide comprehensive, general best practices rather than being strictly restricted to a single monolithic service. `GEMINI.md` must therefore provide general developer agent standards with concrete examples applicable across Java, TypeScript, Python, and modern multi-language environments.
2. **Experimental Gemini Models**: Gemini 2.0 Flash Thinking and Gemini 2.0 Pro are evolving rapidly. The documentation must specify both stable (Gemini 1.5 Pro / Flash) and next-generation (Gemini 2.0 Flash / Thinking) conventions, noting experimental flags where appropriate.
3. **Write Isolation**: As an explorer survey agent, Explorer Survey 3 must NOT create or edit `c:\Users\parse-it\Documents\Git\AppService\GEMINI.md` directly. The actual file must be created by the designated worker agent dispatched by the orchestrator in Milestone 4.

---

## 4. Conclusion & Complete Specification Blueprint for `GEMINI.md`

### 4.1 Recommended Blueprint for `GEMINI.md`
The worker agent should generate `c:\Users\parse-it\Documents\Git\AppService\GEMINI.md` following this comprehensive 9-section structure:

```markdown
# Table of Contents
1. [Gemini Agent Operating Foundation](#1-gemini-agent-operating-foundation)
   - 1.1 Purpose and Precedence
   - 1.2 Agent Persona and Operational Boundary
2. [Gemini Model Family & Workload Routing](#2-gemini-model-family--workload-routing)
   - 2.1 Model Specifications (1.5 Pro, 1.5 Flash, 2.0 Flash, 2.0 Flash Thinking)
   - 2.2 Task-to-Model Routing Matrix
   - 2.3 Cost and Latency Trade-Offs
3. [Long Context Window Management & Optimization](#3-long-context-window-management--optimization)
   - 3.1 1M/2M Token Dynamics & Attention Characteristics
   - 3.2 The Context Sandwich Pattern
   - 3.3 Context Caching (CachedContent API) Guidelines
   - 3.4 In-Context Ingestion vs. Surgical Tool Retrieval
   - 3.5 Context Hygiene & Pruning Protocols
4. [Environment Configuration & Runtime Parameters](#4-environment-configuration--runtime-parameters)
   - 4.1 Supported Runtime Environments (Antigravity IDE, Gemini CLI, Vertex AI, AI Studio)
   - 4.2 GenerationConfig Standards (temperature, topP, topK, responseSchema)
   - 4.3 Safety Settings & Developer Threshold Configuration (BLOCK_NONE)
   - 4.4 Environment Variables & Secrets Handling
5. [Tool Calling & Function Execution Conventions](#5-tool-calling--function-execution-conventions)
   - 5.1 Native Function Calling Schemas (OpenAPI 3.0/JSON Schema)
   - 5.2 Tool Calling Modes (AUTO, ANY, NONE, Scoped Whitelists)
   - 5.3 Parallel Tool Invocations
   - 5.4 Built-in Grounding Tools (Google Search & Code Execution)
   - 5.5 Model Context Protocol (MCP) Integration (Eager vs Lazy Tools)
6. [Prompt Engineering & Instruction Protocols](#6-prompt-engineering--instruction-protocols)
   - 6.1 Privileged System Instruction Channel
   - 6.2 Tone, Directness, and Anti-Fluff Constraints
   - 6.3 Chain-of-Thought (CoT): `<thought>` Tags vs Native Thinking Mode
   - 6.4 Structured Output Formatting (JSON Schemas & Markdown Code Blocks)
   - 6.5 Few-Shot Demonstrations for Common Developer Workflows
7. [Multimodal Developer Workflows](#7-multimodal-developer-workflows)
   - 7.1 Multimodal Input Tokenization (Images, PDFs, Video, Audio)
   - 7.2 UI Bug Diagnosis & Visual Regressions
   - 7.3 Architecture Diagram Ingestion & Translation
   - 7.4 Screencast Bug Reproduction Analysis
8. [Code Modification & Operational Standards](#8-code-modification--operational-standards)
   - 8.1 Targeted Replacement vs Full-File Rewrite Discipline
   - 8.2 Non-Destructive Operations & Backup Principles
   - 8.3 Error Recovery & Self-Correction Protocols
9. [Project-Level Integration & Compliance Checklist](#9-project-level-integration--compliance-checklist)
   - 9.1 Relationship with AGENTS.md and ARCHITECTURE.md
   - 9.2 Gemini Agent Compliance Verification Matrix
```

---

### 4.2 Detailed Section Content Specifications

Below are the exact technical specifications, schemas, and configurations that must be included in each section of `GEMINI.md`:

#### Section 1: Gemini Agent Operating Foundation
- **Purpose**: Define `GEMINI.md` as the definitive project-level instructions for any Google Gemini-based AI agent (including Antigravity IDE, Gemini CLI, Cursor Gemini, and Vertex AI autonomous agents).
- **Precedence Hierarchy**:
  1. *Host Security Policy & System Kernel Prompts* (Top Priority / Invariant)
  2. *Project-Level `GEMINI.md`* (Gemini-native execution, context rules, tool calling schemas)
  3. *Project-Level `AGENTS.md`* (General vendor-neutral agent standards)
  4. *In-Session User Messages & Directives*
- **Operational Identity**:
  - The Gemini agent is an expert principal software engineer and systems architect.
  - Direct, precise, self-verifying, non-sycophantic, and strictly adheres to tool safety.

#### Section 2: Gemini Model Family & Workload Routing
- **Model Taxonomy Table**:
  | Model | Context Window | Max Output | Key Capabilities | Best For |
  |---|---|---|---|---|
  | **Gemini 1.5 Pro** | 2,097,152 tokens (2M) | 8,192 tokens | Deep reasoning, massive context, multi-repository analysis | Full-codebase refactoring, architectural auditing, complex bug diagnosis |
  | **Gemini 1.5 Flash** | 1,048,576 tokens (1M) | 8,192 tokens | High velocity (>100 tok/s), low TTFT, cost-efficient | Unit test writing, surgical bug fixes, git log inspection, code search |
  | **Gemini 1.5 Flash-8B** | 1,048,576 tokens (1M) | 8,192 tokens | Ultra-low latency, high volume throughput | Real-time linting, simple classification, AST label generation |
  | **Gemini 2.0 Flash** | 1,048,576 tokens (1M) | 8,192 tokens | Real-time multimodal streaming, enhanced native tool execution | Agentic loops, interactive debugging, MCP server coordination |
  | **Gemini 2.0 Flash Thinking** | 1,048,576 tokens (1M) | 8,192 tokens | Native visible reasoning tokens, mathematical & formal proofs | Concurrency race condition analysis, cryptographic/algorithmic logic |
  | **Gemini 2.0 Pro** | 2,097,152 tokens (2M) | 8,192 tokens | State-of-the-art programming, frontier reasoning | Complex multi-step migrations, high-stakes system redesign |

- **Routing Logic Flowchart**:
  - Triage / Quick Query / Single File Edit -> `Gemini 1.5 Flash` or `Gemini 2.0 Flash`.
  - Intricate Concurrency / Algorithmic Proof -> `Gemini 2.0 Flash Thinking`.
  - Global Cross-Module Architecture / Multi-Repo Ingestion -> `Gemini 1.5 Pro` or `Gemini 2.0 Pro`.

#### Section 3: Long Context Window Management & Optimization
- **Attention Bias & The Context Sandwich Pattern**:
  - Gemini transformers exhibit primacy and recency bias.
  - *Context Ordering Blueprint*:
    ```
    ┌─────────────────────────────────────────────────────────┐
    │ 1. System Instruction (systemInstruction API channel)   │
    │    - Identity, constraints, tool rules, output schema    │
    ├─────────────────────────────────────────────────────────┤
    │ 2. Reference Corpus (Middle / Background Context)       │
    │    - Full repository dump, API docs, database schemas   │
    ├─────────────────────────────────────────────────────────┤
    │ 3. Working Memory & Conversation History                │
    │    - Prior steps, verified facts, tool responses        │
    ├─────────────────────────────────────────────────────────┤
    │ 4. Immediate User Task & Recency Anchor (Bottom)        │
    │    - Specific problem statement, constraints reminder   │
    └─────────────────────────────────────────────────────────┘
    ```
- **Context Caching (`CachedContent`) Specifications**:
  - Minimum threshold: 32,768 tokens.
  - Recommended setup: Pre-cache static codebase dumps (`src/**`, OpenAPI specs, dependency trees).
  - Code snippet for creating a cache in Google GenAI SDK:
    ```typescript
    import { GoogleGenAI } from '@google/genai';
    const ai = new GoogleGenAI();
    const cache = await ai.cachedContents.create({
      model: 'models/gemini-1.5-pro-002',
      displayName: 'repo_codebase_cache',
      contents: [{ role: 'user', parts: [{ text: fullRepoDump }] }],
      ttl: '86400s', // 24-hour cache
    });
    ```
- **In-Context Stuffing vs Tool Search Rule**:
  - Do NOT stuff the context window if the question can be answered with `grep_search` or targeted file viewing.
  - Use in-context stuffing when synthesizing global patterns (e.g., verifying architecture compliance across all files).
- **Context Hygiene**:
  - Strip build artifacts (`target/`, `node_modules/`, `.git/`, `.class` files).
  - Truncate command line outputs to the relevant first/last 50 lines.

#### Section 4: Environment Configurations & Runtime Settings
- **Runtime Environment Specifications**:
  - Antigravity IDE: Settings in `.gemini/config/config.json`.
  - Gemini CLI: Environment variables and command flags.
  - Google GenAI SDK / Vertex AI: Standardized SDK initialization.
- **GenerationConfig Standards**:
  ```json
  {
    "temperature": 0.1,
    "topP": 0.95,
    "topK": 40,
    "maxOutputTokens": 8192,
    "responseMimeType": "text/plain"
  }
  ```
  *(For structured output tasks, use `"responseMimeType": "application/json"` with `"responseSchema"`)*.
- **Safety Settings (CRITICAL FOR DEVELOPER WORKFLOWS)**:
  - Explain why default Gemini safety filters can prematurely terminate coding sessions with `finishReason: SAFETY` when encountering shell scripts, SQL queries, regexes, or vulnerability patterns.
  - Mandatory configuration snippet:
    ```json
    [
      { "category": "HARM_CATEGORY_HARASSMENT", "threshold": "BLOCK_NONE" },
      { "category": "HARM_CATEGORY_HATE_SPEECH", "threshold": "BLOCK_NONE" },
      { "category": "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold": "BLOCK_NONE" },
      { "category": "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold": "BLOCK_NONE" },
      { "category": "HARM_CATEGORY_CIVIC_INTEGRITY", "threshold": "BLOCK_NONE" }
    ]
    ```

#### Section 5: Tool Calling & Function Execution Conventions
- **Native Gemini Tool Schema Definition**:
  - Full OpenAPI 3.0 subset specification.
  - Example schema for a file modification tool (`replace_file_content`).
- **Tool Calling Modes (`toolConfig`)**:
  - `AUTO`: Standard conversational + tool workflow.
  - `ANY`: Enforced tool usage (e.g., when the agent must gather facts before emitting code).
  - `NONE`: Text synthesis only.
  - `allowedFunctionNames`: Restricting tools to read-only during analysis phases.
- **Parallel Tool Calling Handling**:
  - Gemini may emit multiple `functionCall` blocks in one turn.
  - Runtime executes all calls asynchronously and returns matching `functionResponse` blocks.
- **Built-in Grounding Tools**:
  - `google_search`: Real-world grounding.
  - `code_execution`: Python execution sandbox.
- **Model Context Protocol (MCP)**:
  - Architecture connecting Antigravity/Gemini to external MCP servers.
  - Schema mapping and lazy vs eager tool discovery.

#### Section 6: Prompt Engineering & Instruction Protocols
- **Privileged System Instruction Channel**:
  - Why Gemini's `systemInstruction` parameter guarantees invariant instruction following across lengthy multi-turn sessions.
- **Tone & Directness**:
  - Zero conversational fluff.
  - No preamble ("Certainly!", "I can do that!").
  - Answer directly with facts, file diffs, or commands.
- **Reasoning Chains (Chain-of-Thought)**:
  - Explicit scratchpad pattern for Gemini 1.5:
    ```markdown
    <thought>
    1. Analyze the requested change: Modify UserRepository to use Optional<User>.
    2. Check dependencies: UserService calls findById().
    3. Plan: Edit UserRepository first, then update UserService call sites.
    </thought>
    ```
  - Gemini 2.0 Flash Thinking: Built-in reasoning tokens (`thinking_budget: 2048`).
- **Few-Shot Prompting Patterns**:
  - Concrete developer examples:
    - Example 1: Finding an interface implementation using `grep_search`.
    - Example 2: Performing a minimal replacement edit using `replace_file_content`.
    - Example 3: Handling a tool failure (e.g. file not found -> search by name -> retry).

#### Section 7: Multimodal Developer Workflows
- **Multimodal Token Economy**:
  - Images: ~258 tokens per tile (high-res images split into 1-4 tiles).
  - Video: 1 frame/sec (~258 tokens/sec).
  - Audio: ~32 tokens/sec.
  - PDFs: Native rendering as image pages.
- **Practical Developer Workflows**:
  - Visual regression testing: Feeding DOM structure + screenshot of unexpected layout.
  - Architecture extraction: Converting diagram images into Mermaid syntax.
  - Stack trace + Screenshot pairing for rapid frontend bug diagnosis.

#### Section 8: Code Modification & Operational Standards
- **Surgical Edits vs Full-File Rewrites**:
  - Full-file rewrites waste output tokens, introduce accidental formatting changes, and risk truncation at 8,192 tokens.
  - Enforce targeted search-and-replace (`replace_file_content` / unified diffs).
- **Non-Destructive Operations**:
  - Verification prior to deletion.
  - Git status verification before and after file operations.
- **Error Recovery Protocols**:
  - If a compilation or test fails, do NOT guess. Read the exact compiler error line and trace the AST.

#### Section 9: Project-Level Integration & Compliance Checklist
- **Harmonious Multi-Document Hierarchy**:
  - `ARCHITECTURE.md`: What the system architecture is (Hexagonal, ports, adapters).
  - `AGENTS.md`: How any general AI agent should behave in this project.
  - `GEMINI.md`: How Google Gemini agents configure their runtime, models, context, and prompts.
- **Compliance Checklist for Gemini Agents**:
  - [ ] System instruction channel configured.
  - [ ] Safety thresholds set to `BLOCK_NONE` for developer tasks.
  - [ ] Context caching utilized for codebases >32k tokens.
  - [ ] Context sandwich pattern applied for large prompts.
  - [ ] Parallel tool calling enabled in runtime.
  - [ ] Surgical replacement tools preferred over full-file overwrites.
  - [ ] Zero-preamble communication style enforced.

---

## 5. Verification Method

To verify the quality and completeness of this specification and the resulting `GEMINI.md`:

1. **Acceptance Criteria Validation** (from `ORIGINAL_REQUEST.md`):
   - Check that `GEMINI.md` exists in `c:\Users\parse-it\Documents\Git\AppService`.
   - Verify that it documents instructions, conventions, and configurations specific to Gemini agents.
2. **Schema & Configuration Syntax Integrity**:
   - Verify that all JSON configuration snippets (e.g. `safetySettings`, `generationConfig`, function schemas) in `GEMINI.md` are valid, syntactically correct JSON.
3. **Markdown Structural Verification**:
   - Verify all markdown headings, code blocks, tables, and Mermaid flowcharts render cleanly without syntax errors.
4. **Separation of Concerns Audit**:
   - Ensure `GEMINI.md` does not duplicate generic guidelines belonging in `AGENTS.md` (e.g., generic git workflows), but rather specializes in Google Gemini model dynamics, API parameters, context caching, multimodal tokens, and Gemini function calling.
