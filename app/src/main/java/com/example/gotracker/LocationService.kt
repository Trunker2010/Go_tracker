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
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.gotracker.model.TrackTimer
import com.example.gotracker.ui.fragments.START_TRACKING
import com.example.gotracker.ui.fragments.TrackingFragment
import com.example.gotracker.utils.LocationConverter
import com.example.gotracker.utils.MyLocationListener
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.delay
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

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
        var isBound = false
        var isStarted: Boolean = false
        var isPaused: Boolean = false

    }


    lateinit var locationServiceBinder: LocationServiceBinder
    val PERMISSION_STRING = Manifest.permission.ACCESS_FINE_LOCATION

    var speedKmH: Float = 0.0f
    var distanceInMeters: Double = 0.0
    var maxSpeed: Float = 0.0F
    var tracks = ArrayList<ArrayList<Point>>()
    var distanceKm: Double = 0.0

    var lastLocation: Location? = null
    lateinit var mNotificationManager: NotificationManager
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    /*Таймер*/
    var trackTimer = TrackTimer()
    var startTime = 0L

    lateinit var locationNotification: LocationNotification
    lateinit var locationManager: LocationManager


    val locationListener = object : MyLocationListener() {
        override fun onLocationChanged(location: Location?) {

            val mNotificationManager =
                baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            Log.d("LocationService", "accuracy: ${location!!.accuracy}  dst$distanceInMeters ")
            if (lastLocation == null || isPaused) {
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
    }


    override fun onBind(intent: Intent?): IBinder? {

        return if (!this@LocationService::locationServiceBinder.isInitialized) {
            locationServiceBinder = LocationServiceBinder()

            isBound = true
            Log.d("LocationService", "bind")
            locationServiceBinder
        } else {
            locationServiceBinder
        }


    }

    override fun onUnbind(intent: Intent?): Boolean {

        //return super.onUnbind(intent)
        isBound = false
        Log.d("LocationService", "unbind")
        Toast.makeText(
            this, "Служба отвязана",
            Toast.LENGTH_SHORT
        ).show()

        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        addTrack()
        mNotificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        locationNotification = LocationNotification(context = this)
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager


    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                PERMISSION_STRING
            ) === PackageManager.PERMISSION_GRANTED
        ) {

            startForeground(MINE_NOTIFICATION_ID, locationNotification.createNotification())
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
            contentView.setTextViewText(R.id.card_distance_params, dst)
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

        }
    }

    fun addTrack() {
        tracks.add(ArrayList())

    }

    fun removeLocationUpdate() {
        locationManager.removeUpdates(locationListener)
    }


    fun clearParams() {
        speedKmH = 0.0f
        distanceKm = 0.0
        distanceInMeters = 0.0
        maxSpeed = 0.0f
        tracks.clear()
        lastLocation = null
        addTrack()
        stopForeground(true)

    }


}

