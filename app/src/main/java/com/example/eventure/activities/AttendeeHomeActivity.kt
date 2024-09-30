package com.example.eventure.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
//import androidx.compose.ui.semantics.dialog
//import androidx.compose.ui.semantics.dismiss
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.eventure.R
import com.example.eventure.fragments.HomeFragment
import com.example.eventure.interfaces.ExitInterface
import com.google.android.material.bottomnavigation.BottomNavigationView

class AttendeeHomeActivity : AppCompatActivity() { //HomeFragment.ExitInterface {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.homeFragment) // Navigate to HomeFragment
                    true
                }
                R.id.profile -> {
                    navController.navigate(R.id.profileFragment) // Navigate to ProfileFragment
                    true
                }
                else -> false
            }
        }
    }

    /*override fun onBackPressed() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        if (navController.currentDestination?.id == R.id.homeFragment) {
            // Declare dialog outside the lambda
            val dialog = AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit the app?")
                .setPositiveButton("Yes") { _, _ ->
                    navController.popBackStack(R.id.homeFragment, true)
                    finishAndRemoveTask()
                    dialog.dismiss() // Now accessible
                }
                .setNegativeButton("No", null)
                .create()
            dialog.show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop any background tasks or processes here
    }*/
}
