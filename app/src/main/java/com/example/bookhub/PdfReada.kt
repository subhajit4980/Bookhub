package com.example.bookhub

import android.annotation.SuppressLint
import android.app.ProgressDialog
//import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfiguration.AuthAlgorithm.strings
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.android.synthetic.main.activity_pdf_reada.*
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class PdfReada : AppCompatActivity() {
    private  var dialog:ProgressDialog?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_reada)
        val back:ImageView=findViewById(R.id.back)
        val pdfurl=intent.getStringExtra("pdfname")
        textv.text=pdfurl.toString()
        dialog = ProgressDialog(this);
        dialog!!.setMessage("Loading..");
        dialog!!.show();
        RetrivePdfStream().execute(pdfurl.toString())
        back.setOnClickListener{
            finish()
        }
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
            val pdfView = findViewById<PDFView>(R.id.pdfview)
            pdfView.fromStream(inputStream).load()
            dialog!!.dismiss()
        }
    }
}