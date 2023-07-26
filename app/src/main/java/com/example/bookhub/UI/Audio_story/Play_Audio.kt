package com.example.bookhub.UI.Audio_story

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.example.bookhub.R
import com.example.bookhub.Service.MusicPlayerService
import com.example.bookhub.Utils.common.formatDuration
import com.example.bookhub.Utils.common.showSnackBar
import com.example.bookhub.Utils.constant
import com.example.bookhub.Utils.constant.CHANNEL_ID
import com.example.bookhub.Utils.constant.NOTIFICATION_ID
import com.example.bookhub.Utils.setStatusBarColor
import com.example.bookhub.databinding.ActivityPlayAudioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Play_Audio : AppCompatActivity() {
    private var _binding: ActivityPlayAudioBinding? = null
    private val binding get() = _binding!!
    private lateinit var id: String
    private lateinit var title: String
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var  handler:Handler
    private val viewmodel: com.example.bookhub.ViewModel.ViewModel by viewModels()
    private var play = true
    private var currentPosition: Int = 0
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    private val userid=FirebaseAuth.getInstance().currentUser!!.uid.toString()
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPlayAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        id = intent.getStringExtra("id").toString()
        title = intent.getStringExtra("title").toString()
        music_control()
        UI()
        binding.back.setOnClickListener {
            if (::mediaPlayer.isInitialized) mediaPlayer.reset()
            finish()
        }
    }
    fun UI() {
        setStatusBarColor(R.color.navy_blue)
        binding.title.text= title
        val len=title.toString().length
        binding.title.setSelected(true)
        if (len>15)
        {
            binding.title.apply {
                isSingleLine=true
                ellipsize=android.text.TextUtils.TruncateAt.MARQUEE
                marqueeRepeatLimit=-1
                isFocusable=true
                isFocusableInTouchMode=true
                setHorizontallyScrolling(true)
            }

        }
        val storageref=FirebaseStorage.getInstance().reference.child("bookhub/AudioStroy/image/${id}.jpeg")
        storageref.downloadUrl.addOnSuccessListener {
            Glide.with(this).load(it).placeholder(R.drawable.music).into(binding.img)
        }
//     viewmodel
        viewmodel.checkLikeStatus(id,userid)
        viewmodel.likeStatusLiveData.observe(this,{ isLiked->
            binding.like.apply {
                if (isLiked) {
                    setImageResource(R.drawable.ic_baseline_favorite_24)
                } else {
                    setImageResource(R.drawable.ic_baseline_favorite_border_24)
                }
            }
        })
        binding.like.setOnClickListener {
            viewmodel.checkLikeStatus(id,userid)
            if (viewmodel.likeStatusLiveData.value==true){
                viewmodel.unlike(id,userid){
                    binding.like.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                    showSnackBar(it, findViewById(android.R.id.content) )
                }
            }else{
                viewmodel.like(id,userid){
                    binding.like.setImageResource(R.drawable.ic_baseline_favorite_24)
                    showSnackBar(it, findViewById(android.R.id.content) )
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        if (::mediaPlayer.isInitialized) mediaPlayer.reset()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        if (::mediaPlayer.isInitialized)
        {
            mediaPlayer.release()
        }
    }

    fun music_control() {
        // Fetch the music file from Firebase Storage
        val musicRef = storageRef.child("bookhub/AudioStroy/${id}.mp3")
        musicRef.downloadUrl.addOnSuccessListener { uri ->
            // Prepare the MediaPlayer with the music file
            mediaPlayer = MediaPlayer.create(this, Uri.parse(uri.toString()))
            mediaPlayer.start()
            seekbar()
            updateNotification()
            binding.playButton.setImageResource(R.drawable.ic_baseline_pause_24)
            // Play & Pause button click listener
            binding.apply {
                playButton.setOnClickListener {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                        playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                        currentPosition = mediaPlayer.currentPosition
                        updateNotification()
                    } else {
                        mediaPlayer.seekTo(currentPosition)
                        mediaPlayer.start()
                        binding.playButton.setImageResource(R.drawable.ic_baseline_pause_24)
                        updateNotification()
                    }
                }
            }
            // Fast forward button click listener
            binding.forward.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                     currentPosition = mediaPlayer.currentPosition
                    val duration = mediaPlayer.duration
                    val forwardTime = 10000 // 10 seconds
                    val newPosition = currentPosition + forwardTime
                    if (newPosition <= duration) {
                        mediaPlayer.seekTo(newPosition)
                    } else {
                        mediaPlayer.seekTo(duration)
                    }
                }
            }
            // Rewind button click listener
            binding.backward.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    val currentPosition = mediaPlayer.currentPosition
                    val rewindTime = 10000 // 10 seconds
                    val newPosition = currentPosition - rewindTime
                    if (newPosition >= 0) {
                        mediaPlayer.seekTo(newPosition)
                    } else {
                        mediaPlayer.seekTo(0)
                    }
                }
            }
            mediaPlayer.setOnCompletionListener {
                binding.playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                binding.seekBar.progress=0
                currentPosition=0
                updateNotification()
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun seekbar()
    {
        seekBar=binding.seekBar
        seekBar.max=mediaPlayer.duration
        // Update the MediaPlayer's current position as the SeekBar progresses
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentPosition=progress
                    mediaPlayer.seekTo(currentPosition)
                    binding.startTimer.text=formatDuration(currentPosition.toLong())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                TODO("Not yet implemented")
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                TODO("Not yet implemented")
            }
        })
        // Set up a periodic handler to update the seekbar
        handler = Handler(Looper.getMainLooper())
            handler.post(object : Runnable {
                override fun run() {
                    val currentPosition = mediaPlayer.currentPosition
                    seekBar.progress = currentPosition
                    // Check if the binding is not null before accessing its views
                    val startTime = formatDuration(currentPosition.toLong())
                    binding.startTimer.text = startTime
                    // Run this handler again after a delay of 100 milliseconds
                    handler.postDelayed(this, 100)
                }
            })

        val endTime = formatDuration(mediaPlayer.duration.toLong()) // Format the duration to display as end time
        binding.endTimer.text=endTime
    }
    override fun onStop() {
        super.onStop()
        if (::handler.isInitialized)
        handler.removeCallbacksAndMessages(null)
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Music Player"
            val descriptionText = "Notification for Music Player"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun updateNotification() {
        if (::mediaPlayer.isInitialized){
        val playPauseIcon =
            if (mediaPlayer.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseAction = NotificationCompat.Action.Builder(
            playPauseIcon, "Play/Pause",
            createPendingIntent(constant.ACTION_PLAY_PAUSE)
        ).build()

        val stopAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_media_pause, "Stop",
            createPendingIntent(constant.ACTION_STOP)
        ).build()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Sample Music")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .addAction(playPauseAction)
            .addAction(stopAction)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }
    }
    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.action = action
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    fun handleNotificationAction(action: String) {
        when (action) {
            constant.ACTION_PLAY_PAUSE -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    binding.playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    currentPosition = mediaPlayer.currentPosition
                    updateNotification()
                } else {
                    mediaPlayer.seekTo(currentPosition)
                    mediaPlayer.start()
                    binding.playButton.setImageResource(R.drawable.ic_baseline_pause_24)
                    updateNotification()
                }
            }
            constant.ACTION_STOP -> {
                mediaPlayer.stop()
                mediaPlayer.prepare()
                updateNotification()
            }
        }
    }
}

