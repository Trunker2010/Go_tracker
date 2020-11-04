package com.example.gotracker.ui.fragments

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
import com.example.gotracker.utils.*
import com.yandex.mapkit.geometry.Point
import kotlinx.android.synthetic.main.fragment_track_list.*
import kotlinx.android.synthetic.main.track_item.view.*
import kotlinx.android.synthetic.main.date_item.view.*
import java.util.*


class TrackListFragment : Fragment(R.layout.fragment_track_list) {


    @RequiresApi(Build.VERSION_CODES.N)
    private fun initUserTracks() {
        loadTracksProgressBar.visibility = View.VISIBLE
        userTracks = mutableListOf()

        REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID)
            .addListenerForSingleValueEvent(AppValueEventListener { rootSnapshot ->

                rootSnapshot.children.forEach { trackID ->
                    val userTrack = UserTrack()

                    userTrack.distance =
                        trackID.child(CHILD_DISTANCE).getValue(Double::class.java)!!

                    userTrack.time = LocationConverter.convertMStoTime(
                        trackID.child(CHILD_TIME).getValue(Long::class.java)!!
                    )
                    userTrack.startDate =
                        timeToDate(
                            trackID.child(CHILD_START_TIME).getValue(Long::class.java)!!
                        )

                    REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID)
                        .child(trackID.key.toString()).child(
                            CHILD_TRACK_POINTS
                        ).addListenerForSingleValueEvent(AppValueEventListener { tracks ->

                            tracks.children.forEach { points ->

                                Log.d("dbThread", Thread.currentThread().name)
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

                                userTrack.trackPoints = pointsList
                            }

                            userTracks.add(userTrack)
                            Log.d(
                                "countAndSize",
                                "${rootSnapshot.children.count()} ${userTracks.size}"
                            )
                            if (rootSnapshot.children.count() == userTracks.size) {
                                sortTracks()
                                difTrackDate()
                                loadTracksProgressBar.visibility = View.GONE
                                rv_tracks.adapter = DataAdapter()
                            }
                        }
                        )

                }


            })

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

        class TrackHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val distance: TextView = itemView.card_distance_params
            val time: TextView = itemView.card_time_params
            val date: TextView = itemView.card_date
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

                        holder.distance.text =
                            String.format(Locale.getDefault(), "%.2f", userTrack.distance)
                                .padEnd(8, ' ')
                        holder.time.text = userTrack.time
                        holder.date.text = userTrack.startDate
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