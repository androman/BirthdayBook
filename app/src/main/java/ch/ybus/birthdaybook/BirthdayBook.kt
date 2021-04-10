package ch.ybus.birthdaybook

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.media.RingtoneManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.ybus.birthdaybook.db.Database
import ch.ybus.birthdaybook.db.RawContact
import ch.ybus.birthdaybook.db.Settings
import ch.ybus.birthdaybook.service.ReminderReceiver
import ch.ybus.birthdaybook.service.Scheduler
import ch.ybus.birthdaybook.utils.NextBirthdayComparator
import java.util.*
import kotlin.collections.ArrayList


/**
 * Main activity.
 */
class BirthdayBook : AppCompatActivity() {

    private val daysToShowPassedBirthdays: Int
        get() = Settings(applicationContext).getDaysToShowPassedBirthdays()

    private fun isMyServiceRunning() = Scheduler(this).serviceScheduled

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        // Set my toolbar as the activity's action bar
        setSupportActionBar(findViewById(R.id.toolbar))
        // setup notification channel (repeating it is safe)
        createNotificationChannel()

        // initialize the birthday list view
        val recyclerView = getRecyclerView()
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        val db = Database(applicationContext)
        recyclerView.adapter = BirthdayListAdapter(db, daysToShowPassedBirthdays)
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL))
        load(db)
    }

    private fun load(db : Database) =
        Loader(db, getRecyclerView().adapter as BirthdayListAdapter, daysToShowPassedBirthdays)
            .execute()

    override fun onResume() {
        super.onResume()
        load(Database(applicationContext))
    }

    private fun getRecyclerView() = findViewById<RecyclerView>(R.id.birthdayList)

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(ReminderReceiver.NOTIFICATION_CHANNEL, name, importance)
                .apply {
                    description = descriptionText
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        Notification.AUDIO_ATTRIBUTES_DEFAULT
                    )
                }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.prefs_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val menuItem = menu.findItem(R.id.startStopService)
        updateServiceMenuItem(menuItem)
        return super.onPrepareOptionsMenu(menu)
    }

    private fun updateServiceMenuItem(menuItem: MenuItem) {
        menuItem.isChecked = isMyServiceRunning()
        menuItem.setIcon(
                if (isMyServiceRunning())
                    R.drawable.ic_service_on
                else
                    R.drawable.ic_service_off
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.startStopService -> {
                val scheduler = Scheduler(this)
                if (isMyServiceRunning()) {
                    scheduler.cancelReminder()
                } else {
                    scheduler.activateReminder()
                }
                updateServiceMenuItem(item)
                true
            }
            R.id.menu_set_time -> {
                setReminderTime()
                true
            }
            R.id.passedBirthdays -> {
                setShowPassedBirthdaysFor()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setShowPassedBirthdaysFor() {
        ShowPassedBirthdaysDialogFragment(
            { days ->
                Settings(applicationContext).setDaysToShowPassedBirthdays(days)
                (getRecyclerView().adapter as BirthdayListAdapter).daysToShowPassedBirthdays = days
                load(Database(applicationContext))
            },
            daysToShowPassedBirthdays
        ).show(supportFragmentManager, "passedBirthdays")
    }

    private fun setReminderTime() {
        val time = Scheduler(this).reminderTime
        val dlg = TimePickerDialog(
                this,
                { _, hourOfDay, minute -> Scheduler(this@BirthdayBook).setReminderTime(hourOfDay, minute) },
                time.hour, time.minute, true
        )
        dlg.show()
    }

    private class Loader(
        private val db: Database,
        private val adapter: BirthdayListAdapter,
        private val daysToShowPassedBirthdays: Int
    ) : AsyncTask<Void?, Int?, List<RawContact>>() {

        override fun onPostExecute(results: List<RawContact>) {
            val contactList = this.adapter.contactList
            contactList.clear()
            contactList.addAll(results)
            this.adapter.notifyDataSetChanged()
        }

        override fun doInBackground(vararg params: Void?): List<RawContact> {
            val allContacts = db.getContacts()
            val contactsWithBirthday: MutableList<RawContact> = ArrayList()
            for (contact in allContacts) {
                if (contact.birthDate != null) {
                    contactsWithBirthday.add(contact)
                }
            }
            Collections.sort(
                contactsWithBirthday,
                NextBirthdayComparator(System.currentTimeMillis(), this.daysToShowPassedBirthdays)
            )
            return contactsWithBirthday
        }
    }

}