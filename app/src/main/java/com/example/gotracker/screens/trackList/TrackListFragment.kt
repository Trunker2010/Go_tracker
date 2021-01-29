package com.example.gotracker.screens.trackList

import android.app.Activity
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.gotracker.GoTrackerApplication
import com.example.gotracker.R
import com.example.gotracker.databinding.FragmentTrackListBinding
import com.example.gotracker.model.Date
import com.example.gotracker.model.UserTrack
import com.example.gotracker.screens.trackInfo.TrackInfoActivity
import com.example.gotracker.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.date_item.view.*

import kotlinx.android.synthetic.main.track_item.*
import kotlinx.android.synthetic.main.track_item.view.*

import java.util.*

const val TRACK_PARCELABLE = "track_parcelable"


class TrackListFragment : Fragment(), View.OnClickListener {
    var showCb = false
    val REMOVE_BTN_STATE = "remove_btn_state"
    val MAP_TRACKS = "map_track"
    lateinit var app: GoTrackerApplication
    lateinit var mViewModel: TrackListViewModel
    var _binding: FragmentTrackListBinding? = null
    val mBinding get() = _binding!!

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
                    difTracks()
//                    getIndexesForInputDate()
                    if (mBinding.loadTracksProgressBar != null) {
                        mBinding.loadTracksProgressBar.visibility = View.GONE
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
            mBinding.removeImage.id -> {

                (mBinding.rvTracks.adapter as TracksAdapter).removeSelectedItems()

//                (rv_tracks.adapter as TracksAdapter).notifyDataSetChanged()
                removeEmptyDate()
                mBinding.buttons.visibility = View.GONE


            }
            mBinding.closeImage.id -> {
                showCb = false
                (mBinding.rvTracks.adapter as TracksAdapter).notifyDataSetChanged()
                app.mapSelectedTrack.clear()
                mBinding.buttons.visibility = View.GONE
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun initUserTracks() {
        mBinding.loadTracksProgressBar.visibility = View.VISIBLE
        userTracks = mutableListOf()
        val tracksRefs = REF_DATABASE_ROOT.child(NODE_TRACKS).child(UID)

        tracksRefs
            .addListenerForSingleValueEvent(trackEventListener)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(TrackListViewModel::class.java)



        app = requireActivity().application as GoTrackerApplication


        showCb = app.mapSelectedTrack.isNotEmpty()


        retainInstance = true

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackListBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.removeImage.setOnClickListener(this)
        mBinding.closeImage.setOnClickListener(this)

        if (savedInstanceState != null) {

            mBinding.buttons.visibility = savedInstanceState.getInt(REMOVE_BTN_STATE)

        }


        initUserTracks()
    }

    private fun removeEmptyDate() {
        val datePositionList = mutableListOf<Int>()
        val remDatePositionList = mutableListOf<Int>()
        fun setDatePosition() {
            for ((pos) in userTracks.withIndex()) {
                if (userTracks[pos] !is UserTrack) {
                    datePositionList.add(pos)
                }
            }
            Log.d("removeEmptyDate", "$datePositionList")
        }

        fun findEmptyDate() {
            var lastPosition = 0
            for (pos in datePositionList.indices) {

                if (datePositionList[pos] == userTracks.size - 1) {
                    remDatePositionList.add(datePositionList[pos])

                } else {
                    if ((lastPosition - datePositionList[pos]) == -1) {
                        remDatePositionList.add(lastPosition)

                    }
                }

                lastPosition = datePositionList[pos]


            }
            Log.d("remDatePositionList", "$remDatePositionList")

        }



        setDatePosition()
        findEmptyDate()
        remDatePositionList.sortDescending()
        for (pos in remDatePositionList) {
            mBinding.rvTracks.adapter!!.notifyItemRemoved(pos)

            userTracks.removeAt(pos)
        }

//        mBinding.rvTracks.adapter!!.notifyDataSetChanged()


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

            val revSortedTracksMap = app.mapSelectedTrack.toSortedMap(reverseOrder())
            val indexes = mutableListOf<Int>()
            var i = 1
            do {
                indexes.add(i)
                i++
            } while (i != itemCount + 1)
            for (selected in revSortedTracksMap) {
                indexes.remove(selected.key)
            }
            showCb = false

            for (pos in indexes) {
                notifyItemChanged(pos)
            }






            for (track in revSortedTracksMap) {


                removeDbTrack(track.value)
                notifyItemRemoved(track.key)
                userTracks.removeAt(track.key);


            }

            revSortedTracksMap.clear()
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

//                        Log.d("onCheckedChanged", "isEmpty ${app.mapSelectedTrack.isEmpty()}")
                        if (!mBinding.rvTracks.isComputingLayout) {
                            showCb = false
                            notifyDataSetChanged()
                        }

                    }
                }
                mBinding.buttons.visibility =
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


                        if (showCb) holder.trackCheckBox.visibility =
                            View.VISIBLE else holder.trackCheckBox.visibility = View.GONE
                        Log.d("TrackHolder", showCb.toString())

                        holder.trackCheckBox.isChecked = app.mapSelectedTrack.contains(position)


                    }

                }
            }


        }
    }

//    companion object {
//        fun newInstance(): TrackListFragment {
//            return TrackListFragment()
//        }
//    }

    fun updateUI() {
        mBinding.rvTracks.adapter = TracksAdapter()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(REMOVE_BTN_STATE, mBinding.rvTracks.visibility)

        Log.d(REMOVE_BTN_STATE, mBinding.rvTracks.visibility.toString())
        View.VISIBLE
    }


    fun difTracks() {
        val dateMap = mutableMapOf<Int, Date>()
        fun setIndexesForInputDate() {

            if (userTracks.size >= 1) {

                var firstDate = (userTracks[0] as Date).startDate
                val date = Date()
                date.startDate = firstDate

                dateMap[0] = date

                for (i in userTracks.indices) {

                    if (userTracks.size > i + 1 && userTracks[i + 1] is UserTrack) {

                        if ((userTracks[i] as Date).startDate != (userTracks[i + 1] as Date).startDate) {
                            val bufDate = (userTracks[i + 1] as Date).startDate
                            val date = Date()
                            date.startDate = bufDate
                            dateMap[i + 1] = date
                        }

                    }
                    Log.d("getIndexesForInputDate", "$dateMap")

                }

            }

        }

        fun insetDate() {
            val sortedDate = dateMap.toSortedMap(reverseOrder())
            for (date in sortedDate) {
                userTracks.add(date.key, date.value)
            }
        }
        setIndexesForInputDate()
        insetDate()
    }


}