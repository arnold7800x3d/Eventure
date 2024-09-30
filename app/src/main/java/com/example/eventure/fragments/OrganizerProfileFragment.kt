package com.example.eventure.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.activities.LoginActivity
import com.example.eventure.adapterclass.EventsAdapterTwo
import com.example.eventure.dataclass.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrganizerProfileFragment : Fragment() {

    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventsAdapter: EventsAdapterTwo
    private val eventsList = mutableListOf<Event>()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_organizer_profile, container, false)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize RecyclerView
        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView)
        eventsAdapter = EventsAdapterTwo(eventsList, { event -> viewEvent(event) }, { event -> editEvent(event) }, { event -> deleteEvent(event) })
        eventsRecyclerView.layoutManager = LinearLayoutManager(context)
        eventsRecyclerView.adapter = eventsAdapter

        // Load events from Firestore
        loadEvents()

        auth = FirebaseAuth.getInstance()

        val logoutButton: Button = view.findViewById(R.id.logoutButton2)
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    private fun loadEvents() {
        db.collection("events")
            .get()
            .addOnSuccessListener { documents ->
                eventsList.clear()
                for (document in documents) {
                    val event = document.toObject(Event::class.java)
                    eventsList.add(event)
                }
                eventsAdapter.notifyDataSetChanged() // Notify the adapter of data change
            }
            .addOnFailureListener { e ->
                Log.e("OrganizerProfileFragment", "Error loading events: ${e.message}")
            }
    }

    private fun viewEvent(event: Event) {
        val bundle = Bundle().apply {
            putString("eventName", event.name)
            putString("eventDate", event.date)
            putString("eventLocation", event.location)
            putString("eventDescription", event.description)
        }

        findNavController().navigate(R.id.action_organizerProfileFragment_to_eventDetailsFragment, bundle)
    }


    private fun editEvent(event: Event) {
        val bundle = Bundle().apply {
            putString("eventName", event.name) // Pass the event name to the edit fragment
        }

        findNavController().navigate(R.id.action_organizerProfileFragment_to_editEventFragment, bundle)
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
                            Log.d("OrganizerProfileFragment", "Deleted event: ${event.name}")
                        }
                        .addOnFailureListener { e ->
                            Log.e("OrganizerProfileFragment", "Error deleting event: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("OrganizerProfileFragment", "Error finding event: ${e.message}")
            }
    }

}
