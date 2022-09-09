package com.soundwanders.tantarian.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.soundwanders.tantarian.R
import com.soundwanders.tantarian.TantarianApplication
import com.soundwanders.tantarian.adapter.AdapterFavorites
import com.soundwanders.tantarian.databinding.ActivityProfileBinding
import com.soundwanders.tantarian.models.ModelBook

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var booksArrayList: ArrayList<ModelBook>
    private lateinit var adapterFavorites: AdapterFavorites

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserProfile()
        loadFavorites()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.editProfileBtn.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
    }

    private fun loadUserProfile() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileAvatar = "${snapshot.child("profileAvatar").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    // format date timestamp
                    val formattedDate = TantarianApplication.formatTimeStamp(timestamp.toLong())

                    binding.nameTv.text = name
                    binding.emailTv.text =  email
                    binding.signupDateTv.text = formattedDate
                    binding.privilegeTypeTv.text = userType

                    // use Glide image loading for Profile Avatars
                    try {
                        Glide
                            .with(this@ProfileActivity)
                            .load(profileAvatar)
                            .placeholder(R.drawable.ic_profile_black)
                            .into(binding.profileIv)
                    }
                    catch (e: Exception) {

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadFavorites() {
        booksArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    booksArrayList.clear()

                    for (ds in snapshot.children) {
                        // only need id of book, rest of data is loaded from AdapterFavorites
                        val bookId = "${ds.child("bookId").value}"

                        val modelBook = ModelBook()
                        modelBook.id = bookId

                        booksArrayList.add(modelBook)
                    }

                    // set the number of Favorite Books
                    binding.favoritesCountTv.text = "${booksArrayList.size}"

                    // set up adapter and bind to our Favorites List recycler view
                    adapterFavorites = AdapterFavorites(this@ProfileActivity, booksArrayList)
                    binding.favoritesRv.adapter = adapterFavorites
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}