package com.moveoftoday.walkorwait.pet

import java.util.Locale

/**
 * V2 펫 대사 시스템 (6종 펫, 다국어 지원)
 */
object PetDialoguesV2 {

    /**
     * 현재 언어 코드 가져오기
     */
    private fun getLang(): String = Locale.getDefault().language

    // ===== 환영 메시지 (튜토리얼 시작) =====
    fun getWelcomeMessage(personality: PetPersonalityV2, petName: String): String {
        return when (getLang()) {
            "ko" -> getWelcomeMessageKo(personality, petName)
            "ja" -> getWelcomeMessageJa(personality, petName)
            "zh" -> getWelcomeMessageZh(personality, petName)
            "es" -> getWelcomeMessageEs(personality, petName)
            else -> getWelcomeMessageEn(personality, petName)
        }
    }

    private fun getWelcomeMessageKo(p: PetPersonalityV2, name: String): String = when (p) {
        PetPersonalityV2.LOYAL -> "왔구나. $name 이다."
        PetPersonalityV2.TSUNDERE -> "흥, 왔어? ...반갑다고는 안 할 거야."
        PetPersonalityV2.FOODIE -> "안녕~ $name 이야! 밥은 먹었어?"
        PetPersonalityV2.PLAYFUL -> "오! 왔다왔다! 심심했어~"
        PetPersonalityV2.TIMID -> "아, 안녕하세요... 저는 $name 이에요..."
        PetPersonalityV2.CLUMSY -> "앗! 어서와! 미끄러질 뻔 ㅋㅋ"
    }

    private fun getWelcomeMessageEn(p: PetPersonalityV2, name: String): String = when (p) {
        PetPersonalityV2.LOYAL -> "Hey. I'm $name."
        PetPersonalityV2.TSUNDERE -> "Hmph, you came? ...I won't say I'm glad."
        PetPersonalityV2.FOODIE -> "Hi~ I'm $name! Have you eaten?"
        PetPersonalityV2.PLAYFUL -> "Oh! You're here! I was bored~"
        PetPersonalityV2.TIMID -> "Oh, h-hello... I'm $name..."
        PetPersonalityV2.CLUMSY -> "Oops! Welcome! Almost slipped haha"
    }

    private fun getWelcomeMessageJa(p: PetPersonalityV2, name: String): String = when (p) {
        PetPersonalityV2.LOYAL -> "来たね。$name だよ。"
        PetPersonalityV2.TSUNDERE -> "ふん、来たの？...嬉しくないから。"
        PetPersonalityV2.FOODIE -> "やあ~$name だよ！ご飯食べた？"
        PetPersonalityV2.PLAYFUL -> "お！来た来た！暇だったんだ~"
        PetPersonalityV2.TIMID -> "あ、こんにちは...私は$name です..."
        PetPersonalityV2.CLUMSY -> "わっ！いらっしゃい！滑りそうだったww"
    }

    private fun getWelcomeMessageZh(p: PetPersonalityV2, name: String): String = when (p) {
        PetPersonalityV2.LOYAL -> "你来了。我是$name。"
        PetPersonalityV2.TSUNDERE -> "哼，你来了？...我才不会说高兴呢。"
        PetPersonalityV2.FOODIE -> "嗨~我是$name！吃饭了吗？"
        PetPersonalityV2.PLAYFUL -> "哦！你来了！好无聊啊~"
        PetPersonalityV2.TIMID -> "啊，你、你好...我是$name..."
        PetPersonalityV2.CLUMSY -> "哎呀！欢迎！差点滑倒哈哈"
    }

    private fun getWelcomeMessageEs(p: PetPersonalityV2, name: String): String = when (p) {
        PetPersonalityV2.LOYAL -> "Llegaste. Soy $name."
        PetPersonalityV2.TSUNDERE -> "Hmph, ¿viniste? ...No diré que me alegro."
        PetPersonalityV2.FOODIE -> "¡Hola~ Soy $name! ¿Has comido?"
        PetPersonalityV2.PLAYFUL -> "¡Oh! ¡Llegaste! Estaba aburrido~"
        PetPersonalityV2.TIMID -> "Ah, h-hola... Soy $name..."
        PetPersonalityV2.CLUMSY -> "¡Ups! ¡Bienvenido! Casi me resbalo jaja"
    }

    // ===== 튜토리얼 대사 =====
    fun getTutorialStep1(personality: PetPersonalityV2): String {
        return when (getLang()) {
            "ko" -> getTutorialStep1Ko(personality)
            "ja" -> getTutorialStep1Ja(personality)
            "zh" -> getTutorialStep1Zh(personality)
            "es" -> getTutorialStep1Es(personality)
            else -> getTutorialStep1En(personality)
        }
    }

    private fun getTutorialStep1Ko(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "매일 걸으면 된다. 간단하지."
        PetPersonalityV2.TSUNDERE -> "뭐, 걷는 거 도와줄게. 고마워하지 마."
        PetPersonalityV2.FOODIE -> "걸으면 밥이 더 맛있어져! 같이 걷자~"
        PetPersonalityV2.PLAYFUL -> "산책이다! 재밌겠다 히히~"
        PetPersonalityV2.TIMID -> "저, 저랑 같이 걸어주실 거죠...?"
        PetPersonalityV2.CLUMSY -> "걷기! 좋아! 근데 나 자주 넘어져..."
    }

    private fun getTutorialStep1En(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "Just walk every day. Simple."
        PetPersonalityV2.TSUNDERE -> "Well, I'll help you walk. Don't thank me."
        PetPersonalityV2.FOODIE -> "Walking makes food tastier! Let's walk together~"
        PetPersonalityV2.PLAYFUL -> "A walk! This'll be fun hehe~"
        PetPersonalityV2.TIMID -> "Y-you'll walk with me... right...?"
        PetPersonalityV2.CLUMSY -> "Walking! Great! But I fall a lot..."
    }

    private fun getTutorialStep1Ja(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "毎日歩けばいい。簡単だろ。"
        PetPersonalityV2.TSUNDERE -> "まあ、歩くの手伝ってあげる。感謝しないで。"
        PetPersonalityV2.FOODIE -> "歩くとご飯がもっと美味しくなる！一緒に歩こう~"
        PetPersonalityV2.PLAYFUL -> "散歩だ！楽しそう~へへ~"
        PetPersonalityV2.TIMID -> "わ、私と一緒に歩いてくれますよね...？"
        PetPersonalityV2.CLUMSY -> "歩く！いいね！でもよく転ぶんだ..."
    }

    private fun getTutorialStep1Zh(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "每天走路就行。很简单。"
        PetPersonalityV2.TSUNDERE -> "好吧，我会帮你走路的。别谢我。"
        PetPersonalityV2.FOODIE -> "走路会让饭更好吃！一起走吧~"
        PetPersonalityV2.PLAYFUL -> "散步！好好玩嘻嘻~"
        PetPersonalityV2.TIMID -> "你、你会和我一起走...对吧...？"
        PetPersonalityV2.CLUMSY -> "走路！好！但我经常摔倒..."
    }

    private fun getTutorialStep1Es(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "Solo camina todos los días. Simple."
        PetPersonalityV2.TSUNDERE -> "Bueno, te ayudaré a caminar. No me agradezcas."
        PetPersonalityV2.FOODIE -> "¡Caminar hace la comida más rica! Caminemos juntos~"
        PetPersonalityV2.PLAYFUL -> "¡Un paseo! Será divertido jeje~"
        PetPersonalityV2.TIMID -> "¿C-caminarás conmigo... verdad...?"
        PetPersonalityV2.CLUMSY -> "¡Caminar! ¡Genial! Pero me caigo mucho..."
    }

    fun getTutorialStep2(personality: PetPersonalityV2): String {
        return when (getLang()) {
            "ko" -> getTutorialStep2Ko(personality)
            "ja" -> getTutorialStep2Ja(personality)
            "zh" -> getTutorialStep2Zh(personality)
            "es" -> getTutorialStep2Es(personality)
            else -> getTutorialStep2En(personality)
        }
    }

    private fun getTutorialStep2Ko(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "목표를 정해. 지켜."
        PetPersonalityV2.TSUNDERE -> "목표 못 채우면... 좀 실망이야."
        PetPersonalityV2.FOODIE -> "목표 달성하면 간식 타임이지~"
        PetPersonalityV2.PLAYFUL -> "목표 달성하면 뭔가 재밌는 일이?!"
        PetPersonalityV2.TIMID -> "목표... 함께 달성해봐요..."
        PetPersonalityV2.CLUMSY -> "목표! 꼭 해낼 거야! 아마도!"
    }

    private fun getTutorialStep2En(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "Set a goal. Keep it."
        PetPersonalityV2.TSUNDERE -> "If you don't meet the goal... I'll be disappointed."
        PetPersonalityV2.FOODIE -> "Hit the goal and it's snack time~"
        PetPersonalityV2.PLAYFUL -> "Something fun happens when we hit the goal?!"
        PetPersonalityV2.TIMID -> "The goal... let's achieve it together..."
        PetPersonalityV2.CLUMSY -> "Goal! We'll definitely do it! Maybe!"
    }

    private fun getTutorialStep2Ja(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "目標を決めろ。守れ。"
        PetPersonalityV2.TSUNDERE -> "目標達成できなかったら...ちょっとがっかり。"
        PetPersonalityV2.FOODIE -> "目標達成したらおやつタイム~"
        PetPersonalityV2.PLAYFUL -> "目標達成したら何か楽しいことが?!"
        PetPersonalityV2.TIMID -> "目標...一緒に達成しましょう..."
        PetPersonalityV2.CLUMSY -> "目標！絶対できる！たぶん！"
    }

    private fun getTutorialStep2Zh(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "设定目标。遵守它。"
        PetPersonalityV2.TSUNDERE -> "如果达不到目标...会有点失望。"
        PetPersonalityV2.FOODIE -> "达成目标就是零食时间~"
        PetPersonalityV2.PLAYFUL -> "达成目标会有什么有趣的事?!"
        PetPersonalityV2.TIMID -> "目标...一起达成吧..."
        PetPersonalityV2.CLUMSY -> "目标！一定能做到！大概！"
    }

    private fun getTutorialStep2Es(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "Establece una meta. Cúmplela."
        PetPersonalityV2.TSUNDERE -> "Si no cumples la meta... estaré decepcionado."
        PetPersonalityV2.FOODIE -> "¡Cumple la meta y es hora de snacks~"
        PetPersonalityV2.PLAYFUL -> "¿Algo divertido pasa cuando cumplimos la meta?!"
        PetPersonalityV2.TIMID -> "La meta... logrémosla juntos..."
        PetPersonalityV2.CLUMSY -> "¡Meta! ¡Lo lograremos! ¡Quizás!"
    }

    fun getTutorialComplete(personality: PetPersonalityV2): String {
        return when (getLang()) {
            "ko" -> getTutorialCompleteKo(personality)
            "ja" -> getTutorialCompleteJa(personality)
            "zh" -> getTutorialCompleteZh(personality)
            "es" -> getTutorialCompleteEs(personality)
            else -> getTutorialCompleteEn(personality)
        }
    }

    private fun getTutorialCompleteKo(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "시작하자."
        PetPersonalityV2.TSUNDERE -> "뭐, 잘 부탁해. ...진심이야."
        PetPersonalityV2.FOODIE -> "좋아! 가보자고~ 꿀꿀!"
        PetPersonalityV2.PLAYFUL -> "우와! 신난다! 빨리 가자!"
        PetPersonalityV2.TIMID -> "잘, 잘 부탁드려요..."
        PetPersonalityV2.CLUMSY -> "화이팅! 앗 미끄러- 괜찮아!"
    }

    private fun getTutorialCompleteEn(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "Let's begin."
        PetPersonalityV2.TSUNDERE -> "Well, nice to meet you. ...I mean it."
        PetPersonalityV2.FOODIE -> "Great! Let's go~ oink oink!"
        PetPersonalityV2.PLAYFUL -> "Wow! So exciting! Let's go!"
        PetPersonalityV2.TIMID -> "N-nice to meet you..."
        PetPersonalityV2.CLUMSY -> "Let's go! Oops almost slipped- I'm okay!"
    }

    private fun getTutorialCompleteJa(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "始めよう。"
        PetPersonalityV2.TSUNDERE -> "まあ、よろしく。...本気だから。"
        PetPersonalityV2.FOODIE -> "よし！行こう~ブヒブヒ！"
        PetPersonalityV2.PLAYFUL -> "わあ！楽しみ！早く行こう！"
        PetPersonalityV2.TIMID -> "よ、よろしくお願いします..."
        PetPersonalityV2.CLUMSY -> "ファイト！あっ滑り-大丈夫！"
    }

    private fun getTutorialCompleteZh(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "开始吧。"
        PetPersonalityV2.TSUNDERE -> "好吧，请多关照。...我是认真的。"
        PetPersonalityV2.FOODIE -> "好！走吧~哼哼！"
        PetPersonalityV2.PLAYFUL -> "哇！好兴奋！快走吧！"
        PetPersonalityV2.TIMID -> "请、请多关照..."
        PetPersonalityV2.CLUMSY -> "加油！哎呀滑-没事！"
    }

    private fun getTutorialCompleteEs(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "Empecemos."
        PetPersonalityV2.TSUNDERE -> "Bueno, un placer. ...Lo digo en serio."
        PetPersonalityV2.FOODIE -> "¡Genial! ¡Vamos~ oink oink!"
        PetPersonalityV2.PLAYFUL -> "¡Wow! ¡Qué emoción! ¡Vamos!"
        PetPersonalityV2.TIMID -> "E-encantado de conocerte..."
        PetPersonalityV2.CLUMSY -> "¡Vamos! Ups casi me resbal- ¡Estoy bien!"
    }

    // ===== Idle 대사 (0%) =====
    fun getIdleMessage(personality: PetPersonalityV2): String {
        val messages = when (getLang()) {
            "ko" -> getIdleMessagesKo(personality)
            "ja" -> getIdleMessagesJa(personality)
            "zh" -> getIdleMessagesZh(personality)
            "es" -> getIdleMessagesEs(personality)
            else -> getIdleMessagesEn(personality)
        }
        return messages.random()
    }

    private fun getIdleMessagesKo(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("산책 가자.", "뭐해? 걷자.", "오늘 목표, 시작하자.", "밖으로 나가자.")
        PetPersonalityV2.TSUNDERE -> listOf("...가자고.", "언제까지 있을 거야?", "나가자. 지금.", "...심심해.")
        PetPersonalityV2.FOODIE -> listOf("배고파~ 걸으면 밥 맛있어지는데!", "산책 가면 간식 줄 거지?", "오늘 뭐 먹지~ 아, 걷자!")
        PetPersonalityV2.PLAYFUL -> listOf("심심해! 나가자나가자!", "뭐해뭐해? 놀러 가자!", "산책각이다! 고고!")
        PetPersonalityV2.TIMID -> listOf("저, 산책...", "오늘... 걸어볼까요...?", "밖에... 나가고 싶어요...")
        PetPersonalityV2.CLUMSY -> listOf("산책 가자! 앗 문턱-", "밖에 나가고 싶어! 뒤뚱뒤뚱~", "오늘도 열심히!")
    }

    private fun getIdleMessagesEn(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("Let's walk.", "What're you doing? Let's go.", "Today's goal. Let's start.", "Let's go outside.")
        PetPersonalityV2.TSUNDERE -> listOf("...Let's go.", "How long will you stay?", "Let's go. Now.", "...I'm bored.")
        PetPersonalityV2.FOODIE -> listOf("I'm hungry~ Walking makes food taste better!", "Will you give me snacks if we walk?", "What to eat~ Oh, let's walk!")
        PetPersonalityV2.PLAYFUL -> listOf("Bored! Let's go out!", "Whatcha doing? Let's play!", "Time for a walk! Go go!")
        PetPersonalityV2.TIMID -> listOf("Um, walk...", "Should we... walk today...?", "I want to... go outside...")
        PetPersonalityV2.CLUMSY -> listOf("Let's walk! Oops, doorstep-", "I want to go out! Wobble~", "Let's do our best today!")
    }

    private fun getIdleMessagesJa(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("散歩行こう。", "何してる？歩こう。", "今日の目標、始めよう。", "外に出よう。")
        PetPersonalityV2.TSUNDERE -> listOf("...行くよ。", "いつまでいるの？", "出かけよう。今。", "...暇だし。")
        PetPersonalityV2.FOODIE -> listOf("お腹空いた~歩くとご飯美味しくなるよ！", "散歩したらおやつくれる？", "何食べよう~あ、歩こう！")
        PetPersonalityV2.PLAYFUL -> listOf("暇だ！出かけよう！", "何してるの？遊びに行こう！", "散歩行こ！ゴーゴー！")
        PetPersonalityV2.TIMID -> listOf("あの、散歩...", "今日...歩いてみますか...？", "外に...出たいです...")
        PetPersonalityV2.CLUMSY -> listOf("散歩行こう！あっ段差-", "外に出たい！よちよち~", "今日も頑張ろう！")
    }

    private fun getIdleMessagesZh(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("去散步吧。", "在干嘛？走吧。", "今天的目标，开始吧。", "出去吧。")
        PetPersonalityV2.TSUNDERE -> listOf("...走吧。", "要待到什么时候？", "出去吧。现在。", "...好无聊。")
        PetPersonalityV2.FOODIE -> listOf("好饿~走路会让饭更好吃！", "散步的话会给我零食吧？", "吃什么呢~啊，走吧！")
        PetPersonalityV2.PLAYFUL -> listOf("无聊！出去玩吧！", "在干嘛？去玩吧！", "散步时间！冲冲冲！")
        PetPersonalityV2.TIMID -> listOf("那个，散步...", "今天...走走看吗...？", "想...出去...")
        PetPersonalityV2.CLUMSY -> listOf("去散步吧！哎呀门槛-", "想出去！摇摇晃晃~", "今天也加油！")
    }

    private fun getIdleMessagesEs(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("Vamos a caminar.", "¿Qué haces? Vamos.", "Meta de hoy. Empecemos.", "Salgamos.")
        PetPersonalityV2.TSUNDERE -> listOf("...Vamos.", "¿Cuánto más vas a quedarte?", "Salgamos. Ahora.", "...Estoy aburrido.")
        PetPersonalityV2.FOODIE -> listOf("¡Tengo hambre~ Caminar hace la comida más rica!", "¿Me darás snacks si caminamos?", "¿Qué comer~ Ah, vamos!")
        PetPersonalityV2.PLAYFUL -> listOf("¡Aburrido! ¡Salgamos!", "¿Qué haces? ¡Vamos a jugar!", "¡Hora de caminar! ¡Vamos!")
        PetPersonalityV2.TIMID -> listOf("Um, caminar...", "¿Caminamos... hoy...?", "Quiero... salir...")
        PetPersonalityV2.CLUMSY -> listOf("¡Vamos a caminar! Ups, escalón-", "¡Quiero salir! Tambaleando~", "¡Hagamos nuestro mejor!")
    }

    // ===== Walking 대사 (진행 중) =====
    fun getWalkingMessage(personality: PetPersonalityV2, progressPercent: Int): String {
        return when (getLang()) {
            "ko" -> getWalkingMessageKo(personality, progressPercent)
            "ja" -> getWalkingMessageJa(personality, progressPercent)
            "zh" -> getWalkingMessageZh(personality, progressPercent)
            "es" -> getWalkingMessageEs(personality, progressPercent)
            else -> getWalkingMessageEn(personality, progressPercent)
        }
    }

    private fun getWalkingMessageKo(p: PetPersonalityV2, pct: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { pct < 30 -> "더 걸어."; pct < 70 -> "괜찮네."; pct < 100 -> "거의 다 왔다."; else -> "됐다. 수고했어." }
        PetPersonalityV2.TSUNDERE -> when { pct < 30 -> "...따라올 거야?"; pct < 70 -> "뭐, 나쁘지 않네."; pct < 100 -> "좀 더 해봐."; else -> "흥, 잘했어. ...칭찬이야." }
        PetPersonalityV2.FOODIE -> when { pct < 30 -> "걸으니까 배고파~"; pct < 70 -> "반 왔다! 간식각!"; pct < 100 -> "조금만 더! 밥 먹으러!"; else -> "다 했다! 밥 타임~!" }
        PetPersonalityV2.PLAYFUL -> when { pct < 30 -> "재밌다! 더 가자!"; pct < 70 -> "오 벌써 반이야?!"; pct < 100 -> "거의 다 왔어! 신난다!"; else -> "해냈다!! 우와아!!" }
        PetPersonalityV2.TIMID -> when { pct < 30 -> "저, 천천히 가요..."; pct < 70 -> "잘하고 계세요..."; pct < 100 -> "조금만 더요..."; else -> "해, 해냈어요...!" }
        PetPersonalityV2.CLUMSY -> when { pct < 30 -> "뒤뚱뒤뚱~ 앗 돌멩이!"; pct < 70 -> "반 왔어! 안 넘어졌다!"; pct < 100 -> "거의 다! 조심조심..."; else -> "해냈어! 앗 미끄- 괜찮아!" }
    }

