package com.example.parallelstopwatch.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.parallelstopwatch.R
import com.example.parallelstopwatch.activities.MainActivity
import com.example.parallelstopwatch.util.Constants
import java.util.*
import java.util.concurrent.*

class SWService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null
    private lateinit var notificationManager: NotificationManagerCompat
//    private lateinit var threadPoolExecutor: ThreadPoolExecutor
//    private val INIT_SIZE = 3
//    private val MAX_SIZE = 3
//    private val linkedBlockingQueue = LinkedBlockingQueue<Runnable>()

    private val timer = Timer()

    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationManagerCompat.from(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //this part was meant to carry concurrent timing using executors
//        threadPoolExecutor = ThreadPoolExecutor(
//            INIT_SIZE,
//            MAX_SIZE,
//            60L,
//            TimeUnit.SECONDS,
//            linkedBlockingQueue)
//
//        threadPoolExecutor.execute(Task1())
//        threadPoolExecutor.execute(Task2())
//
//        threadPoolExecutor.shutdown()

        //get the time from intent
        val time = intent.getLongExtra(Constants.TIME_EXTRA, 0L)
        //start timer using TimeTask() with a delay of 1second
        timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
        //create a notification channel to be launched when the service is started
        val channel = NotificationChannel(
            Constants.CHANNEL_ID,
            Constants.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
        startForeground(Constants.NOTIFICATION_ID, getNotificationBuilder().build())
        return START_STICKY
    }

    //actions to be taken when stop button is pressed
    override fun onDestroy() {
        //stop timer
        timer.cancel()
        //stop service
        stopForeground(true)
        stopSelf()
        //stop notification popup
        notificationManager.cancel(Constants.NOTIFICATION_ID)
        super.onDestroy()
    }

    //Timer that extends TimerTask
    private inner class TimeTask(private var time: Long) : TimerTask() {
        override fun run() {
            //everytime, create an intent
            val intent = Intent(Constants.TIME_UPDATED)
            //then increment it by 1
            time++
            //then add this intent in package
            intent.putExtra(Constants.TIME_EXTRA, time)
            //broadcast this intent with updated time
            sendBroadcast(intent)
        }
    }

    //builder to build notification
    private fun getNotificationBuilder(): NotificationCompat.Builder =
        NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Stop-watch is running..")
            .setContentText("Tap to Open!")
            .setContentIntent(getPendingIntent())


    //pending Intent, premade intent to be triggered whenever notification is tapped
    private fun getPendingIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            1,
            Intent(this, MainActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )
}