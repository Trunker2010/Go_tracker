package com.example.gotracker.screens.tracking


import android.app.Activity
import android.content.Intent
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
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


const val START_TRACKING = "start_tracking"
const val SAVE_DIALOG_TAG = "saveDialog"
const val SAVE_DIALOG_ERR_TAG = "saveDialogErr"
const val SAVE_DIALOG_REQUEST_CODE = 1
const val SAVE_ERR_DIALOG_REQUEST_CODE = 2
const val DISTANCE_KEY = "speed"
const val MAX_SPEED_KEY = "max_sped "
const val TIME_KEY = "time"

class TrackingFragment : Fragment(), UserLocationObjectListener,
    View.OnClickListener, CameraListener {
    private var isTouched = false
    private var camZoom = 15.0F
    private val FRAGMENT_TAG = "FragmentTracker"
    lateinit var userLocationLayer: UserLocationLayer
    lateinit var mapKit: MapKit
    private var _binding: FragmentTrackingBinding? = null
    private val mBinding get() = _binding!!


    lateinit var mViewModel: TrackingViewModel
    private lateinit var mapObjects: MapObjectCollection
    private lateinit var polyline: PolylineMapObject
    var locParams = LocParams()
    private var saveTrackDialogFragment = SaveTrackDialogFragment()
    private var saveErrDialogFragment = SaveErrDialogFragment()
    lateinit var updateTimeObserver: Observer<String>
    lateinit var updateLocParamsObserver: Observer<LocParams>


    private fun updateTime(it: String) {
        mBinding.currentTime.text = it
    }

    private fun updateLocParams(locParams: LocParams) {

        this.locParams = locParams
        setCamera()


        mBinding.speedM.text = (String.format(
            Locale.getDefault(),
            "%1$,.2f", locParams.speed

        ))
        mBinding.distanceM.text = (String.format(
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
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        val view = mBinding.root

        mBinding.trackingMapView.map.isRotateGesturesEnabled = true





        mapKit = MapKitFactory.getInstance()
        mBinding.findMe.setOnClickListener(this)
        mBinding.trackingMapView.map.addCameraListener(this)
        userLocationLayer = mapKit.createUserLocationLayer(mBinding.trackingMapView.mapWindow)
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.isVisible = true
        userLocationLayer.setObjectListener(this)
        userLocationLayer.isAutoZoomEnabled = true
        mBinding.startBtn.setOnClickListener(this)
        mBinding.stopBtn.setOnClickListener(this)
        mBinding.pauseBtn.setOnClickListener(this)
        mBinding.settingsBtn.setOnClickListener(this)
        mBinding.resumeBtn.setOnClickListener(this)
        mapObjects = mBinding.trackingMapView.map.mapObjects.addCollection()


        setButton()
        changeButton()
        return view
    }


    override fun onStart() {
        super.onStart()

        mViewModel = ViewModelProvider(this).get(TrackingViewModel::class.java)
        MapKitFactory.getInstance().onStart()
        mBinding.trackingMapView.onStart()
        updateLocParamsObserver = Observer { updateLocParams(it) }
        updateTimeObserver = Observer { updateTime(it) }
        mViewModel.doBindService()



        if (LocationService.isStarted) {
            mViewModel.setLocationParamsObserver(this, updateLocParamsObserver)
            mViewModel.setTimeObserver(
                this, updateTimeObserver)

        }


    }


    override fun onStop() {
        mBinding.trackingMapView.onStop()
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


                mapObjects = mBinding.trackingMapView.map.mapObjects.addCollection()
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

                mBinding.trackingMapView.map.move(
                    CameraPosition(pos, camZoom, 0.0f, 0.0f),
                    Animation(Animation.Type.LINEAR, 0.3F),
                    null
                )
            }
        }


    }

    private fun changeButton() {
        if (LocationService.isStarted) {
            mBinding.stoppedLayout.visibility = View.GONE
            mBinding.startedLayout.visibility = View.VISIBLE


        } else {

            mBinding.stoppedLayout.visibility = View.VISIBLE
            mBinding.startedLayout.visibility = View.GONE
        }

        if (LocationService.isPaused) {
            mBinding.pauseBtn.visibility = View.GONE
            mBinding.resumeBtn.visibility = View.VISIBLE
        } else {
            mBinding.resumeBtn.visibility = View.GONE
            mBinding.pauseBtn.visibility = View.VISIBLE

        }
    }

    private fun setButton() {
        if (LocationService.isStarted) {
            mBinding.stoppedLayout.visibility = View.GONE
            mBinding.startedLayout.visibility = View.VISIBLE
        } else {
            mBinding.stoppedLayout.visibility = View.VISIBLE
            mBinding.startedLayout.visibility = View.GONE
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

    //    override fun onDetach() {
//        super.onDetach()
//
//        if (!LocationService.isStarted) {
//           mViewModel.doUnbindService()
//        }
//    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



