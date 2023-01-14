package com.example.bookhub

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.bookhub.Adapter.Adapter
import com.example.bookhub.Authentication.Loginhome
//import com.example.bookhub.Book.Book
import com.example.bookhub.BookActivity.Book
import com.example.bookhub.BookActivity.BookMark
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.slideview.*
import java.io.ByteArrayOutputStream
import java.io.IOException


class Home : AppCompatActivity() {
    var nav: NavigationView? = null
    var toggle: ActionBarDrawerToggle? = null
    var drawerLayout: DrawerLayout? = null
    var mAuth:FirebaseAuth?=null
    var editDialog:Dialog?=null
    var mydialog:Dialog?=null
    var profileDialog:Dialog?=null
    var reference: DatabaseReference?=null
    var firdata: FirebaseDatabase?=null
    private val PICK_IMAGE_REQUEST = 71
    private val GALLEARY_ACTIVITY_CODE = 1234
    private val RESULT_CROP = 400
    private val GalleryPick = 1
    private val CAMERA_REQUEST = 100
    private val STORAGE_REQUEST = 200
    private val IMAGEPICK_GALLERY_REQUEST = 300
    private val IMAGE_PICKCAMERA_REQUEST = 400
    private var filePath: Uri? = null
    private var bytedata:ByteArray?=null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    lateinit var imagePreview: CircleImageView
    lateinit var navpic:CircleImageView
    lateinit var picp:CircleImageView
    lateinit var uname:TextView
    private  var Names: ArrayList<String> = ArrayList()
    private  var imageurl:ArrayList<String> = ArrayList()
    lateinit var cameraPermission: Array<String>
    lateinit var storagePermission: Array<String>
    var imageuri: Uri? = null
//    lateinit var scaleGestureDetector: ScaleGestureDetector
//    private var mScaleFactor:Float=1.0f
    lateinit var pic:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        reference=FirebaseDatabase.getInstance().getReference("bookhub/users").child(FirebaseAuth.getInstance().currentUser!!.uid)
        reference!!.keepSynced(true)
        // allowing permissions of gallery and camera
        // allowing permissions of gallery and camera
        cameraPermission =
            arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        mydialog = Dialog(this)
        profileDialog= Dialog(this)
        val toolbar = findViewById<View>(R.id.myToolbar) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        nav = findViewById<View>(R.id.nav_view) as NavigationView
        drawerLayout = findViewById<View>(R.id.drawerLayout) as DrawerLayout
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout!!.addDrawerListener(toggle!!)
        toggle!!.syncState()
        nav!!.itemIconTintList=null
        nav!!.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.myItem1 -> {
                    SHowpopup()
                }
                R.id.myItem2 -> {
                    val intent=Intent(this, Book::class.java)
                    startActivity(intent)
                    drawerLayout!!.closeDrawer(GravityCompat.START)
                }
                R.id.bookmarked->{
                    val intent=Intent(this, BookMark::class.java)
                    startActivity(intent)
                    drawerLayout!!.closeDrawer(GravityCompat.START)
                }
                R.id.logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, Loginhome::class.java))
                    finish()
                }
            }
            true
        }
        Headernav()
        val imageList = ArrayList<SlideModel>() // Create image list

        imageList.add(SlideModel("https://subhajit4980.github.io/Bookhubpic.github.io/1.png"))
        imageList.add(SlideModel("https://subhajit4980.github.io/Bookhubpic.github.io/2.png"))
        imageList.add(SlideModel("https://subhajit4980.github.io/Bookhubpic.github.io/3.jpeg"))
        imageList.add(SlideModel("https://subhajit4980.github.io/Bookhubpic.github.io/4.jpeg","100+ story books for all ages"))
        imageList.add(SlideModel("https://subhajit4980.github.io/Bookhubpic.github.io/5.jpeg","open Library for everyone"))

        val imageSlider = findViewById<ImageSlider>(R.id.image_slider)
        imageSlider.setImageList(imageList, ScaleTypes.FIT)
        val poem:ImageView=findViewById(R.id.poem)
        val experience:ImageView=findViewById(R.id.experience)
        val others:ImageView=findViewById(R.id.others)
        Glide.with(this).load("https://subhajit4980.github.io/Bookhubpic.github.io/6.png").into(poem)
        Glide.with(this).load("https://subhajit4980.github.io/Bookhubpic.github.io/7.png").into(experience)
        Glide.with(this).load("https://subhajit4980.github.io/Bookhubpic.github.io/8.png").into(others)
        val book:ImageView=findViewById(R.id.bookimg)
        val audioStory:ImageView=findViewById(R.id.audioStory)
        bookL.setOnClickListener{
            val intent=Intent(this, Book::class.java)
            startActivity(intent)
        }
        story.setOnClickListener{
            val intent=Intent(this, Audio_story_activity::class.java)
            startActivity(intent)
        }
        val writeStory:ImageView=findViewById(R.id.writestory)
        writeStory.setOnClickListener {
            val intent=Intent(this, Write_story::class.java)
            startActivity(intent)
        }

        initimageBitmaps()
    }
    fun Headernav()
    {
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.currentUser
        val headerview = nav?.getHeaderView(0)
        navpic=headerview!!.findViewById(R.id.picnav)
         uname= headerview.findViewById(R.id.fullname)
        val uemail: TextView = headerview.findViewById(R.id.emailuser)
        val userid:TextView= headerview.findViewById(R.id.userid)
        val userId=currentUser!!.uid
        val storageref:StorageReference=FirebaseStorage.getInstance().reference.child("bookhub/users/$userId/profile.jpeg")
        storageref.downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).into(navpic)
        }
        navpic.setOnClickListener{
//            scaleGestureDetector= ScaleGestureDetector(this,ScaleListener())
            profileDialog!!.setContentView(R.layout.pop_pic)
            pic= profileDialog!!.findViewById(R.id.ppic)
            val close:ImageView= profileDialog!!.findViewById(R.id.close)
            storageref.downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(pic)
            }
            close.setOnClickListener{
                profileDialog!!.dismiss()
            }
            profileDialog!!.show()
            profileDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
            profileDialog!!.window!!.setLayout(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT)
        }
        var refuser:DatabaseReference=FirebaseDatabase.getInstance().getReference("bookhub/users").child(userId)
        refuser.get().addOnCompleteListener(this){
            task->
            if(task.isSuccessful){
                val dataSnapshot=task.result
                val name=dataSnapshot.child("name").value.toString()
                val email=dataSnapshot.child("email").value.toString()
                uname.text=name
                uemail.text=email
                userid.text=currentUser.uid
            }
            else{
                Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun SHowpopup(){
        mydialog!!.setContentView(R.layout.popup_profile)
        val Userid=FirebaseAuth.getInstance().currentUser!!.uid
        val close:ImageView= mydialog!!.findViewById(R.id.closepop)
        val edit:ImageView=mydialog!!.findViewById(R.id.profile_edit)
        editDialog= Dialog(this)
        edit.setOnClickListener(View.OnClickListener {
            editProfile()
        })
        close.setOnClickListener(View.OnClickListener {
            mydialog!!.dismiss()
        })
//        profile_data()
        val fname:TextView=mydialog!!.findViewById(R.id.fullname)
        val dbirth:TextView=mydialog!!.findViewById(R.id.dob)
        val mobile:TextView=mydialog!!.findViewById(R.id.phone)
        val mail:TextView=mydialog!!.findViewById(R.id.email)
        val bio:TextView=mydialog!!.findViewById(R.id.biography)
        picp=mydialog!!.findViewById(R.id.picp)
        val refuser:DatabaseReference=FirebaseDatabase.getInstance().getReference("bookhub/users").child(Userid)
        refuser.get().addOnCompleteListener(this){
                task->
            if(task.isSuccessful){
                val dataSnapshot=task.result
                val name=dataSnapshot.child("name").value.toString()
                val email=dataSnapshot.child("email").value.toString()
                val phoneNUm=dataSnapshot.child("phone").value.toString()
                val DOB=dataSnapshot.child("dob").value.toString()
                val BIO=dataSnapshot.child("bio").value.toString()
                fname.text=name
                mail.text=email
                mobile.text=phoneNUm
                dbirth.text=DOB
                bio.text=BIO
            }
            else{
                Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
            }
        }

        val storageref:StorageReference=FirebaseStorage.getInstance().reference.child("bookhub/users/$Userid/profile.jpeg")
        storageref.downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).into(picp)
        }
        mydialog!!.show()
        mydialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

//    edit profile code

    @SuppressLint("CutPasteId")
    fun editProfile(){
        editDialog!!.setContentView(R.layout.editprofile)
        val Userid=FirebaseAuth.getInstance().currentUser!!.uid
        val save:Button=editDialog!!.findViewById(R.id.save)
        val fname:TextInputEditText=editDialog!!.findViewById(R.id.fullnameup)
        val mobile:TextInputEditText=editDialog!!.findViewById(R.id.phoneup)
        val dbirth:TextInputEditText=editDialog!!.findViewById(R.id.dobup)
        val bio:TextInputEditText=editDialog!!.findViewById(R.id.bio)
        val close: ImageView = editDialog!!.findViewById(R.id.closepopup)
        val picu:CircleImageView=editDialog!!.findViewById(R.id.picu)
        close.setOnClickListener(View.OnClickListener {
            editDialog!!.dismiss()
        })
        val storageref:StorageReference=FirebaseStorage.getInstance().reference.child("bookhub/users/$Userid/profile.jpeg")
        storageref.downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).into(picu)
        }
        val refuser:DatabaseReference=FirebaseDatabase.getInstance().getReference("bookhub/users").child(Userid)
        refuser.get().addOnCompleteListener(this){
                task->
            if(task.isSuccessful){
                val dataSnapshot=task.result
                val name=dataSnapshot.child("name").value.toString()
                val phoneNUm=dataSnapshot.child("phone").value.toString()
                val DOB=dataSnapshot.child("dob").value.toString()
                val BIO=dataSnapshot.child("bio").value.toString()
                fname.setText(name)
                mobile.setText(phoneNUm)
                dbirth.setText(DOB)
                bio.setText(BIO)
            }
            else{
                Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
            }
        }
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        imagePreview=editDialog!!.findViewById(R.id.picu)

