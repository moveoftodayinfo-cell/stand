package com.moveoftoday.walkorwait

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.widget.RemoteViews
import com.moveoftoday.walkorwait.pet.PetType
import com.moveoftoday.walkorwait.pet.PetTypeV2
import com.moveoftoday.walkorwait.pet.PetAnimationTypeV2

/**
 * 명언 위젯 - 동기부여 명언 표시
 */
class QuoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                // 새 명언으로 업데이트
                val prefs = PreferenceManager(context)
                prefs.setQuoteIndex((prefs.getQuoteIndex() + 1) % QUOTES.size)
                updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
            }
        }
    }

    companion object {
        private const val ACTION_REFRESH = "com.moveoftoday.walkorwait.QUOTE_REFRESH"

        // 동기부여 명언 모음
        private val QUOTES = listOf(
            Quote("천 리 길도 한 걸음부터", "노자"),
            Quote("오늘 걷지 않으면 내일은 뛰어야 한다", ""),
            Quote("작은 진전도 진전이다", ""),
            Quote("포기하지 않으면 실패가 아니다", ""),
            Quote("매일 조금씩 나아가면 된다", ""),
            Quote("시작이 반이다", "아리스토텔레스"),
            Quote("걷는 것은 최고의 운동이다", "히포크라테스"),
            Quote("몸이 움직이면 마음도 움직인다", ""),
            Quote("오늘의 나는 어제보다 낫다", ""),
            Quote("한 걸음 한 걸음이 목표로 이끈다", ""),
            Quote("건강은 최고의 재산이다", "버질"),
            Quote("꾸준함이 천재를 이긴다", ""),
            Quote("지금 이 순간이 가장 빠르다", ""),
            Quote("할 수 있다고 믿으면 이미 반은 이룬 것", "루즈벨트"),
            Quote("작은 습관이 인생을 바꾼다", ""),
            Quote("움직이는 자만이 앞으로 간다", ""),
            Quote("내 몸은 내가 가꾼다", ""),
            Quote("오늘도 한 발 더", ""),
            Quote("멈추지 않으면 얼마나 천천히 가도 상관없다", "공자"),
            Quote("변화는 오늘부터 시작된다", ""),
            Quote("나 자신을 위한 시간", ""),
            Quote("걸으며 생각하고, 생각하며 걷는다", ""),
            Quote("건강한 몸에 건강한 정신", "유베날리스"),
            Quote("매일 1%씩 성장하자", ""),
            Quote("포기란 없다", ""),
            Quote("끝까지 가보는 거다", ""),
            Quote("오늘 하루도 파이팅", ""),
            Quote("나는 할 수 있다", ""),
            Quote("꿈을 향해 걸어가자", ""),
            Quote("좋은 습관이 좋은 인생을 만든다", "")
        )

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = PreferenceManager(context)
            val quoteIndex = prefs.getQuoteIndex() % QUOTES.size
            val quote = QUOTES[quoteIndex]

            val views = RemoteViews(context.packageName, R.layout.widget_quote)

            // 진행률 계산
            val currentProgress = prefs.getCurrentProgress()
            val goal = prefs.getGoal()
            val percent = if (goal > 0) ((currentProgress / goal) * 100).toInt().coerceAtMost(150) else 0

            // 진행률에 따른 펫 상태 결정
            val (animationType, emotionSymbol) = getStateByProgress(percent)

            // 펫 아이콘 로드 (진행률에 따른 애니메이션)
            val petBitmap = loadPetIconWithAnimation(context, prefs, animationType)
            if (petBitmap != null) {
                views.setImageViewBitmap(R.id.widget_pet_icon, petBitmap)
            } else {
                views.setImageViewResource(R.id.widget_pet_icon, R.mipmap.ic_launcher)
            }

            // 감정 심볼 표시
            views.setTextViewText(R.id.widget_emotion, emotionSymbol)

            // 명언 텍스트
            views.setTextViewText(R.id.widget_quote_text, quote.text)

            // 저자 (없으면 펫 이름)
            val petName = prefs.getPetName() ?: "rebon"
            val authorText = if (quote.author.isNotEmpty()) "- ${quote.author}" else "- $petName"
            views.setTextViewText(R.id.widget_quote_author, authorText)

            // 새로고침 버튼 클릭
            val refreshIntent = Intent(context, QuoteWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)

            // 위젯 전체 클릭 시 앱 열기
            val appIntent = Intent(context, MainActivity::class.java)
            val appPendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, appPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, QuoteWidgetProvider::class.java)
            )
            for (appWidgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        /**
         * 진행률에 따른 펫 상태 결정 (ASCII만 사용)
         */
        private fun getStateByProgress(percent: Int): Pair<String, String> {
            return when (percent) {
                0 -> "idle" to "zzZ"
                in 1..24 -> "walk" to "~"
                in 25..49 -> "walk" to "!"
                in 50..74 -> "run" to "!!"
                in 75..89 -> "run" to "^o^"
                in 90..99 -> "run" to "!!!"
                else -> "bark" to "***"  // 100%+
            }
        }

        /**
         * 펫 아이콘 로드 (진행률 기반 애니메이션, grayscale)
         */
        private fun loadPetIconWithAnimation(context: Context, prefs: PreferenceManager, animationType: String): Bitmap? {
            return try {
                val petTypeV2 = prefs.getPetTypeV2()
                if (petTypeV2 != null) {
                    val stage = prefs.getEffectiveDisplayStage()  // 오버라이드 반영
                    val assetPath = "pets/${petTypeV2.folderName}/${stage.folderName}/$animationType/frame_000.png"
                    context.assets.open(assetPath).use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)?.let { toGrayscale(it) }
                    }
                } else {
                    // V1 펫은 idle만 지원
                    val petTypeName = prefs.getPetType()
                    val petType = petTypeName?.let {
                        try { PetType.valueOf(it) } catch (e: Exception) { null }
                    }
                    if (petType != null) {
                        context.assets.open(petType.idleAssetPath).use { inputStream ->
                            val spriteSheet = BitmapFactory.decodeStream(inputStream)
                            if (spriteSheet != null) {
                                val frameWidth = spriteSheet.width / petType.idleFrames
                                toGrayscale(Bitmap.createBitmap(spriteSheet, 0, 0, frameWidth, spriteSheet.height))
                            } else null
                        }
                    } else null
                }
            } catch (e: Exception) {
                // 애니메이션 폴더가 없으면 idle로 폴백
                try {
                    val petTypeV2 = prefs.getPetTypeV2()
                    if (petTypeV2 != null) {
                        val stage = prefs.getEffectiveDisplayStage()  // 오버라이드 반영
                        val fallbackPath = "pets/${petTypeV2.folderName}/${stage.folderName}/idle/frame_000.png"
                        context.assets.open(fallbackPath).use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)?.let { toGrayscale(it) }
                        }
                    } else null
                } catch (e2: Exception) {
                    null
                }
            }
        }

        /**
         * 펫 아이콘 로드 (grayscale) - 레거시
         */
        private fun loadPetIcon(context: Context, prefs: PreferenceManager): Bitmap? {
            return loadPetIconWithAnimation(context, prefs, "idle")?.let { toGrayscale(it) }
        }

        /**
         * Grayscale 변환
         */
        private fun toGrayscale(original: Bitmap): Bitmap {
            val grayscale = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(grayscale)
            val paint = Paint()
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(0f)
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(original, 0f, 0f, paint)
            return grayscale
        }
    }

    data class Quote(val text: String, val author: String)
}
