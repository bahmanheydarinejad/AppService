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
   - Add a new migration file in `src/main/resources/db/changelog/changes/` (e.g. `09-create-invoice-table.yaml`).
   - Specify table columns, primary key constraint (`pk_invoice`), audit columns (`registered_date`, `updated_date`, `delete_date`), and default status `'ACTIVE_STATUS'`.
2. **Register in Master ChangeLog**:
   - Add an `include` entry inside [`src/main/resources/db/changelog/db.changelog-master.yaml`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/resources/db/changelog/db.changelog-master.yaml).
3. **Create Java Entity Class**:
   - Inherit from [`BaseEntity`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/model/entity/BaseEntity.java) in `ir.appservice.model.entity.domain` or `ir.appservice.model.entity.application`.
   - Annotate with `@Entity`, `@Table(name = "...")`, `@Getter`, `@Setter`.
   - Ensure foreign keys use explicit JPA annotations (`@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn`).
4. **Compile & Validate**:
   - Execute `mvn clean compile` to confirm Hibernate DDL validation succeeds against the Liquibase schema.
5. **Update Contracts**:
   - Update [`documents/contracts/database-schema.md`](file:///c:/Users/Bahma/Documents/GitHub/AppService/documents/contracts/database-schema.md).

---

## 2. Recipe: Adding a Repository, Service & Converter

1. **Create Spring Data Repository**:
   - In `ir.appservice.model.repository`, define an interface extending [`CrudRepository<T, String>`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/model/repository/CrudRepository.java).
   - Add targeted query methods (e.g. `findByDisplayNameIgnoreCase`).
2. **Create Service Layer**:
   - In `ir.appservice.model.service`, define a `@Service` class extending [`CrudService<T>`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/model/service/CrudService.java).
   - Inject repository via constructor: `public MyService(MyRepository repository) { super(repository, MyEntity.class); }`.
   - Implement domain-specific business validations or transactional methods (`@Transactional`).
3. **Create JSF Converter**:
   - In `ir.appservice.model.converter`, create a converter extending `BaseConverter<T>`.
   - Annotate with `@Component` and `@FacesConverter(value = "myEntityConverter", managed = true)`.
4. **Targeted Verification**:
   - Run `mvn test -Dtest=AppServiceTests`.

---

## 3. Recipe: Adding an AdminFaces / PrimeFaces CRUD Panel View

1. **Create Backing Bean**:
   - In `ir.appservice.view.beanComponents.panelBean.crudBean`, create a managed bean extending [`BaseLazyCrudBean<T>`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/view/beanComponents/panelBean/BaseLazyCrudBean.java) or [`BaseCrudBean<T>`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/view/beanComponents/panelBean/crudBean/BaseCrudBean.java).
   - Annotate with `@Named("myCrudBean")` and `@ViewScoped`.
2. **Create XHTML Panel Templates**:
   - In `src/main/resources/META-INF/resources/panels/crud_panels/my_panel/`, create:
     - `my_panel.xhtml` (Main entry containing `p:dataTable` and action buttons).
     - `add_dialog.xhtml` (Dialog for creating records).
     - `edit_dialog.xhtml` (Dialog for editing records).
     - `view_dialog.xhtml` (Read-only preview dialog).
3. **Register Menu & Panel in Database**:
   - Add initialization logic in [`AppInitializer.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/configuration/initializer/AppInitializer.java) to associate a new `Menu` entry with the `Panel(resourceLocation = "/panels/crud_panels/my_panel/my_panel.xhtml")`.
4. **Assign Role Permissions**:
   - Link the newly created `Panel` to appropriate `Role` entities in `role_panels`.

---

## 4. Recipe: Adding & Securing a REST Controller Endpoint

1. **Create Controller**:
   - In `ir.appservice.controller`, create a class extending [`BaseController`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/controller/BaseController.java).
   - Annotate with `@RestController` and `@RequestMapping("/api/v1/...")`.
2. **Configure Security Rules**:
   - In [`AppSecurity.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/configuration/AppSecurity.java), determine whether the endpoint is public or protected:
     - If public: add route pattern to `.requestMatchers(...)` under `.permitAll()`.
     - If protected: leave under `.anyRequest().authenticated()`.
3. **Exception Handling**:
   - Use exceptions from `ir.appservice.configuration.exception` (`NotFoundEntityException`, `DuplicateEntityException`).
4. **Update API Contract**:
   - Record endpoint path, parameters, and status codes in [`documents/contracts/api-endpoints.md`](file:///c:/Users/Bahma/Documents/GitHub/AppService/documents/contracts/api-endpoints.md).

---

## 5. Recipe: Running Tests & Deterministic Verification

| Test Type | Scope | Command | Expected Outcome |
|---|---|---|---|
| **Quick Compilation** | Java syntax & dependencies | `mvn clean compile` | `BUILD SUCCESS`, exit code 0. |
| **Targeted Unit Test** | Single test class | `mvn test -Dtest=AppServiceTests` | 0 failures, 0 errors. |
| **DB Integration Test** | Liquibase + H2 bootstrap | `mvn test -Dtest=DatabaseConfigurationIntegrationTest` | 0 failures, 0 errors. |
| **Full Java Test Suite**| All unit & integration tests | `mvn test` | Tests run: 9, Failures: 0, Errors: 0. |
| **Documentation E2E** | Markdown & Anchor links | `python -m unittest discover -s tests` | Ran 42 tests, OK. |

---

## 6. Recipe: Switching Database Profiles

The active profile is controlled in [`src/main/resources/application.yml`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/resources/application.yml) via `spring.profiles.active`.

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
