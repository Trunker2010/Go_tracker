package com.example.gotracker.model

import com.yandex.mapkit.geometry.Point
import java.util.ArrayList

class LocParams {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var speed: Double = 0.0
    var distance: Double = 0.0

    var tracks = ArrayList<ArrayList<Point>>()

}