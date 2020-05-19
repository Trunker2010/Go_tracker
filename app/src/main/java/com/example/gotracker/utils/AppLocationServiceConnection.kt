package com.example.gotracker.utils

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.example.gotracker.LocationService

class AppLocationServiceConnection : ServiceConnection {

    lateinit var locationService: LocationService
    private lateinit var locationBinder: LocationService.LocationServiceBinder
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

        if (!::locationService.isInitialized) {
            locationBinder =
                service as LocationService.LocationServiceBinder
            locationService = locationBinder.getLocationService()
        }


    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }
}