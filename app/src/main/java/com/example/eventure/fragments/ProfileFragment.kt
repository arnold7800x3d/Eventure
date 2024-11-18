package com.example.eventure.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.activities.LoginActivity
import com.example.eventure.adapterclass.EventsAdapterFour
import com.example.eventure.dataclass.EventReg
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var eventsAdapter: EventsAdapterFour

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Logout Button Implementation
        val logoutButton: Button = view.findViewById(R.id.logoutButton)
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

        val userUID = auth.currentUser?.uid

        if (userUID != null) {
            db.collection("event_registrations")
                .whereEqualTo("userUID", userUID)
                .get()
                .addOnSuccessListener { documents ->
                    val events = mutableListOf<EventReg>()
                    for (document in documents) {
                        val eventName = document.getString("eventName") ?: "Unknown Event"
                        val eventDate = document.getString("eventDate") ?: "Date not available"
                        val eventLocation = document.getString("eventLocation") ?: "Location not specified"
                        val tickets = document.getString("tickets") ?: 0
                        events.add(EventReg(eventName, eventDate, eventLocation, tickets.toString()))
                    }

                    // Set up RecyclerView with EventsAdapterFour
                    val recyclerView: RecyclerView = view.findViewById(R.id.registeredEventsRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    eventsAdapter = EventsAdapterFour(events)
                    recyclerView.adapter = eventsAdapter
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileFragment", "Error fetching registered events: ${e.message}")
                    Toast.makeText(requireContext(), "Failed to load events.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
