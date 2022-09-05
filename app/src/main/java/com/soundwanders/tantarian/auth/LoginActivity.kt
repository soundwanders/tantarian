package com.soundwanders.tantarian.auth

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.soundwanders.tantarian.dashboard.AdminDashboardActivity
import com.soundwanders.tantarian.dashboard.UserDashboardActivity
import com.soundwanders.tantarian.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    // view binding
    private lateinit var binding: ActivityLoginBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // PROGRESS DIALOG
    // *** DEPRECATED *** DEPRECATED -- FIND REPLACEMENT
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading...")
        progressDialog.setCanceledOnTouchOutside(false)

        // if no user account, send to sign up page
        binding.signUpTv.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.loginBtn.setOnClickListener {
            validateData()
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid e-mail, please try again", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
        } else {
            loginUser()
        }
    }

    private fun loginUser() {
        // show progress
        progressDialog.setMessage("Logging in to your account...")
        progressDialog.show()

        // createUserWithEmailAndPassword is from Firebase API ...
        // public Task<AuthResult> createUserWithEmailAndPassword (String email, String password)
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                checkUser()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed to log in due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun checkUser() {
        progressDialog.setMessage("Checking user...")

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()

                    // get user type (potential value is either User or Admin)
                    val userType = snapshot.child("userType").value

                    // navigate to appropriate dashboard depending on user type
                    if (userType == "user") {
                        startActivity(Intent(this@LoginActivity, UserDashboardActivity::class.java))
                    }
                    else if (userType == "admin") {
                        startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}