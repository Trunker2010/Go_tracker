package com.example.gotracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.gotracker.R
import com.example.gotracker.utils.userTracks
import kotlinx.android.synthetic.main.fragment_track_list.*
import kotlinx.android.synthetic.main.track_item.view.*

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

    }

    class DataAdapter : RecyclerView.Adapter<DataAdapter.TracksHolder>() {

        class TracksHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val distance = itemView.card_distance_params

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TracksHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
            return TracksHolder(view)
        }

        override fun getItemCount(): Int {
            return userTracks.size
        }

        override fun onBindViewHolder(holder: TracksHolder, position: Int) {
            holder.distance.text = userTracks[position].distance.toString()
        }
    }
    companion object{
        fun newInstance () : TrackListFragment {
            return TrackListFragment()
        }
    }

}