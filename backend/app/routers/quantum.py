from __future__ import annotations

import time

from fastapi import APIRouter, Depends

from ..config import Settings, get_settings
from ..providers import get_provider
from ..providers.base import ComputeProvider
from ..schemas import (
    QuantumAnomalyRequest,
    QuantumAnomalyResponse,
    QuantumKeyGenResponse,
    QuantumRandomRequest,
    QuantumRandomResponse,
    ServerHealthResponse,
)

router = APIRouter(prefix="/quantum", tags=["quantum"])


def _provider() -> ComputeProvider:
    return get_provider()


@router.get("/health", response_model=ServerHealthResponse)
def health(settings: Settings = Depends(get_settings), provider: ComputeProvider = Depends(_provider)):
    return ServerHealthResponse(
        status="healthy",
        server_time=int(time.time() * 1000),
        quantum_ready=provider.health(),
        gateway_status="online",
    )


@router.post("/random", response_model=QuantumRandomResponse)
def random_bytes(
    request: QuantumRandomRequest,
    provider: ComputeProvider = Depends(_provider),
):
    hex_str, source = provider.random_bytes(request.n_bytes)
    return QuantumRandomResponse(status="success", random_hex=hex_str, entropy_source=source)


@router.post("/keygen", response_model=QuantumKeyGenResponse)
def keygen(provider: ComputeProvider = Depends(_provider), settings: Settings = Depends(get_settings)):
    pk, sk, algorithm, status = provider.kyber_keygen()
    # NOTE: returning a private key over an API is unsafe for real use; keys
    # should be generated on-device. We preserve the app's existing contract
    # but flag provenance honestly in `algorithm`.
    return QuantumKeyGenResponse(
        status=status,
        public_key=pk,
        private_key=sk,
        algorithm=f"{algorithm} ({status})",
    )


@router.post("/anomaly", response_model=QuantumAnomalyResponse)
def anomaly(
    request: QuantumAnomalyRequest,
    provider: ComputeProvider = Depends(_provider),
):
    score, is_threat, elapsed = provider.anomaly_score(request.features)
    return QuantumAnomalyResponse(
        anomaly_score=score,
        is_threat=is_threat,
        quantum_circuit_eval_time_ms=elapsed,
    )
