package com.example.bookhub.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookhub.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.makeramen.roundedimageview.RoundedImageView
import android.content.Context
import android.net.ConnectivityManager
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.bookhub.Utill.constant.isValidContextForGlide
import com.example.bookhub.data.BookData
import com.example.bookhub.databinding.DataBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class Bookmark_Adapter(
    private val booklist: ArrayList<BookData>,
    var listener: OnItemClickListner,
    private val context: Context
) : RecyclerView.Adapter<Bookmark_Adapter.MyVIewHolder>() {
    //    private val context: Context
    private var selectedItemPosition: Int = 0
    private var fvrtChecker: Boolean = false
    private var db = Firebase.firestore

    inner class MyVIewHolder(binding: DataBinding) : RecyclerView.ViewHolder(binding.root) {
        private val title: TextView = binding.title
        private val author: TextView = binding.author
        private val review: TextView = binding.review
        private val reader: TextView = binding.reader
        private val book_img: RoundedImageView = binding.image
        val bookmark = binding.bookmark

        //        @SuppressLint("ResourceType")
        private var pdfname: String? = null

        @SuppressLint("UseCompatLoadingForDrawables", "SuspiciousIndentation")
        fun initialize(item: BookData, action: OnItemClickListner) {
            title.text = item.title
            author.text = item.author
            review.text =String.format("%.1f", item.review!!.toDouble())
            reader.text = item.reads
            pdfname = item.pdfurl.toString()
            val ref: StorageReference =
                FirebaseStorage.getInstance().reference.child("Book/${item.uploder}/${item.title}/pdfImage.jpeg")
            ref.downloadUrl.addOnSuccessListener {
                if (isValidContextForGlide(context)) {
                    // Load image via Glide lib using contex
                    Glide.with(context).load(it).diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(book_img)
                }
            }
            itemView.setOnClickListener { action.onItemClick(item, adapterPosition) }
            //BOOKMARK PART Start
//            val fav_ref=db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("bookmark").document("favourites")
            val fav_list =
                db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .collection("bookmark")
            val BM: ImageView = itemView.findViewById(R.id.bookmark)
            val Ref = fav_list.document(item.bookid.toString())
            BM.setOnClickListener {
                fvrtChecker = true
                Ref.addSnapshotListener { d, e ->
                    if (d!!.exists() && fvrtChecker) {
                        Ref.delete()
//                        val BM:ImageView=itemView.findViewById(R.id.bookmark)
                        BM.setImageResource(R.drawable.ic_baseline_bookmark_add_24)
                        Toast.makeText(context, "Bookmark removed successfully", Toast.LENGTH_SHORT)
                            .show()
                        fvrtChecker = false
                    } else if (fvrtChecker) {
                        Ref.set(item)
//                        val BM:ImageView=itemView.findViewById(R.id.bookmark)
                        BM.setImageResource(R.drawable.ic_baseline_bookmark_added_24)
                        Toast.makeText(context, "Bookmark added successfully", Toast.LENGTH_SHORT)
                            .show()
                        fvrtChecker = false
                    }
                }
            }
            Ref.addSnapshotListener { value, error ->
                if (value!!.exists()) {
                    BM.setImageResource(R.drawable.ic_baseline_bookmark_added_24)
                }
            }
//      BOOKMARK PART END
        }

    }


    interface OnItemClickListner {
        fun onItemClick(item: BookData, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyVIewHolder {
        val binding = DataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyVIewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyVIewHolder, position: Int) {
        holder.initialize(booklist[position], listener)
    }

    override fun getItemCount(): Int {
        return booklist.size
    }

    fun isConnectedToInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

}