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

    // 기본 명언 (AI 실패 시 폴백) - 원본 명언 그대로
    private val originalQuotes = listOf(
        "시작이 반이다. -아리스토텔레스",
        "천 리 길도 한 걸음부터. -노자",
        "느려도 멈추지만 않으면 된다. -공자",
        "걷는 것이 최고의 약이다. -히포크라테스",
        "할 수 있다고 믿으면 이미 반은 온 것이다. -헨리 포드",
        "오늘 할 일을 내일로 미루지 마라. -벤자민 프랭클린",
        "위대한 일은 작은 일들이 모여 이루어진다. -빈센트 반 고흐",
        "행동이 모든 성공의 열쇠다. -파블로 피카소",
        "꾸준함이 천재를 이긴다. -속담",
        "오늘 심은 나무가 내일의 그늘이 된다. -속담",
        "매일 조금씩 나아가면 된다. -존 우든",
        "몸이 움직이면 마음도 따라온다. -윌리엄 제임스"
    )

    // 성격별 매핑 제거 - 모든 성격에 동일한 원본 명언 사용
    private val defaultQuotes = mapOf(
        PetPersonality.TOUGH to originalQuotes,
        PetPersonality.CUTE to originalQuotes,
        PetPersonality.TSUNDERE to originalQuotes,
        PetPersonality.DIALECT to originalQuotes,
        PetPersonality.TIMID to originalQuotes,
        PetPersonality.POSITIVE to originalQuotes
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
     * 명언 생성 프롬프트 (원본 그대로)
     */
    private fun getQuotePrompt(personality: PetPersonality, petName: String): Pair<String, String> {
        val systemPrompt = """
            실제 유명인의 명언을 원본 그대로 전달해줘.

            규칙:
            - 실제 존재하는 유명인(철학자, 작가, 운동선수, 기업인 등)의 명언만 사용
            - 형식: 명언 원문. -인물이름
            - 명언은 20자 이내로 짧게
            - 걷기, 운동, 도전, 성공, 인생, 노력, 꾸준함 관련 명언
            - 이모지 사용 금지
            - 말투 변형 없이 원본 그대로
            - 줄바꿈으로 구분해서 5개 출력
            - 번호 없이 명언만 출력
        """.trimIndent()

        val userMessage = """
            걷기/운동/도전/성공/꾸준함 관련 실제 유명인 명언 5개.
            형식: 명언 원문. -인물이름
            줄바꿈으로 구분, 따옴표 없이, 말투 변형 없이 원본 그대로.
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
