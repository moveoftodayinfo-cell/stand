package com.moveoftoday.walkorwait

import java.util.Locale

/**
 * 여행 회화 다국어 데이터
 * - 6개 언어 지원: en, ko, ja, zh, es, hi
 * - 시스템 언어 기반 표시
 */
object TravelPhraseData {

    /**
     * 다국어 문장 데이터
     * @param category 카테고리 키 (greeting, directions, etc.)
     * @param translations 언어별 번역 (en, ko, ja, zh, es, hi)
     * @param romanization 로마자 발음 (학습 대상 언어용)
     */
    data class MultilingualPhrase(
        val category: String,
        val translations: Map<String, String>,
        val romanization: Map<String, String>  // 언어별 로마자 발음
    )

    // 지원 언어
    val supportedLanguages = listOf("en", "ko", "ja", "zh", "es", "hi")

    // 카테고리 다국어 매핑
    val categories = mapOf(
        "greeting" to mapOf(
            "en" to "Greetings",
            "ko" to "인사",
            "ja" to "挨拶",
            "zh" to "问候",
            "es" to "Saludos",
            "hi" to "अभिवादन"
        ),
        "directions" to mapOf(
            "en" to "Directions",
            "ko" to "길찾기",
            "ja" to "道案内",
            "zh" to "问路",
            "es" to "Direcciones",
            "hi" to "दिशाएं"
        ),
        "restaurant" to mapOf(
            "en" to "Restaurant",
            "ko" to "식당",
            "ja" to "レストラン",
            "zh" to "餐厅",
            "es" to "Restaurante",
            "hi" to "रेस्तरां"
        ),
        "shopping" to mapOf(
            "en" to "Shopping",
            "ko" to "쇼핑",
            "ja" to "ショッピング",
            "zh" to "购物",
            "es" to "Compras",
            "hi" to "खरीदारी"
        ),
        "hotel" to mapOf(
            "en" to "Hotel",
            "ko" to "호텔",
            "ja" to "ホテル",
            "zh" to "酒店",
            "es" to "Hotel",
            "hi" to "होटल"
        ),
        "transport" to mapOf(
            "en" to "Transport",
            "ko" to "교통",
            "ja" to "交通",
            "zh" to "交通",
            "es" to "Transporte",
            "hi" to "परिवहन"
        ),
        "emergency" to mapOf(
            "en" to "Emergency",
            "ko" to "긴급",
            "ja" to "緊急",
            "zh" to "紧急",
            "es" to "Emergencia",
            "hi" to "आपातकाल"
        ),
        "communication" to mapOf(
            "en" to "Communication",
            "ko" to "소통",
            "ja" to "コミュニケーション",
            "zh" to "交流",
            "es" to "Comunicación",
            "hi" to "संचार"
        )
    )

    /**
     * 시스템 언어로 카테고리 이름 가져오기
     */
    fun getCategoryName(categoryKey: String): String {
        val lang = Locale.getDefault().language
        return categories[categoryKey]?.get(lang)
            ?: categories[categoryKey]?.get("en")
            ?: categoryKey
    }

    /**
     * 시스템 언어로 번역 가져오기
     */
    fun getTranslation(phrase: MultilingualPhrase): String {
        val lang = Locale.getDefault().language
        return phrase.translations[lang]
            ?: phrase.translations["en"]
            ?: ""
    }

    /**
     * 특정 언어의 번역 가져오기
     */
    fun getTranslation(phrase: MultilingualPhrase, targetLang: String): String {
        return phrase.translations[targetLang] ?: ""
    }

    /**
     * 특정 언어의 로마자 발음 가져오기
     */
    fun getRomanization(phrase: MultilingualPhrase, targetLang: String): String {
        return phrase.romanization[targetLang] ?: ""
    }

    // ============ 문장 데이터 (60개 × 6언어) ============

