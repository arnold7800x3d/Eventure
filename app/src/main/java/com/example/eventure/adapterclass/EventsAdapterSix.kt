package com.example.eventure.adapterclass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.dataclass.Event

class EventsAdapterSix(
    private var events: List<Event>,
    private val onViewDetails: (Event) -> Unit,
    private val onDelete: (Event) -> Unit
) : RecyclerView.Adapter<EventsAdapterSix.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventNameTextView)
        val eventDate: TextView = itemView.findViewById(R.id.eventDateTextView)
        val eventLocation: TextView = itemView.findViewById(R.id.eventLocationTextView)
        val viewDetailsButton: Button = itemView.findViewById(R.id.viewDetailsButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_event_item_layout, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.eventName.text = event.name
        holder.eventDate.text = event.date
        holder.eventLocation.text = event.location

        holder.viewDetailsButton.setOnClickListener { onViewDetails(event) }
        holder.deleteButton.setOnClickListener { onDelete(event) }
    }

    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<Event>) {
        this.events = newEvents
        notifyDataSetChanged()
    }

    val currentEvents: List<Event>
        get() = events
}
