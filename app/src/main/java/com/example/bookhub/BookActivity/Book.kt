package com.example.bookhub.BookActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookhub.Adapter.MyAdapter
import com.example.bookhub.R
import com.example.bookhub.data.books
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_book.*


class Book : AppCompatActivity(),MyAdapter.OnItemClickListner {
    private lateinit var dbref: DatabaseReference
    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var booksArrayList: ArrayList<books>
    lateinit var adapter: MyAdapter
    lateinit var mHandler: Handler

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book)
        booksRecyclerView = findViewById(R.id.rc)
        booksRecyclerView.layoutManager = LinearLayoutManager(this)
        booksRecyclerView.setHasFixedSize(true)
        booksArrayList = arrayListOf<books>()
        getBooksData()
        back.setOnClickListener {
            finish()
        }
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
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
    private fun getBooksData() {
        dbref = FirebaseDatabase.getInstance().getReference("bookhub/books")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                booksArrayList.clear()
                try {
                    if (snapshot.exists()) {
                        for (bookSnapshot in snapshot.children) {
                            val book = bookSnapshot.getValue(books::class.java)!!
                            booksArrayList.add(book)
                        }
                        adapter = MyAdapter(booksArrayList, this@Book, this@Book)
                        booksRecyclerView.adapter = adapter
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@Book, e.message, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    @SuppressLint("DefaultLocale")
    private fun filter(e: String) {
        val filteritem = ArrayList<books>()
        for (item in booksArrayList) {
            if (item.title!!.toLowerCase().contains(e.toLowerCase())) {
                filteritem.add(item)
            }
        }
        booksRecyclerView.adapter = MyAdapter(filteritem, this, this)
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

    override fun onItemClick(item: books, position: Int) {
        var Pdf_image: String = ""
        val intent = Intent(this, Pdfviewer::class.java)
        intent.putExtra("title", item.title)
        intent.putExtra("image_url", Pdf_image)
        intent.putExtra("pdfname", item.pdfname)
        intent.putExtra("author", item.author)
        startActivity(intent)
    }
}