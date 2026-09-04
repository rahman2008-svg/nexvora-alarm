package com.example.nexvora.alarm.engine

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.*
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.sin

class AudioAndVibrationManager(private val context: Context) {
  private val TAG = "AudioAndVibManager"
  private var mediaPlayer: MediaPlayer? = null
  private var vibrator: Vibrator? = null
  private var audioManager: AudioManager? = null
  private var audioTrack: AudioTrack? = null
  private var volumeRampJob: Job? = null
  private var syntheticBeepJob: Job? = null
  private var strobeJob: Job? = null
  private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  init {
    audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  fun startAlarmSoundAndVibration(
    ringtoneUriStr: String,
    targetVolume: Float,
    isGentleWake: Boolean,
    gentleWakeMinutes: Int,
    shouldVibrate: Boolean,
    vibrationPatternType: String = "STANDARD",
    strobeFlash: Boolean = false
  ) {
    stop()

    // 1. Play Audio
    var audioStarted = false
    try {
      val audioUri: Uri = if (ringtoneUriStr.isNotBlank()) {
        Uri.parse(ringtoneUriStr)
      } else {
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
          ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
          ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      }

      mediaPlayer = MediaPlayer().apply {
        setDataSource(context, audioUri)
        setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        isLooping = true
        val startVolume = if (isGentleWake) 0.05f else targetVolume
        setVolume(startVolume, startVolume)
        prepare()
        start()
      }
      audioStarted = true

      if (isGentleWake && gentleWakeMinutes > 0) {
        rampVolume(targetVolume, gentleWakeMinutes)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Standard ringtone failed (${e.message}), trying fallback...")
      audioStarted = tryFallbackTone(targetVolume)
    }

    // If still not started, trigger synthetic pulse tone
    if (!audioStarted) {
      Log.i(TAG, "Starting synthetic alarm sound generator...")
      startSyntheticPulseTone(targetVolume)
    }

    // 2. Vibration with pattern
    if (shouldVibrate) {
      startVibration(vibrationPatternType)
    }

    // 3. Strobe Flashlight (optional for deep sleepers)
    if (strobeFlash) {
      startStrobeFlashlight()
    }
  }

  private fun rampVolume(targetVolume: Float, minutes: Int) {
    volumeRampJob?.cancel()
    volumeRampJob = coroutineScope.launch {
      val steps = 20
      val delayPerStep = ((minutes * 60 * 1000L) / steps).coerceAtLeast(500L)
      for (i in 1..steps) {
        delay(delayPerStep)
        val currentVol = (i.toFloat() / steps) * targetVolume
        mediaPlayer?.setVolume(currentVol, currentVol)
      }
    }
  }

  private fun tryFallbackTone(volume: Float): Boolean {
    return try {
      val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
      mediaPlayer = MediaPlayer().apply {
        setDataSource(context, defaultUri)
        setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        isLooping = true
        setVolume(volume, volume)
        prepare()
        start()
      }
      true
    } catch (e: Exception) {
      Log.e(TAG, "Fallback tone failed: ${e.message}")
      false
    }
  }

  /**
   * Guaranteed audio generator using AudioTrack PCM sine wave generation.
   * Rings reliably regardless of missing/unreadable device ringtones.
   */
  private fun startSyntheticPulseTone(volume: Float) {
    syntheticBeepJob?.cancel()
    syntheticBeepJob = coroutineScope.launch(Dispatchers.Default) {
      try {
        val sampleRate = 44100
        val durationMs = 200
        val numSamples = (sampleRate * durationMs) / 1000
        val sample = DoubleArray(numSamples)
        val generatedSnd = ByteArray(2 * numSamples)

        val freqOfTone = 950.0 // crisp, high-clarity alarm frequency
        for (i in 0 until numSamples) {
          sample[i] = sin(2.0 * Math.PI * i.toDouble() / (sampleRate / freqOfTone))
        }
        var idx = 0
        for (dVal in sample) {
          val valShort = (dVal * 32767).toInt().toShort()
          generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
          generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
        }

        val minBufferSize = AudioTrack.getMinBufferSize(
          sampleRate,
          AudioFormat.CHANNEL_OUT_MONO,
          AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_ALARM)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          .setAudioFormat(
            AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
              .setSampleRate(sampleRate)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(maxOf(generatedSnd.size, minBufferSize))
          .setTransferMode(AudioTrack.MODE_STATIC)
          .build()

        audioTrack?.write(generatedSnd, 0, generatedSnd.size)
        audioTrack?.setVolume(volume)

        while (isActive) {
          audioTrack?.reloadStaticData()
          audioTrack?.play()
          delay(220L)
          audioTrack?.stop()
          audioTrack?.reloadStaticData()
          audioTrack?.play()
          delay(220L)
          audioTrack?.stop()
          delay(800L) // rhythmic pulsing interval
        }
      } catch (e: Exception) {
        Log.e(TAG, "AudioTrack alarm tone failed: ${e.message}")
      }
    }
  }

  private fun startVibration(patternType: String) {
    try {
      val pattern = when (patternType.uppercase()) {
        "GENTLE" -> longArrayOf(0, 300, 800, 300, 1200)
        "ENERGETIC" -> longArrayOf(0, 150, 100, 150, 100, 150, 500)
        "HEARTBEAT" -> longArrayOf(0, 200, 150, 350, 800)
        else -> longArrayOf(0, 800, 400, 800, 1000) // STANDARD
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = VibrationEffect.createWaveform(pattern, 0)
        vibrator?.vibrate(effect)
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(pattern, 0)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Vibration failed: ${e.message}")
    }
  }

  private fun startStrobeFlashlight() {
    strobeJob?.cancel()
    strobeJob = coroutineScope.launch(Dispatchers.Default) {
      val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return@launch
      val cameraId = try {
        cameraManager.cameraIdList.firstOrNull { id ->
          cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
      } catch (_: Exception) {
        null
      } ?: return@launch

      var state = false
      while (isActive) {
        try {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager.setTorchMode(cameraId, state)
          }
          state = !state
          delay(300L)
        } catch (_: Exception) {
          break
        }
      }
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          cameraManager.setTorchMode(cameraId, false)
        }
      } catch (_: Exception) {}
    }
  }

  fun stop() {
    volumeRampJob?.cancel()
    volumeRampJob = null

    syntheticBeepJob?.cancel()
    syntheticBeepJob = null

    strobeJob?.cancel()
    strobeJob = null

    try {
      audioTrack?.apply {
        stop()
        release()
      }
    } catch (_: Exception) {}
    audioTrack = null

    try {
      mediaPlayer?.apply {
        if (isPlaying) {
          stop()
        }
        release()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error stopping media player: ${e.message}")
    } finally {
      mediaPlayer = null
    }

    try {
      vibrator?.cancel()
    } catch (e: Exception) {
      Log.e(TAG, "Error cancelling vibrator: ${e.message}")
    }

    // Turn off camera torch if left on
    try {
      val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
      cameraManager?.cameraIdList?.firstOrNull()?.let { id ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          cameraManager.setTorchMode(id, false)
        }
      }
    } catch (_: Exception) {}
  }
}
