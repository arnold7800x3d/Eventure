package com.example.eventure.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.adapterclass.EventsAdapterTwo
import com.example.eventure.dataclass.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrganizerTrackFragment : Fragment() {

    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventsAdapter: EventsAdapterTwo
    private val eventsList = mutableListOf<Event>()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_organizer_track, container, false)

        // Initialize Firebase Firestore and Auth
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Set up RecyclerView
        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView)
        eventsAdapter = EventsAdapterTwo(
            eventsList,
            { event -> viewEvent(event) },
            { event -> editEvent(event) },
            { event -> deleteEvent(event) },
            { event -> viewAttendees(event) }
        )
        eventsRecyclerView.layoutManager = LinearLayoutManager(context)
        eventsRecyclerView.adapter = eventsAdapter

        // Load events for the organizer
        loadEvents()

        return view
    }

    private fun viewAttendees(event: Event) {
        val bundle = Bundle().apply {
            putString("eventName", event.name) // Pass the event name to the attendee list fragment
        }

        findNavController().navigate(
            R.id.action_organizerTrackFragment_to_eventAttendeesFragment,
            bundle
        )
    }

    private fun loadEvents() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val organizerId = currentUser.uid
            db.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener { documents ->
                    eventsList.clear()

                    if (documents.isEmpty) {
                        eventsAdapter.notifyDataSetChanged()
                        return@addOnSuccessListener
                    }

                    for (document in documents) {
                        val event = document.toObject(Event::class.java)
                        eventsList.add(event)
                    }

                    eventsAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("OrganizerTrackFragment", "Error loading events: ${e.message}")
                }
        } else {
            Log.e("OrganizerTrackFragment", "User is not authenticated.")
        }
    }

    private fun viewEvent(event: Event) {
        val bundle = Bundle().apply {
            putString("eventName", event.name)
            putString("eventDate", event.date)
            putString("eventLocation", event.location)
            putString("eventDescription", event.description)
        }
        findNavController().navigate(
            R.id.action_organizerTrackFragment_to_eventDetailsFragment,
            bundle
        )
    }

    private fun editEvent(event: Event) {
        val bundle = Bundle().apply {
            putString("eventName", event.name)
        }
        findNavController().navigate(
            R.id.action_organizerTrackFragment_to_editEventFragment,
            bundle
        )
    }

    private fun deleteEvent(event: Event) {
        db.collection("events")
            .whereEqualTo("name", event.name)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("events").document(document.id).delete()
                        .addOnSuccessListener {
                            eventsList.remove(event)
                            eventsAdapter.notifyDataSetChanged()
                            Log.d("OrganizerTrackFragment", "Deleted event: ${event.name}")
                            loadEvents() // Reload events to update the list
                        }
                        .addOnFailureListener { e ->
                            Log.e("OrganizerTrackFragment", "Error deleting event: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("OrganizerTrackFragment", "Error finding event: ${e.message}")
            }
    }
}
