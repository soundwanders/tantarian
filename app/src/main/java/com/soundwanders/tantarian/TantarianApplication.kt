package com.soundwanders.tantarian

import android.app.Application
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import java.util.*

class TantarianApplication:Application() {
    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        fun formatTimeStamp(timestamp: Long) : String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_ADD_TAG"

            // get pdf file reference and metadata from Firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetadata ->
                    Log.d(TAG, "loadPdfSize: Retrieved file data.")
                    val bytes = storageMetadata.sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    // convert bytes to kb
                    val kb = bytes/1024
                    val mb = kb/1024
                    if (mb > 1) {
                        sizeTv.text = "${String.format("$.2f", mb)} Mb"
                    }
                    else if (kb >= 1) {
                        sizeTv.text = "${String.format("$.2f", kb)} Kb"
                    }
                    else {
                        sizeTv.text = "${String.format("$.2f", bytes)} bytes"
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "loadPdfSize: Unable to retrieve file data due to ${e.message}.")
                }
        }

    fun loadFromUrlSinglePage(
        pdfUrl: String,
        pdfTitle: String,
        pdfView: PDFView,
        progressBar: ProgressBar,
        pagesTv: TextView?
    ) {
        val TAG = "PDF_ADD_TAG"
        // get pdf file reference and metadata from Firebase storage
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        ref.getBytes(Constants.MAX_ALLOWABLE_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                pdfView.fromBytes(bytes)
                    .pages(0)
                    .spacing(0)
                    .swipeHorizontal(false)
                    .enableSwipe(false)
                    .onError { t ->
                        progressBar.visibility = View.INVISIBLE
                        Log.d(TAG, "loadFromUrlSinglePage: ${t.message}")
                    }
                    .onPageError { page, t ->
                        progressBar.visibility = View.INVISIBLE
                        Log.d(TAG, "loadFromUrlSinglePage: ${t.message}")
                    }
                    .onLoad { nbPages ->
                        progressBar.visibility = View.INVISIBLE
                        Log.d(TAG, "loadFromUrlSinglePage: Pages: $nbPages")

                        // if pages text view is not null, set page numbers
                        if (pagesTv != null) {
                            pagesTv.text = "$nbPages"
                        }
                    }
                    .load()
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "loadPdfSize: Unable to retrieve file data due to ${e.message}.")
            }
        }
        fun loadCategory(categoryId: String, categoryTv: TextView) {
            // load category using its firebase-assigned id
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // get, then set the category
                        val category = "${snapshot.child("category").value}"
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }
    }
}