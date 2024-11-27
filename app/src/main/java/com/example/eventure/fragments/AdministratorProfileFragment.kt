package com.example.eventure.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.eventure.R
import com.example.eventure.activities.LoginActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import android.content.Context
import android.graphics.BitmapFactory
import android.app.Activity
import android.provider.MediaStore
import java.io.ByteArrayOutputStream


class AdministratorProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText

    private val PICK_IMAGE_REQUEST = 71
    private var profileImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_administrator_profile, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        profileImageView = view.findViewById(R.id.imageView3)
        nameEditText = view.findViewById(R.id.editTextText)
        emailEditText = view.findViewById(R.id.editTextTextEmailAddress)

        // Set profile picture click listener to open image picker
        profileImageView.setOnClickListener {
            openImagePicker()
        }

        // Save profile button implementation
        val saveProfileButton: Button = view.findViewById(R.id.button3)
        saveProfileButton.setOnClickListener {
            saveProfile()
        }

        // Logout Button Implementation
        val logoutButton: Button = view.findViewById(R.id.button5)
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            profileImageUri = data.data
            profileImageView.setImageURI(profileImageUri)
        }
    }

    private fun saveProfile() {
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()

        if (name.isEmpty() || email.isEmpty() || profileImageUri == null) {
            Toast.makeText(context, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        // Save to Firestore in "admin_profiles" collection
        val userProfile: MutableMap<String, Any> = hashMapOf(
            "name" to name,
            "email" to email
        )

        val userId = auth.currentUser?.uid ?: return

        db.collection("admin_profiles").document(userId) // Changed to "admin_profiles"
            .set(userProfile)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile saved to Firestore", Toast.LENGTH_SHORT).show()
                uploadProfileImage(userId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error saving profile to Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun uploadProfileImage(userId: String) {
        profileImageUri?.let { uri ->
            // Create a reference to the Firebase Storage folder "admin_profile_image"
            val profileImageRef = storageRef.child("admin_profile_image/$userId.jpg")

            profileImageRef.putFile(uri)
                .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    profileImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveProfileImageUrlToFirestore(userId, downloadUri.toString())
                    }
                })
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error uploading profile image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfileImageUrlToFirestore(userId: String, imageUrl: String) {
        val userProfile: MutableMap<String, Any> = hashMapOf(
            "profileImageUrl" to imageUrl
        )

        db.collection("admin_profiles").document(userId) // Changed to "admin_profiles"
            .update(userProfile)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile image uploaded and URL saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error saving profile image URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}
