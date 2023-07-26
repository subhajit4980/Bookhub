package com.example.bookhub.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.bookhub.Models.BookData
import com.example.bookhub.R
import com.example.bookhub.databinding.ListitemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class TopBookAdapter(
    private val audiolist: ArrayList<BookData>,
    private val listener: OnItemClickListner,
    private val context: Context
) : RecyclerView.Adapter<TopBookAdapter.MyViewHolder>() {

    private val db = Firebase.firestore
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    inner class MyViewHolder(private val itemBinding: ListitemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        private val title: TextView = itemBinding.textv
        private val image: ImageView = itemBinding.image
        private val review:TextView=itemBinding.review
        @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
        fun initialize(item: BookData, action: OnItemClickListner) {
            review.text=item.review+" ⭐"
            if (item.title!!.length > 30) {
                title.text = item.title.toString().substring(0, 31) + "..."
            } else {
                title.text = item.title
            }
            image.setImageResource(R.drawable.openbook)
            val storageRef = FirebaseStorage.getInstance().reference.child("Book/${item.uploder}/${item.title}/pdfImage.jpeg")
            storageRef.downloadUrl.addOnSuccessListener {
                Glide.with(context).load(it).placeholder(R.drawable.openbook).into(image)
            }
            itemView.setOnClickListener { action.onItemClick(item, adapterPosition) }
        }
    }
    interface OnItemClickListner {
        fun onItemClick(item: BookData, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return audiolist.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.initialize(audiolist[position], listener)
    }
}
