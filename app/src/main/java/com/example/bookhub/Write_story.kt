package com.example.bookhub

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.bookhub.Adapter.MyAdapter
import com.example.bookhub.Adapter.StoryAdapter
//import com.example.bookhub.Adapter.StoryAdapter
import com.example.bookhub.R
import com.example.bookhub.data.AudioData
import com.example.bookhub.data.Story
import com.example.bookhub.data.books
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_book.*
import kotlinx.android.synthetic.main.activity_write_story.*
import kotlinx.android.synthetic.main.slideview.*
import kotlinx.android.synthetic.main.story.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class Write_story : AppCompatActivity(), StoryAdapter.OnItemClickListner {
    private lateinit var dbref: DatabaseReference
    private lateinit var storyRecyclerView: RecyclerView
    private lateinit var storyArraylist: ArrayList<Story>
    lateinit var adapter: StoryAdapter
    lateinit var mHandler: Handler
    private var imaguri:Uri?=null
    private var bytedata:ByteArray?=null
    private val selectImage=registerForActivityResult(ActivityResultContracts.GetContent()){
//        imaguri=it
        val resultUri = it
        imaguri=resultUri
        var bmp: Bitmap?=null
        try {
            bmp= MediaStore.Images.Media.getBitmap(getContentResolver(),resultUri)
        }
        catch (e: IOException)
        {
            e.printStackTrace()
        }
        val boas: ByteArrayOutputStream = ByteArrayOutputStream()
        bmp!!.compress(Bitmap.CompressFormat.JPEG,25,boas)
        bytedata=boas.toByteArray()
    }
private var progressDialog: ProgressDialog? = null
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_story)
        storyRecyclerView=findViewById(R.id.recyclerView)
        storyRecyclerView.layoutManager=LinearLayoutManager(this)
        storyRecyclerView.setHasFixedSize(true)
        progressDialog= ProgressDialog(this)
        try {
            images.setOnClickListener {
                selectImage.launch("image/*")
            }
            mstory.addTextChangedListener(uploadTextwatcher)
            uploadButton.setOnClickListener {
                uploadImage()
            }

        }catch (e:Exception)
        {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    addpost.setOnClickListener {
        show.visibility=View.GONE
        post.visibility=View.VISIBLE
    }
        closep.setOnClickListener {
            show.visibility=View.VISIBLE
            post.visibility=View.GONE
        }
//        fetch stories
        storyArraylist= arrayListOf<Story>()
        getstories()
        picnav.setOnClickListener {
            finish()
        }
        searchStory.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            @SuppressLint("DefaultLocale")
            override fun onQueryTextSubmit(querry: String?): Boolean {
                search.clearFocus()
                if (querry != null) {
                    submitFliter(querry)
                }
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    filter(p0)
                }
                return false
            }
        })
    }
private val uploadTextwatcher=object :TextWatcher{
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        val stn=storyName.text.toString().trim()
        val dsn=disc.text.toString().trim()
        val st=mstory.text.toString().trim()
        uploadButton.isEnabled=!stn.isEmpty()&&!st.isEmpty()&&dsn.isNotEmpty()
    }

    override fun afterTextChanged(p0: Editable?) {
    }

}
    private fun uploadImage() {

                if (storyName.text.toString()!="" && disc.text.toString()!="" ) {
                    val not = listOf('.', '#', '$', '[', ']')
                    var check:Boolean=false
                    for (s in not){
                        if (storyName.text.toString().contains(s))
                        {
                            Toast.makeText(this, "Story name does not contains ${s.toString()}", Toast.LENGTH_SHORT).show()
                            check=true
                        }
                    }
                    if (check==false) {
                        val userid=FirebaseAuth.getInstance().currentUser!!.uid.toString()
                        if(imaguri!=null){
                            val storageref: StorageReference =
                                FirebaseStorage.getInstance().reference.child("bookhub/users/${userid.toString()}/posts/story/${userid.toString() + "_" + storyName.text.toString()}/image.jpeg")
                            storageref.putBytes(bytedata!!).addOnSuccessListener {
                                storageref.downloadUrl.addOnSuccessListener {

                                }
                            }.addOnFailureListener {
                                Toast.makeText(this, "error in uploading image", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        progressDialog!!.setMessage("Uploading Started")
                        progressDialog!!.show()
                        val Userid = FirebaseAuth.getInstance().currentUser!!.uid.toString()
                        var name: String=""
                        var refuser: DatabaseReference =
                            FirebaseDatabase.getInstance().getReference("bookhub/users")
                                .child(Userid)
                        refuser.get().addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val dataSnapshot = task.result
                                name = dataSnapshot.child("name").value.toString()
                            } else {
                                Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        val ref: DatabaseReference =
                            FirebaseDatabase.getInstance().getReference("bookhub")
                        val audiop = Story(
                            storyName.text.toString(),FirebaseAuth.getInstance().currentUser!!.uid.toString(),
                            name.toString(), disc.text.toString(),
                            "0",
                            mstory.text.toString()
                        )
                        ref.child("writeStory")
                            .child(Userid.toString() + "_" + storyName.text.toString())
                            .setValue(audiop)
                            .addOnSuccessListener {
                                storyName.text!!.clear()
                                disc.text!!.clear()
                                mstory.text.clear()
                                progressDialog!!.dismiss()
                                Toast.makeText(this, "Your story Uploaded", Toast.LENGTH_SHORT)
                                    .show()
                                show.visibility = View.VISIBLE
                                post.visibility = View.GONE

                            }
                } else {
                    Toast.makeText(
                        this,
                        "All fields are required !",
                        Toast.LENGTH_SHORT
                    ).show()
                }

        }
    }


    @SuppressLint("DefaultLocale")
    private fun filter(e: String) {
        val filteritem = ArrayList<Story>()
        for (item in storyArraylist) {
            if (item.title!!.toLowerCase().contains(e.toLowerCase())) {
                filteritem.add(item)
            }
        }
        storyRecyclerView.adapter = StoryAdapter(filteritem, this, this)
    }

    @SuppressLint("DefaultLocale")
    private fun submitFliter(querry: String) {
        var pdftitle = ArrayList<String>()
        for (item in storyArraylist) {
            pdftitle.add(item.title!!.toLowerCase())
        }
        try {
            if (pdftitle.contains(querry.toLowerCase())) {
                filter(querry)
            }
        } catch (e: Exception) {
        }
    }

    private fun getstories() {
        dbref = FirebaseDatabase.getInstance().getReference("bookhub/writeStory")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                storyArraylist.clear()
                try {
                    if (snapshot.exists()) {
                        for (storySnapshot in snapshot.children) {
                            val story = storySnapshot.getValue(Story::class.java)!!
                            storyArraylist.add(story)
                        }
                        adapter = StoryAdapter(storyArraylist, this@Write_story, this@Write_story)
                        storyRecyclerView.adapter = adapter
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@Write_story, e.message, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })    }

    override fun onItemClick(item: Story, position: Int) {
    }
}