package ch.ybus.birthdaybook

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import ch.ybus.birthdaybook.db.Database
import ch.ybus.birthdaybook.db.RawContact
import ch.ybus.birthdaybook.utils.DateUtils


class BirthdayListAdapter(
    private val db : Database,
    var daysToShowPassedBirthdays: Int
) : Adapter<BirthdayListAdapter.PersonViewHolder>() {

    val contactList : MutableList<RawContact> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return PersonViewHolder(itemView)
    }

    override fun getItemCount(): Int = contactList.size

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val contact: RawContact = this.contactList[position]
        db.setContactPhoto(contact)
        holder.photo.setImageBitmap(contact.photo)
        holder.displayName.text = contact.displayName

        val eventInfo = DateUtils.getEventInfo(contact.birthDate!!, System.currentTimeMillis(), daysToShowPassedBirthdays)
        val days = eventInfo.daysUntilNextBirthday
        val age = eventInfo.ageAtNextBirthday
        holder.itemView.setOnClickListener { v -> viewContact(v.context, contact) }
        setNextBirthdayText(holder.nextBirthday, days, age)
        if (days < 0) {
            holder.itemView.setBackgroundResource(R.color.background_passed_birthday)
        } else {
            holder.itemView.setBackgroundResource(R.color.background_future_birthday)
        }
    }

    private fun viewContact(context: Context, contact: RawContact) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = Uri.withAppendedPath(
            ContactsContract.Contacts.CONTENT_URI, contact.contactId.toString()
        )
        intent.data = uri
        context.startActivity(intent)
    }

    private fun setNextBirthdayText(view : TextView, days: Int, age: Int) {
        val res = view.context.resources
        val text = when (days) {
            -1 -> res.getString(R.string.birthday_yesterday, age)
            0 -> res.getString(R.string.next_birthday_today, age)
            1 -> res.getString(R.string.next_birthday_tomorrow, age)
            else -> if (days < 0) {
                res.getString(R.string.birthday_before, age, -days)
            } else {
                res.getString(R.string.next_birthday, age, days)
            }
        }
        view.text = text
    }



    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo : ImageView = itemView.findViewById(R.id.photo)
        val displayName : TextView = itemView.findViewById(R.id.contactDisplayName)
        val nextBirthday : TextView = itemView.findViewById(R.id.contactNextBirthday)
    }
}