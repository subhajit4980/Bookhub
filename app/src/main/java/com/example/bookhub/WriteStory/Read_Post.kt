package com.example.bookhub.WriteStory

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookhub.Adapter.CommentAdapter
import com.example.bookhub.data.BookData
import com.example.bookhub.data.Comment
import com.example.bookhub.databinding.ActivityReadPostBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

@SuppressLint("StaticFieldLeak")
private lateinit var binding: ActivityReadPostBinding

class Read_Post : AppCompatActivity() {
    private var db = Firebase.firestore
    private lateinit var RecyclerView: RecyclerView
    private lateinit var ArrayList: ArrayList<BookData>
    var postid = ""
    private lateinit var adapter: CommentAdapter
    private lateinit var collectionRef: CollectionReference
    private lateinit var comment: Comment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val title = intent.getStringExtra("title")
        val story = intent.getStringExtra("story")
        val user_id = intent.getStringExtra("user_id")
        postid = intent.getStringExtra("postid").toString()
        val comment = intent.getStringExtra("comment").toString()
        collectionRef = db.collection("story").document(postid).collection("comments")
        if (comment == "yes") {
            focus_Comment()
        }
        binding.title.text = title.toString()
        binding.story.text = (story.toString())
        val ustorageref =
            FirebaseStorage.getInstance().reference.child("bookhub/users/$user_id/posts/story/$postid.jpeg")
        ustorageref.downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).into(binding.img)
        }
        binding.back.setOnClickListener {
            finish()
        }
        Get_comment()
        binding.send.setOnClickListener {
            upload_comment()
        }
    }

    fun Get_comment() {
        val query = collectionRef.orderBy("time", Query.Direction.DESCENDING)
        val recyclerOptions =
            FirestoreRecyclerOptions.Builder<Comment>().setQuery(query, Comment::class.java).build()
        adapter = CommentAdapter(recyclerOptions, this, binding.typcom, binding.scrollView)
        adapter.setOnItemClickListener(object : CommentAdapter.OnItemClickListener {
            override fun onItemClick(documentSnapshot: DocumentSnapshot, position: Int) {

            }

        })
        val w_RecyclerView = binding.rc
        w_RecyclerView.layoutManager = LinearLayoutManager(this)
        w_RecyclerView.adapter = adapter
        adapter.startListening()
    }

    fun upload_comment() {
        if (binding.typcom.text.isNotEmpty()) {
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val currentTimeMillis = System.currentTimeMillis().toLong()
            val currentSecond = currentTimeMillis / 1000

            if (adapter.check_edit) {
                val edit_item = adapter.edit_item
                collectionRef.document(edit_item.commentid.toString())
                    .update(hashMapOf("comment" to binding.typcom.text.toString()) as Map<String, Any>).addOnSuccessListener {
                        Toast.makeText(this, "your comment Updated", Toast.LENGTH_SHORT).show()
                        binding.typcom.text.clear()
                    }.addOnFailureListener {
                        Toast.makeText(this, "your comment not Updated", Toast.LENGTH_SHORT).show()
                    }
                adapter.check_edit = false
            } else {
                comment = Comment(
                    binding.typcom.text.toString(),
                    uid,
                    currentTimeMillis,
                    uid + currentSecond.toString(),
                    postid
                )
                collectionRef.document(uid + currentSecond.toString()).set(comment)
                    .addOnSuccessListener {
                        Toast.makeText(this, "your comment posted", Toast.LENGTH_SHORT).show()
                        binding.typcom.text.clear()
                    }.addOnFailureListener {
                        Toast.makeText(this, "your comment not posted", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "Please Type your comments", Toast.LENGTH_SHORT).show()
        }


    }

    fun focus_Comment() {
        binding.typcom.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.typcom, InputMethodManager.SHOW_IMPLICIT)
        binding.scrollView.post {
            binding.scrollView.scrollTo(0, binding.typcom.autoLinkMask)
        }
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}