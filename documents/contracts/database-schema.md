# Database Schema Contract & Model Mapping

> **Authoritative Specification & Runtime Contract for AppService Database Architecture**  
> *Targeted dense reference for AI agents and developers. Consult this contract instead of scanning Liquibase changelogs or JPA entity files.*

---

> [!IMPORTANT]
> **Mandatory Task Completion Protocol**:
> Whenever a database table, column, index, constraint, or entity mapping is created or altered, you **MUST** update this file immediately upon task completion.

---

## 1. Schema Conventions & Global Invariants

- **Primary Keys**: All primary keys are `VARCHAR(255)` UUID strings mapped from [`BaseJpaEntity.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-infrastructure-persistence/src/main/java/ir/appservice/infrastructure/persistence/entity/BaseJpaEntity.java).
- **Soft-Delete Paradigm**: Records are never hard-deleted in business workflows. When deleted, `delete_date` is populated with the current timestamp. Active queries MUST filter using `WHERE delete_date IS NULL`.
- **Lifecycle Status**: The `statue` column defaults to `'ACTIVE_STATUS'` (constants: `ACTIVE_STATUS`, `DE_ACTIVE_STATUS`).
- **Audit Columns**: Present across all tables via inheritance from `BaseJpaEntity`:
  - `registered_date` (`TIMESTAMP`): Creation timestamp.
  - `updated_date` (`TIMESTAMP`): Last modification timestamp.
  - `delete_date` (`TIMESTAMP`, nullable): Soft-deletion timestamp.
- **Migration Ownership**: Liquibase owns and executes all DDL mutations (`appservice-infrastructure-persistence/src/main/resources/db/changelog/db.changelog-master.yaml`). Hibernate `ddl-auto` is set to `validate`.

---

## 2. Table-to-Entity Mapping Catalog

### 2.1 Table: `document`
- **JPA Entity**: [`ir.appservice.model.entity.domain.Document`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/model/entity/domain/Document.java)
- **Liquibase ChangeSet**: `01-create-document-table.yaml`
- **Description**: Binary and media storage (avatars, attachments, static docs).

| Column Name | SQL Type | Constraints & Defaults | JPA Field | Description |
|---|---|---|---|---|
| `id` | `VARCHAR(255)` | `PRIMARY KEY`, `NOT NULL` (`pk_document`) | `String id` | Unique identifier. |
| `display_name` | `VARCHAR(255)` | Nullable | `String displayName` | File or document label. |
| `statue` | `VARCHAR(255)` | Default `'ACTIVE_STATUS'` | `String statue` | Entity status. |
| `registered_date` | `TIMESTAMP` | Nullable | `Date registeredDate` | Upload timestamp. |
| `updated_date` | `TIMESTAMP` | Nullable | `Date updatedDate` | Update timestamp. |
| `delete_date` | `TIMESTAMP` | Nullable | `Date deleteDate` | Soft-delete flag. |
| `type` | `VARCHAR(255)` | Nullable | `String type` | MIME type (e.g. `image/png`). |
| `description` | `VARCHAR(255)` | Nullable | `String description` | File notes. |
| `size` | `BIGINT` | `NOT NULL` | `long size` | Byte size of content. |
| `data` | `BLOB` | `NOT NULL` | `byte[] data` | Raw binary payload. |

---

### 2.2 Table: `natural_person`
- **JPA Entity**: [`ir.appservice.model.entity.domain.NaturalPerson`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/model/entity/domain/NaturalPerson.java)
- **Liquibase ChangeSet**: `02-create-natural-person-table.yaml`
- **Description**: Human biographical identity data.

| Column Name | SQL Type | Constraints & Defaults | JPA Field | Description |
|---|---|---|---|---|
| `id` | `VARCHAR(255)` | `PRIMARY KEY`, `NOT NULL` (`pk_natural_person`) | `String id` | Unique identifier. |
| `display_name` | `VARCHAR(255)` | Nullable | `String displayName` | Formatted full name. |
| `statue` | `VARCHAR(255)` | Default `'ACTIVE_STATUS'` | `String statue` | Entity status. |
| `registered_date` | `TIMESTAMP` | Nullable | `Date registeredDate` | Creation timestamp. |
| `updated_date` | `TIMESTAMP` | Nullable | `Date updatedDate` | Update timestamp. |
| `delete_date` | `TIMESTAMP` | Nullable | `Date deleteDate` | Soft-delete flag. |
| `first_name` | `VARCHAR(255)` | Nullable | `String firstName` | Given name. |
| `last_name` | `VARCHAR(255)` | Nullable | `String lastName` | Family surname. |
| `mobile_number` | `VARCHAR(255)` | Nullable | `String mobileNumber` | Contact phone. |
| `birth_date` | `TIMESTAMP` | Nullable | `Date birthDate` | Date of birth. |
| `avatar_id` | `VARCHAR(255)` | `FK -> document(id)`, `INDEX: idx_natural_person_avatar` | `Document avatar` | Foreign key to profile image. |

---

### 2.3 Table: `account`
- **JPA Entity**: [`ir.appservice.model.entity.application.Account`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/model/entity/application/Account.java)
- **Liquibase ChangeSet**: `03-create-account-table.yaml`
- **Description**: Authentication, user credentials, and security principal.

| Column Name | SQL Type | Constraints & Defaults | JPA Field | Description |
|---|---|---|---|---|
| `id` | `VARCHAR(255)` | `PRIMARY KEY`, `NOT NULL` (`pk_account`) | `String id` | Unique identifier. |
| `display_name` | `VARCHAR(255)` | Nullable | `String displayName` | Profile name. |
| `statue` | `VARCHAR(255)` | Default `'ACTIVE_STATUS'` | `String statue` | Entity status. |
| `registered_date` | `TIMESTAMP` | Nullable | `Date registeredDate` | Registration timestamp. |
| `updated_date` | `TIMESTAMP` | Nullable | `Date updatedDate` | Update timestamp. |
| `delete_date` | `TIMESTAMP` | Nullable | `Date deleteDate` | Soft-delete flag. |
| `account_name` | `VARCHAR(255)` | Nullable | `String accountName` | Login username / identifier. |
| `tenant_id` | `VARCHAR(255)` | Default `'default'`, `INDEX: idx_account_tenant`, Composite `UNIQUE (account_name, tenant_id)` (`uk_account_name_tenant`) | `String tenantId` | Multi-tenant organization identifier. |
| `password` | `VARCHAR(255)` | Nullable | `String password` | Hashed credential. |
| `email` | `VARCHAR(255)` | Nullable | `String email` | User email address. |
| `mobile_number` | `VARCHAR(255)` | Nullable | `String mobileNumber` | Contact telephone. |
| `failed_attempts` | `INT` | Default `0` | `int failedAttempts` | Consecutive failed login counter. |
| `last_login_date` | `TIMESTAMP` | Nullable | `Date lastLoginDate` | Timestamp of last successful login. |
| `natural_person_id`| `VARCHAR(255)`| `FK -> natural_person(id)`, `INDEX: idx_account_natural_person` | `NaturalPerson naturalPerson` | Associated personal identity. |
| `avatar_id` | `VARCHAR(255)` | `FK -> document(id)`, `INDEX: idx_account_avatar` | `Document avatar` | User avatar document. |

---

### 2.4 Table: `role`
- **JPA Entity**: [`ir.appservice.model.entity.application.Role`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/model/entity/application/Role.java)
- **Liquibase ChangeSet**: `04-create-role-table.yaml`
- **Description**: RBAC authorization roles.

| Column Name | SQL Type | Constraints & Defaults | JPA Field | Description |
|---|---|---|---|---|
| `id` | `VARCHAR(255)` | `PRIMARY KEY`, `NOT NULL` (`pk_role`) | `String id` | Unique identifier. |
| `display_name` | `VARCHAR(255)` | Nullable | `String displayName` | Role label (e.g. `ADMIN`). |
| `description` | `VARCHAR(255)` | Nullable | `String description` | Functional role description. |
| `tenant_id` | `VARCHAR(255)` | Default `'default'`, `INDEX: idx_role_tenant` | `String tenantId` | Multi-tenant organization identifier. |
| `statue` | `VARCHAR(255)` | Default `'ACTIVE_STATUS'` | `String statue` | Entity status. |
| `registered_date` | `TIMESTAMP` | Nullable | `Date registeredDate` | Creation timestamp. |
| `updated_date` | `TIMESTAMP` | Nullable | `Date updatedDate` | Update timestamp. |
| `delete_date` | `TIMESTAMP` | Nullable | `Date deleteDate` | Soft-delete flag. |

---

### 2.5 Tables: `menu` and `panel`
- **JPA Entities**: [`ir.appservice.model.entity.application.ui.Menu`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/model/entity/application/ui/Menu.java), [`ir.appservice.model.entity.application.ui.Panel`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/model/entity/application/ui/Panel.java)
- **Liquibase ChangeSet**: `05-create-menu-panel-tables.yaml`
- **Description**: Dynamic JSF sidebar navigation tree and dashboard view panels.

#### Table: `menu`
| Column Name | SQL Type | Constraints & Defaults | JPA Field | Description |
|---|---|---|---|---|
| `id` | `VARCHAR(255)` | `PRIMARY KEY`, `NOT NULL` (`pk_menu`) | `String id` | Unique identifier. |
| `display_name` | `VARCHAR(255)` | Nullable | `String displayName` | Menu title. |
| `statue` | `VARCHAR(255)` | Default `'ACTIVE_STATUS'` | `String statue` | Entity status. |
| `registered_date` | `TIMESTAMP` | Nullable | `Date registeredDate` | Creation timestamp. |
| `updated_date` | `TIMESTAMP` | Nullable | `Date updatedDate` | Update timestamp. |
| `delete_date` | `TIMESTAMP` | Nullable | `Date deleteDate` | Soft-delete flag. |
| `priority` | `INT` | `NOT NULL` | `int priority` | Sort display order. |
| `type` | `VARCHAR(255)` | Nullable | `String type` | Menu category. |
| `icon` | `VARCHAR(255)` | Nullable | `String icon` | FontAwesome / Glyphicon icon. |
| `parent_id` | `VARCHAR(255)` | `FK -> menu(id)`, `INDEX: idx_menu_parent` | `Menu parent` | Hierarchical parent menu. |

#### Table: `panel`
| Column Name | SQL Type | Constraints & Defaults | JPA Field | Description |
|---|---|---|---|---|
| `id` | `VARCHAR(255)` | `PRIMARY KEY`, `NOT NULL` (`pk_panel`) | `String id` | Unique identifier. |
| `display_name` | `VARCHAR(255)` | Nullable | `String displayName` | Panel title. |
| `statue` | `VARCHAR(255)` | Default `'ACTIVE_STATUS'` | `String statue` | Entity status. |
| `registered_date` | `TIMESTAMP` | Nullable | `Date registeredDate` | Creation timestamp. |
| `updated_date` | `TIMESTAMP` | Nullable | `Date updatedDate` | Update timestamp. |
| `delete_date` | `TIMESTAMP` | Nullable | `Date deleteDate` | Soft-delete flag. |
| `resource_location`| `VARCHAR(255)`| Nullable | `String resourceLocation` | Path to `.xhtml` panel template. |
| `menu_id` | `VARCHAR(255)` | `FK -> menu(id)`, `INDEX: idx_panel_menu` | `Menu menu` | Linked menu item. |

---

### 2.6 Tables: `email` and `reset_password_token`
- **JPA Entities**: [`ir.appservice.model.entity.application.Email`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/model/entity/application/Email.java), [`ir.appservice.model.entity.application.ResetPasswordToken`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/model/entity/application/ResetPasswordToken.java)
- **Liquibase ChangeSet**: `06-create-email-token-tables.yaml`
- **Description**: Outbound email logs and password reset verification tokens.

#### Table: `email`
| Column Name | SQL Type | Constraints & Defaults | JPA Field | Description |
|---|---|---|---|---|
| `id` | `VARCHAR(255)` | `PRIMARY KEY`, `NOT NULL` (`pk_email`) | `String id` | Unique identifier. |
| `display_name` | `VARCHAR(255)` | Nullable | `String displayName` | Email summary. |
| `statue` | `VARCHAR(255)` | Default `'ACTIVE_STATUS'` | `String statue` | Delivery status. |
| `registered_date` | `TIMESTAMP` | Nullable | `Date registeredDate` | Log creation timestamp. |
| `updated_date` | `TIMESTAMP` | Nullable | `Date updatedDate` | Update timestamp. |
| `delete_date` | `TIMESTAMP` | Nullable | `Date deleteDate` | Soft-delete flag. |
| `mail_from` | `VARCHAR(255)` | Nullable | `String mailFrom` | Sender address. |
| `reply_to` | `VARCHAR(255)` | Nullable | `String replyTo` | Reply address. |
| `mail_to` | `VARCHAR(255)` | Nullable | `String mailTo` | Destination recipient. |
| `mail_cc` | `VARCHAR(255)` | Nullable | `String mailCc` | Carbon copy recipients. |
| `mail_bcc` | `VARCHAR(255)` | Nullable | `String mailBcc` | Blind carbon copy. |
| `sent_date` | `TIMESTAMP` | Nullable | `Date sentDate` | Dispatch timestamp. |
| `subject` | `VARCHAR(255)` | Nullable | `String subject` | Email subject header. |
| `text` | `VARCHAR(255)` | Nullable | `String text` | Email body content. |

#### Table: `reset_password_token`
| Column Name | SQL Type | Constraints & Defaults | JPA Field | Description |
|---|---|---|---|---|
| `id` | `VARCHAR(255)` | `PRIMARY KEY`, `NOT NULL` (`pk_reset_password_token`)| `String id` | Unique identifier. |
| `display_name` | `VARCHAR(255)` | Nullable | `String displayName` | Token label. |
| `statue` | `VARCHAR(255)` | Default `'ACTIVE_STATUS'` | `String statue` | Validity status. |
| `registered_date` | `TIMESTAMP` | Nullable | `Date registeredDate` | Issuance timestamp. |
| `updated_date` | `TIMESTAMP` | Nullable | `Date updatedDate` | Update timestamp. |
| `delete_date` | `TIMESTAMP` | Nullable | `Date deleteDate` | Soft-delete flag. |
| `account_id` | `VARCHAR(255)` | `FK -> account(id)`, `INDEX: idx_reset_password_token_account` | `Account account` | Beneficiary user account. |
| `token` | `VARCHAR(255)` | Nullable | `String token` | High-entropy verification secret. |

---

## 3. Join Tables Matrix (Many-to-Many & One-to-Many Collections)
- **Liquibase ChangeSet**: `07-create-join-tables.yaml`
- **Foreign Keys & Indexes**: `08-create-foreign-keys-and-indexes.yaml`

| Join Table Name | Column 1 (FK) | Column 2 (FK) | Target Relationship | Indexes Created |
|---|---|---|---|---|
| `role_accounts` | `roles_id -> role(id)` | `accounts_id -> account(id)` | `Role.accounts` <-> `Account.roles` | `idx_role_accounts_role`, `idx_role_accounts_account` |
| `role_panels` | `roles_id -> role(id)` | `panels_id -> panel(id)` | `Role.panels` <-> `Panel.roles` | `idx_role_panels_role`, `idx_role_panels_panel` |
| `email_attachments` | `email_id -> email(id)` | `attachments_id -> document(id)` | `Email.attachments` -> `Document` | `idx_email_attachments_email` |

---

## 4. Foreign Key Constraints Reference

| Constraint Name | Source Table | Source Column | Target Table | Target Column |
|---|---|---|---|---|
| `fk_natural_person_avatar` | `natural_person` | `avatar_id` | `document` | `id` |
| `fk_account_natural_person` | `account` | `natural_person_id` | `natural_person` | `id` |
| `fk_account_avatar` | `account` | `avatar_id` | `document` | `id` |
| `fk_menu_parent` | `menu` | `parent_id` | `menu` | `id` |
| `fk_panel_menu` | `panel` | `menu_id` | `menu` | `id` |
| `fk_reset_password_token_account` | `reset_password_token` | `account_id` | `account` | `id` |
| `fk_role_accounts_role` | `role_accounts` | `roles_id` | `role` | `id` |
| `fk_role_accounts_account` | `role_accounts` | `accounts_id` | `account` | `id` |
| `fk_role_panels_role` | `role_panels` | `roles_id` | `role` | `id` |
| `fk_role_panels_panel` | `role_panels` | `panels_id` | `panel` | `id` |
| `fk_email_attachments_email` | `email_attachments` | `email_id` | `email` | `id` |
| `fk_email_attachments_document` | `email_attachments` | `attachments_id` | `document` | `id` |

---

---

## 5. Enterprise Platform Tables Catalog

### 5.1 Table: `tenant_registry`
- **Description**: Stores multi-tenant isolation configuration for the dynamic Hybrid Multi-Tenancy Engine.

| Column Name | SQL Type | Constraints | Description |
|---|---|---|---|
| `id` | `VARCHAR(255)` | `PRIMARY KEY`, `NOT NULL` | Unique Tenant ID (e.g. `tenant_enterprise_01`). |
| `name` | `VARCHAR(255)` | `NOT NULL` | Organization name. |
| `isolation_strategy` | `VARCHAR(50)` | `NOT NULL` | Enum: `SHARED_DB`, `POSTGRESQL_SCHEMA`, `DEDICATED_DB`. |
| `datasource_url` | `VARCHAR(512)` | Nullable | Connection URL for dedicated database instances. |
| `schema_name` | `VARCHAR(128)` | Nullable | Target PostgreSQL schema for schema-isolated tenants. |
| `status` | `VARCHAR(50)` | Default `'ACTIVE'` | Status (`ACTIVE`, `SUSPENDED`). |

---

### 5.2 Table: `audit_event`
- **Description**: Append-only, tamper-resistant audit ledger for security, user, and configuration modifications.

| Column Name | SQL Type | Constraints | Description |
|---|---|---|---|
| `id` | `VARCHAR(255)` | `PRIMARY KEY`, `NOT NULL` | Unique audit event ID. |
| `trace_id` | `VARCHAR(128)` | Indexed, `NOT NULL` | Distributed correlation trace identifier. |
| `span_id` | `VARCHAR(128)` | Nullable | Request span identifier. |
| `actor_id` | `VARCHAR(255)` | Indexed, `NOT NULL` | User ID, service account, or AI agent that triggered the action. |
| `tenant_id` | `VARCHAR(255)` | Indexed, `NOT NULL` | Tenant ownership boundary. |
| `action_verb` | `VARCHAR(100)` | `NOT NULL` | Action performed (e.g. `CREATE_ROLE`, `SUSPEND_USER`, `UPDATE_SETTING`). |
| `entity_type` | `VARCHAR(100)` | `NOT NULL` | Subject entity (e.g. `Account`, `Tenant`, `SmsProvider`). |
| `entity_id` | `VARCHAR(255)` | Nullable | Target resource identifier. |
| `outcome` | `VARCHAR(50)` | `NOT NULL` | Outcome: `SUCCESS`, `DENIED`, `FAILURE`. |
| `ip_address` | `VARCHAR(64)` | Nullable | Origin client IP address. |
| `before_state` | `TEXT` | Nullable | Sanitized JSON representation of state prior to modification. |
| `after_state` | `TEXT` | Nullable | Sanitized JSON representation of state after modification. |
| `timestamp` | `TIMESTAMP` | `NOT NULL`, Indexed | UTC timestamp of event. |

---

### 5.3 Table: `sms_provider_config` & `sms_log`
- **Description**: Configuration and delivery ledger for the Intelligent Multi-Provider SMS Engine.

| Column Name | SQL Type | Constraints | Description |
|---|---|---|---|
| `provider_id` | `VARCHAR(64)` | `PRIMARY KEY` | Provider identifier (`KAVENEGAR`, `FARAZ_SMS`, `TWILIO`, `AWS_SNS`). |
| `priority_rank` | `INT` | `NOT NULL` | Routing priority ordering. |
| `cost_per_sms` | `DECIMAL(10,4)`| `NOT NULL` | Base cost per dispatched message. |
| `rolling_latency_ms`| `BIGINT` | Default `0` | Moving average delivery latency in milliseconds. |
| `rolling_success_rate`| `DECIMAL(5,2)`| Default `100.00` | Rolling percentage of successful carrier delivery receipts. |
| `circuit_state` | `VARCHAR(32)` | Default `'CLOSED'` | Circuit breaker state (`CLOSED`, `OPEN`, `HALF_OPEN`). |

---

### 5.4 Table: `ai_agent_account` & `ai_tool_audit`
- **Description**: Manages AI agent service identities and tracks autonomous tool executions via Model Context Protocol (MCP).

| Column Name | SQL Type | Constraints | Description |
|---|---|---|---|
| `agent_id` | `VARCHAR(255)` | `PRIMARY KEY` | Unique AI Agent identifier (e.g. `agent-antigravity-01`). |
| `agent_name` | `VARCHAR(255)` | `NOT NULL` | Display name and agent type. |
| `capability_token` | `VARCHAR(512)`| `NOT NULL`, Hashed | Cryptographic token granting role-scoped MCP tool access. |
| `token_budget` | `BIGINT` | `NOT NULL` | Maximum token consumption limit. |
| `requires_approval`| `BOOLEAN` | Default `TRUE` | Whether destructive operations require human elevation token. |

---

### 5.5 Table: `i18n_message`
- **Description**: Dynamic database-backed translation storage with hot-reload cache eviction.

| Column Name | SQL Type | Constraints | Description |
|---|---|---|---|
| `id` | `VARCHAR(255)` | `PRIMARY KEY` | Unique key identifier. |
| `message_key` | `VARCHAR(255)` | `NOT NULL`, Indexed | Translation key (e.g. `app.welcome.headline`). |
| `locale` | `VARCHAR(32)` | `NOT NULL`, Indexed | Target locale code (`en`, `fa`, `ar`, `fr`). |
| `message_text` | `TEXT` | `NOT NULL` | Localized translation string. |
| `tenant_id` | `VARCHAR(255)` | Indexed, Nullable | Tenant custom override (`NULL` for global default). |
| `direction` | `VARCHAR(8)` | Default `'ltr'` | Layout direction (`ltr` or `rtl`). |

---

### 5.6 Table: `oauth2_client`
- **Description**: Registered OAuth2.1 / OIDC client applications with PKCE enforcement, SHA-256 hashed secret storage, and token bucket rate limits.
- **Liquibase ChangeSet**: `15-create-oauth2-client-and-api-key-tables.yaml`

| Column Name | SQL Type | Constraints | Description |
|---|---|---|---|
| `client_id` | `VARCHAR(255)` | `PRIMARY KEY`, `NOT NULL` | Unique OAuth2 Client ID. |
| `client_secret_hash` | `VARCHAR(255)` | `NOT NULL` | SHA-256 hash of client secret. Plaintext is never stored. |
| `client_name` | `VARCHAR(255)` | `NOT NULL` | Display name of client application. |
| `scopes` | `VARCHAR(1024)` | Nullable | Comma-separated permitted scopes (e.g. `read,write,admin`). |
| `grant_types` | `VARCHAR(512)` | Nullable | Permitted OAuth2 grants (e.g. `authorization_code,client_credentials`). |
| `redirect_uris` | `VARCHAR(2048)` | Nullable | Valid callback URIs. |
| `require_pkce` | `BOOLEAN` | Default `TRUE`, `NOT NULL` | PKCE challenge mandatory enforcement flag. |
| `rate_limit_per_minute` | `INT` | Default `60`, `NOT NULL` | Per-client token bucket request limit per minute. |
| `status` | `VARCHAR(50)` | Default `'ACTIVE'`, `NOT NULL` | Lifecycle state (`ACTIVE`, `SUSPENDED`, `DELETED`). |
| `tenant_id` | `VARCHAR(255)` | Indexed, Nullable | Organization tenant identifier. |
| `registered_date` | `TIMESTAMP` | Nullable | Registration timestamp. |
| `updated_date` | `TIMESTAMP` | Nullable | Last modification timestamp. |
| `delete_date` | `TIMESTAMP` | Nullable | Soft-delete timestamp. |

---

### 5.7 Table: `api_key`
- **Description**: API consumer keys for automated integrations with SHA-256 secret hashing, token bucket throttling, and tenant scoping.
- **Liquibase ChangeSet**: `15-create-oauth2-client-and-api-key-tables.yaml`

| Column Name | SQL Type | Constraints | Description |
|---|---|---|---|
| `id` | `VARCHAR(255)` | `PRIMARY KEY`, `NOT NULL` | Internal primary key. |
| `key_id` | `VARCHAR(128)` | `UNIQUE`, `NOT NULL` | Public key identifier (e.g. `ak_live_...`). |
| `secret_hash` | `VARCHAR(255)` | `NOT NULL` | SHA-256 hash of secret key. Plaintext secret is never stored. |
| `name` | `VARCHAR(255)` | `NOT NULL` | Friendly key description or integration label. |
| `scopes` | `VARCHAR(1024)` | Nullable | Comma-separated API authorization scopes. |
| `rate_limit_per_minute` | `INT` | Default `120`, `NOT NULL` | Token bucket request capacity per minute. |
| `status` | `VARCHAR(50)` | Default `'ACTIVE'`, `NOT NULL` | Key state (`ACTIVE`, `REVOKED`, `DELETED`). |
| `tenant_id` | `VARCHAR(255)` | Indexed, Nullable | Tenant boundary. |
| `expires_at` | `TIMESTAMP` | Nullable | Expiration timestamp. |
| `registered_date` | `TIMESTAMP` | Nullable | Creation timestamp. |
| `updated_date` | `TIMESTAMP` | Nullable | Last modification timestamp. |
| `delete_date` | `TIMESTAMP` | Nullable | Soft-delete timestamp. |

---

## 6. Maintenance & Task Completion Directives

> **When completing ANY task that affects the database:**
> 1. Add new tables, columns, indexes, or constraints to the catalog above.
> 2. Document changes to Liquibase changelog files in `appservice-infrastructure-persistence/src/main/resources/db/changelog/`.
> 3. Verify schema validation passes across H2, PostgreSQL, or test profiles (`mvn test -pl appservice-infrastructure-persistence`).
