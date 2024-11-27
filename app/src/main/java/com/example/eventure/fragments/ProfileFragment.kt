package com.example.eventure.fragments

import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.activities.LoginActivity
import com.example.eventure.adapterclass.EventsAdapterFour
import com.example.eventure.dataclass.EventReg
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var eventsAdapter: EventsAdapterFour

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Logout Button Implementation
        val logoutButton: Button = view.findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Set up the PDF generation button
        val generateProfileButton: Button = view.findViewById(R.id.button4)
        generateProfileButton.setOnClickListener {
            generateProfilePDF()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userUID = auth.currentUser?.uid

        if (userUID != null) {
            db.collection("event_registrations")
                .whereEqualTo("userUID", userUID)
                .get()
                .addOnSuccessListener { documents ->
                    val events = mutableListOf<EventReg>()
                    for (document in documents) {
                        val eventName = document.getString("eventName") ?: "Unknown Event"
                        val eventDate = document.getString("eventDate") ?: "Date not available"
                        val eventLocation = document.getString("eventLocation") ?: "Location not specified"
                        val tickets = document.getString("tickets") ?: 0
                        events.add(EventReg(eventName, eventDate, eventLocation, tickets.toString()))
                    }

                    // Set up RecyclerView with EventsAdapterFour
                    val recyclerView: RecyclerView = view.findViewById(R.id.registeredEventsRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    eventsAdapter = EventsAdapterFour(events)
                    recyclerView.adapter = eventsAdapter
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileFragment", "Error fetching registered events: ${e.message}")
                    Toast.makeText(requireContext(), "Failed to load events.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateProfilePDF() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        Toast.makeText(context, "No profile data found for PDF generation", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // Extract user data from Firestore document
                    val userName = document.getString("name") ?: "Unknown"
                    val userEmail = document.getString("email") ?: "Unknown"
                    val userPhone = document.getString("phone") ?: "Unknown"
                    val userAddress = document.getString("address") ?: "Unknown"

                    // Create PDF document
                    val pdfDocument = PdfDocument()
                    val paint = Paint().apply { textSize = 12f }
                    var pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                    var page = pdfDocument.startPage(pageInfo)
                    var canvas = page.canvas

                    var yPos = 20f
                    canvas.drawText("Profile Report", 10f, yPos, paint)
                    yPos += 20f

                    canvas.drawText("Name: $userName", 10f, yPos, paint)
                    yPos += 20f
                    canvas.drawText("Email: $userEmail", 10f, yPos, paint)
                    yPos += 20f
                    canvas.drawText("Phone: $userPhone", 10f, yPos, paint)
                    yPos += 20f
                    canvas.drawText("Address: $userAddress", 10f, yPos, paint)
                    yPos += 20f

                    // Check for page overflow
                    if (yPos > pageInfo.pageHeight - 50) {
                        pdfDocument.finishPage(page)
                        pageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        yPos = 20f
                    }

                    // Save the page
                    pdfDocument.finishPage(page)

                    // Save the PDF to file
                    val file = File(
                        requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        "Profile_Report.pdf"
                    )
                    try {
                        FileOutputStream(file).use { pdfDocument.writeTo(it) }
                        Toast.makeText(context, "PDF saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                    } catch (e: IOException) {
                        Log.e("ProfileFragment", "Error writing PDF", e)
                        Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
                    } finally {
                        pdfDocument.close()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileFragment", "Error loading user data for PDF: ${e.message}")
                    Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User is not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}
