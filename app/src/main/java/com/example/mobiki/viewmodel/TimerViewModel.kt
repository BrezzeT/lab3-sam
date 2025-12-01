package com.example.mobiki.viewmodel

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class TimerViewModel : ViewModel(), DefaultLifecycleObserver {
    
    // LiveData для часу таймера
    private val _elapsedTime = MutableLiveData<Long>(0)
    val elapsedTime: LiveData<Long> = _elapsedTime
    
    // LiveData для стану таймера
    private val _isRunning = MutableLiveData<Boolean>(false)
    val isRunning: LiveData<Boolean> = _isRunning
    
    private val _isPaused = MutableLiveData<Boolean>(false)
    val isPaused: LiveData<Boolean> = _isPaused
    
    // LiveData для прогресу синхронізації (у відсотках)
    private val _syncProgress = MutableLiveData<Int>(0)
    val syncProgress: LiveData<Int> = _syncProgress
    
    // LiveData для максимального часу синхронізації (для розрахунку прогресу)
    private val _maxSyncTime = MutableLiveData<Long>(60000) // 60 секунд за замовчуванням
    val maxSyncTime: LiveData<Long> = _maxSyncTime
    
    // LiveData для повідомлень про стан
    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage
    
    fun updateElapsedTime(time: Long) {
        _elapsedTime.postValue(time)
        // Оновлюємо прогрес синхронізації
        updateSyncProgress(time)
    }
    
    fun setRunning(running: Boolean) {
        _isRunning.postValue(running)
    }
    
    fun setPaused(paused: Boolean) {
        _isPaused.postValue(paused)
    }
    
    fun setMaxSyncTime(maxTime: Long) {
        _maxSyncTime.postValue(maxTime)
    }
    
    private fun updateSyncProgress(currentTime: Long) {
        val maxTime = _maxSyncTime.value ?: 60000
        if (maxTime > 0) {
            val progress = ((currentTime.toFloat() / maxTime.toFloat()) * 100).toInt().coerceIn(0, 100)
            _syncProgress.postValue(progress)
        }
    }
    
    fun setStatusMessage(message: String) {
        _statusMessage.postValue(message)
    }
    
    fun setSyncProgress(progress: Int) {
        _syncProgress.postValue(progress.coerceIn(0, 100))
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // Можна додати логіку при зупинці
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        // Очищення ресурсів
    }
    
    override fun onCleared() {
        super.onCleared()
        // Очищення при знищенні ViewModel
    }
}

