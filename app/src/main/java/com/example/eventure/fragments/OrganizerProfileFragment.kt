package com.example.eventure.fragments

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.eventure.R
import com.example.eventure.activities.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.bumptech.glide.Glide
import java.util.*

class OrganizerProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var profileImageView: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profileOrganization: TextView
    private var profileImageUri: Uri? = null
    private lateinit var sharedPreferences: SharedPreferences

    private val PICK_IMAGE_REQUEST = 1
    private val SHARED_PREF_NAME = "organizers_profile"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_organizer_profile, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")
        sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

        profileImageView = view.findViewById(R.id.profileimageView)
        profileName = view.findViewById(R.id.profileName)
        profileEmail = view.findViewById(R.id.profileEmail)
        profileOrganization = view.findViewById(R.id.profileOrganization)
        val setImageButton: Button = view.findViewById(R.id.setImageButton)
        val saveProfileButton: Button = view.findViewById(R.id.saveProfileButton)
        val logoutButton: Button = view.findViewById(R.id.logoutButton2)

        // Load cached profile data if available
        loadCachedProfile()

        profileName.setOnClickListener { showEditDialog("Name", profileName) }
        profileEmail.setOnClickListener { showEditDialog("Email", profileEmail) }
        profileOrganization.setOnClickListener { showEditDialog("Organization", profileOrganization) }

        setImageButton.setOnClickListener { openGallery() }
        saveProfileButton.setOnClickListener { saveProfileInfo() }

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize your views
        val profileName = view.findViewById<TextView>(R.id.profileName)
        val profileEmail = view.findViewById<TextView>(R.id.profileEmail)
        val profileOrganization = view.findViewById<TextView>(R.id.profileOrganization)

        // Retrieve the data (e.g., from SharedPreferences or a database)
        val sharedPreferences = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("name", "Default Name")
        val email = sharedPreferences.getString("email", "Default Email")
        val organization = sharedPreferences.getString("organization", "Default Organization")

        // Set the data into the TextViews
        profileName.text = name
        profileEmail.text = email
        profileOrganization.text = organization
    }

    private fun showEditDialog(field: String, textView: TextView) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit $field")

        val input = EditText(requireContext())
        input.setText(textView.text.toString())
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            textView.text = input.text.toString()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            profileImageUri = data.data
            profileImageView.setImageURI(profileImageUri)
        }
    }

    private fun saveProfileInfo() {
        val name = profileName.text.toString()
        val email = profileEmail.text.toString()
        val organization = profileOrganization.text.toString()

        if (profileImageUri != null) {
            uploadProfileImage(name, email, organization)
        } else {
            saveToFirestore(name, email, organization, null)
        }
    }

    private fun uploadProfileImage(name: String, email: String, organization: String) {
        val fileRef = storageReference.child("${UUID.randomUUID()}.jpg")
        fileRef.putFile(profileImageUri!!).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                saveToFirestore(name, email, organization, uri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Image upload failed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToFirestore(name: String, email: String, organization: String, imageUrl: String?) {
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "organization" to organization,
            "profileImage" to (imageUrl ?: "")
        )

        val docRef: DocumentReference = firestore.collection("organizers_profile")
            .document(auth.currentUser!!.uid)

        docRef.set(user).addOnSuccessListener {
            cacheProfileData(name, email, organization, imageUrl)
            Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Error saving profile!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cacheProfileData(name: String, email: String, organization: String, imageUrl: String?) {
        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("email", email)
        editor.putString("organization", organization)
        editor.putString("profileImage", imageUrl)
        editor.apply()
    }

    private fun loadCachedProfile() {
        profileName.text = sharedPreferences.getString("name", "")
        profileEmail.text = sharedPreferences.getString("email", "")
        profileOrganization.text = sharedPreferences.getString("organization", "")
        val profileImageUrl = sharedPreferences.getString("profileImage", "")

        if (!profileImageUrl.isNullOrEmpty()) {
            Glide.with(this).load(profileImageUrl).into(profileImageView)
        }
    }
}
