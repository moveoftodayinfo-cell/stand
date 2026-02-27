package com.moveoftoday.walkorwait

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.View
import android.widget.RemoteViews
import com.moveoftoday.walkorwait.pet.PetTypeV2

/**
 * 힌디어 여행 회화 위젯 (2x1)
 * - 왼쪽: 시스템 언어로 된 문장
 * - 오른쪽: 펫 + 힌디어/로마자 발음 (클릭으로 전환)
 */
class TravelPhraseHindiWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_RIGHT_CLICK = "com.moveoftoday.walkorwait.TRAVEL_PHRASE_HI_RIGHT_CLICK"
        private const val PREFS_NAME = "TravelPhraseHindiWidget"
        private const val PREF_STATE = "click_state_"
        private const val PREF_PHRASE_INDEX = "phrase_index_"
        private const val PREF_CATEGORY = "category_"
        private const val PREF_TAP_SHOWN_DATE = "tap_shown_date_"

        // 학습 대상 언어
        private const val TARGET_LANG = "hi"
        private const val LANG_CODE = "HI"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, TravelPhraseHindiWidgetProvider::class.java)
            )

            for (appWidgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val prefManager = PreferenceManager(context)

            val state = prefs.getInt(PREF_STATE + appWidgetId, 0)
            val phraseIndex = prefs.getInt(PREF_PHRASE_INDEX + appWidgetId, 0)
            val selectedCategory = prefs.getString(PREF_CATEGORY + appWidgetId, null)

            // TravelPhraseData에서 문장 가져오기
            val allPhrases = TravelPhraseData.phrases
            val filteredPhrases = if (selectedCategory != null) {
                allPhrases.filter { it.category == selectedCategory }
            } else {
                allPhrases
            }

            val phrase = if (filteredPhrases.isNotEmpty()) {
                filteredPhrases[phraseIndex % filteredPhrases.size]
            } else {
                allPhrases[0]
            }

            val views = RemoteViews(context.packageName, R.layout.widget_travel_phrase)

            // 왼쪽: 시스템 언어로 된 문장
            views.setTextViewText(R.id.korean_sentence, TravelPhraseData.getTranslation(phrase))

            // 오늘 날짜 확인
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val tapShownDate = prefs.getString(PREF_TAP_SHOWN_DATE + appWidgetId, null)
            val showTapToday = (tapShownDate != today)

            // 펫 아이콘
            val petTypeV2 = prefManager.getPetTypeV2()
            val petBitmap = if (petTypeV2 != null) {
                loadPetRandomFrame(context, petTypeV2, prefManager)
            } else null

            if (petBitmap != null) {
                views.setImageViewBitmap(R.id.pet_icon, petBitmap)
            }

            // 오른쪽 영역
            when (state) {
                0 -> {
                    if (showTapToday) {
                        views.setTextViewText(R.id.lang_text, LANG_CODE)
                        views.setViewVisibility(R.id.lang_text, View.VISIBLE)
                        views.setViewVisibility(R.id.pet_icon, View.VISIBLE)
                        views.setViewVisibility(R.id.rebon_text, View.VISIBLE)
                        views.setViewVisibility(R.id.answer_text, View.GONE)
                    } else {
                        views.setViewVisibility(R.id.lang_text, View.GONE)
                        views.setViewVisibility(R.id.pet_icon, View.VISIBLE)
                        views.setViewVisibility(R.id.rebon_text, View.GONE)
                        views.setViewVisibility(R.id.answer_text, View.GONE)
                    }
                }
                1 -> {
                    // 힌디어 표시
                    views.setViewVisibility(R.id.lang_text, View.GONE)
                    views.setViewVisibility(R.id.pet_icon, View.GONE)
                    views.setViewVisibility(R.id.rebon_text, View.GONE)
                    views.setViewVisibility(R.id.answer_text, View.VISIBLE)
                    views.setTextViewText(R.id.answer_text, TravelPhraseData.getTranslation(phrase, TARGET_LANG))
                }
                2 -> {
                    // 로마자 발음 표시
                    views.setViewVisibility(R.id.lang_text, View.GONE)
                    views.setViewVisibility(R.id.pet_icon, View.GONE)
                    views.setViewVisibility(R.id.rebon_text, View.GONE)
                    views.setViewVisibility(R.id.answer_text, View.VISIBLE)
                    views.setTextViewText(R.id.answer_text, TravelPhraseData.getRomanization(phrase, TARGET_LANG))
                }
            }

            // 오른쪽 클릭
            val rightClickIntent = Intent(context, TravelPhraseHindiWidgetProvider::class.java).apply {
                action = ACTION_RIGHT_CLICK
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val rightPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                rightClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setOnClickPendingIntent(R.id.right_area, rightPendingIntent)

            // 왼쪽 클릭
            val leftClickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val leftPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId + 7000,
                leftClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.left_area, leftPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun loadPetRandomFrame(context: Context, petType: PetTypeV2, prefs: PreferenceManager): android.graphics.Bitmap? {
            val stage = prefs.getEffectiveDisplayStage()
            val skinId = prefs.getPetSkin()
            val petSkin = com.moveoftoday.walkorwait.pet.DefaultSkins.getById(skinId)

            return try {
                val idlePath = "pets/${petType.folderName}/${stage.folderName}/idle"
                val frames = context.assets.list(idlePath)?.filter { it.endsWith(".png") } ?: listOf("frame_000.png")
                val randomFrame = frames.random()
                val assetPath = "$idlePath/$randomFrame"

                context.assets.open(assetPath).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)?.let { bitmap ->
                        var result = toGrayscale(bitmap)
                        petSkin?.colorMatrix?.let { matrix ->
                            result = applyColorMatrix(result, matrix)
                        }
                        result
                    }
                }
            } catch (e: Exception) {
                null
            }
        }

        private fun toGrayscale(original: android.graphics.Bitmap): android.graphics.Bitmap {
            val grayscale = android.graphics.Bitmap.createBitmap(original.width, original.height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(grayscale)
            val paint = android.graphics.Paint()
            val colorMatrix = android.graphics.ColorMatrix()
            colorMatrix.setSaturation(0f)
            paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(original, 0f, 0f, paint)
            return grayscale
        }

        private fun applyColorMatrix(original: android.graphics.Bitmap, matrix: FloatArray): android.graphics.Bitmap {
            val result = android.graphics.Bitmap.createBitmap(original.width, original.height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(result)
            val paint = android.graphics.Paint()
            val colorMatrix = android.graphics.ColorMatrix(matrix)
            paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(original, 0f, 0f, paint)
            return result
        }

        fun setCategory(context: Context, appWidgetId: Int, category: String?) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(PREF_CATEGORY + appWidgetId, category)
                .putInt(PREF_PHRASE_INDEX + appWidgetId, 0)
                .putInt(PREF_STATE + appWidgetId, 0)
                .apply()

            val appWidgetManager = AppWidgetManager.getInstance(context)
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

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

        if (intent.action == ACTION_RIGHT_CLICK) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val selectedCategory = prefs.getString(PREF_CATEGORY + appWidgetId, null)

                var state = prefs.getInt(PREF_STATE + appWidgetId, 0)
                var phraseIndex = prefs.getInt(PREF_PHRASE_INDEX + appWidgetId, 0)

                val allPhrases = TravelPhraseData.phrases
                val filteredPhrases = if (selectedCategory != null) {
                    allPhrases.filter { it.category == selectedCategory }
                } else {
                    allPhrases
                }

                val previousState = state
                state++

                if (previousState == 0 && state == 1) {
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    prefs.edit().putString(PREF_TAP_SHOWN_DATE + appWidgetId, today).apply()
                }

                if (state > 2) {
                    state = 0
                    if (filteredPhrases.size > 1) {
                        var newIndex: Int
                        do {
                            newIndex = (Math.random() * filteredPhrases.size).toInt()
                        } while (newIndex == phraseIndex)
                        phraseIndex = newIndex
                    }
                }

                prefs.edit()
                    .putInt(PREF_STATE + appWidgetId, state)
                    .putInt(PREF_PHRASE_INDEX + appWidgetId, phraseIndex)
                    .apply()

                val appWidgetManager = AppWidgetManager.getInstance(context)
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        for (appWidgetId in appWidgetIds) {
            editor.remove(PREF_STATE + appWidgetId)
            editor.remove(PREF_PHRASE_INDEX + appWidgetId)
            editor.remove(PREF_CATEGORY + appWidgetId)
            editor.remove(PREF_TAP_SHOWN_DATE + appWidgetId)
        }
        editor.apply()
    }
}
