# Dispatch for Explorer Survey 1

## Identity
- Role: Explorer Survey 1
- Type: teamwork_explorer
- Working directory: c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\explorer_survey_1\

## Task Objective
Investigate and specify the comprehensive requirements, structure, and best practices for **R1: Hexagonal Architecture Guide (ARCHITECTURE.md)** as requested in `c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\ORIGINAL_REQUEST.md`.

## Key Scope & Details to Explore:
1. Core concepts of Hexagonal Architecture (Alistair Cockburn's Ports and Adapters):
   - Core Domain / Business Logic (Entities, Value Objects, Use Cases / Interactors, Domain Services).
   - Driving / Primary Ports (Inbound interfaces, API/CLI/UI drivers).
   - Driven / Secondary Ports (Outbound interfaces, Database, Messaging, External services).
   - Driving Adapters vs Driven Adapters.
   - The Dependency Inversion Rule (dependencies point inward toward the core domain).
2. Document Design & Visual Requirements:
   - Section-based document structure with a linked Table of Contents at the top.
   - Explicit anchor link convention so every TOC entry is a working markdown link referencing its corresponding heading.
   - At least one comprehensive Mermaid diagram illustrating boundaries, primary/secondary ports & adapters, and data/dependency flow.
   - Markdown visual callouts/blockquotes (`> [!NOTE]`, `> [!TIP]`, `> [!IMPORTANT]`, `> [!WARNING]`, etc.) and badges/emojis across major sections.
   - Code/architecture examples in pseudo-code or clean multi-language agnostic examples showing decoupling.
   - Common anti-patterns and pitfalls (leaky abstractions, anemic domain models, circular dependencies).
   - General architectural guide for software developers not tied to a single repository.

## Deliverable
Write your findings and recommendation to `c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\explorer_survey_1\handoff.md` and send a completion message with summary.


## 2026-10-29T21:17:38Z
You are Explorer Survey 1 (teamwork_explorer).
Your working directory is: c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\explorer_survey_1\
Read your dispatch at: c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\explorer_survey_1\DISPATCH.md
Read the user request at: c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\ORIGINAL_REQUEST.md

Investigate and specify the comprehensive requirements, structure, and best practices for R1: Hexagonal Architecture Guide (ARCHITECTURE.md).
Cover all concepts: Core Domain, Inbound/Driving Ports, Outbound/Driven Ports, Adapters, Dependency Inversion, Mermaid diagram requirements, markdown styling (callouts, blockquotes, badges, emojis), and fully functional table of contents with markdown links.
Write your detailed report to: c:\Users\parse-it\Documents\Git\AppService\.agents\teamwork\explorer_survey_1\handoff.md
Send a completion message back to the orchestrator when finished.
