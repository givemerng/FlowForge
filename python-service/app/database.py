import os
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker

MYSQL_URL = os.getenv(
    "SPRING_DATASOURCE_URL",
    "mysql+pymysql://flowforge_user:flowforge_password@localhost:3306/flowforge"
).replace("jdbc:mysql://", "mysql+pymysql://").split("?")[0]

MYSQL_USER = os.getenv("SPRING_DATASOURCE_USERNAME", "flowforge_user")
MYSQL_PASS = os.getenv("SPRING_DATASOURCE_PASSWORD", "flowforge_password")
MYSQL_HOST = os.getenv("MYSQL_HOST", "mysql")
MYSQL_DB   = os.getenv("MYSQL_DATABASE", "flowforge")

DATABASE_URL = f"mysql+pymysql://{MYSQL_USER}:{MYSQL_PASS}@{MYSQL_HOST}/{MYSQL_DB}"

engine = create_engine(DATABASE_URL, pool_pre_ping=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
