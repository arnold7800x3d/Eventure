package com.example.eventure.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.eventure.R
import com.example.eventure.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var selectedRole: String
    lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)

        // Initialize the role spinner
        val roleSpinner: Spinner = binding.roleSpinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.user_roles,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedRole = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }

        //bind sign up action to button
        binding.signUpButton2.setOnClickListener {
            createUser(
                binding.emailSignUp.text.toString().trim(),
                binding.passwordSignUp.text.toString().trim()
            )
        }

        //bind login action to button
        binding.loginButton2.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun createUser(email:String, password: String) {
//        val email = binding.emailSignUp.text.toString()
//        val password = binding.passwordSignUp.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    storeUserRole(user)
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Account creation failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun storeUserRole(user: FirebaseUser?) {
        user?.let {
            val userId = it.uid
            val userData = hashMapOf(
                "email" to it.email,
                "role" to selectedRole  // Save the selected role
            )

            db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener {
                    Log.d(TAG, "User role stored successfully.")
                    updateUI(user)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error storing user role", e)
                }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let {
            // Fetch the user's role from Firestore
            val userId = it.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val role = document.getString("role")

                        // Navigate to the appropriate home screen based on role
                        when (role) {
                            "Attendee" -> {
                                val intent = Intent(this, AttendeeHomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            "Organizer" -> {
                                val intent = Intent(this, OrganizerHomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
//                            "Administrator" -> {
//                                val intent = Intent(this, AdministratorHomeActivity::class.java)
//                                startActivity(intent)
//                                finish()
//                            }
                            else -> {
                                Toast.makeText(this, "Unknown role: $role", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, "Unknown role: $role")
                            }
                        }
                    } else {
                        Log.d(TAG, "No such document in Firestore")
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error fetching user role", e)
                }
        }
    }

}