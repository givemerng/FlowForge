from prometheus_client import Counter, Gauge

JOBS_PROCESSED = Counter(
    "flowforge_jobs_processed_total",
    "Number of jobs processed by outcome",
    ["type", "outcome"],
)
WORKER_CONNECTED = Gauge(
    "flowforge_worker_connected",
    "Whether the RabbitMQ worker currently has an active connection",
)
