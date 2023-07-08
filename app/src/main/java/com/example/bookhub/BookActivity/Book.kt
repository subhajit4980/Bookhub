package com.example.bookhub.BookActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookhub.Adapter.Bookmark_Adapter
import com.example.bookhub.data.BookData
import com.example.bookhub.databinding.ActivityBookBinding
import com.google.firebase.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class Book : AppCompatActivity(),Bookmark_Adapter.OnItemClickListner {
    private lateinit var dbref: DatabaseReference
    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var booksArrayList: ArrayList<BookData>
    lateinit var adapter: Bookmark_Adapter
    lateinit var mHandler: Handler
    private lateinit var binding: ActivityBookBinding
    private var db = Firebase.firestore
    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        booksRecyclerView = binding.rc
        booksRecyclerView.layoutManager = LinearLayoutManager(this)
        booksRecyclerView.setHasFixedSize(true)
        booksArrayList = arrayListOf<BookData>()
        getBooksData()
        binding.back.setOnClickListener {
            finish()
        }
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
    }
    private fun getBooksData() {
        db.collection("Books").addSnapshotListener { snapshot, error ->
            booksArrayList.clear()
            if(!snapshot!!.isEmpty)
            {
                for(doc in snapshot)
                {
                    val book = doc.toObject(BookData::class.java)
                    booksArrayList.add(book)
                }
                adapter = Bookmark_Adapter(booksArrayList, this@Book, this@Book)
                booksRecyclerView.adapter = adapter
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun filter(e: String) {
        val filteritem = ArrayList<BookData>()
        for (item in booksArrayList) {
            if (item.title!!.toLowerCase().contains(e.toLowerCase())) {
                filteritem.add(item)
            }
        }
        booksRecyclerView.adapter = Bookmark_Adapter(filteritem, this, this)
    }

    @SuppressLint("DefaultLocale")
    private fun submitFliter(querry: String) {
        var pdftitle = ArrayList<String>()
        for (item in booksArrayList) {
            pdftitle.add(item.title!!.toLowerCase())
        }
        try {
            if (pdftitle.contains(querry.toLowerCase())) {
                filter(querry)
            }
        } catch (e: Exception) {
        }
    }



    override fun onItemClick(item: BookData, position: Int) {
        val intent = Intent(this, Pdfviewer::class.java)
        intent.putExtra("title", item.title)
        intent.putExtra("image_url", item.imgurl)
        intent.putExtra("pdf_url", item.pdfurl)
        intent.putExtra("author", item.author)
        intent.putExtra("bookid",item.bookid)
        intent.putExtra("description",item.description)
        startActivity(intent)

    }

//    override fun onItemClick(item: DocumentSnapshot, position: Int) {
//        var Pdf_image: String = ""
//        val intent = Intent(this, Pdfviewer::class.java)
//        intent.putExtra("title", item.get("title").toString())
//        intent.putExtra("pdf_url", item.get("pdfurl").toString())
//        intent.putExtra("image_url", item.get("imgurl").toString())
//        intent.putExtra("author", item.get("author").toString())
//        startActivity(intent)
//    }
}