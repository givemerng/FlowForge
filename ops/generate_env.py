import argparse
import base64
import secrets
from pathlib import Path


def secret(bytes_count: int = 32) -> str:
    return secrets.token_urlsafe(bytes_count)


parser = argparse.ArgumentParser(description="Generate a production FlowForge environment file.")
parser.add_argument("--domain", required=True, help="Public HTTPS domain, for example flowforge.example.com")
parser.add_argument("--email", required=True, help="Email used for automatic TLS certificate notices")
parser.add_argument("--output", default=".env.production", help="Output file (default: .env.production)")
args = parser.parse_args()

output = Path(args.output)
if output.exists():
    raise SystemExit(f"Refusing to overwrite existing file: {output}")

jwt_secret = base64.b64encode(secrets.token_bytes(64)).decode("ascii")
values = {
    "MYSQL_DATABASE": "flowforge",
    "MYSQL_USER": "flowforge_user",
    "MYSQL_PASSWORD": secret(),
    "MYSQL_ROOT_PASSWORD": secret(),
    "JWT_SECRET": jwt_secret,
    "JWT_EXPIRATION": "86400000",
    "RABBITMQ_USER": "flowforge",
    "RABBITMQ_PASSWORD": secret(),
    "RABBITMQ_ERLANG_COOKIE": secret(48),
    "GRAFANA_ADMIN_PASSWORD": secret(),
    "FLOWFORGE_DOMAIN": args.domain,
    "TLS_EMAIL": args.email,
    "PROMETHEUS_RETENTION": "30d",
    "CONTAINER_LOG_MAX_SIZE": "10m",
    "CONTAINER_LOG_MAX_FILES": "5",
    "BACKUP_INTERVAL_SECONDS": "86400",
    "BACKUP_RETENTION_DAYS": "30",
}

output.write_text("".join(f"{key}={value}\n" for key, value in values.items()), encoding="utf-8")
print(f"Created {output} with generated production secrets.")
