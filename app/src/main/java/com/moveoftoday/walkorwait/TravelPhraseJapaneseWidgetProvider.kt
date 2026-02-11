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
 * 일본어 여행 회화 위젯 (2x1)
 */
class TravelPhraseJapaneseWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_RIGHT_CLICK = "com.moveoftoday.walkorwait.TRAVEL_PHRASE_JP_RIGHT_CLICK"
        private const val PREFS_NAME = "TravelPhraseJapaneseWidget"
        private const val PREF_STATE = "click_state_"
        private const val PREF_PHRASE_INDEX = "phrase_index_"
        private const val PREF_TAP_SHOWN_DATE = "tap_shown_date_"

        // 여행 회화 데이터: (한국어, 일본어, 한글발음)
        data class Phrase(
            val korean: String,
            val japanese: String,
            val pronunciation: String
        )

        val phrases = listOf(
            // 인사
            Phrase("안녕하세요", "こんにちは", "곤니치와"),
            Phrase("감사합니다", "ありがとうございます", "아리가토 고자이마스"),
            Phrase("죄송합니다", "すみません", "스미마셍"),
            Phrase("실례합니다", "失礼します", "시츠레이시마스"),
            Phrase("안녕히 가세요", "さようなら", "사요나라"),
            Phrase("잘 부탁드립니다", "よろしくお願いします", "요로시쿠 오네가이시마스"),

            // 길찾기
            Phrase("역이 어디에요?", "駅はどこですか?", "에키와 도코데스카?"),
            Phrase("화장실이 어디에요?", "トイレはどこですか?", "토이레와 도코데스카?"),
            Phrase("여기가 어디에요?", "ここはどこですか?", "코코와 도코데스카?"),
            Phrase("걸어서 갈 수 있어요?", "歩いて行けますか?", "아루이테 이케마스카?"),
            Phrase("얼마나 걸려요?", "どのくらいかかりますか?", "도노쿠라이 카카리마스카?"),

            // 식당
            Phrase("메뉴판 주세요", "メニューをください", "메뉴오 쿠다사이"),
            Phrase("이거 주세요", "これをください", "코레오 쿠다사이"),
            Phrase("물 주세요", "お水をください", "오미즈오 쿠다사이"),
            Phrase("계산서 주세요", "お会計お願いします", "오카이게이 오네가이시마스"),
            Phrase("맛있어요!", "おいしいです!", "오이시이데스!"),
            Phrase("포장해 주세요", "持ち帰りでお願いします", "모치카에리데 오네가이시마스"),
            Phrase("추천해 주세요", "おすすめは何ですか?", "오스스메와 난데스카?"),
            Phrase("예약했어요", "予約しています", "요야쿠시테이마스"),

            // 쇼핑
            Phrase("얼마예요?", "いくらですか?", "이쿠라데스카?"),
            Phrase("너무 비싸요", "高すぎます", "타카스기마스"),
            Phrase("깎아 주세요", "安くしてください", "야스쿠시테 쿠다사이"),
            Phrase("카드 돼요?", "カードは使えますか?", "카도와 츠카에마스카?"),
            Phrase("영수증 주세요", "レシートをください", "레시토오 쿠다사이"),
            Phrase("이거 입어봐도 돼요?", "試着してもいいですか?", "시챠쿠시테모 이이데스카?"),

            // 호텔
            Phrase("체크인 하고 싶어요", "チェックインお願いします", "체크인 오네가이시마스"),
            Phrase("체크아웃 하고 싶어요", "チェックアウトお願いします", "체크아웃 오네가이시마스"),
            Phrase("와이파이 비밀번호가 뭐에요?", "WiFiのパスワードは?", "와이파이노 파스와도와?"),
            Phrase("방이 너무 추워요", "部屋が寒いです", "헤야가 사무이데스"),

            // 교통
            Phrase("택시 불러 주세요", "タクシーを呼んでください", "타쿠시오 욘데 쿠다사이"),
            Phrase("여기서 내려 주세요", "ここで降ろしてください", "코코데 오로시테 쿠다사이"),
            Phrase("공항까지 가 주세요", "空港までお願いします", "쿠코마데 오네가이시마스"),
            Phrase("표 한 장 주세요", "切符を一枚ください", "킵푸오 이치마이 쿠다사이"),

            // 긴급
            Phrase("도와주세요!", "助けてください!", "타스케테 쿠다사이!"),
            Phrase("의사가 필요해요", "医者が必要です", "이샤가 히츠요데스"),
            Phrase("경찰을 불러주세요", "警察を呼んでください", "게이사츠오 욘데 쿠다사이"),
            Phrase("길을 잃었어요", "道に迷いました", "미치니 마요이마시타"),

            // 소통
            Phrase("일본어 못해요", "日本語が話せません", "니홍고가 하나세마셍"),
            Phrase("천천히 말해 주세요", "ゆっくり話してください", "윳쿠리 하나시테 쿠다사이"),
            Phrase("다시 말해 주세요", "もう一度言ってください", "모이치도 잇테 쿠다사이"),
            Phrase("모르겠어요", "わかりません", "와카리마셍"),
            Phrase("적어 주세요", "書いてください", "카이테 쿠다사이")
        )

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, TravelPhraseJapaneseWidgetProvider::class.java)
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

            val phrase = phrases[phraseIndex % phrases.size]

            val views = RemoteViews(context.packageName, R.layout.widget_travel_phrase)

            // 한국어 문장
            views.setTextViewText(R.id.korean_sentence, phrase.korean)

            // 오늘 날짜 확인
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val tapShownDate = prefs.getString(PREF_TAP_SHOWN_DATE + appWidgetId, null)
            val showTapToday = (tapShownDate != today)

            // 펫 아이콘 (랜덤 프레임)
            val petTypeV2 = prefManager.getPetTypeV2()
            val petBitmap = if (petTypeV2 != null) {
                loadPetRandomFrame(context, petTypeV2, prefManager)
            } else null

            if (petBitmap != null) {
                views.setImageViewBitmap(R.id.pet_icon, petBitmap)
            }

            // 오른쪽 영역 (상태에 따라)
            when (state) {
                0 -> {
                    if (showTapToday) {
                        views.setTextViewText(R.id.lang_text, "日")
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
                    views.setViewVisibility(R.id.lang_text, View.GONE)
                    views.setViewVisibility(R.id.pet_icon, View.GONE)
                    views.setViewVisibility(R.id.rebon_text, View.GONE)
                    views.setViewVisibility(R.id.answer_text, View.VISIBLE)
                    views.setTextViewText(R.id.answer_text, phrase.japanese)
                }
                2 -> {
                    views.setViewVisibility(R.id.lang_text, View.GONE)
                    views.setViewVisibility(R.id.pet_icon, View.GONE)
                    views.setViewVisibility(R.id.rebon_text, View.GONE)
                    views.setViewVisibility(R.id.answer_text, View.VISIBLE)
                    views.setTextViewText(R.id.answer_text, phrase.pronunciation)
                }
            }

            // 오른쪽 클릭
            val rightClickIntent = Intent(context, TravelPhraseJapaneseWidgetProvider::class.java).apply {
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

            // 왼쪽 클릭 (앱 열기)
            val leftClickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val leftPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId + 2000,
                leftClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.left_area, leftPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * 펫 랜덤 프레임 로드 (grayscale + 스킨)
         */
        private fun loadPetRandomFrame(context: Context, petType: PetTypeV2, prefs: PreferenceManager): android.graphics.Bitmap? {
            val stage = prefs.getEffectiveDisplayStage()  // 오버라이드 반영

            // 스킨 가져오기
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
                        // 스킨 적용 (colorMatrix 방식)
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

        /**
         * 비트맵을 grayscale로 변환
         */
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

        /**
         * ColorMatrix 적용 (스킨 컬러)
         */
        private fun applyColorMatrix(original: android.graphics.Bitmap, matrix: FloatArray): android.graphics.Bitmap {
            val result = android.graphics.Bitmap.createBitmap(original.width, original.height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(result)
            val paint = android.graphics.Paint()
            val colorMatrix = android.graphics.ColorMatrix(matrix)
            paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(original, 0f, 0f, paint)
            return result
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

                var state = prefs.getInt(PREF_STATE + appWidgetId, 0)
                var phraseIndex = prefs.getInt(PREF_PHRASE_INDEX + appWidgetId, 0)

                val previousState = state
                state++

                if (previousState == 0 && state == 1) {
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    prefs.edit().putString(PREF_TAP_SHOWN_DATE + appWidgetId, today).apply()
                }

                if (state > 2) {
                    state = 0
                    if (phrases.size > 1) {
                        var newIndex: Int
                        do {
                            newIndex = (Math.random() * phrases.size).toInt()
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
            editor.remove(PREF_TAP_SHOWN_DATE + appWidgetId)
        }
        editor.apply()
    }
}
