package com.example.eventure.adapterclass // Adjust the package name

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.dataclass.Event

class EventsAdapterTwo(
    private val eventsList: List<Event>,
    private val onViewClicked: (Event) -> Unit,
    private val onEditClicked: (Event) -> Unit,
    private val onDeleteClicked: (Event) -> Unit // Passing the delete logic here
) : RecyclerView.Adapter<EventsAdapterTwo.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventNameTextView)
        val eventDate: TextView = itemView.findViewById(R.id.eventDateTextView)
        val eventLocation: TextView = itemView.findViewById(R.id.eventLocationTextView)
        val viewEventButton: Button = itemView.findViewById(R.id.viewEventButton)
        val editEventButton: Button = itemView.findViewById(R.id.editEventButton)
        val deleteEventButton: Button = itemView.findViewById(R.id.deleteEventButton)

        init {
            viewEventButton.setOnClickListener {
                onViewClicked(eventsList[adapterPosition])
            }
            editEventButton.setOnClickListener {
                onEditClicked(eventsList[adapterPosition])
            }
            deleteEventButton.setOnClickListener {
                onDeleteClicked(eventsList[adapterPosition]) // Trigger delete
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_event_item_layout, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventsList[position]
        holder.eventName.text = event.name
        holder.eventDate.text = event.date
        holder.eventLocation.text = event.location
    }

    override fun getItemCount(): Int {
        return eventsList.size
    }
}

