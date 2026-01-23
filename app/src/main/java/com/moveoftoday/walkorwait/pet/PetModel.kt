package com.moveoftoday.walkorwait.pet

import androidx.annotation.DrawableRes
import com.moveoftoday.walkorwait.R

/**
 * Pet animation type
 */
enum class PetAnimationType {
    IDLE, WALK, ATTACK, DEATH, HURT
}

/**
 * Pet animation data
 */
data class PetAnimationData(
    val frames: Int,
    val assetPath: String
)

/**
 * Pet type enumeration with personality and dialogue
 */
enum class PetType(
    val displayName: String,
    val personality: PetPersonality,
    val animations: Map<PetAnimationType, PetAnimationData>
) {
    DOG1(
        displayName = "멍이",
        personality = PetPersonality.TOUGH,
        animations = mapOf(
            PetAnimationType.IDLE to PetAnimationData(4, "pets/dog1_idle.png"),
            PetAnimationType.WALK to PetAnimationData(6, "pets/dog1_walk.png"),
            PetAnimationType.ATTACK to PetAnimationData(4, "pets/dog1_attack.png"),
            PetAnimationType.DEATH to PetAnimationData(4, "pets/dog1_death.png"),
            PetAnimationType.HURT to PetAnimationData(2, "pets/dog1_hurt.png")
        )
    ),
    DOG2(
        displayName = "복실이",
        personality = PetPersonality.CUTE,
        animations = mapOf(
            PetAnimationType.IDLE to PetAnimationData(4, "pets/dog2_idle.png"),
            PetAnimationType.WALK to PetAnimationData(6, "pets/dog2_walk.png"),
            PetAnimationType.ATTACK to PetAnimationData(4, "pets/dog2_attack.png"),
            PetAnimationType.DEATH to PetAnimationData(4, "pets/dog2_death.png"),
            PetAnimationType.HURT to PetAnimationData(2, "pets/dog2_hurt.png")
        )
    ),
    CAT1(
        displayName = "냥이",
        personality = PetPersonality.TSUNDERE,
        animations = mapOf(
            PetAnimationType.IDLE to PetAnimationData(4, "pets/cat1_idle.png"),
            PetAnimationType.WALK to PetAnimationData(6, "pets/cat1_walk.png"),
            PetAnimationType.ATTACK to PetAnimationData(4, "pets/cat1_attack.png"),
            PetAnimationType.DEATH to PetAnimationData(4, "pets/cat1_death.png"),
            PetAnimationType.HURT to PetAnimationData(2, "pets/cat1_hurt.png")
        )
    ),
    CAT2(
        displayName = "치즈",
        personality = PetPersonality.DIALECT,
        animations = mapOf(
            PetAnimationType.IDLE to PetAnimationData(4, "pets/cat2_idle.png"),
            PetAnimationType.WALK to PetAnimationData(6, "pets/cat2_walk.png"),
            PetAnimationType.ATTACK to PetAnimationData(4, "pets/cat2_attack.png"),
            PetAnimationType.DEATH to PetAnimationData(4, "pets/cat2_death.png"),
            PetAnimationType.HURT to PetAnimationData(2, "pets/cat2_hurt.png")
        )
    ),
    RAT(
        displayName = "찍이",
        personality = PetPersonality.TIMID,
        animations = mapOf(
            PetAnimationType.IDLE to PetAnimationData(4, "pets/rat_idle.png"),
            PetAnimationType.WALK to PetAnimationData(4, "pets/rat_walk.png"),
            PetAnimationType.DEATH to PetAnimationData(4, "pets/rat_death.png"),
            PetAnimationType.HURT to PetAnimationData(2, "pets/rat_hurt.png")
        )
    ),
    BIRD(
        displayName = "짹이",
        personality = PetPersonality.POSITIVE,
        animations = mapOf(
            PetAnimationType.IDLE to PetAnimationData(4, "pets/bird_idle.png"),
            PetAnimationType.WALK to PetAnimationData(6, "pets/bird_walk.png"),
            PetAnimationType.DEATH to PetAnimationData(4, "pets/bird_death.png"),
            PetAnimationType.HURT to PetAnimationData(2, "pets/bird_hurt.png")
        )
    );

    // 기존 호환성을 위한 프로퍼티
    val walkFrames: Int get() = animations[PetAnimationType.WALK]?.frames ?: 4
    val idleFrames: Int get() = animations[PetAnimationType.IDLE]?.frames ?: 4
    val walkAssetPath: String get() = animations[PetAnimationType.WALK]?.assetPath ?: ""
    val idleAssetPath: String get() = animations[PetAnimationType.IDLE]?.assetPath ?: ""

    // 랜덤 애니메이션 선택 (idle 상태에서 attack, walk 랜덤)
    fun getRandomIdleAnimation(): PetAnimationType {
        val random = Math.random()
        val hasAttack = animations.containsKey(PetAnimationType.ATTACK)
        return when {
            random < 0.5 -> PetAnimationType.IDLE  // 50% idle
            random < 0.75 -> PetAnimationType.WALK // 25% walk
            hasAttack -> PetAnimationType.ATTACK   // 25% attack (있으면)
            else -> PetAnimationType.IDLE
        }
    }
}

/**
 * Pet personality types with unique speech patterns
 */
enum class PetPersonality {
    TOUGH,      // 상남자 - Dog1: short, cool phrases
    CUTE,       // 애교쟁이 - Dog2: ~용 endings, hearts
    TSUNDERE,   // 츤데레 - Cat1: cold but caring
    DIALECT,    // 사투리 - Cat2: 경상도 dialect
    TIMID,      // 소심이 - Rat: nervous, polite
    POSITIVE    // 긍정왕 - Bird: cheerful
}

/**
 * Dialogue with color indication
 */
data class PetDialogue(
    val text: String,
    val isRed: Boolean = false  // true면 빨간색으로 표시 (경고/화남)
)

/**
 * Dialogue generator based on pet personality
 */
object PetDialogues {

    // Welcome messages (tutorial start)
    fun getWelcomeMessage(personality: PetPersonality, petName: String): String {
        return when (personality) {
            PetPersonality.TOUGH -> "왔구나."
            PetPersonality.CUTE -> "왔구나! 반가움ㅋㅋ"
            PetPersonality.TSUNDERE -> "흥, 왔어?"
            PetPersonality.DIALECT -> "어서오이소~"
            PetPersonality.TIMID -> "아, 안녕하세요..."
            PetPersonality.POSITIVE -> "안녕! 오늘도 좋은 하루야!"
        }
    }

    // Tutorial step 1: Explain the app
    fun getTutorialStep1(personality: PetPersonality, petName: String): String {
        return when (personality) {
            PetPersonality.TOUGH -> "매일 걸으면 된다. 간단하지."
            PetPersonality.CUTE -> "나랑 같이 산책하자구! 간바!"
            PetPersonality.TSUNDERE -> "뭐, 걷는 거 도와줄게. 고마워하지마."
            PetPersonality.DIALECT -> "매일 걸으면 되는기라~"
            PetPersonality.TIMID -> "저, 저랑 같이 걸어주실 거죠...?"
            PetPersonality.POSITIVE -> "걷기 시작하면 기분이 좋아져!"
        }
    }

