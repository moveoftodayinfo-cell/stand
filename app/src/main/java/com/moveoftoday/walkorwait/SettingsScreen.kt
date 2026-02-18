package com.moveoftoday.walkorwait

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.moveoftoday.walkorwait.BuildConfig
import com.moveoftoday.walkorwait.pet.MockupColors
import com.moveoftoday.walkorwait.pet.PaymentScreen
import com.moveoftoday.walkorwait.pet.PetSprite
import com.moveoftoday.walkorwait.pet.PetType
import com.moveoftoday.walkorwait.pet.PetTypeV2
import com.moveoftoday.walkorwait.pet.PetGrowthStage
import com.moveoftoday.walkorwait.pet.PetAnimationTypeV2
import com.moveoftoday.walkorwait.pet.PetSpriteV2WithGlow
import com.moveoftoday.walkorwait.pet.PetLevel
import com.moveoftoday.walkorwait.pet.PixelIcon
import com.moveoftoday.walkorwait.pet.rememberKenneyFont
import com.moveoftoday.walkorwait.ui.theme.StandColors
import com.moveoftoday.walkorwait.ui.theme.StandTypography
import com.moveoftoday.walkorwait.ui.theme.StandSpacing
import com.moveoftoday.walkorwait.ui.theme.StandSize
import com.moveoftoday.walkorwait.ui.components.*
import java.util.Locale

// ============ 다국어 지원 헬퍼 객체 ============
private object SettingsStrings {
    private fun getLang(): String = Locale.getDefault().language

    // Toast 메시지
    fun dataRestored(): String = when (getLang()) {
        "ko" -> "기존 데이터를 복원했어요!"
        "ja" -> "既存データを復元しました!"
        "zh" -> "已恢复现有数据!"
        "es" -> "¡Datos restaurados!"
        else -> "Data restored!"
    }

    fun dataSaved(): String = when (getLang()) {
        "ko" -> "현재 데이터를 저장했어요!"
        "ja" -> "現在のデータを保存しました!"
        "zh" -> "已保存当前数据!"
        "es" -> "¡Datos guardados!"
        else -> "Data saved!"
    }

    fun googleConnected(): String = when (getLang()) {
        "ko" -> "Google 계정 연결 완료!"
        "ja" -> "Googleアカウント連携完了!"
        "zh" -> "Google账号连接成功!"
        "es" -> "¡Cuenta Google conectada!"
        else -> "Google account connected!"
    }

    fun loginFailed(): String = when (getLang()) {
        "ko" -> "로그인 실패"
        "ja" -> "ログイン失敗"
        "zh" -> "登录失败"
        "es" -> "Error de inicio de sesión"
        else -> "Login failed"
    }

    fun firebaseLoginFailed(): String = when (getLang()) {
        "ko" -> "Firebase 로그인 실패"
        "ja" -> "Firebaseログイン失敗"
        "zh" -> "Firebase登录失败"
        "es" -> "Error de inicio de sesión en Firebase"
        else -> "Firebase login failed"
    }

    fun petChanged(): String = when (getLang()) {
        "ko" -> "펫이 변경되었습니다!"
        "ja" -> "ペットが変更されました!"
        "zh" -> "宠物已更改!"
        "es" -> "¡Mascota cambiada!"
        else -> "Pet changed!"
    }

    fun copied(): String = when (getLang()) {
        "ko" -> "복사 완료!"
        "ja" -> "コピー完了!"
        "zh" -> "复制成功!"
        "es" -> "¡Copiado!"
        else -> "Copied!"
    }

    fun feedbackSent(): String = when (getLang()) {
        "ko" -> "피드백이 전송되었습니다!"
        "ja" -> "フィードバックを送信しました!"
        "zh" -> "反馈已发送!"
        "es" -> "¡Feedback enviado!"
        else -> "Feedback sent!"
    }

    fun sendFailed(error: String?): String = when (getLang()) {
        "ko" -> "전송 실패: $error"
        "ja" -> "送信失敗: $error"
        "zh" -> "发送失败: $error"
        "es" -> "Error de envío: $error"
        else -> "Send failed: $error"
    }

    // 금액 포맷
    fun formatAmount(amount: Int): String {
        return when (getLang()) {
            "ko" -> when {
                amount >= 10000 -> "${amount / 10000}만원"
                amount >= 1000 -> "${amount / 1000}천원"
                else -> "${amount}원"
            }
            "ja" -> when {
                amount >= 10000 -> "${amount / 10000}万円"
                amount >= 1000 -> "${amount / 1000}千円"
                else -> "${amount}円"
            }
            "zh" -> when {
                amount >= 10000 -> "${amount / 10000}万元"
                amount >= 1000 -> "${amount / 1000}千元"
                else -> "${amount}元"
            }
            else -> when {
                amount >= 10000 -> "$${amount / 10000}0K"
                amount >= 1000 -> "$${amount / 1000}K"
                else -> "$$amount"
            }
        }
    }

    // UI 텍스트
    fun friend(): String = when (getLang()) {
        "ko" -> "친구"
        "ja" -> "フレンド"
        "zh" -> "朋友"
        "es" -> "Amigo"
        else -> "Friend"
    }

    fun accessibilityDisabled(): String = when (getLang()) {
        "ko" -> "rebon 비활성화됨"
        "ja" -> "rebon無効化中"
        "zh" -> "rebon已禁用"
        "es" -> "rebon desactivado"
        else -> "rebon disabled"
    }

    fun successDaysFormat(success: Int, total: Int): String = when (getLang()) {
        "ko" -> "${success}/${total}일 성공"
        "ja" -> "${success}/${total}日成功"
        "zh" -> "${success}/${total}天成功"
        "es" -> "${success}/${total} días exitosos"
        else -> "${success}/${total} days success"
    }

    fun currentPet(name: String): String = when (getLang()) {
        "ko" -> "현재: $name"
        "ja" -> "現在: $name"
        "zh" -> "当前: $name"
        "es" -> "Actual: $name"
        else -> "Current: $name"
    }

    fun usingByUser(email: String?): String = when (getLang()) {
        "ko" -> "${email?.substringBefore("@")}님이 사용 중"
        "ja" -> "${email?.substringBefore("@")}さんが使用中"
        "zh" -> "${email?.substringBefore("@")}正在使用"
        "es" -> "${email?.substringBefore("@")} está usando"
        else -> "${email?.substringBefore("@")} is using"
    }

    fun shareToFriend(): String = when (getLang()) {
        "ko" -> "친구에게 공유하기"
        "ja" -> "友達にシェア"
        "zh" -> "分享给朋友"
        "es" -> "Compartir con amigo"
        else -> "Share with friend"
    }

    fun goalDecreaseAvailable(date: String): String = when (getLang()) {
        "ko" -> "목표 감소 가능: $date"
        "ja" -> "目標減少可能: $date"
        "zh" -> "目标可减少: $date"
        "es" -> "Reducción de meta disponible: $date"
        else -> "Goal decrease available: $date"
    }

    fun appRemoveAvailable(date: String): String = when (getLang()) {
        "ko" -> "앱 제거 가능: $date"
        "ja" -> "アプリ削除可能: $date"
        "zh" -> "可删除应用: $date"
        "es" -> "Eliminación de app disponible: $date"
        else -> "App removal available: $date"
    }

    fun none(): String = when (getLang()) {
        "ko" -> "없음"
        "ja" -> "なし"
        "zh" -> "无"
        "es" -> "Ninguno"
        else -> "None"
    }

    fun allDay(): String = when (getLang()) {
        "ko" -> "24시간"
        "ja" -> "24時間"
        "zh" -> "24小时"
        "es" -> "24 horas"
        else -> "24 hours"
    }

    fun periodChangeAvailable(date: String): String = when (getLang()) {
        "ko" -> "시간대 변경 가능: $date"
        "ja" -> "時間帯変更可能: $date"
        "zh" -> "可更改时段: $date"
        "es" -> "Cambio de horario disponible: $date"
        else -> "Period change available: $date"
    }

    fun dayChangeAvailable(date: String): String = when (getLang()) {
        "ko" -> "요일 변경 가능: $date"
        "ja" -> "曜日変更可能: $date"
        "zh" -> "可更改星期: $date"
        "es" -> "Cambio de día disponible: $date"
        else -> "Day change available: $date"
    }

    fun healthConnectRequired(): String = when (getLang()) {
        "ko" -> "Health Connect 연결 필요"
        "ja" -> "Health Connect連携必要"
        "zh" -> "需要连接Health Connect"
        "es" -> "Se requiere Health Connect"
        else -> "Health Connect required"
    }

    fun usingAppData(appName: String?): String = when (getLang()) {
        "ko" -> if (appName != null) "$appName 데이터 사용 중" else "Health Connect 데이터 사용 중"
        "ja" -> if (appName != null) "${appName}データ使用中" else "Health Connectデータ使用中"
        "zh" -> if (appName != null) "正在使用${appName}数据" else "正在使用Health Connect数据"
        "es" -> if (appName != null) "Usando datos de $appName" else "Usando datos de Health Connect"
        else -> if (appName != null) "Using $appName data" else "Using Health Connect data"
    }

    fun batterySaverMode(): String = when (getLang()) {
        "ko" -> "🔋 배터리 절약 모드"
        "ja" -> "🔋 バッテリー節約モード"
        "zh" -> "🔋 省电模式"
        "es" -> "🔋 Modo ahorro de batería"
        else -> "🔋 Battery saver mode"
    }

    fun connected(): String = when (getLang()) {
        "ko" -> "연결됨"
        "ja" -> "連携済み"
        "zh" -> "已连接"
        "es" -> "Conectado"
        else -> "Connected"
    }

    fun googleAccount(): String = when (getLang()) {
        "ko" -> "Google 계정"
        "ja" -> "Googleアカウント"
        "zh" -> "Google账号"
        "es" -> "Cuenta Google"
        else -> "Google Account"
    }

    fun googleLogin(): String = when (getLang()) {
        "ko" -> "Google 로그인"
        "ja" -> "Googleログイン"
        "zh" -> "Google登录"
        "es" -> "Iniciar sesión con Google"
        else -> "Google Sign-In"
    }

    // 다이얼로그 텍스트
    fun achieve95Percent(): String = when (getLang()) {
        "ko" -> "95% 달성하면"
        "ja" -> "95%達成すると"
        "zh" -> "达成95%后"
        "es" -> "Al lograr 95%"
        else -> "Achieve 95%"
    }

    fun couponBenefitDescription(): String = when (getLang()) {
        "ko" -> "• 친구에게 쿠폰을 선물하면\n• 친구가 1달 무료로 사용!\n• 매달 95% 달성하면 매달 쿠폰 획득"
        "ja" -> "• 友達にクーポンをプレゼント\n• 友達は1ヶ月無料！\n• 毎月95%達成で毎月クーポン獲得"
        "zh" -> "• 将优惠券送给朋友\n• 朋友可免费使用1个月!\n• 每月达成95%即可获得优惠券"
        "es" -> "• Regala un cupón a un amigo\n• ¡Tu amigo usa 1 mes gratis!\n• Logra 95% mensual para obtener cupones"
        else -> "• Give coupon to friend\n• Friend uses 1 month free!\n• Achieve 95% monthly for coupons"
    }

    fun howToUse(): String = when (getLang()) {
        "ko" -> "1. 내 초대 코드 복사하기\n2. 친구에게 카톡으로 공유\n3. 친구가 코드 입력하면 끝!"
        "ja" -> "1. 招待コードをコピー\n2. 友達にLINEでシェア\n3. 友達がコード入力で完了！"
        "zh" -> "1. 复制我的邀请码\n2. 分享给朋友\n3. 朋友输入代码即可!"
        "es" -> "1. Copia tu código de invitación\n2. Comparte por mensaje\n3. ¡Tu amigo ingresa el código!"
        else -> "1. Copy my invite code\n2. Share with friend\n3. Friend enters code - done!"
    }

    fun removeAvailableDate(date: String): String = when (getLang()) {
        "ko" -> "제거 가능일: $date"
        "ja" -> "削除可能日: $date"
        "zh" -> "可删除日期: $date"
        "es" -> "Fecha de eliminación disponible: $date"
        else -> "Removal available: $date"
    }

