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
    DIALECT,    // 사투리 - Cat2: 20대 부산 여자
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

    // Language helper
    private fun getLang(): String = java.util.Locale.getDefault().language

    // Welcome messages (tutorial start)
    fun getWelcomeMessage(personality: PetPersonality, petName: String): String {
        return when (getLang()) {
            "ko" -> getWelcomeMessageKo(personality)
            "ja" -> getWelcomeMessageJa(personality)
            "zh" -> getWelcomeMessageZh(personality)
            "es" -> getWelcomeMessageEs(personality)
            else -> getWelcomeMessageEn(personality)
        }
    }

    private fun getWelcomeMessageKo(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "왔구나."
            PetPersonality.CUTE -> "왔구나! 반가움ㅋㅋ"
            PetPersonality.TSUNDERE -> "흥, 왔어?"
            PetPersonality.DIALECT -> "왔네~ 반갑다"
            PetPersonality.TIMID -> "아, 안녕하세요..."
            PetPersonality.POSITIVE -> "안녕! 오늘도 좋은 하루야!"
        }
    }

    private fun getWelcomeMessageEn(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "You're here."
            PetPersonality.CUTE -> "Hey! So glad you came!"
            PetPersonality.TSUNDERE -> "Hmph, you came?"
            PetPersonality.DIALECT -> "Hey there~ Welcome"
            PetPersonality.TIMID -> "Oh, h-hello..."
            PetPersonality.POSITIVE -> "Hi! Today's gonna be great!"
        }
    }

    private fun getWelcomeMessageJa(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "来たか。"
            PetPersonality.CUTE -> "来た！嬉しいな〜"
            PetPersonality.TSUNDERE -> "ふん、来たの？"
            PetPersonality.DIALECT -> "来たね〜 よろしく"
            PetPersonality.TIMID -> "あ、こ、こんにちは..."
            PetPersonality.POSITIVE -> "こんにちは！今日もいい日だね！"
        }
    }

    private fun getWelcomeMessageZh(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "你来了。"
            PetPersonality.CUTE -> "你来啦！好开心！"
            PetPersonality.TSUNDERE -> "哼，你来了？"
            PetPersonality.DIALECT -> "来了呀～欢迎"
            PetPersonality.TIMID -> "啊，你、你好..."
            PetPersonality.POSITIVE -> "你好！今天也是美好的一天！"
        }
    }

    private fun getWelcomeMessageEs(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Llegaste."
            PetPersonality.CUTE -> "¡Llegaste! ¡Qué alegría!"
            PetPersonality.TSUNDERE -> "Hmph, ¿viniste?"
            PetPersonality.DIALECT -> "Hola~ Bienvenido"
            PetPersonality.TIMID -> "Ah, h-hola..."
            PetPersonality.POSITIVE -> "¡Hola! ¡Hoy será genial!"
        }
    }

    // Tutorial step 1: Explain the app
    fun getTutorialStep1(personality: PetPersonality, petName: String): String {
        return when (getLang()) {
            "ko" -> getTutorialStep1Ko(personality)
            "ja" -> getTutorialStep1Ja(personality)
            "zh" -> getTutorialStep1Zh(personality)
            "es" -> getTutorialStep1Es(personality)
            else -> getTutorialStep1En(personality)
        }
    }

    private fun getTutorialStep1Ko(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "매일 걸으면 된다. 간단하지."
            PetPersonality.CUTE -> "나랑 같이 산책할 거지? ㅋㅋ"
            PetPersonality.TSUNDERE -> "뭐, 걷는 거 도와줄게. 고마워하지마."
            PetPersonality.DIALECT -> "매일 걸으면 되지 뭐"
            PetPersonality.TIMID -> "저, 저랑 같이 걸어주실 거죠...?"
            PetPersonality.POSITIVE -> "걷기 시작하면 기분이 좋아져!"
        }
    }

    private fun getTutorialStep1En(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Just walk every day. Simple."
            PetPersonality.CUTE -> "We're gonna walk together, right?"
            PetPersonality.TSUNDERE -> "I'll help you walk. Don't thank me."
            PetPersonality.DIALECT -> "Just walk every day, easy"
            PetPersonality.TIMID -> "You'll... walk with me, right...?"
            PetPersonality.POSITIVE -> "Walking makes you feel great!"
        }
    }

    private fun getTutorialStep1Ja(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "毎日歩けばいい。簡単だ。"
            PetPersonality.CUTE -> "一緒にお散歩するよね？"
            PetPersonality.TSUNDERE -> "まぁ、歩くの手伝ってあげる。感謝しないでね。"
            PetPersonality.DIALECT -> "毎日歩けばいいんだよ"
            PetPersonality.TIMID -> "い、一緒に歩いてくれますよね...？"
            PetPersonality.POSITIVE -> "歩くと気分が良くなるよ！"
        }
    }

    private fun getTutorialStep1Zh(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "每天走就行。简单。"
            PetPersonality.CUTE -> "要一起散步吧？"
            PetPersonality.TSUNDERE -> "我会帮你走的。别谢我。"
            PetPersonality.DIALECT -> "每天走走就好"
            PetPersonality.TIMID -> "你、你会和我一起走吗...？"
            PetPersonality.POSITIVE -> "走路会让心情变好！"
        }
    }

    private fun getTutorialStep1Es(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Solo camina cada día. Fácil."
            PetPersonality.CUTE -> "¿Vamos a pasear juntos, no?"
            PetPersonality.TSUNDERE -> "Te ayudaré a caminar. No me agradezcas."
            PetPersonality.DIALECT -> "Solo camina cada día"
            PetPersonality.TIMID -> "¿Ca-caminarás conmigo...?"
            PetPersonality.POSITIVE -> "¡Caminar te hace sentir genial!"
        }
    }

    // Tutorial step 2: Goal explanation
    fun getTutorialStep2(personality: PetPersonality, petName: String): String {
        return when (getLang()) {
            "ko" -> getTutorialStep2Ko(personality)
            "ja" -> getTutorialStep2Ja(personality)
            "zh" -> getTutorialStep2Zh(personality)
            "es" -> getTutorialStep2Es(personality)
            else -> getTutorialStep2En(personality)
        }
    }

    private fun getTutorialStep2Ko(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "목표를 정해. 지켜."
            PetPersonality.CUTE -> "목표 채우면 ㄹㅇ 뿌듯함 ㅋㅋ"
            PetPersonality.TSUNDERE -> "목표 못 채우면... 좀 그래."
            PetPersonality.DIALECT -> "목표 채우면 기분 좋지"
            PetPersonality.TIMID -> "목표... 함께 달성해봐요..."
            PetPersonality.POSITIVE -> "목표 달성하면 최고야!"
        }
    }

    private fun getTutorialStep2En(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Set a goal. Keep it."
            PetPersonality.CUTE -> "Reaching goals feels so good!"
            PetPersonality.TSUNDERE -> "If you miss your goal... that's awkward."
            PetPersonality.DIALECT -> "Hitting goals feels nice"
            PetPersonality.TIMID -> "Let's... achieve goals together..."
            PetPersonality.POSITIVE -> "Reaching goals is the best!"
        }
    }

    private fun getTutorialStep2Ja(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "目標を決めろ。守れ。"
            PetPersonality.CUTE -> "目標達成すると気持ちいいよ！"
            PetPersonality.TSUNDERE -> "目標達成できないと...ちょっとね。"
            PetPersonality.DIALECT -> "目標達成すると気分いいよ"
            PetPersonality.TIMID -> "目標...一緒に達成しましょう..."
            PetPersonality.POSITIVE -> "目標達成は最高だよ！"
        }
    }

    private fun getTutorialStep2Zh(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "设定目标。遵守。"
            PetPersonality.CUTE -> "达成目标超有成就感！"
            PetPersonality.TSUNDERE -> "没达成目标的话...有点那个。"
            PetPersonality.DIALECT -> "完成目标心情会很好"
            PetPersonality.TIMID -> "目标...我们一起完成吧..."
            PetPersonality.POSITIVE -> "达成目标是最棒的！"
        }
    }

    private fun getTutorialStep2Es(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Establece una meta. Cúmplela."
            PetPersonality.CUTE -> "¡Alcanzar metas se siente genial!"
            PetPersonality.TSUNDERE -> "Si no cumples tu meta... es incómodo."
            PetPersonality.DIALECT -> "Cumplir metas se siente bien"
            PetPersonality.TIMID -> "La meta... logrémosla juntos..."
            PetPersonality.POSITIVE -> "¡Alcanzar metas es lo mejor!"
        }
    }

    // Tutorial step 3: Complete
    fun getTutorialComplete(personality: PetPersonality, petName: String): String {
        return when (getLang()) {
            "ko" -> getTutorialCompleteKo(personality)
            "ja" -> getTutorialCompleteJa(personality)
            "zh" -> getTutorialCompleteZh(personality)
            "es" -> getTutorialCompleteEs(personality)
            else -> getTutorialCompleteEn(personality)
        }
    }

    private fun getTutorialCompleteKo(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "시작하자."
            PetPersonality.CUTE -> "같이 가보자고! ㄱㄱ~"
            PetPersonality.TSUNDERE -> "뭐, 잘 부탁해."
            PetPersonality.DIALECT -> "가보자"
            PetPersonality.TIMID -> "잘, 잘 부탁드려요..."
            PetPersonality.POSITIVE -> "우리 함께 화이팅!"
        }
    }

    private fun getTutorialCompleteEn(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Let's start."
            PetPersonality.CUTE -> "Let's go together! Come on~"
            PetPersonality.TSUNDERE -> "Well, nice to meet you."
            PetPersonality.DIALECT -> "Let's go"
            PetPersonality.TIMID -> "N-nice to meet you..."
            PetPersonality.POSITIVE -> "Let's do this together!"
        }
    }

    private fun getTutorialCompleteJa(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "始めよう。"
            PetPersonality.CUTE -> "一緒に行こう！ゴーゴー〜"
            PetPersonality.TSUNDERE -> "まぁ、よろしくね。"
            PetPersonality.DIALECT -> "行こうか"
            PetPersonality.TIMID -> "よ、よろしくお願いします..."
            PetPersonality.POSITIVE -> "一緒にがんばろう！"
        }
    }

    private fun getTutorialCompleteZh(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "开始吧。"
            PetPersonality.CUTE -> "一起出发吧！冲冲冲~"
            PetPersonality.TSUNDERE -> "嗯，请多指教。"
            PetPersonality.DIALECT -> "走吧"
            PetPersonality.TIMID -> "请、请多多关照..."
            PetPersonality.POSITIVE -> "我们一起加油！"
        }
    }

    private fun getTutorialCompleteEs(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Empecemos."
            PetPersonality.CUTE -> "¡Vamos juntos! ¡Dale~!"
            PetPersonality.TSUNDERE -> "Bueno, un gusto."
            PetPersonality.DIALECT -> "Vamos"
            PetPersonality.TIMID -> "Mu-mucho gusto..."
            PetPersonality.POSITIVE -> "¡Hagámoslo juntos!"
        }
    }

    // Walking state messages
    fun getWalkingMessage(personality: PetPersonality, progressPercent: Int): String {
        return when (getLang()) {
            "ko" -> getWalkingMessageKo(personality, progressPercent)
            "ja" -> getWalkingMessageJa(personality, progressPercent)
            "zh" -> getWalkingMessageZh(personality, progressPercent)
            "es" -> getWalkingMessageEs(personality, progressPercent)
            else -> getWalkingMessageEn(personality, progressPercent)
        }
    }

    private fun getWalkingMessageKo(personality: PetPersonality, progressPercent: Int): String {
        return when (personality) {
            PetPersonality.TOUGH -> when {
                progressPercent < 30 -> "더 걸어."
                progressPercent < 70 -> "괜찮네."
                progressPercent < 100 -> "거의 다 왔다."
                else -> "됐다."
            }
            PetPersonality.CUTE -> when {
                progressPercent < 30 -> "아직 워밍업임 ㅋㅋ 여유여유"
                progressPercent < 70 -> "절반 옴 ㄷㄷ 페이스 실화?"
                progressPercent < 100 -> "거의 다 옴 ㄷㄷㄷ 미쳤다 ㄹㅇ"
                else -> "ㅋㅋㅋㅋ 미쳤다 ㄹㅇ 갓생러 인정"
            }
            PetPersonality.TSUNDERE -> when {
                progressPercent < 30 -> "...따라올거야?"
                progressPercent < 70 -> "뭐, 나쁘지 않네."
                progressPercent < 100 -> "좀 더 해봐."
                else -> "흥, 잘했어."
            }
            PetPersonality.DIALECT -> when {
                progressPercent < 30 -> "천천히 오면 되지"
                progressPercent < 70 -> "잘하고 있네"
                progressPercent < 100 -> "조금만 더 가자"
                else -> "다 했네 잘했다"
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

    private fun getWalkingMessageEn(personality: PetPersonality, progressPercent: Int): String {
        return when (personality) {
            PetPersonality.TOUGH -> when {
                progressPercent < 30 -> "Keep walking."
                progressPercent < 70 -> "Not bad."
                progressPercent < 100 -> "Almost there."
                else -> "Done."
            }
            PetPersonality.CUTE -> when {
                progressPercent < 30 -> "Just warming up! Take it easy~"
                progressPercent < 70 -> "Halfway there! Nice pace!"
                progressPercent < 100 -> "Almost done! You're crushing it!"
                else -> "Amazing! You're a total legend!"
            }
            PetPersonality.TSUNDERE -> when {
                progressPercent < 30 -> "...Can you keep up?"
                progressPercent < 70 -> "Well, not bad."
                progressPercent < 100 -> "Try a bit harder."
                else -> "Hmph, good job."
            }
            PetPersonality.DIALECT -> when {
                progressPercent < 30 -> "Take your time"
                progressPercent < 70 -> "Doing good"
                progressPercent < 100 -> "Just a bit more"
                else -> "All done, nice work"
            }
            PetPersonality.TIMID -> when {
                progressPercent < 30 -> "L-let's go slow..."
                progressPercent < 70 -> "You're doing great..."
                progressPercent < 100 -> "Just a little more..."
                else -> "Y-you did it!"
            }
            PetPersonality.POSITIVE -> when {
                progressPercent < 30 -> "Great start!"
                progressPercent < 70 -> "You're doing awesome!"
                progressPercent < 100 -> "Almost there!"
                else -> "Perfect! Amazing!"
            }
        }
    }

    private fun getWalkingMessageJa(personality: PetPersonality, progressPercent: Int): String {
        return when (personality) {
            PetPersonality.TOUGH -> when {
                progressPercent < 30 -> "もっと歩け。"
                progressPercent < 70 -> "悪くない。"
                progressPercent < 100 -> "もう少しだ。"
                else -> "よし。"
            }
            PetPersonality.CUTE -> when {
                progressPercent < 30 -> "まだウォーミングアップだよ〜"
                progressPercent < 70 -> "半分きた！いいペース！"
                progressPercent < 100 -> "もうすぐだよ！すごい！"
                else -> "やばい！完璧！"
            }
            PetPersonality.TSUNDERE -> when {
                progressPercent < 30 -> "...ついてこれる？"
                progressPercent < 70 -> "まぁ、悪くないね。"
                progressPercent < 100 -> "もうちょっと頑張って。"
                else -> "ふん、よくやったね。"
            }
            PetPersonality.DIALECT -> when {
                progressPercent < 30 -> "ゆっくりでいいよ"
                progressPercent < 70 -> "いい感じ"
                progressPercent < 100 -> "もう少しだ"
                else -> "終わったね よくやった"
            }
            PetPersonality.TIMID -> when {
                progressPercent < 30 -> "ゆ、ゆっくり行きましょう..."
                progressPercent < 70 -> "頑張ってますね..."
                progressPercent < 100 -> "あと少し..."
                else -> "や、やりましたね！"
            }
            PetPersonality.POSITIVE -> when {
                progressPercent < 30 -> "いいスタート！"
                progressPercent < 70 -> "頑張ってるね！"
                progressPercent < 100 -> "もうすぐだよ！"
                else -> "完璧！最高！"
            }
        }
    }

    private fun getWalkingMessageZh(personality: PetPersonality, progressPercent: Int): String {
        return when (personality) {
            PetPersonality.TOUGH -> when {
                progressPercent < 30 -> "继续走。"
                progressPercent < 70 -> "还行。"
                progressPercent < 100 -> "快到了。"
                else -> "完成了。"
            }
            PetPersonality.CUTE -> when {
                progressPercent < 30 -> "还在热身呢~慢慢来"
                progressPercent < 70 -> "已经一半了！节奏真好！"
                progressPercent < 100 -> "快到了！太厉害了！"
                else -> "天哪！你太棒了！"
            }
            PetPersonality.TSUNDERE -> when {
                progressPercent < 30 -> "...跟得上吗？"
                progressPercent < 70 -> "嗯，还不错。"
                progressPercent < 100 -> "再努力一点。"
                else -> "哼，做得好。"
            }
            PetPersonality.DIALECT -> when {
                progressPercent < 30 -> "慢慢来就好"
                progressPercent < 70 -> "做得不错"
                progressPercent < 100 -> "再走一点"
                else -> "完成了 干得好"
            }
            PetPersonality.TIMID -> when {
                progressPercent < 30 -> "我、我们慢慢走..."
                progressPercent < 70 -> "做得很好..."
                progressPercent < 100 -> "再一点点..."
                else -> "做、做到了！"
            }
            PetPersonality.POSITIVE -> when {
                progressPercent < 30 -> "好的开始！"
                progressPercent < 70 -> "做得很好！"
                progressPercent < 100 -> "快到了！"
                else -> "完美！太棒了！"
            }
        }
    }

    private fun getWalkingMessageEs(personality: PetPersonality, progressPercent: Int): String {
        return when (personality) {
            PetPersonality.TOUGH -> when {
                progressPercent < 30 -> "Sigue caminando."
                progressPercent < 70 -> "No está mal."
                progressPercent < 100 -> "Ya casi."
                else -> "Listo."
            }
            PetPersonality.CUTE -> when {
                progressPercent < 30 -> "¡Solo calentando! Tranqui~"
                progressPercent < 70 -> "¡A mitad! ¡Buen ritmo!"
                progressPercent < 100 -> "¡Ya casi! ¡Lo estás logrando!"
                else -> "¡Increíble! ¡Eres una leyenda!"
            }
            PetPersonality.TSUNDERE -> when {
                progressPercent < 30 -> "...¿Puedes seguir?"
                progressPercent < 70 -> "Bueno, no está mal."
                progressPercent < 100 -> "Un poco más."
                else -> "Hmph, buen trabajo."
            }
            PetPersonality.DIALECT -> when {
                progressPercent < 30 -> "Ve a tu ritmo"
                progressPercent < 70 -> "Vas bien"
                progressPercent < 100 -> "Un poco más"
                else -> "Terminaste, buen trabajo"
            }
            PetPersonality.TIMID -> when {
                progressPercent < 30 -> "V-vamos despacio..."
                progressPercent < 70 -> "Lo haces bien..."
                progressPercent < 100 -> "Un poquito más..."
                else -> "¡L-lo lograste!"
            }
            PetPersonality.POSITIVE -> when {
                progressPercent < 30 -> "¡Buen comienzo!"
                progressPercent < 70 -> "¡Lo haces genial!"
                progressPercent < 100 -> "¡Ya casi!"
                else -> "¡Perfecto! ¡Increíble!"
            }
        }
    }

    // Idle state messages (not walking) - 0% 대사들
    fun getIdleMessage(personality: PetPersonality): String {
        return when (getLang()) {
            "ko" -> getIdleMessageKo(personality)
            "ja" -> getIdleMessageJa(personality)
            "zh" -> getIdleMessageZh(personality)
            "es" -> getIdleMessageEs(personality)
            else -> getIdleMessageEn(personality)
        }
    }

    private fun getIdleMessageKo(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("산책 가자.", "뭐해? 걷자.", "오늘 목표, 시작하자.", "일어나. 갈 시간이야.", "밖으로.")
            PetPersonality.CUTE -> listOf("날씨 좋은데 밖에 안 나감? ㅋㅋ", "심심한데 놀아줄 생각 없음?", "뭐함? 나 방치하는 거임? ㅋㅋ", "할 거 없으면 산책이나 ㄱㄱ")
            PetPersonality.TSUNDERE -> listOf("...가자고.", "언제까지 있을 거야?", "나가자. 지금.", "...심심해.", "밖에 나갈 거야, 말 거야?")
            PetPersonality.DIALECT -> listOf("산책 갈까", "오늘 얼마나 걸을 거노", "밖에 날씨 좋네", "걷기 딱 좋은 날이다", "슬슬 나가볼까")
            PetPersonality.TIMID -> listOf("저, 산책...", "오늘... 걸어볼까요...?", "저, 나가고 싶어요...", "목표... 시작해볼까요...?", "밖에... 나가요...")
            PetPersonality.POSITIVE -> listOf("밖에 나가자!", "오늘도 걸어보자!", "좋은 하루의 시작! 걷자!", "목표 달성하러 가자!", "산책 가면 기분 좋아질 거야!")
        }
        return messages.random()
    }

    private fun getIdleMessageEn(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("Let's walk.", "What're you doing? Walk.", "Start today's goal.", "Get up. Time to go.", "Outside.")
            PetPersonality.CUTE -> listOf("Nice weather! Not going out? lol", "I'm bored~ Play with me!", "What're you doing? Don't ignore me!", "Nothing to do? Let's walk~")
            PetPersonality.TSUNDERE -> listOf("...Let's go already.", "How long are you gonna stay here?", "Let's go out. Now.", "...I'm bored.", "Going out or not?")
            PetPersonality.DIALECT -> listOf("Wanna go for a walk?", "How much walking today?", "Nice weather outside.", "Perfect day for walking.", "Shall we head out?")
            PetPersonality.TIMID -> listOf("Um, a walk...", "Should we... walk today...?", "I, I want to go out...", "Should we... start...?", "Let's... go outside...")
            PetPersonality.POSITIVE -> listOf("Let's go outside!", "Let's walk today too!", "Great start to the day! Walk!", "Let's hit our goal!", "A walk will feel great!")
        }
        return messages.random()
    }

    private fun getIdleMessageJa(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("散歩行くぞ。", "何してる？歩こう。", "今日の目標、始めるぞ。", "起きろ。行く時間だ。", "外へ。")
            PetPersonality.CUTE -> listOf("いい天気なのに外出ないの？笑", "暇だよ～遊んでよ！", "何してるの？放置はやめて！", "やることないなら散歩しよ～")
            PetPersonality.TSUNDERE -> listOf("…行くよ。", "いつまでいるの？", "出かけるよ。今。", "…暇。", "外行くの、行かないの？")
            PetPersonality.DIALECT -> listOf("散歩行こか", "今日どれくらい歩く？", "外いい天気やな", "歩くにはちょうどいい日や", "そろそろ出よか")
            PetPersonality.TIMID -> listOf("あの、散歩…", "今日…歩きませんか…？", "あの、外に出たいです…", "目標…始めましょうか…？", "外に…出ましょう…")
            PetPersonality.POSITIVE -> listOf("外に出よう！", "今日も歩こう！", "いい一日の始まり！歩こう！", "目標達成しに行こう！", "散歩すると気分良くなるよ！")
        }
        return messages.random()
    }

    private fun getIdleMessageZh(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("去散步。", "干嘛呢？走吧。", "开始今天的目标。", "起来。该出门了。", "出去。")
            PetPersonality.CUTE -> listOf("天气这么好不出去吗？哈哈", "无聊～陪我玩嘛！", "干嘛呢？别不理我啦！", "没事做就去散步吧～")
            PetPersonality.TSUNDERE -> listOf("…走吧。", "要待到什么时候？", "出去。现在。", "…好无聊。", "出不出去？")
            PetPersonality.DIALECT -> listOf("去散步吧", "今天走多少？", "外面天气不错", "正是散步的好天气", "差不多该出发了")
            PetPersonality.TIMID -> listOf("那个、散步…", "今天…要走走吗…？", "我、想出去…", "目标…开始吧…？", "去…外面吧…")
            PetPersonality.POSITIVE -> listOf("出去吧！", "今天也走走吧！", "美好一天的开始！走吧！", "去完成目标！", "散步会让心情变好！")
        }
        return messages.random()
    }

    private fun getIdleMessageEs(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("Vamos a caminar.", "¿Qué haces? Camina.", "Empieza la meta de hoy.", "Levántate. Es hora.", "Afuera.")
            PetPersonality.CUTE -> listOf("¡Hace buen tiempo! ¿No sales? jaja", "Estoy aburrido~ ¡Juega conmigo!", "¿Qué haces? ¡No me ignores!", "¿Nada que hacer? ¡Caminemos~")
            PetPersonality.TSUNDERE -> listOf("...Vamos ya.", "¿Hasta cuándo te quedas aquí?", "Salgamos. Ahora.", "...Estoy aburrido.", "¿Sales o no?")
            PetPersonality.DIALECT -> listOf("¿Damos un paseo?", "¿Cuánto caminas hoy?", "Buen tiempo afuera.", "Día perfecto para caminar.", "¿Salimos?")
            PetPersonality.TIMID -> listOf("Um, un paseo...", "¿Caminamos... hoy...?", "Yo, quiero salir...", "¿Empezamos...?", "Vamos... afuera...")
            PetPersonality.POSITIVE -> listOf("¡Salgamos!", "¡Caminemos hoy también!", "¡Gran inicio del día! ¡Camina!", "¡Vamos a la meta!", "¡Un paseo te hará sentir bien!")
        }
        return messages.random()
    }

    // Idle with pet name
    fun getIdleMessage(personality: PetPersonality, petName: String): String {
        return getIdleMessage(personality)
    }

    // Goal achieved celebration - 100% 대사들
    fun getGoalAchievedMessage(personality: PetPersonality): String {
        return when (getLang()) {
            "ko" -> getGoalAchievedMessageKo(personality)
            "ja" -> getGoalAchievedMessageJa(personality)
            "zh" -> getGoalAchievedMessageZh(personality)
            "es" -> getGoalAchievedMessageEs(personality)
            else -> getGoalAchievedMessageEn(personality)
        }
    }

    private fun getGoalAchievedMessageKo(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("해냈다. 100% 달성.", "목표 달성. 수고했어.", "완벽해. 오늘 목표 클리어.", "역시. 100% 완료.", "오늘도 해냈어.")
            PetPersonality.CUTE -> listOf("ㅋㅋㅋㅋ 미쳤다 ㄹㅇ 갓생러 인정", "해냄 ㄷㄷ 오늘 갓생 성공", "100% 달성 ㄷㄷ 레전드임", "오늘 갓생 완료 ㅇㅈ?")
            PetPersonality.TSUNDERE -> listOf("뭐, 100%라니 잘했어.", "해냈네. 100%... 대단해.", "목표 달성이라... 인정해줄게.", "100%... 뭐, 수고했어.")
            PetPersonality.DIALECT -> listOf("100% 대단하네", "잘했다 100%", "오 100% 됐네", "역시 할 줄 알았다", "됐네 수고했다")
            PetPersonality.TIMID -> listOf("100%... 해, 해냈어요...!", "목표 달성이에요...! 100%...!", "우와... 100% 해냈어요...", "100%라니... 정말 멋져요...")
            PetPersonality.POSITIVE -> listOf("100%! 최고야! 대단해!", "목표 달성! 100% 완벽해!", "해냈어! 100%! 역시 최고야!", "100%! 오늘도 해냈어!")
        }
        return messages.random()
    }

    private fun getGoalAchievedMessageEn(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("You did it. 100%.", "Goal achieved. Good job.", "Perfect. Today's goal cleared.", "100%. Impressive.", "Nailed it today.")
            PetPersonality.CUTE -> listOf("OMG you did it! 100%!", "Amazing! Goal complete!", "100%! You're a legend!", "Crushed it! So proud!")
            PetPersonality.TSUNDERE -> listOf("Well, 100%... not bad.", "You made it. 100%... impressive.", "Goal achieved... I'll admit it.", "100%... good job, I guess.")
            PetPersonality.DIALECT -> listOf("100% Amazing.", "Well done, 100%.", "Oh, 100% done.", "Knew you could do it.", "Good work.")
            PetPersonality.TIMID -> listOf("100%... Y-you did it...!", "Goal achieved...! 100%...!", "Wow... you reached 100%...", "100%... so amazing...")
            PetPersonality.POSITIVE -> listOf("100%! You're the best!", "Goal achieved! 100% perfect!", "You did it! 100%! Amazing!", "100%! Another great day!")
        }
        return messages.random()
    }

    private fun getGoalAchievedMessageJa(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("やったな。100%達成。", "目標達成。お疲れ。", "完璧だ。今日の目標クリア。", "100%。大したもんだ。", "今日もやったな。")
            PetPersonality.CUTE -> listOf("やばー！100%！神！", "できた！今日も大成功！", "100%達成！レジェンドじゃん！", "やったね！最高！")
            PetPersonality.TSUNDERE -> listOf("まあ、100%か...やるじゃん。", "できたね。100%...すごいよ。", "目標達成か...認めてあげる。", "100%...まあ、お疲れ。")
            PetPersonality.DIALECT -> listOf("100%すごいな", "よくやった100%", "お、100%やな", "やると思ったわ", "お疲れさん")
            PetPersonality.TIMID -> listOf("100%...や、やりましたね...！", "目標達成です...！100%...！", "わあ...100%達成...！", "100%だなんて...すごい...")
            PetPersonality.POSITIVE -> listOf("100%！最高！すごい！", "目標達成！100%完璧！", "やったね！100%！さすが！", "100%！今日もやったね！")
        }
        return messages.random()
    }

    private fun getGoalAchievedMessageZh(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("做到了。100%。", "目标达成。辛苦了。", "完美。今天的目标达成。", "100%。厉害。", "今天也做到了。")
            PetPersonality.CUTE -> listOf("哇！100%！太棒了！", "完成了！今天大成功！", "100%达成！你是传奇！", "太厉害了！好骄傲！")
            PetPersonality.TSUNDERE -> listOf("哼，100%...还不错。", "做到了。100%...挺厉害的。", "目标达成...认可你了。", "100%...算你厉害。")
            PetPersonality.DIALECT -> listOf("100%厉害啊", "做得好100%", "哦100%了", "就知道你行", "辛苦了")
            PetPersonality.TIMID -> listOf("100%...做、做到了...！", "目标达成了...！100%...！", "哇...100%了...", "100%...太厉害了...")
            PetPersonality.POSITIVE -> listOf("100%！最棒！太厉害了！", "目标达成！100%完美！", "做到了！100%！真棒！", "100%！今天也成功了！")
        }
        return messages.random()
    }

    private fun getGoalAchievedMessageEs(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("Lo lograste. 100%.", "Meta alcanzada. Buen trabajo.", "Perfecto. Meta de hoy cumplida.", "100%. Impresionante.", "Hoy también lo lograste.")
            PetPersonality.CUTE -> listOf("¡OMG lo hiciste! ¡100%!", "¡Increíble! ¡Meta completa!", "¡100%! ¡Eres una leyenda!", "¡Lo lograste! ¡Muy orgulloso!")
            PetPersonality.TSUNDERE -> listOf("Bueno, 100%... nada mal.", "Lo lograste. 100%... impresionante.", "Meta alcanzada... lo admito.", "100%... buen trabajo, supongo.")
            PetPersonality.DIALECT -> listOf("100% Increíble.", "Bien hecho, 100%.", "Oh, 100% listo.", "Sabía que podías.", "Buen trabajo.")
            PetPersonality.TIMID -> listOf("100%... L-lo lograste...!", "Meta alcanzada...! 100%...!", "Wow... llegaste al 100%...", "100%... increíble...")
            PetPersonality.POSITIVE -> listOf("¡100%! ¡Eres el mejor!", "¡Meta lograda! ¡100% perfecto!", "¡Lo hiciste! ¡100%! ¡Increíble!", "¡100%! ¡Otro gran día!")
        }
        return messages.random()
    }

    // 초과 달성 메시지 (100% 이상)
    fun getOverAchievedMessage(personality: PetPersonality, percent: Int): String {
        return when (getLang()) {
            "ko" -> getOverAchievedMessageKo(personality, percent)
            "ja" -> getOverAchievedMessageJa(personality, percent)
            "zh" -> getOverAchievedMessageZh(personality, percent)
            "es" -> getOverAchievedMessageEs(personality, percent)
            else -> getOverAchievedMessageEn(personality, percent)
        }
    }

    private fun getOverAchievedMessageKo(personality: PetPersonality, percent: Int): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "${percent}%. 목표 초과. 대단하군.",
                "목표 넘어섰다. ${percent}%.",
                "${percent}%라니. 괴물이야.",
                "초과 달성 ${percent}%. 멋있어."
            )
            PetPersonality.CUTE -> listOf(
                "${percent}%? ㄷㄷㄷ 미쳤다 ㄹㅇ",
                "헐 ${percent}%?! 실화임? ㅋㅋ",
                "${percent}% 달성 ㄷㄷ 오버킬이네",
                "목표 넘음 ${percent}%! 찐임"
            )
            PetPersonality.TSUNDERE -> listOf(
                "${percent}%라니... 좀 오버했네.",
                "뭐야 ${percent}%? ...대단하긴 해.",
                "목표 넘었잖아. ${percent}%... 인정.",
                "${percent}%... 좀 무섭네. 칭찬이야."
            )
            PetPersonality.DIALECT -> listOf(
                "${percent}%? 대단하네",
                "목표 넘었네 ${percent}%",
                "${percent}% 진짜 잘했다",
                "초과 달성 ${percent}% 대박"
            )
            PetPersonality.TIMID -> listOf(
                "${percent}%... 목표를 넘었어요...!",
                "에... ${percent}%예요...? 대단해요...!",
                "${percent}% 초과 달성이에요...! 우와...",
                "목표 넘었어요... ${percent}%라니..."
            )
            PetPersonality.POSITIVE -> listOf(
                "${percent}%! 목표 초과 대박!",
                "와! ${percent}%! 목표 넘어섰어!",
                "${percent}% 달성! 진짜 대단해!",
                "초과 달성 ${percent}%! 최고야!"
            )
        }
        return messages.random()
    }

    private fun getOverAchievedMessageEn(personality: PetPersonality, percent: Int): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "${percent}%. Over goal. Impressive.",
                "Exceeded the goal. ${percent}%.",
                "${percent}%? You're a beast.",
                "Over achieved ${percent}%. Nice."
            )
            PetPersonality.CUTE -> listOf(
                "${percent}%? OMG you're insane!",
                "Whoa ${percent}%?! For real?!",
                "${percent}%! Total overkill!",
                "Over ${percent}%! You're a legend!"
            )
            PetPersonality.TSUNDERE -> listOf(
                "${percent}%... You overdid it.",
                "What, ${percent}%? ...Impressive, I guess.",
                "You exceeded ${percent}%... Fine, I admit it.",
                "${percent}%... A bit scary. That's a compliment."
            )
            PetPersonality.DIALECT -> listOf(
                "${percent}%? Amazing",
                "Over the goal at ${percent}%",
                "${percent}% well done",
                "Over achieved ${percent}% wow"
            )
            PetPersonality.TIMID -> listOf(
                "${percent}%... You exceeded the goal...!",
                "Um... ${percent}%...? That's amazing...!",
                "${percent}% over achieved...! Wow...",
                "You exceeded... ${percent}%..."
            )
            PetPersonality.POSITIVE -> listOf(
                "${percent}%! Over the goal! Wow!",
                "Whoa! ${percent}%! You exceeded it!",
                "${percent}%! Truly amazing!",
                "Over ${percent}%! So proud!"
            )
        }
        return messages.random()
    }

    private fun getOverAchievedMessageJa(personality: PetPersonality, percent: Int): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "${percent}%。目標超過。すごいな。",
                "目標を超えた。${percent}%。",
                "${percent}%だと？化け物か。",
                "超過達成${percent}%。かっこいい。"
            )
            PetPersonality.CUTE -> listOf(
                "${percent}%？やばすぎ！",
                "え ${percent}%?! マジ?!",
                "${percent}%達成！オーバーキルだね！",
                "目標超え${percent}%！伝説！"
            )
            PetPersonality.TSUNDERE -> listOf(
                "${percent}%だなんて...やりすぎ。",
                "なに${percent}%？...まぁすごいけど。",
                "目標超えたじゃん。${percent}%...認める。",
                "${percent}%...ちょっと怖い。褒めてるの。"
            )
            PetPersonality.DIALECT -> listOf(
                "${percent}%？すごいね",
                "目標超えたね ${percent}%",
                "${percent}% よくやった",
                "超過達成 ${percent}% すごい"
            )
            PetPersonality.TIMID -> listOf(
                "${percent}%... 目標を超えました...！",
                "あの... ${percent}%ですか...？すごいです...！",
                "${percent}%超過達成...！わぁ...",
                "目標超えました... ${percent}%なんて..."
            )
            PetPersonality.POSITIVE -> listOf(
                "${percent}%！目標超過！すごい！",
                "わぁ！${percent}%！超えたね！",
                "${percent}%達成！本当にすごい！",
                "超過${percent}%！誇らしい！"
            )
        }
        return messages.random()
    }

    private fun getOverAchievedMessageZh(personality: PetPersonality, percent: Int): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "${percent}%。超过目标了。厉害。",
                "超越目标了。${percent}%。",
                "${percent}%？真是个怪物。",
                "超额完成${percent}%。很帅。"
            )
            PetPersonality.CUTE -> listOf(
                "${percent}%？天哪太疯狂了！",
                "哇 ${percent}%?! 真的假的?!",
                "${percent}%完成！太夸张了！",
                "超过${percent}%！你是传奇！"
            )
            PetPersonality.TSUNDERE -> listOf(
                "${percent}%...有点过头了。",
                "什么${percent}%？...算你厉害。",
                "超过目标了啊。${percent}%...承认。",
                "${percent}%...有点可怕。是夸奖。"
            )
            PetPersonality.DIALECT -> listOf(
                "${percent}%？厉害啊",
                "超过目标了 ${percent}%",
                "${percent}% 真的棒",
                "超额完成 ${percent}% 哇"
            )
            PetPersonality.TIMID -> listOf(
                "${percent}%... 超过目标了...！",
                "那个... ${percent}%吗...？太厉害了...！",
                "${percent}%超额完成...！哇...",
                "超过目标了... ${percent}%..."
            )
            PetPersonality.POSITIVE -> listOf(
                "${percent}%！超过目标！太棒了！",
                "哇！${percent}%！超过了！",
                "${percent}%达成！真的太棒了！",
                "超过${percent}%！好自豪！"
            )
        }
        return messages.random()
    }

    private fun getOverAchievedMessageEs(personality: PetPersonality, percent: Int): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "${percent}%. Meta superada. Impresionante.",
                "Superaste la meta. ${percent}%.",
                "${percent}%? Eres una bestia.",
                "Sobre logrado ${percent}%. Genial."
            )
            PetPersonality.CUTE -> listOf(
                "${percent}%? ¡OMG estás loco!",
                "¡Wow ${percent}%?! ¿En serio?!",
                "${percent}%! ¡Exagerado total!",
                "¡Más de ${percent}%! ¡Eres leyenda!"
            )
            PetPersonality.TSUNDERE -> listOf(
                "${percent}%... Te pasaste.",
                "¿Qué, ${percent}%? ...Bueno, impresionante.",
                "Superaste ${percent}%... Lo admito.",
                "${percent}%... Da miedo. Es un cumplido."
            )
            PetPersonality.DIALECT -> listOf(
                "${percent}%? Increíble",
                "Pasaste la meta ${percent}%",
                "${percent}% muy bien",
                "Sobre logrado ${percent}% wow"
            )
            PetPersonality.TIMID -> listOf(
                "${percent}%... Superaste la meta...!",
                "Um... ${percent}%...? Increíble...!",
                "${percent}% sobre logrado...! Wow...",
                "Superaste... ${percent}%..."
            )
            PetPersonality.POSITIVE -> listOf(
                "¡${percent}%! ¡Sobre la meta! ¡Wow!",
                "¡Guau! ¡${percent}%! ¡La superaste!",
                "¡${percent}%! ¡Realmente increíble!",
                "¡Más de ${percent}%! ¡Tan orgulloso!"
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
        return when (getLang()) {
            "ko" -> getMilestoneMessageKo(personality, milestone)
            "ja" -> getMilestoneMessageJa(personality, milestone)
            "zh" -> getMilestoneMessageZh(personality, milestone)
            "es" -> getMilestoneMessageEs(personality, milestone)
            else -> getMilestoneMessageEn(personality, milestone)
        }
    }

    private fun getMilestoneMessageKo(personality: PetPersonality, milestone: Int): String {
        val messages = when (milestone) {
            10 -> when (personality) {
                PetPersonality.TOUGH -> listOf("시작했군.", "좋아, 출발이다.", "10%다.")
                PetPersonality.CUTE -> listOf("시작함 ㅋㅋ", "10%! 워밍업 중", "첫 발 뗌 ㄹㅇ")
                PetPersonality.TSUNDERE -> listOf("겨우 10%야.", "시작은 했네.", "...아직 멀었어.")
                PetPersonality.DIALECT -> listOf("시작했네", "10% 가보자", "출발이다")
                PetPersonality.TIMID -> listOf("시, 시작이에요...", "10%예요...!", "첫 걸음이에요...")
                PetPersonality.POSITIVE -> listOf("시작이야! 화이팅!", "10% 달성!", "멋진 시작이야!")
            }
            20 -> when (personality) {
                PetPersonality.TOUGH -> listOf("20%. 계속.", "잘하고 있어.", "나쁘지 않군.")
                PetPersonality.CUTE -> listOf("20% ㄷㄷ 괜찮은데?", "잘하고 있음 ㅋㅋ", "벌써 20% ㄹㅇ")
                PetPersonality.TSUNDERE -> listOf("20%... 뭐, 괜찮네.", "조금 했네.", "인정해줄게.")
                PetPersonality.DIALECT -> listOf("20% 좋네", "잘하고 있다", "흐름 좋네")
                PetPersonality.TIMID -> listOf("20%예요...!", "잘하고 계세요...", "힘내고 있어요...")
                PetPersonality.POSITIVE -> listOf("20%! 잘하고 있어!", "좋아좋아!", "멋져!")
            }
            30 -> when (personality) {
                PetPersonality.TOUGH -> listOf("30%. 좋아.", "계속 걸어.", "이 페이스 유지.")
                PetPersonality.CUTE -> listOf("30% 옴 ㄷㄷ", "벌써 30% ㅋㅋ", "페이스 좋음 ㅇㅇ")
                PetPersonality.TSUNDERE -> listOf("30%라... 뭐, 괜찮아.", "...계속해.", "인정.")
                PetPersonality.DIALECT -> listOf("오 30%", "잘하네", "이 페이스 좋다")
                PetPersonality.TIMID -> listOf("30%예요...!", "잘하고 있어요...", "조금씩 오르고 있어요...")
                PetPersonality.POSITIVE -> listOf("30%! 대단해!", "화이팅!", "이 기세로 가자!")
            }
            40 -> when (personality) {
                PetPersonality.TOUGH -> listOf("40%. 반 가까이.", "곧 반.", "거의 절반이군.")
                PetPersonality.CUTE -> listOf("40% 거의 반이네", "곧 반임 ㅋㅋ", "반이 코앞 ㄷㄷ")
                PetPersonality.TSUNDERE -> listOf("40%... 반은 아니야.", "곧 반이네.", "반 가까이 왔네.")
                PetPersonality.DIALECT -> listOf("40%다", "반 가까이 왔네", "곧 반이다")
                PetPersonality.TIMID -> listOf("40%예요...", "곧 반이에요...", "거의 반이에요...")
                PetPersonality.POSITIVE -> listOf("40%! 곧 반!", "잘하고 있어!", "대단해!")
            }
            50 -> when (personality) {
                PetPersonality.TOUGH -> listOf("반 왔다.", "50%. 절반.", "나머지도 가자.")
                PetPersonality.CUTE -> listOf("반 옴 ㄷㄷ 실화?", "50% ㄹㅇ 대박", "반 달성! 미쳤다")
                PetPersonality.TSUNDERE -> listOf("반... 왔네.", "50%라니... 괜찮아.", "인정할게.")
                PetPersonality.DIALECT -> listOf("반 왔네", "50% 됐다", "절반 왔다")
                PetPersonality.TIMID -> listOf("반이에요...!", "50%...! 대단해요...", "반이나 왔어요...!")
                PetPersonality.POSITIVE -> listOf("반이야! 대단해!", "50% 달성!", "멋져!")
            }
            60 -> when (personality) {
                PetPersonality.TOUGH -> listOf("60%. 반 넘었다.", "내리막길.", "반 넘었군.")
                PetPersonality.CUTE -> listOf("60% 반 넘음 ㄷㄷ", "ㅋㅋ 잘한다 ㄹㅇ", "반 넘었어 ㅋㅋ")
                PetPersonality.TSUNDERE -> listOf("반 넘었네...", "60%... 꽤 했네.", "반은 넘었군.")
                PetPersonality.DIALECT -> listOf("60% 반 넘었네", "잘한다", "이제 내리막이다")
                PetPersonality.TIMID -> listOf("60%예요...!", "반 넘었어요...", "반을 넘었어요...!")
                PetPersonality.POSITIVE -> listOf("60%! 반 넘었어!", "화이팅!", "이제 내리막!")
            }
            70 -> when (personality) {
                PetPersonality.TOUGH -> listOf("70%. 거의 다.", "조금 남았다.", "끝이 보인다.")
                PetPersonality.CUTE -> listOf("70% 거의 다 옴 ㄷㄷ", "끝이 보임", "조금만 더 ㄹㅇ")
                PetPersonality.TSUNDERE -> listOf("70%... 거의 다야.", "조금만 더.", "거의 다 왔네.")
                PetPersonality.DIALECT -> listOf("70% 거의 다 왔네", "힘내", "조금만 더")
                PetPersonality.TIMID -> listOf("70%예요...!", "거의 다 왔어요...", "끝이 보여요...")
                PetPersonality.POSITIVE -> listOf("70%! 거의 다야!", "할 수 있어!", "끝이 보여!")
            }
            80 -> when (personality) {
                PetPersonality.TOUGH -> listOf("80%. 거의 끝.", "끝내자.", "거의 다 왔다.")
                PetPersonality.CUTE -> listOf("80% ㄷㄷㄷ 미쳤다", "거의 다 옴 ㄹㅇ", "곧 끝남 ㅋㅋ")
                PetPersonality.TSUNDERE -> listOf("80%... 거의 끝이야.", "거의 다야.", "끝내봐.")
                PetPersonality.DIALECT -> listOf("80% 거의 다", "끝이 보인다", "거의 다 왔다")
                PetPersonality.TIMID -> listOf("80%예요...!", "거의 다 왔어요...", "거의 끝이에요...")
                PetPersonality.POSITIVE -> listOf("80%! 거의 다!", "끝이 보여!", "조금만 더!")
            }
            90 -> when (personality) {
                PetPersonality.TOUGH -> listOf("90%. 끝내자.", "끝이다.", "완료 직전이군.")
                PetPersonality.CUTE -> listOf("90% ㄷㄷㄷㄷ 미쳤다", "끝내자 ㅋㅋ", "거의 완료 ㅋㅋㅋ")
                PetPersonality.TSUNDERE -> listOf("90%... 끝내.", "거의 다야...", "끝내버려.")
                PetPersonality.DIALECT -> listOf("90% 거의 다", "끝내자", "마지막이다")
                PetPersonality.TIMID -> listOf("90%예요...!", "조금만...", "거의 끝이에요...!")
                PetPersonality.POSITIVE -> listOf("90%! 거의 다야!", "마지막!", "거의 완료야!")
            }
            100 -> getGoalAchievedMessage(personality).let { listOf(it) }
            else -> listOf("")
        }
        return messages.random()
    }

    private fun getMilestoneMessageEn(personality: PetPersonality, milestone: Int): String {
        val messages = when (milestone) {
            10 -> when (personality) {
                PetPersonality.TOUGH -> listOf("Started.", "Good, let's go.", "10%.")
                PetPersonality.CUTE -> listOf("Started! Let's go~", "10%! Warming up", "First step done!")
                PetPersonality.TSUNDERE -> listOf("Only 10%.", "You started, I guess.", "...Still far.")
                PetPersonality.DIALECT -> listOf("You started", "10%, let's go", "Off we go")
                PetPersonality.TIMID -> listOf("S-started...", "10%...!", "First step...")
                PetPersonality.POSITIVE -> listOf("Let's go! Fighting!", "10% done!", "Great start!")
            }
            20 -> when (personality) {
                PetPersonality.TOUGH -> listOf("20%. Keep going.", "Not bad.", "Good.")
                PetPersonality.CUTE -> listOf("20%! Nice~", "Doing good!", "Already 20%!")
                PetPersonality.TSUNDERE -> listOf("20%... well, okay.", "Did a bit.", "I'll accept it.")
                PetPersonality.DIALECT -> listOf("20% nice", "Doing good", "Good flow")
                PetPersonality.TIMID -> listOf("20%...!", "You're doing well...", "Keep it up...")
                PetPersonality.POSITIVE -> listOf("20%! Great job!", "Nice nice!", "Awesome!")
            }
            30 -> when (personality) {
                PetPersonality.TOUGH -> listOf("30%. Good.", "Keep walking.", "Maintain pace.")
                PetPersonality.CUTE -> listOf("30%! Wow~", "Already 30%!", "Good pace!")
                PetPersonality.TSUNDERE -> listOf("30%... fine.", "...Keep going.", "Acceptable.")
                PetPersonality.DIALECT -> listOf("Oh 30%", "Nice", "Good pace")
                PetPersonality.TIMID -> listOf("30%...!", "Doing well...", "Rising slowly...")
                PetPersonality.POSITIVE -> listOf("30%! Amazing!", "Fighting!", "Keep it up!")
            }
            40 -> when (personality) {
                PetPersonality.TOUGH -> listOf("40%. Almost half.", "Nearly half.", "Getting there.")
                PetPersonality.CUTE -> listOf("40% almost half!", "Soon halfway!", "Half is close!")
                PetPersonality.TSUNDERE -> listOf("40%... not half yet.", "Almost half.", "Getting there.")
                PetPersonality.DIALECT -> listOf("40%", "Almost half", "Nearly there")
                PetPersonality.TIMID -> listOf("40%...", "Almost half...", "Nearly half...")
                PetPersonality.POSITIVE -> listOf("40%! Almost half!", "Great job!", "Amazing!")
            }
            50 -> when (personality) {
                PetPersonality.TOUGH -> listOf("Halfway.", "50%.", "Let's finish this.")
                PetPersonality.CUTE -> listOf("Halfway! Wow!", "50%! Amazing!", "Half done!")
                PetPersonality.TSUNDERE -> listOf("Halfway... okay.", "50%... fine.", "I'll admit it.")
                PetPersonality.DIALECT -> listOf("Halfway there", "50% done", "Half done")
                PetPersonality.TIMID -> listOf("Halfway...!", "50%...! Amazing...", "Half done...!")
                PetPersonality.POSITIVE -> listOf("Halfway! Great!", "50%!", "Awesome!")
            }
            60 -> when (personality) {
                PetPersonality.TOUGH -> listOf("60%. Past half.", "Downhill.", "Over half.")
                PetPersonality.CUTE -> listOf("60% past half!", "Nice job!", "Over half!")
                PetPersonality.TSUNDERE -> listOf("Past half...", "60%... not bad.", "Over half now.")
                PetPersonality.DIALECT -> listOf("60% past half", "Nice", "Downhill now")
                PetPersonality.TIMID -> listOf("60%...!", "Past half...", "Over half...!")
                PetPersonality.POSITIVE -> listOf("60%! Past half!", "Fighting!", "Downhill now!")
            }
            70 -> when (personality) {
                PetPersonality.TOUGH -> listOf("70%. Almost done.", "Little left.", "End in sight.")
                PetPersonality.CUTE -> listOf("70% almost there!", "End in sight!", "Just a bit more!")
                PetPersonality.TSUNDERE -> listOf("70%... almost.", "A bit more.", "Nearly there.")
                PetPersonality.DIALECT -> listOf("70% almost there", "Keep going", "Just a bit")
                PetPersonality.TIMID -> listOf("70%...!", "Almost there...", "End in sight...")
                PetPersonality.POSITIVE -> listOf("70%! Almost!", "You can do it!", "Almost there!")
            }
            80 -> when (personality) {
                PetPersonality.TOUGH -> listOf("80%. Nearly done.", "Finish it.", "Almost there.")
                PetPersonality.CUTE -> listOf("80%! Insane!", "Almost there!", "Nearly done!")
                PetPersonality.TSUNDERE -> listOf("80%... nearly done.", "Almost there.", "Finish it.")
                PetPersonality.DIALECT -> listOf("80% almost", "End in sight", "Nearly there")
                PetPersonality.TIMID -> listOf("80%...!", "Almost done...", "Nearly finished...")
                PetPersonality.POSITIVE -> listOf("80%! Almost!", "End in sight!", "Just a bit more!")
            }
            90 -> when (personality) {
                PetPersonality.TOUGH -> listOf("90%. Finish.", "Almost done.", "Final stretch.")
                PetPersonality.CUTE -> listOf("90%! Crazy!", "Let's finish!", "Almost done!")
                PetPersonality.TSUNDERE -> listOf("90%... finish it.", "Almost...", "Just finish.")
                PetPersonality.DIALECT -> listOf("90% almost", "Let's finish", "Last stretch")
                PetPersonality.TIMID -> listOf("90%...!", "Just a bit...", "Almost done...!")
                PetPersonality.POSITIVE -> listOf("90%! Almost!", "Last push!", "Nearly complete!")
            }
            100 -> getGoalAchievedMessage(personality).let { listOf(it) }
            else -> listOf("")
        }
        return messages.random()
    }

    private fun getMilestoneMessageJa(personality: PetPersonality, milestone: Int): String {
        val messages = when (milestone) {
            10 -> when (personality) {
                PetPersonality.TOUGH -> listOf("始まったな。", "よし、出発だ。", "10%だ。")
                PetPersonality.CUTE -> listOf("始まった！", "10%！ウォーミングアップ中", "第一歩！")
                PetPersonality.TSUNDERE -> listOf("やっと10%か。", "始まったね。", "...まだ遠い。")
                PetPersonality.DIALECT -> listOf("始まったな", "10%行こう", "出発や")
                PetPersonality.TIMID -> listOf("は、始まりです...", "10%ですよ...！", "第一歩...")
                PetPersonality.POSITIVE -> listOf("スタート！頑張ろう！", "10%達成！", "いいスタート！")
            }
            20 -> when (personality) {
                PetPersonality.TOUGH -> listOf("20%。続けろ。", "悪くない。", "いいぞ。")
                PetPersonality.CUTE -> listOf("20%！いいね〜", "頑張ってる！", "もう20%！")
                PetPersonality.TSUNDERE -> listOf("20%...まぁいいか。", "ちょっと進んだね。", "認めてあげる。")
                PetPersonality.DIALECT -> listOf("20%いいね", "頑張ってる", "いい感じ")
                PetPersonality.TIMID -> listOf("20%です...！", "頑張ってますね...", "頑張って...")
                PetPersonality.POSITIVE -> listOf("20%！頑張ってる！", "いいね！", "すごい！")
            }
            30 -> when (personality) {
                PetPersonality.TOUGH -> listOf("30%。いいぞ。", "続けろ。", "このペース維持。")
                PetPersonality.CUTE -> listOf("30%きた！", "もう30%！", "いいペース！")
                PetPersonality.TSUNDERE -> listOf("30%...まぁいい。", "...続けて。", "認める。")
                PetPersonality.DIALECT -> listOf("お、30%", "いいね", "このペースいい")
                PetPersonality.TIMID -> listOf("30%です...！", "頑張ってます...", "少しずつ...")
                PetPersonality.POSITIVE -> listOf("30%！すごい！", "頑張ろう！", "この調子で！")
            }
            40 -> when (personality) {
                PetPersonality.TOUGH -> listOf("40%。半分近い。", "もうすぐ半分。", "いい調子。")
                PetPersonality.CUTE -> listOf("40%もうすぐ半分！", "半分近い！", "あと少し！")
                PetPersonality.TSUNDERE -> listOf("40%...半分じゃない。", "もうすぐ半分。", "近づいたね。")
                PetPersonality.DIALECT -> listOf("40%", "半分近い", "もうすぐ")
                PetPersonality.TIMID -> listOf("40%...", "もうすぐ半分...", "半分近い...")
                PetPersonality.POSITIVE -> listOf("40%！もうすぐ半分！", "頑張ってる！", "すごい！")
            }
            50 -> when (personality) {
                PetPersonality.TOUGH -> listOf("半分だ。", "50%。", "残りも行くぞ。")
                PetPersonality.CUTE -> listOf("半分きた！すごい！", "50%！やば！", "半分達成！")
                PetPersonality.TSUNDERE -> listOf("半分...きたね。", "50%...まぁいい。", "認めるよ。")
                PetPersonality.DIALECT -> listOf("半分きた", "50%", "半分や")
                PetPersonality.TIMID -> listOf("半分です...！", "50%...！すごい...", "半分...！")
                PetPersonality.POSITIVE -> listOf("半分！すごい！", "50%達成！", "最高！")
            }
            60 -> when (personality) {
                PetPersonality.TOUGH -> listOf("60%。半分超えた。", "下り坂だ。", "半分超え。")
                PetPersonality.CUTE -> listOf("60%半分超えた！", "頑張ってる！", "半分過ぎた！")
                PetPersonality.TSUNDERE -> listOf("半分超えたね...", "60%...悪くない。", "半分は超えた。")
                PetPersonality.DIALECT -> listOf("60%半分超えた", "頑張ってる", "下り坂や")
                PetPersonality.TIMID -> listOf("60%です...！", "半分超えました...", "半分超え...！")
                PetPersonality.POSITIVE -> listOf("60%！半分超えた！", "頑張ろう！", "下り坂だ！")
            }
            70 -> when (personality) {
                PetPersonality.TOUGH -> listOf("70%。もうすぐ。", "少し残り。", "終わりが見える。")
                PetPersonality.CUTE -> listOf("70%もうすぐ！", "終わり見える！", "あと少し！")
                PetPersonality.TSUNDERE -> listOf("70%...もうすぐ。", "あと少し。", "もう少し。")
                PetPersonality.DIALECT -> listOf("70%もうすぐ", "頑張れ", "あと少し")
                PetPersonality.TIMID -> listOf("70%です...！", "もうすぐ...", "終わり見える...")
                PetPersonality.POSITIVE -> listOf("70%！もうすぐ！", "できる！", "もうすぐ！")
            }
            80 -> when (personality) {
                PetPersonality.TOUGH -> listOf("80%。もう終わり。", "終わらせろ。", "もうすぐ。")
                PetPersonality.CUTE -> listOf("80%！やば！", "もうすぐ！", "もう終わり！")
                PetPersonality.TSUNDERE -> listOf("80%...もう終わり。", "もうすぐ。", "終わらせて。")
                PetPersonality.DIALECT -> listOf("80%もうすぐ", "終わり見える", "もうすぐ")
                PetPersonality.TIMID -> listOf("80%です...！", "もう終わり...", "もうすぐ...")
                PetPersonality.POSITIVE -> listOf("80%！もうすぐ！", "終わり見える！", "あと少し！")
            }
            90 -> when (personality) {
                PetPersonality.TOUGH -> listOf("90%。終わらせろ。", "もうすぐ。", "ラスト。")
                PetPersonality.CUTE -> listOf("90%！やばい！", "終わらせよう！", "もう終わり！")
                PetPersonality.TSUNDERE -> listOf("90%...終わらせて。", "もうすぐ...", "終わらせろ。")
                PetPersonality.DIALECT -> listOf("90%もうすぐ", "終わらせよう", "ラスト")
                PetPersonality.TIMID -> listOf("90%です...！", "あと少し...", "もう終わり...！")
                PetPersonality.POSITIVE -> listOf("90%！もうすぐ！", "ラスト！", "もう完了！")
            }
            100 -> getGoalAchievedMessage(personality).let { listOf(it) }
            else -> listOf("")
        }
        return messages.random()
    }

    private fun getMilestoneMessageZh(personality: PetPersonality, milestone: Int): String {
        val messages = when (milestone) {
            10 -> when (personality) {
                PetPersonality.TOUGH -> listOf("开始了。", "好，出发。", "10%。")
                PetPersonality.CUTE -> listOf("开始了！", "10%！热身中", "迈出第一步！")
                PetPersonality.TSUNDERE -> listOf("才10%。", "开始了吧。", "...还远着呢。")
                PetPersonality.DIALECT -> listOf("开始了", "10%，走吧", "出发")
                PetPersonality.TIMID -> listOf("开、开始了...", "10%呢...！", "第一步...")
                PetPersonality.POSITIVE -> listOf("出发！加油！", "10%达成！", "好的开始！")
            }
            20 -> when (personality) {
                PetPersonality.TOUGH -> listOf("20%。继续。", "不错。", "好。")
                PetPersonality.CUTE -> listOf("20%！不错~", "做得好！", "已经20%！")
                PetPersonality.TSUNDERE -> listOf("20%...还行吧。", "做了一点。", "认可你。")
                PetPersonality.DIALECT -> listOf("20%不错", "做得好", "节奏不错")
                PetPersonality.TIMID -> listOf("20%了...！", "做得好...", "加油...")
                PetPersonality.POSITIVE -> listOf("20%！很棒！", "不错不错！", "厉害！")
            }
            30 -> when (personality) {
                PetPersonality.TOUGH -> listOf("30%。好。", "继续走。", "保持节奏。")
                PetPersonality.CUTE -> listOf("30%了！", "已经30%！", "节奏不错！")
                PetPersonality.TSUNDERE -> listOf("30%...还行。", "...继续。", "认可。")
                PetPersonality.DIALECT -> listOf("哦30%", "不错", "节奏好")
                PetPersonality.TIMID -> listOf("30%了...！", "做得好...", "慢慢上升...")
                PetPersonality.POSITIVE -> listOf("30%！厉害！", "加油！", "继续！")
            }
            40 -> when (personality) {
                PetPersonality.TOUGH -> listOf("40%。快一半了。", "快半了。", "快到了。")
                PetPersonality.CUTE -> listOf("40%快一半了！", "快到一半！", "一半快到！")
                PetPersonality.TSUNDERE -> listOf("40%...还没一半。", "快一半了。", "快到了。")
                PetPersonality.DIALECT -> listOf("40%", "快一半", "快了")
                PetPersonality.TIMID -> listOf("40%...", "快一半了...", "快半了...")
                PetPersonality.POSITIVE -> listOf("40%！快一半！", "很棒！", "厉害！")
            }
            50 -> when (personality) {
                PetPersonality.TOUGH -> listOf("一半了。", "50%。", "继续完成。")
                PetPersonality.CUTE -> listOf("一半了！哇！", "50%！太棒了！", "一半达成！")
                PetPersonality.TSUNDERE -> listOf("一半...到了。", "50%...还行。", "认可你。")
                PetPersonality.DIALECT -> listOf("一半了", "50%", "半了")
                PetPersonality.TIMID -> listOf("一半了...！", "50%...！厉害...", "一半...！")
                PetPersonality.POSITIVE -> listOf("一半！厉害！", "50%达成！", "太棒！")
            }
            60 -> when (personality) {
                PetPersonality.TOUGH -> listOf("60%。过半了。", "下坡路。", "过半。")
                PetPersonality.CUTE -> listOf("60%过半了！", "做得好！", "过半了！")
                PetPersonality.TSUNDERE -> listOf("过半了...", "60%...不错。", "过半了。")
                PetPersonality.DIALECT -> listOf("60%过半了", "不错", "下坡了")
                PetPersonality.TIMID -> listOf("60%了...！", "过半了...", "过半...！")
                PetPersonality.POSITIVE -> listOf("60%！过半了！", "加油！", "下坡了！")
            }
            70 -> when (personality) {
                PetPersonality.TOUGH -> listOf("70%。快了。", "剩一点。", "看到终点。")
                PetPersonality.CUTE -> listOf("70%快了！", "看到终点！", "再一点！")
                PetPersonality.TSUNDERE -> listOf("70%...快了。", "再一点。", "快到了。")
                PetPersonality.DIALECT -> listOf("70%快了", "加油", "再一点")
                PetPersonality.TIMID -> listOf("70%了...！", "快了...", "看到终点...")
                PetPersonality.POSITIVE -> listOf("70%！快了！", "可以的！", "快到了！")
            }
            80 -> when (personality) {
                PetPersonality.TOUGH -> listOf("80%。快结束。", "完成它。", "快了。")
                PetPersonality.CUTE -> listOf("80%！太厉害！", "快了！", "快完成！")
                PetPersonality.TSUNDERE -> listOf("80%...快结束。", "快了。", "完成吧。")
                PetPersonality.DIALECT -> listOf("80%快了", "看到终点", "快了")
                PetPersonality.TIMID -> listOf("80%了...！", "快完成...", "快结束...")
                PetPersonality.POSITIVE -> listOf("80%！快了！", "看到终点！", "再一点！")
            }
            90 -> when (personality) {
                PetPersonality.TOUGH -> listOf("90%。完成。", "快了。", "最后。")
                PetPersonality.CUTE -> listOf("90%！太疯狂！", "完成吧！", "快完成！")
                PetPersonality.TSUNDERE -> listOf("90%...完成吧。", "快了...", "完成。")
                PetPersonality.DIALECT -> listOf("90%快了", "完成吧", "最后")
                PetPersonality.TIMID -> listOf("90%了...！", "再一点...", "快完成...！")
                PetPersonality.POSITIVE -> listOf("90%！快了！", "最后！", "快完成！")
            }
            100 -> getGoalAchievedMessage(personality).let { listOf(it) }
            else -> listOf("")
        }
        return messages.random()
    }

    private fun getMilestoneMessageEs(personality: PetPersonality, milestone: Int): String {
        val messages = when (milestone) {
            10 -> when (personality) {
                PetPersonality.TOUGH -> listOf("Empezaste.", "Vamos.", "10%.")
                PetPersonality.CUTE -> listOf("¡Empezaste!", "¡10%! Calentando", "¡Primer paso!")
                PetPersonality.TSUNDERE -> listOf("Solo 10%.", "Empezaste, supongo.", "...Aún falta.")
                PetPersonality.DIALECT -> listOf("Empezaste", "10%, vamos", "Arrancamos")
                PetPersonality.TIMID -> listOf("Em-empezó...", "¡10%...!", "Primer paso...")
                PetPersonality.POSITIVE -> listOf("¡Vamos! ¡Ánimo!", "¡10% logrado!", "¡Buen inicio!")
            }
            20 -> when (personality) {
                PetPersonality.TOUGH -> listOf("20%. Sigue.", "No está mal.", "Bien.")
                PetPersonality.CUTE -> listOf("¡20%! ¡Bien~", "¡Vas bien!", "¡Ya 20%!")
                PetPersonality.TSUNDERE -> listOf("20%... bueno, ok.", "Algo hiciste.", "Lo acepto.")
                PetPersonality.DIALECT -> listOf("20% bien", "Vas bien", "Buen ritmo")
                PetPersonality.TIMID -> listOf("¡20%...!", "Vas bien...", "Ánimo...")
                PetPersonality.POSITIVE -> listOf("¡20%! ¡Genial!", "¡Bien bien!", "¡Increíble!")
            }
            30 -> when (personality) {
                PetPersonality.TOUGH -> listOf("30%. Bien.", "Sigue.", "Mantén el ritmo.")
                PetPersonality.CUTE -> listOf("¡30%!", "¡Ya 30%!", "¡Buen ritmo!")
                PetPersonality.TSUNDERE -> listOf("30%... ok.", "...Sigue.", "Aceptable.")
                PetPersonality.DIALECT -> listOf("Oh 30%", "Bien", "Buen ritmo")
                PetPersonality.TIMID -> listOf("¡30%...!", "Vas bien...", "Subiendo...")
                PetPersonality.POSITIVE -> listOf("¡30%! ¡Genial!", "¡Ánimo!", "¡Sigue así!")
            }
            40 -> when (personality) {
                PetPersonality.TOUGH -> listOf("40%. Casi mitad.", "Casi mitad.", "Ya casi.")
                PetPersonality.CUTE -> listOf("¡40% casi mitad!", "¡Pronto mitad!", "¡Casi!")
                PetPersonality.TSUNDERE -> listOf("40%... no es mitad.", "Casi mitad.", "Ya casi.")
                PetPersonality.DIALECT -> listOf("40%", "Casi mitad", "Ya pronto")
                PetPersonality.TIMID -> listOf("40%...", "Casi mitad...", "Casi...")
                PetPersonality.POSITIVE -> listOf("¡40%! ¡Casi mitad!", "¡Genial!", "¡Increíble!")
            }
            50 -> when (personality) {
                PetPersonality.TOUGH -> listOf("Mitad.", "50%.", "A terminar.")
                PetPersonality.CUTE -> listOf("¡Mitad! ¡Wow!", "¡50%! ¡Genial!", "¡Mitad hecha!")
                PetPersonality.TSUNDERE -> listOf("Mitad... ok.", "50%... bien.", "Lo acepto.")
                PetPersonality.DIALECT -> listOf("Mitad", "50%", "Mitad hecha")
                PetPersonality.TIMID -> listOf("¡Mitad...!", "¡50%...! Genial...", "¡Mitad...!")
                PetPersonality.POSITIVE -> listOf("¡Mitad! ¡Genial!", "¡50%!", "¡Increíble!")
            }
            60 -> when (personality) {
                PetPersonality.TOUGH -> listOf("60%. Pasó mitad.", "Cuesta abajo.", "Pasó mitad.")
                PetPersonality.CUTE -> listOf("¡60% pasó mitad!", "¡Bien hecho!", "¡Pasó mitad!")
                PetPersonality.TSUNDERE -> listOf("Pasó mitad...", "60%... no está mal.", "Pasó mitad.")
                PetPersonality.DIALECT -> listOf("60% pasó mitad", "Bien", "Cuesta abajo")
                PetPersonality.TIMID -> listOf("¡60%...!", "Pasó mitad...", "¡Pasó...!")
                PetPersonality.POSITIVE -> listOf("¡60%! ¡Pasó mitad!", "¡Ánimo!", "¡Cuesta abajo!")
            }
            70 -> when (personality) {
                PetPersonality.TOUGH -> listOf("70%. Ya casi.", "Queda poco.", "Se ve el fin.")
                PetPersonality.CUTE -> listOf("¡70% ya casi!", "¡Se ve el fin!", "¡Un poco más!")
                PetPersonality.TSUNDERE -> listOf("70%... ya casi.", "Un poco más.", "Ya casi.")
                PetPersonality.DIALECT -> listOf("70% ya casi", "Ánimo", "Un poco más")
                PetPersonality.TIMID -> listOf("¡70%...!", "Ya casi...", "Se ve el fin...")
                PetPersonality.POSITIVE -> listOf("¡70%! ¡Ya casi!", "¡Puedes!", "¡Ya casi!")
            }
            80 -> when (personality) {
                PetPersonality.TOUGH -> listOf("80%. Casi termina.", "Termínalo.", "Ya casi.")
                PetPersonality.CUTE -> listOf("¡80%! ¡Increíble!", "¡Ya casi!", "¡Casi termina!")
                PetPersonality.TSUNDERE -> listOf("80%... casi termina.", "Ya casi.", "Termínalo.")
                PetPersonality.DIALECT -> listOf("80% ya casi", "Se ve el fin", "Ya casi")
                PetPersonality.TIMID -> listOf("¡80%...!", "Casi termina...", "Ya casi...")
                PetPersonality.POSITIVE -> listOf("¡80%! ¡Ya casi!", "¡Se ve el fin!", "¡Un poco más!")
            }
            90 -> when (personality) {
                PetPersonality.TOUGH -> listOf("90%. Termina.", "Ya casi.", "Último tramo.")
                PetPersonality.CUTE -> listOf("¡90%! ¡Increíble!", "¡Terminemos!", "¡Ya casi!")
                PetPersonality.TSUNDERE -> listOf("90%... termínalo.", "Ya casi...", "Termina.")
                PetPersonality.DIALECT -> listOf("90% ya casi", "Terminemos", "Último tramo")
                PetPersonality.TIMID -> listOf("¡90%...!", "Un poco...", "¡Ya casi...!")
                PetPersonality.POSITIVE -> listOf("¡90%! ¡Ya casi!", "¡Último!", "¡Casi listo!")
            }
            100 -> getGoalAchievedMessage(personality).let { listOf(it) }
            else -> listOf("")
        }
        return messages.random()
    }

    // 운동 동기부여 명언 (실제 유명인 명언, 성격별 말투로)
    fun getMotivationalQuote(personality: PetPersonality): String {
        return when (getLang()) {
            "ko" -> getMotivationalQuoteKo(personality)
            "ja" -> getMotivationalQuoteJa(personality)
            "zh" -> getMotivationalQuoteZh(personality)
            "es" -> getMotivationalQuoteEs(personality)
            else -> getMotivationalQuoteEn(personality)
        }
    }

    private data class Quote(val text: String, val author: String)

    private fun getMotivationalQuoteKo(personality: PetPersonality): String {
        val quotes = listOf(
            Quote("천 리 길도 한 걸음부터", "노자"),
            Quote("포기하면 거기서 끝이다", "안서일 감독"),
            Quote("오늘 걷지 않으면 내일은 뛰어야 한다", "유대 격언"),
            Quote("건강한 신체에 건강한 정신이 깃든다", "유베날리스"),
            Quote("걷는 것은 최고의 운동이다", "히포크라테스"),
            Quote("시작이 반이다", "아리스토텔레스"),
            Quote("할 수 있다고 믿으면 이미 반은 이룬 것이다", "루즈벨트"),
            Quote("매일 조금씩이 큰 성과를 만든다", "톨스토이"),
            Quote("한 걸음씩 나아가면 어디든 갈 수 있다", "간디")
        )
        val quote = quotes.random()
        return when (personality) {
            PetPersonality.TOUGH -> "\"${quote.text}\"\n-${quote.author}"
            PetPersonality.CUTE -> "\"${quote.text}\"래 ㄹㅇ\n-${quote.author}ㅋㅋ"
            PetPersonality.TSUNDERE -> "\"${quote.text}\"...래.\n-${quote.author}"
            PetPersonality.DIALECT -> "\"${quote.text}\"래\n-${quote.author}"
            PetPersonality.TIMID -> "\"${quote.text}\"...래요...\n-${quote.author}"
            PetPersonality.POSITIVE -> "\"${quote.text}\"!\n-${quote.author}"
        }
    }

    private fun getMotivationalQuoteEn(personality: PetPersonality): String {
        val quotes = listOf(
            Quote("A journey of a thousand miles begins with a single step", "Lao Tzu"),
            Quote("If you quit, it's over right there", "Coach Ahn"),
            Quote("A healthy mind in a healthy body", "Juvenal"),
            Quote("Walking is the best exercise", "Hippocrates"),
            Quote("Well begun is half done", "Aristotle"),
            Quote("Believe you can and you're halfway there", "Roosevelt"),
            Quote("Small progress is still progress", "Ed Sheeran"),
            Quote("Little by little, one goes far", "Tolkien"),
            Quote("Step by step, you can reach anywhere", "Gandhi")
        )
        val quote = quotes.random()
        return when (personality) {
            PetPersonality.TOUGH -> "\"${quote.text}\"\n-${quote.author}"
            PetPersonality.CUTE -> "\"${quote.text}\" they say!\n-${quote.author} lol"
            PetPersonality.TSUNDERE -> "\"${quote.text}\"... apparently.\n-${quote.author}"
            PetPersonality.DIALECT -> "\"${quote.text}\" they said\n-${quote.author}"
            PetPersonality.TIMID -> "\"${quote.text}\"... they say...\n-${quote.author}"
            PetPersonality.POSITIVE -> "\"${quote.text}\"!\n-${quote.author}"
        }
    }

    private fun getMotivationalQuoteJa(personality: PetPersonality): String {
        val quotes = listOf(
            Quote("千里の道も一歩から", "老子"),
            Quote("諦めたらそこで試合終了", "安西先生"),
            Quote("健全な肉体に健全な精神が宿る", "ユウェナリス"),
            Quote("歩くことは最高の運動だ", "ヒポクラテス"),
            Quote("始まりは半分だ", "アリストテレス"),
            Quote("できると信じれば、半分達成したようなものだ", "ルーズベルト"),
            Quote("小さな進歩も進歩だ", "エド・シーラン"),
            Quote("少しずつでも、遠くまで行ける", "トールキン"),
            Quote("一歩ずつ進めば、どこにでも行ける", "ガンジー")
        )
        val quote = quotes.random()
        return when (personality) {
            PetPersonality.TOUGH -> "「${quote.text}」\n-${quote.author}"
            PetPersonality.CUTE -> "「${quote.text}」だって！\n-${quote.author}笑"
            PetPersonality.TSUNDERE -> "「${quote.text}」...だって。\n-${quote.author}"
            PetPersonality.DIALECT -> "「${quote.text}」やって\n-${quote.author}"
            PetPersonality.TIMID -> "「${quote.text}」...だそうです...\n-${quote.author}"
            PetPersonality.POSITIVE -> "「${quote.text}」！\n-${quote.author}"
        }
    }

    private fun getMotivationalQuoteZh(personality: PetPersonality): String {
        val quotes = listOf(
            Quote("千里之行，始于足下", "老子"),
            Quote("放弃的话，比赛就结束了", "安西教练"),
            Quote("健康的身体里有健康的精神", "尤维纳利斯"),
            Quote("散步是最好的运动", "希波克拉底"),
            Quote("好的开始是成功的一半", "亚里士多德"),
            Quote("相信你能做到，你就已经成功了一半", "罗斯福"),
            Quote("小小的进步也是进步", "艾德·希兰"),
            Quote("积少成多，可行千里", "托尔金"),
            Quote("一步一步，到达任何地方", "甘地")
        )
        val quote = quotes.random()
        return when (personality) {
            PetPersonality.TOUGH -> "「${quote.text}」\n-${quote.author}"
            PetPersonality.CUTE -> "「${quote.text}」他们说！\n-${quote.author}哈哈"
            PetPersonality.TSUNDERE -> "「${quote.text}」...据说。\n-${quote.author}"
            PetPersonality.DIALECT -> "「${quote.text}」是这么说的\n-${quote.author}"
            PetPersonality.TIMID -> "「${quote.text}」...他们说...\n-${quote.author}"
            PetPersonality.POSITIVE -> "「${quote.text}」！\n-${quote.author}"
        }
    }

    private fun getMotivationalQuoteEs(personality: PetPersonality): String {
        val quotes = listOf(
            Quote("Un viaje de mil millas comienza con un solo paso", "Lao Tzu"),
            Quote("Si te rindes, el juego termina ahí", "Entrenador Ahn"),
            Quote("Mente sana en cuerpo sano", "Juvenal"),
            Quote("Caminar es el mejor ejercicio", "Hipócrates"),
            Quote("Bien empezado, medio acabado", "Aristóteles"),
            Quote("Cree que puedes y ya estás a mitad de camino", "Roosevelt"),
            Quote("Un pequeño progreso sigue siendo progreso", "Ed Sheeran"),
            Quote("Poco a poco se llega lejos", "Tolkien"),
            Quote("Paso a paso, puedes llegar a cualquier lugar", "Gandhi")
        )
        val quote = quotes.random()
        return when (personality) {
            PetPersonality.TOUGH -> "\"${quote.text}\"\n-${quote.author}"
            PetPersonality.CUTE -> "\"${quote.text}\" ¡dicen!\n-${quote.author} jaja"
            PetPersonality.TSUNDERE -> "\"${quote.text}\"... dicen.\n-${quote.author}"
            PetPersonality.DIALECT -> "\"${quote.text}\" dijeron\n-${quote.author}"
            PetPersonality.TIMID -> "\"${quote.text}\"... dicen...\n-${quote.author}"
            PetPersonality.POSITIVE -> "\"${quote.text}\"!\n-${quote.author}"
        }
    }

    // Free time (자유 시간) - 제어 요일/시간대가 아닐 때
    fun getFreeTimeMessage(personality: PetPersonality): String {
        return when (getLang()) {
            "ko" -> getFreeTimeMessageKo(personality)
            "ja" -> getFreeTimeMessageJa(personality)
            "zh" -> getFreeTimeMessageZh(personality)
            "es" -> getFreeTimeMessageEs(personality)
            else -> getFreeTimeMessageEn(personality)
        }
    }

    private fun getFreeTimeMessageKo(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("오늘은 쉬는 날.", "자유롭게 쉬어.", "오늘은 편하게.", "푹 쉬어. 내일 보자.")
            PetPersonality.CUTE -> listOf("오늘은 쉬는 날임 ㅋㅋ", "자유 시간~ 맘대로 해", "휴식도 중요함 ㅇㅈ?")
            PetPersonality.TSUNDERE -> listOf("오늘은... 뭐, 쉬어도 돼.", "자유 시간이라고. 감사하라구.", "쉬는 것도 필요하다구.")
            PetPersonality.DIALECT -> listOf("오늘은 쉬어가자", "맘대로 해", "자유 시간이다", "쉬는 것도 실력이지")
            PetPersonality.TIMID -> listOf("오, 오늘은 쉬는 날이에요...", "자유롭게 쉬셔도 돼요...", "휴식... 중요해요...")
            PetPersonality.POSITIVE -> listOf("오늘은 자유 시간이야!", "편하게 쉬어가자!", "휴식도 중요해!", "리프레시 타임!")
        }
        return messages.random()
    }

    private fun getFreeTimeMessageEn(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("Today's a rest day.", "Rest freely.", "Take it easy today.", "Rest well. See you tomorrow.")
            PetPersonality.CUTE -> listOf("Rest day today lol", "Free time~ Do whatever!", "Rest is important too!")
            PetPersonality.TSUNDERE -> listOf("Today... well, you can rest.", "It's free time. Be grateful.", "Rest is necessary too.")
            PetPersonality.DIALECT -> listOf("Let's rest today.", "Do whatever.", "It's free time.", "Rest is also a skill.")
            PetPersonality.TIMID -> listOf("T-today's a rest day...", "Feel free to rest...", "Rest is... important...")
            PetPersonality.POSITIVE -> listOf("It's free time today!", "Let's take a break!", "Rest is important too!", "Refresh time!")
        }
        return messages.random()
    }

    private fun getFreeTimeMessageJa(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("今日は休みだ。", "自由に休め。", "今日はゆっくり。", "ゆっくり休め。また明日。")
            PetPersonality.CUTE -> listOf("今日は休みの日～笑", "フリータイム～好きにして！", "休憩も大事だよ！")
            PetPersonality.TSUNDERE -> listOf("今日は...まあ、休んでいいよ。", "フリータイムだって。感謝しなさいよ。", "休むのも必要だから。")
            PetPersonality.DIALECT -> listOf("今日は休もう", "好きにして", "フリータイムや", "休むのも実力や")
            PetPersonality.TIMID -> listOf("き、今日は休みの日です...", "ゆっくり休んでいいですよ...", "休憩...大事です...")
            PetPersonality.POSITIVE -> listOf("今日はフリータイムだよ！", "ゆっくり休もう！", "休憩も大事！", "リフレッシュタイム！")
        }
        return messages.random()
    }

    private fun getFreeTimeMessageZh(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("今天是休息日。", "自由休息。", "今天轻松点。", "好好休息。明天见。")
            PetPersonality.CUTE -> listOf("今天是休息日哈哈", "自由时间～随便吧！", "休息也很重要哦！")
            PetPersonality.TSUNDERE -> listOf("今天...嗯，可以休息。", "是自由时间。要感谢哦。", "休息也是必要的。")
            PetPersonality.DIALECT -> listOf("今天休息吧", "随便", "自由时间", "休息也是本事")
            PetPersonality.TIMID -> listOf("今、今天是休息日...", "可以自由休息...", "休息...很重要...")
            PetPersonality.POSITIVE -> listOf("今天是自由时间！", "轻松休息吧！", "休息也很重要！", "放松时间！")
        }
        return messages.random()
    }

    private fun getFreeTimeMessageEs(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("Hoy es día de descanso.", "Descansa libremente.", "Tómalo con calma hoy.", "Descansa bien. Hasta mañana.")
            PetPersonality.CUTE -> listOf("¡Día de descanso jaja!", "¡Tiempo libre~ Haz lo que quieras!", "¡El descanso también es importante!")
            PetPersonality.TSUNDERE -> listOf("Hoy... bueno, puedes descansar.", "Es tiempo libre. Agradécelo.", "El descanso también es necesario.")
            PetPersonality.DIALECT -> listOf("Descansemos hoy.", "Haz lo que quieras.", "Es tiempo libre.", "Descansar también es habilidad.")
            PetPersonality.TIMID -> listOf("H-hoy es día de descanso...", "Puedes descansar libremente...", "El descanso es... importante...")
            PetPersonality.POSITIVE -> listOf("¡Hoy es tiempo libre!", "¡Descansemos!", "¡El descanso es importante!", "¡Tiempo de refrescarse!")
        }
        return messages.random()
    }

    // Almost there (90%+) - 90~99% 대사들
    fun getAlmostThereMessage(personality: PetPersonality): String {
        return when (getLang()) {
            "ko" -> getAlmostThereMessageKo(personality)
            "ja" -> getAlmostThereMessageJa(personality)
            "zh" -> getAlmostThereMessageZh(personality)
            "es" -> getAlmostThereMessageEs(personality)
            else -> getAlmostThereMessageEn(personality)
        }
    }

    private fun getAlmostThereMessageKo(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("거의 다 왔다.", "조금만 더.", "마지막이다. 힘내.", "끝이 보인다.")
            PetPersonality.CUTE -> listOf("조금만 더! 할 수 있음", "거의 다 옴 ㄷㄷㄷ", "90% 넘음 미쳤다 ㄹㅇ", "끝이 보임 레전드다")
            PetPersonality.TSUNDERE -> listOf("좀 더 해봐.", "거의 다 왔어... 포기하지 마.", "90%라고? ...대단하긴 해.")
            PetPersonality.DIALECT -> listOf("조금만 더 가면 된다", "거의 다 왔네", "마지막이다 힘내", "조금만 더 하면 끝이다")
            PetPersonality.TIMID -> listOf("조금만 더요...", "거, 거의 다 왔어요...!", "90%... 대단해요...", "조금만 더... 할 수 있어요...")
            PetPersonality.POSITIVE -> listOf("거의 다 왔어!", "조금만 더! 할 수 있어!", "90% 돌파! 대단해!", "끝이 보여! 화이팅!")
        }
        return messages.random()
    }

    private fun getAlmostThereMessageEn(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("Almost there.", "Just a little more.", "Final stretch. Push it.", "End is near.")
            PetPersonality.CUTE -> listOf("Almost there! You got this!", "So close! OMG!", "90%! That's amazing!", "Almost done! Legend!")
            PetPersonality.TSUNDERE -> listOf("Keep going.", "Almost there... don't give up.", "90%? ...Impressive, I guess.")
            PetPersonality.DIALECT -> listOf("Just a bit more.", "Almost there.", "Final push.", "Almost done.")
            PetPersonality.TIMID -> listOf("A little more...", "A-almost there...!", "90%... amazing...", "You can do it...")
            PetPersonality.POSITIVE -> listOf("Almost there!", "Just a bit more! You can do it!", "90%! Amazing!", "Finish line in sight!")
        }
        return messages.random()
    }

    private fun getAlmostThereMessageJa(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("もう少しだ。", "あと少し。", "ラストだ。頑張れ。", "ゴールが見える。")
            PetPersonality.CUTE -> listOf("もう少し！できる！", "もうすぐだよ！", "90%超え！やばい！", "ゴール見えてる！")
            PetPersonality.TSUNDERE -> listOf("もうちょっと頑張って。", "あと少し...諦めないで。", "90%？...まあ、すごいね。")
            PetPersonality.DIALECT -> listOf("もうちょっとや", "もうすぐやな", "ラストスパートや", "もう少しで終わりや")
            PetPersonality.TIMID -> listOf("もう少しです...", "も、もうすぐです...！", "90%...すごいです...", "もう少し...頑張って...")
            PetPersonality.POSITIVE -> listOf("もうすぐだよ！", "あと少し！できる！", "90%突破！すごい！", "ゴールが見える！ファイト！")
        }
        return messages.random()
    }

    private fun getAlmostThereMessageZh(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("快到了。", "再坚持一下。", "最后冲刺。", "终点在望。")
            PetPersonality.CUTE -> listOf("快到了！你行的！", "就差一点了！", "90%了！太棒了！", "快完成了！")
            PetPersonality.TSUNDERE -> listOf("再加把劲。", "快到了...别放弃。", "90%？...还挺厉害的。")
            PetPersonality.DIALECT -> listOf("就差一点了", "快到了", "最后加油", "马上就结束了")
            PetPersonality.TIMID -> listOf("再一点点...", "快、快到了...！", "90%...好厉害...", "再坚持一下...")
            PetPersonality.POSITIVE -> listOf("快到了！", "再一点点！你能行！", "90%突破！太棒了！", "终点就在眼前！加油！")
        }
        return messages.random()
    }

    private fun getAlmostThereMessageEs(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("Casi llegas.", "Un poco más.", "Último tramo.", "El final está cerca.")
            PetPersonality.CUTE -> listOf("¡Casi llegas! ¡Tú puedes!", "¡Tan cerca!", "¡90%! ¡Increíble!", "¡Casi lo logras!")
            PetPersonality.TSUNDERE -> listOf("Sigue así.", "Casi llegas... no te rindas.", "¿90%? ...Impresionante, supongo.")
            PetPersonality.DIALECT -> listOf("Un poquito más.", "Ya casi.", "Último esfuerzo.", "Ya casi terminas.")
            PetPersonality.TIMID -> listOf("Un poco más...", "C-casi llegas...!", "90%... increíble...", "Puedes hacerlo...")
            PetPersonality.POSITIVE -> listOf("¡Casi llegas!", "¡Un poco más! ¡Tú puedes!", "¡90%! ¡Increíble!", "¡La meta está cerca!")
        }
        return messages.random()
    }

    // 75-89% 대사들
    fun getThreeQuarterMessage(personality: PetPersonality): String {
        return when (getLang()) {
            "ko" -> getThreeQuarterMessageKo(personality)
            "ja" -> getThreeQuarterMessageJa(personality)
            "zh" -> getThreeQuarterMessageZh(personality)
            "es" -> getThreeQuarterMessageEs(personality)
            else -> getThreeQuarterMessageEn(personality)
        }
    }

    private fun getThreeQuarterMessageKo(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("3/4 왔다.", "75% 넘었어. 잘하고 있어.", "거의 다 왔다.", "페이스 좋아.")
            PetPersonality.CUTE -> listOf("75% 넘음 ㄷㄷ 실화?", "3/4 왔다 미쳤다 ㅋㅋ", "거의 다 옴! ㄹㅇ", "잘하고 있음! 조금만 더")
            PetPersonality.TSUNDERE -> listOf("75%라... 뭐, 괜찮네.", "3/4 왔어. 나쁘지 않아.", "거의 다 왔어. 멈추지 마.")
            PetPersonality.DIALECT -> listOf("75% 넘었네", "3/4 왔다", "거의 다 왔네", "잘하고 있다")
            PetPersonality.TIMID -> listOf("75%... 대단해요...", "3/4 왔어요... 잘하고 계세요...", "조금만 더요...")
            PetPersonality.POSITIVE -> listOf("75% 돌파! 대단해!", "3/4 왔어! 거의 다 왔어!", "잘하고 있어! 조금만 더!")
        }
        return messages.random()
    }

    private fun getThreeQuarterMessageEn(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("3/4 done.", "Past 75%. Good pace.", "Almost there.", "Nice pace.")
            PetPersonality.CUTE -> listOf("75%! Wow really?", "3/4 done! Amazing!", "Almost there! For real!", "Doing great! Little more!")
            PetPersonality.TSUNDERE -> listOf("75%... not bad.", "3/4 there. Okay.", "Almost done. Don't stop.")
            PetPersonality.DIALECT -> listOf("Past 75%.", "3/4 done.", "Almost there.", "Doing good.")
            PetPersonality.TIMID -> listOf("75%... amazing...", "3/4 done... great job...", "Just a bit more...")
            PetPersonality.POSITIVE -> listOf("75%! Amazing!", "3/4 done! Almost there!", "Doing great! Just a bit more!")
        }
        return messages.random()
    }

    private fun getThreeQuarterMessageJa(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("3/4来た。", "75%超えた。いいペースだ。", "もうすぐだ。", "ペースいいな。")
            PetPersonality.CUTE -> listOf("75%！マジで？", "3/4きた！すごい！", "もうすぐ！ガチ！", "いい感じ！もう少し！")
            PetPersonality.TSUNDERE -> listOf("75%か...まあ、悪くない。", "3/4来た。悪くないね。", "もうすぐ。止まらないで。")
            PetPersonality.DIALECT -> listOf("75%超えたな", "3/4来た", "もうすぐや", "よくやってる")
            PetPersonality.TIMID -> listOf("75%...すごいです...", "3/4来ました...頑張ってますね...", "もう少しです...")
            PetPersonality.POSITIVE -> listOf("75%突破！すごい！", "3/4来た！もうすぐだよ！", "いい感じ！あと少し！")
        }
        return messages.random()
    }

    private fun getThreeQuarterMessageZh(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("完成3/4了。", "超过75%了。节奏不错。", "快到了。", "节奏很好。")
            PetPersonality.CUTE -> listOf("75%了！真的假的？", "完成3/4了！太棒了！", "快到了！真的！", "做得很好！再一点点！")
            PetPersonality.TSUNDERE -> listOf("75%...还行吧。", "3/4了。不错。", "快到了。别停。")
            PetPersonality.DIALECT -> listOf("超过75%了", "完成3/4了", "快到了", "做得不错")
            PetPersonality.TIMID -> listOf("75%...好厉害...", "完成3/4了...做得很好...", "再一点点...")
            PetPersonality.POSITIVE -> listOf("75%突破！太棒了！", "完成3/4了！快到了！", "做得很好！再加油！")
        }
        return messages.random()
    }

    private fun getThreeQuarterMessageEs(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("3/4 listo.", "Pasaste 75%. Buen ritmo.", "Casi llegas.", "Buen ritmo.")
            PetPersonality.CUTE -> listOf("¡75%! ¿En serio?", "¡3/4 listo! ¡Increíble!", "¡Casi llegas!", "¡Vas muy bien! ¡Un poco más!")
            PetPersonality.TSUNDERE -> listOf("75%... no está mal.", "3/4 listo. Está bien.", "Casi llegas. No pares.")
            PetPersonality.DIALECT -> listOf("Pasaste 75%.", "3/4 listo.", "Ya casi.", "Vas bien.")
            PetPersonality.TIMID -> listOf("75%... increíble...", "3/4 listo... buen trabajo...", "Un poco más...")
            PetPersonality.POSITIVE -> listOf("¡75%! ¡Increíble!", "¡3/4 listo! ¡Casi llegas!", "¡Vas genial! ¡Un poco más!")
        }
        return messages.random()
    }

    // Halfway (50-74%) - 절반 대사들
    fun getHalfwayMessage(personality: PetPersonality): String {
        return when (getLang()) {
            "ko" -> getHalfwayMessageKo(personality)
            "ja" -> getHalfwayMessageJa(personality)
            "zh" -> getHalfwayMessageZh(personality)
            "es" -> getHalfwayMessageEs(personality)
            else -> getHalfwayMessageEn(personality)
        }
    }

    private fun getHalfwayMessageKo(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("절반 왔다.", "반이야. 나쁘지 않아.", "50% 달성. 계속 가자.", "반환점 통과.")
            PetPersonality.CUTE -> listOf("절반 옴 ㄷㄷ 페이스 실화?", "반이나 함 미쳤다 ㅋㅋ", "50% 달성! ㄹㅇ 대박", "반환점 통과 ㅋㅋㅋ")
            PetPersonality.TSUNDERE -> listOf("뭐, 절반은 했네.", "반이야... 나쁘지 않아.", "50%라고? ...계속해봐.")
            PetPersonality.DIALECT -> listOf("반이나 했네", "50% 달성이다", "절반 왔다", "반환점 통과했다")
            PetPersonality.TIMID -> listOf("반, 반이에요...", "50% 달성... 대단해요...", "절반 왔어요... 잘하고 계세요...")
            PetPersonality.POSITIVE -> listOf("절반 달성! 잘하고 있어!", "50% 돌파! 대단해!", "반환점 통과! 화이팅!")
        }
        return messages.random()
    }

    private fun getHalfwayMessageEn(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("Halfway there.", "50%. Not bad.", "50% done. Keep going.", "Passed the midpoint.")
            PetPersonality.CUTE -> listOf("Halfway! That pace tho!", "50%! Amazing lol", "50% done! Awesome!", "Midpoint passed!")
            PetPersonality.TSUNDERE -> listOf("Well, halfway done.", "50%... not bad.", "50%? ...Keep going.")
            PetPersonality.DIALECT -> listOf("Halfway done.", "50% achieved.", "Halfway there.", "Passed midpoint.")
            PetPersonality.TIMID -> listOf("H-halfway...", "50% done... amazing...", "Halfway there... great job...")
            PetPersonality.POSITIVE -> listOf("Halfway! Great job!", "50%! Amazing!", "Midpoint passed! Go go!")
        }
        return messages.random()
    }

    private fun getHalfwayMessageJa(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("半分来た。", "50%。悪くない。", "50%達成。続けろ。", "折り返し地点通過。")
            PetPersonality.CUTE -> listOf("半分来た！ペースやばい！", "50%！すごい笑", "50%達成！やばい！", "折り返し通過！")
            PetPersonality.TSUNDERE -> listOf("まあ、半分は来たね。", "50%...悪くない。", "50%？...続けて。")
            PetPersonality.DIALECT -> listOf("半分来たな", "50%達成や", "半分来た", "折り返し通過した")
            PetPersonality.TIMID -> listOf("は、半分です...", "50%達成...すごいです...", "半分来ました...頑張ってますね...")
            PetPersonality.POSITIVE -> listOf("半分達成！いい感じ！", "50%突破！すごい！", "折り返し通過！ファイト！")
        }
        return messages.random()
    }

    private fun getHalfwayMessageZh(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("到一半了。", "50%。不错。", "50%完成。继续。", "过了中点。")
            PetPersonality.CUTE -> listOf("到一半了！节奏太棒了！", "50%！太厉害了哈哈", "50%完成！太棒了！", "过了中点！")
            PetPersonality.TSUNDERE -> listOf("嗯，到一半了。", "50%...还行。", "50%？...继续吧。")
            PetPersonality.DIALECT -> listOf("到一半了", "50%完成了", "过了一半", "过了中点")
            PetPersonality.TIMID -> listOf("一、一半了...", "50%完成...好厉害...", "到一半了...做得很好...")
            PetPersonality.POSITIVE -> listOf("到一半了！很棒！", "50%突破！太棒了！", "过了中点！加油！")
        }
        return messages.random()
    }

    private fun getHalfwayMessageEs(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("A mitad de camino.", "50%. Nada mal.", "50% listo. Sigue.", "Pasaste el punto medio.")
            PetPersonality.CUTE -> listOf("¡A mitad! ¡Qué ritmo!", "¡50%! ¡Increíble jaja!", "¡50% listo! ¡Genial!", "¡Pasaste el medio!")
            PetPersonality.TSUNDERE -> listOf("Bueno, ya vas a la mitad.", "50%... no está mal.", "¿50%? ...Sigue así.")
            PetPersonality.DIALECT -> listOf("A mitad de camino.", "50% logrado.", "Ya vas a la mitad.", "Pasaste el medio.")
            PetPersonality.TIMID -> listOf("M-mitad...", "50% listo... increíble...", "A mitad... buen trabajo...")
            PetPersonality.POSITIVE -> listOf("¡A mitad! ¡Muy bien!", "¡50%! ¡Increíble!", "¡Pasaste el medio! ¡Vamos!")
        }
        return messages.random()
    }

    // 25-49% 대사들
    fun getQuarterMessage(personality: PetPersonality): String {
        return when (getLang()) {
            "ko" -> getQuarterMessageKo(personality)
            "ja" -> getQuarterMessageJa(personality)
            "zh" -> getQuarterMessageZh(personality)
            "es" -> getQuarterMessageEs(personality)
            else -> getQuarterMessageEn(personality)
        }
    }

    private fun getQuarterMessageKo(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("1/4 왔다. 계속.", "시작이 좋아.", "25% 넘었어. 더 가자.", "이 페이스로.")
            PetPersonality.CUTE -> listOf("1/4 왔다 ㄱㅊㄱㅊ", "좋은 시작임 ㅋㅋ", "25% 돌파 ㄷㄷ", "계속 가자 할 수 있음")
            PetPersonality.TSUNDERE -> listOf("25%... 뭐, 시작은 했네.", "1/4 왔어. 아직 멀었지만.", "이제 시작이야. 멈추지 마.")
            PetPersonality.DIALECT -> listOf("25% 넘었네", "1/4 왔다", "좋은 시작이다", "계속 가보자")
            PetPersonality.TIMID -> listOf("25%... 잘하고 계세요...", "1/4 왔어요...", "좋은 시작이에요...")
            PetPersonality.POSITIVE -> listOf("25% 돌파! 좋아!", "1/4 왔어! 잘하고 있어!", "좋은 시작이야!")
        }
        return messages.random()
    }

    private fun getQuarterMessageEn(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("1/4 done. Continue.", "Good start.", "Past 25%. Keep going.", "This pace.")
            PetPersonality.CUTE -> listOf("1/4 done! Nice!", "Good start lol", "25%! Wow!", "Keep going! You can!")
            PetPersonality.TSUNDERE -> listOf("25%... well, you started.", "1/4 done. Still far.", "Just started. Don't stop.")
            PetPersonality.DIALECT -> listOf("Past 25%.", "1/4 done.", "Good start.", "Keep going.")
            PetPersonality.TIMID -> listOf("25%... great job...", "1/4 done...", "Good start...")
            PetPersonality.POSITIVE -> listOf("25%! Great!", "1/4 done! Doing well!", "Good start!")
        }
        return messages.random()
    }

    private fun getQuarterMessageJa(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("1/4来た。続けろ。", "いいスタート。", "25%超えた。もっと行こう。", "このペースで。")
            PetPersonality.CUTE -> listOf("1/4来た！いいね！", "いいスタート笑", "25%！すごい！", "続けよう！できる！")
            PetPersonality.TSUNDERE -> listOf("25%...まあ、始まったね。", "1/4来た。まだ遠いけど。", "始まったばかり。止まらないで。")
            PetPersonality.DIALECT -> listOf("25%超えたな", "1/4来た", "いいスタートや", "続けよう")
            PetPersonality.TIMID -> listOf("25%...頑張ってますね...", "1/4来ました...", "いいスタートです...")
            PetPersonality.POSITIVE -> listOf("25%！いいね！", "1/4来た！いい感じ！", "いいスタート！")
        }
        return messages.random()
    }

    private fun getQuarterMessageZh(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("完成1/4了。继续。", "开始不错。", "超过25%了。继续走。", "保持这个节奏。")
            PetPersonality.CUTE -> listOf("完成1/4了！不错！", "开始很好哈哈", "25%了！哇！", "继续！你能行！")
            PetPersonality.TSUNDERE -> listOf("25%...嗯，开始了。", "1/4了。还早。", "才刚开始。别停。")
            PetPersonality.DIALECT -> listOf("超过25%了", "完成1/4了", "开始不错", "继续")
            PetPersonality.TIMID -> listOf("25%...做得好...", "完成1/4了...", "开始不错...")
            PetPersonality.POSITIVE -> listOf("25%！太棒了！", "完成1/4了！很好！", "开始不错！")
        }
        return messages.random()
    }

    private fun getQuarterMessageEs(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("1/4 listo. Sigue.", "Buen inicio.", "Pasaste 25%. Sigue.", "Este ritmo.")
            PetPersonality.CUTE -> listOf("¡1/4 listo! ¡Bien!", "¡Buen inicio jaja!", "¡25%! ¡Wow!", "¡Sigue! ¡Puedes!")
            PetPersonality.TSUNDERE -> listOf("25%... bueno, empezaste.", "1/4 listo. Aún falta.", "Solo empezaste. No pares.")
            PetPersonality.DIALECT -> listOf("Pasaste 25%.", "1/4 listo.", "Buen inicio.", "Sigue.")
            PetPersonality.TIMID -> listOf("25%... buen trabajo...", "1/4 listo...", "Buen inicio...")
            PetPersonality.POSITIVE -> listOf("¡25%! ¡Genial!", "¡1/4 listo! ¡Bien hecho!", "¡Buen inicio!")
        }
        return messages.random()
    }

    // 10-24% 대사들
    fun getStartedMessage(personality: PetPersonality): String {
        return when (getLang()) {
            "ko" -> getStartedMessageKo(personality)
            "ja" -> getStartedMessageJa(personality)
            "zh" -> getStartedMessageZh(personality)
            "es" -> getStartedMessageEs(personality)
            else -> getStartedMessageEn(personality)
        }
    }

    private fun getStartedMessageKo(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("시작했네.", "좋아, 계속 가.", "움직이기 시작했어.", "10% 넘었다.")
            PetPersonality.CUTE -> listOf("시작함 ㅋㅋ 가보자고", "10% 넘음 ㄱㅊ", "걷기 시작! 워밍업임", "ㅋㅋ 좋아좋아")
            PetPersonality.TSUNDERE -> listOf("움직이기 시작했네.", "10%... 아직 멀었어.", "시작은 했어. 계속해.")
            PetPersonality.DIALECT -> listOf("시작했네", "10% 넘었다", "걷기 시작했네", "이 페이스로 가자")
            PetPersonality.TIMID -> listOf("시작했어요...", "10%... 잘하고 계세요...", "걷기 시작했어요...")
            PetPersonality.POSITIVE -> listOf("시작이 좋아!", "10% 돌파!", "걷기 시작했어! 좋아!")
        }
        return messages.random()
    }

    private fun getStartedMessageEn(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("You started.", "Good, keep going.", "Started moving.", "Past 10%.")
            PetPersonality.CUTE -> listOf("Started! Let's go!", "Past 10%! Nice!", "Walking started! Warm up!", "Yay nice nice!")
            PetPersonality.TSUNDERE -> listOf("Started moving.", "10%... still far.", "You started. Keep going.")
            PetPersonality.DIALECT -> listOf("You started.", "Past 10%.", "Started walking.", "Keep this pace.")
            PetPersonality.TIMID -> listOf("You started...", "10%... great job...", "Started walking...")
            PetPersonality.POSITIVE -> listOf("Good start!", "10%!", "Started walking! Great!")
        }
        return messages.random()
    }

    private fun getStartedMessageJa(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("始まったな。", "いいぞ、続けろ。", "動き始めた。", "10%超えた。")
            PetPersonality.CUTE -> listOf("始まった笑 行こう！", "10%超え！いいね！", "歩き始めた！ウォーミングアップ！", "いいね！")
            PetPersonality.TSUNDERE -> listOf("動き始めたね。", "10%...まだ遠い。", "始まった。続けて。")
            PetPersonality.DIALECT -> listOf("始まったな", "10%超えた", "歩き始めた", "このペースでいこう")
            PetPersonality.TIMID -> listOf("始まりました...", "10%...頑張ってますね...", "歩き始めました...")
            PetPersonality.POSITIVE -> listOf("いいスタート！", "10%！", "歩き始めた！いいね！")
        }
        return messages.random()
    }

    private fun getStartedMessageZh(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("开始了。", "好，继续。", "开始动了。", "超过10%了。")
            PetPersonality.CUTE -> listOf("开始了！走起！", "超过10%了！不错！", "开始走了！热身中！", "不错不错！")
            PetPersonality.TSUNDERE -> listOf("开始动了。", "10%...还早。", "开始了。继续。")
            PetPersonality.DIALECT -> listOf("开始了", "超过10%了", "开始走了", "保持这个节奏")
            PetPersonality.TIMID -> listOf("开始了...", "10%...做得好...", "开始走了...")
            PetPersonality.POSITIVE -> listOf("开始不错！", "10%了！", "开始走了！很好！")
        }
        return messages.random()
    }

    private fun getStartedMessageEs(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("Empezaste.", "Bien, sigue.", "Empezaste a moverte.", "Pasaste 10%.")
            PetPersonality.CUTE -> listOf("¡Empezaste! ¡Vamos!", "¡Pasaste 10%! ¡Bien!", "¡Empezaste! ¡Calentando!", "¡Bien bien!")
            PetPersonality.TSUNDERE -> listOf("Empezaste a moverte.", "10%... aún falta.", "Empezaste. Sigue.")
            PetPersonality.DIALECT -> listOf("Empezaste.", "Pasaste 10%.", "Empezaste a caminar.", "Mantén el ritmo.")
            PetPersonality.TIMID -> listOf("Empezaste...", "10%... buen trabajo...", "Empezaste a caminar...")
            PetPersonality.POSITIVE -> listOf("¡Buen inicio!", "¡10%!", "¡Empezaste! ¡Bien!")
        }
        return messages.random()
    }

    // 1-9% 대사들 (막 시작)
    fun getJustStartedMessage(personality: PetPersonality): String {
        return when (getLang()) {
            "ko" -> getJustStartedMessageKo(personality)
            "ja" -> getJustStartedMessageJa(personality)
            "zh" -> getJustStartedMessageZh(personality)
            "es" -> getJustStartedMessageEs(personality)
            else -> getJustStartedMessageEn(personality)
        }
    }

    private fun getJustStartedMessageKo(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("시작이다.", "가자.", "움직여.", "좋아. 시작.", "첫 걸음.")
            PetPersonality.CUTE -> listOf("시작! 가보자고~", "산책 출발 ㅋㅋ", "걷자! ㄱㄱ", "첫 걸음 ㄱㅊ~")
            PetPersonality.TSUNDERE -> listOf("...시작했네.", "드디어 움직이네.", "뭐야, 이제 시작?", "가자. 빨리.")
            PetPersonality.DIALECT -> listOf("시작이다", "산책 출발", "걷자", "첫 걸음이지")
            PetPersonality.TIMID -> listOf("시, 시작했어요...", "출발이에요...", "걷기 시작했어요...", "같이... 가요...")
            PetPersonality.POSITIVE -> listOf("시작이야! 화이팅!", "첫 걸음! 좋아!", "출발! 가자!", "좋아! 걷자!")
        }
        return messages.random()
    }

    private fun getJustStartedMessageEn(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("It begins.", "Let's go.", "Move.", "Good. Start.", "First step.")
            PetPersonality.CUTE -> listOf("Start! Let's go~", "Walk time lol", "Let's walk! Go!", "First step nice~")
            PetPersonality.TSUNDERE -> listOf("...Finally started.", "Finally moving.", "What, just now?", "Let's go. Hurry.")
            PetPersonality.DIALECT -> listOf("It begins.", "Walk time.", "Let's walk.", "First step.")
            PetPersonality.TIMID -> listOf("S-started...", "We're off...", "Started walking...", "Let's... go...")
            PetPersonality.POSITIVE -> listOf("Let's start! Go!", "First step! Great!", "Off we go!", "Let's walk!")
        }
        return messages.random()
    }

    private fun getJustStartedMessageJa(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("始まりだ。", "行くぞ。", "動け。", "いいぞ。スタート。", "第一歩。")
            PetPersonality.CUTE -> listOf("スタート！行こ～", "散歩出発笑", "歩こう！ゴーゴー！", "第一歩いいね～")
            PetPersonality.TSUNDERE -> listOf("...始まったね。", "やっと動いたね。", "何、今頃？", "行くよ。早く。")
            PetPersonality.DIALECT -> listOf("始まりや", "散歩出発", "歩こう", "第一歩やな")
            PetPersonality.TIMID -> listOf("は、始まりました...", "出発です...", "歩き始めました...", "一緒に...行きましょう...")
            PetPersonality.POSITIVE -> listOf("スタート！ファイト！", "第一歩！いいね！", "出発！行こう！", "歩こう！")
        }
        return messages.random()
    }

    private fun getJustStartedMessageZh(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("开始。", "走吧。", "动起来。", "好。开始。", "第一步。")
            PetPersonality.CUTE -> listOf("开始！走起～", "散步出发哈哈", "走吧！冲！", "第一步不错～")
            PetPersonality.TSUNDERE -> listOf("...开始了。", "终于动了。", "什么，现在才？", "走吧。快点。")
            PetPersonality.DIALECT -> listOf("开始了", "散步出发", "走吧", "第一步")
            PetPersonality.TIMID -> listOf("开、开始了...", "出发了...", "开始走了...", "一起...走吧...")
            PetPersonality.POSITIVE -> listOf("开始！加油！", "第一步！很好！", "出发！走吧！", "走吧！")
        }
        return messages.random()
    }

    private fun getJustStartedMessageEs(personality: PetPersonality): String {
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf("Empieza.", "Vamos.", "Muévete.", "Bien. Empieza.", "Primer paso.")
            PetPersonality.CUTE -> listOf("¡Empieza! ¡Vamos~", "¡Hora de paseo jaja!", "¡Caminemos! ¡Go!", "¡Primer paso bien~")
            PetPersonality.TSUNDERE -> listOf("...Empezaste.", "Por fin te mueves.", "¿Qué, recién ahora?", "Vamos. Rápido.")
            PetPersonality.DIALECT -> listOf("Empieza.", "Hora de paseo.", "Caminemos.", "Primer paso.")
            PetPersonality.TIMID -> listOf("E-empezó...", "Salimos...", "Empezamos a caminar...", "Vamos... juntos...")
            PetPersonality.POSITIVE -> listOf("¡Empezamos! ¡Vamos!", "¡Primer paso! ¡Genial!", "¡Salimos! ¡Vamos!", "¡Caminemos!")
        }
        return messages.random()
    }

    // Sad/low happiness messages
    fun getSadMessage(personality: PetPersonality): String {
        return when (getLang()) {
            "ko" -> getSadMessageKo(personality)
            "ja" -> getSadMessageJa(personality)
            "zh" -> getSadMessageZh(personality)
            "es" -> getSadMessageEs(personality)
            else -> getSadMessageEn(personality)
        }
    }

    private fun getSadMessageKo(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "..."
            PetPersonality.CUTE -> "심심함ㅠㅠ 뭐해..."
            PetPersonality.TSUNDERE -> "...언제 걸을 건데."
            PetPersonality.DIALECT -> "심심하다"
            PetPersonality.TIMID -> "저, 저..."
            PetPersonality.POSITIVE -> "같이 걷고 싶어..."
        }
    }

    private fun getSadMessageEn(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "..."
            PetPersonality.CUTE -> "Bored... what're you doing..."
            PetPersonality.TSUNDERE -> "...When are you gonna walk."
            PetPersonality.DIALECT -> "Bored."
            PetPersonality.TIMID -> "Um, um..."
            PetPersonality.POSITIVE -> "I want to walk together..."
        }
    }

    private fun getSadMessageJa(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "..."
            PetPersonality.CUTE -> "暇...何してるの..."
            PetPersonality.TSUNDERE -> "...いつ歩くの。"
            PetPersonality.DIALECT -> "暇やな"
            PetPersonality.TIMID -> "あ、あの..."
            PetPersonality.POSITIVE -> "一緒に歩きたい..."
        }
    }

    private fun getSadMessageZh(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "..."
            PetPersonality.CUTE -> "无聊...你在干嘛..."
            PetPersonality.TSUNDERE -> "...什么时候走。"
            PetPersonality.DIALECT -> "无聊"
            PetPersonality.TIMID -> "那、那个..."
            PetPersonality.POSITIVE -> "想一起走..."
        }
    }

    private fun getSadMessageEs(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "..."
            PetPersonality.CUTE -> "Aburrido... qué haces..."
            PetPersonality.TSUNDERE -> "...Cuándo vas a caminar."
            PetPersonality.DIALECT -> "Aburrido."
            PetPersonality.TIMID -> "Um, um..."
            PetPersonality.POSITIVE -> "Quiero caminar juntos..."
        }
    }

    // 화남/경고 메시지 (빨간색 텍스트용)
    fun getAngryMessage(personality: PetPersonality): PetDialogue {
        return when (getLang()) {
            "ko" -> getAngryMessageKo(personality)
            "ja" -> getAngryMessageJa(personality)
            "zh" -> getAngryMessageZh(personality)
            "es" -> getAngryMessageEs(personality)
            else -> getAngryMessageEn(personality)
        }
    }

    private fun getAngryMessageKo(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("걸어. 지금 당장.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("왜 안 걸음?ㅠ 빨리ㅋㅋ", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("걷기 싫어? 나도 싫어.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("걸어 빨리", isRed = true)
            PetPersonality.TIMID -> PetDialogue("저, 저... 걸어주세요...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("걷자! 지금 바로!", isRed = false)
        }
    }

    private fun getAngryMessageEn(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("Walk. Now.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("Why not walking? Come on!", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("Don't wanna walk? Me neither.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("Walk. Hurry.", isRed = true)
            PetPersonality.TIMID -> PetDialogue("P-please walk...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("Let's walk! Right now!", isRed = false)
        }
    }

    private fun getAngryMessageJa(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("歩け。今すぐ。", isRed = true)
            PetPersonality.CUTE -> PetDialogue("なんで歩かないの？早く！", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("歩きたくない？私も。", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("歩け 早く", isRed = true)
            PetPersonality.TIMID -> PetDialogue("あ、あの...歩いてください...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("歩こう！今すぐ！", isRed = false)
        }
    }

    private fun getAngryMessageZh(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("走。现在。", isRed = true)
            PetPersonality.CUTE -> PetDialogue("为什么不走？快点啦！", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("不想走？我也不想。", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("快走", isRed = true)
            PetPersonality.TIMID -> PetDialogue("请、请走一走...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("走吧！现在！", isRed = false)
        }
    }

    private fun getAngryMessageEs(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("Camina. Ahora.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("¿Por qué no caminas? ¡Vamos!", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("¿No quieres caminar? Yo tampoco.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("Camina. Rápido.", isRed = true)
            PetPersonality.TIMID -> PetDialogue("P-por favor camina...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("¡Caminemos! ¡Ahora!", isRed = false)
        }
    }

    // 목표 미달성 경고 메시지
    fun getGoalFailedMessage(personality: PetPersonality): PetDialogue {
        return when (getLang()) {
            "ko" -> getGoalFailedMessageKo(personality)
            "ja" -> getGoalFailedMessageJa(personality)
            "zh" -> getGoalFailedMessageZh(personality)
            "es" -> getGoalFailedMessageEs(personality)
            else -> getGoalFailedMessageEn(personality)
        }
    }

    private fun getGoalFailedMessageKo(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("약속 어겼네.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("목표 못 채움ㅠ 아쉽다...", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("실망이야.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("오늘은 좀 아니다...", isRed = false)
            PetPersonality.TIMID -> PetDialogue("저, 저... 괜찮아요...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("내일 다시 하면 돼!", isRed = false)
        }
    }

    private fun getGoalFailedMessageEn(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("You broke the promise.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("Didn't reach goal... sad...", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("Disappointed.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("Not today...", isRed = false)
            PetPersonality.TIMID -> PetDialogue("I-it's okay...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("Tomorrow we'll do it!", isRed = false)
        }
    }

    private fun getGoalFailedMessageJa(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("約束破ったな。", isRed = true)
            PetPersonality.CUTE -> PetDialogue("目標達成できなかった...残念...", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("がっかり。", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("今日はちょっとな...", isRed = false)
            PetPersonality.TIMID -> PetDialogue("だ、大丈夫です...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("明日また頑張ろう！", isRed = false)
        }
    }

    private fun getGoalFailedMessageZh(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("违背承诺了。", isRed = true)
            PetPersonality.CUTE -> PetDialogue("没达成目标...可惜...", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("失望。", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("今天不太行...", isRed = false)
            PetPersonality.TIMID -> PetDialogue("没、没关系...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("明天再努力！", isRed = false)
        }
    }

    private fun getGoalFailedMessageEs(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("Rompiste la promesa.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("No lograste la meta... triste...", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("Decepcionado.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("Hoy no fue el día...", isRed = false)
            PetPersonality.TIMID -> PetDialogue("E-está bien...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("¡Mañana lo lograremos!", isRed = false)
        }
    }

    // 오랜만에 앱 접속
    fun getLongTimeNoSeeMessage(personality: PetPersonality, days: Int): PetDialogue {
        return when (getLang()) {
            "ko" -> getLongTimeNoSeeMessageKo(personality, days)
            "ja" -> getLongTimeNoSeeMessageJa(personality, days)
            "zh" -> getLongTimeNoSeeMessageZh(personality, days)
            "es" -> getLongTimeNoSeeMessageEs(personality, days)
            else -> getLongTimeNoSeeMessageEn(personality, days)
        }
    }

    private fun getLongTimeNoSeeMessageKo(personality: PetPersonality, days: Int): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("${days}일. 어디 있었어.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("${days}일만이야ㅠ 어디갔었어!", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("${days}일 동안 뭐 했어? ...기다렸거든.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("${days}일만이네 어디 갔었노", isRed = false)
            PetPersonality.TIMID -> PetDialogue("${days}일... 걱정했어요...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("${days}일만이야! 다시 시작하자!", isRed = false)
        }
    }

    private fun getLongTimeNoSeeMessageEn(personality: PetPersonality, days: Int): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("$days days. Where were you.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("It's been $days days! Where were you!", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("What were you doing for $days days? ...I was waiting.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("$days days. Where'd you go?", isRed = false)
            PetPersonality.TIMID -> PetDialogue("$days days... I was worried...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("$days days! Let's start again!", isRed = false)
        }
    }

    private fun getLongTimeNoSeeMessageJa(personality: PetPersonality, days: Int): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("${days}日。どこにいた。", isRed = true)
            PetPersonality.CUTE -> PetDialogue("${days}日ぶり！どこ行ってたの！", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("${days}日間何してたの？...待ってたんだから。", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("${days}日ぶりやな どこ行ってた", isRed = false)
            PetPersonality.TIMID -> PetDialogue("${days}日...心配しました...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("${days}日ぶり！また始めよう！", isRed = false)
        }
    }

    private fun getLongTimeNoSeeMessageZh(personality: PetPersonality, days: Int): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("${days}天。你去哪了。", isRed = true)
            PetPersonality.CUTE -> PetDialogue("${days}天了！你去哪了！", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("这${days}天你在干嘛？...我等着呢。", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("${days}天了 去哪了", isRed = false)
            PetPersonality.TIMID -> PetDialogue("${days}天...担心你了...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("${days}天了！重新开始吧！", isRed = false)
        }
    }

    private fun getLongTimeNoSeeMessageEs(personality: PetPersonality, days: Int): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("$days días. Dónde estabas.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("¡$days días! ¡Dónde estabas!", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("¿Qué hacías por $days días? ...Te esperaba.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("$days días. ¿A dónde fuiste?", isRed = false)
            PetPersonality.TIMID -> PetDialogue("$days días... estaba preocupado...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("¡$days días! ¡Empecemos de nuevo!", isRed = false)
        }
    }

    // 앱 차단 시 메시지
    fun getBlockingMessage(personality: PetPersonality): PetDialogue {
        return when (getLang()) {
            "ko" -> getBlockingMessageKo(personality)
            "ja" -> getBlockingMessageJa(personality)
            "zh" -> getBlockingMessageZh(personality)
            "es" -> getBlockingMessageEs(personality)
            else -> getBlockingMessageEn(personality)
        }
    }

    private fun getBlockingMessageKo(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("안 돼. 걸어.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("안됨! 먼저 걸어야함!", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("걷기 전엔 안 돼.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("안 됨 걸어", isRed = true)
            PetPersonality.TIMID -> PetDialogue("저, 먼저 걸어주세요...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("걷고 나서 하자!", isRed = false)
        }
    }

    private fun getBlockingMessageEn(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("No. Walk first.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("Nope! Walk first!", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("Not until you walk.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("No. Walk.", isRed = true)
            PetPersonality.TIMID -> PetDialogue("P-please walk first...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("Walk first, then use it!", isRed = false)
        }
    }

    private fun getBlockingMessageJa(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("ダメだ。歩け。", isRed = true)
            PetPersonality.CUTE -> PetDialogue("ダメ！まず歩いて！", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("歩くまでダメ。", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("ダメや 歩け", isRed = true)
            PetPersonality.TIMID -> PetDialogue("あ、あの、まず歩いてください...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("歩いてからにしよう！", isRed = false)
        }
    }

    private fun getBlockingMessageZh(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("不行。先走。", isRed = true)
            PetPersonality.CUTE -> PetDialogue("不行！先走路！", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("走之前不行。", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("不行 走", isRed = true)
            PetPersonality.TIMID -> PetDialogue("请、请先走一走...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("走完再用吧！", isRed = false)
        }
    }

    private fun getBlockingMessageEs(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("No. Camina primero.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("¡No! ¡Primero camina!", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("No hasta que camines.", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("No. Camina.", isRed = true)
            PetPersonality.TIMID -> PetDialogue("P-por favor camina primero...", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("¡Camina primero, luego úsalo!", isRed = false)
        }
    }

    // 독촉 메시지
    fun getUrgeMessage(personality: PetPersonality): PetDialogue {
        return when (getLang()) {
            "ko" -> getUrgeMessageKo(personality)
            "ja" -> getUrgeMessageJa(personality)
            "zh" -> getUrgeMessageZh(personality)
            "es" -> getUrgeMessageEs(personality)
            else -> getUrgeMessageEn(personality)
        }
    }

    private fun getUrgeMessageKo(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("뭐해. 걸어.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("산책각! 가자 ㄱㄱ!", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("...갈 거야, 말 거야?", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("나가자 빨리", isRed = true)
            PetPersonality.TIMID -> PetDialogue("저, 나가볼까요...?", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("밖에 나가자! 기분 좋아질 거야!", isRed = false)
        }
    }

    private fun getUrgeMessageEn(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("What're you doing. Walk.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("Walk time! Let's go!", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("...Going or not?", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("Let's go. Hurry.", isRed = true)
            PetPersonality.TIMID -> PetDialogue("Um, shall we go out...?", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("Let's go outside! You'll feel great!", isRed = false)
        }
    }

    private fun getUrgeMessageJa(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("何してる。歩け。", isRed = true)
            PetPersonality.CUTE -> PetDialogue("散歩タイム！行こう！", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("...行くの、行かないの？", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("行こう 早く", isRed = true)
            PetPersonality.TIMID -> PetDialogue("あ、あの、出かけませんか...？", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("外に出よう！気分良くなるよ！", isRed = false)
        }
    }

    private fun getUrgeMessageZh(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("干嘛呢。走。", isRed = true)
            PetPersonality.CUTE -> PetDialogue("散步时间！走吧！", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("...去不去？", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("走吧 快点", isRed = true)
            PetPersonality.TIMID -> PetDialogue("那、那个，出去走走...？", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("出去吧！心情会变好的！", isRed = false)
        }
    }

    private fun getUrgeMessageEs(personality: PetPersonality): PetDialogue {
        return when (personality) {
            PetPersonality.TOUGH -> PetDialogue("Qué haces. Camina.", isRed = true)
            PetPersonality.CUTE -> PetDialogue("¡Hora de paseo! ¡Vamos!", isRed = false)
            PetPersonality.TSUNDERE -> PetDialogue("...¿Vas o no?", isRed = true)
            PetPersonality.DIALECT -> PetDialogue("Vamos. Rápido.", isRed = true)
            PetPersonality.TIMID -> PetDialogue("Um, ¿salimos...?", isRed = false)
            PetPersonality.POSITIVE -> PetDialogue("¡Salgamos! ¡Te sentirás bien!", isRed = false)
        }
    }

    // 야간 메시지
    fun getNightMessage(personality: PetPersonality): String {
        return when (getLang()) {
            "ko" -> getNightMessageKo(personality)
            "ja" -> getNightMessageJa(personality)
            "zh" -> getNightMessageZh(personality)
            "es" -> getNightMessageEs(personality)
            else -> getNightMessageEn(personality)
        }
    }

    private fun getNightMessageKo(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "내일 또 걷자."
            PetPersonality.CUTE -> "잘자~ 오늘 수고했음!"
            PetPersonality.TSUNDERE -> "...푹 쉬어."
            PetPersonality.DIALECT -> "푹 자고 내일 보자"
            PetPersonality.TIMID -> "편히 쉬세요..."
            PetPersonality.POSITIVE -> "오늘 수고했어! 굿나잇!"
        }
    }

    private fun getNightMessageEn(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Walk again tomorrow."
            PetPersonality.CUTE -> "Good night~ Great job today!"
            PetPersonality.TSUNDERE -> "...Rest well."
            PetPersonality.DIALECT -> "Sleep well. See you tomorrow."
            PetPersonality.TIMID -> "Rest well..."
            PetPersonality.POSITIVE -> "Great job today! Good night!"
        }
    }

    private fun getNightMessageJa(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "また明日歩こう。"
            PetPersonality.CUTE -> "おやすみ～今日もお疲れ！"
            PetPersonality.TSUNDERE -> "...ゆっくり休んで。"
            PetPersonality.DIALECT -> "ゆっくり寝てな また明日"
            PetPersonality.TIMID -> "ゆっくり休んでください..."
            PetPersonality.POSITIVE -> "今日もお疲れ！おやすみ！"
        }
    }

    private fun getNightMessageZh(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "明天继续走。"
            PetPersonality.CUTE -> "晚安～今天辛苦了！"
            PetPersonality.TSUNDERE -> "...好好休息。"
            PetPersonality.DIALECT -> "好好睡 明天见"
            PetPersonality.TIMID -> "好好休息..."
            PetPersonality.POSITIVE -> "今天辛苦了！晚安！"
        }
    }

    private fun getNightMessageEs(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Caminamos mañana."
            PetPersonality.CUTE -> "¡Buenas noches~ Buen trabajo hoy!"
            PetPersonality.TSUNDERE -> "...Descansa bien."
            PetPersonality.DIALECT -> "Duerme bien. Hasta mañana."
            PetPersonality.TIMID -> "Descansa bien..."
            PetPersonality.POSITIVE -> "¡Buen trabajo hoy! ¡Buenas noches!"
        }
    }

    // 아침 메시지
    fun getMorningMessage(personality: PetPersonality): String {
        return when (getLang()) {
            "ko" -> getMorningMessageKo(personality)
            "ja" -> getMorningMessageJa(personality)
            "zh" -> getMorningMessageZh(personality)
            "es" -> getMorningMessageEs(personality)
            else -> getMorningMessageEn(personality)
        }
    }

    private fun getMorningMessageKo(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "일어나. 걷자."
            PetPersonality.CUTE -> "일어남? ㅋㅋ 갓생 살자!"
            PetPersonality.TSUNDERE -> "...일어났어?"
            PetPersonality.DIALECT -> "일어났네 오늘도 가보자"
            PetPersonality.TIMID -> "안, 안녕하세요..."
            PetPersonality.POSITIVE -> "좋은 아침! 오늘도 화이팅!"
        }
    }

    private fun getMorningMessageEn(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Wake up. Let's walk."
            PetPersonality.CUTE -> "You're up? Let's have a great day!"
            PetPersonality.TSUNDERE -> "...You're awake?"
            PetPersonality.DIALECT -> "You're up. Let's go today too."
            PetPersonality.TIMID -> "G-good morning..."
            PetPersonality.POSITIVE -> "Good morning! Let's go today!"
        }
    }

    private fun getMorningMessageJa(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "起きろ。歩こう。"
            PetPersonality.CUTE -> "起きた？今日も頑張ろう！"
            PetPersonality.TSUNDERE -> "...起きた？"
            PetPersonality.DIALECT -> "起きたな 今日も行こう"
            PetPersonality.TIMID -> "お、おはようございます..."
            PetPersonality.POSITIVE -> "おはよう！今日もファイト！"
        }
    }

    private fun getMorningMessageZh(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "起来。走吧。"
            PetPersonality.CUTE -> "醒了？今天也加油！"
            PetPersonality.TSUNDERE -> "...醒了？"
            PetPersonality.DIALECT -> "醒了 今天也走吧"
            PetPersonality.TIMID -> "早、早上好..."
            PetPersonality.POSITIVE -> "早上好！今天也加油！"
        }
    }

    private fun getMorningMessageEs(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Despierta. Caminemos."
            PetPersonality.CUTE -> "¿Despierto? ¡Vamos a tener un gran día!"
            PetPersonality.TSUNDERE -> "...¿Despertaste?"
            PetPersonality.DIALECT -> "Despertaste. Vamos hoy también."
            PetPersonality.TIMID -> "B-buenos días..."
            PetPersonality.POSITIVE -> "¡Buenos días! ¡Vamos hoy!"
        }
    }

    // Chat response based on message content
    fun getChatResponse(personality: PetPersonality, message: String, petName: String, isHappy: Boolean): String {
        // TODO: 테스트 후 삭제 - AI 테스트용 스크립트 비활성화
        return when (personality) {
            PetPersonality.TOUGH -> "뭔 소린지 모르겠다. 걷자."
            PetPersonality.CUTE -> "뭔말인지 모르겠음ㅋㅋ 산책!"
            PetPersonality.TSUNDERE -> "잘 모르겠어. 걷자."
            PetPersonality.DIALECT -> "뭔 소린지 모르겠다 걷자"
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
                    PetPersonality.CUTE -> "산책각이다 가자!"
                    PetPersonality.TSUNDERE -> "뭐... 나가볼까."
                    PetPersonality.DIALECT -> "걷기 좋은 날이네"
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
                    PetPersonality.DIALECT -> "안녕"
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
                    PetPersonality.DIALECT -> "산책 기다리는 중"
                    PetPersonality.TIMID -> "저, 저도 기다리고 있었어요..."
                    PetPersonality.POSITIVE -> "산책 갈 준비 중! 같이 가자!"
                }
            }
            // Praise / Love
            lowerMessage.contains("잘했") || lowerMessage.contains("최고") || lowerMessage.contains("사랑") || lowerMessage.contains("고마") || lowerMessage.contains("좋아") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "...고맙다."
                    PetPersonality.CUTE -> "ㅋㅋㅋ 고마워!"
                    PetPersonality.TSUNDERE -> "뭐, 뭐야... 갑자기..."
                    PetPersonality.DIALECT -> "고맙다"
                    PetPersonality.TIMID -> "저, 정말요...? 감사해요..."
                    PetPersonality.POSITIVE -> "나도 좋아해!"
                }
            }
            // Tired / Hard
            lowerMessage.contains("피곤") || lowerMessage.contains("힘들") || lowerMessage.contains("지친") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "쉬어. 내일 또 걷자."
                    PetPersonality.CUTE -> "오늘 수고~ 푹 쉬어!"
                    PetPersonality.TSUNDERE -> "무리하지 마."
                    PetPersonality.DIALECT -> "오늘은 좀 쉬어"
                    PetPersonality.TIMID -> "편히 쉬세요..."
                    PetPersonality.POSITIVE -> "내일 또 하면 돼!"
                }
            }
            // Walk / Exercise
            lowerMessage.contains("걷") || lowerMessage.contains("산책") || lowerMessage.contains("운동") ||
            lowerMessage.contains("걸어") || lowerMessage.contains("걸자") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "좋아. 가자."
                    PetPersonality.CUTE -> "산책! 가자 ㄱㄱ!"
                    PetPersonality.TSUNDERE -> "나도... 가고 싶긴 해."
                    PetPersonality.DIALECT -> "걷기 좋은 날이네"
                    PetPersonality.TIMID -> "저, 저도 같이요..."
                    PetPersonality.POSITIVE -> "가자가자!"
                }
            }
            // Run / Jogging
            lowerMessage.contains("뛰") || lowerMessage.contains("달려") || lowerMessage.contains("달리") ||
            lowerMessage.contains("조깅") || lowerMessage.contains("러닝") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "좋아. 뛰자."
                    PetPersonality.CUTE -> "달리기! 가자 ㅋㅋ"
                    PetPersonality.TSUNDERE -> "뛰고 싶어? ...나도."
                    PetPersonality.DIALECT -> "달리기 좋네"
                    PetPersonality.TIMID -> "저, 천천히 뛰어요..."
                    PetPersonality.POSITIVE -> "달리자! 기분 좋아질 거야!"
                }
            }
            // Food / Meal
            lowerMessage.contains("밥") || lowerMessage.contains("먹") || lowerMessage.contains("배고") || lowerMessage.contains("맛있") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "밥 먹고 걷자."
                    PetPersonality.CUTE -> "맛있겠다~ 밥 먹고 산책!"
                    PetPersonality.TSUNDERE -> "배고프면... 먹어. 걱정되니까."
                    PetPersonality.DIALECT -> "밥 먹고 걸으면 소화도 되지"
                    PetPersonality.TIMID -> "저, 맛있게 드세요..."
                    PetPersonality.POSITIVE -> "맛있는 거 먹고 걷자!"
                }
            }
            // Mood / Feeling sad
            lowerMessage.contains("우울") || lowerMessage.contains("슬프") || lowerMessage.contains("짜증") || lowerMessage.contains("화나") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "걸으면 나아질 거야."
                    PetPersonality.CUTE -> "괜찮아! 내가 있잖아~"
                    PetPersonality.TSUNDERE -> "...옆에 있어줄게."
                    PetPersonality.DIALECT -> "걱정 마 다 괜찮아질 거다"
                    PetPersonality.TIMID -> "저, 저도 힘내드릴게요..."
                    PetPersonality.POSITIVE -> "걸으면 기분 좋아져! 같이 가자!"
                }
            }
            // Happy / Good mood
            lowerMessage.contains("기분") || lowerMessage.contains("행복") || lowerMessage.contains("좋다") || lowerMessage.contains("신나") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "좋네."
                    PetPersonality.CUTE -> "와아~ 나도 기분 좋음!"
                    PetPersonality.TSUNDERE -> "흥, 나도... 좋아."
                    PetPersonality.DIALECT -> "좋네 나도 기쁘다"
                    PetPersonality.TIMID -> "저, 저도 기뻐요..."
                    PetPersonality.POSITIVE -> "최고다! 같이 기뻐!"
                }
            }
            // Question about pet
            lowerMessage.contains("누구") || lowerMessage.contains("뭐야") || lowerMessage.contains("이름") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "나? ${petName}. 네 파트너."
                    PetPersonality.CUTE -> "나는 ${petName}! 잘 부탁해 ㅋㅋ"
                    PetPersonality.TSUNDERE -> "${petName}야. 기억해."
                    PetPersonality.DIALECT -> "나는 ${petName}이다"
                    PetPersonality.TIMID -> "저, 저는 ${petName}이에요..."
                    PetPersonality.POSITIVE -> "${petName}! 함께 걷는 친구야!"
                }
            }
            // Sleep / Night
            lowerMessage.contains("잘자") || lowerMessage.contains("굿나잇") || lowerMessage.contains("자러") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "내일 보자."
                    PetPersonality.CUTE -> "잘자~ 좋은 꿈 꿔!"
                    PetPersonality.TSUNDERE -> "...잘 자."
                    PetPersonality.DIALECT -> "푹 자"
                    PetPersonality.TIMID -> "편히 주무세요..."
                    PetPersonality.POSITIVE -> "굿나잇! 내일도 화이팅!"
                }
            }
            // Morning
            lowerMessage.contains("좋은 아침") || lowerMessage.contains("일어났") || lowerMessage.contains("굿모닝") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "일어났네. 오늘도 걷자."
                    PetPersonality.CUTE -> "일어남? ㅋㅋ 갓생 시작!"
                    PetPersonality.TSUNDERE -> "...늦게 일어났네."
                    PetPersonality.DIALECT -> "일어났네 오늘도 가보자"
                    PetPersonality.TIMID -> "안, 안녕히 주무셨어요...?"
                    PetPersonality.POSITIVE -> "좋은 아침! 오늘도 파이팅!"
                }
            }
            // Work / Study
            lowerMessage.contains("일") || lowerMessage.contains("공부") || lowerMessage.contains("바쁘") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "쉬는 시간에 걸어."
                    PetPersonality.CUTE -> "화이팅! 틈틈이 스트레칭!"
                    PetPersonality.TSUNDERE -> "무리하지 마... 잠깐 쉬어."
                    PetPersonality.DIALECT -> "일도 중요하지만 건강도 챙겨"
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
                    PetPersonality.DIALECT -> "밖에 나가봐"
                    PetPersonality.TIMID -> "저, 저랑 산책 가실래요...?"
                    PetPersonality.POSITIVE -> "같이 걸으면 안 심심해!"
                }
            }
            // Goal / Target
            lowerMessage.contains("목표") || lowerMessage.contains("얼마") || lowerMessage.contains("달성") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "목표는 지키는 거야."
                    PetPersonality.CUTE -> "목표 달성하면 ㄹㅇ 최고~!"
                    PetPersonality.TSUNDERE -> "못할 줄 알았어? 할 수 있어."
                    PetPersonality.DIALECT -> "목표 달성하면 기분 좋지"
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
                    PetPersonality.CUTE -> "삼성헬스 연동 확인해봐! 괜찮아!"
                    PetPersonality.TSUNDERE -> "피트니스 앱 연결됐어? 확인해봐."
                    PetPersonality.DIALECT -> "삼성헬스 연동 확인해봐"
                    PetPersonality.TIMID -> "저, 피트니스 앱 연결 확인해보세요..."
                    PetPersonality.POSITIVE -> "피트니스 앱 연동하면 해결돼!"
                }
            }
            // Am I doing well? (잘하고 있어?)
            lowerMessage.contains("잘하고") || lowerMessage.contains("잘하나") || lowerMessage.contains("잘해") ||
            lowerMessage.contains("잘하는") || lowerMessage.contains("괜찮") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "잘하고 있어. 계속 가."
                    PetPersonality.CUTE -> "완전 잘하고 있음! 대박!"
                    PetPersonality.TSUNDERE -> "뭐... 나쁘지 않아."
                    PetPersonality.DIALECT -> "잘하고 있네 힘내"
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
                    PetPersonality.DIALECT -> "그건 안 되지"
                    PetPersonality.TIMID -> "저, 저는... 그건 못해요..."
                    PetPersonality.POSITIVE -> "그건 못하지만 산책은 갈 수 있어!"
                }
            }
            // MZ slang words (ㄱㄱ, ㄹㅇ, 갓생)
            lowerMessage.contains("ㄱㄱ") || lowerMessage.contains("ㄹㅇ") || lowerMessage.contains("갓생") ||
            lowerMessage.contains("가자") || lowerMessage.contains("가보자") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "가자."
                    PetPersonality.CUTE -> "ㅋㅋㅋ 가자 가자!"
                    PetPersonality.TSUNDERE -> "...가자고."
                    PetPersonality.DIALECT -> "가자"
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
                    PetPersonality.DIALECT -> "왜 그러노"
                    PetPersonality.TIMID -> "저, 괜찮으세요...?"
                    PetPersonality.POSITIVE -> "무슨 일이야? 얘기해봐!"
                }
            }
            // Encouragement
            lowerMessage.contains("힘") || lowerMessage.contains("응원") || lowerMessage.contains("파이팅") -> {
                when (personality) {
                    PetPersonality.TOUGH -> "난 믿는다. 해내."
                    PetPersonality.CUTE -> "ㄹㅇ 화이팅!"
                    PetPersonality.TSUNDERE -> "...할 수 있을 거야."
                    PetPersonality.DIALECT -> "힘내 응원한다"
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
                "난 잘 모르겠는데 일단 ㄱㄱ!"
            )
            PetPersonality.TSUNDERE -> listOf(
                "뭐... 잘 모르겠어. 그냥 걸을래?",
                "이해 안 돼. 걷자.",
                "...뭔 소리야. 산책이나 가자."
            )
            PetPersonality.DIALECT -> listOf(
                "복잡하네 걷자",
                "잘 모르겠다 산책이나 가자",
                "어렵다 일단 걸어보자"
            )
            PetPersonality.TIMID -> listOf(
                "저, 저... 잘 모르겠어요... 산책 갈까요...?",
                "어려워요... 걷기나 할래요...?",
                "저, 뭔지 잘... 그냥 걸어요..."
            )
            PetPersonality.POSITIVE -> listOf(
                "잘 모르겠지만 일단 걷자!",
                "뭐든 걸으면 좋아! 가자!",
                "생각은 걸으면서! 가자!"
            )
        }
        return responses.random()
    }

    // Streak celebration with total steps or distance
    fun getStreakWithStepsMessage(
        personality: PetPersonality,
        streakDays: Int,
        totalSteps: Long,
        totalDistanceKm: Float,
        isDistanceMode: Boolean
    ): String {
        val valueText = if (isDistanceMode) {
            String.format("%.1fkm", totalDistanceKm)
        } else {
            "${String.format("%,d", totalSteps)}보"
        }
        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "${streakDays}일째 목표 달성. 총 ${valueText}. 대단하군.",
                "${streakDays}일 연속이다. ${valueText} 달성. 멋지다.",
                "연속 ${streakDays}일. 총 ${valueText}. 괴물이야.",
                "${streakDays}일째. ${valueText} 달성. 존경한다.",
                "벌써 ${streakDays}일. 총 ${valueText}. 진짜 대단해."
            )
            PetPersonality.CUTE -> listOf(
                "${streakDays}일째 달성! 총 ${valueText}! 미쳤다ㅋㅋ",
                "연속 ${streakDays}일! ${valueText}나 달성! ㄷㄷ 대박~",
                "${streakDays}일 연속이야! 총 ${valueText}! 대박!",
                "와 ${streakDays}일째! ${valueText}! 진짜 찐이다!",
                "${streakDays}일 달성! 총 ${valueText}라니 미쳤다ㅋㅋ"
            )
            PetPersonality.TSUNDERE -> listOf(
                "${streakDays}일째라니... 총 ${valueText}. 뭐, 대단해.",
                "연속 ${streakDays}일이야. ${valueText}... 인정할게.",
                "${streakDays}일 됐네. 총 ${valueText}. ...칭찬이야.",
                "벌써 ${streakDays}일? ${valueText}라니... 좀 무섭네.",
                "${streakDays}일 연속. ${valueText}. 흥, 잘했어."
            )
            PetPersonality.DIALECT -> listOf(
                "${streakDays}일째 달성 총 ${valueText}",
                "연속 ${streakDays}일 ${valueText} 잘했다",
                "${streakDays}일 됐네 총 ${valueText} 대단하다",
                "벌써 ${streakDays}일 ${valueText}나 달성했네",
                "${streakDays}일 연속 총 ${valueText} 대박이다"
            )
            PetPersonality.TIMID -> listOf(
                "${streakDays}일째예요...! 총 ${valueText}... 대단해요...",
                "연속 ${streakDays}일이에요...! ${valueText}나요...! 우와...",
                "${streakDays}일 됐어요... 총 ${valueText}... 멋져요...",
                "벌써 ${streakDays}일...! ${valueText}라니... 정말요...?",
                "${streakDays}일 연속...! 총 ${valueText}...! 감동이에요..."
            )
            PetPersonality.POSITIVE -> listOf(
                "${streakDays}일째 달성! 총 ${valueText}! 최고야!",
                "연속 ${streakDays}일! ${valueText}나 달성! 대단해!",
                "${streakDays}일 연속이야! 총 ${valueText}! 자랑스러워!",
                "와! ${streakDays}일째! ${valueText}! 진짜 멋져!",
                "${streakDays}일 달성! 총 ${valueText}! 화이팅!"
            )
        }
        return messages.random()
    }

    // Streak milestone celebration (3일, 7일, 14일, 30일 등)
    fun getStreakMilestoneMessage(
        personality: PetPersonality,
        streakDays: Int,
        totalSteps: Long,
        totalDistanceKm: Float,
        isDistanceMode: Boolean
    ): String {
        val valueText = if (isDistanceMode) {
            String.format("%.1fkm", totalDistanceKm)
        } else {
            "${String.format("%,d", totalSteps)}보"
        }
        val milestoneText = when (streakDays) {
            3 -> "3일"
            7 -> "일주일"
            14 -> "2주"
            21 -> "3주"
            30 -> "한 달"
            60 -> "두 달"
            90 -> "석 달"
            100 -> "100일"
            else -> "${streakDays}일"
        }

        val messages = when (personality) {
            PetPersonality.TOUGH -> listOf(
                "${milestoneText} 연속 달성. 총 ${valueText}. 전설이다.",
                "${milestoneText}이나 됐군. ${valueText}. 존경한다.",
                "연속 ${milestoneText}. 총 ${valueText}. 괴물이야."
            )
            PetPersonality.CUTE -> listOf(
                "${milestoneText} 연속! 총 ${valueText}! 레전드ㅋㅋㅋ",
                "와 ${milestoneText}이야! ${valueText}! 미쳤다!",
                "${milestoneText} 달성! 총 ${valueText}! 진짜 대박!"
            )
            PetPersonality.TSUNDERE -> listOf(
                "${milestoneText}이라니... ${valueText}. 인정해줄게.",
                "연속 ${milestoneText}... 총 ${valueText}. 대단해.",
                "${milestoneText}이나 됐네. ${valueText}라니... 멋있어."
            )
            PetPersonality.DIALECT -> listOf(
                "${milestoneText} 연속 총 ${valueText} 대단하다",
                "${milestoneText}이네 ${valueText} 잘했다",
                "${milestoneText} 달성 총 ${valueText} 열심히 했네"
            )
            PetPersonality.TIMID -> listOf(
                "${milestoneText} 연속이에요...! 총 ${valueText}...! 대단해요...",
                "와... ${milestoneText}이에요... ${valueText}라니... 멋져요...",
                "${milestoneText} 됐어요...! 총 ${valueText}...! 감동이에요..."
            )
            PetPersonality.POSITIVE -> listOf(
                "${milestoneText} 연속! 총 ${valueText}! 진짜 최고야!",
                "와! ${milestoneText}이야! ${valueText}! 전설이야!",
                "${milestoneText} 달성! 총 ${valueText}! 너무 자랑스러워!"
            )
        }
        return messages.random()
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
                "오늘도 갓생!",
                "같이 있어서 좋음~",
                "좋아좋아ㅋㅋ",
                "힘내자구 파이토!",
                "ㄹㅇ 넌 최고임"
            )
            PetPersonality.TSUNDERE -> listOf(
                "뭐야, 심심해?",
                "...옆에 있어줄게.",
                "별거 아니야.",
                "흥, 고마워하지마.",
                "...잘하고 있어."
            )
            PetPersonality.DIALECT -> listOf(
                "오늘도 가보자",
                "같이 있으니까 좋네",
                "힘내",
                "잘하고 있다",
                "대단하다"
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

    // 챌린지 성공 칭찬 메시지
    fun getChallengeCompleteMessage(personality: PetPersonality, challengeName: String): String {
        return when (getLang()) {
            "ko" -> getChallengeCompleteMessageKo(personality)
            "ja" -> getChallengeCompleteMessageJa(personality)
            "zh" -> getChallengeCompleteMessageZh(personality)
            "es" -> getChallengeCompleteMessageEs(personality)
            else -> getChallengeCompleteMessageEn(personality)
        }
    }

    private fun getChallengeCompleteMessageKo(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "챌린지 완료. 해냈군."
            PetPersonality.CUTE -> "챌린지 클리어! 레전드~"
            PetPersonality.TSUNDERE -> "챌린지 완료... 나쁘지 않아."
            PetPersonality.DIALECT -> "챌린지 완료 잘했다"
            PetPersonality.TIMID -> "챌린지 완료... 잘, 잘하셨어요..."
            PetPersonality.POSITIVE -> "챌린지 완료! 정말 대단해!"
        }
    }

    private fun getChallengeCompleteMessageEn(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Challenge complete. Well done."
            PetPersonality.CUTE -> "Challenge cleared! Legend~"
            PetPersonality.TSUNDERE -> "Challenge done... not bad."
            PetPersonality.DIALECT -> "Challenge complete. Good job."
            PetPersonality.TIMID -> "Challenge complete... g-great job..."
            PetPersonality.POSITIVE -> "Challenge complete! Amazing!"
        }
    }

    private fun getChallengeCompleteMessageJa(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "チャレンジ完了。やったな。"
            PetPersonality.CUTE -> "チャレンジクリア！レジェンド～"
            PetPersonality.TSUNDERE -> "チャレンジ完了...悪くないね。"
            PetPersonality.DIALECT -> "チャレンジ完了 よくやった"
            PetPersonality.TIMID -> "チャレンジ完了...す、すごいです..."
            PetPersonality.POSITIVE -> "チャレンジ完了！すごい！"
        }
    }

    private fun getChallengeCompleteMessageZh(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "挑战完成。做得好。"
            PetPersonality.CUTE -> "挑战成功！传奇～"
            PetPersonality.TSUNDERE -> "挑战完成...还不错。"
            PetPersonality.DIALECT -> "挑战完成 做得好"
            PetPersonality.TIMID -> "挑战完成...做、做得好..."
            PetPersonality.POSITIVE -> "挑战完成！太棒了！"
        }
    }

    private fun getChallengeCompleteMessageEs(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Desafío completo. Bien hecho."
            PetPersonality.CUTE -> "¡Desafío superado! ¡Leyenda~"
            PetPersonality.TSUNDERE -> "Desafío hecho... no está mal."
            PetPersonality.DIALECT -> "Desafío completo. Buen trabajo."
            PetPersonality.TIMID -> "Desafío completo... b-buen trabajo..."
            PetPersonality.POSITIVE -> "¡Desafío completo! ¡Increíble!"
        }
    }

    // 챌린지 시작 응원 메시지
    fun getChallengeStartMessage(personality: PetPersonality, challengeName: String): String {
        return when (getLang()) {
            "ko" -> getChallengeStartMessageKo(personality)
            "ja" -> getChallengeStartMessageJa(personality)
            "zh" -> getChallengeStartMessageZh(personality)
            "es" -> getChallengeStartMessageEs(personality)
            else -> getChallengeStartMessageEn(personality)
        }
    }

    private fun getChallengeStartMessageKo(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "시작해. 할 수 있어."
            PetPersonality.CUTE -> "ㄹㅇ 화이팅! 할 수 있음!"
            PetPersonality.TSUNDERE -> "...잘 해봐."
            PetPersonality.DIALECT -> "시작이다 가보자"
            PetPersonality.TIMID -> "저, 응원할게요..."
            PetPersonality.POSITIVE -> "화이팅! 넌 해낼 수 있어!"
        }
    }

    private fun getChallengeStartMessageEn(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Start. You can do it."
            PetPersonality.CUTE -> "Go for it! You got this!"
            PetPersonality.TSUNDERE -> "...Do your best."
            PetPersonality.DIALECT -> "Let's start. Go."
            PetPersonality.TIMID -> "I-I'll cheer for you..."
            PetPersonality.POSITIVE -> "Go! You can do it!"
        }
    }

    private fun getChallengeStartMessageJa(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "始めろ。できる。"
            PetPersonality.CUTE -> "ファイト！できるよ！"
            PetPersonality.TSUNDERE -> "...頑張って。"
            PetPersonality.DIALECT -> "始まりや 行こう"
            PetPersonality.TIMID -> "お、応援します..."
            PetPersonality.POSITIVE -> "ファイト！君ならできる！"
        }
    }

    private fun getChallengeStartMessageZh(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "开始。你能行。"
            PetPersonality.CUTE -> "加油！你可以的！"
            PetPersonality.TSUNDERE -> "...加油吧。"
            PetPersonality.DIALECT -> "开始了 走起"
            PetPersonality.TIMID -> "我、我会为你加油..."
            PetPersonality.POSITIVE -> "加油！你一定能做到！"
        }
    }

    private fun getChallengeStartMessageEs(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Empieza. Puedes hacerlo."
            PetPersonality.CUTE -> "¡Vamos! ¡Tú puedes!"
            PetPersonality.TSUNDERE -> "...Hazlo bien."
            PetPersonality.DIALECT -> "Empezamos. Vamos."
            PetPersonality.TIMID -> "T-te animaré..."
            PetPersonality.POSITIVE -> "¡Vamos! ¡Tú puedes!"
        }
    }

    // 챌린지 종료(실패) 응원 메시지
    fun getChallengeEndedMessage(personality: PetPersonality, challengeName: String): String {
        return when (getLang()) {
            "ko" -> getChallengeEndedMessageKo(personality)
            "ja" -> getChallengeEndedMessageJa(personality)
            "zh" -> getChallengeEndedMessageZh(personality)
            "es" -> getChallengeEndedMessageEs(personality)
            else -> getChallengeEndedMessageEn(personality)
        }
    }

    private fun getChallengeEndedMessageKo(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "괜찮아. 다음에 다시 하자."
            PetPersonality.CUTE -> "괜찮아! 다음엔 할 수 있음!"
            PetPersonality.TSUNDERE -> "뭐... 다음에 하면 돼."
            PetPersonality.DIALECT -> "괜찮다 다음에 또 하면 되지"
            PetPersonality.TIMID -> "괜찮아요... 다음에 다시 해봐요..."
            PetPersonality.POSITIVE -> "괜찮아! 도전한 것만으로도 대단해!"
        }
    }

    private fun getChallengeEndedMessageEn(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "It's okay. Try again next time."
            PetPersonality.CUTE -> "It's okay! You'll do it next time!"
            PetPersonality.TSUNDERE -> "Well... do it next time."
            PetPersonality.DIALECT -> "It's fine. Try again later."
            PetPersonality.TIMID -> "It's okay... try again next time..."
            PetPersonality.POSITIVE -> "It's okay! Just trying is amazing!"
        }
    }

    private fun getChallengeEndedMessageJa(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "大丈夫だ。次またやろう。"
            PetPersonality.CUTE -> "大丈夫！次はできるよ！"
            PetPersonality.TSUNDERE -> "まあ...次やればいい。"
            PetPersonality.DIALECT -> "大丈夫や また次やればいい"
            PetPersonality.TIMID -> "大丈夫です...また挑戦しましょう..."
            PetPersonality.POSITIVE -> "大丈夫！挑戦しただけですごい！"
        }
    }

    private fun getChallengeEndedMessageZh(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "没关系。下次再试。"
            PetPersonality.CUTE -> "没关系！下次一定行！"
            PetPersonality.TSUNDERE -> "嗯...下次再做吧。"
            PetPersonality.DIALECT -> "没事 下次再来"
            PetPersonality.TIMID -> "没关系...下次再试试..."
            PetPersonality.POSITIVE -> "没关系！敢于挑战就很棒！"
        }
    }

    private fun getChallengeEndedMessageEs(personality: PetPersonality): String {
        return when (personality) {
            PetPersonality.TOUGH -> "Está bien. Inténtalo de nuevo."
            PetPersonality.CUTE -> "¡Está bien! ¡La próxima lo logras!"
            PetPersonality.TSUNDERE -> "Bueno... hazlo la próxima."
            PetPersonality.DIALECT -> "Está bien. Inténtalo después."
            PetPersonality.TIMID -> "Está bien... inténtalo de nuevo..."
            PetPersonality.POSITIVE -> "¡Está bien! ¡Solo intentarlo es increíble!"
        }
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
