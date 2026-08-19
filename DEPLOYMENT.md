# NetShield Pro — Cloud + Quantum/GPU Deployment Guide

How to take the mockup's "quantum" features and back them with **real** compute in
the cloud: a FastAPI backend you control, with a pluggable provider that can run
on an NVIDIA GPU (CUDA-Q / cuQuantum) or talk to real quantum hardware (IBM
Quantum), without changing the Android app.

---

## Architecture

```
Android app (unchanged UI)
   │  Retrofit over HTTPS  (BuildConfig.NETSHIELD_BACKEND_URL)
   ▼
FastAPI backend  (backend/)            ← real /quantum/* and /gateway/* endpoints
   │
   ├─ local      : OS CSPRNG + (real Kyber via liboqs, else clearly-simulated)
   ├─ qrng       : true quantum entropy from a cloud QRNG API
   ├─ cudaq      : NVIDIA GPU quantum-circuit simulation (CUDA-Q / cuQuantum)
   └─ ibm       : async jobs on real IBM QPUs (demo panel only)
```

The app's existing Retrofit/Moshi models deserialize the backend's responses
unchanged — the JSON contract matches `ApiModels.kt` exactly.

---

## 1. Run the backend

### Locally
```bash
cd backend
pip install -r requirements.txt
cp .env.example .env
uvicorn app.main:app --reload --port 8000
```

### Docker (any cloud VM)
```bash
cd backend
cp .env.example .env
docker compose up --build      # http://localhost:8000
```

### Verify
```bash
curl http://localhost:8000/health
curl -X POST http://localhost:8000/quantum/random -H "Content-Type: application/json" -d '{"n_bytes":16}'
curl -X POST http://localhost:8000/quantum/keygen
curl -X POST http://localhost:8000/quantum/anomaly -H "Content-Type: application/json" -d '{"features":[0.95,0.92,0.9]}'
```

---

## 2. Point the Android app at the backend

The app's `RetrofitClient` now reads `BuildConfig.NETSHIELD_BACKEND_URL`
(injected by the Secrets Gradle Plugin from `.env`) and installs the mock
simulator **only in debug builds** — release builds hit the real backend.

`.env` (or `.env.example`):
```
NETSHIELD_BACKEND_URL=http://10.0.2.2:8000/   # emulator → host loopback
```
For a real device / production, set this to your deployed `https://...` URL.

Then build with Android Studio / `./gradlew assembleRelease` (requires the
Android SDK; not buildable in this sandbox).

---

## 3. Choose your compute provider

Set `COMPUTE_PROVIDER` in `backend/.env`:

| Value | What it does | Setup |
|---|---|---|
| `local` | OS CSPRNG; simulated Kyber (real ML-KEM if liboqs installed); deterministic anomaly scoring | none |
| `qrng` | True quantum-origin entropy from a cloud QRNG API | `QRNG_API_URL` (+ key) |
| `cudaq` / `cuquantum` | Quantum-circuit anomaly scoring on an NVIDIA GPU | install CUDA-Q on a GPU host |
| `ibm` | Async quantum jobs on real IBM QPUs (demo only) | `IBM_TOKEN` + qiskit |

### Real Kyber (ML-KEM-1024)
Build liboqs + install `python-oqs` (see `backend/README.md`). Then `keygen`
returns `algorithm: "ML-KEM-1024 (real-liboqs)"`.

### NVIDIA GPU quantum simulation (CUDA-Q)
Provision a GPU VM (AWS g5, GCP + T4/A100, Azure NC, or RunPod), install
[NVIDIA CUDA-Q](https://developer.nvidia.com/cuquantum-sdk) (or use the
[cuQuantum Appliance](https://docs.nvidia.com/cuda/cuquantum/latest/appliance/index.html)
container), set `COMPUTE_PROVIDER=cudaq`, and the `/quantum/anomaly` endpoint
runs a real parameterized quantum circuit on the GPU.

### Real quantum hardware (IBM)
`COMPUTE_PROVIDER=ibm`, `IBM_TOKEN=...`, `pip install qiskit qiskit-ibm-runtime`.
Real QPUs are queued/noisy and are used **only** for the demo job path — never
for inline blocking, because Kyber is post-quantum but classical, and QPUs are
too slow for live decisions.

---

## What's honest now

- The "quantum random" endpoint reports its real entropy source
  (`os_csprng`, `cloud_qrng`, …) — no false claim of a quantum computer.
- The "Kyber keygen" endpoint reports whether it used real liboqs or a simulated
  fallback, in the `algorithm` field.
- The mock network interceptor is gone from release builds — the app talks to
  your real backend.
- Hardcoded bearer tokens are removed.

---

## Next steps (toward a real product)

This gives you a real cloud backend behind the app's existing UI. The remaining
work to make it a genuine security product (not just a backend) is the
on-device traffic capture via Android `VpnService` — see the Product Roadmap
deliverable for that track.