    fun changeAvailableDate(date: String): String = when (getLang()) {
        "ko" -> "변경 가능일: $date"
        "ja" -> "変更可能日: $date"
        "zh" -> "可更改日期: $date"
        "es" -> "Fecha de cambio disponible: $date"
        else -> "Change available: $date"
    }

    fun cancel(): String = when (getLang()) {
        "ko" -> "취소"
        "ja" -> "キャンセル"
        "zh" -> "取消"
        "es" -> "Cancelar"
        else -> "Cancel"
    }

    fun apply(): String = when (getLang()) {
        "ko" -> "적용"
        "ja" -> "適用"
        "zh" -> "应用"
        "es" -> "Aplicar"
        else -> "Apply"
    }

    fun controlDays(): String = when (getLang()) {
        "ko" -> "제어 요일"
        "ja" -> "制御曜日"
        "zh" -> "控制星期"
        "es" -> "Días de control"
        else -> "Control Days"
    }

    fun selectControlDays(): String = when (getLang()) {
        "ko" -> "제어할 요일을 선택하세요"
        "ja" -> "制御する曜日を選択してください"
        "zh" -> "请选择控制的星期"
        "es" -> "Selecciona los días de control"
        else -> "Select days to control"
    }

    fun weekdays(): String = when (getLang()) {
        "ko" -> "평일"
        "ja" -> "平日"
        "zh" -> "工作日"
        "es" -> "Entre semana"
        else -> "Weekdays"
    }

    fun weekends(): String = when (getLang()) {
        "ko" -> "주말"
        "ja" -> "週末"
        "zh" -> "周末"
        "es" -> "Fin de semana"
        else -> "Weekends"
    }

    fun everyday(): String = when (getLang()) {
        "ko" -> "매일"
        "ja" -> "毎日"
        "zh" -> "每天"
        "es" -> "Todos los días"
        else -> "Everyday"
    }

    fun removalRestricted(): String = when (getLang()) {
        "ko" -> "제거 제한 중"
        "ja" -> "削除制限中"
        "zh" -> "删除限制中"
        "es" -> "Eliminación restringida"
        else -> "Removal restricted"
    }

    fun addOnlyAvailable(date: String): String = when (getLang()) {
        "ko" -> "추가만 가능 · 제거 가능일: $date"
        "ja" -> "追加のみ可能 · 削除可能日: $date"
        "zh" -> "仅可添加 · 可删除日期: $date"
        "es" -> "Solo agregar · Eliminación disponible: $date"
        else -> "Add only · Removal available: $date"
    }

    fun recommendWeekdays(): String = when (getLang()) {
        "ko" -> "추천: 평일(월~금)"
        "ja" -> "おすすめ: 平日(月〜金)"
        "zh" -> "推荐: 工作日(周一至周五)"
        "es" -> "Recomendado: Entre semana (Lun-Vie)"
        else -> "Recommended: Weekdays (Mon-Fri)"
    }

    fun freeWeekends(): String = when (getLang()) {
        "ko" -> "주말은 자유롭게!"
        "ja" -> "週末は自由に！"
        "zh" -> "周末自由!"
        "es" -> "¡Fines de semana libres!"
        else -> "Weekends free!"
    }

    fun noBlockingSelected(): String = when (getLang()) {
        "ko" -> "선택하지 않으면 차단되지 않습니다"
        "ja" -> "選択しないとブロックされません"
        "zh" -> "不选择则不会被阻止"
        "es" -> "Sin selección no hay bloqueo"
        else -> "No selection means no blocking"
    }

    // 펫 변경 다이얼로그
    fun changePet(): String = when (getLang()) {
        "ko" -> "펫 변경"
        "ja" -> "ペット変更"
        "zh" -> "更换宠物"
        "es" -> "Cambiar mascota"
        else -> "Change Pet"
    }

    fun selectNewFriend(): String = when (getLang()) {
        "ko" -> "새로운 친구를 선택하세요"
        "ja" -> "新しい友達を選んでください"
        "zh" -> "选择新朋友"
        "es" -> "Selecciona un nuevo amigo"
        else -> "Select a new friend"
    }

    fun petName(): String = when (getLang()) {
        "ko" -> "펫 이름"
        "ja" -> "ペット名"
        "zh" -> "宠物名字"
        "es" -> "Nombre de mascota"
        else -> "Pet Name"
    }

    fun petChangeCost(): String = when (getLang()) {
        "ko" -> "펫 변경 비용: "
        "ja" -> "ペット変更費用: "
        "zh" -> "更换宠物费用: "
        "es" -> "Costo de cambio: "
        else -> "Pet change cost: "
    }

    fun checkout(): String = when (getLang()) {
        "ko" -> "결제하기"
        "ja" -> "購入する"
        "zh" -> "支付"
        "es" -> "Pagar"
        else -> "Checkout"
    }

    // 불편사항 다이얼로그
    fun submitFeedback(): String = when (getLang()) {
        "ko" -> "불편사항 접수"
        "ja" -> "フィードバック送信"
        "zh" -> "提交反馈"
        "es" -> "Enviar feedback"
        else -> "Submit Feedback"
    }

    fun category(): String = when (getLang()) {
        "ko" -> "분류"
        "ja" -> "カテゴリー"
        "zh" -> "分类"
        "es" -> "Categoría"
        else -> "Category"
    }

    fun title(): String = when (getLang()) {
        "ko" -> "제목"
        "ja" -> "タイトル"
        "zh" -> "标题"
        "es" -> "Título"
        else -> "Title"
    }

    fun titlePlaceholder(): String = when (getLang()) {
        "ko" -> "간단한 제목을 입력하세요"
        "ja" -> "簡単なタイトルを入力してください"
        "zh" -> "请输入简短标题"
        "es" -> "Ingresa un título breve"
        else -> "Enter a brief title"
    }

    fun content(): String = when (getLang()) {
        "ko" -> "내용"
        "ja" -> "内容"
        "zh" -> "内容"
        "es" -> "Contenido"
        else -> "Content"
    }

    fun contentPlaceholder(): String = when (getLang()) {
        "ko" -> "자세한 내용을 입력하세요\n\n어떤 상황에서 문제가 발생했는지,\n기대했던 동작은 무엇인지 알려주세요."
        "ja" -> "詳細を入力してください\n\nどのような状況で問題が発生したか、\n期待していた動作を教えてください。"
        "zh" -> "请输入详细内容\n\n请告诉我们在什么情况下出现问题，\n以及您期望的结果。"
        "es" -> "Ingresa los detalles\n\nDescribe en qué situación ocurrió el problema\ny cuál era el comportamiento esperado."
        else -> "Enter details\n\nDescribe the situation when the problem occurred\nand what behavior you expected."
    }

    fun screenshotOptional(): String = when (getLang()) {
        "ko" -> "스크린샷 (선택)"
        "ja" -> "スクリーンショット (任意)"
        "zh" -> "截图 (可选)"
        "es" -> "Captura de pantalla (opcional)"
        else -> "Screenshot (optional)"
    }

    fun imageAttached(): String = when (getLang()) {
        "ko" -> "이미지 첨부됨"
        "ja" -> "画像添付済み"
        "zh" -> "已附加图片"
        "es" -> "Imagen adjunta"
        else -> "Image attached"
    }

    fun tapToChange(): String = when (getLang()) {
        "ko" -> "탭하여 변경"
        "ja" -> "タップして変更"
        "zh" -> "点击更改"
        "es" -> "Toca para cambiar"
        else -> "Tap to change"
    }

    fun tapToSelectImage(): String = when (getLang()) {
        "ko" -> "탭하여 이미지 선택"
        "ja" -> "タップして画像選択"
        "zh" -> "点击选择图片"
        "es" -> "Toca para seleccionar imagen"
        else -> "Tap to select image"
    }

    fun submit(): String = when (getLang()) {
        "ko" -> "접수하기"
        "ja" -> "送信する"
        "zh" -> "提交"
        "es" -> "Enviar"
        else -> "Submit"
    }

    fun notice(): String = when (getLang()) {
        "ko" -> "안내"
        "ja" -> "お知らせ"
        "zh" -> "提示"
        "es" -> "Aviso"
        else -> "Notice"
    }

    fun feedbackNotice(): String = when (getLang()) {
        "ko" -> "접수된 내용은 빠른 시일 내에 검토하겠습니다.\n개인정보는 문의 처리 목적으로만 사용됩니다."
        "ja" -> "いただいた内容は早急に確認いたします。\n個人情報はお問い合わせ対応目的でのみ使用されます。"
        "zh" -> "我们将尽快审核您的反馈。\n个人信息仅用于处理咨询。"
        "es" -> "Revisaremos tu feedback lo antes posible.\nLos datos personales solo se usan para procesar la consulta."
        else -> "We will review your feedback promptly.\nPersonal information is only used for inquiry processing."
    }

    // 데이터 충돌 다이얼로그
    fun dataSelection(): String = when (getLang()) {
        "ko" -> "⚠️ 데이터 선택"
        "ja" -> "⚠️ データ選択"
        "zh" -> "⚠️ 数据选择"
        "es" -> "⚠️ Selección de datos"
        else -> "⚠️ Data Selection"
    }

    fun dataConflictMessage(): String = when (getLang()) {
        "ko" -> "기존 Google 계정에 저장된 데이터가 있어요.\n어떤 데이터를 사용할까요?"
        "ja" -> "既存のGoogleアカウントに保存されたデータがあります。\nどのデータを使用しますか？"
        "zh" -> "您的Google账号中已有保存的数据。\n您想使用哪个数据？"
        "es" -> "Hay datos guardados en tu cuenta de Google.\n¿Qué datos quieres usar?"
        else -> "There is saved data in your Google account.\nWhich data do you want to use?"
    }

    fun restoreExistingData(): String = when (getLang()) {
        "ko" -> "기존 데이터 복원"
        "ja" -> "既存データを復元"
        "zh" -> "恢复现有数据"
        "es" -> "Restaurar datos existentes"
        else -> "Restore existing data"
    }

    fun keepCurrentData(): String = when (getLang()) {
        "ko" -> "현재 데이터 유지"
        "ja" -> "現在のデータを維持"
        "zh" -> "保留当前数据"
        "es" -> "Mantener datos actuales"
        else -> "Keep current data"
    }

    fun petInfo(name: String?, typeName: String): String = when (getLang()) {
        "ko" -> "펫: ${name ?: "이름없음"} ($typeName)"
        "ja" -> "ペット: ${name ?: "名前なし"} ($typeName)"
        "zh" -> "宠物: ${name ?: "无名"} ($typeName)"
        "es" -> "Mascota: ${name ?: "Sin nombre"} ($typeName)"
        else -> "Pet: ${name ?: "Unnamed"} ($typeName)"
    }

    fun streakAndSteps(streak: Int, steps: Long): String = when (getLang()) {
        "ko" -> "연속 달성: ${streak}일 | 총 걸음: ${String.format("%,d", steps)}보"
        "ja" -> "連続達成: ${streak}日 | 総歩数: ${String.format("%,d", steps)}歩"
        "zh" -> "连续达成: ${streak}天 | 总步数: ${String.format("%,d", steps)}步"
        "es" -> "Racha: ${streak} días | Pasos totales: ${String.format("%,d", steps)}"
        else -> "Streak: ${streak} days | Total steps: ${String.format("%,d", steps)}"
    }

    fun getPetDisplayName(petType: String?): String = when (getLang()) {
        "ko" -> when (petType) {
            "DOG1" -> "강아지"
            "CAT" -> "고양이"
            "HAMSTER" -> "햄스터"
            "RABBIT" -> "토끼"
            else -> petType ?: "기본"
        }
        "ja" -> when (petType) {
            "DOG1" -> "犬"
            "CAT" -> "猫"
            "HAMSTER" -> "ハムスター"
            "RABBIT" -> "うさぎ"
            else -> petType ?: "デフォルト"
        }
        "zh" -> when (petType) {
            "DOG1" -> "狗狗"
            "CAT" -> "猫咪"
            "HAMSTER" -> "仓鼠"
            "RABBIT" -> "兔子"
            else -> petType ?: "默认"
        }
        "es" -> when (petType) {
            "DOG1" -> "Perro"
            "CAT" -> "Gato"
            "HAMSTER" -> "Hámster"
            "RABBIT" -> "Conejo"
            else -> petType ?: "Predeterminado"
        }
        else -> when (petType) {
            "DOG1" -> "Dog"
            "CAT" -> "Cat"
            "HAMSTER" -> "Hamster"
            "RABBIT" -> "Rabbit"
            else -> petType ?: "Default"
        }
    }

