package com.bazical.app.ui.home

import com.bazical.app.domain.model.Gender
import com.bazical.app.domain.model.LunarDate

data class HomeUiState(
    val name: String = "",
    val birthday: Long? = null,
    val hour: Int = 0,
    val minute: Int = 0,
    val gender: Gender = Gender.MALE,
    val lunarDate: LunarDate? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val calculated: Boolean = false,
    val timeValue: String = "", // 时辰值，空字符串表示未知
    val birthdayType: String = "solar", // solar=阳历，lunar=阴历
    val showDatePicker: Boolean = false
)