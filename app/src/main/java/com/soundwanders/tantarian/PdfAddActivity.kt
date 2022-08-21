package com.soundwanders.tantarian

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.soundwanders.tantarian.databinding.ActivityAddPdfBinding

class PdfAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPdfBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private var pdfUri: Uri? = null
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait :)")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener() {
            onBackPressed()
        }

        binding.categoryTv.setOnClickListener() {
            categorySelectDialog()
        }

        binding.attachPdfBtn.setOnClickListener() {
            pdfChooseIntent()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        Log.d(TAG, "validateData: Validating data.")

        // retrieve data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        // then validate the data recv'd
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
        Log.d(TAG, "uploadPdf: Uploading Pdf to cloud storage.")

        progressDialog.setMessage("Uploading Pdf...")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()
        val fileNameAndPath = "Books/$timestamp"
        val storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath)

        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadPdf: Uploaded Pdf, generating url")

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val storedPdfUrl = "${uriTask.result}"
                storePdfInDatabase(storedPdfUrl, timestamp)
            }
            .addOnFailureListener { e->
                Log.d(TAG, "uploadPdf: Failed to upload Pdf due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload Pdf due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun storePdfInDatabase(storedPdfUrl: String, timestamp: Long) {
        Log.d(TAG, "storedPdfUrl: Uploading pdf to database...")
        progressDialog.setMessage("Uploading pdf to database...")

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
                Log.d(TAG, "uploadPdf: Uploaded Pdf, generating url...")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded Pdf, generating url...", Toast.LENGTH_SHORT).show()
                pdfUri == null
            }
            .addOnFailureListener { e->
                Log.d(TAG, "uploadPdf: Failed to upload Pdf due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload Pdf due to ${e.message}", Toast.LENGTH_SHORT).show()
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
                TODO("Not yet implemented")
            }
        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categorySelectDialog() {
        Log.d(TAG, "categorySelectDialog: Showing select Pdf category dialog")

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
                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categorySelectDialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG, "categorySelectDialog: Selected Category title: $selectedCategoryTitle")
            }
            .show()
    }

    private fun pdfChooseIntent() {
        Log.d(TAG, "pdfChooseIntent: Starting Pdf choose Intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "Pdf Selected")
                pdfUri = result.data!!.data
            }
            else {
                Log.d(TAG, "Pdf Deselected")
                Toast.makeText(this, "Deselected", Toast.LENGTH_SHORT).show()
            }

        }
    )

}