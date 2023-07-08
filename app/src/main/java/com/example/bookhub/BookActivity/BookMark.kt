package com.example.bookhub.BookActivity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookhub.Adapter.Bookmark_Adapter
import com.example.bookhub.data.BookData
import com.example.bookhub.databinding.BookmarkBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class BookMark : AppCompatActivity(),Bookmark_Adapter.OnItemClickListner {
    private lateinit var dbref:DatabaseReference
    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var booksArrayList: ArrayList<BookData>
    private var db = Firebase.firestore
    lateinit var adapter: Bookmark_Adapter
    private lateinit var binding:BookmarkBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=BookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        booksRecyclerView=binding.rc
        booksRecyclerView.layoutManager= LinearLayoutManager(this)
        booksRecyclerView.setHasFixedSize(true)
        booksArrayList= arrayListOf<BookData>()
        getBooksData()
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            @SuppressLint("DefaultLocale")
            override fun onQueryTextSubmit(querry: String?): Boolean {
                binding.search.clearFocus()
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
        binding.back.setOnClickListener{
            finish()
        }
    }

    private fun getBooksData() {
        val emptyView = binding.viewEmpty
        db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("bookmark").addSnapshotListener{
            v,e->
            val bookmark= arrayListOf<String>()
            if (v != null) {
                for(doc in v) {
                    bookmark.add(doc.id)
                }
                db.collection("Books").addSnapshotListener { snapshot, error ->
                    booksArrayList.clear()
                    if(!snapshot!!.isEmpty)
                    {
                        for(doc in snapshot)
                        {
                            if(bookmark.contains(doc.id)){
                                val book = doc.toObject(BookData::class.java)
                                booksArrayList.add(book)
                            }
                        }
                        adapter = Bookmark_Adapter(booksArrayList, this, this)
                        booksRecyclerView.adapter = adapter
                        emptyView.root.visibility=View.GONE
                    }
                }
            }else{
                emptyView.root.visibility=View.VISIBLE
            }
        }

    }
    @SuppressLint("DefaultLocale")
    private fun filter(e:String)
    {
        val filteritem=ArrayList<BookData>()
        for(item in booksArrayList){
            if (item.title!!.toLowerCase().contains(e.toLowerCase()))
            {
                filteritem.add(item)
            }
        }
        booksRecyclerView.adapter = Bookmark_Adapter(filteritem,this,this)
    }
    @SuppressLint("DefaultLocale")
    private fun submitFliter(querry : String){
        var pdftitle=ArrayList<String>()
        for (item in booksArrayList)
        {
            pdftitle.add(item.title!!.toLowerCase())
        }
        try {
            if (pdftitle.contains(querry.toLowerCase())) {
                filter(querry)
            }
        }
        catch(e:Exception){
        }
    }
    override fun onItemClick(item: BookData, position: Int) {
        val intent = Intent(this, Pdfviewer::class.java)
        intent.putExtra("title", item.title)
        intent.putExtra("image_url", item.imgurl)
        intent.putExtra("pdf_url", item.pdfurl)
        intent.putExtra("author", item.author)
        intent.putExtra("bookid",item.bookid)
        startActivity(intent)
    }
}