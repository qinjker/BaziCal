package com.bazical.app.domain.usecase

import com.bazical.app.domain.model.BaZi
import com.bazical.app.domain.model.User
import com.bazical.app.domain.repository.BaziRepository
import com.bazical.app.domain.repository.UserRepository
import javax.inject.Inject

class CalculateBaziUseCase @Inject constructor(
    private val baziRepository: BaziRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<Pair<User, BaZi>> {
        val result = baziRepository.calculateBazi(user)
        if (result.isSuccess) {
            val (savedUser, bazi) = result.getOrThrow()
            userRepository.saveUser(savedUser)
            userRepository.saveBaZi(bazi)
        }
        return result
    }
}

class GetCalendarUseCase @Inject constructor(
    private val baziRepository: BaziRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(year: Int, month: Int): Result<com.bazical.app.domain.model.CalendarMonth> {
        val userId = userRepository.getUserId() ?: return Result.failure(Exception("User not found"))
        return baziRepository.getCalendar(userId, year, month)
    }
}

class SolarToLunarUseCase @Inject constructor(
    private val baziRepository: BaziRepository
) {
    suspend operator fun invoke(date: String): Result<com.bazical.app.domain.model.LunarDate> {
        return baziRepository.solarToLunar(date)
    }
}

class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): User? {
        return userRepository.getUser()
    }
}

class HasUserDataUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Boolean {
        return userRepository.hasUserData()
    }
}