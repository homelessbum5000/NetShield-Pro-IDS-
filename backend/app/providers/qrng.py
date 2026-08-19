"""QRNG provider — sources true quantum-origin entropy from a cloud QRNG API.

Example compatible services:
- ANU QRNG (now AWS-hosted, requires API key): https://quantumnumbers.anu.edu.au
- QRNG API (qrngapi.com): https://qrngapi.com/api/random

If no API is configured or the call fails, we transparently fall back to the
local CSPRNG so the endpoint never breaks — but we report the real source.
"""
from __future__ import annotations

from typing import Tuple

import httpx

from .base import ComputeProvider
from .local import LocalProvider


class QrngProvider(ComputeProvider):
    name = "qrng"

    def __init__(self, api_url: str, api_key: str):
        self.api_url = api_url
        self.api_key = api_key
        self._fallback = LocalProvider()

    def random_bytes(self, n: int) -> Tuple[str, str]:
        if not self.api_url:
            return self._fallback.random_bytes(n)
        try:
            # QRNG APIs differ; we try a common ?bytes=N&format=hex shape.
            headers = {}
            if self.api_key:
                headers["X-API-Key"] = self.api_key
            with httpx.Client(timeout=5.0) as client:
                resp = client.get(self.api_url, params={"bytes": n, "format": "hex"}, headers=headers)
                resp.raise_for_status()
                data = resp.json()
            hex_str = data.get("data") or data.get("random_hex") or data.get("hex") or ""
            if hex_str:
                return hex_str, "cloud_qrng"
        except Exception:
            pass
        return self._fallback.random_bytes(n)

    def kyber_keygen(self) -> Tuple[str, str, str, str]:
        # Key generation must use a vetted KEM library, not a QRNG API.
        return self._fallback.kyber_keygen()

    def anomaly_score(self, features):
        return self._fallback.anomaly_score(features)

    def health(self) -> bool:
        # Only "ready" if a real QRNG API is configured; otherwise we fall
        # back to the local CSPRNG.
        return bool(self.api_url)
