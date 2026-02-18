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

    // 언어 감지
    private fun getLang(): String = Locale.getDefault().language

    // 기본 명언 (AI 실패 시 폴백) - 다국어 지원
    private val quotesKo = listOf(
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

    private val quotesEn = listOf(
        "Well begun is half done. -Aristotle",
        "A journey of a thousand miles begins with a single step. -Lao Tzu",
        "It does not matter how slowly you go as long as you do not stop. -Confucius",
        "Walking is man's best medicine. -Hippocrates",
        "Whether you think you can or think you can't, you're right. -Henry Ford",
        "Never leave till tomorrow what you can do today. -Benjamin Franklin",
        "Great things are done by a series of small things. -Vincent van Gogh",
        "Action is the foundational key to all success. -Pablo Picasso",
        "Consistency beats talent. -Proverb",
        "The tree planted today is tomorrow's shade. -Proverb",
        "Make each day your masterpiece. -John Wooden",
        "Act as if what you do makes a difference. It does. -William James"
    )

    private val quotesJa = listOf(
        "始まりは半分終わったも同然。 -アリストテレス",
        "千里の道も一歩から。 -老子",
        "止まらなければ、どんなにゆっくりでも進める。 -孔子",
        "歩くことは最高の薬である。 -ヒポクラテス",
        "できると信じれば、半分は達成したも同然だ。 -ヘンリー・フォード",
        "今日できることを明日に延ばすな。 -ベンジャミン・フランクリン",
        "偉大なことは小さなことの積み重ね。 -フィンセント・ファン・ゴッホ",
        "行動がすべての成功の鍵だ。 -パブロ・ピカソ",
        "継続は才能に勝る。 -ことわざ",
        "今日植えた木が明日の日陰になる。 -ことわざ",
        "毎日少しずつ進めばいい。 -ジョン・ウッデン",
        "体が動けば心もついてくる。 -ウィリアム・ジェームズ"
    )

    private val quotesZh = listOf(
        "好的开始是成功的一半。 -亚里士多德",
        "千里之行，始于足下。 -老子",
        "不怕慢，只怕站。 -孔子",
        "行走是最好的药。 -希波克拉底",
        "相信自己能做到，就已经成功一半了。 -亨利·福特",
        "今日事今日毕。 -本杰明·富兰克林",
        "伟大的事业是由小事组成的。 -梵高",
        "行动是成功的关键。 -毕加索",
        "坚持胜于天赋。 -谚语",
        "今天种的树，明天乘凉。 -谚语",
        "每天进步一点点。 -约翰·伍登",
        "身体动起来，心也会跟上。 -威廉·詹姆斯"
    )

    private val quotesEs = listOf(
        "Bien empezado, medio acabado. -Aristóteles",
        "Un viaje de mil millas comienza con un solo paso. -Lao Tzu",
        "No importa lo lento que vayas mientras no te detengas. -Confucio",
        "Caminar es la mejor medicina. -Hipócrates",
        "Si crees que puedes, ya estás a medio camino. -Henry Ford",
        "No dejes para mañana lo que puedes hacer hoy. -Benjamin Franklin",
        "Las grandes cosas se hacen con pequeños pasos. -Vincent van Gogh",
        "La acción es la clave del éxito. -Pablo Picasso",
        "La constancia vence al talento. -Proverbio",
        "El árbol plantado hoy es la sombra de mañana. -Proverbio",
        "Haz de cada día tu obra maestra. -John Wooden",
        "Actúa como si lo que haces marcara la diferencia. -William James"
    )

    // 언어별 기본 명언 반환
    private fun getDefaultQuotesList(): List<String> = when (getLang()) {
        "ko" -> quotesKo
        "ja" -> quotesJa
        "zh" -> quotesZh
        "es" -> quotesEs
        else -> quotesEn
    }

    // 언어별 폴백 메시지
    private fun getFallbackMessage(): String = when (getLang()) {
        "ko" -> "오늘도 힘내자!"
        "ja" -> "今日も頑張ろう!"
        "zh" -> "今天也加油!"
        "es" -> "Hoy también, ¡ánimo!"
        else -> "Let's do our best today!"
    }

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
        val quotes = cachedQuotes[personality]
        if (quotes != null && quotes.isNotEmpty()) {
            return quotes.random()
        }
        // 캐시 없으면 언어별 기본 명언 사용
        val defaultList = getDefaultQuotesList()
        return if (defaultList.isNotEmpty()) defaultList.random() else getFallbackMessage()
    }

    /**
     * 모든 명언 가져오기
     */
    fun getAllQuotes(personality: PetPersonality): List<String> {
        return cachedQuotes[personality] ?: getDefaultQuotesList()
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
                cachedQuotes[personality] = getDefaultQuotesList()
                return
            }

            Log.d(TAG, "Generating quotes for $personality")
            val quotes = callClaudeForQuotes(personality, petName)

            if (quotes.isNotEmpty()) {
                cachedQuotes[personality] = quotes
                Log.d(TAG, "Generated ${quotes.size} quotes for $personality")
            } else {
                cachedQuotes[personality] = getDefaultQuotesList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate quotes: ${e.message}")
            cachedQuotes[personality] = getDefaultQuotesList()
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
     * 명언 생성 프롬프트 (언어별)
     */
    private fun getQuotePrompt(personality: PetPersonality, petName: String): Pair<String, String> {
        val lang = getLang()

        val (systemPrompt, userMessage) = when (lang) {
            "ko" -> Pair(
                """
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
                """.trimIndent(),
                """
                걷기/운동/도전/성공/꾸준함 관련 실제 유명인 명언 5개.
                형식: 명언 원문. -인물이름
                줄바꿈으로 구분, 따옴표 없이, 말투 변형 없이 원본 그대로.
                """.trimIndent()
            )
            "ja" -> Pair(
                """
                有名人の名言をそのまま伝えてください。

                ルール:
                - 実在の有名人（哲学者、作家、アスリート、企業家など）の名言のみ使用
                - 形式: 名言原文。 -人物名
                - 名言は20文字以内で短く
                - ウォーキング、運動、挑戦、成功、人生、努力、継続に関する名言
                - 絵文字禁止
                - 口調変更なしで原文そのまま
                - 改行で区切って5つ出力
                - 番号なしで名言のみ出力
                """.trimIndent(),
                """
                ウォーキング/運動/挑戦/成功/継続に関する実在有名人の名言5つ。
                形式: 名言原文。 -人物名
                改行で区切り、引用符なし、口調変更なしで原文そのまま。
                """.trimIndent()
            )
            "zh" -> Pair(
                """
                请提供真实名人的原话。

                规则:
                - 只使用真实存在的名人（哲学家、作家、运动员、企业家等）的名言
                - 格式: 名言原文。 -人物名
                - 名言20字以内
                - 关于步行、运动、挑战、成功、人生、努力、坚持的名言
                - 禁止使用表情符号
                - 不改变语气，保持原文
                - 用换行分隔，输出5条
                - 不要编号，只输出名言
                """.trimIndent(),
                """
                关于步行/运动/挑战/成功/坚持的真实名人名言5条。
                格式: 名言原文。 -人物名
                用换行分隔，不加引号，不改变语气，保持原文。
                """.trimIndent()
            )
            "es" -> Pair(
                """
                Proporciona citas de personas famosas reales tal como son.

                Reglas:
                - Solo citas de personas famosas reales (filósofos, escritores, atletas, empresarios, etc.)
                - Formato: Cita original. -Nombre de la persona
                - Citas de 20 caracteres o menos
                - Citas sobre caminar, ejercicio, desafío, éxito, vida, esfuerzo, perseverancia
                - Sin emojis
                - Sin cambio de tono, original tal cual
                - Separadas por saltos de línea, 5 citas
                - Sin números, solo citas
                """.trimIndent(),
                """
                5 citas de personas famosas reales sobre caminar/ejercicio/desafío/éxito/perseverancia.
                Formato: Cita original. -Nombre de la persona
                Separadas por saltos de línea, sin comillas, sin cambio de tono, original tal cual.
                """.trimIndent()
            )
            else -> Pair(
                """
                Provide real famous people's quotes as they are.

                Rules:
                - Only use quotes from real existing famous people (philosophers, writers, athletes, entrepreneurs, etc.)
                - Format: Original quote. -Person's name
                - Quotes should be 20 characters or less
                - Quotes about walking, exercise, challenge, success, life, effort, perseverance
                - No emojis
                - No tone changes, keep original as is
                - Separate by line breaks, output 5
                - No numbers, only quotes
                """.trimIndent(),
                """
                5 quotes from real famous people about walking/exercise/challenge/success/perseverance.
                Format: Original quote. -Person's name
                Separated by line breaks, no quotation marks, no tone changes, original as is.
                """.trimIndent()
            )
        }

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
