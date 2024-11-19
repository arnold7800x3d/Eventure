package com.example.eventure.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
    private lateinit var organizersAttendees: TextView  // Reference to the TextView for total attendees

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_organizer_track, container, false)

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize RecyclerView
        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView)
        eventsAdapter = EventsAdapterTwo(eventsList, { event -> viewEvent(event) }, { event -> editEvent(event) }, { event -> deleteEvent(event) })
        eventsRecyclerView.layoutManager = LinearLayoutManager(context)
        eventsRecyclerView.adapter = eventsAdapter

        // Reference to the TextView for total attendees
        organizersAttendees = view.findViewById(R.id.organizersAttendees)

        // Load events from Firestore
        loadEvents()

        return view
    }

    private fun loadEvents() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val organizerId = currentUser.uid
            db.collection("events")
                .whereEqualTo("organizerId", organizerId) // Query events by organizerId
                .get()
                .addOnSuccessListener { documents ->
                    eventsList.clear()
                    var totalAttendees = 0  // Variable to hold total attendee count
                    var processedEvents = 0  // Track number of processed events

                    for (document in documents) {
                        val event = document.toObject(Event::class.java)
                        val eventId = document.id

                        // Query the events_registrations collection to check for name registrations
                        db.collection("events_registrations")
                            .whereEqualTo("eventName", event.name) // Ensure we get registrations for this event
                            .get()
                            .addOnSuccessListener { registrationDocs ->
                                Log.d("OrganizerTrackFragment", "Found ${registrationDocs.size()} registrations for event: ${event.name}")

                                // Count the number of registrations by checking if 'name' field exists
                                registrationDocs.forEach { registrationDoc ->
                                    val name = registrationDoc.getString("name")
                                    if (!name.isNullOrEmpty()) {
                                        // Count this as a registration if the name is present
                                        totalAttendees++
                                    }
                                }

                                // Add the event to the list
                                event.attendeeCount = totalAttendees
                                eventsList.add(event)

                                requireActivity().runOnUiThread {
                                    organizersAttendees.text = "Total Attendees: $totalAttendees"
                                }

                                processedEvents++

                                if (processedEvents == documents.size()) {
                                    eventsAdapter.notifyDataSetChanged()
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("OrganizerTrackFragment", "Error counting registrations: ${e.message}")
                            }
                    }
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

        findNavController().navigate(R.id.action_organizerTrackFragment_to_eventDetailsFragment, bundle)
    }

    private fun editEvent(event: Event) {
        val bundle = Bundle().apply {
            putString("eventName", event.name) // Pass the event name to the edit fragment
        }

        findNavController().navigate(R.id.action_organizerTrackFragment_to_editEventFragment, bundle)
    }

    private fun deleteEvent(event: Event) {
        db.collection("events")
            .whereEqualTo("name", event.name) // Query Firestore for the event by name
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("events").document(document.id).delete()
                        .addOnSuccessListener {
                            loadEvents() // Refresh the event list after deletion
                            Log.d("OrganizerTrackFragment", "Deleted event: ${event.name}")
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
