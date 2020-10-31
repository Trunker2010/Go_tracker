package com.example.gotracker

import android.app.Application
import com.yandex.mapkit.MapKitFactory
const val MAP_KIT_API_KEY = "6a3e8505-4082-499a-ba52-2a5c023e57ed"
class GoTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(MAP_KIT_API_KEY)
    }
}