package core.model.player

import core.HOModelBuilder
import core.db.PersistenceManager
import core.file.hrf.HRF
import core.model.HOVerwaltung
import core.model.StaffMember
import core.model.Team
import core.model.XtraData
import core.model.match.MatchLineupTeam
import core.model.misc.Basics
import core.model.misc.Economy
import core.model.misc.Verein
import core.model.series.Liga
import core.util.HODateTime
import module.series.MatchFixtures
import tool.arenasizer.Stadium
import java.sql.Timestamp
import kotlin.test.Test
import kotlin.test.assertEquals

class TestPersistenceManager : PersistenceManager {
    override fun getMaxIdHrf(): HRF? {
        TODO("Not yet implemented")
    }

    override fun loadHRF(id: Int): HRF? {
        TODO("Not yet implemented")
    }

    override fun loadLatestHRFDownloadedBefore(date: Timestamp?): HRF? {
        TODO("Not yet implemented")
    }

    override fun getBasics(hrfId: Int): Basics? {
        TODO("Not yet implemented")
    }

    override fun getVerein(hrfId: Int): Verein? {
        TODO("Not yet implemented")
    }

    override fun getTeam(hrfId: Int): Team? {
        TODO("Not yet implemented")
    }

    override fun getEconomy(hrfId: Int): Economy? {
        TODO("Not yet implemented")
    }

    override fun getLiga(hrfId: Int): Liga? {
        TODO("Not yet implemented")
    }

    override fun getStadion(hrfId: Int): Stadium? {
        TODO("Not yet implemented")
    }

    override fun getXtraDaten(hrfId: Int): XtraData? {
        TODO("Not yet implemented")
    }

    override fun loadAllPlayers(): List<Player?>? {
        TODO("Not yet implemented")
    }

    override fun getSpieler(hrfId: Int): List<Player?>? {
        TODO("Not yet implemented")
    }

    override fun loadHOConfigurationParameter(key: String?): String? {
        TODO("Not yet implemented")
    }

    override fun loadPreviousMatchLineup(teamId: Int): MatchLineupTeam? {
        TODO("Not yet implemented")
    }

    override fun loadNextMatchLineup(teamId: Int): MatchLineupTeam? {
        TODO("Not yet implemented")
    }

    override fun getLatestSpielplan(): MatchFixtures? {
        TODO("Not yet implemented")
    }

    override fun getStaffByHrfId(hrfId: Int): List<StaffMember?>? {
        TODO("Not yet implemented")
    }

    override fun getLatestPlayerDownloadBefore(
        playerId: Int,
        before: Timestamp?
    ): Player? {
        TODO("Not yet implemented")
    }

}
class InjuryTests {
    @Test
    fun testInjury() {

        val hoAdmin = HOVerwaltung.instance()
        val hoModel = HOModelBuilder()
            .hrfId(42)
            .persistenceManager(TestPersistenceManager())
            .build()
        hoAdmin.model = hoModel


        val player = Player()
        val injury = Injury(player);
        assertEquals(false, injury.isInvalid)
        assertEquals(HODateTime.fromHT("2026-01-02 18:00:00"), injury.whenHealthy);
        assertEquals(HODateTime.fromHT("2026-01-02 18:00:00"), injury.whenSlightlyInjured);
        assertEquals(Injury.TypeOfEstimate.REALISTIC_ESTIMATE, injury.typeOfEstimate);

    }
}
