package com.soundwanders.tantarian.profile

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.soundwanders.tantarian.R
import com.soundwanders.tantarian.TantarianApplication
import com.soundwanders.tantarian.books.BookDetailsActivity
import com.soundwanders.tantarian.databinding.ActivityEditProfileBinding

class EditProfileActivity: AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var imageUri: Uri? = null
    private var username = ""
    private var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserProfile()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.profileIv.setOnClickListener {
            attachImagePopup()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        username = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()

        if (username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please enter your name & e-mail", Toast.LENGTH_SHORT).show()
        }

        else {
            if (imageUri == null) {
                updateProfile("")
                Toast.makeText(this, "Unable to upload new profile image", Toast.LENGTH_SHORT).show()
            }
            else {
                uploadImage()
            }
        }
    }

    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("Saving changes...")

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["name"] = "$username"
        hashMap["email"] = "$email"
        if (imageUri != null) {
            hashMap["profileAvatar"] = uploadedImageUrl
        }

        // set changes to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Saved changes!", Toast.LENGTH_SHORT).show()
            }

            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save changes due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImage() {
        progressDialog.setMessage("Saving changes...")
        progressDialog.show()

        val filePathAndName = "ProfileAvatars/"+firebaseAuth.uid

        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl

                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"
                updateProfile(uploadedImageUrl)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to save changes due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfile() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val email = "${snapshot.child("email").value}"
                    val profileAvatar = "${snapshot.child("profileAvatar").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"

                    binding.nameEt.setText(name)
                    binding.emailEt.setText(email)

                    // use Glide image loading for Profile Avatars
                    try {
                        Glide
                            .with(this@EditProfileActivity)
                            .load(profileAvatar)
                            .placeholder(R.drawable.ic_profile_black)
                            .into(binding.profileIv)
                    }
                    catch (e: Exception) {
                        Log.d("SAVE_TO_DOWNLOADS", "Failed due to ${e.message}")
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ERROR", "Unable to load user profile: $error")
                }
            })
    }

    private fun attachImagePopup() {
        val popupMenu = PopupMenu(this, binding.profileIv)

        popupMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val id = item.itemId
            if (id == 0) {
                chooseDeviceCamera()
            }
            else if (id == 1){
                chooseDeviceGallery()
            }
            true
        }
    }

    private fun chooseDeviceCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "temp_title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "temp_description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        cameraActivityResultLauncher.launch(intent)
    }

    private fun chooseDeviceGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult (
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultData = result.data
                imageUri = resultData!!.data

                binding.profileIv.setImageURI(imageUri)
            }
            else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private val galleryActivityResultLauncher = registerForActivityResult (
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultData = result.data
                imageUri = resultData!!.data

                binding.profileIv.setImageURI(imageUri)
            }
            else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )
}