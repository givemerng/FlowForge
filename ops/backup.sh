#!/bin/sh
set -eu

umask 077
mkdir -p /backups

while true; do
  timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
  destination="/backups/${MYSQL_DATABASE}_${timestamp}.sql"
  temporary="${destination}.tmp"

  mysqldump \
    --host="${MYSQL_HOST}" \
    --user="${MYSQL_USER}" \
    --single-transaction \
    --routines \
    --triggers \
    "${MYSQL_DATABASE}" > "${temporary}"
  mv "${temporary}" "${destination}"
  find /backups -type f -name "${MYSQL_DATABASE}_*.sql" -mtime "+${BACKUP_RETENTION_DAYS}" -delete
  sleep "${BACKUP_INTERVAL_SECONDS}"
done
