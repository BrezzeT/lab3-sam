package com.example.mobiki.service

interface TimerObserver {
    fun onTimerUpdate(elapsedTime: Long)
    fun onTimerStateChanged(isRunning: Boolean, isPaused: Boolean)
}

