package pl.devnowak.animalove

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import pl.devnowak.animalove.model.User


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d("TAG", "The token refreshed: $token")
        var newRegistrationToken: String
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            newRegistrationToken = it

            if (FirebaseAuth.getInstance().currentUser != null) {
                addTokenToFirestore(newRegistrationToken)
            }
        }
    }

    companion object {
        val auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        fun addTokenToFirestore(newRegistrationToken: String?) {
            if (newRegistrationToken == null) throw NullPointerException("FCM token is null.")
            getFCMRegistrationToken { token ->
                if (token == newRegistrationToken) {
                    return@getFCMRegistrationToken
                }
                setFCMRegistrationTokens(newRegistrationToken)
            }
        }

        fun getFCMRegistrationToken(onComplete: (token: String) -> Unit) {
            db.document("users/${auth.currentUser.uid}").get().addOnSuccessListener {
                val user = it.toObject(User::class.java)!!
                onComplete(user.deviceToken)
            }
        }

        fun setFCMRegistrationTokens(registrationToken: String) {
            db.document("users/${auth.currentUser.uid}").update(mapOf("deviceToken" to registrationToken))
        }
    }
}