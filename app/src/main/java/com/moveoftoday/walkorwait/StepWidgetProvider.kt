package com.moveoftoday.walkorwait

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.NumberFormat
import java.util.Locale

class StepWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = PreferenceManager(context)
            val goalUnit = prefs.getGoalUnit()
            val isKmMode = goalUnit == "km"

            // 비교용 값 (항상 걸음 수 기준)
            val currentProgress = prefs.getCurrentProgress()
            val goal = prefs.getGoal()
            val isAchieved = currentProgress >= goal
            val percent = if (goal > 0) ((currentProgress / goal) * 100).toInt().coerceAtMost(100) else 0

            // 표시용 값 (단위에 맞게 변환)
            val displayValue = prefs.getCurrentProgressForDisplay()
            val displayGoal = prefs.getGoalForDisplay()

            val views = RemoteViews(context.packageName, R.layout.widget_step_counter)

            // 숫자 포맷
            val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
            if (isKmMode) {
                views.setTextViewText(R.id.widget_steps, String.format("%.2f", displayValue))
                views.setTextViewText(R.id.widget_goal, "/ ${String.format("%.2f", displayGoal)}")
            } else {
                views.setTextViewText(R.id.widget_steps, numberFormat.format(displayValue.toInt()))
                views.setTextViewText(R.id.widget_goal, "/ ${numberFormat.format(displayGoal.toInt())}")
            }
            views.setTextViewText(R.id.widget_percent, "${percent}%")

            // 목표 달성 시 골드 배경, 미달성 시 티얼 배경
            if (isAchieved) {
                views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.widget_background_achieved)
                views.setTextViewText(R.id.widget_label, "달성!")
            } else {
                views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.widget_background)
                views.setTextViewText(R.id.widget_label, if (isKmMode) "km" else "걸음")
            }

            // 위젯 전체 클릭 시 앱 열기
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, StepWidgetProvider::class.java)
            )
            for (appWidgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}