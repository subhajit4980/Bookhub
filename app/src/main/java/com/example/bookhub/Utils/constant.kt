package com.example.bookhub.Utils

import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
@RequiresApi(Build.VERSION_CODES.S)
object constant {
    var directoryPath: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RECORDINGS)
            .toString() + "/BookHUB/"
    const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    const val PICK_FILE = 99
    const val CAMERA_PERMISSION_CODE = 100
    const val STORAGE_PERMISSION_CODE = 101
    const val CHANNEL_ID = "MusicPlayerChannel"
    const val NOTIFICATION_ID = 1
    const val ACTION_PLAY_PAUSE = "action_play_pause"
    const val ACTION_STOP = "action_stop"
}