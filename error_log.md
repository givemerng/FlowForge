# FlowForge Error & Troubleshooting Log

This file documents all errors encountered during the development and deployment of the FlowForge platform, along with their causes and solutions. New errors will be appended to this file as they occur.

---

### 1. Maven Test Context Failure
- **Error**: `ApplicationContext` failed to load during `mvn test`.
- **Cause**: The installed Java 25 runtime was newer than the Byte Buddy version bundled with Mockito.
- **Fix**: Standardized the project, Docker image, and CI on Java 21. Surefire retains Byte Buddy's experimental flag only so contributors accidentally running Java 25 can still execute the suite.

### 2. PowerShell Command Syntax Error
- **Error**: `The token '&&' is not a valid statement separator in this version.`
- **Cause**: Attempted to chain git commands using `&&` (which works in Bash and cmd) but is not supported in older PowerShell versions.
- **Fix**: Ran the commands separated by semicolons (`;`) instead.

### 3. Docker Compose MySQL Timeout
- **Error**: `dependency failed to start: container flowforge-mysql is unhealthy`
- **Cause**: On the very first run, MySQL took longer than 30 seconds to initialize its internal data files. The Docker healthcheck timed out at 30 seconds and marked it unhealthy, preventing dependent containers from starting.
- **Fix**: Increased the `start_period` for the MySQL healthcheck in `docker-compose.yml` from `30s` to `120s`.

### 4. React / Vite Crash (Blank Screen)
- **Error**: `Uncaught ReferenceError: global is not defined`
- **Cause**: The `sockjs-client` package (used for WebSockets) expects a Node.js-like environment where a `global` variable exists. Vite does not polyfill this by default, causing the library to crash the entire React application on load.
- **Fix**: Added `define: { global: 'window' }` to the `vite.config.ts` file.

### 5. Default Login Failed (Database Empty)
- **Error**: Could not log in using `admin` / `admin123!`.
- **Cause**: The `DataSeeder.java` file (which creates the dummy users) only runs if the `dev` profile is active. When running via Docker, it defaulted to the production profile, leaving the database completely empty.
- **Fix**: Compose defaults to the safe `prod` profile. Local demo accounts are created only when `SPRING_PROFILES_ACTIVE=dev` is explicitly set.

### 6. Broken Authentication Links
- **Error**: Clicking "Register here" or "Forgot password" on the Login page did nothing.
- **Cause**: Registration used a placeholder anchor, and no password-reset flow exists.
- **Fix**: Routed registration to `/register` and removed the unsupported password-reset control.

### 7. Java Backend Crash on Startup (SQL Syntax Error)
- **Error**: `java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax... near 'read,title,user_id)'`
- **Cause**: The `Notification` entity had a boolean column named `read`. In MySQL 8.0+, `READ` is a reserved keyword, which caused Hibernate's auto-generated SQL to fail. This crash prevented `DataSeeder` from running, and subsequently caused the backend API to shut down immediately. Because the backend was offline, the frontend login failed.
- **Fix**: Updated `Notification.java` to explicitly map the column to `is_read` (`@Column(name = "is_read")`) and updated the `@Index` definition accordingly.

### 8. Frontend Login CORS Error
- **Error**: The browser blocked the login request to `http://localhost:8080/api/auth/login` (Silent failure on frontend, console shows CORS policy violation).
- **Cause**: The Java backend's Spring Security configuration was missing global Cross-Origin Resource Sharing (CORS) rules. Browsers automatically block requests made from port `5173` (React) to port `8080` (Java) for security reasons.
- **Fix**: Added a `CorsConfigurationSource` bean backed by the `CORS_ALLOWED_ORIGINS` environment variable. Compose defaults it to the local frontend origin only.

### 9. Flyway Failed Against MySQL 8
- **Error**: `Unsupported Database: MySQL 8.0` during container startup.
- **Cause**: Modern Flyway versions package MySQL support separately from `flyway-core`.
- **Fix**: Added `flyway-mysql`, created baseline and follow-up migrations, and changed Hibernate to `ddl-auto: validate`.

### 10. Hibernate Enum Validation Failed
- **Error**: Hibernate reported `VARCHAR`/`ENUM` schema mismatches after Flyway completed.
- **Cause**: The original schema used string columns while Hibernate's MySQL enum mappings expected native enum columns.
- **Fix**: Added migration `V3__native_enum_columns.sql` so the migrated schema matches the entity mappings.

### 11. Frontend Container Stayed Unhealthy
- **Error**: The application loaded, but the frontend healthcheck remained unhealthy.
- **Cause**: Alpine `wget` resolved `localhost` to IPv6 while Nginx listened on IPv4.
- **Fix**: Changed the healthcheck URL to `http://127.0.0.1/`.

### 12. Report Job Remained Queued
- **Error**: The Python worker logged `Processing job None`, while the report stayed `QUEUED` in the UI.
- **Cause**: RabbitMQ received the message before the database transaction that created the job committed, so the worker's idempotency lookup could not find it.
- **Fix**: Added `JobQueuePublisher` to publish only after a successful transaction commit and added a regression test. The full Playwright flow now passes.

### 13. Existing Database Failed Flyway Baseline
- **Error**: The API stopped during startup because migration V1 tried to create tables that already existed.
- **Cause**: A pre-Flyway database was baselined at version `0`, so Flyway correctly attempted V1 instead of treating the existing schema as the V1 baseline.
- **Fix**: Changed the existing-schema baseline to version `1`. Empty databases still execute V1, while legacy schemas continue with V2 and later migrations.

### 14. RabbitMQ Erlang Cookie Permission Failure
- **Error**: RabbitMQ exited with `Error when reading /var/lib/rabbitmq/.erlang.cookie: eacces`, leaving dependent API and frontend containers unstarted.
- **Cause**: A persisted anonymous RabbitMQ volume contained a root-owned cookie file that the `rabbitmq` process could not read.
- **Fix**: Added a root entrypoint preflight that corrects the cookie ownership and mode before delegating to the official RabbitMQ entrypoint.

---
*(End of log - new errors will be added below)*
