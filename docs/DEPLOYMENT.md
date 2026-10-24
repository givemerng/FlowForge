# Production Deployment

## Host Requirements

- Linux host with Docker Engine and Compose v2
- A domain with DNS records pointing to the host
- Inbound TCP ports `80` and `443`
- Optional UDP port `443` for HTTP/3
- Persistent disk capacity for MySQL, Prometheus, Grafana, Caddy, and backups

## Generate Secrets

From the repository root:

```bash
python ops/generate_env.py \
  --domain flowforge.example.com \
  --email ops@example.com
```

This creates `.env.production` with random database, JWT, RabbitMQ, Erlang cookie, and Grafana credentials. The command refuses to overwrite an existing file. Protect this file with host-level permissions and an external secret backup; it is ignored by Git.

## Validate Configuration

```bash
docker compose --env-file .env.production \
  -f docker-compose.yml \
  -f docker-compose.prod.yml \
  config --quiet
```

Required variables use Compose's required-value syntax, so production validation fails rather than silently using development credentials.

## Start or Upgrade

```bash
docker compose --env-file .env.production \
  -f docker-compose.yml \
  -f docker-compose.prod.yml \
  pull

docker compose --env-file .env.production \
  -f docker-compose.yml \
  -f docker-compose.prod.yml \
  up -d --build --wait
```

Caddy obtains and renews certificates automatically. MySQL, Redis, RabbitMQ, the Java API, frontend, Prometheus, and Grafana have no direct production host ports; Caddy is the public entry point.

## Health Checks

```bash
docker compose --env-file .env.production \
  -f docker-compose.yml \
  -f docker-compose.prod.yml \
  ps

curl --fail https://flowforge.example.com/
```

Replace the example domain. Review service logs with `docker compose ... logs --tail=200 SERVICE` without printing `.env.production`.

## Backups

`mysql-backup` writes a logical SQL dump to the `mysql_backups` volume every `BACKUP_INTERVAL_SECONDS` seconds and removes dumps older than `BACKUP_RETENTION_DAYS`.

List backup files:

```bash
docker compose --env-file .env.production \
  -f docker-compose.yml \
  -f docker-compose.prod.yml \
  exec mysql-backup ls -lh /backups
```

Copy backups regularly from the Docker volume to encrypted storage on another host or object store. A local Docker volume is not sufficient disaster recovery.

Restore into a maintenance environment first:

```bash
cat flowforge_TIMESTAMP.sql | \
docker compose --env-file .env.production \
  -f docker-compose.yml \
  -f docker-compose.prod.yml \
  exec -T mysql sh -c \
    'mysql --user="$MYSQL_USER" --password="$MYSQL_PASSWORD" "$MYSQL_DATABASE"'
```

The credentials expand inside the MySQL container rather than appearing in the operator shell. Test restoration periodically.

## Retention and Logs

- Prometheus retention defaults to `30d` and is controlled by `PROMETHEUS_RETENTION`.
- Container JSON logs default to five files of 10 MB each, controlled by `CONTAINER_LOG_MAX_FILES` and `CONTAINER_LOG_MAX_SIZE`.
- Backup retention defaults to 30 days.
- Configure host disk alerts before production traffic.

## Rollback

Application containers can be rolled back by checking out a previously verified commit and rebuilding. Database migrations are forward-only; take a verified backup before upgrades and restore it if a schema rollback is required.

## Security Checklist

- Keep `SPRING_PROFILES_ACTIVE=prod`.
- Never expose MySQL, Redis, RabbitMQ, Prometheus, Grafana, or Actuator directly to the internet.
- Restrict SSH access and keep Docker and the host patched.
- Store `.env.production` outside backups that untrusted users can read.
- Send backup copies off-host and test restores.
- Add external availability and disk-capacity alerts appropriate to the hosting provider.
