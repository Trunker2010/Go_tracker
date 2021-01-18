package com.example.gotracker.screens.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.gotracker.R
import com.example.gotracker.screens.settings.SettingsFragment
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity(R.layout.activity_settings) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(settings_data_container.id, SettingsFragment())
            .commit()
    }

    override fun onStart() {
        super.onStart()


    }
}
