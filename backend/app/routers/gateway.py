from __future__ import annotations

import time

from fastapi import APIRouter, Depends

from ..config import Settings, get_settings
from ..providers import get_provider
from ..providers.base import ComputeProvider
from ..schemas import EncryptedIDSRequest, GatewayIngestResponse, ServerHealthResponse
from .store import ingest_store

router = APIRouter(prefix="/gateway", tags=["gateway"])


@router.get("/health", response_model=ServerHealthResponse)
def health(provider: ComputeProvider = Depends(get_provider)):
    return ServerHealthResponse(
        status="healthy",
        server_time=int(time.time() * 1000),
        quantum_ready=provider.health(),
        gateway_status="online",
    )


@router.post("/ingest", response_model=GatewayIngestResponse)
def ingest(
    request: EncryptedIDSRequest,
    settings: Settings = Depends(get_settings),
):
    ingest_id = ingest_store.add(
        {
            "kyber_ciphertext": request.kyber_ciphertext,
            "wrapped_aes_key": request.wrapped_aes_key,
            "aes_iv": request.aes_iv,
            "payload_data": request.payload_data,
            "timestamp": request.timestamp,
            "provider": settings.compute_provider,
        }
    )
    return GatewayIngestResponse(
        status="accepted",
        ingest_id=ingest_id,
        message="Payload routed to Morpheus pipeline",
        kafka_topic="ids-events",
    )
