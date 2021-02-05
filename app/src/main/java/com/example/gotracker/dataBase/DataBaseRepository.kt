package com.example.gotracker.dataBase

import com.example.gotracker.model.LocParams

interface DataBaseRepository {
    fun insertTrack(locParams: LocParams, startTime: Long)
    fun deleteTrack(id: String)
    fun getAllTracks(onSuccess: () -> Unit)

}