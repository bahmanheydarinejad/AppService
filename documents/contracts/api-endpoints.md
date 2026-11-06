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

## 2. REST & HTTP Controller Matrix

### 2.1 Document Controller (`ir.appservice.controller.DocumentController`)
- **Base Path**: `/document`
- **Component Type**: `@Controller`
- **Class Reference**: [`DocumentController.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/controller/DocumentController.java)

| HTTP Method | Route | Parameters | Content-Type | Response Type | Description & Headers |
|---|---|---|---|---|---|
| `GET` | `/document` | None | `application/json` | `List<Document>` | Returns JSON array of all active documents (`deleteDate IS NULL`). |
| `GET` | `/document/{id}` | `id` (Path, `String`) | Variable (`document.getType()`) | Binary stream (`byte[]`) | Streams raw binary file data. Sets headers: `Cache-Control: no-store`, `Pragma: no-cache`, `Expires: 0`. |

#### Response Schema Example: `GET /document`
```json
[
  {
    "id": "e4b2d5a1-7c9e-4f1b-a2c3-9d8e7f6a5b4c",
    "displayName": "company-logo.png",
    "statue": "ACTIVE_STATUS",
    "registeredDate": "2026-01-15T10:30:00.000+00:00",
    "updatedDate": null,
    "deleteDate": null,
    "type": "image/png",
    "description": "Corporate branding avatar",
    "size": 45120
  }
]
```

---

### 2.2 REST Index Controller (`ir.appservice.controller.RestIndexController`)
- **Base Path**: `/api`
- **Component Type**: `@RestController`
- **Class Reference**: [`RestIndexController.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/controller/RestIndexController.java)

| HTTP Method | Route | Parameters | Content-Type | Response Type | Description & Auth |
|---|---|---|---|---|---|
| `GET` | `/api/index` | None | `application/json` | `Account` | Returns demo account metadata for API status verification. Requires authenticated session. |

#### Response Schema Example: `GET /api/index`
```json
{
  "displayName": "Bahman Heydari Nejad",
  "email": "bahmanheydarinejad@gmail.com",
  "accountName": null,
  "statue": "ACTIVE_STATUS",
  "id": null
}
```

---

### 2.3 Password Reset Controller (`ir.appservice.controller.ResetPasswordController`)
- **Base Path**: `/resetPassword`
- **Component Type**: `@Controller` / JSF View Intermediary
- **Class Reference**: [`ResetPasswordController.java`](file:///c:/Users/Bahma/Documents/GitHub/AppService/src/main/java/ir/appservice/controller/ResetPasswordController.java)

| HTTP Method | Route | Parameters | Response Action | Description |
|---|---|---|---|---|
| `GET` | `/resetPassword` | None | Forward `/resetPassword.xhtml` | Navigates user to default password reset entry page. |
| `GET` | `/resetPassword/{resetPasswordToken}` | `resetPasswordToken` (Path, `String`) | Forward `/resetPassword.xhtml?token={token}` | Validates token existence via `ResetPasswordTokenService.existById()`. Forwards to token verification view if valid. |

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

## 3. Standard HTTP Status Codes & Error Mapping

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
