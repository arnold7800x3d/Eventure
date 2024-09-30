package com.example.eventure.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.eventure.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventDetailsFragment : Fragment() {

    private lateinit var registrationButton: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event_details, container, false)

        // Get the event data passed from the previous fragment
        val eventName = arguments?.getString("eventName")
        val eventDate = arguments?.getString("eventDate")
        val eventLocation = arguments?.getString("eventLocation")
        val eventDescription = arguments?.getString("eventDescription")

        // Find UI elements
        val eventNameTextView: TextView = view.findViewById(R.id.eventTitle)
        val eventDateTextView: TextView = view.findViewById(R.id.eventDate)
        val eventLocationTextView: TextView = view.findViewById(R.id.eventLocation)
        val eventDescriptionTextView: TextView = view.findViewById(R.id.eventDescription)

        // Set the event details
        eventNameTextView.text = eventName
        eventDateTextView.text = eventDate
        eventLocationTextView.text = eventLocation
        eventDescriptionTextView.text = eventDescription

        registrationButton = view.findViewById(R.id.eventRegistrationButton)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid.toString()

        // Fetch user role and toggle button visibility
        checkUserRole()

        return view
    }

    private fun checkUserRole() {
        db.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userRole = document.getString("role")

                    // Check role and hide/show the registration button
                    if (userRole == "attendee") {
                        registrationButton.visibility = View.VISIBLE // Show button
                    } else if (userRole == "organizer") {
                        registrationButton.visibility = View.GONE // Hide button
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("EventDetailsFragment", "Error fetching user role: ${e.message}")
            }
    }
}
