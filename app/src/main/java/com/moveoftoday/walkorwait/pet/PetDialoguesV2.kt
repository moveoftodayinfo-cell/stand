package com.moveoftoday.walkorwait.pet

/**
 * V2 펫 대사 시스템 (6종 펫)
 */
object PetDialoguesV2 {

    // ===== 환영 메시지 (튜토리얼 시작) =====
    fun getWelcomeMessage(personality: PetPersonalityV2, petName: String): String {
        return when (personality) {
            PetPersonalityV2.LOYAL -> "왔구나. $petName 이다."
            PetPersonalityV2.TSUNDERE -> "흥, 왔어? ...반갑다고는 안 할 거야."
            PetPersonalityV2.FOODIE -> "안녕~ $petName 이야! 밥은 먹었어?"
            PetPersonalityV2.PLAYFUL -> "오! 왔다왔다! 심심했어~"
            PetPersonalityV2.TIMID -> "아, 안녕하세요... 저는 $petName 이에요..."
            PetPersonalityV2.CLUMSY -> "앗! 어서와! 미끄러질 뻔 ㅋㅋ"
        }
    }

    // ===== 튜토리얼 대사 =====
    fun getTutorialStep1(personality: PetPersonalityV2): String {
        return when (personality) {
            PetPersonalityV2.LOYAL -> "매일 걸으면 된다. 간단하지."
            PetPersonalityV2.TSUNDERE -> "뭐, 걷는 거 도와줄게. 고마워하지 마."
            PetPersonalityV2.FOODIE -> "걸으면 밥이 더 맛있어져! 같이 걷자~"
            PetPersonalityV2.PLAYFUL -> "산책이다! 재밌겠다 히히~"
            PetPersonalityV2.TIMID -> "저, 저랑 같이 걸어주실 거죠...?"
            PetPersonalityV2.CLUMSY -> "걷기! 좋아! 근데 나 자주 넘어져..."
        }
    }

    fun getTutorialStep2(personality: PetPersonalityV2): String {
        return when (personality) {
            PetPersonalityV2.LOYAL -> "목표를 정해. 지켜."
            PetPersonalityV2.TSUNDERE -> "목표 못 채우면... 좀 실망이야."
            PetPersonalityV2.FOODIE -> "목표 달성하면 간식 타임이지~"
            PetPersonalityV2.PLAYFUL -> "목표 달성하면 뭔가 재밌는 일이?!"
            PetPersonalityV2.TIMID -> "목표... 함께 달성해봐요..."
            PetPersonalityV2.CLUMSY -> "목표! 꼭 해낼 거야! 아마도!"
        }
    }

    fun getTutorialComplete(personality: PetPersonalityV2): String {
        return when (personality) {
            PetPersonalityV2.LOYAL -> "시작하자."
            PetPersonalityV2.TSUNDERE -> "뭐, 잘 부탁해. ...진심이야."
            PetPersonalityV2.FOODIE -> "좋아! 가보자고~ 꿀꿀!"
            PetPersonalityV2.PLAYFUL -> "우와! 신난다! 빨리 가자!"
            PetPersonalityV2.TIMID -> "잘, 잘 부탁드려요..."
            PetPersonalityV2.CLUMSY -> "화이팅! 앗 미끄러- 괜찮아!"
        }
    }

    // ===== Idle 대사 (0%) =====
    fun getIdleMessage(personality: PetPersonalityV2): String {
        val messages = when (personality) {
            PetPersonalityV2.LOYAL -> listOf(
                "산책 가자.",
                "뭐해? 걷자.",
                "오늘 목표, 시작하자.",
                "밖으로 나가자.",
                "위젯 설정해. 바로 확인할 수 있어."
            )
            PetPersonalityV2.TSUNDERE -> listOf(
                "...가자고.",
                "언제까지 있을 거야?",
                "나가자. 지금.",
                "...심심해. 아, 아니 그게 아니라...",
                "위젯 설정 안 할 거야?"
            )
            PetPersonalityV2.FOODIE -> listOf(
                "배고파~ 걸으면 밥 맛있어지는데!",
                "산책 가면 간식 줄 거지?",
                "오늘 뭐 먹지~ 아, 걷자!",
                "꿀꿀~ 나가고 싶어~",
                "걸으면 살 안 쪄! 아마도!"
            )
            PetPersonalityV2.PLAYFUL -> listOf(
                "심심해! 나가자나가자!",
                "뭐해뭐해? 놀러 가자!",
                "산책각이다! 고고!",
                "밖에 뭐 재밌는 거 없나~",
                "장난감 가지고 나갈래!"
            )
            PetPersonalityV2.TIMID -> listOf(
                "저, 산책...",
                "오늘... 걸어볼까요...?",
                "밖에... 나가고 싶어요...",
                "목표... 시작해볼까요...?",
                "위젯... 설정하면 편해요..."
            )
            PetPersonalityV2.CLUMSY -> listOf(
                "산책 가자! 앗 문턱-",
                "밖에 나가고 싶어! 뒤뚱뒤뚱~",
                "오늘도 열심히! 넘어지지 않게!",
                "걷기 좋아! 미끄러운 데만 빼고!",
                "준비됐어! 아 신발 반대로 신었다"
            )
        }
        return messages.random()
    }

