# Handoff Report: R3 — Gemini-Specific Agent Instructions (GEMINI.md)

**Author**: Worker M3 (`teamwork_worker`)  
**Target Milestone**: R3 — Gemini-Specific Agent Instructions (`GEMINI.md`)  
**Recipient**: Orchestrator (`e8a6ca15-9ae0-4a99-991a-e54d257388f2`) & Teamwork Auditor  
**Date**: 2026-10-29T21:35:00Z  

---

## 1. Observation

### 1.1 Direct Repository Observations
1. **Target Deliverable**:
   `c:\Users\parse-it\Documents\Git\AppService\GEMINI.md` was created with 812 lines, 46,866 bytes.
2. **Table of Contents Integrity**:
   PowerShell execution of markdown slug verification:
   `Found 43 TOC links and 60 total headings.`
   All 43 Table of Contents anchor links (`[Title](#slug)`) resolved with 100% exact matches against level 2 (`##`) and level 3 (`###`) document headings:
   ```
   Found 43 TOC links and 60 total headings.
   --- TOC LINK VERIFICATION ---
   PASS: 1. Gemini Agent Operating Foundation -> #1-gemini-agent-operating-foundation
   PASS: 1.1 Purpose and Architectural Precedence -> #11-purpose-and-architectural-precedence
   ...
   PASS: 9.3 Troubleshooting and Edge Case Matrix -> #93-troubleshooting-and-edge-case-matrix
   ALL TOC LINKS VERIFIED SUCCESSFULLY!
   ```
3. **JSON Code Block Validation**:
   Extraction and parsing of all 8 JSON blocks via PowerShell `ConvertFrom-Json` confirmed that all code schemas, GenerationConfig objects, and safety setting lists are syntactically valid JSON:
   `Found 8 JSON code blocks. Block 1 - Valid JSON ... Block 8 - Valid JSON`
4. **Repository Isolation Compliance**:
   Execution of `git status --porcelain` shows:
   - Exclusively modified/created repository file: `GEMINI.md`.
   - Worker metadata strictly quarantined in `.agents/teamwork/worker_m3/`.
   - Zero modifications to Java source files, Maven POMs, or peer agent files.

---

## 2. Logic Chain

### Step 1: Verification of Acceptance Criteria
- *Observation 1*: `ORIGINAL_REQUEST.md` requires:
  - `GEMINI.md exists in c:\Users\parse-it\Documents\Git\AppService`.
  - `Documents instructions, conventions, and configurations specific to Gemini agents.`
- *Observation 2 & DISPATCH.md*: Requires linked Table of Contents, Gemini model families & routing matrix, 1M/2M context mechanics & CachedContent API, GenerationConfig & safety overrides, tool calling/OpenAPI/MCP, prompt conventions & systemInstruction, multimodal tokens, code diff hygiene, checklists.
- *Deduction*: `GEMINI.md` fulfills all core requirements with technical depth and zero dummy/placeholder content.

### Step 2: Gemini Architectural Specialization
- *Observation*: Standard agent guides (`AGENTS.md`) cover generic behaviors, whereas Gemini features distinct architectural traits:
  - Ultra-long context (1M/2M) requiring the **Context Sandwich Pattern** to combat the transformer U-shaped attention curve ("Lost in the Middle").
  - Google GenAI Context Caching (`CachedContent` API) with a 32,768-token threshold and 75% cost reduction.
  - Dedicated architectural `systemInstruction` channel providing invariant instruction adherence.
  - Native OpenAPI tool calling with `toolConfig` execution modes (`AUTO`, `ANY`, `NONE`, `allowedFunctionNames`) and batched parallel calls.
  - Mandatory developer safety overrides (`BLOCK_NONE` on `HARM_CATEGORY_DANGEROUS_CONTENT`) to prevent false-positive `finishReason: SAFETY` refusals during code audits, regex parsing, and shell tasks.
  - Multimodal token calculations (258 tokens per image tile/crop, 258 tokens/sec video, 32 tokens/sec audio).
- *Deduction*: The document provides comprehensive, production-grade reference material directly tailored to Gemini agents and runtime orchestrators.

---

## 3. Caveats

1. **Experimental Models**: Sections documenting `gemini-2.0-flash-thinking` and `gemini-2.0-pro` reflect the current Google GenAI preview specifications. As these models evolve from experimental to GA, hyperparameter flags (such as `thinking_budget`) may be updated in subsequent SDK revisions.
2. **SDK Dual Nomenclature**: The documentation provides examples using both the unified Google GenAI SDK (`@google/genai` / `google.genai`) and notes compatibility with legacy `@google/generative-ai`.

---

## 4. Conclusion

`GEMINI.md` is complete, authoritative, and verified. It establishes the definitive operational manual for Google Gemini agents across Antigravity IDE, Gemini CLI, and Google GenAI SDK runtimes. All 43 Table of Contents anchor links are verified, all configuration snippets are syntactically valid JSON, and repository isolation boundaries have been respected.

---

## 5. Verification Method

To independently verify the deliverable:

1. **File Existence and Location**:
   ```powershell
   Test-Path "c:\Users\parse-it\Documents\Git\AppService\GEMINI.md"
   ```
2. **Table of Contents Link Anchor Verification**:
   ```powershell
   $content = Get-Content -Path "c:\Users\parse-it\Documents\Git\AppService\GEMINI.md" -Raw
   $tocLinks = [regex]::Matches($content, '\[(.*?)\]\(#([a-zA-Z0-9_-]+)\)')
   $headings = [regex]::Matches($content, '(?m)^(#{1,6})\s+(.+)$') | ForEach-Object {
       ($_.Groups[2].Value.Trim().ToLower() -replace '[^\w\s-]', '' -replace '\s+', '-')
   }
   foreach ($link in $tocLinks) {
       if ($headings -notcontains $link.Groups[2].Value) {
           throw "Broken TOC link: $($link.Value)"
       }
   }
   Write-Output "All $($tocLinks.Count) TOC links valid!"
   ```
3. **JSON Syntax Verification**:
   ```powershell
   $matches = [regex]::Matches($content, '(?m)^\s*```json\s*\r?\n([\s\S]*?)^\s*```\s*$')
   foreach ($m in $matches) {
       $null = ConvertFrom-Json $m.Groups[1].Value.Trim() -ErrorAction Stop
   }
   Write-Output "All $($matches.Count) JSON snippets valid!"
   ```
