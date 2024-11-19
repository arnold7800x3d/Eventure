package com.example.eventure.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.adapterclass.EventsAdapterFive
import com.example.eventure.dataclass.Event
import com.google.firebase.firestore.FirebaseFirestore

class AdministratorHomeFragment : Fragment() {

    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventsAdapter: EventsAdapterFive
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_administrator_home, container, false)
        eventsRecyclerView = view.findViewById(R.id.upcomingEventsRecyclerView)
        eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        db = FirebaseFirestore.getInstance()

        loadEvents()

        return view
    }

    private fun loadEvents() {
        db.collection("events")
            .get()
            .addOnSuccessListener { documents ->
                val eventsList = mutableListOf<Event>()
                for (document in documents) {
                    val event = document.toObject(Event::class.java)
                    eventsList.add(event)
                }
                eventsAdapter = EventsAdapterFive(eventsList)
                eventsRecyclerView.adapter = eventsAdapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load events: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
