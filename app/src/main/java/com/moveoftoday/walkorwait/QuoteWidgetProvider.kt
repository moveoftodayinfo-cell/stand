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
                prefs.setQuoteIndex((prefs.getQuoteIndex() + 1) % getQuotes().size)
                updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
            }
        }
    }

    companion object {
        private const val ACTION_REFRESH = "com.moveoftoday.walkorwait.QUOTE_REFRESH"

        // 언어별 명언 가져오기
        private fun getQuotes(): List<Quote> {
            return when (java.util.Locale.getDefault().language) {
                "ko" -> QUOTES_KO
                "ja" -> QUOTES_JA
                "zh" -> QUOTES_ZH
                "es" -> QUOTES_ES
                else -> QUOTES_EN
            }
        }

        private val QUOTES_KO = listOf(
            Quote("천 리 길도 한 걸음부터", "노자"),
            Quote("오늘 걷지 않으면 내일은 뛰어야 한다", ""),
            Quote("작은 진전도 진전이다", ""),
            Quote("포기하지 않으면 실패가 아니다", ""),
            Quote("매일 조금씩 나아가면 된다", ""),
            Quote("시작이 반이다", "아리스토텔레스"),
            Quote("걷는 것은 최고의 운동이다", "히포크라테스"),
            Quote("몸이 움직이면 마음도 움직인다", ""),
            Quote("한 걸음 한 걸음이 목표로 이끈다", ""),
            Quote("건강은 최고의 재산이다", "버질"),
            Quote("꾸준함이 천재를 이긴다", ""),
            Quote("할 수 있다고 믿으면 이미 반은 이룬 것", "루즈벨트"),
            Quote("작은 습관이 인생을 바꾼다", ""),
            Quote("멈추지 않으면 얼마나 천천히 가도 상관없다", "공자"),
            Quote("건강한 몸에 건강한 정신", "유베날리스")
        )

        private val QUOTES_EN = listOf(
            Quote("A journey of a thousand miles begins with a single step", "Lao Tzu"),
            Quote("If you don't walk today, you'll have to run tomorrow", ""),
            Quote("Small progress is still progress", ""),
            Quote("It's not failure if you don't give up", ""),
            Quote("Take it one day at a time", ""),
            Quote("Well begun is half done", "Aristotle"),
            Quote("Walking is the best exercise", "Hippocrates"),
            Quote("When the body moves, the mind follows", ""),
            Quote("Every step leads to the goal", ""),
            Quote("Health is the greatest wealth", "Virgil"),
            Quote("Consistency beats talent", ""),
            Quote("Believe you can and you're halfway there", "Roosevelt"),
            Quote("Small habits change lives", ""),
            Quote("It doesn't matter how slowly you go, as long as you don't stop", "Confucius"),
            Quote("A healthy mind in a healthy body", "Juvenal")
        )

        private val QUOTES_JA = listOf(
            Quote("千里の道も一歩から", "老子"),
            Quote("今日歩かなければ、明日は走らなければならない", ""),
            Quote("小さな進歩も進歩だ", ""),
            Quote("諦めなければ失敗ではない", ""),
            Quote("毎日少しずつ進めばいい", ""),
            Quote("始まりは半分だ", "アリストテレス"),
            Quote("歩くことは最高の運動だ", "ヒポクラテス"),
            Quote("体が動けば心も動く", ""),
            Quote("一歩一歩が目標に導く", ""),
            Quote("健康は最高の財産", "ウェルギリウス"),
            Quote("継続は天才に勝る", ""),
            Quote("できると信じれば、半分達成したようなものだ", "ルーズベルト"),
            Quote("小さな習慣が人生を変える", ""),
            Quote("止まらなければ、どんなにゆっくりでも構わない", "孔子"),
            Quote("健全な肉体に健全な精神", "ユウェナリス")
        )

        private val QUOTES_ZH = listOf(
            Quote("千里之行，始于足下", "老子"),
            Quote("今天不走，明天就要跑", ""),
            Quote("小小的进步也是进步", ""),
            Quote("不放弃就不是失败", ""),
            Quote("每天进步一点点", ""),
            Quote("好的开始是成功的一半", "亚里士多德"),
            Quote("散步是最好的运动", "希波克拉底"),
            Quote("身体动，心也动", ""),
            Quote("每一步都通向目标", ""),
            Quote("健康是最大的财富", "维吉尔"),
            Quote("坚持胜过天才", ""),
            Quote("相信你能做到，你就已经成功了一半", "罗斯福"),
            Quote("小习惯改变人生", ""),
            Quote("只要不停下来，走得多慢都没关系", "孔子"),
            Quote("健康的身体里有健康的精神", "尤维纳利斯")
        )

        private val QUOTES_ES = listOf(
            Quote("Un viaje de mil millas comienza con un solo paso", "Lao Tzu"),
            Quote("Si no caminas hoy, tendrás que correr mañana", ""),
            Quote("Un pequeño progreso sigue siendo progreso", ""),
            Quote("No es fracaso si no te rindes", ""),
            Quote("Un día a la vez", ""),
            Quote("Bien empezado, medio acabado", "Aristóteles"),
            Quote("Caminar es el mejor ejercicio", "Hipócrates"),
            Quote("Cuando el cuerpo se mueve, la mente sigue", ""),
            Quote("Cada paso lleva a la meta", ""),
            Quote("La salud es la mayor riqueza", "Virgilio"),
            Quote("La constancia vence al talento", ""),
            Quote("Cree que puedes y ya estás a mitad de camino", "Roosevelt"),
            Quote("Los pequeños hábitos cambian vidas", ""),
            Quote("No importa cuán lento vayas, siempre y cuando no te detengas", "Confucio"),
            Quote("Mente sana en cuerpo sano", "Juvenal")
        )

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = PreferenceManager(context)
            val quotes = getQuotes()
            val quoteIndex = prefs.getQuoteIndex() % quotes.size
            val quote = quotes[quoteIndex]

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

            // 저자 (없으면 펫 이름) - V2 우선
            val petName = prefs.getPetNameV2()?.takeIf { it.isNotBlank() }
                ?: prefs.getPetName() ?: "rebon"
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
         * 펫 아이콘 로드 (진행률 기반 애니메이션, grayscale + 스킨)
         */
        private fun loadPetIconWithAnimation(context: Context, prefs: PreferenceManager, animationType: String): Bitmap? {
            // 스킨 가져오기
            val skinId = prefs.getPetSkin()
            val petSkin = com.moveoftoday.walkorwait.pet.DefaultSkins.getById(skinId)

            return try {
                val petTypeV2 = prefs.getPetTypeV2()
                if (petTypeV2 != null) {
                    val stage = prefs.getEffectiveDisplayStage()  // 오버라이드 반영
                    val assetPath = "pets/${petTypeV2.folderName}/${stage.folderName}/$animationType/frame_000.png"
                    context.assets.open(assetPath).use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)?.let { bitmap ->
                            var result = toGrayscale(bitmap)
                            // 스킨 적용
                            if (petSkin?.colorMatrix != null) {
                                result = applyColorMatrix(result, petSkin.colorMatrix)
                            } else if (petSkin?.overlayColor != null) {
                                result = applyOverlayColor(result, petSkin.overlayColor)
                            }
                            result
                        }
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
                                val frame = Bitmap.createBitmap(spriteSheet, 0, 0, frameWidth, spriteSheet.height)
                                var result = toGrayscale(frame)
                                // 스킨 적용
                                if (petSkin?.colorMatrix != null) {
                                    result = applyColorMatrix(result, petSkin.colorMatrix)
                                } else if (petSkin?.overlayColor != null) {
                                    result = applyOverlayColor(result, petSkin.overlayColor)
                                }
                                result
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
                            BitmapFactory.decodeStream(inputStream)?.let { bitmap ->
                                var result = toGrayscale(bitmap)
                                // 스킨 적용
                                if (petSkin?.colorMatrix != null) {
                                    result = applyColorMatrix(result, petSkin.colorMatrix)
                                } else if (petSkin?.overlayColor != null) {
                                    result = applyOverlayColor(result, petSkin.overlayColor)
                                }
                                result
                            }
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

        /**
         * ColorMatrix 적용 (장비 시스템)
         */
        private fun applyColorMatrix(original: Bitmap, matrix: FloatArray): Bitmap {
            val result = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint()
            val colorMatrix = ColorMatrix(matrix)
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(original, 0f, 0f, paint)
            return result
        }

        /**
         * 단색 오버레이 적용
         */
        private fun applyOverlayColor(original: Bitmap, color: Int): Bitmap {
            val result = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint()

            // 원본 그리기
            canvas.drawBitmap(original, 0f, 0f, null)

            // 오버레이 색상 적용 (Modulate 모드)
            paint.colorFilter = android.graphics.PorterDuffColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY)
            canvas.drawBitmap(original, 0f, 0f, paint)

            return result
        }
    }

    data class Quote(val text: String, val author: String)
}
