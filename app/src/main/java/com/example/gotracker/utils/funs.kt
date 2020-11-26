package com.example.gotracker.utils

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gotracker.R
import android.content.Intent
import com.example.gotracker.model.UserTrack
import com.google.firebase.database.DataSnapshot
import com.yandex.mapkit.geometry.Point
import java.text.SimpleDateFormat
import java.util.*

fun createTrack(trackID: DataSnapshot): UserTrack {
    val userTrack = UserTrack()
    userTrack.trackID = trackID.key.toString()

    val time = trackID.child(CHILD_START_TIME).getValue(Long::class.java)!!

    userTrack.startTimeInMs = time
    userTrack.distance =
        trackID.child(CHILD_DISTANCE).getValue(Double::class.java)!!
    userTrack.activeDuration =
        trackID.child(CHILD_TIME_DURATION).getValue(Long::class.java)!!


    userTrack.startDate =
        timeMsToDate(time)
    userTrack.startTime = timeMsToTime(time)
    userTrack.maxSpeed = if (trackID.hasChild(CHILD_MAX_SPEED)) trackID.child(CHILD_MAX_SPEED)
        .getValue(Float::class.java)!! else 0.0F

    return userTrack
}

fun AppCompatActivity.replaceFragment(fragment: Fragment) {
    if (fragment.isAdded) {
        return
    } else {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mine_fragment, fragment)

            .commit()
    }

}

fun createPoints(points: DataSnapshot): MutableList<Point> {
    val pointsList = mutableListOf<Point>()

    points.children.forEach { point ->
        pointsList.add(
            Point(
                point.child(CHILD_LATITUDE)
                    .getValue(Double::class.java) as Double,
                point.child(CHILD_LONGITUDE)
                    .getValue(Double::class.java) as Double
            )
        )
    }

    return pointsList
}

fun AppCompatActivity.replaceActivity(activity: AppCompatActivity) {
    val intent = Intent(this, activity::class.java)
    startActivity(intent)
    this.finish()
}

fun Fragment.replaceRegFragment(fragment: Fragment) {
    this.fragmentManager?.beginTransaction()
        ?.addToBackStack(null)
        ?.replace(
            R.id.register_data_container,
            fragment
        )?.commit()
}

fun Fragment.showToast(text: String) {
    Toast.makeText(
        this.context,
        text,
        Toast.LENGTH_SHORT
    ).show()

}

fun timeMsToDate(time: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = time
    return ("${calendar.get(Calendar.DAY_OF_MONTH)}.${calendar.get(Calendar.MONTH)}.${
        calendar.get(Calendar.YEAR)
    }")
}

fun timeMsToTime(time: Long): String {
    val calendar = Calendar.getInstance()


    calendar.timeInMillis = time

    val time = SimpleDateFormat("H:m:s").parse(
        "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}:${
            calendar.get(Calendar.SECOND)
        }"
    )
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(time)
}