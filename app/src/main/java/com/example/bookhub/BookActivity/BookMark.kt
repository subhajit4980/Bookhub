package com.example.bookhub.BookActivity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookhub.Adapter.MyAdapter
import com.example.bookhub.R
import com.example.bookhub.data.books
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.bookmark.*
import kotlinx.android.synthetic.main.bookmark.search

class BookMark : AppCompatActivity(),MyAdapter.OnItemClickListner {
//    private lateinit var favBooksArrayList: ArrayList<Fav_Book>
//    private lateinit var book_title:ArrayList<String>
//    private lateinit var book_author:ArrayList<String>
    private lateinit var dbref:DatabaseReference
    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var booksArrayList: ArrayList<books>
    lateinit var adapter: MyAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bookmark)
        booksRecyclerView=findViewById(R.id.rc)
        booksRecyclerView.layoutManager= LinearLayoutManager(this)
        booksRecyclerView.setHasFixedSize(true)
        booksArrayList= arrayListOf<books>()
        getBooksData()
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
        back.setOnClickListener{
            finish()
        }
//        checkIfEmpty()
    }

    private fun getBooksData() {
        dbref=FirebaseDatabase.getInstance().getReference("bookhub/users/${FirebaseAuth.getInstance().currentUser!!.uid}/bookmark/favouritList")
        dbref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                booksArrayList.clear()
                try{
                    if(snapshot.exists()){
                        for (bookSnapshot in snapshot.children)
                        {
                            val book= bookSnapshot.getValue(books::class.java)!!
                            booksArrayList.add(book)
                        }
                        adapter= MyAdapter(booksArrayList,this@BookMark,this@BookMark)
                        booksRecyclerView.adapter=adapter
                        rc.visibility=View.VISIBLE
                        view_empty.visibility=View.GONE
                    }
                    else{
                        adapter= MyAdapter(booksArrayList,this@BookMark,this@BookMark)
                        booksRecyclerView.adapter=adapter
                        view_empty.visibility=View.VISIBLE
                    }
                }
                catch (e:Exception)
                {
                    Toast.makeText(this@BookMark, e.message, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    @SuppressLint("DefaultLocale")
    private fun filter(e:String)
    {
        val filteritem=ArrayList<books>()
        for(item in booksArrayList){
            if (item.title!!.toLowerCase().contains(e.toLowerCase()))
            {
                filteritem.add(item)
            }
        }
        booksRecyclerView.adapter = MyAdapter(filteritem,this,this)
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
    private fun checkIfEmpty() {
//            val emptyViewVisible = rc!!.adapter!!.itemCount == 0
//            view_empty!!.visibility = if (emptyViewVisible) View.VISIBLE else View.GONE
//            rc!!.visibility = if (emptyViewVisible) View.GONE else View.VISIBLE

    }
    override fun onItemClick(item: books, position: Int) {
        var Pdf_image:String=""
        val intent= Intent(this, Pdfviewer::class.java)
        intent.putExtra("title",item.title)
        intent.putExtra("image_url",Pdf_image)
        intent.putExtra("pdfname",item.pdfname)
        intent.putExtra("author",item.author)
        startActivity(intent)
    }
}