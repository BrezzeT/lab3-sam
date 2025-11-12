package com.example.mobiki.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mobiki.MainActivity
import com.example.mobiki.R
import java.text.SimpleDateFormat
import java.util.*

class ClockFragment : Fragment() {
    
    private lateinit var yearTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var timezoneTextView: TextView
    private lateinit var timerButton: Button
    private lateinit var settingsButton: Button
    
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private var selectedTimezone: String = TimeZone.getDefault().id
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_clock, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        yearTextView = view.findViewById(R.id.tv_year)
        dateTextView = view.findViewById(R.id.tv_date)
        timeTextView = view.findViewById(R.id.tv_time)
        timezoneTextView = view.findViewById(R.id.tv_timezone)
        timerButton = view.findViewById(R.id.btn_timer)
        settingsButton = view.findViewById(R.id.btn_settings)
        
        timerButton.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(TimerFragment())
        }
        
        settingsButton.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(SettingsFragment())
        }
        
        // Load saved timezone from shared preferences
        val prefs = requireContext().getSharedPreferences("ClockPrefs", Context.MODE_PRIVATE)
        selectedTimezone = prefs.getString("timezone", TimeZone.getDefault().id) ?: TimeZone.getDefault().id
        
        startTimeUpdates()
    }
    
    override fun onResume() {
        super.onResume()
        // Reload timezone when returning from settings
        val prefs = requireContext().getSharedPreferences("ClockPrefs", Context.MODE_PRIVATE)
        selectedTimezone = prefs.getString("timezone", TimeZone.getDefault().id) ?: TimeZone.getDefault().id
        updateTime()
        // Restart updates if they were stopped
        if (updateRunnable == null) {
            startTimeUpdates()
        }
    }
    
    private fun startTimeUpdates() {
        updateRunnable = object : Runnable {
            override fun run() {
                updateTime()
                handler.postDelayed(this, 1000) // Update every second
            }
        }
        handler.post(updateRunnable!!)
    }
    
    private fun updateTime() {
        val timezone = TimeZone.getTimeZone(selectedTimezone)
        val calendar = Calendar.getInstance(timezone)
        
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        yearFormat.timeZone = timezone
        yearTextView.text = yearFormat.format(calendar.time)
        
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("uk", "UA"))
        dateFormat.timeZone = timezone
        dateTextView.text = dateFormat.format(calendar.time)
        
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        timeFormat.timeZone = timezone
        timeTextView.text = timeFormat.format(calendar.time)
        
        timezoneTextView.text = timezone.displayName
    }
    
    override fun onPause() {
        super.onPause()
        updateRunnable?.let { handler.removeCallbacks(it) }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        updateRunnable?.let { handler.removeCallbacks(it) }
    }
}

