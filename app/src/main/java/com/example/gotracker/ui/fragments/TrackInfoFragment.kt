package com.example.gotracker.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gotracker.R
import com.example.gotracker.model.UserTrack
import com.example.gotracker.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_track_info.*
import kotlinx.android.synthetic.main.fragment_track_list.*

class TrackInfoFragment : Fragment(R.layout.fragment_track_info) {

    lateinit var trackID: String
    lateinit var track: UserTrack

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        trackID = arguments?.getString(TRACK_ID).toString()
        getTrackInfo(trackID)
        return super.onCreateView(inflater, container, savedInstanceState)


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    private fun getTrackInfo(trackId: String) {
        val tracksRefs = REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID).child(trackId)
        tracksRefs.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(trackID: DataSnapshot) {
                track = createTrack(trackID)
                Log.d("trackInfo", track.trackID)
                val pointsRef = REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID).child(trackId).child(CHILD_TRACK_POINTS).addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(poins: DataSnapshot) {
                        createPoints(poins)

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
}