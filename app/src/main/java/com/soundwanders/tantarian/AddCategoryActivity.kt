package com.soundwanders.tantarian

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.soundwanders.tantarian.databinding.ActivityAddCategoryBinding

class AddCategoryActivity : AppCompatActivity() {
    // view binding
    private lateinit var binding: ActivityAddCategoryBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // PROGRESS DIALOG
    // *** DEPRECATED *** DEPRECATED -- FIND REPLACEMENT
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("One moment please...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            // navigate to previous screen
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var category = ""

    private fun validateData() {
        category = binding.categoryEt.text.toString().trim()

        if (category.isEmpty()) {
            Toast.makeText(this, "Please add a category", Toast.LENGTH_SHORT).show()
        } else {
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        // create hashmap for firebase data
        // set p2 to Any because value could be of any type
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp

        // send data to db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Added a new category!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed to add category due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}