package com.example.bookhub.UI.startup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.bookhub.UI.Home
import com.example.bookhub.UI.Authentication.Activities.Loginhome
import com.example.bookhub.R
import com.google.firebase.auth.FirebaseAuth

class wellcomeuser : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wellcomeuser)
        val btn: Button =findViewById(R.id.login1)
        val fAuth: FirebaseAuth = FirebaseAuth.getInstance()
        if(fAuth.currentUser !=null && fAuth.currentUser!!.isEmailVerified)
        {
            startActivity(Intent(this, Home::class.java))
            finish()
        }
        btn.setOnClickListener{
            startActivity(Intent(this, Loginhome::class.java))
            finish()
        }
    }
}