package com.example.gotracker.utils

import java.util.*

class LocationConverter {
    companion object {
        fun convertSpeed(meterPerSecond: Float): Float {
            return meterPerSecond * 60 * 60 / 1000

        }

        fun convertDistance(meters: Double): Double {
            return meters / 1000
        }

        fun convertMStoTime(timeInMS: Long): String {


            var secs = (timeInMS / 1000).toInt()
            var minutes = secs / 60
            secs %= 60
            return "$minutes:${String.format("%02d", secs)}"/*:${String.format("%02d", milliseconds)}*/
        }
    }

}
