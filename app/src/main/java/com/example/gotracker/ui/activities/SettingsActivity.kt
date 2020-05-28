package com.example.gotracker.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.gotracker.R
import com.example.gotracker.ui.fragments.SettingsFragment
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity(R.layout.activity_settings) {

    override fun onStart() {
        super.onStart()
        supportFragmentManager.beginTransaction()
            .add(settings_data_container.id, SettingsFragment())
            .commit()

    }
}
