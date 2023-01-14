package com.example.bookhub.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookhub.BookActivity.Pdfviewer
//import com.example.bookhub.Pdfviewer
import com.example.bookhub.R
import java.util.ArrayList



class Adapter(
    context: Context,
    names: ArrayList<String>,
    imageUrls: ArrayList<String>
) :
    RecyclerView.Adapter<Adapter.ViewHolder>() {
    //vars
    private var mNames = ArrayList<String>()
    private var mImageUrls = ArrayList<String>()
    private val mContext: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.listitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: called.")
        Glide.with(mContext)
            .asBitmap()
            .load(mImageUrls[position])
            .into(holder.image)
        holder.name.text = mNames[position]
        holder.image.setOnClickListener {
            Log.d(
                TAG,
                "onClick: clicked on an image: " + mNames[position]
            )
            val intent:Intent= Intent(mContext, Pdfviewer::class.java)
            intent.putExtra("image_url",mImageUrls.get(position))
            intent.putExtra("title",mNames.get(position))
            mContext.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return mImageUrls.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView
        var name: TextView

        init {
            image = itemView.findViewById(R.id.image)
            name = itemView.findViewById(R.id.textv)
        }
    }

    companion object {
        private const val TAG = "RecyclerViewAdapter"
    }

    init {
        mNames = names
        mImageUrls = imageUrls
        mContext = context
    }
}