    // 요일 이름
    fun getDayNames(): List<String> = when (getLang()) {
        "ko" -> listOf("일", "월", "화", "수", "목", "금", "토")
        "ja" -> listOf("日", "月", "火", "水", "木", "金", "土")
        "zh" -> listOf("日", "一", "二", "三", "四", "五", "六")
        "es" -> listOf("Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sá")
        else -> listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    }

    // 공유 메시지
    fun shareMessage(inviteCode: String): String = when (getLang()) {
        "ko" -> """
🏃 rebon - 걸어서 앱을 해제하세요!

친구가 rebon 앱을 추천했어요.
목표 걸음수를 달성하면 앱이 해제되는 신개념 건강 앱!

📱 앱 다운로드: https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait

🎁 초대 코드: $inviteCode
위 코드를 입력하면 1달 무료!
""".trimIndent()
        "ja" -> """
🏃 rebon - 歩いてアプリをアンロック！

友達がrebonアプリをおすすめしています。
目標歩数達成でアプリがアンロックされる新感覚健康アプリ！

📱 ダウンロード: https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait

🎁 招待コード: $inviteCode
このコードで1ヶ月無料！
""".trimIndent()
        "zh" -> """
🏃 rebon - 走路解锁应用！

朋友推荐了rebon应用。
达成目标步数即可解锁应用的创新健康应用！

📱 下载: https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait

🎁 邀请码: $inviteCode
输入此代码免费使用1个月！
""".trimIndent()
        "es" -> """
🏃 rebon - ¡Desbloquea apps caminando!

Tu amigo te recomienda rebon.
¡Una app de salud que desbloquea al lograr tu meta de pasos!

📱 Descargar: https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait

🎁 Código de invitación: $inviteCode
¡Ingresa este código y obtén 1 mes gratis!
""".trimIndent()
        else -> """
🏃 rebon - Unlock apps by walking!

Your friend recommends rebon.
A health app that unlocks when you reach your step goal!

📱 Download: https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait

🎁 Invite code: $inviteCode
Enter this code for 1 month free!
""".trimIndent()
    }

    fun shareMessageSimple(): String = when (getLang()) {
        "ko" -> """
🏃 rebon - 걸어서 앱을 해제하세요!

친구가 rebon 앱을 추천했어요.
목표 걸음수를 달성하면 앱이 해제되는 신개념 건강 앱!

📱 앱 다운로드: https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait
""".trimIndent()
        "ja" -> """
🏃 rebon - 歩いてアプリをアンロック！

友達がrebonアプリをおすすめしています。
目標歩数達成でアプリがアンロックされる新感覚健康アプリ！

📱 ダウンロード: https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait
""".trimIndent()
        "zh" -> """
🏃 rebon - 走路解锁应用！

朋友推荐了rebon应用。
达成目标步数即可解锁应用的创新健康应用！

📱 下载: https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait
""".trimIndent()
        "es" -> """
🏃 rebon - ¡Desbloquea apps caminando!

Tu amigo te recomienda rebon.
¡Una app de salud que desbloquea al lograr tu meta de pasos!

📱 Descargar: https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait
""".trimIndent()
        else -> """
🏃 rebon - Unlock apps by walking!

Your friend recommends rebon.
A health app that unlocks when you reach your step goal!

📱 Download: https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait
""".trimIndent()
    }

    fun currentAchievement(rate: Float): String = when (getLang()) {
        "ko" -> "현재 달성률: ${String.format("%.0f", rate)}% → 95% 필요"
        "ja" -> "現在の達成率: ${String.format("%.0f", rate)}% → 95%必要"
        "zh" -> "当前达成率: ${String.format("%.0f", rate)}% → 需要95%"
        "es" -> "Logro actual: ${String.format("%.0f", rate)}% → 95% requerido"
        else -> "Current achievement: ${String.format("%.0f", rate)}% → 95% needed"
    }

    fun icon(): String = when (getLang()) {
        "ko" -> "아이콘"
        "ja" -> "アイコン"
        "zh" -> "图标"
        "es" -> "Icono"
        else -> "Icon"
    }
}

// 데이터 충돌 정보 클래스
data class RemoteDataInfo(
    val petType: String?,
    val petName: String?,
    val streak: Int,
    val petTotalSteps: Long,
    val tutorialCompleted: Boolean,
    val paidDeposit: Boolean
)

