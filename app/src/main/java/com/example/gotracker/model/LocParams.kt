package com.example.gotracker.model

import com.yandex.mapkit.geometry.Point
import java.util.ArrayList

class LocParams {


    var maxSpeed: Float = 0.0F
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var speed: Double = 0.0
    var distance: Double = 0.0
    var trackPoints = ArrayList<ArrayList<Point>>()

    //    var time = ""
    var durationTimeMS = 0L

    constructor()
    constructor(
        maxSpeed: Float,
        latitude: Double,
        longitude: Double,
        speed: Double,
        distance: Double,
        trackPoints: ArrayList<ArrayList<Point>>,
        durationTimeMS: Long
    ) {
        this.maxSpeed = maxSpeed
        this.latitude = latitude
        this.longitude = longitude
        this.speed = speed
        this.distance = distance
        this.trackPoints = trackPoints
        this.durationTimeMS = durationTimeMS
    }


}