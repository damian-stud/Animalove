package pl.devnowak.animalove.model

import java.util.*

data class Appointment(
        var date: Date = Date(System.currentTimeMillis()),
        var animalNumber: Long = -1,
        var animalName: String = "-",
        var animalImage: String = "-",
        var userId: String = "-",
        var username: String = "-"

)