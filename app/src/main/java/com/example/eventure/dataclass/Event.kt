package com.example.eventure.dataclass

data class Event(
    val name: String = "",
    val category: String = "",
    val location: String = "",
    val date: String = "",
    val maxAttendees: Int = 0,
    val registrationDeadline: String = "",
    val description: String = "",
    val imageUrl: String? = null
)

