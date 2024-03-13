package com.example.isign

import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.isign.fragment.CameraFragment

class GameManager (
    private val maxDifficulty: Int,
    context: Context
): ViewModel() {
    private val wordsByLength = WordDataLoader.loadWordData(context)
    private val difficultyLevels = wordsByLength.keys.sorted().withIndex().associate { (index, length) ->
        length to index + 1
    }

    private var currentDifficulty = 3
    private val difficultyThreshold = 0.7

    private var _currentWord = ""
    val currentWord: String get() = _currentWord

    private var lettersCorrect = BooleanArray(0)
    private var _letterIndex = 0
    val letterIndex: Int get() = _letterIndex

    private val _score = MutableLiveData(0L)
    val score: LiveData<Long> = _score

    private val _timeRemaining = MutableLiveData(30_000L)
    val timeRemaining: LiveData<Long> get() = _timeRemaining.map { it / 1000 }
    private val _gameState = MutableLiveData<GameState>(GameState.IDLE)
    val gameState: LiveData<GameState> = _gameState

    private var currentWordStartTime: Long? = null
    private var timer: CountDownTimer? = null

    fun stopRound() {
        _gameState.value = GameState.IDLE
        _score.value = 0
        timer?.cancel()
        _timeRemaining.value = 30_000L
    }

    fun startRound() {
        val wordsToPresent = difficultyLevels.flatMap { (length, level) ->
            if (level <= currentDifficulty && level <= maxDifficulty)
                wordsByLength[length]?.shuffled() ?: emptyList()
            else emptyList()
        }

        currentWordStartTime = System.currentTimeMillis()
        val wordData = wordsToPresent.randomOrNull() ?: return
        setCurrentWord(wordData.word)
        lettersCorrect = BooleanArray(currentWord.length) { false }
        resetLetterIndex()

        _score.value = 0
        _gameState.value = GameState.IN_PROGRESS
        _timeRemaining.value = 30_000L

        startTimer()
    }

    fun nextRound() {
        val wordsToPresent = difficultyLevels.flatMap { (length, level) ->
            if (level <= currentDifficulty && level <= maxDifficulty)
                wordsByLength[length]?.shuffled() ?: emptyList()
            else emptyList()
        }

        currentWordStartTime = System.currentTimeMillis()
        val wordData = wordsToPresent.randomOrNull() ?: return
        setCurrentWord(wordData.word)
        lettersCorrect = BooleanArray(currentWord.length) { false }
        resetLetterIndex()

        _gameState.value = GameState.IN_PROGRESS
        startTimer()
    }

     fun handleUserInput(handSign: String, fragment: CameraFragment) {
         if (_gameState.value != GameState.IN_PROGRESS) return
         if (letterIndex == currentWord.length) return

         if (handSign.equals(currentWord[letterIndex].toString(), ignoreCase = true) || currentWord[letterIndex].toString() == "-") {
             lettersCorrect[letterIndex] = true
             addLetterIndex()

             if (letterIndex == currentWord.length) {
                 val userPerformance = lettersCorrect.count { it } / currentWord.length.toDouble()
                 val wordData = wordsByLength.values.flatten().find { it.word == currentWord } ?: return
                 val points = wordData.points
                 val timeBonus = (currentWord.length * 1000L)

                 _score.value = (_score.value ?: 0) + points
                 _timeRemaining.value = _timeRemaining.value?.plus(timeBonus)

                 adjustDifficultyLevel(userPerformance)
                 _gameState.value = GameState.WORD_COMPLETED
             } else {
                 fragment.handleNextLetter()
             }
         }
     }

    private fun adjustDifficultyLevel(userPerformance: Double) {
        currentDifficulty = if (userPerformance >= difficultyThreshold) {
            (currentDifficulty + 1).coerceAtMost(difficultyLevels.size)
        } else {
            (currentDifficulty - 1).coerceAtLeast(1)
        }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(_timeRemaining.value ?: 0, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeRemaining.value = millisUntilFinished
            }

            override fun onFinish() {
                _gameState.value = GameState.TIME_UP
            }
        }.start()
    }

    private fun setCurrentWord(word: String) {
        _currentWord = word
    }

    private fun resetLetterIndex() {
        _letterIndex = 0
    }
    private fun addLetterIndex() {
        _letterIndex += 1
    }

    enum class GameState {
        IDLE,
        IN_PROGRESS,
        WORD_COMPLETED,
        TIME_UP
    }
}