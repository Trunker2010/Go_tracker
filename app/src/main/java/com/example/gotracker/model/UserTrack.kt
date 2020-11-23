package com.example.gotracker.model

import android.os.Parcel
import android.os.Parcelable
import com.example.gotracker.utils.LocationConverter
import com.yandex.mapkit.geometry.Point
import java.util.*

class UserTrack() : Date() ,Parcelable {
    var maxSpeed = 0.0F
    var trackID = ""
    var distance = 0.0
    var activeDuration = ""
    var startTime = ""
    var trackPoints: MutableList<MutableList<Point>> = mutableListOf()

    constructor(parcel: Parcel) : this() {
        startDate = parcel.readString().toString()
        maxSpeed = parcel.readFloat()
        trackID = parcel.readString().toString()
        distance = parcel.readDouble()
        activeDuration = parcel.readString().toString()
        startTime = parcel.readString().toString()
    }

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(startDate)
        parcel.writeFloat(maxSpeed)
        parcel.writeString(trackID)
        parcel.writeDouble(distance)
        parcel.writeString(activeDuration)
        parcel.writeString(startTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserTrack> {
        override fun createFromParcel(parcel: Parcel): UserTrack {
            return UserTrack(parcel)
        }

        override fun newArray(size: Int): Array<UserTrack?> {
            return arrayOfNulls(size)
        }
    }
}



