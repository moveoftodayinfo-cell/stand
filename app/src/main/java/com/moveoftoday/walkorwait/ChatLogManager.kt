package com.moveoftoday.walkorwait

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * 채팅 로그 관리자
 * 사용자 채팅을 Firebase에 저장하고 관리
 */
object ChatLogManager {
    private const val TAG = "ChatLogManager"
    private const val COLLECTION_CHAT_LOGS = "chatLogs"

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    /**
     * 채팅 로그 저장
     */
    fun saveChat(
        userMessage: String,
        petResponse: String,
        petName: String,
        petType: String,
        responseType: String // "script", "ai", "filtered", "limit_reached"
    ) {
        val userId = auth.currentUser?.uid ?: return
        val userEmail = auth.currentUser?.email ?: ""

        val chatLog = hashMapOf(
            "userId" to userId,
            "userEmail" to userEmail,
            "userMessage" to userMessage,
            "petResponse" to petResponse,
            "petName" to petName,
            "petType" to petType,
            "responseType" to responseType,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "date" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(Date())
        )

        db.collection(COLLECTION_CHAT_LOGS)
            .add(chatLog)
            .addOnSuccessListener { docRef ->
                Log.d(TAG, "Chat logged: ${docRef.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to log chat: ${e.message}")
            }
    }

    /**
     * 최근 채팅 로그 가져오기 (관리자용)
     */
    suspend fun getRecentChats(limit: Int = 100): List<ChatLog> {
        return try {
            val snapshot = db.collection(COLLECTION_CHAT_LOGS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    ChatLog(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userEmail = doc.getString("userEmail") ?: "",
                        userMessage = doc.getString("userMessage") ?: "",
                        petResponse = doc.getString("petResponse") ?: "",
                        petName = doc.getString("petName") ?: "",
                        petType = doc.getString("petType") ?: "",
                        responseType = doc.getString("responseType") ?: "",
                        timestamp = doc.getTimestamp("timestamp")?.toDate() ?: Date(),
                        date = doc.getString("date") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get chats: ${e.message}")
            emptyList()
        }
    }

    /**
     * 특정 사용자의 채팅 로그 가져오기
     */
    suspend fun getUserChats(userId: String, limit: Int = 50): List<ChatLog> {
        return try {
            val snapshot = db.collection(COLLECTION_CHAT_LOGS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    ChatLog(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userEmail = doc.getString("userEmail") ?: "",
                        userMessage = doc.getString("userMessage") ?: "",
                        petResponse = doc.getString("petResponse") ?: "",
                        petName = doc.getString("petName") ?: "",
                        petType = doc.getString("petType") ?: "",
                        responseType = doc.getString("responseType") ?: "",
                        timestamp = doc.getTimestamp("timestamp")?.toDate() ?: Date(),
                        date = doc.getString("date") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user chats: ${e.message}")
            emptyList()
        }
    }

    /**
     * 오래된 채팅 로그 삭제 (30일 이상)
     */
    suspend fun deleteOldChats(): Int {
        return try {
            val thirtyDaysAgo = Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
            val snapshot = db.collection(COLLECTION_CHAT_LOGS)
                .whereLessThan("timestamp", com.google.firebase.Timestamp(thirtyDaysAgo))
                .get()
                .await()

            var deletedCount = 0
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
                deletedCount++
            }

            Log.d(TAG, "Deleted $deletedCount old chat logs")
            deletedCount
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete old chats: ${e.message}")
            0
        }
    }
}

/**
 * 채팅 로그 데이터 클래스
 */
data class ChatLog(
    val id: String,
    val userId: String,
    val userEmail: String,
    val userMessage: String,
    val petResponse: String,
    val petName: String,
    val petType: String,
    val responseType: String,
    val timestamp: Date,
    val date: String
)