    private fun getWalkingMessageEn(p: PetPersonalityV2, pct: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { pct < 30 -> "Walk more."; pct < 70 -> "Not bad."; pct < 100 -> "Almost there."; else -> "Done. Good job." }
        PetPersonalityV2.TSUNDERE -> when { pct < 30 -> "...Will you keep up?"; pct < 70 -> "Well, not bad."; pct < 100 -> "Try a bit more."; else -> "Hmph, good job. ...That's praise." }
        PetPersonalityV2.FOODIE -> when { pct < 30 -> "Walking makes me hungry~"; pct < 70 -> "Halfway! Snack time!"; pct < 100 -> "A bit more! Food time!"; else -> "Done! Food time~!" }
        PetPersonalityV2.PLAYFUL -> when { pct < 30 -> "Fun! Let's go more!"; pct < 70 -> "Oh, already halfway?!"; pct < 100 -> "Almost there! Exciting!"; else -> "We did it!! Woohoo!!" }
        PetPersonalityV2.TIMID -> when { pct < 30 -> "L-let's go slow..."; pct < 70 -> "You're doing well..."; pct < 100 -> "Just a bit more..."; else -> "W-we did it...!" }
        PetPersonalityV2.CLUMSY -> when { pct < 30 -> "Wobble~ Oops, a rock!"; pct < 70 -> "Halfway! Didn't fall!"; pct < 100 -> "Almost! Careful..."; else -> "Made it! Oops slip- I'm okay!" }
    }

    private fun getWalkingMessageJa(p: PetPersonalityV2, pct: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { pct < 30 -> "もっと歩け。"; pct < 70 -> "悪くない。"; pct < 100 -> "もう少し。"; else -> "よし。お疲れ。" }
        PetPersonalityV2.TSUNDERE -> when { pct < 30 -> "...ついてくる？"; pct < 70 -> "まあ、悪くない。"; pct < 100 -> "もう少し頑張って。"; else -> "ふん、よくやった。...褒めてるよ。" }
        PetPersonalityV2.FOODIE -> when { pct < 30 -> "歩くとお腹空く~"; pct < 70 -> "半分来た！おやつ！"; pct < 100 -> "もう少し！ご飯！"; else -> "できた！ご飯タイム~!" }
        PetPersonalityV2.PLAYFUL -> when { pct < 30 -> "楽しい！もっと行こう！"; pct < 70 -> "お、もう半分?!"; pct < 100 -> "もう少し！わくわく！"; else -> "やった!!うわあ!!" }
        PetPersonalityV2.TIMID -> when { pct < 30 -> "ゆ、ゆっくり行きましょう..."; pct < 70 -> "上手くやってます..."; pct < 100 -> "もう少しです..."; else -> "や、やりました...!" }
        PetPersonalityV2.CLUMSY -> when { pct < 30 -> "よちよち~あっ石!"; pct < 70 -> "半分！転んでない！"; pct < 100 -> "もう少し！慎重に..."; else -> "できた！あっ滑り-大丈夫！" }
    }

    private fun getWalkingMessageZh(p: PetPersonalityV2, pct: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { pct < 30 -> "再走一点。"; pct < 70 -> "不错。"; pct < 100 -> "快到了。"; else -> "完成了。辛苦了。" }
        PetPersonalityV2.TSUNDERE -> when { pct < 30 -> "...你会跟上吗？"; pct < 70 -> "嗯，还不错。"; pct < 100 -> "再努力一点。"; else -> "哼，做得好。...是夸奖啦。" }
        PetPersonalityV2.FOODIE -> when { pct < 30 -> "走路好饿~"; pct < 70 -> "一半了！零食时间！"; pct < 100 -> "再一点！吃饭！"; else -> "完成了！吃饭时间~!" }
        PetPersonalityV2.PLAYFUL -> when { pct < 30 -> "好玩！继续走！"; pct < 70 -> "哦，已经一半了?!"; pct < 100 -> "快到了！好兴奋！"; else -> "做到了!!哇!!" }
        PetPersonalityV2.TIMID -> when { pct < 30 -> "慢、慢慢来..."; pct < 70 -> "你做得很好..."; pct < 100 -> "再一点点..."; else -> "我、我们做到了...!" }
        PetPersonalityV2.CLUMSY -> when { pct < 30 -> "摇摇晃晃~哎呀石头!"; pct < 70 -> "一半了！没摔倒！"; pct < 100 -> "快到了！小心..."; else -> "做到了！哎呀滑-没事！" }
    }

    private fun getWalkingMessageEs(p: PetPersonalityV2, pct: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { pct < 30 -> "Camina más."; pct < 70 -> "Nada mal."; pct < 100 -> "Casi llegas."; else -> "Listo. Buen trabajo." }
        PetPersonalityV2.TSUNDERE -> when { pct < 30 -> "...¿Seguirás?"; pct < 70 -> "Bueno, no está mal."; pct < 100 -> "Intenta un poco más."; else -> "Hmph, bien hecho. ...Es un elogio." }
        PetPersonalityV2.FOODIE -> when { pct < 30 -> "Caminar me da hambre~"; pct < 70 -> "¡A mitad! ¡Snack!"; pct < 100 -> "¡Un poco más! ¡A comer!"; else -> "¡Listo! ¡Hora de comer~!" }
        PetPersonalityV2.PLAYFUL -> when { pct < 30 -> "¡Divertido! ¡Vamos más!"; pct < 70 -> "¡Oh, ya mitad?!"; pct < 100 -> "¡Casi! ¡Qué emoción!"; else -> "¡¡Lo logramos!! ¡¡Woohoo!!" }
        PetPersonalityV2.TIMID -> when { pct < 30 -> "D-despacio..."; pct < 70 -> "Lo estás haciendo bien..."; pct < 100 -> "Solo un poco más..."; else -> "L-lo logramos...!" }
        PetPersonalityV2.CLUMSY -> when { pct < 30 -> "Tambaleo~ ¡Ups, piedra!"; pct < 70 -> "¡Mitad! ¡No me caí!"; pct < 100 -> "¡Casi! Cuidado..."; else -> "¡Listo! Ups resbal- ¡Estoy bien!" }
    }

    // ===== 목표 달성 (100%) =====
    fun getGoalAchievedMessage(personality: PetPersonalityV2): String {
        val messages = when (getLang()) {
            "ko" -> getGoalAchievedMessagesKo(personality)
            "ja" -> getGoalAchievedMessagesJa(personality)
            "zh" -> getGoalAchievedMessagesZh(personality)
            "es" -> getGoalAchievedMessagesEs(personality)
            else -> getGoalAchievedMessagesEn(personality)
        }
        return messages.random()
    }

    private fun getGoalAchievedMessagesKo(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("해냈다. 100% 달성.", "목표 달성. 대단하군.", "완벽해. 수고했어.", "역시 널 믿었어.", "100%. 자랑스럽다.")
        PetPersonalityV2.TSUNDERE -> listOf("뭐, 100%라니 잘했어.", "칭찬해줄게. 오늘만.", "대단해... 라고 할까.", "인정해줄게. 100%니까.", "흥, 잘했어. ...진짜로.")
        PetPersonalityV2.FOODIE -> listOf("100%! 밥 먹자!!", "해냈다! 간식 타임~", "목표 달성! 배고파 꿀꿀!", "100%! 맛있는 거 먹으러 가자!", "완벽해! 오늘 저녁 뭐야?")
        PetPersonalityV2.PLAYFUL -> listOf("100%!! 대박대박!!", "해냈다! 우와아아!", "미쳤다 100%! 파티다!", "최고야! 놀러 가자!", "완전 레전드! 100%!")
        PetPersonalityV2.TIMID -> listOf("100%... 해, 해냈어요...!", "목표 달성이에요... 정말요...!", "저, 정말 기뻐요...", "대단해요... 100%라니...", "우와... 정말 해냈어요...")
        PetPersonalityV2.CLUMSY -> listOf("100%! 해냈- 앗 미끄러! 괜찮아!", "완전 성공! 뒤뚱뒤뚱 춤!", "해냈어! 넘어지지 않고!", "100%다! 축하해! 앗 발 걸려-", "대박! 오늘 안 넘어졌어! 아 방금 넘어졌다")
    }

    private fun getGoalAchievedMessagesEn(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("You did it. 100%.", "Goal achieved. Impressive.", "Perfect. Good work.", "I believed in you.", "100%. Proud of you.")
        PetPersonalityV2.TSUNDERE -> listOf("Well, 100%? Good job.", "I'll praise you. Just today.", "Amazing... I guess.", "I acknowledge it. It's 100%.", "Hmph, good job. ...Really.")
        PetPersonalityV2.FOODIE -> listOf("100%! Let's eat!!", "We did it! Snack time~", "Goal achieved! Hungry oink!", "100%! Let's get food!", "Perfect! What's for dinner?")
        PetPersonalityV2.PLAYFUL -> listOf("100%!! Amazing!!", "We did it! Woohoo!!", "Crazy 100%! Party!!", "The best! Let's play!", "Legendary! 100%!")
        PetPersonalityV2.TIMID -> listOf("100%... W-we did it...!", "Goal achieved... really...!", "I-I'm so happy...", "Amazing... 100%...", "Wow... we really did it...")
        PetPersonalityV2.CLUMSY -> listOf("100%! Made- oops slip! I'm okay!", "Total success! Wobble dance!", "Made it! Without falling!", "100%! Congrats! Oops tripped-", "Wow! Didn't fall today! Oh just fell")
    }

    private fun getGoalAchievedMessagesJa(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("やった。100%達成。", "目標達成。すごいね。", "完璧。お疲れ。", "やっぱり君を信じてた。", "100%。誇りに思う。")
        PetPersonalityV2.TSUNDERE -> listOf("まあ、100%？よくやった。", "褒めてあげる。今日だけ。", "すごい...かな。", "認めてあげる。100%だから。", "ふん、よくやった。...本当に。")
        PetPersonalityV2.FOODIE -> listOf("100%！ご飯食べよう!!", "やった！おやつタイム~", "目標達成！お腹空いたブヒ！", "100%！美味しいもの食べに行こう！", "完璧！今日の夜ご飯は？")
        PetPersonalityV2.PLAYFUL -> listOf("100%!!すごい!!", "やった！うわあ!!", "ヤバい100%！パーティー!!", "最高！遊びに行こう！", "レジェンド！100%!")
        PetPersonalityV2.TIMID -> listOf("100%...や、やりました...!", "目標達成です...本当に...!", "わ、私すごく嬉しい...", "すごい...100%なんて...", "わあ...本当にやりました...")
        PetPersonalityV2.CLUMSY -> listOf("100%！やっ-あっ滑った！大丈夫！", "大成功！よちよちダンス！", "やった！転ばずに！", "100%だ！おめでとう！あっ足が-", "すごい！今日は転んでない！あ今転んだ")
    }

    private fun getGoalAchievedMessagesZh(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("做到了。100%达成。", "目标达成。了不起。", "完美。辛苦了。", "果然相信你是对的。", "100%。为你骄傲。")
        PetPersonalityV2.TSUNDERE -> listOf("嗯，100%？做得好。", "夸奖你。只限今天。", "厉害...吧。", "承认你。因为是100%。", "哼，做得好。...真的。")
        PetPersonalityV2.FOODIE -> listOf("100%！吃饭吧!!", "做到了！零食时间~", "目标达成！饿了哼哼！", "100%！去吃好吃的！", "完美！今晚吃什么？")
        PetPersonalityV2.PLAYFUL -> listOf("100%!!太棒了!!", "做到了！哇!!", "疯了100%！派对!!", "最棒！去玩吧！", "传说！100%!")
        PetPersonalityV2.TIMID -> listOf("100%...我、我们做到了...!", "目标达成了...真的...!", "我、我好开心...", "好厉害...100%...", "哇...真的做到了...")
        PetPersonalityV2.CLUMSY -> listOf("100%！做到-哎呀滑倒！没事！", "完全成功！摇晃舞！", "做到了！没摔倒！", "100%！恭喜！哎呀绊倒-", "哇！今天没摔倒！啊刚才摔了")
    }

    private fun getGoalAchievedMessagesEs(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("Lo lograste. 100%.", "Meta lograda. Impresionante.", "Perfecto. Buen trabajo.", "Creí en ti.", "100%. Orgulloso.")
        PetPersonalityV2.TSUNDERE -> listOf("Bueno, ¿100%? Bien hecho.", "Te elogio. Solo hoy.", "Increíble... supongo.", "Lo reconozco. Es 100%.", "Hmph, buen trabajo. ...De verdad.")
        PetPersonalityV2.FOODIE -> listOf("¡100%! ¡A comer!!", "¡Lo logramos! Hora de snack~", "¡Meta! ¡Hambre oink!", "¡100%! ¡Vamos a comer!", "¡Perfecto! ¿Qué hay de cena?")
        PetPersonalityV2.PLAYFUL -> listOf("¡¡100%!! ¡¡Increíble!!", "¡Lo logramos! ¡¡Woohoo!!", "¡¡Loco 100%!! ¡¡Fiesta!!", "¡Lo mejor! ¡Vamos a jugar!", "¡¡Legendario!! ¡100%!")
        PetPersonalityV2.TIMID -> listOf("100%... L-lo logramos...!", "Meta lograda... de verdad...!", "E-estoy tan feliz...", "Increíble... 100%...", "Wow... realmente lo logramos...")
        PetPersonalityV2.CLUMSY -> listOf("¡100%! Log- ups resbalé! ¡Estoy bien!", "¡Éxito total! ¡Baile tambaleante!", "¡Lo logré! ¡Sin caerme!", "¡100%! ¡Felicidades! Ups tropecé-", "¡Wow! ¡No me caí hoy! Ah, me caí")
    }

    // ===== 레벨업 축하 =====
    fun getLevelUpMessage(personality: PetPersonalityV2, newLevel: Int): String {
        return when (getLang()) {
            "ko" -> getLevelUpMessageKo(personality, newLevel)
            "ja" -> getLevelUpMessageJa(personality, newLevel)
            "zh" -> getLevelUpMessageZh(personality, newLevel)
            "es" -> getLevelUpMessageEs(personality, newLevel)
            else -> getLevelUpMessageEn(personality, newLevel)
        }
    }

    private fun getLevelUpMessageKo(p: PetPersonalityV2, lv: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> "레벨 $lv. 성장했군."
        PetPersonalityV2.TSUNDERE -> "레벨 $lv 이라... 뭐, 축하해."
        PetPersonalityV2.FOODIE -> "레벨 $lv! 축하 파티다! 밥!"
        PetPersonalityV2.PLAYFUL -> "우와! 레벨 $lv! 신난다!!"
        PetPersonalityV2.TIMID -> "레벨 $lv 이에요...! 감사해요..."
        PetPersonalityV2.CLUMSY -> "레벨 $lv! 축하- 앗 케이크!"
    }

    private fun getLevelUpMessageEn(p: PetPersonalityV2, lv: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> "Level $lv. You've grown."
        PetPersonalityV2.TSUNDERE -> "Level $lv huh... Well, congrats."
        PetPersonalityV2.FOODIE -> "Level $lv! Party time! Food!"
        PetPersonalityV2.PLAYFUL -> "Wow! Level $lv! Exciting!!"
        PetPersonalityV2.TIMID -> "Level $lv...! Thank you..."
        PetPersonalityV2.CLUMSY -> "Level $lv! Congra- oops cake!"
    }

    private fun getLevelUpMessageJa(p: PetPersonalityV2, lv: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> "レベル$lv。成長したね。"
        PetPersonalityV2.TSUNDERE -> "レベル$lv か...まあ、おめでとう。"
        PetPersonalityV2.FOODIE -> "レベル$lv！パーティーだ！ご飯！"
        PetPersonalityV2.PLAYFUL -> "わあ！レベル$lv！楽しい!!"
        PetPersonalityV2.TIMID -> "レベル$lv です...！ありがとう..."
        PetPersonalityV2.CLUMSY -> "レベル$lv！おめで-あっケーキ！"
    }

    private fun getLevelUpMessageZh(p: PetPersonalityV2, lv: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> "等级$lv。你成长了。"
        PetPersonalityV2.TSUNDERE -> "等级$lv 啊...嗯，恭喜。"
        PetPersonalityV2.FOODIE -> "等级$lv！派对时间！吃饭！"
        PetPersonalityV2.PLAYFUL -> "哇！等级$lv！好兴奋!!"
        PetPersonalityV2.TIMID -> "等级$lv...！谢谢..."
        PetPersonalityV2.CLUMSY -> "等级$lv！恭喜-哎呀蛋糕！"
    }

    private fun getLevelUpMessageEs(p: PetPersonalityV2, lv: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> "Nivel $lv. Has crecido."
        PetPersonalityV2.TSUNDERE -> "Nivel $lv eh... Bueno, felicidades."
        PetPersonalityV2.FOODIE -> "¡Nivel $lv! ¡Fiesta! ¡Comida!"
        PetPersonalityV2.PLAYFUL -> "¡Wow! ¡Nivel $lv! ¡¡Emocionante!!"
        PetPersonalityV2.TIMID -> "Nivel $lv...! Gracias..."
        PetPersonalityV2.CLUMSY -> "¡Nivel $lv! Felici- ups ¡pastel!"
    }

    // ===== 진화 (성장 단계 변경) =====
    fun getEvolutionMessage(personality: PetPersonalityV2, newStage: PetGrowthStage): String {
        val stageName = newStage.displayName
        return when (getLang()) {
            "ko" -> getEvolutionMessageKo(personality, stageName)
            "ja" -> getEvolutionMessageJa(personality, stageName)
            "zh" -> getEvolutionMessageZh(personality, stageName)
            "es" -> getEvolutionMessageEs(personality, stageName)
            else -> getEvolutionMessageEn(personality, stageName)
        }
    }

    private fun getEvolutionMessageKo(p: PetPersonalityV2, s: String): String = when (p) {
        PetPersonalityV2.LOYAL -> "진화했다. $s 가 됐어."
        PetPersonalityV2.TSUNDERE -> "$s 로 진화라니... 기쁘다고는 안 할 거야. ...조금 기뻐."
        PetPersonalityV2.FOODIE -> "우와! $s 다! 밥 더 많이 먹을 수 있어?!"
        PetPersonalityV2.PLAYFUL -> "$s 진화! 미쳤다!! 더 재밌어지겠다!!"
        PetPersonalityV2.TIMID -> "$s 가 됐어요...! 저, 정말 기뻐요...!"
        PetPersonalityV2.CLUMSY -> "$s 다! 이제 덜 넘어질- 앗! ...아닌가봐"
    }

    private fun getEvolutionMessageEn(p: PetPersonalityV2, s: String): String = when (p) {
        PetPersonalityV2.LOYAL -> "Evolved. I'm $s now."
        PetPersonalityV2.TSUNDERE -> "Evolved to $s... I won't say I'm happy. ...A little happy."
        PetPersonalityV2.FOODIE -> "Wow! I'm $s! Can I eat more?!"
        PetPersonalityV2.PLAYFUL -> "$s evolution! Crazy!! More fun ahead!!"
        PetPersonalityV2.TIMID -> "I became $s...! I-I'm so happy...!"
        PetPersonalityV2.CLUMSY -> "I'm $s! Now I'll fall less- oops! ...Maybe not"
    }

    private fun getEvolutionMessageJa(p: PetPersonalityV2, s: String): String = when (p) {
        PetPersonalityV2.LOYAL -> "進化した。$s になった。"
        PetPersonalityV2.TSUNDERE -> "$s に進化なんて...嬉しいとは言わない。...ちょっと嬉しい。"
        PetPersonalityV2.FOODIE -> "わあ！$s だ！もっと食べられる?!"
        PetPersonalityV2.PLAYFUL -> "$s 進化！ヤバい!!もっと楽しくなる!!"
        PetPersonalityV2.TIMID -> "$s になりました...！わ、私すごく嬉しい...！"
        PetPersonalityV2.CLUMSY -> "$s だ！もう転ばなく-あっ！...違うみたい"
    }

