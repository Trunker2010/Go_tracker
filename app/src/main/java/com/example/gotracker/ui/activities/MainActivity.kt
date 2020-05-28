package com.example.gotracker.ui.activities

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gotracker.R
import com.example.gotracker.ui.fragments.FragmentStatistic
import com.example.gotracker.ui.fragments.TrackingFragment
import com.example.gotracker.utils.AUTH
import com.example.gotracker.utils.ReplaceFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

const val PERMISSION_REQUEST_CODE = 7
const val LOC_PARAMS = 1

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    lateinit var trackingFragment: TrackingFragment
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFields()
        initFunc(savedInstanceState)
    }



    override fun onStart() {
        checkPermission()

        super.onStart()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.ACCESS_FINE_LOCATION)
            !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }

    }

    private fun initFunc(savedInstanceState: Bundle?) {

        if (AUTH.currentUser !=null) {
            if (savedInstanceState == null) {

                trackingFragment = TrackingFragment.newInstance()
                ReplaceFragment(trackingFragment)
            }

            navigation.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.track -> {
                        trackingFragment = TrackingFragment.newInstance()
                        ReplaceFragment(trackingFragment)


                        //checkPermission()
                        true
                    }
                    R.id.list -> true
                    R.id.statistic -> {
                        ReplaceFragment(FragmentStatistic.newInstance())
                        true
                    }
                    else -> false
                }

            }
        } else {

            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun initFields() {
        AUTH = FirebaseAuth.getInstance()

    }


}




