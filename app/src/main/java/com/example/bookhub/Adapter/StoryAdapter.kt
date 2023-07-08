package com.example.bookhub.Adapter

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookhub.Adapter.StoryAdapter.*
import com.example.bookhub.R
import com.example.bookhub.Utill.constant
import com.example.bookhub.WriteStory.Read_Post
import com.example.bookhub.data.Story
import com.example.bookhub.databinding.StoryBinding
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView


class StoryAdapter(
    private val storylist: ArrayList<Story>,
    var listener: OnItemClickListner,
    private val context: Context,
) : RecyclerView.Adapter<StoryAdapter.MyviewHolder>() {
    private var fvrtChecker: Boolean = false
    private var images: ImageView? = null
    private var uploadButton: AppCompatButton? = null
    private var postDialog: Dialog? = null
    private var ustorageref: StorageReference? = null
    private var imaguri: Uri? = null
    private var bytedata: ByteArray? = null
    private val GALLERY_REQUEST_CODE = 1234
    private var progressDialog: ProgressDialog? = null
    var image_uri: String? = null
    private val db = Firebase.firestore
    private val currentuserid = FirebaseAuth.getInstance().currentUser!!.uid
    var postid=""
    private lateinit var binding: StoryBinding
    inner class MyviewHolder(binding: StoryBinding) : RecyclerView.ViewHolder(binding.root) {
        private var title: TextView = binding.ptitle
        private var views: TextView = binding.noLikes
        private var img: ImageView = binding.stimg
        var description: TextView = binding.descrip
        val picu: CircleImageView = binding.picu
        val more = binding.more
        val like = binding.like
        val share = binding.share
        val cont = binding.cont
        val no_like = binding.noLikes
        val time=binding.timeago
        val name=binding.name
        val comment=binding.comment
        fun  showmore(binding: StoryBinding)
        {
            binding.descrip.apply {
                setShowingLine(4)
                addShowMoreText(" Read More ")
                addShowLessText(" Hide ")
                setShowMoreColor(Color.RED)
                setShowLessTextColor(Color.RED)
            }
        }
        @SuppressLint("SuspiciousIndentation")
        fun initialize(item: Story, action: OnItemClickListner) {
            title.text = item.title
            description.text = item.decription
            time.text= TimeAgo.using(item.time?:System.currentTimeMillis().toLong().toLong())
            var Userid = item.usid.toString()
             postid=item.postid.toString()
//            author name
            db.collection("users").document(Userid).addSnapshotListener { value, error ->
                name.text=value!!.get("name").toString()
            }
//    post image
            image_uri =
                "bookhub/users/$Userid/posts/story/$postid.jpeg"
            ustorageref = FirebaseStorage.getInstance().reference.child(image_uri!!)
            ustorageref!!.downloadUrl.addOnSuccessListener {
                if (constant.isValidContextForGlide(context)) {
                    Glide.with(context).load(it).into(img)
                }
            }
//    profile image
            val storageref =
                FirebaseStorage.getInstance().reference.child("bookhub/users/$Userid/profile.jpeg")
            storageref.downloadUrl.addOnSuccessListener {
                if (constant.isValidContextForGlide(context)) {
                    Glide.with(context).load(it).placeholder(R.drawable.pp).into(picu)
                }

            }
//              working with more button
            if (Userid.toString() == currentuserid) {
                more.visibility = View.VISIBLE
                if (more.visibility == View.VISIBLE) {
                    more.setOnClickListener {
                        showDialog(item)
                    }
                }
            }

// onclick action
            cont.setOnClickListener { action.onItemClick(item, adapterPosition) }

//                    likes part
            val collection_ref =
                db.collection("story").document(postid).collection("likes")
            val ref = collection_ref.document(currentuserid)
//    listening likes
            val Lik: ImageView = itemView.findViewById(R.id.like)
            like(ref, itemView) {}
            Count_(collection_ref, no_like)
//    update likes
            like.setOnClickListener {
                var check = true
                like(ref, itemView) {
                    if (it && check) {
                        ref.delete().addOnSuccessListener {
                            Lik.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                            Count_(collection_ref, no_like)
                            return@addOnSuccessListener
                        }
                        check = false
                    } else if (check) {
                        ref.set(hashMapOf("like_by" to currentuserid)).addOnSuccessListener {
                            Count_(collection_ref, no_like)
                        }
                        check = false
                    }
                }
            }
//        share
            share.setOnClickListener {
                val message = item.title.toString()+"\n\n"+item.story.toString()
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Story"))
            }
//            comments
            comment.setOnClickListener {
                val intent = Intent(context, Read_Post::class.java)
                intent.putExtra("title", item.title)
                intent.putExtra("story", item.story)
                intent.putExtra("user_id", item.usid)
                intent.putExtra("postid", item.postid)
                intent.putExtra("comment","yes")
                context.startActivity(intent)
            }
            Count_(db.collection("story").document(item.postid.toString()).collection("comments"),binding.noComments)
        }

        //          dialog for edit and delete the post
        @SuppressLint("ResourceType")
        private fun showDialog(item: Story) {
            val dialog: Dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.more_option)
            val edit: TextView = dialog.findViewById(R.id.editPost)
            val delete: TextView = dialog.findViewById(R.id.deletePost)
            edit.setOnClickListener {
                val bundle = bundleOf(
                    "edit" to "edit",
                    "title" to item.title,
                    "des" to item.decription,
                    "story" to item.story,
                    "img" to image_uri.toString(),
                    "postId" to item.postid
                )
                itemView.findNavController()
                    .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
                dialog.dismiss()
            }
            delete.setOnClickListener {
                val DeleteDialog = AlertDialog.Builder(it.context)
                DeleteDialog.setTitle("Do you want to delete the story ?")
                DeleteDialog.setPositiveButton("Yes"){
                    Dialog,which->
                    db.collection("story").document(item.postid.toString()).delete()
                    ustorageref!!.delete()
                    Dialog.dismiss()
                    dialog.dismiss()
                }
                DeleteDialog.setNegativeButton(
                    "No"
                ) { Dialog, which ->
                    // close the dialog
                    Dialog.dismiss()
                    dialog.dismiss()
                }
                DeleteDialog.create().show()
            }
            dialog.show()
            dialog.window!!.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.attributes.windowAnimations = R.style.DialogAnim
            dialog.window!!.setGravity(Gravity.BOTTOM)

        }

    }


    interface OnItemClickListner {
        fun onItemClick(item: Story, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyviewHolder {
        binding = StoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyviewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyviewHolder, position: Int) {
        holder.initialize(storylist[position], listener)
        holder.showmore(binding)
    }

    override fun getItemCount(): Int {
        return storylist.size
    }

    fun like(ref: DocumentReference, itemView: View, callback: (Boolean) -> Unit) {
        val Lik: ImageView = itemView.findViewById(R.id.like)
        ref.addSnapshotListener { value, error ->
            if (value != null && value.exists()) {
                Lik.setImageResource(R.drawable.ic_baseline_favorite_24)
                callback(true)
            } else {
                callback(false)
            }
        }
    }

    fun Count_(collection_ref: CollectionReference, no_: TextView) {
        // Get the initial likes count
        collection_ref.get().addOnSuccessListener { querySnapshot ->
            // Update the likes count
            no_.text = querySnapshot.size().toString()
        }.addOnFailureListener { exception ->
            // Handle any errors
        }

        // Listen for real-time updates to the likes count
        collection_ref.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                // Handle any errors
                return@addSnapshotListener
            }

            // Update the likes count
            if (querySnapshot != null && !querySnapshot.isEmpty) {
                no_.text = querySnapshot.size().toString()
            }
        }
    }
//    fun shareImage(imageFile: File,title:String,story:String) {
//        val shareIntent = Intent().apply {
//            action = Intent.ACTION_SEND_MULTIPLE
////            type = "image/jpeg"
////            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "com.example.bookhub.fileprovider", imageFile))
////            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            putExtra(Intent.EXTRA_TEXT,title+"\n"+story)
//        }
//
//        context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
//    }
//    private fun getmageToShare(bitmap: Bitmap): Uri? {
//        val imagefolder: File = File(getCacheDir(), "images")
//        var uri: Uri? = null
//        try {
//            imagefolder.mkdirs()
//            val file = File(imagefolder, "shared_image.png")
//            val outputStream = FileOutputStream(file)
//            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
//            outputStream.flush()
//            outputStream.close()
//            uri = FileProvider.getUriForFile(this, "com.anni.shareimage.fileprovider", file)
//        } catch (e: Exception) {
//            Toast.makeText(this, "" + e.message, Toast.LENGTH_LONG).show()
//        }
//        return uri
//    }
}