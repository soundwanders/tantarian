package com.soundwanders.tantarian

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import java.util.*
import kotlin.collections.HashMap

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

        fun deleteBook(
            context: Context,
            bookId: String,
            bookUrl: String,
            bookTitle: String
        ) {
            // context, used for progressDialog and Toast
            // bookId, used to delete correct book from db
            // bookUrl, delete book from Firebase store
            // bookTitle, show in dialog for user awareness

            val TAG = "DELETE_BOOK_TAG"

            Log.d(TAG, "deleteBook: Deleting selected book")

            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("One moment please...")
            progressDialog.setMessage("Deleting ${bookTitle}")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: Deleting from Cloud Storage")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: Permanently deleting selected book!")
                    Log.d(TAG, "deleteBook: Deleting book from Cloud storage")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")

                    ref.child(bookId)
                        .removeValue()

                        // NESTED onSuccess and onFailure listeners
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: Successfully deleted and removed book from database")
                            Toast.makeText(context, "Successfully deleted...forever!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: Failed to delete from database due to ${e.message}"
                            )
                            Toast.makeText(
                                context,
                                "Failed to delete book due to ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "deleteBook: Failed to delete book from storage due to ${e.message}")
                    Toast.makeText(
                        context,
                        "Failed to delete book from storage due to ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        fun incrementBookViewCount(bookId: String) {
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if (viewsCount == "" || viewsCount == "null") {
                            viewsCount = "0"
                        }

                        // increment views count +1
                        val newViewsCount = viewsCount.toLong() + 1
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewsCount

                        // set to database
                        val databaseRef = FirebaseDatabase.getInstance().getReference("Books")
                        databaseRef.child(bookId).updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }
    }
}