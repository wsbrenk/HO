package core.model.player

import core.HOModelBuilder
import core.model.HOVerwaltung
import core.util.HODateTime
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIsNot

class InjuryTest {
    @Test
    fun testInjury() {

        val hoAdmin = HOVerwaltung.instance()
        val hoModel = HOModelBuilder()
            .hrfId(43)
            .build()
        hoAdmin.model = hoModel

        val player = Player()
        val injury = Injury(player)
        assertEquals(false, injury.isSportsInvalid)
        assertIsNot<HODateTime>(injury.whenHealthy)
        assertIsNot<HODateTime>(injury.whenSlightlyInjured)
        assertEquals(TypeOfRecoveryEstimation.REALISTIC_ESTIMATE, injury.typeOfEstimate)

        val dailyUpdates = hoModel.xtraDaten.dailyUpdates

        val expectedIsDisabledPlayer = mutableMapOf(
            1 to false,
            2 to false,
            3 to false,
            4 to true,
        )
        val expectedWhenHealthy = mutableMapOf(
            1 to dailyUpdates[0],
            2 to dailyUpdates[4].plus(7, ChronoUnit.DAYS),
            3 to dailyUpdates[2].plus(14 * 7, ChronoUnit.DAYS),
            4 to null,
        )
        val expectedWhenSlightlyInjured = mutableMapOf(
            1 to null,
            2 to dailyUpdates[4],
            3 to dailyUpdates[0].plus(9 * 7, ChronoUnit.DAYS),
            4 to null,
        )
        val expectedTypeOfEstimate = mutableMapOf(
            1 to TypeOfRecoveryEstimation.OPTIMISTIC_ESTIMATE,
            2 to TypeOfRecoveryEstimation.REALISTIC_ESTIMATE,
            3 to TypeOfRecoveryEstimation.PESSIMISTIC_ESTIMATE,
            4 to TypeOfRecoveryEstimation.PESSIMISTIC_ESTIMATE,
        )

        val players = hoModel.currentPlayers
        players.forEach { p ->
            run {
                val injury = Injury(p)
                assertEquals(expectedIsDisabledPlayer[p.playerId], injury.isSportsInvalid)
                assertEquals(expectedWhenHealthy[p.playerId], injury.whenHealthy)
                assertEquals(expectedWhenSlightlyInjured[p.playerId], injury.whenSlightlyInjured)
                assertEquals(expectedTypeOfEstimate[p.playerId], injury.typeOfEstimate)
            }
        }
    }
}
