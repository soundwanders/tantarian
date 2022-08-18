package com.soundwanders.tantarian

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PdfAdminActivity : AppCompatActivity() {

    private var categoryId = ""
    private var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_admin)

        // get intent passed from Adapter
        // double bang (!!) operator ensures that variable are always non-null
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!
    }
}