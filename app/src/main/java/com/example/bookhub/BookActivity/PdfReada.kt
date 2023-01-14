package com.example.bookhub.BookActivity

import android.annotation.SuppressLint
import android.app.ProgressDialog
//import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfiguration.AuthAlgorithm.strings
import android.os.AsyncTask
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.bookhub.R
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.android.synthetic.main.activity_pdf_reada.*
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class PdfReada : AppCompatActivity() {
    private  var dialog:ProgressDialog?=null
    private val webview:WebView?=null
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_reada)
        val back:ImageView=findViewById(R.id.back)
        val pdfurl=intent.getStringExtra("pdfname")
        textv.text=pdfurl.toString()
//        dialog = ProgressDialog(this);
//        dialog!!.setMessage("Loading..");
//        dialog!!.show();
//        RetrivePdfStream().execute(pdfurl.toString())
        val webview:WebView=findViewById(R.id.pdfview)
        webview.webViewClient = WebViewClient()
        webview.loadUrl("https://drive.google.com/file/d/1maxY74YzJbhwO6KyfCbNK0pTBJhYFOw8/view")

        // this will enable the javascript settings, it can also allow xss vulnerabilities
        webview.settings.javaScriptEnabled = true

        // if you want to enable zoom feature
        webview.settings.setSupportZoom(true)
        back.setOnClickListener{
            finish()
        }
    }
    // if you press Back button this code will work
    override fun onBackPressed() {
        // if your webview can go back it will go back
        if (webview!!.canGoBack())
            webview.goBack()
        // if your webview cannot go back
        // it will exit the application
        else
            super.onBackPressed()
    }
// retrive the pdf through RetrivePdfStream class by passing url as argument
    @SuppressLint("StaticFieldLeak")
    inner class RetrivePdfStream: AsyncTask<String, Void, InputStream>() {

        override fun doInBackground(vararg p0: String?): InputStream? {
            var inputStream: InputStream?=null
            try {
                val url: URL = URL(strings[0])
                val urlconnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                if(urlconnection.responseCode==200)
                {
                    inputStream= BufferedInputStream(urlconnection.inputStream)
                }
            }
            catch (e:Exception)
            {
                return  null
            }
            return  inputStream
        }
        @Deprecated("Deprecated in Java")
        override fun onPostExecute(inputStream: InputStream?) {
            val pdfView = findViewById<WebView>(R.id.pdfview)
//            pdfView.fromStream(inputStream).load()
            dialog!!.dismiss()
        }
    }
}