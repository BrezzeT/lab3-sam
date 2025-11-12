package com.example.mobiki

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mobiki.fragments.ClockFragment
import com.example.mobiki.fragments.SettingsFragment
import com.example.mobiki.fragments.TimerFragment
import com.example.mobiki.service.TimerService

class MainActivity : AppCompatActivity() {
    
    private var timerService: TimerService? = null
    private var isServiceBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isServiceBound = true
            // Notify TimerFragment if it's already created
            findFragment<TimerFragment>()?.onServiceConnected(timerService!!)
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            isServiceBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Bind to TimerService (BIND_AUTO_CREATE will create the service if needed)
        Intent(this, TimerService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        
        // Handle back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = supportFragmentManager.fragments.firstOrNull()
                if (currentFragment is TimerFragment || currentFragment is SettingsFragment) {
                    // Return to ClockFragment
                    replaceFragment(ClockFragment())
                } else {
                    // If already on ClockFragment, exit app
                    finish()
                }
            }
        })
        
        // Start with ClockFragment
        if (savedInstanceState == null) {
            replaceFragment(ClockFragment())
        }
    }
    
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    fun getTimerService(): TimerService? = timerService
    
    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
    
    private inline fun <reified T : Fragment> findFragment(): T? {
        return supportFragmentManager.fragments.firstOrNull { it is T } as? T
    }
}
