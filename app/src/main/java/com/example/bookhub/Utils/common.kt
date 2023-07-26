package com.example.bookhub.Utils

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import com.example.bookhub.Models.AudioData
import com.example.bookhub.Models.BookData
import com.example.bookhub.UI.Audio_story.Audio_story_activity
import com.example.bookhub.UI.Home
import com.example.bookhub.Utils.constant.PICK_FILE
import com.example.bookhub.Utils.constant.STORAGE_PERMISSION_CODE
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.util.concurrent.TimeUnit

object common {
    fun compressPdf(inputUri: Uri, context: Context): ByteArray {
        val inputStream = context.contentResolver.openInputStream(inputUri)
        val outputStream = ByteArrayOutputStream()
        val reader = PdfReader(inputStream)
        val stamper = PdfStamper(reader, outputStream)
        stamper.setFullCompression()
        stamper.close()
        reader.close()

        val compressedBytes = outputStream.toByteArray()
        return compressedBytes
    }

    fun isValidContextForGlide(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        if (context is Activity) {
            val activity = context as Activity
            if (activity.isDestroyed || activity.isFinishing) {
                return false
            }
        }
        return true
    }

    fun getMP3Duration(filePath: String): String {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(filePath)
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val duration = durationStr?.toLongOrNull()
        retriever.release()
        return formatDuration(duration)
    }

    fun formatDuration(duration: Long?): String {
        val hours = TimeUnit.MILLISECONDS.toHours(duration!!)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun showSnackBar(msg: String, view: View) {
        var snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    fun <T> search(
        search: androidx.appcompat.widget.SearchView,
        list: ArrayList<T>,
        context: Context,
        onFilteredList: (ArrayList<T>) -> Unit,
    ) {
        search.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                search.clearFocus()
                if (query != null) {
                    val title = ArrayList<String>()
                    for (item in list) {
                        // Assuming the items in the list have a 'title' property (you can adjust this accordingly)
                        if (item is BookData) {
                            title.add(item.title?.toLowerCase() ?: "")
                        }
                        if (item is AudioData) {
                            title.add(item.title?.toLowerCase() ?: "")
                        }
                    }
                    try {
                        if (title.contains(query.toLowerCase())) {
                            filter(query, list, onFilteredList)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return false
            }

            override fun onQueryTextChange(e: String?): Boolean {
                if (e != null) {
                    val filterItem = ArrayList<T>()
                    for (item in list) {
                        // Assuming the items in the list have a 'title' property (you can adjust this accordingly)
                        if (item is BookData) {
                            if (item.title?.toLowerCase()?.contains(e.toLowerCase()) == true) {
                                filterItem.add(item)
                            }
                        }
                        if (item is AudioData) {
                            if (item.title?.toLowerCase()?.contains(e.toLowerCase()) == true) {
                                filterItem.add(item)
                            }
                        }
                    }
                    onFilteredList(filterItem)
                }
                return false
            }
        })
    }

    fun <T> filter(query: String, list: ArrayList<T>, onFilteredList: (ArrayList<T>) -> Unit) {
        val filterItem = ArrayList<T>()
        for (item in list) {
            // Assuming the items in the list have a 'title' property (you can adjust this accordingly)
            if (item is BookData && item.title?.toLowerCase()
                    ?.contains(query.toLowerCase()) == true
            ) {
                filterItem.add(item)
            }
        }
        onFilteredList(filterItem)
    }

//    val storagePermission =
//    arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
//        // checking storage permissions
//         fun checkStoragePermission(context: Activity): Boolean? {
//            return ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.READ_MEDIA_IMAGES
//            ) == PackageManager.PERMISSION_GRANTED
//        }
//
//        // Requesting  gallery permission
//        @RequiresApi(Build.VERSION_CODES.M)
//         fun requestStoragePermission(context: Activity) {
//            ActivityCompat.requestPermissions(context, storagePermission,
//                context.STORAGE_PERMISSION_CODE
//            )
//        }
//
//        // checking camera permissions
//        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//         fun checkCameraPermission(context: Activity): Boolean? {
//            val result = ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.CAMERA
//            ) == PackageManager.PERMISSION_GRANTED
//            val result1 = ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.READ_MEDIA_IMAGES
//            ) == PackageManager.PERMISSION_GRANTED
//            return result
//        }
//
//        // Requesting camera permission
//        @RequiresApi(Build.VERSION_CODES.M)
//         fun requestCameraPermission(context: Activity) {
//            ActivityCompat.requestPermissions(context, storagePermission,
//                context.CAMERA_PERMISSION_CODE
//            )
//        }


    val storagePermission = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.CAMERA
    )
    fun checkStoragePermission(context: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestStoragePermission(context: Activity) {
        ActivityCompat.requestPermissions(
            context,
            common.storagePermission,
            constant.STORAGE_PERMISSION_CODE
        )
    }

    fun checkCameraPermission(context: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestCameraPermission(context: Activity) {
        ActivityCompat.requestPermissions(
            context,
            arrayOf(Manifest.permission.CAMERA),
            constant.CAMERA_PERMISSION_CODE
        )

}
    @RequiresApi(Build.VERSION_CODES.M)
     fun launchGallery(context:Activity,photouri:(Uri)->Unit) {
        val options = arrayOf("Camera", "Gallery")
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Pick Image From")
        builder.setItems(options) { dialog, which ->
            if (which == 0) {
                if (!checkCameraPermission(context)) {
                    Toast.makeText(context, "Camera Permission not Granted", Toast.LENGTH_SHORT)
                        .show()
                    requestCameraPermission(context)
                } else {
                    val fileName = "new-photo-name.jpg"
                    // Create parameters for Intent with filename
                    val values = ContentValues()
                    values.put(MediaStore.Images.Media.TITLE, fileName)
                    values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera")
                    val photoUri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    context.intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    context.startActivityForResult(intent, constant.CAMERA_PERMISSION_CODE)
                    photouri.invoke(photoUri!!)
                }
            } else if (which == 1) {
                if (!checkStoragePermission(context)) {
                    requestStoragePermission(context)
                } else {
                    val galleryIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    context.startActivityForResult(galleryIntent, constant.STORAGE_PERMISSION_CODE)
                }
            }
        }
        builder.create().show()
    }

    fun cropImage(uri: Uri,context: Activity) {
        CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(400, 400)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(context)
    }
    interface OnResultListener {
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    }
}

