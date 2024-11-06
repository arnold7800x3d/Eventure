package com.example.eventure.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.eventure.R
import com.example.eventure.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        binding.loginButton.setOnClickListener {
            loginWithEmailAndPassword(
                binding.emailLogin.text.toString().trim(),
                binding.passwordLogin.text.toString().trim()
            )
        }
        binding.signUpButton1.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.passResetButton.setOnClickListener {
            showPasswordResetDialog()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showPasswordResetDialog() {
        // Dialog for entering email address
        val emailInput = EditText(this).apply {
            hint = "Enter your email"
        }

        AlertDialog.Builder(this)
            .setTitle("Password Reset")
            .setMessage("Enter your email to receive password reset instructions.")
            .setView(emailInput)
            .setPositiveButton("Send") { _, _ ->
                val email = emailInput.text.toString().trim()
                if (email.isNotEmpty()) {
                    resetPassword(email)
                } else {
                    Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetPassword(email: String) {
        // Firebase password reset functionality
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidUserException) {
                        // This error occurs when there's no account with this email
                        Toast.makeText(this, "No account found with this email.", Toast.LENGTH_LONG).show()
                    } else {
                        // Other potential errors, like network issues
                        Toast.makeText(this, "Error: ${exception?.message}", Toast.LENGTH_LONG).show()
                    }
                    Log.e("ResetPasswordError", "Failed to send reset email: ${exception?.message}")
                }
            }
    }

    // User login
    private fun loginWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    user?.let { fetchUserRole(it.uid) } // Fetch user role from Firestore
                } else {
                    // Handle exceptions
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthException) {
                        val errorCode = e.errorCode
                        Log.e(TAG, "Error Code: $errorCode")
                        // Handle FirebaseAuthException errors as before
                        when (errorCode) {
                            "ERROR_INVALID_EMAIL" -> {
                                Toast.makeText(
                                    this,
                                    "The email address is badly formatted.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            "ERROR_WRONG_PASSWORD" -> {
                                Toast.makeText(
                                    this,
                                    "The password is incorrect.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            "ERROR_USER_NOT_FOUND" -> {
                                Toast.makeText(
                                    this,
                                    "There is no user corresponding to this identifier.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            else -> {
                                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    } catch (e: FirebaseNetworkException) {
                        // Handle FirebaseNetworkException (network errors)
                        Log.e(TAG, "Network error during login: ${e.message}")
                        Toast.makeText(
                            this,
                            "Network error. Please check your connection.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        // Handle other exceptions
                        Log.e(TAG, "Unexpected error during login: ${e.message}")
                        Toast.makeText(this, "An unexpected error occurred.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
    }

    //Check if user is already signed in
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchUserRole(currentUser.uid) // Fetch user role if already signed in
        }
    }

    // Fetch user role from Firestore and navigate to the appropriate home screen
    private fun fetchUserRole(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
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

                        "Administrator" -> {
                            val intent = Intent(this, AdministratorHomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }

                        else -> {
                            Toast.makeText(this, "Unknown role: $role", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "Unknown role: $role")
                        }
                    }
                } else {
                    Log.d(TAG, "No such document")
                    Toast.makeText(this, "No user data found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error fetching user role", e)
            }
    }
}