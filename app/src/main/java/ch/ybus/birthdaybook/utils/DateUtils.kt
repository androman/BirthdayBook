package ch.ybus.birthdaybook.utils

import ch.ybus.birthdaybook.db.EventInfo
import java.util.*

/**
 * Some utilities to handle dates.
 */
object DateUtils {

    private val CAL = Calendar.getInstance()

    /**
     * One day in milliseconds.
     */
    const val ONE_DAY = 24L * 60L * 60L * 1000L

    /**
     * Returns a Date object where the time is cleared and the date is according to the parameters.
     *
     * @param year the year.
     * @param monthOfYear the month (zero based.)
     * @param dayOfMonth the day of the month.
     * @return the date.
     */
    fun getDate(year: Int, monthOfYear: Int, dayOfMonth: Int): Date {
        synchronized(CAL) {
            CAL.clear()
            CAL[year, monthOfYear] = dayOfMonth
            return CAL.time
        }
    }

    /**
     * Returns the information about the next birthday event.
     * @param birthDate the birth date.
     * @param currentTime the current time. This parameter is needed if we want to use the same
     * reference time for several birth date (e.g. when comparing the age or days to the
     * next birthday).
     * @param keepSameYearForDays the number of days to keep the birthday in the current year. I.e.
     * if the value of this parameter is 5 and the birthday happened less or equal 5 days ago then
     * the days will be returned as a negative number denoting the number of days ago the birthday
     * happened.
     * @return the information about the next birthday event.
     */
    fun getEventInfo(birthDate: Date, currentTime: Long, keepSameYearForDays: Int): EventInfo {
        var delta: Long = 0
        var age = 0
        synchronized(CAL) {
            // Calculate today at midnight 0:00:00 hours
            CAL.timeInMillis = currentTime
            CAL[Calendar.HOUR_OF_DAY] = 0
            CAL[Calendar.MINUTE] = 0
            CAL[Calendar.SECOND] = 0
            CAL[Calendar.MILLISECOND] = 0
            val now = CAL.time
            val currentYear = CAL[Calendar.YEAR]

            CAL.time = birthDate
            val yearBorn = CAL[Calendar.YEAR]
            CAL[Calendar.YEAR] = currentYear
            val beforeNow = now.time - keepSameYearForDays * ONE_DAY;
            if (beforeNow > CAL.timeInMillis) {
                CAL[Calendar.YEAR] = currentYear + 1
            }
            delta = CAL.timeInMillis - now.time
            age = CAL[Calendar.YEAR] - yearBorn
        }
        val days = (delta / ONE_DAY).toInt()
        return EventInfo(days, age)
    }

    /**
     * Returns the year of the given date.
     * @param date the date for which to return the year.
     * @return the year of the given date.
     */
    fun getYear(date: Date?): Int {
        synchronized(CAL) {
            CAL.time = date
            return CAL[Calendar.YEAR]
        }
    }

    /**
     * Returns the month of the given date. The month is zero based.
     *
     * @param date the date for which to return the month.
     * @return the month of the given date.
     */
    fun getMonth(date: Date?): Int {
        synchronized(CAL) {
            CAL.time = date
            return CAL[Calendar.MONTH]
        }
    }

    /**
     * Returns the day of month of the given date.
     * @param date the date for which to return the day.
     * @return the day of the given date.
     */
    fun getDay(date: Date?): Int {
        synchronized(CAL) {
            CAL.time = date
            return CAL[Calendar.DAY_OF_MONTH]
        }
    }

    /**
     * Returns the next occurrence of the time specified by hours and minutes.
     *
     * @param hourOfDay the hours of the time (0-23)
     * @param minutes the minutes of the time.
     * @return the next occurrence of the given time.
     */
    fun getNext(hourOfDay: Int, minutes: Int): Date {
        val now = Date()
        synchronized(CAL) {
            CAL.time = now
            CAL[Calendar.HOUR_OF_DAY] = hourOfDay
            CAL[Calendar.MINUTE] = minutes
            CAL[Calendar.SECOND] = 0
            CAL[Calendar.MILLISECOND] = 0
            if (now.after(CAL.time)) {
                CAL.add(Calendar.DAY_OF_MONTH, 1)
            }
            return CAL.time
        }
    }
}