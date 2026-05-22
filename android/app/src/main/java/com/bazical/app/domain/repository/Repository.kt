package com.bazical.app.domain.repository

import com.bazical.app.domain.model.BaZi
import com.bazical.app.domain.model.CalendarMonth
import com.bazical.app.domain.model.DailyDetail
import com.bazical.app.domain.model.LunarDate
import com.bazical.app.domain.model.User

interface BaziRepository {
    suspend fun calculateBazi(user: User): Result<Pair<User, BaZi>>
    suspend fun getCalendar(userId: String, year: Int, month: Int): Result<CalendarMonth>
    suspend fun solarToLunar(date: String): Result<LunarDate>
    suspend fun getDailyDetail(userId: String, date: String): Result<DailyDetail>
}

interface UserRepository {
    suspend fun saveUser(user: User)
    suspend fun saveBaZi(bazi: BaZi)
    suspend fun getUser(): User?
    suspend fun hasUserData(): Boolean
    suspend fun getUserId(): String?
    suspend fun clear()
}