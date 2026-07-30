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
