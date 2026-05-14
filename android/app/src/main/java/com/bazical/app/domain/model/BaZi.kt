package com.bazical.app.domain.model

data class BaZi(
    val year: Pillars,
    val month: Pillars,
    val day: Pillars,
    val hour: Pillars,
    val wuxing: WuXing,
    val shishen: ShiShen
)

data class Pillars(
    val stem: String,
    val branch: String
)

data class WuXing(
    val mu: Int,
    val huo: Int,
    val tu: Int,
    val jin: Int,
    val shui: Int
)

data class ShiShen(
    val year: String,
    val month: String,
    val day: String,
    val hour: String
)