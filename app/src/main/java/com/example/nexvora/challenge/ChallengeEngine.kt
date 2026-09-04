package com.example.nexvora.challenge

import kotlin.random.Random

sealed class WakeUpChallenge {
  data class Math(
    val expression: String,
    val correctAnswer: Int,
    val options: List<Int>
  ) : WakeUpChallenge()

  data class Memory(
    val sequence: List<Int>, // 1..6
    val displayDurationMs: Long = 2500L
  ) : WakeUpChallenge()

  data class Pattern(
    val sequenceDisplay: String,
    val correctAnswer: Int,
    val options: List<Int>
  ) : WakeUpChallenge()

  data class QrMission(
    val expectedCode: String
  ) : WakeUpChallenge()
}

object ChallengeEngine {

  fun generateMathChallenge(difficulty: String = "MEDIUM"): WakeUpChallenge.Math {
    val random = Random.Default
    val (expr, ans) = when (difficulty.uppercase()) {
      "EASY" -> {
        val a = random.nextInt(3, 15)
        val b = random.nextInt(3, 15)
        if (random.nextBoolean()) {
          "$a + $b" to (a + b)
        } else {
          val maxVal = maxOf(a, b)
          val minVal = minOf(a, b)
          "$maxVal - $minVal" to (maxVal - minVal)
        }
      }
      "HARD" -> {
        val a = random.nextInt(6, 15)
        val b = random.nextInt(3, 10)
        val c = random.nextInt(5, 25)
        if (random.nextBoolean()) {
          "($a × $b) + $c" to ((a * b) + c)
        } else {
          "($a × $b) - $c" to ((a * b) - c)
        }
      }
      else -> { // MEDIUM
        val type = random.nextInt(3)
        when (type) {
          0 -> {
            val a = random.nextInt(12, 50)
            val b = random.nextInt(12, 50)
            "$a + $b" to (a + b)
          }
          1 -> {
            val a = random.nextInt(30, 99)
            val b = random.nextInt(10, 29)
            "$a - $b" to (a - b)
          }
          else -> {
            val a = random.nextInt(4, 11)
            val b = random.nextInt(4, 11)
            "$a × $b" to (a * b)
          }
        }
      }
    }

    val wrongOptions = mutableSetOf<Int>()
    var attempts = 0
    while (wrongOptions.size < 3 && attempts < 50) {
      attempts++
      val offset = random.nextInt(-10, 11)
      val fake = ans + offset
      if (fake != ans && fake >= 0) {
        wrongOptions.add(fake)
      }
    }
    while (wrongOptions.size < 3) {
      wrongOptions.add(ans + wrongOptions.size + 1)
    }

    val allOptions = (wrongOptions + ans).toList().shuffled()
    return WakeUpChallenge.Math(expr, ans, allOptions)
  }

  fun generateMemoryChallenge(difficulty: String = "MEDIUM"): WakeUpChallenge.Memory {
    val length = when (difficulty.uppercase()) {
      "EASY" -> 4
      "HARD" -> 6
      else -> 5
    }
    val sequence = (1..length).map { Random.nextInt(1, 7) }
    return WakeUpChallenge.Memory(sequence)
  }

  fun generatePatternChallenge(): WakeUpChallenge.Pattern {
    val patterns = listOf(
      // Arithmetic
      {
        val start = Random.nextInt(2, 10)
        val step = Random.nextInt(3, 8)
        val seq = (0..3).map { start + it * step }
        val ans = start + 4 * step
        WakeUpChallenge.Pattern("${seq.joinToString(", ")}, ?", ans, generateOptions(ans))
      },
      // Multiples of 2 or 3
      {
        val base = if (Random.nextBoolean()) 2 else 3
        val multiplier = Random.nextInt(2, 5)
        val seq = (1..4).map { it * multiplier * base }
        val ans = 5 * multiplier * base
        WakeUpChallenge.Pattern("${seq.joinToString(", ")}, ?", ans, generateOptions(ans))
      },
      // Squares
      {
        val start = Random.nextInt(1, 4)
        val seq = (start..(start + 3)).map { it * it }
        val ans = (start + 4) * (start + 4)
        WakeUpChallenge.Pattern("${seq.joinToString(", ")}, ?", ans, generateOptions(ans))
      },
      // Alternating
      {
        val a = Random.nextInt(5, 15)
        val b = Random.nextInt(20, 30)
        val seq = listOf(a, b, a + 2, b + 2)
        val ans = a + 4
        WakeUpChallenge.Pattern("${seq.joinToString(", ")}, ?", ans, generateOptions(ans))
      }
    )

    return patterns.random().invoke()
  }

  private fun generateOptions(ans: Int): List<Int> {
    val wrong = mutableSetOf<Int>()
    val offsets = listOf(-5, -2, 2, 4, -4, 5, 8, -8).shuffled()
    for (offset in offsets) {
      if (wrong.size >= 3) break
      val candidate = ans + offset
      if (candidate != ans && candidate > 0) {
        wrong.add(candidate)
      }
    }
    while (wrong.size < 3) {
      wrong.add(ans + wrong.size + 3)
    }
    return (wrong + ans).toList().shuffled()
  }
}
