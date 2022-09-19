package com.soundwanders.tantarian.auth

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.soundwanders.tantarian.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        email = binding.emailEt.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(
                this,
                "Enter your account's e-mail address...",
                Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(
                this,
                "Please enter a valid e-mail address",
                Toast.LENGTH_SHORT).show()
        }
        else {
            recoverPassword()
        }
    }

    private fun recoverPassword() {
        progressDialog.setMessage("Sending recovery e-mail to $email")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Recovery e-mail sent. Please check your inbox!",
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                        Toast.makeText(
                        this,
                "E-mail failed to send due to ${e.message}",
                Toast.LENGTH_SHORT).show()
            }
    }
}