package com.example.gotracker.ui.fragments

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.gotracker.R
import com.example.gotracker.databinding.FragmentTrackingBinding


class SaveTrackDialogFragment : DialogFragment(), View.OnClickListener {
    lateinit var speedTextView: TextView
    lateinit var distanceTextView: TextView
    lateinit var maxSpdTextView: TextView

    lateinit var saveBtn: Button
    lateinit var cancel: Button
    lateinit var noSaveBtn: Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setTitle("Сохранить трек?")
        val distance = arguments?.getDouble(TrackingFragment().DISTANCE_KEY)
        val v = inflater.inflate(R.layout.fragment_save_track, null)
        distanceTextView = v.findViewById(R.id.dialog_params_distance)
        distanceTextView.text = distance.toString()
        saveBtn = v.findViewById(R.id.save_btn)
        cancel = v.findViewById(R.id.cancel_btn)
        noSaveBtn = v.findViewById(R.id.no_save_btn)

        saveBtn.setOnClickListener(this)
        cancel.setOnClickListener(this)
        noSaveBtn.setOnClickListener(this)

        return v
    }

    override fun onClick(v: View?) {
        val intent = Intent()
        when (v?.id) {
            R.id.no_save_btn -> {
                dismiss()
            }
            R.id.cancel_btn -> {
                targetFragment?.onActivityResult(
                    targetRequestCode,
                    Activity.RESULT_CANCELED,
                    intent
                )
                dismiss()
            }
            R.id.save_btn -> {

                targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
                dismiss()


            }
        }
    }


}