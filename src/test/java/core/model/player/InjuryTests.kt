package core.model.player

import core.HOModelBuilder
import core.model.HOVerwaltung
import core.util.HODateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIsNot

class InjuryTests {
    @Test
    fun testInjury() {

        val hoAdmin = HOVerwaltung.instance()
        val hoModel = HOModelBuilder()
            .hrfId(42)
            .build()
        hoAdmin.model = hoModel

        val player = Player()
        val injury = Injury(player)
        assertEquals(false, injury.isInvalid)
        assertIsNot<HODateTime>(injury.whenHealthy)
        assertIsNot<HODateTime>(injury.whenSlightlyInjured)
        assertEquals(Injury.TypeOfEstimate.REALISTIC_ESTIMATE, injury.typeOfEstimate)
    }
}
