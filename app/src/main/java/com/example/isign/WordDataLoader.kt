package com.example.isign

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

object WordDataLoader {
    fun loadWordData(context: Context): Map<Int, List<WordData>> {
        val wordsByLength = mutableMapOf<Int, MutableList<WordData>>()

        context.assets.open("words.csv").use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.useLines { lines ->
                lines.forEach { line ->
                    val (word, points, length) = line.split(",")
                    val wordLength = length.trim().toInt()
                    val wordData = WordData(word, points.trim().toInt())
                    wordsByLength.getOrPut(wordLength) { mutableListOf() }.add(wordData)
                }
            }
        }

        return wordsByLength
    }
    data class WordData(val word: String, val points: Int)
}