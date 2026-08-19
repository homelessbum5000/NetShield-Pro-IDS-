"""
Pydantic schemas mirroring the Android app's ApiModels.kt exactly,
so the app's Retrofit/Moshi models deserialize the backend's responses unchanged.
"""
from __future__ import annotations

import time
from typing import List

from pydantic import BaseModel, Field


# ---- Quantum service --------------------------------------------------------

class QuantumRandomRequest(BaseModel):
    n_bytes: int = Field(default=32, ge=1, le=4096)


class QuantumRandomResponse(BaseModel):
    status: str = "success"
    random_hex: str = ""
    entropy_source: str = "os_csprng"


class QuantumKeyGenResponse(BaseModel):
    status: str = "success"
    public_key: str = ""
    private_key: str = ""
    algorithm: str = "Kyber1024"


class QuantumAnomalyRequest(BaseModel):
    features: List[float] = Field(default_factory=list)


class QuantumAnomalyResponse(BaseModel):
    anomaly_score: float = 0.0
    is_threat: bool = False
    quantum_circuit_eval_time_ms: int = 0


# ---- Gateway service --------------------------------------------------------

class EncryptedIDSRequest(BaseModel):
    kyber_ciphertext: str
    wrapped_aes_key: str
    aes_iv: str
    payload_data: str
    timestamp: int = Field(default_factory=lambda: int(time.time() * 1000))


class GatewayIngestResponse(BaseModel):
    status: str = "accepted"
    ingest_id: str = ""
    message: str = "Payload routed to Morpheus pipeline"
    kafka_topic: str = "ids-events"


# ---- Health -----------------------------------------------------------------

class ServerHealthResponse(BaseModel):
    status: str = "healthy"
    server_time: int = Field(default_factory=lambda: int(time.time() * 1000))
    quantum_ready: bool = True
    gateway_status: str = "online"
