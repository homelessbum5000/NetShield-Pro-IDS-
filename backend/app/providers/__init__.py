"""Provider factory. Selects the compute backend from settings."""
from __future__ import annotations

from ..config import get_settings
from .base import ComputeProvider
from .cudaq import CudaqProvider
from .ibm import IbmProvider
from .local import LocalProvider
from .qrng import QrngProvider

__all__ = ["get_provider"]


def get_provider() -> ComputeProvider:
    settings = get_settings()
    name = settings.compute_provider.lower()

    if name in ("cudaq", "cuquantum"):
        return CudaqProvider()
    if name == "qrng":
        return QrngProvider(settings.qrng_api_url, settings.qrng_api_key)
    if name == "ibm":
        return IbmProvider(token=settings.ibm_token, instance=settings.ibm_instance)
    if name == "braket":
        # AWS Braket is wired similarly to IBM; left as a stub that falls back locally.
        return LocalProvider()
    # default
    return LocalProvider()
