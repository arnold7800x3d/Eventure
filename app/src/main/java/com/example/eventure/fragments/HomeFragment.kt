package com.example.eventure.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.adapterclass.EventsAdapterThree
import com.example.eventure.dataclass.Event
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchButton: Button
    private lateinit var searchView: SearchView
    private lateinit var eventsAdapter: EventsAdapterThree
    private val eventsList = mutableListOf<Event>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Find UI elements
        recyclerView = view.findViewById(R.id.recyclerView)
        searchButton = view.findViewById(R.id.button)
        searchView = view.findViewById(R.id.searchView)

        // Set up RecyclerView
        eventsAdapter = EventsAdapterThree(eventsList) { event ->
            // Handle register button click
            registerForEvent(event)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = eventsAdapter

        // Set up search button click listener
        searchButton.setOnClickListener {
            val searchText = searchView.query.toString()
            if (searchText.isNotEmpty()) {
                searchEvents(searchText)
            } else {
                Toast.makeText(context, "Please enter a search term.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun searchEvents(searchText: String) {
        db.collection("events")
            .get()
            .addOnSuccessListener { documents ->
                eventsList.clear() // Clear old data displayed for previously searched events
                for (document in documents) {
                    val event = document.toObject(Event::class.java)

                    // Check if the event matches the search criteria
                    if (event.name.contains(searchText, ignoreCase = true) ||
                        event.description.contains(searchText, ignoreCase = true) ||
                        event.category.contains(searchText, ignoreCase = true)) {
                        eventsList.add(event)
                    }
                }

                eventsAdapter.notifyDataSetChanged() // Notify adapter of data change

                // Check if the list is empty and display a message on if an event is found or not
                if (eventsList.isEmpty()) {
                    Toast.makeText(context, "No events found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e -> // error message in case of error
                Log.e("HomeFragment", "Error searching events: ${e.message}")
            }
    }


    private fun registerForEvent(event: Event) {
        val bundle = Bundle().apply {
            putString("eventName", event.name) // Pass the event name
        }
        findNavController().navigate(R.id.action_homeFragment_to_eventRegistrationFragment, bundle)
    }

}
