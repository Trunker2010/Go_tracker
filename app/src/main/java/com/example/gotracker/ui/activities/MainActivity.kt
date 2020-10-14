package com.example.gotracker.ui.activities

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gotracker.R
import com.example.gotracker.model.Preload
import com.example.gotracker.model.Preload.Callback
import com.example.gotracker.model.User
import com.example.gotracker.ui.fragments.FragmentStatistic
import com.example.gotracker.ui.fragments.TrackListFragment
import com.example.gotracker.ui.fragments.TrackingFragment
import com.example.gotracker.utils.*
import kotlinx.android.synthetic.main.activity_main.*

const val PERMISSION_REQUEST_CODE = 7
const val LOC_PARAMS = 1


class MainActivity : AppCompatActivity(R.layout.activity_main) {
    lateinit var trackingFragment: TrackingFragment
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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initFunc(savedInstanceState: Bundle?) {

        if (AUTH.currentUser != null) {
            if (savedInstanceState == null) {

                trackingFragment = TrackingFragment.newInstance()
                ReplaceFragment(trackingFragment)
            }

            navigation.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.track -> {
                        initUserTracks()

                        trackingFragment = TrackingFragment.newInstance()
                        ReplaceFragment(trackingFragment)


                        //checkPermission()
                        true
                    }
                    R.id.list -> {
                        ReplaceFragment(TrackListFragment.newInstance())
                        true
                    }
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
        initFirebase()
        initUser()
    }

    private fun initUser() {
        REF_DATABASE_ROOT.child(NODE_USERS).child(UID)
            .addListenerForSingleValueEvent(AppValueEventListener {
                USER = it.getValue(User::class.java) ?: User()
                initUserTracks()
            })
    }


}




