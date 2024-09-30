package com.example.eventure.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.eventure.R
import com.example.eventure.interfaces.ExitInterface

class HomeFragment : Fragment() {
    /*private var exitInterface: ExitInterface? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ExitInterface) {
            exitInterface = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        exitInterface = null
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    /*private fun handleOnBackPressed() {
        exitInterface?.onBackPressed()
    }*/
}