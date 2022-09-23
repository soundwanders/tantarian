package com.soundwanders.tantarian.books

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.soundwanders.tantarian.models.ModelCategory
import com.soundwanders.tantarian.R
import com.soundwanders.tantarian.databinding.ActivityAddBookBinding

class BookAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBookBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private var pdfUri: Uri? = null
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Gathering data...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.categoryTv.setOnClickListener {
            categorySelectDialog()
        }

        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        binding.attachPdfBtnIcon.setOnClickListener {
            pdfPickIntent()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        Log.d(TAG, "validateData: Validated data")

        // retrieve data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        // then validate the data received
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter title.", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty()) {
            Toast.makeText(this, "Enter description.", Toast.LENGTH_SHORT).show()
        }
        else if (category.isEmpty()) {
            Toast.makeText(this, "Select a category.", Toast.LENGTH_SHORT).show()
        }
        else if (pdfUri == null ) {
            Toast.makeText(this, "Select a Pdf.", Toast.LENGTH_SHORT).show()
        }
        // if data is valid, upload
        else {
            uploadPdf()
        }
    }

    private fun uploadPdf() {
        Log.d(TAG, "uploadPdf: Added successfully")

        progressDialog.setMessage("Adding to library...")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()
        val filePathAndName = "Books/$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)

        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadPdf: Upload successful")

                // get url of uploaded Pdf
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val storedPdfUrl = "${uriTask.result}"
                storePdfInDatabase(storedPdfUrl, timestamp)
            }
            .addOnFailureListener { e->
                Log.d(TAG, "uploadPdf: Failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun storePdfInDatabase(storedPdfUrl: String, timestamp: Long) {
        Log.d(TAG, "storedPdfUrl: Saved!")
        progressDialog.setMessage("Saving to library...")

        val uid = firebaseAuth.uid

        // create hashmap, data for new category firebase store
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["uid"] = "$uid"
        hashMap["title"] = "$title"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["description"] = "$description"
        hashMap["url"] = "$storedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdf: $title added to library")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploading $title", Toast.LENGTH_SHORT).show()
                pdfUri = null
            }
            .addOnFailureListener { e->
                Log.d(TAG, "uploadPdf: Failed to upload $title due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading categories")
        // initialize Array List
        categoryArrayList = ArrayList()

        // db reference to load categories DF > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ERROR", "Unable to load categories...Error: $error")
            }
        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categorySelectDialog() {
        Log.d(TAG, "categorySelectDialog: Showing select Book category dialog")

        // get string in array of categories from arrayList
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        // alert dialog
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Select Category")
            .setItems(categoriesArray) {dialog, which ->
                selectedCategoryId = categoryArrayList[which].id
                selectedCategoryTitle = categoryArrayList[which].category

                // bind to enable render of category in textview UI
                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categorySelectDialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG, "categorySelectDialog: Selected Category title: $selectedCategoryTitle")
            }
            .show()
    }

    private fun pdfPickIntent() {
        Log.d(TAG, "pdfChooseIntent: Starting Pdf choose Intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    private val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "Pdf Selected")
                pdfUri = result.data!!.data
                val text = findViewById<TextView>(R.id.attachPdfBtn)
                text.text = R.string.attached_successfully.toString()
            }
            else {
                Log.d(TAG, "Pdf Deselected")
                Toast.makeText(this, "Deselected", Toast.LENGTH_SHORT).show()
            }
        }
    )
}