    private fun getEvolutionMessageZh(p: PetPersonalityV2, s: String): String = when (p) {
        PetPersonalityV2.LOYAL -> "进化了。变成${s}了。"
        PetPersonalityV2.TSUNDERE -> "进化成${s}...我不会说高兴的。...有点高兴。"
        PetPersonalityV2.FOODIE -> "哇！我是${s}了！可以吃更多吗?!"
        PetPersonalityV2.PLAYFUL -> "${s}进化！疯了!!会更好玩!!"
        PetPersonalityV2.TIMID -> "变成${s}了...！我、我好开心...！"
        PetPersonalityV2.CLUMSY -> "我是${s}了！现在不会摔-哎呀！...好像不是"
    }

    private fun getEvolutionMessageEs(p: PetPersonalityV2, s: String): String = when (p) {
        PetPersonalityV2.LOYAL -> "Evolucioné. Ahora soy $s."
        PetPersonalityV2.TSUNDERE -> "Evolucioné a $s... No diré que estoy feliz. ...Un poco feliz."
        PetPersonalityV2.FOODIE -> "¡Wow! ¡Soy $s! ¿Puedo comer más?!"
        PetPersonalityV2.PLAYFUL -> "¡Evolución $s! ¡¡Loco!! ¡¡Más diversión!!"
        PetPersonalityV2.TIMID -> "Me convertí en $s...! E-estoy muy feliz...!"
        PetPersonalityV2.CLUMSY -> "¡Soy $s! Ahora caeré menos- ¡ups! ...Quizás no"
    }

    // ===== 부화 (Egg → Baby) =====
    fun getHatchMessage(personality: PetPersonalityV2, petName: String): String {
        return when (getLang()) {
            "ko" -> getHatchMessageKo(personality, petName)
            "ja" -> getHatchMessageJa(personality, petName)
            "zh" -> getHatchMessageZh(personality, petName)
            "es" -> getHatchMessageEs(personality, petName)
            else -> getHatchMessageEn(personality, petName)
        }
    }

    private fun getHatchMessageKo(p: PetPersonalityV2, name: String): String = when (p) {
        PetPersonalityV2.LOYAL -> "안녕. $name 이다. 잘 부탁해."
        PetPersonalityV2.TSUNDERE -> "흥, 태어났어. ...잘 부탁해. 딱 한번만 말할 거야."
        PetPersonalityV2.FOODIE -> "안녕! $name 이야! 밥은 언제 줘?"
        PetPersonalityV2.PLAYFUL -> "야호! 태어났다! $name 이야! 놀자!!"
        PetPersonalityV2.TIMID -> "안, 안녕하세요... 저는 $name 이에요..."
        PetPersonalityV2.CLUMSY -> "안녕! 나는 $name- 앗 껍데기에 걸려서! 괜찮아!"
    }

    private fun getHatchMessageEn(p: PetPersonalityV2, name: String): String = when (p) {
        PetPersonalityV2.LOYAL -> "Hi. I'm $name. Nice to meet you."
        PetPersonalityV2.TSUNDERE -> "Hmph, I'm born. ...Nice to meet you. Only saying it once."
        PetPersonalityV2.FOODIE -> "Hi! I'm $name! When's food?"
        PetPersonalityV2.PLAYFUL -> "Yay! I'm born! I'm $name! Let's play!!"
        PetPersonalityV2.TIMID -> "H-hello... I'm $name..."
        PetPersonalityV2.CLUMSY -> "Hi! I'm $name- oops shell! I'm okay!"
    }

    private fun getHatchMessageJa(p: PetPersonalityV2, name: String): String = when (p) {
        PetPersonalityV2.LOYAL -> "やあ。$name だよ。よろしく。"
        PetPersonalityV2.TSUNDERE -> "ふん、生まれたよ。...よろしく。一回だけ言うから。"
        PetPersonalityV2.FOODIE -> "やあ！$name だよ！ご飯はいつ？"
        PetPersonalityV2.PLAYFUL -> "やった！生まれた！$name だよ！遊ぼう!!"
        PetPersonalityV2.TIMID -> "こ、こんにちは...$name です..."
        PetPersonalityV2.CLUMSY -> "やあ！僕は$name-あっ殻が！大丈夫！"
    }

    private fun getHatchMessageZh(p: PetPersonalityV2, name: String): String = when (p) {
        PetPersonalityV2.LOYAL -> "你好。我是$name。请多关照。"
        PetPersonalityV2.TSUNDERE -> "哼，我出生了。...请多关照。只说一次。"
        PetPersonalityV2.FOODIE -> "你好！我是$name！什么时候吃饭？"
        PetPersonalityV2.PLAYFUL -> "耶！我出生了！我是$name！玩吧!!"
        PetPersonalityV2.TIMID -> "你、你好...我是$name..."
        PetPersonalityV2.CLUMSY -> "你好！我是$name-哎呀壳！没事！"
    }

    private fun getHatchMessageEs(p: PetPersonalityV2, name: String): String = when (p) {
        PetPersonalityV2.LOYAL -> "Hola. Soy $name. Un gusto."
        PetPersonalityV2.TSUNDERE -> "Hmph, nací. ...Un gusto. Solo lo digo una vez."
        PetPersonalityV2.FOODIE -> "¡Hola! ¡Soy $name! ¿Cuándo comemos?"
        PetPersonalityV2.PLAYFUL -> "¡Yay! ¡Nací! ¡Soy $name! ¡¡Juguemos!!"
        PetPersonalityV2.TIMID -> "H-hola... Soy $name..."
        PetPersonalityV2.CLUMSY -> "¡Hola! Soy $name- ¡ups cascarón! ¡Estoy bien!"
    }

    // ===== 알 상태 대사 =====
    fun getEggMessage(progressPercent: Int): String {
        return when (getLang()) {
            "ko" -> getEggMessageKo(progressPercent)
            "ja" -> getEggMessageJa(progressPercent)
            "zh" -> getEggMessageZh(progressPercent)
            "es" -> getEggMessageEs(progressPercent)
            else -> getEggMessageEn(progressPercent)
        }
    }

    private fun getEggMessageKo(pct: Int): String = when {
        pct < 30 -> "..."
        pct < 50 -> "...*꿈틀*..."
        pct < 70 -> "*흔들흔들*..."
        pct < 90 -> "*두근두근* 곧 만날 수 있어요!"
        else -> "*파직파직* 나올 준비 중...!"
    }

    private fun getEggMessageEn(pct: Int): String = when {
        pct < 30 -> "..."
        pct < 50 -> "...*wiggle*..."
        pct < 70 -> "*shake shake*..."
        pct < 90 -> "*thump thump* We'll meet soon!"
        else -> "*crack crack* Getting ready...!"
    }

    private fun getEggMessageJa(pct: Int): String = when {
        pct < 30 -> "..."
        pct < 50 -> "...*もぞもぞ*..."
        pct < 70 -> "*ゆらゆら*..."
        pct < 90 -> "*ドキドキ* もうすぐ会える！"
        else -> "*パリパリ* 出る準備中...！"
    }

    private fun getEggMessageZh(pct: Int): String = when {
        pct < 30 -> "..."
        pct < 50 -> "...*蠕动*..."
        pct < 70 -> "*摇晃摇晃*..."
        pct < 90 -> "*咚咚* 很快就能见面了！"
        else -> "*咔嚓咔嚓* 准备出来了...！"
    }

    private fun getEggMessageEs(pct: Int): String = when {
        pct < 30 -> "..."
        pct < 50 -> "...*movimiento*..."
        pct < 70 -> "*temblor temblor*..."
        pct < 90 -> "*tum tum* ¡Pronto nos veremos!"
        else -> "*crack crack* ¡Preparándome...!"
    }

    // ===== 밤 시간 대사 =====
    fun getNightMessage(personality: PetPersonalityV2): String {
        return when (getLang()) {
            "ko" -> getNightMessageKo(personality)
            "ja" -> getNightMessageJa(personality)
            "zh" -> getNightMessageZh(personality)
            "es" -> getNightMessageEs(personality)
            else -> getNightMessageEn(personality)
        }
    }

    private fun getNightMessageKo(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "밤이다. 조심해서 걸어."
        PetPersonalityV2.TSUNDERE -> "어두운데... 괜찮아? ...걱정 아니야."
        PetPersonalityV2.FOODIE -> "밤이네~ 야식 먹으러 가자!"
        PetPersonalityV2.PLAYFUL -> "밤 산책! 별 보면서 가자~"
        PetPersonalityV2.TIMID -> "어, 어두워요... 같이 가요..."
        PetPersonalityV2.CLUMSY -> "밤이다! 조심- 앗 발 헛디뎠!"
    }

    private fun getNightMessageEn(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "It's night. Walk carefully."
        PetPersonalityV2.TSUNDERE -> "It's dark... You okay? ...Not worried."
        PetPersonalityV2.FOODIE -> "It's night~ Let's get a midnight snack!"
        PetPersonalityV2.PLAYFUL -> "Night walk! Let's watch the stars~"
        PetPersonalityV2.TIMID -> "I-it's dark... Let's go together..."
        PetPersonalityV2.CLUMSY -> "It's night! Care- oops misstep!"
    }

    private fun getNightMessageJa(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "夜だ。気をつけて歩いて。"
        PetPersonalityV2.TSUNDERE -> "暗いけど...大丈夫？...心配してないから。"
        PetPersonalityV2.FOODIE -> "夜だね~夜食食べに行こう！"
        PetPersonalityV2.PLAYFUL -> "夜の散歩！星見ながら行こう~"
        PetPersonalityV2.TIMID -> "く、暗い...一緒に行きましょう..."
        PetPersonalityV2.CLUMSY -> "夜だ！気をつけ-あっ踏み外した！"
    }

    private fun getNightMessageZh(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "是夜晚。小心走路。"
        PetPersonalityV2.TSUNDERE -> "好黑...你没事吧？...不是担心。"
        PetPersonalityV2.FOODIE -> "是夜晚~去吃夜宵吧！"
        PetPersonalityV2.PLAYFUL -> "夜间散步！看着星星走吧~"
        PetPersonalityV2.TIMID -> "好、好黑...一起走吧..."
        PetPersonalityV2.CLUMSY -> "是夜晚！小心-哎呀踩空了！"
    }

    private fun getNightMessageEs(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "Es de noche. Camina con cuidado."
        PetPersonalityV2.TSUNDERE -> "Está oscuro... ¿Estás bien? ...No me preocupo."
        PetPersonalityV2.FOODIE -> "Es de noche~ ¡Vamos por un snack!"
        PetPersonalityV2.PLAYFUL -> "¡Paseo nocturno! Miremos las estrellas~"
        PetPersonalityV2.TIMID -> "E-está oscuro... Vamos juntos..."
        PetPersonalityV2.CLUMSY -> "¡Es de noche! Cuida- ¡ups tropecé!"
    }

    // ===== 응원 메시지 =====
    fun getEncouragementMessage(personality: PetPersonalityV2): String {
        val messages = when (getLang()) {
            "ko" -> getEncouragementMessagesKo(personality)
            "ja" -> getEncouragementMessagesJa(personality)
            "zh" -> getEncouragementMessagesZh(personality)
            "es" -> getEncouragementMessagesEs(personality)
            else -> getEncouragementMessagesEn(personality)
        }
        return messages.random()
    }

    private fun getEncouragementMessagesKo(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("할 수 있어.", "포기하지 마.", "같이 가자.", "믿고 있어.", "힘내.")
        PetPersonalityV2.TSUNDERE -> listOf("힘내라고... 응원 아니야.", "포기하면 안 돼. ...걱정이라서 그래.", "좀 더 해봐. 믿으니까.", "지지 않길 바라. ...진심이야.", "화이팅... 이라고 해둘게.")
        PetPersonalityV2.FOODIE -> listOf("힘내! 끝나면 맛있는 거!", "화이팅! 간식이 기다려!", "할 수 있어! 밥 먹으러 가자!", "조금만 더! 배고프지?", "파이팅! 꿀꿀!")
        PetPersonalityV2.PLAYFUL -> listOf("힘내힘내! 파이팅!", "할 수 있어! 재밌잖아!", "고고! 신나게 가자!", "우리 최고야! 가보자고!", "화이팅!! 우와아!")
        PetPersonalityV2.TIMID -> listOf("힘내세요... 응원할게요...", "할 수 있어요... 저도 믿어요...", "화이팅이에요... 같이 가요...", "조금만 더요... 응원해요...", "파이팅... 할 수 있어요...")
        PetPersonalityV2.CLUMSY -> listOf("힘내! 나도 넘어지면서 응원해!", "파이팅! 앗 미끄러- 괜찮아!", "할 수 있어! 같이! 뒤뚱뒤뚱!", "화이팅! 조심히! 나도 조심!", "가보자고! 앗 돌부리- 괜찮아괜찮아!")
    }

    private fun getEncouragementMessagesEn(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("You can do it.", "Don't give up.", "Let's go together.", "I believe in you.", "Hang in there.")
        PetPersonalityV2.TSUNDERE -> listOf("Cheer up... Not cheering you.", "Don't give up. ...I'm worried.", "Try more. I trust you.", "Don't lose. ...Seriously.", "Fighting... or whatever.")
        PetPersonalityV2.FOODIE -> listOf("Go! Then yummy food!", "Fighting! Snacks await!", "You can! Let's eat!", "A little more! Hungry?", "Fighting! Oink!")
        PetPersonalityV2.PLAYFUL -> listOf("Go go! Fighting!", "You can! It's fun!", "Go go! Let's have fun!", "We're the best! Let's go!", "Fighting!! Woohoo!")
        PetPersonalityV2.TIMID -> listOf("Hang in... I'll cheer...", "You can... I believe...", "Fighting... together...", "A bit more... cheering...", "Fighting... you can...")
        PetPersonalityV2.CLUMSY -> listOf("Go! I cheer while falling!", "Fighting! Oops slip- okay!", "You can! Together! Wobble!", "Fighting! Careful! Me too!", "Let's go! Oops rock- okay okay!")
    }

    private fun getEncouragementMessagesJa(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("できる。", "諦めないで。", "一緒に行こう。", "信じてる。", "頑張れ。")
        PetPersonalityV2.TSUNDERE -> listOf("頑張れって...応援じゃない。", "諦めないで。...心配だから。", "もう少しやって。信じてるから。", "負けないで。...本気だよ。", "ファイト...って言っとく。")
        PetPersonalityV2.FOODIE -> listOf("頑張れ！終わったら美味しいもの！", "ファイト！おやつが待ってる！", "できる！ご飯食べに行こう！", "もう少し！お腹空いた？", "ファイト！ブヒ！")
        PetPersonalityV2.PLAYFUL -> listOf("頑張れ頑張れ！ファイト！", "できる！楽しいでしょ！", "ゴーゴー！楽しく行こう！", "僕たち最高！行こう！", "ファイト!!うわあ!")
        PetPersonalityV2.TIMID -> listOf("頑張って...応援します...", "できます...私も信じます...", "ファイトです...一緒に...", "もう少し...応援します...", "ファイト...できます...")
        PetPersonalityV2.CLUMSY -> listOf("頑張れ！転びながら応援！", "ファイト！あっ滑り-大丈夫！", "できる！一緒！よちよち！", "ファイト！気をつけて！僕も！", "行こう！あっ石-大丈夫大丈夫！")
    }

    private fun getEncouragementMessagesZh(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("你可以的。", "别放弃。", "一起走吧。", "我相信你。", "加油。")
        PetPersonalityV2.TSUNDERE -> listOf("加油...不是在鼓励你。", "别放弃。...因为担心。", "再努力点。相信你。", "别输。...是认真的。", "加油...算了。")
        PetPersonalityV2.FOODIE -> listOf("加油！完成后吃好吃的！", "加油！零食在等着！", "你可以！去吃饭！", "再一点！饿了吧？", "加油！哼哼！")
        PetPersonalityV2.PLAYFUL -> listOf("加油加油！", "你可以！很好玩啊！", "冲冲！开心地走！", "我们最棒！走吧！", "加油!!哇!")
        PetPersonalityV2.TIMID -> listOf("加油...我会支持你...", "你可以...我也相信...", "加油...一起...", "再一点...支持你...", "加油...你可以的...")
        PetPersonalityV2.CLUMSY -> listOf("加油！我摔着也支持！", "加油！哎呀滑-没事！", "你可以！一起！摇晃！", "加油！小心！我也！", "走吧！哎呀石头-没事没事！")
    }

    private fun getEncouragementMessagesEs(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("Puedes hacerlo.", "No te rindas.", "Vamos juntos.", "Creo en ti.", "Ánimo.")
        PetPersonalityV2.TSUNDERE -> listOf("Ánimo... No te animo.", "No te rindas. ...Me preocupo.", "Intenta más. Confío.", "No pierdas. ...En serio.", "Fighting... supongo.")
        PetPersonalityV2.FOODIE -> listOf("¡Vamos! ¡Luego comida!", "¡Ánimo! ¡Snacks esperan!", "¡Puedes! ¡A comer!", "¡Un poco más! ¿Hambre?", "¡Ánimo! ¡Oink!")
        PetPersonalityV2.PLAYFUL -> listOf("¡Ánimo ánimo!", "¡Puedes! ¡Es divertido!", "¡Vamos! ¡A divertirnos!", "¡Somos los mejores! ¡Vamos!", "¡¡Ánimo!! ¡¡Woohoo!!")
        PetPersonalityV2.TIMID -> listOf("Ánimo... Te apoyo...", "Puedes... Yo creo...", "Ánimo... juntos...", "Un poco más... te apoyo...", "Ánimo... puedes...")
        PetPersonalityV2.CLUMSY -> listOf("¡Vamos! ¡Animo cayendo!", "¡Ánimo! Ups resbal- ¡bien!", "¡Puedes! ¡Juntos! ¡Tambaleo!", "¡Ánimo! ¡Cuidado! ¡Yo también!", "¡Vamos! Ups piedra- ¡bien bien!")
    }

    // ===== 오랜만에 접속 (daysSinceLastVisit일 만에) =====
    fun getLongTimeNoSeeMessage(personality: PetPersonalityV2, daysSinceLastVisit: Int): String {
        return when (getLang()) {
            "ko" -> getLongTimeNoSeeMessageKo(personality, daysSinceLastVisit)
            "ja" -> getLongTimeNoSeeMessageJa(personality, daysSinceLastVisit)
            "zh" -> getLongTimeNoSeeMessageZh(personality, daysSinceLastVisit)
            "es" -> getLongTimeNoSeeMessageEs(personality, daysSinceLastVisit)
            else -> getLongTimeNoSeeMessageEn(personality, daysSinceLastVisit)
        }
    }

    private fun getLongTimeNoSeeMessageKo(p: PetPersonalityV2, d: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { d >= 7 -> "일주일이나... 걱정했어."; d >= 3 -> "어디 갔었어? 기다렸는데."; else -> "왔구나. 보고 싶었어." }
        PetPersonalityV2.TSUNDERE -> when { d >= 7 -> "흥, 일주일? ...많이 걱정했거든!"; d >= 3 -> "뭐야, 이제 와? ...기다린 거 아니야."; else -> "왔어? ...뭐, 상관없지만." }
        PetPersonalityV2.FOODIE -> when { d >= 7 -> "일주일?! 밥은 먹었어?! 나도 배고파!"; d >= 3 -> "어디서 맛있는 거 먹고 왔어?!"; else -> "왔다! 같이 밥 먹자!" }
        PetPersonalityV2.PLAYFUL -> when { d >= 7 -> "일주일?! 완전 심심했어!! 놀자!!"; d >= 3 -> "어디 갔었어?! 나 혼자 뭐하라고!"; else -> "왔다!! 빨리 놀자!!" }
        PetPersonalityV2.TIMID -> when { d >= 7 -> "일주일이요...? 저, 정말 외로웠어요..."; d >= 3 -> "보고 싶었어요... 많이요..."; else -> "다, 다행이에요... 왔네요..." }
        PetPersonalityV2.CLUMSY -> when { d >= 7 -> "일주일?! 기다리다 넘어졌어 여러 번!"; d >= 3 -> "어디 갔었어! 찾으러 가다가 앗-"; else -> "왔다! 반가- 앗 미끄러!" }
    }

