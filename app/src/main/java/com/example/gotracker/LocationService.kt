package com.example.gotracker

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.gotracker.ui.fragments.START_TRACKING
import com.example.gotracker.utils.LocationConverter
import com.yandex.mapkit.geometry.Point
import java.util.*
import kotlin.collections.ArrayList

const val UPDATE_NOTIFY_CHANNEL = "update_notify"
const val MINE_NOTIFICATION_ID = 1
const val STOP_LOC_SERVICE = "goTracker_stopLocServ"
const val START_TRACKING = "goTracker_start_tracking"


class LocationService : Service() {
    inner class LocationServiceBinder : Binder() {
        fun getLocationService(): LocationService {
            return this@LocationService
        }
    }

    companion object {
        var isStarted: Boolean = false
    }

    var isPaused: Boolean = false
    val locationServiceBinder = LocationServiceBinder()
    val PERMISSION_STRING = Manifest.permission.ACCESS_FINE_LOCATION
    var speedKmH: Float = 0.0f
    var distanceInMeters: Double = 0.0
    var maxSpeed: Float = 0.0F
    lateinit var lastLocation: Location
    lateinit var mNotificationManager: NotificationManager
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var tracks = ArrayList<ArrayList<Point>>()
    var distanceKm: Double = 0.0
    lateinit var locationNotification: LocationNotification
    lateinit var locationManager: LocationManager


    val locationListener = object : LocationListener {

        override fun onLocationChanged(location: Location?) {

            val mNotificationManager =
                baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            Log.d("LocationService", "accuracy: ${location!!.accuracy}  dst$distanceInMeters ")
            if (!this@LocationService::lastLocation.isInitialized) {
                lastLocation = location
            }
            if (!isPaused) {
                distanceInMeters += location.distanceTo(lastLocation)
                lastLocation = location

                speedKmH = LocationConverter.convertSpeed(location.speed)
                distanceKm = LocationConverter.convertDistance(distanceInMeters)

                if (maxSpeed < speedKmH) {
                    maxSpeed = speedKmH
                }
                latitude = location.latitude
                longitude = location.longitude
                val point = Point(latitude, longitude)
                tracks[tracks.lastIndex].add(point)
                mNotificationManager.notify(
                    MINE_NOTIFICATION_ID,
                    locationNotification.updateNotification(
                        speed = String.format(
                            Locale.getDefault(),
                            "%1$,.2f",
                            speedKmH
                        ),
                        dst = String.format(
                            Locale.getDefault(),
                            "%1$,.2f",
                            LocationConverter.convertDistance(distanceInMeters)
                        )
                    )
                )
            }


        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            TODO("Not yet implemented")
        }

        override fun onProviderEnabled(provider: String?) {
            TODO("Not yet implemented")
        }

        override fun onProviderDisabled(provider: String?) {
            TODO("Not yet implemented")
        }

    }


    override fun onBind(intent: Intent?): IBinder? {
        startService(intent)
        return locationServiceBinder

    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        return true
    }

    override fun onCreate() {
        super.onCreate()
        addTrack()
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager


    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        mNotificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        locationNotification = LocationNotification(context = this)

        when (intent?.action) {
            START_TRACKING -> {
                val notification = locationNotification.createNotification()
                startForeground(MINE_NOTIFICATION_ID, notification)
                speedKmH = 0.0f

                isStarted = true

            }


        }

        return START_REDELIVER_INTENT
    }

    fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                PERMISSION_STRING
            ) === PackageManager.PERMISSION_GRANTED
        ) {

            val provider: String = locationManager.getBestProvider(Criteria(), true)
            locationManager.requestLocationUpdates(provider, 1000, 1f, locationListener)
        }

    }

    override fun onDestroy() {
        removeLocationUpdate()
        Toast.makeText(
            this, "Служба остановлена",
            Toast.LENGTH_SHORT
        ).show()
        isStarted = false
        super.onDestroy()
    }


    class LocationNotification(private val context: Context) {
        val CHANNEL_ID = "1"
        private val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        @RequiresApi(Build.VERSION_CODES.O)
        fun createNotification(): Notification {
            val intentUpdate = Intent(context, LocationService::class.java)
            intentUpdate.action = UPDATE_NOTIFY_CHANNEL
            val startPendingIntent = PendingIntent.getService(context, 0, intentUpdate, 0)

            val icon = R.mipmap.ic_launcher
            val when_ms = System.currentTimeMillis()

            val contentView = RemoteViews(context.packageName, R.layout.service_notify)
            contentView.setOnClickPendingIntent(R.id.start_btn, startPendingIntent)
            createChannel()
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(icon)
                .setCustomContentView(contentView)
                .setTicker("tracker")
                .setContentIntent(startPendingIntent)
                .setContentTitle("title")
                .setOngoing(true)
                .setWhen(when_ms)
                .build()
            mNotificationManager.notify(1, notification)
            return notification


        }

        fun updateNotification(speed: String, dst: String): Notification {
            val intentUpdate = Intent(context, LocationService::class.java)
            intentUpdate.action = UPDATE_NOTIFY_CHANNEL
            val startPendingIntent = PendingIntent.getService(context, 0, intentUpdate, 0)

            val icon = R.mipmap.ic_launcher
            val when_ms = System.currentTimeMillis()
            val contentView = RemoteViews(context.packageName, R.layout.service_notify)
            contentView.setOnClickPendingIntent(R.id.start_btn, startPendingIntent)
            contentView.setTextViewText(R.id.speed_params, speed)
            contentView.setTextViewText(R.id.distance_params, dst)
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(icon)
                .setCustomContentView(contentView)
                .setTicker("tracker")
                .setContentIntent(startPendingIntent)
                .setContentTitle("title")
                .setWhen(when_ms)
                .build()

            return notification
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createChannel() {

            val channelName: CharSequence = "tracking channel"
            val importance = NotificationManager.IMPORTANCE_MIN
            val notificationChannel = NotificationChannel(CHANNEL_ID, channelName, importance)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            }

            mNotificationManager.createNotificationChannel(notificationChannel)
            //return CHANNEL_ID
        }
    }

    fun addTrack() {
        tracks.add(ArrayList())
    }

    fun removeLocationUpdate() {
        locationManager.removeUpdates(locationListener)
    }


}

