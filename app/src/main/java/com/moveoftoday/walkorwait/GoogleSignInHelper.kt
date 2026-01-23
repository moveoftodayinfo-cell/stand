package com.moveoftoday.walkorwait

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Google Sign-In helper using Credential Manager API
 * Replaces deprecated GoogleSignInOptions/GoogleSignIn
 */
object GoogleSignInHelper {
    private const val TAG = "GoogleSignInHelper"

    // Web Client ID from Firebase Console
    const val WEB_CLIENT_ID = "735624722400-ds0maq28ptpa01kc6cebs2727o3c6kar.apps.googleusercontent.com"

    sealed class SignInResult {
        data class Success(val idToken: String, val email: String?) : SignInResult()
        data class Error(val message: String, val isCancelled: Boolean = false) : SignInResult()
    }

    /**
     * Start Google Sign-In flow using Credential Manager
     */
    suspend fun signIn(context: Context): SignInResult {
        val credentialManager = CredentialManager.create(context)

        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            handleSignInResult(result)
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Sign-in cancelled by user")
            SignInResult.Error("로그인이 취소되었습니다", isCancelled = true)
        } catch (e: NoCredentialException) {
            Log.e(TAG, "No credential available: ${e.message}")
            SignInResult.Error("사용 가능한 Google 계정이 없습니다")
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential error: ${e.message}")
            SignInResult.Error("Google 로그인 실패: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}")
            SignInResult.Error("로그인 중 오류 발생: ${e.message}")
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse): SignInResult {
        val credential = result.credential

        return when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        val email = googleIdTokenCredential.id

                        Log.d(TAG, "Sign-in successful: $email")
                        SignInResult.Success(idToken, email)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Invalid Google ID token: ${e.message}")
                        SignInResult.Error("Google 토큰 처리 실패")
                    }
                } else {
                    Log.e(TAG, "Unexpected credential type: ${credential.type}")
                    SignInResult.Error("예상치 못한 인증 유형")
                }
            }
            else -> {
                Log.e(TAG, "Unexpected credential: ${credential.javaClass.simpleName}")
                SignInResult.Error("예상치 못한 인증 정보")
            }
        }
    }

    /**
     * Authenticate with Firebase using Google ID token
     */
    suspend fun signInToFirebase(idToken: String): Result<String> {
        return try {
            val auth = FirebaseAuth.getInstance()

            // Sign out anonymous user if exists
            if (auth.currentUser?.isAnonymous == true) {
                auth.signOut()
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()

            Log.d(TAG, "Firebase sign-in successful: ${authResult.user?.uid}")
            Result.success(authResult.user?.uid ?: "")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase sign-in failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Clear credential state on sign out
     */
    suspend fun signOut(context: Context) {
        try {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(
                androidx.credentials.ClearCredentialStateRequest()
            )
            FirebaseAuth.getInstance().signOut()
            Log.d(TAG, "Sign-out successful")
        } catch (e: Exception) {
            Log.e(TAG, "Sign-out error: ${e.message}")
        }
    }
}