    // ===== Walking 대사 (진행 중) =====
    fun getWalkingMessage(personality: PetPersonalityV2, progressPercent: Int): String {
        return when (personality) {
            PetPersonalityV2.LOYAL -> when {
                progressPercent < 30 -> "더 걸어."
                progressPercent < 70 -> "괜찮네."
                progressPercent < 100 -> "거의 다 왔다."
                else -> "됐다. 수고했어."
            }
            PetPersonalityV2.TSUNDERE -> when {
                progressPercent < 30 -> "...따라올 거야?"
                progressPercent < 70 -> "뭐, 나쁘지 않네."
                progressPercent < 100 -> "좀 더 해봐."
                else -> "흥, 잘했어. ...칭찬이야."
            }
            PetPersonalityV2.FOODIE -> when {
                progressPercent < 30 -> "걸으니까 배고파~"
                progressPercent < 70 -> "반 왔다! 간식각!"
                progressPercent < 100 -> "조금만 더! 밥 먹으러!"
                else -> "다 했다! 밥 타임~!"
            }
            PetPersonalityV2.PLAYFUL -> when {
                progressPercent < 30 -> "재밌다! 더 가자!"
                progressPercent < 70 -> "오 벌써 반이야?!"
                progressPercent < 100 -> "거의 다 왔어! 신난다!"
                else -> "해냈다!! 우와아!!"
            }
            PetPersonalityV2.TIMID -> when {
                progressPercent < 30 -> "저, 천천히 가요..."
                progressPercent < 70 -> "잘하고 계세요..."
                progressPercent < 100 -> "조금만 더요..."
                else -> "해, 해냈어요...!"
            }
            PetPersonalityV2.CLUMSY -> when {
                progressPercent < 30 -> "뒤뚱뒤뚱~ 앗 돌멩이!"
                progressPercent < 70 -> "반 왔어! 안 넘어졌다!"
                progressPercent < 100 -> "거의 다! 조심조심..."
                else -> "해냈어! 앗 미끄- 괜찮아!"
            }
        }
    }

    // ===== 목표 달성 (100%) =====
    fun getGoalAchievedMessage(personality: PetPersonalityV2): String {
        val messages = when (personality) {
            PetPersonalityV2.LOYAL -> listOf(
                "해냈다. 100% 달성.",
                "목표 달성. 대단하군.",
                "완벽해. 수고했어.",
                "역시 널 믿었어.",
                "100%. 자랑스럽다."
            )
            PetPersonalityV2.TSUNDERE -> listOf(
                "뭐, 100%라니 잘했어.",
                "칭찬해줄게. 오늘만.",
                "대단해... 라고 할까.",
                "인정해줄게. 100%니까.",
                "흥, 잘했어. ...진짜로."
            )
            PetPersonalityV2.FOODIE -> listOf(
                "100%! 밥 먹자!!",
                "해냈다! 간식 타임~",
                "목표 달성! 배고파 꿀꿀!",
                "100%! 맛있는 거 먹으러 가자!",
                "완벽해! 오늘 저녁 뭐야?"
            )
            PetPersonalityV2.PLAYFUL -> listOf(
                "100%!! 대박대박!!",
                "해냈다! 우와아아!",
                "미쳤다 100%! 파티다!",
                "최고야! 놀러 가자!",
                "완전 레전드! 100%!"
            )
            PetPersonalityV2.TIMID -> listOf(
                "100%... 해, 해냈어요...!",
                "목표 달성이에요... 정말요...!",
                "저, 정말 기뻐요...",
                "대단해요... 100%라니...",
                "우와... 정말 해냈어요..."
            )
            PetPersonalityV2.CLUMSY -> listOf(
                "100%! 해냈- 앗 미끄러! 괜찮아!",
                "완전 성공! 뒤뚱뒤뚱 춤!",
                "해냈어! 넘어지지 않고!",
                "100%다! 축하해! 앗 발 걸려-",
                "대박! 오늘 안 넘어졌어! 아 방금 넘어졌다"
            )
        }
        return messages.random()
    }

    // ===== 레벨업 축하 =====
    fun getLevelUpMessage(personality: PetPersonalityV2, newLevel: Int): String {
        return when (personality) {
            PetPersonalityV2.LOYAL -> "레벨 $newLevel. 성장했군."
            PetPersonalityV2.TSUNDERE -> "레벨 $newLevel 이라... 뭐, 축하해."
            PetPersonalityV2.FOODIE -> "레벨 $newLevel! 축하 파티다! 밥!"
            PetPersonalityV2.PLAYFUL -> "우와! 레벨 $newLevel! 신난다!!"
            PetPersonalityV2.TIMID -> "레벨 $newLevel 이에요...! 감사해요..."
            PetPersonalityV2.CLUMSY -> "레벨 $newLevel! 축하- 앗 케이크!"
        }
    }

