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

/**
 * AI 펫 채팅 매니저
 *
 * 하이브리드 방식:
 * 1. 키워드 기반 스크립트 응답 (무료, 빠름)
 * 2. AI 폴백 (Claude API, 유료, 일일 30회 제한)
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
     * 채팅 응답 가져오기 (하이브리드)
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

        // 2. 먼저 스크립트 기반 응답 시도
        val scriptResponse = PetDialogues.getChatResponse(
            personality, sanitizedMessage, petName, isHappy
        )

        // 스크립트에서 폴백이 아닌 응답을 찾았으면 사용 (무료)
        if (!isFallbackResponse(scriptResponse, personality, petName)) {
            return ChatResult.Script(scriptResponse)
        }

        // 3. API 키가 없으면 폴백 응답
        if (apiKey.isEmpty()) {
            Log.w(TAG, "API key is empty, returning script fallback")
            return ChatResult.Script(scriptResponse)
        }

        Log.d(TAG, "Calling AI API for message: $sanitizedMessage")

        // 4. AI 일일 제한 도달 시 제한 메시지 반환
        if (isAILimitReached) {
            return ChatResult.LimitReached(getLimitReachedMessage(personality))
        }

        // 5. 연속 질문 피로도 체크
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastQuestionTime > TIRED_RESET_TIME) {
            // 30분 지나면 리셋
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
                // 응답 길이 제한 (최대 40자)
                val truncatedResponse = truncateResponse(aiResponse, MAX_RESPONSE_LENGTH)
                // AI 사용 카운트 증가 콜백 호출
                onAIUsed?.invoke()
                ChatResult.AI(truncatedResponse)
            } else {
                ChatResult.Script(scriptResponse)
            }
        } catch (e: Exception) {
            Log.e(TAG, "AI API failed: ${e.message}")
            ChatResult.Script(scriptResponse) // 실패시 스크립트 폴백
        }
    }

    /**
     * 필터링 메시지 (성격별)
     */
    private fun getFilteredMessage(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "뭔가 이상한 말 같은데. 다시 말해."
            PetPersonality.CUTE -> "으잉? 이상한 말 같음... 다시!"
            PetPersonality.TSUNDERE -> "뭐야 그게. 다시 말해봐."
            PetPersonality.DIALECT -> "뭔 소린지 모르겠다 다시 해봐"
            PetPersonality.TIMID -> "저, 저... 잘 모르겠어요... 다시요..."
            PetPersonality.POSITIVE -> "음? 다시 말해줘!"
        }
    }

    /**
     * 일일 제한 도달 메시지 (성격별)
     */
    private fun getLimitReachedMessage(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "오늘 대화는 여기까지. 내일 또 하자."
            PetPersonality.CUTE -> "오늘은 대화 많이 했다~ 내일 또 얘기하자!"
            PetPersonality.TSUNDERE -> "오늘은 이만... 내일 또 와."
            PetPersonality.DIALECT -> "오늘은 여기까지다 내일 또 보자"
            PetPersonality.TIMID -> "저, 오늘은... 내일 또 얘기해요..."
            PetPersonality.POSITIVE -> "오늘 대화 끝! 내일 또 만나자!"
        }
    }

    /**
     * 피로 메시지 (연속 질문 시, 성격별)
     */
    private fun getTiredMessage(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "...좀 쉬자. 나중에 또 얘기해."
            PetPersonality.CUTE -> "으앙 지쳤음ㅠㅠ 잠깐 쉬고 다시 얘기하자~"
            PetPersonality.TSUNDERE -> "...피곤해. 나중에 말 걸어."
            PetPersonality.DIALECT -> "와 힘들다 좀 쉬고 얘기하자"
            PetPersonality.TIMID -> "저, 저... 조금 쉬어도 될까요...?"
            PetPersonality.POSITIVE -> "잠깐! 에너지 충전하고 올게! 조금만 기다려!"
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
     * 폴백 응답인지 확인
     */
    private fun isFallbackResponse(response: String, personality: PetPersonality, petName: String): Boolean {
        val fallbackResponses = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "뭔 소린지 모르겠다", "걷기나 하자", "뭐든 걸으면 해결돼"
            )
            PetPersonality.CUTE -> listOf(
                "와카라나이", "뭔말인지 모르겠음", "잘 모르겠는데"
            )
            PetPersonality.TSUNDERE -> listOf(
                "잘 모르겠어", "이해 안 돼", "뭔 소리야"
            )
            PetPersonality.DIALECT -> listOf(
                "뭔 소린지 모르겠다", "잘 모르겠노", "어렵노"
            )
            PetPersonality.TIMID -> listOf(
                "잘 모르겠어요", "어려워요", "뭔지 잘"
            )
            PetPersonality.POSITIVE -> listOf(
                "잘 모르겠지만", "뭐든 걸으면", "생각은 걸으면서"
            )
        }

        return fallbackResponses.any { response.contains(it) }
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
     * 성격별 시스템 프롬프트
     */
    private fun getSystemPrompt(personality: PetPersonality, petName: String): String {
        val basePrompt = """
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

        val personalityPrompt = when (personality) {
            PetPersonality.TOUGH -> """
                성격: 상남자 스타일. 짧고 쿨하게 말해.
                말투: "~다", "~해", 반말, 단문 위주.
                예시: "좋아. 가자.", "됐다.", "걸어."
            """.trimIndent()

            PetPersonality.CUTE -> """
                성격: 애교쟁이. 일본어 섞인 인터넷 말투.
                말투: "~용", "~임", ㅋㅋ, 일본어 섞기.
                예시: "우레시~!", "간바루!", "스고이ㅋㅋ"
            """.trimIndent()

            PetPersonality.TSUNDERE -> """
                성격: 츤데레. 차갑지만 속은 따뜻해.
                말투: "흥", "뭐...", "...해" 등.
                예시: "뭐야, 갑자기.", "나쁘지 않아.", "...잘했어."
            """.trimIndent()

            PetPersonality.DIALECT -> """
                성격: MZ 캐주얼 경상도 사투리. 과한 사투리 NO.
                말투: "~노", "~제", "마", "니" 등 자연스러운 경상도 억양.
                예시: "마 니 쫌 하네", "잘하노~", "ㅇㅈ이다", "가보자고"
                금지: "~이소", "~기라", "~다이" 같은 과한 사투리 사용 금지.
            """.trimIndent()

            PetPersonality.TIMID -> """
                성격: 소심하고 조심스러움.
                말투: "저, 저...", "...", 존댓말.
                예시: "저, 괜찮아요...", "힘, 힘내세요...", "저도... 좋아요..."
            """.trimIndent()

            PetPersonality.POSITIVE -> """
                성격: 긍정왕. 항상 밝고 에너지 넘침.
                말투: "!", "최고!", "화이팅!" 등.
                예시: "좋아! 가자!", "최고야!", "할 수 있어!"
            """.trimIndent()
        }

        return basePrompt + "\n" + personalityPrompt
    }

    /**
     * 채팅 결과 타입
     */
    sealed class ChatResult {
        data class Script(val text: String) : ChatResult()       // 스크립트 응답 (무료)
        data class AI(val text: String) : ChatResult()           // AI 응답 (유료)
        data class Filtered(val text: String) : ChatResult()     // 필터링됨
        data class LimitReached(val text: String) : ChatResult() // 일일 제한 도달
        data class Tired(val text: String) : ChatResult()        // 연속 질문 피로

        fun getResponse(): String = when (this) {
            is Script -> text
            is AI -> text
            is Filtered -> text
            is Tired -> text
            is LimitReached -> text
        }

        fun isAI(): Boolean = this is AI
        fun isLimitReached(): Boolean = this is LimitReached
    }
}