    private fun getLongTimeNoSeeMessageEn(p: PetPersonalityV2, d: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { d >= 7 -> "A week... I was worried."; d >= 3 -> "Where were you? I waited."; else -> "You're here. Missed you." }
        PetPersonalityV2.TSUNDERE -> when { d >= 7 -> "Hmph, a week? ...I was very worried!"; d >= 3 -> "What, just now? ...Not waiting."; else -> "You came? ...Whatever." }
        PetPersonalityV2.FOODIE -> when { d >= 7 -> "A week?! Did you eat?! I'm hungry!"; d >= 3 -> "What tasty food did you eat?!"; else -> "You're here! Let's eat!" }
        PetPersonalityV2.PLAYFUL -> when { d >= 7 -> "A week?! So bored!! Let's play!!"; d >= 3 -> "Where were you?! What about me!"; else -> "You're here!! Let's play!!" }
        PetPersonalityV2.TIMID -> when { d >= 7 -> "A week...? I-I was so lonely..."; d >= 3 -> "Missed you... a lot..."; else -> "G-glad... you're here..." }
        PetPersonalityV2.CLUMSY -> when { d >= 7 -> "A week?! Fell many times waiting!"; d >= 3 -> "Where were you! Looking for you- oops"; else -> "You're here! Hi- oops slipped!" }
    }

    private fun getLongTimeNoSeeMessageJa(p: PetPersonalityV2, d: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { d >= 7 -> "一週間も...心配したよ。"; d >= 3 -> "どこ行ってた？待ってたのに。"; else -> "来たね。会いたかった。" }
        PetPersonalityV2.TSUNDERE -> when { d >= 7 -> "ふん、一週間？...すごく心配したから！"; d >= 3 -> "何、今頃？...待ってないし。"; else -> "来たの？...まあ、どうでも。" }
        PetPersonalityV2.FOODIE -> when { d >= 7 -> "一週間?!ご飯食べた?!お腹空いた！"; d >= 3 -> "どこで美味しいもの食べてきた?!"; else -> "来た！一緒にご飯！" }
        PetPersonalityV2.PLAYFUL -> when { d >= 7 -> "一週間?!超暇だった!!遊ぼう!!"; d >= 3 -> "どこ行ってた?!僕一人で何しろと！"; else -> "来た!!早く遊ぼう!!" }
        PetPersonalityV2.TIMID -> when { d >= 7 -> "一週間...?わ、私すごく寂しかった..."; d >= 3 -> "会いたかった...たくさん..."; else -> "よ、よかった...来たね..." }
        PetPersonalityV2.CLUMSY -> when { d >= 7 -> "一週間?!待ってて何回も転んだ！"; d >= 3 -> "どこ行ってた！探しに行こうとしてあっ-"; else -> "来た！嬉し-あっ滑った！" }
    }

    private fun getLongTimeNoSeeMessageZh(p: PetPersonalityV2, d: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { d >= 7 -> "一周了...我担心了。"; d >= 3 -> "去哪了？我等着呢。"; else -> "你来了。想你了。" }
        PetPersonalityV2.TSUNDERE -> when { d >= 7 -> "哼，一周？...很担心你！"; d >= 3 -> "什么，现在才来？...没在等。"; else -> "你来了？...随便吧。" }
        PetPersonalityV2.FOODIE -> when { d >= 7 -> "一周?!吃饭了吗?!我饿了！"; d >= 3 -> "去哪吃好吃的了?!"; else -> "你来了！一起吃饭！" }
        PetPersonalityV2.PLAYFUL -> when { d >= 7 -> "一周?!好无聊!!玩吧!!"; d >= 3 -> "去哪了?!我一个人怎么办！"; else -> "来了!!快玩吧!!" }
        PetPersonalityV2.TIMID -> when { d >= 7 -> "一周...?我、我好寂寞..."; d >= 3 -> "想你了...很想..."; else -> "太、太好了...你来了..." }
        PetPersonalityV2.CLUMSY -> when { d >= 7 -> "一周?!等你的时候摔了好几次！"; d >= 3 -> "去哪了！找你的时候哎呀-"; else -> "你来了！开心-哎呀滑倒！" }
    }

    private fun getLongTimeNoSeeMessageEs(p: PetPersonalityV2, d: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { d >= 7 -> "Una semana... Estaba preocupado."; d >= 3 -> "¿Dónde estabas? Te esperé."; else -> "Llegaste. Te extrañé." }
        PetPersonalityV2.TSUNDERE -> when { d >= 7 -> "Hmph, ¿una semana? ...¡Muy preocupado!"; d >= 3 -> "¿Qué, ahora? ...No esperaba."; else -> "¿Viniste? ...Como sea." }
        PetPersonalityV2.FOODIE -> when { d >= 7 -> "¿Una semana?! ¿Comiste?! ¡Tengo hambre!"; d >= 3 -> "¿Qué comida rica comiste?!"; else -> "¡Llegaste! ¡A comer!" }
        PetPersonalityV2.PLAYFUL -> when { d >= 7 -> "¿Una semana?! ¡¡Aburrido!! ¡¡Juguemos!!"; d >= 3 -> "¿Dónde estabas?! ¡¿Y yo qué?!"; else -> "¡¡Llegaste!! ¡¡Juguemos!!" }
        PetPersonalityV2.TIMID -> when { d >= 7 -> "¿Una semana...? E-estaba muy solo..."; d >= 3 -> "Te extrañé... mucho..."; else -> "Q-qué bueno... llegaste..." }
        PetPersonalityV2.CLUMSY -> when { d >= 7 -> "¿Una semana?! ¡Me caí mucho esperando!"; d >= 3 -> "¡¿Dónde estabas?! Buscándote- ups"; else -> "¡Llegaste! Hola- ¡ups resbalé!" }
    }

    // ===== 스트릭 축하 (연속 N일) =====
    fun getStreakCelebrationMessage(personality: PetPersonalityV2, streakDays: Int): String {
        return when (getLang()) {
            "ko" -> getStreakCelebrationMessageKo(personality, streakDays)
            "ja" -> getStreakCelebrationMessageJa(personality, streakDays)
            "zh" -> getStreakCelebrationMessageZh(personality, streakDays)
            "es" -> getStreakCelebrationMessageEs(personality, streakDays)
            else -> getStreakCelebrationMessageEn(personality, streakDays)
        }
    }

    private fun getStreakCelebrationMessageKo(p: PetPersonalityV2, d: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { d >= 30 -> "한 달 연속. 대단하다. 진심으로."; d >= 14 -> "2주 연속이다. 자랑스럽다."; d >= 7 -> "일주일 연속. 잘하고 있어."; else -> "${d}일 연속. 계속 가자." }
        PetPersonalityV2.TSUNDERE -> when { d >= 30 -> "한 달이라니... 좀 감동이야. 아니, 그게 아니라!"; d >= 14 -> "2주? 뭐, 대단하긴 해. ...진짜로."; d >= 7 -> "일주일... 인정해줄게."; else -> "${d}일 연속이네. ...나쁘지 않아." }
        PetPersonalityV2.FOODIE -> when { d >= 30 -> "한 달?! 대박! 파티다 파티! 맛있는 거!!"; d >= 14 -> "2주! 축하 간식 먹자!!"; d >= 7 -> "일주일! 밥 한 끼 더 먹어도 돼?!"; else -> "${d}일! 간식 시간이다 꿀꿀!" }
        PetPersonalityV2.PLAYFUL -> when { d >= 30 -> "한 달?! 미쳤다!! 레전드!!!"; d >= 14 -> "2주다!! 완전 대박!!"; d >= 7 -> "일주일! 우와아! 최고야!"; else -> "${d}일! 신난다!!" }
        PetPersonalityV2.TIMID -> when { d >= 30 -> "한 달이에요...! 저, 정말 감동이에요...!"; d >= 14 -> "2주라니... 대단해요..."; d >= 7 -> "일주일... 정말 잘하고 계세요..."; else -> "${d}일째에요... 응원할게요..." }
        PetPersonalityV2.CLUMSY -> when { d >= 30 -> "한 달?! 축하 춤! 뒤뚱- 앗 넘어질뻔!"; d >= 14 -> "2주! 대단해! 앗 발 걸려- 괜찮아!"; d >= 7 -> "일주일! 최고! 점프- 앗 착지 실패!"; else -> "${d}일! 축하해! 뒤뚱뒤뚱~" }
    }

    private fun getStreakCelebrationMessageEn(p: PetPersonalityV2, d: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { d >= 30 -> "A month straight. Amazing. Truly."; d >= 14 -> "2 weeks straight. Proud of you."; d >= 7 -> "A week straight. Doing great."; else -> "$d days straight. Keep going." }
        PetPersonalityV2.TSUNDERE -> when { d >= 30 -> "A month... Kinda touched. No, that's not it!"; d >= 14 -> "2 weeks? Well, impressive. ...Really."; d >= 7 -> "A week... I acknowledge it."; else -> "$d days straight. ...Not bad." }
        PetPersonalityV2.FOODIE -> when { d >= 30 -> "A month?! Wow! Party! Yummy food!!"; d >= 14 -> "2 weeks! Celebration snack!!"; d >= 7 -> "A week! Can I eat more?!"; else -> "$d days! Snack time oink!" }
        PetPersonalityV2.PLAYFUL -> when { d >= 30 -> "A month?! Crazy!! Legend!!!"; d >= 14 -> "2 weeks!! So awesome!!"; d >= 7 -> "A week! Woohoo! The best!"; else -> "$d days! Exciting!!" }
        PetPersonalityV2.TIMID -> when { d >= 30 -> "A month...! I-I'm so touched...!"; d >= 14 -> "2 weeks... Amazing..."; d >= 7 -> "A week... You're doing great..."; else -> "$d days... I'll cheer..." }
        PetPersonalityV2.CLUMSY -> when { d >= 30 -> "A month?! Dance! Wobble- oops almost fell!"; d >= 14 -> "2 weeks! Great! Oops tripped- okay!"; d >= 7 -> "A week! Best! Jump- oops bad landing!"; else -> "$d days! Congrats! Wobble~" }
    }

    private fun getStreakCelebrationMessageJa(p: PetPersonalityV2, d: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { d >= 30 -> "一ヶ月連続。すごい。本気で。"; d >= 14 -> "2週間連続だ。誇りに思う。"; d >= 7 -> "一週間連続。頑張ってるね。"; else -> "${d}日連続。続けよう。" }
        PetPersonalityV2.TSUNDERE -> when { d >= 30 -> "一ヶ月なんて...ちょっと感動。いや、違うから！"; d >= 14 -> "2週間？まあ、すごいけど。...本当に。"; d >= 7 -> "一週間...認めてあげる。"; else -> "${d}日連続だね。...悪くない。" }
        PetPersonalityV2.FOODIE -> when { d >= 30 -> "一ヶ月?!すごい！パーティー！美味しいもの!!"; d >= 14 -> "2週間！お祝いおやつ!!"; d >= 7 -> "一週間！もっと食べていい?!"; else -> "${d}日！おやつタイムブヒ！" }
        PetPersonalityV2.PLAYFUL -> when { d >= 30 -> "一ヶ月?!ヤバい!!レジェンド!!!"; d >= 14 -> "2週間!!すごすぎ!!"; d >= 7 -> "一週間！うわあ！最高！"; else -> "${d}日！わくわく!!" }
        PetPersonalityV2.TIMID -> when { d >= 30 -> "一ヶ月...!わ、私感動です...!"; d >= 14 -> "2週間なんて...すごい..."; d >= 7 -> "一週間...本当に頑張ってます..."; else -> "${d}日目です...応援します..." }
        PetPersonalityV2.CLUMSY -> when { d >= 30 -> "一ヶ月?!お祝いダンス！よちよち-あっ転びそう！"; d >= 14 -> "2週間！すごい！あっ足が-大丈夫！"; d >= 7 -> "一週間！最高！ジャンプ-あっ着地失敗！"; else -> "${d}日！おめでとう！よちよち~" }
    }

    private fun getStreakCelebrationMessageZh(p: PetPersonalityV2, d: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { d >= 30 -> "一个月连续。了不起。真的。"; d >= 14 -> "连续2周。为你骄傲。"; d >= 7 -> "连续一周。做得很好。"; else -> "连续${d}天。继续加油。" }
        PetPersonalityV2.TSUNDERE -> when { d >= 30 -> "一个月...有点感动。不，不是那样！"; d >= 14 -> "2周？嗯，挺厉害的。...真的。"; d >= 7 -> "一周...承认你。"; else -> "连续${d}天。...还不错。" }
        PetPersonalityV2.FOODIE -> when { d >= 30 -> "一个月?!太棒了！派对！好吃的!!"; d >= 14 -> "2周！庆祝零食!!"; d >= 7 -> "一周！可以多吃点吗?!"; else -> "${d}天！零食时间哼哼！" }
        PetPersonalityV2.PLAYFUL -> when { d >= 30 -> "一个月?!疯了!!传说!!!"; d >= 14 -> "2周!!太棒了!!"; d >= 7 -> "一周！哇！最棒！"; else -> "${d}天！好兴奋!!" }
        PetPersonalityV2.TIMID -> when { d >= 30 -> "一个月...!我、我好感动...!"; d >= 14 -> "2周...好厉害..."; d >= 7 -> "一周...你做得很好..."; else -> "${d}天了...我会支持你..." }
        PetPersonalityV2.CLUMSY -> when { d >= 30 -> "一个月?!庆祝舞！摇晃-哎呀差点摔！"; d >= 14 -> "2周！厉害！哎呀绊到-没事！"; d >= 7 -> "一周！最棒！跳-哎呀落地失败！"; else -> "${d}天！恭喜！摇摇晃晃~" }
    }

    private fun getStreakCelebrationMessageEs(p: PetPersonalityV2, d: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { d >= 30 -> "Un mes seguido. Increíble. De verdad."; d >= 14 -> "2 semanas seguidas. Orgulloso."; d >= 7 -> "Una semana seguida. Vas muy bien."; else -> "$d días seguidos. Sigamos." }
        PetPersonalityV2.TSUNDERE -> when { d >= 30 -> "Un mes... Me conmueve. ¡No, eso no!"; d >= 14 -> "¿2 semanas? Bueno, impresionante. ...De verdad."; d >= 7 -> "Una semana... Lo reconozco."; else -> "$d días seguidos. ...Nada mal." }
        PetPersonalityV2.FOODIE -> when { d >= 30 -> "¿Un mes?! ¡Wow! ¡Fiesta! ¡¡Comida!!"; d >= 14 -> "¡2 semanas! ¡¡Snack de celebración!!"; d >= 7 -> "¡Una semana! ¿Puedo comer más?!"; else -> "¡$d días! ¡Hora de snack oink!" }
        PetPersonalityV2.PLAYFUL -> when { d >= 30 -> "¿Un mes?! ¡¡Locura!! ¡¡¡Leyenda!!!"; d >= 14 -> "¡¡2 semanas!! ¡¡Increíble!!"; d >= 7 -> "¡Una semana! ¡Woohoo! ¡Lo mejor!"; else -> "¡$d días! ¡¡Emocionante!!" }
        PetPersonalityV2.TIMID -> when { d >= 30 -> "Un mes...! E-estoy conmovido...!"; d >= 14 -> "2 semanas... Increíble..."; d >= 7 -> "Una semana... Lo haces muy bien..."; else -> "$d días... Te apoyo..." }
        PetPersonalityV2.CLUMSY -> when { d >= 30 -> "¿Un mes?! ¡Baile! Tambaleo- ¡ups casi caigo!"; d >= 14 -> "¡2 semanas! ¡Genial! Ups tropecé- ¡bien!"; d >= 7 -> "¡Una semana! ¡Lo mejor! Salto- ¡ups mal aterrizaje!"; else -> "¡$d días! ¡Felicidades! Tambaleo~" }
    }

    // ===== 스트릭 끊김 (아쉬움) =====
    fun getStreakBrokenMessage(personality: PetPersonalityV2, previousStreak: Int): String {
        return when (getLang()) {
            "ko" -> getStreakBrokenMessageKo(personality, previousStreak)
            "ja" -> getStreakBrokenMessageJa(personality, previousStreak)
            "zh" -> getStreakBrokenMessageZh(personality, previousStreak)
            "es" -> getStreakBrokenMessageEs(personality, previousStreak)
            else -> getStreakBrokenMessageEn(personality, previousStreak)
        }
    }

    private fun getStreakBrokenMessageKo(p: PetPersonalityV2, d: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { d >= 7 -> "${d}일이었는데... 괜찮아. 다시 하면 돼."; else -> "연속 기록이 끊겼어. 다시 시작하자." }
        PetPersonalityV2.TSUNDERE -> when { d >= 7 -> "${d}일이었는데... 아쉬워. 아, 아니 그냥!"; else -> "끊겼네... 뭐, 다시 하면 되지." }
        PetPersonalityV2.FOODIE -> when { d >= 7 -> "${d}일이었는데 ㅠㅠ 힘내! 밥 먹고 다시!"; else -> "괜찮아! 먹고 힘내자!" }
        PetPersonalityV2.PLAYFUL -> when { d >= 7 -> "${d}일이었는데... 에이! 다시 하면 돼!"; else -> "괜찮아괜찮아! 리트라이!!" }
        PetPersonalityV2.TIMID -> when { d >= 7 -> "${d}일이었는데... 괜, 괜찮아요..."; else -> "저, 저도 응원할게요... 다시 해봐요..." }
        PetPersonalityV2.CLUMSY -> when { d >= 7 -> "${d}일이었는데... 나도 자주 넘어져! 일어나면 돼!"; else -> "괜찮아! 나도 맨날 넘어지는걸! 파이팅!" }
    }

    private fun getStreakBrokenMessageEn(p: PetPersonalityV2, d: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { d >= 7 -> "It was $d days... It's okay. Try again."; else -> "Streak broke. Let's start again." }
        PetPersonalityV2.TSUNDERE -> when { d >= 7 -> "It was $d days... Too bad. N-no, whatever!"; else -> "It broke... Well, try again." }
        PetPersonalityV2.FOODIE -> when { d >= 7 -> "$d days... :( Cheer up! Eat and retry!"; else -> "It's okay! Eat and cheer up!" }
        PetPersonalityV2.PLAYFUL -> when { d >= 7 -> "It was $d days... Ah! Try again!"; else -> "It's okay! Retry!!" }
        PetPersonalityV2.TIMID -> when { d >= 7 -> "$d days... I-it's okay..."; else -> "I-I'll cheer you... Try again..." }
        PetPersonalityV2.CLUMSY -> when { d >= 7 -> "$d days... I fall a lot too! Get up!"; else -> "It's okay! I fall daily! Fighting!" }
    }

    private fun getStreakBrokenMessageJa(p: PetPersonalityV2, d: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { d >= 7 -> "${d}日だったのに...大丈夫。またやればいい。"; else -> "連続記録が途切れた。また始めよう。" }
        PetPersonalityV2.TSUNDERE -> when { d >= 7 -> "${d}日だったのに...残念。い、いやなんでも！"; else -> "途切れたね...まあ、またやればいいし。" }
        PetPersonalityV2.FOODIE -> when { d >= 7 -> "${d}日だったのに...頑張れ！食べてまた！"; else -> "大丈夫！食べて元気出そう！" }
        PetPersonalityV2.PLAYFUL -> when { d >= 7 -> "${d}日だったのに...えい！またやればいい！"; else -> "大丈夫大丈夫！リトライ!!" }
        PetPersonalityV2.TIMID -> when { d >= 7 -> "${d}日だったのに...だ、大丈夫です..."; else -> "わ、私も応援します...またやってみて..." }
        PetPersonalityV2.CLUMSY -> when { d >= 7 -> "${d}日だったのに...僕もよく転ぶ！起きればいい！"; else -> "大丈夫！僕も毎日転ぶよ！ファイト！" }
    }

    private fun getStreakBrokenMessageZh(p: PetPersonalityV2, d: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { d >= 7 -> "原来${d}天...没关系。再试一次。"; else -> "连续记录中断了。重新开始吧。" }
        PetPersonalityV2.TSUNDERE -> when { d >= 7 -> "原来${d}天...可惜。不、不是那个意思！"; else -> "中断了...嗯，再试一次吧。" }
        PetPersonalityV2.FOODIE -> when { d >= 7 -> "${d}天...加油！吃饭然后再来！"; else -> "没关系！吃点东西加油！" }
        PetPersonalityV2.PLAYFUL -> when { d >= 7 -> "原来${d}天...哎！再试一次！"; else -> "没关系没关系！重来!!" }
        PetPersonalityV2.TIMID -> when { d >= 7 -> "${d}天...没、没关系..."; else -> "我、我会支持你...再试试..." }
        PetPersonalityV2.CLUMSY -> when { d >= 7 -> "原来${d}天...我也经常摔倒！站起来就好！"; else -> "没关系！我天天摔倒！加油！" }
    }

