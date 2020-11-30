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
import androidx.fragment.app.Fragment
import com.example.gotracker.R
import com.example.gotracker.model.UserTrack
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
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_track_info.*
import java.util.*

class TrackInfoFragment : Fragment(R.layout.fragment_track_info) {
    private var userTrack: UserTrack = UserTrack()
    private lateinit var trackObject: MapObjectCollection
    private lateinit var trackRef: DatabaseReference
    private val pointEventListener = AppValueEventListener { pointsGroup ->


        pointsGroup.children.forEach { points ->

            userTrack.trackPoints.add(createPoints(points))


            Log.d("pointsGroup", "id =${userTrack.trackID} points= ${userTrack.trackPoints.size}")


        }
        drawTrackPoints(userTrack.trackPoints)

        val boundingBox = findBoundingBoxPoints(userTrack.trackPoints)
        var cameraPosition = trackMapView.map.cameraPosition(boundingBox)
        cameraPosition = CameraPosition(
            cameraPosition.target,
            cameraPosition.zoom - 0.8f,
            cameraPosition.azimuth,
            cameraPosition.tilt
        )
        trackMapView.map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 0f), null)

//        trackMapView.map.move(
//            CameraPosition(userTrack.trackPoints[0][0], 14.0f, 0.0f, 0.0f),
//            Animation(Animation.Type.SMOOTH, 1F), Map.CameraCallback { }
//        )
//        trackMapView.map.cameraPosition(BoundingBox())


        var startPoint = trackObject.addCircle(
            Circle(userTrack.trackPoints[0][0], 2F),
            Color.RED, 2f, Color.GREEN
        )


        main_info.text = "${userTrack.startDate} ${userTrack.startTime}"
        distance_params.text =
            "${String.format(Locale.getDefault(), "%.2f", userTrack.distance)} км"
        max_speed_params.text = userTrack.maxSpeed.toString()
        duration_params.text = LocationConverter.convertMStoTime(userTrack.activeDuration)
        av_speed_params.text = "${
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

        MapKitFactory.initialize(this.context)
        userTrack = arguments?.getParcelable(TRACK_PARCELABLE)!!

        return super.onCreateView(inflater, container, savedInstanceState)


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
        trackRef.removeEventListener(pointEventListener)

    }

    private fun findBoundingBoxPoints(pointLists: MutableList<MutableList<Point>>): BoundingBox {

        var i = 0
        Log.d("findBoundingBoxPoints", "start $i")
        var maxLatitude: Double = pointLists[0][0].latitude
        var maxLongitude: Double = pointLists[0][0].longitude
        var minLatitude: Double = pointLists[0][0].latitude
        var minLongitude: Double = pointLists[0][0].longitude
        for (pointList in pointLists) {
            for (point in pointList) {

                if (point.latitude > maxLatitude) {
                    maxLatitude = point.latitude

                } else if (point.latitude < minLatitude) {
                    minLatitude = point.latitude
                }

                if (point.longitude > maxLongitude) {
                    maxLongitude = point.longitude

                } else if (point.longitude < minLongitude) {
                    minLongitude = point.longitude
                }


                i++
            }
        }
        val southWest = Point(minLatitude, minLongitude)
        val northEast = Point(maxLatitude, maxLongitude)

        Log.d("findBoundingBoxPoints", "end $i")
        return BoundingBox(southWest, northEast)

    }
}