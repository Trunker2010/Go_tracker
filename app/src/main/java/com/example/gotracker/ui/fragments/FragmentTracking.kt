package com.example.gotracker.ui.fragments


import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.PointF
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.gotracker.LocationService
import com.example.gotracker.R
import com.example.gotracker.STOP_LOC_SERVICE
import com.example.gotracker.databinding.FragmentTrackingBinding
import com.example.gotracker.model.LocParams
import com.example.gotracker.ui.LOC_PARAMS
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import java.util.*
import kotlin.collections.ArrayList


const val START_TRACKING = "start_tracking"
const val TAG_FRAGMENT = "fragment"


class TrackingFragment : Fragment(), UserLocationObjectListener, View.OnClickListener {

    private val MAP_KIT_API_KEY = "6a3e8505-4082-499a-ba52-2a5c023e57ed"
    lateinit var userLocationLayer: UserLocationLayer
    lateinit var mapKit: MapKit
    lateinit var binding: FragmentTrackingBinding
    var isStarted: Boolean = false
    lateinit var locationService: LocationService
    private lateinit var intentService: Intent
    private var bound: Boolean = false
    private lateinit var connection: ServiceConnection
    private lateinit var mapObjects: MapObjectCollection
    lateinit var locationListener: LocationListener
    lateinit var locParamsHandler: Handler
    private lateinit var locParamsRunnable: Runnable
    private lateinit var polyline: PolylineMapObject
    val locParams = LocParams()

    private fun getLocParams() {

        locParamsRunnable = object : Runnable {
            var message = Message()

            override fun run() {

                if (LocationService.isStarted && ::locationService.isInitialized) {
                    locParams.speed = locationService.speedKmH.toDouble()
                    locParams.distance = locationService.distanceKm
                    locParams.latitude = locationService.latitude
                    locParams.longitude = locationService.longitude
                    locParams.tracks = locationService.tracks
                    message = locParamsHandler.obtainMessage(LOC_PARAMS, locParams)
                    locParamsHandler.sendMessage(message)

                }
                locParamsHandler.postDelayed(this, 1000)
            }

        }
        locParamsRunnable.run()

    }

    fun doBindService() {
        activity?.bindService(
            intentService,
            connection,
            Context.BIND_AUTO_CREATE
        )
        bound = true

    }

    fun doUnbindeSerivice() {
        if (bound) {
            activity?.unbindService(connection)
            bound = false
        }

    }


    fun updateParams(locParams: LocParams) {
        setCamera()
        binding.speedM.text = (String.format(
            Locale.getDefault(),
            "%1$,.2f", locParams.speed

        ))
        binding.distanceM.text = (String.format(
            Locale.getDefault(),
            "%1$,.2f", locParams.distance

        ))
        drawTrackPoints(locParams.tracks)

    }

    private fun drawTrackPoints(tracks: ArrayList<ArrayList<Point>>) {
        Log.d("FragmentTracking", "drawTrackPoints")


        for (track in tracks) {

            polyline = mapObjects.addPolyline(Polyline(track))

        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey(MAP_KIT_API_KEY)
        MapKitFactory.initialize(activity)
        isStarted = LocationService.isStarted
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
        intentService = Intent(activity, LocationService::class.java)

        locParamsHandler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {

                if (msg.what == LOC_PARAMS) {
                    var locParams = msg.obj as LocParams
                    updateParams(locParams)

                }

            }
        }


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackingBinding.inflate(layoutInflater)
        binding.mapview.map.isRotateGesturesEnabled = true
        mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(binding.mapview.mapWindow)
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.isVisible = true
        userLocationLayer.setObjectListener(this)
        userLocationLayer.isAutoZoomEnabled = true
        binding.startBtn.setOnClickListener(this)
        binding.stopBtn.setOnClickListener(this)
        binding.pauseBtn.setOnClickListener(this)
        mapObjects = binding.mapview.map.mapObjects.addCollection()



        setButton()
        doBindService()
        return binding.root
    }


    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapview.onStart()


    }

    override fun onResume() {
        if (LocationService.isStarted) {
            getLocParams()
        }

        setCamera()
        super.onResume()
    }

    override fun onStop() {
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        doUnbindeSerivice()
        if (this@TrackingFragment::locParamsRunnable.isInitialized){
            locParamsHandler.removeCallbacks(locParamsRunnable)
        }




        super.onStop()

    }

    companion object {

        fun newInstance(): TrackingFragment {
            return TrackingFragment()
        }
    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {

    }

    override fun onObjectRemoved(p0: UserLocationView) {

    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        if (activity != null) {

            userLocationView.arrow.setIcon(
                ImageProvider.fromResource(
                    activity, R.drawable.user_arrow
                ), IconStyle().setRotationType(RotationType.ROTATE)
            )


            var pinIcon = userLocationView.pin.useCompositeIcon()
            pinIcon.setIcon(
                "pin",
                ImageProvider.fromResource(
                    activity,
                    R.drawable.search_result
                ),
                IconStyle().setAnchor(PointF(0.0f, 0.0f))
                    .setRotationType(RotationType.ROTATE)
                    .setZIndex(5f)
                    .setScale(0f)

            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(v: View?) {


        when (v?.id) {

            R.id.start_btn -> {
                mapObjects = binding.mapview.map.mapObjects.addCollection()
                setCamera()
                intentService.action = START_TRACKING

                activity?.startService(intentService)
                locationService.startLocationUpdates()
                isStarted = true
                getLocParams()
                changeButton()

            }
            R.id.stop_btn -> {
                Log.d("FragmentTracking", "stop_btn")
                mapObjects.clear()
                intentService.action = STOP_LOC_SERVICE
                doUnbindeSerivice()
                activity?.stopService(intentService)
                locationService.removeLocationUpdate()
                locParamsHandler.removeCallbacks(locParamsRunnable)
                isStarted = false
                changeButton()

            }
            R.id.pause_btn -> {
                locParamsHandler.removeCallbacks(locParamsRunnable)
                locationService.removeLocationUpdate()
                locationService.isPaused = true
            }

        }
    }

    fun setCamera() {

        if (locParams.latitude != 0.0) {
            val pos = Point(locParams.latitude, locParams.longitude)
            binding.mapview.map.move(
                CameraPosition(pos, 15.0f, 0.0f, 0.0f),
                Animation(Animation.Type.LINEAR, 0.5F),
                null
            )
        }


    }

    private fun changeButton() {
        if (isStarted) {
            binding.stoppedLayout.visibility = View.GONE
            binding.startedLayout.visibility = View.VISIBLE


        } else {

            binding.stoppedLayout.visibility = View.VISIBLE
            binding.startedLayout.visibility = View.GONE
        }
    }

    private fun setButton() {
        if (LocationService.isStarted) {
            binding.stoppedLayout.visibility = View.GONE
            binding.startedLayout.visibility = View.VISIBLE
        } else {
            binding.stoppedLayout.visibility = View.VISIBLE
            binding.startedLayout.visibility = View.GONE
        }
    }

}



