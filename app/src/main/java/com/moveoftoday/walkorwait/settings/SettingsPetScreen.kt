package com.moveoftoday.walkorwait.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moveoftoday.walkorwait.*
import com.moveoftoday.walkorwait.pet.*
import kotlinx.coroutines.launch
import java.util.Locale

// ============ 다국어 지원 헬퍼 객체 ============
private object SettingsPetStrings {
    private fun getLang(): String = Locale.getDefault().language

    fun currentPet(): String = when (getLang()) {
        "ko" -> "현재 펫"
        "ja" -> "現在のペット"
        "zh" -> "当前宠物"
        "es" -> "Mascota actual"
        else -> "Current Pet"
    }

    fun growthStage(): String = when (getLang()) {
        "ko" -> "성장 단계"
        "ja" -> "成長段階"
        "zh" -> "成长阶段"
        "es" -> "Etapa de crecimiento"
        else -> "Growth Stage"
    }

    fun petSkin(): String = when (getLang()) {
        "ko" -> "펫 스킨"
        "ja" -> "ペットスキン"
        "zh" -> "宠物皮肤"
        "es" -> "Skin de mascota"
        else -> "Pet Skin"
    }

    fun petTitle(): String = when (getLang()) {
        "ko" -> "칭호 선택"
        "ja" -> "称号選択"
        "zh" -> "称号选择"
        "es" -> "Seleccionar título"
        else -> "Select Title"
    }

    fun noTitlesYet(): String = when (getLang()) {
        "ko" -> "아직 획득한 칭호가 없어요\n챌린지를 완료하면 칭호를 얻을 수 있어요!"
        "ja" -> "まだ称号がありません\nチャレンジを完了して称号を獲得しよう！"
        "zh" -> "还没有获得称号\n完成挑战即可获得称号！"
        "es" -> "Aún no tienes títulos\n¡Completa desafíos para ganar títulos!"
        else -> "No titles earned yet\nComplete challenges to earn titles!"
    }

    fun equipped(): String = when (getLang()) {
        "ko" -> "장착중"
        "ja" -> "装備中"
        "zh" -> "已装备"
        "es" -> "Equipado"
        else -> "Equipped"
    }

    fun unequip(): String = when (getLang()) {
        "ko" -> "해제"
        "ja" -> "解除"
        "zh" -> "卸下"
        "es" -> "Desequipar"
        else -> "Unequip"
    }

    fun equip(): String = when (getLang()) {
        "ko" -> "장착"
        "ja" -> "装備"
        "zh" -> "装备"
        "es" -> "Equipar"
        else -> "Equip"
    }

    fun titleCount(count: Int): String = when (getLang()) {
        "ko" -> "획득한 칭호 ($count)"
        "ja" -> "獲得した称号 ($count)"
        "zh" -> "已获得称号 ($count)"
        "es" -> "Títulos obtenidos ($count)"
        else -> "Earned Titles ($count)"
    }

    fun goToChallenge(): String = when (getLang()) {
        "ko" -> "챌린지 하러 가기"
        "ja" -> "チャレンジへ"
        "zh" -> "去挑战"
        "es" -> "Ir a desafíos"
        else -> "Go to Challenge"
    }

    fun petStats(): String = when (getLang()) {
        "ko" -> "펫 통계"
        "ja" -> "ペット統計"
        "zh" -> "宠物统计"
        "es" -> "Estadísticas de mascota"
        else -> "Pet Stats"
    }

    fun changePet(): String = when (getLang()) {
        "ko" -> "펫 변경"
        "ja" -> "ペット変更"
        "zh" -> "更换宠物"
        "es" -> "Cambiar mascota"
        else -> "Change Pet"
    }

    fun totalSteps(): String = when (getLang()) {
        "ko" -> "총 걸음수"
        "ja" -> "総歩数"
        "zh" -> "总步数"
        "es" -> "Pasos totales"
        else -> "Total Steps"
    }

    fun totalExp(): String = when (getLang()) {
        "ko" -> "총 경험치"
        "ja" -> "総経験値"
        "zh" -> "总经验"
        "es" -> "EXP total"
        else -> "Total EXP"
    }

    fun currentPersonality(): String = when (getLang()) {
        "ko" -> "현재 성격"
        "ja" -> "現在の性格"
        "zh" -> "当前性格"
        "es" -> "Personalidad actual"
        else -> "Current Personality"
    }

    fun changeToOtherPet(): String = when (getLang()) {
        "ko" -> "다른 펫으로 변경"
        "ja" -> "他のペットに変更"
        "zh" -> "更换为其他宠物"
        "es" -> "Cambiar a otra mascota"
        else -> "Change to another pet"
    }

    fun levelKept(): String = when (getLang()) {
        "ko" -> "레벨은 유지됩니다"
        "ja" -> "レベルは維持されます"
        "zh" -> "等级将保留"
        "es" -> "El nivel se mantiene"
        else -> "Level is kept"
    }

    fun petChanged(): String = when (getLang()) {
        "ko" -> "펫이 변경되었습니다!"
        "ja" -> "ペットが変更されました！"
        "zh" -> "宠物已更改！"
        "es" -> "¡Mascota cambiada!"
        else -> "Pet changed!"
    }

    fun friend(): String = when (getLang()) {
        "ko" -> "친구"
        "ja" -> "フレンド"
        "zh" -> "朋友"
        "es" -> "Amigo"
        else -> "Friend"
    }

    fun petName(): String = when (getLang()) {
        "ko" -> "펫 이름"
        "ja" -> "ペット名"
        "zh" -> "宠物名字"
        "es" -> "Nombre de mascota"
        else -> "Pet Name"
    }

    fun cancel(): String = when (getLang()) {
        "ko" -> "취소"
        "ja" -> "キャンセル"
        "zh" -> "取消"
        "es" -> "Cancelar"
        else -> "Cancel"
    }

    fun changeWithPrice(price: String?): String {
        val displayPrice = price ?: defaultPrice()
        return when (getLang()) {
            "ko" -> "변경 ($displayPrice)"
            "ja" -> "変更 ($displayPrice)"
            "zh" -> "更换 ($displayPrice)"
            "es" -> "Cambiar ($displayPrice)"
            else -> "Change ($displayPrice)"
        }
    }

    fun priceOnly(price: String?): String = price ?: defaultPrice()

    private fun defaultPrice(): String = when (getLang()) {
        "ko" -> "₩1,000"
        "ja" -> "¥100"
        "zh" -> "¥7"
        "es" -> "\$0.99"
        else -> "\$0.99"
    }

    fun restoreToDefault(): String = when (getLang()) {
        "ko" -> "기본으로 복원"
        "ja" -> "デフォルトに復元"
        "zh" -> "恢复默认"
        "es" -> "Restaurar a predeterminado"
        else -> "Restore to Default"
    }

    fun changeAppearance(): String = when (getLang()) {
        "ko" -> "외형 변경"
        "ja" -> "外見変更"
        "zh" -> "更改外观"
        "es" -> "Cambiar apariencia"
        else -> "Change Appearance"
    }

    fun restoreAppearance(): String = when (getLang()) {
        "ko" -> "외형 복원"
        "ja" -> "外見復元"
        "zh" -> "恢复外观"
        "es" -> "Restaurar apariencia"
        else -> "Restore Appearance"
    }

    fun restoreAppearanceConfirm(): String = when (getLang()) {
        "ko" -> "펫의 외형을 기본으로 복원하시겠습니까?"
        "ja" -> "ペットの外見をデフォルトに復元しますか？"
        "zh" -> "确定要将宠物外观恢复为默认吗？"
        "es" -> "¿Restaurar la apariencia de la mascota a predeterminado?"
        else -> "Restore pet appearance to default?"
    }

