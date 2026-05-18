package com.bazical.app.domain.model

data class User(
    val userId: String,
    val name: String,
    val birthday: String,
    val birthdayType: String = "solar", // solar=阳历，lunar=阴历
    val hour: Int,
    val minute: Int,
    val gender: Gender
)

enum class Gender {
    MALE,
    FEMALE
}