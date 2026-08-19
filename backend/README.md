# NetShield Pro — Cloud Backend

A real FastAPI backend that implements the endpoints the Android app currently
mocks (`/quantum/*`, `/gateway/*`) with a pluggable compute provider so you can
swap between local CSPRNG, NVIDIA CUDA-Q/cuQuantum (GPU quantum simulation),
cloud QRNG, and real IBM Quantum — without changing the app.

## What this is (and isn't)

- **Is:** a working backend that replaces the app's fictitious
  `http://quantum.onion:9000/` / `http://gateway.onion/` mock endpoints with
  real HTTPS endpoints returning the exact JSON contract the app expects.
- **Isn't:** a quantum computer. Kyber/ML-KEM is post-quantum but runs on
  classical hardware. Real QPUs (IBM/Braket/Azure) are queued and noisy and are
  used here only for async research/demo jobs — never for inline decisions.

## Endpoints (match `ApiModels.kt`)

| Method | Path | Request | Response |
|---|---|---|---|
| GET  | `/health` | — | provider + endpoint list |
| GET  | `/quantum/health` | — | `ServerHealthResponse` |
| POST | `/quantum/random` | `{"n_bytes": 16}` | `{"status","random_hex","entropy_source"}` |
| POST | `/quantum/keygen` | — | `{"status","public_key","private_key","algorithm"}` |
| POST | `/quantum/anomaly` | `{"features":[0.9,0.1,...]}` | `{"anomaly_score","is_threat","quantum_circuit_eval_time_ms"}` |
| GET  | `/gateway/health` | — | `ServerHealthResponse` |
| POST | `/gateway/ingest` | `EncryptedIDSRequest` | `{"status","ingest_id","message","kafka_topic"}` |

## Run locally

```bash
cd backend
python -m venv venv && source venv/bin/activate
pip install -r requirements.txt
cp .env.example .env            # edit provider/keys as needed
uvicorn app.main:app --reload --port 8000
```

## Run with Docker

```bash
cd backend
cp .env.example .env
docker compose up --build      # http://localhost:8000
```

## Choose a compute provider (`COMPUTE_PROVIDER`)

| Value | Behavior | Needs |
|---|---|---|
| `local` (default) | OS CSPRNG entropy; simulated Kyber keypair (real ML-KEM if `python-oqs`/liboqs installed); deterministic anomaly scoring | nothing |
| `qrng` | Sources true quantum-origin entropy from a cloud QRNG API | `QRNG_API_URL` (+ `QRNG_API_KEY`) |
| `cudaq` / `cuquantum` | Quantum-circuit anomaly scoring simulated on an NVIDIA GPU via CUDA-Q | CUDA-Q installed on a GPU host |
| `ibm` | Async quantum jobs on real IBM QPUs (demo panel only) | `IBM_TOKEN` + `qiskit-ibm-runtime` |

### Enabling real Kyber (ML-KEM-1024)

`python-oqs` needs liboqs built from source on most platforms:

```bash
git clone --depth 1 https://github.com/open-quantum-safe/liboqs.git
cmake -S liboqs -B liboqs/build -DCMAKE_INSTALL_PREFIX=/usr/local
cmake --build liboqs/build --parallel 8
sudo cmake --install liboqs/build
pip install python-oqs
```

Then `keygen` returns `algorithm: "ML-KEM-1024 (real-liboqs)"` instead of the
simulated fallback.

### Enabling CUDA-Q (NVIDIA GPU)

Follow NVIDIA's CUDA-Q install docs, then set `COMPUTE_PROVIDER=cudaq`. The
`anomaly` endpoint will run a parameterized quantum circuit on the GPU and
return a feature-dependent score.

## Deploy to a cloud GPU VM

1. Provision an NVIDIA GPU VM (AWS `g5.*`, GCP `n1-standard-* + T4/A100`, Azure `NC*`, or RunPod).
2. Install the [NVIDIA CUDA-Q container / cuQuantum Appliance](https://docs.nvidia.com/cuda/cuquantum/latest/appliance/index.html) (for `cudaq` provider) or just Docker for `local`/`qrng`.
3. Push this `backend/` directory, `docker compose up --build`, and put an HTTPS reverse proxy (Caddy/nginx) in front.
4. Point the Android app at the resulting `https://your-host/` URL (see `backend/../app/...` RetrofitClient + `.env` `NETSHIELD_BACKEND_URL`).

## Security notes

- Returning a private key over `/quantum/keygen` is unsafe for production —
  keys should be generated on-device. This endpoint preserves the app's
  existing contract but flags provenance honestly in `algorithm`.
- Use HTTPS in production (reverse proxy). The dev server is plain HTTP.
- `/gateway/ingest` stores payloads in memory for demo only — wire it to
  Kafka/your SIEM in production.
