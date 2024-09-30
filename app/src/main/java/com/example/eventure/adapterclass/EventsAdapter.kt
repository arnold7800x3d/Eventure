import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.dataclass.Event

class EventsAdapter(private val eventsList: List<Event>) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventNameTextView)
        val eventDate: TextView = itemView.findViewById(R.id.eventDateTextView)
        val eventLocation: TextView = itemView.findViewById(R.id.eventLocationTextView)
        val eventDescription: TextView = itemView.findViewById(R.id.eventDescriptionTextView)
        val eventMaxAttendees: TextView = itemView.findViewById(R.id.eventMaxAttendeesTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item_layout, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventsList[position]
        holder.eventName.text = event.name
        holder.eventDate.text = event.date
        holder.eventLocation.text = event.location
        holder.eventDescription.text = event.description
        holder.eventMaxAttendees.text = "Max Attendees: ${event.maxAttendees}"
    }

    override fun getItemCount(): Int {
        return eventsList.size
    }
}
