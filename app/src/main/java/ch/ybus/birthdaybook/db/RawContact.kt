package ch.ybus.birthdaybook.db

import android.graphics.Bitmap
import java.util.*

/**
 * Contains the minimal information about the raw contact and its birthday.
 */
class RawContact (val id: Long, val contactId: Long, val displayName: String?)
    : Comparable<RawContact>
{
    var birthdayEvent: BirthdayEvent? = null
    var photo: Bitmap? = null

    //---- Methods

    var birthDate: Date?
        get() = birthdayEvent?.birthDate
        set(date) {
            if (birthdayEvent == null) {
                birthdayEvent = BirthdayEvent(null, java.lang.Long.valueOf(id), date)
            } else {
                birthdayEvent!!.birthDate = date
            }
        }

    override fun compareTo(other: RawContact): Int {
        if (this.displayName == null) {
            return if (other.displayName == null) 0 else -1
        }
        return if (other.displayName == null) {
            1
        } else {
            displayName.toUpperCase(Locale.GERMAN).compareTo(other.displayName.toUpperCase(Locale.GERMAN))
        }
    }

    //---- Constructors
}