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

    // ===== 오랜만에 접속 (daysSinceLastVisit일 만에) =====
    fun getLongTimeNoSeeMessage(personality: PetPersonalityV2, daysSinceLastVisit: Int): String {
        return when (personality) {
            PetPersonalityV2.LOYAL -> when {
                daysSinceLastVisit >= 7 -> "일주일이나... 걱정했어."
                daysSinceLastVisit >= 3 -> "어디 갔었어? 기다렸는데."
                else -> "왔구나. 보고 싶었어."
            }
            PetPersonalityV2.TSUNDERE -> when {
                daysSinceLastVisit >= 7 -> "흥, 일주일? ...많이 걱정했거든!"
                daysSinceLastVisit >= 3 -> "뭐야, 이제 와? ...기다린 거 아니야."
                else -> "왔어? ...뭐, 상관없지만."
            }
            PetPersonalityV2.FOODIE -> when {
                daysSinceLastVisit >= 7 -> "일주일?! 밥은 먹었어?! 나도 배고파!"
                daysSinceLastVisit >= 3 -> "어디서 맛있는 거 먹고 왔어?!"
                else -> "왔다! 같이 밥 먹자!"
            }
            PetPersonalityV2.PLAYFUL -> when {
                daysSinceLastVisit >= 7 -> "일주일?! 완전 심심했어!! 놀자!!"
                daysSinceLastVisit >= 3 -> "어디 갔었어?! 나 혼자 뭐하라고!"
                else -> "왔다!! 빨리 놀자!!"
            }
            PetPersonalityV2.TIMID -> when {
                daysSinceLastVisit >= 7 -> "일주일이요...? 저, 정말 외로웠어요..."
                daysSinceLastVisit >= 3 -> "보고 싶었어요... 많이요..."
                else -> "다, 다행이에요... 왔네요..."
            }
            PetPersonalityV2.CLUMSY -> when {
                daysSinceLastVisit >= 7 -> "일주일?! 기다리다 넘어졌어 여러 번!"
                daysSinceLastVisit >= 3 -> "어디 갔었어! 찾으러 가다가 앗-"
                else -> "왔다! 반가- 앗 미끄러!"
            }
        }
    }

    // ===== 스트릭 축하 (연속 N일) =====
    fun getStreakCelebrationMessage(personality: PetPersonalityV2, streakDays: Int): String {
        return when (personality) {
            PetPersonalityV2.LOYAL -> when {
                streakDays >= 30 -> "한 달 연속. 대단하다. 진심으로."
                streakDays >= 14 -> "2주 연속이다. 자랑스럽다."
                streakDays >= 7 -> "일주일 연속. 잘하고 있어."
                else -> "${streakDays}일 연속. 계속 가자."
            }
            PetPersonalityV2.TSUNDERE -> when {
                streakDays >= 30 -> "한 달이라니... 좀 감동이야. 아니, 그게 아니라!"
                streakDays >= 14 -> "2주? 뭐, 대단하긴 해. ...진짜로."
                streakDays >= 7 -> "일주일... 인정해줄게."
                else -> "${streakDays}일 연속이네. ...나쁘지 않아."
            }
            PetPersonalityV2.FOODIE -> when {
                streakDays >= 30 -> "한 달?! 대박! 파티다 파티! 맛있는 거!!"
                streakDays >= 14 -> "2주! 축하 간식 먹자!!"
                streakDays >= 7 -> "일주일! 밥 한 끼 더 먹어도 돼?!"
                else -> "${streakDays}일! 간식 시간이다 꿀꿀!"
            }
            PetPersonalityV2.PLAYFUL -> when {
                streakDays >= 30 -> "한 달?! 미쳤다!! 레전드!!!"
                streakDays >= 14 -> "2주다!! 완전 대박!!"
                streakDays >= 7 -> "일주일! 우와아! 최고야!"
                else -> "${streakDays}일! 신난다!!"
            }
            PetPersonalityV2.TIMID -> when {
                streakDays >= 30 -> "한 달이에요...! 저, 정말 감동이에요...!"
                streakDays >= 14 -> "2주라니... 대단해요..."
                streakDays >= 7 -> "일주일... 정말 잘하고 계세요..."
                else -> "${streakDays}일째에요... 응원할게요..."
            }
            PetPersonalityV2.CLUMSY -> when {
                streakDays >= 30 -> "한 달?! 축하 춤! 뒤뚱- 앗 넘어질뻔!"
                streakDays >= 14 -> "2주! 대단해! 앗 발 걸려- 괜찮아!"
                streakDays >= 7 -> "일주일! 최고! 점프- 앗 착지 실패!"
                else -> "${streakDays}일! 축하해! 뒤뚱뒤뚱~"
            }
        }
    }

    // ===== 스트릭 끊김 (아쉬움) =====
    fun getStreakBrokenMessage(personality: PetPersonalityV2, previousStreak: Int): String {
        return when (personality) {
            PetPersonalityV2.LOYAL -> when {
                previousStreak >= 7 -> "${previousStreak}일이었는데... 괜찮아. 다시 하면 돼."
                else -> "연속 기록이 끊겼어. 다시 시작하자."
            }
            PetPersonalityV2.TSUNDERE -> when {
                previousStreak >= 7 -> "${previousStreak}일이었는데... 아쉬워. 아, 아니 그냥!"
                else -> "끊겼네... 뭐, 다시 하면 되지."
            }
            PetPersonalityV2.FOODIE -> when {
                previousStreak >= 7 -> "${previousStreak}일이었는데 ㅠㅠ 힘내! 밥 먹고 다시!"
                else -> "괜찮아! 먹고 힘내자!"
            }
            PetPersonalityV2.PLAYFUL -> when {
                previousStreak >= 7 -> "${previousStreak}일이었는데... 에이! 다시 하면 돼!"
                else -> "괜찮아괜찮아! 리트라이!!"
            }
            PetPersonalityV2.TIMID -> when {
                previousStreak >= 7 -> "${previousStreak}일이었는데... 괜, 괜찮아요..."
                else -> "저, 저도 응원할게요... 다시 해봐요..."
            }
            PetPersonalityV2.CLUMSY -> when {
                previousStreak >= 7 -> "${previousStreak}일이었는데... 나도 자주 넘어져! 일어나면 돼!"
                else -> "괜찮아! 나도 맨날 넘어지는걸! 파이팅!"
            }
        }
    }

    // ===== 시간대별 인사 =====
    fun getTimeOfDayGreeting(personality: PetPersonalityV2, hour: Int): String {
        val timeOfDay = when {
            hour in 5..8 -> "early_morning"   // 이른 아침
            hour in 9..11 -> "morning"        // 아침
            hour in 12..13 -> "lunch"         // 점심
            hour in 14..17 -> "afternoon"     // 오후
            hour in 18..21 -> "evening"       // 저녁
            else -> "night"                   // 밤
        }

        return when (personality) {
            PetPersonalityV2.LOYAL -> when (timeOfDay) {
                "early_morning" -> "일찍 일어났네. 좋은 아침."
                "morning" -> "좋은 아침이다."
                "lunch" -> "점심 먹었어?"
                "afternoon" -> "오후도 화이팅."
                "evening" -> "저녁이다. 오늘 수고했어."
                else -> "밤이네. 푹 쉬어."
            }
            PetPersonalityV2.TSUNDERE -> when (timeOfDay) {
                "early_morning" -> "벌써 일어났어? ...부지런하네."
                "morning" -> "아침이야... 뭐, 좋은 아침."
                "lunch" -> "밥은 먹었어? ...그냥 물어본 거야."
                "afternoon" -> "나른하네... 졸린 거 아니야."
                "evening" -> "저녁이다... 오늘도 수고. ...진심이야."
                else -> "밤이네... 잘 자. 내일 봐."
            }
            PetPersonalityV2.FOODIE -> when (timeOfDay) {
                "early_morning" -> "아침밥 시간!! 일어났어?!"
                "morning" -> "좋은 아침! 아침밥 뭐야?!"
                "lunch" -> "점심이다!! 뭐 먹지?!"
                "afternoon" -> "간식 타임이다~ 꿀꿀!"
                "evening" -> "저녁!! 맛있는 거 먹자!!"
                else -> "야식 먹을까? ...아 살찐다!"
            }
            PetPersonalityV2.PLAYFUL -> when (timeOfDay) {
                "early_morning" -> "우와 일찍이다! 놀 시간 많다!"
                "morning" -> "좋은 아침!! 오늘 뭐 하지?!"
                "lunch" -> "점심이다! 먹고 놀자!"
                "afternoon" -> "오후다! 심심해! 뭐 하지?!"
                "evening" -> "저녁이다~ 불금?! 아 평일이네"
                else -> "밤이다! 밤새 놀자! ...농담!"
            }
            PetPersonalityV2.TIMID -> when (timeOfDay) {
                "early_morning" -> "아, 안녕하세요... 일찍 일어나셨네요..."
                "morning" -> "좋, 좋은 아침이에요..."
                "lunch" -> "점심... 드셨나요...?"
                "afternoon" -> "오후예요... 힘내세요..."
                "evening" -> "저녁이에요... 오늘도 수고하셨어요..."
                else -> "밤이에요... 푹 쉬세요..."
            }
            PetPersonalityV2.CLUMSY -> when (timeOfDay) {
                "early_morning" -> "좋은 아침! 앗 아직 졸려서 비틀-"
                "morning" -> "아침이다! 스트레칭- 앗 뻐근!"
                "lunch" -> "점심!! 밥 먹다가 흘리면 안 돼!"
                "afternoon" -> "오후다! 산책- 앗 문턱!"
                "evening" -> "저녁이다! 앗 어두워서 안 보- 쿵!"
                else -> "밤이다! 잘 자- 앗 이불에 걸려!"
            }
        }
    }

    // ===== 터치/상호작용 반응 =====
    fun getTouchReactionMessage(personality: PetPersonalityV2): String {
        val messages = when (personality) {
            PetPersonalityV2.LOYAL -> listOf(
                "왜? 무슨 일이야?",
                "쓰다듬는 거야? 좋네.",
                "그래그래.",
                "뭐, 싫지 않아.",
                "...좋아."
            )
            PetPersonalityV2.TSUNDERE -> listOf(
                "뭐, 뭐야?!",
                "만지지 마! ...조금만.",
                "흥, 뭔데? ...기분 나쁘진 않아.",
                "갑자기 왜 그래! ...더 해도 돼.",
                "부, 부끄러워!"
            )
            PetPersonalityV2.FOODIE -> listOf(
                "뭐야? 간식?!",
                "쓰다듬어 줘? 히히~",
                "좋다~ 꿀꿀!",
                "배 만지면 안 돼! 간지러워!",
                "더 해줘~ 기분 좋아!"
            )
            PetPersonalityV2.PLAYFUL -> listOf(
                "뭐야뭐야?! 놀자?!",
                "우와! 기분 좋아!",
                "더더더! 신난다!",
                "간지러워 히히히!",
                "좋아좋아!! 최고야!"
            )
            PetPersonalityV2.TIMID -> listOf(
                "앗...! 깜짝이야...",
                "저, 저요...?",
                "부, 부드럽게요...",
                "...좋아요...",
                "감사해요... 헤헤..."
            )
            PetPersonalityV2.CLUMSY -> listOf(
                "앗! 깜짝이- 비틀!",
                "간지러워! 앗 넘어질-",
                "좋다! 뒤뚱뒤뚱~",
                "더 해줘! 앗 균형이-",
                "히히! 앗 미끄러질뻔!"
            )
        }
        return messages.random()
    }

    // ===== 목표 실패 (오늘 못 채웠을 때) =====
    fun getGoalFailedMessage(personality: PetPersonalityV2, achievedPercent: Int): String {
        return when (personality) {
            PetPersonalityV2.LOYAL -> when {
                achievedPercent >= 80 -> "아깝다. ${achievedPercent}%였어. 내일은 해내자."
                achievedPercent >= 50 -> "절반은 했네. 다음엔 더 잘할 수 있어."
                else -> "오늘은 힘들었나 보네. 괜찮아. 내일이 있어."
            }
            PetPersonalityV2.TSUNDERE -> when {
                achievedPercent >= 80 -> "${achievedPercent}%... 아까워. ...다음엔 꼭 해."
                achievedPercent >= 50 -> "절반은 했네... 뭐, 안 한 것보단 낫지."
                else -> "오늘은... 그래, 쉬는 날도 필요해. ...걱정이야."
            }
            PetPersonalityV2.FOODIE -> when {
                achievedPercent >= 80 -> "${achievedPercent}%! 아깝다! 내일 밥 먹고 도전!"
                achievedPercent >= 50 -> "절반! 괜찮아! 밥 먹고 힘내자!"
                else -> "오늘은 쉬어! 맛있는 거 먹으면 기운 나!"
            }
            PetPersonalityV2.PLAYFUL -> when {
                achievedPercent >= 80 -> "${achievedPercent}%?! 아까워! 내일 리벤지!!"
                achievedPercent >= 50 -> "절반! 괜찮아괜찮아! 다음엔 더 재밌게!"
                else -> "오늘은 쉬어! 내일 더 신나게 하자!"
            }
            PetPersonalityV2.TIMID -> when {
                achievedPercent >= 80 -> "${achievedPercent}%에요... 아, 아깝지만 잘했어요..."
                achievedPercent >= 50 -> "절반이나 했어요... 대단해요..."
                else -> "괜, 괜찮아요... 쉬는 것도 중요해요..."
            }
            PetPersonalityV2.CLUMSY -> when {
                achievedPercent >= 80 -> "${achievedPercent}%! 아깝다! 나도 자주 실패해! 괜찮아!"
                achievedPercent >= 50 -> "절반! 나도 반은 넘어지니까 괜찮아!"
                else -> "오늘은 쉬어! 나도 자주 쉬는- 앗 거짓말!"
            }
        }
    }

    // ===== 목표 초과 달성 (120% 이상) =====
    fun getOverAchievementMessage(personality: PetPersonalityV2, achievedPercent: Int): String {
        return when (personality) {
            PetPersonalityV2.LOYAL -> when {
                achievedPercent >= 200 -> "200%?! ...대단하다. 진심으로 감동이야."
                achievedPercent >= 150 -> "150%. 열심히 했네. 자랑스럽다."
                else -> "초과 달성. 멋있어."
            }
            PetPersonalityV2.TSUNDERE -> when {
                achievedPercent >= 200 -> "200%?! 미, 미쳤어?! ...대단해."
                achievedPercent >= 150 -> "150%... 오버하는 거 아니야? ...멋있긴 해."
                else -> "목표 넘겼네. ...뭐, 잘했어."
            }
            PetPersonalityV2.FOODIE -> when {
                achievedPercent >= 200 -> "200%?! 밥 두 배로 먹어도 돼!!"
                achievedPercent >= 150 -> "150%! 간식 추가다!!"
                else -> "초과 달성! 맛있는 거 먹을 자격 있어!"
            }
            PetPersonalityV2.PLAYFUL -> when {
                achievedPercent >= 200 -> "200%?! 미쳤다!! 전설이야!!!"
                achievedPercent >= 150 -> "150%!! 대박대박!! 파티다!!"
                else -> "초과!! 우와아!! 신난다!!"
            }
            PetPersonalityV2.TIMID -> when {
                achievedPercent >= 200 -> "200%...?! 저, 정말 대단해요...!"
                achievedPercent >= 150 -> "150%... 너무 멋있어요..."
                else -> "초과 달성이에요... 정말 잘하셨어요..."
            }
            PetPersonalityV2.CLUMSY -> when {
                achievedPercent >= 200 -> "200%?! 축하 점프!! 앗 착지 실패!! 괜찮아!!"
                achievedPercent >= 150 -> "150%!! 대단해! 춤출- 앗 넘어져!"
                else -> "초과다!! 최고! 뒤뚱뒤뚱~ 앗!"
            }
        }
    }

    // ===== 특별한 날 (기념일 등) =====
    fun getSpecialDayMessage(personality: PetPersonalityV2, dayType: String): String {
        return when (dayType) {
            "first_meeting" -> when (personality) {  // 만난 지 100일
                PetPersonalityV2.LOYAL -> "벌써 100일이야. 앞으로도 함께하자."
                PetPersonalityV2.TSUNDERE -> "100일이라니... 뭐, 축하해. ...나도 기뻐."
                PetPersonalityV2.FOODIE -> "100일!! 케이크 먹자!!"
                PetPersonalityV2.PLAYFUL -> "100일이다!! 파티파티!!"
                PetPersonalityV2.TIMID -> "100일이에요... 감사해요..."
                PetPersonalityV2.CLUMSY -> "100일! 축하 춤! 앗 넘어졌다!"
            }
            "new_year" -> when (personality) {  // 새해
                PetPersonalityV2.LOYAL -> "새해 복 많이 받아."
                PetPersonalityV2.TSUNDERE -> "새해다... 뭐, 올해도 잘 부탁해."
                PetPersonalityV2.FOODIE -> "새해! 떡국 먹자!!"
                PetPersonalityV2.PLAYFUL -> "새해다!! 올해도 신나게!!"
                PetPersonalityV2.TIMID -> "새해 복 많이 받으세요..."
                PetPersonalityV2.CLUMSY -> "새해! 올해는 안 넘어질- 앗!"
            }
            "birthday" -> when (personality) {  // 생일 (펫 생성일 기준)
                PetPersonalityV2.LOYAL -> "생일 축하해. 태어나줘서 고마워."
                PetPersonalityV2.TSUNDERE -> "생일이잖아... 축하해. ...진심이야."
                PetPersonalityV2.FOODIE -> "생일이다!! 케이크 케이크!!"
                PetPersonalityV2.PLAYFUL -> "생일!! 축하해!! 파티다!!"
                PetPersonalityV2.TIMID -> "생, 생일 축하드려요..."
                PetPersonalityV2.CLUMSY -> "생일 축하! 촛불 불- 앗 얼굴에!"
            }
            else -> when (personality) {  // 기본
                PetPersonalityV2.LOYAL -> "오늘도 좋은 하루 되길."
                PetPersonalityV2.TSUNDERE -> "뭐, 오늘도 화이팅."
                PetPersonalityV2.FOODIE -> "오늘도 맛있는 하루!"
                PetPersonalityV2.PLAYFUL -> "오늘도 재밌는 하루!"
                PetPersonalityV2.TIMID -> "오늘도 좋은 하루 되세요..."
                PetPersonalityV2.CLUMSY -> "오늘도 화이팅! 조심조심!"
            }
        }
    }

    // ===== 챌린지 시작 =====
    fun getChallengeStartMessage(personality: PetPersonalityV2): String {
        return when (personality) {
            PetPersonalityV2.LOYAL -> "챌린지 시작. 같이 가자."
            PetPersonalityV2.TSUNDERE -> "챌린지? ...뭐, 도와줄게."
            PetPersonalityV2.FOODIE -> "챌린지다! 끝나면 간식!"
            PetPersonalityV2.PLAYFUL -> "챌린지!! 신난다!! 고고!!"
            PetPersonalityV2.TIMID -> "챌린지... 같이 해요..."
            PetPersonalityV2.CLUMSY -> "챌린지! 시작! 앗 출발부터 비틀!"
        }
    }

    // ===== 배고픔 상태 (happiness 낮을 때) =====
    fun getHungryMessage(personality: PetPersonalityV2, happiness: Int): String {
        return when (personality) {
            PetPersonalityV2.LOYAL -> when {
                happiness < 30 -> "...배고파."
                happiness < 50 -> "밥 때 아니야?"
                else -> "간식 있어?"
            }
            PetPersonalityV2.TSUNDERE -> when {
                happiness < 30 -> "배고프다고! ...미안, 좀 예민해."
                happiness < 50 -> "밥... 언제 줘? 기다리는 거 아니야!"
                else -> "간식... 있으면 좋겠다고. 달라는 거 아니야!"
            }
            PetPersonalityV2.FOODIE -> when {
                happiness < 30 -> "배고파아아!! 밥!! 밥 줘!!"
                happiness < 50 -> "꼬르륵... 배고파~ 밥~"
                else -> "간식 타임 아니야?"
            }
            PetPersonalityV2.PLAYFUL -> when {
                happiness < 30 -> "배고파서 힘이 안 나..."
                happiness < 50 -> "밥 먹고 놀자! 배고파!"
                else -> "간식 먹고 더 놀자!"
            }
            PetPersonalityV2.TIMID -> when {
                happiness < 30 -> "저, 저... 배가 고파요..."
                happiness < 50 -> "혹시... 밥... 있나요...?"
                else -> "간식... 주실 수 있을까요...?"
            }
            PetPersonalityV2.CLUMSY -> when {
                happiness < 30 -> "배고파서 비틀비틀... 앗!"
                happiness < 50 -> "밥 어디야! 찾다가 넘어- 앗!"
                else -> "간식! 앗 흘렸다!"
            }
        }
    }

    // ===== 운동 챌린지 응원 메시지 (10개 단위) =====
    fun getExerciseEncouragement(petType: PetTypeV2, reps: Int): String {
        return when (petType.personality) {
            PetPersonalityV2.LOYAL -> "와! ${reps}개 했어! 힘내!"
            PetPersonalityV2.TSUNDERE -> "흥, ${reps}개? ...괜찮네."
            PetPersonalityV2.FOODIE -> "${reps}개! 운동하면 밥이 맛있어!"
            PetPersonalityV2.PLAYFUL -> "야호! ${reps}개! 최고야!"
            PetPersonalityV2.TIMID -> "${reps}개... 대단해요..."
            PetPersonalityV2.CLUMSY -> "오, ${reps}개! 나도 할래! 앗-"
        }
    }

    // ===== 운동 챌린지 완료 메시지 =====
    fun getExerciseCompleteMessage(petType: PetTypeV2, reps: Int): String {
        return when (petType.personality) {
            PetPersonalityV2.LOYAL -> "${reps}개 완료! 너 진짜 대단해!"
            PetPersonalityV2.TSUNDERE -> "${reps}개? ...뭐, 잘했어. 인정해줄게."
            PetPersonalityV2.FOODIE -> "와! ${reps}개 성공! 이제 간식 먹자~"
            PetPersonalityV2.PLAYFUL -> "우와아! ${reps}개 다 했어! 너무 멋져!"
            PetPersonalityV2.TIMID -> "${reps}개... 다 하셨어요... 정말 대단해요..."
            PetPersonalityV2.CLUMSY -> "${reps}개 완료! 나도 박수! 짝짝- 앗 손이!"
        }
    }
}