data class LocalDataInfo(
    val petType: String?,
    val petName: String?,
    val streak: Int,
    val petTotalSteps: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferenceManager: PreferenceManager?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as WalkorWaitApp
    val repository = app.userDataRepository
    val hapticManager = remember { HapticManager(context) }
    val scope = rememberCoroutineScope()

    // UserSettings StateFlow 관찰 (Firebase 데이터 복원 시 자동 UI 갱신)
    val userSettings by repository.userSettings.collectAsState()

    var currentSteps by remember { mutableIntStateOf(repository.getTodaySteps()) }
    var goal by remember { mutableIntStateOf(repository.getGoal()) }
    var goalUnit by remember { mutableStateOf(preferenceManager?.getGoalUnit() ?: "steps") }
    var deposit by remember { mutableIntStateOf(repository.getDeposit()) }
    var successDays by remember { mutableIntStateOf(repository.getSuccessDays()) }
    var totalDays by remember { mutableIntStateOf(preferenceManager?.getTotalControlDays() ?: 0) }
    var requiredDays by remember {
        mutableIntStateOf(
            preferenceManager?.getRequiredSuccessDays() ?: 0
        )
    }
    val startDate = remember { repository.getControlStartDate() }
    val endDate = remember { repository.getControlEndDate() }
    val isPaidDeposit = remember { repository.isPaidDeposit() }

    // 접근성 서비스 체크
    var isAccessibilityEnabled by remember { mutableStateOf(false) }

    // 설정 값 로컬 상태 (변경 시 즉시 UI 반영)
    var lockedAppsState by remember { mutableStateOf(preferenceManager?.getLockedApps() ?: emptySet<String>()) }
    var blockingPeriodsState by remember { mutableStateOf(preferenceManager?.getBlockingPeriods() ?: emptySet<String>()) }
    var controlDaysState by remember { mutableStateOf(preferenceManager?.getControlDays() ?: emptySet<Int>()) }

    // Firebase에서 복원된 데이터로 로컬 상태 업데이트
    LaunchedEffect(userSettings) {
        userSettings?.let { settings ->
            if (settings.lockedApps.isNotEmpty() && lockedAppsState.isEmpty()) {
                lockedAppsState = settings.lockedApps
            }
            if (settings.blockingPeriods.isNotEmpty() && blockingPeriodsState.isEmpty()) {
                blockingPeriodsState = settings.blockingPeriods
            }
            if (settings.controlDays.isNotEmpty() && controlDaysState.isEmpty()) {
                controlDaysState = settings.controlDays
            }
        }
    }

    var showGoalDialog by remember { mutableStateOf(false) }
    var showAppLockScreen by remember { mutableStateOf(false) }
    var showPaymentScreen by remember { mutableStateOf(false) }
    var showDepositInfoDialog by remember { mutableStateOf(false) }
    var showFitnessAppConnectionScreen by remember { mutableStateOf(false) }
    var showBlockingPeriodsDialog by remember { mutableStateOf(false) }
    var showControlDaysDialog by remember { mutableStateOf(false) }
    var showChangeConfirmDialog by remember { mutableStateOf<String?>(null) } // "goal", "controlDays", "blockingPeriods"
    var showFeedbackDialog by remember { mutableStateOf(false) }

    // Google 로그인 관련 상태
    val auth = remember { FirebaseAuth.getInstance() }
    var isGoogleSignedIn by remember { mutableStateOf(auth.currentUser != null && auth.currentUser?.isAnonymous != true) }
    var googleEmail by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var isGoogleLoading by remember { mutableStateOf(false) }

    // 데이터 충돌 다이얼로그 상태
    var showDataConflictDialog by remember { mutableStateOf(false) }
    var remoteDataInfo by remember { mutableStateOf<RemoteDataInfo?>(null) }
    var localDataInfo by remember { mutableStateOf<LocalDataInfo?>(null) }

    // 선택에 따른 동기화 처리
    fun handleDataChoice(useRemoteData: Boolean) {
        scope.launch {
            if (useRemoteData) {
                // 원격 데이터로 복원 (기존 동기화 로직)
                repository.startSync()
                Toast.makeText(context, SettingsStrings.dataRestored(), Toast.LENGTH_SHORT).show()
            } else {
                // 현재 로컬 데이터를 Firebase에 덮어쓰기
                repository.forceUploadLocalData()
                Toast.makeText(context, SettingsStrings.dataSaved(), Toast.LENGTH_SHORT).show()
            }
            showDataConflictDialog = false
            isGoogleSignedIn = true
            googleEmail = auth.currentUser?.email ?: ""
            hapticManager.success()
        }
    }

    // Google Sign-In 함수 (Credential Manager 사용)
    fun performGoogleSignIn() {
        isGoogleLoading = true
        scope.launch {
            val result = GoogleSignInHelper.signIn(context)
            when (result) {
                is GoogleSignInHelper.SignInResult.Success -> {
                    val firebaseResult = GoogleSignInHelper.signInToFirebase(result.idToken)
                    if (firebaseResult.isSuccess) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            // Firebase에서 기존 데이터 확인
                            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            try {
                                val settingsDoc = firestore.collection("users")
                                    .document(userId)
                                    .collection("userData")
                                    .document("settings")
                                    .get()
                                    .await()

                                val remoteTutorialCompleted = settingsDoc.getBoolean("tutorialCompleted") ?: false
                                val remotePetType = settingsDoc.getString("petType")
                                val remotePetName = settingsDoc.getString("petName")
                                val remoteStreak = settingsDoc.getLong("streak")?.toInt() ?: 0
                                val remotePetSteps = settingsDoc.getLong("petTotalSteps") ?: 0L
                                val remotePaidDeposit = settingsDoc.getBoolean("paidDeposit") ?: false

                                // 원격에 유효한 데이터가 있는지 확인
                                val hasRemoteData = remoteTutorialCompleted ||
                                    (remotePetType != null && remotePetType != "DOG1") ||
                                    remoteStreak > 0 || remotePetSteps > 0

                                // 로컬에 유효한 데이터가 있는지 확인
                                val localPetType = preferenceManager?.getPetType()
                                val localPetName = preferenceManager?.getPetName()
                                val localStreak = preferenceManager?.getStreak() ?: 0
                                val localPetSteps = preferenceManager?.getPetTotalSteps() ?: 0L
                                val hasLocalData = (localPetType != null && localPetType != "DOG1") ||
                                    localStreak > 0 || localPetSteps > 0

                                android.util.Log.d("SettingsScreen", "🔍 Data check - hasRemoteData: $hasRemoteData, hasLocalData: $hasLocalData")
                                android.util.Log.d("SettingsScreen", "🔍 Remote - petType: $remotePetType, streak: $remoteStreak, steps: $remotePetSteps")
                                android.util.Log.d("SettingsScreen", "🔍 Local - petType: $localPetType, streak: $localStreak, steps: $localPetSteps")

                                isGoogleLoading = false

                                // 양쪽에 데이터가 있고 다르면 충돌 다이얼로그 표시
                                if (hasRemoteData && hasLocalData) {
                                    remoteDataInfo = RemoteDataInfo(
                                        petType = remotePetType,
                                        petName = remotePetName,
                                        streak = remoteStreak,
                                        petTotalSteps = remotePetSteps,
                                        tutorialCompleted = remoteTutorialCompleted,
                                        paidDeposit = remotePaidDeposit
                                    )
                                    localDataInfo = LocalDataInfo(
                                        petType = localPetType,
                                        petName = localPetName,
                                        streak = localStreak,
                                        petTotalSteps = localPetSteps
                                    )
                                    showDataConflictDialog = true
                                } else if (hasRemoteData) {
                                    // 원격에만 데이터 있으면 복원
                                    repository.startSync()
                                    isGoogleSignedIn = true
                                    googleEmail = auth.currentUser?.email ?: ""
                                    hapticManager.success()
                                    Toast.makeText(context, SettingsStrings.dataRestored(), Toast.LENGTH_SHORT).show()
                                } else {
                                    // 로컬에만 데이터 있거나 양쪽 다 없으면 로컬 업로드
                                    repository.forceUploadLocalData()
                                    isGoogleSignedIn = true
                                    googleEmail = auth.currentUser?.email ?: ""
                                    hapticManager.success()
                                    Toast.makeText(context, SettingsStrings.googleConnected(), Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("SettingsScreen", "❌ Firebase check failed: ${e.message}")
                                // 에러 시 기본 동기화
                                repository.startSync()
                                isGoogleLoading = false
                                isGoogleSignedIn = true
                                googleEmail = auth.currentUser?.email ?: ""
                                hapticManager.success()
                                Toast.makeText(context, SettingsStrings.googleConnected(), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            isGoogleLoading = false
                            Toast.makeText(context, SettingsStrings.loginFailed(), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        isGoogleLoading = false
                        Toast.makeText(context, SettingsStrings.firebaseLoginFailed(), Toast.LENGTH_SHORT).show()
                    }
                }
                is GoogleSignInHelper.SignInResult.Error -> {
                    isGoogleLoading = false
                    if (!result.isCancelled) {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // 앱 제어 섹션 접기/펼치기 상태 (기본: 접힘)
    var isAppControlExpanded by remember { mutableStateOf(false) }

    // 펫 변경 관련 상태
    val showPetChangeDialogRef = remember { mutableStateOf(false) }

    // 외부에서 사용할 변수
    var showPetChangeDialog by showPetChangeDialogRef

    // 펫 변경용 BillingManager (nullable state - 다이얼로그 열 때 생성)
    var petChangeBillingManager by remember { mutableStateOf<BillingManager?>(null) }

    // 펫 변경 결제 시작 함수 (V2)
    fun startPetChangePurchase(newPetType: PetTypeV2, newPetName: String) {
        android.util.Log.d("PetChange", "🚀 startPetChangePurchase - newPetType=${newPetType.name}, newPetName=$newPetName")
        // PreferenceManager에 임시 저장 (Activity 재생성 대비)
        preferenceManager?.savePendingPetChange(newPetType.name, newPetName)

        // 먼저 다이얼로그 닫기 (결제 UI가 뜨기 전에)
        showPetChangeDialogRef.value = false

        val activity = context as? android.app.Activity ?: return

        // 약간의 지연 후 결제 시작 (다이얼로그 닫힌 후)
        scope.launch {
            kotlinx.coroutines.delay(100)

            // 매번 새로운 BillingManager 생성 (콜백 stale 방지)
            petChangeBillingManager = BillingManager(
                    context = context,
                    onPurchaseSuccess = { purchase ->
                        // 결제 성공 시 펫 변경 저장 - PreferenceManager에서 읽기
                        val petTypeName = preferenceManager?.getPendingPetType()
                        val petName = preferenceManager?.getPendingPetName() ?: ""
                        android.util.Log.d("PetChange", "🔥 onPurchaseSuccess - petTypeName=$petTypeName, petName=$petName")

                        if (petTypeName != null) {
                            try {
                                val appContext = context.applicationContext
                                // V2 펫 타입으로 저장
                                val petTypeV2 = try { PetTypeV2.valueOf(petTypeName) } catch (e: Exception) { PetTypeV2.SHIBA }
                                preferenceManager?.savePetTypeV2(petTypeV2)
                                preferenceManager?.savePetNameV2(petName)

                                // 기존 레벨 유지, 없으면 레벨 1로 시작
                                val existingLevel = preferenceManager?.getPetLevelV2()
                                if (existingLevel == null || existingLevel.level == 0) {
                                    preferenceManager?.savePetLevelV2(PetLevel(level = 1, currentExp = 0, totalExp = 0))
                                }

                                // Firebase에도 동기화
                                val app = appContext as WalkorWaitApp
                                app.userDataRepository.savePetInfo(petTypeName, petName)

                                // 펫 교체 결제 추적
                                app.userDataRepository.trackPetChangePurchase(petTypeName, petName)
                                AnalyticsManager.trackPurchaseCompleted("pet_change", 2500.0)

                                StepWidgetProvider.updateAllWidgets(appContext)
                                Toast.makeText(appContext, SettingsStrings.petChanged(), Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                android.util.Log.e("SettingsScreen", "Pet change failed: ${e.message}")
                            }
                        }
                        // 임시 저장 데이터 삭제
                        preferenceManager?.clearPendingPetChange()
                    },
                    onPurchaseFailure = { error ->
                        preferenceManager?.clearPendingPetChange()
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
            )
            petChangeBillingManager?.startPetChangePurchase(activity)
        }
    }

    // Analytics: 설정 화면 조회 추적
    LaunchedEffect(Unit) {
        try {
            AnalyticsManager.trackScreenView("SettingsScreen", "SettingsScreen")
        } catch (e: Exception) {
            // Analytics 실패는 무시
        }
    }

    // 1초마다 업데이트 + 접근성 체크
    LaunchedEffect(Unit) {
        while (true) {
            try {
                currentSteps = repository.getTodaySteps()
                goal = repository.getGoal()
                deposit = repository.getDeposit()
                successDays = repository.getSuccessDays()
                totalDays = preferenceManager?.getTotalControlDays() ?: 0
                requiredDays = preferenceManager?.getRequiredSuccessDays() ?: 0

                // 접근성 서비스 체크
                val enabledServices = android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                isAccessibilityEnabled = enabledServices?.contains("com.moveoftoday.walkorwait") == true
            } catch (e: Exception) {
                // 업데이트 실패는 무시
            }
            delay(1000)
        }
    }

    val achievementRate = if (totalDays > 0) (successDays.toFloat() / totalDays * 100) else 0f

    // 2단계 색상 판정 (블루/레드만 사용)
    val statusColor = when {
        achievementRate >= 95f -> MockupColors.Blue   // 달성
        else -> MockupColors.Red                       // 미달성
    }

    val statusText = when {
        achievementRate >= 95f -> context.getString(R.string.fully_achieved)
        achievementRate >= 80f -> context.getString(R.string.partially_achieved)
        else -> context.getString(R.string.in_progress)
    }

    val statusDescription = when {
        achievementRate >= 95f -> context.getString(R.string.invite_coupon_earned)
        else -> context.getString(R.string.target_95)
    }

    fun formatAmount(amount: Int): String {
        return when {
            amount >= 10000 -> "${amount / 10000}만원"
            amount >= 1000 -> "${amount / 1000}천원"
            else -> "${amount}원"
        }
    }

    if (showAppLockScreen) {
        AppLockScreen(
            preferenceManager = preferenceManager,
            onBack = { showAppLockScreen = false },
            onLockedAppsChanged = { newApps -> lockedAppsState = newApps }
        )
    } else if (showPaymentScreen && preferenceManager != null) {
        val prefs = preferenceManager
        val savedPetType = prefs.getPetTypeV2() ?: PetTypeV2.SHIBA
        val savedPetName = prefs.getPetName() ?: SettingsStrings.friend()

        PaymentScreen(
            petType = savedPetType,
            petName = savedPetName,
            preferenceManager = prefs,
            hapticManager = hapticManager,
            onComplete = { showPaymentScreen = false }
        )
    } else if (showFitnessAppConnectionScreen) {
        FitnessAppConnectionScreen(
            onBack = { showFitnessAppConnectionScreen = false },
            onConnectionComplete = { showFitnessAppConnectionScreen = false }
        )
    } else if (showGoalDialog) {
        // 목표 설정 (풀스크린)
        GoalSettingDialog(
            currentGoal = goal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { newGoal ->
                repository.saveGoal(newGoal)
                goal = newGoal
                goalUnit = preferenceManager?.getGoalUnit() ?: "steps"  // 단위도 업데이트
                showGoalDialog = false
                // 위젯 업데이트 (목표 단위 변경 시 위젯 반영)
                StepWidgetProvider.updateAllWidgets(context)
            },
            preferenceManager = preferenceManager,
            hapticManager = hapticManager
        )
    } else if (showBlockingPeriodsDialog) {
        // 차단 시간대 선택 (풀스크린)
        val canRemovePeriods = preferenceManager?.canChangeBlockingPeriods() ?: true
        BlockingPeriodsDialog(
            currentPeriods = blockingPeriodsState,
            canRemove = canRemovePeriods,
            nextRemoveDate = if (!canRemovePeriods) preferenceManager?.getNextBlockingPeriodsChangeDate() ?: "" else "",
            onDismiss = { showBlockingPeriodsDialog = false },
            onConfirm = { newPeriods, hasRemovals ->
                preferenceManager?.saveBlockingPeriods(newPeriods)
                blockingPeriodsState = newPeriods  // 로컬 상태 업데이트
                // 제거가 있을 때만 변경 시간 기록
                if (hasRemovals) {
                    preferenceManager?.saveBlockingPeriodsChangeTime()
                }
                showBlockingPeriodsDialog = false
            }
        )
    } else if (showControlDaysDialog) {
        // 제어 요일 선택 (풀스크린)
        val canRemoveDays = preferenceManager?.canChangeControlDays() ?: true
        ControlDaysDialog(
            currentDays = controlDaysState,
            canRemove = canRemoveDays,
            nextRemoveDate = if (!canRemoveDays) preferenceManager?.getNextControlDaysChangeDate() ?: "" else "",
            onDismiss = { showControlDaysDialog = false },
            onConfirm = { newDays, hasRemovals ->
                preferenceManager?.saveControlDays(newDays)
                controlDaysState = newDays  // 로컬 상태 업데이트
                // 제거가 있을 때만 변경 시간 기록
                if (hasRemovals) {
                    preferenceManager?.saveControlDaysChangeTime()
                }
                showControlDaysDialog = false
            }
        )
    } else {
        // 깔끔한 레트로 스타일 - 3색 시스템 (Black/White, Red, Blue)
        val kenneyFont = rememberKenneyFont()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MockupColors.Background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 상단 헤더 - 깔끔한 레트로 스타일
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 30.dp, bottom = 16.dp)
                    ) {
                        // 뒤로가기 버튼
                        Box(
                            modifier = Modifier
                                .clickable {
                                    hapticManager.click()
                                    onBack()
                                }
                                .align(Alignment.CenterStart)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "←",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        // 중앙 타이틀
                        Text(
                            text = "setting",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.Border,
                            fontFamily = kenneyFont,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // 하단 구분선만
                    HorizontalDivider(
                        color = MockupColors.Border,
                        thickness = 3.dp
                    )
                }

                // 스크롤 가능한 컨텐츠 - 깔끔한 레트로 스타일
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    // ⚠️ 접근성 서비스 경고 (항상 최상단에 표시)
                    if (!isAccessibilityEnabled) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .border(3.dp, MockupColors.Red, RoundedCornerShape(12.dp))
                                .background(MockupColors.RedLight, RoundedCornerShape(12.dp))
                                .clickable {
                                    val intent = android.content.Intent(
                                        android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS
                                    )
                                    context.startActivity(intent)
                                }
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MockupColors.Red)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        SettingsStrings.accessibilityDisabled(),
                                        color = MockupColors.Red,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        fontFamily = kenneyFont
                                    )
                                    Text(
                                        stringResource(R.string.tap_to_enable_in_settings),
                                        color = MockupColors.TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // 💳 구독 관리
                    val statusColor = MockupColors.TextPrimary

                    // 프로모션(친구초대)으로 들어온 사용자인지 확인
                    val isPromoFreeUser = preferenceManager?.getPromoCodeType() != null

                    // 섹션 타이틀
                    RetroSectionTitle(title = stringResource(R.string.section_subscription), fontFamily = kenneyFont)

                    // 이번 달 달성 현황 카드
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                            .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            // 달성률 헤더 (크게 강조)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.this_month_achievement),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.TextPrimary
                                )
                                Text(
                                    text = "${achievementRate.toInt()}%",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    fontFamily = kenneyFont
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 레트로 스타일 프로그레스 바
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                                    .border(2.dp, MockupColors.Border, RoundedCornerShape(4.dp))
                                    .background(MockupColors.Background, RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(achievementRate / 100f)
                                        .background(statusColor, RoundedCornerShape(2.dp))
                                )
                                // 95% 마커
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .offset(x = (0.95f * 280).dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .fillMaxHeight()
                                            .background(MockupColors.Blue)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = SettingsStrings.successDaysFormat(successDays, totalDays),
                                    fontSize = 13.sp,
                                    color = MockupColors.TextSecondary
                                )
                                Text(
                                    text = stringResource(R.string.target_95),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.Blue
                                )
                            }

                            // 쿠폰 혜택 박스 - 프로모션(친구초대) 사용자에게는 표시 안함
                            if (!isPromoFreeUser) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MockupColors.CardBackground)
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = stringResource(R.string.invite_feature),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MockupColors.TextPrimary
                                            )
                                            Text(
                                                text = stringResource(R.string.invite_available_by_subscription),
                                                fontSize = 13.sp,
                                                color = MockupColors.TextSecondary
                                            )
                                        }
                                        PixelIcon(
                                            iconName = "icon_chest",
                                            size = 32.dp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 구독 갱신 카드
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                            .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                            .clickable {
                                hapticManager.click()
                                showPaymentScreen = true
                            }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.subscription_renew),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.TextPrimary,
                                    fontFamily = kenneyFont
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.subscription_renew_desc),
                                    fontSize = 13.sp,
                                    color = MockupColors.TextSecondary
                                )
                            }
                            PixelIcon(iconName = "icon_arrow_right", size = 24.dp)
                        }
                    }

                    // 펫 변경 카드
                    val currentPetType = preferenceManager?.getPetType()
                    val currentPetName = preferenceManager?.getPetName() ?: SettingsStrings.friend()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                            .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                            .clickable {
                                android.widget.Toast.makeText(context, "펫 변경 클릭!", android.widget.Toast.LENGTH_SHORT).show()
                                hapticManager.click()
                                showPetChangeDialog = true
                            }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.pet_change),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.TextPrimary,
                                    fontFamily = kenneyFont
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = SettingsStrings.currentPet(currentPetName),
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
                                    text = "₩1,000",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.TextPrimary,
                                    fontFamily = kenneyFont
                                )
                            }
                        }
                    }

                    // 친구 초대 카드
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val monthId = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())

                    // 코드 생성 (SubscriptionManager와 동일한 알고리즘)
                    val userPart = userId.take(3).uppercase()
                    val basicHash = (userId + monthId).hashCode().toString(16).takeLast(4).uppercase()
                    val bonusHash = (userId + monthId + "bonus").hashCode().toString(16).takeLast(4).uppercase()
                    val basicInviteCode = if (userId.isNotEmpty()) "REBON-$userPart$basicHash" else ""
                    val bonusInviteCode = if (userId.isNotEmpty()) "BONUS-$userPart$bonusHash" else ""

                    // 프로모션 코드 사용자인지 확인 (무료 사용자는 초대 코드 발급 불가)
                    val promoCodeTypeForInvite = preferenceManager?.getPromoCodeType()
                    val isPromoUserForInvite = promoCodeTypeForInvite != null
                    val canShareInviteCode = isPaidDeposit && !isPromoUserForInvite && basicInviteCode.isNotEmpty()

                    // 95% 달성 여부 (보너스 코드 활성화 조건) - earnedCoupon은 이미 위에서 정의됨

                    // Guest 정보 상태
                    var basicGuestEmail by remember { mutableStateOf<String?>(null) }
                    var basicGuestInfo by remember { mutableStateOf<String?>(null) }
                    var bonusGuestEmail by remember { mutableStateOf<String?>(null) }
                    var bonusGuestInfo by remember { mutableStateOf<String?>(null) }

                    // Firebase에서 Guest 정보 가져오기
                    LaunchedEffect(userId, monthId) {
                        if (userId.isNotEmpty() && isPaidDeposit) {
                            try {
                                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                val subDoc = db.collection("users").document(userId)
                                    .collection("subscriptions").document(monthId).get().await()
                                if (subDoc.exists()) {
                                    basicGuestEmail = subDoc.getString("inviteGuestEmail")
                                    bonusGuestEmail = subDoc.getString("bonusGuestEmail")

                                    // 기본 게스트 정보 가져오기
                                    val basicGuestId = subDoc.getString("inviteGuestId")
                                    if (basicGuestId != null) {
                                        val guestDoc = db.collection("users").document(basicGuestId).get().await()
                                        if (guestDoc.exists()) {
                                            val petName = guestDoc.getString("petName") ?: "-"
                                            val goal = guestDoc.getLong("goal")?.toInt() ?: 0
                                            val success = guestDoc.getLong("successDays")?.toInt() ?: 0
                                            val total = guestDoc.getLong("totalDays")?.toInt() ?: 0
                                            basicGuestInfo = "$petName / ${goal}보 / $success/$total"
                                        }
                                    }

                                    // 보너스 게스트 정보 가져오기
                                    val bonusGuestId = subDoc.getString("bonusGuestId")
                                    android.util.Log.d("SettingsScreen", "bonusGuestId: $bonusGuestId")
                                    if (bonusGuestId != null) {
                                        val guestDoc = db.collection("users").document(bonusGuestId).get().await()
                                        android.util.Log.d("SettingsScreen", "guestDoc.exists: ${guestDoc.exists()}")
                                        if (guestDoc.exists()) {
                                            val petName = guestDoc.getString("petName") ?: "-"
                                            val goal = guestDoc.getLong("goal")?.toInt() ?: 0
                                            val success = guestDoc.getLong("successDays")?.toInt() ?: 0
                                            val total = guestDoc.getLong("totalDays")?.toInt() ?: 0
                                            bonusGuestInfo = "$petName / ${goal}보 / $success/$total"
                                            android.util.Log.d("SettingsScreen", "bonusGuestInfo: $bonusGuestInfo")
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("SettingsScreen", "Error fetching guest info", e)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(3.dp, MockupColors.Blue, RoundedCornerShape(12.dp))
                            .background(MockupColors.BlueLight, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.invite_friend),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary,
                                fontFamily = kenneyFont
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.invite_friend_desc),
                                fontSize = 13.sp,
                                color = MockupColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (canShareInviteCode) {
                                // ===== 기본 초대 코드 =====
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = stringResource(R.string.info),
                                        tint = MockupColors.Blue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.basic_invite_code),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.TextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, if (basicGuestEmail != null) MockupColors.Green else MockupColors.Border, RoundedCornerShape(8.dp))
                                        .background(if (basicGuestEmail != null) MockupColors.GreenLight else MockupColors.Background, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    if (basicGuestEmail != null) {
                                        // 사용됨 - 코드 숨김
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = stringResource(R.string.in_use),
                                                    tint = MockupColors.Green,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = SettingsStrings.usingByUser(basicGuestEmail),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MockupColors.Green
                                                )
                                            }
                                            val guestInfo = basicGuestInfo
                                            if (guestInfo != null) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = guestInfo,
                                                    fontSize = 12.sp,
                                                    color = MockupColors.TextSecondary
                                                )
                                            }
                                        }
                                    } else {
                                        // 미사용 - 코드 표시
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = basicInviteCode,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MockupColors.Blue,
                                                fontFamily = kenneyFont
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .border(2.dp, MockupColors.Blue, RoundedCornerShape(6.dp))
                                                    .background(MockupColors.CardBackground, RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        hapticManager.success()
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        val clip = ClipData.newPlainText("invite_code", basicInviteCode)
                                                        clipboard.setPrimaryClip(clip)
                                                        Toast.makeText(context, SettingsStrings.copied(), Toast.LENGTH_SHORT).show()
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.copy),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MockupColors.Blue,
                                                    fontFamily = kenneyFont
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // ===== 보너스 초대 코드 (미사용 - 옛날 기능) =====
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = stringResource(R.string.icon),
                                        tint = MockupColors.TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.bonus_invite_code),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, when {
                                            bonusGuestEmail != null -> MockupColors.TextPrimary
                                            else -> MockupColors.Border
                                        }, RoundedCornerShape(8.dp))
                                        .background(when {
                                            bonusGuestEmail != null -> MockupColors.CardBackground
                                            else -> MockupColors.Background.copy(alpha = 0.5f)
                                        }, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    when {
                                        // 사용됨 - 코드 숨김
                                        bonusGuestEmail != null -> {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Filled.CheckCircle,
                                                        contentDescription = stringResource(R.string.icon),
                                                        tint = MockupColors.TextPrimary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = SettingsStrings.usingByUser(bonusGuestEmail),
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MockupColors.TextPrimary
                                                    )
                                                }
                                                val bonusInfo = bonusGuestInfo
                                                if (bonusInfo != null) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = bonusInfo,
                                                        fontSize = 12.sp,
                                                        color = MockupColors.TextSecondary
                                                    )
                                                }
                                            }
                                        }
                                        // 기본 상태 - 보너스 코드 표시
                                        else -> {
                                            Column {
                                                Text(
                                                    text = "🔒 ${bonusInviteCode.take(10)}...",
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MockupColors.TextMuted,
                                                    fontFamily = kenneyFont
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "현재 달성률: ${String.format("%.0f", achievementRate)}% → 95% 필요",
                                                    fontSize = 11.sp,
                                                    color = MockupColors.TextMuted
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                RetroButton(
                                    text = stringResource(R.string.share_with_invite_code),
                                    onClick = {
                                        hapticManager.click()
                                        // Analytics: 초대 코드 공유 추적
                                        AnalyticsManager.trackInviteCodeShared()

                                        val shareText = """
🏃 rebon - 걸어서 앱을 해제하세요!

친구가 rebon 앱을 추천했어요.
목표 걸음수를 달성하면 앱이 해제되는 신개념 건강 앱!

📱 앱 다운로드: https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait

🎁 초대 코드: $basicInviteCode
위 코드를 입력하면 1달 무료!
                                        """.trimIndent()

                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, SettingsStrings.shareToFriend())
                                        context.startActivity(shareIntent)
                                    },
                                    backgroundColor = MockupColors.Blue,
                                    fontFamily = kenneyFont
                                )
                            } else {
                                // 프로모션 사용자: 유료 결제 안내
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, MockupColors.Red, RoundedCornerShape(8.dp))
                                        .background(MockupColors.RedLight, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🔒", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.invite_code_after_payment),
                                            fontSize = 13.sp,
                                            color = MockupColors.Red
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                RetroButton(
                                    text = stringResource(R.string.share_app_link),
                                    onClick = {
                                        hapticManager.click()
                                        val shareText = """
🏃 rebon - 걸어서 앱을 해제하세요!

친구가 rebon 앱을 추천했어요.
목표 걸음수를 달성하면 앱이 해제되는 신개념 건강 앱!

📱 앱 다운로드: https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait
                                        """.trimIndent()

                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, SettingsStrings.shareToFriend())
                                        context.startActivity(shareIntent)
                                    },
                                    backgroundColor = MockupColors.Blue,
                                    fontFamily = kenneyFont
                                )
                            }
                        }
                    }

                    // 🔧 디버그 전용: 96% 달성률 설정 버튼
                    if (BuildConfig.DEBUG) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val coroutineScope = rememberCoroutineScope()
                        RetroButton(
                            text = "🔧 [DEBUG] 96% 달성률로 설정",
                            onClick = {
                                hapticManager.click()
                                coroutineScope.launch {
                                    try {
                                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                                        val currentMonthId = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())
                                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                                        // 96% = 24/25
                                        val successDays = 24
                                        val totalDays = 25
                                        val newAchievementRate = 96f

                                        // Firebase 업데이트
                                        db.collection("users").document(uid)
                                            .collection("subscriptions").document(currentMonthId)
                                            .update(mapOf(
                                                "successDays" to successDays,
                                                "totalDays" to totalDays,
                                                "achievementRate" to newAchievementRate,
                                                "earnedFriendCoupon" to true
                                            )).await()

                                        // 로컬 저장소 업데이트
                                        repository?.saveSuccessDays(successDays)

                                        // settings 문서도 업데이트
                                        db.collection("users").document(uid)
                                            .collection("userData").document("settings")
                                            .update(mapOf(
                                                "successDays" to successDays,
                                                "totalDays" to totalDays
                                            )).await()

                                        Toast.makeText(context, "✅ 96% 달성률 설정 완료! 화면을 새로고침하세요", Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "❌ 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            backgroundColor = MockupColors.Orange,
                            fontFamily = kenneyFont
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🎯 앱 제어 (접기/펼치기 가능)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                            .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                            .clickable {
                                hapticManager.click()
                                isAppControlExpanded = !isAppControlExpanded
                            }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.app_control),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary,
                                fontFamily = kenneyFont
                            )
                            Text(
                                text = if (isAppControlExpanded) "▲" else "▼",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextMuted,
                                fontFamily = kenneyFont
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isAppControlExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))

                            // 🎯 목표 설정
                            RetroSectionTitle(title = stringResource(R.string.section_goal), fontFamily = kenneyFont)

                            RetroSettingsItem(
                        title = stringResource(R.string.daily_goal),
                        value = if (goalUnit == "km") "%.2f km".format(goal / 1300.0) else "%,d보".format(goal),
                        onClick = {
                            hapticManager.click()
                            showChangeConfirmDialog = "goal"
                        },
                        fontFamily = kenneyFont
                    )

                    if (preferenceManager?.canDecreaseGoal() == false) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = SettingsStrings.goalDecreaseAvailable(preferenceManager.getNextGoalDecreaseDate()),
                            fontSize = 13.sp,
                            color = MockupColors.Red,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔒 잠금 앱 관리
                    RetroSectionTitle(title = stringResource(R.string.section_locked_apps), fontFamily = kenneyFont)

                    // 로컬 상태 사용 (변경 시 즉시 반영, Firebase 복원 시에도 자동 갱신)
                    val lockedApps = lockedAppsState

                    // 차단 앱 목록 표시
                    if (lockedApps.isNotEmpty()) {
                        val packageManager = context.packageManager
                        val lockedAppItems = remember(lockedApps) {
                            lockedApps.mapNotNull { packageName ->
                                try {
                                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                                    val appName =
                                        packageManager.getApplicationLabel(appInfo).toString()
                                    val iconBitmap =
                                        packageManager.getApplicationIcon(appInfo).toBitmap()
                                            .asImageBitmap()
                                    Triple(packageName, appName, iconBitmap)
                                } catch (e: Exception) {
                                    null
                                }
                            }.sortedBy { it.second }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .border(3.dp, MockupColors.Red, RoundedCornerShape(12.dp))
                                .background(MockupColors.RedLight, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.blocking),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.Red,
                                        fontFamily = kenneyFont
                                    )
                                    Text(
                                        text = "${lockedApps.size}개",
                                        fontSize = 14.sp,
                                        color = MockupColors.TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                lockedAppItems.forEach { (packageName, appName, iconBitmap) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        androidx.compose.foundation.Image(
                                            bitmap = iconBitmap,
                                            contentDescription = appName,
                                            modifier = Modifier.size(28.dp),
                                            colorFilter = ColorFilter.colorMatrix(
                                                ColorMatrix().apply { setToSaturation(0f) }
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = appName,
                                            fontSize = 13.sp,
                                            color = MockupColors.Red,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "X",
                                            fontSize = 14.sp,
                                            fontFamily = kenneyFont,
                                            color = MockupColors.Red
                                        )
                                    }
                                }
                            }
                        }
                    }

                    RetroButton(
                        text = if (lockedApps.isEmpty()) stringResource(R.string.select_apps) else stringResource(R.string.edit_apps),
                        onClick = {
                            hapticManager.click()
                            showAppLockScreen = true
                        },
                        backgroundColor = MockupColors.Red,
                        fontFamily = kenneyFont
                    )

                    if (preferenceManager?.canRemoveLockedApp() == false) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = SettingsStrings.appRemoveAvailable(preferenceManager.getNextAppRemoveDate()),
                            fontSize = 13.sp,
                            color = MockupColors.Red,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // ⏰ 차단 시간대
                    RetroSectionTitle(title = stringResource(R.string.section_blocking_periods), fontFamily = kenneyFont)

                    // 로컬 상태 사용 (변경 시 즉시 반영, Firebase 복원 시에도 자동 갱신)
                    val blockingPeriods = blockingPeriodsState
                    val periodNames = mapOf(
                        "morning" to stringResource(R.string.time_morning),
                        "afternoon" to stringResource(R.string.time_afternoon),
                        "evening" to stringResource(R.string.time_evening),
                        "night" to stringResource(R.string.time_night)
                    )
                    val selectedPeriodNames =
                        blockingPeriods.mapNotNull { periodNames[it] }.joinToString(", ")
                    val displayValue = if (blockingPeriods.isEmpty()) {
                        SettingsStrings.none()
                    } else if (blockingPeriods.size == 4) {
                        SettingsStrings.allDay()
                    } else {
                        selectedPeriodNames
                    }

                    RetroSettingsItem(
                        title = stringResource(R.string.time_period_setting),
                        value = displayValue,
                        onClick = {
                            hapticManager.click()
                            showChangeConfirmDialog = "blockingPeriods"
                        },
                        fontFamily = kenneyFont
                    )

                    if (preferenceManager?.canChangeBlockingPeriods() == false) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = SettingsStrings.periodChangeAvailable(preferenceManager.getNextBlockingPeriodsChangeDate()),
                            fontSize = 13.sp,
                            color = MockupColors.Red,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(2.dp, MockupColors.Blue, RoundedCornerShape(8.dp))
                            .background(MockupColors.BlueLight, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Tip",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.Blue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.blocking_periods_desc),
                                fontSize = 13.sp,
                                color = MockupColors.TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 📅 제어 요일
                    RetroSectionTitle(title = stringResource(R.string.section_control_days), fontFamily = kenneyFont)

                    // 로컬 상태 사용 (변경 시 즉시 반영, Firebase 복원 시에도 자동 갱신)
                    val controlDays = controlDaysState
                    val dayNames2 = listOf(stringResource(R.string.day_sun), stringResource(R.string.day_mon), stringResource(R.string.day_tue), stringResource(R.string.day_wed), stringResource(R.string.day_thu), stringResource(R.string.day_fri), stringResource(R.string.day_sat))
                    val selectedDayNames = controlDays.sorted().map { dayNames2[it] }.joinToString(", ")
                    val displayDays = if (controlDays.isEmpty()) SettingsStrings.none() else selectedDayNames

                    RetroSettingsItem(
                        title = stringResource(R.string.day_setting),
                        value = displayDays,
                        onClick = {
                            hapticManager.click()
                            showChangeConfirmDialog = "controlDays"
                        },
                        fontFamily = kenneyFont
                    )

                            if (preferenceManager?.canChangeControlDays() == false) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = SettingsStrings.dayChangeAvailable(preferenceManager.getNextControlDaysChangeDate()),
                                    fontSize = 13.sp,
                                    color = MockupColors.Red,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // TODO: 15분 휴식 버튼 - 추후 개발 완료 후 활성화
                    // RetroSectionTitle(title = "15분 휴식", fontFamily = kenneyFont)
                    // ... (비활성화됨)

                    // 🏃 피트니스 앱 연결
                    RetroSectionTitle(title = stringResource(R.string.section_fitness), fontFamily = kenneyFont)

                    val healthConnectManager = remember { HealthConnectManager(context) }
                    val isHealthConnectAvailable = remember { healthConnectManager.isAvailable() }
                    val isHealthConnectConnected = preferenceManager?.isHealthConnectConnected() ?: false
                    val connectedAppName = preferenceManager?.getConnectedFitnessAppName() ?: ""

                    // Health Connect 연결 필요 경고 배너
                    if (isHealthConnectAvailable && !isHealthConnectConnected) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFF3E0))
                                .border(1.dp, Color(0xFFFF9800), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = SettingsStrings.healthConnectRequired(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                    Text(
                                        text = stringResource(R.string.connect_fitness_for_accuracy),
                                        fontSize = 12.sp,
                                        color = Color(0xFFF57C00)
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(
                                3.dp,
                                if (isHealthConnectConnected) MockupColors.Blue else MockupColors.Border,
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (isHealthConnectConnected) MockupColors.BlueLight else MockupColors.CardBackground,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    if (isHealthConnectConnected) {
                                        Text(
                                            text = stringResource(R.string.connected),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MockupColors.Blue,
                                            fontFamily = kenneyFont
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (connectedAppName.isNotEmpty())
                                                "$connectedAppName 데이터 사용 중"
                                            else
                                                SettingsStrings.usingAppData(null),
                                            fontSize = 13.sp,
                                            color = MockupColors.TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = SettingsStrings.batterySaverMode(),
                                            fontSize = 13.sp,
                                            color = MockupColors.Blue
                                        )
                                    } else {
                                        Text(
                                            text = stringResource(R.string.step_measurement),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MockupColors.TextPrimary,
                                            fontFamily = kenneyFont
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isHealthConnectAvailable)
                                                "                                                stringResource(R.string.connect_samsung_health_google_fit)"
                                            else
                                                SettingsStrings.healthConnectRequired(),
                                            fontSize = 13.sp,
                                            color = MockupColors.TextSecondary
                                        )
                                    }
                                }
                                Text(
                                    text = if (isHealthConnectConnected) "OK" else "?",
                                    fontSize = 24.sp,
                                    fontFamily = kenneyFont,
                                    color = if (isHealthConnectConnected) MockupColors.Blue else MockupColors.TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            RetroButton(
                                text = if (isHealthConnectConnected) stringResource(R.string.manage) else stringResource(R.string.connect),
                                onClick = {
                                    hapticManager.click()
                                    showFitnessAppConnectionScreen = true
                                },
                                backgroundColor = if (isHealthConnectConnected) MockupColors.Blue else MockupColors.Blue,
                                fontFamily = kenneyFont
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 👤 계정
                    RetroSectionTitle(title = stringResource(R.string.section_account), fontFamily = kenneyFont)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(
                                3.dp,
                                if (isGoogleSignedIn) MockupColors.Blue else MockupColors.Border,
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (isGoogleSignedIn) MockupColors.BlueLight else MockupColors.CardBackground,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = !isGoogleSignedIn && !isGoogleLoading) {
                                hapticManager.click()
                                performGoogleSignIn()
                            }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (isGoogleSignedIn) {
                                    Text(
                                        text = SettingsStrings.connected(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.Blue,
                                        fontFamily = kenneyFont
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = googleEmail,
                                        fontSize = 13.sp,
                                        color = MockupColors.TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.auto_backup_in_progress),
                                        fontSize = 13.sp,
                                        color = MockupColors.Blue
                                    )
                                } else {
                                    Text(
                                        text = SettingsStrings.googleAccount(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.TextPrimary,
                                        fontFamily = kenneyFont
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.connect_for_auto_backup),
                                        fontSize = 13.sp,
                                        color = MockupColors.TextSecondary
                                    )
                                }
                            }
                            if (isGoogleLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MockupColors.Blue,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isGoogleSignedIn) "OK" else "?",
                                    fontSize = 24.sp,
                                    fontFamily = kenneyFont,
                                    color = if (isGoogleSignedIn) MockupColors.Blue else MockupColors.TextMuted
                                )
                            }
                        }
                    }

                    if (!isGoogleSignedIn) {
                        RetroButton(
                            text = SettingsStrings.googleLogin(),
                            onClick = {
                                hapticManager.click()
                                performGoogleSignIn()
                            },
                            backgroundColor = MockupColors.Blue,
                            fontFamily = kenneyFont
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    // 불편사항 접수
                    Spacer(modifier = Modifier.height(16.dp))

                    RetroSectionTitle(stringResource(R.string.section_feedback), kenneyFont)

                    Spacer(modifier = Modifier.height(8.dp))

                    RetroCard(onClick = { showFeedbackDialog = true }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.send_feedback),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.TextPrimary
                                )
                                Text(
                                    text = stringResource(R.string.feedback_desc),
                                    fontSize = 13.sp,
                                    color = MockupColors.TextSecondary
                                )
                            }
                            PixelIcon(iconName = "icon_chat", size = 28.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    // 앱 정보
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "rebon v${BuildConfig.VERSION_NAME}",
                        fontSize = 12.sp,
                        color = MockupColors.TextMuted,
                        fontFamily = kenneyFont,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 개발자 정보
                    Text(
                        text = "© moveoftoday",
                        fontSize = 11.sp,
                        color = MockupColors.TextMuted,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 웹사이트, 인스타그램 링크
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "web",
                            fontSize = 11.sp,
                            color = MockupColors.Blue,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://moveoftoday.life/"))
                                context.startActivity(intent)
                            }
                        )
                        Text(
                            text = "·",
                            fontSize = 11.sp,
                            color = MockupColors.TextMuted
                        )
                        Text(
                            text = "insta",
                            fontSize = 11.sp,
                            color = MockupColors.Blue,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/moveoftoday/"))
                                context.startActivity(intent)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // 🎁 혜택 안내 다이얼로그
            if (showDepositInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showDepositInfoDialog = false },
                    icon = {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "아이콘",
                            tint = StandColors.Primary,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.benefit_info),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PixelIcon(iconName = "icon_trophy", size = 20.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = SettingsStrings.achieve95Percent(),
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.Blue
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.invite_coupon_reward),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.Blue
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = SettingsStrings.couponBenefitDescription(),
                                fontSize = StandTypography.bodyMedium,
                                lineHeight = 22.sp,
                                color = MockupColors.TextPrimary
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PixelIcon(iconName = "icon_chest", size = 20.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.how_to_invite),
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = SettingsStrings.howToUse(),
                                fontSize = StandTypography.bodyMedium,
                                lineHeight = 22.sp,
                                color = MockupColors.TextPrimary
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MockupColors.BlueLight
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        PixelIcon(iconName = "icon_light_bulb", size = 16.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.tip),
                                            fontSize = StandTypography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MockupColors.Blue
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.tip_message),
                                        fontSize = StandTypography.bodySmall,
                                        lineHeight = 18.sp,
                                        color = MockupColors.TextPrimary
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showDepositInfoDialog = false }
                        ) {
                            Text(stringResource(R.string.confirm))
                        }
                    }
                )
            }

            // 3일 제한 확인 팝업 - 레트로 스타일
            showChangeConfirmDialog?.let { type ->
                val title = when (type) {
                    "goal" -> stringResource(R.string.change_goal)
                    "controlDays" -> stringResource(R.string.change_control_days)
                    "blockingPeriods" -> stringResource(R.string.change_blocking_periods)
                    else -> stringResource(R.string.change_settings)
                }
                // 목표는 낮추기만 제한, 요일/시간대는 제거만 제한
                val canRemove = when (type) {
                    "goal" -> preferenceManager?.canDecreaseGoal() ?: true
                    "controlDays" -> preferenceManager?.canChangeControlDays() ?: true
                    "blockingPeriods" -> preferenceManager?.canChangeBlockingPeriods() ?: true
                    else -> true
                }
                val nextDate = when (type) {
                    "goal" -> preferenceManager?.getNextGoalDecreaseDate() ?: ""
                    "controlDays" -> preferenceManager?.getNextControlDaysChangeDate() ?: ""
                    "blockingPeriods" -> preferenceManager?.getNextBlockingPeriodsChangeDate() ?: ""
                    else -> ""
                }
                // 요일/시간대는 추가는 항상 가능
                val isAddRemoveType = type == "controlDays" || type == "blockingPeriods"

                // 오버레이
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showChangeConfirmDialog = null },
                    contentAlignment = Alignment.Center
                ) {
                    // 팝업 카드
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .border(3.dp, MockupColors.Border, RoundedCornerShape(16.dp))
                            .background(MockupColors.Background, RoundedCornerShape(16.dp))
                            .clickable(enabled = false) { }
                            .padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary,
                                fontFamily = kenneyFont
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 요일/시간대는 추가 자유, 제거만 제한 안내
                            if (isAddRemoveType) {
                                Text(
                                    text = stringResource(R.string.add_free_remove_restricted),
                                    fontSize = 15.sp,
                                    color = MockupColors.TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.goal_decrease_restricted),
                                    fontSize = 15.sp,
                                    color = MockupColors.TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                            }

                            if (!canRemove) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, MockupColors.Red, RoundedCornerShape(8.dp))
                                        .background(MockupColors.RedLight, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = if (isAddRemoveType) "제거 가능일: $nextDate" else "변경 가능일: $nextDate",
                                        fontSize = 14.sp,
                                        color = MockupColors.Red,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // 버튼 영역
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 취소 버튼
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(3.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                                        .background(Color.White, RoundedCornerShape(10.dp))
                                        .clickable { showChangeConfirmDialog = null }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.cancel),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.TextPrimary,
                                        fontFamily = kenneyFont
                                    )
                                }

                                // 변경하기 버튼 (추가/제거 타입은 항상 가능, 제거만 제한됨)
                                val canProceed = canRemove || isAddRemoveType
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(3.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                                        .background(
                                            if (canProceed) MockupColors.Red else MockupColors.TextMuted,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable(enabled = canProceed) {
                                            showChangeConfirmDialog = null
                                            when (type) {
                                                "goal" -> showGoalDialog = true
                                                "controlDays" -> showControlDaysDialog = true
                                                "blockingPeriods" -> showBlockingPeriodsDialog = true
                                            }
                                        }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (canProceed) stringResource(R.string.change) else stringResource(R.string.not_allowed),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = kenneyFont
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 펫 변경 다이얼로그
            if (showPetChangeDialog) {
                PetChangeDialog(
                    currentPetType = preferenceManager?.getPetType(),
                    currentPetName = preferenceManager?.getPetName() ?: "",
                    onDismiss = { showPetChangeDialog = false },
                    onConfirm = { newPetType, newPetName ->
                        startPetChangePurchase(newPetType, newPetName)
                    },
                    hapticManager = hapticManager
                )
            }

            // 불편사항 접수 다이얼로그
            if (showFeedbackDialog) {
                FeedbackDialog(
                    onDismiss = { showFeedbackDialog = false },
                    onSubmitted = {
                        Toast.makeText(context, SettingsStrings.feedbackSent(), Toast.LENGTH_SHORT).show()
                        showFeedbackDialog = false
                    },
                    hapticManager = hapticManager
                )
            }

            // 데이터 충돌 선택 다이얼로그
            val remoteInfo = remoteDataInfo
            val localInfo = localDataInfo
            if (showDataConflictDialog && remoteInfo != null && localInfo != null) {
                DataConflictDialog(
                    remoteInfo = remoteInfo,
                    localInfo = localInfo,
                    onUseRemote = {
                        hapticManager.click()
                        handleDataChoice(useRemoteData = true)
                    },
                    onUseLocal = {
                        hapticManager.click()
                        handleDataChoice(useRemoteData = false)
                    },
                    onDismiss = {
                        showDataConflictDialog = false
                        // 취소 시 로그아웃
                        scope.launch {
                            GoogleSignInHelper.signOut(context)
                        }
                    }
                )
            }

        }
    }
}

