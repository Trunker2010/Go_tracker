package com.example.gotracker.model

import com.yandex.mapkit.geometry.Point
import java.util.*

const val CONTENT_TYPE = 1
const val DATE_TYPE = 0

class UserTrack(_type: Int) {


    var type = _type
    var startTime = ""
    var distance = 0.0

    //var time = 0
    var time = ""
    var trackPoints: MutableList<Point> = mutableListOf()

}
