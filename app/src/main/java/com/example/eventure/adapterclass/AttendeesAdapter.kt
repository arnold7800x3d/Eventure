package com.example.eventure.adapterclass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.dataclass.Attendee

class AttendeesAdapter(
    private val attendees: List<Attendee>
) : RecyclerView.Adapter<AttendeesAdapter.AttendeeViewHolder>() {

    inner class AttendeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val attendeeName: TextView = itemView.findViewById(R.id.attendeeNameTextView)
        val attendeeEmail: TextView = itemView.findViewById(R.id.attendeeEmailTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendeeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.attendee_item_layout, parent, false)
        return AttendeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendeeViewHolder, position: Int) {
        val attendee = attendees[position]
        holder.attendeeName.text = attendee.name
        holder.attendeeEmail.text = attendee.email
    }

    override fun getItemCount(): Int = attendees.size
}