//        Launch gallery
        imagePreview.setOnClickListener { launchGallery() }

            /**
             * CHECK WHETHER INTERNET CONNECTION IS AVAILABLE OR NOT
             */
            fun checkConnection(context: Context): Boolean {
                val connMgr = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                if (connMgr != null) {
                    val activeNetworkInfo = connMgr.activeNetworkInfo
                    if (activeNetworkInfo != null) { // connected to the internet
                        // connected to the mobile provider's data plan
                        return if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
                            // connected to wifi
                            true
                        } else activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE
                    }
                }
                return false
        }
        save.setOnClickListener{
            try{
                if(checkConnection(this)){
                uname.text=fname.text
                val ref:DatabaseReference = FirebaseDatabase.getInstance().getReference("bookhub/users")
                ref.child(Userid).child("name").setValue(fname.text.toString())
                ref.child(Userid).child("phone").setValue(mobile.text.toString())
                ref.child(Userid).child("dob").setValue(dbirth.text.toString())
                ref.child(Userid).child("bio").setValue(bio.text.toString())
                Toast.makeText(this, "your profile updated", Toast.LENGTH_SHORT).show()
//                upload profile pic
                if(filePath != null){
                    val ref = storageReference?.child("bookhub/users/$Userid/profile.jpeg")
//                    val uploadTask = ref?.putFile(filePath!!)
                    val uploadTask= ref?.putBytes(bytedata!!)
                }
            }
                else{
                    Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
                }
            }

            catch (e :Exception)
            {
                Toast.makeText(this, "sorry profile doesn't updated", Toast.LENGTH_SHORT).show()
            }

        }
        editDialog!!.show()
        editDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun initimageBitmaps(){
        imageurl.add("https://static-cse.canva.com/blob/142541/Yellow-Surgeon-Creative-Book-Cover.jpg")
        Names.add("Review 5⭐")
    imageurl.add("data:image/jpeg;base64,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")
    Names.add("Review 4.8⭐")
    imageurl.add("data:image/jpeg;base64,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")
    Names.add("Review 4.6⭐")
    imageurl.add("data:image/jpeg;base64,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")
    Names.add("Review 4.5⭐")
    imageurl.add("data:image/jpeg;base64,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")
    Names.add("Review 4.3⭐")
    imageurl.add("data:image/jpeg;base64,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")
    Names.add("Review 4.0⭐")
    initRecyclerview()
    }

    private fun initRecyclerview() {
       val layoutManager:LinearLayoutManager= LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        val recyclerView:RecyclerView=findViewById(R.id.recycleview1)
        recyclerView.layoutManager = layoutManager
        val adapter = Adapter(this, Names, imageurl)
        recyclerView.adapter = adapter

    }
    @SuppressLint("NewApi")
    private fun launchGallery() {
//        val options = arrayOf("Camera", "Gallery")
//        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
//        builder.setTitle("Pick Image From")
//        builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
//            if (which == 0) {
//                if (!this.checkCameraPermission()!!) {
////                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        requestCameraPermission()
////                    }
//                } else {
//                    pickFromGallery()
//                }
//            } else if (which == 1) {
                if (!checkStoragePermission()!!) {
                    requestStoragePermission()
                } else {
                    pickFromGallery()
                }
//            }
//        })
//        builder.create().show()
    }

    // checking storage permissions
    private fun checkStoragePermission(): Boolean? {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Requesting  gallery permission
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestStoragePermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(storagePermission, STORAGE_REQUEST)
//        }
    }

    // checking camera permissions
