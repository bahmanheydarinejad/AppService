Create ARCHITECTURE.md as a comprehensive hexagonal architecture guide and best-practices reference for software developers, along with AGENTS.md and GEMINI.md defining agent guidelines and workflows.

Working directory: c:\Users\parse-it\Documents\Git\AppService
Integrity mode: development

## Requirements

### R1. Hexagonal Architecture Guide (ARCHITECTURE.md)
Create a comprehensive, general hexagonal architecture guide for software developers not tied to a specific codebase. The document must explain key concepts (core domain, driving/primary and driven/secondary ports, adapters, dependency inversion rule) and architectural best practices. It must be section-based with a linked table of contents linking directly to each corresponding section, and use visual styling elements including Mermaid diagrams, markdown callouts/blockquotes, and badges or emojis.

### R2. Agent Best Practices Guide (AGENTS.md)
Create AGENTS.md establishing default best practices for AI agents, including core agent workflows, instructions, project interaction guidelines, and standards for task execution.

### R3. Gemini-Specific Agent Instructions (GEMINI.md)
Create a separate GEMINI.md specifying Gemini-specific agent instructions, environment configurations, and prompt conventions.

## Acceptance Criteria

### ARCHITECTURE.md Structure and Content
- [ ] ARCHITECTURE.md exists in c:\Users\parse-it\Documents\Git\AppService.
- [ ] Contains a Table of Contents at the top where every entry is a working markdown link referencing its corresponding section heading in the document.
- [ ] Covers key hexagonal architecture concepts: Ports (primary/driving and secondary/driven), Adapters, Domain Core/Entities, and the Dependency Rule.
- [ ] Includes at least one Mermaid diagram illustrating hexagonal architecture boundaries and data/dependency flow.
- [ ] Uses markdown visual callouts/blockquotes and badges or emojis across major sections.
- [ ] Content is presented as a general architectural guide for software developers rather than tied to a single repository.

### AGENTS.md Structure and Content
- [ ] AGENTS.md exists in c:\Users\parse-it\Documents\Git\AppService.
- [ ] Documents default best practices, behavioral guidelines, and operational standards for AI agents.

### GEMINI.md Structure and Content
- [ ] GEMINI.md exists in c:\Users\parse-it\Documents\Git\AppService.
- [ ] Documents instructions, conventions, and configurations specific to Gemini agents.
