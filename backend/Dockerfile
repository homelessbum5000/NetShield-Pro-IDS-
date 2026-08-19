FROM python:3.12-slim

WORKDIR /app

# System deps for liboqs-backed Kyber (python-oqs). If liboqs is unavailable,
# the backend falls back to a clearly-marked simulated keypair.
RUN apt-get update \
    && apt-get install -y --no-install-recommends build-essential cmake git pkg-config \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

ENV PYTHONUNBUFFERED=1
EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
