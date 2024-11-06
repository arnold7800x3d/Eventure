package com.example.eventure.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.eventure.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.location.Geocoder
import android.location.Address
import android.content.Context

class EventRegistrationFragment : Fragment(), OnMapReadyCallback {

    private lateinit var db: FirebaseFirestore
    private lateinit var eventName: String // Use event name

    private lateinit var eventTitleTextView: TextView
    private lateinit var eventDateTextView: TextView
    private lateinit var eventLocationTextView: TextView
    private lateinit var attendeeNameEditText: EditText
    private lateinit var attendeeEmailEditText: EditText
    private lateinit var attendeePhoneEditText: EditText
    private lateinit var ticketSpinner: Spinner
    private lateinit var submitButton: Button
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_event_registration, container, false)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Get the event name from arguments
        eventName = arguments?.getString("eventName").toString()

        // Find UI elements
        eventTitleTextView = view.findViewById(R.id.eventTitle2)
        eventDateTextView = view.findViewById(R.id.eventDate2)
        eventLocationTextView = view.findViewById(R.id.eventLocation2)
        attendeeNameEditText = view.findViewById(R.id.attendeeName)
        attendeeEmailEditText = view.findViewById(R.id.attendeeEmail)
        attendeePhoneEditText = view.findViewById(R.id.attendeePhone)
        ticketSpinner = view.findViewById(R.id.ticketSpinner)
        submitButton = view.findViewById(R.id.submitButton)

        // Load event details
        loadEventDetails()

        // Set up ticket spinner
        val spinner: Spinner = view.findViewById(R.id.ticketSpinner)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.ticket_options_array,  // Reference to the string array
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        // Set a listener for when an item is selected in the spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Handle the selected item (e.g., display a message or save selection)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle case where no item is selected
            }
        }

        // Handle submit button click
        submitButton.setOnClickListener {
            registerForEvent()
        }

        // Get the SupportMapFragment and request the map to be ready
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    private fun loadEventDetails() {
        // Query Firestore to get event details by event name
        db.collection("events")
            .whereEqualTo("name", eventName)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0] // Assuming first match is the correct one
                    eventTitleTextView.text = document.getString("name")
                    eventDateTextView.text = document.getString("date")
                    eventLocationTextView.text = document.getString("location")

                    // After loading event details, update the map with the event location
                    updateMapWithEventLocation()
                } else {
                    Log.e("EventRegistrationFragment", "Event not found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("EventRegistrationFragment", "Error loading event details: ${e.message}")
            }
    }

    private fun updateMapWithEventLocation() {
        // Get the event location from the TextView
        val eventLocation = eventLocationTextView.text.toString()

        // Use Geocoder to get the latitude and longitude of the event location
        val latLng = getLatLngFromAddress(eventLocation)

        // If the location is valid, update the map
        if (latLng != null) {
            googleMap.addMarker(MarkerOptions().position(latLng).title("Event Location"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f)) // Adjust zoom level as needed
        } else {
            Log.e("EventRegistrationFragment", "Event location not found")
        }
    }

    private fun getLatLngFromAddress(address: String): LatLng? {
        val geocoder = Geocoder(requireContext())
        try {
            val addresses = geocoder.getFromLocationName(address, 1) // No type declaration
            if (addresses != null && addresses.isNotEmpty()) { // Null check
                val location = addresses[0]
                return LatLng(location.latitude, location.longitude)
            }
        } catch (e: Exception) {
            Log.e("EventRegistrationFragment", "Error getting location: ${e.message}")
        }
        return null
    }

    private fun registerForEvent() {
        val name = attendeeNameEditText.text.toString()
        val email = attendeeEmailEditText.text.toString()
        val phone = attendeePhoneEditText.text.toString()
        val tickets = ticketSpinner.selectedItem.toString()

        // Validate input
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create registration data
        val registrationData = hashMapOf(
            "eventName" to eventName,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "tickets" to tickets
        )

        // Store registration data in Firestore
        db.collection("event_registrations")
            .add(registrationData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
                // Navigate back or clear fields if needed
                requireActivity().onBackPressed()
            }
            .addOnFailureListener { e ->
                Log.e("EventRegistrationFragment", "Error registering for event: ${e.message}")
                Toast.makeText(requireContext(), "Registration failed.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }
}
