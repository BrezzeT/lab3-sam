package com.example.mobiki.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import java.util.concurrent.CopyOnWriteArrayList

class TimerService : Service() {
    
    private val binder = TimerBinder()
    private val observers = CopyOnWriteArrayList<TimerObserver>()
    
    private var startTime: Long = 0
    private var pausedTime: Long = 0
    private var isRunning = false
    private var isPaused = false
    
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    
    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    fun startTimer() {
        if (isRunning && !isPaused) return
        
        if (!isRunning) {
            // New start - reset everything
            startTime = System.currentTimeMillis()
            pausedTime = 0
            isRunning = true
            isPaused = false
            notifyStateChanged()
            startUpdates()
        } else if (isPaused) {
            // Resume from pause
            val pauseDuration = System.currentTimeMillis() - pausedTime
            startTime += pauseDuration
            isPaused = false
            isRunning = true
            notifyStateChanged()
            startUpdates()
        }
    }
    
    fun stopTimer() {
        isRunning = false
        isPaused = false
        startTime = 0
        pausedTime = 0
        stopUpdates()
        notifyStateChanged()
    }
    
    fun pauseTimer() {
        if (!isRunning || isPaused) return
        
        isPaused = true
        pausedTime = System.currentTimeMillis()
        stopUpdates()
        notifyStateChanged()
    }
    
    fun getCurrentTime(): Long {
        if (!isRunning) return 0
        if (isPaused) {
            return pausedTime - startTime
        }
        return System.currentTimeMillis() - startTime
    }
    
    fun isTimerRunning(): Boolean = isRunning && !isPaused
    
    fun isTimerPaused(): Boolean = isPaused
    
    fun registerObserver(observer: TimerObserver) {
        observers.add(observer)
        // Immediately notify current state
        observer.onTimerUpdate(getCurrentTime())
        observer.onTimerStateChanged(isRunning, isPaused)
    }
    
    fun unregisterObserver(observer: TimerObserver) {
        observers.remove(observer)
    }
    
    private fun startUpdates() {
        stopUpdates()
        updateRunnable = object : Runnable {
            override fun run() {
                if (isRunning && !isPaused) {
                    val elapsed = getCurrentTime()
                    notifyTimeUpdate(elapsed)
                    handler.postDelayed(this, 10) // Update every 10ms for smooth display
                }
            }
        }
        handler.post(updateRunnable!!)
    }
    
    private fun stopUpdates() {
        updateRunnable?.let {
            handler.removeCallbacks(it)
            updateRunnable = null
        }
    }
    
    private fun notifyTimeUpdate(elapsedTime: Long) {
        observers.forEach { it.onTimerUpdate(elapsedTime) }
    }
    
    private fun notifyStateChanged() {
        observers.forEach { it.onTimerStateChanged(isRunning, isPaused) }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopUpdates()
        observers.clear()
    }
}

