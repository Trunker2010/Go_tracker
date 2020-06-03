package com.example.gotracker.utils

import android.graphics.Point
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gotracker.model.LocParams
import com.example.gotracker.model.User
import com.example.gotracker.model.UserTracks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


lateinit var AUTH: FirebaseAuth
lateinit var REF_DATABASE_ROOT: DatabaseReference
lateinit var USER: User
lateinit var UID: String
lateinit var USER_TRACKS: UserTracks

const val NODE_TRACKS = "tracks"
const val CHILD_LONGITUDE = "longitude"
const val CHILD_LATITUDE = "latitude"

const val NODE_USERS = "user"
const val CHILD_ID = "id"
const val CHILD_PHONE = "phone"
const val CHILD_USERNAME = "username"
const val CHILD_DISTANCE = "distance"
const val CHILD_TRACK_POINTS = "tracks_points"


fun initFirebase() {
    AUTH = FirebaseAuth.getInstance()
    REF_DATABASE_ROOT = FirebaseDatabase.getInstance().reference
    USER = User()
    UID = AUTH.currentUser?.uid.toString()
}

@RequiresApi(Build.VERSION_CODES.N)
fun initUserTracks() {
    USER_TRACKS = UserTracks()
    REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID)
        .addListenerForSingleValueEvent(AppValueEventListener {


            it.children.forEach { trackID ->
                println(trackID.key)

                REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID).child(trackID.key.toString()).child(
                    CHILD_TRACK_POINTS
                ).addListenerForSingleValueEvent(AppValueEventListener { tracks ->

                    tracks.children.forEach { points ->

                        points.children.forEach { point ->


//                                USER_TRACKS.tracks[][points.key.toString().toInt()].latitude =
//                                    (REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID)
//                                        .child(trackID.toString())
//                                        .child(
//                                            CHILD_TRACK_POINTS
//                                        ).child(point.toString()).child(CHILD_LATITUDE)) as Double

                        }

                    }

                })
            }


        })


}