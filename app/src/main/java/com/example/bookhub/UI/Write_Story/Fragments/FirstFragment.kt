package com.example.bookhub.UI.Write_Story.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.bookhub.databinding.FragmentFirstBinding
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookhub.Adapter.StoryAdapter
import com.example.bookhub.R
import com.example.bookhub.Models.Story
import com.example.bookhub.UI.Write_Story.Activities.Read_Post
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Suppress("UNREACHABLE_CODE")
class FirstFragment : Fragment(), StoryAdapter.OnItemClickListner {
    private var _binding: FragmentFirstBinding? = null
    private lateinit var dbref: DatabaseReference
    private lateinit var storyRecyclerView: RecyclerView
    private lateinit var storyArraylist: ArrayList<Story>
    lateinit var adapter: StoryAdapter
    lateinit var mHandler: Handler
    private var postDialog: Dialog? = null
    private var imaguri: Uri? = null
    private var bytedata: ByteArray? = null
    private var mstory: EditText? = null
    private var images: ImageView? = null
    private var disc: TextInputEditText? = null
    private var storyName: TextInputEditText? = null
    private var uploadButton: AppCompatButton? = null
    private val binding get() = _binding!!
    private var fcontext:Context?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        fcontext=requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storyRecyclerView = binding.recyclerView
        storyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        storyRecyclerView.setHasFixedSize(true)
        //        fetch stories
        storyArraylist = arrayListOf<Story>()
        getstories()
        search()
        binding.addpost.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        binding.backmain.setOnClickListener {
            requireActivity().finish()

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
    }

    private fun getstories() {
        Firebase.firestore.collection("story").addSnapshotListener { Datasnapshot, error ->
            storyArraylist.clear()
            if(error!=null)return@addSnapshotListener
            if (Datasnapshot!=null)
            {
                for (doc in Datasnapshot)
                {
                    val story=doc.toObject(Story::class.java)
                    storyArraylist.add(story)
                }
                adapter = StoryAdapter(storyArraylist, this@FirstFragment, fcontext!!)
                storyRecyclerView.adapter = adapter
            }
        }

//        dbref = FirebaseDatabase.getInstance().getReference("bookhub/writeStory")
//        dbref.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                storyArraylist.clear()
//                try {
//                    if (snapshot.exists()) {
//                        for (storySnapshot in snapshot.children) {
//                            val story = storySnapshot.getValue(Story::class.java)!!
//                            storyArraylist.add(story)
//                        }
//                        adapter = StoryAdapter(storyArraylist, this@FirstFragment, requireContext())
//                        storyRecyclerView.adapter = adapter
//                    }
//                } catch (e: Exception) {
//                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//        })
    }

    @SuppressLint("DefaultLocale")
    private fun filter(e: String) {
        val filteritem = ArrayList<Story>()
        for (item in storyArraylist) {
            if (item.title!!.toLowerCase().contains(e.toLowerCase())) {
                filteritem.add(item)
            }
        }
        storyRecyclerView.adapter = StoryAdapter(filteritem, this, requireContext())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(item: Story, position: Int) {
        val intent = Intent(requireContext(), Read_Post::class.java)
        intent.putExtra("title", item.title)
        intent.putExtra("story", item.story)
        intent.putExtra("user_id", item.usid)
        intent.putExtra("postid", item.postid)
        startActivity(intent)
    }

    private fun search() {
        binding.searchStory.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            @SuppressLint("DefaultLocale")
            override fun onQueryTextSubmit(querry: String?): Boolean {
                binding.searchStory.clearFocus()
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

}