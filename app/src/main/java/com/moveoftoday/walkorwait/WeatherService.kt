package com.moveoftoday.walkorwait

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale

/**
 * Open-Meteo API를 사용한 날씨 서비스
 * - 무료, API 키 불필요
 * - 비상업적 사용 10,000 호출/일
 */
object WeatherService {
    private const val TAG = "WeatherService"
    private const val BASE_URL = "https://api.open-meteo.com/v1/forecast"

    // 캐시 (1시간마다 갱신)
    private var cachedWeather: WeatherData? = null
    private var lastFetchTime: Long = 0
    private const val CACHE_DURATION = 60 * 60 * 1000L // 1시간

    // 기본 위치 (서울)
    private const val DEFAULT_LAT = 37.5665
    private const val DEFAULT_LON = 126.9780

    data class WeatherData(
        val temperature: Double,      // 현재 온도 (섭씨)
        val weatherCode: Int,         // WMO 날씨 코드
        val description: String,      // 한국어 설명
        val icon: String,             // 아이콘 이름
        val isDay: Boolean            // 낮/밤
    )

    data class HourlyForecast(
        val hour: Int,                // 시간 (0-23)
        val temperature: Int,         // 온도
        val weatherCode: Int,         // 날씨 코드
        val icon: String              // 아이콘 이름
    )

    // 시간별 예보 캐시
    private var cachedHourlyForecast: List<HourlyForecast>? = null

    // 위치 이름 캐시
    private var cachedLocationName: String? = null

    /**
     * 날씨 데이터 가져오기 (캐시 사용)
     */
    suspend fun getWeather(context: Context): WeatherData? {
        val now = System.currentTimeMillis()

        // 캐시가 유효하면 반환
        if (cachedWeather != null && (now - lastFetchTime) < CACHE_DURATION) {
            return cachedWeather
        }

        return try {
            val location = getLocation(context)
            val lat = location?.latitude ?: DEFAULT_LAT
            val lon = location?.longitude ?: DEFAULT_LON

            val weather = fetchWeather(lat, lon)
            cachedWeather = weather
            lastFetchTime = now
            weather
        } catch (e: Exception) {
            Log.e(TAG, "날씨 가져오기 실패", e)
            cachedWeather // 실패 시 캐시 반환
        }
    }

    /**
     * 시간별 예보 가져오기 (현재 + 3시간 간격 5개)
     */
    suspend fun getHourlyForecast(context: Context): List<HourlyForecast> {
        val now = System.currentTimeMillis()

        // 캐시가 유효하면 반환
        val cached = cachedHourlyForecast
        if (cached != null && (now - lastFetchTime) < CACHE_DURATION) {
            return cached
        }

        return try {
            val location = getLocation(context)
            val lat = location?.latitude ?: DEFAULT_LAT
            val lon = location?.longitude ?: DEFAULT_LON

            val forecast = fetchHourlyForecast(lat, lon)
            cachedHourlyForecast = forecast
            lastFetchTime = now
            forecast
        } catch (e: Exception) {
            Log.e(TAG, "시간별 예보 가져오기 실패", e)
            cachedHourlyForecast ?: emptyList()
        }
    }

    /**
     * Open-Meteo API에서 시간별 예보 가져오기
     */
    private suspend fun fetchHourlyForecast(lat: Double, lon: Double): List<HourlyForecast> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL?latitude=$lat&longitude=$lon&hourly=temperature_2m,weather_code&timezone=Asia/Seoul&forecast_days=2"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val hourly = json.getJSONObject("hourly")

            val times = hourly.getJSONArray("time")
            val temps = hourly.getJSONArray("temperature_2m")
            val codes = hourly.getJSONArray("weather_code")

