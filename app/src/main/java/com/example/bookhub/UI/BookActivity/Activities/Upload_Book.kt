package com.example.bookhub.UI.BookActivity.Activities

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.example.bookhub.R
import com.example.bookhub.databinding.ActivityUploadBookBinding
import com.example.bookhub.databinding.SelectImageBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.ByteArrayOutputStream
import com.example.bookhub.Utils.common.compressPdf
import com.example.bookhub.Models.BookData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class Upload_Book : AppCompatActivity() {
    private var categories:String?=null
    private var ImagebyteArray:ByteArray?=null
    private lateinit var binding:ActivityUploadBookBinding
    private lateinit var Image_bytedata:ByteArray
    private lateinit var Pdf_bytedata:ByteArray
    private lateinit var ImageUri:Uri
    private var db = Firebase.firestore
    private var storageReference = FirebaseStorage.getInstance().reference
    val Userid= FirebaseAuth.getInstance().currentUser!!.uid
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityUploadBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bookscategory()
        binding.apply {
            thumb.setOnClickListener{
                customImageSelectorDialog()
            }
            uploadpdf.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/pdf"
                startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF)
            }
            upload.setOnClickListener {
                upload()
            }
        }
    }
    private fun customImageSelectorDialog() {
                Dexter.withContext(this)
                    .withPermission(android.Manifest.permission.READ_MEDIA_IMAGES)
                    .withListener(object: PermissionListener {
                        override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                            val galleryIntent=Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            startActivityForResult(galleryIntent, GALLERY)
                        }

                        override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                            Toast.makeText(this@Upload_Book,"Permission Denied",Toast.LENGTH_SHORT).show()
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            p0: com.karumi.dexter.listener.PermissionRequest?,
                            p1: PermissionToken?
                        ) {
                            showRationDialogPermissions()
                        }
                    }).onSameThread().check()
        }

        @Deprecated("Deprecated in Java")
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode== Activity.RESULT_OK){
                    if(requestCode== CAMERA){
                        val thumbnail: Bitmap = data?.extras!!.get("data") as Bitmap
                        binding.thumbnailImage.visibility=View.VISIBLE
                        compressImage(ImageUri)
                        Glide.with(this).load(thumbnail).fitCenter().into(binding.thumbnailImage)
                    }
                    if(requestCode== GALLERY) {
                        val selectedPhotoUri = data!!.data
                        binding.thumbnailImage.visibility = View.VISIBLE
                        binding.thumbnailImage.scaleType=ImageView.ScaleType.FIT_XY
                        if (selectedPhotoUri != null) {
                            compressImage(selectedPhotoUri)
                        }
                        Glide.with(this).load(selectedPhotoUri).fitCenter()
                            .into(binding.thumbnailImage)
                    }
                if(requestCode== PDF)
                {
                    val selectedFile = data!!.data
                    val fileName = selectedFile?.let { getFileName(it) }
                    val maxLength = 20 // maximum number of characters to show before the ellipsis
                    val truncatedText = if (fileName!!.length > maxLength) "${fileName.substring(0, maxLength)}..." else fileName
                    binding.pdfimage.setImageResource(R.drawable.baseline_picture_as_pdf_24)
                    binding.pdfurl.apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        text=truncatedText.toString()
                    }
                    Pdf_bytedata =compressPdf(selectedFile,this)
                }
            }else if(resultCode==Activity.RESULT_CANCELED){
                Log.e("cancelled","User Cancelled Image Selection")
            }
        }
        private fun showRationDialogPermissions(){
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("It looks like you have turned off permissions required for this feature.It cam be enabled under Application settings")
                .setPositiveButton("Go to SETTINGS"){
                        _,_->
                    try {

                        val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri= Uri.fromParts("package",packageName,null)
                        intent.data=uri
                        startActivity(intent)
                    }catch (e: ActivityNotFoundException){
                        e.printStackTrace()
                    }
                }
                .setNegativeButton("Cancel"){dialog,_->
                    dialog.dismiss()
                }.show()
        }
    fun bookscategory()
    {
        val bookCategories = arrayOf(
            "Fiction",
            "Classics",
            "Sci-Fi",
            "Non-fiction",
            "Biography",
            "Self-help",
            "Children",
            "Picture books",
            "Young adult"
        )
        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown, bookCategories)
        binding.category.apply {
            setAdapter(arrayAdapter)
            setOnItemClickListener { adapterView, view, i, l ->
                categories=adapterView.getItemAtPosition(i).toString()
            }
        }
    }
    private fun compressImage(uri: Uri)
    {
        var bmp = MediaStore.Images.Media.getBitmap(
            getContentResolver(),
            uri
        )
        val boas: ByteArrayOutputStream = ByteArrayOutputStream()
        bmp!!.compress(Bitmap.CompressFormat.JPEG, 25, boas)
        Image_bytedata = boas.toByteArray()
    }
    fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }
    private fun upload()
    {
        val title=binding.bookname.text.toString()
        val author=binding.author.text.toString()
        val description=binding.discription.text.toString()
        val ref=db.collection("Books").document()
        val id=ref.id
        if (title==""||author==""||description==""|| categories==null||Image_bytedata.isEmpty()||Pdf_bytedata.isEmpty())
        {
            Toast.makeText(this, "fill all the fields", Toast.LENGTH_SHORT).show()
        }else{
            val book= BookData(id,title,author,description,categories,"0","0",Userid.toString(),System.currentTimeMillis().toLong(),"Book/$Userid/$title/pdfImage.jpeg","Book/$Userid/$title/file.pdf")
            val Storage_ref = storageReference.child("Book/$Userid/$title")
            Storage_ref.child("file.pdf").putFile(byteArrayToUri(Pdf_bytedata)).addOnSuccessListener {
                val intent = Intent(this, Upload_Book::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }.addOnFailureListener {

            }
            Storage_ref.child("pdfImage.jpeg").putBytes(Image_bytedata).addOnSuccessListener {  }.addOnFailureListener{}
            ref.set(book).addOnSuccessListener {
                Toast.makeText(this, "Your Book is Uploaded", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Your Book is not Uploaded", Toast.LENGTH_SHORT).show()

            }
        }

    }
    fun byteArrayToUri(byteArray: ByteArray): Uri {
        val fileName = "file.pdf"
        val file = File(this.cacheDir, fileName)
        FileOutputStream(file).use { fos ->
            fos.write(byteArray)
            fos.flush()
        }
        return Uri.fromFile(file)
    }
    companion object{
        private const val CAMERA=1
        private const val GALLERY=2
        private const val PDF=3
    }
}