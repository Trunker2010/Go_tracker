package com.example.gotracker.screens.saveErrorDialog

import android.app.Activity
import android.content.Intent
import android.icu.text.CaseMap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.gotracker.R
import kotlinx.android.synthetic.main.dialog_fragment_save_err.view.*

const val RESULT_RESUMED: Int = 3

class SaveErrDialogFragment : DialogFragment(), View.OnClickListener {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.dialog_fragment_save_err, container)
        v.err_dialog_cancel.setOnClickListener(this)
        v.err_dialog_resume.setOnClickListener(this)
        return v

    }

    override fun onClick(v: View) {
        val intent = Intent()
        when (v.id) {
            R.id.err_dialog_cancel -> {
                targetFragment?.onActivityResult(
                    targetRequestCode,
                    Activity.RESULT_CANCELED,
                    intent
                )
                dismiss()

            }
            R.id.err_dialog_resume -> {
                targetFragment?.onActivityResult(
                    targetRequestCode,
                    RESULT_RESUMED,
                    intent
                )
                dismiss()


            }
        }
    }
}