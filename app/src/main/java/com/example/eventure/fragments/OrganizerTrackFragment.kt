package com.example.eventure.fragments

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.adapterclass.EventsAdapterTwo
import com.example.eventure.dataclass.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

        // Set up PDF generation button
        val generateReportButton: Button = view.findViewById(R.id.generateReportsButton)
        generateReportButton.setOnClickListener {
            generatePDFReport()
        }

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

    private fun generatePDFReport() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val organizerId = currentUser.uid
            db.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(context, "No events found for PDF generation", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val pdfDocument = PdfDocument()
                    val paint = Paint().apply { textSize = 12f }
                    var pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                    var page = pdfDocument.startPage(pageInfo)
                    var canvas = page.canvas

                    var yPos = 20f
                    canvas.drawText("Event Report with Attendees", 10f, yPos, paint)
                    yPos += 20f

                    for (document in documents) {
                        val event = document.toObject(Event::class.java)
                        canvas.drawText("Event: ${event.name}", 10f, yPos, paint)
                        yPos += 20f
                        canvas.drawText("Date: ${event.date}", 10f, yPos, paint)
                        yPos += 20f
                        canvas.drawText("Location: ${event.location}", 10f, yPos, paint)
                        yPos += 20f
                        canvas.drawText("Attendees:", 10f, yPos, paint)
                        yPos += 20f

                        db.collection("event_registration")
                            .whereEqualTo("eventName", event.name)
                            .get()
                            .addOnSuccessListener { attendeesDocuments ->
                                for (attendeeDoc in attendeesDocuments) {
                                    val attendeeName = attendeeDoc.getString("name") ?: "Unknown"
                                    val attendeeEmail = attendeeDoc.getString("email") ?: "Unknown"
                                    val attendeePhone = attendeeDoc.getString("phone") ?: "Unknown"
                                    canvas.drawText("- Name: $attendeeName", 30f, yPos, paint)
                                    yPos += 20f
                                    canvas.drawText("  Email: $attendeeEmail", 30f, yPos, paint)
                                    yPos += 20f
                                    canvas.drawText("  Phone: $attendeePhone", 30f, yPos, paint)
                                    yPos += 20f

                                    // Check for page overflow
                                    if (yPos > pageInfo.pageHeight - 50) {
                                        pdfDocument.finishPage(page)
                                        pageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                                        page = pdfDocument.startPage(pageInfo)
                                        canvas = page.canvas
                                        yPos = 20f
                                    }
                                }

                                // Save the page after processing attendees
                                pdfDocument.finishPage(page)
                            }
                            .addOnFailureListener { e ->
                                Log.e("OrganizerTrackFragment", "Error loading attendees: ${e.message}")
                            }
                    }

                    // Save to file
                    val file = File(
                        requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        "Event_Report_With_Attendees.pdf"
                    )
                    try {
                        FileOutputStream(file).use { pdfDocument.writeTo(it) }
                        Toast.makeText(context, "PDF saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                    } catch (e: IOException) {
                        Log.e("GeneratePDF", "Error writing PDF", e)
                        Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
                    } finally {
                        pdfDocument.close()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("OrganizerTrackFragment", "Error loading events for PDF: ${e.message}")
                    Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
