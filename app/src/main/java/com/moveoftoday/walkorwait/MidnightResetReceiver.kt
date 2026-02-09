package com.moveoftoday.walkorwait

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * 자정(00:00)에 자동으로 걸음수 및 위젯 초기화
 * - Health Connect: 자동으로 자정부터 0부터 시작 (별도 처리 불필요)
 * - 기본 센서: 자정에 걸음수 0으로 리셋
 * - 모든 위젯: 자정에 업데이트 (0보부터 시작)
 */
class MidnightResetReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "MidnightResetReceiver"
        private const val ACTION_MIDNIGHT_RESET = "com.moveoftoday.walkorwait.MIDNIGHT_RESET"

        /**
         * 다음 자정(00:00)에 알람 설정
         */
        fun scheduleMidnightAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, MidnightResetReceiver::class.java).apply {
                action = ACTION_MIDNIGHT_RESET
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 다음 자정 시간 계산
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val nextMidnight = calendar.timeInMillis
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            Log.d(TAG, "⏰ Scheduling midnight alarm at: ${sdf.format(Date(nextMidnight))}")

            // 정확한 시간에 실행 (Android 12+ 대응)
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextMidnight,
                    pendingIntent
                )
                Log.d(TAG, "✅ Midnight alarm scheduled successfully")
            } catch (e: SecurityException) {
                // Android 13+ SCHEDULE_EXACT_ALARM 권한 필요
                Log.w(TAG, "⚠️ Cannot schedule exact alarm, using inexact alarm: ${e.message}")
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextMidnight,
                    pendingIntent
                )
            }
        }

        /**
         * 알람 취소 (필요 시)
         */
        fun cancelMidnightAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, MidnightResetReceiver::class.java).apply {
                action = ACTION_MIDNIGHT_RESET
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "❌ Midnight alarm cancelled")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_MIDNIGHT_RESET) return

        Log.d(TAG, "🌙 Midnight reset triggered!")

        val prefs = PreferenceManager(context)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 날짜 변경 확인 (안전장치)
        val lastReset = prefs.getLastResetDate()
        if (lastReset == today) {
            Log.w(TAG, "⚠️ Already reset today, skipping")
            scheduleMidnightAlarm(context) // 다음 자정 예약
            return
        }

        try {
            // 1. 걸음수 데이터 초기화 (기본 센서만, Health Connect는 자동)
            if (prefs.useHealthConnect()) {
                Log.d(TAG, "📊 Health Connect active - skip manual reset (자동 자정 리셋)")
            } else {
                Log.d(TAG, "📊 Resetting steps for default sensor")
                prefs.saveTodaySteps(0)
                prefs.saveTodayDistance(0.0)
            }

            // 2. 앱 차단 데이터 초기화
            prefs.resetDailyData()

            // 3. 리셋 날짜 갱신
            prefs.saveLastResetDate(today)

            // 4. 모든 위젯 업데이트
            updateAllWidgets(context)

            Log.d(TAG, "✅ Midnight reset completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during midnight reset: ${e.message}", e)
        }

        // 다음 자정 알람 예약
        scheduleMidnightAlarm(context)
    }

    /**
     * 모든 위젯 업데이트
     */
    private fun updateAllWidgets(context: Context) {
        try {
            StepWidgetProvider.updateAllWidgets(context)
            PetWidget2x2Provider.updateAllWidgets(context)
            WeatherWidgetProvider.updateAllWidgets(context)
            SudokuWidgetProvider.updateAllWidgets(context)
            QuoteWidgetProvider.updateAllWidgets(context)
            TravelPhraseWidgetProvider.updateAllWidgets(context)
            TravelPhraseJapaneseWidgetProvider.updateAllWidgets(context)
            TravelPhraseChineseWidgetProvider.updateAllWidgets(context)
            Log.d(TAG, "✅ All widgets updated")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating widgets: ${e.message}", e)
        }
    }
}
