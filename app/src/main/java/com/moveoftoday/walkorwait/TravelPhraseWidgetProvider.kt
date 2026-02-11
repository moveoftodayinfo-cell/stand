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
 * 여행 회화 위젯 (2x1)
 * - 왼쪽: 카테고리 + 한국어 문장 (클릭하면 설정)
 * - 가운데: 실선
 * - 오른쪽: 펫 + 영어/발음 (클릭으로 전환)
 */
class TravelPhraseWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_RIGHT_CLICK = "com.moveoftoday.walkorwait.TRAVEL_PHRASE_RIGHT_CLICK"
        private const val ACTION_LEFT_CLICK = "com.moveoftoday.walkorwait.TRAVEL_PHRASE_LEFT_CLICK"
        private const val PREFS_NAME = "TravelPhraseWidget"
        private const val PREF_STATE = "click_state_"
        private const val PREF_PHRASE_INDEX = "phrase_index_"
        private const val PREF_COUNTRY = "country_"
        private const val PREF_CATEGORY = "category_"
        private const val PREF_TAP_SHOWN_DATE = "tap_shown_date_"

        // 나라별 언어 약자
        val countries = mapOf(
            "english" to "EN",
            "japanese" to "日",
            "chinese" to "中",
            "spanish" to "ES",
            "french" to "FR"
        )

        // 카테고리 (상황)
        val categories = listOf(
            "인사", "길찾기", "식당", "쇼핑", "호텔", "교통", "긴급", "소통"
        )

        // 여행 회화 데이터: (카테고리, 한국어, 영어, 한글발음)
        data class Phrase(
            val category: String,
            val korean: String,
            val english: String,
            val pronunciation: String
        )

        val phrases = listOf(
            // 인사
            Phrase("인사", "안녕하세요", "Hello", "헬로"),
            Phrase("인사", "감사합니다", "Thank you", "땡큐"),
            Phrase("인사", "죄송합니다", "I'm sorry", "아임 쏘리"),
            Phrase("인사", "실례합니다", "Excuse me", "익스큐즈 미"),
            Phrase("인사", "안녕히 가세요", "Goodbye", "굿바이"),
            Phrase("인사", "좋은 하루 되세요", "Have a nice day", "해브 어 나이스 데이"),

            // 길찾기
            Phrase("길찾기", "역이 어디에요?", "Where is the station?", "웨어 이즈 더 스테이션?"),
            Phrase("길찾기", "화장실이 어디에요?", "Where is the restroom?", "웨어 이즈 더 레스트룸?"),
            Phrase("길찾기", "여기가 어디에요?", "Where am I?", "웨어 엠 아이?"),
            Phrase("길찾기", "지도 좀 보여주세요", "Can you show me the map?", "캔 유 쇼 미 더 맵?"),
            Phrase("길찾기", "걸어서 갈 수 있어요?", "Can I walk there?", "캔 아이 워크 데어?"),
            Phrase("길찾기", "얼마나 걸려요?", "How long does it take?", "하우 롱 더즈 잇 테이크?"),

            // 식당
            Phrase("식당", "메뉴판 주세요", "Menu, please", "메뉴 플리즈"),
            Phrase("식당", "이거 주세요", "This one, please", "디스 원 플리즈"),
            Phrase("식당", "물 주세요", "Water, please", "워터 플리즈"),
            Phrase("식당", "계산서 주세요", "Check, please", "첵 플리즈"),
            Phrase("식당", "맛있어요!", "It's delicious!", "잇츠 딜리셔스!"),
            Phrase("식당", "포장해 주세요", "To go, please", "투 고 플리즈"),
            Phrase("식당", "추천해 주세요", "What do you recommend?", "왓 두 유 레커멘드?"),
            Phrase("식당", "예약했어요", "I have a reservation", "아이 해브 어 레저베이션"),

            // 쇼핑
            Phrase("쇼핑", "얼마예요?", "How much is it?", "하우 머치 이즈 잇?"),
            Phrase("쇼핑", "너무 비싸요", "It's too expensive", "잇츠 투 익스펜시브"),
            Phrase("쇼핑", "깎아 주세요", "Can you give me a discount?", "캔 유 기브 미 어 디스카운트?"),
            Phrase("쇼핑", "카드 돼요?", "Do you accept cards?", "두 유 억셉트 카즈?"),
            Phrase("쇼핑", "영수증 주세요", "Receipt, please", "리싯 플리즈"),
            Phrase("쇼핑", "이거 입어봐도 돼요?", "Can I try this on?", "캔 아이 트라이 디스 온?"),
            Phrase("쇼핑", "다른 색 있어요?", "Do you have other colors?", "두 유 해브 아더 컬러즈?"),

            // 호텔
            Phrase("호텔", "체크인 하고 싶어요", "I'd like to check in", "아이드 라이크 투 첵 인"),
            Phrase("호텔", "체크아웃 하고 싶어요", "I'd like to check out", "아이드 라이크 투 첵 아웃"),
            Phrase("호텔", "와이파이 비밀번호가 뭐에요?", "What's the WiFi password?", "왓츠 더 와이파이 패스워드?"),
            Phrase("호텔", "방이 너무 추워요", "The room is too cold", "더 룸 이즈 투 콜드"),
            Phrase("호텔", "수건 더 주세요", "More towels, please", "모어 타월즈 플리즈"),
            Phrase("호텔", "조식은 몇 시에요?", "What time is breakfast?", "왓 타임 이즈 브렉퍼스트?"),

            // 교통
            Phrase("교통", "택시 불러 주세요", "Please call a taxi", "플리즈 콜 어 택시"),
            Phrase("교통", "여기서 내려 주세요", "Please stop here", "플리즈 스탑 히어"),
            Phrase("교통", "공항까지 가 주세요", "To the airport, please", "투 디 에어포트 플리즈"),
            Phrase("교통", "이 버스가 어디로 가요?", "Where does this bus go?", "웨어 더즈 디스 버스 고?"),
            Phrase("교통", "표 한 장 주세요", "One ticket, please", "원 티켓 플리즈"),
            Phrase("교통", "환승해야 하나요?", "Do I need to transfer?", "두 아이 니드 투 트랜스퍼?"),

            // 긴급
            Phrase("긴급", "도와주세요!", "Help!", "헬프!"),
            Phrase("긴급", "의사가 필요해요", "I need a doctor", "아이 니드 어 닥터"),
            Phrase("긴급", "경찰을 불러주세요", "Please call the police", "플리즈 콜 더 폴리스"),
            Phrase("긴급", "길을 잃었어요", "I'm lost", "아임 로스트"),
            Phrase("긴급", "핸드폰을 잃어버렸어요", "I lost my phone", "아이 로스트 마이 폰"),
            Phrase("긴급", "여권을 잃어버렸어요", "I lost my passport", "아이 로스트 마이 패스포트"),

            // 소통
            Phrase("소통", "영어 할 줄 아세요?", "Do you speak English?", "두 유 스픽 잉글리시?"),
            Phrase("소통", "천천히 말해 주세요", "Please speak slowly", "플리즈 스픽 슬로울리"),
            Phrase("소통", "다시 말해 주세요", "Please say that again", "플리즈 세이 댓 어게인"),
            Phrase("소통", "모르겠어요", "I don't understand", "아이 돈트 언더스탠드"),
            Phrase("소통", "적어 주세요", "Please write it down", "플리즈 라이트 잇 다운"),
            Phrase("소통", "사진 찍어도 돼요?", "Can I take a picture?", "캔 아이 테이크 어 픽쳐?")
        )

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, TravelPhraseWidgetProvider::class.java)
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
            val country = prefs.getString(PREF_COUNTRY + appWidgetId, "english") ?: "english"
            val selectedCategory = prefs.getString(PREF_CATEGORY + appWidgetId, null)

            // 카테고리 필터링
            val filteredPhrases = if (selectedCategory != null) {
                phrases.filter { it.category == selectedCategory }
            } else {
                phrases
            }

            val phrase = if (filteredPhrases.isNotEmpty()) {
                filteredPhrases[phraseIndex % filteredPhrases.size]
            } else {
                phrases[0]
            }

            val views = RemoteViews(context.packageName, R.layout.widget_travel_phrase)

            // 언어 코드 (EN, 日, 中 등)
            val langCode = countries[country] ?: "EN"

            // 한국어 문장
            views.setTextViewText(R.id.korean_sentence, phrase.korean)

            // 오늘 날짜 확인 (하루에 한 번만 lang/rebon 표시)
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
                    // 초기 상태
                    if (showTapToday) {
                        // 하루 첫 표시: EN / pet / rebon
                        views.setTextViewText(R.id.lang_text, langCode)
                        views.setViewVisibility(R.id.lang_text, View.VISIBLE)
                        views.setViewVisibility(R.id.pet_icon, View.VISIBLE)
                        views.setViewVisibility(R.id.rebon_text, View.VISIBLE)
                        views.setViewVisibility(R.id.answer_text, View.GONE)
                    } else {
                        // 이후: pet만 (다른 스프라이트)
                        views.setViewVisibility(R.id.lang_text, View.GONE)
                        views.setViewVisibility(R.id.pet_icon, View.VISIBLE)
                        views.setViewVisibility(R.id.rebon_text, View.GONE)
                        views.setViewVisibility(R.id.answer_text, View.GONE)
                    }
                }
                1 -> {
                    // 영어 표시
                    views.setViewVisibility(R.id.lang_text, View.GONE)
                    views.setViewVisibility(R.id.pet_icon, View.GONE)
                    views.setViewVisibility(R.id.rebon_text, View.GONE)
                    views.setViewVisibility(R.id.answer_text, View.VISIBLE)
                    views.setTextViewText(R.id.answer_text, phrase.english)
                }
                2 -> {
                    // 한글 발음 표시
                    views.setViewVisibility(R.id.lang_text, View.GONE)
                    views.setViewVisibility(R.id.pet_icon, View.GONE)
                    views.setViewVisibility(R.id.rebon_text, View.GONE)
                    views.setViewVisibility(R.id.answer_text, View.VISIBLE)
                    views.setTextViewText(R.id.answer_text, phrase.pronunciation)
                }
            }

            // 오른쪽 클릭 (정답 보기)
            val rightClickIntent = Intent(context, TravelPhraseWidgetProvider::class.java).apply {
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

            // 왼쪽 클릭 (설정 - 앱 열기)
            val leftClickIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("open_travel_settings", true)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val leftPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId + 1000, // 다른 request code
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
                // idle 폴더에서 프레임 파일 목록 가져오기
                val idlePath = "pets/${petType.folderName}/${stage.folderName}/idle"
                val frames = context.assets.list(idlePath)?.filter { it.endsWith(".png") } ?: listOf("frame_000.png")

                // 랜덤 프레임 선택
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

        /**
         * 나라 설정
         */
        fun setCountry(context: Context, appWidgetId: Int, country: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(PREF_COUNTRY + appWidgetId, country).apply()

            val appWidgetManager = AppWidgetManager.getInstance(context)
            updateWidget(context, appWidgetManager, appWidgetId)
        }

        /**
         * 카테고리 설정
         */
        fun setCategory(context: Context, appWidgetId: Int, category: String?) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(PREF_CATEGORY + appWidgetId, category)
                .putInt(PREF_PHRASE_INDEX + appWidgetId, 0) // 인덱스 초기화
                .putInt(PREF_STATE + appWidgetId, 0) // 상태 초기화
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

                // 카테고리 필터링
                val filteredPhrases = if (selectedCategory != null) {
                    phrases.filter { it.category == selectedCategory }
                } else {
                    phrases
                }

                // 상태 전환: 0 → 1 → 2 → 0 (다음 문장, 랜덤)
                val previousState = state
                state++

                // state 0 → 1 전환 시 (첫 탭) 오늘 날짜 저장
                if (previousState == 0 && state == 1) {
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    prefs.edit().putString(PREF_TAP_SHOWN_DATE + appWidgetId, today).apply()
                }

                if (state > 2) {
                    state = 0
                    // 랜덤으로 다음 문장 선택 (현재와 다른 문장)
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
            editor.remove(PREF_COUNTRY + appWidgetId)
            editor.remove(PREF_CATEGORY + appWidgetId)
            editor.remove(PREF_TAP_SHOWN_DATE + appWidgetId)
        }
        editor.apply()
    }
}
