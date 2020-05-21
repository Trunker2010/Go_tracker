package com.example.gotracker.ui.fragments


import android.content.Intent
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.gotracker.LocationService
import com.example.gotracker.R
import com.example.gotracker.STOP_LOC_SERVICE
import com.example.gotracker.databinding.FragmentTrackingBinding
import com.example.gotracker.ui.MainActivity
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider


const val START_TRACKING = "start_tracking"
const val TAG_FRAGMENT = "fragment"


class TrackingFragment : Fragment(), UserLocationObjectListener, View.OnClickListener {

    val MAPKIT_API_KEY = "6a3e8505-4082-499a-ba52-2a5c023e57ed"
    lateinit var userLocationLayer: UserLocationLayer
    lateinit var mapKit: MapKit
    lateinit var binding: FragmentTrackingBinding
    var isStarted: Boolean = false

    var h = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        MapKitFactory.initialize(activity)
        isStarted = LocationService.isStarted

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackingBinding.inflate(layoutInflater)
        //mineActivity = activity as MainActivity
        binding.mapview.map.isRotateGesturesEnabled = true
        mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(binding.mapview.mapWindow)
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.isVisible = true
        userLocationLayer.setObjectListener(this)
        userLocationLayer.isAutoZoomEnabled = true
        binding.startBtn.setOnClickListener(this)
        binding.stopBtn.setOnClickListener(this)
        setButton()

        return binding.root
    }


    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapview.onStart()


    }

    override fun onResume() {
        setCamera()
        super.onResume()
    }

    override fun onStop() {
        binding.mapview.onStart()
        MapKitFactory.getInstance().onStop()
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
            (activity as MainActivity).setCamera()
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
                IconStyle().setAnchor(PointF(0.0f, 0.5f))
                    .setRotationType(RotationType.ROTATE)
                    .setZIndex(1f)
                    .setScale(0.5f)
            )
        }


    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(v: View?) {


        when (v?.id) {

            R.id.start_btn -> {
                val intent = Intent(activity, LocationService::class.java)
                setCamera()
                (activity as MainActivity).doBindService()
                intent.action = START_TRACKING
                activity?.startService(intent)
                changeButton()

            }
            R.id.stop_btn -> {
                val intent = Intent(activity, LocationService::class.java)
                intent.action = STOP_LOC_SERVICE
                activity?.stopService(intent)
                (activity as MainActivity).doUnbindeSerivice()
                changeButton()
            }


        }
    }

    fun setCamera() {

        val cameraRunnable = object : Runnable {
            override fun run() {
                mapKit.createLocationManager().requestSingleUpdate(object : LocationListener {
                    override fun onLocationStatusUpdated(p0: LocationStatus) {

                    }

                    override fun onLocationUpdated(location: Location) {


                        binding.mapview.getMap().move(

                            CameraPosition(location.position, 25.0f, 0.0f, 0.0f),
                            Animation(Animation.Type.LINEAR, 0.5F),
                            null
                        )


                    }


                })

            }

        }
            h.post(cameraRunnable)


    }

    private fun changeButton() {
        if (isStarted) {
            binding.startBtn.visibility = View.VISIBLE
            binding.stopBtn.visibility = View.GONE
            isStarted = !isStarted
        } else {
            isStarted = true
            binding.startBtn.visibility = View.GONE
            binding.stopBtn.visibility = View.VISIBLE
        }
    }

    private fun setButton() {
        if (isStarted) {
            binding.startBtn.visibility = View.GONE
            binding.stopBtn.visibility = View.VISIBLE

        }
    }

}



