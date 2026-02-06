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
 * 중국어 여행 회화 위젯 (2x1)
 */
class TravelPhraseChineseWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_RIGHT_CLICK = "com.moveoftoday.walkorwait.TRAVEL_PHRASE_CN_RIGHT_CLICK"
        private const val PREFS_NAME = "TravelPhraseChineseWidget"
        private const val PREF_STATE = "click_state_"
        private const val PREF_PHRASE_INDEX = "phrase_index_"
        private const val PREF_TAP_SHOWN_DATE = "tap_shown_date_"

        // 여행 회화 데이터: (한국어, 중국어, 한글발음)
        data class Phrase(
            val korean: String,
            val chinese: String,
            val pronunciation: String
        )

        val phrases = listOf(
            // 인사
            Phrase("안녕하세요", "你好", "니하오"),
            Phrase("감사합니다", "谢谢", "씨에씨에"),
            Phrase("죄송합니다", "对不起", "뚜이부치"),
            Phrase("실례합니다", "请问", "칭원"),
            Phrase("안녕히 가세요", "再见", "짜이찌엔"),
            Phrase("잘 부탁드립니다", "请多关照", "칭뚜오 꽌자오"),

            // 길찾기
            Phrase("역이 어디에요?", "车站在哪里?", "처잔 짜이나리?"),
            Phrase("화장실이 어디에요?", "洗手间在哪里?", "시서우찌엔 짜이나리?"),
            Phrase("여기가 어디에요?", "这里是哪里?", "쩌리 스나리?"),
            Phrase("걸어서 갈 수 있어요?", "可以走着去吗?", "커이 저우저 취마?"),
            Phrase("얼마나 걸려요?", "要多长时间?", "야오 뚜오창 스찌엔?"),

            // 식당
            Phrase("메뉴판 주세요", "请给我菜单", "칭게이워 차이단"),
            Phrase("이거 주세요", "我要这个", "워야오 쩌거"),
            Phrase("물 주세요", "请给我水", "칭게이워 쉐이"),
            Phrase("계산서 주세요", "买单", "마이단"),
            Phrase("맛있어요!", "很好吃!", "헌하오츠!"),
            Phrase("포장해 주세요", "打包", "다바오"),
            Phrase("추천해 주세요", "有什么推荐?", "요우션머 투이찌엔?"),
            Phrase("예약했어요", "我预约了", "워 위위에러"),

            // 쇼핑
            Phrase("얼마예요?", "多少钱?", "뚜오샤오치엔?"),
            Phrase("너무 비싸요", "太贵了", "타이꾸이러"),
            Phrase("깎아 주세요", "便宜一点", "피엔이 이디엔"),
            Phrase("카드 돼요?", "可以刷卡吗?", "커이 슈아카마?"),
            Phrase("영수증 주세요", "请给我发票", "칭게이워 파피아오"),
            Phrase("이거 입어봐도 돼요?", "可以试穿吗?", "커이 스촨마?"),

            // 호텔
            Phrase("체크인 하고 싶어요", "我要办理入住", "워야오 반리루주"),
            Phrase("체크아웃 하고 싶어요", "我要退房", "워야오 투이팡"),
            Phrase("와이파이 비밀번호가 뭐에요?", "WiFi密码是什么?", "와이파이 미마 스션머?"),
            Phrase("방이 너무 추워요", "房间太冷了", "팡찌엔 타이렁러"),

            // 교통
            Phrase("택시 불러 주세요", "请帮我叫出租车", "칭방워 찌아오 추쭈처"),
            Phrase("여기서 내려 주세요", "请在这里停车", "칭짜이 쩌리 팅처"),
            Phrase("공항까지 가 주세요", "请去机场", "칭취 찌창"),
            Phrase("표 한 장 주세요", "请给我一张票", "칭게이워 이장피아오"),

            // 긴급
            Phrase("도와주세요!", "救命!", "찌우밍!"),
            Phrase("의사가 필요해요", "我需要医生", "워쉬야오 이셩"),
            Phrase("경찰을 불러주세요", "请叫警察", "칭찌아오 찡차"),
            Phrase("길을 잃었어요", "我迷路了", "워미루러"),

            // 소통
            Phrase("중국어 못해요", "我不会说中文", "워부후이 슈오 쭝원"),
            Phrase("천천히 말해 주세요", "请说慢一点", "칭슈오 만이디엔"),
            Phrase("다시 말해 주세요", "请再说一遍", "칭짜이 슈오이비엔"),
            Phrase("모르겠어요", "我不懂", "워부똥"),
            Phrase("적어 주세요", "请写下来", "칭씨에 시아라이")
        )

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, TravelPhraseChineseWidgetProvider::class.java)
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
                        views.setTextViewText(R.id.lang_text, "中")
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
                    views.setTextViewText(R.id.answer_text, phrase.chinese)
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
            val rightClickIntent = Intent(context, TravelPhraseChineseWidgetProvider::class.java).apply {
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
                appWidgetId + 3000,
                leftClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.left_area, leftPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun loadPetRandomFrame(context: Context, petType: PetTypeV2, prefs: PreferenceManager): android.graphics.Bitmap? {
            val petLevel = prefs.getPetLevelV2()
            val stage = petLevel.stage

            return try {
                val idlePath = "pets/${petType.folderName}/${stage.folderName}/idle"
                val frames = context.assets.list(idlePath)?.filter { it.endsWith(".png") } ?: listOf("frame_000.png")
                val randomFrame = frames.random()
                val assetPath = "$idlePath/$randomFrame"

                context.assets.open(assetPath).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                null
            }
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
