package com.moveoftoday.walkorwait

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import com.moveoftoday.walkorwait.pet.PetTypeV2
import com.moveoftoday.walkorwait.pet.PetDialoguesV2
import kotlinx.coroutines.*
import java.util.Locale
import kotlin.math.sqrt

class ExerciseSensorManager(private val context: Context) : SensorEventListener {
    private val TAG = "ExerciseSensorManager"
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Lifecycle-managed CoroutineScope
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // 햅틱 및 사운드 매니저
    private val hapticManager = HapticFeedbackManager(context)
    private var soundEffectManager: SoundEffectManager? = null

    // TTS 음성 피드백
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    // 스쿼트 감지 상태
    private var squatState = SquatState.IDLE
    private var currentReps = 0
    private var targetReps = 0
    private var lastRepTime = 0L

    // 스쿼트 감지 파라미터
    private var previousMagnitude = 0f
    private var downMagnitude = 0f            // DOWN 감지 시점의 magnitude
    private val squatDownThreshold = 8.0f     // 앉을 때 임계값 (더 낮춤)
    private val squatUpThreshold = 11.0f      // 일어설 때 임계값 (더 높임)
    private val squatMinDelta = 2.5f          // DOWN→UP 최소 변화량 (걷기 필터링)
    private val squatMinInterval = 800L       // 최소 간격 (밀리초)

    // 펫 정보 (음성 메시지용)
    private var petType: PetTypeV2? = null

    // 피드백 설정
    private val prefs = PreferenceManager(context)
    private val voiceEnabled: Boolean get() = prefs.getExerciseVoiceEnabled()
    private val soundEnabled: Boolean get() = prefs.getExerciseSoundEnabled()
    private val hapticEnabled: Boolean get() = prefs.getExerciseHapticEnabled()

    // 콜백
    var onSquatDetected: ((Int) -> Unit)? = null

    enum class SquatState {
        IDLE,
        DOWN_DETECTED
    }

    init {
        // TTS 초기화
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.KOREAN)
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
                Log.d(TAG, "TTS initialized: ready=$ttsReady")
            }
        }

        // SoundEffectManager 초기화
        soundEffectManager = SoundEffectManager(context)
    }

    fun startListening(targetReps: Int, petType: PetTypeV2? = null) {
        this.targetReps = targetReps
        this.petType = petType
        this.currentReps = 0
        this.squatState = SquatState.IDLE
        this.lastRepTime = System.currentTimeMillis()  // 초기값 설정
        this.previousMagnitude = 0f
        this.downMagnitude = 0f

        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI  // 60ms 간격 (배터리 절약)
            )
            Log.d(TAG, "Started listening for squats (target: $targetReps, downThreshold: $squatDownThreshold, upThreshold: $squatUpThreshold, minDelta: $squatMinDelta)")
        } ?: run {
            Log.e(TAG, "Accelerometer not available")
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
        Log.d(TAG, "Stopped listening for squats")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            detectSquat(event)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    private fun detectSquat(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // 전체 가속도 크기
        val magnitude = sqrt(x * x + y * y + z * z)
        val currentTime = System.currentTimeMillis()

        previousMagnitude = magnitude

        when (squatState) {
            SquatState.IDLE -> {
                // 앉기 동작 감지 (가속도 감소) - 폰 방향 무관
                if (magnitude < squatDownThreshold) {
                    squatState = SquatState.DOWN_DETECTED
                    downMagnitude = magnitude  // DOWN 시점 magnitude 저장
                    Log.d(TAG, "Squat DOWN detected (magnitude: $magnitude)")
                }
            }

            SquatState.DOWN_DETECTED -> {
                // 일어서기 동작 감지 (가속도 증가) - 폰 방향 무관
                val delta = magnitude - downMagnitude

                if (magnitude > squatUpThreshold && delta >= squatMinDelta) {
                    // 최소 간격 체크
                    if (currentTime - lastRepTime > squatMinInterval) {
                        // 횟수 증가!
                        currentReps++
                        lastRepTime = currentTime

                        Log.d(TAG, "✅ Squat completed! Total: $currentReps/$targetReps (UP: $magnitude, DOWN: $downMagnitude, delta: $delta)")

                        // 콜백 호출
                        onSquatDetected?.invoke(currentReps)

                        // 피드백 제공
                        provideFeedback(currentReps, targetReps)
                    }

                    squatState = SquatState.IDLE
                } else if (magnitude < squatDownThreshold) {
                    // 계속 낮은 상태 유지 - DOWN magnitude 업데이트
                    downMagnitude = magnitude
                }
            }
        }
    }

    private fun provideFeedback(reps: Int, targetReps: Int) {
        // 1. 햅틱 피드백
        if (hapticEnabled) {
            provideHapticFeedback(reps, targetReps)
        }

        // 2. 사운드 효과 (10개 단위)
        if (soundEnabled && reps % 10 == 0) {
            soundEffectManager?.playDingDong()
        }

        // 3. 음성 피드백 (10개 단위 + 완료)
        if (voiceEnabled) {
            provideVoiceFeedback(reps, targetReps)
        }
    }

    private fun provideHapticFeedback(reps: Int, targetReps: Int) {
        scope.launch {
            when {
                reps == targetReps -> {
                    // 완료! 3번 진동 (축하)
                    repeat(3) {
                        hapticManager.mediumClick()
                        delay(100)
                    }
                }
                reps % 10 == 0 -> {
                    // 10개 단위: 자리수만큼 진동 (10=1번, 20=2번, 30=3번)
                    val times = reps / 10
                    repeat(times) {
                        hapticManager.mediumClick()
                        delay(100)
                    }
                }
                else -> {
                    // 일반 카운트: 가볍게 1번
                    hapticManager.lightClick()
                }
            }
        }
    }

    private fun provideVoiceFeedback(reps: Int, targetReps: Int) {
        if (!ttsReady) return

        val message = when {
            reps == targetReps -> {
                // 완료 메시지 (펫 성격별)
                petType?.let {
                    PetDialoguesV2.getExerciseCompleteMessage(it, reps)
                } ?: "${reps}개 완료! 성공!"
            }
            reps == targetReps - 1 -> {
                "마지막 1개!"
            }
            reps % 10 == 0 -> {
                // 10개 단위 응원 (펫 성격별)
                petType?.let {
                    PetDialoguesV2.getExerciseEncouragement(it, reps)
                } ?: "${reps}개"
            }
            else -> null
        }

        message?.let {
            tts?.speak(it, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun cleanup() {
        scope.cancel()  // CoroutineScope 취소
        stopListening()
        tts?.stop()
        tts?.shutdown()
        soundEffectManager?.release()
    }
}
