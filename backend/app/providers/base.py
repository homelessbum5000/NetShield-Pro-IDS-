"""Provider interface. Each concrete provider implements these three operations.

A 'provider' is the compute backend behind the app's mocked /quantum/* endpoints.
Real QPUs and GPU simulators are NOT used for inline IDS decisions — they back
research/entropy/demo operations and are always behind an async, fail-safe wrapper.
"""
from __future__ import annotations

from abc import ABC, abstractmethod
from typing import List, Tuple


class ComputeProvider(ABC):
    name: str = "abstract"

    @abstractmethod
    def random_bytes(self, n: int) -> Tuple[str, str]:
        """Return (random_hex, entropy_source)."""
        raise NotImplementedError

    @abstractmethod
    def kyber_keygen(self) -> Tuple[str, str, str, str]:
        """Return (public_key_hex, private_key_hex, algorithm, status)."""
        raise NotImplementedError

    @abstractmethod
    def anomaly_score(self, features: List[float]) -> Tuple[float, bool, int]:
        """Return (anomaly_score 0..1, is_threat, eval_time_ms)."""
        raise NotImplementedError

    def health(self) -> bool:
        return True
