package ch.ybus.birthdaybook.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import ch.ybus.birthdaybook.R
import ch.ybus.birthdaybook.db.Database
import ch.ybus.birthdaybook.db.RawContact
import ch.ybus.birthdaybook.utils.DateUtils
import ch.ybus.birthdaybook.utils.NotificationHelper
import java.util.*

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "onReceive()")
        checkBirthdays(context)
    }

    private fun checkBirthdays(context: Context?) {
        if (context == null) {
            return
        }
        Log.d(TAG, "checkBirthdays()")
        val now = Date()
        val db = Database(context)
        val list = db.getTodaysBirthdays(now)
        for (contact in list) {
            sendNotification(context, contact, now.time)
        }
    }

    private fun sendNotification(context: Context, contact: RawContact, referenceTime: Long) {
        val tickerText: CharSequence? = contact.displayName
        val contentTitle: CharSequence = context.resources.getString(
            R.string.birthday_notification)
        val eventInfo = DateUtils.getEventInfo(contact.birthDate!!, referenceTime, 0)
        val contentText: CharSequence = context.resources.getString(
            R.string.birthday_notification_text, contact.displayName,
            Integer.valueOf(eventInfo.ageAtNextBirthday))
        NotificationHelper.sendNotification(
            context, contact.photo, tickerText, contentTitle,
            contentText, System.currentTimeMillis().toInt()
        )
    }

    companion object {
        // ---- Static
        private val TAG = ReminderReceiver::class.java.simpleName
        public val NOTIFICATION_CHANNEL = "birthday"
    }

}