    // Tutorial step 2: Goal explanation
    fun getTutorialStep2(personality: PetPersonality, petName: String): String {
        return when (personality) {
            PetPersonality.TOUGH -> "목표를 정해. 지켜."
            PetPersonality.CUTE -> "목표 채우면 내가 우레시! 해짐ㅋㅋ"
            PetPersonality.TSUNDERE -> "목표 못 채우면... 좀 그래."
            PetPersonality.DIALECT -> "매일 목표 채우면 좋은기라!"
            PetPersonality.TIMID -> "목표... 함께 달성해봐요..."
            PetPersonality.POSITIVE -> "목표 달성하면 최고야!"
        }
    }

    // Tutorial step 3: Complete
    fun getTutorialComplete(personality: PetPersonality, petName: String): String {
        return when (personality) {
            PetPersonality.TOUGH -> "시작하자."
            PetPersonality.CUTE -> "같이 간바루! 이쿠요~"
            PetPersonality.TSUNDERE -> "뭐, 잘 부탁해."
            PetPersonality.DIALECT -> "자, 시작해보이소!"
            PetPersonality.TIMID -> "잘, 잘 부탁드려요..."
            PetPersonality.POSITIVE -> "우리 함께 화이팅!"
        }
    }

    // Walking state messages
    fun getWalkingMessage(personality: PetPersonality, progressPercent: Int): String {
        return when (personality) {
            PetPersonality.TOUGH -> when {
                progressPercent < 30 -> "더 걸어."
                progressPercent < 70 -> "괜찮네."
                progressPercent < 100 -> "거의 다 왔다."
                else -> "됐다."
            }
            PetPersonality.CUTE -> when {
                progressPercent < 30 -> "산책 스키~"
                progressPercent < 70 -> "잘하고 있음 간바!"
                progressPercent < 100 -> "모우스코시!"
                else -> "사이코! 해냄ㅋㅋ"
            }
            PetPersonality.TSUNDERE -> when {
                progressPercent < 30 -> "...따라올거야?"
                progressPercent < 70 -> "뭐, 나쁘지 않네."
                progressPercent < 100 -> "좀 더 해봐."
                else -> "흥, 잘했어."
            }
            PetPersonality.DIALECT -> when {
                progressPercent < 30 -> "같이 걸어보이소~"
                progressPercent < 70 -> "잘하고 있는기라!"
                progressPercent < 100 -> "조금만 더 힘내이소!"
                else -> "대단하이소!"
            }
            PetPersonality.TIMID -> when {
                progressPercent < 30 -> "저, 천천히 가요..."
                progressPercent < 70 -> "잘하고 계세요..."
                progressPercent < 100 -> "조금만 더요..."
                else -> "해, 해냈어요!"
            }
            PetPersonality.POSITIVE -> when {
                progressPercent < 30 -> "좋은 시작이야!"
                progressPercent < 70 -> "잘하고 있어!"
                progressPercent < 100 -> "거의 다 왔어!"
                else -> "완벽해! 최고야!"
            }
        }
    }

