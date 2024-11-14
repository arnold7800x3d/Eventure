package com.example.eventure.adapterclass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.dataclass.Event

class EventsAdapterThree(
    private var eventsList: MutableList<Event>, // Use MutableList for dynamic updates
    private val onRegisterClicked: (Event) -> Unit // Callback for register button
) : RecyclerView.Adapter<EventsAdapterThree.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventName)
        val eventDate: TextView = itemView.findViewById(R.id.eventDate)
        //val eventLocation: TextView = itemView.findViewById(R.id.eventLocationTextView)
        val registerButton: Button = itemView.findViewById(R.id.registerButton2)

        init {
            registerButton.setOnClickListener {
                onRegisterClicked(eventsList[adapterPosition]) // Trigger register
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventsList[position]
        holder.eventName.text = event.name
        holder.eventDate.text = event.date
        //holder.eventLocation.text = event.location
    }

    override fun getItemCount(): Int {
        return eventsList.size
    }

    // Method to update the list and notify the adapter
    fun updateEvents(newEventsList: List<Event>) {
        eventsList.clear()
        eventsList.addAll(newEventsList)
        notifyDataSetChanged()
    }
}
