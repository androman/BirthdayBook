package ch.ybus.birthdaybook.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ch.ybus.birthdaybook.BirthdayBook
import ch.ybus.birthdaybook.R
import ch.ybus.birthdaybook.service.ReminderReceiver

class NotificationHelper {
    companion object {

        fun sendNotification(
            context: Context, largeIcon: Bitmap?, tickerText: CharSequence?, contentTitle: CharSequence, contentText: CharSequence, msgId: Int
        ) {
            val icon = R.drawable.ic_notification
            val executionTime = System.currentTimeMillis() + 1000L
            val notificationIntent = Intent(context, BirthdayBook::class.java)
            val contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
            val notification = NotificationCompat.Builder(context, ReminderReceiver.NOTIFICATION_CHANNEL)
                .setSmallIcon(icon)
                .setLargeIcon(largeIcon)
                .setTicker(tickerText)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setWhen(executionTime)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build()
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(msgId, notification)
        }
    }

}