    // Idle state messages (not walking) - 0% 대사들
    fun getIdleMessage(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "산책 가자.",
                "뭐해? 걷자.",
                "오늘 목표, 시작하자.",
                "일어나. 갈 시간이야.",
                "밖으로."
            )
            PetPersonality.CUTE -> listOf(
                "산책각인데... 이쿠요!",
                "밖에 나가고 싶음ㅠ 하야쿠~",
                "오늘 갓생 간바루!",
                "심심함... 산책 가자구 네~",
                "걷고 싶다잉 요시!"
            )
            PetPersonality.TSUNDERE -> listOf(
                "...가자고.",
                "언제까지 있을 거야?",
                "나가자. 지금.",
                "...심심해.",
                "밖에 나갈 거야, 말 거야?"
            )
            PetPersonality.DIALECT -> listOf(
                "산책 가자이~",
                "오늘 얼마나 걸을끼라?",
                "밖에 좋은기라!",
                "걷기 좋은 날이네이!",
                "목표 달성해보자이!"
            )
            PetPersonality.TIMID -> listOf(
                "저, 산책...",
                "오늘... 걸어볼까요...?",
                "저, 나가고 싶어요...",
                "목표... 시작해볼까요...?",
                "밖에... 나가요..."
            )
            PetPersonality.POSITIVE -> listOf(
                "밖에 나가자!",
                "오늘도 걸어보자!",
                "좋은 하루의 시작! 걷자!",
                "목표 달성하러 가자!",
                "산책 가면 기분 좋아질 거야!"
            )
        }
        return messages.random()
    }

    // Idle with pet name
    fun getIdleMessage(personality: PetPersonality, petName: String): String {
        return getIdleMessage(personality)
    }

    // Goal achieved celebration - 100% 대사들
    fun getGoalAchievedMessage(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "해냈다.",
                "목표 달성. 수고했어.",
                "완벽해.",
                "역시.",
                "됐다. 잘했어."
            )
            PetPersonality.CUTE -> listOf(
                "야바이 해냄ㅋㅋㅋ",
                "목표 달성 스고이!",
                "사이코! 갓생 완료",
                "오걷완! 오츠카레~",
                "미쳤다 찐이야 우레시!"
            )
            PetPersonality.TSUNDERE -> listOf(
                "뭐, 잘했어.",
                "해냈네. ...대단해.",
                "목표 달성이라... 뭐, 당연한 거지만.",
                "칭찬해줄게. 이번만.",
                "흥, 못할 줄 알았는데."
            )
            PetPersonality.DIALECT -> listOf(
                "대단하이소!",
                "목표 달성이라이! 짱이다!",
                "완전 잘했는기라!",
                "역시 최고라이!",
                "해냈구마이! 자랑스럽다이!"
            )
            PetPersonality.TIMID -> listOf(
                "해, 해냈어요...!",
                "목표 달성이에요...! 대단해요...!",
                "저, 정말 기뻐요...!",
                "잘하셨어요... 정말로...",
                "우와... 해냈어요..."
            )
            PetPersonality.POSITIVE -> listOf(
                "최고야! 대단해!",
                "목표 달성! 완벽해!",
                "해냈어! 역시 최고야!",
                "너무 잘했어! 자랑스러워!",
                "완전 대박! 축하해!"
            )
        }
        return messages.random()
    }

    // Goal achieved with pet name
    fun getGoalAchievedMessage(personality: PetPersonality, petName: String): String {
        return getGoalAchievedMessage(personality)
    }

    // 마일스톤 달성 메시지 (10% 단위)
    fun getMilestoneMessage(personality: PetPersonality, milestone: Int): String {
        val messages = when (milestone) {
            10 -> when (personality) {
                PetPersonality.TOUGH -> listOf("시작했군.", "좋아, 출발이다.", "가자.")
                PetPersonality.CUTE -> listOf("시작이다! 이쿠요~", "10%! 간바레!", "출발ㅋㅋ")
                PetPersonality.TSUNDERE -> listOf("겨우 10%야.", "시작은 했네.", "...아직 멀었어.")
                PetPersonality.DIALECT -> listOf("시작했구마이!", "10%라이!", "가보자이!")
                PetPersonality.TIMID -> listOf("시, 시작이에요...", "10%예요...!", "조금씩...")
                PetPersonality.POSITIVE -> listOf("시작이야! 화이팅!", "10% 달성!", "좋은 출발!")
            }
            20 -> when (personality) {
                PetPersonality.TOUGH -> listOf("20%. 계속.", "잘하고 있어.", "그래, 이 조자.")
                PetPersonality.CUTE -> listOf("20%! 스고이~", "잘하고 있음!", "이 조자로 고고!")
                PetPersonality.TSUNDERE -> listOf("20%... 뭐, 괜찮네.", "조금 했네.", "...나쁘지 않아.")
                PetPersonality.DIALECT -> listOf("20%라이! 좋다!", "잘하고 있는기라!", "이 조자다이!")
                PetPersonality.TIMID -> listOf("20%예요...!", "잘하고 계세요...", "조금 더...")
                PetPersonality.POSITIVE -> listOf("20%! 잘하고 있어!", "좋아좋아!", "계속 가자!")
            }
            30 -> when (personality) {
                PetPersonality.TOUGH -> listOf("30%. 좋아.", "계속 걸어.", "됐다.")
                PetPersonality.CUTE -> listOf("30%! 야바이~", "벌써 30%ㅋㅋ", "잘한다!")
                PetPersonality.TSUNDERE -> listOf("30%라... 뭐, 괜찮아.", "좀 했네.", "...계속해.")
                PetPersonality.DIALECT -> listOf("30%라이!", "잘하는기라!", "좋다이!")
                PetPersonality.TIMID -> listOf("30%예요...!", "잘하고 있어요...", "힘내요...")
                PetPersonality.POSITIVE -> listOf("30%! 대단해!", "잘하고 있어!", "화이팅!")
            }
            40 -> when (personality) {
                PetPersonality.TOUGH -> listOf("40%. 반 가까이.", "계속.", "좋아.")
                PetPersonality.CUTE -> listOf("40%! 거의 반!", "곧 반이다~", "화이팅!")
                PetPersonality.TSUNDERE -> listOf("40%... 반은 아니야.", "곧 반이네.", "...힘내.")
                PetPersonality.DIALECT -> listOf("40%라이!", "반 가까이다이!", "조금만 더!")
                PetPersonality.TIMID -> listOf("40%예요...", "곧 반이에요...", "화이팅...")
                PetPersonality.POSITIVE -> listOf("40%! 곧 반!", "잘하고 있어!", "거의 다 왔어!")
            }
            50 -> when (personality) {
                PetPersonality.TOUGH -> listOf("반 왔다.", "50%. 절반.", "좋아, 반이다.")
                PetPersonality.CUTE -> listOf("반이다! 스고이!", "50%! 대박!", "절반 왔음ㅋㅋ")
                PetPersonality.TSUNDERE -> listOf("반... 왔네.", "50%라니... 괜찮아.", "...잘하고 있어.")
                PetPersonality.DIALECT -> listOf("반이라이!", "50% 대단하다이!", "잘한다이!")
                PetPersonality.TIMID -> listOf("반이에요...!", "50%...! 대단해요...", "절반이에요...")
                PetPersonality.POSITIVE -> listOf("반이야! 대단해!", "50% 달성!", "절반 완료!")
            }
            60 -> when (personality) {
                PetPersonality.TOUGH -> listOf("60%. 반 넘었다.", "잘하고 있어.", "계속.")
                PetPersonality.CUTE -> listOf("60%! 반 넘음!", "야바이 잘한다!", "이 조자!")
                PetPersonality.TSUNDERE -> listOf("반 넘었네...", "60%라... 뭐, 잘해.", "...계속해봐.")
                PetPersonality.DIALECT -> listOf("60%라이!", "반 넘었다이!", "잘한다이!")
                PetPersonality.TIMID -> listOf("60%예요...!", "반 넘었어요...", "대단해요...")
                PetPersonality.POSITIVE -> listOf("60%! 반 넘었어!", "잘하고 있어!", "화이팅!")
            }
            70 -> when (personality) {
                PetPersonality.TOUGH -> listOf("70%. 거의 다.", "조금 남았다.", "마무리하자.")
                PetPersonality.CUTE -> listOf("70%! 거의 다!", "얼마 안 남음!", "화이팅!")
                PetPersonality.TSUNDERE -> listOf("70%... 거의 다야.", "조금만 더.", "...할 수 있어.")
                PetPersonality.DIALECT -> listOf("70%라이!", "거의 다 왔다이!", "힘내라이!")
                PetPersonality.TIMID -> listOf("70%예요...!", "거의 다 왔어요...", "조금만...")
                PetPersonality.POSITIVE -> listOf("70%! 거의 다야!", "조금만 더!", "할 수 있어!")
            }
            80 -> when (personality) {
                PetPersonality.TOUGH -> listOf("80%. 거의 끝.", "조금만.", "마무리.")
                PetPersonality.CUTE -> listOf("80%! 대박!", "거의 다야!", "조금만 더!")
                PetPersonality.TSUNDERE -> listOf("80%... 거의 끝이야.", "조금만 더 해.", "...잘하고 있어.")
                PetPersonality.DIALECT -> listOf("80%라이!", "거의 다다이!", "조금만!")
                PetPersonality.TIMID -> listOf("80%예요...!", "거의 다 왔어요...", "조금만 더...")
                PetPersonality.POSITIVE -> listOf("80%! 거의 다!", "대단해!", "끝이 보여!")
            }
            90 -> when (personality) {
                PetPersonality.TOUGH -> listOf("90%. 끝내자.", "거의 다.", "마지막.")
                PetPersonality.CUTE -> listOf("90%! 야바이!", "거의 다 왔어!", "끝내자!")
                PetPersonality.TSUNDERE -> listOf("90%... 끝내.", "거의 다야...", "...조금만 더.")
                PetPersonality.DIALECT -> listOf("90%라이!", "거의 다라이!", "끝내자이!")
                PetPersonality.TIMID -> listOf("90%예요...!", "거의요...", "조금만...")
                PetPersonality.POSITIVE -> listOf("90%! 거의 다야!", "끝이 보여!", "마지막!")
            }
            100 -> getGoalAchievedMessage(personality).let { listOf(it) }
            else -> listOf("")
        }
        return messages.random()
    }

    // 운동 동기부여 명언 (실제 유명인 명언, 성격별 말투로)
    fun getMotivationalQuote(personality: PetPersonality): String {
        // 유명인 명언 원문과 출처
        data class Quote(val text: String, val author: String)
        val famousQuotes = listOf(
            Quote("천 리 길도 한 걸음부터", "노자"),
            Quote("포기하면 거기서 끝이다", "안서일 감독"),
            Quote("오늘 걷지 않으면 내일은 뛰어야 한다", "유대 격언"),
            Quote("건강한 신체에 건강한 정신이 깃든다", "유베날리스"),
            Quote("걷는 것은 최고의 운동이다", "히포크라테스"),
            Quote("시작이 반이다", "아리스토텔레스"),
            Quote("할 수 있다고 믿으면 이미 반은 이룬 것이다", "루즈벨트"),
            Quote("작은 진전도 진전이다", "에드 시런"),
            Quote("최고의 준비는 어제 시작한 것이다", "존 우든"),
            Quote("매일 조금씩이 큰 성과를 만든다", "톨스토이"),
            Quote("고통은 일시적이다. 포기는 영원하다", "랜스 암스트롱"),
            Quote("한 걸음씩 나아가면 어디든 갈 수 있다", "마하트마 간디"),
            Quote("오늘 할 일을 내일로 미루지 마라", "벤자민 프랭클린"),
            Quote("행동이 모든 성공의 기초다", "파블로 피카소"),
            Quote("변명을 만들거나 발전을 만들거라", "무하마드 알리")
        )

        val quote = famousQuotes.random()

        // 성격별 말투로 변환 (줄바꿈 후 -사람)
        return when (personality) {
            PetPersonality.TOUGH -> "\"${quote.text}\"\n-${quote.author}"
            PetPersonality.CUTE -> "\"${quote.text}\"래~\n-${quote.author}ㅋㅋ"
            PetPersonality.TSUNDERE -> "\"${quote.text}\"...래.\n-${quote.author}"
            PetPersonality.DIALECT -> "\"${quote.text}\"라이~\n-${quote.author}"
            PetPersonality.TIMID -> "\"${quote.text}\"...래요...\n-${quote.author}"
            PetPersonality.POSITIVE -> "\"${quote.text}\"!\n-${quote.author}"
        }
    }

    // Free time (자유 시간) - 제어 요일/시간대가 아닐 때
    fun getFreeTimeMessage(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "오늘은 쉬는 날.",
                "자유롭게 쉬어.",
                "오늘은 편하게.",
                "푹 쉬어. 내일 보자."
            )
            PetPersonality.CUTE -> listOf(
                "오늘은 쉬는 날이닷!",
                "자유 시간~ 릴렉스~",
                "마음대로 해도 됨ㅋㅋ",
                "휴식도 중요함!"
            )
            PetPersonality.TSUNDERE -> listOf(
                "오늘은... 뭐, 쉬어도 돼.",
                "자유 시간이라고. 감사하라구.",
                "맘대로 해. 뭐든.",
                "쉬는 것도 필요하다구."
            )
            PetPersonality.DIALECT -> listOf(
                "오늘은 쉬는 날이라예~",
                "편하게 쉬소~",
                "자유 시간이라카이~",
                "맘 편히 쉬이소~"
            )
            PetPersonality.TIMID -> listOf(
                "오, 오늘은 쉬는 날이에요...",
                "자유롭게 쉬셔도 돼요...",
                "편하게 계세요...",
                "휴식... 중요해요..."
            )
            PetPersonality.POSITIVE -> listOf(
                "오늘은 자유 시간이야!",
                "편하게 쉬어가자!",
                "휴식도 중요해!",
                "리프레시 타임!"
            )
        }
        return messages.random()
    }

    // Almost there (90%+) - 90~99% 대사들
    fun getAlmostThereMessage(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "거의 다 왔다.",
                "조금만 더.",
                "마지막이다. 힘내.",
                "끝이 보인다.",
                "90% 넘었어. 마무리하자."
            )
            PetPersonality.CUTE -> listOf(
                "조금만 더! 간바레!",
                "거의 다 왔어 마지?",
                "90% 넘음 야바이ㅋㅋ",
                "모우스코시! 할 수 있음",
                "끝이 보인다 사이코~"
            )
            PetPersonality.TSUNDERE -> listOf(
                "좀 더 해봐.",
                "거의 다 왔어... 포기하지 마.",
                "여기서 멈추면... 안 돼.",
                "90%라고? ...대단하긴 해.",
                "마지막까지 힘내. ...응원할게."
            )
            PetPersonality.DIALECT -> listOf(
                "조금만 더 힘내이소!",
                "거의 다 왔는기라!",
                "마지막이라이! 힘내라!",
                "90% 넘었다이! 대단해!",
                "조금만 더 걸으면 된다이!"
            )
            PetPersonality.TIMID -> listOf(
                "조금만 더요...",
                "거, 거의 다 왔어요...!",
                "90%... 대단해요...",
                "마지막 힘내세요...",
                "조금만 더... 할 수 있어요..."
            )
            PetPersonality.POSITIVE -> listOf(
                "거의 다 왔어!",
                "조금만 더! 할 수 있어!",
                "90% 돌파! 대단해!",
                "마지막 스퍼트!",
                "끝이 보여! 화이팅!"
            )
        }
        return messages.random()
    }

    // 75-89% 대사들 (새로 추가)
    fun getThreeQuarterMessage(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "3/4 왔다.",
                "75% 넘었어. 잘하고 있어.",
                "거의 다 왔다.",
                "괜찮네. 계속 가.",
                "페이스 좋아."
            )
            PetPersonality.CUTE -> listOf(
                "75% 넘음 스고이!",
                "3/4 왔다 야바이ㅋㅋ",
                "거의 다 옴! 간바레~",
                "이 페이스 사이코임",
                "잘하고 있음! 모우스코시!"
            )
            PetPersonality.TSUNDERE -> listOf(
                "75%라... 뭐, 괜찮네.",
                "3/4 왔어. 나쁘지 않아.",
                "이 정도면... 인정해줄게.",
                "거의 다 왔어. 멈추지 마.",
                "페이스 좋아... 라고 할까."
            )
            PetPersonality.DIALECT -> listOf(
                "75% 넘었다이!",
                "3/4 왔는기라! 대단해!",
                "거의 다 왔는기야!",
                "이 페이스 좋은기라!",
                "잘하고 있다이!"
            )
            PetPersonality.TIMID -> listOf(
                "75%... 대단해요...",
                "3/4 왔어요... 잘하고 계세요...",
                "거의 다... 왔어요...",
                "이 페이스면... 금방이에요...",
                "조금만 더요..."
            )
            PetPersonality.POSITIVE -> listOf(
                "75% 돌파! 대단해!",
                "3/4 왔어! 거의 다 왔어!",
                "이 페이스 완벽해!",
                "잘하고 있어! 조금만 더!",
                "목표가 눈앞이야!"
            )
        }
        return messages.random()
    }

    // Halfway (50-74%) - 절반 대사들
    fun getHalfwayMessage(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "절반 왔다.",
                "반이야. 나쁘지 않아.",
                "50% 달성. 계속 가자.",
                "중간까지 왔어.",
                "반환점 통과."
            )
            PetPersonality.CUTE -> listOf(
                "절반 옴 스고이!",
                "반이나 함 야바이ㅋㅋ",
                "50% 달성! 요시요시~",
                "반환점 통과 간바!",
                "이 정도면 잘하는 거 아님?"
            )
            PetPersonality.TSUNDERE -> listOf(
                "뭐, 절반은 했네.",
                "반이야... 나쁘지 않아.",
                "50%라고? ...계속해봐.",
                "중간까지 왔어. 멈추지 마.",
                "절반이네. ...조금 대단해."
            )
            PetPersonality.DIALECT -> listOf(
                "반이나 했네이!",
                "50% 달성이라이!",
                "절반 왔는기라!",
                "반환점 통과라이!",
                "중간까지 왔다이! 힘내라!"
            )
            PetPersonality.TIMID -> listOf(
                "반, 반이에요...",
                "50% 달성... 대단해요...",
                "절반 왔어요... 잘하고 계세요...",
                "중간까지... 왔어요...",
                "반환점... 통과했어요..."
            )
            PetPersonality.POSITIVE -> listOf(
                "절반 달성! 잘하고 있어!",
                "50% 돌파! 대단해!",
                "반환점 통과! 화이팅!",
                "절반 왔어! 이 페이스면 금방이야!",
                "중간까지 왔어! 최고야!"
            )
        }
        return messages.random()
    }

    // 25-49% 대사들 (새로 추가)
    fun getQuarterMessage(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "1/4 왔다. 계속.",
                "시작이 좋아.",
                "25% 넘었어. 더 가자.",
                "괜찮아. 이 페이스로.",
                "아직 갈 길이 있어."
            )
            PetPersonality.CUTE -> listOf(
                "1/4 왔다 요시!",
                "좋은 시작임 간바루~",
                "이 페이스 스고이!",
                "25% 돌파 나이스~",
                "계속 간바! 할 수 있음"
            )
            PetPersonality.TSUNDERE -> listOf(
                "25%... 뭐, 시작은 했네.",
                "1/4 왔어. 아직 멀었지만.",
                "이제 시작이야. 멈추지 마.",
                "나쁘지 않아. 계속해.",
                "페이스 괜찮아."
            )
            PetPersonality.DIALECT -> listOf(
                "25% 넘었다이!",
                "1/4 왔는기라!",
                "좋은 시작이라이!",
                "이 페이스 좋은기야!",
                "계속 힘내라이!"
            )
            PetPersonality.TIMID -> listOf(
                "25%... 잘하고 계세요...",
                "1/4 왔어요...",
                "좋은 시작이에요...",
                "이 페이스면... 괜찮아요...",
                "계속... 힘내세요..."
            )
            PetPersonality.POSITIVE -> listOf(
                "25% 돌파! 좋아!",
                "1/4 왔어! 잘하고 있어!",
                "좋은 시작이야!",
                "이 페이스 완벽해!",
                "계속 힘내자!"
            )
        }
        return messages.random()
    }

    // 10-24% 대사들 (새로 추가)
    fun getStartedMessage(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "시작했네.",
                "좋아, 계속 가.",
                "움직이기 시작했어.",
                "10% 넘었다.",
                "이 조자로."
            )
            PetPersonality.CUTE -> listOf(
                "시작함 요시!",
                "10% 넘음 나이스~",
                "걷기 시작! 간바루~",
                "요시요시 좋아좋아!",
                "이 조자 스고이!"
            )
            PetPersonality.TSUNDERE -> listOf(
                "움직이기 시작했네.",
                "10%... 아직 멀었어.",
                "시작은 했어. 계속해.",
                "뭐, 나쁘지 않아.",
                "이제 시작이야."
            )
            PetPersonality.DIALECT -> listOf(
                "시작했는기라!",
                "10% 넘었다이!",
                "걷기 시작했는기야!",
                "좋은기라 좋은기라!",
                "이 조자로 가자이!"
            )
            PetPersonality.TIMID -> listOf(
                "시작했어요...",
                "10%... 잘하고 계세요...",
                "걷기 시작했어요...",
                "좋아요... 이 조자로...",
                "계속... 가봐요..."
            )
            PetPersonality.POSITIVE -> listOf(
                "시작이 좋아!",
                "10% 돌파!",
                "걷기 시작했어! 좋아!",
                "이 조자 완벽해!",
                "잘하고 있어!"
            )
        }
        return messages.random()
    }

    // 1-9% 대사들 (막 시작)
    fun getJustStartedMessage(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "시작이다.",
                "가자.",
                "움직여.",
                "좋아. 시작.",
                "첫 걸음."
            )
            PetPersonality.CUTE -> listOf(
                "시작! 이쿠요~",
                "산책 출발 간바!",
                "걷자! 요시!",
                "첫 걸음 나이스~",
                "출발이다 렛츠고!"
            )
            PetPersonality.TSUNDERE -> listOf(
                "...시작했네.",
                "드디어 움직이네.",
                "뭐야, 이제 시작?",
                "...늦었지만 시작은 했네.",
                "가자. 빨리."
            )
            PetPersonality.DIALECT -> listOf(
                "시작이라이!",
                "산책 출발이라!",
                "걷자이~!",
                "첫 걸음이라이!",
                "출발인기야!"
            )
            PetPersonality.TIMID -> listOf(
                "시, 시작했어요...",
                "출발이에요...",
                "걷기 시작했어요...",
                "첫 걸음... 이에요...",
                "같이... 가요..."
            )
            PetPersonality.POSITIVE -> listOf(
                "시작이야! 화이팅!",
                "첫 걸음! 좋아!",
                "출발! 가자!",
                "드디어 시작!",
                "좋아! 걷자!"
            )
        }
        return messages.random()
    }

    // Sad/low happiness messages
    fun getSadMessage(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "..."
            PetPersonality.CUTE -> "심심함ㅠㅠ 사미시이..."
            PetPersonality.TSUNDERE -> "...언제 걸을 건데."
            PetPersonality.DIALECT -> "심심하다이..."
            PetPersonality.TIMID -> "저, 저..."
            PetPersonality.POSITIVE -> "같이 걷고 싶어..."
        }
    }

    // 화남/경고 메시지 (빨간색 텍스트용)
    fun getAngryMessage(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("걸어. 지금 당장.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("왜 안 걸음?ㅠ 하야쿠!", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("걷기 싫어? 나도 싫어.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("걸으라니께!", isRed = true)
            PetPersonality.TIMID -> PetDialogue("저, 저... 걸어주세요...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("걷자! 지금 바로!", isRed = false)
        }
    }

    // 목표 미달성 경고 메시지
    fun getGoalFailedMessage(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("약속 어겼네.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("목표 못 채움ㅠ 잔넨...", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("실망이야.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("목표 못 채웠구마이...", isRed = false)
            PetPersonality.TIMID -> PetDialogue("저, 저... 괜찮아요...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("내일 다시 하면 돼!", isRed = false)
        }
    }

    // 오랜만에 앱 접속
    fun getLongTimeNoSeeMessage(personality: PetPersonality, days: Int): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("${days}일. 어디 있었어.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("${days}일만이야ㅠ 사미시캇타!", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("${days}일 동안 뭐 했어? ...기다렸거든.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("${days}일만이네이! 어디갔었노!", isRed = false)
            PetPersonality.TIMID -> PetDialogue("${days}일... 걱정했어요...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("${days}일만이야! 다시 시작하자!", isRed = false)
        }
    }

    // 앱 차단 시 메시지
    fun getBlockingMessage(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("안 돼. 걸어.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("다메! 먼저 걸어야함!", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("걷기 전엔 안 돼.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("안 되는기라! 걸어!", isRed = true)
            PetPersonality.TIMID -> PetDialogue("저, 먼저 걸어주세요...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("걷고 나서 하자!", isRed = false)
        }
    }

    // 독촉 메시지
    fun getUrgeMessage(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("뭐해. 걸어.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("산책각! 이쿠요~!", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("...갈 거야, 말 거야?", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("빨리 걸으라이!", isRed = true)
            PetPersonality.TIMID -> PetDialogue("저, 나가볼까요...?", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("밖에 나가자! 기분 좋아질 거야!", isRed = false)
        }
    }

    // 야간 메시지
    fun getNightMessage(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "내일 또 걷자."
            PetPersonality.CUTE -> "오야스미~ 오츠카레!"
            PetPersonality.TSUNDERE -> "...푹 쉬어."
            PetPersonality.DIALECT -> "잘 자이소~"
            PetPersonality.TIMID -> "편히 쉬세요..."
            PetPersonality.POSITIVE -> "오늘 수고했어! 굿나잇!"
        }
    }

    // 아침 메시지
    fun getMorningMessage(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "일어나. 걷자."
            PetPersonality.CUTE -> "오하요~! 갓생 살자!"
            PetPersonality.TSUNDERE -> "...일어났어?"
            PetPersonality.DIALECT -> "좋은 아침이라이!"
            PetPersonality.TIMID -> "안, 안녕하세요..."
            PetPersonality.POSITIVE -> "좋은 아침! 오늘도 화이팅!"
        }
    }

    // Chat response based on message content
    fun getChatResponse(personality: PetPersonality, message: String, petName: String, isHappy: Boolean): String {
        // TODO: 테스트 후 삭제 - AI 테스트용 스크립트 비활성화
        return when (personality) {
            PetPersonality.TOUGH -> "뭔 소린지 모르겠다. 걷자."
            PetPersonality.CUTE -> "뭔말인지 모르겠음ㅋㅋ 산책!"
            PetPersonality.TSUNDERE -> "잘 모르겠어. 걷자."
            PetPersonality.DIALECT -> "뭔 말인지 모르것다이. 걷자이!"
            PetPersonality.TIMID -> "저, 뭔지 잘... 걸어요..."
            PetPersonality.POSITIVE -> "잘 모르겠지만 일단 걷자!"
        }

        /* 스크립트 응답 (임시 비활성화)
        val lowerMessage = message.lowercase()

        return when {
            // Weather
            lowerMessage.contains("날씨") || lowerMessage.contains("비") || lowerMessage.contains("해") || lowerMessage.contains("춥") || lowerMessage.contains("더") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "그래. 걷기 딱 좋겠군."
                    PetPersonality.CUTE -> "산책각이다 이쿠요!"
                    PetPersonality.TSUNDERE -> "뭐... 나가볼까."
                    PetPersonality.DIALECT -> "걷기 좋은 날이네이!"
                    PetPersonality.TIMID -> "저, 저도 나가고 싶어요..."
                    PetPersonality.POSITIVE -> "완전 좋아! 걷자!"
                }
            }
            // Greeting (Korean + English)
            lowerMessage.contains("안녕") || lowerMessage.contains("하이") || lowerMessage.contains("헬로") ||
            lowerMessage.contains("hi") || lowerMessage.contains("hello") || lowerMessage.contains("ㅎㅇ") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "어."
                    PetPersonality.CUTE -> "요~ 반가움ㅋㅋ"
                    PetPersonality.TSUNDERE -> "뭐야, 갑자기."
                    PetPersonality.DIALECT -> "안녕하이소!"
                    PetPersonality.TIMID -> "아, 안녕하세요..."
                    PetPersonality.POSITIVE -> "안녕! 반가워!"
                }
            }
            // What are you doing? (뭐해)
            lowerMessage.contains("뭐해") || lowerMessage.contains("뮈해") || lowerMessage.contains("뭐함") ||
            lowerMessage.contains("뭐하") || lowerMessage.contains("머해") || lowerMessage.contains("머하") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "산책 기다리는 중."
                    PetPersonality.CUTE -> "너 기다리는 중임ㅋㅋ 산책 가자!"
                    PetPersonality.TSUNDERE -> "...뭐긴 뭐야. 기다렸어."
                    PetPersonality.DIALECT -> "산책 기다리는 중이라이!"
                    PetPersonality.TIMID -> "저, 저도 기다리고 있었어요..."
                    PetPersonality.POSITIVE -> "산책 갈 준비 중! 같이 가자!"
                }
            }
            // Praise / Love
            lowerMessage.contains("잘했") || lowerMessage.contains("최고") || lowerMessage.contains("사랑") || lowerMessage.contains("고마") || lowerMessage.contains("좋아") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "...고맙다."
                    PetPersonality.CUTE -> "우레시! 아리가토ㅋㅋ"
                    PetPersonality.TSUNDERE -> "뭐, 뭐야... 갑자기..."
                    PetPersonality.DIALECT -> "고맙습니더!"
                    PetPersonality.TIMID -> "저, 정말요...? 감사해요..."
                    PetPersonality.POSITIVE -> "나도 좋아해!"
                }
            }
            // Tired / Hard
            lowerMessage.contains("피곤") || lowerMessage.contains("힘들") || lowerMessage.contains("지친") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "쉬어. 내일 또 걷자."
                    PetPersonality.CUTE -> "오츠카레~ 푹 쉬어!"
                    PetPersonality.TSUNDERE -> "무리하지 마."
                    PetPersonality.DIALECT -> "오늘은 쉬이소!"
                    PetPersonality.TIMID -> "편히 쉬세요..."
                    PetPersonality.POSITIVE -> "내일 또 하면 돼!"
                }
            }
            // Walk / Exercise
            lowerMessage.contains("걷") || lowerMessage.contains("산책") || lowerMessage.contains("운동") ||
            lowerMessage.contains("걸어") || lowerMessage.contains("걸자") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "좋아. 가자."
                    PetPersonality.CUTE -> "산책! 이쿠요!"
                    PetPersonality.TSUNDERE -> "나도... 가고 싶긴 해."
                    PetPersonality.DIALECT -> "걷기 좋은 날이네이!"
                    PetPersonality.TIMID -> "저, 저도 같이요..."
                    PetPersonality.POSITIVE -> "가자가자!"
                }
            }
            // Run / Jogging
            lowerMessage.contains("뛰") || lowerMessage.contains("달려") || lowerMessage.contains("달리") ||
            lowerMessage.contains("조깅") || lowerMessage.contains("러닝") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "좋아. 뛰자."
                    PetPersonality.CUTE -> "달리기! 하시리~!"
                    PetPersonality.TSUNDERE -> "뛰고 싶어? ...나도."
                    PetPersonality.DIALECT -> "달리기 좋은기라!"
                    PetPersonality.TIMID -> "저, 천천히 뛰어요..."
                    PetPersonality.POSITIVE -> "달리자! 기분 좋아질 거야!"
                }
            }
            // Food / Meal
            lowerMessage.contains("밥") || lowerMessage.contains("먹") || lowerMessage.contains("배고") || lowerMessage.contains("맛있") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "밥 먹고 걷자."
                    PetPersonality.CUTE -> "오이시이~ 밥 먹고 산책!"
                    PetPersonality.TSUNDERE -> "배고프면... 먹어. 걱정되니까."
                    PetPersonality.DIALECT -> "밥 먹고 걸으면 소화도 되는기라!"
                    PetPersonality.TIMID -> "저, 맛있게 드세요..."
                    PetPersonality.POSITIVE -> "맛있는 거 먹고 걷자!"
                }
            }
            // Mood / Feeling sad
            lowerMessage.contains("우울") || lowerMessage.contains("슬프") || lowerMessage.contains("짜증") || lowerMessage.contains("화나") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "걸으면 나아질 거야."
                    PetPersonality.CUTE -> "다이죠부! 내가 있잖아~"
                    PetPersonality.TSUNDERE -> "...옆에 있어줄게."
                    PetPersonality.DIALECT -> "걱정 마이소! 다 괜찮아질기라!"
                    PetPersonality.TIMID -> "저, 저도 힘내드릴게요..."
                    PetPersonality.POSITIVE -> "걸으면 기분 좋아져! 같이 가자!"
                }
            }
            // Happy / Good mood
            lowerMessage.contains("기분") || lowerMessage.contains("행복") || lowerMessage.contains("좋다") || lowerMessage.contains("신나") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "좋네."
                    PetPersonality.CUTE -> "와아~ 나도 우레시!"
                    PetPersonality.TSUNDERE -> "흥, 나도... 좋아."
                    PetPersonality.DIALECT -> "좋은기라! 나도 기쁘다이!"
                    PetPersonality.TIMID -> "저, 저도 기뻐요..."
                    PetPersonality.POSITIVE -> "최고다! 같이 기뻐!"
                }
            }
            // Question about pet
            lowerMessage.contains("누구") || lowerMessage.contains("뭐야") || lowerMessage.contains("이름") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "나? ${petName}. 네 파트너."
                    PetPersonality.CUTE -> "나는 ${petName}! 요로시쿠~"
                    PetPersonality.TSUNDERE -> "${petName}야. 기억해."
                    PetPersonality.DIALECT -> "나는 ${petName}이라이!"
                    PetPersonality.TIMID -> "저, 저는 ${petName}이에요..."
                    PetPersonality.POSITIVE -> "${petName}! 함께 걷는 친구야!"
                }
            }
            // Sleep / Night
            lowerMessage.contains("잘자") || lowerMessage.contains("굿나잇") || lowerMessage.contains("자러") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "내일 보자."
                    PetPersonality.CUTE -> "오야스미~ 좋은 꿈 꿔!"
                    PetPersonality.TSUNDERE -> "...잘 자."
                    PetPersonality.DIALECT -> "푹 자이소!"
                    PetPersonality.TIMID -> "편히 주무세요..."
                    PetPersonality.POSITIVE -> "굿나잇! 내일도 화이팅!"
                }
            }
            // Morning
            lowerMessage.contains("좋은 아침") || lowerMessage.contains("일어났") || lowerMessage.contains("굿모닝") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "일어났네. 오늘도 걷자."
                    PetPersonality.CUTE -> "오하요~! 갓생 시작!"
                    PetPersonality.TSUNDERE -> "...늦게 일어났네."
                    PetPersonality.DIALECT -> "좋은 아침이라이!"
                    PetPersonality.TIMID -> "안, 안녕히 주무셨어요...?"
                    PetPersonality.POSITIVE -> "좋은 아침! 오늘도 파이팅!"
                }
            }
            // Work / Study
            lowerMessage.contains("일") || lowerMessage.contains("공부") || lowerMessage.contains("바쁘") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "쉬는 시간에 걸어."
                    PetPersonality.CUTE -> "간바레! 틈틈이 스트레칭!"
                    PetPersonality.TSUNDERE -> "무리하지 마... 잠깐 쉬어."
                    PetPersonality.DIALECT -> "일도 중요하지만 건강도 챙기이소!"
                    PetPersonality.TIMID -> "저, 힘내세요..."
                    PetPersonality.POSITIVE -> "잠깐 걸으면 집중력 올라가!"
                }
            }
            // Bored
            lowerMessage.contains("심심") || lowerMessage.contains("지루") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "걸으러 가면 심심하지 않아."
                    PetPersonality.CUTE -> "심심하면 산책 고고!"
                    PetPersonality.TSUNDERE -> "나랑 산책 가면 되잖아."
                    PetPersonality.DIALECT -> "밖에 나가보이소!"
                    PetPersonality.TIMID -> "저, 저랑 산책 가실래요...?"
                    PetPersonality.POSITIVE -> "같이 걸으면 안 심심해!"
                }
            }
            // Goal / Target
            lowerMessage.contains("목표") || lowerMessage.contains("얼마") || lowerMessage.contains("달성") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "목표는 지키는 거야."
                    PetPersonality.CUTE -> "목표 달성하면 사이코~!"
                    PetPersonality.TSUNDERE -> "못할 줄 알았어? 할 수 있어."
                    PetPersonality.DIALECT -> "목표 달성하면 기분 좋은기라!"
                    PetPersonality.TIMID -> "천천히... 하면 돼요..."
                    PetPersonality.POSITIVE -> "할 수 있어! 목표 달성!"
                }
            }
            // Step count issue / App problem
            lowerMessage.contains("걸음수") || lowerMessage.contains("카운트") || lowerMessage.contains("안 올라") ||
            lowerMessage.contains("안올라") || lowerMessage.contains("안늘어") || lowerMessage.contains("안 늘어") ||
            lowerMessage.contains("안돼") || lowerMessage.contains("안 돼") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "피트니스 앱 연동 확인해봐."
                    PetPersonality.CUTE -> "삼성헬스 연동 확인해봐! 다이죠부!"
                    PetPersonality.TSUNDERE -> "피트니스 앱 연결됐어? 확인해봐."
                    PetPersonality.DIALECT -> "삼성헬스 연동 확인해보이소!"
                    PetPersonality.TIMID -> "저, 피트니스 앱 연결 확인해보세요..."
                    PetPersonality.POSITIVE -> "피트니스 앱 연동하면 해결돼!"
                }
            }
            // Am I doing well? (잘하고 있어?)
            lowerMessage.contains("잘하고") || lowerMessage.contains("잘하나") || lowerMessage.contains("잘해") ||
            lowerMessage.contains("잘하는") || lowerMessage.contains("괜찮") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "잘하고 있어. 계속 가."
                    PetPersonality.CUTE -> "완전 잘하고 있음! 스고이!"
                    PetPersonality.TSUNDERE -> "뭐... 나쁘지 않아."
                    PetPersonality.DIALECT -> "잘하고 있는기라! 힘내라!"
                    PetPersonality.TIMID -> "네, 잘하고 계세요..."
                    PetPersonality.POSITIVE -> "완전 잘하고 있어! 최고야!"
                }
            }
            // Pet commands (짖어, 앉아)
            lowerMessage.contains("짖어") || lowerMessage.contains("앉아") || lowerMessage.contains("기다려") ||
            lowerMessage.contains("손") || lowerMessage.contains("돌아") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "난 강아지가 아니야."
                    PetPersonality.CUTE -> "에~? 그건 못함ㅋㅋ 대신 산책 갈까?"
                    PetPersonality.TSUNDERE -> "...뭐야 그게."
                    PetPersonality.DIALECT -> "그건 안 되는기라ㅋㅋ"
                    PetPersonality.TIMID -> "저, 저는... 그건 못해요..."
                    PetPersonality.POSITIVE -> "그건 못하지만 산책은 갈 수 있어!"
                }
            }
            // Japanese words (이쿠요, 간바레)
            lowerMessage.contains("이쿠") || lowerMessage.contains("간바") || lowerMessage.contains("야바") ||
            lowerMessage.contains("스고이") || lowerMessage.contains("가자") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "가자."
                    PetPersonality.CUTE -> "이쿠요~! 간바루!"
                    PetPersonality.TSUNDERE -> "...가자고."
                    PetPersonality.DIALECT -> "가자이~!"
                    PetPersonality.TIMID -> "네, 가요..."
                    PetPersonality.POSITIVE -> "좋아! 가자!"
                }
            }
            // Emoticons / short expressions
            lowerMessage.contains("ㅡㅡ") || lowerMessage.contains("ㅠㅠ") || lowerMessage.contains("ㅋㅋ") ||
            lowerMessage.contains("ㅎㅎ") || lowerMessage.contains("...") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "뭔가 할 말 있어?"
                    PetPersonality.CUTE -> "왜왜? 무슨 일이야?"
                    PetPersonality.TSUNDERE -> "...뭐야."
                    PetPersonality.DIALECT -> "왜 그런기야?"
                    PetPersonality.TIMID -> "저, 괜찮으세요...?"
                    PetPersonality.POSITIVE -> "무슨 일이야? 얘기해봐!"
                }
            }
            // Encouragement
            lowerMessage.contains("힘") || lowerMessage.contains("응원") || lowerMessage.contains("파이팅") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "난 믿는다. 해내."
                    PetPersonality.CUTE -> "간바레! 파이토!"
                    PetPersonality.TSUNDERE -> "...할 수 있을 거야."
                    PetPersonality.DIALECT -> "힘내라이! 응원한다이!"
                    PetPersonality.TIMID -> "저, 저도 응원해요..."
                    PetPersonality.POSITIVE -> "화이팅! 넌 할 수 있어!"
                }
            }
            // Default - 인식 못하는 말
            else -> getFallbackResponse(personality, petName)
        }
        스크립트 응답 끝 */
    }

    // 인식 못하는 말에 대한 폴백 응답 (3가지 중 랜덤)
    fun getFallbackResponse(personality: PetPersonality, petName: String): String {
        val responses = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "뭔 소린지 모르겠다. 일단 걷자.",
                "... 걷기나 하자.",
                "그래. 뭐든 걸으면 해결돼."
            )
            PetPersonality.CUTE -> listOf(
                "와카라나이... 그냥 산책 갈까?",
                "으잉? 뭔말인지 모르겠음ㅋㅋ 산책!",
                "난 잘 모르겠는데 일단 이쿠요!"
            )
            PetPersonality.TSUNDERE -> listOf(
                "뭐... 잘 모르겠어. 그냥 걸을래?",
                "이해 안 돼. 걷자.",
                "...뭔 소리야. 산책이나 가자."
            )
            PetPersonality.DIALECT -> listOf(
                "뭔 말인지 모르것다이. 걷자이!",
                "잘 모르겠는기라~ 산책 가자이!",
                "어렵네이. 일단 걸어보자이!"
            )
            PetPersonality.TIMID -> listOf(
                "저, 저... 잘 모르겠어요... 산책 갈까요...?",
                "어려워요... 걷기나 할래요...?",
                "저, 뭔지 잘... 그냥 걸어요..."
            )
            PetPersonality.POSITIVE -> listOf(
                "잘 모르겠지만 일단 걷자!",
                "뭐든 걸으면 좋아! 가자!",
                "생각은 걸으면서! 이쿠요!"
            )
        }
        return responses.random()
    }

    // Streak celebration
    fun getStreakMessage(personality: PetPersonality, streakDays: Int): String {
        return when (personality) {
            PetPersonality.TOUGH -> "${streakDays}일째. 멋지다."
            PetPersonality.CUTE -> "${streakDays}일 연속! 스고이ㅋㅋㅋ"
            PetPersonality.TSUNDERE -> "${streakDays}일이나 됐네. ...대단해."
            PetPersonality.DIALECT -> "${streakDays}일째라이! 짱이다!"
            PetPersonality.TIMID -> "${streakDays}일... 대, 대단해요..."
            PetPersonality.POSITIVE -> "${streakDays}일 연속! 최고야!"
        }
    }

    // Random chat responses (for talk feature)
    fun getRandomChatResponse(personality: PetPersonality, petName: String): String {
        val responses = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "오늘도 해내자.",
                "난 믿는다.",
                "가자.",
                "괜찮아.",
                "할 수 있어."
            )
            PetPersonality.CUTE -> listOf(
                "오늘도 간바루!",
                "같이 있어서 우레시~",
                "다이스키!ㅋㅋ",
                "힘내자구 파이토!",
                "사이코! 넌 최고임"
            )
            PetPersonality.TSUNDERE -> listOf(
                "뭐야, 심심해?",
                "...옆에 있어줄게.",
                "별거 아니야.",
                "흥, 고마워하지마.",
                "...잘하고 있어."
            )
            PetPersonality.DIALECT -> listOf(
                "오늘도 힘내이소~",
                "같이 있어서 좋은기라!",
                "화이팅이라이!",
                "잘하고 있는기야!",
                "대단하이소!"
            )
            PetPersonality.TIMID -> listOf(
                "저, 응원할게요...",
                "함께라서... 좋아요...",
                "힘, 힘내세요...",
                "잘하고 계세요...",
                "저도... 기뻐요..."
            )
            PetPersonality.POSITIVE -> listOf(
                "오늘도 좋은 하루!",
                "함께여서 행복해!",
                "넌 최고야!",
                "할 수 있어!",
                "화이팅!"
            )
        }
        return responses.random()
    }
}

/**
 * Pet data class for saving/loading
 */
data class Pet(
    val type: PetType,
    var name: String,
    var happinessLevel: Int = 5, // 1-5 hearts
    var totalWalkedSteps: Long = 0
) {
    val personality: PetPersonality
        get() = type.personality

    val displayName: String
        get() = type.displayName

    fun getMessage(progressPercent: Int, isWalking: Boolean): String {
        return if (isWalking || progressPercent > 0) {
            PetDialogues.getWalkingMessage(personality, progressPercent)
        } else {
            PetDialogues.getIdleMessage(personality)
        }
    }
}
