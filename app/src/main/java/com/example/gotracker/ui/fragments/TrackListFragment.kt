package com.example.gotracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.gotracker.R
import com.example.gotracker.utils.difTrackDate
import com.example.gotracker.utils.sortTracks
import com.example.gotracker.utils.userTracks
import kotlinx.android.synthetic.main.fragment_track_list.*
import kotlinx.android.synthetic.main.track_item.view.*
import kotlinx.android.synthetic.main.date_item.*
import kotlinx.android.synthetic.main.date_item.view.*

const val TYPE_DATE = 0;
const val TYPE_CONTENT = 1;

class TrackListFragment : Fragment(R.layout.fragment_track_list) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return super.onCreateView(inflater, container, savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        rv_tracks.adapter = DataAdapter()
        sortTracks()
        difTrackDate()

    }

    class DataAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemViewType(position: Int): Int {
            return userTracks[position].type
        }

        class TracksHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val distance = itemView.card_distance_params
            val time = itemView.card_time_params
            val date = itemView.card_date
        }

        class DateHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val date = itemView.date_item
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == TYPE_DATE) {
                var viewDate = LayoutInflater.from(parent.context)
                    .inflate(R.layout.date_item, parent, false)
                return DateHolder(viewDate)
            } else {
                var view =
                    LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)

                return TracksHolder(view)
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