    fun changeAppearanceConfirm(stageName: String): String = when (getLang()) {
        "ko" -> "펫의 외형을 ${stageName}으로 변경하시겠습니까?"
        "ja" -> "ペットの外見を${stageName}に変更しますか？"
        "zh" -> "确定要将宠物外观更改为${stageName}吗？"
        "es" -> "¿Cambiar la apariencia de la mascota a $stageName?"
        else -> "Change pet appearance to $stageName?"
    }

    fun confirm(): String = when (getLang()) {
        "ko" -> "확인"
        "ja" -> "確認"
        "zh" -> "确认"
        "es" -> "Confirmar"
        else -> "Confirm"
    }

    fun owned(count: Int): String = when (getLang()) {
        "ko" -> "보유중 ($count)"
        "ja" -> "所持中 ($count)"
        "zh" -> "已拥有 ($count)"
        "es" -> "Poseído ($count)"
        else -> "Owned ($count)"
    }

    fun notOwned(count: Int): String = when (getLang()) {
        "ko" -> "미보유 ($count)"
        "ja" -> "未所持 ($count)"
        "zh" -> "未拥有 ($count)"
        "es" -> "No poseído ($count)"
        else -> "Not Owned ($count)"
    }

    fun changeSkin(): String = when (getLang()) {
        "ko" -> "스킨 변경"
        "ja" -> "スキン変更"
        "zh" -> "更换皮肤"
        "es" -> "Cambiar skin"
        else -> "Change Skin"
    }

    fun changeSkinConfirm(): String = when (getLang()) {
        "ko" -> "이 스킨으로 변경하시겠습니까?"
        "ja" -> "このスキンに変更しますか？"
        "zh" -> "确定要更换为此皮肤吗？"
        "es" -> "¿Cambiar a este skin?"
        else -> "Change to this skin?"
    }

    fun change(): String = when (getLang()) {
        "ko" -> "변경"
        "ja" -> "変更"
        "zh" -> "更换"
        "es" -> "Cambiar"
        else -> "Change"
    }

    fun unlockCondition(): String = when (getLang()) {
        "ko" -> "해금 조건"
        "ja" -> "解除条件"
        "zh" -> "解锁条件"
        "es" -> "Condición de desbloqueo"
        else -> "Unlock Condition"
    }

    fun defaultSkin(): String = when (getLang()) {
        "ko" -> "기본"
        "ja" -> "デフォルト"
        "zh" -> "默认"
        "es" -> "Predeterminado"
        else -> "Default"
    }

    fun stepsCondition(steps: Long): String = when (getLang()) {
        "ko" -> "${steps}보"
        "ja" -> "${steps}歩"
        "zh" -> "${steps}步"
        "es" -> "$steps pasos"
        else -> "$steps steps"
    }

    fun streakCondition(days: Int): String = when (getLang()) {
        "ko" -> "${days}일 연속"
        "ja" -> "${days}日連続"
        "zh" -> "连续${days}天"
        "es" -> "$days días seguidos"
        else -> "$days day streak"
    }

    fun levelCondition(level: Int): String = when (getLang()) {
        "ko" -> "Lv.$level"
        "ja" -> "Lv.$level"
        "zh" -> "Lv.$level"
        "es" -> "Lv.$level"
        else -> "Lv.$level"
    }

    fun challengeCondition(category: String, count: Int): String = when (getLang()) {
        "ko" -> "$category ${count}회"
        "ja" -> "$category ${count}回"
        "zh" -> "$category ${count}次"
        "es" -> "$category $count veces"
        else -> "$category $count times"
    }

    fun defaultSkinProvided(): String = when (getLang()) {
        "ko" -> "기본 제공 스킨"
        "ja" -> "デフォルト提供スキン"
        "zh" -> "默认提供皮肤"
        "es" -> "Skin predeterminado"
        else -> "Default skin"
    }

    fun earnedBySteps(steps: Long): String = when (getLang()) {
        "ko" -> "총 ${steps}보 달성으로 획득"
        "ja" -> "総${steps}歩達成で獲得"
        "zh" -> "累计${steps}步后获得"
        "es" -> "Obtenido con $steps pasos totales"
        else -> "Earned by $steps total steps"
    }

    fun earnedByStreak(days: Int): String = when (getLang()) {
        "ko" -> "${days}일 연속 달성으로 획득"
        "ja" -> "${days}日連続達成で獲得"
        "zh" -> "连续${days}天达成后获得"
        "es" -> "Obtenido con $days días seguidos"
        else -> "Earned by $days day streak"
    }

    fun earnedByLevel(lvl: Int): String = when (getLang()) {
        "ko" -> "레벨 $lvl 달성으로 획득"
        "ja" -> "レベル${lvl}達成で獲得"
        "zh" -> "达到等级$lvl 后获得"
        "es" -> "Obtenido al nivel $lvl"
        else -> "Earned at level $lvl"
    }

    fun earnedByEvent(eventId: String): String = when (getLang()) {
        "ko" -> "이벤트 '$eventId'로 획득"
        "ja" -> "イベント '$eventId' で獲得"
        "zh" -> "通过活动 '$eventId' 获得"
        "es" -> "Obtenido en evento '$eventId'"
        else -> "Earned by event '$eventId'"
    }

    fun earnedByChallenge(category: String, count: Int): String = when (getLang()) {
        "ko" -> "$category ${count}회 완료로 획득"
        "ja" -> "$category ${count}回完了で獲得"
        "zh" -> "完成 $category ${count}次后获得"
        "es" -> "Obtenido completando $category $count veces"
        else -> "Earned by completing $category $count times"
    }

    fun defaultSkinDescription(): String = when (getLang()) {
        "ko" -> "기본 제공되는 스킨입니다."
        "ja" -> "デフォルトで提供されるスキンです。"
        "zh" -> "这是默认提供的皮肤。"
        "es" -> "Este es el skin predeterminado."
        else -> "This is the default skin."
    }

    fun stepsUnlockDetail(required: Long, current: Long): String = when (getLang()) {
        "ko" -> "총 ${required}보 걸으면 해금됩니다.\n현재: ${current}보 / ${required}보"
        "ja" -> "総${required}歩歩くと解除されます。\n現在: ${current}歩 / ${required}歩"
        "zh" -> "累计走${required}步后解锁。\n当前: ${current}步 / ${required}步"
        "es" -> "Se desbloquea con $required pasos totales.\nActual: $current / $required pasos"
        else -> "Unlocks at $required total steps.\nCurrent: $current / $required steps"
    }

    fun streakUnlockDetail(required: Int, current: Int): String = when (getLang()) {
        "ko" -> "${required}일 연속 목표 달성 시 해금됩니다.\n현재: ${current}일 연속"
        "ja" -> "${required}日連続目標達成で解除されます。\n現在: ${current}日連続"
        "zh" -> "连续${required}天达成目标后解锁。\n当前: 连续${current}天"
        "es" -> "Se desbloquea con $required días seguidos.\nActual: $current días seguidos"
        else -> "Unlocks at $required day streak.\nCurrent: $current day streak"
    }

    fun levelUnlockDetail(required: Int, current: Int): String = when (getLang()) {
        "ko" -> "펫 레벨 $required 달성 시 해금됩니다.\n현재 레벨: Lv.$current"
        "ja" -> "ペットレベル${required}達成で解除されます。\n現在のレベル: Lv.$current"
        "zh" -> "宠物达到等级$required 后解锁。\n当前等级: Lv.$current"
        "es" -> "Se desbloquea al nivel $required.\nNivel actual: Lv.$current"
        else -> "Unlocks at pet level $required.\nCurrent level: Lv.$current"
    }

    fun eventUnlockDetail(eventId: String): String = when (getLang()) {
        "ko" -> "특별 이벤트 기간에 해금됩니다.\n이벤트: $eventId"
        "ja" -> "特別イベント期間中に解除されます。\nイベント: $eventId"
        "zh" -> "在特别活动期间解锁。\n活动: $eventId"
        "es" -> "Se desbloquea durante el evento.\nEvento: $eventId"
        else -> "Unlocks during special event.\nEvent: $eventId"
    }

