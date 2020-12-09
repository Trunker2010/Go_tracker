package com.example.gotracker.ui.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.gotracker.GoTrackerApplication
import com.example.gotracker.R
import com.example.gotracker.model.Date
import com.example.gotracker.model.UserTrack
import com.example.gotracker.ui.activities.TrackInfoActivity
import com.example.gotracker.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.date_item.view.*
import kotlinx.android.synthetic.main.fragment_track_list.*
import kotlinx.android.synthetic.main.track_item.*
import kotlinx.android.synthetic.main.track_item.view.*

import java.util.*

const val TRACK_PARCELABLE = "track_parcelable"


class TrackListFragment : Fragment(R.layout.fragment_track_list), View.OnClickListener {
    var showCb = false
    val REMOVE_BTN_STATE = "remove_btn_state"
    val MAP_TRACKS = "map_track"
    lateinit var app: GoTrackerApplication
    private val trackEventListener = object : ValueEventListener {

        override fun onDataChange(rootSnapshot: DataSnapshot) {
            var countTracks = rootSnapshot.children.count()
            rootSnapshot.children.forEach { trackID ->
                val userTrack = createTrack(trackID)
                if (userTrack.distance > 0) {
                    userTracks.add(userTrack)
                } else
                    countTracks--



                if (countTracks == userTracks.size) {
                    sortTracks()
                    difTrackDate()
                    if (loadTracksProgressBar != null) {
                        loadTracksProgressBar.visibility = View.GONE
                        updateUI()
                    }

                }


            }
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }


    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            removeImage.id -> {
                showCb = false
                (rv_tracks.adapter as TracksAdapter).removeSelectedItems()
                (rv_tracks.adapter as TracksAdapter).notifyDataSetChanged()

                buttons.visibility = View.GONE


            }
            closeImage.id -> {
                showCb = false
                (rv_tracks.adapter as TracksAdapter).notifyDataSetChanged()
                app.mapSelectedTrack.clear()
                buttons.visibility = View.GONE
            }
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
        app = requireActivity().application as GoTrackerApplication

        showCb = app.mapSelectedTrack.isNotEmpty()


        retainInstance = true

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        removeImage.setOnClickListener(this)
        closeImage.setOnClickListener(this)

        if (savedInstanceState != null) {

            buttons.visibility = savedInstanceState.getInt(REMOVE_BTN_STATE)

        }


        initUserTracks()
    }


    inner class TracksAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        override fun getItemViewType(position: Int): Int {
            return when (userTracks[position]) {
                is UserTrack -> R.layout.track_item
                is Date -> R.layout.date_item

                else -> -1

            }
        }

        private fun removeDbTrack(id: String) {
            val ref = REF_DATABASE_ROOT.child(NODE_TRACKS).child(AUTH.uid.toString()).child(id).ref
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.ref.removeValue()

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


        }

        fun removeSelectedItems() {
            val sortedTracksMap = app.mapSelectedTrack.toSortedMap(reverseOrder())



            for (track in sortedTracksMap) {


                removeDbTrack(track.value)
                userTracks.removeAt(track.key);
                notifyItemRemoved(track.key)


            }
            sortedTracksMap.clear()
            app.mapSelectedTrack.clear()


        }


        inner class TrackHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener, View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {
            val distance: TextView = itemView.card_distance_params
            val duration: TextView = itemView.card_duration_params
            val startTime: TextView = itemView.card_star_time
            val trackCheckBox: CheckBox = itemView.track_item_cb

            init {
                itemView.setOnClickListener(this)
                itemView.setOnLongClickListener(this)
                trackCheckBox.setOnCheckedChangeListener(this)
            }

            lateinit var mUserTrack: UserTrack


            override fun onClick(v: View?) {

                val intent = Intent(this@TrackListFragment.context, TrackInfoActivity::class.java)
                intent.putExtra(TRACK_PARCELABLE, mUserTrack)
                Log.d("mUserTrack.trackID", mUserTrack.startTime)
                this@TrackListFragment.startActivity(intent)
            }

            fun bindTrack(userTrack: UserTrack) {
                mUserTrack = userTrack
            }

            override fun onLongClick(v: View?): Boolean {
                showCb = true
                notifyDataSetChanged()

                return true
            }

            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {


                if (isChecked) {
                    app.mapSelectedTrack[layoutPosition] = mUserTrack.trackID


                } else {
                    app.mapSelectedTrack.remove(layoutPosition)


                    if (app.mapSelectedTrack.isEmpty()) {
                        showCb = false
                        Log.d("onCheckedChanged", "isEmpty ${app.mapSelectedTrack.isEmpty()}")
                        if (!rv_tracks.isComputingLayout){
                            notifyDataSetChanged()
                        }

                    }
                }
                buttons.visibility =
                    if (app.mapSelectedTrack.isNotEmpty()) View.VISIBLE else View.GONE


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
                            "${
                                String.format(
                                    Locale.getDefault(),
                                    "%.2f",
                                    userTrack.distance
                                )
                            } км"
                        holder.duration.text =
                            LocationConverter.convertMStoTime(userTrack.activeDuration)
                        holder.startTime.text = userTrack.startTime


                        if (showCb) holder.trackCheckBox.visibility = View.VISIBLE else holder.trackCheckBox.visibility = View.GONE

                        holder.trackCheckBox.isChecked = app.mapSelectedTrack.contains(position)


                    }
                    Log.d("TrackHolder", position.toString())
                }
            }

        }
    }

    companion object {
        fun newInstance(): TrackListFragment {
            return TrackListFragment()
        }
    }

    fun updateUI() {
        rv_tracks.adapter = TracksAdapter()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(REMOVE_BTN_STATE, buttons.visibility)

        Log.d(REMOVE_BTN_STATE, buttons.visibility.toString())
        View.VISIBLE
    }


}