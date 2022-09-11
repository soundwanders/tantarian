package com.soundwanders.tantarian

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.soundwanders.tantarian.dashboard.AdminDashboardActivity
import com.soundwanders.tantarian.dashboard.UserDashboardActivity

private lateinit var firebaseAuth: FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            startActivity(Intent(this, MainActivity::class.java))
        }, 2000) // 2 second delay
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser

        // if user null, return to Main screen
        if (firebaseUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // get user type (potential value is either User or Admin)
                        val userType = snapshot.child("userType").value

                        // navigate to appropriate dashboard depending on user type
                        if (userType == "user") {
                            startActivity(Intent(this@SplashActivity, UserDashboardActivity::class.java))
                        }
                        else if (userType == "admin") {
                            startActivity(Intent(this@SplashActivity, AdminDashboardActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ERROR", "Firebase Auth Error: $error")
                    }
                })


        }
    }
}