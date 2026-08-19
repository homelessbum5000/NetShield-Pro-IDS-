from __future__ import annotations

from fastapi import APIRouter, Depends

from .config import Settings, get_settings
from .providers import get_provider
from .providers.base import ComputeProvider
from .routers import gateway, quantum

router = APIRouter()


@router.get("/health")
def root_health(settings: Settings = Depends(get_settings), provider: ComputeProvider = Depends(get_provider)):
    return {
        "status": "healthy",
        "compute_provider": settings.compute_provider,
        "provider_ready": provider.health(),
        "endpoints": ["/quantum/health", "/quantum/random", "/quantum/keygen", "/quantum/anomaly", "/gateway/health", "/gateway/ingest"],
    }


def create_app():
    from fastapi import FastAPI
    from fastapi.middleware.cors import CORSMiddleware

    settings = get_settings()

    app = FastAPI(title="NetShield Pro Backend", version="1.0.0")
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.cors_origin_list,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    app.include_router(router)
    app.include_router(quantum.router)
    app.include_router(gateway.router)
    return app


app = create_app()


if __name__ == "__main__":
    import uvicorn

    settings = get_settings()
    uvicorn.run("app.main:app", host=settings.host, port=settings.port, reload=False)