//    private fun checkCameraPermission(): Boolean? {
//        val result = ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.CAMERA
//        ) == PackageManager.PERMISSION_GRANTED
//        val result1 = ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        ) == PackageManager.PERMISSION_GRANTED
//        return result && result1
//    }

    // Requesting camera permission
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestCameraPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(cameraPermission, CAMERA_REQUEST)
//        }
    }

    // Requesting camera and gallery
    // permission if not given
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
//            CAMERA_REQUEST -> {
//                if (grantResults.size > 0) {
//                    val camera_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
//                    val writeStorageaccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
//                    if (camera_accepted && writeStorageaccepted) {
//                        pickFromGallery()
//                    } else {
//                        Toast.makeText(
//                            this,
//                            "Please Enable Camera and Storage Permissions",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }
//            }
            STORAGE_REQUEST -> {
                if (grantResults.size > 0) {
                    val writeStorageaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (writeStorageaccepted) {
                        pickFromGallery()
                    } else {
                        Toast.makeText(this, "Please Enable Storage Permissions", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }

    // Here we will pick image from gallery or camera
    private fun pickFromGallery():Boolean {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(400,400)
            .setCropShape(CropImageView.CropShape.RECTANGLE).start(this@Home)
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val result = CropImage.getActivityResult(data)
                val resultUri = result.uri
                filePath=resultUri
                var bmp:Bitmap?=null
                try {
                    bmp=MediaStore.Images.Media.getBitmap(getContentResolver(),resultUri)
                }
                catch (e: IOException)
                {
                    e.printStackTrace()
                }
                val boas:ByteArrayOutputStream = ByteArrayOutputStream()
                bmp!!.compress(Bitmap.CompressFormat.JPEG,25,boas)
                bytedata=boas.toByteArray()
                Glide.with(this).load(resultUri).into(imagePreview)
                Glide.with(this).load(resultUri).into(navpic)
                Glide.with(this).load(resultUri).into(picp)
            }
        }
    }
//    zoom in and out
//    public override fun onTouchEvent(event: MotionEvent?): Boolean {
//        scaleGestureDetector.onTouchEvent(event)
//        return true
//    }
//    private inner class ScaleListener : ScaleGestureDetector.OnScaleGestureListener {
//        override fun onScale(p0: ScaleGestureDetector?): Boolean {
////            TODO("Not yet implemented")
//            mScaleFactor*=scaleGestureDetector.scaleFactor
//            mScaleFactor= max(0.1f, min(mScaleFactor,10.0f))
//            pic.setScaleX(mScaleFactor)
//            pic.setScaleY(mScaleFactor)
//            return true
//        }
//
//        override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {
//            TODO("Not yet implemented")
//        }
//
//        override fun onScaleEnd(p0: ScaleGestureDetector?) {
//            TODO("Not yet implemented")
//        }
//    }
}
