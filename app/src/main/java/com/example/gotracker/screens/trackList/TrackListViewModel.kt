package com.example.gotracker.screens.trackList

import android.app.Application
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.gotracker.GoTrackerApplication
import com.example.gotracker.model.Date
import com.example.gotracker.model.UserTrack
import com.example.gotracker.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_track_list.*

class TrackListViewModel(app: Application) : AndroidViewModel(app) {
    private fun difTracks() {
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

    private fun sortTracks() = (userTracks as MutableList<*>).sortByDescending {
        (it as? UserTrack)?.startTimeInMs
    }

    fun getUserTracks(onSuccess: () -> Unit) {
        REPOSITORY.getAllTracks {
            sortTracks()
            difTracks()
            onSuccess()
        }
    }

    fun removeTrack(id: String) {
        REPOSITORY.deleteTrack(id)
    }



    fun getPrepareItems(itemCount: Int): List<Int> {
        val itemCount = itemCount

        val revSortedTracksMap =
            getApplication<GoTrackerApplication>().mapSelectedTrack.toSortedMap(reverseOrder())
        val indexes = (1..itemCount).toMutableList()

        for (selected in revSortedTracksMap) {
            indexes.remove(selected.key)
        }
        return indexes

    }

    fun removeEmptyDate(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
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
            adapter.notifyItemRemoved(pos)

            userTracks.removeAt(pos)
        }

//        mBinding.rvTracks.adapter!!.notifyDataSetChanged()


    }


}
