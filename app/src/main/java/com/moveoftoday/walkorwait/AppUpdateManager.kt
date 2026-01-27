package com.moveoftoday.walkorwait

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await

/**
 * 앱 업데이트 관리자
 * Firebase Remote Config를 사용하여 최신 버전 확인
 */
object AppUpdateManager {
    private const val TAG = "AppUpdateManager"

    // Remote Config 키
    private const val KEY_LATEST_VERSION_CODE = "latest_version_code"
    private const val KEY_LATEST_VERSION_NAME = "latest_version_name"
    private const val KEY_FORCE_UPDATE_VERSION_CODE = "force_update_version_code"
    private const val KEY_UPDATE_MESSAGE = "update_message"
    private const val KEY_PLAY_STORE_URL = "play_store_url"

    // 기본값
    private const val DEFAULT_PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait"

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance().apply {
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0) // 테스트용: 캐시 없음 (배포 시 3600으로 변경)
                .build()
            setConfigSettingsAsync(configSettings)

            // 기본값 설정
            setDefaultsAsync(mapOf(
                KEY_LATEST_VERSION_CODE to 1L,
                KEY_LATEST_VERSION_NAME to "1.0.0",
                KEY_FORCE_UPDATE_VERSION_CODE to 1L,
                KEY_UPDATE_MESSAGE to "새로운 버전이 출시되었습니다.\n더 나은 서비스를 위해 업데이트해주세요.",
                KEY_PLAY_STORE_URL to DEFAULT_PLAY_STORE_URL
            ))
        }
    }

    data class UpdateInfo(
        val isUpdateAvailable: Boolean,
        val isForceUpdate: Boolean,
        val latestVersionName: String,
        val currentVersionName: String,
        val updateMessage: String,
        val playStoreUrl: String
    )

    /**
     * 업데이트 확인
     */
    suspend fun checkForUpdate(context: Context): UpdateInfo {
        return try {
            // Remote Config 가져오기
            remoteConfig.fetchAndActivate().await()

            val latestVersionCode = remoteConfig.getLong(KEY_LATEST_VERSION_CODE)
            val latestVersionName = remoteConfig.getString(KEY_LATEST_VERSION_NAME)
            val forceUpdateVersionCode = remoteConfig.getLong(KEY_FORCE_UPDATE_VERSION_CODE)
            val updateMessage = remoteConfig.getString(KEY_UPDATE_MESSAGE)
            val playStoreUrl = remoteConfig.getString(KEY_PLAY_STORE_URL).ifEmpty { DEFAULT_PLAY_STORE_URL }

            // 현재 앱 버전 가져오기
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            val currentVersionName = packageInfo.versionName ?: "Unknown"

            Log.d(TAG, "Current: $currentVersionCode ($currentVersionName), Latest: $latestVersionCode ($latestVersionName), Force: $forceUpdateVersionCode")

            val isUpdateAvailable = currentVersionCode < latestVersionCode
            val isForceUpdate = currentVersionCode < forceUpdateVersionCode

            UpdateInfo(
                isUpdateAvailable = isUpdateAvailable,
                isForceUpdate = isForceUpdate,
                latestVersionName = latestVersionName,
                currentVersionName = currentVersionName,
                updateMessage = updateMessage,
                playStoreUrl = playStoreUrl
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check for update: ${e.message}")
            // 오류 시 업데이트 없음으로 처리
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            UpdateInfo(
                isUpdateAvailable = false,
                isForceUpdate = false,
                latestVersionName = "",
                currentVersionName = packageInfo.versionName ?: "Unknown",
                updateMessage = "",
                playStoreUrl = DEFAULT_PLAY_STORE_URL
            )
        }
    }

    /**
     * Play Store로 이동
     */
    fun openPlayStore(context: Context, url: String = DEFAULT_PLAY_STORE_URL) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Play Store: ${e.message}")
        }
    }
}
