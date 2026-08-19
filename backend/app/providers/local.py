"""Local provider — no external services. This is the safe default.

- random_bytes: OS CSPRNG (secrets). True cryptographic randomness, just not
  *quantum-origin*. Honest about its entropy source.
- kyber_keygen: real ML-KEM-1024 via liboqs (python-oqs) when installed; otherwise
  a clearly-marked simulated keypair. We never claim a quantum computer here —
  Kyber is post-quantum but runs on classical hardware.
- anomaly_score: deterministic, explainable scoring from input features. No
  fake "LLM confirmed zero-day" claims.
"""
from __future__ import annotations

import secrets
import time
from typing import List, Tuple

from .base import ComputeProvider


def _has_oqs() -> bool:
    try:
        import oqs  # type: ignore
        return True
    except Exception:
        return False


class LocalProvider(ComputeProvider):
    name = "local"

    def random_bytes(self, n: int) -> Tuple[str, str]:
        return secrets.token_hex(n), "os_csprng"

    def kyber_keygen(self) -> Tuple[str, str, str, str]:
        if _has_oqs():
            try:
                import oqs  # type: ignore
                alg = "ML-KEM-1024"
                if alg not in oqs.get_enabled_kem_mechanisms():
                    alg = "Kyber1024"
                with oqs.KeyEncapsulation(alg) as kem:
                    pk = kem.generate_keypair()
                    sk = kem.export_secret_key()
                return (
                    pk.hex() if isinstance(pk, (bytes, bytearray)) else str(pk),
                    sk.hex() if isinstance(sk, (bytes, bytearray)) else str(sk),
                    alg,
                    "real-liboqs",
                )
            except Exception as e:  # pragma: no cover - defensive
                return self._simulated_keypair(f"liboqs_error:{e}")

        return self._simulated_keypair("liboqs_not_installed")

    @staticmethod
    def _simulated_keypair(reason: str) -> Tuple[str, str, str, str]:
        # Honest placeholder: arbitrary random bytes sized like ML-KEM-1024.
        pk = secrets.token_hex(1568)   # 1568-byte public key -> 3136 hex chars
        sk = secrets.token_hex(3168)  # 3168-byte secret key -> 6336 hex chars
        return pk, sk, "Kyber1024", f"simulated:{reason}"

    def anomaly_score(self, features: List[float]) -> Tuple[float, bool, int]:
        start = time.perf_counter()
        if not features:
            return 0.0, False, 0

        # Simple, explainable signal: how far is the mean feature magnitude
        # from a 0.5 baseline, combined with variance (a proxy for
        # "spread" of suspicious indicators).
        mean = sum(features) / len(features)
        var = sum((f - mean) ** 2 for f in features) / len(features)
        deviation = abs(mean - 0.5)

        score = min(1.0, (deviation * 1.6) + (var * 0.8))
        is_threat = score >= 0.6
        elapsed = max(1, int((time.perf_counter() - start) * 1000))
        return round(score, 4), is_threat, elapsed
