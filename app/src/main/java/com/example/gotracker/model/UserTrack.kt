package com.example.gotracker.model

import com.example.gotracker.utils.LocationConverter
import com.yandex.mapkit.geometry.Point
import java.util.*

class UserTrack() : Date() {

    var trackID = ""
    var startTime = ""
    var distance = 0.0
    var activeDuration = ""
    var trackPoints: MutableList<MutableList<Point>> = mutableListOf()

    constructor(
        trackID: String,
        distance: Double,
        activeDuration: String,
        trackPoints: MutableList<MutableList<Point>>
    ) : this() {
        this.trackID = trackID
        this.distance = distance
        this.activeDuration = activeDuration
        this.trackPoints = trackPoints
    }
}