    fun challengeUnlockDetail(category: String, required: Int, current: Int): String = when (getLang()) {
        "ko" -> "$category 챌린지 ${required}회 완료 시 해금됩니다.\n현재: ${current}회 / ${required}회"
        "ja" -> "${category}チャレンジ${required}回完了で解除されます。\n現在: ${current}回 / ${required}回"
        "zh" -> "完成$category 挑战${required}次后解锁。\n当前: ${current}次 / ${required}次"
        "es" -> "Se desbloquea completando $category $required veces.\nActual: $current / $required veces"
        else -> "Unlocks by completing $category $required times.\nCurrent: $current / $required times"
    }
}

/**
 * 펫 관리 화면
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsPetScreen(
    preferenceManager: PreferenceManager?,
    repository: UserDataRepository,
    hapticManager: HapticManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val kenneyFont = rememberKenneyFont()
    val scope = rememberCoroutineScope()

    // 펫 정보 (상태로 관리 - 변경 시 UI 업데이트됨)
    var petTypeV2 by remember { mutableStateOf(preferenceManager?.getPetTypeV2()) }
    var petName by remember { mutableStateOf(preferenceManager?.getPetNameV2() ?: "친구") }
    var petLevel by remember { mutableStateOf(preferenceManager?.getPetLevelV2() ?: PetLevel()) }
    var petTotalSteps by remember { mutableStateOf(preferenceManager?.getPetTotalSteps() ?: 0L) }

    // 펫 정보 새로고침 트리거
    var refreshTrigger by remember { mutableStateOf(0) }

    // refreshTrigger가 변경되면 펫 정보 다시 로드
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            android.util.Log.d("SettingsPetScreen", "🔄 Refreshing pet info (trigger=$refreshTrigger)")
            petTypeV2 = preferenceManager?.getPetTypeV2()
            petName = preferenceManager?.getPetNameV2() ?: "친구"
            petLevel = preferenceManager?.getPetLevelV2() ?: PetLevel()
            petTotalSteps = preferenceManager?.getPetTotalSteps() ?: 0L
            android.util.Log.d("SettingsPetScreen", "✅ Refreshed: petTypeV2=${petTypeV2?.name}, petName=$petName")
        }
    }

    // 펫 변경 다이얼로그 상태
    var showPetChangeDialog by remember { mutableStateOf(false) }

    // BillingManager
    var billingManager by remember { mutableStateOf<BillingManager?>(null) }

    // 펫 변경 가격 (Google Play에서 조회)
    var petChangePrice by remember { mutableStateOf<String?>(null) }

    // 가격 조회
    LaunchedEffect(billingManager) {
        billingManager?.let { billing ->
            petChangePrice = billing.getPetChangePrice()
        }
    }

    // 펫 변경 결제 시작
    fun startPetChangePurchase(newPetType: PetTypeV2, newPetName: String) {
        android.util.Log.d("SettingsPetScreen", "🛒 Starting purchase for: ${newPetType.name}, name: $newPetName")
        android.util.Log.d("SettingsPetScreen", "📌 Current UI state BEFORE purchase: petTypeV2=${petTypeV2?.name}, petName=$petName")

        preferenceManager?.savePendingPetChange(newPetType.name, newPetName)
        showPetChangeDialog = false

        val activity = context as? android.app.Activity ?: return

        scope.launch {
            kotlinx.coroutines.delay(100)

            billingManager = BillingManager(
                context = context,
                onPurchaseSuccess = { purchase ->
                    // ✅ 펫 변경 purchase인지 확인 (구독 purchase는 무시)
                    if (!purchase.products.contains(BillingManager.PET_CHANGE_PRODUCT_ID)) {
                        android.util.Log.d("SettingsPetScreen", "⚠️ Ignoring non-pet-change purchase: ${purchase.products}")
                        return@BillingManager
                    }

                    android.util.Log.d("SettingsPetScreen", "✅ Pet change purchase confirmed: ${purchase.orderId}")

                    val petTypeName = preferenceManager?.getPendingPetType()
                    val savedPetName = preferenceManager?.getPendingPetName() ?: ""

                    if (petTypeName != null) {
                        try {
                            val petType = try { PetTypeV2.valueOf(petTypeName) } catch (e: Exception) { PetTypeV2.SHIBA }
                            preferenceManager?.savePetTypeV2(petType)
                            preferenceManager?.savePetNameV2(savedPetName)

                            val existingLevel = preferenceManager?.getPetLevelV2()
                            if (existingLevel == null || existingLevel.level == 0) {
                                preferenceManager?.savePetLevelV2(PetLevel(level = 1, currentExp = 0, totalExp = 0))
                            }

                            val app = context.applicationContext as WalkorWaitApp
                            app.userDataRepository.savePetInfo(petTypeName, savedPetName)
                            app.userDataRepository.trackPetChangePurchase(petTypeName, savedPetName)
                            AnalyticsManager.trackPurchaseCompleted("pet_change", 2500.0)

                            android.util.Log.d("SettingsPetScreen", "✅ Payment SUCCESS! Updating UI now...")
                            android.util.Log.d("SettingsPetScreen", "📌 UI state BEFORE update: petTypeV2=${petTypeV2?.name}, petName=$petName")

                            // ✅ UI 즉시 업데이트 (결제 완료 후에만!) - refreshTrigger로 리컴포지션 트리거
                            refreshTrigger++

                            android.util.Log.d("SettingsPetScreen", "📌 Triggered refresh (refreshTrigger=$refreshTrigger)")

                            StepWidgetProvider.updateAllWidgets(context.applicationContext)

                            // Toast는 메인 스레드에서만
                            scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                Toast.makeText(context, SettingsPetStrings.petChanged(), Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("SettingsPetScreen", "Pet change failed: ${e.message}")
                        }
                    }
                    preferenceManager?.clearPendingPetChange()
                },
                onPurchaseFailure = { error ->
                    android.util.Log.e("SettingsPetScreen", "❌ Payment FAILED! UI should NOT change. Error: $error")
                    android.util.Log.d("SettingsPetScreen", "📌 UI state (should be unchanged): petTypeV2=${petTypeV2?.name}, petName=$petName")
                    preferenceManager?.clearPendingPetChange()
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            )
            billingManager?.startPetChangePurchase(activity)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 헤더
            SettingsHeader(
                title = "pet",
                kenneyFont = kenneyFont,
                onBack = {
                    hapticManager.click()
                    onBack()
                }
            )

            // 스크롤 콘텐츠
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
                    .navigationBarsPadding()
            ) {
                // ========== 현재 펫 ==========
                RetroSectionTitle(SettingsPetStrings.currentPet(), kenneyFont)

                // 디버그 모드: 길게 누르면 레벨업
                val isDebug = BuildConfig.DEBUG
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(MockupColors.Border.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .combinedClickable(
                            onClick = { },
                            onLongClick = {
                                if (isDebug) {
                                    hapticManager.success()
                                    // 레벨업 (경험치 +1000)
                                    val currentLevel = preferenceManager?.getPetLevelV2() ?: PetLevel()
                                    val newLevel = currentLevel.addExp(1000)
                                    preferenceManager?.savePetLevelV2(newLevel)
                                    petLevel = newLevel
                                    android.widget.Toast.makeText(context, "DEBUG: +1000 EXP → Lv.${newLevel.level}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        .padding(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 펫 스프라이트 (스킨 + 오버라이드 반영)
                        petTypeV2?.let { petType ->
                            val effectiveStage = preferenceManager?.getEffectiveDisplayStage() ?: petLevel.stage
                            val equipmentState = preferenceManager?.getEquipmentState() ?: EquipmentState()

                            PetSpriteV2WithEquipment(
                                petType = petType,
                                stage = effectiveStage,
                                animationType = PetAnimationTypeV2.IDLE,
                                equipmentState = equipmentState,
                                size = 100.dp,
                                monochrome = true,
                                showGlow = true
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // 펫 이름 (칭호 포함)
                        val displayName = remember(petName) {
                            val challengeManager = ChallengeManager.getInstance(context)
                            challengeManager.getPetNameWithTitle(petName)
                        }
                        Text(
                            text = displayName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary,
                            fontFamily = kenneyFont,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // 레벨 & 단계 (실제 성장 단계 표시)
                        val displayStage = preferenceManager?.getEffectiveDisplayStage() ?: petLevel.stage
                        Text(
                            text = "Lv.${petLevel.level} (${displayStage.getLocalizedName()})",
                            fontSize = 15.sp,
                            color = MockupColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = kenneyFont
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 경험치 바
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "EXP",
                                    fontSize = 12.sp,
                                    color = MockupColors.TextSecondary
                                )
                                Text(
                                    text = "${(petLevel.expProgress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    color = MockupColors.TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .border(2.dp, MockupColors.Border, RoundedCornerShape(4.dp))
                                    .background(Color.White, RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(petLevel.expProgress)
                                        .background(MockupColors.TextPrimary, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ========== 성장 단계 미리보기 ==========
                EvolutionPreviewSection(
                    petTypeV2 = petTypeV2,
                    petLevel = petLevel,
                    kenneyFont = kenneyFont
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ========== 칭호 선택 ==========
                TitleSelectionSection(
                    kenneyFont = kenneyFont,
                    hapticManager = hapticManager
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ========== 펫 스킨 ==========
                SkinManagementSection(
                    petTypeV2 = petTypeV2,
                    petLevel = petLevel,
                    kenneyFont = kenneyFont,
                    hapticManager = hapticManager
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ========== 펫 통계 ==========
                RetroSectionTitle(SettingsPetStrings.petStats(), kenneyFont)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                        .background(MockupColors.CardBackground, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        StatRow(SettingsPetStrings.totalSteps(), "${petTotalSteps.formatWithComma()}", kenneyFont)
                        Spacer(modifier = Modifier.height(8.dp))
                        StatRow(SettingsPetStrings.totalExp(), "${petLevel.totalExp} EXP", kenneyFont)
                        Spacer(modifier = Modifier.height(8.dp))
                        StatRow(SettingsPetStrings.currentPersonality(), petTypeV2?.personality?.description ?: "-", kenneyFont)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ========== 펫 변경 ==========
                RetroSectionTitle(SettingsPetStrings.changePet(), kenneyFont)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                        .background(MockupColors.CardBackground, RoundedCornerShape(10.dp))
                        .clickable {
                            hapticManager.click()
                            showPetChangeDialog = true
                        }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = SettingsPetStrings.changeToOtherPet(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                            Text(
                                text = SettingsPetStrings.levelKept(),
                                fontSize = 13.sp,
                                color = MockupColors.TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .border(2.dp, MockupColors.Border, RoundedCornerShape(6.dp))
                                .background(MockupColors.Background, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = SettingsPetStrings.priceOnly(petChangePrice),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary,
                                fontFamily = kenneyFont
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // 펫 변경 다이얼로그
        if (showPetChangeDialog) {
            PetChangeDialog(
                currentPetType = petTypeV2?.name,
                currentPetName = petName,
                price = petChangePrice,
                onDismiss = { showPetChangeDialog = false },
                onConfirm = { newType, newName ->
                    startPetChangePurchase(newType, newName)
                },
                hapticManager = hapticManager
            )
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    kenneyFont: androidx.compose.ui.text.font.FontFamily
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MockupColors.TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary,
            fontFamily = kenneyFont
        )
    }
}

private fun Long.formatWithComma(): String {
    return String.format("%,d", this)
}

/**
 * 펫 변경 다이얼로그
 */
