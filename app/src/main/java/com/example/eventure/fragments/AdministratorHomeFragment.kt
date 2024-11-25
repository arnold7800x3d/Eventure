package com.example.eventure.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.adapterclass.EventsAdapterFive
import com.example.eventure.adapterclass.UsersAdapter
import com.example.eventure.dataclass.Event
import com.example.eventure.dataclass.User
import com.google.firebase.firestore.FirebaseFirestore

class AdministratorHomeFragment : Fragment() {

    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventsAdapter: EventsAdapterFive
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersAdapter: UsersAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var totalEventsTextView: TextView
    private lateinit var totalUsersTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_administrator_home, container, false)

        // Initialize RecyclerViews
        eventsRecyclerView = view.findViewById(R.id.upcomingEventsRecyclerView)
        eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        usersRecyclerView = view.findViewById(R.id.usersRecyclerView)
        usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize TextViews
        totalEventsTextView = view.findViewById(R.id.totalEventsTextView)
        totalUsersTextView = view.findViewById(R.id.totalUsersTextView)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Load events and users
        loadEvents()
        loadUsers()

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

                // Update the total events count
                totalEventsTextView.text = "Total Events: ${documents.size()}"
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load events: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val usersList = mutableListOf<User>()
                for (document in documents) {
                    val user = document.toObject(User::class.java)
                    usersList.add(user)
                }
                usersAdapter = UsersAdapter(usersList)
                usersRecyclerView.adapter = usersAdapter

                // Update the total users count
                totalUsersTextView.text = "Total Users: ${documents.size()}"
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load users: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
