package com.example.parallelstopwatch.activities

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.parallelstopwatch.R
import com.example.parallelstopwatch.databinding.ActivityMainBinding
import com.example.parallelstopwatch.service.SWService
import com.example.parallelstopwatch.thread.SWThread
import com.example.parallelstopwatch.util.Constants
import com.example.parallelstopwatch.util.TimeConverter


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var serviceIntent: Intent
    private lateinit var threadIntent: Intent
    private lateinit var swThread: SWThread
    private var isServiceTimerOn = false
    private var isThreadTimerOn = false

    //time of timer (service)
    private var serviceTime = 0L

    //time of timer (thread)
    private var threadTime = 0L

    //utility methods to convert and format time to string
    private val timeConverter = TimeConverter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get binded reference
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //create the service using baseContext to be safe because it is lifecycle dependant
        serviceIntent = Intent(baseContext, SWService::class.java)
        //create the intent for thread class
        threadIntent = Intent(baseContext, SWThread::class.java)
        swThread = SWThread(threadIntent, this)

        //this receivers observe the broadcasts and act accordingly with their
        // BroadCast Receiver classes
        registerReceiver(updateServiceTime, IntentFilter(Constants.TIME_UPDATED))
        registerReceiver(updateThreadTime, IntentFilter(Constants.TIME_THREAD_UPDATED))

        //since the UI resets everytime activity is created, check if a previous service is
        // still running and update UI accordingly
        if (isMyServiceRunning(SWService::class.java)) {
            binding.btnStartStopService.text = getString(R.string.button_stop)
            isServiceTimerOn = true
        } else {
            binding.btnStartStopService.text = getString(R.string.button_start)
            isServiceTimerOn = false
        }

        //onClickListener start/stop button for service timer
        binding.btnStartStopService.setOnClickListener {
            if (!isServiceTimerOn) startServiceTimer() else stopServiceTimer()
        }

        //onClickListener reset button for service timer
        binding.btnResetService.setOnClickListener {
            stopServiceTimer()
            serviceTime = 0L
            binding.tvServiceTime.text = getString(R.string.default_time)
        }

        //onCLickListener start/stop button for thread timer
        binding.btnStartStopThread.setOnClickListener {
            if (!isThreadTimerOn) startThreadTimer() else stopThreadTimer()
        }

        //onClickListener rest button for thread timer
        binding.btnResetThread.setOnClickListener {
            stopThreadTimer()
            threadTime = 0L
            binding.tvThreadTime.text = getString(R.string.default_time)
        }
    }

    //actions to be taken when killing Thread
    private fun stopThreadTimer() {
        isThreadTimerOn = false
        swThread.onDestroy()
        swThread.interrupt()
    }

    //actions to be taken when starting the Thread
    private fun startThreadTimer() {
        threadIntent.putExtra(Constants.TIME_THREAD_EXTRA, threadTime)
        swThread.run()
        isThreadTimerOn = true
    }

    //check if service is running by going through all running services and matching className
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    //service broadcast receiver that catches the intents and updates UI
    private val updateServiceTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val time = intent.getLongExtra(Constants.TIME_EXTRA, 0L)
            binding.tvServiceTime.text = timeConverter.longToString3Digits(time)
        }
    }

    //thread broadcast receiver that catches the intents and updates UI
    private val updateThreadTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val time = intent.getLongExtra(Constants.TIME_THREAD_EXTRA, 0L)
            binding.tvThreadTime.text = timeConverter.longToString3Digits(time)
        }
    }

    //actions to be taken when service is to be killed
    private fun stopServiceTimer() {
        stopService(serviceIntent)
        binding.btnStartStopService.text = getString(R.string.button_start)
        isServiceTimerOn = false
    }

    //actions to be taken when service is to be started
    private fun startServiceTimer() {
        serviceIntent.putExtra(Constants.TIME_EXTRA, serviceTime)
        startService(serviceIntent)
        binding.btnStartStopService.text = getString(R.string.button_stop)
        isServiceTimerOn = true
    }
}