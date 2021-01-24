package com.example.gotracker.dataBase

import com.example.gotracker.model.LocParams

interface AppDataBaseRepository {
    fun addTrack(locParams: LocParams)
    fun removeTrack()

}