            // 현재 시간 찾기
            val calendar = java.util.Calendar.getInstance()
            val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.time)

            // 현재 시간 인덱스 찾기
            var startIndex = 0
            for (i in 0 until times.length()) {
                val timeStr = times.getString(i)
                if (timeStr.startsWith(currentDate) && timeStr.contains("T${String.format("%02d", currentHour)}:")) {
                    startIndex = i
                    break
                }
            }

            // 현재 + 3시간 간격으로 7개 가져오기
            val forecasts = mutableListOf<HourlyForecast>()
            val intervals = listOf(0, 3, 6, 9, 12, 15, 18) // 현재, +3h, +6h, +9h, +12h, +15h, +18h

            for (offset in intervals) {
                val index = startIndex + offset
                if (index < times.length()) {
                    val timeStr = times.getString(index)
                    val hour = timeStr.substring(11, 13).toIntOrNull() ?: 0
                    val temp = temps.getDouble(index).toInt()
                    val code = codes.getInt(index)
                    val (_, icon) = getWeatherDescription(code, hour in 6..18)

                    forecasts.add(HourlyForecast(hour, temp, code, icon))
                }
            }

            forecasts
        } catch (e: Exception) {
            Log.e(TAG, "시간별 예보 API 호출 실패", e)
            emptyList()
        }
    }

    /**
     * 위치 이름 가져오기 (영어)
     */
    fun getLocationName(context: Context): String {
        val cachedName = cachedLocationName
        if (cachedName != null) return cachedName

        val location = getLocation(context) ?: return "Seoul"

        return try {
            val geocoder = Geocoder(context, Locale.ENGLISH)
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            val name = addresses?.firstOrNull()?.locality
                ?: addresses?.firstOrNull()?.subAdminArea
                ?: addresses?.firstOrNull()?.adminArea
                ?: "Seoul"
            cachedLocationName = name
            name
        } catch (e: Exception) {
            Log.e(TAG, "위치 이름 가져오기 실패", e)
            "Seoul"
        }
    }

    /**
     * 위치 가져오기
     */
    private fun getLocation(context: Context): Location? {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return try {
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            Log.e(TAG, "위치 가져오기 실패", e)
            null
        }
    }

    /**
     * Open-Meteo API 호출
     */
    private suspend fun fetchWeather(lat: Double, lon: Double): WeatherData? = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code,is_day&timezone=Asia/Seoul"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val current = json.getJSONObject("current")

            val temperature = current.getDouble("temperature_2m")
            val weatherCode = current.getInt("weather_code")
            val isDay = current.getInt("is_day") == 1

            val (description, icon) = getWeatherDescription(weatherCode, isDay)

            WeatherData(
                temperature = temperature,
                weatherCode = weatherCode,
                description = description,
                icon = icon,
                isDay = isDay
            )
        } catch (e: Exception) {
            Log.e(TAG, "API 호출 실패", e)
            null
        }
    }

    /**
     * WMO 날씨 코드를 한국어 설명으로 변환
     * https://open-meteo.com/en/docs 참고
     */
    private fun getWeatherDescription(code: Int, isDay: Boolean): Pair<String, String> {
        return when (code) {
            0 -> "맑음" to if (isDay) "sunny" else "clear_night"
            1 -> "대체로 맑음" to if (isDay) "partly_sunny" else "partly_cloudy_night"
            2 -> "약간 흐림" to "partly_cloudy"
            3 -> "흐림" to "cloudy"
            45, 48 -> "안개" to "foggy"
            51 -> "가벼운 이슬비" to "drizzle"
            53 -> "이슬비" to "drizzle"
            55 -> "짙은 이슬비" to "drizzle"
            56, 57 -> "얼어붙는 이슬비" to "freezing_drizzle"
            61 -> "약한 비" to "rainy"
            63 -> "비" to "rainy"
            65 -> "강한 비" to "heavy_rain"
            66, 67 -> "얼어붙는 비" to "freezing_rain"
            71 -> "약한 눈" to "snowy"
            73 -> "눈" to "snowy"
            75 -> "강한 눈" to "heavy_snow"
            77 -> "싸락눈" to "snowy"
            80 -> "약한 소나기" to "rainy"
            81 -> "소나기" to "rainy"
            82 -> "강한 소나기" to "heavy_rain"
            85 -> "약한 눈소나기" to "snowy"
            86 -> "강한 눈소나기" to "heavy_snow"
            95 -> "천둥번개" to "thunderstorm"
            96, 99 -> "우박 동반 천둥번개" to "thunderstorm"
            else -> "알 수 없음" to "unknown"
        }
    }

    /**
     * 날씨에 따른 펫 대사 생성
     */
    fun getWeatherDialogue(weather: WeatherData?, petName: String): String? {
        if (weather == null) return null

        val temp = weather.temperature.toInt()

        return when {
            // 온도 기반
            temp <= -10 -> listOf(
                "헐 영하 ${temp}도야... 오늘은 집콕하자 🥶",
                "너무 추워... ${petName}도 덜덜 떨려",
                "이 추위에 밖에 나가면 얼어붙어!"
            ).random()

            temp in -9..0 -> listOf(
                "영하 ${kotlin.math.abs(temp)}도! 따뜻하게 입고 나가",
                "추운데 잠깐만 걸어도 괜찮아",
                "손 꽁꽁 숨기고 다녀~"
            ).random()

            temp in 1..10 -> listOf(
                "쌀쌀하네~ 겉옷 챙겨!",
                "바람 불면 춥겠다, 목도리 어때?",
                "${temp}도면 걷기 딱 좋아!"
            ).random()

            temp in 11..20 -> listOf(
                "산책하기 좋은 날씨다!",
                "오늘 날씨 완벽해 🌸",
                "${temp}도! 나가자나가자~"
            ).random()

            temp in 21..27 -> listOf(
                "따뜻하다~ 밖에 나가볼까?",
                "좋은 날씨! 걷기 딱이야",
                "햇살 좋네~ 같이 걷자!"
            ).random()

            temp in 28..32 -> listOf(
                "더워... 물 꼭 챙겨!",
                "그늘로 다니자, 덥다더워 🥵",
                "아침이나 저녁에 걷는 게 나을듯"
            ).random()

            temp > 32 -> listOf(
                "폭염이야! 무리하지 마 🔥",
                "너무 더워서 ${petName}도 녹을 것 같아...",
                "오늘은 실내 운동 어때?"
            ).random()

            else -> null
        } ?: when (weather.weatherCode) {
            // 날씨 상태 기반
            in 51..67, in 80..82 -> listOf(
                "비 온다! 우산 챙겨 ☔",
                "비 오는 날이네~ 실내 운동 할까?",
                "빗소리 들으며 걷는 것도 좋지~"
            ).random()

            in 71..77, in 85..86 -> listOf(
                "눈 온다! ❄️ 미끄러우니 조심해",
                "눈 쌓이면 발자국 남기러 가자!",
                "눈 오는 날 산책도 운치있어~"
            ).random()

            95, 96, 99 -> listOf(
                "천둥번개야! 오늘은 집에 있자 ⚡",
                "무서워... 밖에 나가지 마!",
                "폭풍우 지나가면 나가자"
            ).random()

            45, 48 -> listOf(
                "안개 꼈네~ 조심해서 다녀",
                "앞이 잘 안 보여, 천천히 걷자",
                "안개 낀 날도 분위기 있지?"
            ).random()

            else -> null
        }
    }
}
