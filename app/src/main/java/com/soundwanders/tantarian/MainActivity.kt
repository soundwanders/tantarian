package com.soundwanders.tantarian

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.soundwanders.tantarian.auth.LoginActivity
import com.soundwanders.tantarian.dashboard.UserDashboardActivity
import com.soundwanders.tantarian.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // handle button clicks
        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.skipBtn.setOnClickListener {
            startActivity(Intent(this, UserDashboardActivity::class.java))
        }
    }
}