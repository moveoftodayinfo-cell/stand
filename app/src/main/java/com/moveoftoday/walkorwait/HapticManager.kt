package com.moveoftoday.walkorwait

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * 햅틱 피드백 관리 클래스
 * 스탠드 불빛 컨셉에 맞춘 다양한 햅틱 효과
 */
class HapticManager(private val context: Context) {

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    /**
     * 💡 불빛 켜기 - 목표 달성 시
     * 부드럽게 점점 강해지는 진동
     */
    fun lightOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 50, 50, 100, 50, 150)
            val amplitudes = intArrayOf(0, 80, 0, 150, 0, 255)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 50, 100, 50, 150), -1)
        }
    }

    /**
     * 🚫 차단 - 앱 차단 시도 시
     * 단호한 두 번의 짧은 진동
     */
    fun blocked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(
                longArrayOf(0, 30, 100, 30),
                intArrayOf(0, 200, 0, 200),
                -1
            )
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 30, 100, 30), -1)
        }
    }

    /**
     * ✨ 성공 - 긍정적인 액션
     * 경쾌한 한 번의 진동
     */
    fun success() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    /**
     * ⚠️ 경고 - 주의가 필요한 상황
     * 강한 한 번의 진동
     */
    fun warning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(100, 200)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }

    /**
     * 👆 클릭 - 일반 버튼 클릭
     * 매우 짧은 가벼운 진동
     */
    fun click() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10)
        }
    }

    /**
     * 👆 가벼운 클릭 - 부드러운 인터랙션
     * 아주 가벼운 진동
     */
    fun lightClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            vibrator.vibrate(effect)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(5, 50)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(5)
        }
    }

    /**
     * 💪 중요 클릭 - 중요한 액션 (결제, 확인 등)
     * 조금 더 강한 클릭
     */
    fun heavyClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }

    /**
     * 🆘 긴급 모드 활성화
     * 특별한 패턴
     */
    fun emergencyMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(
                longArrayOf(0, 50, 50, 50, 50, 100),
                intArrayOf(0, 150, 0, 150, 0, 255),
                -1
            )
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 50, 50, 50, 100), -1)
        }
    }

    /**
     * 🎉 목표 완료 - 일일 목표 달성
     * 축하하는 느낌의 진동 패턴
     */
    fun goalAchieved() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(
                longArrayOf(0, 50, 50, 50, 50, 50, 100, 200),
                intArrayOf(0, 100, 0, 150, 0, 200, 0, 255),
                -1
            )
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 50, 50, 50, 50, 100, 200), -1)
        }
    }

    /**
     * 진동 지원 여부 확인
     */
    fun hasVibrator(): Boolean {
        return vibrator.hasVibrator()
    }
}

/**
 * Compose용 햅틱 확장 함수
 */
fun HapticFeedback.performClick() {
    performHapticFeedback(HapticFeedbackType.LongPress)
}
