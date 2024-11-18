package com.example.eventure.adapterclass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.dataclass.EventReg

class EventsAdapterFour(private val events: List<EventReg>) :
    RecyclerView.Adapter<EventsAdapterFour.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventNameTextView)
        val eventDate: TextView = itemView.findViewById(R.id.eventDateTextView)
        val eventLocation: TextView = itemView.findViewById(R.id.eventLocationTextView)
        val tickets: TextView = itemView.findViewById(R.id.ticketsTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.attendee_profile_item_layout, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.eventName.text = event.name ?: "Unknown Event"
        holder.eventDate.text = event.date ?: "Date not available"
        holder.eventLocation.text = event.location ?: "Location not specified"
        holder.tickets.text = "Tickets: ${event.tickets ?: "N/A"}"
    }

    override fun getItemCount(): Int = events.size
}
