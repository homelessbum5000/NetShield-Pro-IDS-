package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Quantum Service Data Models
@JsonClass(generateAdapter = true)
data class QuantumRandomRequest(
    @Json(name = "n_bytes") val nBytes: Int = 32
)

@JsonClass(generateAdapter = true)
data class QuantumRandomResponse(
    @Json(name = "status") val status: String = "success",
    @Json(name = "random_hex") val randomHex: String = "",
    @Json(name = "entropy_source") val entropySource: String = "quantum_cirq"
)

@JsonClass(generateAdapter = true)
data class QuantumKeyGenResponse(
    @Json(name = "status") val status: String = "success",
    @Json(name = "public_key") val publicKey: String = "",
    @Json(name = "private_key") val privateKey: String = "",
    @Json(name = "algorithm") val algorithm: String = "Kyber1024"
)

@JsonClass(generateAdapter = true)
data class QuantumAnomalyRequest(
    @Json(name = "features") val features: List<Float>
)

@JsonClass(generateAdapter = true)
data class QuantumAnomalyResponse(
    @Json(name = "anomaly_score") val anomalyScore: Float = 0f,
    @Json(name = "is_threat") val isThreat: Boolean = false,
    @Json(name = "quantum_circuit_eval") val circuitEvalTimeMs: Long = 0L
)

// Gateway Service Data Models
@JsonClass(generateAdapter = true)
data class EncryptedIDSRequest(
    @Json(name = "kyber_ciphertext") val kyberCiphertext: String,
    @Json(name = "wrapped_aes_key") val wrappedAesKey: String,
    @Json(name = "aes_iv") val aesIv: String,
    @Json(name = "payload_data") val payloadData: String,
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class GatewayIngestResponse(
    @Json(name = "status") val status: String = "accepted",
    @Json(name = "ingest_id") val ingestId: String = "",
    @Json(name = "message") val message: String = "Payload routed to Morpheus pipeline",
    @Json(name = "kafka_topic") val kafkaTopic: String = "ids-events"
)

// Health Check Response
@JsonClass(generateAdapter = true)
data class ServerHealthResponse(
    @Json(name = "status") val status: String = "healthy",
    @Json(name = "server_time") val serverTime: Long = System.currentTimeMillis(),
    @Json(name = "quantum_ready") val quantumReady: Boolean = true,
    @Json(name = "gateway_status") val gatewayStatus: String = "online"
)
