package com.bazical.app.data.repository

import com.bazical.app.data.local.UserDataStore
import com.bazical.app.data.remote.api.BaziApi
import com.bazical.app.data.remote.dto.CalculateRequest
import com.bazical.app.data.remote.dto.CalendarDayDto
import com.bazical.app.domain.model.BaZi
import com.bazical.app.domain.model.CalendarDay
import com.bazical.app.domain.model.CalendarMonth
import com.bazical.app.domain.model.Gender
import com.bazical.app.domain.model.LunarDate
import com.bazical.app.domain.model.Pillars
import com.bazical.app.domain.model.ShiShen
import com.bazical.app.domain.model.User
import com.bazical.app.domain.model.WuXing
import com.bazical.app.domain.repository.BaziRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BaziRepositoryImpl @Inject constructor(
    private val api: BaziApi
) : BaziRepository {

    override suspend fun calculateBazi(user: User): Result<Pair<User, BaZi>> {
        return try {
            val request = CalculateRequest(
                name = user.name,
                birthday = user.birthday,
                hour = user.hour,
                minute = user.minute,
                gender = if (user.gender == Gender.MALE) "男" else "女"
            )
            val response = api.calculateBazi(request)
            val bazi = BaZi(
                year = Pillars(response.bazi.year.stem, response.bazi.year.branch),
                month = Pillars(response.bazi.month.stem, response.bazi.month.branch),
                day = Pillars(response.bazi.day.stem, response.bazi.day.branch),
                hour = Pillars(response.bazi.hour.stem, response.bazi.hour.branch),
                wuxing = WuXing(
                    mu = response.bazi.wuxing.mu,
                    huo = response.bazi.wuxing.huo,
                    tu = response.bazi.wuxing.tu,
                    jin = response.bazi.wuxing.jin,
                    shui = response.bazi.wuxing.shui
                ),
                shishen = ShiShen(
                    year = response.bazi.shishen.year,
                    month = response.bazi.shishen.month,
                    day = response.bazi.shishen.day,
                    hour = response.bazi.shishen.hour
                )
            )
            Result.success(Pair(User(response.userId, user.name, user.birthday, user.hour, user.minute, user.gender), bazi))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCalendar(userId: String, year: Int, month: Int): Result<CalendarMonth> {
        return try {
            val response = api.getCalendar(userId, year, month)
            val days = response.days.map { dto ->
                CalendarDay(
                    date = dto.date,
                    ganzhi = dto.ganzhi,
                    wuxing = dto.wuxing,
                    yi = dto.yi,
                    ji = dto.ji,
                    star = dto.star,
                    luck = dto.luck
                )
            }
            Result.success(CalendarMonth(year, month, days))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun solarToLunar(date: String): Result<LunarDate> {
        return try {
            val response = api.solarToLunar(date)
            Result.success(LunarDate(response.lunarYear, response.lunarMonth, response.lunarDay, response.isLeapMonth))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDataStore: UserDataStore
) : com.bazical.app.domain.repository.UserRepository {

    override suspend fun saveUser(user: User) {
        userDataStore.saveUser(user)
    }

    override suspend fun saveBaZi(bazi: BaZi) {
        userDataStore.saveBaZi(bazi)
    }

    override suspend fun getUser(): User? {
        return userDataStore.getUser()
    }

    override suspend fun hasUserData(): Boolean {
        return userDataStore.hasUserData()
    }

    override suspend fun getUserId(): String? {
        return userDataStore.getUserId()
    }

    override suspend fun clear() {
        userDataStore.clear()
    }
}