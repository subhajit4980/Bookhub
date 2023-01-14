package com.example.bookhub.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookhub.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.makeramen.roundedimageview.RoundedImageView
import android.content.Context
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.bookhub.data.AudioData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.data.view.*
class AudioAdapter(private  val audiolist:ArrayList<AudioData>, var listener: OnItemClickListner, private val context: Context):
    RecyclerView.Adapter<AudioAdapter.MyVIewHolder> () {
    //    private val context: Context
    inner class MyVIewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title1)
        private val author: TextView = itemView.findViewById(R.id.author1)
        private val review: TextView = itemView.findViewById(R.id.review1)
        private val disc: TextView = itemView.findViewById(R.id.discrip)
        private val reader: TextView = itemView.findViewById(R.id.reader1)
        private val book_img: RoundedImageView = itemView.findViewById(R.id.image)
        private var pdfname: String? = null

        @SuppressLint("UseCompatLoadingForDrawables")
        fun initialize(item: AudioData, action: OnItemClickListner) {
            title.text = item.title
            author.text = item.author
            review.text = item.review
            reader.text = item.reads
            disc.text=item.discription
            pdfname = item.pdfname.toString()
//            val storageref: StorageReference =
//                FirebaseStorage.getInstance().reference.child("bookhub/Books_image/${item.title}.jpeg")
//            storageref.downloadUrl.addOnSuccessListener {
//                Glide.with(context).load(it).into(book_img)
//            }
            itemView.setOnClickListener { action.onItemClick(item, adapterPosition) }
        }

    }

    interface OnItemClickListner {
            fun onItemClick(item: AudioData,position: Int)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioAdapter.MyVIewHolder {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.audiodata,parent,false)
        return  MyVIewHolder(itemView)
    }
    override fun getItemCount(): Int {
        return audiolist.size
    }

    override fun onBindViewHolder(holder: MyVIewHolder, position: Int) {
        holder.initialize(audiolist[position],listener)
    }

//    override fun onBindViewHolder(holder: MyAdapter.MyVIewHolder, position: Int) {
//        holder.initialize(audiolist[position],listener1)
//    }
}

