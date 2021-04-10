package ch.ybus.birthdaybook.db

/**
 * A holder of information about the next birthday event of a person.
 */
class EventInfo (
    val daysUntilNextBirthday: Int,
    val ageAtNextBirthday: Int
)