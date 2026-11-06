# AppService Developer & Agent Reference Guide

> **Primary Fast-Path Reference for Developers and AI Coding Assistants**  
> *Single entry point for AppService architecture, commands, invariants, contracts, and software engineering principles.*

---

## 1. Project Overview & Technology Stack

**AppService** is an enterprise management platform built on Spring Boot and JoinFaces (JSF / PrimeFaces / AdminFaces):

- **Backend Runtime**: Java 17, Spring Boot 4.1.1, Spring Framework 7.
- **Frontend / UI**: JoinFaces 6.1.1, Mojarra 4 (Jakarta Faces), PrimeFaces 14, AdminFaces (AdminLTE theme for JSF).
- **Persistence & Migrations**: Spring Data JPA, Hibernate, Liquibase (declarative YAML migrations).
- **Security**: Spring Security 7 (custom `SecurityFilterChain`, `ProviderManager` with `AccountService`, JSF `AuthenticationBean`).
- **Database Profiles**:
  - `h2`: File-based database (`./data/appservice.mv.db`) with web console at `/h2-console`.
  - `postgres`: PostgreSQL 14+ on `localhost:5432/appservice`.
  - `mysql`: MySQL 8.x on `localhost:3306/appservice`.
  - `test`: In-memory H2 (`jdbc:h2:mem:testdb`) for zero-dependency test execution.

---

## 2. High-Density Token Optimization & File Routing Matrix

To maximize execution speed and minimize token consumption, **never scan entire directories**. Consult the single authoritative document corresponding to your task:

| If Your Task Involves... | Read This File ONLY | Token Impact |
|---|---|---|
| **API endpoints, REST controllers, routes, security filters** | [`documents/contracts/api-endpoints.md`](file:///c:/Users/Bahma/Documents/GitHub/AppService/documents/contracts/api-endpoints.md) | ~1.5k tokens (replaces scanning all controllers) |
| **Database tables, JPA entities, Liquibase changelogs, columns** | [`documents/contracts/database-schema.md`](file:///c:/Users/Bahma/Documents/GitHub/AppService/documents/contracts/database-schema.md) | ~2.5k tokens (replaces scanning 10 entities + 8 YAMLs) |
| **Step-by-step recipes, adding beans/panels, troubleshooting** | [`documents/runbooks/recipes.md`](file:///c:/Users/Bahma/Documents/GitHub/AppService/documents/runbooks/recipes.md) | ~2k tokens (replaces guessing patterns) |
| **Hexagonal boundaries, ports & adapters, domain isolation** | [`ARCHITECTURE.md`](file:///c:/Users/Bahma/Documents/GitHub/AppService/ARCHITECTURE.md) | Global architectural blueprint |
| **Universal agent boundaries, verification ladder, git rules** | [`AGENTS.md`](file:///c:/Users/Bahma/Documents/GitHub/AppService/AGENTS.md) | Universal agent operational standard |
| **Gemini model routing, context caching, prompt schemas** | [`GEMINI.md`](file:///c:/Users/Bahma/Documents/GitHub/AppService/GEMINI.md) | Gemini runtime manual |

---

## 3. Essential Developer & Build Commands

```bash
# Clean and compile Java classes
mvn clean compile

# Run targeted unit/integration test
mvn test -Dtest=AppServiceTests
mvn test -Dtest=DatabaseConfigurationIntegrationTest

# Run all Java unit and integration tests
mvn test

# Run documentation & anchor integrity E2E tests
python -m unittest discover -s tests

# Run Spring Boot application (default H2 profile)
mvn spring-boot:run

# Run with PostgreSQL profile
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

---

## 4. Hard Invariants & Negative Constraints

1. **Never Enable AdminFaces Filter**: Keep `joinfaces.adminfaces.disable-filter: true` in `application.yml`. Enabling it creates an infinite redirect loop with Spring Security.
2. **Never Let Hibernate Mutate Schema**: Liquibase owns all DDL mutations. `spring.jpa.hibernate.ddl-auto` must remain `validate`.
3. **Always Respect Soft-Delete**: Entities extend `BaseEntity`. Never write `DELETE FROM` SQL in business services; use `deleteDate` timestamps.
4. **No Drive-by Refactoring**: Limit code modifications strictly to the minimal lines needed to satisfy requirements.
5. **Deterministic Verification Required**: Every claim of completion must be backed by exit code 0 test executions.

---

## 5. Software Development Principles in AppService

### 5.1 SOLID Principles
- **S (Single Responsibility)**: Strictly decouple Controllers (HTTP), Services (Business), Repositories (Data Access), Converters (Type translation), and JSF Beans (UI state).
- **O (Open/Closed)**: Extend base abstractions ([`BaseEntity`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/model/entity/BaseEntity.java), [`CrudService`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/model/service/CrudService.java), [`BaseCrudBean`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/view/beanComponents/panelBean/crudBean/BaseCrudBean.java)) rather than modifying shared core methods.
- **L (Liskov Substitution)**: All entity classes must adhere to `BaseEntity` equality and identity contracts.
- **I (Interface Segregation)**: Repository interfaces extend focused contracts (`CrudRepository`, `JpaSpecificationExecutor`).
- **D (Dependency Inversion)**: High-level modules depend on abstractions (Spring Service and Repository interfaces), wired via constructor injection.

### 5.2 YAGNI (You Aren't Gonna Need It)
- Do not build speculative generic wrappers, unused entity fields, or premature caching layers.
- Implement only the features requested for the current task.

### 5.3 OOP & OOD
- Maintain encapsulation: backing beans manage private UI state with getters/setters.
- Favor composition over inheritance: backing beans compose service dependencies rather than inheriting business logic.

### 5.4 AOP (Aspect-Oriented Programming)
- Keep cross-cutting concerns (transaction management `@Transactional`, security filtering `AppSecurityFilter`, auditing) isolated in aspects and filters, never entangled in domain services.

### 5.5 Architectural & In-Code Design Patterns
- **Architectural**: Hexagonal Ports & Adapters, Layered Architecture, MVVM for JSF (ManagedBeans as ViewModels binding to PrimeFaces components).
- **GoF In-Code**:
  - *Template Method*: `BaseConverter`, `BaseCrudBean`, `BaseLazyCrudBean`.
  - *Repository*: `CrudRepository` and entity-specific repositories.
  - *Service Layer*: `CrudService` encapsulates business transactions.
  - *Adapter / Converter*: JSF Converters (`AccountConverter`, `RoleConverter`) bridging String IDs to domain entities.

---

## 6. Mandatory Task Completion Protocol

> **On completing ANY task, you MUST update the affected contract/doc files before finishing:**
> 1. Changed an endpoint or route? -> Update [`documents/contracts/api-endpoints.md`](file:///c:/Users/Bahma/Documents/GitHub/AppService/documents/contracts/api-endpoints.md).
> 2. Changed a DB entity, column, or migration? -> Update [`documents/contracts/database-schema.md`](file:///c:/Users/Bahma/Documents/GitHub/AppService/documents/contracts/database-schema.md).
> 3. Changed architecture or core contracts? -> Update [`ARCHITECTURE.md`](file:///c:/Users/Bahma/Documents/GitHub/AppService/ARCHITECTURE.md) and [`AGENTS.md`](file:///c:/Users/Bahma/Documents/GitHub/AppService/AGENTS.md).
> 4. Changed a workflow or recipe? -> Update [`documents/runbooks/recipes.md`](file:///c:/Users/Bahma/Documents/GitHub/AppService/documents/runbooks/recipes.md).
