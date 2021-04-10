package ch.ybus.birthdaybook.utils

import ch.ybus.birthdaybook.db.RawContact
import java.util.*

/**
 * Comparator which compares the number of days to the next birthday of two [RawContact]s.
 */
class NextBirthdayComparator(private val referenceTime: Long, private val daysBeforeNextYear: Int) : Comparator<RawContact> {

    override fun compare(object1: RawContact, object2: RawContact): Int {
        return getDaysUntilNextBirthday(object1) - getDaysUntilNextBirthday(object2)
    }

    private fun getDaysUntilNextBirthday(contact: RawContact): Int {
        return DateUtils.getEventInfo(contact.birthDate!!, referenceTime, daysBeforeNextYear)
                .daysUntilNextBirthday
    }
}