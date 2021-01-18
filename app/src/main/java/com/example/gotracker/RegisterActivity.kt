package com.example.gotracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation

import com.example.gotracker.R.*
import com.example.gotracker.databinding.ActivityRegisterBinding
import com.example.gotracker.utils.CURRENT_ACTIVITY
import com.example.gotracker.utils.initFirebase

class RegisterActivity : AppCompatActivity() {

    private var _binding: ActivityRegisterBinding? = null
    private val mBinding get() = _binding!!
    lateinit var mNavigation: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initFirebase()
        CURRENT_ACTIVITY = this
        mNavigation = Navigation.findNavController(this, id.nav_host_reg_fragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
