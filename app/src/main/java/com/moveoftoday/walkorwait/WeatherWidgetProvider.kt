package com.moveoftoday.walkorwait

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.Log
import android.widget.RemoteViews
import com.moveoftoday.walkorwait.pet.PetTypeV2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 날씨 위젯 (4x2) - Pixel Pals 스타일
 * - 왼쪽에 펫
 * - 오른쪽에 시간대별 날씨 예보
 */
class WeatherWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = PreferenceManager(context)
        val views = RemoteViews(context.packageName, R.layout.widget_weather)

        // 펫 정보
        val petTypeV2 = prefs.getPetTypeV2()

        // 펫 아이콘
        val petBitmap = if (petTypeV2 != null) {
            loadPetFirstFrame(context, petTypeV2, prefs)
        } else null

        if (petBitmap != null) {
            views.setImageViewBitmap(R.id.widget_pet_icon, petBitmap)
        }

        // 위젯 클릭 시 앱 열기
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        // 기본 로딩 표시
        views.setTextViewText(R.id.weather_time_0, "Now")
        views.setTextViewText(R.id.weather_temp_0, "--°")

        appWidgetManager.updateAppWidget(appWidgetId, views)

        // 백그라운드에서 날씨 업데이트
        scope.launch {
            fetchAndUpdateWeather(context, appWidgetManager, appWidgetId)
        }
    }

    private suspend fun fetchAndUpdateWeather(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = PreferenceManager(context)
        val forecasts = WeatherService.getHourlyForecast(context)

        Log.d("WeatherWidget", "Forecasts count: ${forecasts.size}")
        if (forecasts.isEmpty()) {
            Log.w("WeatherWidget", "No forecasts available")
            return
        }

        val views = RemoteViews(context.packageName, R.layout.widget_weather)

        // 펫 정보 다시 설정
        val petTypeV2 = prefs.getPetTypeV2()
        val petBitmap = if (petTypeV2 != null) {
            loadPetFirstFrame(context, petTypeV2, prefs)
        } else null

        if (petBitmap != null) {
            views.setImageViewBitmap(R.id.widget_pet_icon, petBitmap)
        }

        // 위치 이름 표시
        val locationName = WeatherService.getLocationName(context)
        views.setTextViewText(R.id.widget_location, locationName)

        // 시간별 예보 표시 (7개 슬롯)
        val iconIds = listOf(0, R.id.weather_icon_1, R.id.weather_icon_2, R.id.weather_icon_3, R.id.weather_icon_4, R.id.weather_icon_5, R.id.weather_icon_6) // 0번은 펫이 대신
        val tempIds = listOf(R.id.weather_temp_0, R.id.weather_temp_1, R.id.weather_temp_2, R.id.weather_temp_3, R.id.weather_temp_4, R.id.weather_temp_5, R.id.weather_temp_6)
        val timeIds = listOf(R.id.weather_time_0, R.id.weather_time_1, R.id.weather_time_2, R.id.weather_time_3, R.id.weather_time_4, R.id.weather_time_5, R.id.weather_time_6)
        val groupIds = listOf(R.id.weather_group_0, R.id.weather_group_1, R.id.weather_group_2, R.id.weather_group_3, R.id.weather_group_4, R.id.weather_group_5, R.id.weather_group_6)

        // 온도 범위 계산 (7단계 높이용)
        val temps = forecasts.take(7).map { it.temperature }
        val minTemp = temps.minOrNull() ?: 0
        val maxTemp = temps.maxOrNull() ?: 30
        val tempRange = (maxTemp - minTemp).coerceAtLeast(1)
        val density = context.resources.displayMetrics.density
        val maxBottomPadding = (10 * density).toInt() // 최대 10dp 높이 차이

        for (i in forecasts.indices.take(7)) {
            val forecast = forecasts[i]
            views.setTextViewText(tempIds[i], "${forecast.temperature}°")

            // 시간 포맷 (AM/PM)
            val timeLabel = if (i == 0) {
                "Now"
            } else {
                val hour = forecast.hour
                when {
                    hour == 0 -> "12AM"
                    hour < 12 -> "${hour}AM"
                    hour == 12 -> "12PM"
                    else -> "${hour - 12}PM"
                }
            }
            views.setTextViewText(timeIds[i], timeLabel)

            // 슬롯 1~6만 날씨 아이콘 적용 (슬롯 0은 펫이 대신)
            if (i > 0) {
                val symbol = getWeatherSymbol(forecast.icon)
                Log.d("WeatherWidget", "Slot $i: icon=${forecast.icon}, symbol=$symbol")
                views.setTextViewText(iconIds[i], symbol)
            }

            // 온도에 따른 높이 조절 (높은 온도 = 위로, 낮은 온도 = 아래로)
            // bottom padding 방식: 그룹 내 gravity=bottom이므로 bottomPadding으로 위로 밀어올림
            // 높은 온도 = 많은 bottomPadding = 위로 올라감
            val normalizedTemp = (forecast.temperature - minTemp).toFloat() / tempRange
            val bottomPadding = (normalizedTemp * maxBottomPadding).toInt()
            views.setViewPadding(groupIds[i], 0, 0, 0, bottomPadding)
            Log.d("WeatherWidget", "Slot $i: temp=${forecast.temperature}, bottomPadding=$bottomPadding")
        }

        // 위젯 클릭 시 앱 열기
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

    /**
     * 펫 첫 프레임 로드 (grayscale)
     */
    private fun loadPetFirstFrame(context: Context, petType: PetTypeV2, prefs: PreferenceManager): Bitmap? {
        val stage = prefs.getEffectiveDisplayStage()  // 오버라이드 반영

        return try {
            val assetPath = "pets/${petType.folderName}/${stage.folderName}/idle/frame_000.png"
            context.assets.open(assetPath).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)?.let { toGrayscale(it) }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 비트맵을 grayscale로 변환
     */
    private fun toGrayscale(original: Bitmap): Bitmap {
        val grayscale = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayscale)
        val paint = android.graphics.Paint()
        val colorMatrix = android.graphics.ColorMatrix()
        colorMatrix.setSaturation(0f)
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(original, 0f, 0f, paint)
        return grayscale
    }

    /**
     * 진행률에 따른 감정 심볼
     */
    private fun getEmotionByProgress(percent: Int): String {
        return when (percent) {
            0 -> "zzZ"
            in 1..24 -> "~"
            in 25..49 -> "!"
            in 50..74 -> "!!"
            in 75..89 -> "^o^"
            in 90..99 -> "!!!"
            else -> "***"
        }
    }

    /**
     * 날씨 아이콘 - 모노크롬 텍스트 심볼
     * U+FE0E (텍스트 변형 선택자)를 추가하여 이모지 대신 텍스트로 렌더링
     */
    private fun getWeatherSymbol(icon: String?): String {
        val textSelector = "\uFE0E" // 텍스트 스타일 강제
        return when (icon) {
            "sunny" -> "☀$textSelector"
            "clear_night" -> "☾$textSelector"
            "partly_sunny", "partly_cloudy_night" -> "⛅$textSelector"
            "partly_cloudy" -> "⛅$textSelector"
            "cloudy" -> "☁$textSelector"
            "foggy" -> "≡"
            "drizzle" -> "☂$textSelector"
            "rainy" -> "☂$textSelector"
            "heavy_rain" -> "☔$textSelector"
            "freezing_drizzle", "freezing_rain" -> "☔$textSelector"
            "snowy" -> "❄$textSelector"
            "heavy_snow" -> "❄$textSelector"
            "thunderstorm" -> "⚡$textSelector"
            else -> "○"
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, WeatherWidgetProvider::class.java)
            )

            val provider = WeatherWidgetProvider()
            for (appWidgetId in appWidgetIds) {
                provider.updateWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
