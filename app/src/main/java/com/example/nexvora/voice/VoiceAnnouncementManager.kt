package com.example.nexvora.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class VoiceAnnouncementManager(context: Context) {
  private val TAG = "VoiceAnnouncement"
  private var tts: TextToSpeech? = null
  private var isInitialized = false
  private var pendingSpeech: String? = null

  init {
    tts = TextToSpeech(context.applicationContext) { status ->
      if (status == TextToSpeech.SUCCESS) {
        val result = tts?.setLanguage(Locale.getDefault())
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
          tts?.setLanguage(Locale.US)
        }
        isInitialized = true
        pendingSpeech?.let {
          speakNow(it)
          pendingSpeech = null
        }
      } else {
        Log.w(TAG, "TextToSpeech init failed with status: $status")
      }
    }
  }

  fun setSpeechRate(rate: Float) {
    tts?.setSpeechRate(rate)
  }

  fun announceAlarm(greeting: String, label: String, goal: String, routine: String) {
    val builder = StringBuilder()
    if (greeting.isNotBlank()) {
      builder.append(greeting).append(". ")
    }
    if (label.isNotBlank() && !label.equals("Alarm", ignoreCase = true)) {
      builder.append("It is time for ").append(label).append(". ")
    }
    if (goal.isNotBlank()) {
      builder.append("Your morning goal is: ").append(goal).append(". ")
    }
    if (routine.isNotBlank()) {
      builder.append("Next scheduled routine: ").append(routine).append(". ")
    }

    val message = builder.toString().trim()
    if (message.isNotEmpty()) {
      if (isInitialized) {
        speakNow(message)
      } else {
        pendingSpeech = message
      }
    }
  }

  fun announceGoalStart(goal: String) {
    val message = if (goal.isNotBlank()) {
      "Today's first goal, $goal, has started. Stay focused!"
    } else {
      "Good morning! Your day has officially begun."
    }
    if (isInitialized) {
      speakNow(message)
    } else {
      pendingSpeech = message
    }
  }

  private fun speakNow(text: String) {
    try {
      tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "NEXVORA_ALARM_TTS")
    } catch (e: Exception) {
      Log.e(TAG, "TTS speak failed: ${e.message}")
    }
  }

  fun stop() {
    try {
      tts?.stop()
    } catch (e: Exception) {
      Log.e(TAG, "TTS stop failed: ${e.message}")
    }
  }

  fun shutdown() {
    try {
      tts?.stop()
      tts?.shutdown()
      tts = null
    } catch (e: Exception) {
      Log.e(TAG, "TTS shutdown failed: ${e.message}")
    }
  }
}
