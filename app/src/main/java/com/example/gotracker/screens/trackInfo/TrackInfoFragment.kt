package com.example.gotracker.screens.trackInfo

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gotracker.R
import com.example.gotracker.databinding.BottomSheetBinding
import com.example.gotracker.databinding.FragmentTrackInfoBinding
import com.example.gotracker.model.UserTrack
import com.example.gotracker.screens.trackList.TRACK_PARCELABLE

import com.example.gotracker.utils.*
import com.google.firebase.database.DatabaseReference
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import java.util.*

class TrackInfoFragment : Fragment() {
    private var userTrack: UserTrack = UserTrack()
    private lateinit var trackObject: MapObjectCollection
    private lateinit var trackRef: DatabaseReference
    private lateinit var mViewModel: TrackInfoViewModel
    private var _binding: FragmentTrackInfoBinding? = null
    private val mBinding get() = _binding!!
    private var _bindingBottomSheet: BottomSheetBinding? = null
    private val mBindingBottomSheetBinding get() = _bindingBottomSheet!!
    private val pointEventListener = AppValueEventListener { pointsGroup ->


        pointsGroup.children.forEach { points ->

            userTrack.trackPoints.add(createPoints(points))



        }
        drawTrackPoints(userTrack.trackPoints)

        val boundingBox = mViewModel.findBoundingBoxPoints(userTrack.trackPoints)
        var cameraPosition = mBinding.trackMapView.map.cameraPosition(boundingBox)
        cameraPosition = CameraPosition(
            cameraPosition.target,
            cameraPosition.zoom - 0.8f,
            cameraPosition.azimuth,
            cameraPosition.tilt
        )
        mBinding.trackMapView.map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 0f), null)




        var startPoint = trackObject.addCircle(
            Circle(userTrack.trackPoints[0][0], 2F),
            Color.RED, 2f, Color.GREEN
        )


        mBindingBottomSheetBinding.mainInfo.text = "${userTrack.startDate} ${userTrack.startTime}"
        mBindingBottomSheetBinding.distanceParams.text =
            "${String.format(Locale.getDefault(), "%.2f", userTrack.distance)} км"
        mBindingBottomSheetBinding.maxSpeedParams.text = userTrack.maxSpeed.toString()
        mBindingBottomSheetBinding.durationParams.text =
            LocationConverter.convertMStoTime(userTrack.activeDuration)
        mBindingBottomSheetBinding.avSpeedParams.text = "${
            String.format(
                Locale.getDefault(),
                "%.2f",
                (userTrack.distance / (userTrack.activeDuration.toDouble() / 1000 / 60 / 60))
            )
        } км/ч"


    }


    private fun drawTrackPoints(tracks: MutableList<MutableList<Point>>) {
        // Log.i("FragmentTracking", "drawTrackPoints")


        for (track in tracks) {

            var a = trackObject.addPolyline(Polyline(track))


        }


    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackInfoBinding.inflate(inflater, container, false)
        _bindingBottomSheet = mBinding.bottomSheet
        val view = mBinding.root

        mViewModel = ViewModelProvider(this).get(TrackInfoViewModel::class.java)
        MapKitFactory.initialize(this.context)
        userTrack = arguments?.getParcelable(TRACK_PARCELABLE)!!

        return view


    }

    private fun getTrackPoints(trackId: String) {
        trackRef = REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID)
            .child(trackId).child(
                CHILD_TRACK_POINTS
            )
        trackRef.addValueEventListener(pointEventListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()
        getTrackPoints(userTrack.trackID)
        trackObject = mBinding.trackMapView.map.mapObjects.addCollection()
        mBindingBottomSheetBinding.root.setOnTouchListener { _, event ->

            mBinding.trackMapView.map.isScrollGesturesEnabled = false

            if (event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP) {

                mBinding.trackMapView.map.isScrollGesturesEnabled = true
            }
            true
        }


    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mBinding.trackMapView.onStart()

    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
        mBinding.trackMapView.onStop()
        trackRef.removeEventListener(pointEventListener)

    }


}