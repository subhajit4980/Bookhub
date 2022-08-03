package com.example.bookhub.Adapter_files
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookhub.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.makeramen.roundedimageview.RoundedImageView
import android.content.Context
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.data.view.*


class MyAdapter(private  val booklist:ArrayList<books>,var listener: OnItemClickListner,private val context: Context):RecyclerView.Adapter<MyAdapter.MyVIewHolder> (){
//    private val context: Context
private var selectedItemPosition: Int = 0
    private  var fvrtChecker:Boolean =false
    inner class MyVIewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        private val title:TextView=itemView.findViewById(R.id.title)
        private val author:TextView=itemView.findViewById(R.id.author)
        private val review:TextView=itemView.findViewById(R.id.review)
        private val reader:TextView=itemView.findViewById(R.id.reader)
        private val book_img:RoundedImageView=itemView.findViewById(R.id.image)
//        @SuppressLint("ResourceType")
        val constraintLayout:ConstraintLayout=itemView.findViewById(R.id.cardv)
        private var pdfname:String?=null

        @SuppressLint("UseCompatLoadingForDrawables")
        fun initialize(item: books, action:OnItemClickListner){
            title.text=item.title
            author.text=item.author
            review.text=item.review
            reader.text=item.reads
            pdfname= item.pdfname.toString()
            val storageref: StorageReference =
                FirebaseStorage.getInstance().reference.child("bookhub/Books_image/${item.title}.jpeg")
            storageref.downloadUrl.addOnSuccessListener {
                Glide.with(context).load(it).into(book_img)
            }
//            val rc:RecyclerView=
//            val emptyDataObserver= empty_data_observer(rc,itemView)
            itemView.setOnClickListener{action.onItemClick(item,adapterPosition)}
            //BOOKMARK PART Start
            val currUser=FirebaseAuth.getInstance().currentUser!!.uid
            val fvrtref=FirebaseDatabase.getInstance().getReference("bookhub/users/${FirebaseAuth.getInstance().currentUser!!.uid}/bookmark/favourites")
            val fvrt_list=FirebaseDatabase.getInstance().getReference("bookhub/users/${FirebaseAuth.getInstance().currentUser!!.uid}/bookmark/favouritList")
            itemView.bookmark.setOnClickListener {
                fvrtChecker=true
                fvrtref.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (fvrtChecker.equals(true))
                        {
                            if(snapshot.hasChild("${item.title}->${item.author}"))
                            {
                                fvrtref.child("${item.title}->${item.author}").removeValue()
                                fvrt_list.child("${item.title}->${item.author}").removeValue()
                                fvrtChecker=false
                                val BM:ImageView=itemView.findViewById(R.id.bookmark)
                                BM.setImageResource(R.drawable.ic_baseline_bookmark_add_24)
                                Toast.makeText(context, "Bookmark removed successfully", Toast.LENGTH_SHORT).show()
                            }
                            else{
                                fvrtref.child("${item.title}->${item.author}").setValue(true)
                                fvrt_list.child("${item.title}->${item.author}").child("title").setValue(item.title.toString())
                                fvrt_list.child("${item.title}->${item.author}").child("author").setValue(item.author.toString())
                                fvrt_list.child("${item.title}->${item.author}").child("pdfname").setValue(item.pdfname.toString())
                                fvrt_list.child("${item.title}->${item.author}").child("reads").setValue(item.reads.toString())
                                fvrt_list.child("${item.title}->${item.author}").child("review").setValue(item.review.toString())
                                fvrtChecker=false
                                val BM:ImageView=itemView.findViewById(R.id.bookmark)
                                BM.setImageResource(R.drawable.ic_baseline_bookmark_added_24)
                                Toast.makeText(context, "Bookmark added successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            }
            fvrtref.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.hasChild("${item.title}->${item.author}"))
                        {
                            val BM:ImageView=itemView.findViewById(R.id.bookmark)
                            BM.setImageResource(R.drawable.ic_baseline_bookmark_added_24)
                        }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
//      BOOKMARK PART END
        }

    }


    interface OnItemClickListner{
        fun onItemClick(item : books,position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyVIewHolder {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.data,parent,false)
        return  MyVIewHolder(itemView)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyVIewHolder, position: Int) {
        holder.initialize(booklist[position],listener)
//        holder.itemView.setOnClickListener{
//            selectedItemPosition=position.toInt()
//            notifyDataSetChanged()
//        }
//        if(selectedItemPosition==position)
//        {
//            holder.constraintLayout.setBackgroundColor(Color.parseColor("#FF9800"))
//        }

    }

    override fun getItemCount(): Int {
        return booklist.size
    }

}