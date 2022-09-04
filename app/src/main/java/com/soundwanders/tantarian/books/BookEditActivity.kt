package com.soundwanders.tantarian.books

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.soundwanders.tantarian.databinding.ActivityBookEditBinding

class BookEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookEditBinding

    private companion object {
        private const val TAG = "PDF_EDIT_TAG"
    }

    // get bookId from intent started in AdapterPdfAdmin
    private var bookId = ""

    // array lists to store category title and id
    private lateinit var categoryTitleArrayList: ArrayList<String>
    private lateinit var categoryIdArrayList: ArrayList<String>

    private lateinit var progressDialog: ProgressDialog

    // for use in categoryDialog function
    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    // for use in validateData function
    private var title = ""
    private var description = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // fetch bookId in order to edit correct book
        bookId = intent.getStringExtra("bookId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("One moment please...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }

        loadCategories()
        loadBookInfo()
    }

    private fun categoryDialog() {
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)

        for (i in categoryTitleArrayList.indices) {
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Category")
            .setItems(categoriesArray){ dialog, position ->
                // save category id and title on click
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                // set to TextView for render to ui
                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()
    }

    private fun loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book information")

        // begin updating book info
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val title = snapshot.child("title").value.toString()
                    val description = snapshot.child("description").value.toString()

                    // set to Views for render to UI
                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    Log.d(TAG, "loadBookInfo: Loading book information")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val category = snapshot.child("category").value

                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun validateData() {
        // get data and convert to string so we can work with it
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Enter a title", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty()) {
            Toast.makeText(this, "Enter a description", Toast.LENGTH_SHORT).show()
        }
        else if (selectedCategoryId.isEmpty()) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show()
        }
        else {
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.d(TAG, "updatePdf: Updating book info")

        progressDialog.setMessage("Updating book info...")
        progressDialog.show()

        // set updated data that will be sent to db
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["id"] = "$selectedCategoryId"

        // begin updating book info
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)

            .addOnSuccessListener {
                Log.d(TAG, "updatePdf: Updated successful!")
                Toast.makeText(
                    this,
                    "Updated data successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }

            .addOnFailureListener { e ->
                Log.d(TAG, "updatePdf: Failed to update data due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed to update data due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories.")
        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before data is added
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children) {
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryTitleArrayList.add(category)
                    categoryIdArrayList.add(id)

                    Log.d(TAG, "onDataChange: Category ID: $id")
                    Log.d(TAG, "onDataChange: Category Title: $category")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}