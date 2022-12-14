package com.soundwanders.tantarian.dashboard

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.soundwanders.tantarian.MainActivity
import com.soundwanders.tantarian.models.ModelCategory
import com.soundwanders.tantarian.adapter.AdapterCategory
import com.soundwanders.tantarian.books.BookAddActivity
import com.soundwanders.tantarian.databinding.ActivityAdminDashboardBinding
import com.soundwanders.tantarian.profile.ProfileActivity


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
                    Log.d("ERROR", "Error while filtering ADMIN book list: ${e.message}")
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
                    startActivity(Intent(this, MainActivity::class.java))
                    Toast.makeText(this, "Logged out.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Cancel") { a, d ->
                    a.dismiss()
                }
                .show()

            checkUser()
        }

        // add a new category
        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this, AdminAddCategoryActivity::class.java))
        }

        // Fab = floating action button
        binding.addPdfFab.setOnClickListener {
            startActivity(Intent(this, BookAddActivity::class.java))
        }

        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
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
                Log.e("ERROR", "Unable to load ADMIN book list: $error")
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