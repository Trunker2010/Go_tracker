package com.example.gotracker.utils

class LocationConverter {
    companion object {
        fun convertSpeed(meterPerSecond: Float): Float {
            return meterPerSecond * 60 * 60 / 1000

        }

        fun convertDistance(meters: Double): Double {
            return meters / 1000
        }
    }

}