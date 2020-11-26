package com.example.gotracker.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.gotracker.R
import com.example.gotracker.ui.fragments.*
import kotlinx.android.synthetic.main.activity_track_info.*

class TrackInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_info)
        val extras = intent.extras
        if (extras != null) {
            val fragment: Fragment = TrackInfoFragment()
            fragment.arguments = extras
            supportFragmentManager.beginTransaction()
                .replace(track_info_f_container.id, fragment)
                .commit()
        }

    }

    override fun onStart() {
        super.onStart()

    }

}