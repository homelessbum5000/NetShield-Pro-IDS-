package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val QUANTUM_BASE_URL = "http://quantum.onion:9000/"
    private const val GATEWAY_BASE_URL = "http://gateway.onion/"

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

    // Shared Simulation Interceptor for demo & testing resilience
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

    // OkHttpClient specifically configured with exponential backoff for Quantum Server
    val quantumOkHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(mockSimulator)
        .addInterceptor(quantumBackoffInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    // OkHttpClient specifically configured with exponential backoff for Gateway Server
    val gatewayOkHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(mockSimulator)
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
