"""In-memory ingest log for demo purposes (replace with Kafka/queue in prod)."""
from __future__ import annotations

import threading
import uuid
from collections import deque
from typing import Deque, Dict


class IngestStore:
    def __init__(self, maxlen: int = 1000):
        self._lock = threading.Lock()
        self._items: Deque[Dict] = deque(maxlen=maxlen)

    def add(self, item: Dict) -> str:
        ingest_id = uuid.uuid4().hex
        item = {**item, "ingest_id": ingest_id}
        with self._lock:
            self._items.append(item)
        return ingest_id

    def list(self) -> list:
        with self._lock:
            return list(self._items)


ingest_store = IngestStore()
