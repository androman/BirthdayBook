package ch.ybus.birthdaybook.db

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds
import android.provider.ContactsContract.RawContacts
import android.util.Log
import ch.ybus.birthdaybook.R
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Helper class to access the system contacts database. The contacts structure is not straight
 * forward so you should have a look.
 *
 * @see "http://developer.android.com/resources/articles/contacts.html"
 */
class Database(private val context: Context) {
    private val contentResolver: ContentResolver
        get() = context.contentResolver
    private val unknownPhoto: Bitmap
        get() = BitmapFactory.decodeResource(context.resources, R.drawable.ic_photo_unknown)

    //---- Methods

    /**
     * Retrieve the contacts with their birthdays from the system database.
     * @return the list of all contacts, whether they have a birthday or not.
     */
    fun getContacts(): List<RawContact> {
        val allRawContacts = getAllVisibleGroupRawContacts()
        val birthdays = getAllBirthdays()
        mergeBirthdays(allRawContacts, birthdays)
        return ArrayList(allRawContacts.values)
    }

    private fun mergeBirthdays(
            allRawContacts: Map<Long, RawContact>, birthdays: List<BirthdayEvent>
    ) {
        for (be in birthdays) {
            val contact = allRawContacts[be.rawContactId]
            // if the contact is not found then the birthday belongs to a account type of the
            // raw contact which we do not support yet (skip).
            if (contact != null) {
                // If more than one birthday is stored for the same raw contact then the
                // last in the list wins.
                contact.birthdayEvent = be
            }
        }
    }

