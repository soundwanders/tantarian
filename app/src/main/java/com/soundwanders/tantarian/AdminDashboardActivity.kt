package com.soundwanders.tantarian

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.soundwanders.tantarian.databinding.ActivityAdminDashboardBinding

class AdminDashboardActivity : AppCompatActivity() {
    // view binding
    private lateinit var binding: ActivityAdminDashboardBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        // sign out on click
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser

        // if user null, return to Main screen
        if (firebaseUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else {
            val email = firebaseUser.email
            binding.subTitleTv.text = email
        }
    }
}