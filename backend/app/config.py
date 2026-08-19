"""Runtime configuration. All values can be overridden via env/.env."""
from __future__ import annotations

from functools import lru_cache
from typing import List

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_prefix="", extra="ignore")

    # Which compute provider backs the "quantum" endpoints.
    # local | cudaq | cuquantum | qrng | ibm | braket
    compute_provider: str = "local"

    # Backend / network
    host: str = "0.0.0.0"
    port: int = 8000
    cors_origins: str = "*"

    # QRNG provider (optional). If set, /quantum/random can source true quantum entropy.
    qrng_api_url: str = ""        # e.g. https://api.qrngapi.com/api/random
    qrng_api_key: str = ""

    # IBM Quantum (optional, async research jobs only).
    ibm_token: str = ""
    ibm_instance: str = ""

    # Kyber/ML-KEM key generation. Use a real vetted library when available.
    use_real_kyber: bool = True   # fall back to a clearly-marked simulated keypair if liboqs missing

    @property
    def cors_origin_list(self) -> List[str]:
        return [o.strip() for o in self.cors_origins.split(",") if o.strip()]


@lru_cache
def get_settings() -> Settings:
    return Settings()
