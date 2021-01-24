package com.example.gotracker.model

import android.os.Handler
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.gotracker.utils.LocationConverter

class TrackTimer {
    private var sumTimer = 0L
    private var startTime = 0L
    private var startTimeInMileSeconds = 0L
    private var pausedTime = 0L
    private var startPauseTime = 0L
    private var pauseDuration = 0L
    private var isPaused = false
    private var timeSwapBuffer = 0L
    var durationTime = 0L

    //    var currentTime: String = "0:00"
    var liveCurrentTime = MutableLiveData<String>()


    lateinit var timerThread: Thread
    fun stopTimer() {
        timerThread.interrupt()
    }

    fun clearTimerParams() {
        startPauseTime = 0
        startTime = 0L
        startTimeInMileSeconds = 0L
        timeSwapBuffer = 0L
        durationTime = 0L
//        currentTime = "0:00"
        isPaused = false
        liveCurrentTime.postValue("0:00")
        pauseDuration = 0L
        pausedTime = 0L
        sumTimer = 0L
    }

    /*Приостанавливает время таймера*/
    fun onPauseTimer() {
        isPaused = true
        startPauseTime = SystemClock.uptimeMillis()
    }

    /*Отменяет приостановление таймера*/
    fun offPauseTimer() {
        isPaused = false
        sumTimer += pauseDuration

        pauseDuration = 0L
    }

    private fun updatePauseTime() {
        pausedTime = SystemClock.uptimeMillis() - startPauseTime
        pauseDuration = pausedTime
//        Log.d("pauseTime", pauseDuration.toString())
    }

    fun startTimer() {

        timerThread = object : Thread() {

            override fun run() {
                startTime = SystemClock.uptimeMillis();

                while (!isInterrupted) {


                    while (isPaused) {
                        updatePauseTime()
                        //Log.d("timerThread", "updatePauseTime")
                    }

                    startTimeInMileSeconds = SystemClock.uptimeMillis() - startTime
                    durationTime = timeSwapBuffer + startTimeInMileSeconds - sumTimer
                    liveCurrentTime.postValue(LocationConverter.convertMStoTime(durationTime))
                    // Log.d("timerThread", "pauseDuration = $pauseDuration")
                    if (!isInterrupted) {
                        sleep(100)
                    }


                }


                clearTimerParams()
                Log.d("timerThread", "Interrupted")

                return

            }

        }

        timerThread.start()
    }


}