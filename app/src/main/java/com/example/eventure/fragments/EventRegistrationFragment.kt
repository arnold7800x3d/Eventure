package com.example.eventure.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.eventure.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class EventRegistrationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_event_registration, container, false)

        val spinner: Spinner = view.findViewById(R.id.ticketSpinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.ticket_options_array,  // Reference to the string array
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        // Set a listener for when an item is selected
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Get the selected item
                val selectedEventType = parent.getItemAtPosition(position).toString()
                // Handle the selected item (e.g., display a message or save selection)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: handle the case where no item is selected
            }
        }

//        val db = Firebase.firestore
//
//        //Create a collection of attendees
//        val attendees_collection = db.collection("Attendees")
//
//        //Create document for an attendee
////        val attendee1 = hashMapOf(
////            "name"

        return view
    }
}