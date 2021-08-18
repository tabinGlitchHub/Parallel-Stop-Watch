package com.example.parallelstopwatch.thread

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.parallelstopwatch.util.Constants
import java.util.*
import kotlin.properties.Delegates

// this is a make do timer that uses Thread and timer to run the timer, actual implementation
//would have been to use an ThreadPoolExecutor and run the timers together in different threads
//due to lack of time and knowledge. This implementation survives the app being in the background
// but it does not survive the lifecycle ending events unlike service class.
class SWThread(private val intent: Intent, private val context: Context) : Thread() {

    //declarations
    private lateinit var timer: Timer
    var time by Delegates.notNull<Long>()

    //use to initialize the values
    private fun init(){
        timer = Timer()
        time = intent.getLongExtra(Constants.TIME_THREAD_EXTRA, 0L)
    }

    override fun run() {
        init()
        //start timer
        timer.scheduleAtFixedRate(TimeTask(time),0,1000)
    }

    //timer extending the TimerTask()
    private inner class TimeTask(private var time: Long) : TimerTask() {
        override fun run() {
            //everytime, create an intent
            val intent = Intent(Constants.TIME_THREAD_UPDATED)
            //then increment it by 1
            time++
            //then add this intent in package
            intent.putExtra(Constants.TIME_THREAD_EXTRA, time)
            //broadcast this intent with updated time
            context.sendBroadcast(intent)
        }
    }

    //stop timer and broadcast one last intent to store the value
    fun onDestroy(){
        val intent = Intent(Constants.TIME_THREAD_UPDATED)
        intent.putExtra(Constants.TIME_THREAD_EXTRA, time)
        timer.cancel()
    }
}