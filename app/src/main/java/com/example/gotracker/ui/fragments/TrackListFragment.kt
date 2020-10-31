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
import com.example.gotracker.model.UserTrack
import com.example.gotracker.utils.*
import com.yandex.mapkit.geometry.Point
import kotlinx.android.synthetic.main.fragment_track_list.*
import kotlinx.android.synthetic.main.track_item.view.*
import kotlinx.android.synthetic.main.date_item.view.*

const val TYPE_DATE = 0;
const val TYPE_CONTENT = 1;

class TrackListFragment : Fragment(R.layout.fragment_track_list) {


    @RequiresApi(Build.VERSION_CODES.N)
    private fun initUserTracks() {
        loadTracksProgressBar.visibility = View.VISIBLE
        userTracks = mutableListOf()

        REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID)
            .addListenerForSingleValueEvent(AppValueEventListener { rootSnapshot ->

                rootSnapshot.children.forEach { trackID ->
                    var userTrack = UserTrack(TYPE_CONTENT)

                    userTrack.distance =
                        trackID.child(CHILD_DISTANCE).getValue(Double::class.java)!!

                    userTrack.time = LocationConverter.convertMStoTime(
                        trackID.child(CHILD_TIME).getValue(Long::class.java)!!
                    )
                    userTrack.startTime =
                        timeToDate(
                            trackID.child(CHILD_START_TIME).getValue(Long::class.java)!!
                        )

                    REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID)
                        .child(trackID.key.toString()).child(
                            CHILD_TRACK_POINTS
                        ).addListenerForSingleValueEvent(AppValueEventListener { tracks ->

                            tracks.children.forEach { points ->

                                Log.d("dbThread", Thread.currentThread().name)
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
                            Log.d(
                                "countAndSize",
                                "${rootSnapshot.children.count()} ${userTracks.size}"
                            )
                            if (rootSnapshot.children.count() == userTracks.size) {
                                sortTracks()
                                difTrackDate()
                                loadTracksProgressBar.visibility = View.GONE
                                rv_tracks.adapter = DataAdapter()
                                Log.d("countAndSize", "if=true")


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
            return userTracks[position].type
        }


        class TracksHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val distance: TextView = itemView.card_distance_params
            val time: TextView = itemView.card_time_params
            val date: TextView = itemView.card_date
        }

        class DateHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val date = itemView.date_item
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == TYPE_DATE) {
                var viewDate = LayoutInflater.from(parent.context)
                    .inflate(R.layout.date_item, parent, false)
                DateHolder(viewDate)
            } else {
                var view =
                    LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)

                TracksHolder(view)
            }
        }

        override fun getItemCount(): Int {
            return userTracks.size
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is TracksHolder) {
                holder.distance.text = userTracks[position].distance.toString()
                holder.time.text = userTracks[position].time
                holder.date.text = userTracks[position].startTime
            }
            if (holder is DateHolder) {
                holder.date.text = userTracks[position].startTime
            }


        }
    }


    companion object {
        fun newInstance(): TrackListFragment {
            return TrackListFragment()
        }
    }

}