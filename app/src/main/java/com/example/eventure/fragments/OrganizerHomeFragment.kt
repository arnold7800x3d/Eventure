package com.example.eventure.fragments

import EventsAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.dataclass.Event
import com.google.firebase.firestore.FirebaseFirestore

class OrganizerHomeFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var eventNumberTextView: TextView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var createEventButton: Button

    private val eventsList = mutableListOf<Event>()
    private lateinit var eventsAdapter: EventsAdapter // Create this adapter class

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_organizer_home, container, false)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Find UI elements
        eventNumberTextView = view.findViewById(R.id.eventNumber)
        eventsRecyclerView = view.findViewById(R.id.recyclerView)
        createEventButton = view.findViewById(R.id.createEventButton)

        // Set up RecyclerView
        eventsAdapter = EventsAdapter(eventsList)
        eventsRecyclerView.layoutManager = LinearLayoutManager(context)
        eventsRecyclerView.adapter = eventsAdapter

        // Load events from Firestore
        loadEvents()

        createEventButton.setOnClickListener {
            findNavController().navigate(R.id.action_organizerHomeFragment_to_createEventFragment)
        }

        return view
    }

    private fun loadEvents() {
        db.collection("events")
            .get()
            .addOnSuccessListener { documents ->
                eventsList.clear() // Clear old data
                for (document in documents) {
                    val event = document.toObject(Event::class.java)
                    eventsList.add(event)
                }
                eventsAdapter.notifyDataSetChanged() // Notify adapter of data change

                // Update the event count and total attendees
                eventNumberTextView.text = eventsList.size.toString()
            }
            .addOnFailureListener { e ->
                Log.e("OrganizerHomeFragment", "Error loading events: ${e.message}")
            }
    }
}
