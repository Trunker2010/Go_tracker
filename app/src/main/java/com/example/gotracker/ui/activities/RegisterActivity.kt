package com.example.gotracker.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.gotracker.R
import com.example.gotracker.databinding.ActivityRegisterBinding
import com.example.gotracker.ui.fragments.EnterPhoneFragment
import com.example.gotracker.utils.initFirebase

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFirebase()
    }

    override fun onStart() {
        super.onStart()
        supportFragmentManager.beginTransaction()
            .add(R.id.register_data_container, EnterPhoneFragment())
            .commit()
    }
}
