package com.example.mobiki.service

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import java.util.concurrent.CopyOnWriteArrayList

class TimerHandlerThread : HandlerThread("TimerThread") {
    
    private lateinit var handler: Handler
    private val observers = CopyOnWriteArrayList<TimerObserver>()
    
    private var startTime: Long = 0
    private var pausedTime: Long = 0
    private var isRunning = false
    private var isPaused = false
    
    private var updateRunnable: Runnable? = null
    
    override fun onLooperPrepared() {
        super.onLooperPrepared()
        handler = Handler(looper) { msg ->
            when (msg.what) {
                MSG_START -> {
                    startTimerInternal()
                    true
                }
                MSG_STOP -> {
                    stopTimerInternal()
                    true
                }
                MSG_PAUSE -> {
                    pauseTimerInternal()
                    true
                }
                else -> false
            }
        }
    }
    
    fun startTimer() {
        if (isRunning && !isPaused) return
        if (!::handler.isInitialized) return
        
        handler.post {
            startTimerInternal()
        }
    }
    
    fun stopTimer() {
        if (!::handler.isInitialized) return
        
        handler.post {
            stopTimerInternal()
        }
    }
    
    fun pauseTimer() {
        if (!::handler.isInitialized) return
        
        handler.post {
            pauseTimerInternal()
        }
    }
    
    private fun startTimerInternal() {
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
    
    private fun stopTimerInternal() {
        isRunning = false
        isPaused = false
        startTime = 0
        pausedTime = 0
        stopUpdates()
        notifyStateChanged()
    }
    
    private fun pauseTimerInternal() {
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
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post {
            observer.onTimerUpdate(getCurrentTime())
            observer.onTimerStateChanged(isRunning, isPaused)
        }
    }
    
    fun unregisterObserver(observer: TimerObserver) {
        observers.remove(observer)
    }
    
    private fun startUpdates() {
        stopUpdates()
        val mainHandler = Handler(Looper.getMainLooper())
        updateRunnable = object : Runnable {
            override fun run() {
                if (isRunning && !isPaused) {
                    val elapsed = getCurrentTime()
                    notifyTimeUpdate(elapsed)
                    mainHandler.postDelayed(this, 10) // Update every 10ms for smooth display
                }
            }
        }
        mainHandler.post(updateRunnable!!)
    }
    
    private fun stopUpdates() {
        val mainHandler = Handler(Looper.getMainLooper())
        updateRunnable?.let {
            mainHandler.removeCallbacks(it)
            updateRunnable = null
        }
    }
    
    private fun notifyTimeUpdate(elapsedTime: Long) {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post {
            observers.forEach { it.onTimerUpdate(elapsedTime) }
        }
    }
    
    private fun notifyStateChanged() {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post {
            observers.forEach { it.onTimerStateChanged(isRunning, isPaused) }
        }
    }
    
    override fun quitSafely(): Boolean {
        stopUpdates()
        observers.clear()
        return super.quitSafely()
    }
    
    companion object {
        private const val MSG_START = 1
        private const val MSG_STOP = 2
        private const val MSG_PAUSE = 3
    }
}

