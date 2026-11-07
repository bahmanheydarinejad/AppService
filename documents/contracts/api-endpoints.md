# API Endpoints Contract & REST Reference

> **Authoritative Specification & Runtime Contract for AppService HTTP Endpoints**  
> *Targeted dense reference for AI agents and developers. Consult this contract instead of scanning Java controller source files.*

---

> [!IMPORTANT]
> **Mandatory Task Completion Protocol**:
> Whenever an endpoint is added, removed, or modified, you **MUST** update this file immediately upon task completion.

---

## 1. Global Route Security Architecture

Spring Security (`ir.appservice.configuration.AppSecurity`) enforces a perimeter filter chain. Authentication state is established via JSF managed bean (`AuthenticationBean`) interacting with Spring's `ProviderManager` backed by `AccountService`.

| Category | URL Pattern | Permitted Roles / Auth | Handler / Controller |
|---|---|---|---|
| **JSF Static Resources** | `/javax.faces.resource/**`, `/jakarta.faces.resource/**` | Public (`permitAll`) | Mojarra JSF ResourceHandler |
| **WebJars & Static Assets** | `/webjars/**`, `/css/**`, `/js/**`, `/images/**`, `/favicon.ico` | Public (`permitAll`) | Spring Web ResourceHttpRequestHandler |
| **Public Landing Pages** | `/`, `/index`, `/index.xhtml` | Public (`permitAll`) | Mojarra JSF Servlet |
| **Authentication Views** | `/signin`, `/signin.xhtml`, `/login`, `/login.xhtml` | Public (`permitAll`) | `AuthenticationBean` / Mojarra |
| **Session Invalidation** | `/signout` | Public (`permitAll`) | Spring Security Logout Filter |
| **Error Views** | `/error`, `/403.xhtml`, `/404.xhtml`, `/500.xhtml` | Public (`permitAll`) | Spring Boot BasicErrorController / JSF |
| **Document Delivery** | `/document/**` | Public (`permitAll`) | `ir.appservice.controller.DocumentController` |
| **Password Reset Views** | `/resetPassword`, `/resetPassword.xhtml`, `/resetPassword/**` | Public (`permitAll`) | `ResetPasswordController` / JSF |
| **Protected Admin Console** | `/dashboard.xhtml`, `/panels/**` | Authenticated Session | `ir.appservice.view.beanComponents.SessionBean` |
| **Protected REST APIs** | `/api/**` | Authenticated Session | Spring `@RestController` endpoints |
| **Spring Boot Actuator** | `/actuator/**` | Authenticated / Configured | Spring Boot Actuator Endpoint Handler |

---

## 2. Inbound Driving Controller & UI Console Matrix

### 2.1 Single-Page PrimeFaces Console
- **Base Path**: `/dashboard.xhtml`
- **Component Type**: JSF View with Dynamic Include Navigation
- **View Reference**: [`dashboard.xhtml`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-ui-primefaces/src/main/resources/META-INF/resources/dashboard.xhtml)
- **Controller/Bean Reference**: [`PanelHandler.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-ui-primefaces/src/main/java/ir/appservice/ui/bean/PanelHandler.java)

### 2.2 OpenAPI 3.0 Documentation Controller
- **Base Path**: `/v3/api-docs`
- **Component Type**: `@RestController`
- **Class Reference**: [`OpenApiController.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-api-rest/src/main/java/ir/appservice/api/rest/controller/OpenApiController.java)

