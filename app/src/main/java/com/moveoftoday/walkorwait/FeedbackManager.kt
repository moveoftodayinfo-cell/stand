package com.moveoftoday.walkorwait

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.Date

/**
 * 불편사항/피드백 관리자
 * 사용자 피드백을 Firebase에 저장하고 관리
 */
object FeedbackManager {
    private const val TAG = "FeedbackManager"
    private const val COLLECTION_FEEDBACK = "feedback"

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    /**
     * 피드백 카테고리
     */
    enum class Category(val displayName: String) {
        BUG("버그/오류"),
        SUGGESTION("기능 제안"),
        UI("UI/디자인"),
        PAYMENT("결제 문제"),
        OTHER("기타")
    }

    /**
     * 피드백 상태
     */
    enum class Status(val displayName: String) {
        PENDING("대기중"),
        IN_PROGRESS("처리중"),
        RESOLVED("해결됨"),
        CLOSED("종료")
    }

    /**
     * 피드백 제출
     */
    suspend fun submitFeedback(
        context: Context,
        category: Category,
        title: String,
        content: String,
        screenshotUri: Uri? = null
    ): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: "anonymous"
            val userEmail = auth.currentUser?.email ?: "unknown"

            // 스크린샷을 Base64로 변환 (있는 경우)
            val screenshotBase64 = screenshotUri?.let { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    // 이미지 크기 제한 (1MB 이하로 압축)
                    if (bytes != null && bytes.size > 1024 * 1024) {
                        // 큰 이미지는 압축
                        compressImage(context, uri)
                    } else {
                        bytes?.let { Base64.encodeToString(it, Base64.DEFAULT) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to encode screenshot: ${e.message}")
                    null
                }
            }

            // 디바이스 정보
            val deviceInfo = mapOf(
                "model" to android.os.Build.MODEL,
                "manufacturer" to android.os.Build.MANUFACTURER,
                "androidVersion" to android.os.Build.VERSION.RELEASE,
                "sdkVersion" to android.os.Build.VERSION.SDK_INT,
                "appVersion" to try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                } catch (e: Exception) { "unknown" }
            )

            val feedback = hashMapOf(
                "userId" to userId,
                "userEmail" to userEmail,
                "category" to category.name,
                "title" to title,
                "content" to content,
                "screenshot" to screenshotBase64,
                "hasScreenshot" to (screenshotBase64 != null),
                "deviceInfo" to deviceInfo,
                "status" to Status.PENDING.name,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "updatedAt" to com.google.firebase.Timestamp.now(),
                "adminNote" to ""
            )

            val docRef = db.collection(COLLECTION_FEEDBACK).add(feedback).await()
            Log.d(TAG, "Feedback submitted: ${docRef.id}")

            // Analytics 추적
            AnalyticsManager.trackSettingsChanged("feedback_submitted", category.name)

            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit feedback: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * 이미지 압축
     */
    private fun compressImage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // 크기 조정 (최대 800px)
            val maxSize = 800
            val scale = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height, 1f)
            val scaledBitmap = Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compress image: ${e.message}")
            null
        }
    }

    /**
     * 모든 피드백 가져오기 (관리자용)
     */
    suspend fun getAllFeedback(limit: Int = 100): List<Feedback> {
        return try {
            val snapshot = db.collection(COLLECTION_FEEDBACK)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    Feedback(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userEmail = doc.getString("userEmail") ?: "",
                        category = doc.getString("category") ?: "",
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        hasScreenshot = doc.getBoolean("hasScreenshot") ?: false,
                        screenshot = doc.getString("screenshot"),
                        status = doc.getString("status") ?: Status.PENDING.name,
                        createdAt = doc.getTimestamp("createdAt")?.toDate() ?: Date(),
                        adminNote = doc.getString("adminNote") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get feedback: ${e.message}")
            emptyList()
        }
    }

    /**
     * 피드백 상태 업데이트 (관리자용)
     */
    suspend fun updateFeedbackStatus(feedbackId: String, status: Status, adminNote: String = ""): Boolean {
        return try {
            db.collection(COLLECTION_FEEDBACK).document(feedbackId)
                .update(mapOf(
                    "status" to status.name,
                    "adminNote" to adminNote,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                ))
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update feedback: ${e.message}")
            false
        }
    }
}

/**
 * 피드백 데이터 클래스
 */
data class Feedback(
    val id: String,
    val userId: String,
    val userEmail: String,
    val category: String,
    val title: String,
    val content: String,
    val hasScreenshot: Boolean,
    val screenshot: String?,
    val status: String,
    val createdAt: Date,
    val adminNote: String
)
