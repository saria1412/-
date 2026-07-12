package com.baynana.couplesgame

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChallengeBankTest {
    @Test
    fun bankContainsEveryBoldnessLevel() {
        assertTrue(ChallengeBank.all.any { it.boldness == Boldness.GENTLE })
        assertTrue(ChallengeBank.all.any { it.boldness == Boldness.BOLD })
        assertTrue(ChallengeBank.all.any { it.boldness == Boldness.BOLDER })
    }

    @Test
    fun gentleRoundOnlyContainsGentleCards() {
        val round = ChallengeBank.createRound(Boldness.GENTLE, 15, 42)
        assertEquals(15, round.size)
        assertTrue(round.all { it.boldness.rank <= Boldness.GENTLE.rank })
    }

    @Test
    fun bolderRoundHasVariedChallengeTypes() {
        val round = ChallengeBank.createRound(Boldness.BOLDER, 20, 7)
        assertTrue(round.map { it.type }.distinct().size >= 3)
    }
}
