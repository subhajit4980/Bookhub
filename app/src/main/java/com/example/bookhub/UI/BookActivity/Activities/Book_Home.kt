package com.example.bookhub.UI.BookActivity.Activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookhub.Adapter.Bookmark_Adapter
import com.example.bookhub.Models.BookData
import com.example.bookhub.Utils.common.search
import com.example.bookhub.databinding.ActivityBookBinding
import com.google.firebase.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class Book_Home : AppCompatActivity(),Bookmark_Adapter.OnItemClickListner {
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
                adapter = Bookmark_Adapter(booksArrayList, this@Book_Home, this@Book_Home)
                booksRecyclerView.adapter = adapter
            }
        }
//      searching
        search(binding.search,booksArrayList,this){
            adapter = Bookmark_Adapter(it, this@Book_Home, this@Book_Home)
            booksRecyclerView.adapter = adapter
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
}