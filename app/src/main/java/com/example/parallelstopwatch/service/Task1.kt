package com.example.parallelstopwatch.service

import android.util.Log

class Task1: Runnable {
    //test tasks, was supposed to carry timer funtions
    override fun run() {
        for(i in 1..10){
            Log.d("RUNNABLE",i.toString())
        }
    }
}