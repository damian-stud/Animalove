package pl.devnowak.animalove.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Message(
    var id: String = "",
    var sender: String = "",
    var reciever: String = "",
    var message: String = "",
    var time: Date = Date(0),
    var senderName: String = ""
) : Parcelable