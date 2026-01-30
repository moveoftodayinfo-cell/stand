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
 * AI 명언 생성 매니저
 *
 * 앱 시작 시 성격에 맞는 명언 5개를 AI로 생성하고 캐시
 * 메인 화면 말풍선에서 사용
 */
object PetAIQuoteManager {
    private val TAG = "PetAIQuoteManager"

    // API 설정
    private const val API_URL = "https://api.anthropic.com/v1/messages"
    private const val MODEL = "claude-3-haiku-20240307"
    private const val MAX_TOKENS = 500

    // 캐시된 명언
    private var cachedQuotes: MutableMap<PetPersonality, List<String>> = mutableMapOf()
    private var isLoading = false

    // API 키
    private var apiKey: String = ""
    private var apiKeyLoaded = false

    // 기본 명언 (AI 실패 시 폴백) - 실제 유명인 명언, 성격별 말투
    private val defaultQuotes = mapOf(
        // 상남자: 짧고 단호하게
        PetPersonality.TOUGH to listOf(
            "시작이 반이다 -아리스토텔레스",
            "천 리 길도 한 걸음부터다 -노자",
            "멈추지 마. 느려도 된다 -공자",
            "할 수 있다고 믿어라 -헨리 포드",
            "걷는 게 최고의 약이다 -히포크라테스"
        ),
        // MZ: ㅋㅋ 붙이고 가볍게
        PetPersonality.CUTE to listOf(
            "시작이 반이래ㅋ -아리스토텔레스",
            "천 리 길도 한 걸음부터래ㅋ -노자",
            "느려도 멈추지만 않으면 된대ㅋ -공자",
            "걷기가 최고의 운동이래ㅋ -히포크라테스",
            "생각하는 대로 살라래ㅋ -폴 발레리"
        ),
        // 츤데레: ...으로 뜸 들이기
        PetPersonality.TSUNDERE to listOf(
            "뭐... 시작이 반이래 -아리스토텔레스",
            "흥, 천 리 길도 한 걸음부터래 -노자",
            "...느려도 멈추지만 않으면 된대 -공자",
            "뭐, 걷는 게 최고 운동이래 -히포크라테스",
            "...위대한 일은 작게 시작한대 -피터 드러커"
        ),
        // 사투리: 20대 부산 여자, 쿨하게
        PetPersonality.DIALECT to listOf(
            "시작이 반이래 -아리스토텔레스",
            "천 리 길도 한 걸음부터라네 -노자",
            "느려도 멈추지만 않으면 된다 -공자",
            "걷기가 최고 운동이래 -히포크라테스",
            "오늘 할 일 미루지 말래 -벤자민 프랭클린"
        ),
        // 소심: 존댓말 + ...
        PetPersonality.TIMID to listOf(
            "저... 시작이 반이래요 -아리스토텔레스",
            "저, 천 리 길도 한 걸음부터래요... -노자",
            "느려도 멈추지만 않으면 된대요... -공자",
            "저... 걷기가 최고 운동이래요 -히포크라테스",
            "작은 기회가 큰 일의 시작이래요... -데모스테네스"
        ),
        // 긍정: ! 붙여서 밝게
        PetPersonality.POSITIVE to listOf(
            "시작이 반이래! -아리스토텔레스",
            "천 리 길도 한 걸음부터래! -노자",
            "느려도 멈추지만 않으면 돼! -공자",
            "걷기가 최고의 운동이래! -히포크라테스",
            "오늘 심은 나무가 내일 그늘이 돼! -속담"
        )
    )

    /**
     * API 키 로드
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
            apiKeyLoaded = true
            Log.d(TAG, "API key loaded: ${if (apiKey.isNotEmpty()) "success" else "EMPTY"}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load API key: ${e.message}")
            apiKeyLoaded = true
        }
    }

    /**
     * 명언 가져오기 (캐시에서)
     */
    fun getQuote(personality: PetPersonality): String {
        val quotes = cachedQuotes[personality] ?: defaultQuotes[personality] ?: listOf("오늘도 힘내자!")
        return quotes.random()
    }

    /**
     * 모든 명언 가져오기
     */
    fun getAllQuotes(personality: PetPersonality): List<String> {
        return cachedQuotes[personality] ?: defaultQuotes[personality] ?: listOf("오늘도 힘내자!")
    }

    /**
     * 명언이 로드되었는지 확인
     */
    fun isLoaded(personality: PetPersonality): Boolean {
        return cachedQuotes.containsKey(personality)
    }

