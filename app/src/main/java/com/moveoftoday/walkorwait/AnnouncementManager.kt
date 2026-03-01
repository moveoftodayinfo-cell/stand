package com.moveoftoday.walkorwait

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Locale

/**
 * 서버 제어 공지/팝업 관리 (다국어 지원)
 *
 * Firestore 구조:
 * announcements/
 *   └── current/
 *         ├── isActive: Boolean
 *         ├── type: String ("event", "update", "notice")
 *         ├── title_ko: String, title_en: String, title_ja: String, ...
 *         ├── message_ko: String, message_en: String, message_ja: String, ...
 *         ├── imageUrl: String? (선택)
 *         ├── primaryButtonText_ko: String, primaryButtonText_en: String, ...
 *         ├── primaryButtonAction: String ("dismiss", "url", "promo", "update")
 *         ├── primaryButtonUrl: String? (action이 url일 때)
 *         ├── secondaryButtonText_ko: String?, secondaryButtonText_en: String?, ...
 *         ├── minVersion: Int (이 버전 이상만 표시)
 *         ├── maxVersion: Int? (이 버전 이하만 표시, 업데이트 알림용)
 *         ├── startDate: Timestamp?
 *         ├── endDate: Timestamp?
 *         └── dismissible: Boolean (닫기 가능 여부)
 *
 * 다국어 필드 우선순위: {field}_{lang} → {field}_en → {field} (레거시 호환)
 * 지원 언어: ko, en, ja, zh, es, hi
 */
class AnnouncementManager(private val context: Context) {
    private val TAG = "AnnouncementManager"
    private val db: FirebaseFirestore = Firebase.firestore
    private val preferenceManager = PreferenceManager(context)

    data class Announcement(
        val id: String,
        val isActive: Boolean,
        val type: String,           // "event", "update", "notice"
        val title: String,
        val message: String,
        val imageUrl: String?,
        val primaryButtonText: String,
        val primaryButtonAction: String,  // "dismiss", "url", "promo", "update"
        val primaryButtonUrl: String?,
        val secondaryButtonText: String?,
        val minVersion: Int,
        val maxVersion: Int?,
        val dismissible: Boolean
    )

    /**
     * 다국어 필드 읽기
     * 우선순위: {field}_{lang} → {field}_en → {field} (레거시 호환)
     */
    private fun getLocalizedString(
        doc: DocumentSnapshot,
        field: String,
        default: String = ""
    ): String {
        val lang = Locale.getDefault().language
        return doc.getString("${field}_$lang")
            ?: doc.getString("${field}_en")
            ?: doc.getString(field)
            ?: default
    }

    /**
     * 현재 표시할 공지 가져오기
     */
    suspend fun getActiveAnnouncement(): Announcement? {
        try {
            val doc = db.collection("announcements")
                .document("current")
                .get()
                .await()

            if (!doc.exists()) {
                Log.d(TAG, "No announcement document")
                return null
            }

            val isActive = doc.getBoolean("isActive") ?: false
            if (!isActive) {
                Log.d(TAG, "Announcement is not active")
                return null
            }

            // 버전 체크
            val currentVersion = BuildConfig.VERSION_CODE
            val minVersion = doc.getLong("minVersion")?.toInt() ?: 0
            val maxVersion = doc.getLong("maxVersion")?.toInt()

            if (currentVersion < minVersion) {
                Log.d(TAG, "Version too low: $currentVersion < $minVersion")
                return null
            }

            if (maxVersion != null && currentVersion > maxVersion) {
                Log.d(TAG, "Version too high: $currentVersion > $maxVersion")
                return null
            }

            // 오늘 그만보기 했는지 확인
            val announcementId = doc.getString("id") ?: doc.id
            if (preferenceManager.isAnnouncementDismissedToday(announcementId)) {
                Log.d(TAG, "Announcement dismissed today: $announcementId")
                return null
            }

            val lang = Locale.getDefault().language
            Log.d(TAG, "Loading announcement for locale: $lang")

            return Announcement(
                id = announcementId,
                isActive = true,
                type = doc.getString("type") ?: "notice",
                title = getLocalizedString(doc, "title"),
                message = getLocalizedString(doc, "message"),
                imageUrl = doc.getString("imageUrl"),
                primaryButtonText = getLocalizedString(doc, "primaryButtonText", "OK"),
                primaryButtonAction = doc.getString("primaryButtonAction") ?: "dismiss",
                primaryButtonUrl = doc.getString("primaryButtonUrl"),
                secondaryButtonText = getLocalizedString(doc, "secondaryButtonText").ifEmpty { null },
                minVersion = minVersion,
                maxVersion = maxVersion,
                dismissible = doc.getBoolean("dismissible") ?: true
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get announcement: ${e.message}")
            return null
        }
    }

    /**
     * 오늘 그만보기
     */
    fun dismissForToday(announcementId: String) {
        preferenceManager.setAnnouncementDismissedToday(announcementId)
        Log.d(TAG, "Dismissed announcement for today: $announcementId")
    }
}
