package com.moveoftoday.walkorwait

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "stand_emergency_channel"
        const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "긴급 모드 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "긴급 15분 사용 타이머"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showEmergencyNotification(minutes: Long, seconds: Long) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🆘 긴급 모드 활성")
            .setContentText("남은 시간: ${minutes}분 ${seconds}초")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true) // 스와이프로 지울 수 없음
            .setContentIntent(pendingIntent)
            .setProgress(900, (minutes * 60 + seconds).toInt(), false)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun showBlockedNotification(appName: String, currentProgress: Double, goal: Double, remaining: Double, estimatedTime: String, unit: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val (currentText, goalText, remainingText) = if (unit == "km") {
            Triple(
                String.format("%.2f", currentProgress),
                String.format("%.2f", goal),
                String.format("%.2f", remaining)
            )
        } else {
            Triple(
                currentProgress.toInt().toString(),
                goal.toInt().toString(),
                remaining.toInt().toString()
            )
        }

        val unitText = if (unit == "km") "km" else "걸음"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("🚶 조금만 더 걸어볼까요?")
            .setContentText(currentText + " / " + goalText + " " + unitText + " (" + remainingText + unitText + " 남음)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("현재 $currentText / $goalText $unitText\n" + remainingText + unitText + "만 더 걸으면 목표 달성!\n\n급할 땐 긴급 15분 버튼을 눌러주세요"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    fun cancelEmergencyNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}