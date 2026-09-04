package com.example

import com.example.nexvora.alarm.engine.AlarmScheduler
import com.example.nexvora.challenge.ChallengeEngine
import com.example.nexvora.data.model.AlarmEntity
import com.example.nexvora.data.repository.SleepRepository
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class NexVoraUnitTests {

  @Test
  fun testMathChallengeGeneration_Medium() {
    val challenge = ChallengeEngine.generateMathChallenge("MEDIUM")
    assertNotNull(challenge.expression)
    assertTrue("Expression should contain arithmetic operator",
      challenge.expression.contains("+") || challenge.expression.contains("-") || challenge.expression.contains("×")
    )
    assertEquals(4, challenge.options.size)
    assertTrue("Options must contain the correct answer", challenge.options.contains(challenge.correctAnswer))
  }

  @Test
  fun testMathChallengeGeneration_EasyAndHard() {
    val easy = ChallengeEngine.generateMathChallenge("EASY")
    assertTrue(easy.options.contains(easy.correctAnswer))

    val hard = ChallengeEngine.generateMathChallenge("HARD")
    assertTrue(hard.options.contains(hard.correctAnswer))
  }

  @Test
  fun testMemoryChallengeGeneration() {
    val easy = ChallengeEngine.generateMemoryChallenge("EASY")
    assertEquals(4, easy.sequence.size)

    val medium = ChallengeEngine.generateMemoryChallenge("MEDIUM")
    assertEquals(5, medium.sequence.size)

    val hard = ChallengeEngine.generateMemoryChallenge("HARD")
    assertEquals(6, hard.sequence.size)
  }

  @Test
  fun testPatternChallengeGeneration() {
    val pattern = ChallengeEngine.generatePatternChallenge()
    assertTrue(pattern.sequenceDisplay.contains("?"))
    assertEquals(4, pattern.options.size)
    assertTrue(pattern.options.contains(pattern.correctAnswer))
  }

  @Test
  fun testSleepDurationCalculation() {
    // 23:00 (11 PM) to 07:00 (7 AM) -> 8.0 hours
    val duration = SleepRepository.calculateTargetDurationHours(23, 0, 7, 0)
    assertEquals(8.0, duration, 0.01)

    // 22:30 (10:30 PM) to 06:30 (6:30 AM) -> 8.0 hours
    val duration2 = SleepRepository.calculateTargetDurationHours(22, 30, 6, 30)
    assertEquals(8.0, duration2, 0.01)

    // 01:00 (1 AM) to 08:30 (8:30 AM) -> 7.5 hours
    val duration3 = SleepRepository.calculateTargetDurationHours(1, 0, 8, 30)
    assertEquals(7.5, duration3, 0.01)
  }

  @Test
  fun testAlarmTriggerCalculation_FutureTime() {
    val now = Calendar.getInstance().timeInMillis
    val alarm = AlarmEntity(
      id = 1,
      timeHour = 6,
      timeMinute = 30,
      isEnabled = true,
      repeatDays = "1,2,3,4,5"
    )

    val nextTrigger = AlarmScheduler.calculateNextTriggerTime(alarm, now)
    assertTrue("Next trigger time must be in the future", nextTrigger > now)
  }
}
