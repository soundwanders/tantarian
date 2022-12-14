package com.soundwanders.tantarian.books

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.soundwanders.tantarian.adapter.AdapterBookUser
import com.soundwanders.tantarian.models.ModelBook
import com.soundwanders.tantarian.databinding.FragmentBooksUserBinding

class BooksUserFragment : Fragment {
    private lateinit var binding: FragmentBooksUserBinding

    companion object {
        const val TAG = "PDF_LIST_USER"

        fun newInstance(categoryId: String, category: String, uid: String) : BooksUserFragment {
            val fragment = BooksUserFragment()

            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)

            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<ModelBook>
    private lateinit var adapterBookUser: AdapterBookUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get arguments passed from newInstance function
        val args = arguments
        if (args != null) {
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)

        Log.d(TAG, "onCreateView: Category: $category" )
        when (category) {
            "All" -> {
                loadAllItems()
            }
            "Most Viewed" -> {
                loadMostPopular("viewsCount")
            }
            "Most Downloaded" -> {
                loadMostPopular("downloadsCount")
            }
            else -> {
                loadCategorizedItems()
            }
        }

        binding.searchEt.addTextChangedListener { object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterBookUser.filter.filter(s)
                }
                catch (e: Exception) {
                    Log.d(TAG, "onTextChanged: Search Error: ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        }}
        return binding.root
    }

    private fun loadAllItems() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelBook::class.java)

                    pdfArrayList.add(model!!)
                }

                adapterBookUser = AdapterBookUser(context!!, pdfArrayList)
                binding.booksRv.adapter = adapterBookUser
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ERROR", "Unable to retrieve collection of *all* books...Error: $error")
            }
        })
    }

    private fun loadMostPopular(orderBy: String) {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")

        ref.orderByChild(orderBy).limitToLast(10)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelBook::class.java)

                        pdfArrayList.add(model!!)
                    }

                    adapterBookUser = AdapterBookUser(context!!, pdfArrayList)
                    binding.booksRv.adapter = adapterBookUser
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ERROR", "Unable to load *most popular* books...Error: $error")
                }
            })
    }

    private fun loadCategorizedItems() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")

        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelBook::class.java)

                        pdfArrayList.add(model!!)
                    }

                    adapterBookUser = AdapterBookUser(context!!, pdfArrayList)
                    binding.booksRv.adapter = adapterBookUser
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ERROR", "Encountered error while loading categorized books: $error")
                }
            })
    }

}