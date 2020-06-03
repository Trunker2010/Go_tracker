package com.example.gotracker.ui.fragments


import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.PointF
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.gotracker.LocationService
import com.example.gotracker.R
import com.example.gotracker.STOP_LOC_SERVICE
import com.example.gotracker.databinding.FragmentTrackingBinding
import com.example.gotracker.model.LocParams
import com.example.gotracker.ui.activities.LOC_PARAMS
import com.example.gotracker.ui.activities.SettingsActivity
import com.example.gotracker.utils.*
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.*
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import java.util.*


const val START_TRACKING = "start_tracking"
const val SAVE_DIALOG_TAG = "saveDialog"
const val SAVE_DIALOG_REQUEST_CODE = 1

class TrackingFragment : Fragment(R.layout.fragment_tracking), UserLocationObjectListener,
    View.OnClickListener {

    private val FRAGMENT_TAG = "FragmentTracker"
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
    lateinit var locParamsHandler: Handler
    private lateinit var locParamsRunnable: Runnable
    private lateinit var polyline: PolylineMapObject
    val locParams = LocParams()
    private var saveDialog = SaveTrackDialogFragment()
    val DISTANCE_KEY: String = "speed"
    val MAX_SPEED_KEY = "max_sped"
    val TIME_KEY = "time"


    private fun getLocParams() {

        locParamsRunnable = object : Runnable {
            var message = Message()

            override fun run() {

                if (LocationService.isStarted && ::locationService.isInitialized) {
                    locParams.speed = locationService.speedKmH.toDouble()
                    locParams.distance = locationService.distanceKm
                    locParams.latitude = locationService.latitude
                    locParams.longitude = locationService.longitude
                    locParams.tracks_points = locationService.tracks
                    message = locParamsHandler.obtainMessage(LOC_PARAMS, locParams)
                    locParamsHandler.sendMessage(message)

                }
                locParamsHandler.postDelayed(this, 1000)
            }

        }
        locParamsRunnable.run()

    }

    private fun doBindService() {
        activity?.bindService(
            intentService,
            connection,
            Context.BIND_AUTO_CREATE
        )
        bound = true

    }

    private fun doUnbindService() {
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
        drawTrackPoints(locParams.tracks_points)

    }

    private fun drawTrackPoints(tracks: ArrayList<ArrayList<Point>>) {
        Log.i("FragmentTracking", "drawTrackPoints")


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
                    val locParams = msg.obj as LocParams
                    updateParams(locParams)

                }

            }
        }

        retainInstance = true

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
        binding.settingsBtn.setOnClickListener(this)
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
        doUnbindService()
        if (this@TrackingFragment::locParamsRunnable.isInitialized) {
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



                saveDialog.arguments = createBundleParams(locParams)
                saveDialog.setTargetFragment(this, SAVE_DIALOG_REQUEST_CODE)

                fragmentManager?.let {
                    saveDialog.show(it, SAVE_DIALOG_TAG)

                }


            }
            R.id.pause_btn -> {
                locParamsHandler.removeCallbacks(locParamsRunnable)
                locationService.removeLocationUpdate()
                locationService.isPaused = true
            }
            R.id.settings_btn -> {
                val intent = Intent(activity, SettingsActivity::class.java)
                startActivity(intent)
            }

        }
    }

    private fun createBundleParams(locaParams: LocParams): Bundle {
        val bundle = Bundle()
        bundle.putDouble(DISTANCE_KEY, locaParams.distance)
        return bundle

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SAVE_DIALOG_REQUEST_CODE -> {

                    mapObjects.clear()
                    intentService.action = STOP_LOC_SERVICE
                    doUnbindService()
                    activity?.stopService(intentService)
                    locationService.removeLocationUpdate()
                    saveTrack()

                    locationService.clearParams()
                    locParamsHandler.removeCallbacks(locParamsRunnable)
                    isStarted = false
                    changeButton()


                    Log.d(FRAGMENT_TAG, "SAVE_DIALOG_REQUEST_CODE")
                }
            }

        }

    }

    private fun saveTrack() {
        val dateMap = mutableMapOf<String, Any>()
        dateMap[CHILD_DISTANCE] = locParams.distance
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

    private fun setCamera() {

        if (locParams.latitude != 0.0) {
            val pos = Point(locParams.latitude, locParams.longitude)
            binding.mapview.map.move(
                CameraPosition(pos, 15.0f, 0.0f, 0.0f),
                Animation(Animation.Type.LINEAR, 0.3F),
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



