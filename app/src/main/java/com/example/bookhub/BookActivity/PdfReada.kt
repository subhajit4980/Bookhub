package com.example.bookhub.BookActivity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
//import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfiguration.AuthAlgorithm.strings
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookhub.R
import com.example.bookhub.databinding.ActivityPdfReadaBinding
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.link.DefaultLinkHandler
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class PdfReada : AppCompatActivity() {
    private var dialog: ProgressDialog? = null
    private val webview: WebView? = null
    private lateinit var binding: ActivityPdfReadaBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfReadaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val back = binding.back
        val pdfurl = intent.getStringExtra("pdfname")
        dialog = ProgressDialog(this);
        dialog!!.setMessage("Loading..");
        dialog!!.show();
        show_pdf(pdfurl)
        back.setOnClickListener {
            finish()
        }
    }

    private fun show_pdf(pdf_url: String?) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val Userid = FirebaseAuth.getInstance().currentUser!!.uid
        val pdfRef = storageRef.child(pdf_url.toString())
        val localFile = File(applicationContext.cacheDir, "file.pdf")
        // Download the PDF from Firebase Storage to the local file
        pdfRef.getFile(localFile)
            .addOnSuccessListener {
                dialog!!.dismiss()
                binding.pdfView.fromFile(localFile)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .onLoad {
                    }
                    .onError {
                        Toast.makeText(this, "PDF not loaded : $it", Toast.LENGTH_SHORT).show()
                    }
                    .onPageChange { page, pageCount -> /* Handle page change */ }
                    .enableAnnotationRendering(false)
                    .scrollHandle(DefaultScrollHandle(this))
                    .spacing(10)
                    .pageSnap(false)
                    .load()
            }
            .addOnFailureListener {
            }
    }
    private fun  findText()
    {

    }
}