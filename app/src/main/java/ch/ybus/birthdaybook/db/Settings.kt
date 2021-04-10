package ch.ybus.birthdaybook.db

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalTime

class Settings(private val context: Context) {

    companion object {
        private const val SETTINGS = "SETTINGS"
        private const val SCHEDULED = "scheduled"
        private const val HOURS = "hours"
        private const val MINUTES = "minutes"
        private const val PASSED_BIRTHDAYS = "passed"
    }
    
    private val preferences: SharedPreferences
        get() = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)


    fun getReminderTime(): LocalTime {
        val hour = preferences.getInt(HOURS, 6)
        val minute = preferences.getInt(MINUTES, 0)
        return LocalTime.of(hour, minute)
    }

    fun setReminderTime(hourOfDay: Int, minute: Int) {
        val edit = preferences.edit()
        edit.putInt(HOURS, hourOfDay)
        edit.putInt(MINUTES, minute)
        edit.commit()
    }

    fun isScheduled(): Boolean {
        return preferences.getBoolean(SCHEDULED, false)
    }

    fun setScheduled(scheduled: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(SCHEDULED, scheduled)
        editor.commit()
    }

    fun setDaysToShowPassedBirthdays(days: Int) {
        val editor = preferences.edit()
        editor.putInt(PASSED_BIRTHDAYS, days)
        editor.commit()
    }

    fun getDaysToShowPassedBirthdays(): Int {
        return preferences.getInt(PASSED_BIRTHDAYS, 3)
    }


}