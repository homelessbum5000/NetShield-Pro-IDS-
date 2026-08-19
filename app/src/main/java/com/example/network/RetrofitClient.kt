package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Real backend URL injected from .env via the Secrets Gradle Plugin
    // (NETSHIELD_BACKEND_URL). Use https:// in production. Default points at the
    // FastAPI backend on the dev host as seen from the Android emulator
    // (10.0.2.2 == host loopback).
    private fun normalizeBaseUrl(url: String): String =
        if (url.endsWith("/")) url else "$url/"

    private val BACKEND_BASE_URL: String = normalizeBaseUrl(
        BuildConfig.NETSHIELD_BACKEND_URL.takeIf { it.isNotBlank() }
            ?: "http://10.0.2.2:8000/"
    )

    // Mock simulator is opt-in via NETSHIELD_USE_MOCK_NETWORK so debug builds
    // can hit the real backend by default (set to "true" to restore mock data).
    private val USE_MOCK_NETWORK: Boolean =
        BuildConfig.NETSHIELD_USE_MOCK_NETWORK.equals("true", ignoreCase = true)

    private val QUANTUM_BASE_URL = BACKEND_BASE_URL
    private val GATEWAY_BASE_URL = BACKEND_BASE_URL

    private val _retryLogs = MutableStateFlow<List<RetryLogEntry>>(emptyList())
    val retryLogs: StateFlow<List<RetryLogEntry>> = _retryLogs.asStateFlow()

    private val listener = RetryEventListener { entry ->
        val currentList = _retryLogs.value.toMutableList()
        currentList.add(0, entry) // Add newest at top
        if (currentList.size > 100) {
            currentList.removeAt(currentList.lastIndex)
        }
        _retryLogs.value = currentList
    }

    // Shared Simulation Interceptor — DEBUG ONLY. In release builds the app
    // talks to the real backend (BACKEND_BASE_URL) and real responses flow
    // through the backoff/retry interceptor instead of canned mock data.
    val mockSimulator = MockNetworkSimulationInterceptor()

    // Exponential Backoff Interceptor for Quantum Server
    val quantumBackoffInterceptor = ExponentialBackoffInterceptor(
        maxRetries = 3,
        initialDelayMs = 1000L,
        maxDelayMs = 16000L,
        backoffMultiplier = 2.0,
        useJitter = true,
        listener = listener
    )

    // Exponential Backoff Interceptor for Gateway Server
    val gatewayBackoffInterceptor = ExponentialBackoffInterceptor(
        maxRetries = 4,
        initialDelayMs = 800L,
        maxDelayMs = 12000L,
        backoffMultiplier = 2.0,
        useJitter = true,
        listener = listener
    )

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // OkHttpClient for the Quantum Server. The mock simulator is installed only
    // in debug builds so release builds hit the real backend and actually
    // exercise the exponential backoff / retry logic.
    val quantumOkHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .apply { if (USE_MOCK_NETWORK) addInterceptor(mockSimulator) }
        .addInterceptor(quantumBackoffInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    // OkHttpClient for the Gateway Server (same opt-in mock gating).
    val gatewayOkHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .apply { if (USE_MOCK_NETWORK) addInterceptor(mockSimulator) }
        .addInterceptor(gatewayBackoffInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val quantumApiService: QuantumApiService = Retrofit.Builder()
        .baseUrl(QUANTUM_BASE_URL)
        .client(quantumOkHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(QuantumApiService::class.java)

    val gatewayApiService: GatewayApiService = Retrofit.Builder()
        .baseUrl(GATEWAY_BASE_URL)
        .client(gatewayOkHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GatewayApiService::class.java)

    fun clearLogs() {
        _retryLogs.value = emptyList()
    }

    fun updateConfig(
        maxRetries: Int,
        initialDelayMs: Long,
        maxDelayMs: Long,
        backoffMultiplier: Double,
        useJitter: Boolean
    ) {
        quantumBackoffInterceptor.maxRetries = maxRetries
        quantumBackoffInterceptor.initialDelayMs = initialDelayMs
        quantumBackoffInterceptor.maxDelayMs = maxDelayMs
        quantumBackoffInterceptor.backoffMultiplier = backoffMultiplier
        quantumBackoffInterceptor.useJitter = useJitter

        gatewayBackoffInterceptor.maxRetries = maxRetries
        gatewayBackoffInterceptor.initialDelayMs = initialDelayMs
        gatewayBackoffInterceptor.maxDelayMs = maxDelayMs
        gatewayBackoffInterceptor.backoffMultiplier = backoffMultiplier
        gatewayBackoffInterceptor.useJitter = useJitter
    }
}