    private fun getAllBirthdays(): List<BirthdayEvent> {
        val cursor: Cursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(
                    ContactsContract.Data._ID,
                    ContactsContract.Data.RAW_CONTACT_ID,
                    CommonDataKinds.Event.START_DATE
                ),
                ContactsContract.Data.MIMETYPE + "=? AND " + CommonDataKinds.Event.TYPE + "=?",
                arrayOf(CommonDataKinds.Event.CONTENT_ITEM_TYPE, CommonDataKinds.Event.TYPE_BIRTHDAY.toString()),
                null
        )!!
        cursor.use {
            val result: MutableList<BirthdayEvent> = ArrayList()
            while (it.moveToNext()) {
                var index = 0
                val id = it.getLong(index++)
                val rawContactId = it.getLong(index++)
                val birthDate = parseDate(it.getString(index))
                if (birthDate != null) {
                    result.add(
                        BirthdayEvent(java.lang.Long.valueOf(id), java.lang.Long.valueOf(rawContactId), birthDate)
                    )
                }
            }
            return result
        }
    }

    /**
     * Returns all contacts which have their birthday today.
     * @param date the reference time for choosing all birthdays.
     * @return all contacts which have their birthday today.
     */
    fun getTodaysBirthdays(date: Date): List<RawContact> {
        val monthDayStartIndex = 4
        val today = formatDate(date).substring(monthDayStartIndex)
        val cursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(
                    ContactsContract.Data._ID,
                    ContactsContract.Data.RAW_CONTACT_ID,
                    ContactsContract.Data.CONTACT_ID,
                    ContactsContract.Data.DISPLAY_NAME,
                    CommonDataKinds.Event.START_DATE
                ),
                ContactsContract.Data.MIMETYPE + "=? AND " + CommonDataKinds.Event.TYPE + "=? ",
                arrayOf(CommonDataKinds.Event.CONTENT_ITEM_TYPE, CommonDataKinds.Event.TYPE_BIRTHDAY.toString()),
                null
        )!!
        cursor.use {
            val result: MutableList<RawContact> = ArrayList()
            while (it.moveToNext()) {
                var index = 0
                val id = it.getLong(index++)
                val rawContactId = it.getLong(index++)
                val contactId = it.getLong(index++)
                val displayName = it.getString(index++)
                val birthDateString = it.getString(index)
                val birthDate = parseDate(birthDateString)
                if (birthDate != null
                        && today == birthDateString.substring(monthDayStartIndex)) {
//					Log.d(TAG, "> " + rawContactId + " - " + id + ": " + birthDate);
                    val event = BirthdayEvent(java.lang.Long.valueOf(id), java.lang.Long.valueOf(rawContactId), birthDate)
                    val contact = RawContact(rawContactId, contactId, displayName)
                    contact.birthdayEvent = event
                    setContactPhoto(contact)
                    result.add(contact)
                }
            }
            return result
        }
    }

    private fun parseDate(date: String?): Date? {
        if (date != null) {
            try {
                synchronized(DATE_FORMAT) { return DATE_FORMAT.parse(date) }
            } catch (ex: ParseException) {
                Log.e(TAG, "Error in date format (yyyy-MM-dd expected): $date", ex)
            }
        }
        return null
    }// only add the contact if the display name exists (which mean it is in
    // a visible group).

    /**
     * This method retrieves all raw contacts and joins it to the contact and the display name.
     * Several raw contacts may be assigned to one contact and the birthday information of the
     * Data table is always linked to the raw contact. We only return the raw contacts with an
     * account type of 'com.google' as this is the only account type we support so far.
     *
     * @return a map of all raw contacts of google type
     */
    private fun getAllVisibleGroupRawContacts(): Map<Long, RawContact> {
        val displayNameMap = getAllVisibleGroupDisplayNames()
        val cursor = contentResolver.query(
                RawContacts.CONTENT_URI,
                arrayOf(RawContacts._ID, RawContacts.CONTACT_ID),
                RawContacts.ACCOUNT_TYPE + "=? and " + RawContacts.DELETED + "=0",
                arrayOf("com.google"),
                null
        )!!
        cursor.use {
            val result: MutableMap<Long, RawContact> = HashMap()
            while (it.moveToNext()) {
                var index = 0
                val id = it.getLong(index++)
                val contactId = it.getLong(index)
                val displayName = displayNameMap[java.lang.Long.valueOf(contactId)]
                // only add the contact if the display name exists (which mean it is in
                // a visible group).
                if (displayName != null) {
                    val contact = RawContact(id, contactId, displayName)
                    result[id] = contact
                }
            }
            return result
        }
    }

    /**
     * Returns the bitmap with the photo of the contact with the given contact id.
     * @param contactId the id of the contact.
     * @return the bitmap with the photo of the contact or `null` if the contact has
     * no photo.
     */
    private fun getContactPhoto(contactId: Long): Bitmap? {
        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val inputStream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver, contactUri)
        inputStream?.use {
            return BitmapFactory.decodeStream(it)
        }
        return null
    }

    /**
     * Returns a map of contact id to display name of all contacts which are in a visible group.
     * The visible group is defined by the flag IN_VISIBLE_GROUP. The google plus contacts which
     * are not merged with the contacts in MyContacts of the google account have this flag set
     * to 0.
     * @return a map of contact id to display name.
     */
    private fun getAllVisibleGroupDisplayNames(): Map<Long, String> {
            val cursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI, arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME),
                    null, null, null
            )!!
            cursor.use {
                val result: MutableMap<Long, String> = HashMap(it.count)
                while (it.moveToNext()) {
                    result[it.getLong(0)] = it.getString(1)
                }
                return result
            }
        }

    private fun formatDate(date: Date): String {
        synchronized(DATE_FORMAT) { return DATE_FORMAT.format(date) }
    }

    /**
     * Extracts the photo for the given contract and sets it if not yet set.
     * @param contact the contact for which to extract
     */
    fun setContactPhoto(contact: RawContact) {
        var photo = contact.photo
        if (photo == null) {
            photo = getContactPhoto(contact.contactId)
            if (photo == null) {
                photo = unknownPhoto
            }
            contact.photo = photo
        }
    }

    companion object {
        //---- Static
        private val TAG = Database::class.java.simpleName
        private val DATE_FORMAT: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }
}