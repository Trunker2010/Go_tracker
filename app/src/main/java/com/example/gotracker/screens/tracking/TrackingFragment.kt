package com.example.gotracker.screens.tracking


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.gotracker.LocationService
import com.example.gotracker.R
import com.example.gotracker.databinding.FragmentTrackingBinding
import com.example.gotracker.model.LocParams
import com.example.gotracker.screens.saveErrorDialog.RESULT_RESUMED
import com.example.gotracker.screens.saveErrorDialog.SaveErrDialogFragment
import com.example.gotracker.screens.saveTrackDialog.SaveTrackDialogFragment
import com.example.gotracker.screens.settings.SettingsActivity
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import java.util.*
import kotlin.collections.ArrayList


const val START_TRACKING = "start_tracking"
const val SAVE_DIALOG_TAG = "saveDialog"
const val SAVE_DIALOG_ERR_TAG = "saveDialogErr"
const val SAVE_DIALOG_REQUEST_CODE = 1
const val SAVE_ERR_DIALOG_REQUEST_CODE = 2
const val DISTANCE_KEY = "speed"
const val MAX_SPEED_KEY = "max_sped "
const val TIME_KEY = "time"

class TrackingFragment : Fragment(R.layout.fragment_tracking), UserLocationObjectListener,
    View.OnClickListener, CameraListener {
    private var isTouched = false
    private var camZoom = 15.0F
    private val FRAGMENT_TAG = "FragmentTracker"
    lateinit var userLocationLayer: UserLocationLayer
    lateinit var mapKit: MapKit
    lateinit var binding: FragmentTrackingBinding
    lateinit var locationService: LocationService
    lateinit var mViewModel: TrackingViewModel
    private lateinit var mapObjects: MapObjectCollection
    private lateinit var polyline: PolylineMapObject
    var locParams = LocParams()
    private var saveTrackDialogFragment = SaveTrackDialogFragment()
    private var saveErrDialogFragment = SaveErrDialogFragment()
    lateinit var updateTimeObserver: Observer<String>
    lateinit var updateLocParamsObserver: Observer<LocParams>


    private fun updateTime(it: String) {
        binding.currentTime.text = it
    }

    private fun updateLocParams(locParams: LocParams) {

        this.locParams = locParams
        setCamera()


        binding.speedM.text = (String.format(
            Locale.getDefault(),
            "%1$,.2f", locParams.speed

        ))
        binding.distanceM.text = (String.format(
            Locale.getDefault(),
            "%1$,.2f", locParams.distance

        ))
//        binding.currentTime.text = LocationConverter.convertMStoTime(locParams.durationTimeMS)
        drawTrackPoints(locParams.trackPoints)

    }


    private fun drawTrackPoints(tracks: ArrayList<ArrayList<Point>>) {
        // Log.i("FragmentTracking", "drawTrackPoints")


        for (track in tracks) {

            polyline = mapObjects.addPolyline(Polyline(track))

        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        MapKitFactory.initialize(requireActivity().applicationContext)


        retainInstance = true

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackingBinding.inflate(layoutInflater)

        binding.trackingMapView.map.isRotateGesturesEnabled = true





        mapKit = MapKitFactory.getInstance()
        binding.findMe.setOnClickListener(this)
        binding.trackingMapView.map.addCameraListener(this)
        userLocationLayer = mapKit.createUserLocationLayer(binding.trackingMapView.mapWindow)
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.isVisible = true
        userLocationLayer.setObjectListener(this)
        userLocationLayer.isAutoZoomEnabled = true
        binding.startBtn.setOnClickListener(this)
        binding.stopBtn.setOnClickListener(this)
        binding.pauseBtn.setOnClickListener(this)
        binding.settingsBtn.setOnClickListener(this)
        binding.resumeBtn.setOnClickListener(this)
        mapObjects = binding.trackingMapView.map.mapObjects.addCollection()


        setButton()
        changeButton()
        return binding.root
    }


    override fun onStart() {
        super.onStart()
        mViewModel = ViewModelProvider(this).get(TrackingViewModel::class.java)
        MapKitFactory.getInstance().onStart()
        binding.trackingMapView.onStart()
        mViewModel.doBindService()


    }

    override fun onResume() {
//        setCamera()


        super.onResume()
    }

    override fun onStop() {
        binding.trackingMapView.onStop()
        MapKitFactory.getInstance().onStop()


        if (!LocationService.isStarted) {
            mViewModel.doUnbindService()
        }


        super.onStop()

    }


    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {

    }

    override fun onObjectRemoved(p0: UserLocationView) {

    }


    override fun onObjectAdded(userLocationView: UserLocationView) {


        userLocationView.arrow.setIcon(
            ImageProvider.fromResource(
                requireActivity().applicationContext, R.drawable.user_arrow
            ), IconStyle().setRotationType(RotationType.ROTATE)

        )


        var pinIcon = userLocationView.pin.useCompositeIcon()
        pinIcon.setIcon(
            "pin",
            ImageProvider.fromResource(
                activity?.applicationContext,
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
                updateLocParamsObserver = Observer { updateLocParams(it) }
                updateTimeObserver = Observer { updateTime(it) }

                mapObjects = binding.trackingMapView.map.mapObjects.addCollection()
                isTouched = false
                setCamera()



                mViewModel.startClick()
                mViewModel.setLocationParamsObserver(this, updateLocParamsObserver)
                mViewModel.setTimeObserver(
                    this, updateTimeObserver
                )


                changeButton()
            }
            R.id.stop_btn -> {


                Log.d("FragmentTracking", "stop_btn")
                mViewModel.stopClick(
                    {
                        saveTrackDialogFragment.arguments = createBundleParams(locParams)
                        saveTrackDialogFragment.setTargetFragment(this, SAVE_DIALOG_REQUEST_CODE)


                        saveTrackDialogFragment.show(
                            parentFragmentManager,
                            SAVE_DIALOG_TAG
                        )
                    },
                    {
                        saveErrDialogFragment.setTargetFragment(this, SAVE_ERR_DIALOG_REQUEST_CODE)
                        saveErrDialogFragment.show(
                            parentFragmentManager,
                            SAVE_DIALOG_ERR_TAG
                        )
                    }

                )


            }
            R.id.pause_btn -> {
                mViewModel.pauseClick()
                changeButton()

            }
            R.id.resume_btn -> {
                mViewModel.resumeClick()
                changeButton()
            }
            R.id.settings_btn -> {
                val intent = Intent(activity?.applicationContext, SettingsActivity::class.java)
                startActivity(intent)


            }
            R.id.find_me -> {
                Log.d("findMe", "onclick")
                isTouched = false
                setCamera()
            }
        }


    }


    private fun createBundleParams(locParams: LocParams): Bundle {
        val bundle = Bundle()
        bundle.putDouble(DISTANCE_KEY, locParams.distance)
        bundle.putLong(TIME_KEY, locParams.durationTimeMS)
        bundle.putFloat(MAX_SPEED_KEY, locParams.maxSpeed)
        return bundle

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SAVE_DIALOG_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {


                    mViewModel.resultSave()
                    mapObjects.clear()
                    Intent(requireActivity().applicationContext, LocationService::class.java)

                    changeButton()
                    Log.d(FRAGMENT_TAG, "SAVE_DIALOG_REQUEST_CODE")

                }
                else -> {
                    mViewModel.pauseOff()
                }

            }


        } else if (requestCode == SAVE_ERR_DIALOG_REQUEST_CODE) {

            when (resultCode) {
                RESULT_RESUMED -> {
                    mViewModel.pauseOff()

                }
                Activity.RESULT_CANCELED -> {
                    mapObjects.clear()
                    Intent(requireActivity().applicationContext, LocationService::class.java)
                    mViewModel.stopTracking()
                    changeButton()
                }

            }
        }


    }