### 2.3 Administration Control Plane Controllers
- **Class Reference**: [`UserSearchController.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-api-rest/src/main/java/ir/appservice/api/rest/controller/UserSearchController.java) (`/api/admin/users/search`)
- **Class Reference**: [`AuditManagementController.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-api-rest/src/main/java/ir/appservice/api/rest/controller/AuditManagementController.java) (`/api/admin/audit-events/**`)
- **Class Reference**: [`SchedulerManagementController.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-api-rest/src/main/java/ir/appservice/api/rest/controller/SchedulerManagementController.java) (`/api/admin/jobs/**`)
- **Class Reference**: [`SmsManagementController.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-api-rest/src/main/java/ir/appservice/api/rest/controller/SmsManagementController.java) (`/api/admin/sms/**`)
- **Class Reference**: [`MailManagementController.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-api-rest/src/main/java/ir/appservice/api/rest/controller/MailManagementController.java) (`/api/admin/mail/**`)
- **Class Reference**: [`OpsMonitoringController.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-api-rest/src/main/java/ir/appservice/api/rest/controller/OpsMonitoringController.java) (`/api/admin/ops/**`)
- **Class Reference**: [`TenantManagementController.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-api-rest/src/main/java/ir/appservice/api/rest/controller/TenantManagementController.java) (`/api/admin/tenants/**`)
- **Class Reference**: [`OAuth2ClientManagementController.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-api-rest/src/main/java/ir/appservice/api/rest/controller/OAuth2ClientManagementController.java) (`/api/admin/clients/**`)
- **Class Reference**: [`McpAgentController.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/appservice-api-rest/src/main/java/ir/appservice/api/rest/controller/McpAgentController.java) (`/api/admin/ai/**`)

---

### 2.4 Spring Boot Actuator Endpoints
- **Base Path**: `/actuator`
- **Exposure**: Enabled by Spring Boot Starter Actuator

| HTTP Method | Route | Description |
|---|---|---|
| `GET` | `/actuator` | Discovery endpoint listing available actuator hypermedia links. |
| `GET` | `/actuator/health` | Health check indicator (status: `UP`, database connection state). |
| `GET` | `/actuator/info` | Application build and version information. |

---

---

## 3. Standardized Generic CRUD Search API Specification

> **Architectural Standard**: All search and query operations across every administrative entity share an identical signature, contract, and underlying execution pipeline.

### 3.1 Uniform Search Route Contract
- **HTTP Method**: `POST`
- **Route Pattern**: `/api/admin/{subject}/search`
- **Supported Subjects**: `users`, `roles`, `permissions`, `tenants`, `audit-events`, `jobs`, `sms-logs`, `mail-logs`, `clients`, `documents`, `i18n-messages`
- **Headers**:
  - `Authorization: Bearer <JWT_TOKEN>`
  - `X-Trace-Id: <UUID>` (Optional, auto-generated if omitted)
  - `Content-Type: application/json`

#### Universal Search Request Schema
```json
{
  "filters": [
    {
      "field": "status",
      "operator": "EQUALS",
      "value": "ACTIVE"
    },
    {
      "field": "createdAt",
      "operator": "BETWEEN",
      "value": ["2026-01-01T00:00:00Z", "2026-11-07T00:00:00Z"]
    }
  ],
  "searchQuery": "optional full-text token",
  "logic": "AND",
  "sorts": [
    {
      "field": "createdAt",
      "direction": "DESC"
    }
  ],
  "page": 0,
  "size": 20,
  "traceId": "tr-9481-ac2d"
}
```

#### Universal Search Response Envelope (`PagedApiResponse<T>`)
```json
{
  "success": true,
  "traceId": "tr-9481-ac2d",
  "timestamp": "2026-11-07T02:20:00Z",
  "subject": "users",
  "page": 0,
  "size": 20,
  "totalElements": 1250,
  "totalPages": 63,
  "executionTimeMs": 14,
  "data": [
    {
      "id": "usr_94a821",
      "username": "admin_sys",
      "email": "admin@appservice.internal",
      "status": "ACTIVE",
      "tenantId": "tenant_system"
    }
  ]
}
```

---

## 4. Dedicated Administrative Control Plane Endpoints

### 4.1 Audit Log Dashboard (Trace Filters)
| Method | Route | Description | Scope Required |
|---|---|---|---|
| `POST` | `/api/admin/audit-events/search` | Search audit logs by `traceId`, `spanId`, `correlationId`, actor, IP, outcome. | `AUDIT_READ` |
| `GET` | `/api/admin/audit-events/{id}/diff` | Side-by-side JSON before/after state comparison with masked secrets. | `AUDIT_READ` |

### 4.2 Job Scheduler Management
| Method | Route | Description | Scope Required |
|---|---|---|---|
| `GET` | `/api/admin/jobs` | List clustered Quartz jobs, triggers, cron expressions, next fire times. | `JOBS_READ` |
| `POST` | `/api/admin/jobs/{id}/fire` | Trigger instant execution ("Fire Now") on cluster. | `JOBS_EXECUTE` |
| `POST` | `/api/admin/jobs/{id}/pause` | Pause trigger execution. | `JOBS_MANAGE` |
| `POST` | `/api/admin/jobs/{id}/resume` | Resume paused job trigger. | `JOBS_MANAGE` |

### 4.3 Mail Management & Template Studio
| Method | Route | Description | Scope Required |
|---|---|---|---|
| `GET` | `/api/admin/mail/providers` | Outbound SMTP / API provider configs and health status. | `MAIL_READ` |
| `POST` | `/api/admin/mail/templates` | Create or update Jinja2 / HTML email template with tokens. | `MAIL_MANAGE` |
| `POST` | `/api/admin/mail/test-send` | Dispatch test email with simulated token payload. | `MAIL_MANAGE` |
| `POST` | `/api/admin/mail-logs/search` | Standard CRUD search across email delivery and bounce records. | `MAIL_READ` |

### 4.4 Intelligent Multi-Provider SMS Management
| Method | Route | Description | Scope Required |
|---|---|---|---|
| `GET` | `/api/admin/sms/providers` | List configured SMS providers (Kavenegar, FarazSms, Twilio, AWS SNS). | `SMS_READ` |
| `GET` | `/api/admin/sms/health` | Live carrier latency, rolling delivery rate, and circuit breaker status. | `SMS_READ` |
| `PUT` | `/api/admin/sms/routing-policy` | Configure routing strategy (Cost-Optimized, Latency-Optimized, Balanced). | `SMS_MANAGE` |
| `POST` | `/api/admin/sms/send` | Intelligent dispatch choosing optimal provider with fallback circuit breaker. | `SMS_SEND` |
| `POST` | `/api/admin/sms-logs/search` | Standard CRUD search across SMS dispatch and carrier DLR logs. | `SMS_READ` |

### 4.5 Monitoring & Operations Telemetry
| Method | Route | Description | Scope Required |
|---|---|---|---|
| `GET` | `/api/admin/ops/metrics` | Real-time API latency percentiles (P50, P95, P99), 4xx/5xx HTTP error rates. | `OPS_READ` |
| `GET` | `/api/admin/ops/pools` | HikariCP database connection pool metrics (active, idle, wait time). | `OPS_READ` |
| `GET` | `/api/admin/ops/circuits` | Resilience4j circuit breaker state and failure rate thresholds. | `OPS_READ` |

### 4.6 Tenant Management Panel (Hybrid Multi-Tenancy)
| Method | Route | Description | Scope Required |
|---|---|---|---|
| `POST` | `/api/admin/tenants/search` | Standard CRUD search across registered customer tenants. | `TENANTS_READ` |
| `PUT` | `/api/admin/tenants/{id}/strategy` | Update tenant isolation tier: `SHARED_DB`, `POSTGRESQL_SCHEMA`, `DEDICATED_DB`. | `TENANTS_MANAGE` |
| `POST` | `/api/admin/tenants/{id}/provision` | Execute automated Liquibase schema / DB migration for tenant. | `TENANTS_MANAGE` |

### 4.7 Client & API Key Management
| Method | Route | Description | Scope Required |
|---|---|---|---|
| `POST` | `/api/admin/clients/search` | Standard CRUD search across OAuth2 clients and external consumer apps. | `CLIENTS_READ` |
| `POST` | `/api/admin/clients` | Register new OAuth2 confidential client with PKCE enforcement. | `CLIENTS_MANAGE` |
| `POST` | `/api/admin/clients/{id}/keys/rotate` | Rotate API secret key; returns newly generated secret once, stores SHA-256. | `CLIENTS_MANAGE` |

### 4.8 AI Agent Infrastructure Plane (MCP & Tool Execution)
| Method | Route | Description | Scope Required |
|---|---|---|---|
| `GET` | `/api/admin/ai/agents` | List authorized AI agent identities, capability tokens, and rate limits. | `AI_MANAGE` |
| `GET` | `/api/admin/ai/mcp/tools` | Model Context Protocol discovery endpoint exposing available tools and JSON schemas. | `AI_TOOLS_READ` |
| `POST` | `/api/admin/ai/mcp/invoke` | Sandboxed AI tool invocation endpoint with parameter verification and audit logging. | `AI_INVOKE` |
| `POST` | `/api/admin/ai-audit/search` | Standard CRUD search across AI agent prompt tokens, tool calls, and diff records. | `AUDIT_READ` |

### 4.9 OpenAPI Documentation & REST Explorer
| Method | Route | Description | Scope Required |
|---|---|---|---|
| `GET` | `/v3/api-docs` | OpenAPI 3.0.1 machine-readable JSON specification of all platform endpoints. | Public / Configured |

---

## 5. Standard HTTP Status Codes & Error Mapping

The application maps service-layer exceptions to standardized error responses via `ir.appservice.configuration.exception`:

| Exception Class | HTTP Status | Standard Response / Behavior |
|---|---|---|
| `NotFoundEntityException` | `404 Not Found` | Entity not found in database; redirects to `404.xhtml` or returns 404 status. |
| `DuplicateEntityException` | `409 Conflict` | Unique constraint violation (e.g. `uk_account_account_name`). |
| `InvalidEmailException` | `400 Bad Request` | Email fails regex validation against `regx.email`. |
| `InvalidTokenException` | `401 Unauthorized` | Password reset token expired, nonexistent, or invalid. |
| `ControllerLayerException` | `500 Internal Error` | Unhandled controller or serialization error; handled by `500.xhtml`. |
| `AccessDeniedException` | `403 Forbidden` | Authenticated account lacks required `Role` or `Panel` privilege; renders `403.xhtml`. |

---

## 4. Maintenance & Task Completion Directives

> **When completing ANY task that affects endpoints:**
> 1. Add new routes, methods, and parameters to the matrices above.
> 2. Document changes to authentication rules or request/response payloads.
> 3. Verify changes with targeted tests (`mvn test -Dtest=AppServiceTests`).
