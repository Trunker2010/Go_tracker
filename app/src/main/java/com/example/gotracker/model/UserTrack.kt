package com.example.gotracker.model

import com.yandex.mapkit.geometry.Point
import java.util.*

class UserTrack() : Date() {

    var distance = 0.0
    var time = ""
    var trackPoints: MutableList<Point> = mutableListOf()

}
