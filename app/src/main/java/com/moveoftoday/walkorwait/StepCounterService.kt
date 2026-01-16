package com.moveoftoday.walkorwait

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class StepCounterService : Service() {

    private lateinit var stepSensorManager: StepSensorManager
    private lateinit var preferenceManager: PreferenceManager

    companion object {
        const val CHANNEL_ID = "step_counter_channel"
        const val NOTIFICATION_ID = 2001

        fun start(context: Context) {
            val intent = Intent(context, StepCounterService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, StepCounterService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(this)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification(0))

        // 센서 초기화 및 시작을 try-catch로 감싸기
        try {
            stepSensorManager = StepSensorManager(this)
            stepSensorManager.onStepCountChanged = { steps ->
                preferenceManager.saveTodaySteps(steps)
                // 걸음 기록 저장 (평균 속도 계산용)
                preferenceManager.saveStepRecord(System.currentTimeMillis(), steps)
                StepWidgetProvider.updateAllWidgets(this)
                updateNotification(steps)
            }
            stepSensorManager.onDistanceChanged = { distanceKm ->
                preferenceManager.saveTodayDistance(distanceKm)
                // 거리 기록 저장 (평균 속도 계산용)
                preferenceManager.saveDistanceRecord(System.currentTimeMillis(), distanceKm)
            }
            stepSensorManager.startListening()
        } catch (e: SecurityException) {
            android.util.Log.e("StepCounterService", "Sensor permission error: ${e.message}")
            // 센서 없어도 서비스는 계속 실행
        } catch (e: Exception) {
            android.util.Log.e("StepCounterService", "Sensor error: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "걸음 측정",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(steps: Int): Notification {
        val intent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Stand - 측정 중")
            .setContentText("오늘: $steps / ${preferenceManager.getGoal()} 걸음")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setContentIntent(intent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(steps: Int) {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NOTIFICATION_ID, createNotification(steps))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        try {
            stepSensorManager.stopListening()
        } catch (e: Exception) {
            android.util.Log.e("StepCounterService", "Stop listening error: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}