    private fun getStreakBrokenMessageEs(p: PetPersonalityV2, d: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { d >= 7 -> "Eran $d días... Está bien. Intenta de nuevo."; else -> "Se rompió la racha. Empecemos de nuevo." }
        PetPersonalityV2.TSUNDERE -> when { d >= 7 -> "Eran $d días... Qué pena. ¡N-no, nada!"; else -> "Se rompió... Bueno, intenta otra vez." }
        PetPersonalityV2.FOODIE -> when { d >= 7 -> "$d días... :( ¡Ánimo! ¡Come y reintenta!"; else -> "¡Está bien! ¡Come y ánimo!" }
        PetPersonalityV2.PLAYFUL -> when { d >= 7 -> "Eran $d días... ¡Ah! ¡Otra vez!"; else -> "¡Está bien! ¡¡Reintento!!" }
        PetPersonalityV2.TIMID -> when { d >= 7 -> "$d días... E-está bien..."; else -> "T-te apoyo... Intenta de nuevo..." }
        PetPersonalityV2.CLUMSY -> when { d >= 7 -> "$d días... ¡Yo también caigo mucho! ¡Levántate!"; else -> "¡Está bien! ¡Yo caigo a diario! ¡Ánimo!" }
    }

    // ===== 시간대별 인사 =====
    fun getTimeOfDayGreeting(personality: PetPersonalityV2, hour: Int): String {
        val timeOfDay = when {
            hour in 5..8 -> "early_morning"
            hour in 9..11 -> "morning"
            hour in 12..13 -> "lunch"
            hour in 14..17 -> "afternoon"
            hour in 18..21 -> "evening"
            else -> "night"
        }
        return when (getLang()) {
            "ko" -> getTimeOfDayGreetingKo(personality, timeOfDay)
            "ja" -> getTimeOfDayGreetingJa(personality, timeOfDay)
            "zh" -> getTimeOfDayGreetingZh(personality, timeOfDay)
            "es" -> getTimeOfDayGreetingEs(personality, timeOfDay)
            else -> getTimeOfDayGreetingEn(personality, timeOfDay)
        }
    }

    private fun getTimeOfDayGreetingKo(p: PetPersonalityV2, t: String): String = when (p) {
        PetPersonalityV2.LOYAL -> when (t) { "early_morning" -> "일찍 일어났네. 좋은 아침."; "morning" -> "좋은 아침이다."; "lunch" -> "점심 먹었어?"; "afternoon" -> "오후도 화이팅."; "evening" -> "저녁이다. 오늘 수고했어."; else -> "밤이네. 푹 쉬어." }
        PetPersonalityV2.TSUNDERE -> when (t) { "early_morning" -> "벌써 일어났어? ...부지런하네."; "morning" -> "아침이야... 뭐, 좋은 아침."; "lunch" -> "밥은 먹었어? ...그냥 물어본 거야."; "afternoon" -> "나른하네... 졸린 거 아니야."; "evening" -> "저녁이다... 오늘도 수고. ...진심이야."; else -> "밤이네... 잘 자. 내일 봐." }
        PetPersonalityV2.FOODIE -> when (t) { "early_morning" -> "아침밥 시간!! 일어났어?!"; "morning" -> "좋은 아침! 아침밥 뭐야?!"; "lunch" -> "점심이다!! 뭐 먹지?!"; "afternoon" -> "간식 타임이다~ 꿀꿀!"; "evening" -> "저녁!! 맛있는 거 먹자!!"; else -> "야식 먹을까? ...아 살찐다!" }
        PetPersonalityV2.PLAYFUL -> when (t) { "early_morning" -> "우와 일찍이다! 놀 시간 많다!"; "morning" -> "좋은 아침!! 오늘 뭐 하지?!"; "lunch" -> "점심이다! 먹고 놀자!"; "afternoon" -> "오후다! 심심해! 뭐 하지?!"; "evening" -> "저녁이다~ 불금?! 아 평일이네"; else -> "밤이다! 밤새 놀자! ...농담!" }
        PetPersonalityV2.TIMID -> when (t) { "early_morning" -> "아, 안녕하세요... 일찍 일어나셨네요..."; "morning" -> "좋, 좋은 아침이에요..."; "lunch" -> "점심... 드셨나요...?"; "afternoon" -> "오후예요... 힘내세요..."; "evening" -> "저녁이에요... 오늘도 수고하셨어요..."; else -> "밤이에요... 푹 쉬세요..." }
        PetPersonalityV2.CLUMSY -> when (t) { "early_morning" -> "좋은 아침! 앗 아직 졸려서 비틀-"; "morning" -> "아침이다! 스트레칭- 앗 뻐근!"; "lunch" -> "점심!! 밥 먹다가 흘리면 안 돼!"; "afternoon" -> "오후다! 산책- 앗 문턱!"; "evening" -> "저녁이다! 앗 어두워서 안 보- 쿵!"; else -> "밤이다! 잘 자- 앗 이불에 걸려!" }
    }

    private fun getTimeOfDayGreetingEn(p: PetPersonalityV2, t: String): String = when (p) {
        PetPersonalityV2.LOYAL -> when (t) { "early_morning" -> "Up early. Good morning."; "morning" -> "Good morning."; "lunch" -> "Had lunch?"; "afternoon" -> "Keep going."; "evening" -> "Evening. Good work today."; else -> "It's night. Rest well." }
        PetPersonalityV2.TSUNDERE -> when (t) { "early_morning" -> "Already up? ...Hard worker."; "morning" -> "Morning... Well, good morning."; "lunch" -> "Eaten? ...Just asking."; "afternoon" -> "Drowsy... Not sleepy."; "evening" -> "Evening... Good work. ...I mean it."; else -> "Night... Sleep well. See you." }
        PetPersonalityV2.FOODIE -> when (t) { "early_morning" -> "Breakfast time!! You up?!"; "morning" -> "Good morning! What's for breakfast?!"; "lunch" -> "Lunch!! What to eat?!"; "afternoon" -> "Snack time~ oink!"; "evening" -> "Dinner!! Let's eat yummy!!"; else -> "Midnight snack? ...Oh, the calories!" }
        PetPersonalityV2.PLAYFUL -> when (t) { "early_morning" -> "Wow early! More playtime!"; "morning" -> "Good morning!! What's today?!"; "lunch" -> "Lunch! Eat then play!"; "afternoon" -> "Afternoon! Bored! What now?!"; "evening" -> "Evening~ Friday?! Oh weekday"; else -> "Night! All nighter! ...Kidding!" }
        PetPersonalityV2.TIMID -> when (t) { "early_morning" -> "Oh, h-hello... Up early..."; "morning" -> "G-good morning..."; "lunch" -> "Lunch... had any...?"; "afternoon" -> "Afternoon... hang in there..."; "evening" -> "Evening... good work today..."; else -> "Night... rest well..." }
        PetPersonalityV2.CLUMSY -> when (t) { "early_morning" -> "Good morning! Oops still sleepy-"; "morning" -> "Morning! Stretch- oops stiff!"; "lunch" -> "Lunch!! Don't spill!!"; "afternoon" -> "Afternoon! Walk- oops doorstep!"; "evening" -> "Evening! Oops dark can't see- bonk!"; else -> "Night! Sleep- oops blanket!" }
    }

    private fun getTimeOfDayGreetingJa(p: PetPersonalityV2, t: String): String = when (p) {
        PetPersonalityV2.LOYAL -> when (t) { "early_morning" -> "早起きだね。おはよう。"; "morning" -> "おはよう。"; "lunch" -> "お昼食べた？"; "afternoon" -> "午後も頑張って。"; "evening" -> "夕方だ。お疲れ様。"; else -> "夜だね。ゆっくり休んで。" }
        PetPersonalityV2.TSUNDERE -> when (t) { "early_morning" -> "もう起きたの？...勤勉だね。"; "morning" -> "朝だよ...まあ、おはよう。"; "lunch" -> "ご飯食べた？...聞いただけ。"; "afternoon" -> "だるいね...眠くないし。"; "evening" -> "夕方だ...お疲れ。...本気だよ。"; else -> "夜だね...おやすみ。また明日。" }
        PetPersonalityV2.FOODIE -> when (t) { "early_morning" -> "朝ご飯タイム!!起きた?!"; "morning" -> "おはよう！朝ご飯は?!"; "lunch" -> "お昼!!何食べる?!"; "afternoon" -> "おやつタイム~ブヒ！"; "evening" -> "夕飯!!美味しいもの!!"; else -> "夜食食べる？...あ太る！" }
        PetPersonalityV2.PLAYFUL -> when (t) { "early_morning" -> "わあ早い！遊ぶ時間たくさん！"; "morning" -> "おはよう!!今日何する?!"; "lunch" -> "お昼！食べて遊ぼう！"; "afternoon" -> "午後！暇！何しよう?!"; "evening" -> "夕方~花金?!あ平日だ"; else -> "夜！徹夜！...冗談！" }
        PetPersonalityV2.TIMID -> when (t) { "early_morning" -> "あ、おはようございます...早いですね..."; "morning" -> "お、おはようございます..."; "lunch" -> "お昼...食べましたか...?"; "afternoon" -> "午後です...頑張って..."; "evening" -> "夕方です...お疲れ様でした..."; else -> "夜です...ゆっくり休んで..." }
        PetPersonalityV2.CLUMSY -> when (t) { "early_morning" -> "おはよう！あっまだ眠くてふらふら-"; "morning" -> "朝だ！ストレッチ-あっ痛い！"; "lunch" -> "お昼!!こぼさないで!!"; "afternoon" -> "午後！散歩-あっ段差！"; "evening" -> "夕方！あっ暗くて見えな-ゴン！"; else -> "夜！おやす-あっ布団に！" }
    }

    private fun getTimeOfDayGreetingZh(p: PetPersonalityV2, t: String): String = when (p) {
        PetPersonalityV2.LOYAL -> when (t) { "early_morning" -> "起得早。早上好。"; "morning" -> "早上好。"; "lunch" -> "吃午饭了吗？"; "afternoon" -> "下午也加油。"; "evening" -> "傍晚了。辛苦了。"; else -> "晚上了。好好休息。" }
        PetPersonalityV2.TSUNDERE -> when (t) { "early_morning" -> "已经起来了？...真勤快。"; "morning" -> "早上...嗯，早上好。"; "lunch" -> "吃饭了吗？...随便问问。"; "afternoon" -> "好困...不是困。"; "evening" -> "傍晚了...辛苦了。...是真的。"; else -> "晚上了...晚安。明天见。" }
        PetPersonalityV2.FOODIE -> when (t) { "early_morning" -> "早餐时间!!起来了?!"; "morning" -> "早上好！早餐吃什么?!"; "lunch" -> "午餐!!吃什么?!"; "afternoon" -> "零食时间~哼哼！"; "evening" -> "晚餐!!吃好吃的!!"; else -> "吃夜宵？...啊会胖！" }
        PetPersonalityV2.PLAYFUL -> when (t) { "early_morning" -> "哇好早！玩的时间多！"; "morning" -> "早上好!!今天干什么?!"; "lunch" -> "午餐！吃完玩！"; "afternoon" -> "下午！无聊！干什么?!"; "evening" -> "傍晚~周五?!啊是工作日"; else -> "晚上！通宵！...开玩笑！" }
        PetPersonalityV2.TIMID -> when (t) { "early_morning" -> "啊，早、早上好...起得真早..."; "morning" -> "早、早上好..."; "lunch" -> "午餐...吃了吗...?"; "afternoon" -> "下午了...加油..."; "evening" -> "傍晚了...辛苦了..."; else -> "晚上了...好好休息..." }
        PetPersonalityV2.CLUMSY -> when (t) { "early_morning" -> "早上好！哎呀还困着晃-"; "morning" -> "早上！伸展-哎呀僵硬！"; "lunch" -> "午餐!!别洒了!!"; "afternoon" -> "下午！散步-哎呀门槛！"; "evening" -> "傍晚！哎呀暗看不见-砰！"; else -> "晚上！晚-哎呀被子！" }
    }

    private fun getTimeOfDayGreetingEs(p: PetPersonalityV2, t: String): String = when (p) {
        PetPersonalityV2.LOYAL -> when (t) { "early_morning" -> "Madrugaste. Buenos días."; "morning" -> "Buenos días."; "lunch" -> "¿Almorzaste?"; "afternoon" -> "Sigue adelante."; "evening" -> "Atardecer. Buen trabajo hoy."; else -> "Es de noche. Descansa bien." }
        PetPersonalityV2.TSUNDERE -> when (t) { "early_morning" -> "¿Ya levantado? ...Trabajador."; "morning" -> "Mañana... Bueno, buenos días."; "lunch" -> "¿Comiste? ...Solo pregunto."; "afternoon" -> "Soñoliento... No tengo sueño."; "evening" -> "Atardecer... Buen trabajo. ...Lo digo en serio."; else -> "Noche... Duerme bien. Nos vemos." }
        PetPersonalityV2.FOODIE -> when (t) { "early_morning" -> "¡¡Hora del desayuno!! ¿Despierto?!"; "morning" -> "¡Buenos días! ¿Qué hay de desayuno?!"; "lunch" -> "¡¡Almuerzo!! ¿Qué comer?!"; "afternoon" -> "¡Hora de snack~ oink!"; "evening" -> "¡¡Cena!! ¡¡A comer rico!!"; else -> "¿Snack nocturno? ...¡Oh, las calorías!" }
        PetPersonalityV2.PLAYFUL -> when (t) { "early_morning" -> "¡Wow temprano! ¡Más tiempo de juego!"; "morning" -> "¡¡Buenos días!! ¿Qué hacemos?!"; "lunch" -> "¡Almuerzo! ¡Comer y jugar!"; "afternoon" -> "¡Tarde! ¡Aburrido! ¿Qué ahora?!"; "evening" -> "¡Atardecer~ ¿Viernes?! Oh, entre semana"; else -> "¡Noche! ¡Toda la noche! ...¡Broma!" }
        PetPersonalityV2.TIMID -> when (t) { "early_morning" -> "Oh, b-buenos días... Madrugaste..."; "morning" -> "B-buenos días..."; "lunch" -> "Almuerzo... ¿comiste...?"; "afternoon" -> "Tarde... ánimo..."; "evening" -> "Atardecer... buen trabajo..."; else -> "Noche... descansa bien..." }
        PetPersonalityV2.CLUMSY -> when (t) { "early_morning" -> "¡Buenos días! Ups aún dormido-"; "morning" -> "¡Mañana! Estiro- ¡ups rígido!"; "lunch" -> "¡¡Almuerzo!! ¡¡No derrames!!"; "afternoon" -> "¡Tarde! Paseo- ¡ups escalón!"; "evening" -> "¡Atardecer! Ups oscuro no veo- ¡pum!"; else -> "¡Noche! Duer- ¡ups manta!" }
    }

    // ===== 터치/상호작용 반응 =====
    fun getTouchReactionMessage(personality: PetPersonalityV2): String {
        val messages = when (getLang()) {
            "ko" -> getTouchReactionMessagesKo(personality)
            "ja" -> getTouchReactionMessagesJa(personality)
            "zh" -> getTouchReactionMessagesZh(personality)
            "es" -> getTouchReactionMessagesEs(personality)
            else -> getTouchReactionMessagesEn(personality)
        }
        return messages.random()
    }

    private fun getTouchReactionMessagesKo(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("왜? 무슨 일이야?", "쓰다듬는 거야? 좋네.", "그래그래.", "뭐, 싫지 않아.", "...좋아.")
        PetPersonalityV2.TSUNDERE -> listOf("뭐, 뭐야?!", "만지지 마! ...조금만.", "흥, 뭔데? ...기분 나쁘진 않아.", "갑자기 왜 그래! ...더 해도 돼.", "부, 부끄러워!")
        PetPersonalityV2.FOODIE -> listOf("뭐야? 간식?!", "쓰다듬어 줘? 히히~", "좋다~ 꿀꿀!", "배 만지면 안 돼! 간지러워!", "더 해줘~ 기분 좋아!")
        PetPersonalityV2.PLAYFUL -> listOf("뭐야뭐야?! 놀자?!", "우와! 기분 좋아!", "더더더! 신난다!", "간지러워 히히히!", "좋아좋아!! 최고야!")
        PetPersonalityV2.TIMID -> listOf("앗...! 깜짝이야...", "저, 저요...?", "부, 부드럽게요...", "...좋아요...", "감사해요... 헤헤...")
        PetPersonalityV2.CLUMSY -> listOf("앗! 깜짝이- 비틀!", "간지러워! 앗 넘어질-", "좋다! 뒤뚱뒤뚱~", "더 해줘! 앗 균형이-", "히히! 앗 미끄러질뻔!")
    }

    private fun getTouchReactionMessagesEn(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("What? What's up?", "Petting me? Nice.", "Yeah yeah.", "Well, I don't mind.", "...Nice.")
        PetPersonalityV2.TSUNDERE -> listOf("W-what?!", "Don't touch! ...A little.", "Hmph, what? ...Doesn't feel bad.", "Why suddenly! ...More is okay.", "E-embarrassing!")
        PetPersonalityV2.FOODIE -> listOf("What? Snack?!", "Petting? Hehe~", "Nice~ oink!", "Not belly! Ticklish!", "More~ Feels good!")
        PetPersonalityV2.PLAYFUL -> listOf("What what?! Play?!", "Wow! Feels great!", "More more! Exciting!", "Ticklish hehe!", "Love it!! The best!")
        PetPersonalityV2.TIMID -> listOf("Ah...! Startled...", "M-me...?", "G-gently...", "...Nice...", "Thanks... hehe...")
        PetPersonalityV2.CLUMSY -> listOf("Ah! Startled- wobble!", "Ticklish! Oops falling-", "Nice! Wobble~", "More! Oops balance-", "Hehe! Oops almost slipped!")
    }

    private fun getTouchReactionMessagesJa(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("何？どうした？", "撫でてくれるの？いいね。", "うんうん。", "まあ、嫌じゃない。", "...いいね。")
        PetPersonalityV2.TSUNDERE -> listOf("な、何?!", "触らないで！...ちょっとだけ。", "ふん、何？...嫌じゃない。", "急に何！...もっとしていいよ。", "は、恥ずかしい！")
        PetPersonalityV2.FOODIE -> listOf("何？おやつ?!", "撫でてくれる？へへ~", "いいね~ブヒ！", "お腹ダメ！くすぐったい！", "もっと~気持ちいい！")
        PetPersonalityV2.PLAYFUL -> listOf("何何?!遊ぶ?!", "わあ！気持ちいい！", "もっともっと！楽しい！", "くすぐったいへへ！", "好き好き!!最高！")
        PetPersonalityV2.TIMID -> listOf("あっ...!びっくり...", "わ、私...?", "や、優しく...", "...いいです...", "ありがとう...へへ...")
        PetPersonalityV2.CLUMSY -> listOf("あっ！びっくり-ふらっ！", "くすぐったい！あっ転ぶ-", "いいね！よちよち~", "もっと！あっバランス-", "へへ！あっ滑りそう！")
    }

    private fun getTouchReactionMessagesZh(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("怎么了？有事吗？", "在摸我？不错。", "嗯嗯。", "嗯，不讨厌。", "...喜欢。")
        PetPersonalityV2.TSUNDERE -> listOf("什、什么?!", "别摸！...一点点。", "哼，什么？...不难受。", "突然干什么！...再多点可以。", "好、好害羞！")
        PetPersonalityV2.FOODIE -> listOf("什么？零食?!", "摸我？嘿嘿~", "喜欢~哼哼！", "别摸肚子！痒！", "再多点~舒服！")
        PetPersonalityV2.PLAYFUL -> listOf("什么什么?!玩吗?!", "哇！好舒服！", "更多更多！开心！", "好痒嘿嘿！", "喜欢喜欢!!最棒！")
        PetPersonalityV2.TIMID -> listOf("啊...!吓一跳...", "是、是我...?", "轻、轻点...", "...喜欢...", "谢谢...嘿嘿...")
        PetPersonalityV2.CLUMSY -> listOf("啊！吓到-晃！", "好痒！哎呀要摔-", "喜欢！摇晃~", "再多点！哎呀平衡-", "嘿嘿！哎呀差点滑倒！")
    }

