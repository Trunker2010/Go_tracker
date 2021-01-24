package com.example.gotracker.screens.trackList

import android.app.Application
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import com.example.gotracker.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_track_list.*

class TrackListViewModel(app: Application) : AndroidViewModel(app) {


//    private val trackEventListener = object : ValueEventListener {
//
//        override fun onDataChange(rootSnapshot: DataSnapshot) {
//            var countTracks = rootSnapshot.children.count()
//            rootSnapshot.children.forEach { trackID ->
//                val userTrack = createTrack(trackID)
//                if (userTrack.distance > 0) {
//                    userTracks.add(userTrack)
//                } else
//                    countTracks--
//
//
//
//                if (countTracks == userTracks.size) {
//                    sortTracks()
//                    difTracks()
////                    getIndexesForInputDate()
//                    if (loadTracksProgressBar != null) {
//                        loadTracksProgressBar.visibility = View.GONE
//                        updateUI()
//                    }
//
//                }
//
//
//            }
//        }
//
//
//        override fun onCancelled(error: DatabaseError) {
//            TODO("Not yet implemented")
//        }
//
//
//    }
//
//    @RequiresApi(Build.VERSION_CODES.N)
//    private fun initUserTracks() {
//        loadTracksProgressBar.visibility = View.VISIBLE
//        userTracks = mutableListOf()
//        val tracksRefs = REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID)
//
//        tracksRefs
//            .addListenerForSingleValueEvent(trackEventListener)
//    }
}