package ch.ybus.birthdaybook

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class ShowPassedBirthdaysDialogFragment(
    private val okListener: (days: Int) -> Unit,
    private val initialDays: Int
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return this.activity?.let {
            val view = layoutInflater.inflate(R.layout.passed_birthdays_view, null)
            val inputField = view.findViewById<TextView>(R.id.daysToShowPassedBirthdays)
            inputField.text = "${initialDays}"
            AlertDialog.Builder(it)
                .setView(view)
                .setPositiveButton(
                    R.string.button_ok,
                    { dialog, id ->
                        val value = inputField.text.toString()
                        this.okListener.invoke(value.toInt())
                    }
                )
                .setNegativeButton(
                    R.string.button_cancel,
                    { dialog, id ->
                    // do nothing
                    }
                )
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

}