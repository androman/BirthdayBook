package ch.ybus.birthdaybook.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import ch.ybus.birthdaybook.db.Settings
import ch.ybus.birthdaybook.utils.DateUtils
import java.time.LocalTime

/**
 * This class handles the scheduling of the reminder service.
 */
class Scheduler(private val context: Context) {

    /**
     * Activates the reminder service for the birthdays.
     */
    fun activateReminder() {
        val time = reminderTime
        setReminder(time.hour, time.minute)
    }

    private fun setReminder(hours: Int, minutes: Int) {
        val myIntent = createIntent()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            myIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val date = DateUtils.getNext(hours, minutes)
        Log.d(TAG, "Set alarm for $date.")
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP, date.time, DateUtils.ONE_DAY, pendingIntent
        )
        serviceScheduled = true
    }

    private fun createIntent(): Intent = Intent(context, ReminderReceiver::class.java)

    /**
     * Returns whether the reminding service is scheduled. The reminding service can be started
     * using [.setReminder] or [.activateReminder] and canceled using
     * [.cancelReminder].
     * @return `true` if the reminding service is/should be scheduled. `false`
     * otherwise. There is no way to find out whether the alarm has been set so it just
     * returns the state of the preference.
     */
    var serviceScheduled: Boolean
        get() = Settings(context).isScheduled()
        private set(isScheduled) = Settings(context).setScheduled(isScheduled)

    /**
     * Cancels the active reminding service. If the reminding was is not currently active then
     * this method does nothing.
     */
    fun cancelReminder() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            createIntent(),
            PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            Log.i(TAG, "Cancelled alarm");
            alarmManager.cancel(pendingIntent)
        }
        serviceScheduled = false
    }

    /**
     * Returns the current reminder time.
     * @return the current reminder time.
     */
    val reminderTime: LocalTime
        get() = Settings(context).getReminderTime()

    /**
     * Sets the reminder time. If the reminder service is currently enabled then its reminder
     * time is adjusted to this time as well.
     * @param hourOfDay the hours to set.
     * @param minute the minute of the reminder time.
     */
    fun setReminderTime(hourOfDay: Int, minute: Int) {
        if (serviceScheduled) {
            setReminder(hourOfDay, minute)
        }
        Settings(context).setReminderTime(hourOfDay, minute)
    }

    companion object {
        private val TAG = Scheduler::class.java.simpleName
    }
}