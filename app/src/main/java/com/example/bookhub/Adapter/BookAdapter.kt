package com.example.bookhub.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookhub.Models.BookData
import com.example.bookhub.databinding.DataBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class BookAdapter (options: FirestoreRecyclerOptions<BookData>,val context: Context,var listener: OnItemClickListner):
    FirestoreRecyclerAdapter<BookData, BookAdapter.ViewHolder>(options) {

    private var filterText: String = ""
    class ViewHolder(val binding: DataBinding): RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding=DataBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: BookData) {
        val ref: StorageReference =
            FirebaseStorage.getInstance().reference.child("Book/${model.uploder}/${model.title}/pdfImage.jpeg")
        ref.downloadUrl.addOnSuccessListener {
            Glide.with(context).load(it).into(holder.binding.image)
        }
        holder.binding.apply {
            title.text=model.title
            author.text=model.author
            review.text=model.review.toString()
            reader.text=model.reads
        }
        holder.itemView.setOnClickListener {
            val snapshot = snapshots.getSnapshot(position)
            listener.onItemClick(snapshot, position)
        }
    }
    interface OnItemClickListner{
        fun onItemClick(item: DocumentSnapshot, position: Int)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setTextFilter(filterText: String) {
        this.filterText = filterText
        notifyDataSetChanged()
    }
//    override fun getFilter(): Filter {
//        return object : Filter() {
//            override fun performFiltering(constraint: CharSequence?): FilterResults {
//                val filteredList = mutableListOf<MyModel>()
//                val queryText = constraint?.toString()?.trim()?.toLowerCase(Locale.getDefault())
//
//                if (!queryText.isNullOrEmpty()) {
//                    for (item in snapshots) {
//                        if (item.name?.toLowerCase(Locale.getDefault())
//                                ?.contains(queryText) == true
//                        ) {
//                            filteredList.add(item)
//                        }
//                    }
//                } else {
//                    filteredList.addAll(snapshots)
//                }
//
//                val results = FilterResults()
//                results.values = filteredList
//                return results
//            }
//
//            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//                val filteredList = results?.values as? MutableList<MyModel>
//                submitList(filteredList)
//            }
//        }
//    }
}