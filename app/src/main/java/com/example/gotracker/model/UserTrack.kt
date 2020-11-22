package com.example.gotracker.model

import com.example.gotracker.utils.LocationConverter
import com.yandex.mapkit.geometry.Point
import java.util.*

class UserTrack() : Date() {

    var trackID = ""
    var distance = 0.0
    var activeDuration = ""
    var startTime = ""
    var trackPoints: MutableList<MutableList<Point>> = mutableListOf()

    constructor(
        trackID: String,
        distance: Double,
        activeDuration: String,
        trackPoints: MutableList<MutableList<Point>>,
        startTime: String,
        startDate: String
    ) : this() {
        this.trackID = trackID
        this.distance = distance
        this.activeDuration = activeDuration
        this.startTime = startTime
        this.trackPoints = trackPoints
        this.startDate = startDate
    }
}



