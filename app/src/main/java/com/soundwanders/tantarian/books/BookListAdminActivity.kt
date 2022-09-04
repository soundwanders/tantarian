package com.soundwanders.tantarian.books

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.soundwanders.tantarian.adapter.AdapterPdfAdmin
import com.soundwanders.tantarian.databinding.ActivityBookListAdminBinding
import com.soundwanders.tantarian.models.ModelBook
import kotlin.Exception

class BookListAdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookListAdminBinding
    private lateinit var pdfArrayList: ArrayList<ModelBook>
    private lateinit var adapterPdfAdmin: AdapterPdfAdmin

    private companion object {
        const val TAG = "PDF_LIST_ADMIN"
    }

    private var categoryId = ""
    private var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get intent passed from Adapter
        // double bang (!!) operator ensures that variable are always non-null
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!
        binding.subTitleTv.text = category

        loadPdfList()

        binding.searchEt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfAdmin.filter.filter(s)
                }
                catch (e: Exception) {
                    Log.d(TAG, "onTextChanged: ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.addPdfFab.setOnClickListener {
            startActivity(Intent(this, BookAddActivity::class.java))
        }
    }

    private fun loadPdfList() {
        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelBook::class.java)
                        // add created model to list
                        if (model != null) {
                            pdfArrayList.add(model)
                            Log.d(TAG, "onDataChange: ${model.title} ${model.categoryId}")
                        }
                    }
                    adapterPdfAdmin = AdapterPdfAdmin(this@BookListAdminActivity, pdfArrayList)
                    binding.booksRv.adapter = adapterPdfAdmin
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}