package com.example.bookhub.Adapter

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bookhub.Adapter.StoryAdapter.*
import com.example.bookhub.R
import com.example.bookhub.data.Story
import com.example.bookhub.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.data.view.*
import kotlinx.android.synthetic.main.story.view.*

class StoryAdapter(private  val storylist:ArrayList<Story>, var listener: OnItemClickListner, private val context: Context): RecyclerView.Adapter<StoryAdapter.MyviewHolder>() {
    private  var fvrtChecker:Boolean =false
    inner class MyviewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        private var title: TextView = itemView.findViewById(R.id.ptitle)
        private var description: TextView = itemView.findViewById(R.id.descrip)
        private var views: TextView = itemView.findViewById(R.id.noLikes)
        private var img: ImageView = itemView.findViewById(R.id.stimg)
        val picu: CircleImageView =itemView!!.findViewById(R.id.picu)
        @SuppressLint("SuspiciousIndentation")
        fun initialize(item: Story, action: OnItemClickListner) {
            title.text = item.title
            description.text = item.decription
            views.text = item.review
//            img.setImageResource(
//            Picasso.get().load(item.imagelink).into(img)
//            Glide.with(context).load(item.imagelink.toString()).into(img)
            var Userid=item.usid.toString()
            val ustorageref:StorageReference=FirebaseStorage.getInstance().reference.child("bookhub/users/$Userid/posts/story/${Userid.toString()+"_"+item.title.toString()}/image.jpeg")
            ustorageref.downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(img)
            }
            val storageref: StorageReference = FirebaseStorage.getInstance().reference.child("bookhub/users/$Userid/profile.jpeg")
            storageref.downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(picu)
            }
//              working with more button
            try {
                if (Userid.toString()==FirebaseAuth.getInstance().currentUser!!.uid.toString())
                {
                    itemView.more.visibility=View.VISIBLE
                        if(itemView.more.visibility==View.VISIBLE){
                        itemView.more.setOnClickListener {
                            showDialog(item)
                        }
                    }
                }
            }catch (e:Exception)
            {

            }

        itemView.cont.setOnClickListener{action.onItemClick(item,adapterPosition)}
            //        likes part
            val fvrtref = FirebaseDatabase.getInstance()
                .getReference("bookhub/users/${FirebaseAuth.getInstance().currentUser!!.uid}/Likes")
            itemView.like.setOnClickListener {
                fvrtChecker=true
                fvrtref.addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(fvrtChecker==true)
                        {
                            if(snapshot.hasChild("${item.title}"))
                            {
                                fvrtref.child("${item.title}").removeValue()
                                fvrtChecker=false
                                val Lik:ImageView=itemView.findViewById(R.id.like)
                                Lik.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                            }else{
                                fvrtref.child("${item.title}").setValue(true)
                                fvrtChecker=false
                                val Lik:ImageView=itemView.findViewById(R.id.like)
                                Lik.setImageResource(R.drawable.ic_baseline_favorite_24)
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
                    if(snapshot.hasChild("${item.title}"))
                    {
                        val BM:ImageView=itemView.findViewById(R.id.like)
                        BM.setImageResource(R.drawable.ic_baseline_favorite_24)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }


//          dialog for edit and delete the post
        private fun showDialog(item: Story) {
            val dialog:Dialog= Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.more_option)
            val edit:TextView=dialog.findViewById(R.id.editPost)
            val delete:TextView=dialog.findViewById(R.id.deletePost)
            edit.setOnClickListener {
                Toast.makeText(context, "editing", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            delete.setOnClickListener {
               val dbref:DatabaseReference = FirebaseDatabase.getInstance().getReference("bookhub/writeStory")

                val Userid=FirebaseAuth.getInstance().currentUser!!.uid


//                deleteing the image and data of the post
                dbref.addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.hasChild("${Userid.toString()+"_"+item.title.toString()}")){
                            dbref.child("${Userid.toString()+"_"+item.title.toString()}").removeValue()
                            FirebaseStorage.getInstance().reference.child("bookhub/users/${Userid.toString()}/posts/story/${Userid.toString()+"_"+item.title.toString()}/image.jpeg").delete()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    }
                })
//                perform the deleteing of like button
                val users=FirebaseDatabase.getInstance().getReference("bookhub/users")
                val userid= arrayListOf<String>()
                users.addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children)
                        {
                            val usid=child.key
                            userid.add(usid.toString())
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
                for(p in userid)
                {
                 try {
                     Toast.makeText(context, p.toString(), Toast.LENGTH_SHORT).show()
                     val fvrtref = FirebaseDatabase.getInstance()
                         .getReference("bookhub/users/${p.toString()}/Likes")
                     fvrtref.addValueEventListener(object :ValueEventListener{
                         override fun onDataChange(snapshot: DataSnapshot) {
                             if(snapshot.hasChild("${item.title}"))
                             {
                                 fvrtref.child("${item.title}").removeValue()
                             }
                         }

                         override fun onCancelled(error: DatabaseError) {
                             Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                         }

                     })
                 } catch (e:Exception)
                 {

                 }
                }
                dialog.dismiss()
            }
            dialog.show()
            dialog.window!!.setLayout(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.attributes.windowAnimations=R.style.DialogAnim
            dialog.window!!.setGravity(Gravity.BOTTOM)

        }
    }
    interface OnItemClickListner {
            fun onItemClick(item: Story,position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyviewHolder {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.story,parent,false)
        return MyviewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyviewHolder, position: Int) {
        holder.initialize(storylist[position],listener)
    }

    override fun getItemCount(): Int {
        return storylist.size
    }

}