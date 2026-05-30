package vn.chiennl.cartracker.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(private val context: Context) {
    fun currentUser(): FirebaseUser? =
        if (FirebaseApp.getApps(context).isEmpty()) null else FirebaseAuth.getInstance().currentUser

    fun isConfigured(): Boolean = FirebaseApp.getApps(context).isNotEmpty() && webClientId() != null

    suspend fun signIn(activity: Activity): Result<FirebaseUser> = runCatching {
        check(FirebaseApp.getApps(context).isNotEmpty()) {
            "Chưa cấu hình Firebase: cần thêm app/google-services.json."
        }
        val clientId = requireNotNull(webClientId()) {
            "Không tìm thấy OAuth Web Client ID trong cấu hình Firebase."
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credential = CredentialManager.create(context)
            .getCredential(activity, request)
            .credential
        check(credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            "Thông tin đăng nhập Google không hợp lệ."
        }
        val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val firebaseCredential = GoogleAuthProvider.getCredential(token, null)
        requireNotNull(FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await().user)
    }

    fun signOut() {
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            FirebaseAuth.getInstance().signOut()
        }
    }

    private fun webClientId(): String? {
        val resourceId = context.resources.getIdentifier(
            "default_web_client_id",
            "string",
            context.packageName
        )
        return resourceId.takeIf { it != 0 }?.let(context::getString)?.takeIf(String::isNotBlank)
    }
}
