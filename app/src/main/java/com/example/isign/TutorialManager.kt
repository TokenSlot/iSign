package com.example.isign

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.isign.fragment.TutorialFragment

class TutorialManager(
    context: Context
): ViewModel() {

    private val _gameState = MutableLiveData<TutorialState>(TutorialState.IDLE)
    val gameState: LiveData<TutorialState> = _gameState

    private val _isCorrect = MutableLiveData<Boolean>(false)
    val isCorrect: LiveData<Boolean> = _isCorrect

    fun stopTutorial() {
        _gameState.value = TutorialState.IDLE
    }

    fun startTutorial() {
        _gameState.value = TutorialState.IN_PROGRESS
    }

    fun handleUserInput(handSign: String, letterToCheck: String, fragment: TutorialFragment) {
        Log.d("KULAS","Letter: $letterToCheck, Sign: $handSign")
        _isCorrect.value = handSign.equals(letterToCheck, ignoreCase = true)
    }

    enum class TutorialState {
        IDLE,
        IN_PROGRESS,
    }
}