@Composable
fun BlockingPeriodsDialog(
    currentPeriods: Set<String>,
    canRemove: Boolean,
    nextRemoveDate: String,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>, Boolean) -> Unit  // hasRemovals 추가
) {
    val kenneyFont = rememberKenneyFont()
    var selectedPeriods by remember { mutableStateOf(currentPeriods) }

    val morningHours = stringResource(R.string.time_morning_hours)
    val afternoonHours = stringResource(R.string.time_afternoon_hours)
    val eveningHours = stringResource(R.string.time_evening_hours)
    val nightHours = stringResource(R.string.time_night_hours)
    val periods = listOf(
        "morning" to morningHours,
        "afternoon" to afternoonHours,
        "evening" to eveningHours,
        "night" to nightHours
    )

    // 제거 여부 확인
    val hasRemovals = currentPeriods.any { it !in selectedPeriods }
    // 제거 불가 상태에서 제거하려고 할 때
    val isRemovalBlocked = !canRemove && hasRemovals

    // 풀스크린 스타일 다이얼로그
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 타이틀
            Text(
                text = stringResource(R.string.section_blocking_periods),
                fontSize = 28.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.select_blocking_periods),
                fontSize = 16.sp,
                color = MockupColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 시간대 선택 - 가로 배열
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                periods.forEach { (periodId, label) ->
                    val isSelected = selectedPeriods.contains(periodId)
                    val wasOriginallySelected = currentPeriods.contains(periodId)
                    // 원래 선택되어 있었고 제거 불가 상태면 잠금 표시
                    val isLocked = wasOriginallySelected && !canRemove

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .border(
                                width = if (isSelected) 3.dp else 2.dp,
                                color = when {
                                    isLocked && isSelected -> MockupColors.TextMuted
                                    isSelected -> MockupColors.Border
                                    else -> Color(0xFFE0E0E0)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(
                                when {
                                    isLocked && isSelected -> Color(0xFFE8E8E8)
                                    isSelected -> Color(0xFFE0E0E0)
                                    else -> Color.White
                                },
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (isSelected && isLocked) {
                                    // 잠금 상태에서 해제 시도 - 아무것도 안함 (안내만 표시됨)
                                } else {
                                    selectedPeriods = if (isSelected) {
                                        selectedPeriods - periodId
                                    } else {
                                        selectedPeriods + periodId
                                    }
                                }
                            }
                            .padding(vertical = 16.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isLocked) MockupColors.TextMuted else MockupColors.TextPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            if (isLocked && isSelected) {
                                Text(
                                    text = "🔒",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 안내 (제거 불가 시 다른 안내)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        if (!canRemove) MockupColors.Red else MockupColors.Border,
                        RoundedCornerShape(12.dp)
                    )
                    .background(
                        if (!canRemove) MockupColors.RedLight else Color.White,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    if (!canRemove) {
                        Text(
                            text = "제거 제한 중",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.Red
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "추가만 가능 · 제거 가능일: $nextRemoveDate",
                            fontSize = 14.sp,
                            color = MockupColors.TextSecondary
                        )
                    } else {
                        Text(
                            text = "Tip",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "선택하지 않으면 차단되지 않습니다",
                            fontSize = 14.sp,
                            color = MockupColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 버튼 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 취소 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .clickable { onDismiss() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = SettingsStrings.cancel(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary,
                        fontFamily = kenneyFont
                    )
                }

                // 적용 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(MockupColors.Border, RoundedCornerShape(12.dp))
                        .clickable { onConfirm(selectedPeriods, hasRemovals) }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = SettingsStrings.apply(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = kenneyFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun ControlDaysDialog(
    currentDays: Set<Int>,
    canRemove: Boolean,
    nextRemoveDate: String,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>, Boolean) -> Unit  // hasRemovals 추가
) {
    val kenneyFont = rememberKenneyFont()
    var selectedDays by remember { mutableStateOf(currentDays) }

    val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")

    // 제거 여부 확인
    val hasRemovals = currentDays.any { it !in selectedDays }

    // 풀스크린 스타일 다이얼로그
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 타이틀
            Text(
                text = SettingsStrings.controlDays(),
                fontSize = 28.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "제어할 요일을 선택하세요",
                fontSize = 16.sp,
                color = MockupColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 요일 선택 - 가로 배열
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dayNames.forEachIndexed { index, day ->
                    val isSelected = selectedDays.contains(index)
                    val wasOriginallySelected = currentDays.contains(index)
                    // 원래 선택되어 있었고 제거 불가 상태면 잠금
                    val isLocked = wasOriginallySelected && !canRemove

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isLocked -> MockupColors.TextMuted
                                isSelected -> MockupColors.TextPrimary
                                else -> MockupColors.TextMuted
                            }
                        )
                        if (isLocked && isSelected) {
                            Text(text = "🔒", fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                if (!checked && isLocked) {
                                    // 잠금 상태에서 해제 시도 - 무시
                                } else {
                                    selectedDays = if (checked) {
                                        selectedDays + index
                                    } else {
                                        selectedDays - index
                                    }
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = if (isLocked) MockupColors.TextMuted else MockupColors.Border,
                                uncheckedColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 빠른 선택 버튼 (제거 불가 시 기존 선택 유지하면서 추가만)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    SettingsStrings.weekdays() to setOf(1, 2, 3, 4, 5),
                    SettingsStrings.weekends() to setOf(0, 6),
                    SettingsStrings.everyday() to setOf(0, 1, 2, 3, 4, 5, 6)
                ).forEach { (label, days) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .clickable {
                                if (canRemove) {
                                    selectedDays = days
                                } else {
                                    // 제거 불가 시 기존 선택 유지 + 새로운 것만 추가
                                    selectedDays = currentDays + days
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 안내 (제거 불가 시 다른 안내)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        if (!canRemove) MockupColors.Red else MockupColors.Border,
                        RoundedCornerShape(12.dp)
                    )
                    .background(
                        if (!canRemove) MockupColors.RedLight else Color.White,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    if (!canRemove) {
                        Text(
                            text = "제거 제한 중",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.Red
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "추가만 가능 · 제거 가능일: $nextRemoveDate",
                            fontSize = 14.sp,
                            color = MockupColors.TextSecondary
                        )
                    } else {
                        Text(
                            text = SettingsStrings.recommendWeekdays(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = SettingsStrings.freeWeekends(),
                            fontSize = 14.sp,
                            color = MockupColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 버튼 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 취소 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .clickable { onDismiss() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = SettingsStrings.cancel(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary,
                        fontFamily = kenneyFont
                    )
                }

                // 적용 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(MockupColors.Border, RoundedCornerShape(12.dp))
                        .clickable { onConfirm(selectedDays, hasRemovals) }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = SettingsStrings.apply(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = kenneyFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = StandTypography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = value,
                fontSize = StandTypography.bodyLarge,
                color = MockupColors.Blue,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============ 깔끔한 레트로 스타일 컴포넌트 ============

@Composable
private fun RetroSectionTitle(
    title: String,
    fontFamily: androidx.compose.ui.text.font.FontFamily
) {
    Text(
        text = title,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = MockupColors.TextPrimary,
        fontFamily = fontFamily,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
private fun RetroSettingsItem(
    title: String,
    value: String,
    onClick: () -> Unit,
    fontFamily: androidx.compose.ui.text.font.FontFamily
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
            .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MockupColors.TextPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = MockupColors.Blue,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = ">",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.Border,
                    fontFamily = fontFamily
                )
            }
        }
    }
}

@Composable
private fun RetroButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(3.dp, MockupColors.Border, RoundedCornerShape(10.dp))
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = fontFamily
        )
    }
}

@Composable
private fun RetroMiniButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(2.dp, MockupColors.Border, RoundedCornerShape(6.dp))
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = fontFamily
        )
    }
}

@Composable
private fun RetroCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
            .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        content()
    }
}

/**
 * 펫 변경 다이얼로그 (V2)
 */
@Composable
private fun PetChangeDialog(
    currentPetType: String?,
    currentPetName: String,
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

    // 풀스크린 오버레이
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // 다이얼로그 카드
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {} // 클릭 이벤트 전파 방지
                .border(4.dp, MockupColors.Border, RoundedCornerShape(16.dp))
                .background(MockupColors.Background, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 타이틀
                Text(
                    text = SettingsStrings.changePet(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = kenneyFont,
                    color = MockupColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = SettingsStrings.selectNewFriend(),
                    fontSize = 14.sp,
                    color = MockupColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 펫 선택 그리드 (3x2) - V2 펫 6종, Baby 모습
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1 (3마리)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        petTypes.take(3).forEach { pet ->
                            val isSelected = selectedPet == pet
                            Card(
                                onClick = {
                                    hapticManager.click()
                                    selectedPet = pet
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFD0D0D0) else MockupColors.CardBackground
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 3.dp else 2.dp,
                                    color = MockupColors.Border
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PetSpriteV2WithGlow(
                                        petType = pet,
                                        stage = PetGrowthStage.BABY,
                                        animationType = PetAnimationTypeV2.IDLE,
                                        size = 56.dp,
                                        monochrome = true,
                                        showGlow = false
                                    )
                                }
                            }
                        }
                    }
                    // Row 2 (3마리)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        petTypes.drop(3).take(3).forEach { pet ->
                            val isSelected = selectedPet == pet
                            Card(
                                onClick = {
                                    hapticManager.click()
                                    selectedPet = pet
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFD0D0D0) else MockupColors.CardBackground
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 3.dp else 2.dp,
                                    color = MockupColors.Border
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PetSpriteV2WithGlow(
                                        petType = pet,
                                        stage = PetGrowthStage.BABY,
                                        animationType = PetAnimationTypeV2.IDLE,
                                        size = 56.dp,
                                        monochrome = true,
                                        showGlow = false
                                    )
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
                    label = { Text(SettingsStrings.petName()) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MockupColors.Border,
                        unfocusedBorderColor = Color(0xFFCCCCCC)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 가격 안내
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                        .background(MockupColors.Background, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SettingsStrings.petChangeCost(),
                            fontSize = 14.sp,
                            color = MockupColors.TextSecondary
                        )
                        Text(
                            text = "₩1,000",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary,
                            fontFamily = kenneyFont
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 취소 버튼
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(3.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .clickable {
                                hapticManager.click()
                                onDismiss()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = SettingsStrings.cancel(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary,
                            fontFamily = kenneyFont
                        )
                    }

                    // 결제 버튼
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(3.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                            .background(
                                if (selectedPet != null && petName.isNotBlank()) MockupColors.Border
                                else MockupColors.TextMuted,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = selectedPet != null && petName.isNotBlank()) {
                                hapticManager.success()
                                selectedPet?.let { pet ->
                                    onConfirm(pet, petName)
                                }
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = SettingsStrings.checkout(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = kenneyFont
                        )
                    }
                }
            }
        }
    }
}

/**
 * 불편사항 접수 다이얼로그
 */
@Composable
private fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSubmitted: () -> Unit,
    hapticManager: HapticManager
) {
    val context = LocalContext.current
    val kenneyFont = rememberKenneyFont()
    val scope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf(FeedbackManager.Category.BUG) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var screenshotUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        screenshotUri = uri
    }

    val categories = FeedbackManager.Category.entries.toList()

    // 풀스크린 다이얼로그
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                        .background(MockupColors.Background, RoundedCornerShape(8.dp))
                        .clickable {
                            hapticManager.click()
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "<",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.Border,
                        fontFamily = kenneyFont
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = SettingsStrings.submitFeedback(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary,
                    fontFamily = kenneyFont
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 카테고리 선택
            Text(
                text = "분류",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.take(3).forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isSelected) 3.dp else 2.dp,
                                color = if (isSelected) MockupColors.Blue else MockupColors.Border,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                if (isSelected) MockupColors.BlueLight else MockupColors.CardBackground,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.displayName,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MockupColors.Blue else MockupColors.TextPrimary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.drop(3).forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isSelected) 3.dp else 2.dp,
                                color = if (isSelected) MockupColors.Blue else MockupColors.Border,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                if (isSelected) MockupColors.BlueLight else MockupColors.CardBackground,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.displayName,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MockupColors.Blue else MockupColors.TextPrimary
                        )
                    }
                }
                // 빈 공간 채우기
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 제목
            Text(
                text = "제목",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 50) title = it },
                placeholder = { Text("간단한 제목을 입력하세요") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MockupColors.Border,
                    unfocusedBorderColor = Color(0xFFCCCCCC)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 내용
            Text(
                text = "내용",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { if (it.length <= 500) content = it },
                placeholder = { Text("자세한 내용을 입력하세요\n\n어떤 상황에서 문제가 발생했는지,\n기대했던 동작은 무엇인지 알려주세요.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MockupColors.Border,
                    unfocusedBorderColor = Color(0xFFCCCCCC)
                )
            )
            Text(
                text = "${content.length}/500",
                fontSize = 12.sp,
                color = MockupColors.TextMuted,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 스크린샷 첨부
            Text(
                text = "스크린샷 (선택)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(
                        width = 2.dp,
                        color = if (screenshotUri != null) MockupColors.Blue else Color(0xFFCCCCCC),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        if (screenshotUri != null) MockupColors.BlueLight else MockupColors.CardBackground,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        hapticManager.click()
                        imagePickerLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                if (screenshotUri != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "OK",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.Blue,
                            fontFamily = kenneyFont
                        )
                        Column {
                            Text(
                                text = "이미지 첨부됨",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.Blue
                            )
                            Text(
                                text = "탭하여 변경",
                                fontSize = 12.sp,
                                color = MockupColors.TextSecondary
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "+",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextMuted,
                            fontFamily = kenneyFont
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "탭하여 이미지 선택",
                            fontSize = 13.sp,
                            color = MockupColors.TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 제출 버튼
            val canSubmit = title.isNotBlank() && content.isNotBlank() && !isSubmitting

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                    .background(
                        if (canSubmit) MockupColors.Blue else MockupColors.TextMuted,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = canSubmit) {
                        hapticManager.success()
                        isSubmitting = true
                        scope.launch {
                            val result = FeedbackManager.submitFeedback(
                                context = context,
                                category = selectedCategory,
                                title = title,
                                content = content,
                                screenshotUri = screenshotUri
                            )
                            isSubmitting = false
                            if (result.isSuccess) {
                                onSubmitted()
                            } else {
                                Toast
                                    .makeText(
                                        context,
                                        "전송 실패: ${result.exceptionOrNull()?.message}",
                                        Toast.LENGTH_LONG
                                    )
                                    .show()
                            }
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                }else {
                    Text(
                        text = "접수하기",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = kenneyFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 안내 문구
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                    .background(MockupColors.CardBackground, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "안내",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "접수된 내용은 빠른 시일 내에 검토하겠습니다.\n개인정보는 문의 처리 목적으로만 사용됩니다.",
                        fontSize = 12.sp,
                        color = MockupColors.TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

/**
 * 데이터 충돌 선택 다이얼로그
 * Google 로그인 시 기존 데이터와 현재 데이터가 모두 있을 때 표시
 */
@Composable
private fun DataConflictDialog(
    remoteInfo: RemoteDataInfo,
    localInfo: LocalDataInfo,
    onUseRemote: () -> Unit,
    onUseLocal: () -> Unit,
    onDismiss: () -> Unit
) {
    val kenneyFont = rememberKenneyFont()

    // 펫 타입 이름 변환
    fun getPetDisplayName(petType: String?): String {
        return when (petType) {
            "DOG1" -> "강아지"
            "CAT" -> "고양이"
            "RAT" -> "쥐"
            "HAMSTER" -> "햄스터"
            "RABBIT" -> "토끼"
            else -> petType ?: "기본"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(4.dp, MockupColors.Border, RoundedCornerShape(16.dp))
                .background(MockupColors.CardBackground, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 제목
            Text(
                text = SettingsStrings.dataSelection(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = kenneyFont,
                color = MockupColors.Orange
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = SettingsStrings.dataConflictMessage(),
                fontSize = 14.sp,
                color = MockupColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 기존 데이터 (Google)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, MockupColors.Blue, RoundedCornerShape(12.dp))
                    .background(MockupColors.BlueLight, RoundedCornerShape(12.dp))
                    .clickable { onUseRemote() }
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "☁️",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = SettingsStrings.restoreExistingData(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = kenneyFont,
                            color = MockupColors.Blue
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = SettingsStrings.petInfo(remoteInfo.petName, SettingsStrings.getPetDisplayName(remoteInfo.petType)),
                        fontSize = 14.sp,
                        color = MockupColors.TextPrimary
                    )
                    Text(
                        text = SettingsStrings.streakAndSteps(remoteInfo.streak, remoteInfo.petTotalSteps),
                        fontSize = 13.sp,
                        color = MockupColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "VS",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = kenneyFont,
                color = MockupColors.TextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 현재 데이터 (로컬)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, MockupColors.Green, RoundedCornerShape(12.dp))
                    .background(MockupColors.GreenLight, RoundedCornerShape(12.dp))
                    .clickable { onUseLocal() }
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📱",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = SettingsStrings.keepCurrentData(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = kenneyFont,
                            color = MockupColors.Green
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = SettingsStrings.petInfo(localInfo.petName, SettingsStrings.getPetDisplayName(localInfo.petType)),
                        fontSize = 14.sp,
                        color = MockupColors.TextPrimary
                    )
                    Text(
                        text = SettingsStrings.streakAndSteps(localInfo.streak, localInfo.petTotalSteps),
                        fontSize = 13.sp,
                        color = MockupColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 취소 버튼
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = SettingsStrings.cancel(),
                    fontSize = 14.sp,
                    color = MockupColors.TextMuted
                )
            }
        }
    }
}
