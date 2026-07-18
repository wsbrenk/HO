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
        assertEquals(false, injury.isDisabledPlayer)
        assertIsNot<HODateTime>(injury.whenHealthy)
        assertIsNot<HODateTime>(injury.whenSlightlyInjured)
        assertEquals(Injury.TypeOfEstimate.REALISTIC_ESTIMATE, injury.typeOfEstimate)

        val dailyUpdates =  hoModel.xtraDaten.dailyUpdates

        val expectedIsInvalid = mutableMapOf( 1 to false, 2 to false, 3 to false)
        val expectedWhenHealthy = mutableMapOf(
            1 to dailyUpdates[2],
            2 to dailyUpdates[4].plus(7, ChronoUnit.DAYS),
            3 to dailyUpdates[4].plus(13*7, ChronoUnit.DAYS),
        )
        val expectedWhenSlightlyInjured = mutableMapOf(
            1 to null,
            2 to dailyUpdates[4],
            3 to dailyUpdates[3].plus(8*7, ChronoUnit.DAYS),
        )
        val expectedTypeOfEstimate = mutableMapOf( 1 to Injury.TypeOfEstimate.REALISTIC_ESTIMATE, 2 to Injury.TypeOfEstimate.REALISTIC_ESTIMATE, 3 to Injury.TypeOfEstimate.REALISTIC_ESTIMATE)

        val players = hoModel.currentPlayers
        players.forEach {p-> run{
            val injury = Injury(p)
            assertEquals(expectedIsInvalid[p.playerId], injury.isDisabledPlayer)
            assertEquals(expectedWhenHealthy[p.playerId], injury.whenHealthy)
            assertEquals(expectedWhenSlightlyInjured[p.playerId], injury.whenSlightlyInjured)
            assertEquals(expectedTypeOfEstimate[p.playerId], injury.typeOfEstimate)
        }}
    }
}
