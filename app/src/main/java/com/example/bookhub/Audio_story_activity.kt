package com.example.bookhub

import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_audio_story.*

class Audio_story_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_story)
        addpost.setOnClickListener{
            home_view.visibility=View.GONE
            audio_post.visibility=View.VISIBLE
        }
        backhome.setOnClickListener{
            home_view.visibility=View.VISIBLE
            audio_post.visibility=View.GONE
        }
    }
}