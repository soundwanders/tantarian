package com.soundwanders.tantarian

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.soundwanders.tantarian.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    // private val mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // FirebaseAnalytics.getInstance(this);

        // handle button clicks
        binding.loginBtn.setOnClickListener {

        }

        binding.skipBtn.setOnClickListener {

        }
    }
}