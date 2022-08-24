package com.soundwanders.tantarian

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.soundwanders.tantarian.databinding.ActivityPdfDetailsBinding

class PdfDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfDetailsBinding

    private var bookId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get bookId from intent
        bookId = intent.getStringExtra("bookId")!!
        loadBookDetails()

        TantarianApplication.incrementBookViewCount(bookId)

        binding.backBtn.setOnClickListener{
            onBackPressed()
        }
    }

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get book data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val title = "${snapshot.child("title").value}"
                    val description = "${snapshot.child("description").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"
                    val date = TantarianApplication.formatTimeStamp(timestamp.toLong())

                    TantarianApplication.loadCategory(categoryId, binding.categoryTv)
                    TantarianApplication.loadFromUrlSinglePage(
                        "$url",
                        "$title",
                        binding.pdfView,
                        binding.progressBar,
                        binding.pagesTv
                    )
                    TantarianApplication.loadPdfSize("$url","$title", binding.sizeTv)

                    // set book data
                    binding.titleTv.text = title
                    binding.descriptionTv.text = description
                    binding.downloadsTv.text = downloadsCount
                    binding.viewsTv.text = viewsCount
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

}