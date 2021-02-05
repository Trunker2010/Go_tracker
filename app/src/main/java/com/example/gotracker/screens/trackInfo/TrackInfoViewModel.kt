package com.example.gotracker.screens.trackInfo

import android.app.Application
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.gotracker.utils.AppValueEventListener
import com.example.gotracker.utils.LocationConverter
import com.example.gotracker.utils.createPoints
import com.google.firebase.database.DatabaseReference
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import java.util.*

class TrackInfoViewModel(app: Application) : AndroidViewModel(app) {
    private lateinit var trackRef: DatabaseReference
    override fun onCleared() {
        super.onCleared()
    }

    fun findBoundingBoxPoints(pointLists: MutableList<MutableList<Point>>): BoundingBox {

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