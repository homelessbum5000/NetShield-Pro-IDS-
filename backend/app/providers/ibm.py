"""IBM Quantum provider — submits async jobs to real QPUs via Qiskit Runtime.

Real QPUs are queued, noisy, and high-latency. They are NEVER used for inline
decisions. This provider exists only to power a "Quantum Lab" demo panel that
submits a job, polls for completion, and displays the result.

To enable: pip install qiskit qiskit-ibm-runtime, set IBM_TOKEN, COMPUTE_PROVIDER=ibm.
Without Qiskit installed, all calls fall back to the local provider.
"""
from __future__ import annotations

from typing import List, Tuple

from .base import ComputeProvider
from .local import LocalProvider


def _qiskit_available() -> bool:
    try:
        import qiskit  # type: ignore
        import qiskit_ibm_runtime  # type: ignore  # noqa: F401
        return True
    except Exception:
        return False


class IbmProvider(ComputeProvider):
    name = "ibm"

    def __init__(self, token: str = "", instance: str = ""):
        self._fallback = LocalProvider()
        self._token = token
        self._instance = instance
        self._ready = bool(token) and _qiskit_available()

    def health(self) -> bool:
        return self._ready

    def random_bytes(self, n: int) -> Tuple[str, str]:
        # Real-QPU QRNG is possible but slow/queued; defer to local for the API.
        return self._fallback.random_bytes(n)

    def kyber_keygen(self) -> Tuple[str, str, str, str]:
        return self._fallback.kyber_keygen()

    def anomaly_score(self, features: List[float]) -> Tuple[float, bool, int]:
        # Submitting a QPU job for a live anomaly score is impractical.
        return self._fallback.anomaly_score(features)

    def submit_demo_job(self, shots: int = 1024) -> str:
        """Submit a simple Bell-state job. Returns a job id (or a fallback marker)."""
        if not self._ready:
            return "simulated:no_qiskit"
        try:
            from qiskit import QuantumCircuit  # type: ignore
            from qiskit_ibm_runtime import QiskitRuntimeService, Sampler  # type: ignore

            service = QiskitRuntimeService(token=self._token, instance=self._instance or None)
            qc = QuantumCircuit(2)
            qc.h(0)
            qc.cx(0, 1)
            qc.measure_all()
            backend = service.least_busy(operational=True, simulator=False)
            sampler = Sampler(backend)
            job = sampler.run(qc, shots=shots)
            return job.job_id()
        except Exception as e:
            return f"simulated:error:{e}"
