package com.example.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface QuantumApiService {
    @GET("quantum/health")
    suspend fun checkHealth(): Response<ServerHealthResponse>

    @POST("quantum/random")
    suspend fun getRandomBytes(
        @Header("Authorization") authHeader: String = "",
        @Body request: QuantumRandomRequest
    ): Response<QuantumRandomResponse>

    @POST("quantum/keygen")
    suspend fun generateKyberKeys(
        @Header("Authorization") authHeader: String = ""
    ): Response<QuantumKeyGenResponse>

    @POST("quantum/anomaly")
    suspend fun scoreAnomaly(
        @Header("Authorization") authHeader: String = "",
        @Body request: QuantumAnomalyRequest
    ): Response<QuantumAnomalyResponse>
}

interface GatewayApiService {
    @GET("gateway/health")
    suspend fun checkHealth(): Response<ServerHealthResponse>

    @POST("gateway/ingest")
    suspend fun ingestBatch(
        @Header("Authorization") authHeader: String = "",
        @Body request: EncryptedIDSRequest
    ): Response<GatewayIngestResponse>
}
