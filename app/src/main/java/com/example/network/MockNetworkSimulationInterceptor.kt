package com.example.network

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

enum class SimulationMode {
    NONE,                   // Normal network call (or mock success)
    MOCK_SUCCESS,           // Always return 200 OK immediately
    FORCE_FAIL_THEN_SUCCEED, // Fails first N times with HTTP 503, then succeeds
    FORCE_503_OVERLOAD,     // Always fails with 503 Service Unavailable
    FORCE_429_RATE_LIMIT,   // Always fails with 429 Too Many Requests
    FORCE_NETWORK_OUTAGE    // Throws IOException (SocketTimeoutException)
}

class MockNetworkSimulationInterceptor : Interceptor {

    var mode: SimulationMode = SimulationMode.FORCE_FAIL_THEN_SUCCEED
    var failCountTarget: Int = 2
    private var currentFailuresCount = 0

    fun resetCounts() {
        currentFailuresCount = 0
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        return when (mode) {
            SimulationMode.NONE -> {
                try {
                    chain.proceed(request)
                } catch (e: IOException) {
                    // If real network isn't reachable, fallback to mock success for demo
                    buildSuccessResponse(request, "{\"status\":\"success\", \"mode\":\"live_fallback\"}")
                }
            }

            SimulationMode.MOCK_SUCCESS -> {
                buildSuccessResponse(request, "{\"status\":\"success\", \"message\":\"Instant success response\"}")
            }

            SimulationMode.FORCE_FAIL_THEN_SUCCEED -> {
                if (currentFailuresCount < failCountTarget) {
                    currentFailuresCount++
                    buildErrorResponse(
                        request = request,
                        code = 503,
                        message = "Service Unavailable (Simulated failure $currentFailuresCount of $failCountTarget)"
                    )
                } else {
                    currentFailuresCount = 0
                    buildSuccessResponse(
                        request = request,
                        json = "{\"status\":\"success\", \"message\":\"Recovered after $failCountTarget retry attempts!\"}"
                    )
                }
            }

            SimulationMode.FORCE_503_OVERLOAD -> {
                buildErrorResponse(
                    request = request,
                    code = 503,
                    message = "503 Service Unavailable - Server Overloaded"
                )
            }

            SimulationMode.FORCE_429_RATE_LIMIT -> {
                buildErrorResponse(
                    request = request,
                    code = 429,
                    message = "429 Too Many Requests - Quantum Rate Limit Exceeded"
                )
            }

            SimulationMode.FORCE_NETWORK_OUTAGE -> {
                throw IOException("Simulated Network Timeout / Connection Refused (Gateway Unreachable)")
            }
        }
    }

    private fun buildSuccessResponse(request: okhttp3.Request, json: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun buildErrorResponse(request: okhttp3.Request, code: Int, message: String): Response {
        val json = "{\"error\":\"$message\", \"code\":$code}"
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
    }
}
