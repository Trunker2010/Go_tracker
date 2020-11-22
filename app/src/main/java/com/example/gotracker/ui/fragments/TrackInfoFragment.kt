package com.example.gotracker.ui.fragments

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
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.example.gotracker.R
import com.example.gotracker.model.UserTrack
import com.example.gotracker.utils.*
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import kotlinx.android.synthetic.main.fragment_track_info.*
import java.util.*

class TrackInfoFragment : Fragment(R.layout.fragment_track_info) {
    private var userTrack: UserTrack = UserTrack()
    private var trackDistance: Double = 0.0
    private lateinit var trackID: String
    private lateinit var trackDuration: String
    private lateinit var trackStartTime: String
    private lateinit var trackStartDate: String
    private var trackPoints: MutableList<MutableList<Point>> = mutableListOf()
    private lateinit var trackObject: MapObjectCollection
    private lateinit var polyline: PolylineMapObject
    private val pointEventListener = AppValueEventListener { pointsGroup ->


        pointsGroup.children.forEach { points ->

            userTrack.trackPoints.add(createPoints(points))


            Log.d("pointsGroup", "id =${userTrack.trackID} points= ${userTrack.trackPoints.size}")


        }
        drawTrackPoints(userTrack.trackPoints)


        trackMapView.map.move(
            CameraPosition(userTrack.trackPoints[0][0], 14.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1F), Map.CameraCallback { }
        )


        var startPoint = trackObject.addCircle(
            Circle(userTrack.trackPoints[0][0], 2F),
            Color.RED, 2f, Color.GREEN
        )
        main_info.text = "${userTrack.startDate} ${userTrack.startTime}"
        distance_params.text = "${String.format(Locale.getDefault(), "%.2f", userTrack.distance)} км."


    }

    private fun drawTrackPoints(tracks: MutableList<MutableList<Point>>) {
        // Log.i("FragmentTracking", "drawTrackPoints")


        for (track in tracks) {

            trackObject.addPolyline(Polyline(track))

        }


    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        MapKitFactory.initialize(this.context)

        arguments?.let { args ->

            trackID = args.getString(TRACK_ID) ?: "null"
            trackDistance = args.getDouble(TRACK_DISTANCE)
            trackDuration = args.getString(TRACK_DURATION) ?: "00:00:00"
            trackStartTime = args.getString(TRACK_START_TIME).toString()
            trackStartDate = args.getString(TRACK_START_DATE) ?: ""

        }

        userTrack = UserTrack(
                trackID,
        trackDistance,
        trackDuration,
        trackPoints,
        trackStartTime,
        trackStartDate
        )





        return super.onCreateView(inflater, container, savedInstanceState)


    }

    private fun getTrackPoints(trackId: String) {
        val trackRef = REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID)
            .child(trackId).child(
                CHILD_TRACK_POINTS
            )
        trackRef.addValueEventListener(pointEventListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()
//        trackMapView.map.isScrollGesturesEnabled = false
        getTrackPoints(trackID)
        trackObject = trackMapView.map.mapObjects.addCollection()
        bottom_sheet.setOnTouchListener { _, event ->
            Log.d("MotionEvent", event!!.action.toString())
            trackMapView.map.isScrollGesturesEnabled = false

            if (event!!.action == MotionEvent.ACTION_CANCEL || event!!.action == MotionEvent.ACTION_UP) {

                trackMapView.map.isScrollGesturesEnabled = true
            }
            true
        }


    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        trackMapView.onStart()

    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
        trackMapView.onStop()
    }
}