package com.example.eventure.fragments

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
import android.widget.TextView
import android.widget.Toast
import com.example.eventure.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class EventRegistrationFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var eventName: String // Use event name

    private lateinit var eventTitleTextView: TextView
    private lateinit var eventDateTextView: TextView
    private lateinit var eventLocationTextView: TextView
    private lateinit var attendeeNameEditText: EditText
    private lateinit var attendeeEmailEditText: EditText
    private lateinit var attendeePhoneEditText: EditText
    private lateinit var ticketSpinner: Spinner
    private lateinit var submitButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_event_registration, container, false)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Get the event name from arguments
        eventName = arguments?.getString("eventName").toString()

        // Find UI elements
        eventTitleTextView = view.findViewById(R.id.eventTitle2)
        eventDateTextView = view.findViewById(R.id.eventDate2)
        eventLocationTextView = view.findViewById(R.id.eventLocation2)
        attendeeNameEditText = view.findViewById(R.id.attendeeName)
        attendeeEmailEditText = view.findViewById(R.id.attendeeEmail)
        attendeePhoneEditText = view.findViewById(R.id.attendeePhone)
        ticketSpinner = view.findViewById(R.id.ticketSpinner)
        submitButton = view.findViewById(R.id.submitButton)

        // Load event details
        loadEventDetails()

        val spinner: Spinner = view.findViewById(R.id.ticketSpinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.ticket_options_array,  // Reference to the string array
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        // Set a listener for when an item is selected
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Get the selected item
                val selectedEventType = parent.getItemAtPosition(position).toString()
                // Handle the selected item (e.g., display a message or save selection)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: handle the case where no item is selected
            }
        }

        submitButton.setOnClickListener {
            registerForEvent()
        }

        return view
    }
    private fun loadEventDetails() {
        // Query Firestore to get event details by event name
        db.collection("events")
            .whereEqualTo("name", eventName)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0] // Assuming first match is the correct one
                    eventTitleTextView.text = document.getString("name")
                    eventDateTextView.text = document.getString("date")
                    eventLocationTextView.text = document.getString("location")
                } else {
                    Log.e("EventRegistrationFragment", "Event not found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("EventRegistrationFragment", "Error loading event details: ${e.message}")
            }
    }

    private fun registerForEvent() {
        val name = attendeeNameEditText.text.toString()
        val email = attendeeEmailEditText.text.toString()
        val phone = attendeePhoneEditText.text.toString()
        val tickets = ticketSpinner.selectedItem.toString()

        // Validate input
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create registration data
        val registrationData = hashMapOf(
            "eventName" to eventName, // Store event name
            "name" to name,
            "email" to email,
            "phone" to phone,
            "tickets" to tickets
        )

        // Store registration data in Firestore
        db.collection("event_registrations")
            .add(registrationData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
                // Navigate back or clear fields if needed
                requireActivity().onBackPressed()
            }
            .addOnFailureListener { e ->
                Log.e("EventRegistrationFragment", "Error registering for event: ${e.message}")
                Toast.makeText(requireContext(), "Registration failed.", Toast.LENGTH_SHORT).show()
            }
    }
}