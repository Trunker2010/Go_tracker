package com.example.gotracker.model

import com.yandex.mapkit.geometry.Point
import java.util.*

class UserTrack() {


    var startTime = 0L
    var distance = 0.0
    var time = 0
    var trackPoints: MutableList<Point> = mutableListOf()
}
