package com.example.eventure.adapterclass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.dataclass.Event

class EventsAdapterFive(
    private val events: List<Event>
) : RecyclerView.Adapter<EventsAdapterFive.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventNameTextView)
        val eventDate: TextView = itemView.findViewById(R.id.eventDateTextView)
        val eventLocation: TextView = itemView.findViewById(R.id.eventLocationTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_event_item, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.eventName.text = event.name
        holder.eventDate.text = event.date
        holder.eventLocation.text = event.location
    }

    override fun getItemCount(): Int = events.size
}
