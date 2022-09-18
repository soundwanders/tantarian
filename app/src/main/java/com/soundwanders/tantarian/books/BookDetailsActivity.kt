package com.soundwanders.tantarian.books

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.soundwanders.tantarian.Constants
import com.soundwanders.tantarian.R
import com.soundwanders.tantarian.TantarianApplication
import com.soundwanders.tantarian.databinding.ActivityBookDetailsBinding
import java.io.FileOutputStream

class BookDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookDetailsBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    private var bookId = ""
    private var bookUrl = ""
    private var bookTitle = ""

    // boolean used to check if selection is in User favorites
    private var isFavorite = false

    private companion object {
        const val TAG = "BOOK_DETAILS_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get bookId from intent
        bookId = intent.getStringExtra("bookId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading your book...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            checkIsFavorite()
        }

        TantarianApplication.incrementBookViewCount(bookId)

        loadBookDetails()

        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this, BookViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }

        binding.downloadBookBtn.setOnClickListener {
            // check if user has permission
            if (ContextCompat.checkSelfPermission (
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onCreate: Storage permission has already been granted")
            }
            else {
                Log.d(TAG, "onCreate: Storage permission rejected, checking if user is Admin...")
                requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        binding.favoriteBtn.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, "Please log in to add to Favorites", Toast.LENGTH_SHORT)
                    .show()
            }
            else {
                if (isFavorite) {
                    TantarianApplication.removeFavorite(this, bookId)
                }
                else addFavorite()
            }
        }
    }

    private val requestStoragePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "onCreate: Storage permission has been granted")
            downloadBook()
        }
        else {
            Log.d(TAG, "onCreate: Storage permission has been denied")
            Toast.makeText(this, "Request denied.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadBook() {
        Log.d(TAG, "downloadBook: Downloading $bookTitle!")
        progressDialog.setMessage("Loading $bookTitle...")
        progressDialog.show()

        // download pdf from firebase storage
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_ALLOWABLE_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "downloadBook: Downloading $bookTitle!")
                saveToDownloadsPath(bytes)
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "downloadBook: Failed to download $bookTitle... ${e.message}")
                Toast.makeText(this, "Failed to download $bookTitle... ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToDownloadsPath(bytes: ByteArray?) {
        Log.d(TAG, "saveToDownloadsPath: Saving $bookTitle to your Downloads folder")

        val fieNameWithExtension = "${System.currentTimeMillis()}.pdf"

        try {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            // if folder does not exist, create path
            downloadsFolder.mkdirs()

            val filePath = downloadsFolder.path + "/" + fieNameWithExtension
            val output = FileOutputStream(filePath)

            output.write(bytes)
            output.close()

            Toast.makeText(this, ("Saving $bookTitle to your Downloads folder"), Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()

            incrementDownloadCount()
        }
        catch (e: Exception) {
            progressDialog.dismiss()
            Log.d(TAG, "saveToDownloadsPath: Failed to download $bookTitle due to ${e.message}")
            Toast.makeText(this, ("Failed to download $bookTitle due to ${e.message}"), Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get book data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val description = "${snapshot.child("description").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"
                    val date = TantarianApplication.formatTimeStamp(timestamp.toLong())

                    TantarianApplication.loadCategory(categoryId, binding.categoryTv)

                    TantarianApplication.loadFromUrlSinglePage(
                        "$bookUrl",
                        "$bookTitle",
                        binding.pdfView,
                        binding.progressBar,
                        binding.pagesTv
                    )

                    TantarianApplication.loadPdfSize(bookUrl, bookTitle, binding.sizeTv)

                    // set book data
                    binding.titleTv.text = bookTitle
                    binding.descriptionTv.text = description
                    binding.downloadsTv.text = downloadsCount
                    binding.viewsTv.text = viewsCount
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ERROR", "Unable to load book details...Error: $error")
                }
            })
    }

    private fun incrementDownloadCount() {
        Log.d(TAG, "incrementDownloadCount: ")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadsCount = "${snapshot.child("downloadsCount").value}"
                    Log.d(TAG, "onDataChange: Downloads count: $downloadsCount")

                    if (downloadsCount == "" || downloadsCount == "null") {
                        downloadsCount = "0"
                    }

                    val newDownloadsCount: Long = downloadsCount.toLong() + 1
                    Log.d(TAG, "onDataChange: Updated downloads count: $newDownloadsCount")

                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["downloadsCount"] = newDownloadsCount

                    val databaseRef = FirebaseDatabase.getInstance().getReference("Books")
                    databaseRef.child(bookId)
                        .updateChildren(hashMap)

                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: IncrementDownload function passed...Downloads count increased")
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "onDataChange: Failed to increment downloads count due to ${e.message}")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ERROR", "Error while incrementing download count: $error")
                }
            })
    }

    private fun checkIsFavorite() {
        Log.d(TAG, "checkIsFavorite: Checking if item is already in your Favorites")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isFavorite = snapshot.exists()
                    if (isFavorite) {
                        Log.d(TAG, "onDataChange: Available to add to Favorites")

                        // set drawable to filled favorite icon to indicate item is favorite
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_black,
                            0,
                            0
                        )
                        binding.favoriteBtn.text = getString(R.string.favorite)
                    }
                    else {
                        Log.d(TAG, "onDataChange: Unable to add to Favorites")

                        // set drawable to filled favorite icon to indicate item is favorite
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_border_black,
                            0,
                            0
                        )
                        binding.favoriteBtn.text = getString(R.string.favorite)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ERROR", "Error while checking favorites: $error")
                }
            })
    }

    private fun addFavorite() {
        Log.d(TAG, "addFavorite: Adding to your Favorites")
        val timestamp = System.currentTimeMillis()

        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        // save favorite to database
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "addFavorite: Adding to your Favorites")
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "addFavorite: Unable to Favorite due to ${e.message}")
                Toast.makeText(
                    this,
                    "Unable to add selection to Favorites due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}