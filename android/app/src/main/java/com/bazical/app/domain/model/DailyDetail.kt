package com.bazical.app.domain.model

data class DailyDetail(
    val date: String,
    val ganzhi: String,
    val shishen: String,
    val branchShishen: String,
    val energyLevel: Int,
    val energyDescription: String,
    val luckyDirection: String,
    val luckyTime: String,
    val luckyColor: String,
    val luckyNumber: Int,
    val messages: List<String>,
    val tags: List<String>
)