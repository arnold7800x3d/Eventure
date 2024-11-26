package com.example.eventure.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.example.eventure.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID
import androidx.navigation.fragment.findNavController // Import this for navigation

class CreateEventFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private var imageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_event, container, false)

        // Initialize Firebase Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        // Find UI elements
        val eventName: EditText = view.findViewById(R.id.eventName)
        val eventCategory: Spinner = view.findViewById(R.id.eventCategorySpinner)
        val eventLocation: EditText = view.findViewById(R.id.eventLocation3)
        val eventDate: EditText = view.findViewById(R.id.eventDate3)
        val maxAttendees: EditText = view.findViewById(R.id.maxAttendees)
        val registrationDeadline: EditText = view.findViewById(R.id.deadlineDate)
        val eventDescription: EditText = view.findViewById(R.id.eventDescription2)
        val createEventButton: Button = view.findViewById(R.id.createEventButton2)
        val eventImageButton: Button = view.findViewById(R.id.eventImageButton)

        // Handle the Set Event Image button
        eventImageButton.setOnClickListener {
            pickImageFromGallery()
        }

        val spinner: Spinner = view.findViewById(R.id.eventCategorySpinner)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.event_category,  // Reference to the string array
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        // Set a listener for when an item is selected in the spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Handle the selected item (e.g., display a message or save selection)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle case where no item is selected
            }
        }

        // Handle the Create Event button click
        createEventButton.setOnClickListener {
            val name = eventName.text.toString().trim()
            val category = eventCategory.selectedItem.toString().trim()
            val location = eventLocation.text.toString().trim()
            val date = eventDate.text.toString().trim()
            val attendees = maxAttendees.text.toString().trim()
            val deadline = registrationDeadline.text.toString().trim()
            val description = eventDescription.text.toString().trim()

            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null && name.isNotEmpty() && category.isNotEmpty() && location.isNotEmpty() &&
                date.isNotEmpty() && attendees.isNotEmpty() && deadline.isNotEmpty() && description.isNotEmpty()
            ) {
                if (imageUri != null) {
                    // If an image is selected, upload it
                    uploadImageToFirebase(
                        currentUser.uid,
                        name,
                        category,
                        location,
                        date,
                        attendees,
                        deadline,
                        description
                    )
                } else {
                    // If no image is selected, create event without image URL
                    storeEventInFirestore(
                        currentUser.uid,
                        name,
                        category,
                        location,
                        date,
                        attendees,
                        deadline,
                        description,
                        null // No image URL
                    )
                }
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // Function to pick an image from the gallery
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data // Get the selected image URI
        }
    }

    // Function to upload the image to Firebase Storage and store the event in Firestore
    private fun uploadImageToFirebase(
        organizerId: String,
        name: String,
        category: String,
        location: String,
        date: String,
        attendees: String,
        deadline: String,
        description: String
    ) {
        val fileName = UUID.randomUUID().toString() // Generate a unique file name for the image
        val imageRef = storageRef.child("event_images/$fileName")

        // Upload the image to Firebase Storage
        imageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    // Get the download URL of the uploaded image
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        storeEventInFirestore(
                            organizerId,
                            name,
                            category,
                            location,
                            date,
                            attendees,
                            deadline,
                            description,
                            downloadUri.toString() // Pass the image URL
                        )
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Failed to upload image: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    // Function to store the event data in Firestore
    private fun storeEventInFirestore(
        organizerId: String,
        name: String,
        category: String,
        location: String,
        date: String,
        attendees: String,
        deadline: String,
        description: String,
        imageUrl: String? // Optional image URL
    ) {
        val eventData = hashMapOf(
            "organizerId" to organizerId, // Add the organizer's UID
            "name" to name,
            "category" to category,
            "location" to location,
            "date" to date,
            "maxAttendees" to attendees.toInt(),
            "registrationDeadline" to deadline,
            "description" to description,
            "imageUrl" to imageUrl // Optional field for image URL
        )

        db.collection("events")
            .add(eventData)
            .addOnSuccessListener {
                Toast.makeText(context, "Event created successfully!", Toast.LENGTH_SHORT).show()
                // Navigate back to HomeFragment after successful event creation
                findNavController().navigate(R.id.action_createEventFragment_to_organizerHomeFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to create event: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}
