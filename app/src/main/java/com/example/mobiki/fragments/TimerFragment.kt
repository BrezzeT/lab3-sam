package com.example.mobiki.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.mobiki.R
import com.example.mobiki.service.TimerHandlerThread
import com.example.mobiki.service.TimerObserver
import com.example.mobiki.viewmodel.TimerViewModel
import java.util.Locale

class TimerFragment : Fragment(), TimerObserver {
    
    private lateinit var timeDisplay: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var pauseButton: Button
    private lateinit var lapButton: Button
    private lateinit var lapsContainer: LinearLayout
    private lateinit var lapsTitle: TextView
    private lateinit var syncProgressText: TextView
    private lateinit var syncProgressBar: ProgressBar
    
    private val viewModel: TimerViewModel by viewModels()
    private var timerThread: TimerHandlerThread? = null
    private val laps = mutableListOf<Long>()
    private var lastLapTime: Long = 0
    
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
        syncProgressText = view.findViewById(R.id.tv_sync_progress)
        syncProgressBar = view.findViewById(R.id.progress_sync)
        
        // Ініціалізуємо HandlerThread
        timerThread = TimerHandlerThread().apply {
            start()
        }
        timerThread?.registerObserver(this)
        
        // Спостерігаємо за LiveData з ViewModel
        observeViewModel()
        
        // Налаштування кнопок
        setupButtons()
        
        updateLapsDisplay()
        
        // Встановлюємо максимальний час синхронізації (60 секунд)
        viewModel.setMaxSyncTime(60000)
    }
    
    private fun observeViewModel() {
        // Спостерігаємо за часом таймера
        viewModel.elapsedTime.observe(this, Observer { time ->
            timeDisplay.text = formatTime(time)
        })
        
        // Спостерігаємо за станом таймера
        viewModel.isRunning.observe(this, Observer { running ->
            updateButtonStates(running, viewModel.isPaused.value ?: false)
        })
        
        viewModel.isPaused.observe(this, Observer { paused ->
            updateButtonStates(viewModel.isRunning.value ?: false, paused)
        })
        
        // Спостерігаємо за прогресом синхронізації
        viewModel.syncProgress.observe(this, Observer { progress ->
            syncProgressBar.progress = progress
            syncProgressText.text = getString(R.string.sync_progress, progress)
        })
        
        // Спостерігаємо за повідомленнями
        viewModel.statusMessage.observe(this, Observer { message ->
            if (message.isNotEmpty()) {
                // Можна показати Toast або інше повідомлення
            }
        })
    }
    
    private fun setupButtons() {
        startButton.setOnClickListener {
            val wasStopped = !(timerThread?.isTimerRunning() ?: false) && !(timerThread?.isTimerPaused() ?: false)
            if (wasStopped) {
                clearLaps()
                lastLapTime = 0
                viewModel.setMaxSyncTime(60000) // Скидаємо максимальний час
            }
            timerThread?.startTimer()
        }
        
        stopButton.setOnClickListener {
            timerThread?.stopTimer()
            clearLaps()
            viewModel.updateElapsedTime(0)
            viewModel.setSyncProgress(0)
        }
        
        pauseButton.setOnClickListener {
            if (timerThread?.isTimerPaused() == true) {
                timerThread?.startTimer() // Resume
            } else {
                timerThread?.pauseTimer()
            }
        }
        
        lapButton.setOnClickListener {
            val currentTime = timerThread?.getCurrentTime() ?: 0
            if (currentTime > 0) {
                val lapTime = currentTime - lastLapTime
                laps.add(lapTime)
                lastLapTime = currentTime
                updateLapsDisplay()
            }
        }
    }
    
    override fun onTimerUpdate(elapsedTime: Long) {
        viewModel.updateElapsedTime(elapsedTime)
    }
    
    override fun onTimerStateChanged(isRunning: Boolean, isPaused: Boolean) {
        viewModel.setRunning(isRunning)
        viewModel.setPaused(isPaused)
        
        if (!isRunning && !isPaused) {
            viewModel.updateElapsedTime(0)
            viewModel.setSyncProgress(0)
            lastLapTime = 0
        }
    }
    
    private fun updateButtonStates(isRunning: Boolean, isPaused: Boolean) {
        startButton.isEnabled = !isRunning || isPaused
        stopButton.isEnabled = isRunning || isPaused
        pauseButton.isEnabled = isRunning || isPaused
        lapButton.isEnabled = isRunning && !isPaused
        
        if (isPaused) {
            pauseButton.text = getString(R.string.resume)
        } else {
            pauseButton.text = getString(R.string.pause)
        }
        
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
                setTextColor(0xFF212121.toInt())
                setPadding(24, 18, 24, 18)
                setBackgroundColor(0xFFF5F5F5.toInt())
            }
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
            lapView.layoutParams = layoutParams
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
        timerThread?.unregisterObserver(this)
        timerThread?.quitSafely()
        timerThread = null
    }
}
