package com.example.gotracker.dataBase

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.example.gotracker.model.Date
import com.example.gotracker.model.LocParams
import com.example.gotracker.model.User
import com.example.gotracker.model.UserTrack
import com.example.gotracker.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AppFirebaseRepository : DataBaseRepository {

    init {
        AUTH = FirebaseAuth.getInstance()
        REF_DATABASE_ROOT = FirebaseDatabase.getInstance().reference
        USER = User()
        UID = AUTH.currentUser?.uid.toString()
        REF_USER_TRACKS = REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID)
        initUser()
    }


    override fun insertTrack(locParams: LocParams, startTime: Long) {
        val dateMap = mutableMapOf<String, Any>()
        dateMap[CHILD_DISTANCE] = locParams.distance
        dateMap[CHILD_TIME_DURATION] = locParams.durationTimeMS
        dateMap[CHILD_START_TIME] = startTime
        dateMap[CHILD_MAX_SPEED] = locParams.maxSpeed

        REF_USER_TRACKS
            .child(dateMap.hashCode().toString())
            .updateChildren(dateMap)


        for ((trackNumber, track) in locParams.trackPoints.withIndex()) {

            var pos = 0;
            track.forEach {

                REF_USER_TRACKS
                    .child(dateMap.hashCode().toString())
                    .child(CHILD_TRACK_POINTS).child(trackNumber.toString())
                    .child(pos.toString()).child(CHILD_LATITUDE).setValue(it.latitude)

                REF_USER_TRACKS
                    .child(dateMap.hashCode().toString())
                    .child(CHILD_TRACK_POINTS).child(trackNumber.toString())
                    .child(pos.toString()).child(CHILD_LONGITUDE).setValue(it.longitude)
                pos++
            }
        }
    }

    private fun initUser() {
        REF_DATABASE_ROOT.child(NODE_USERS).child(UID)
            .addListenerForSingleValueEvent(AppValueEventListener {
                USER = it.getValue(User::class.java) ?: User()
            })
    }

    override fun deleteTrack(id: String) {

        val ref =    REF_USER_TRACKS.child(id).ref
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.ref.removeValue()

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


    }

    override fun getAllTracks(onSucess: () -> Unit) {
        userTracks = mutableListOf()

        val trackEventListener = object : ValueEventListener {

            override fun onDataChange(rootSnapshot: DataSnapshot) {

                rootSnapshot.children.forEach { trackID ->
                    val userTrack = createTrack(trackID)

                    userTracks.add(userTrack)
                }
                onSucess()

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        REF_USER_TRACKS
            .addListenerForSingleValueEvent(trackEventListener)


    }


}