//    private fun stopTracking() {
//        locationService.trackTimer.stopTimer()
//        LocationService.isPaused = false
//        locationService.removeLocationUpdate()
//        locationService.clearParams()
//        LocationService.isStarted = false
//        locationService.trackTimer.offPauseTimer()
//    }


    private fun setCamera() {
        Log.d("isTouched", isTouched.toString())
        if (!isTouched) {
            if (locParams.latitude != 0.0) {
                val pos = Point(locParams.latitude, locParams.longitude)

                binding.trackingMapView.map.move(
                    CameraPosition(pos, camZoom, 0.0f, 0.0f),
                    Animation(Animation.Type.LINEAR, 0.3F),
                    null
                )
            }
        }


    }

    private fun changeButton() {
        if (LocationService.isStarted) {
            binding.stoppedLayout.visibility = View.GONE
            binding.startedLayout.visibility = View.VISIBLE


        } else {

            binding.stoppedLayout.visibility = View.VISIBLE
            binding.startedLayout.visibility = View.GONE
        }

        if (LocationService.isPaused) {
            binding.pauseBtn.visibility = View.GONE
            binding.resumeBtn.visibility = View.VISIBLE
        } else {
            binding.resumeBtn.visibility = View.GONE
            binding.pauseBtn.visibility = View.VISIBLE

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

    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateSource: CameraUpdateSource,
        b: Boolean
    ) {
        Log.d("cameraUpdateSource", camZoom.toString())
        if (cameraUpdateSource == CameraUpdateSource.GESTURES) {
            camZoom = cameraPosition.zoom
            isTouched = true

        }
    }


}



