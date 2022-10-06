package com.soundwanders.tantarian.auth

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.soundwanders.tantarian.dashboard.UserDashboardActivity
import com.soundwanders.tantarian.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    // view binding
    private lateinit var binding: ActivityRegisterBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // PROGRESS DIALOG
    // *** DEPRECATED *** DEPRECATED -- FIND REPLACEMENT
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            // navigate to previous screen
            onBackPressed()
        }

        binding.registerBtn.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
        // input data

        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEt.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid e-mail, please try again", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()

        } else if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show()
        } else if (confirmPassword != password) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
        } else {
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        // show progress
        progressDialog.setMessage("Creating your account...")
        progressDialog.show()

        // createUserWithEmailAndPassword is from Firebase API ...
        // public Task<AuthResult> createUserWithEmailAndPassword (String email, String password)
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                updateUserInfo()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed to create account due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateUserInfo() {
        progressDialog.setMessage("Saving user info...")

        val timestamp = System.currentTimeMillis()
        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileAvatar"] = ""
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        // send data to db
        // (uid!!) ensures child is a String
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
        .setValue(hashMap)
        .addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@RegisterActivity, UserDashboardActivity::class.java))
            finish()
        }
        .addOnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(
                this,
                "Failed to register new user due to ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}