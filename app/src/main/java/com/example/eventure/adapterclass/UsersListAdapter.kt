package com.example.eventure.adapterclass

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.eventure.R
import com.example.eventure.dataclass.User
import com.google.firebase.firestore.FirebaseFirestore

class UsersListAdapter(
    val usersList: MutableList<User>,
    private val context: Context,
    private val onDelete: (User) -> Unit
) : RecyclerView.Adapter<UsersListAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = usersList[position]
        holder.userEmailTextView.text = user.email

        holder.deleteUserButton.setOnClickListener {
            onDelete(user)
        }
    }

    override fun getItemCount(): Int = usersList.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userEmailTextView: TextView = itemView.findViewById(R.id.userEmailTextView)
        val deleteUserButton: Button = itemView.findViewById(R.id.deleteUserButton)
    }

    fun updateUsers(updatedUsers: List<User>) {
        usersList.clear()
        usersList.addAll(updatedUsers)
        notifyDataSetChanged()
    }
}
