package com.example.gotracker.ui

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gotracker.LocationService
import com.example.gotracker.R
import com.example.gotracker.model.LocParams
import com.example.gotracker.ui.fragments.FragmentStatistic
import com.example.gotracker.ui.fragments.TrackingFragment
import com.example.gotracker.utils.ReplaceFragment
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.mapview.MapView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

const val PERMISSION_REQUEST_CODE = 7
const val LOC_PARAMS = 1

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    val ft = supportFragmentManager.beginTransaction()
    lateinit var speedTextView: TextView

    lateinit var yandexMap: MapView
    lateinit var distanceTextView: TextView
    lateinit var locParamsHandler: Handler
    lateinit var locationService: LocationService
    lateinit var connection: ServiceConnection
    private lateinit var intentService: Intent
    lateinit var trackingFragment: TrackingFragment
    private var bound: Boolean = false
    lateinit var locParamsRunnable: Runnable
    lateinit var mapObjects: MapObjectCollection


    fun doBindService() {
        bindService(
            intentService,
            connection,
            Context.BIND_AUTO_CREATE
        )
        bound = true

    }

    fun updateParams(locParams: LocParams) {
        initViews()
        speedTextView.text = (String.format(
            Locale.getDefault(),
            "%1$,.2f", locParams.speed

        ))
        distanceTextView.text = (String.format(
            Locale.getDefault(),
            "%1$,.2f", locParams.distance

        ))
        drawTrackPoints(locParams.track)
    }

    private fun initViews() {
        val trackingBinding = trackingFragment.binding
        speedTextView = trackingBinding.speedM
        distanceTextView = trackingBinding.distanceM
        yandexMap = trackingBinding.mapview
        mapObjects = yandexMap.map.mapObjects.addCollection();
    }

    fun doUnbindeSerivice() {
        if (bound) {
            unbindService(connection)
            bound = false
        }

    }


    fun drawTrackPoints(position: ArrayList<Point>) {

        var polyline: PolylineMapObject = mapObjects.addPolyline(Polyline(position))
        polyline.strokeColor = android.graphics.Color.GREEN


    }


    fun getLocParams() {

        locParamsRunnable = object : Runnable {
            var message = Message()
            val locParams = LocParams()
            override fun run() {

                if (LocationService.isStarted && this@MainActivity::locationService.isInitialized) {
                    locParams.speed = locationService.speedKmH.toDouble()
                    locParams.distance = locationService.distanceKm
                    locParams.latitude = locationService.latitude
                    locParams.longitude = locationService.longitude
                    locParams.track = locationService.track
                    message = locParamsHandler.obtainMessage(LOC_PARAMS, locParams)
                    locParamsHandler.sendMessage(message)


                }
                locParamsHandler.postDelayed(this, 1000)
            }

        }
        locParamsRunnable.run()

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        trackingFragment = TrackingFragment.newInstance()


        ReplaceFragment(trackingFragment)
        intentService = Intent(this, LocationService::class.java)
        connection = object : ServiceConnection {
            private lateinit var locationBinder: LocationService.LocationServiceBinder
            override fun onServiceDisconnected(name: ComponentName?) {

            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                if (!::locationService.isInitialized) {
                    locationBinder =
                        service as LocationService.LocationServiceBinder
                    locationService = locationBinder.getLocationService()
                }
            }
        }


        locParamsHandler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                var locParams = msg.obj as LocParams
                if (msg.what == LOC_PARAMS) {
                    if (navigation.selectedItemId == R.id.track && navigation != null && bound) {


                        updateParams(locParams)
                        setCamera()

                        drawTrackPoints(locParams.track)
                    }
                }

            }
        }

        navigation.setOnNavigationItemSelectedListener { item ->
            if (item.itemId != R.id.track && this::locParamsRunnable.isInitialized && bound) {
                doUnbindeSerivice()
                locParamsHandler.removeCallbacks(locParamsRunnable)
            }

            when (item.itemId) {
                R.id.track -> {

                    doBindService()
                    if (!this@MainActivity::trackingFragment.isInitialized) {
                        trackingFragment = TrackingFragment.newInstance()
                        if (LocationService.isStarted) {
                            setCamera()
                        }

                    }
                    ReplaceFragment(trackingFragment)
                    getLocParams()

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


    }

    fun setPauselocSevice() {
        locationService.isPaused = true
    }

    fun setCamera() {


        trackingFragment.userLocationLayer.cameraPosition()?.let {
            yandexMap.map.move(
                it,
                Animation(
                    Animation.Type.LINEAR,
                    1F
                ),
                null
            )
        }


    }

    override fun onStart() {

        doBindService()
        getLocParams()
        checkPermission()
        super.onStart()
    }


    override fun onDestroy() {
        super.onDestroy()
        locParamsHandler.removeCallbacks(locParamsRunnable)
        doUnbindeSerivice()
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


}




