package com.example.eventure.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import com.example.eventure.R
import com.google.firebase.firestore.FirebaseFirestore

class EditEventFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var eventName: String

    private lateinit var eventNameEditText: EditText
    private lateinit var eventDateEditText: EditText
    private lateinit var eventLocationEditText: EditText
    private lateinit var eventDescriptionEditText: EditText
    private lateinit var updateButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_event, container, false)

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Get event name passed from the previous fragment
        eventName = arguments?.getString("eventName").toString()

        // Find UI elements
        eventNameEditText = view.findViewById(R.id.editEventName)
        eventDateEditText = view.findViewById(R.id.editEventDate)
        eventLocationEditText = view.findViewById(R.id.editEventLocation)
        eventDescriptionEditText = view.findViewById(R.id.editEventDescription)
        updateButton = view.findViewById(R.id.updateEventButton)

        // Load event details into the fields
        loadEventDetails()

        // Handle update button click
        updateButton.setOnClickListener {
            updateEventInFirestore()
        }

        return view
    }

    // Function to load event details by name
    private fun loadEventDetails() {
        db.collection("events")
            .whereEqualTo("name", eventName) // Query for event by name
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.first() // Assuming unique name, take the first match
                    eventNameEditText.setText(document.getString("name"))
                    eventDateEditText.setText(document.getString("date"))
                    eventLocationEditText.setText(document.getString("location"))
                    eventDescriptionEditText.setText(document.getString("description"))
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditEventFragment", "Error loading event: ${e.message}")
            }
    }

    // Function to update event details by name
    private fun updateEventInFirestore() {
        db.collection("events")
            .whereEqualTo("name", eventName) // Query for event by name
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.first() // Assuming unique name

                    val updatedEvent = hashMapOf(
                        "name" to eventNameEditText.text.toString(),
                        "date" to eventDateEditText.text.toString(),
                        "location" to eventLocationEditText.text.toString(),
                        "description" to eventDescriptionEditText.text.toString()
                    )

                    db.collection("events").document(document.id)
                        .update(updatedEvent as Map<String, Any>)
                        .addOnSuccessListener {
                            Log.d("EditEventFragment", "Event updated successfully")
                            findNavController().popBackStack() // Navigate back
                        }
                        .addOnFailureListener { e ->
                            Log.e("EditEventFragment", "Error updating event: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditEventFragment", "Error querying event: ${e.message}")
            }
    }
}
