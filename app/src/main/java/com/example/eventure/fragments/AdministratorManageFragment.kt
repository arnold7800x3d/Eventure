package com.example.eventure.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.adapterclass.EventsAdapterSix
import com.example.eventure.adapterclass.UsersListAdapter
import com.example.eventure.dataclass.Event
import com.example.eventure.dataclass.User
import com.google.firebase.firestore.FirebaseFirestore

class AdministratorManageFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var eventsAdapter: EventsAdapterSix
    private lateinit var usersAdapter: UsersListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_administrator_manage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        val manageEventsRecyclerView: RecyclerView =
            view.findViewById(R.id.manageEventsRecyclerView)
        manageEventsRecyclerView.layoutManager = LinearLayoutManager(context)

        fetchEvents { events ->
            eventsAdapter = EventsAdapterSix(
                events,
                onViewDetails = { event -> navigateToDetails(event) },
                onDelete = { event -> deleteEvent(event) }
            )
            manageEventsRecyclerView.adapter = eventsAdapter
        }

        val manageUsersRecyclerView: RecyclerView =
            view.findViewById(R.id.manageUsersRecyclerView)
        manageUsersRecyclerView.layoutManager = LinearLayoutManager(context)

        fetchUsers { users ->
            usersAdapter = UsersListAdapter(
                users.toMutableList(),
                context = requireContext(),
                onDelete = { user -> deleteUser(user) }
            )
            manageUsersRecyclerView.adapter = usersAdapter
        }
    }

    private fun fetchEvents(callback: (List<Event>) -> Unit) {
        db.collection("events")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val events = querySnapshot.documents.mapNotNull { it.toObject(Event::class.java) }
                callback(events)
            }
            .addOnFailureListener { e ->
                Log.e("ManageEvents", "Error fetching events", e)
                Toast.makeText(context, "Failed to load events", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToDetails(event: Event) {
        // Create a bundle and pass the event details
        val bundle = Bundle().apply {
            putString("eventName", event.name)
            putString("eventDate", event.date)
            putString("eventLocation", event.location)
            putString("eventDescription", event.description)
        }

        // Navigate to EventDetailsFragment with the bundle
        findNavController().navigate(R.id.action_administratorManageFragment_to_eventDetailsFragment2, bundle)
    }

    private fun deleteEvent(event: Event) {
        db.collection("events")
            .whereEqualTo("name", event.name) // Query by the name field
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    // Assume only one document per event name
                    val documentId = querySnapshot.documents[0].id
                    db.collection("events").document(documentId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show()

                            // Remove the deleted event from the current list and update the RecyclerView
                            val updatedEvents = eventsAdapter.currentEvents.filter { it.name != event.name }
                            eventsAdapter.updateEvents(updatedEvents)
                        }
                        .addOnFailureListener { e ->
                            Log.e("ManageEvents", "Error deleting event", e)
                            Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Event not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ManageEvents", "Error querying event", e)
                Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUsers(callback: (List<User>) -> Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val users = querySnapshot.documents.mapNotNull { it.toObject(User::class.java) }
                callback(users)
            }
            .addOnFailureListener { e ->
                Log.e("ManageUsers", "Error fetching users", e)
                Toast.makeText(context, "Failed to load users", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteUser(user: User) {
        db.collection("users")
            .whereEqualTo("email", user.email) // Query by the email field
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    // Assume only one document per email
                    val documentId = querySnapshot.documents[0].id
                    db.collection("users").document(documentId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "User deleted", Toast.LENGTH_SHORT).show()

                            // Remove the deleted user from the current list and update the RecyclerView
                            val updatedUsers = usersAdapter.usersList.filter { it.email != user.email }
                            usersAdapter.updateUsers(updatedUsers)
                        }
                        .addOnFailureListener { e ->
                            Log.e("ManageUsers", "Error deleting user", e)
                            Toast.makeText(context, "Failed to delete user", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ManageUsers", "Error querying user", e)
                Toast.makeText(context, "Failed to delete user", Toast.LENGTH_SHORT).show()
            }
    }
}