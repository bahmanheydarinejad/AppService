# Developer Runbooks & Operational Recipes

> **Authoritative Engineering Recipes, Workflows, and Checklists for AppService**  
> *Targeted step-by-step developer guides designed for rapid execution and minimal token consumption.*

---

> [!IMPORTANT]
> **Mandatory Task Completion Protocol**:
> Whenever a workflow, build target, configuration procedure, or engineering pattern changes, you **MUST** update this runbook immediately upon task completion.

---

## 1. Recipe: Adding a New Domain Entity & Migration

Follow these deterministic steps to introduce a new persistent domain model:

1. **Create Liquibase ChangeSet**:
   - Add a new migration file in `appservice-infrastructure-persistence/src/main/resources/db/changelog/changes/` (e.g. `17-create-invoice-table.yaml`).
   - Specify table columns, primary key constraint (`pk_invoice`), audit columns (`registered_date`, `updated_date`, `delete_date`), and default status `'ACTIVE_STATUS'`.
2. **Register in Master ChangeLog**:
   - Add an `include` entry inside [`db.changelog-master.yaml`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-infrastructure-persistence/src/main/resources/db/changelog/db.changelog-master.yaml).
3. **Create Java Entity Class**:
   - Inherit from [`BaseJpaEntity`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-infrastructure-persistence/src/main/java/ir/appservice/infrastructure/persistence/entity/BaseJpaEntity.java) in `appservice-infrastructure-persistence`.
   - Annotate with `@Entity`, `@Table(name = "...")`, `@Getter`, `@Setter`.
   - Ensure foreign keys use explicit JPA annotations (`@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn`).
4. **Compile & Validate**:
   - Execute `mvn clean compile` to confirm Hibernate DDL validation succeeds against the Liquibase schema.
