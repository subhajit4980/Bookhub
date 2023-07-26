package com.example.bookhub.Service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.bookhub.UI.Audio_story.Play_Audio
import com.example.bookhub.UI.MainActivity

class MusicPlayerService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            (applicationContext as Play_Audio).handleNotificationAction(it)
        }
        return super.onStartCommand(intent, flags, startId)
    }
}
