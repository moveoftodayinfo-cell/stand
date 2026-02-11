package com.moveoftoday.walkorwait

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class HapticFeedbackManager(private val context: Context) {
    private val TAG = "HapticFeedbackManager"

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * 가벼운 클릭 (햅틱 피드백)
     * 매 스쿼트 카운트 시 사용
     */
    fun lightClick() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(10)
            }
            Log.d(TAG, "Light click haptic")
        }
    }

    /**
     * 중간 세기 클릭
     * 10개 단위 달성 시 사용
     */
    fun mediumClick() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30)
            }
            Log.d(TAG, "Medium click haptic")
        }
    }

    /**
     * 강한 진동 패턴
     * 챌린지 완료 시 사용 (3번 연속)
     */
    fun successPattern() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // 패턴: 진동-쉼-진동-쉼-진동
                val pattern = longArrayOf(0, 100, 100, 100, 100, 100)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)

                vibrator.vibrate(
                    VibrationEffect.createWaveform(pattern, amplitudes, -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 100, 100, 100, 100, 100), -1)
            }
            Log.d(TAG, "Success pattern haptic")
        }
    }

    /**
     * 커스텀 패턴 진동
     * @param times 진동 횟수 (10의 자리수에 해당)
     */
    fun patternForTens(times: Int) {
        if (vibrator?.hasVibrator() == true && times > 0) {
            val pattern = mutableListOf<Long>()
            val amplitudes = mutableListOf<Int>()

            pattern.add(0) // 시작 딜레이
            amplitudes.add(0)

            repeat(times) {
                pattern.add(80) // 진동
                pattern.add(100) // 쉼
                amplitudes.add(200)
                amplitudes.add(0)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        pattern.toLongArray(),
                        amplitudes.toIntArray(),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern.toLongArray(), -1)
            }
            Log.d(TAG, "Pattern haptic: $times times")
        }
    }
}
