package com.example.bookhub.Adapter

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookhub.R
import com.example.bookhub.Utill.constant
import com.example.bookhub.data.Comment
import com.example.bookhub.databinding.CommentsBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class CommentAdapter(
    options: FirestoreRecyclerOptions<Comment>, val context: Context,
    val editText: EditText,
    val scrollView: NestedScrollView
) :
    FirestoreRecyclerAdapter<Comment, CommentAdapter.ViewHolder>(options) {
    private val db = Firebase.firestore
    private val currentuserid = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var commentView:TextView
    private lateinit var binding: CommentsBinding
     var check_edit=false
    lateinit var edit_item:Comment
    class ViewHolder(val binding: CommentsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
         binding = CommentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Comment) {

        var Userid = model.usid.toString()
        commentView=holder.binding.comments
        holder.binding.apply {
            comments.apply {
                text = model.comment
                setShowingLine(3)
                addShowMoreText(" Read More ")
                addShowLessText(" Hide ")
                setShowMoreColor(Color.RED)
                setShowLessTextColor(Color.RED)
            }
            timeago.text = TimeAgo.using(model.time ?: System.currentTimeMillis().toLong().toLong())

//    profile image
            val storageref =
                FirebaseStorage.getInstance().reference.child("bookhub/users/$Userid/profile.jpeg")
            storageref.downloadUrl.addOnSuccessListener {
                if (constant.isValidContextForGlide(context)) {
                    Glide.with(context).load(it).placeholder(R.drawable.pp).into(picu)
                }
            }
            db.collection("users").document(model.usid.toString())
                .addSnapshotListener { value, error ->
                    if (value!!.exists()) {
                        name.text = value!!.get("name").toString()
                    }
                }
//              working with more button
            if (Userid.toString() == currentuserid) {
                more.visibility = View.VISIBLE
                if (more.visibility == View.VISIBLE) {
                    more.setOnClickListener {
                        showDialog(model)
                    }
                }
            }
        }
        holder.itemView.setOnClickListener {
            val snapshot=snapshots.getSnapshot(position)
            listener?.onItemClick(snapshot,position)

        }
    }

    // dialog for edit and delete the post
    @SuppressLint("ResourceType")
    private fun showDialog(item: Comment) {
        val dialog: Dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.more_option)
        val edit: TextView = dialog.findViewById(R.id.editPost)
        val delete: TextView = dialog.findViewById(R.id.deletePost)
//        edit the comment
        edit.setOnClickListener {
                editText.requestFocus()
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                scrollView.post {
                    scrollView.scrollTo(binding.more.bottom, editText.bottom)
                }
            check_edit=true
            edit_item=item
            editText.setText(item.comment)
            dialog.dismiss()
        }
//        delete the comment
        delete.setOnClickListener {
            db.collection("story").document(item.postid.toString()).collection("comments").document(item.commentid.toString()).delete().addOnSuccessListener {
                Toast.makeText(context, "Okay", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(
                    context,
                    "nope",
                    Toast.LENGTH_SHORT
                ).show() }
            dialog.dismiss()
        }
        dialog.show()
        dialog.window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnim
        dialog.window!!.setGravity(Gravity.BOTTOM)

    }

    interface OnItemClickListener{
        fun onItemClick(documentSnapshot: DocumentSnapshot,position: Int)
    }
    private var listener:OnItemClickListener?=null
    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener=listener
    }

}