package com.example.bookhub.WriteStory


import androidx.navigation.fragment.findNavController
import com.example.bookhub.databinding.FragmentSecondBinding
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.example.bookhub.R
import com.example.bookhub.data.Story
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {
    private var progressDialog: ProgressDialog? = null
    private var imaguri: Uri? = null
    private var bytedata: ByteArray? = null
    var closep: ImageView? = null
    var mstory: TextInputEditText? = null
    var storyName: TextInputEditText? = null
    var disc: TextInputEditText? = null
    var images: ImageView? = null
    var uploadButton: AppCompatButton? = null
    private var _binding: FragmentSecondBinding? = null
    val userid = FirebaseAuth.getInstance().currentUser!!.uid.toString()
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private lateinit var storageref: StorageReference
    var edit = ""
    var postId = ""
    var oldstory_name = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closep.setOnClickListener {
            findNavController().navigateUp()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
        edit = arguments?.getString("edit").toString()
        if (edit == "edit") {
            binding.apply {
                oldstory_name = arguments?.getString("title").toString()
                postId = arguments?.getString("postId").toString()
                storyName.setText(oldstory_name)
                val descrip = arguments?.getString("des").toString()
                disc.setText(descrip)
                val story = arguments?.getString("story").toString()
                mstory.setText(story)
                val img = arguments?.getString("img")
                val ref = FirebaseStorage.getInstance().reference.child(img.toString())
                ref.downloadUrl.addOnSuccessListener {
                    Glide.with(requireActivity()).load(it).into(binding.postimg)
                }
            }
        }
        UI()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun UI() {
        closep = binding.closep
        mstory = binding.mstory
        storyName = binding.storyName
        disc = binding.disc
        images = binding.images
        uploadButton = binding.uploadButton
        progressDialog = ProgressDialog(requireActivity())
//        textlistener(storyName!!)
//        textlistener(mstory!!)
//        textlistener(disc!!)
        images!!.setOnClickListener {
            selectImageFromGallery()
            return@setOnClickListener
        }
        mstory!!.addTextChangedListener(uploadTextwatcher)
        storyName!!.addTextChangedListener(uploadTextwatcher)
        disc!!.addTextChangedListener(uploadTextwatcher)
        uploadButton!!.setOnClickListener {
            Upload()
        }
//        mstory!!.setOnTouchListener { view, event ->
//            view.parent.requestDisallowInterceptTouchEvent(true)
//            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
//                view.parent.requestDisallowInterceptTouchEvent(false)
//            }
//            return@setOnTouchListener false
//        }

    }

    private val uploadTextwatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            val stn = storyName!!.text.toString().trim()
            val dsn = disc!!.text.toString().trim()
            val st = mstory!!.text.toString().trim()
            uploadButton!!.isEnabled = !stn.isEmpty() && !st.isEmpty() && dsn.isNotEmpty()
        }

        override fun afterTextChanged(p0: Editable?) {
        }

    }

    private fun Upload() {
        progressDialog!!.setMessage("Uploading Started..")
        if (validity()) {
            progressDialog!!.show()
            val ref = db.collection("story").document()
            val id = ref.id
            val content = Story(
                storyName!!.text.toString(),
                userid,
                disc!!.text.toString(),
                System.currentTimeMillis().toLong(),
                mstory!!.text.toString(),
                id.toString()
            )
            storageref =
                FirebaseStorage.getInstance().reference.child("bookhub/users/${userid.toString()}/posts/story")
            if ( edit == "edit") {
                storageref = storageref.child(postId + ".jpeg")
                db.collection("story").document(postId).update(
                    hashMapOf(
                        "title" to storyName!!.text.toString(),
                        "decription" to disc!!.text.toString(),
                        "story" to mstory!!.text.toString()
                    ) as Map<String, Any>
                ).addOnSuccessListener {
                    upload_image()
                }.addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Some Error occurred!!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                progressDialog!!.dismiss()
            } else {
                storageref = storageref.child(id + ".jpeg")
                ref.set(content)
                    .addOnSuccessListener {
                        upload_image()
                    }.addOnFailureListener {
                        storageref.delete()
                        Toast.makeText(
                            requireContext(),
                            "Some Error occurred!!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                progressDialog!!.dismiss()
            }
//            }

        }
    }

    @SuppressLint("IntentReset")
    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            imaguri = data.data
            var bmp: Bitmap? = null
            bmp = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imaguri)
            val boas: ByteArrayOutputStream = ByteArrayOutputStream()
            bmp!!.compress(Bitmap.CompressFormat.JPEG, 25, boas)
            bytedata = boas.toByteArray()
            Glide.with(requireActivity()).load(bytedata).into(binding.postimg)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validity(): Boolean {
        if (storyName!!.text!!.isNotEmpty() && disc!!.text!!.isNotEmpty() && mstory!!.text!!.isNotEmpty()) {
            val not = listOf('.', '#', '$', '[', ']')
            for (s in not) {
                if (storyName!!.text.toString().contains(s)) {
                    Toast.makeText(
                        requireContext(),
                        "Story name does not contains ${s.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                "All Fields are required !",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    fun upload_image() {
        if (imaguri != null) {
            storageref.putBytes(bytedata!!).addOnSuccessListener {
                Toast.makeText(requireContext(), "Story Posted Successfully!!", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigateUp()
            }.addOnFailureListener {
                db.collection("story").document(userid + storyName!!.text.toString()).delete()
                Toast.makeText(requireContext(), "Some Error occurred!!", Toast.LENGTH_SHORT).show()

            }
        } else {
            Toast.makeText(requireContext(), "Story Posted Successfully!!", Toast.LENGTH_SHORT)
                .show()
            findNavController().navigateUp()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
//    fun textlistener(T: TextInputEditText) {
//        T.setOnTouchListener { view, motionEvent ->
//            if (view.id == R.id.mstory) {
//                view.parent.requestDisallowInterceptTouchEvent(true)
//                when (motionEvent.action and MotionEvent.ACTION_MASK) {
//                    MotionEvent.ACTION_UP -> {
//                        view.parent.requestDisallowInterceptTouchEvent(false)
//                    }
//                }
//
//            }
//            false
//        }
//    }

    companion object {
        private const val CAMERA = 1
        private const val GALLERY = 2
    }

}