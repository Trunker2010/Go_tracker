package com.example.gotracker.utils

import android.util.Log
import com.example.gotracker.model.Date
import com.example.gotracker.model.User
import com.example.gotracker.model.UserTrack
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.ExecutorService


lateinit var AUTH: FirebaseAuth
lateinit var REF_DATABASE_ROOT: DatabaseReference
lateinit var USER: User

lateinit var UID: String
lateinit var userTracks: MutableList<Any>

const val NODE_TRACKS = "tracks"
const val CHILD_LONGITUDE = "longitude"
const val CHILD_LATITUDE = "latitude"

const val NODE_USERS = "user"
const val CHILD_ID = "id"
const val CHILD_PHONE = "phone"
const val CHILD_USERNAME = "username"
const val CHILD_DISTANCE = "distance"
const val CHILD_TIME_DURATION = "time"
const val CHILD_TRACK_POINTS = "tracks_points"
const val CHILD_START_TIME = "start_time"
const val CHILD_MAX_SPEED = "max_speed"
lateinit var executor: ExecutorService

fun initFirebase() {
    AUTH = FirebaseAuth.getInstance()
    REF_DATABASE_ROOT = FirebaseDatabase.getInstance().reference
    USER = User()
    UID = AUTH.currentUser?.uid.toString()
}



fun sortTracks() = (userTracks as MutableList<*>).sortByDescending {
    (it as? UserTrack)?.startTimeInMs
}




