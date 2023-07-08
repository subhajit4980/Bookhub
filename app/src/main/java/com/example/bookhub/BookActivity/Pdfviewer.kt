package com.example.bookhub.BookActivity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.bookhub.R
import com.example.bookhub.Utill.constant
import com.example.bookhub.databinding.ActivityPdfviewerBinding
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class Pdfviewer : AppCompatActivity(){
    var pdfDialog:Dialog?=null
    var pdf_view:PDFView?=null
    private  lateinit var title:String
    private lateinit var author:String
    private lateinit var binding:ActivityPdfviewerBinding
    private var db = Firebase.firestore
    var id=""
    private var isUserInteraction=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPdfviewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        binding.ratingbar.onRatingBarChangeListener = this
        Log.d(TAG, "onCreate: started.")
        incomingIntent()
        binding.back.setOnClickListener{
            finish()
        }
    }

    private fun incomingIntent(){
            Log.d(TAG, "getIncomingIntent: checking for incoming intents.")
            if (intent.hasExtra("image_url") && intent.hasExtra("title")&& intent.hasExtra("pdf_url")) {
                Log.d(TAG, "getIncomingIntent: found intent extras.")
                val imageUrl = intent.getStringExtra("image_url")
                title = intent.getStringExtra("title").toString()
                id=intent.getStringExtra("bookid").toString()
                author=intent.getStringExtra("author").toString()
                val pdfurl=intent.getStringExtra("pdf_url")
                val desc=intent.getStringExtra("description").toString()
                binding.apply {
                    imageDescription.text=title
                    description.apply {
                        text=desc
                        setShowingLine(4)
                        addShowMoreText(" Read More ")
                        addShowLessText(" Hide ")
                        setShowMoreColor(Color.RED)
                        setShowLessTextColor(Color.RED)
                    }

                }
                setImage(imageUrl)
                getRating(title)
                setRating()
                val view_pdf:Button=binding.viewPdf
                view_pdf.setOnClickListener{
                    val intent=Intent(this, PdfReada::class.java)
                    intent.putExtra("pdfname",pdfurl.toString())
                    startActivity(intent)
                }

            }
        }

    @SuppressLint("ClickableViewAccessibility")
    private fun setRating() {
        binding.ratingbar.onRatingBarChangeListener=object :RatingBar.OnRatingBarChangeListener{
            override fun onRatingChanged(p0: RatingBar?, p1: Float, p2: Boolean) {
                if(p2)
                {
                    var rate="$p1"
                    if (p1 < 1) {
                        binding.ratingbar.rating = 1f
                        rate="1"
                    } else {
                        // Perform your actions for a valid rating
                        var check_rating=true
                        val curruser=FirebaseAuth.getInstance().currentUser!!.uid
                        val Ref=db.collection("users").document(curruser).collection("review").document(id)
                        val book_ref=db.collection("Books").document(id)
                        var userpdfrev:String?=null
                        Ref.addSnapshotListener { value, error ->
                            if(value!!.exists())
                            {
                                userpdfrev= value.get("rateing").toString()
                            }
                        }
                        var revCount=0.0F
                        book_ref.addSnapshotListener { value, error ->
                            if(value!!.exists() &&check_rating)
                            {
                                var reviwCount=value.get("review").toString()+"F"
                                revCount= reviwCount.toFloatOrNull()!!
                                var reads=value.get("reads")?:0
                                reads=reads.toString()
                                if(userpdfrev==null) {
                                    var reads_int = reads.toInt()+1
                                    book_ref.update("reads",reads_int.toString())
                                    revCount=revCount+p1
                                    var review=revCount/reads_int
                                    book_ref.update("review",review.toString())
                                    check_rating=false
                                }
                                else{
                                    var changereview=(userpdfrev+"F").toFloatOrNull()
                                    revCount=(revCount*reads.toInt())- changereview!! +p1
                                    var review=revCount/reads.toInt()
                                    book_ref.update("review",review.toString())
                                    check_rating=false
                                }
                                Ref.set(hashMapOf("rateing" to rate.toString())).addOnSuccessListener {
                                    Toast.makeText(applicationContext, "Thanks for your review", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                }
            }
        }
    }

//
//            val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("bookhub/users/$curruser/review/$title")
//            try {
//                var userpdfrev:String?=null
//                val bookrev: DatabaseReference = FirebaseDatabase.getInstance().getReference("bookhub/books/$title->$author/reads")
//                val revcount: DatabaseReference = FirebaseDatabase.getInstance().getReference("bookhub/books/$title->$author/review")
//                ref.addValueEventListener(object :ValueEventListener{
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        try {
//                            if(snapshot.exists())
//                            {
//                                userpdfrev=snapshot.value.toString()
//                            }
//                            else{
//                                userpdfrev=null
//                            }
//                        }catch (e:Exception)
//                        {
//                            Toast.makeText(this@Pdfviewer, e.message, Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        TODO("Not yet implemented")
//                    }
//
//                })
//                var revCount=0.0F
//                revcount.get().addOnCompleteListener {
//                    task->
//                    if (task.isSuccessful)
//                    {
//                        var reviwCount=task.result.value.toString()+"F"
//                        revCount= reviwCount.toFloatOrNull()!!
//                    }
//                }
//                bookrev.get().addOnCompleteListener { task->
//                    if (task.isSuccessful)
//                    {
//                        try{
//                            var reads=task.result.value.toString()
//                            if(userpdfrev==null) {
//                                var reads_int = reads.toInt()+1
//                                bookrev.setValue(reads_int.toString())
//                                revCount=revCount+p1
//                                var review=revCount/reads_int
//                                revcount.setValue(review.toString())
//                            }
//                            else{
//                                var changereview=(userpdfrev+"F").toFloatOrNull()
//                                revCount=(revCount*reads.toInt())- changereview!! +p1
//                                var review=revCount/reads.toInt()
//                                revcount.setValue(review.toString())
//                            }
//                        }catch (e:Exception)
//                        {
//                            if(userpdfrev==null) {
//                                var reads_int =1
//                                bookrev.setValue(reads_int.toString())
//                            }
//                            else{
//                                revcount.setValue(p1.toString())
//                            }
//                        }
//                    }
//                }
//            }
//            catch (e:Exception)
//            {
//                Toast.makeText(this, "some thing wrong", Toast.LENGTH_SHORT).show()
//            }
//            Toast.makeText(this, "Thanks for your review", Toast.LENGTH_SHORT).show()
////            if (rate.toFloatOrNull()!!<1.0f)
////            {
////                ref.setValue(1.0f.toString())
////            }
////            else{
//                ref.setValue(rate.toString())
////            }
    @SuppressLint("ResourceType")
    private fun replaceFragment(fragment:Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.pdfcover,fragment).commit()
    }

    private fun setImage(imageUrl: String?) {
        val name = binding.imageDescription
        val image = binding.image
            val storageref: StorageReference =
                FirebaseStorage.getInstance().reference.child(imageUrl.toString())
            storageref.downloadUrl.addOnSuccessListener {
                if (constant.isValidContextForGlide(this)) {
                    // Load image via Glide lib using contex
                    Glide.with(this)
                        .load(it)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(image)
                }

                binding.progress.visibility= View.GONE
                binding.mainContent.visibility=View.VISIBLE
            }
        }
    companion object {
        private const val TAG = "GalleryActivity"
    }

    @SuppressLint("UseValueOf")
    fun getRating(title:String?)
    {
        val curruser=FirebaseAuth.getInstance().currentUser!!.uid

        val Ref=db.collection("users").document(curruser).collection("review").document(id)
        Ref.addSnapshotListener { value, error ->
            if(value!!.exists())
            {
                val rate_t_s= value.get("rateing").toString()+"F"
                val rate=rate_t_s.toFloatOrNull()
                val rating=binding.ratingbar
                if (rate != null) {
                    rating.rating=rate
                }
            }

        }

//        val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("bookhub/users/$curruser/review/$title")
//        ref.get().addOnCompleteListener (this){ task->
//            if (task.isSuccessful)
//            {
//                val datasnapshot=task.result
//                val rate_t_s= datasnapshot.value.toString()+"F"
//                val rate=rate_t_s.toFloatOrNull()
//                val rating=binding.ratingbar
//                if (rate != null) {
//                    rating.rating=rate
//                }
//
//            }
//        }

    }
}
