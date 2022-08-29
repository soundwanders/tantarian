package com.soundwanders.tantarian

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.soundwanders.tantarian.databinding.ActivityAdminDashboardBinding


class AdminDashboardActivity : AppCompatActivity() {
    // view binding
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategory.filter.filter(s)
                } catch (e: Exception) {
                    // add error handling
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        // log out on click
        binding.logoutBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete")
                .setMessage("Log out of your account?")
                .setPositiveButton("Log Out") {a, d ->
                    firebaseAuth.signOut()
                    Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel") { a, d ->
                    a.dismiss()
                }
                .show()

            checkUser()
        }

        // add a new category
        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this, AddCategoryActivity::class.java))
        }

        binding.addPdfFab.setOnClickListener {
            startActivity(Intent(this, PdfAddActivity::class.java))
        }
    }

    private fun loadCategories() {
        categoryArrayList = ArrayList()

        // get all categories from Firebase
        val ref = FirebaseDatabase.getInstance().getReference("Categories")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                }
                adapterCategory =
                    AdapterCategory(this@AdminDashboardActivity, categoryArrayList)
                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser

        // if user null, return to Main screen
        if (firebaseUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            val email = firebaseUser.email
            binding.subTitleTv.text = email
        }
    }
}