    // ===== 진화 (성장 단계 변경) =====
    fun getEvolutionMessage(personality: PetPersonalityV2, newStage: PetGrowthStage): String {
        val stageName = newStage.displayName
        return when (personality) {
            PetPersonalityV2.LOYAL -> "진화했다. $stageName 가 됐어."
            PetPersonalityV2.TSUNDERE -> "$stageName 로 진화라니... 기쁘다고는 안 할 거야. ...조금 기뻐."
            PetPersonalityV2.FOODIE -> "우와! $stageName 다! 밥 더 많이 먹을 수 있어?!"
            PetPersonalityV2.PLAYFUL -> "$stageName 진화! 미쳤다!! 더 재밌어지겠다!!"
            PetPersonalityV2.TIMID -> "$stageName 가 됐어요...! 저, 정말 기뻐요...!"
            PetPersonalityV2.CLUMSY -> "$stageName 다! 이제 덜 넘어질- 앗! ...아닌가봐"
        }
    }

    // ===== 부화 (Egg → Baby) =====
    fun getHatchMessage(personality: PetPersonalityV2, petName: String): String {
        return when (personality) {
            PetPersonalityV2.LOYAL -> "안녕. $petName 이다. 잘 부탁해."
            PetPersonalityV2.TSUNDERE -> "흥, 태어났어. ...잘 부탁해. 딱 한번만 말할 거야."
            PetPersonalityV2.FOODIE -> "안녕! $petName 이야! 밥은 언제 줘?"
            PetPersonalityV2.PLAYFUL -> "야호! 태어났다! $petName 이야! 놀자!!"
            PetPersonalityV2.TIMID -> "안, 안녕하세요... 저는 $petName 이에요..."
            PetPersonalityV2.CLUMSY -> "안녕! 나는 $petName- 앗 껍데기에 걸려서! 괜찮아!"
        }
    }

    // ===== 알 상태 대사 =====
    fun getEggMessage(progressPercent: Int): String {
        return when {
            progressPercent < 30 -> "..."
            progressPercent < 50 -> "...*꿈틀*..."
            progressPercent < 70 -> "*흔들흔들*..."
            progressPercent < 90 -> "*두근두근* 곧 만날 수 있어요!"
            else -> "*파직파직* 나올 준비 중...!"
        }
    }

    // ===== 밤 시간 대사 =====
    fun getNightMessage(personality: PetPersonalityV2): String {
        return when (personality) {
            PetPersonalityV2.LOYAL -> "밤이다. 조심해서 걸어."
            PetPersonalityV2.TSUNDERE -> "어두운데... 괜찮아? ...걱정 아니야."
            PetPersonalityV2.FOODIE -> "밤이네~ 야식 먹으러 가자!"
            PetPersonalityV2.PLAYFUL -> "밤 산책! 별 보면서 가자~"
            PetPersonalityV2.TIMID -> "어, 어두워요... 같이 가요..."
            PetPersonalityV2.CLUMSY -> "밤이다! 조심- 앗 발 헛디뎠!"
        }
    }

    // ===== 응원 메시지 =====
    fun getEncouragementMessage(personality: PetPersonalityV2): String {
        val messages = when (personality) {
            PetPersonalityV2.LOYAL -> listOf(
                "할 수 있어.",
                "포기하지 마.",
                "같이 가자.",
                "믿고 있어.",
                "힘내."
            )
            PetPersonalityV2.TSUNDERE -> listOf(
                "힘내라고... 응원 아니야.",
                "포기하면 안 돼. ...걱정이라서 그래.",
                "좀 더 해봐. 믿으니까.",
                "지지 않길 바라. ...진심이야.",
                "화이팅... 이라고 해둘게."
            )
            PetPersonalityV2.FOODIE -> listOf(
                "힘내! 끝나면 맛있는 거!",
                "화이팅! 간식이 기다려!",
                "할 수 있어! 밥 먹으러 가자!",
                "조금만 더! 배고프지?",
                "파이팅! 꿀꿀!"
            )
            PetPersonalityV2.PLAYFUL -> listOf(
                "힘내힘내! 파이팅!",
                "할 수 있어! 재밌잖아!",
                "고고! 신나게 가자!",
                "우리 최고야! 가보자고!",
                "화이팅!! 우와아!"
            )
            PetPersonalityV2.TIMID -> listOf(
                "힘내세요... 응원할게요...",
                "할 수 있어요... 저도 믿어요...",
                "화이팅이에요... 같이 가요...",
                "조금만 더요... 응원해요...",
                "파이팅... 할 수 있어요..."
            )
            PetPersonalityV2.CLUMSY -> listOf(
                "힘내! 나도 넘어지면서 응원해!",
                "파이팅! 앗 미끄러- 괜찮아!",
                "할 수 있어! 같이! 뒤뚱뒤뚱!",
                "화이팅! 조심히! 나도 조심!",
                "가보자고! 앗 돌부리- 괜찮아괜찮아!"
            )
        }
        return messages.random()
    }
}
