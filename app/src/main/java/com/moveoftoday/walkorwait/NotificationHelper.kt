package com.moveoftoday.walkorwait

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Locale

class NotificationHelper(private val context: Context) {

    private fun getLang(): String = Locale.getDefault().language

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
                getRestModeChannelName(),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getRestModeChannelDesc()
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(emergencyChannel)

            // 목표 달성 채널
            val goalChannel = NotificationChannel(
                GOAL_CHANNEL_ID,
                getGoalChannelName(),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getGoalChannelDesc()
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
            .setContentTitle(getRestModeActiveTitle())
            .setContentText(getRemainingTimeText(minutes, seconds))
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

        val unitText = if (unit == "km") "km" else getStepsUnit()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle(getWalkMoreTitle())
            .setContentText("$currentText / $goalText $unitText (${getRemainingLabel(remainingText, unitText)})")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(getBlockedBigText(currentText, goalText, remainingText, unitText)))
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
            String.format("%,d", goal.toInt()) + getStepsUnit()
        }

        val notification = NotificationCompat.Builder(context, GOAL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getGoalAchievedTitle())
            .setContentText(getTodayCompletedText(goalText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE or NotificationCompat.DEFAULT_SOUND)
            .build()

        notificationManager.notify(GOAL_NOTIFICATION_ID, notification)
    }

    /**
     * 튜토리얼 목표 달성 알림
     * @param targetSteps 목표 걸음수
     */
    fun showTutorialGoalAchievedNotification(targetSteps: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, GOAL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getGoalAchievedTitle())
            .setContentText(getTutorialGoalText(targetSteps))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE or NotificationCompat.DEFAULT_SOUND)
            .build()

        notificationManager.notify(GOAL_NOTIFICATION_ID + 1, notification)
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

        val worryMessages = getWorryMessages()
        val message = worryMessages.random()

        val notification = NotificationCompat.Builder(context, GOAL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(getPetWorryTitle(petName))
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

    // Localization helpers
    private fun getRestModeChannelName(): String = when (getLang()) {
        "ko" -> "휴식 모드 알림"
        "ja" -> "休憩モード通知"
        "zh" -> "休息模式通知"
        "es" -> "Notificación modo descanso"
        else -> "Rest Mode Notification"
    }

    private fun getRestModeChannelDesc(): String = when (getLang()) {
        "ko" -> "15분 휴식 타이머"
        "ja" -> "15分休憩タイマー"
        "zh" -> "15分钟休息计时器"
        "es" -> "Temporizador de descanso de 15 min"
        else -> "15-minute rest timer"
    }

    private fun getGoalChannelName(): String = when (getLang()) {
        "ko" -> "목표 달성 알림"
        "ja" -> "目標達成通知"
        "zh" -> "目标达成通知"
        "es" -> "Notificación de meta"
        else -> "Goal Achievement Notification"
    }

    private fun getGoalChannelDesc(): String = when (getLang()) {
        "ko" -> "일일 목표 달성 시 알림"
        "ja" -> "毎日の目標達成時に通知"
        "zh" -> "每日目标达成时通知"
        "es" -> "Notificación al alcanzar meta diaria"
        else -> "Notification when daily goal is achieved"
    }

    private fun getRestModeActiveTitle(): String = when (getLang()) {
        "ko" -> "휴식 모드 활성"
        "ja" -> "休憩モード有効"
        "zh" -> "休息模式激活"
        "es" -> "Modo descanso activo"
        else -> "Rest Mode Active"
    }

    private fun getRemainingTimeText(minutes: Long, seconds: Long): String = when (getLang()) {
        "ko" -> "남은 시간: ${minutes}분 ${seconds}초"
        "ja" -> "残り時間: ${minutes}分${seconds}秒"
        "zh" -> "剩余时间: ${minutes}分${seconds}秒"
        "es" -> "Tiempo restante: ${minutes}m ${seconds}s"
        else -> "Remaining: ${minutes}m ${seconds}s"
    }

    private fun getStepsUnit(): String = when (getLang()) {
        "ko" -> "보"
        "ja" -> "歩"
        "zh" -> "步"
        else -> " steps"
    }

    private fun getWalkMoreTitle(): String = when (getLang()) {
        "ko" -> "조금만 더 걸어볼까요?"
        "ja" -> "もう少し歩いてみませんか？"
        "zh" -> "再走一点吧？"
        "es" -> "¿Caminas un poco más?"
        else -> "Walk a little more?"
    }

    private fun getRemainingLabel(remaining: String, unit: String): String = when (getLang()) {
        "ko" -> "$remaining$unit 남음"
        "ja" -> "残り$remaining$unit"
        "zh" -> "剩余$remaining$unit"
        "es" -> "$remaining$unit restantes"
        else -> "$remaining$unit left"
    }

    private fun getBlockedBigText(current: String, goal: String, remaining: String, unit: String): String = when (getLang()) {
        "ko" -> "현재 $current / $goal $unit\n${remaining}${unit}만 더 걸으면 목표 달성!\n\n급할 땐 15분 휴식 버튼을 눌러주세요"
        "ja" -> "現在 $current / $goal $unit\nあと${remaining}${unit}で目標達成！\n\n急いでいる時は15分休憩ボタンを押してください"
        "zh" -> "当前 $current / $goal $unit\n再走${remaining}${unit}即可达成目标！\n\n紧急时请按15分钟休息按钮"
        "es" -> "Actual $current / $goal $unit\n¡Solo ${remaining}${unit} más para la meta!\n\nSi tienes prisa, usa el botón de descanso de 15 min"
        else -> "Current $current / $goal $unit\nJust ${remaining}${unit} more to reach your goal!\n\nIf in a hurry, use the 15-min rest button"
    }

    private fun getGoalAchievedTitle(): String = when (getLang()) {
        "ko" -> "목표 달성!"
        "ja" -> "目標達成！"
        "zh" -> "目标达成！"
        "es" -> "¡Meta alcanzada!"
        else -> "Goal Achieved!"
    }

    private fun getTodayCompletedText(goalText: String): String = when (getLang()) {
        "ko" -> "오늘 $goalText 완료"
        "ja" -> "今日 $goalText 完了"
        "zh" -> "今天 $goalText 完成"
        "es" -> "Hoy $goalText completado"
        else -> "Today $goalText completed"
    }

    private fun getTutorialGoalText(steps: Int): String = when (getLang()) {
        "ko" -> "${steps}보 완료! 앱으로 돌아와서 다음 단계를 진행하세요."
        "ja" -> "${steps}歩完了！アプリに戻って次のステップに進んでください。"
        "zh" -> "${steps}步完成！返回应用进行下一步。"
        "es" -> "¡${steps} pasos completados! Vuelve a la app para continuar."
        else -> "$steps steps done! Return to the app to continue."
    }

    private fun getPetWorryTitle(petName: String): String = when (getLang()) {
        "ko" -> "$petName 가 걱정하고 있어요"
        "ja" -> "$petName が心配しています"
        "zh" -> "$petName 在担心你"
        "es" -> "$petName está preocupado"
        else -> "$petName is worried about you"
    }

    private fun getWorryMessages(): List<String> = when (getLang()) {
        "ko" -> listOf(
            "오늘 괜찮아? 평소 이 시간엔 같이 산책했는데...",
            "무슨 일 있어? 걱정돼서 기다리고 있었어",
            "오늘 많이 바쁜가봐... 잠깐이라도 나와줄 수 있어?",
            "혹시 아픈 건 아니지? 오늘 아직 못 봤네...",
            "보고싶다... 잠깐만 얼굴이라도 보여줘!"
        )
        "ja" -> listOf(
            "今日は大丈夫？いつもこの時間に散歩してたのに...",
            "何かあった？心配で待ってたんだ",
            "今日は忙しいのかな...ちょっとでも来てくれる？",
            "もしかして体調悪い？今日まだ会ってないね...",
            "会いたいな...ちょっとだけでも顔見せて！"
        )
        "zh" -> listOf(
            "今天还好吗？平时这个时候我们一起散步的...",
            "发生什么事了吗？我一直在担心地等着",
            "今天好像很忙...能出来一下吗？",
            "是不是不舒服？今天还没见到你...",
            "想你了...给我看看你的脸吧！"
        )
        "es" -> listOf(
            "¿Estás bien hoy? Normalmente paseamos a esta hora...",
            "¿Pasó algo? Estaba esperando preocupado",
            "Pareces muy ocupado hoy... ¿Puedes venir un momento?",
            "¿No estarás enfermo? Hoy no te he visto...",
            "Te extraño... ¡Muéstrame tu cara aunque sea un momento!"
        )
        else -> listOf(
            "Are you okay today? We usually walk at this time...",
            "Did something happen? I was waiting worried",
            "You seem busy today... Can you come out for a bit?",
            "You're not sick, are you? Haven't seen you today...",
            "I miss you... Just show me your face for a moment!"
        )
    }
}
