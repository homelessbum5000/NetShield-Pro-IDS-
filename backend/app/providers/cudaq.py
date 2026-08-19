"""CUDA-Q / cuQuantum provider — runs quantum-circuit simulations on an NVIDIA GPU.

NVIDIA CUDA-Q (formerly QODA) and the cuQuantum SDK accelerate quantum-circuit
state-vector/tensor-network simulation on GPUs:
  https://developer.nvidia.com/cuquantum-sdk

This provider is ONLY used for research/demo anomaly scoring. It is lazily
imported so the backend runs fine on a machine without CUDA-Q installed; in that
case it falls back to the local provider.

To enable for real: install CUDA-Q (see NVIDIA docs) and set COMPUTE_PROVIDER=cudaq.
"""
from __future__ import annotations

import time
from typing import List, Tuple

from .base import ComputeProvider
from .local import LocalProvider


def _cudaq_available() -> bool:
    try:
        import cudaq  # type: ignore
        return True
    except Exception:
        return False


class CudaqProvider(ComputeProvider):
    name = "cudaq"

    def __init__(self):
        self._fallback = LocalProvider()
        self._ready = _cudaq_available()

    def health(self) -> bool:
        return self._ready

    def random_bytes(self, n: int) -> Tuple[str, str]:
        # QRNG-from-GPU-simulation is possible but overkill; defer to local CSPRNG.
        return self._fallback.random_bytes(n)

    def kyber_keygen(self) -> Tuple[str, str, str, str]:
        return self._fallback.kyber_keygen()

    def anomaly_score(self, features: List[float]) -> Tuple[float, bool, int]:
        if not self._ready or not features:
            return self._fallback.anomaly_score(features)

        try:
            import cudaq  # type: ignore

            start = time.perf_counter()
            n = min(8, len(features))

            # Build a parameterized circuit whose measurement distribution
            # depends on the input features. This is a *legitimate* use of a
            # quantum simulator: it produces a real, feature-dependent score.
            @cudaq.kernel  # type: ignore
            def scored_circuit(angles: List[float]):
                q = cudaq.qvector(n)  # type: ignore
                for i in range(n):
                    cudaq.ry(angles[i], q[i])  # type: ignore
                for i in range(n - 1):
                    cudaq.cx(q[i], q[i + 1])  # type: ignore
                cudaq.h(q[0])  # type: ignore

            angles = [float(features[i % len(features)]) * 3.14159 for i in range(n)]
            counts = cudaq.sample(scored_circuit, angles)  # type: ignore

            # A simple proxy for "anomalous": how concentrated is the output
            # distribution? A near-uniform distribution is "normal".
            probs = [c / sum(counts.values()) for c in counts.values()]
            import math
            entropy = -sum(p * math.log2(p) for p in probs if p > 0)
            max_entropy = math.log2(len(probs)) if len(probs) > 1 else 1.0
            concentration = 1.0 - (entropy / max_entropy if max_entropy else 0.0)

            score = round(min(1.0, concentration), 4)
            elapsed = max(1, int((time.perf_counter() - start) * 1000))
            return score, score >= 0.6, elapsed
        except Exception:
            return self._fallback.anomaly_score(features)
