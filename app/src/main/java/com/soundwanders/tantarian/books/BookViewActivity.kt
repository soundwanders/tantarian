package com.soundwanders.tantarian.books

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.soundwanders.tantarian.Constants
import com.soundwanders.tantarian.databinding.ActivityPdfViewBinding

class BookViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfViewBinding

    private companion object {
        val TAG = "PDF_VIEW_TAG"
    }

    var bookId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        loadBookDetails()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Loaded book data")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bookUrl = snapshot.child("url").value
                    Log.d(TAG, "onDataChange: PDF_URL: $bookUrl")

                    // call function to load book using relevant url
                    loadBookFromUrl("$bookUrl")
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadBookFromUrl(bookUrl: String) {
        Log.d(TAG, "loadBookFromUrl: Loading book using data url")

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        reference.getBytes(Constants.MAX_ALLOWABLE_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "loadBookDetails: Loaded book data")

                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        val currentPage = page + 1

                        // current page / total pages to show reading progress
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                        Log.d(TAG, "loadBookFromUrl: $currentPage/$pageCount")
                    }
                    .onError { t ->
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .onPageError { page, t ->
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .load()

                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "loadBookFromUrl: Failed to retrieve book data due to ${e.message} ")
                binding.progressBar.visibility = View.GONE
            }
    }
}

