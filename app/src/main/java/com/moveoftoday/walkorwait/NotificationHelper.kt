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
        const val GOAL_CHANNEL_ID = "stand_goal_channel"
        const val NOTIFICATION_ID = 1001
        const val GOAL_NOTIFICATION_ID = 1002
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 휴식 모드 채널
            val emergencyChannel = NotificationChannel(
                CHANNEL_ID,
                "휴식 모드 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "15분 휴식 타이머"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(emergencyChannel)

            // 목표 달성 채널
            val goalChannel = NotificationChannel(
                GOAL_CHANNEL_ID,
                "목표 달성 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "일일 목표 달성 시 알림"
                setShowBadge(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 100, 300)
            }
            notificationManager.createNotificationChannel(goalChannel)
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
            .setContentTitle("휴식 모드 활성")
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
            .setContentTitle("조금만 더 걸어볼까요?")
            .setContentText(currentText + " / " + goalText + " " + unitText + " (" + remainingText + unitText + " 남음)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("현재 $currentText / $goalText $unitText\n" + remainingText + unitText + "만 더 걸으면 목표 달성!\n\n급할 땐 15분 휴식 버튼을 눌러주세요"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    fun cancelEmergencyNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /**
     * 목표 달성 축하 알림
     * @param goal 목표 값 (걸음수 또는 km)
     * @param unit 단위 ("steps" 또는 "km")
     */
    fun showGoalAchievedNotification(goal: Double, unit: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val goalText = if (unit == "km") {
            String.format("%.2fkm", goal)
        } else {
            String.format("%,d보", goal.toInt())
        }

        val notification = NotificationCompat.Builder(context, GOAL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("목표 달성!")
            .setContentText("오늘 $goalText 완료")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE or NotificationCompat.DEFAULT_SOUND)
            .build()

        notificationManager.notify(GOAL_NOTIFICATION_ID, notification)
    }

    /**
     * 걱정 알림 - 평소 운동 시간에 움직임이 없을 때
     * 펫이 걱정하는 말투로 알림
     * @param petName 펫 이름
     */
    fun showWorryNotification(petName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 걱정하는 말투의 메시지들
        val worryMessages = listOf(
            "오늘 괜찮아? 평소 이 시간엔 같이 산책했는데...",
            "무슨 일 있어? 걱정돼서 기다리고 있었어",
            "오늘 많이 바쁜가봐... 잠깐이라도 나와줄 수 있어?",
            "혹시 아픈 건 아니지? 오늘 아직 못 봤네...",
            "보고싶다... 잠깐만 얼굴이라도 보여줘!"
        )

        val message = worryMessages.random()

        val notification = NotificationCompat.Builder(context, GOAL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("$petName 가 걱정하고 있어요")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(GOAL_NOTIFICATION_ID + 10, notification)
        } catch (e: SecurityException) {
            android.util.Log.e("NotificationHelper", "Failed to show worry notification: ${e.message}")
        }
    }
}
