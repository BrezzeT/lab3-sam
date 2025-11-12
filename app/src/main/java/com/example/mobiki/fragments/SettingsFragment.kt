package com.example.mobiki.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mobiki.R
import java.util.*

class SettingsFragment : Fragment() {
    
    private lateinit var timezoneSpinner: Spinner
    private lateinit var titleTextView: TextView
    
    private val timezones = arrayOf(
        "Europe/Kiev",
        "Europe/London",
        "Europe/Paris",
        "Europe/Berlin",
        "Europe/Moscow",
        "America/New_York",
        "America/Los_Angeles",
        "Asia/Tokyo",
        "Asia/Shanghai",
        "Australia/Sydney",
        "UTC"
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        titleTextView = view.findViewById(R.id.tv_settings_title)
        timezoneSpinner = view.findViewById(R.id.spinner_timezone)
        
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            timezones.map { getTimezoneDisplayName(it) }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timezoneSpinner.adapter = adapter
        
        // Load current timezone
        val prefs = requireContext().getSharedPreferences("ClockPrefs", Context.MODE_PRIVATE)
        val currentTimezone = prefs.getString("timezone", TimeZone.getDefault().id) ?: TimeZone.getDefault().id
        val currentIndex = timezones.indexOfFirst { it == currentTimezone }
        if (currentIndex >= 0) {
            timezoneSpinner.setSelection(currentIndex)
        }
        
        // Save on selection change
        timezoneSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedTimezone = timezones[position]
                val prefs = requireContext().getSharedPreferences("ClockPrefs", Context.MODE_PRIVATE)
                prefs.edit().putString("timezone", selectedTimezone).apply()
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }
    
    private fun getTimezoneDisplayName(timezoneId: String): String {
        val tz = TimeZone.getTimeZone(timezoneId)
        val offset = tz.rawOffset / (1000 * 60 * 60) // hours
        val sign = if (offset >= 0) "+" else ""
        return "$timezoneId (UTC$sign$offset)"
    }
}

