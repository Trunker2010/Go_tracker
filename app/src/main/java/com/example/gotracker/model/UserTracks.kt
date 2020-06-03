package com.example.gotracker.model

data class UserTracks(
    var tracks: MutableList<MutableList<LocParams>> = mutableListOf(mutableListOf<LocParams>())
)
