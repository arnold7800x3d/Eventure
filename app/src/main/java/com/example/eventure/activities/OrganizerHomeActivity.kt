package com.example.eventure.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.eventure.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class OrganizerHomeActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_organizer_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment2) as NavHostFragment
        navController = navHostFragment.navController

        // Set up the BottomNavigationView with NavController
        bottomNavigationView = findViewById(R.id.bottom_navigation_view2)
        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.organizerHomeFragment) // Navigate to HomeFragment
                    true
                }
                R.id.profile -> {
                    navController.navigate(R.id.organizerProfileFragment) // Navigate to ProfileFragment
                    true
                }
                else -> false
            }
        }

        // Listen for destination changes and hide/show the BottomNavigationView
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.createEventFragment -> bottomNavigationView.visibility = View.GONE
                else -> bottomNavigationView.visibility = View.VISIBLE
            }
        }
    }
}