package com.example.gotracker.model

import com.example.gotracker.utils.LocationConverter
import com.yandex.mapkit.geometry.Point
import java.util.*

class UserTrack() : Date() {
    var trackID = ""
    var start_time = ""
    var distance = 0.0
    var time = ""
    var trackPoints: MutableList<Point> = mutableListOf()
        }



