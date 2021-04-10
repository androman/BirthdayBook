package ch.ybus.birthdaybook.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ch.ybus.birthdaybook.R
import ch.ybus.birthdaybook.utils.NotificationHelper

/**
 * This class is instantiated and called when an intent has been broadcasted and this class
 * is interested in that intent. The configuration for this class is added to the manifest.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // After a reboot the alarm needs to be set again.
            val scheduler = Scheduler(context)
            var messageId = R.string.not_activated
            if (scheduler.serviceScheduled) {
                scheduler.activateReminder()
                messageId = R.string.activated
            }
            val tickerText = context.resources.getString(R.string.boot_ticker)
            NotificationHelper.sendNotification(
                context, null,
                tickerText,
                tickerText,
                context.resources.getString(messageId),
                System.currentTimeMillis().toInt()
            )
        }
    }
}