package com.example.bookhub.Utill

import android.app.Activity
import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper

object constant {
    fun compressPdf(inputUri: Uri, context:Context): ByteArray {
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

}