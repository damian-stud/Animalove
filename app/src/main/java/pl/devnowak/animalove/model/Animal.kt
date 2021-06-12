package pl.devnowak.animalove.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Parcelize
class Animal(
        var id: String = "-",
        var number: Long = -1,
        var name: String = "-",
        var since: Date = Date(System.currentTimeMillis()),
        var bitmap: Bitmap? = null,
        var image: String = "-",
        var age: Int = -1,
        var sex: String = "-",
        var type: String = "-",
        var size: String = "-"
) : Parcelable {
    constructor(
            id: String,
            number: Long,
            name: String,
            since: Date,
            image: String,
            age: Int,
            sex: String,
            type: String,
            size: String
    ): this(
            id,
            number,
            name,
            since,
            null,
            image,
            age,
            sex,
            type,
            size
    )
}