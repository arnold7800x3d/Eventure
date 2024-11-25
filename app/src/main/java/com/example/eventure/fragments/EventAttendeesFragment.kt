package com.example.eventure.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.adapterclass.AttendeesAdapter
import com.example.eventure.dataclass.Attendee
import com.google.firebase.firestore.FirebaseFirestore

class EventAttendeesFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var attendeesAdapter: AttendeesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_attendees, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val attendeesRecyclerView: RecyclerView = view.findViewById(R.id.attendeesRecyclerView)
        attendeesRecyclerView.layoutManager = LinearLayoutManager(context)

        val eventName = arguments?.getString("eventName") // Get event name from bundle

        db = FirebaseFirestore.getInstance()
        fetchAttendees(eventName) { attendees ->
            attendeesAdapter = AttendeesAdapter(attendees)
            attendeesRecyclerView.adapter = attendeesAdapter
        }
    }

    private fun fetchAttendees(eventName: String?, callback: (List<Attendee>) -> Unit) {
        db.collection("event_registrations") // Use the correct collection name
            .whereEqualTo("eventName", eventName) // Query by eventName
            .get()
            .addOnSuccessListener { querySnapshot ->
                val attendees = querySnapshot.documents.mapNotNull { document ->
                    val name = document.getString("name") ?: return@mapNotNull null
                    val email = document.getString("email") ?: return@mapNotNull null
                    // Map other necessary fields if needed (e.g., phone, tickets, etc.)
                    Attendee(name = name, email = email)
                }
                callback(attendees)
            }
            .addOnFailureListener { e ->
                Log.e("EventAttendeesFragment", "Error fetching attendees", e)
                Toast.makeText(context, "Failed to load attendees", Toast.LENGTH_SHORT).show()
            }
    }
}

