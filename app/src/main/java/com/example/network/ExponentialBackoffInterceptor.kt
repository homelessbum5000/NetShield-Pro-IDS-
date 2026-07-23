package com.example.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.math.pow
import kotlin.random.Random

/**
 * Data class representing an event during a retry attempt.
 */
data class RetryLogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestampMs: Long = System.currentTimeMillis(),
    val targetHost: String,
    val endpoint: String,
    val attemptNumber: Int,
    val maxRetries: Int,
    val delayMs: Long,
    val reason: String,
    val isSuccess: Boolean = false,
    val isFinalFailure: Boolean = false
)

/**
 * Listener interface to receive real-time notifications of retry attempts.
 */
fun interface RetryEventListener {
    fun onRetryEvent(entry: RetryLogEntry)
}

/**
 * An OkHttp Interceptor that executes exponential backoff retries for failed HTTP requests.
 * Specifically designed for Quantum and Gateway server network resilience.
 */
class ExponentialBackoffInterceptor(
    var maxRetries: Int = 3,
    var initialDelayMs: Long = 1000L,
    var maxDelayMs: Long = 16000L,
    var backoffMultiplier: Double = 2.0,
    var useJitter: Boolean = true,
    private val listener: RetryEventListener? = null
) : Interceptor {

    companion object {
        private const val TAG = "ExponentialBackoff"
        
        // HTTP status codes eligible for exponential retry
        private val RETRYABLE_HTTP_CODES = setOf(
            429, // Too Many Requests
            500, // Internal Server Error
            502, // Bad Gateway
            503, // Service Unavailable
            504  // Gateway Timeout
        )
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val url = request.url.toString()
        val host = request.url.host
        val endpoint = request.url.encodedPath

        var attempt = 1
        var response: Response? = null
        var lastException: IOException? = null

        while (attempt <= maxRetries + 1) {
            try {
                Log.d(TAG, "[$host] Requesting attempt $attempt of ${maxRetries + 1}: $url")
                
                // Close previous unhandled response body if present
                response?.close()
                
                response = chain.proceed(request)

                if (response.isSuccessful) {
                    if (attempt > 1) {
                        val successLog = RetryLogEntry(
                            targetHost = host,
                            endpoint = endpoint,
                            attemptNumber = attempt,
                            maxRetries = maxRetries,
                            delayMs = 0L,
                            reason = "Request succeeded on attempt $attempt after previous retries.",
                            isSuccess = true
                        )
                        listener?.onRetryEvent(successLog)
                        Log.i(TAG, "[$host] Success on attempt $attempt")
                    }
                    return response
                }

                // Check if HTTP status code is retryable
                val statusCode = response.code
                if (!RETRYABLE_HTTP_CODES.contains(statusCode)) {
                    Log.w(TAG, "[$host] Received non-retryable HTTP status code: $statusCode. Aborting retries.")
                    return response
                }

                if (attempt > maxRetries) {
                    Log.e(TAG, "[$host] Reached max retries ($maxRetries) for HTTP status $statusCode.")
                    val failLog = RetryLogEntry(
                        targetHost = host,
                        endpoint = endpoint,
                        attemptNumber = attempt,
                        maxRetries = maxRetries,
                        delayMs = 0L,
                        reason = "HTTP $statusCode - Max retries exceeded.",
                        isFinalFailure = true
                    )
                    listener?.onRetryEvent(failLog)
                    return response
                }

                // Calculate exponential backoff delay
                val delay = calculateDelay(attempt)
                val reason = "HTTP $statusCode status received"

                Log.w(TAG, "[$host] Attempt $attempt failed ($reason). Retrying in ${delay}ms...")
                
                val retryLog = RetryLogEntry(
                    targetHost = host,
                    endpoint = endpoint,
                    attemptNumber = attempt,
                    maxRetries = maxRetries,
                    delayMs = delay,
                    reason = reason
                )
                listener?.onRetryEvent(retryLog)

                sleepUninterruptibly(delay)

            } catch (e: IOException) {
                lastException = e
                Log.e(TAG, "[$host] IOException on attempt $attempt: ${e.localizedMessage}")

                if (attempt > maxRetries) {
                    Log.e(TAG, "[$host] Reached max retries ($maxRetries) after IOException.")
                    val failLog = RetryLogEntry(
                        targetHost = host,
                        endpoint = endpoint,
                        attemptNumber = attempt,
                        maxRetries = maxRetries,
                        delayMs = 0L,
                        reason = "Network error: ${e.message ?: "IOException"} - Max retries exceeded.",
                        isFinalFailure = true
                    )
                    listener?.onRetryEvent(failLog)
                    throw e
                }

                val delay = calculateDelay(attempt)
                val reason = "Network failure: ${e.javaClass.simpleName}"

                val retryLog = RetryLogEntry(
                    targetHost = host,
                    endpoint = endpoint,
                    attemptNumber = attempt,
                    maxRetries = maxRetries,
                    delayMs = delay,
                    reason = reason
                )
                listener?.onRetryEvent(retryLog)

                sleepUninterruptibly(delay)
            }

            attempt++
        }

        response?.let { return it }
        throw lastException ?: IOException("Failed after $maxRetries retries with no response")
    }

    /**
     * Calculates exponential delay with optional randomized jitter:
     * Delay = min(MaxDelay, InitialDelay * (Multiplier ^ (attempt - 1))) + Jitter
     */
    fun calculateDelay(attempt: Int): Long {
        val baseDelay = (initialDelayMs * backoffMultiplier.pow((attempt - 1).toDouble())).toLong()
        val clampedDelay = baseDelay.coerceAtMost(maxDelayMs)

        return if (useJitter) {
            val maxJitter = (clampedDelay * 0.25).toLong().coerceAtLeast(10L)
            val jitterAmount = Random.nextLong(0, maxJitter)
            clampedDelay + jitterAmount
        } else {
            clampedDelay
        }
    }

    private fun sleepUninterruptibly(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
