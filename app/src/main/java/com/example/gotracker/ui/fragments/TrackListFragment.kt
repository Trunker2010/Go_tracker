package com.example.gotracker.ui.fragments

import android.os.Build
import android.os.Bundle
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
import com.example.gotracker.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.yandex.mapkit.geometry.Point
import kotlinx.android.synthetic.main.fragment_track_list.*
import kotlinx.android.synthetic.main.track_item.view.*
import kotlinx.android.synthetic.main.date_item.view.*
import java.util.*


class TrackListFragment : Fragment(R.layout.fragment_track_list) {
    var listeners: MutableMap<DatabaseReference, ValueEventListener> = mutableMapOf()
    private lateinit var pointEventListener: AppValueEventListener
    var isCanceled = false
    val trackEventListener = object : ValueEventListener {

        override fun onDataChange(rootSnapshot: DataSnapshot) {
            rootSnapshot.children.forEach { trackID ->
                val userTrack = createTrack(trackID)

                pointEventListener = AppValueEventListener { tracks ->

                    tracks.children.forEach { points ->
                        userTrack.trackPoints = createPoints(points)

                    }
                    userTracks.add(userTrack)


                    if (rootSnapshot.children.count() == userTracks.size) {
                        sortTracks()
                        difTrackDate()
                        if (loadTracksProgressBar !=null){
                            loadTracksProgressBar.visibility = View.GONE
                            rv_tracks.adapter = DataAdapter()
                        }

                    }
                }
                val trackIdRef = REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID)
                    .child(trackID.key.toString()).child(
                        CHILD_TRACK_POINTS
                    )
                listeners[trackIdRef] = pointEventListener

                trackIdRef.addListenerForSingleValueEvent(
                    pointEventListener
                )


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

        listeners[tracksRefs] = trackEventListener
        tracksRefs
            .addListenerForSingleValueEvent(trackEventListener)
    }

    private fun createPoints(points: DataSnapshot): MutableList<Point> {
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

    private fun createTrack(trackID: DataSnapshot): UserTrack {
        val userTrack = UserTrack()
        userTrack.trackID = trackID.key.toString()
        val time = trackID.child(CHILD_START_TIME).getValue(Long::class.java)!!
        userTrack.distance =
            trackID.child(CHILD_DISTANCE).getValue(Double::class.java)!!
        userTrack.time = LocationConverter.convertMStoTime(
            trackID.child(CHILD_TIME).getValue(Long::class.java)!!
        )
        userTrack.startDate =
            timeMsToDate(time)
        userTrack.start_time = timeMsToTime(time)
        return userTrack
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

    class DataAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        override fun getItemViewType(position: Int): Int {
            return when (userTracks[position]) {
                is UserTrack -> R.layout.track_item
                is Date -> R.layout.date_item
                else -> -1

            }
        }

        class TrackHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {

            init {
                itemView.setOnClickListener(this)
            }

            lateinit var mUserTrack: UserTrack
            val distance: TextView = itemView.card_distance_params
            val duration: TextView = itemView.card_duration_params
            val startTime: TextView = itemView.card_star_time


            override fun onClick(v: View?) {
                if (v == itemView) {

                }


            }


            fun bindTrack(userTrack: UserTrack) {
                mUserTrack = userTrack

            }


        }

        class DateHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
                        holder.duration.text = userTrack.time
                        holder.startTime.text = userTrack.start_time

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

    override fun onPause() {
        super.onPause()
        removeListeners()
        isCanceled = true

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun removeListeners() {
        listeners.forEach { (ref, listener) -> ref.removeEventListener(listener) }

    }
}