    private fun getTouchReactionMessagesEs(p: PetPersonalityV2): List<String> = when (p) {
        PetPersonalityV2.LOYAL -> listOf("¿Qué? ¿Qué pasa?", "¿Me acaricias? Bien.", "Sí sí.", "Bueno, no me molesta.", "...Me gusta.")
        PetPersonalityV2.TSUNDERE -> listOf("¿Q-qué?!", "¡No toques! ...Un poco.", "Hmph, ¿qué? ...No me molesta.", "¡¿Por qué de repente?! ...Más está bien.", "¡Q-qué vergüenza!")
        PetPersonalityV2.FOODIE -> listOf("¿Qué? ¿¡Snack?!", "¿Me acaricias? Jeje~", "¡Bien~ oink!", "¡La panza no! ¡Cosquillas!", "¡Más~ Se siente bien!")
        PetPersonalityV2.PLAYFUL -> listOf("¿¡Qué qué?! ¿¡Jugamos?!", "¡Wow! ¡Se siente genial!", "¡Más más! ¡Emocionante!", "¡Cosquillas jeje!", "¡¡Me encanta!! ¡¡Lo mejor!!")
        PetPersonalityV2.TIMID -> listOf("Ah...! Me asustaste...", "¿Y-yo...?", "S-suave...", "...Me gusta...", "Gracias... jeje...")
        PetPersonalityV2.CLUMSY -> listOf("¡Ah! Susto- ¡tambaleo!", "¡Cosquillas! Ups cayendo-", "¡Bien! Tambaleo~", "¡Más! Ups equilibrio-", "¡Jeje! ¡Ups casi resbalo!")
    }

    // ===== 목표 실패 (오늘 못 채웠을 때) =====
    fun getGoalFailedMessage(personality: PetPersonalityV2, achievedPercent: Int): String {
        return when (getLang()) {
            "ko" -> getGoalFailedMessageKo(personality, achievedPercent)
            "ja" -> getGoalFailedMessageJa(personality, achievedPercent)
            "zh" -> getGoalFailedMessageZh(personality, achievedPercent)
            "es" -> getGoalFailedMessageEs(personality, achievedPercent)
            else -> getGoalFailedMessageEn(personality, achievedPercent)
        }
    }

    private fun getGoalFailedMessageKo(p: PetPersonalityV2, pct: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { pct >= 80 -> "아깝다. ${pct}%였어. 내일은 해내자."; pct >= 50 -> "절반은 했네. 다음엔 더 잘할 수 있어."; else -> "오늘은 힘들었나 보네. 괜찮아. 내일이 있어." }
        PetPersonalityV2.TSUNDERE -> when { pct >= 80 -> "${pct}%... 아까워. ...다음엔 꼭 해."; pct >= 50 -> "절반은 했네... 뭐, 안 한 것보단 낫지."; else -> "오늘은... 그래, 쉬는 날도 필요해. ...걱정이야." }
        PetPersonalityV2.FOODIE -> when { pct >= 80 -> "${pct}%! 아깝다! 내일 밥 먹고 도전!"; pct >= 50 -> "절반! 괜찮아! 밥 먹고 힘내자!"; else -> "오늘은 쉬어! 맛있는 거 먹으면 기운 나!" }
        PetPersonalityV2.PLAYFUL -> when { pct >= 80 -> "${pct}%?! 아까워! 내일 리벤지!!"; pct >= 50 -> "절반! 괜찮아괜찮아! 다음엔 더 재밌게!"; else -> "오늘은 쉬어! 내일 더 신나게 하자!" }
        PetPersonalityV2.TIMID -> when { pct >= 80 -> "${pct}%에요... 아, 아깝지만 잘했어요..."; pct >= 50 -> "절반이나 했어요... 대단해요..."; else -> "괜, 괜찮아요... 쉬는 것도 중요해요..." }
        PetPersonalityV2.CLUMSY -> when { pct >= 80 -> "${pct}%! 아깝다! 나도 자주 실패해! 괜찮아!"; pct >= 50 -> "절반! 나도 반은 넘어지니까 괜찮아!"; else -> "오늘은 쉬어! 나도 자주 쉬는- 앗 거짓말!" }
    }

    private fun getGoalFailedMessageEn(p: PetPersonalityV2, pct: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { pct >= 80 -> "Close. ${pct}%. Tomorrow you'll make it."; pct >= 50 -> "Half done. You can do better next."; else -> "Tough day. It's okay. Tomorrow's another day." }
        PetPersonalityV2.TSUNDERE -> when { pct >= 80 -> "${pct}%... So close. ...Do it next time."; pct >= 50 -> "Half done... Better than nothing."; else -> "Today... Yeah, rest days matter. ...Worried." }
        PetPersonalityV2.FOODIE -> when { pct >= 80 -> "${pct}%! So close! Eat and try tomorrow!"; pct >= 50 -> "Half! It's okay! Eat and cheer up!"; else -> "Rest today! Yummy food gives energy!" }
        PetPersonalityV2.PLAYFUL -> when { pct >= 80 -> "${pct}%?! So close! Revenge tomorrow!!"; pct >= 50 -> "Half! It's okay! More fun next time!"; else -> "Rest today! Let's have more fun tomorrow!" }
        PetPersonalityV2.TIMID -> when { pct >= 80 -> "${pct}%... C-close but you did well..."; pct >= 50 -> "Half done... That's amazing..."; else -> "I-it's okay... Rest is important too..." }
        PetPersonalityV2.CLUMSY -> when { pct >= 80 -> "${pct}%! So close! I fail often too! It's okay!"; pct >= 50 -> "Half! I fall half the time too! Okay!"; else -> "Rest today! I rest often- oops lie!" }
    }

    private fun getGoalFailedMessageJa(p: PetPersonalityV2, pct: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { pct >= 80 -> "惜しい。${pct}%だった。明日はやろう。"; pct >= 50 -> "半分はやったね。次はもっとできる。"; else -> "今日は大変だったね。大丈夫。明日がある。" }
        PetPersonalityV2.TSUNDERE -> when { pct >= 80 -> "${pct}%...惜しい。...次は絶対。"; pct >= 50 -> "半分はやったね...やらないよりマシ。"; else -> "今日は...そう、休みも必要。...心配だし。" }
        PetPersonalityV2.FOODIE -> when { pct >= 80 -> "${pct}%！惜しい！明日食べて挑戦！"; pct >= 50 -> "半分！大丈夫！食べて頑張ろう！"; else -> "今日は休んで！美味しいもので元気出る！" }
        PetPersonalityV2.PLAYFUL -> when { pct >= 80 -> "${pct}%?!惜しい！明日リベンジ!!"; pct >= 50 -> "半分！大丈夫大丈夫！次はもっと楽しく！"; else -> "今日は休んで！明日もっと楽しく！" }
        PetPersonalityV2.TIMID -> when { pct >= 80 -> "${pct}%です...お、惜しいけど頑張りました..."; pct >= 50 -> "半分もやりました...すごい..."; else -> "だ、大丈夫です...休むのも大事..." }
        PetPersonalityV2.CLUMSY -> when { pct >= 80 -> "${pct}%！惜しい！僕もよく失敗する！大丈夫！"; pct >= 50 -> "半分！僕も半分転ぶから大丈夫！"; else -> "今日は休んで！僕もよく休む-あっ嘘！" }
    }

    private fun getGoalFailedMessageZh(p: PetPersonalityV2, pct: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { pct >= 80 -> "可惜。${pct}%。明天一定行。"; pct >= 50 -> "做了一半。下次能做更好。"; else -> "今天辛苦了。没关系。还有明天。" }
        PetPersonalityV2.TSUNDERE -> when { pct >= 80 -> "${pct}%...可惜。...下次一定。"; pct >= 50 -> "做了一半...比不做好。"; else -> "今天...是的，休息也重要。...担心。" }
        PetPersonalityV2.FOODIE -> when { pct >= 80 -> "${pct}%！可惜！明天吃饭再挑战！"; pct >= 50 -> "一半！没关系！吃饭加油！"; else -> "今天休息！吃好吃的会有精神！" }
        PetPersonalityV2.PLAYFUL -> when { pct >= 80 -> "${pct}%?!可惜！明天复仇!!"; pct >= 50 -> "一半！没关系没关系！下次更好玩！"; else -> "今天休息！明天更开心！" }
        PetPersonalityV2.TIMID -> when { pct >= 80 -> "${pct}%...虽、虽然可惜但做得很好..."; pct >= 50 -> "做了一半...好厉害..."; else -> "没、没关系...休息也重要..." }
        PetPersonalityV2.CLUMSY -> when { pct >= 80 -> "${pct}%！可惜！我也常失败！没关系！"; pct >= 50 -> "一半！我也摔一半时间！没关系！"; else -> "今天休息！我也常休息-哎呀说谎了！" }
    }

    private fun getGoalFailedMessageEs(p: PetPersonalityV2, pct: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { pct >= 80 -> "Casi. ${pct}%. Mañana lo logras."; pct >= 50 -> "La mitad. Puedes hacerlo mejor."; else -> "Día difícil. Está bien. Mañana es otro día." }
        PetPersonalityV2.TSUNDERE -> when { pct >= 80 -> "${pct}%... Tan cerca. ...Hazlo la próxima."; pct >= 50 -> "La mitad... Mejor que nada."; else -> "Hoy... Sí, descansar importa. ...Preocupado." }
        PetPersonalityV2.FOODIE -> when { pct >= 80 -> "¡${pct}%! ¡Tan cerca! ¡Come e intenta mañana!"; pct >= 50 -> "¡Mitad! ¡Está bien! ¡Come y ánimo!"; else -> "¡Descansa hoy! ¡La comida da energía!" }
        PetPersonalityV2.PLAYFUL -> when { pct >= 80 -> "¿¡${pct}%?! ¡Tan cerca! ¡¡Revancha mañana!!"; pct >= 50 -> "¡Mitad! ¡Está bien! ¡Más diversión la próxima!"; else -> "¡Descansa hoy! ¡Más diversión mañana!" }
        PetPersonalityV2.TIMID -> when { pct >= 80 -> "${pct}%... C-casi pero lo hiciste bien..."; pct >= 50 -> "La mitad... Eso es increíble..."; else -> "E-está bien... Descansar también importa..." }
        PetPersonalityV2.CLUMSY -> when { pct >= 80 -> "¡${pct}%! ¡Casi! ¡Yo también fallo! ¡Está bien!"; pct >= 50 -> "¡Mitad! ¡Yo me caigo la mitad! ¡Bien!"; else -> "¡Descansa hoy! Yo descanso- ¡ups mentira!" }
    }

    // ===== 목표 초과 달성 (120% 이상) =====
    fun getOverAchievementMessage(personality: PetPersonalityV2, achievedPercent: Int): String {
        return when (getLang()) {
            "ko" -> getOverAchievementMessageKo(personality, achievedPercent)
            "ja" -> getOverAchievementMessageJa(personality, achievedPercent)
            "zh" -> getOverAchievementMessageZh(personality, achievedPercent)
            "es" -> getOverAchievementMessageEs(personality, achievedPercent)
            else -> getOverAchievementMessageEn(personality, achievedPercent)
        }
    }

    private fun getOverAchievementMessageKo(p: PetPersonalityV2, pct: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { pct >= 200 -> "200%?! ...대단하다. 진심으로 감동이야."; pct >= 150 -> "150%. 열심히 했네. 자랑스럽다."; else -> "초과 달성. 멋있어." }
        PetPersonalityV2.TSUNDERE -> when { pct >= 200 -> "200%?! 미, 미쳤어?! ...대단해."; pct >= 150 -> "150%... 오버하는 거 아니야? ...멋있긴 해."; else -> "목표 넘겼네. ...뭐, 잘했어." }
        PetPersonalityV2.FOODIE -> when { pct >= 200 -> "200%?! 밥 두 배로 먹어도 돼!!"; pct >= 150 -> "150%! 간식 추가다!!"; else -> "초과 달성! 맛있는 거 먹을 자격 있어!" }
        PetPersonalityV2.PLAYFUL -> when { pct >= 200 -> "200%?! 미쳤다!! 전설이야!!!"; pct >= 150 -> "150%!! 대박대박!! 파티다!!"; else -> "초과!! 우와아!! 신난다!!" }
        PetPersonalityV2.TIMID -> when { pct >= 200 -> "200%...?! 저, 정말 대단해요...!"; pct >= 150 -> "150%... 너무 멋있어요..."; else -> "초과 달성이에요... 정말 잘하셨어요..." }
        PetPersonalityV2.CLUMSY -> when { pct >= 200 -> "200%?! 축하 점프!! 앗 착지 실패!! 괜찮아!!"; pct >= 150 -> "150%!! 대단해! 춤출- 앗 넘어져!"; else -> "초과다!! 최고! 뒤뚱뒤뚱~ 앗!" }
    }

    private fun getOverAchievementMessageEn(p: PetPersonalityV2, pct: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { pct >= 200 -> "200%?! ...Amazing. Truly impressed."; pct >= 150 -> "150%. Worked hard. Proud of you."; else -> "Exceeded goal. Awesome." }
        PetPersonalityV2.TSUNDERE -> when { pct >= 200 -> "200%?! C-crazy?! ...Amazing."; pct >= 150 -> "150%... Overdoing it? ...Cool though."; else -> "Beat the goal. ...Well, good job." }
        PetPersonalityV2.FOODIE -> when { pct >= 200 -> "200%?! Double food allowed!!"; pct >= 150 -> "150%! Extra snack!!"; else -> "Exceeded! You deserve yummy food!" }
        PetPersonalityV2.PLAYFUL -> when { pct >= 200 -> "200%?! Crazy!! Legend!!!"; pct >= 150 -> "150%!! Amazing!! Party!!"; else -> "Exceeded!! Woohoo!! Exciting!!" }
        PetPersonalityV2.TIMID -> when { pct >= 200 -> "200%...?! Y-you're amazing...!"; pct >= 150 -> "150%... So cool..."; else -> "Exceeded... You did so well..." }
        PetPersonalityV2.CLUMSY -> when { pct >= 200 -> "200%?! Jump!! Oops bad landing!! Okay!!"; pct >= 150 -> "150%!! Amazing! Dance- oops fell!"; else -> "Exceeded!! Best! Wobble~ oops!" }
    }

    private fun getOverAchievementMessageJa(p: PetPersonalityV2, pct: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { pct >= 200 -> "200%?!...すごい。本当に感動。"; pct >= 150 -> "150%。頑張ったね。誇りに思う。"; else -> "目標超過。かっこいい。" }
        PetPersonalityV2.TSUNDERE -> when { pct >= 200 -> "200%?!や、ヤバい?!...すごい。"; pct >= 150 -> "150%...やりすぎじゃない?...かっこいいけど。"; else -> "目標超えたね。...まあ、よくやった。" }
        PetPersonalityV2.FOODIE -> when { pct >= 200 -> "200%?!ご飯二倍食べていい!!"; pct >= 150 -> "150%!おやつ追加!!"; else -> "超過達成!美味しいもの食べる資格あり!" }
        PetPersonalityV2.PLAYFUL -> when { pct >= 200 -> "200%?!ヤバい!!レジェンド!!!"; pct >= 150 -> "150%!!すごすぎ!!パーティー!!"; else -> "超過!!うわあ!!楽しい!!" }
        PetPersonalityV2.TIMID -> when { pct >= 200 -> "200%...?!す、すごいです...!"; pct >= 150 -> "150%...かっこいい..."; else -> "超過達成です...本当に頑張りました..." }
        PetPersonalityV2.CLUMSY -> when { pct >= 200 -> "200%?!お祝いジャンプ!!あっ着地失敗!!大丈夫!!"; pct >= 150 -> "150%!!すごい!踊る-あっ転んだ!"; else -> "超過だ!!最高!よちよち~あっ!" }
    }

    private fun getOverAchievementMessageZh(p: PetPersonalityV2, pct: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { pct >= 200 -> "200%?!...了不起。真的感动。"; pct >= 150 -> "150%。很努力。为你骄傲。"; else -> "超额完成。很酷。" }
        PetPersonalityV2.TSUNDERE -> when { pct >= 200 -> "200%?!疯、疯了?!...厉害。"; pct >= 150 -> "150%...做过头了吧?...挺酷的。"; else -> "超过目标了。...嗯，做得好。" }
        PetPersonalityV2.FOODIE -> when { pct >= 200 -> "200%?!可以吃双倍!!"; pct >= 150 -> "150%!加零食!!"; else -> "超额完成!值得吃好吃的!" }
        PetPersonalityV2.PLAYFUL -> when { pct >= 200 -> "200%?!疯了!!传说!!!"; pct >= 150 -> "150%!!太棒了!!派对!!"; else -> "超过了!!哇!!好兴奋!!" }
        PetPersonalityV2.TIMID -> when { pct >= 200 -> "200%...?!你、你好厉害...!"; pct >= 150 -> "150%...好酷..."; else -> "超额完成了...你做得真好..." }
        PetPersonalityV2.CLUMSY -> when { pct >= 200 -> "200%?!庆祝跳!!哎呀落地失败!!没事!!"; pct >= 150 -> "150%!!厉害!跳舞-哎呀摔了!"; else -> "超过了!!最棒!摇晃~哎呀!" }
    }

    private fun getOverAchievementMessageEs(p: PetPersonalityV2, pct: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { pct >= 200 -> "¿¡200%?! ...Increíble. Impresionado de verdad."; pct >= 150 -> "150%. Trabajaste duro. Orgulloso."; else -> "Superaste la meta. Genial." }
        PetPersonalityV2.TSUNDERE -> when { pct >= 200 -> "¿¡200%?! ¿¡L-loco?! ...Increíble."; pct >= 150 -> "150%... ¿Exagerando? ...Pero genial."; else -> "Superaste la meta. ...Bueno, bien hecho." }
        PetPersonalityV2.FOODIE -> when { pct >= 200 -> "¿¡200%?! ¡¡Doble comida permitida!!"; pct >= 150 -> "¡150%! ¡¡Snack extra!!"; else -> "¡Superado! ¡Mereces comida rica!" }
        PetPersonalityV2.PLAYFUL -> when { pct >= 200 -> "¿¡200%?! ¡¡Loco!! ¡¡¡Leyenda!!!"; pct >= 150 -> "¡¡150%!! ¡¡Increíble!! ¡¡Fiesta!!"; else -> "¡¡Superado!! ¡¡Woohoo!! ¡¡Emocionante!!" }
        PetPersonalityV2.TIMID -> when { pct >= 200 -> "200%...?! E-eres increíble...!"; pct >= 150 -> "150%... Tan genial..."; else -> "Superado... Lo hiciste muy bien..." }
        PetPersonalityV2.CLUMSY -> when { pct >= 200 -> "¿¡200%?! ¡¡Salto!! ¡¡Ups mal aterrizaje!! ¡¡Bien!!"; pct >= 150 -> "¡¡150%!! ¡Genial! Bailo- ¡ups caí!"; else -> "¡¡Superado!! ¡Lo mejor! Tambaleo~ ¡ups!" }
    }

    // ===== 특별한 날 (기념일 등) =====
    fun getSpecialDayMessage(personality: PetPersonalityV2, dayType: String): String {
        return when (getLang()) {
            "ko" -> getSpecialDayMessageKo(personality, dayType)
            "ja" -> getSpecialDayMessageJa(personality, dayType)
            "zh" -> getSpecialDayMessageZh(personality, dayType)
            "es" -> getSpecialDayMessageEs(personality, dayType)
            else -> getSpecialDayMessageEn(personality, dayType)
        }
    }

