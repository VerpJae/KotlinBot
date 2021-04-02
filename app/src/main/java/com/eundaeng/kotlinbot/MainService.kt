package com.eundaeng.kotlinbot

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast

/**
 * Created by Dark Tornado on 2018-01-17.
 */
class MainService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val fuck = intent.getStringExtra("toast")
        if (fuck != null) Toast.makeText(this, fuck, Toast.LENGTH_SHORT).show()
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
