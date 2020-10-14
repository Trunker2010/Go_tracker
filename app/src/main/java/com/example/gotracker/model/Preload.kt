package com.example.gotracker.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gotracker.utils.initUserTracks

class Preload {

    interface Callback {
        fun callingBack() {

        }
    }

    lateinit var callback: Callback
    public fun regCallBack(callback: Callback) {
        this.callback = callback
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public fun doPreload() {
        initUserTracks()
        callback.callingBack()


    }
}