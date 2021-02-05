package com.example.gotracker.screens.tracking

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.example.gotracker.GoTrackerApplication
import com.example.gotracker.LocationService
import com.example.gotracker.model.LocParams
import com.example.gotracker.utils.*
import java.util.*

class TrackingViewModel(application: Application) : AndroidViewModel(application) {
    private var intentService: Intent = Intent(getApplication(), LocationService::class.java)
    lateinit var locationService: LocationService


    private val connection = object : ServiceConnection {
        private lateinit var locationBinder: LocationService.LocationServiceBinder
        override fun onServiceDisconnected(name: ComponentName?) {
            LocationService.isBound = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            if (!::locationService.isInitialized) {
                locationBinder =
                    service as LocationService.LocationServiceBinder
                locationService = locationBinder.getLocationService()
                Log.d("ServiceConnection", "onServiceConnected")
            }
        }

    }

    fun doBindService() {

        getApplication<GoTrackerApplication>().bindService(
            intentService,
            connection,
            Context.BIND_AUTO_CREATE
        )
        LocationService.isBound = true

    }

    fun doUnbindService() {
        if (LocationService.isBound) {
            getApplication<GoTrackerApplication>().unbindService(connection)

            LocationService.isBound = false
        }

    }

    fun startClick() {
        locationService.startTime = System.currentTimeMillis()
        locationService.startLocationUpdates()
        LocationService.trackTimer.startTimer()
        LocationService.isStarted = true
    }

    fun pauseClick() {
        LocationService.isPaused = true
        LocationService.trackTimer.onPauseTimer()

    }

    fun resumeClick() {
        LocationService.isPaused = false
        locationService.addTrack()
        LocationService.trackTimer.offPauseTimer()
    }

    fun stopClick(onGoodParams: () -> Unit, onErrorParams: () -> Unit) {
        if (LocationService.liveLocationParams.value!!.distance > 0) {


            LocationService.trackTimer.onPauseTimer()
            onGoodParams()

        } else {
            LocationService.trackTimer.onPauseTimer()
            onErrorParams()
        }
    }

    fun setLocationParamsObserver(owner: LifecycleOwner, observer: Observer<LocParams>) {
        LocationService.liveLocationParams.observe(owner, observer)
    }

    fun setTimeObserver(owner: LifecycleOwner, observer: Observer<String>) {
        LocationService.trackTimer.liveCurrentTime.observe(owner, observer)
    }

    fun resultSave() {
        val locParams = LocationService.liveLocationParams.value
        val startTime = locationService.startTime

        saveTrack(locParams!!, startTime)
        stopTracking()
    }

    private fun saveTrack(locParams: LocParams, startTime: Long) {
        REPOSITORY.insertTrack(locParams, startTime)
    }

    fun stopTracking() {
        LocationService.trackTimer.stopTimer()
        LocationService.isPaused = false
        locationService.removeLocationUpdate()
        locationService.clearParams()
        LocationService.isStarted = false
        pauseOff()
    }

    fun pauseOff() {
        LocationService.trackTimer.offPauseTimer()
    }


}