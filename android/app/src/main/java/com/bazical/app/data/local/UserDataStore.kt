package com.bazical.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bazical.app.domain.model.BaZi
import com.bazical.app.domain.model.Gender
import com.bazical.app.domain.model.Pillars
import com.bazical.app.domain.model.ShiShen
import com.bazical.app.domain.model.User
import com.bazical.app.domain.model.WuXing
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_NAME = stringPreferencesKey("name")
        private val KEY_BIRTHDAY = stringPreferencesKey("birthday")
        private val KEY_BIRTHDAY_TYPE = stringPreferencesKey("birthday_type")
        private val KEY_HOUR = stringPreferencesKey("hour")
        private val KEY_MINUTE = stringPreferencesKey("minute")
        private val KEY_GENDER = stringPreferencesKey("gender")
        private val KEY_BAZI_JSON = stringPreferencesKey("bazi_json")
    }

    suspend fun saveUser(user: User) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = user.userId
            prefs[KEY_NAME] = user.name
            prefs[KEY_BIRTHDAY] = user.birthday
            prefs[KEY_BIRTHDAY_TYPE] = user.birthdayType
            prefs[KEY_HOUR] = user.hour.toString()
            prefs[KEY_MINUTE] = user.minute.toString()
            prefs[KEY_GENDER] = user.gender.name
        }
    }

    suspend fun saveBaZi(bazi: BaZi) {
        // 存储八字完整数据，包含十神
        context.dataStore.edit { prefs ->
            prefs[KEY_BAZI_JSON] = buildString {
                // 四柱天干地支
                append("${bazi.year.stem},${bazi.year.branch}|")
                append("${bazi.month.stem},${bazi.month.branch}|")
                append("${bazi.day.stem},${bazi.day.branch}|")
                append("${bazi.hour.stem},${bazi.hour.branch}|")
                // 十神
                append("${bazi.shishen.year},${bazi.shishen.month},${bazi.shishen.day},${bazi.shishen.hour}|")
                // 五行
                append("${bazi.wuxing.mu},${bazi.wuxing.huo},${bazi.wuxing.tu},${bazi.wuxing.jin},${bazi.wuxing.shui}")
            }
        }
    }

    suspend fun getBaZi(): BaZi? {
        val prefs = context.dataStore.data.first()
        val baziJson = prefs[KEY_BAZI_JSON] ?: return null

        return try {
            val parts = baziJson.split("|")
            if (parts.size < 6) return null

            val (yearStem, yearBranch) = parts[0].split(",")
            val (monthStem, monthBranch) = parts[1].split(",")
            val (dayStem, dayBranch) = parts[2].split(",")
            val (hourStem, hourBranch) = parts[3].split(",")
            val (shishenYear, shishenMonth, shishenDay, shishenHour) = parts[4].split(",")
            val wuxingParts = parts[5].split(",")

            BaZi(
                year = Pillars(yearStem, yearBranch),
                month = Pillars(monthStem, monthBranch),
                day = Pillars(dayStem, dayBranch),
                hour = Pillars(hourStem, hourBranch),
                shishen = ShiShen(shishenYear, shishenMonth, shishenDay, shishenHour),
                wuxing = WuXing(
                    mu = wuxingParts[0].toInt(),
                    huo = wuxingParts[1].toInt(),
                    tu = wuxingParts[2].toInt(),
                    jin = wuxingParts[3].toInt(),
                    shui = wuxingParts[4].toInt()
                )
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUser(): User? {
        val prefs = context.dataStore.data.first()
        val userId = prefs[KEY_USER_ID] ?: return null
        val name = prefs[KEY_NAME] ?: return null
        val birthday = prefs[KEY_BIRTHDAY] ?: return null
        val birthdayType = prefs[KEY_BIRTHDAY_TYPE] ?: "solar"
        val hour = prefs[KEY_HOUR]?.toIntOrNull() ?: return null
        val minute = prefs[KEY_MINUTE]?.toIntOrNull() ?: return null
        val gender = prefs[KEY_GENDER]?.let { Gender.valueOf(it) } ?: return null

        return User(userId, name, birthday, birthdayType, hour, minute, gender)
    }

    suspend fun hasUserData(): Boolean {
        return context.dataStore.data.first()[KEY_USER_ID] != null
    }

    suspend fun getUserId(): String? {
        return context.dataStore.data.first()[KEY_USER_ID]
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}