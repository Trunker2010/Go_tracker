package com.example.gotracker

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.gotracker.databinding.ActivityMainBinding
import com.example.gotracker.model.User
import com.example.gotracker.screens.tracking.TrackingFragment

import com.example.gotracker.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

const val PERMISSION_REQUEST_CODE = 7
const val LOC_PARAMS = 1
lateinit var navController: NavController

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        navController = findNavController(R.id.mine_fragment_cont)
        binding.navigation.setupWithNavController(navController)
        initFields()
        initFunc(savedInstanceState)


//        setupActionBarWithNavController(navController, appBarConfiguration)


    }


    override fun onStart() {
        checkPermission()

        super.onStart()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(baseContext, ACCESS_BACKGROUND_LOCATION)
            !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(ACCESS_BACKGROUND_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initFunc(savedInstanceState: Bundle?) {

        if (AUTH.currentUser != null) {
            if (savedInstanceState == null) {

            }

        } else {

            navController.navigate(R.id.registerActivity)


            finish()
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun initFields() {
        initFirebase()
    }



}