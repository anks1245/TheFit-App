package com.example.ajatusfit.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast

class MyServices:Service() {

    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented");
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(applicationContext, "This Application is Running in Background", Toast.LENGTH_SHORT).show()

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext,this::class.java)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }
}