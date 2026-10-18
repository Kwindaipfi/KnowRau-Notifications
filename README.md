# KnowRau Notification Service

Standalone Spring Boot 3 / Java 17 microservice that stores and serves
notifications for **both tutors and students** — e.g. "your request was
selected" or "your offer was accepted".

## Does it need a database?

Yes. Notifications must survive restarts, be visible when a user next logs in,
and track read/unread state, so they are persisted in PostgreSQL (matching the
rest of the KnowRau stack: Postgres + Flyway).

## Architecture

This service is the **sender** across every channel. Other services only
**trigger** it — they never send email/SMS themselves.

- **Public API** (`/api/notifications/**`) — called by the Angular app. The
  caller is authenticated by the end-user **JWT** (same secret as the auth
  service). A user only ever sees/mutates their own notifications.
- **Internal API** (`/api/notifications/internal/**`) — called by trusted
  backend services (matching, booking, chat) to **create** a notification.
  Secured by a shared **service token** header (`X-Service-Token`), mirroring
  the DocumentService `request-service` pattern.

```
matching-service ──(X-Service-Token)──▶ POST /api/notifications/internal   (create)
         │
         ▼  the notification service then does ALL delivery:
         1. stores the in-app row (the bell notification)
         2. reads the user's channel preferences
         3. sends EMAIL (SMTP)        — async, if enabled
         4. sends SMS (provider stub) — async, if enabled

Angular app ──(Bearer JWT)──▶ GET   /api/notifications            (list)
                              GET   /api/notifications/unread-count
                              PATCH /api/notifications/{id}/read
                              PATCH /api/notifications/read-all
                              GET   /api/notifications/preferences
                              PUT   /api/notifications/preferences  (toggle email/sms)
```

## Channels

| Channel | Status      | Notes                                                                                                                            |
| ------- | ----------- | -------------------------------------------------------------------------------------------------------------------------------- |
| In-app  | always on   | the stored DB row; shown by the bell                                                                                             |
| Email   | SMTP, ready | disabled automatically if `MAIL_HOST` is blank                                                                                   |
| SMS     | **stub**    | logs instead of sending; drop in Twilio/your provider in `SmsNotificationChannel.send()` and set `NOTIFICATION_SMS_ENABLED=true` |

Delivery to email/SMS is **asynchronous** (`@Async`) so a slow SMTP/SMS call
never blocks the create request. Each channel swallows its own failures so one
channel can't break another.

**Contact details (Option A):** this service stores only `recipientUserId`, so
the triggering service passes `recipientEmail` / `recipientPhone` in the
internal request. Per-user `email`/`sms` on-off toggles live in the
`notification_preferences` table (defaults: email on, SMS off).

## Configuration (environment variables)

Secrets are read from env vars; do **not** commit real values. Local-dev
defaults exist only where safe.

| Variable                       | Purpose                                                | Default (dev)                                              |
| ------------------------------ | ------------------------------------------------------ | ---------------------------------------------------------- |
| `NOTIFICATION_DB_URL`          | JDBC URL                                               | `jdbc:postgresql://localhost:5435/Brainwave_Notifications` |
| `NOTIFICATION_DB_USERNAME`     | DB user                                                | `postgres`                                                 |
| `NOTIFICATION_DB_PASSWORD`     | DB password                                            | _(empty — must set)_                                       |
| `NOTIFICATION_SERVER_PORT`     | HTTP port                                              | `8089`                                                     |
| `JWT_SECRET`                   | HMAC secret, **must match the auth service**           | _(empty — must set, app fails fast if missing)_            |
| `NOTIFICATION_SERVICE_TOKEN`   | Shared token trusted services send on the internal API | `notification-service-dev-token-2026`                      |
| `NOTIFICATION_CORS_ORIGINS`    | Comma-separated allowed origins                        | `http://localhost:4200,http://localhost:4300`              |
| `MAIL_HOST`                    | SMTP host (blank = email channel off)                  | _(empty)_                                                  |
| `MAIL_PORT`                    | SMTP port                                              | `587`                                                      |
| `MAIL_USERNAME`                | SMTP username                                          | _(empty)_                                                  |
| `MAIL_PASSWORD`                | SMTP password / app password                           | _(empty)_                                                  |
| `NOTIFICATION_EMAIL_ENABLED`   | Master switch for the email channel                    | `true`                                                     |
| `NOTIFICATION_EMAIL_FROM`      | From address                                           | `no-reply@knowrau.local`                                   |
| `NOTIFICATION_EMAIL_FROM_NAME` | From display name                                      | `KnowRau`                                                  |
| `NOTIFICATION_SMS_ENABLED`     | Master switch for the SMS channel (stub)               | `false`                                                    |

## Database setup

Create the database once (Flyway creates the tables on startup):

```sql
CREATE DATABASE "Brainwave_Notifications";
```

The schema lives in `src/main/resources/db/migration/V1__create_notifications.sql`.

## Run

```bash
export JWT_SECRET='<same-secret-as-auth-service>'
export NOTIFICATION_DB_PASSWORD='<your-db-password>'
mvn spring-boot:run
```

Health check: `GET http://localhost:8089/actuator/health`

## Creating a notification (from another service)

When the matching service accepts an offer, it calls:

```http
POST http://localhost:8089/api/notifications/internal
X-Service-Token: <NOTIFICATION_SERVICE_TOKEN>
Content-Type: application/json

{
  "recipientUserId": "42",
  "recipientRole": "TUTOR",
  "type": "OFFER_ACCEPTED",
  "title": "Your offer was accepted",
  "message": "Thandi accepted your offer for Mathematics Grade 11.",
  "recipientEmail": "tutor@example.com",
  "recipientPhone": "+27710000000",
  "referenceId": "1007",
  "referenceType": "OFFER",
  "link": "/tutorDashboard"
}
```

`recipientEmail` / `recipientPhone` are optional — include them to enable the
email/SMS channels for this notification (the service only sends on a channel
if the user has it enabled AND an address is provided). For a selected student
request, use `recipientRole: "STUDENT"` and `type: "REQUEST_SELECTED"`.

### Channel preferences (from the Angular app)

```http
GET http://localhost:8089/api/notifications/preferences
Authorization: Bearer <user-jwt>
# → { "emailEnabled": true, "smsEnabled": false }

PUT http://localhost:8089/api/notifications/preferences
Authorization: Bearer <user-jwt>
Content-Type: application/json
{ "emailEnabled": true, "smsEnabled": false }
```

## Reading notifications (from the Angular app)

```http
GET http://localhost:8089/api/notifications?unreadOnly=false&page=0&size=20
Authorization: Bearer <user-jwt>
```

Returns a Spring `Page` of notifications (newest first). Also available:
`/unread-count` (for the bell badge), `PATCH /{id}/read`, `PATCH /read-all`.

## Notes / next steps

- Real-time push (SSE or WebSocket) is intentionally **not** included in v1 —
  the frontend can poll `/unread-count`. The service layer is a clean seam to
  add push later.
- `spring.jpa.hibernate.ddl-auto` is `validate`; Flyway owns the schema.
