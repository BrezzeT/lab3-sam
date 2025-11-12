package com.example.mobiki.fragments

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mobiki.MainActivity
import com.example.mobiki.R
import com.example.mobiki.service.TimerObserver
import com.example.mobiki.service.TimerService
import java.util.Locale

class TimerFragment : Fragment(), TimerObserver {
    
    private lateinit var timeDisplay: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var pauseButton: Button
    private lateinit var lapButton: Button
    private lateinit var lapsContainer: LinearLayout
    private lateinit var lapsTitle: TextView
    
    private var timerService: TimerService? = null
    private var isServiceBound = false
    private val laps = mutableListOf<Long>()
    private var lastLapTime: Long = 0
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isServiceBound = true
            timerService?.registerObserver(this@TimerFragment)
            // Update UI with current state
            activity?.runOnUiThread {
                val isRunning = timerService?.isTimerRunning() ?: false
                val isPaused = timerService?.isTimerPaused() ?: false
                if (isRunning || isPaused) {
                    timeDisplay.text = formatTime(timerService?.getCurrentTime() ?: 0)
                }
                updateButtonStates(isRunning, isPaused)
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            isServiceBound = false
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        timeDisplay = view.findViewById(R.id.tv_timer_display)
        startButton = view.findViewById(R.id.btn_start)
        stopButton = view.findViewById(R.id.btn_stop)
        pauseButton = view.findViewById(R.id.btn_pause)
        lapButton = view.findViewById(R.id.btn_lap)
        lapsContainer = view.findViewById(R.id.ll_laps_container)
        lapsTitle = view.findViewById(R.id.tv_laps_title)
        
        startButton.setOnClickListener {
            // If timer was stopped, reset laps
            val wasStopped = !(timerService?.isTimerRunning() ?: false) && !(timerService?.isTimerPaused() ?: false)
            if (wasStopped) {
                clearLaps()
                lastLapTime = 0
            }
            timerService?.startTimer()
            // Force update button states immediately
            val isRunning = timerService?.isTimerRunning() ?: false
            val isPaused = timerService?.isTimerPaused() ?: false
            updateButtonStates(isRunning, isPaused)
        }
        
        stopButton.setOnClickListener {
            timerService?.stopTimer()
            clearLaps()
        }
        
        pauseButton.setOnClickListener {
            if (timerService?.isTimerPaused() == true) {
                timerService?.startTimer() // Resume
            } else {
                timerService?.pauseTimer()
            }
        }
        
        lapButton.setOnClickListener {
            val currentTime = timerService?.getCurrentTime() ?: 0
            if (currentTime > 0) {
                val lapTime = currentTime - lastLapTime
                laps.add(lapTime)
                lastLapTime = currentTime
                updateLapsDisplay()
            }
        }
        
        // Try to get service from MainActivity
        val mainActivity = activity as? MainActivity
        timerService = mainActivity?.getTimerService()
        if (timerService != null) {
            timerService?.registerObserver(this)
            // Update UI with current state
            val isRunning = timerService?.isTimerRunning() ?: false
            val isPaused = timerService?.isTimerPaused() ?: false
            if (isRunning || isPaused) {
                timeDisplay.text = formatTime(timerService?.getCurrentTime() ?: 0)
            }
            updateButtonStates(isRunning, isPaused)
        } else {
            // Bind to service if not available
            val intent = android.content.Intent(requireContext(), TimerService::class.java)
            requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            // Set initial state
            updateButtonStates(false, false)
        }
        
        updateLapsDisplay()
    }
    
    fun onServiceConnected(service: TimerService) {
        timerService = service
        service.registerObserver(this)
        // Update UI with current state
        val isRunning = service.isTimerRunning()
        val isPaused = service.isTimerPaused()
        if (isRunning || isPaused) {
            timeDisplay.text = formatTime(service.getCurrentTime())
        }
        updateButtonStates(isRunning, isPaused)
    }
    
    override fun onTimerUpdate(elapsedTime: Long) {
        activity?.runOnUiThread {
            timeDisplay.text = formatTime(elapsedTime)
        }
    }
    
    override fun onTimerStateChanged(isRunning: Boolean, isPaused: Boolean) {
        activity?.runOnUiThread {
            updateButtonStates(isRunning, isPaused)
            if (!isRunning && !isPaused) {
                // Timer stopped
                timeDisplay.text = formatTime(0)
                lastLapTime = 0
            } else if (isRunning && !isPaused && lastLapTime == 0L) {
                // Timer just started - initialize lastLapTime to 0 for first lap
                lastLapTime = 0
            }
        }
    }
    
    private fun updateButtonStates(isRunning: Boolean, isPaused: Boolean) {
        // Update button states
        startButton.isEnabled = !isRunning || isPaused
        stopButton.isEnabled = isRunning || isPaused
        pauseButton.isEnabled = isRunning || isPaused
        lapButton.isEnabled = isRunning && !isPaused
        
        // Update button text
        if (isPaused) {
            pauseButton.text = getString(R.string.resume)
        } else {
            pauseButton.text = getString(R.string.pause)
        }
        
        // Force refresh
        startButton.invalidate()
        stopButton.invalidate()
        pauseButton.invalidate()
        lapButton.invalidate()
    }
    
    private fun clearLaps() {
        laps.clear()
        lastLapTime = 0
        updateLapsDisplay()
    }
    
    private fun updateLapsDisplay() {
        lapsContainer.removeAllViews()
        
        if (laps.isEmpty()) {
            lapsTitle.text = getString(R.string.no_laps)
            return
        }
        
        lapsTitle.text = getString(R.string.laps)
        
        laps.forEachIndexed { index, lapTime ->
            val lapView = TextView(requireContext()).apply {
                text = "Коло ${index + 1}: ${formatTime(lapTime)}"
                textSize = 16f
                setPadding(32, 16, 32, 16)
            }
            lapsContainer.addView(lapView)
        }
    }
    
    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        val milliseconds = (millis % 1000) / 10
        
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d", hours, minutes, seconds, milliseconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d.%02d", minutes, seconds, milliseconds)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        timerService?.unregisterObserver(this)
        if (isServiceBound) {
            requireContext().unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}

