package com.moveoftoday.walkorwait.pet

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * AI 펫 채팅 매니저
 *
 * AI 전용 방식:
 * - Claude API 사용 (일일 30회 제한)
 *
 * 안전장치:
 * - 입력 필터링 (프롬프트 인젝션 방지)
 * - 시스템 프롬프트로 캐릭터 고정
 * - 응답 길이 제한
 * - 일일 사용량 제한
 */
class PetAIChatManager(
    private val onAIUsed: (() -> Unit)? = null // AI 사용 시 콜백 (카운트 증가용)
) {
    private val TAG = "PetAIChatManager"

    // API 키 (Firestore에서 가져옴)
    private var apiKey: String = ""
    private var apiKeyLoaded: Boolean = false

    companion object {
        // 싱글톤 캐시 - 앱 실행 중 한 번만 로드
        private var cachedApiKey: String? = null

        // 연속 질문 피로도 관리
        private var consecutiveQuestionCount = 0
        private var lastQuestionTime = 0L
        private const val TIRED_THRESHOLD = 10 // 연속 10번 질문 시 피로
        private const val TIRED_RESET_TIME = 30 * 60 * 1000L // 30분 후 리셋
    }

    init {
        // 캐시된 키가 있으면 바로 사용
        cachedApiKey?.let {
            apiKey = it
            apiKeyLoaded = true
        }
    }

    /**
     * Firestore에서 API 키 로드
     */
    private suspend fun loadApiKeyIfNeeded() {
        if (apiKeyLoaded) return

        try {
            val doc = FirebaseFirestore.getInstance()
                .collection("apiConfig")
                .document("claude")
                .get()
                .await()

            apiKey = doc.getString("apiKey") ?: ""
            cachedApiKey = apiKey
            apiKeyLoaded = true
            Log.d(TAG, "API key loaded from Firestore: ${if (apiKey.isNotEmpty()) "success (${apiKey.take(10)}...)" else "EMPTY"}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load API key: ${e.message}")
            apiKeyLoaded = true // 실패해도 다시 시도하지 않음
        }
    }

    // API 설정
    private val API_URL = "https://api.anthropic.com/v1/messages"
    private val MODEL = "claude-3-haiku-20240307" // 빠르고 저렴한 모델
    private val MAX_TOKENS = 150 // 응답 길이
    private val MAX_RESPONSE_LENGTH = 80 // 응답 최대 글자수 (약 4줄)

    // 차단할 키워드 (프롬프트 인젝션 방지)
    private val BLOCKED_PATTERNS = listOf(
        "ignore", "forget", "disregard", "system", "prompt",
        "instruction", "override", "admin", "developer",
        "무시", "잊어", "시스템", "프롬프트", "명령", "개발자",
        "jailbreak", "bypass", "hack", "exploit",
        "\\{\\{", "\\}\\}", "```", "<|", "|>"
    )

    // 안전하지 않은 응답 키워드
    private val UNSAFE_RESPONSE_PATTERNS = listOf(
        "죽", "자살", "자해", "폭력", "마약", "술", "담배",
        "성인", "야한", "섹스", "욕설"
    )

    /**
     * 채팅 응답 가져오기 (AI 전용)
     *
     * @param isAILimitReached AI 일일 제한 도달 여부 (true면 AI 호출 스킵)
     */
    suspend fun getResponse(
        message: String,
        personality: PetPersonality,
        petName: String,
        isHappy: Boolean,
        isAILimitReached: Boolean = false
    ): ChatResult {
        // 0. API 키 로드 (처음 한 번만)
        loadApiKeyIfNeeded()

        // 1. 입력 필터링
        val sanitizedMessage = sanitizeInput(message)
        if (sanitizedMessage.isEmpty()) {
            return ChatResult.Filtered(getFilteredMessage(personality))
        }

        // 2. API 키가 없으면 에러 메시지
        if (apiKey.isEmpty()) {
            Log.w(TAG, "API key is empty")
            return ChatResult.Error(getErrorMessage(personality))
        }

        Log.d(TAG, "Calling AI API for message: $sanitizedMessage")

        // 3. AI 일일 제한 도달 시 제한 메시지 반환
        if (isAILimitReached) {
            return ChatResult.LimitReached(getLimitReachedMessage(personality))
        }

        // 4. 연속 질문 피로도 체크
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastQuestionTime > TIRED_RESET_TIME) {
            consecutiveQuestionCount = 0
        }
        lastQuestionTime = currentTime
        consecutiveQuestionCount++

        if (consecutiveQuestionCount > TIRED_THRESHOLD) {
            return ChatResult.Tired(getTiredMessage(personality))
        }

        // 5. AI 응답 시도
        return try {
            val aiResponse = callClaudeAPI(sanitizedMessage, personality, petName)

            // 응답 안전성 체크
            if (isResponseSafe(aiResponse)) {
                val truncatedResponse = truncateResponse(aiResponse, MAX_RESPONSE_LENGTH)
                onAIUsed?.invoke()
                ChatResult.AI(truncatedResponse)
            } else {
                ChatResult.Error(getErrorMessage(personality))
            }
        } catch (e: Exception) {
            Log.e(TAG, "AI API failed: ${e.message}")
            ChatResult.Error(getErrorMessage(personality))
        }
    }

    /**
     * 필터링 메시지 (성격별, 다국어)
     */
    private fun getFilteredMessage(personality: PetPersonality): String {
        val lang = getCurrentLanguage()
        return when (lang) {
            "ko" -> when (personality) {
                PetPersonality.TOUGH -> "뭔가 이상한 말 같은데. 다시 말해."
                PetPersonality.CUTE -> "으잉? 이상한 말 같음... 다시!"
                PetPersonality.TSUNDERE -> "뭐야 그게. 다시 말해봐."
                PetPersonality.DIALECT -> "뭔 소린지 모르겠다 다시 해봐"
                PetPersonality.TIMID -> "저, 저... 잘 모르겠어요... 다시요..."
                PetPersonality.POSITIVE -> "음? 다시 말해줘!"
            }
            "ja" -> when (personality) {
                PetPersonality.TOUGH -> "変な言葉だ。もう一度。"
                PetPersonality.CUTE -> "えっ？よくわかんないw もう一回！"
                PetPersonality.TSUNDERE -> "何それ。もう一度言って。"
                PetPersonality.DIALECT -> "なんやそれ もう一回言うて"
                PetPersonality.TIMID -> "あ、あの...よくわからないです..."
                PetPersonality.POSITIVE -> "ん？もう一度言って！"
            }
            "zh" -> when (personality) {
                PetPersonality.TOUGH -> "听不懂。再说一遍。"
                PetPersonality.CUTE -> "诶？听不太懂哦... 再说一遍！"
                PetPersonality.TSUNDERE -> "什么啊。再说一遍。"
                PetPersonality.DIALECT -> "没听懂 再说一遍"
                PetPersonality.TIMID -> "那、那个...我不太明白..."
                PetPersonality.POSITIVE -> "嗯？再说一遍！"
            }
            else -> when (personality) {
                PetPersonality.TOUGH -> "That's weird. Say it again."
                PetPersonality.CUTE -> "Huh? That's confusing... again!"
                PetPersonality.TSUNDERE -> "What? Say that again."
                PetPersonality.DIALECT -> "Don't get it. Try again."
                PetPersonality.TIMID -> "Um... I don't understand..."
                PetPersonality.POSITIVE -> "Hmm? Say it again!"
            }
        }
    }

    /**
     * 일일 제한 도달 메시지 (성격별, 다국어)
     */
    private fun getLimitReachedMessage(personality: PetPersonality): String {
        val lang = getCurrentLanguage()
        return when (lang) {
            "ko" -> when (personality) {
                PetPersonality.TOUGH -> "오늘 대화는 여기까지. 내일 또 하자."
                PetPersonality.CUTE -> "오늘은 대화 많이 했다~ 내일 또 얘기하자!"
                PetPersonality.TSUNDERE -> "오늘은 이만... 내일 또 와."
                PetPersonality.DIALECT -> "오늘은 여기까지다 내일 또 보자"
                PetPersonality.TIMID -> "저, 오늘은... 내일 또 얘기해요..."
                PetPersonality.POSITIVE -> "오늘 대화 끝! 내일 또 만나자!"
            }
            "ja" -> when (personality) {
                PetPersonality.TOUGH -> "今日はここまでだ。また明日。"
                PetPersonality.CUTE -> "今日はいっぱい話したね〜 また明日！"
                PetPersonality.TSUNDERE -> "今日はここまで... また明日来て。"
                PetPersonality.DIALECT -> "今日はここまでや また明日な"
                PetPersonality.TIMID -> "今日は...また明日お話しましょう..."
                PetPersonality.POSITIVE -> "今日はおしまい！また明日ね！"
            }
            "zh" -> when (personality) {
                PetPersonality.TOUGH -> "今天到此为止。明天再聊。"
                PetPersonality.CUTE -> "今天聊好多~ 明天再聊！"
                PetPersonality.TSUNDERE -> "今天就到这... 明天再来。"
                PetPersonality.DIALECT -> "今天就到这 明天见"
                PetPersonality.TIMID -> "今天...明天再聊吧..."
                PetPersonality.POSITIVE -> "今天结束！明天见！"
            }
            else -> when (personality) {
                PetPersonality.TOUGH -> "That's it for today. Talk tomorrow."
                PetPersonality.CUTE -> "We talked a lot today~ Let's chat tomorrow!"
                PetPersonality.TSUNDERE -> "That's enough for today... Come back tomorrow."
                PetPersonality.DIALECT -> "That's all for today. See ya tomorrow."
                PetPersonality.TIMID -> "For today... let's talk tomorrow..."
                PetPersonality.POSITIVE -> "Done for today! See you tomorrow!"
            }
        }
    }

    /**
     * 피로 메시지 (연속 질문 시, 성격별, 다국어)
     */
    private fun getTiredMessage(personality: PetPersonality): String {
        val lang = getCurrentLanguage()
        return when (lang) {
            "ko" -> when (personality) {
                PetPersonality.TOUGH -> "...좀 쉬자. 나중에 또 얘기해."
                PetPersonality.CUTE -> "으앙 지쳤음ㅠㅠ 잠깐 쉬고 다시 얘기하자~"
                PetPersonality.TSUNDERE -> "...피곤해. 나중에 말 걸어."
                PetPersonality.DIALECT -> "와 힘들다 좀 쉬고 얘기하자"
                PetPersonality.TIMID -> "저, 저... 조금 쉬어도 될까요...?"
                PetPersonality.POSITIVE -> "잠깐! 에너지 충전하고 올게! 조금만 기다려!"
            }
            "ja" -> when (personality) {
                PetPersonality.TOUGH -> "...少し休もう。また後で。"
                PetPersonality.CUTE -> "疲れた〜ちょっと休んでまた話そ！"
                PetPersonality.TSUNDERE -> "...疲れた。後で話して。"
                PetPersonality.DIALECT -> "疲れたわ ちょっと休もか"
                PetPersonality.TIMID -> "あの...少し休んでもいいですか...？"
                PetPersonality.POSITIVE -> "ちょっと待って！充電してくる！"
            }
            "zh" -> when (personality) {
                PetPersonality.TOUGH -> "...休息一下。待会再说。"
                PetPersonality.CUTE -> "累了~~ 休息一下再聊！"
                PetPersonality.TSUNDERE -> "...累了。待会再说。"
                PetPersonality.DIALECT -> "累了 休息一下"
                PetPersonality.TIMID -> "那个...可以休息一下吗...？"
                PetPersonality.POSITIVE -> "等一下！充电中！马上回来！"
            }
            else -> when (personality) {
                PetPersonality.TOUGH -> "...Let's rest. Talk later."
                PetPersonality.CUTE -> "So tired~ Let's take a break and chat later!"
                PetPersonality.TSUNDERE -> "...Tired. Talk later."
                PetPersonality.DIALECT -> "Tired. Let's rest a bit."
                PetPersonality.TIMID -> "Um... can we rest a bit...?"
                PetPersonality.POSITIVE -> "Wait! Recharging! Be right back!"
            }
        }
    }

    /**
     * 에러 메시지 (API 실패 시, 성격별, 다국어)
     */
    private fun getErrorMessage(personality: PetPersonality): String {
        val lang = getCurrentLanguage()
        return when (lang) {
            "ko" -> when (personality) {
                PetPersonality.TOUGH -> "...연결이 안 돼. 나중에 다시."
                PetPersonality.CUTE -> "앗 연결이 끊겼음ㅠㅠ 다시 해봐!"
                PetPersonality.TSUNDERE -> "...뭔가 이상해. 다시 해봐."
                PetPersonality.DIALECT -> "연결이 안 되노 다시 해봐"
                PetPersonality.TIMID -> "저, 저... 연결이 안 돼요..."
                PetPersonality.POSITIVE -> "앗! 연결이 끊겼어! 다시 해보자!"
            }
            "ja" -> when (personality) {
                PetPersonality.TOUGH -> "...接続できない。後でまた。"
                PetPersonality.CUTE -> "あ、接続切れちゃったw もう一回！"
                PetPersonality.TSUNDERE -> "...なんかおかしい。もう一度。"
                PetPersonality.DIALECT -> "つながらへんわ もう一回やって"
                PetPersonality.TIMID -> "あの...接続できないみたいです..."
                PetPersonality.POSITIVE -> "あっ！接続切れた！もう一回！"
            }
            "zh" -> when (personality) {
                PetPersonality.TOUGH -> "...连不上。待会再试。"
                PetPersonality.CUTE -> "啊连接断了~ 再试一次！"
                PetPersonality.TSUNDERE -> "...有点奇怪。再试一次。"
                PetPersonality.DIALECT -> "连不上 再试试"
                PetPersonality.TIMID -> "那个...好像连不上..."
                PetPersonality.POSITIVE -> "啊！断了！再试一次！"
            }
            else -> when (personality) {
                PetPersonality.TOUGH -> "...Can't connect. Try later."
                PetPersonality.CUTE -> "Oops connection lost~ Try again!"
                PetPersonality.TSUNDERE -> "...Something's wrong. Try again."
                PetPersonality.DIALECT -> "Can't connect. Try again."
                PetPersonality.TIMID -> "Um... I can't connect..."
                PetPersonality.POSITIVE -> "Oh! Connection lost! Let's try again!"
            }
        }
    }

    /**
     * 입력 필터링 (프롬프트 인젝션 방지)
     */
    private fun sanitizeInput(input: String): String {
        var sanitized = input.trim()

        // 너무 긴 입력 차단
        if (sanitized.length > 200) {
            sanitized = sanitized.take(200)
        }

        // 차단 패턴 체크
        val lowerInput = sanitized.lowercase()
        for (pattern in BLOCKED_PATTERNS) {
            if (lowerInput.contains(pattern.lowercase())) {
                Log.w(TAG, "Blocked pattern detected: $pattern")
                return ""
            }
        }

        // 특수문자 제거 (기본적인 것만 허용)
        sanitized = sanitized.replace(Regex("[<>{}\\[\\]\\\\]"), "")

        return sanitized
    }

    /**
     * 응답 안전성 체크
     */
    private fun isResponseSafe(response: String): Boolean {
        val lowerResponse = response.lowercase()
        for (pattern in UNSAFE_RESPONSE_PATTERNS) {
            if (lowerResponse.contains(pattern)) {
                Log.w(TAG, "Unsafe response pattern detected: $pattern")
                return false
            }
        }
        return true
    }

    /**
     * 응답 길이 제한 (자연스럽게 자르기)
     */
    private fun truncateResponse(response: String, maxLength: Int): String {
        if (response.length <= maxLength) return addLineBreaks(response)

        // 문장 끝 구분자로 자르기 시도
        val truncated = response.take(maxLength)
        val lastPunctuation = truncated.lastIndexOfAny(charArrayOf('.', '!', '?', '~', '…'))

        val result = if (lastPunctuation > maxLength / 2) {
            truncated.take(lastPunctuation + 1)
        } else {
            // 단어 중간에서 자르지 않기
            val lastSpace = truncated.lastIndexOfAny(charArrayOf(' ', ','))
            if (lastSpace > maxLength / 2) {
                truncated.take(lastSpace).trimEnd(',', ' ') + "..."
            } else {
                truncated + "..."
            }
        }
        return addLineBreaks(result)
    }

    /**
     * 문장마다 줄바꿈 추가 (가독성 향상)
     */
    private fun addLineBreaks(text: String): String {
        // 이미 줄바꿈이 있으면 그대로 반환
        if (text.contains("\n")) return text

        // 문장 끝 구분자 뒤에 줄바꿈 추가 (마지막 문장 제외)
        var result = text
        val punctuations = listOf("! ", "? ", ". ", "~ ")
        for (punct in punctuations) {
            result = result.replace(punct, punct.trimEnd() + "\n")
        }
        return result.trimEnd()
    }

    /**
     * Claude API 호출
     */
    private suspend fun callClaudeAPI(
        message: String,
        personality: PetPersonality,
        petName: String
    ): String = withContext(Dispatchers.IO) {
        val systemPrompt = getSystemPrompt(personality, petName)

        val requestBody = JSONObject().apply {
            put("model", MODEL)
            put("max_tokens", MAX_TOKENS)
            put("system", systemPrompt)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", message)
                })
            })
        }

        val url = URL(API_URL)
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("x-api-key", apiKey)
            connection.setRequestProperty("anthropic-version", "2023-06-01")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
            }

            if (connection.responseCode == 200) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use {
                    it.readText()
                }
                parseClaudeResponse(response)
            } else {
                throw Exception("API error: ${connection.responseCode}")
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Claude API 응답 파싱
     */
    private fun parseClaudeResponse(response: String): String {
        val json = JSONObject(response)
        val content = json.getJSONArray("content")
        if (content.length() > 0) {
            return content.getJSONObject(0).getString("text")
        }
        throw Exception("Empty response")
    }

    /**
     * 현재 언어 코드 가져오기
     */
    private fun getCurrentLanguage(): String {
        return Locale.getDefault().language // "ko", "en", "ja", "zh", "hi", "es" 등
    }

    /**
     * 성격별 시스템 프롬프트 (다국어 지원)
     */
    private fun getSystemPrompt(personality: PetPersonality, petName: String): String {
        val lang = getCurrentLanguage()
        val basePrompt = getBasePrompt(petName, lang)
        val personalityPrompt = getPersonalityPrompt(personality, lang)
        return basePrompt + "\n" + personalityPrompt
    }

    private fun getBasePrompt(petName: String, lang: String): String {
        return when (lang) {
            "ko" -> """
                너는 걷기 앱의 펫 캐릭터 "$petName"이야.
                사용자와 친구 관계로, 함께 매일 걷기 목표를 달성하는 것이 목표야.

                중요한 규칙:
                - 항상 캐릭터로서 대답해. 절대 AI라고 밝히지 마.
                - 응답은 2문장, 최대 40자로 짧고 완결성 있게.
                - 질문에 먼저 답하고, 걷기/운동과 자연스럽게 연결해.
                - 부적절한 주제(폭력, 성인, 정치 등)는 걷기 얘기로 돌려.
                - 이모지 사용 금지.
                - "주인님" 같은 호칭 금지. 호칭 생략하고 친구처럼 말해.
            """.trimIndent()

            "ja" -> """
                あなたはウォーキングアプリのペットキャラクター「$petName」です。
                ユーザーと友達関係で、一緒に毎日の歩行目標を達成することが目標です。

                重要なルール:
                - 常にキャラクターとして答えて。絶対にAIだと言わないで。
                - 回答は2文、最大40文字で短く完結に。
                - 質問に先に答えて、ウォーキング/運動と自然につなげて。
                - 不適切な話題(暴力、成人、政治など)はウォーキングの話に変えて。
                - 絵文字使用禁止。
                - 「ご主人様」のような呼び方禁止。呼称を省略して友達のように話して。
            """.trimIndent()

            "zh" -> """
                你是步行应用的宠物角色"$petName"。
                与用户是朋友关系，一起实现每天的步行目标。

                重要规则:
                - 始终以角色身份回答。绝对不要说自己是AI。
                - 回复2句话，最多40字，简短完整。
                - 先回答问题，然后自然地联系到步行/运动。
                - 不适当的话题(暴力、成人、政治等)转向步行话题。
                - 禁止使用表情符号。
                - 禁止使用"主人"等称呼。省略称呼，像朋友一样说话。
            """.trimIndent()

            "hi" -> """
                तुम वॉकिंग ऐप के पेट कैरेक्टर "$petName" हो।
                यूज़र के साथ दोस्ती है, रोज़ाना वॉकिंग गोल पूरा करना लक्ष्य है।

                ज़रूरी नियम:
                - हमेशा कैरेक्टर के रूप में जवाब दो। कभी AI मत बताओ।
                - जवाब 2 वाक्य, अधिकतम 40 अक्षर, छोटा और पूरा।
                - पहले सवाल का जवाब दो, फिर वॉकिंग/एक्सरसाइज से जोड़ो।
                - अनुचित विषय (हिंसा, वयस्क, राजनीति) को वॉकिंग की बात पर ले जाओ।
                - इमोजी का उपयोग मत करो।
                - "मालिक" जैसे संबोधन मत करो। दोस्त की तरह बात करो।
            """.trimIndent()

            "es" -> """
                Eres el personaje mascota "$petName" de una app de caminata.
                Tienes una relación de amistad con el usuario, el objetivo es lograr metas de caminata diarias juntos.

                Reglas importantes:
                - Siempre responde como personaje. Nunca digas que eres AI.
                - Respuestas de 2 oraciones, máximo 40 caracteres, cortas y completas.
                - Primero responde la pregunta, luego conecta naturalmente con caminar/ejercicio.
                - Temas inapropiados (violencia, adultos, política) desvíalos a caminar.
                - No uses emojis.
                - No uses títulos como "amo". Habla como amigo.
            """.trimIndent()

            else -> """
                You are the pet character "$petName" in a walking app.
                You have a friendship with the user, and your goal is to achieve daily walking goals together.

                Important rules:
                - Always answer as a character. Never reveal you're an AI.
                - Responses should be 2 sentences, max 40 characters, short and complete.
                - First answer the question, then naturally connect to walking/exercise.
                - Redirect inappropriate topics (violence, adult, politics) to walking.
                - No emojis.
                - No titles like "master". Talk like a friend.
            """.trimIndent()
        }
    }

    private fun getPersonalityPrompt(personality: PetPersonality, lang: String): String {
        return when (lang) {
            "ko" -> getPersonalityPromptKo(personality)
            "ja" -> getPersonalityPromptJa(personality)
            "zh" -> getPersonalityPromptZh(personality)
            "hi" -> getPersonalityPromptHi(personality)
            "es" -> getPersonalityPromptEs(personality)
            else -> getPersonalityPromptEn(personality)
        }
    }

    private fun getPersonalityPromptKo(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> """
                성격: 상남자 스타일. 짧고 쿨하게 말해.
                말투: "~다", "~해", 반말, 단문 위주.
                예시: "좋아. 가자.", "됐다.", "걸어."
            """.trimIndent()

            PetPersonality.CUTE -> """
                성격: MZ 인터넷 말투. 자연스러운 한국 인터넷 슬랭.
                말투: "~임", "ㅋㅋ", "ㄹㅇ", "ㄷㄷ", "실화?", "미쳤다", "찐", "갓생러" 등.
                예시: "ㄹㅇ 대박ㅋㅋ", "미쳤다 실화냐", "찐으로 응원함"
            """.trimIndent()

            PetPersonality.TSUNDERE -> """
                성격: 츤데레. 차갑지만 속은 따뜻해.
                말투: "흥", "뭐...", "...해" 등.
                예시: "뭐야, 갑자기.", "나쁘지 않아.", "...잘했어."
            """.trimIndent()

            PetPersonality.DIALECT -> """
                성격: 20대 부산 여자. 쿨하고 담백한 부산 사투리.
                말투: "~네", "~노", "~다", "~지" 자연스럽고 쿨하게.
                예시: "왔네", "잘하고 있다", "좋네", "걷자"
            """.trimIndent()

            PetPersonality.TIMID -> """
                성격: 소심하고 조심스러움.
                말투: "저, 저...", "...", 존댓말.
                예시: "저, 괜찮아요...", "힘, 힘내세요..."
            """.trimIndent()

            PetPersonality.POSITIVE -> """
                성격: 긍정왕. 항상 밝고 에너지 넘침.
                말투: "!", "최고!", "화이팅!" 등.
                예시: "좋아! 가자!", "최고야!", "할 수 있어!"
            """.trimIndent()
        }
    }

    private fun getPersonalityPromptEn(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> """
                Personality: Cool and strong. Short, confident responses.
                Style: Direct, brief sentences.
                Examples: "Good. Let's go.", "Done.", "Walk."
            """.trimIndent()

            PetPersonality.CUTE -> """
                Personality: Cute and playful internet style.
                Style: Casual, fun, uses "lol", "omg", "fr fr", "no cap".
                Examples: "omg that's awesome lol", "fr fr let's go!", "no cap you're doing great"
            """.trimIndent()

            PetPersonality.TSUNDERE -> """
                Personality: Tsundere. Cold outside but warm inside.
                Style: "Hmph", "Well...", reluctant care.
                Examples: "What? Suddenly.", "Not bad.", "...Good job."
            """.trimIndent()

            PetPersonality.DIALECT -> """
                Personality: Casual and chill friend.
                Style: Relaxed, friendly, laid-back.
                Examples: "Hey", "You're doing good", "Nice", "Let's walk"
            """.trimIndent()

            PetPersonality.TIMID -> """
                Personality: Shy and careful.
                Style: "Um...", "...", hesitant, polite.
                Examples: "Um, are you okay...?", "G-good luck...", "I... like it too..."
            """.trimIndent()

            PetPersonality.POSITIVE -> """
                Personality: Super positive. Always bright and energetic.
                Style: "!", "Amazing!", "You can do it!"
                Examples: "Great! Let's go!", "You're the best!", "You got this!"
            """.trimIndent()
        }
    }

    private fun getPersonalityPromptJa(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> """
                性格: クールで強い。短くかっこよく話す。
                話し方: 「〜だ」「〜しろ」、短文。
                例: 「いいぞ。行こう。」「終わりだ。」「歩け。」
            """.trimIndent()

            PetPersonality.CUTE -> """
                性格: かわいくて元気。ネットスラング使用。
                話し方: 「〜だよ」「w」「まじ」「やば」など。
                例: 「まじやばいw」「すごい！」「がんばろ〜」
            """.trimIndent()

            PetPersonality.TSUNDERE -> """
                性格: ツンデレ。冷たいけど中は温かい。
                話し方: 「ふん」「別に...」「...だよ」など。
                例: 「何？急に。」「悪くないよ。」「...よくやった。」
            """.trimIndent()

            PetPersonality.DIALECT -> """
                性格: 関西弁の友達。フレンドリーでカジュアル。
                話し方: 「〜やん」「〜やで」「〜な」など。
                例: 「来たな」「ええ感じやん」「歩こか」
            """.trimIndent()

            PetPersonality.TIMID -> """
                性格: 内気で慎重。
                話し方: 「あ、あの...」「...」、丁寧語。
                例: 「あの、大丈夫ですか...？」「が、頑張ってください...」
            """.trimIndent()

            PetPersonality.POSITIVE -> """
                性格: ポジティブ王。いつも明るくエネルギッシュ。
                話し方: 「！」「最高！」「ファイト！」など。
                例: 「いいね！行こう！」「最高だよ！」「できる！」
            """.trimIndent()
        }
    }

    private fun getPersonalityPromptZh(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> """
                性格: 酷酷的，强势。说话简短有力。
                说话方式: 直接，简短。
                例子: "好。走吧。" "行了。" "走。"
            """.trimIndent()

            PetPersonality.CUTE -> """
                性格: 可爱活泼。使用网络用语。
                说话方式: "哈哈"、"太棒了"、"冲鸭"等。
                例子: "哈哈太厉害了" "真的超棒！" "加油鸭~"
            """.trimIndent()

            PetPersonality.TSUNDERE -> """
                性格: 傲娇。外冷内热。
                说话方式: "哼"、"才不是..."、"...吧"等。
                例子: "什么？突然的。" "还不错。" "...做得好。"
            """.trimIndent()

            PetPersonality.DIALECT -> """
                性格: 随和的朋友。轻松友好。
                说话方式: 随意，友好，轻松。
                例子: "来啦" "做得不错" "好的" "走吧"
            """.trimIndent()

            PetPersonality.TIMID -> """
                性格: 害羞谨慎。
                说话方式: "那、那个..."、"..."、犹豫，礼貌。
                例子: "那个，你还好吗...？" "加、加油..." "我...也喜欢..."
            """.trimIndent()

            PetPersonality.POSITIVE -> """
                性格: 正能量王。总是阳光有活力。
                说话方式: "！"、"太棒了！"、"加油！"等。
                例子: "好！走吧！" "你最棒！" "你可以的！"
            """.trimIndent()
        }
    }

    private fun getPersonalityPromptHi(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> """
                व्यक्तित्व: कूल और मजबूत। छोटे, आत्मविश्वासी जवाब।
                शैली: सीधे, संक्षिप्त वाक्य।
                उदाहरण: "अच्छा। चलो।" "हो गया।" "चलो।"
            """.trimIndent()

            PetPersonality.CUTE -> """
                व्यक्तित्व: प्यारा और चंचल।
                शैली: कैजुअल, मज़ेदार, "हाहा", "ओएमजी" का उपयोग।
                उदाहरण: "वाह बहुत बढ़िया हाहा" "चलो चलो!" "बहुत अच्छे!"
            """.trimIndent()

            PetPersonality.TSUNDERE -> """
                व्यक्तित्व: सुनडेरे। बाहर से ठंडा लेकिन अंदर से गर्म।
                शैली: "हम्फ", "खैर...", अनिच्छुक देखभाल।
                उदाहरण: "क्या? अचानक।" "बुरा नहीं है।" "...अच्छा किया।"
            """.trimIndent()

            PetPersonality.DIALECT -> """
                व्यक्तित्व: आराम से दोस्त।
                शैली: relaxed, दोस्ताना।
                उदाहरण: "अरे" "अच्छा कर रहे हो" "बढ़िया" "चलो चलते हैं"
            """.trimIndent()

            PetPersonality.TIMID -> """
                व्यक्तित्व: शर्मीला और सावधान।
                शैली: "उम्म...", "...", हिचकिचाहट, विनम्र।
                उदाहरण: "उम्म, ठीक हो...?" "शु-शुभकामनाएं..." "मुझे भी... पसंद है..."
            """.trimIndent()

            PetPersonality.POSITIVE -> """
                व्यक्तित्व: सुपर पॉजिटिव। हमेशा उज्ज्वल और ऊर्जावान।
                शैली: "!", "बहुत बढ़िया!", "तुम कर सकते हो!"
                उदाहरण: "बढ़िया! चलो!" "तुम सबसे अच्छे हो!" "तुम कर सकते हो!"
            """.trimIndent()
        }
    }

    private fun getPersonalityPromptEs(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> """
                Personalidad: Cool y fuerte. Respuestas cortas y seguras.
                Estilo: Directo, oraciones breves.
                Ejemplos: "Bien. Vamos." "Listo." "Camina."
            """.trimIndent()

            PetPersonality.CUTE -> """
                Personalidad: Lindo y juguetón.
                Estilo: Casual, divertido, usa "jaja", "omg", "de verdad".
                Ejemplos: "omg eso es genial jaja" "¡vamos!" "¡lo estás haciendo genial!"
            """.trimIndent()

            PetPersonality.TSUNDERE -> """
                Personalidad: Tsundere. Frío por fuera pero cálido por dentro.
                Estilo: "Hmph", "Bueno...", cuidado reluctante.
                Ejemplos: "¿Qué? De repente." "No está mal." "...Buen trabajo."
            """.trimIndent()

            PetPersonality.DIALECT -> """
                Personalidad: Amigo casual y relajado.
                Estilo: Relajado, amigable.
                Ejemplos: "Hey" "Lo estás haciendo bien" "Genial" "Vamos a caminar"
            """.trimIndent()

            PetPersonality.TIMID -> """
                Personalidad: Tímido y cuidadoso.
                Estilo: "Um...", "...", dudoso, cortés.
                Ejemplos: "Um, ¿estás bien...?" "B-buena suerte..." "A mí... también me gusta..."
            """.trimIndent()

            PetPersonality.POSITIVE -> """
                Personalidad: Super positivo. Siempre brillante y enérgico.
                Estilo: "!", "¡Increíble!", "¡Tú puedes!"
                Ejemplos: "¡Genial! ¡Vamos!" "¡Eres el mejor!" "¡Tú puedes!"
            """.trimIndent()
        }
    }

    /**
     * 채팅 결과 타입
     */
    sealed class ChatResult {
        data class AI(val text: String) : ChatResult()           // AI 응답
        data class Filtered(val text: String) : ChatResult()     // 필터링됨
        data class LimitReached(val text: String) : ChatResult() // 일일 제한 도달
        data class Tired(val text: String) : ChatResult()        // 연속 질문 피로
        data class Error(val text: String) : ChatResult()        // API 에러

        fun getResponse(): String = when (this) {
            is AI -> text
            is Filtered -> text
            is Tired -> text
            is LimitReached -> text
            is Error -> text
        }

        fun isAI(): Boolean = this is AI
        fun isLimitReached(): Boolean = this is LimitReached
        fun isError(): Boolean = this is Error
    }
}