@Composable
private fun PetChangeDialog(
    currentPetType: String?,
    currentPetName: String,
    price: String?,
    onDismiss: () -> Unit,
    onConfirm: (PetTypeV2, String) -> Unit,
    hapticManager: HapticManager
) {
    val kenneyFont = rememberKenneyFont()
    var selectedPet by remember { mutableStateOf<PetTypeV2?>(currentPetType?.let {
        try { PetTypeV2.valueOf(it) } catch (e: Exception) { null }
    }) }
    var petName by remember { mutableStateOf(currentPetName) }

    val petTypes = PetTypeV2.entries.toList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {}
                .border(4.dp, MockupColors.Border, RoundedCornerShape(16.dp))
                .background(MockupColors.Background, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = SettingsPetStrings.changePet(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary,
                    fontFamily = kenneyFont
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 펫 선택 그리드 (2x3)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in petTypes.chunked(3)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (pet in row) {
                                val isSelected = selectedPet == pet
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .border(
                                            3.dp,
                                            if (isSelected) MockupColors.TextPrimary else MockupColors.Border,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .background(
                                            if (isSelected) MockupColors.Border.copy(alpha = 0.15f) else MockupColors.CardBackground,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            hapticManager.click()
                                            selectedPet = pet
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // 스프라이트만 2배 크기로 가운데 정렬 (이름 숨김)
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        PetSpriteV2WithGlow(
                                            petType = pet,
                                            stage = PetGrowthStage.BABY,
                                            animationType = PetAnimationTypeV2.IDLE,
                                            size = 72.dp,  // 2배 크기 (48 → 72, 박스 내에서 적절한 크기)
                                            monochrome = true,
                                            showGlow = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 이름 입력
                OutlinedTextField(
                    value = petName,
                    onValueChange = { if (it.length <= 10) petName = it },
                    label = { Text("펫 이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                            .clickable {
                                hapticManager.click()
                                onDismiss()
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = SettingsPetStrings.cancel(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, MockupColors.TextPrimary, RoundedCornerShape(8.dp))
                            .background(MockupColors.TextPrimary, RoundedCornerShape(8.dp))
                            .clickable(enabled = selectedPet != null && petName.isNotBlank()) {
                                hapticManager.success()
                                selectedPet?.let { onConfirm(it, petName) }
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = SettingsPetStrings.changeWithPrice(price),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ========== Evolution Preview Section - Helper Functions ==========

/**
 * 성장 단계 도달 상태
 */
private enum class EvolutionStageStatus {
    PASSED,   // 이미 지나간 단계 (현재 레벨 > 단계 최대 레벨)
    CURRENT,  // 현재 단계 (현재 레벨이 단계 범위 내)
    LOCKED    // 아직 도달 안함 (현재 레벨 < 단계 최소 레벨)
}

/**
 * 단계 스타일 데이터
 */
private data class StageStyle(
    val borderColor: Color,
    val borderWidth: Dp,
    val backgroundColor: Color,
    val textColor: Color,
    val spriteAlpha: Float,
    val showGlow: Boolean
)

/**
 * 현재 레벨에서 해당 단계의 상태 계산
 */
private fun getStageStatus(
    stage: PetGrowthStage,
    currentLevel: Int
): EvolutionStageStatus {
    return when {
        currentLevel > stage.levelRange.last -> EvolutionStageStatus.PASSED
        currentLevel in stage.levelRange -> EvolutionStageStatus.CURRENT
        else -> EvolutionStageStatus.LOCKED
    }
}

/**
 * 상태별 스타일 가져오기 (모노크롬)
 * @param isSelected 선택된 카드인지 여부 (오버라이드 선택)
 */
private fun getStageStyle(
    status: EvolutionStageStatus,
    isSelected: Boolean = false
): StageStyle {
    return when {
        // 선택된 카드는 검정 테두리 + 글로우 (CURRENT 스타일)
        isSelected -> StageStyle(
            borderColor = MockupColors.TextPrimary,
            borderWidth = 3.dp,
            backgroundColor = MockupColors.Border.copy(alpha = 0.15f),
            textColor = MockupColors.TextPrimary,
            spriteAlpha = 1.0f,
            showGlow = true
        )
        status == EvolutionStageStatus.PASSED -> StageStyle(
            borderColor = MockupColors.Border,        // 검정
            borderWidth = 2.dp,
            backgroundColor = MockupColors.CardBackground,  // 연한 회색
            textColor = MockupColors.TextPrimary,
            spriteAlpha = 1.0f,
            showGlow = false
        )
        status == EvolutionStageStatus.CURRENT -> StageStyle(
            borderColor = MockupColors.Border,        // 검정
            borderWidth = 3.dp,                        // 더 두껍게 (현재 강조)
            backgroundColor = MockupColors.CardBackground,
            textColor = MockupColors.TextPrimary,
            spriteAlpha = 1.0f,
            showGlow = true                            // 글로우로 현재 강조
        )
        else -> StageStyle(  // LOCKED
            borderColor = MockupColors.Border.copy(alpha = 0.3f),  // 반투명 검정
            borderWidth = 2.dp,
            backgroundColor = Color(0xFFFAFAFA),       // 아주 연한 회색
            textColor = MockupColors.TextMuted,
            spriteAlpha = 0.4f,
            showGlow = false
        )
    }
}

/**
 * 성장 단계별 레벨 범위 텍스트
 */
private fun getLevelRangeText(stage: PetGrowthStage): String {
    return when (stage) {
        PetGrowthStage.BABY -> "Lv.1-10"
        PetGrowthStage.TEEN -> "Lv.11-20"
        PetGrowthStage.ADULT -> "Lv.21+"
        else -> ""
    }
}

/**
 * 상태 아이콘 표시 (유니코드 텍스트 심볼 + PNG 아이콘)
 */
@Composable
private fun StatusIcon(status: EvolutionStageStatus) {
    // 텍스트 스타일 강제 (이모지 대신 흑백 텍스트로 렌더링)
    val textSelector = "\uFE0E"

    when (status) {
        EvolutionStageStatus.PASSED -> {
            Text(
                text = "✓$textSelector",  // 체크마크 (흑백)
                fontSize = 16.sp,
                color = MockupColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        EvolutionStageStatus.LOCKED -> {
            // IconGodotNode의 icon_lock.png 사용
            DrawableIcon(
                iconName = "icon_lock",
                size = 14.dp,
                tint = MockupColors.TextMuted
            )
        }
        EvolutionStageStatus.CURRENT -> {
            // 현재 단계는 아이콘 없음 (테두리 + 글로우로 강조)
        }
    }
}

/**
 * 개별 성장 단계 카드
 */
@Composable
private fun EvolutionStageCard(
    petType: PetTypeV2?,
    stage: PetGrowthStage,
    status: EvolutionStageStatus,
    isSelected: Boolean = false,  // 선택 상태
    onClick: () -> Unit = {},      // 클릭 핸들러
    modifier: Modifier = Modifier
) {
    val style = getStageStyle(status, isSelected)

    Box(
        modifier = modifier
            .aspectRatio(0.9f)  // 더 납작한 카드
            .border(style.borderWidth, style.borderColor, RoundedCornerShape(10.dp))
            .background(style.backgroundColor, RoundedCornerShape(10.dp))
            .clickable(
                enabled = status == EvolutionStageStatus.PASSED || status == EvolutionStageStatus.CURRENT,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // 펫 스프라이트
            petType?.let {
                PetSpriteV2WithGlow(
                    petType = it,
                    stage = stage,
                    animationType = PetAnimationTypeV2.IDLE,
                    size = 60.dp,
                    monochrome = true,
                    showGlow = style.showGlow,
                    modifier = Modifier.alpha(style.spriteAlpha)
                )
            }

            // 단계 이름 + 레벨 범위
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stage.getLocalizedName(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = style.textColor
                )
                Text(
                    text = getLevelRangeText(stage),
                    fontSize = 11.sp,
                    color = style.textColor.copy(alpha = 0.7f)
                )
            }

            // 상태 아이콘
            StatusIcon(status)
        }

        // 선택 표시 (우상단 체크마크)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(MockupColors.TextPrimary, CircleShape)
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * 성장 단계 미리보기 섹션 (BABY-TEEN-ADULT)
 * 클릭하여 외형 오버라이드 가능
 */
@Composable
private fun EvolutionPreviewSection(
    petTypeV2: PetTypeV2?,
    petLevel: PetLevel,
    kenneyFont: FontFamily
) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }

    // 현재 오버라이드된 stage 가져오기
    var displayStageOverride by remember {
        mutableStateOf(prefs.getDisplayStageOverride())
    }

    // 확인 다이얼로그 상태
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingStage by remember { mutableStateOf<PetGrowthStage?>(null) }
    var isRestoreAction by remember { mutableStateOf(false) }

    Column {
        RetroSectionTitle(SettingsPetStrings.growthStage(), kenneyFont)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                PetGrowthStage.BABY,
                PetGrowthStage.TEEN,
                PetGrowthStage.ADULT
            ).forEach { stage ->
                val status = getStageStatus(stage, petLevel.level)
                val isSelected = displayStageOverride == stage

                EvolutionStageCard(
                    petType = petTypeV2,
                    stage = stage,
                    status = status,
                    isSelected = isSelected,
                    onClick = {
                        when (status) {
                            EvolutionStageStatus.PASSED -> {
                                // PASSED 클릭 → 확인 다이얼로그 표시
                                pendingStage = stage
                                isRestoreAction = false
                                showConfirmDialog = true
                            }
                            EvolutionStageStatus.CURRENT -> {
                                // CURRENT 클릭 → 복원 확인 다이얼로그 표시
                                pendingStage = null
                                isRestoreAction = true
                                showConfirmDialog = true
                            }
                            else -> {}  // LOCKED는 아무 동작 없음
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // "기본으로 복원" 버튼 (오버라이드가 설정된 경우만 표시)
        if (displayStageOverride != null) {
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    pendingStage = null
                    isRestoreAction = true
                    showConfirmDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFAAAAAA)
                )
            ) {
                Text(
                    text = SettingsPetStrings.restoreToDefault(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    // 확인 다이얼로그
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = if (isRestoreAction) "외형 복원" else "외형 변경",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val message = if (isRestoreAction) {
                    SettingsPetStrings.restoreAppearanceConfirm()
                } else {
                    val stageName = pendingStage?.getLocalizedName() ?: ""
                    SettingsPetStrings.changeAppearanceConfirm(stageName)
                }
                Text(text = message)
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isRestoreAction) {
                            // 기본으로 복원
                            prefs.clearDisplayStageOverride()
                            displayStageOverride = null
                        } else {
                            // 외형 변경
                            pendingStage?.let { stage ->
                                prefs.saveDisplayStageOverride(stage)
                                displayStageOverride = stage
                            }
                        }
                        updateAllWidgetsAfterOverride(context)
                        showConfirmDialog = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFAAAAAA)
                    )
                ) {
                    Text("취소")
                }
            }
        )
    }
}

/**
 * 외형 오버라이드 변경 후 모든 위젯 업데이트
 */
private fun updateAllWidgetsAfterOverride(context: Context) {
    StepWidgetProvider.updateAllWidgets(context)
    PetWidget2x2Provider.updateAllWidgets(context)
    WeatherWidgetProvider.updateAllWidgets(context)
    SudokuWidgetProvider.updateAllWidgets(context)
    QuoteWidgetProvider.updateAllWidgets(context)
    TravelPhraseWidgetProvider.updateAllWidgets(context)
    TravelPhraseJapaneseWidgetProvider.updateAllWidgets(context)
    TravelPhraseChineseWidgetProvider.updateAllWidgets(context)
}

// ========== 펫 스킨 시스템 UI ==========

/**
 * 펫 스킨 관리 섹션
 */
@Composable
private fun SkinManagementSection(
    petTypeV2: PetTypeV2?,
    petLevel: PetLevel,
    kenneyFont: FontFamily,
    hapticManager: HapticManager
) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }

    // 펼침/접힘 상태
    var isExpanded by remember { mutableStateOf(false) }

    // 현재 스킨
    var currentSkinId by remember { mutableStateOf(prefs.getPetSkin()) }

    // 보유 스킨 목록 (기본 스킨은 항상 포함)
    var ownedSkins by remember {
        // 초기화 시 기본 스킨 보장
        prefs.initializeDefaultSkins()
        mutableStateOf(prefs.getOwnedSkins())
    }

    // 펫 정보
    val petStage = prefs.getEffectiveDisplayStage()

    // 확인 다이얼로그 상태 (보유 스킨 변경)
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingSkin by remember { mutableStateOf<PetSkin?>(null) }

    // 해금 조건 다이얼로그 상태 (잠긴 스킨 클릭)
    var showUnlockInfoDialog by remember { mutableStateOf(false) }
    var lockedSkinInfo by remember { mutableStateOf<PetSkin?>(null) }

    // 스킨 자동 해금 (기본 스킨 포함 + 조건 달성 스킨)
    LaunchedEffect(Unit) {
        // 기본 스킨 초기화 (항상 보유)
        prefs.initializeDefaultSkins()
        // 조건 달성 스킨 해금
        prefs.checkAndUnlockNewSkins()
        // 상태 갱신 (항상)
        ownedSkins = prefs.getOwnedSkins()
    }

    // 스킨 분류
    val ownedSkinsList = DefaultSkins.ALL_SKINS.filter { ownedSkins.contains(it.id) }
    val lockedSkinsList = DefaultSkins.ALL_SKINS.filter { !ownedSkins.contains(it.id) }

    Column {
        // 타이틀
        RetroSectionTitle(SettingsPetStrings.petSkin(), kenneyFont)

        Spacer(modifier = Modifier.height(8.dp))

        // ========== 보유중 섹션 (항상 표시) ==========
        Text(
            text = SettingsPetStrings.owned(ownedSkinsList.size),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // 보유 스킨 그리드 (3열, 고정 높이 계산)
        val ownedRows = (ownedSkinsList.size + 2) / 3
        val ownedGridHeight = (ownedRows * 120).coerceAtLeast(120).dp

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(ownedGridHeight),
            userScrollEnabled = false
        ) {
            items(ownedSkinsList) { skin ->
                val isSelected = currentSkinId == skin.id

                SkinItemCompact(
                    skin = skin,
                    isOwned = true,
                    isSelected = isSelected,
                    petTypeV2 = petTypeV2,
                    petStage = petStage,
                    onClick = {
                        if (currentSkinId != skin.id) {
                            hapticManager.lightClick()
                            pendingSkin = skin
                            showConfirmDialog = true
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ========== 미보유 섹션 (토글) ==========
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    hapticManager.lightClick()
                    isExpanded = !isExpanded
                }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = SettingsPetStrings.notOwned(lockedSkinsList.size),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextMuted
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isExpanded) "▼" else "▶",
                fontSize = 12.sp,
                color = MockupColors.TextMuted,
                fontFamily = kenneyFont
            )
        }

        // 미보유 스킨 그리드 (펼쳤을 때만)
        if (isExpanded) {
            val lockedRows = (lockedSkinsList.size + 2) / 3
            val lockedGridHeight = (lockedRows * 120).coerceAtLeast(120).dp

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(lockedGridHeight),
                userScrollEnabled = false
            ) {
                items(lockedSkinsList) { skin ->
                    SkinItemCompact(
                        skin = skin,
                        isOwned = false,
                        isSelected = false,
                        petTypeV2 = petTypeV2,
                        petStage = petStage,
                        onClick = {
                            hapticManager.lightClick()
                            lockedSkinInfo = skin
                            showUnlockInfoDialog = true
                        }
                    )
                }
            }
        }
    }

    // 스킨 변경 확인 다이얼로그
    if (showConfirmDialog && pendingSkin != null) {
        val skinToChange = pendingSkin!!
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                pendingSkin = null
            },
            title = {
                Text(
                    text = SettingsPetStrings.changeSkin(),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    // 스킨 미리보기
                    if (petTypeV2 != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val equipmentState = EquipmentState(
                                headId = null,
                                backgroundId = null,
                                colorId = skinToChange.id
                            )
                            PetSpriteV2WithEquipment(
                                petType = petTypeV2,
                                stage = petStage,
                                animationType = PetAnimationTypeV2.IDLE,
                                equipmentState = equipmentState,
                                size = 70.dp,
                                monochrome = true,
                                showGlow = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = skinToChange.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 획득 조건 표시 (기본 스킨은 "기본 제공", 나머지는 조건 표시)
                    Text(
                        text = getOwnedSkinConditionText(skinToChange.unlockCondition),
                        fontSize = 13.sp,
                        color = MockupColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = SettingsPetStrings.changeSkinConfirm(),
                        fontSize = 14.sp,
                        color = MockupColors.TextPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingSkin?.let { skin ->
                            android.util.Log.d("SkinManagement", "🎨 Saving skin: ${skin.id} (${skin.displayName})")
                            prefs.savePetSkin(skin.id)
                            currentSkinId = skin.id
                            android.util.Log.d("SkinManagement", "✅ Skin saved, currentSkinId now: $currentSkinId")
                            updateAllWidgetsAfterOverride(context)
                            android.util.Log.d("SkinManagement", "🔄 Widgets updated")
                        }
                        showConfirmDialog = false
                        pendingSkin = null
                    }
                ) {
                    Text("변경")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        pendingSkin = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFAAAAAA)
                    )
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 해금 조건 안내 다이얼로그 (잠긴 스킨 클릭 시)
    val currentLockedSkinInfo = lockedSkinInfo
    if (showUnlockInfoDialog && currentLockedSkinInfo != null) {
        AlertDialog(
            onDismissRequest = {
                showUnlockInfoDialog = false
                lockedSkinInfo = null
            },
            title = {
                Text(
                    text = currentLockedSkinInfo.displayName,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    // 스킨 미리보기
                    if (petTypeV2 != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val equipmentState = EquipmentState(
                                headId = null,
                                backgroundId = null,
                                colorId = currentLockedSkinInfo.id
                            )

                            PetSpriteV2WithEquipment(
                                petType = petTypeV2,
                                stage = petStage,
                                animationType = PetAnimationTypeV2.IDLE,
                                equipmentState = equipmentState,
                                size = 80.dp,
                                monochrome = true,
                                showGlow = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 해금 조건
                    Text(
                        text = SettingsPetStrings.unlockCondition(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = getUnlockConditionDetailText(currentLockedSkinInfo.unlockCondition, prefs),
                        fontSize = 14.sp,
                        color = MockupColors.TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnlockInfoDialog = false
                        lockedSkinInfo = null
                    }
                ) {
                    Text(SettingsPetStrings.confirm())
                }
            }
        )
    }
}

/**
 * 컴팩트한 스킨 아이템 (펫 관리 화면용) - ChallengeBox 스타일
 */
@Composable
private fun SkinItemCompact(
    skin: PetSkin,
    isOwned: Boolean,
    isSelected: Boolean,
    petTypeV2: PetTypeV2?,
    petStage: PetGrowthStage,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(
                width = 2.dp,
                color = if (isSelected) Color.Black else Color(0xFFDDDDDD),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                if (isSelected) Color(0xFFF5F5F5) else Color.White,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .alpha(if (isOwned) 1f else 0.5f)
            .padding(8.dp)
    ) {
        // 펫 미리보기 (중앙 상단)
        if (petTypeV2 != null && isOwned) {
            val equipmentState = EquipmentState(
                headId = null,
                backgroundId = null,
                colorId = skin.id
            )

            PetSpriteV2WithEquipment(
                petType = petTypeV2,
                stage = petStage,
                animationType = PetAnimationTypeV2.IDLE,
                equipmentState = equipmentState,
                size = 72.dp,
                monochrome = true,
                showGlow = false,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-12).dp)
            )
        } else if (!isOwned) {
            // 잠금 아이콘
            Text(
                text = "▣",
                fontSize = 40.sp,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-12).dp)
            )
        }

        // 스킨 이름 (하단 고정)
        Text(
            text = skin.getLocalizedDisplayName(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = if (!isOwned) (-14).dp else 0.dp)
        )

        // 해금 조건 (최하단)
        if (!isOwned) {
            Text(
                text = getUnlockConditionText(skin.unlockCondition),
                fontSize = 9.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 4.dp)
            )
        }
    }
}

// 다국어 헬퍼 (해금 조건 텍스트)
private object UnlockConditionStrings {
    private fun getLang(): String = java.util.Locale.getDefault().language

    fun defaultText(): String = when (getLang()) {
        "ko" -> "기본"; "ja" -> "デフォルト"; "zh" -> "默认"; "es" -> "Defecto"; else -> "Default"
    }

    fun steps(count: Long): String = when (getLang()) {
        "ko" -> "${count}보"; "ja" -> "${count}歩"; "zh" -> "${count}步"; "es" -> "${count} pasos"; else -> "${count} steps"
    }

    fun streak(days: Int): String = when (getLang()) {
        "ko" -> "${days}일 연속"; "ja" -> "${days}日連続"; "zh" -> "连续${days}天"; "es" -> "${days} días seguidos"; else -> "${days} day streak"
    }

    fun challengeCount(category: String, count: Int): String = when (getLang()) {
        "ko" -> "$category ${count}회"; "ja" -> "$category ${count}回"; "zh" -> "$category ${count}次"; "es" -> "$category ${count}x"; else -> "$category ${count}x"
    }

    fun defaultSkin(): String = when (getLang()) {
        "ko" -> "기본 제공 스킨"; "ja" -> "デフォルトスキン"; "zh" -> "默认皮肤"; "es" -> "Skin predeterminado"; else -> "Default skin"
    }

    fun stepsEarned(totalSteps: Long): String = when (getLang()) {
        "ko" -> "총 ${totalSteps}보 달성으로 획득"; "ja" -> "合計${totalSteps}歩達成で獲得"; "zh" -> "累计${totalSteps}步获得"; "es" -> "Obtenido con ${totalSteps} pasos"; else -> "Earned with ${totalSteps} steps"
    }

    fun streakEarned(days: Int): String = when (getLang()) {
        "ko" -> "${days}일 연속 달성으로 획득"; "ja" -> "${days}日連続達成で獲得"; "zh" -> "连续${days}天达成获得"; "es" -> "Obtenido con ${days} días seguidos"; else -> "Earned with ${days} day streak"
    }

    fun levelEarned(level: Int): String = when (getLang()) {
        "ko" -> "레벨 ${level} 달성으로 획득"; "ja" -> "レベル${level}達成で獲得"; "zh" -> "达到等级${level}获得"; "es" -> "Obtenido al nivel ${level}"; else -> "Earned at level ${level}"
    }

    fun eventEarned(eventId: String): String = when (getLang()) {
        "ko" -> "이벤트 '${eventId}'로 획득"; "ja" -> "イベント「${eventId}」で獲得"; "zh" -> "通过活动「${eventId}」获得"; "es" -> "Obtenido en evento '${eventId}'"; else -> "Earned from event '${eventId}'"
    }

    fun challengeCountEarned(category: String, count: Int): String = when (getLang()) {
        "ko" -> "$category ${count}회 완료로 획득"; "ja" -> "$category ${count}回完了で獲得"; "zh" -> "$category 完成${count}次获得"; "es" -> "Obtenido con ${count}x $category"; else -> "Earned with ${count}x $category"
    }

    fun defaultSkinDetail(): String = when (getLang()) {
        "ko" -> "기본 제공되는 스킨입니다."; "ja" -> "デフォルトで提供されるスキンです。"; "zh" -> "这是默认提供的皮肤。"; "es" -> "Este skin es predeterminado."; else -> "This is a default skin."
    }

    fun stepsDetailText(required: Long, current: Long): String = when (getLang()) {
        "ko" -> "총 ${required}보 걸으면 해금됩니다.\n현재: ${current}보 / ${required}보"
        "ja" -> "合計${required}歩で解放されます。\n現在: ${current}歩 / ${required}歩"
        "zh" -> "累计行走${required}步后解锁。\n当前: ${current}步 / ${required}步"
        "es" -> "Se desbloquea con ${required} pasos.\nActual: ${current} / ${required} pasos"
        else -> "Unlocks at ${required} steps.\nCurrent: ${current} / ${required} steps"
    }

    fun streakDetailText(required: Int, current: Int): String = when (getLang()) {
        "ko" -> "${required}일 연속 목표 달성 시 해금됩니다.\n현재: ${current}일 연속"
        "ja" -> "${required}日連続目標達成で解放されます。\n現在: ${current}日連続"
        "zh" -> "连续${required}天达成目标后解锁。\n当前: 连续${current}天"
        "es" -> "Se desbloquea con ${required} días seguidos.\nActual: ${current} días seguidos"
        else -> "Unlocks at ${required} day streak.\nCurrent: ${current} day streak"
    }

    fun levelDetailText(required: Int, current: Int): String = when (getLang()) {
        "ko" -> "펫 레벨 ${required} 달성 시 해금됩니다.\n현재 레벨: Lv.${current}"
        "ja" -> "ペットレベル${required}達成で解放されます。\n現在レベル: Lv.${current}"
        "zh" -> "宠物达到等级${required}后解锁。\n当前等级: Lv.${current}"
        "es" -> "Se desbloquea al nivel ${required}.\nNivel actual: Lv.${current}"
        else -> "Unlocks at pet level ${required}.\nCurrent level: Lv.${current}"
    }

    fun eventDetailText(eventId: String): String = when (getLang()) {
        "ko" -> "특별 이벤트 기간에 해금됩니다.\n이벤트: ${eventId}"
        "ja" -> "特別イベント期間中に解放されます。\nイベント: ${eventId}"
        "zh" -> "特别活动期间解锁。\n活动: ${eventId}"
        "es" -> "Se desbloquea durante evento especial.\nEvento: ${eventId}"
        else -> "Unlocks during special event.\nEvent: ${eventId}"
    }

    fun challengeDetailText(category: String, required: Int, current: Int): String = when (getLang()) {
        "ko" -> "$category 챌린지 ${required}회 완료 시 해금됩니다.\n현재: ${current}회 / ${required}회"
        "ja" -> "$category チャレンジ${required}回完了で解放されます。\n現在: ${current}回 / ${required}回"
        "zh" -> "完成${required}次$category 挑战后解锁。\n当前: ${current}次 / ${required}次"
        "es" -> "Se desbloquea con ${required}x $category.\nActual: ${current} / ${required}"
        else -> "Unlocks with ${required}x $category challenge.\nCurrent: ${current} / ${required}"
    }

    /** 카테고리 이름 로컬라이즈 */
    fun localizeCategory(category: String): String {
        val lang = getLang()
        return when (category) {
            "독서" -> when (lang) { "ko" -> "독서"; "ja" -> "読書"; "zh" -> "阅读"; "es" -> "Lectura"; else -> "Reading" }
            "명상" -> when (lang) { "ko" -> "명상"; "ja" -> "瞑想"; "zh" -> "冥想"; "es" -> "Meditación"; else -> "Meditation" }
            "공부" -> when (lang) { "ko" -> "공부"; "ja" -> "勉強"; "zh" -> "学习"; "es" -> "Estudio"; else -> "Study" }
            "운동" -> when (lang) { "ko" -> "운동"; "ja" -> "運動"; "zh" -> "运动"; "es" -> "Ejercicio"; else -> "Exercise" }
            "웰니스" -> when (lang) { "ko" -> "웰니스"; "ja" -> "ウェルネス"; "zh" -> "健康"; "es" -> "Bienestar"; else -> "Wellness" }
            else -> category
        }
    }
}

/**
 * 해금 조건을 텍스트로 변환 (간단한 버전)
 */
private fun getUnlockConditionText(condition: UnlockCondition): String {
    return when (condition) {
        is UnlockCondition.Default -> UnlockConditionStrings.defaultText()
        is UnlockCondition.Steps -> UnlockConditionStrings.steps(condition.totalSteps)
        is UnlockCondition.Streak -> UnlockConditionStrings.streak(condition.days)
        is UnlockCondition.Level -> "Lv.${condition.level}"
        is UnlockCondition.Event -> condition.eventId
        is UnlockCondition.ChallengeCount -> {
            val localizedCategory = UnlockConditionStrings.localizeCategory(condition.category)
            UnlockConditionStrings.challengeCount(localizedCategory, condition.count)
        }
    }
}

/**
 * 보유 스킨의 획득 조건 표시 (스킨 변경 다이얼로그용)
 */
private fun getOwnedSkinConditionText(condition: UnlockCondition): String {
    return when (condition) {
        is UnlockCondition.Default -> UnlockConditionStrings.defaultSkin()
        is UnlockCondition.Steps -> UnlockConditionStrings.stepsEarned(condition.totalSteps)
        is UnlockCondition.Streak -> UnlockConditionStrings.streakEarned(condition.days)
        is UnlockCondition.Level -> UnlockConditionStrings.levelEarned(condition.level)
        is UnlockCondition.Event -> UnlockConditionStrings.eventEarned(condition.eventId)
        is UnlockCondition.ChallengeCount -> {
            val localizedCategory = UnlockConditionStrings.localizeCategory(condition.category)
            UnlockConditionStrings.challengeCountEarned(localizedCategory, condition.count)
        }
    }
}

/**
 * 해금 조건 상세 텍스트 (현재 진행도 포함)
 */
private fun getUnlockConditionDetailText(condition: UnlockCondition, prefs: PreferenceManager): String {
    return when (condition) {
        is UnlockCondition.Default -> UnlockConditionStrings.defaultSkinDetail()
        is UnlockCondition.Steps -> {
            val current = prefs.getPetTotalSteps()
            val required = condition.totalSteps
            UnlockConditionStrings.stepsDetailText(required, current)
        }
        is UnlockCondition.Streak -> {
            val current = prefs.getStreak()
            val required = condition.days
            UnlockConditionStrings.streakDetailText(required, current)
        }
        is UnlockCondition.Level -> {
            val current = prefs.getPetLevelV2()?.level ?: 1
            val required = condition.level
            UnlockConditionStrings.levelDetailText(required, current)
        }
        is UnlockCondition.Event -> UnlockConditionStrings.eventDetailText(condition.eventId)
        is UnlockCondition.ChallengeCount -> {
            val localizedCategory = UnlockConditionStrings.localizeCategory(condition.category)
            val current = prefs.getChallengeCountByCategory(condition.category)
            val required = condition.count
            UnlockConditionStrings.challengeDetailText(localizedCategory, required, current)
        }
    }
}

// ========== 칭호 선택 시스템 UI ==========

/**
 * 칭호 선택 섹션
 */
@Composable
private fun TitleSelectionSection(
    kenneyFont: FontFamily,
    hapticManager: HapticManager
) {
    val context = LocalContext.current
    val challengeManager = remember { ChallengeManager.getInstance(context) }

    // 획득한 칭호 목록
    val unlockedTitles by challengeManager.unlockedTitles.collectAsState()

    // 현재 장착된 칭호
    val equippedTitle by challengeManager.equippedTitle.collectAsState()

    // 펼침/접힘 상태
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        // 타이틀
        RetroSectionTitle(SettingsPetStrings.petTitle(), kenneyFont)

        Spacer(modifier = Modifier.height(8.dp))

        if (unlockedTitles.isEmpty()) {
            // 획득한 칭호가 없을 때
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MockupColors.Border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 잠금 아이콘
                    DrawableIcon(
                        iconName = "icon_lock",
                        size = 32.dp,
                        tint = MockupColors.TextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = SettingsPetStrings.noTitlesYet(),
                        fontSize = 13.sp,
                        color = MockupColors.TextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 챌린지 하러 가기 버튼
                    Box(
                        modifier = Modifier
                            .border(2.dp, MockupColors.TextPrimary, RoundedCornerShape(8.dp))
                            .background(MockupColors.TextPrimary, RoundedCornerShape(8.dp))
                            .clickable {
                                hapticManager.click()
                                // MainActivity로 챌린지 화면 이동 요청
                                val activity = context as? android.app.Activity
                                activity?.let {
                                    val intent = android.content.Intent("com.moveoftoday.walkorwait.NAVIGATE_TO_CHALLENGE")
                                    context.sendBroadcast(intent)
                                    // 또는 뒤로가기로 메인 화면 복귀
                                    (context as? android.app.Activity)?.onBackPressed()
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = SettingsPetStrings.goToChallenge(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            // 획득한 칭호가 있을 때
            // 현재 장착 중인 칭호 표시
            if (equippedTitle != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, MockupColors.TextPrimary, RoundedCornerShape(12.dp))
                        .background(MockupColors.Border.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = SettingsPetStrings.equipped(),
                                fontSize = 11.sp,
                                color = MockupColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = equippedTitle!!.getLocalizedTitle(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary,
                                fontFamily = kenneyFont
                            )
                        }

                        // 해제 버튼
                        Box(
                            modifier = Modifier
                                .border(2.dp, MockupColors.Border, RoundedCornerShape(6.dp))
                                .clickable {
                                    hapticManager.click()
                                    challengeManager.equipTitle(null)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = SettingsPetStrings.unequip(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 획득한 칭호 목록 (펼침/접힘)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        hapticManager.lightClick()
                        isExpanded = !isExpanded
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SettingsPetStrings.titleCount(unlockedTitles.size),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isExpanded) "▼" else "▶",
                    fontSize = 12.sp,
                    color = MockupColors.TextMuted,
                    fontFamily = kenneyFont
                )
            }

            // 칭호 목록 (펼쳤을 때)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                val titlesList = unlockedTitles.toList()
                val rows = (titlesList.size + 2) / 3
                val gridHeight = (rows * 80).coerceAtLeast(80).dp

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeight),
                    userScrollEnabled = false
                ) {
                    items(titlesList) { titleType ->
                        val isEquipped = equippedTitle == titleType

                        TitleItemCompact(
                            titleType = titleType,
                            isEquipped = isEquipped,
                            onClick = {
                                hapticManager.click()
                                if (isEquipped) {
                                    challengeManager.equipTitle(null)
                                } else {
                                    challengeManager.equipTitle(titleType)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 컴팩트한 칭호 아이템 - ChallengeBox 스타일
 */
@Composable
private fun TitleItemCompact(
    titleType: ChallengeType,
    isEquipped: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1.2f)
            .border(
                width = if (isEquipped) 3.dp else 2.dp,
                color = if (isEquipped) MockupColors.TextPrimary else MockupColors.Border,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                if (isEquipped) MockupColors.Border.copy(alpha = 0.15f) else MockupColors.CardBackground,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 칭호 이름
            Text(
                text = titleType.getLocalizedTitle(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 장착/장착해제 상태 표시
            Text(
                text = if (isEquipped) SettingsPetStrings.equipped() else SettingsPetStrings.equip(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = if (isEquipped) MockupColors.TextPrimary else MockupColors.TextMuted
            )
        }

        // 장착 표시 (우상단 체크마크)
        if (isEquipped) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .background(MockupColors.TextPrimary, CircleShape)
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
