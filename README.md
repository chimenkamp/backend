# Conferia backend

The backend requires Java Development Kit 17.

## Start locally for development

Create the local environment file before the first start.

```bash
cp .env.example .env
```

Replace `CONFERIA_SETUP_TOKEN` and `CONFERIA_JWT_SECRET` in `.env` with
high-entropy values. The JSON Web Token secret must be a Base64-encoded value
that decodes to at least 256 bits. The following commands generate suitable
values in the same order as the variables.

```bash
# Output for CONFERIA_SETUP_TOKEN
openssl rand -hex 32

# Different output for CONFERIA_JWT_SECRET
openssl rand -base64 32
```

Load the environment file and start Spring Boot.

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

The backend is available at `http://localhost:8080`.

## Enable Agenda Web Push reminders

The Agenda reminder switch remains unavailable until the backend has a VAPID
key pair. Generate one persistent pair from the frontend repository.

```bash
npm run generate:vapid
```

Copy the generated `CONFERIA_VAPID_PUBLIC_KEY`,
`CONFERIA_VAPID_PRIVATE_KEY`, and `CONFERIA_VAPID_SUBJECT` values into this
repository's `.env`. Replace the example subject email with a monitored contact
address. The public and private key must remain the same across backend
restarts. Keep the private key secret.

`CONFERIA_REMINDER_MINUTES` controls the reminder lead time and defaults to
15 minutes. `CONFERIA_REMINDER_POLL_MS` controls the scheduler interval and
defaults to 30000 milliseconds. Production Web Push requires an HTTPS
frontend origin.
