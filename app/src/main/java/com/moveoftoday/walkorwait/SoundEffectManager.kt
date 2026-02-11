package com.moveoftoday.walkorwait

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

class SoundEffectManager(context: Context) {
    private val TAG = "SoundEffectManager"

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var dingDongSound: Int = -1
    private var loaded = false

    init {
        // 사운드 파일 로드
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                loaded = true
                Log.d(TAG, "Sound effects loaded successfully")
            } else {
                Log.e(TAG, "Failed to load sound effects")
            }
        }

        // ding_dong.mp3 로드
        // TODO: res/raw/ding_dong.mp3 파일을 추가해야 합니다
        try {
            // 임시로 주석 처리 - 사운드 파일이 없어도 앱이 크래시하지 않도록
            // dingDongSound = soundPool.load(context, R.raw.ding_dong, 1)
            Log.w(TAG, "Sound file not loaded - ding_dong.mp3 not available")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load ding_dong sound: ${e.message}")
        }
    }

    fun playDingDong() {
        if (loaded && dingDongSound != -1) {
            soundPool.play(dingDongSound, 1.0f, 1.0f, 1, 0, 1.0f)
            Log.d(TAG, "Playing ding dong sound")
        } else {
            Log.w(TAG, "Ding dong sound not loaded yet")
        }
    }

    fun release() {
        soundPool.release()
        Log.d(TAG, "Sound pool released")
    }
}
