package com.example.gotracker.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gotracker.model.User
import com.example.gotracker.model.UserTrack
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.yandex.mapkit.geometry.Point


lateinit var AUTH: FirebaseAuth
lateinit var REF_DATABASE_ROOT: DatabaseReference
lateinit var USER: User

lateinit var UID: String
lateinit var userTracks: MutableList<UserTrack>

const val NODE_TRACKS = "tracks"
const val CHILD_LONGITUDE = "longitude"
const val CHILD_LATITUDE = "latitude"

const val NODE_USERS = "user"
const val CHILD_ID = "id"
const val CHILD_PHONE = "phone"
const val CHILD_USERNAME = "username"
const val CHILD_DISTANCE = "distance"
const val CHILD_TIME = "time"
const val CHILD_TRACK_POINTS = "tracks_points"
const val CHILD_START_TIME= "start_time"


fun initFirebase() {
    AUTH = FirebaseAuth.getInstance()
    REF_DATABASE_ROOT = FirebaseDatabase.getInstance().reference
    USER = User()
    UID = AUTH.currentUser?.uid.toString()
}

@RequiresApi(Build.VERSION_CODES.N)
fun initUserTracks() {



    userTracks = mutableListOf()
    REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID)
        .addListenerForSingleValueEvent(AppValueEventListener {
            /*получаеи список тереков*/
            it.children.forEach { trackID ->

                var userTrack = UserTrack()
                userTrack.distance = trackID.child(CHILD_DISTANCE).getValue(Double::class.java)!!

                println(trackID.key)
                REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID).child(trackID.key.toString()).child(
                    CHILD_TRACK_POINTS
                ).addListenerForSingleValueEvent(AppValueEventListener { tracks ->

                    /*получаем список точек*/
                    tracks.children.forEach { points ->

                        var pointsList = mutableListOf<Point>()

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
                        userTrack.trackPoints = pointsList

                    }
                    userTracks.add(userTrack)

                })


            }


        })


}