    val phrases = listOf(
        // ========== GREETING (인사) ==========
        MultilingualPhrase(
            category = "greeting",
            translations = mapOf(
                "en" to "Hello",
                "ko" to "안녕하세요",
                "ja" to "こんにちは",
                "zh" to "你好",
                "es" to "Hola",
                "hi" to "नमस्ते"
            ),
            romanization = mapOf(
                "ko" to "annyeonghaseyo",
                "ja" to "konnichiwa",
                "zh" to "nǐ hǎo",
                "hi" to "namaste"
            )
        ),
        MultilingualPhrase(
            category = "greeting",
            translations = mapOf(
                "en" to "Thank you",
                "ko" to "감사합니다",
                "ja" to "ありがとうございます",
                "zh" to "谢谢",
                "es" to "Gracias",
                "hi" to "धन्यवाद"
            ),
            romanization = mapOf(
                "ko" to "gamsahamnida",
                "ja" to "arigatou gozaimasu",
                "zh" to "xiè xie",
                "hi" to "dhanyavaad"
            )
        ),
        MultilingualPhrase(
            category = "greeting",
            translations = mapOf(
                "en" to "I'm sorry",
                "ko" to "죄송합니다",
                "ja" to "すみません",
                "zh" to "对不起",
                "es" to "Lo siento",
                "hi" to "माफ़ कीजिए"
            ),
            romanization = mapOf(
                "ko" to "joesonghamnida",
                "ja" to "sumimasen",
                "zh" to "duì bu qǐ",
                "hi" to "maaf keejiye"
            )
        ),
        MultilingualPhrase(
            category = "greeting",
            translations = mapOf(
                "en" to "Excuse me",
                "ko" to "실례합니다",
                "ja" to "失礼します",
                "zh" to "打扰一下",
                "es" to "Disculpe",
                "hi" to "सुनिए"
            ),
            romanization = mapOf(
                "ko" to "sillyehamnida",
                "ja" to "shitsurei shimasu",
                "zh" to "dǎ rǎo yī xià",
                "hi" to "suniye"
            )
        ),
        MultilingualPhrase(
            category = "greeting",
            translations = mapOf(
                "en" to "Goodbye",
                "ko" to "안녕히 가세요",
                "ja" to "さようなら",
                "zh" to "再见",
                "es" to "Adiós",
                "hi" to "अलविदा"
            ),
            romanization = mapOf(
                "ko" to "annyeonghi gaseyo",
                "ja" to "sayounara",
                "zh" to "zài jiàn",
                "hi" to "alvida"
            )
        ),
        MultilingualPhrase(
            category = "greeting",
            translations = mapOf(
                "en" to "Have a nice day",
                "ko" to "좋은 하루 되세요",
                "ja" to "良い一日を",
                "zh" to "祝你有美好的一天",
                "es" to "Que tenga un buen día",
                "hi" to "आपका दिन शुभ हो"
            ),
            romanization = mapOf(
                "ko" to "joeun haru doeseyo",
                "ja" to "yoi ichinichi wo",
                "zh" to "zhù nǐ yǒu měi hǎo de yī tiān",
                "hi" to "aapka din shubh ho"
            )
        ),

        // ========== DIRECTIONS (길찾기) ==========
        MultilingualPhrase(
            category = "directions",
            translations = mapOf(
                "en" to "Where is the station?",
                "ko" to "역이 어디에요?",
                "ja" to "駅はどこですか？",
                "zh" to "车站在哪里？",
                "es" to "¿Dónde está la estación?",
                "hi" to "स्टेशन कहाँ है?"
            ),
            romanization = mapOf(
                "ko" to "yeogi eodieyo?",
                "ja" to "eki wa doko desu ka?",
                "zh" to "chē zhàn zài nǎ lǐ?",
                "hi" to "station kahaan hai?"
            )
        ),
        MultilingualPhrase(
            category = "directions",
            translations = mapOf(
                "en" to "Where is the restroom?",
                "ko" to "화장실이 어디에요?",
                "ja" to "トイレはどこですか？",
                "zh" to "洗手间在哪里？",
                "es" to "¿Dónde está el baño?",
                "hi" to "शौचालय कहाँ है?"
            ),
            romanization = mapOf(
                "ko" to "hwajangsil-i eodieyo?",
                "ja" to "toire wa doko desu ka?",
                "zh" to "xǐ shǒu jiān zài nǎ lǐ?",
                "hi" to "shauchalaya kahaan hai?"
            )
        ),
        MultilingualPhrase(
            category = "directions",
            translations = mapOf(
                "en" to "Where am I?",
                "ko" to "여기가 어디에요?",
                "ja" to "ここはどこですか？",
                "zh" to "这里是哪里？",
                "es" to "¿Dónde estoy?",
                "hi" to "मैं कहाँ हूँ?"
            ),
            romanization = mapOf(
                "ko" to "yeogiga eodieyo?",
                "ja" to "koko wa doko desu ka?",
                "zh" to "zhè lǐ shì nǎ lǐ?",
                "hi" to "main kahaan hoon?"
            )
        ),
        MultilingualPhrase(
            category = "directions",
            translations = mapOf(
                "en" to "Can you show me the map?",
                "ko" to "지도 좀 보여주세요",
                "ja" to "地図を見せてください",
                "zh" to "能给我看一下地图吗？",
                "es" to "¿Puede mostrarme el mapa?",
                "hi" to "क्या आप मुझे नक्शा दिखा सकते हैं?"
            ),
            romanization = mapOf(
                "ko" to "jido jom boyeojuseyo",
                "ja" to "chizu wo misete kudasai",
                "zh" to "néng gěi wǒ kàn yī xià dì tú ma?",
                "hi" to "kya aap mujhe naksha dikha sakte hain?"
            )
        ),
        MultilingualPhrase(
            category = "directions",
            translations = mapOf(
                "en" to "Can I walk there?",
                "ko" to "걸어서 갈 수 있어요?",
                "ja" to "歩いて行けますか？",
                "zh" to "可以走路去吗？",
                "es" to "¿Puedo ir caminando?",
                "hi" to "क्या मैं वहाँ पैदल जा सकता हूँ?"
            ),
            romanization = mapOf(
                "ko" to "georeoseo gal su isseoyo?",
                "ja" to "aruite ikemasu ka?",
                "zh" to "kě yǐ zǒu lù qù ma?",
                "hi" to "kya main vahaan paidal ja sakta hoon?"
            )
        ),
        MultilingualPhrase(
            category = "directions",
            translations = mapOf(
                "en" to "How long does it take?",
                "ko" to "얼마나 걸려요?",
                "ja" to "どのくらいかかりますか？",
                "zh" to "需要多长时间？",
                "es" to "¿Cuánto tiempo tarda?",
                "hi" to "कितना समय लगेगा?"
            ),
            romanization = mapOf(
                "ko" to "eolmana geollyeoyo?",
                "ja" to "dono kurai kakarimasu ka?",
                "zh" to "xū yào duō cháng shí jiān?",
                "hi" to "kitna samay lagega?"
            )
        ),

        // ========== RESTAURANT (식당) ==========
        MultilingualPhrase(
            category = "restaurant",
            translations = mapOf(
                "en" to "Menu, please",
                "ko" to "메뉴판 주세요",
                "ja" to "メニューをください",
                "zh" to "请给我菜单",
                "es" to "El menú, por favor",
                "hi" to "मेन्यू दीजिए"
            ),
            romanization = mapOf(
                "ko" to "menyupan juseyo",
                "ja" to "menyuu wo kudasai",
                "zh" to "qǐng gěi wǒ cài dān",
                "hi" to "menu dijiye"
            )
        ),
        MultilingualPhrase(
            category = "restaurant",
            translations = mapOf(
                "en" to "This one, please",
                "ko" to "이거 주세요",
                "ja" to "これをください",
                "zh" to "请给我这个",
                "es" to "Este, por favor",
                "hi" to "यह दीजिए"
            ),
            romanization = mapOf(
                "ko" to "igeo juseyo",
                "ja" to "kore wo kudasai",
                "zh" to "qǐng gěi wǒ zhè ge",
                "hi" to "yeh dijiye"
            )
        ),
        MultilingualPhrase(
            category = "restaurant",
            translations = mapOf(
                "en" to "Water, please",
                "ko" to "물 주세요",
                "ja" to "お水をください",
                "zh" to "请给我水",
                "es" to "Agua, por favor",
                "hi" to "पानी दीजिए"
            ),
            romanization = mapOf(
                "ko" to "mul juseyo",
                "ja" to "omizu wo kudasai",
                "zh" to "qǐng gěi wǒ shuǐ",
                "hi" to "paani dijiye"
            )
        ),
        MultilingualPhrase(
            category = "restaurant",
            translations = mapOf(
                "en" to "Check, please",
                "ko" to "계산서 주세요",
                "ja" to "お会計をお願いします",
                "zh" to "请结账",
                "es" to "La cuenta, por favor",
                "hi" to "बिल दीजिए"
            ),
            romanization = mapOf(
                "ko" to "gyesanseo juseyo",
                "ja" to "okaikei wo onegai shimasu",
                "zh" to "qǐng jié zhàng",
                "hi" to "bill dijiye"
            )
        ),
        MultilingualPhrase(
            category = "restaurant",
            translations = mapOf(
                "en" to "It's delicious!",
                "ko" to "맛있어요!",
                "ja" to "美味しいです！",
                "zh" to "很好吃！",
                "es" to "¡Está delicioso!",
                "hi" to "बहुत स्वादिष्ट है!"
            ),
            romanization = mapOf(
                "ko" to "masisseoyo!",
                "ja" to "oishii desu!",
                "zh" to "hěn hǎo chī!",
                "hi" to "bahut swaadisht hai!"
            )
        ),
        MultilingualPhrase(
            category = "restaurant",
            translations = mapOf(
                "en" to "To go, please",
                "ko" to "포장해 주세요",
                "ja" to "持ち帰りでお願いします",
                "zh" to "请打包",
                "es" to "Para llevar, por favor",
                "hi" to "पैक कर दीजिए"
            ),
            romanization = mapOf(
                "ko" to "pojanghae juseyo",
                "ja" to "mochikaeri de onegai shimasu",
                "zh" to "qǐng dǎ bāo",
                "hi" to "pack kar dijiye"
            )
        ),
        MultilingualPhrase(
            category = "restaurant",
            translations = mapOf(
                "en" to "What do you recommend?",
                "ko" to "추천해 주세요",
                "ja" to "おすすめは何ですか？",
                "zh" to "有什么推荐的吗？",
                "es" to "¿Qué recomienda?",
                "hi" to "आप क्या सुझाव देंगे?"
            ),
            romanization = mapOf(
                "ko" to "chucheonhae juseyo",
                "ja" to "osusume wa nan desu ka?",
                "zh" to "yǒu shén me tuī jiàn de ma?",
                "hi" to "aap kya sujhaav denge?"
            )
        ),
        MultilingualPhrase(
            category = "restaurant",
            translations = mapOf(
                "en" to "I have a reservation",
                "ko" to "예약했어요",
                "ja" to "予約しています",
                "zh" to "我有预约",
                "es" to "Tengo una reserva",
                "hi" to "मेरा आरक्षण है"
            ),
            romanization = mapOf(
                "ko" to "yeyakhaesseoyo",
                "ja" to "yoyaku shite imasu",
                "zh" to "wǒ yǒu yù yuē",
                "hi" to "mera aarakshan hai"
            )
        ),

        // ========== SHOPPING (쇼핑) ==========
        MultilingualPhrase(
            category = "shopping",
            translations = mapOf(
                "en" to "How much is it?",
                "ko" to "얼마예요?",
                "ja" to "いくらですか？",
                "zh" to "多少钱？",
                "es" to "¿Cuánto cuesta?",
                "hi" to "इसका दाम क्या है?"
            ),
            romanization = mapOf(
                "ko" to "eolmayeyo?",
                "ja" to "ikura desu ka?",
                "zh" to "duō shǎo qián?",
                "hi" to "iska daam kya hai?"
            )
        ),
        MultilingualPhrase(
            category = "shopping",
            translations = mapOf(
                "en" to "It's too expensive",
                "ko" to "너무 비싸요",
                "ja" to "高すぎます",
                "zh" to "太贵了",
                "es" to "Es muy caro",
                "hi" to "यह बहुत महंगा है"
            ),
            romanization = mapOf(
                "ko" to "neomu bissayo",
                "ja" to "takasugimasu",
                "zh" to "tài guì le",
                "hi" to "yeh bahut mahanga hai"
            )
        ),
        MultilingualPhrase(
            category = "shopping",
            translations = mapOf(
                "en" to "Can you give me a discount?",
                "ko" to "깎아 주세요",
                "ja" to "値引きしてもらえますか？",
                "zh" to "能便宜一点吗？",
                "es" to "¿Puede hacerme un descuento?",
                "hi" to "क्या आप छूट दे सकते हैं?"
            ),
            romanization = mapOf(
                "ko" to "kkakka juseyo",
                "ja" to "nebiki shite moraemasu ka?",
                "zh" to "néng pián yi yī diǎn ma?",
                "hi" to "kya aap chhoot de sakte hain?"
            )
        ),
        MultilingualPhrase(
            category = "shopping",
            translations = mapOf(
                "en" to "Do you accept cards?",
                "ko" to "카드 돼요?",
                "ja" to "カードは使えますか？",
                "zh" to "可以刷卡吗？",
                "es" to "¿Aceptan tarjeta?",
                "hi" to "क्या कार्ड चलेगा?"
            ),
            romanization = mapOf(
                "ko" to "kadeu dwaeyo?",
                "ja" to "kaado wa tsukaemasu ka?",
                "zh" to "kě yǐ shuā kǎ ma?",
                "hi" to "kya card chalega?"
            )
        ),
        MultilingualPhrase(
            category = "shopping",
            translations = mapOf(
                "en" to "Receipt, please",
                "ko" to "영수증 주세요",
                "ja" to "領収書をください",
                "zh" to "请给我收据",
                "es" to "El recibo, por favor",
                "hi" to "रसीद दीजिए"
            ),
            romanization = mapOf(
                "ko" to "yeongsujeung juseyo",
                "ja" to "ryoushuusho wo kudasai",
                "zh" to "qǐng gěi wǒ shōu jù",
                "hi" to "raseed dijiye"
            )
        ),
        MultilingualPhrase(
            category = "shopping",
            translations = mapOf(
                "en" to "Can I try this on?",
                "ko" to "이거 입어봐도 돼요?",
                "ja" to "試着してもいいですか？",
                "zh" to "可以试穿吗？",
                "es" to "¿Puedo probármelo?",
                "hi" to "क्या मैं इसे पहनकर देख सकता हूँ?"
            ),
            romanization = mapOf(
                "ko" to "igeo ibeobwado dwaeyo?",
                "ja" to "shichaku shitemo ii desu ka?",
                "zh" to "kě yǐ shì chuān ma?",
                "hi" to "kya main ise pehankar dekh sakta hoon?"
            )
        ),
        MultilingualPhrase(
            category = "shopping",
            translations = mapOf(
                "en" to "Do you have other colors?",
                "ko" to "다른 색 있어요?",
                "ja" to "他の色はありますか？",
                "zh" to "有其他颜色吗？",
                "es" to "¿Tiene otros colores?",
                "hi" to "क्या दूसरे रंग हैं?"
            ),
            romanization = mapOf(
                "ko" to "dareun saek isseoyo?",
                "ja" to "hoka no iro wa arimasu ka?",
                "zh" to "yǒu qí tā yán sè ma?",
                "hi" to "kya doosre rang hain?"
            )
        ),

        // ========== HOTEL (호텔) ==========
        MultilingualPhrase(
            category = "hotel",
            translations = mapOf(
                "en" to "I'd like to check in",
                "ko" to "체크인 하고 싶어요",
                "ja" to "チェックインをお願いします",
                "zh" to "我想办理入住",
                "es" to "Quisiera registrarme",
                "hi" to "मैं चेक इन करना चाहता हूँ"
            ),
            romanization = mapOf(
                "ko" to "chekeu-in hago sipeoyo",
                "ja" to "chekkuin wo onegai shimasu",
                "zh" to "wǒ xiǎng bàn lǐ rù zhù",
                "hi" to "main check in karna chahta hoon"
            )
        ),
        MultilingualPhrase(
            category = "hotel",
            translations = mapOf(
                "en" to "I'd like to check out",
                "ko" to "체크아웃 하고 싶어요",
                "ja" to "チェックアウトをお願いします",
                "zh" to "我想退房",
                "es" to "Quisiera hacer el check-out",
                "hi" to "मैं चेक आउट करना चाहता हूँ"
            ),
            romanization = mapOf(
                "ko" to "chekeu-aut hago sipeoyo",
                "ja" to "chekkuauto wo onegai shimasu",
                "zh" to "wǒ xiǎng tuì fáng",
                "hi" to "main check out karna chahta hoon"
            )
        ),
        MultilingualPhrase(
            category = "hotel",
            translations = mapOf(
                "en" to "What's the WiFi password?",
                "ko" to "와이파이 비밀번호가 뭐에요?",
                "ja" to "WiFiのパスワードは何ですか？",
                "zh" to "WiFi密码是多少？",
                "es" to "¿Cuál es la contraseña del WiFi?",
                "hi" to "वाईफाई का पासवर्ड क्या है?"
            ),
            romanization = mapOf(
                "ko" to "waipai bimilbeonhoga mwoeyo?",
                "ja" to "waifai no pasuwaado wa nan desu ka?",
                "zh" to "WiFi mì mǎ shì duō shǎo?",
                "hi" to "WiFi ka password kya hai?"
            )
        ),
        MultilingualPhrase(
            category = "hotel",
            translations = mapOf(
                "en" to "The room is too cold",
                "ko" to "방이 너무 추워요",
                "ja" to "部屋が寒すぎます",
                "zh" to "房间太冷了",
                "es" to "La habitación está muy fría",
                "hi" to "कमरा बहुत ठंडा है"
            ),
            romanization = mapOf(
                "ko" to "bangi neomu chuwoyo",
                "ja" to "heya ga samusugimasu",
                "zh" to "fáng jiān tài lěng le",
                "hi" to "kamra bahut thanda hai"
            )
        ),
        MultilingualPhrase(
            category = "hotel",
            translations = mapOf(
                "en" to "More towels, please",
                "ko" to "수건 더 주세요",
                "ja" to "タオルをもっとください",
                "zh" to "请多给我一些毛巾",
                "es" to "Más toallas, por favor",
                "hi" to "और तौलिए दीजिए"
            ),
            romanization = mapOf(
                "ko" to "sugeon deo juseyo",
                "ja" to "taoru wo motto kudasai",
                "zh" to "qǐng duō gěi wǒ yī xiē máo jīn",
                "hi" to "aur tauliye dijiye"
            )
        ),
        MultilingualPhrase(
            category = "hotel",
            translations = mapOf(
                "en" to "What time is breakfast?",
                "ko" to "조식은 몇 시에요?",
                "ja" to "朝食は何時ですか？",
                "zh" to "早餐几点开始？",
                "es" to "¿A qué hora es el desayuno?",
                "hi" to "नाश्ता कितने बजे है?"
            ),
            romanization = mapOf(
                "ko" to "josig-eun myeot sieyo?",
                "ja" to "choushoku wa nanji desu ka?",
                "zh" to "zǎo cān jǐ diǎn kāi shǐ?",
                "hi" to "naashta kitne baje hai?"
            )
        ),

        // ========== TRANSPORT (교통) ==========
        MultilingualPhrase(
            category = "transport",
            translations = mapOf(
                "en" to "Please call a taxi",
                "ko" to "택시 불러 주세요",
                "ja" to "タクシーを呼んでください",
                "zh" to "请帮我叫出租车",
                "es" to "Llame un taxi, por favor",
                "hi" to "कृपया टैक्सी बुलाइए"
            ),
            romanization = mapOf(
                "ko" to "taeksi bulleo juseyo",
                "ja" to "takushii wo yonde kudasai",
                "zh" to "qǐng bāng wǒ jiào chū zū chē",
                "hi" to "kripya taxi bulaiye"
            )
        ),
        MultilingualPhrase(
            category = "transport",
            translations = mapOf(
                "en" to "Please stop here",
                "ko" to "여기서 내려 주세요",
                "ja" to "ここで降ろしてください",
                "zh" to "请在这里停车",
                "es" to "Pare aquí, por favor",
                "hi" to "कृपया यहाँ रुकिए"
            ),
            romanization = mapOf(
                "ko" to "yeogiseo naeryeo juseyo",
                "ja" to "koko de oroshite kudasai",
                "zh" to "qǐng zài zhè lǐ tíng chē",
                "hi" to "kripya yahaan rukiye"
            )
        ),
        MultilingualPhrase(
            category = "transport",
            translations = mapOf(
                "en" to "To the airport, please",
                "ko" to "공항까지 가 주세요",
                "ja" to "空港までお願いします",
                "zh" to "请去机场",
                "es" to "Al aeropuerto, por favor",
                "hi" to "कृपया हवाई अड्डे तक"
            ),
            romanization = mapOf(
                "ko" to "gonghangkkaji ga juseyo",
                "ja" to "kuukou made onegai shimasu",
                "zh" to "qǐng qù jī chǎng",
                "hi" to "kripya hawai adde tak"
            )
        ),
        MultilingualPhrase(
            category = "transport",
            translations = mapOf(
                "en" to "Where does this bus go?",
                "ko" to "이 버스가 어디로 가요?",
                "ja" to "このバスはどこに行きますか？",
                "zh" to "这辆公交车去哪里？",
                "es" to "¿A dónde va este autobús?",
                "hi" to "यह बस कहाँ जाती है?"
            ),
            romanization = mapOf(
                "ko" to "i beoseuga eodiro gayo?",
                "ja" to "kono basu wa doko ni ikimasu ka?",
                "zh" to "zhè liàng gōng jiāo chē qù nǎ lǐ?",
                "hi" to "yeh bus kahaan jaati hai?"
            )
        ),
        MultilingualPhrase(
            category = "transport",
            translations = mapOf(
                "en" to "One ticket, please",
                "ko" to "표 한 장 주세요",
                "ja" to "切符を一枚ください",
                "zh" to "请给我一张票",
                "es" to "Un billete, por favor",
                "hi" to "एक टिकट दीजिए"
            ),
            romanization = mapOf(
                "ko" to "pyo han jang juseyo",
                "ja" to "kippu wo ichimai kudasai",
                "zh" to "qǐng gěi wǒ yī zhāng piào",
                "hi" to "ek ticket dijiye"
            )
        ),
        MultilingualPhrase(
            category = "transport",
            translations = mapOf(
                "en" to "Do I need to transfer?",
                "ko" to "환승해야 하나요?",
                "ja" to "乗り換えが必要ですか？",
                "zh" to "需要换乘吗？",
                "es" to "¿Necesito hacer transbordo?",
                "hi" to "क्या मुझे ट्रांसफर करना होगा?"
            ),
            romanization = mapOf(
                "ko" to "hwanseunghaeya hanayo?",
                "ja" to "norikae ga hitsuyou desu ka?",
                "zh" to "xū yào huàn chéng ma?",
                "hi" to "kya mujhe transfer karna hoga?"
            )
        ),

        // ========== EMERGENCY (긴급) ==========
        MultilingualPhrase(
            category = "emergency",
            translations = mapOf(
                "en" to "Help!",
                "ko" to "도와주세요!",
                "ja" to "助けて！",
                "zh" to "救命！",
                "es" to "¡Ayuda!",
                "hi" to "मदद करो!"
            ),
            romanization = mapOf(
                "ko" to "dowajuseyo!",
                "ja" to "tasukete!",
                "zh" to "jiù mìng!",
                "hi" to "madad karo!"
            )
        ),
        MultilingualPhrase(
            category = "emergency",
            translations = mapOf(
                "en" to "I need a doctor",
                "ko" to "의사가 필요해요",
                "ja" to "医者が必要です",
                "zh" to "我需要医生",
                "es" to "Necesito un médico",
                "hi" to "मुझे डॉक्टर चाहिए"
            ),
            romanization = mapOf(
                "ko" to "uisaga piryohaeyo",
                "ja" to "isha ga hitsuyou desu",
                "zh" to "wǒ xū yào yī shēng",
                "hi" to "mujhe doctor chahiye"
            )
        ),
        MultilingualPhrase(
            category = "emergency",
            translations = mapOf(
                "en" to "Please call the police",
                "ko" to "경찰을 불러주세요",
                "ja" to "警察を呼んでください",
                "zh" to "请叫警察",
                "es" to "Llame a la policía, por favor",
                "hi" to "कृपया पुलिस को बुलाइए"
            ),
            romanization = mapOf(
                "ko" to "gyeongchareul bulleojuseyo",
                "ja" to "keisatsu wo yonde kudasai",
                "zh" to "qǐng jiào jǐng chá",
                "hi" to "kripya police ko bulaiye"
            )
        ),
        MultilingualPhrase(
            category = "emergency",
            translations = mapOf(
                "en" to "I'm lost",
                "ko" to "길을 잃었어요",
                "ja" to "道に迷いました",
                "zh" to "我迷路了",
                "es" to "Estoy perdido",
                "hi" to "मैं खो गया हूँ"
            ),
            romanization = mapOf(
                "ko" to "gireul ilheosseoyo",
                "ja" to "michi ni mayoimashita",
                "zh" to "wǒ mí lù le",
                "hi" to "main kho gaya hoon"
            )
        ),
        MultilingualPhrase(
            category = "emergency",
            translations = mapOf(
                "en" to "I lost my phone",
                "ko" to "핸드폰을 잃어버렸어요",
                "ja" to "携帯をなくしました",
                "zh" to "我的手机丢了",
                "es" to "Perdí mi teléfono",
                "hi" to "मेरा फ़ोन खो गया"
            ),
            romanization = mapOf(
                "ko" to "haendeupon-eul ireobeoryeosseoyo",
                "ja" to "keitai wo nakushimashita",
                "zh" to "wǒ de shǒu jī diū le",
                "hi" to "mera phone kho gaya"
            )
        ),
        MultilingualPhrase(
            category = "emergency",
            translations = mapOf(
                "en" to "I lost my passport",
                "ko" to "여권을 잃어버렸어요",
                "ja" to "パスポートをなくしました",
                "zh" to "我的护照丢了",
                "es" to "Perdí mi pasaporte",
                "hi" to "मेरा पासपोर्ट खो गया"
            ),
            romanization = mapOf(
                "ko" to "yeogwon-eul ireobeoryeosseoyo",
                "ja" to "pasupooto wo nakushimashita",
                "zh" to "wǒ de hù zhào diū le",
                "hi" to "mera passport kho gaya"
            )
        ),

        // ========== COMMUNICATION (소통) ==========
        MultilingualPhrase(
            category = "communication",
            translations = mapOf(
                "en" to "Do you speak English?",
                "ko" to "영어 할 줄 아세요?",
                "ja" to "英語を話せますか？",
                "zh" to "你会说英语吗？",
                "es" to "¿Habla inglés?",
                "hi" to "क्या आप अंग्रेज़ी बोलते हैं?"
            ),
            romanization = mapOf(
                "ko" to "yeongeo hal jul aseyo?",
                "ja" to "eigo wo hanasemasu ka?",
                "zh" to "nǐ huì shuō yīng yǔ ma?",
                "hi" to "kya aap angrezi bolte hain?"
            )
        ),
        MultilingualPhrase(
            category = "communication",
            translations = mapOf(
                "en" to "Please speak slowly",
                "ko" to "천천히 말해 주세요",
                "ja" to "ゆっくり話してください",
                "zh" to "请说慢一点",
                "es" to "Hable más despacio, por favor",
                "hi" to "कृपया धीरे बोलिए"
            ),
            romanization = mapOf(
                "ko" to "cheoncheonhi malhae juseyo",
                "ja" to "yukkuri hanashite kudasai",
                "zh" to "qǐng shuō màn yī diǎn",
                "hi" to "kripya dheere boliye"
            )
        ),
        MultilingualPhrase(
            category = "communication",
            translations = mapOf(
                "en" to "Please say that again",
                "ko" to "다시 말해 주세요",
                "ja" to "もう一度言ってください",
                "zh" to "请再说一遍",
                "es" to "Repita eso, por favor",
                "hi" to "कृपया फिर से कहिए"
            ),
            romanization = mapOf(
                "ko" to "dasi malhae juseyo",
                "ja" to "mou ichido itte kudasai",
                "zh" to "qǐng zài shuō yī biàn",
                "hi" to "kripya phir se kahiye"
            )
        ),
        MultilingualPhrase(
            category = "communication",
            translations = mapOf(
                "en" to "I don't understand",
                "ko" to "모르겠어요",
                "ja" to "わかりません",
                "zh" to "我不明白",
                "es" to "No entiendo",
                "hi" to "मुझे समझ नहीं आया"
            ),
            romanization = mapOf(
                "ko" to "moreugesseoyo",
                "ja" to "wakarimasen",
                "zh" to "wǒ bù míng bai",
                "hi" to "mujhe samajh nahi aaya"
            )
        ),
        MultilingualPhrase(
            category = "communication",
            translations = mapOf(
                "en" to "Please write it down",
                "ko" to "적어 주세요",
                "ja" to "書いてください",
                "zh" to "请写下来",
                "es" to "Escríbalo, por favor",
                "hi" to "कृपया लिख दीजिए"
            ),
            romanization = mapOf(
                "ko" to "jeogeo juseyo",
                "ja" to "kaite kudasai",
                "zh" to "qǐng xiě xià lái",
                "hi" to "kripya likh dijiye"
            )
        ),
        MultilingualPhrase(
            category = "communication",
            translations = mapOf(
                "en" to "Can I take a picture?",
                "ko" to "사진 찍어도 돼요?",
                "ja" to "写真を撮ってもいいですか？",
                "zh" to "可以拍照吗？",
                "es" to "¿Puedo tomar una foto?",
                "hi" to "क्या मैं फ़ोटो ले सकता हूँ?"
            ),
            romanization = mapOf(
                "ko" to "sajin jjigeodo dwaeyo?",
                "ja" to "shashin wo tottemo ii desu ka?",
                "zh" to "kě yǐ pāi zhào ma?",
                "hi" to "kya main photo le sakta hoon?"
            )
        )
    )