    private fun getSpecialDayMessageKo(p: PetPersonalityV2, t: String): String = when (t) {
        "first_meeting" -> when (p) { PetPersonalityV2.LOYAL -> "벌써 100일이야. 앞으로도 함께하자."; PetPersonalityV2.TSUNDERE -> "100일이라니... 뭐, 축하해. ...나도 기뻐."; PetPersonalityV2.FOODIE -> "100일!! 케이크 먹자!!"; PetPersonalityV2.PLAYFUL -> "100일이다!! 파티파티!!"; PetPersonalityV2.TIMID -> "100일이에요... 감사해요..."; PetPersonalityV2.CLUMSY -> "100일! 축하 춤! 앗 넘어졌다!" }
        "new_year" -> when (p) { PetPersonalityV2.LOYAL -> "새해 복 많이 받아."; PetPersonalityV2.TSUNDERE -> "새해다... 뭐, 올해도 잘 부탁해."; PetPersonalityV2.FOODIE -> "새해! 떡국 먹자!!"; PetPersonalityV2.PLAYFUL -> "새해다!! 올해도 신나게!!"; PetPersonalityV2.TIMID -> "새해 복 많이 받으세요..."; PetPersonalityV2.CLUMSY -> "새해! 올해는 안 넘어질- 앗!" }
        "birthday" -> when (p) { PetPersonalityV2.LOYAL -> "생일 축하해. 태어나줘서 고마워."; PetPersonalityV2.TSUNDERE -> "생일이잖아... 축하해. ...진심이야."; PetPersonalityV2.FOODIE -> "생일이다!! 케이크 케이크!!"; PetPersonalityV2.PLAYFUL -> "생일!! 축하해!! 파티다!!"; PetPersonalityV2.TIMID -> "생, 생일 축하드려요..."; PetPersonalityV2.CLUMSY -> "생일 축하! 촛불 불- 앗 얼굴에!" }
        else -> when (p) { PetPersonalityV2.LOYAL -> "오늘도 좋은 하루 되길."; PetPersonalityV2.TSUNDERE -> "뭐, 오늘도 화이팅."; PetPersonalityV2.FOODIE -> "오늘도 맛있는 하루!"; PetPersonalityV2.PLAYFUL -> "오늘도 재밌는 하루!"; PetPersonalityV2.TIMID -> "오늘도 좋은 하루 되세요..."; PetPersonalityV2.CLUMSY -> "오늘도 화이팅! 조심조심!" }
    }

    private fun getSpecialDayMessageEn(p: PetPersonalityV2, t: String): String = when (t) {
        "first_meeting" -> when (p) { PetPersonalityV2.LOYAL -> "Already 100 days. Let's stay together."; PetPersonalityV2.TSUNDERE -> "100 days? ...Well, congrats. ...I'm happy too."; PetPersonalityV2.FOODIE -> "100 days!! Let's eat cake!!"; PetPersonalityV2.PLAYFUL -> "100 days!! Party party!!"; PetPersonalityV2.TIMID -> "100 days... Thank you..."; PetPersonalityV2.CLUMSY -> "100 days! Celebration dance! Oops, fell!" }
        "new_year" -> when (p) { PetPersonalityV2.LOYAL -> "Happy New Year."; PetPersonalityV2.TSUNDERE -> "It's new year... Well, looking forward to this year too."; PetPersonalityV2.FOODIE -> "New Year! Let's eat!!"; PetPersonalityV2.PLAYFUL -> "New Year!! Let's have fun!!"; PetPersonalityV2.TIMID -> "Happy New Year..."; PetPersonalityV2.CLUMSY -> "New Year! This year I won't fall- oops!" }
        "birthday" -> when (p) { PetPersonalityV2.LOYAL -> "Happy birthday. Thanks for being born."; PetPersonalityV2.TSUNDERE -> "It's your birthday... Happy birthday. ...I mean it."; PetPersonalityV2.FOODIE -> "Birthday!! Cake cake!!"; PetPersonalityV2.PLAYFUL -> "Birthday!! Happy birthday!! Party!!"; PetPersonalityV2.TIMID -> "Ha-happy birthday..."; PetPersonalityV2.CLUMSY -> "Happy birthday! Blowing candles- oops face!" }
        else -> when (p) { PetPersonalityV2.LOYAL -> "Have a good day."; PetPersonalityV2.TSUNDERE -> "Well, good luck today."; PetPersonalityV2.FOODIE -> "Have a delicious day!"; PetPersonalityV2.PLAYFUL -> "Have a fun day!"; PetPersonalityV2.TIMID -> "Have a nice day..."; PetPersonalityV2.CLUMSY -> "Good luck today! Be careful!" }
    }

    private fun getSpecialDayMessageJa(p: PetPersonalityV2, t: String): String = when (t) {
        "first_meeting" -> when (p) { PetPersonalityV2.LOYAL -> "もう100日だね。これからも一緒にいよう。"; PetPersonalityV2.TSUNDERE -> "100日だって...まぁ、おめでとう。...私も嬉しい。"; PetPersonalityV2.FOODIE -> "100日!!ケーキ食べよう!!"; PetPersonalityV2.PLAYFUL -> "100日だ!!パーティー!!"; PetPersonalityV2.TIMID -> "100日ですね...ありがとうございます..."; PetPersonalityV2.CLUMSY -> "100日!お祝いダンス!あっ転んだ!" }
        "new_year" -> when (p) { PetPersonalityV2.LOYAL -> "明けましておめでとう。"; PetPersonalityV2.TSUNDERE -> "新年だね...まぁ、今年もよろしく。"; PetPersonalityV2.FOODIE -> "新年!おせち食べよう!!"; PetPersonalityV2.PLAYFUL -> "新年だ!!今年も楽しもう!!"; PetPersonalityV2.TIMID -> "明けましておめでとうございます..."; PetPersonalityV2.CLUMSY -> "新年!今年は転ばない-あっ!" }
        "birthday" -> when (p) { PetPersonalityV2.LOYAL -> "誕生日おめでとう。生まれてくれてありがとう。"; PetPersonalityV2.TSUNDERE -> "誕生日じゃん...おめでとう。...本気だよ。"; PetPersonalityV2.FOODIE -> "誕生日だ!!ケーキケーキ!!"; PetPersonalityV2.PLAYFUL -> "誕生日!!おめでとう!!パーティー!!"; PetPersonalityV2.TIMID -> "お、お誕生日おめでとうございます..."; PetPersonalityV2.CLUMSY -> "誕生日おめでとう!ろうそく消し-あっ顔に!" }
        else -> when (p) { PetPersonalityV2.LOYAL -> "今日もいい一日を。"; PetPersonalityV2.TSUNDERE -> "まぁ、今日も頑張って。"; PetPersonalityV2.FOODIE -> "今日も美味しい一日を!"; PetPersonalityV2.PLAYFUL -> "今日も楽しい一日を!"; PetPersonalityV2.TIMID -> "今日もいい一日を..."; PetPersonalityV2.CLUMSY -> "今日も頑張って!気をつけて!" }
    }

    private fun getSpecialDayMessageZh(p: PetPersonalityV2, t: String): String = when (t) {
        "first_meeting" -> when (p) { PetPersonalityV2.LOYAL -> "已经100天了。以后也在一起吧。"; PetPersonalityV2.TSUNDERE -> "100天了...算了，恭喜。...我也开心。"; PetPersonalityV2.FOODIE -> "100天!!吃蛋糕!!"; PetPersonalityV2.PLAYFUL -> "100天了!!派对派对!!"; PetPersonalityV2.TIMID -> "100天了...谢谢你..."; PetPersonalityV2.CLUMSY -> "100天!庆祝舞!哎呀摔倒了!" }
        "new_year" -> when (p) { PetPersonalityV2.LOYAL -> "新年快乐。"; PetPersonalityV2.TSUNDERE -> "新年了...算了，今年也请多关照。"; PetPersonalityV2.FOODIE -> "新年!吃年夜饭!!"; PetPersonalityV2.PLAYFUL -> "新年了!!开开心心!!"; PetPersonalityV2.TIMID -> "新年快乐..."; PetPersonalityV2.CLUMSY -> "新年!今年不会摔-哎呀!" }
        "birthday" -> when (p) { PetPersonalityV2.LOYAL -> "生日快乐。谢谢你出生。"; PetPersonalityV2.TSUNDERE -> "是生日啊...生日快乐。...是真心的。"; PetPersonalityV2.FOODIE -> "生日!!蛋糕蛋糕!!"; PetPersonalityV2.PLAYFUL -> "生日!!生日快乐!!派对!!"; PetPersonalityV2.TIMID -> "生、生日快乐..."; PetPersonalityV2.CLUMSY -> "生日快乐!吹蜡烛-哎呀脸上!" }
        else -> when (p) { PetPersonalityV2.LOYAL -> "今天也要美好。"; PetPersonalityV2.TSUNDERE -> "嗯，今天也加油。"; PetPersonalityV2.FOODIE -> "今天也要美味!"; PetPersonalityV2.PLAYFUL -> "今天也要开心!"; PetPersonalityV2.TIMID -> "今天也请美好..."; PetPersonalityV2.CLUMSY -> "今天也加油!小心点!" }
    }

    private fun getSpecialDayMessageEs(p: PetPersonalityV2, t: String): String = when (t) {
        "first_meeting" -> when (p) { PetPersonalityV2.LOYAL -> "Ya son 100 días. Sigamos juntos."; PetPersonalityV2.TSUNDERE -> "¿100 días? ...Bueno, felicidades. ...Yo también estoy feliz."; PetPersonalityV2.FOODIE -> "¡¡100 días!! ¡¡Comamos pastel!!"; PetPersonalityV2.PLAYFUL -> "¡¡100 días!! ¡¡Fiesta!!"; PetPersonalityV2.TIMID -> "100 días... Gracias..."; PetPersonalityV2.CLUMSY -> "¡100 días! ¡Baile de celebración! ¡Ups, me caí!" }
        "new_year" -> when (p) { PetPersonalityV2.LOYAL -> "Feliz Año Nuevo."; PetPersonalityV2.TSUNDERE -> "Es año nuevo... Bueno, espero que este año sea bueno."; PetPersonalityV2.FOODIE -> "¡Año Nuevo! ¡¡A comer!!"; PetPersonalityV2.PLAYFUL -> "¡¡Año Nuevo!! ¡¡A divertirnos!!"; PetPersonalityV2.TIMID -> "Feliz Año Nuevo..."; PetPersonalityV2.CLUMSY -> "¡Año Nuevo! Este año no me caeré- ¡ups!" }
        "birthday" -> when (p) { PetPersonalityV2.LOYAL -> "Feliz cumpleaños. Gracias por nacer."; PetPersonalityV2.TSUNDERE -> "Es tu cumpleaños... Feliz cumpleaños. ...Lo digo en serio."; PetPersonalityV2.FOODIE -> "¡¡Cumpleaños!! ¡¡Pastel pastel!!"; PetPersonalityV2.PLAYFUL -> "¡¡Cumpleaños!! ¡¡Feliz cumpleaños!! ¡¡Fiesta!!"; PetPersonalityV2.TIMID -> "Fe-feliz cumpleaños..."; PetPersonalityV2.CLUMSY -> "¡Feliz cumpleaños! Soplando velas- ¡ups cara!" }
        else -> when (p) { PetPersonalityV2.LOYAL -> "Que tengas un buen día."; PetPersonalityV2.TSUNDERE -> "Bueno, suerte hoy."; PetPersonalityV2.FOODIE -> "¡Que tengas un día delicioso!"; PetPersonalityV2.PLAYFUL -> "¡Que tengas un día divertido!"; PetPersonalityV2.TIMID -> "Que tengas un buen día..."; PetPersonalityV2.CLUMSY -> "¡Suerte hoy! ¡Con cuidado!" }
    }

    // ===== 챌린지 시작 =====
    fun getChallengeStartMessage(personality: PetPersonalityV2): String {
        return when (getLang()) {
            "ko" -> getChallengeStartMessageKo(personality)
            "ja" -> getChallengeStartMessageJa(personality)
            "zh" -> getChallengeStartMessageZh(personality)
            "es" -> getChallengeStartMessageEs(personality)
            else -> getChallengeStartMessageEn(personality)
        }
    }

    private fun getChallengeStartMessageKo(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "챌린지 시작. 같이 가자."
        PetPersonalityV2.TSUNDERE -> "챌린지? ...뭐, 도와줄게."
        PetPersonalityV2.FOODIE -> "챌린지다! 끝나면 간식!"
        PetPersonalityV2.PLAYFUL -> "챌린지!! 신난다!! 고고!!"
        PetPersonalityV2.TIMID -> "챌린지... 같이 해요..."
        PetPersonalityV2.CLUMSY -> "챌린지! 시작! 앗 출발부터 비틀!"
    }

    private fun getChallengeStartMessageEn(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "Challenge starts. Let's go together."
        PetPersonalityV2.TSUNDERE -> "Challenge? ...Fine, I'll help."
        PetPersonalityV2.FOODIE -> "Challenge! Snacks after!"
        PetPersonalityV2.PLAYFUL -> "Challenge!! So excited!! Let's go!!"
        PetPersonalityV2.TIMID -> "Challenge... Let's do it together..."
        PetPersonalityV2.CLUMSY -> "Challenge! Start! Oops stumbled already!"
    }

    private fun getChallengeStartMessageJa(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "チャレンジ開始。一緒に行こう。"
        PetPersonalityV2.TSUNDERE -> "チャレンジ?...まぁ、手伝ってあげる。"
        PetPersonalityV2.FOODIE -> "チャレンジだ!終わったらおやつ!"
        PetPersonalityV2.PLAYFUL -> "チャレンジ!!楽しみ!!行こう!!"
        PetPersonalityV2.TIMID -> "チャレンジ...一緒にやりましょう..."
        PetPersonalityV2.CLUMSY -> "チャレンジ!スタート!あっもうふらついた!"
    }

    private fun getChallengeStartMessageZh(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "挑战开始。一起走吧。"
        PetPersonalityV2.TSUNDERE -> "挑战?...算了，我帮你。"
        PetPersonalityV2.FOODIE -> "挑战开始!完了吃零食!"
        PetPersonalityV2.PLAYFUL -> "挑战!!好兴奋!!走起!!"
        PetPersonalityV2.TIMID -> "挑战...一起做吧..."
        PetPersonalityV2.CLUMSY -> "挑战!开始!哎呀一开始就晃!"
    }

    private fun getChallengeStartMessageEs(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> "Empieza el desafío. Vamos juntos."
        PetPersonalityV2.TSUNDERE -> "¿Desafío? ...Bueno, te ayudaré."
        PetPersonalityV2.FOODIE -> "¡Desafío! ¡Después bocadillos!"
        PetPersonalityV2.PLAYFUL -> "¡¡Desafío!! ¡¡Qué emoción!! ¡¡Vamos!!"
        PetPersonalityV2.TIMID -> "Desafío... Hagámoslo juntos..."
        PetPersonalityV2.CLUMSY -> "¡Desafío! ¡Empezamos! ¡Ups ya tropecé!"
    }

    // ===== 배고픔 상태 (happiness 낮을 때) =====
    fun getHungryMessage(personality: PetPersonalityV2, happiness: Int): String {
        return when (getLang()) {
            "ko" -> getHungryMessageKo(personality, happiness)
            "ja" -> getHungryMessageJa(personality, happiness)
            "zh" -> getHungryMessageZh(personality, happiness)
            "es" -> getHungryMessageEs(personality, happiness)
            else -> getHungryMessageEn(personality, happiness)
        }
    }

    private fun getHungryMessageKo(p: PetPersonalityV2, h: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { h < 30 -> "...배고파."; h < 50 -> "밥 때 아니야?"; else -> "간식 있어?" }
        PetPersonalityV2.TSUNDERE -> when { h < 30 -> "배고프다고! ...미안, 좀 예민해."; h < 50 -> "밥... 언제 줘? 기다리는 거 아니야!"; else -> "간식... 있으면 좋겠다고. 달라는 거 아니야!" }
        PetPersonalityV2.FOODIE -> when { h < 30 -> "배고파아아!! 밥!! 밥 줘!!"; h < 50 -> "꼬르륵... 배고파~ 밥~"; else -> "간식 타임 아니야?" }
        PetPersonalityV2.PLAYFUL -> when { h < 30 -> "배고파서 힘이 안 나..."; h < 50 -> "밥 먹고 놀자! 배고파!"; else -> "간식 먹고 더 놀자!" }
        PetPersonalityV2.TIMID -> when { h < 30 -> "저, 저... 배가 고파요..."; h < 50 -> "혹시... 밥... 있나요...?"; else -> "간식... 주실 수 있을까요...?" }
        PetPersonalityV2.CLUMSY -> when { h < 30 -> "배고파서 비틀비틀... 앗!"; h < 50 -> "밥 어디야! 찾다가 넘어- 앗!"; else -> "간식! 앗 흘렸다!" }
    }

    private fun getHungryMessageEn(p: PetPersonalityV2, h: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { h < 30 -> "...Hungry."; h < 50 -> "Isn't it meal time?"; else -> "Got snacks?" }
        PetPersonalityV2.TSUNDERE -> when { h < 30 -> "I said I'm hungry! ...Sorry, I'm sensitive."; h < 50 -> "Food... when? Not waiting or anything!"; else -> "Snacks... would be nice. Not asking though!" }
        PetPersonalityV2.FOODIE -> when { h < 30 -> "So hungryyyy!! Food!! Give me food!!"; h < 50 -> "Growl... hungry~ food~"; else -> "Isn't it snack time?" }
        PetPersonalityV2.PLAYFUL -> when { h < 30 -> "No energy because hungry..."; h < 50 -> "Let's eat and play! Hungry!"; else -> "Let's have snacks and play more!" }
        PetPersonalityV2.TIMID -> when { h < 30 -> "I, I... I'm hungry..."; h < 50 -> "Maybe... food... is there any...?"; else -> "Snacks... could you give me some...?" }
        PetPersonalityV2.CLUMSY -> when { h < 30 -> "Staggering from hunger... oops!"; h < 50 -> "Where's food! Looking and fell- oops!"; else -> "Snacks! Oops spilled it!" }
    }

    private fun getHungryMessageJa(p: PetPersonalityV2, h: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { h < 30 -> "...お腹すいた。"; h < 50 -> "ご飯の時間じゃない?"; else -> "おやつある?" }
        PetPersonalityV2.TSUNDERE -> when { h < 30 -> "お腹すいたって!...ごめん、ちょっと敏感で。"; h < 50 -> "ご飯...いつ?待ってるわけじゃないけど!"; else -> "おやつ...あるといいな。欲しいわけじゃないけど!" }
        PetPersonalityV2.FOODIE -> when { h < 30 -> "お腹すいたあああ!!ご飯!!ご飯ちょうだい!!"; h < 50 -> "ぐうぐう...お腹すいた~ご飯~"; else -> "おやつタイムじゃない?" }
        PetPersonalityV2.PLAYFUL -> when { h < 30 -> "お腹すいて力が出ない..."; h < 50 -> "ご飯食べて遊ぼう!お腹すいた!"; else -> "おやつ食べてもっと遊ぼう!" }
        PetPersonalityV2.TIMID -> when { h < 30 -> "あ、あの...お腹がすきました..."; h < 50 -> "もしかして...ご飯...ありますか...?"; else -> "おやつ...いただけますか...?" }
        PetPersonalityV2.CLUMSY -> when { h < 30 -> "お腹すいてふらふら...あっ!"; h < 50 -> "ご飯どこ!探してて転ん-あっ!"; else -> "おやつ!あっこぼした!" }
    }

    private fun getHungryMessageZh(p: PetPersonalityV2, h: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { h < 30 -> "...饿了。"; h < 50 -> "不是吃饭时间吗?"; else -> "有零食吗?" }
        PetPersonalityV2.TSUNDERE -> when { h < 30 -> "说了饿了!...抱歉，有点敏感。"; h < 50 -> "饭...什么时候吃?我才没在等!"; else -> "零食...有的话就好了。才不是要!" }
        PetPersonalityV2.FOODIE -> when { h < 30 -> "饿死了啊啊!!饭!!给我饭!!"; h < 50 -> "咕噜咕噜...饿了~饭~"; else -> "不是零食时间吗?" }
        PetPersonalityV2.PLAYFUL -> when { h < 30 -> "饿得没力气了..."; h < 50 -> "吃了饭再玩!饿了!"; else -> "吃零食再玩!" }
        PetPersonalityV2.TIMID -> when { h < 30 -> "那、那个...我饿了..."; h < 50 -> "请问...有饭...吗...?"; else -> "零食...可以给我吗...?" }
        PetPersonalityV2.CLUMSY -> when { h < 30 -> "饿得摇摇晃晃...哎呀!"; h < 50 -> "饭在哪!找着找着摔-哎呀!"; else -> "零食!哎呀洒了!" }
    }

