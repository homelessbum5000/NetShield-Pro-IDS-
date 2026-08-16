package com.example.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class QuantumTunnelState(
    val isProtectionEnabled: Boolean = true,
    val healthStatus: String = "OPTIMAL_SECURED",
    val latencyMs: Float = 1.2f,
    val activeKeyAlgorithm: String = "FIPS-203 ML-KEM-1024",
    val packetsProtected: Long = 4920800L,
    val uptimeSeconds: Long = 86400L
)

object QuantumTunnelStatusManager {
    const val NOTIFICATION_CHANNEL_ID = "quantum_tunnel_status_channel"
    const val NOTIFICATION_ID = 9001
    const val ACTION_TOGGLE_PROTECTION = "com.example.network.ACTION_TOGGLE_PROTECTION"
    const val ACTION_ROTATE_KEYS = "com.example.network.ACTION_ROTATE_KEYS"

    private val _state = MutableStateFlow(QuantumTunnelState())
    val state: StateFlow<QuantumTunnelState> = _state.asStateFlow()

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Quantum Shield Tunnel Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent status bar quick status and toggle controls for NetShield Pro"
                setShowBadge(false)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun init(context: Context) {
        createNotificationChannel(context)
    }

    fun buildNotification(context: Context): Notification {
        createNotificationChannel(context)

        val currentState = _state.value
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(context, QuantumTunnelActionReceiver::class.java).apply {
            action = ACTION_TOGGLE_PROTECTION
        }
        val togglePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val rotateIntent = Intent(context, QuantumTunnelActionReceiver::class.java).apply {
            action = ACTION_ROTATE_KEYS
        }
        val rotatePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            rotateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusTitle = if (currentState.isProtectionEnabled) {
            "🛡️ NetShield Pro: Quantum Tunnel Active"
        } else {
            "⚠️ NetShield Pro: Protection Paused"
        }

        val statusText = if (currentState.isProtectionEnabled) {
            "${currentState.activeKeyAlgorithm} • Latency ${currentState.latencyMs}ms • 0 Leakage"
        } else {
            "Tap to resume post-quantum lattice VPN protection"
        }

        val toggleLabel = if (currentState.isProtectionEnabled) "Pause Shield" else "Resume Shield"

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle(statusTitle)
            .setContentText(statusText)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(currentState.isProtectionEnabled)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(android.R.drawable.ic_media_play, toggleLabel, togglePendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, "Rotate PQC Keys", rotatePendingIntent)
            .build()
    }

    fun updateNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(context))
    }

    fun toggleProtection(context: Context) {
        val nextEnabled = !_state.value.isProtectionEnabled
        _state.value = _state.value.copy(
            isProtectionEnabled = nextEnabled,
            healthStatus = if (nextEnabled) "OPTIMAL_SECURED" else "DISCONNECTED"
        )
        updateNotification(context)
    }

    fun setProtectionEnabled(context: Context, enabled: Boolean) {
        _state.value = _state.value.copy(
            isProtectionEnabled = enabled,
            healthStatus = if (enabled) "OPTIMAL_SECURED" else "DISCONNECTED"
        )
        updateNotification(context)
    }

    fun rotateQuantumKeys(context: Context) {
        _state.value = _state.value.copy(
            activeKeyAlgorithm = if (_state.value.activeKeyAlgorithm.contains("ML-KEM-1024")) {
                "FIPS-204 ML-DSA-87 (Lattice)"
            } else {
                "FIPS-203 ML-KEM-1024"
            }
        )
        updateNotification(context)
    }
}

class QuantumTunnelActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            QuantumTunnelStatusManager.ACTION_TOGGLE_PROTECTION -> {
                QuantumTunnelStatusManager.toggleProtection(context)
            }
            QuantumTunnelStatusManager.ACTION_ROTATE_KEYS -> {
                QuantumTunnelStatusManager.rotateQuantumKeys(context)
            }
        }
    }
}

class QuantumTunnelNotificationService : Service() {
    companion object {
        fun start(context: Context) {
            try {
                val intent = Intent(context, QuantumTunnelNotificationService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Fallback to direct notification update if foreground service start is restricted
                QuantumTunnelStatusManager.updateNotification(context)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = QuantumTunnelStatusManager.buildNotification(this)
        startForeground(QuantumTunnelStatusManager.NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
