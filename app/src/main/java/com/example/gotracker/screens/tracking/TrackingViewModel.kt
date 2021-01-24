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
                Log.d("connection", "onServiceConnected")
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
        locationService.trackTimer.startTimer()
        LocationService.isStarted = true
    }

    fun pauseClick() {
        LocationService.isPaused = true
        locationService.trackTimer.onPauseTimer()

    }

    fun resumeClick() {
        LocationService.isPaused = false
        locationService.addTrack()
        locationService.trackTimer.offPauseTimer()
    }

    fun stopClick(onGoodParams: () -> Unit, onErrorParams: () -> Unit) {
        if (locationService.liveLocationParams.value!!.distance > 0) {


            locationService.trackTimer.onPauseTimer()
            onGoodParams()

        } else {


            locationService.trackTimer.onPauseTimer()
            onErrorParams()
        }
    }

    fun setLocationParamsObserver(owner: LifecycleOwner, observer: Observer<LocParams>) {
        locationService.liveLocationParams.observe(owner, observer)
    }

    fun setTimeObserver(owner: LifecycleOwner, observer: Observer<String>) {
        locationService.trackTimer.liveCurrentTime.observe(owner, observer)
    }

    fun resultSave() {
        val locParams = locationService.liveLocationParams.value

        saveTrack(locParams!!)
        stopTracking()

    }

    private fun saveTrack(locParams: LocParams) {
        val dateMap = mutableMapOf<String, Any>()
        dateMap[CHILD_DISTANCE] = locParams.distance
        dateMap[CHILD_TIME_DURATION] = locParams.durationTimeMS
        dateMap[CHILD_START_TIME] = locationService.startTime
        dateMap[CHILD_MAX_SPEED] = locParams.maxSpeed

        REF_DATABASE_ROOT.child(NODE_TRACKS).child(AUTH.uid.toString())
            .child(dateMap.hashCode().toString())
            .updateChildren(dateMap)


        for ((trackNumber, track) in locationService.tracks.withIndex()) {

            var pos = 0;
            track.forEach {

                REF_DATABASE_ROOT.child(NODE_TRACKS).child(AUTH.uid.toString())
                    .child(dateMap.hashCode().toString())
                    .child(CHILD_TRACK_POINTS).child(trackNumber.toString())
                    .child(pos.toString()).child(CHILD_LATITUDE).setValue(it.latitude)

                REF_DATABASE_ROOT.child(NODE_TRACKS).child(AUTH.uid.toString())
                    .child(dateMap.hashCode().toString())
                    .child(CHILD_TRACK_POINTS).child(trackNumber.toString())
                    .child(pos.toString()).child(CHILD_LONGITUDE).setValue(it.longitude)
                pos++
            }
        }
    }

    fun stopTracking() {
        locationService.trackTimer.stopTimer()
        LocationService.isPaused = false
        locationService.removeLocationUpdate()
        locationService.clearParams()
        LocationService.isStarted = false
        pauseOff()
    }

    fun pauseOff() {
        locationService.trackTimer.offPauseTimer()
    }


}