package com.example.isign

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.isign.fragment.GestureRecognizerResultsAdapter
import java.lang.IllegalArgumentException

class GameManagerFactory(
    private val maxDifficulty: Int,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameManager::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameManager(maxDifficulty, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}