    /**
     * 앱 시작 시 명언 생성 (백그라운드)
     */
    suspend fun generateQuotes(personality: PetPersonality, petName: String) {
        if (isLoading || cachedQuotes.containsKey(personality)) return
        isLoading = true

        try {
            loadApiKeyIfNeeded()

            if (apiKey.isEmpty()) {
                Log.w(TAG, "API key empty, using default quotes")
                cachedQuotes[personality] = defaultQuotes[personality] ?: listOf()
                return
            }

            Log.d(TAG, "Generating quotes for $personality")
            val quotes = callClaudeForQuotes(personality, petName)

            if (quotes.isNotEmpty()) {
                cachedQuotes[personality] = quotes
                Log.d(TAG, "Generated ${quotes.size} quotes for $personality")
            } else {
                cachedQuotes[personality] = defaultQuotes[personality] ?: listOf()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate quotes: ${e.message}")
            cachedQuotes[personality] = defaultQuotes[personality] ?: listOf()
        } finally {
            isLoading = false
        }
    }

    /**
     * Claude API로 명언 생성
     */
    private suspend fun callClaudeForQuotes(
        personality: PetPersonality,
        petName: String
    ): List<String> = withContext(Dispatchers.IO) {
        val prompt = getQuotePrompt(personality, petName)

        val requestBody = JSONObject().apply {
            put("model", MODEL)
            put("max_tokens", MAX_TOKENS)
            put("system", prompt.first)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt.second)
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
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
            }

            if (connection.responseCode == 200) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use {
                    it.readText()
                }
                parseQuotesResponse(response)
            } else {
                Log.e(TAG, "API error: ${connection.responseCode}")
                emptyList()
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * 성격별 프롬프트
     */
    private fun getQuotePrompt(personality: PetPersonality, petName: String): Pair<String, String> {
        val systemPrompt = """
            너는 걷기 앱의 펫 캐릭터 "$petName"이야.
            실제 유명인의 명언을 캐릭터 말투로 전달해줘.

            규칙:
            - 실제 존재하는 유명인(철학자, 작가, 운동선수, 기업인 등)의 명언만 사용
            - 형식: 명언내용이래ㅋ -인물이름 (따옴표 없이, ~이래/~래 형태로)
            - 명언은 15자 이내로 짧게
            - 걷기, 운동, 도전, 성공, 인생, 노력 관련 명언
            - 이모지 사용 금지
            - 줄바꿈으로 구분해서 5개 출력
            - 번호 없이 명언만 출력
        """.trimIndent()

        val personalityInstruction = when (personality) {
            PetPersonality.TOUGH -> "상남자 말투. 짧고 단호하게. ~다, ~해라 체. 예: '시작이 반이다 -아리스토텔레스', '멈추지 마 -공자'"
            PetPersonality.CUTE -> "MZ 말투. ㅋ 붙이고 가볍게. 예: '시작이 반이래ㅋ -아리스토텔레스', '멈추지 말래ㅋ -공자'"
            PetPersonality.TSUNDERE -> "츤데레 말투. '뭐...', '흥,' 시작. 예: '뭐... 시작이 반이래 -아리스토텔레스', '흥, 멈추지 말래 -공자'"
            PetPersonality.DIALECT -> "20대 부산 여자 말투. 쿨하게 ~네, ~다, ~래 체. 예: '시작이 반이래 -아리스토텔레스', '걷기가 최고래 -히포크라테스'"
            PetPersonality.TIMID -> "소심한 말투. '저...' 시작, 존댓말. 예: '저... 시작이 반이래요 -아리스토텔레스'"
            PetPersonality.POSITIVE -> "긍정 말투. ! 붙여서 밝게. 예: '시작이 반이래! -아리스토텔레스', '할 수 있어! -헨리 포드'"
        }

        val userMessage = """
            $personalityInstruction

            걷기/운동/도전/성공 관련 실제 유명인 명언 5개.
            형식: 성격에 맞는 말투로 명언 전달 -인물이름
            줄바꿈으로 구분, 따옴표 없이.
        """.trimIndent()

        return Pair(systemPrompt, userMessage)
    }

    /**
     * 응답 파싱
     */
    private fun parseQuotesResponse(response: String): List<String> {
        return try {
            val json = JSONObject(response)
            val content = json.getJSONArray("content")
            if (content.length() > 0) {
                val text = content.getJSONObject(0).getString("text")
                text.split("\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && it.length <= 30 }
                    .take(5)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: ${e.message}")
            emptyList()
        }
    }

    /**
     * 캐시 초기화 (디버그용)
     */
    fun clearCache() {
        cachedQuotes.clear()
    }
}
