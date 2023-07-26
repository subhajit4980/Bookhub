package com.example.bookhub.Adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.bumptech.glide.Glide
import com.example.bookhub.Models.AudioData
import com.example.bookhub.R
import com.example.bookhub.UI.Audio_story.Audio_story_activity
import com.example.bookhub.Utils.common.showSnackBar
import com.example.bookhub.Utils.getUserId
import com.example.bookhub.ViewModel.ViewModel
import com.example.bookhub.databinding.AudioItemBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class AudioAdapter(private val audiolist:ArrayList<AudioData>, var listener: Audio_story_activity, private val context: Activity, private val viewModel: ViewModel):
    RecyclerView.Adapter<AudioAdapter.MyVIewHolder> () {
    //    private val context: Context
    private val db = Firebase.firestore
    private  lateinit var binding:AudioItemBinding
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    inner class MyVIewHolder(_binding: AudioItemBinding) : RecyclerView.ViewHolder(_binding.root) {
        private val title: TextView = (_binding.title)
        private val author: TextView = (_binding.author)
        private val review: TextView = (_binding.review)

        @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
        fun initialize(item: AudioData, action: Audio_story_activity) {
            title.text = item.title
            binding.duration.text=item.time
            db.collection("users").document(item.usid.toString()).addSnapshotListener { value, error ->
                author.text=value!!.get("name").toString()
            }
            val storageref=FirebaseStorage.getInstance().reference.child("bookhub/AudioStroy/image/${item.postid}.jpeg")
            storageref.downloadUrl.addOnSuccessListener {
                Glide.with(context).load(it).placeholder(R.drawable.music).into(binding.imageView1)
            }
//            delete item
            if(item.usid== getUserId()){
                binding.delete.visibility= View.VISIBLE
                binding.delete.setOnClickListener {
                    showAlertDialog(item)
                }
            }

//            like count
            binding.like.text=item.likedUserIds.size.toString()
            itemView.setOnClickListener { action.onItemClick(item, adapterPosition) }
        }

    }

    interface OnItemClickListner {
            fun onItemClick(item: AudioData,position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioAdapter.MyVIewHolder {
        binding= AudioItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  MyVIewHolder(binding)
    }
    override fun getItemCount(): Int {
        return audiolist.size
    }

    override fun onBindViewHolder(holder: MyVIewHolder, position: Int) {
        holder.initialize(audiolist[position],listener)
    }

    private fun showAlertDialog(item: AudioData) {
        MaterialAlertDialogBuilder(context)
            .setIcon(R.drawable.alart)
            .setTitle("Delete the story")
            .setMessage("Are you sure you want to delete this story")
            .setPositiveButton("OK") { dialog, which ->
                viewModel.DeleteData(item.postid!!, getUserId(),true){
                    storageRef.child("bookhub/AudioStroy/${item.postid}.mp3").delete()
                    storageRef.child("bookhub/AudioStroy/image/${item.postid}.jpeg").delete()
                    showSnackBar(it,context.findViewById(android.R.id.content))
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // Negative button clicked
                dialog.dismiss()
            }
            .show()
    }
}