    /**
     * 카테고리별 문장 필터링
     */
    fun getPhrasesByCategory(category: String): List<MultilingualPhrase> {
        return phrases.filter { it.category == category }
    }

    /**
     * 랜덤 문장 가져오기
     */
    fun getRandomPhrase(excludeIndex: Int = -1): Pair<Int, MultilingualPhrase> {
        val availableIndices = phrases.indices.filter { it != excludeIndex }
        val randomIndex = availableIndices.random()
        return randomIndex to phrases[randomIndex]
    }

    /**
     * 위젯 이름 다국어
     */
    object WidgetNames {
        fun getEnglishWidgetName(): String = when (Locale.getDefault().language) {
            "ko" -> "여행 영어"
            "ja" -> "旅行英語"
            "zh" -> "旅行英语"
            "es" -> "Inglés de viaje"
            "hi" -> "यात्रा अंग्रेज़ी"
            else -> "Travel English"
        }

        fun getJapaneseWidgetName(): String = when (Locale.getDefault().language) {
            "ko" -> "여행 일본어"
            "ja" -> "旅行日本語"
            "zh" -> "旅行日语"
            "es" -> "Japonés de viaje"
            "hi" -> "यात्रा जापानी"
            else -> "Travel Japanese"
        }

        fun getChineseWidgetName(): String = when (Locale.getDefault().language) {
            "ko" -> "여행 중국어"
            "ja" -> "旅行中国語"
            "zh" -> "旅行中文"
            "es" -> "Chino de viaje"
            "hi" -> "यात्रा चीनी"
            else -> "Travel Chinese"
        }

        fun getKoreanWidgetName(): String = when (Locale.getDefault().language) {
            "ko" -> "여행 한국어"
            "ja" -> "旅行韓国語"
            "zh" -> "旅行韩语"
            "es" -> "Coreano de viaje"
            "hi" -> "यात्रा कोरियाई"
            else -> "Travel Korean"
        }

        fun getSpanishWidgetName(): String = when (Locale.getDefault().language) {
            "ko" -> "여행 스페인어"
            "ja" -> "旅行スペイン語"
            "zh" -> "旅行西班牙语"
            "es" -> "Español de viaje"
            "hi" -> "यात्रा स्पेनिश"
            else -> "Travel Spanish"
        }

        fun getHindiWidgetName(): String = when (Locale.getDefault().language) {
            "ko" -> "여행 힌디어"
            "ja" -> "旅行ヒンディー語"
            "zh" -> "旅行印地语"
            "es" -> "Hindi de viaje"
            "hi" -> "यात्रा हिंदी"
            else -> "Travel Hindi"
        }
    }
}
