package com.example.gotracker.ui.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.gotracker.R
import com.example.gotracker.model.Date
import com.example.gotracker.model.UserTrack
import com.example.gotracker.ui.activities.TrackInfoActivity
import com.example.gotracker.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_track_list.*
import kotlinx.android.synthetic.main.track_item.view.*
import kotlinx.android.synthetic.main.date_item.view.*
import java.util.*

const val TRACK_ID = "track_id"
const val TRACK_DISTANCE = "track_distance"
const val TRACK_DURATION = "track_duration"
const val TRACK_START_TIME = "track_start_time"
const val TRACK_START_DATE="track_start_date"


class TrackListFragment : Fragment(R.layout.fragment_track_list) {

    var isCanceled = false

    private val trackEventListener = object : ValueEventListener {

        override fun onDataChange(rootSnapshot: DataSnapshot) {
            rootSnapshot.children.forEach { trackID ->
                val userTrack = createTrack(trackID)

                userTracks.add(userTrack)


                if (rootSnapshot.children.count() == userTracks.size) {
                    sortTracks()
                    difTrackDate()
                    if (loadTracksProgressBar != null) {
                        loadTracksProgressBar.visibility = View.GONE
                        rv_tracks.adapter = DataAdapter()
                    }

                }


            }
        }

        override fun onCancelled(error: DatabaseError) {
            isCanceled = true
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun initUserTracks() {
        loadTracksProgressBar.visibility = View.VISIBLE
        userTracks = mutableListOf()
        val tracksRefs = REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID)

        tracksRefs
            .addListenerForSingleValueEvent(trackEventListener)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return super.onCreateView(inflater, container, savedInstanceState)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        isCanceled = false
        initUserTracks()

    }

    inner class DataAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        override fun getItemViewType(position: Int): Int {
            return when (userTracks[position]) {
                is UserTrack -> R.layout.track_item
                is Date -> R.layout.date_item
                else -> -1

            }
        }

        inner class TrackHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {

            init {
                itemView.setOnClickListener(this)
            }

            lateinit var mUserTrack: UserTrack
            val distance: TextView = itemView.card_distance_params
            val duration: TextView = itemView.card_duration_params
            val startTime: TextView = itemView.card_star_time


            override fun onClick(v: View?) {

                val intent = Intent(this@TrackListFragment.context, TrackInfoActivity::class.java)
                intent.putExtra(TRACK_ID, mUserTrack.trackID)
                intent.putExtra(TRACK_DISTANCE, mUserTrack.distance)
                intent.putExtra(TRACK_DURATION, mUserTrack.activeDuration)
                intent.putExtra(TRACK_START_TIME, mUserTrack.startTime)
                intent.putExtra(TRACK_START_DATE, mUserTrack.startDate)


                Log.d("mUserTrack.trackID", mUserTrack.startTime)
                this@TrackListFragment.startActivity(intent)
            }

            fun bindTrack(userTrack: UserTrack) {
                mUserTrack = userTrack

            }


        }

        inner class DateHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val date: TextView = itemView.date_item
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view: View
            return if (viewType == R.layout.date_item) {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.date_item, parent, false)
                DateHolder(view)
            } else {
                view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.track_item, parent, false)


                TrackHolder(view)
            }
        }


        override fun getItemCount(): Int {
            return userTracks.size
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is DateHolder -> {
                    (userTracks[position] as Date).let { date ->
                        holder.date.text = date.startDate
                    }
                }

                is TrackHolder -> {
                    (userTracks[position] as UserTrack).let { userTrack ->
                        holder.bindTrack(userTrack)
                        holder.distance.text =
                            "${String.format(Locale.getDefault(), "%.2f", userTrack.distance)} км"
                        holder.duration.text = userTrack.activeDuration
                        holder.startTime.text = userTrack.startTime

                    }
                }
            }

        }
    }


    companion object {
        fun newInstance(): TrackListFragment {
            return TrackListFragment()
        }
    }


}