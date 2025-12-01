package com.example.mobiki

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mobiki.fragments.ClockFragment
import com.example.mobiki.fragments.SettingsFragment
import com.example.mobiki.fragments.TimerFragment

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
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
}
