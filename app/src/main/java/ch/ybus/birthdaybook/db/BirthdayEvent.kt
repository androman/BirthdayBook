package ch.ybus.birthdaybook.db

import android.provider.ContactsContract.RawContacts
import java.util.*

/**
 * Stores the birth date together with the id of the Data row and the id of the [RawContacts]
 * it belongs to.
 */
class BirthdayEvent(val id: Long?, val rawContactId: Long, birthDate: Date?) {

    var birthDate: Date? = null
        get() = field?.clone() as Date
        set(date) {
            field = date?.clone() as Date
        }

    init {
        this.birthDate = birthDate
    }
}