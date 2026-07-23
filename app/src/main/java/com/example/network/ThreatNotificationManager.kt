package com.example.network

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LocalThreatAlert(
    val id: Int,
    val title: String,
    val description: String,
    val severity: String, // CRITICAL, HIGH, MEDIUM
    val timestamp: String,
    val isNotified: Boolean
)

class ThreatNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "ids_threat_alerts_channel"
        const val CHANNEL_NAME = "NetShield Threat Alerts"
        const val CHANNEL_DESC = "High priority local push alerts for IDS detected network threats"
    }

    private val notificationManager = NotificationManagerCompat.from(context)
    private val _alertHistory = MutableStateFlow<List<LocalThreatAlert>>(emptyList())
    val alertHistory: StateFlow<List<LocalThreatAlert>> = _alertHistory.asStateFlow()

    private var nextNotificationId = 1001

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 100, 250)
                setShowBadge(true)
            }
            val systemManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            systemManager.createNotificationChannel(channel)
        }
    }

    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun triggerHighSeverityThreatNotification(
        title: String,
        message: String,
        severity: String = "CRITICAL"
    ): Boolean {
        val alertId = nextNotificationId++
        val timeFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val alert = LocalThreatAlert(
            id = alertId,
            title = title,
            description = message,
            severity = severity,
            timestamp = timeFormatted,
            isNotified = hasPermission()
        )

        val updatedList = _alertHistory.value.toMutableList().apply {
            add(0, alert)
        }
        _alertHistory.value = updatedList

        if (!hasPermission()) {
            return false
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 [IDS THREAT ALERT] $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            notificationManager.notify(alertId, builder.build())
            return true
        } catch (e: SecurityException) {
            return false
        }
    }
}
