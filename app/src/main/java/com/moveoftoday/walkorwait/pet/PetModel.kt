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
        val lowerMessage = message.lowercase()

        return when {
            // Weather
            lowerMessage.contains("날씨") || lowerMessage.contains("비") || lowerMessage.contains("해") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "그래. 걷기 딱 좋겠군."
                    PetPersonality.CUTE -> "산책각이다 이쿠요!"
                    PetPersonality.TSUNDERE -> "뭐... 나가볼까."
                    PetPersonality.DIALECT -> "걷기 좋은 날이네이!"
                    PetPersonality.TIMID -> "저, 저도 나가고 싶어요..."
                    PetPersonality.POSITIVE -> "완전 좋아! 걷자!"
                }
            }
            // Greeting
            lowerMessage.contains("안녕") || lowerMessage.contains("하이") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "어."
                    PetPersonality.CUTE -> "요~ 반가움ㅋㅋ"
                    PetPersonality.TSUNDERE -> "뭐야, 갑자기."
                    PetPersonality.DIALECT -> "안녕하이소!"
                    PetPersonality.TIMID -> "아, 안녕하세요..."
                    PetPersonality.POSITIVE -> "안녕! 반가워!"
                }
            }
            // Praise
            lowerMessage.contains("잘했") || lowerMessage.contains("최고") || lowerMessage.contains("사랑") || lowerMessage.contains("고마") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "...고맙다."
                    PetPersonality.CUTE -> "우레시! 아리가토ㅋㅋ"
                    PetPersonality.TSUNDERE -> "뭐, 뭐야... 갑자기..."
                    PetPersonality.DIALECT -> "고맙습니더!"
                    PetPersonality.TIMID -> "저, 정말요...? 감사해요..."
                    PetPersonality.POSITIVE -> "나도 좋아해!"
                }
            }
            // Tired
            lowerMessage.contains("피곤") || lowerMessage.contains("힘들") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "쉬어. 내일 또 걷자."
                    PetPersonality.CUTE -> "오츠카레~ 푹 쉬어!"
                    PetPersonality.TSUNDERE -> "무리하지 마."
                    PetPersonality.DIALECT -> "오늘은 쉬이소!"
                    PetPersonality.TIMID -> "편히 쉬세요..."
                    PetPersonality.POSITIVE -> "내일 또 하면 돼!"
                }
            }
            // Walk
            lowerMessage.contains("걷") || lowerMessage.contains("산책") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "좋아. 가자."
                    PetPersonality.CUTE -> "산책! 이쿠요!"
                    PetPersonality.TSUNDERE -> "나도... 가고 싶긴 해."
                    PetPersonality.DIALECT -> "걷기 좋은 날이네이!"
                    PetPersonality.TIMID -> "저, 저도 같이요..."
                    PetPersonality.POSITIVE -> "가자가자!"
                }
            }
            // Default
            else -> getRandomChatResponse(personality, petName)
        }
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
