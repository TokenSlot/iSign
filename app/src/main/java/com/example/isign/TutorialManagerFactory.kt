package com.example.isign

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TutorialManagerFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TutorialManager::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TutorialManager(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}