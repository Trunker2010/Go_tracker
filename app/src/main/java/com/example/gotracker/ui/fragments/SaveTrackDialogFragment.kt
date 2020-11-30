package com.example.gotracker.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.gotracker.R
import com.example.gotracker.utils.LocationConverter
import java.util.*


class SaveTrackDialogFragment : DialogFragment(), View.OnClickListener {

    lateinit var distanceTV: TextView
    lateinit var maxSpeedTV: TextView
    lateinit var timeDurationTV: TextView

    lateinit var saveBtn: Button
    lateinit var cancel: Button
    lateinit var noSaveBtn: Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setTitle("Сохранить трек?")

        val distance = requireArguments().getDouble(DISTANCE_KEY)
        val time = requireArguments().getLong(TIME_KEY)
        val maxSpeed = requireArguments().getFloat(MAX_SPEED_KEY)
        val v = inflater.inflate(R.layout.fragment_save_track, null)
        initViews(v)

        distanceTV.text = (String.format(Locale.getDefault(), "%.2f", distance))
        timeDurationTV.text = LocationConverter.convertMStoTime(time)
        maxSpeedTV.text = (String.format(Locale.getDefault(), "%.2f", maxSpeed))


        saveBtn.setOnClickListener(this)
        cancel.setOnClickListener(this)
        noSaveBtn.setOnClickListener(this)

        return v
    }

    private fun initViews(v: View) {
        distanceTV = v.findViewById(R.id.dialog_params_distance)
        timeDurationTV = v.findViewById(R.id.dialog_params_duration)
        maxSpeedTV = v.findViewById(R.id.dialog_params_maxSpd)
        saveBtn = v.findViewById(R.id.save_btn)
        cancel = v.findViewById(R.id.cancel_btn)
        noSaveBtn = v.findViewById(R.id.no_save_btn)
    }


    override fun onClick(v: View) {
        val intent = Intent()
        when (v.id) {
            R.id.no_save_btn -> {
                dismiss()
                // TODO: 26.11.2020 обработать "не сохранять"
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