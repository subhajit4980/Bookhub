package com.example.bookhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.example.bookhub.startup.wellcomeuser

class MainActivity : AppCompatActivity() {
    val SPLASH_SCREEN=5000
    private lateinit var topAnimation:Animation
    private lateinit var bottomAnimation: Animation
    private lateinit var imageView: ImageView
    private lateinit var title_txt:TextView
    private lateinit var develop_txt:TextView
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        topAnimation=AnimationUtils.loadAnimation(this,R.anim.top_animation)
        bottomAnimation=AnimationUtils.loadAnimation(this,R.anim.bottom_animation)
        imageView =findViewById(R.id.virusc)
        title_txt =findViewById(R.id.title_text)
        develop_txt =findViewById(R.id.develop)
        imageView.animation=topAnimation
        title_txt.animation=bottomAnimation
        develop_txt.animation=bottomAnimation
        Handler().postDelayed({
            val intent=Intent(this, wellcomeuser::class.java)
            startActivity(intent)
            finish()
        },1500)

    }
}