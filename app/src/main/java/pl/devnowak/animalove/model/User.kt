package pl.devnowak.animalove.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
        var id: String = "",
        var email: String = "",
        var username: String = "",
        var profileImagePath: String = "default",
        var profileImage: Bitmap? = null,
        var role: String = "user",
        var status: String = "offline",
        val deviceToken: String = "-"
) : Parcelable