5. **Update Contracts**:
   - Update [`documents/contracts/database-schema.md`](file:///c:/Users/Bahma/Documents/GitHub/AppService/documents/contracts/database-schema.md).

---

## 2. Recipe: Adding a Spring Data Repository & Adapter

1. **Create Outbound Port**:
   - In `appservice-core-domain/src/main/java/ir/appservice/core/domain/port/outbound`, define an interface (e.g. `UserRepositoryPort`).
2. **Create Spring Data Repository**:
   - In `appservice-infrastructure-persistence/src/main/java/ir/appservice/infrastructure/persistence/repository`, define a Spring Data JPA interface extending `JpaRepository` and `JpaSpecificationExecutor`.
3. **Implement Adapter**:
   - In `appservice-infrastructure-persistence/src/main/java/ir/appservice/infrastructure/persistence/adapter`, implement the domain outbound port by injecting the Spring Data repository.
4. **Targeted Verification**:
   - Run `mvn test -Dtest=MultiTenantLiquibaseJpaTest`.

---

## 3. Recipe: Adding a PrimeFaces Single-Page Panel View

1. **Create Backing Bean**:
   - In `appservice-ui-primefaces/src/main/java/ir/appservice/ui/bean`, create a managed bean (e.g. `MyManagementBean`).
   - Annotate with `@Named("myManagementBean")` and `@ViewScoped`.
2. **Create XHTML Panel Template**:
   - In `appservice-ui-primefaces/src/main/resources/META-INF/resources/panels/my_panel.xhtml`, create the panel snippet.
3. **Register Dynamic View in PanelHandler**:
   - In [`PanelHandler.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-ui-primefaces/src/main/java/ir/appservice/ui/bean/PanelHandler.java), add the panel routing enum and dynamic include case.
4. **Link to Dashboard**:
   - In [`dashboard.xhtml`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-ui-primefaces/src/main/resources/META-INF/resources/dashboard.xhtml), bind the sidebar navigation command button to `panelHandler.setActiveView('my_panel')`.

---

## 4. Recipe: Adding & Securing a REST Controller Endpoint

1. **Create Controller**:
   - In `appservice-api-rest/src/main/java/ir/appservice/api/rest/controller`, create a class extending [`BaseSearchController`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-api-rest/src/main/java/ir/appservice/api/rest/controller/BaseSearchController.java).
   - Annotate with `@RestController` and `@RequestMapping("/api/admin/...")`.
2. **Configure Security Rules**:
   - In `appservice-infrastructure-security/src/main/java/ir/appservice/infrastructure/security/config/SecurityConfig.java`, specify endpoint authorization rules (`@PreAuthorize("hasAuthority('ADMIN')")`).
3. **Exception Handling**:
   - Rely on [`GlobalRestExceptionHandler`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-api-rest/src/main/java/ir/appservice/api/rest/advice/GlobalRestExceptionHandler.java) for RFC 7807 ProblemDetail responses.
4. **Update API Contract**:
   - Record endpoint path, parameters, and status codes in [`documents/contracts/api-endpoints.md`](file:///c:/Users/Bahma/Documents/GitHub/AppService/documents/contracts/api-endpoints.md).

---

## 5. Recipe: Running Tests & Deterministic Verification

| Test Type | Scope | Command | Expected Outcome |
|---|---|---|---|
| **Full Aggregator CI Verification** | All 8 submodules + parent POM | `powershell -File .\scripts\verify-build.ps1` | `BUILD SUCCESS`, exit code 0 (120 tests pass). |
| **Quick Compilation** | Java syntax & dependencies | `mvn clean test-compile` | `BUILD SUCCESS`, exit code 0. |
| **Targeted Submodule Tests** | Single submodule | `mvn test -pl appservice-api-rest` | 0 failures, 0 errors. |
| **End-to-End System Tests** | Full architecture integration | `mvn test -Dtest=AppServiceEndToEndIntegrationTest` | 5/5 tests pass, exit code 0. |

---

## 6. Recipe: Switching Database Profiles

The active profile is controlled in [`appservice-ui-primefaces/src/main/resources/application.yml`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-ui-primefaces/src/main/resources/application.yml) via `spring.profiles.active`.

### Profile Matrix
- **`h2` (Default)**: Embedded file database located at `./data/appservice.mv.db`. Web console at `/h2-console`.
- **`postgres`**: Production-grade PostgreSQL database on `localhost:5432/appservice`. Uses `PostgreSQLDialect` and non-contextual LOB creation.
- **`mysql`**: MySQL 8.x database on `localhost:3306/appservice`.
- **`test`**: In-memory H2 database (`jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1`) utilized during automated test execution.

#### Command-Line Activation:
```bash
# Run application with PostgreSQL
mvn spring-boot:run -Dspring-boot.run.profiles=postgres

# Run application with H2 File DB
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

---

## 7. Recipe: Troubleshooting Common Gotchas

### Gotcha 1: AdminFilter vs. Spring Security Redirect Loop
- **Symptom**: Browser enters infinite redirect loop between `/signin` and `/index`.
- **Root Cause**: AdminFaces `AdminFilter` attempts to enforce its own authentication filter while Spring Security manages the filter chain.
- **Solution**: Keep `joinfaces.adminfaces.disable-filter: true` in `application.yml`. Spring Security exclusively owns authentication redirection.

### Gotcha 2: PostgreSQL LOB Creation Error
- **Symptom**: `org.postgresql.util.PSQLException: Large Objects may not be used in auto-commit mode`.
- **Solution**: Ensure `spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation: true` is set in `application-postgres.yml`.

### Gotcha 3: Liquibase Lock Contention
- **Symptom**: Application hangs at startup with `Waiting for changelog lock...`.
- **Solution**: Execute SQL: `UPDATE DATABASECHANGELOGLOCK SET LOCKED=0, LOCKGRANTED=NULL, LOCKEDBY=NULL WHERE ID=1;`.

---

## 8. Recipe: Code-Graph-RAG Impact Analysis & Knowledge Graph Sync

1. **Verify CGR Availability**:
   - Run `cgr --version` and `cgr doctor`.
   - If not installed, execute `.\scripts\setup-cgr.ps1` (or `pip install -r requirements-cgr.txt`).
2. **Impact Analysis Before Code Edits**:
   - Resolve symbol: `cgr graph resolve MyService`.
   - Inspect definition: `cgr graph definition <QualifiedName>`.
   - Discover callers (blast radius): `cgr graph callers <QualifiedName>`.
   - Discover callees (collaborators): `cgr graph callees <QualifiedName>`.
3. **Synchronize Graph Post-Edit**:
   - Run: `cgr start --repo-path . --update-graph --no-embeddings --exclude target`.
   - Verify counts: `cgr stats`.

---

## 9. Operational Checklists

### 9.1 Pre-Flight Checklist
- [ ] Understand requirements and locate target files.
- [ ] Run CGR impact analysis (`cgr graph callers <Symbol>`).
- [ ] Check repository status (`git status`).
- [ ] Verify baseline build (`mvn clean compile`).

### 9.2 Modification Checklist
- [ ] Apply minimal diff (no drive-by refactoring or reformatting).
- [ ] Follow SOLID, YAGNI, OOP/OOD, and AOP principles.
- [ ] Ensure proper layer encapsulation (`Controller` -> `Service` -> `Repository` -> `Entity`).

### 9.3 Verification Checklist
- [ ] Run targeted tests (`mvn test -Dtest=...`).
- [ ] Run full test suite (`mvn test`).
- [ ] Run doc test suite (`python -m unittest discover -s tests`).
- [ ] Check `git status` for untracked scratch files.

### 9.4 Mandatory Task Completion Checklist
- [ ] If an endpoint was altered, update [`documents/contracts/api-endpoints.md`](documents/contracts/api-endpoints.md).
- [ ] If a database table/column was altered, update [`documents/contracts/database-schema.md`](documents/contracts/database-schema.md).
- [ ] If a workflow or command was altered, update this file ([`documents/runbooks/recipes.md`](documents/runbooks/recipes.md)).
- [ ] Synchronize Code-Graph-RAG AST graph (`cgr start --repo-path . --update-graph --no-embeddings --exclude target`).
