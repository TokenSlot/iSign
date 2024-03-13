package com.example.isign

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private var _delegate: Int = GestureRecognizerHelper.DELEGATE_CPU
    private var _minHandDetectionConfidence: Float =
        GestureRecognizerHelper.DEFAULT_HAND_DETECTION_CONFIDENCE
    private var _minHandTrackingConfidence: Float = GestureRecognizerHelper
        .DEFAULT_HAND_TRACKING_CONFIDENCE
    private var _minHandPresenceConfidence: Float = GestureRecognizerHelper
        .DEFAULT_HAND_PRESENCE_CONFIDENCE

    private var _dataText: String = "FSL Letter A"
    private var _dataImage: Int = R.drawable.fsl_letter_a
    private var _isGameMode: Boolean = false
    private var _hasScore: Boolean = false
    private var _currentScore: Long = 0

    val dataText: String get() = _dataText
    val dataImage: Int get() = _dataImage
    val isGameMode: Boolean get() = _isGameMode
    val hasScore: Boolean get() = _hasScore
    val currentScore: Long get() = _currentScore


    val currentDelegate: Int get() = _delegate
    val currentMinHandDetectionConfidence: Float
        get() =
            _minHandDetectionConfidence
    val currentMinHandTrackingConfidence: Float
        get() =
            _minHandTrackingConfidence
    val currentMinHandPresenceConfidence: Float
        get() =
            _minHandPresenceConfidence

    fun setDelegate(delegate: Int) {
        _delegate = delegate
    }
    fun setMinHandDetectionConfidence(confidence: Float) {
        _minHandDetectionConfidence = confidence
    }

    fun setMinHandTrackingConfidence(confidence: Float) {
        _minHandTrackingConfidence = confidence
    }

    fun setMinHandPresenceConfidence(confidence: Float) {
        _minHandPresenceConfidence = confidence
    }

    fun setListData(text: String, image: Int) {
        _dataText = text
        _dataImage = image
    }

    fun setIsGameMode(gameMode: Boolean) {
        _isGameMode = gameMode
    }

    fun setCurrentScore(score: Long) {
        _currentScore = score
    }
}