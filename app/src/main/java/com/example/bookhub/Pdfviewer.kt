package com.example.bookhub

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.wifi.WifiConfiguration.AuthAlgorithm.strings
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_pdfviewer.*
import kotlinx.android.synthetic.main.data.*
//import kotlinx.android.synthetic.main.activity_pdfviewer.*
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class Pdfviewer : AppCompatActivity() ,RatingBar.OnRatingBarChangeListener{
    var pdfDialog:Dialog?=null
    var pdf_view:PDFView?=null
    private  lateinit var title:String
    private lateinit var author:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfviewer)
        ratingbar.onRatingBarChangeListener = this
        Log.d(TAG, "onCreate: started.")
        incomingIntent
        back.setOnClickListener{
            finish()
        }
    }

    private val incomingIntent: Unit
        get() {
            Log.d(TAG, "getIncomingIntent: checking for incoming intents.")
            if (intent.hasExtra("image_url") && intent.hasExtra("title")&& intent.hasExtra("pdfname")) {
                Log.d(TAG, "getIncomingIntent: found intent extras.")
                val imageUrl = intent.getStringExtra("image_url")
                title = intent.getStringExtra("title").toString()
                author=intent.getStringExtra("author").toString()
                val pdfurl=intent.getStringExtra("pdfname")
                setImage(imageUrl, title)
                setRating(title)
                val view_pdf:Button=findViewById(R.id.view_pdf)
                view_pdf.setOnClickListener{
                    val intent=Intent(this,PdfReada::class.java)
                    intent.putExtra("pdfname",pdfurl.toString())
                    startActivity(intent)

                }

            }
        }

    @SuppressLint("ResourceType")
    private fun replaceFragment(fragment:Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.pdf12,fragment).commit()
    }

    private fun setImage(imageUrl: String?, imageName: String?) {
        Log.d(TAG, "setImage: setting te image and name to widgets.")
        val name = findViewById<TextView>(R.id.image_description)
        name.text = imageName
        val image = findViewById<ImageView>(R.id.image)
        if(imageUrl==""){
            val storageref: StorageReference =
                FirebaseStorage.getInstance().reference.child("bookhub/Books_image/${imageName}.jpeg")
            storageref.downloadUrl.addOnSuccessListener {
                Glide.with(this)
                    .load(it)
                    .into(image)
            }
        }
        else
        {
            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(image)
        }

    }

    companion object {
        private const val TAG = "GalleryActivity"
    }

    override fun onRatingChanged(p0: RatingBar?, p1: Float, p2: Boolean) {
        val rate="$p1"
        findViewById<Button>(R.id.rateingButton).setOnClickListener{
            val curruser=FirebaseAuth.getInstance().currentUser!!.uid
            val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("bookhub/users/$curruser/review/$title")
            try {
                var userpdfrev:String?=null
                val bookrev: DatabaseReference = FirebaseDatabase.getInstance().getReference("bookhub/books/$title->$author/reads")
                val revcount: DatabaseReference = FirebaseDatabase.getInstance().getReference("bookhub/books/$title->$author/review")
                ref.addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            if(snapshot.exists())
                            {
                                userpdfrev=snapshot.value.toString()
                            }
                            else{
                                userpdfrev=null
                            }
                        }catch (e:Exception)
                        {
                            Toast.makeText(this@Pdfviewer, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
                var revCount=0.0F
                revcount.get().addOnCompleteListener {
                    task->
                    if (task.isSuccessful)
                    {
                        var reviwCount=task.result.value.toString()+"F"
                        revCount= reviwCount.toFloatOrNull()!!
                    }
                }
                bookrev.get().addOnCompleteListener { task->
                    if (task.isSuccessful)
                    {
                        try{
                            var reads=task.result.value.toString()
                            if(userpdfrev==null) {
                                var reads_int = reads.toInt()+1
                                bookrev.setValue(reads_int.toString())
                                revCount=revCount+p1
                                var review=revCount/reads_int
                                revcount.setValue(review.toString())
                            }
                            else{
                                var changereview=(userpdfrev+"F").toFloatOrNull()
                                revCount=(revCount*reads.toInt())- changereview!! +p1
                                var review=revCount/reads.toInt()
                                revcount.setValue(review.toString())
                            }
                        }catch (e:Exception)
                        {
                            if(userpdfrev==null) {
                                var reads_int =1
                                bookrev.setValue(reads_int.toString())
                            }
                            else{
                                revcount.setValue(p1.toString())
                            }
                        }
                    }
                }
            }
            catch (e:Exception)
            {
                Toast.makeText(this, "some thing wrong", Toast.LENGTH_SHORT).show()
            }
            Toast.makeText(this, "Thanks for your review", Toast.LENGTH_SHORT).show()
//            if (rate.toFloatOrNull()!!<1.0f)
//            {
//                ref.setValue(1.0f.toString())
//            }
//            else{
                ref.setValue(rate.toString())
//            }
        }
    }
    @SuppressLint("UseValueOf")
    fun setRating(title:String?)
    {
        val curruser=FirebaseAuth.getInstance().currentUser!!.uid
        val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("bookhub/users/$curruser/review/$title")
        ref.get().addOnCompleteListener (this){ task->
            if (task.isSuccessful)
            {
                val datasnapshot=task.result
                val rate_t_s= datasnapshot.value.toString()+"F"
                val rate=rate_t_s.toFloatOrNull()
                val rating=findViewById<RatingBar>(R.id.ratingbar)
                if (rate != null) {
                    rating.rating=rate
                }

            }
        }

    }

}