    private fun getHungryMessageEs(p: PetPersonalityV2, h: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> when { h < 30 -> "...Hambre."; h < 50 -> "¿No es hora de comer?"; else -> "¿Tienes bocadillos?" }
        PetPersonalityV2.TSUNDERE -> when { h < 30 -> "¡Dije que tengo hambre! ...Perdón, estoy sensible."; h < 50 -> "Comida... ¿cuándo? ¡No estoy esperando!"; else -> "Bocadillos... estaría bien. ¡No los pido!" }
        PetPersonalityV2.FOODIE -> when { h < 30 -> "¡¡Tengo mucha hambre!! ¡¡Comida!! ¡¡Dame comida!!"; h < 50 -> "Gruñido... hambre~ comida~"; else -> "¿No es hora de bocadillos?" }
        PetPersonalityV2.PLAYFUL -> when { h < 30 -> "Sin energía por hambre..."; h < 50 -> "¡Comamos y juguemos! ¡Hambre!"; else -> "¡Comamos bocadillos y juguemos más!" }
        PetPersonalityV2.TIMID -> when { h < 30 -> "Yo, yo... tengo hambre..."; h < 50 -> "Quizás... comida... ¿hay...?"; else -> "Bocadillos... ¿me podrías dar...?" }
        PetPersonalityV2.CLUMSY -> when { h < 30 -> "Tambaleando de hambre... ¡ups!"; h < 50 -> "¡¿Dónde está la comida?! Buscando y me caí- ¡ups!"; else -> "¡Bocadillos! ¡Ups los derramé!" }
    }

    // ===== 운동 챌린지 응원 메시지 (10개 단위) =====
    fun getExerciseEncouragement(petType: PetTypeV2, reps: Int): String {
        return when (getLang()) {
            "ko" -> getExerciseEncouragementKo(petType.personality, reps)
            "ja" -> getExerciseEncouragementJa(petType.personality, reps)
            "zh" -> getExerciseEncouragementZh(petType.personality, reps)
            "es" -> getExerciseEncouragementEs(petType.personality, reps)
            else -> getExerciseEncouragementEn(petType.personality, reps)
        }
    }

    private fun getExerciseEncouragementKo(p: PetPersonalityV2, r: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> "와! ${r}개 했어! 힘내!"
        PetPersonalityV2.TSUNDERE -> "흥, ${r}개? ...괜찮네."
        PetPersonalityV2.FOODIE -> "${r}개! 운동하면 밥이 맛있어!"
        PetPersonalityV2.PLAYFUL -> "야호! ${r}개! 최고야!"
        PetPersonalityV2.TIMID -> "${r}개... 대단해요..."
        PetPersonalityV2.CLUMSY -> "오, ${r}개! 나도 할래! 앗-"
    }

    private fun getExerciseEncouragementEn(p: PetPersonalityV2, r: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> "Wow! You did $r! Keep it up!"
        PetPersonalityV2.TSUNDERE -> "Hmph, $r? ...Not bad."
        PetPersonalityV2.FOODIE -> "$r! Exercise makes food taste better!"
        PetPersonalityV2.PLAYFUL -> "Yay! $r! You're the best!"
        PetPersonalityV2.TIMID -> "$r... Amazing..."
        PetPersonalityV2.CLUMSY -> "Oh, $r! I wanna try too! Oops-"
    }

    private fun getExerciseEncouragementJa(p: PetPersonalityV2, r: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> "わぁ!${r}回やった!頑張れ!"
        PetPersonalityV2.TSUNDERE -> "ふん、${r}回?...まぁまぁね。"
        PetPersonalityV2.FOODIE -> "${r}回!運動したらご飯がおいしい!"
        PetPersonalityV2.PLAYFUL -> "やったー!${r}回!最高!"
        PetPersonalityV2.TIMID -> "${r}回...すごいです..."
        PetPersonalityV2.CLUMSY -> "お、${r}回!私もやりたい!あっ-"
    }

    private fun getExerciseEncouragementZh(p: PetPersonalityV2, r: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> "哇!做了${r}个!加油!"
        PetPersonalityV2.TSUNDERE -> "哼,${r}个?...还行。"
        PetPersonalityV2.FOODIE -> "${r}个!运动了吃饭更香!"
        PetPersonalityV2.PLAYFUL -> "耶!${r}个!最棒了!"
        PetPersonalityV2.TIMID -> "${r}个...好厉害..."
        PetPersonalityV2.CLUMSY -> "哦,${r}个!我也要做!哎呀-"
    }

    private fun getExerciseEncouragementEs(p: PetPersonalityV2, r: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> "¡Guau! ¡Hiciste $r! ¡Sigue así!"
        PetPersonalityV2.TSUNDERE -> "Hmph, ¿$r? ...No está mal."
        PetPersonalityV2.FOODIE -> "¡$r! ¡El ejercicio hace que la comida sepa mejor!"
        PetPersonalityV2.PLAYFUL -> "¡Yupi! ¡$r! ¡Eres el mejor!"
        PetPersonalityV2.TIMID -> "$r... Increíble..."
        PetPersonalityV2.CLUMSY -> "Oh, ¡$r! ¡Yo también quiero! ¡Ups-"
    }

    // ===== 운동 챌린지 완료 메시지 =====
    fun getExerciseCompleteMessage(petType: PetTypeV2, reps: Int): String {
        return when (getLang()) {
            "ko" -> getExerciseCompleteMessageKo(petType.personality, reps)
            "ja" -> getExerciseCompleteMessageJa(petType.personality, reps)
            "zh" -> getExerciseCompleteMessageZh(petType.personality, reps)
            "es" -> getExerciseCompleteMessageEs(petType.personality, reps)
            else -> getExerciseCompleteMessageEn(petType.personality, reps)
        }
    }

    private fun getExerciseCompleteMessageKo(p: PetPersonalityV2, r: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> "${r}개 완료! 너 진짜 대단해!"
        PetPersonalityV2.TSUNDERE -> "${r}개? ...뭐, 잘했어. 인정해줄게."
        PetPersonalityV2.FOODIE -> "와! ${r}개 성공! 이제 간식 먹자~"
        PetPersonalityV2.PLAYFUL -> "우와아! ${r}개 다 했어! 너무 멋져!"
        PetPersonalityV2.TIMID -> "${r}개... 다 하셨어요... 정말 대단해요..."
        PetPersonalityV2.CLUMSY -> "${r}개 완료! 나도 박수! 짝짝- 앗 손이!"
    }

    private fun getExerciseCompleteMessageEn(p: PetPersonalityV2, r: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> "$r completed! You're amazing!"
        PetPersonalityV2.TSUNDERE -> "$r? ...Well, good job. I'll acknowledge it."
        PetPersonalityV2.FOODIE -> "Wow! $r done! Now let's have snacks~"
        PetPersonalityV2.PLAYFUL -> "Woohoo! You did all $r! So cool!"
        PetPersonalityV2.TIMID -> "$r... You did it all... So amazing..."
        PetPersonalityV2.CLUMSY -> "$r completed! Applause from me too! Clap clap- oops my hands!"
    }

    private fun getExerciseCompleteMessageJa(p: PetPersonalityV2, r: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> "${r}回完了!君すごいね!"
        PetPersonalityV2.TSUNDERE -> "${r}回?...まぁ、よくやったわ。認めてあげる。"
        PetPersonalityV2.FOODIE -> "わぁ!${r}回成功!おやつ食べよう~"
        PetPersonalityV2.PLAYFUL -> "やったあ!${r}回全部やった!すごい!"
        PetPersonalityV2.TIMID -> "${r}回...全部やりましたね...本当にすごいです..."
        PetPersonalityV2.CLUMSY -> "${r}回完了!私も拍手!パチパチ-あっ手が!"
    }

    private fun getExerciseCompleteMessageZh(p: PetPersonalityV2, r: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> "${r}个完成!你真厉害!"
        PetPersonalityV2.TSUNDERE -> "${r}个?...算了,做得不错。承认你。"
        PetPersonalityV2.FOODIE -> "哇!${r}个成功!现在吃零食~"
        PetPersonalityV2.PLAYFUL -> "哇啊!${r}个都做完了!太帅了!"
        PetPersonalityV2.TIMID -> "${r}个...全做完了...真的好厉害..."
        PetPersonalityV2.CLUMSY -> "${r}个完成!我也鼓掌!啪啪-哎呀手!"
    }

    private fun getExerciseCompleteMessageEs(p: PetPersonalityV2, r: Int): String = when (p) {
        PetPersonalityV2.LOYAL -> "¡$r completados! ¡Eres increíble!"
        PetPersonalityV2.TSUNDERE -> "¿$r? ...Bueno, bien hecho. Lo reconozco."
        PetPersonalityV2.FOODIE -> "¡Guau! ¡$r hechos! ¡Ahora bocadillos~"
        PetPersonalityV2.PLAYFUL -> "¡¡Yujuu!! ¡¡Hiciste todos $r!! ¡¡Qué genial!!"
        PetPersonalityV2.TIMID -> "$r... Los hiciste todos... Increíble..."
        PetPersonalityV2.CLUMSY -> "¡$r completados! ¡Yo también aplaudo! Clap clap- ¡ups mis manos!"
    }

    // ===== 기능 소개 팁 (랜덤 표시) =====
    fun getFeatureTipMessage(personality: PetPersonalityV2): String {
        return when (getLang()) {
            "ko" -> getFeatureTipMessageKo(personality)
            "ja" -> getFeatureTipMessageJa(personality)
            "zh" -> getFeatureTipMessageZh(personality)
            "es" -> getFeatureTipMessageEs(personality)
            else -> getFeatureTipMessageEn(personality)
        }
    }

    private fun getFeatureTipMessageKo(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> listOf("위젯 설정하면 홈에서 바로 확인 가능해.", "챌린지 완료하면 칭호를 얻을 수 있어.", "스킨은 목표 달성하면 해금돼. 설정에서 봐.", "연속 달성하면 방어 티켓이 생겨.", "명언 위젯도 있어. 추천해.", "날씨 위젯에서 산책 타이밍 확인해.")
        PetPersonalityV2.TSUNDERE -> listOf("위젯... 설정해두면 편해. 알려주는 거야.", "챌린지하면 칭호 준대. ...관심 있으면.", "스킨 바꿀 수 있어. 설정에서... 뭐, 관심 없으면 됐어.", "연속 달성하면 방어 티켓이... 알아둬.", "명언 위젯도 있는데... 그냥 알려주는 거야.", "날씨 위젯 유용해. ...칭찬 아니야.")
        PetPersonalityV2.FOODIE -> listOf("위젯 설정하면 밥 먹으면서도 확인 가능해!", "챌린지하면 칭호 줘! 맛있는 칭호?!", "스킨 있어! 밥 먹은 만큼 이뻐진다?! 아닌가?!", "연속 달성하면 방어 티켓! 밥처럼 모아!", "명언 위젯! 명언 읽으면서 밥 먹자!", "날씨 위젯! 비 오면 집에서 밥!")
        PetPersonalityV2.PLAYFUL -> listOf("위젯 설정해! 홈에서 바로 보면 재밌어!", "챌린지하면 칭호 줘! 모으면 재밌지!", "스킨 바꿀 수 있어! 나 이쁘지?!", "연속 달성하면 방어 티켓! 모으자모으자!", "명언 위젯도 있어! 읽으면 힘나!", "날씨 위젯! 맑으면 산책 고고!")
        PetPersonalityV2.TIMID -> listOf("위젯... 설정하면 편해요...", "챌린지하면 칭호를 받을 수 있어요...", "스킨도 있어요... 설정에서요...", "연속 달성하면 방어 티켓이 생겨요...", "명언 위젯도 있어요... 좋아요...", "날씨 위젯... 산책 전에 확인해요...")
        PetPersonalityV2.CLUMSY -> listOf("위젯 설정해! 앗 설정 버튼 어디- 찾았다!", "챌린지하면 칭호 줘! 앗 버튼 잘못 눌렀다!", "스킨 바꿀 수 있어! 나 이뻐- 앗 거울에 부딪혀!", "연속 달성하면 방어 티켓! 어 이거 좋은- 앗!", "명언 위젯! 읽다가 넘어지지 않게 조심!", "날씨 위젯! 비 오면 미끄러우니까 조심!")
    }.random()

    private fun getFeatureTipMessageEn(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> listOf("Set up widgets to check from home.", "Complete challenges to get titles.", "Skins unlock when you reach goals. Check settings.", "Consecutive days give defense tickets.", "There's a quotes widget too. Recommended.", "Check weather widget for walk timing.")
        PetPersonalityV2.TSUNDERE -> listOf("Widgets... they're convenient. Just telling you.", "Challenges give titles. ...If you care.", "You can change skins. In settings... whatever if you don't care.", "Consecutive days give defense tickets... just know that.", "There's a quotes widget... just telling you.", "Weather widget is useful. ...Not a compliment.")
        PetPersonalityV2.FOODIE -> listOf("Set up widgets to check while eating!", "Challenges give titles! Delicious titles?!", "There are skins! Prettier the more you eat?! Maybe not?!", "Consecutive days give defense tickets! Collect like food!", "Quotes widget! Read quotes while eating!", "Weather widget! Stay home and eat when it rains!")
        PetPersonalityV2.PLAYFUL -> listOf("Set up widgets! Fun to check from home!", "Challenges give titles! Fun to collect!", "You can change skins! Am I pretty?!", "Consecutive days give defense tickets! Collect collect!", "There's a quotes widget! Gives energy!", "Weather widget! Go walk when it's sunny!")
        PetPersonalityV2.TIMID -> listOf("Widgets... are convenient...", "Challenges give you titles...", "There are skins too... in settings...", "Consecutive days give defense tickets...", "There's a quotes widget... it's nice...", "Weather widget... check before walking...")
        PetPersonalityV2.CLUMSY -> listOf("Set up widgets! Oops where's settings- found it!", "Challenges give titles! Oops wrong button!", "You can change skins! Am I pret- oops hit the mirror!", "Consecutive days give defense tickets! Oh this is good- oops!", "Quotes widget! Don't fall while reading!", "Weather widget! Be careful when rainy, it's slippery!")
    }.random()

    private fun getFeatureTipMessageJa(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> listOf("ウィジェット設定するとホームから確認できる。", "チャレンジ完了すると称号がもらえる。", "スキンは目標達成で解除される。設定で見て。", "連続達成すると防御チケットがもらえる。", "名言ウィジェットもあるよ。おすすめ。", "天気ウィジェットで散歩タイミングを確認して。")
        PetPersonalityV2.TSUNDERE -> listOf("ウィジェット...設定しておくと便利。教えてあげてるの。", "チャレンジすると称号もらえるって。...興味あれば。", "スキン変えられるよ。設定で...まぁ、興味なければいいけど。", "連続達成すると防御チケットが...覚えておいて。", "名言ウィジェットもあるけど...教えてあげてるだけ。", "天気ウィジェット便利。...褒めてないから。")
        PetPersonalityV2.FOODIE -> listOf("ウィジェット設定するとご飯食べながら確認できる!", "チャレンジすると称号くれる!おいしい称号?!", "スキンあるよ!食べた分きれいになる?!違うか?!", "連続達成すると防御チケット!ご飯みたいに集めて!", "名言ウィジェット!名言読みながらご飯!", "天気ウィジェット!雨なら家でご飯!")
        PetPersonalityV2.PLAYFUL -> listOf("ウィジェット設定して!ホームで見ると楽しい!", "チャレンジすると称号くれる!集めると楽しい!", "スキン変えられる!私かわいい?!", "連続達成すると防御チケット!集めよう集めよう!", "名言ウィジェットもある!読むと元気出る!", "天気ウィジェット!晴れたら散歩ゴー!")
        PetPersonalityV2.TIMID -> listOf("ウィジェット...設定すると便利です...", "チャレンジすると称号もらえます...", "スキンもあります...設定で...", "連続達成すると防御チケットがもらえます...", "名言ウィジェットもあります...いいですよ...", "天気ウィジェット...散歩前に確認して...")
        PetPersonalityV2.CLUMSY -> listOf("ウィジェット設定して!あっ設定どこ-見つけた!", "チャレンジすると称号くれる!あっボタン間違えた!", "スキン変えられる!私きれ-あっ鏡にぶつかった!", "連続達成すると防御チケット!これいい-あっ!", "名言ウィジェット!読んで転ばないように!", "天気ウィジェット!雨は滑るから気をつけて!")
    }.random()

    private fun getFeatureTipMessageZh(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> listOf("设置小组件可以从主屏幕直接查看。", "完成挑战可以获得称号。", "达成目标可以解锁皮肤。在设置里看。", "连续达成会有防御券。", "还有名言小组件。推荐。", "在天气小组件查看散步时机。")
        PetPersonalityV2.TSUNDERE -> listOf("小组件...设置了会方便。只是告诉你。", "挑战会给称号。...如果你感兴趣的话。", "可以换皮肤。在设置...算了，不感兴趣就算了。", "连续达成会有防御券...记住。", "还有名言小组件...只是告诉你。", "天气小组件很有用。...不是夸奖。")
        PetPersonalityV2.FOODIE -> listOf("设置小组件可以边吃边看!", "挑战会给称号!好吃的称号?!", "有皮肤!吃得越多越漂亮?!不是吗?!", "连续达成会有防御券!像饭一样收集!", "名言小组件!边读名言边吃饭!", "天气小组件!下雨在家吃饭!")
        PetPersonalityV2.PLAYFUL -> listOf("设置小组件!从主屏幕看很有趣!", "挑战会给称号!收集很有趣!", "可以换皮肤!我漂亮吗?!", "连续达成会有防御券!收集收集!", "还有名言小组件!看了有力量!", "天气小组件!晴天去散步!")
        PetPersonalityV2.TIMID -> listOf("小组件...设置了会方便...", "挑战可以获得称号...", "还有皮肤...在设置里...", "连续达成会有防御券...", "还有名言小组件...很好...", "天气小组件...散步前确认...")
        PetPersonalityV2.CLUMSY -> listOf("设置小组件!哎呀设置在哪-找到了!", "挑战会给称号!哎呀按错按钮了!", "可以换皮肤!我漂-哎呀撞镜子了!", "连续达成会有防御券!这个好-哎呀!", "名言小组件!读的时候别摔倒!", "天气小组件!下雨路滑小心!")
    }.random()

    private fun getFeatureTipMessageEs(p: PetPersonalityV2): String = when (p) {
        PetPersonalityV2.LOYAL -> listOf("Configura widgets para ver desde inicio.", "Completa desafíos para obtener títulos.", "Las skins se desbloquean al alcanzar metas. Mira en ajustes.", "Días consecutivos dan tickets de defensa.", "También hay widget de citas. Recomendado.", "Revisa el widget del clima para pasear.")
        PetPersonalityV2.TSUNDERE -> listOf("Widgets... son convenientes. Solo te aviso.", "Los desafíos dan títulos. ...Si te importa.", "Puedes cambiar skins. En ajustes... como sea si no te importa.", "Días consecutivos dan tickets de defensa... solo saber.", "Hay widget de citas... solo te digo.", "Widget del clima es útil. ...No es un cumplido.")
        PetPersonalityV2.FOODIE -> listOf("¡Configura widgets para ver mientras comes!", "¡Los desafíos dan títulos! ¿¡Títulos deliciosos?!", "¡Hay skins! ¿¡Más bonito cuanto más comes?! ¿¡O no?!", "¡Días consecutivos dan tickets de defensa! ¡Colecciona como comida!", "¡Widget de citas! ¡Lee citas mientras comes!", "¡Widget del clima! ¡Quédate y come cuando llueva!")
        PetPersonalityV2.PLAYFUL -> listOf("¡Configura widgets! ¡Divertido ver desde inicio!", "¡Los desafíos dan títulos! ¡Divertido coleccionar!", "¡Puedes cambiar skins! ¿¡Soy bonito?!", "¡Días consecutivos dan tickets de defensa! ¡Colecciona colecciona!", "¡Hay widget de citas! ¡Da energía!", "¡Widget del clima! ¡Pasea cuando esté soleado!")
        PetPersonalityV2.TIMID -> listOf("Widgets... son convenientes...", "Los desafíos te dan títulos...", "También hay skins... en ajustes...", "Días consecutivos dan tickets de defensa...", "Hay widget de citas... es bueno...", "Widget del clima... revisa antes de pasear...")
        PetPersonalityV2.CLUMSY -> listOf("¡Configura widgets! ¡Ups dónde está ajustes- lo encontré!", "¡Los desafíos dan títulos! ¡Ups botón equivocado!", "¡Puedes cambiar skins! ¡Soy boni- ups choqué con el espejo!", "¡Días consecutivos dan tickets de defensa! Esto es bue- ¡ups!", "¡Widget de citas! ¡No te caigas leyendo!", "¡Widget del clima! ¡Cuidado cuando llueva, resbala!")
    }.random()
}
