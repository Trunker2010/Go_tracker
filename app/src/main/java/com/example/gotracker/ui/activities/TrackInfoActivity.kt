package com.example.gotracker.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.gotracker.R
import com.example.gotracker.ui.fragments.TRACK_ID
import com.example.gotracker.ui.fragments.TrackInfoFragment
import kotlinx.android.synthetic.main.activity_track_info.*

class TrackInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_info)


    }

    override fun onStart() {
        super.onStart()
        val extras = intent.extras
        if (extras != null) {
            val trackId = extras.getString(TRACK_ID)
            Log.d("TrackInfoActivity", trackId)
            val args = Bundle()
            args.putString(TRACK_ID, trackId)
            val fragment: Fragment = TrackInfoFragment()
            fragment.arguments = args
            supportFragmentManager.beginTransaction()
                .replace(track_info_f_container.id, fragment)
                .commit()
        }


    }
}