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
import java.util.Locale

/**
 * 한국어 학습 위젯 (2x1) - 외국인을 위한 한국어 회화
 * - 왼쪽: 외국어 문장 (기기 언어에 따라 영어/일어/중어/스페인어)
 * - 오른쪽: 펫 + 한국어/로마자 발음 (클릭으로 전환)
 *
 * 기기 언어가 한국어인 경우 이 위젯은 숨김 처리됨
 */
class TravelPhraseKoreanWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_RIGHT_CLICK = "com.moveoftoday.walkorwait.TRAVEL_PHRASE_KR_RIGHT_CLICK"
        private const val PREFS_NAME = "TravelPhraseKoreanWidget"
        private const val PREF_STATE = "click_state_"
        private const val PREF_PHRASE_INDEX = "phrase_index_"
        private const val PREF_CATEGORY = "category_"
        private const val PREF_TAP_SHOWN_DATE = "tap_shown_date_"

        // 카테고리 (상황) - 기기 언어별
        fun getCategories(lang: String): List<String> = when (lang) {
            "ja" -> listOf("挨拶", "道案内", "レストラン", "ショッピング", "ホテル", "交通", "緊急", "コミュニケーション")
            "zh" -> listOf("问候", "问路", "餐厅", "购物", "酒店", "交通", "紧急", "交流")
            "es" -> listOf("Saludos", "Direcciones", "Restaurante", "Compras", "Hotel", "Transporte", "Emergencia", "Comunicación")
            else -> listOf("Greetings", "Directions", "Restaurant", "Shopping", "Hotel", "Transport", "Emergency", "Communication")
        }

        // 한국어 학습 데이터: (카테고리, 외국어, 한국어, 로마자발음)
        data class KoreanPhrase(
            val categoryEn: String,
            val english: String,
            val japanese: String,
            val chinese: String,
            val spanish: String,
            val korean: String,
            val romanization: String
        )

        val phrases = listOf(
            // Greetings (인사)
            KoreanPhrase("Greetings", "Hello", "こんにちは", "你好", "Hola", "안녕하세요", "annyeonghaseyo"),
            KoreanPhrase("Greetings", "Thank you", "ありがとう", "谢谢", "Gracias", "감사합니다", "gamsahamnida"),
            KoreanPhrase("Greetings", "I'm sorry", "すみません", "对不起", "Lo siento", "죄송합니다", "joesonghamnida"),
            KoreanPhrase("Greetings", "Excuse me", "すみません", "打扰一下", "Perdón", "실례합니다", "sillyehamnida"),
            KoreanPhrase("Greetings", "Goodbye", "さようなら", "再见", "Adiós", "안녕히 가세요", "annyeonghi gaseyo"),
            KoreanPhrase("Greetings", "Nice to meet you", "はじめまして", "很高兴认识你", "Mucho gusto", "만나서 반갑습니다", "mannaseo bangapseumnida"),

            // Directions (길찾기)
            KoreanPhrase("Directions", "Where is the station?", "駅はどこですか？", "车站在哪里？", "¿Dónde está la estación?", "역이 어디예요?", "yeogi eodiyeyo?"),
            KoreanPhrase("Directions", "Where is the restroom?", "トイレはどこですか？", "洗手间在哪里？", "¿Dónde está el baño?", "화장실이 어디예요?", "hwajangsiri eodiyeyo?"),
            KoreanPhrase("Directions", "Where am I?", "ここはどこですか？", "这是哪里？", "¿Dónde estoy?", "여기가 어디예요?", "yeogiga eodiyeyo?"),
            KoreanPhrase("Directions", "How long does it take?", "どのくらいかかりますか？", "需要多长时间？", "¿Cuánto tiempo toma?", "얼마나 걸려요?", "eolmana geollyeoyo?"),
            KoreanPhrase("Directions", "Turn left", "左に曲がって", "向左转", "Gira a la izquierda", "왼쪽으로 가세요", "oenjjogeuro gaseyo"),
            KoreanPhrase("Directions", "Turn right", "右に曲がって", "向右转", "Gira a la derecha", "오른쪽으로 가세요", "oreunjjogeuro gaseyo"),

            // Restaurant (식당)
            KoreanPhrase("Restaurant", "Menu, please", "メニューをください", "请给我菜单", "El menú, por favor", "메뉴판 주세요", "menyupan juseyo"),
            KoreanPhrase("Restaurant", "This one, please", "これをください", "请给我这个", "Este, por favor", "이거 주세요", "igeo juseyo"),
            KoreanPhrase("Restaurant", "Water, please", "お水をください", "请给我水", "Agua, por favor", "물 주세요", "mul juseyo"),
            KoreanPhrase("Restaurant", "Check, please", "お会計をお願いします", "请结账", "La cuenta, por favor", "계산해 주세요", "gyesanhae juseyo"),
            KoreanPhrase("Restaurant", "It's delicious!", "おいしい！", "好吃！", "¡Está delicioso!", "맛있어요!", "masisseoyo!"),
            KoreanPhrase("Restaurant", "Spicy", "辛い", "辣", "Picante", "매워요", "maewoyo"),
            KoreanPhrase("Restaurant", "Not spicy, please", "辛くないのをください", "请不要辣", "Sin picante, por favor", "안 맵게 해주세요", "an maepge haejuseyo"),

            // Shopping (쇼핑)
            KoreanPhrase("Shopping", "How much is it?", "いくらですか？", "多少钱？", "¿Cuánto cuesta?", "얼마예요?", "eolmayeyo?"),
            KoreanPhrase("Shopping", "Too expensive", "高すぎます", "太贵了", "Muy caro", "너무 비싸요", "neomu bissayo"),
            KoreanPhrase("Shopping", "Discount, please", "割引してください", "请打折", "Descuento, por favor", "깎아 주세요", "kkakka juseyo"),
            KoreanPhrase("Shopping", "Do you accept cards?", "カードは使えますか？", "可以刷卡吗？", "¿Aceptan tarjeta?", "카드 돼요?", "kadeu dwaeyo?"),
            KoreanPhrase("Shopping", "Can I try this on?", "試着できますか？", "可以试穿吗？", "¿Puedo probármelo?", "입어봐도 돼요?", "ibeobwado dwaeyo?"),

            // Hotel (호텔)
            KoreanPhrase("Hotel", "Check in, please", "チェックインお願いします", "我要办理入住", "Quiero hacer el check-in", "체크인 하고 싶어요", "chekeu-in hago sipeoyo"),
            KoreanPhrase("Hotel", "Check out, please", "チェックアウトお願いします", "我要退房", "Quiero hacer el check-out", "체크아웃 하고 싶어요", "chekeuaus hago sipeoyo"),
            KoreanPhrase("Hotel", "What's the WiFi password?", "WiFiのパスワードは？", "WiFi密码是什么？", "¿Cuál es la contraseña del WiFi?", "와이파이 비밀번호가 뭐예요?", "waipai bimilbeonhoga mwoyeyo?"),
            KoreanPhrase("Hotel", "More towels, please", "タオルをもっとください", "请多给我毛巾", "Más toallas, por favor", "수건 더 주세요", "sugeon deo juseyo"),

            // Transport (교통)
            KoreanPhrase("Transport", "Call a taxi, please", "タクシーを呼んでください", "请叫出租车", "Llame un taxi, por favor", "택시 불러 주세요", "taeksi bulleo juseyo"),
            KoreanPhrase("Transport", "Stop here, please", "ここで止めてください", "请在这里停", "Pare aquí, por favor", "여기서 내려 주세요", "yeogiseo naeryeo juseyo"),
            KoreanPhrase("Transport", "To the airport, please", "空港までお願いします", "请去机场", "Al aeropuerto, por favor", "공항까지 가 주세요", "gonghangkkaji ga juseyo"),
            KoreanPhrase("Transport", "One ticket, please", "チケット一枚ください", "请给我一张票", "Un boleto, por favor", "표 한 장 주세요", "pyo han jang juseyo"),

            // Emergency (긴급)
            KoreanPhrase("Emergency", "Help!", "助けて！", "救命！", "¡Ayuda!", "도와주세요!", "dowajuseyo!"),
            KoreanPhrase("Emergency", "I need a doctor", "医者が必要です", "我需要医生", "Necesito un médico", "의사가 필요해요", "uisaga piryohaeyo"),
            KoreanPhrase("Emergency", "Call the police", "警察を呼んでください", "请叫警察", "Llame a la policía", "경찰을 불러주세요", "gyeongchareul bulleojuseyo"),
            KoreanPhrase("Emergency", "I'm lost", "迷いました", "我迷路了", "Estoy perdido", "길을 잃었어요", "gireul ilheosseoyo"),
            KoreanPhrase("Emergency", "I lost my phone", "携帯をなくしました", "我手机丢了", "Perdí mi teléfono", "핸드폰을 잃어버렸어요", "haendeupon-eul ilheobeoryeosseoyo"),

            // Communication (소통)
            KoreanPhrase("Communication", "Do you speak English?", "英語を話せますか？", "你会说英语吗？", "¿Hablas inglés?", "영어 할 줄 아세요?", "yeongeo hal jul aseyo?"),
            KoreanPhrase("Communication", "Speak slowly, please", "ゆっくり話してください", "请说慢一点", "Habla despacio, por favor", "천천히 말해 주세요", "cheoncheonhi malhae juseyo"),
            KoreanPhrase("Communication", "Say it again, please", "もう一度言ってください", "请再说一遍", "Repite, por favor", "다시 말해 주세요", "dasi malhae juseyo"),
            KoreanPhrase("Communication", "I don't understand", "わかりません", "我不明白", "No entiendo", "모르겠어요", "moreugesseoyo"),
            KoreanPhrase("Communication", "I love Korea!", "韓国が大好き！", "我爱韩国！", "¡Amo Corea!", "한국 좋아해요!", "hanguk joahaeyo!")
        )

        /**
         * 기기 언어가 한국어인지 확인 (한국어면 이 위젯은 의미 없음)
         */
        fun isDeviceKorean(): Boolean {
            return Locale.getDefault().language == "ko"
        }

        /**
         * 기기 언어 코드 반환
         */
        private fun getDeviceLang(): String = Locale.getDefault().language

        /**
         * 기기 언어에 따른 외국어 문장 반환
         */
        private fun getForeignSentence(phrase: KoreanPhrase): String {
            return when (getDeviceLang()) {
                "ja" -> phrase.japanese
                "zh" -> phrase.chinese
                "es" -> phrase.spanish
                else -> phrase.english
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, TravelPhraseKoreanWidgetProvider::class.java)
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

            // 카테고리 필터링
            val filteredPhrases = if (selectedCategory != null) {
                phrases.filter { it.categoryEn == selectedCategory }
            } else {
                phrases
            }

            val phrase = if (filteredPhrases.isNotEmpty()) {
                filteredPhrases[phraseIndex % filteredPhrases.size]
            } else {
                phrases[0]
            }

            val views = RemoteViews(context.packageName, R.layout.widget_travel_phrase)

            // 언어 코드 (KR 고정)
            val langCode = "KR"

            // 외국어 문장 (기기 언어에 따라)
            views.setTextViewText(R.id.korean_sentence, getForeignSentence(phrase))

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
                        // 하루 첫 표시: KR / pet / rebon
                        views.setTextViewText(R.id.lang_text, langCode)
                        views.setViewVisibility(R.id.lang_text, View.VISIBLE)
                        views.setViewVisibility(R.id.pet_icon, View.VISIBLE)
                        views.setViewVisibility(R.id.rebon_text, View.VISIBLE)
                        views.setViewVisibility(R.id.answer_text, View.GONE)
                    } else {
                        // 이후: pet만
                        views.setViewVisibility(R.id.lang_text, View.GONE)
                        views.setViewVisibility(R.id.pet_icon, View.VISIBLE)
                        views.setViewVisibility(R.id.rebon_text, View.GONE)
                        views.setViewVisibility(R.id.answer_text, View.GONE)
                    }
                }
                1 -> {
                    // 한국어 표시
                    views.setViewVisibility(R.id.lang_text, View.GONE)
                    views.setViewVisibility(R.id.pet_icon, View.GONE)
                    views.setViewVisibility(R.id.rebon_text, View.GONE)
                    views.setViewVisibility(R.id.answer_text, View.VISIBLE)
                    views.setTextViewText(R.id.answer_text, phrase.korean)
                }
                2 -> {
                    // 로마자 발음 표시
                    views.setViewVisibility(R.id.lang_text, View.GONE)
                    views.setViewVisibility(R.id.pet_icon, View.GONE)
                    views.setViewVisibility(R.id.rebon_text, View.GONE)
                    views.setViewVisibility(R.id.answer_text, View.VISIBLE)
                    views.setTextViewText(R.id.answer_text, phrase.romanization)
                }
            }

            // 오른쪽 클릭 (정답 보기)
            val rightClickIntent = Intent(context, TravelPhraseKoreanWidgetProvider::class.java).apply {
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
                appWidgetId + 5000, // 다른 request code
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

                val filteredPhrases = if (selectedCategory != null) {
                    phrases.filter { it.categoryEn == selectedCategory }
                } else {
                    phrases
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
