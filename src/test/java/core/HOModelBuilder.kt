package core

import core.db.PersistenceManager
import core.file.hrf.HRF
import core.model.HOModel
import core.model.StaffMember
import core.model.StaffType
import core.model.Team
import core.model.XtraData
import core.model.match.MatchLineupTeam
import core.model.misc.Basics
import core.model.misc.Economy
import core.model.misc.Verein
import core.model.player.Player
import core.model.series.Liga
import core.util.HODateTime
import module.series.MatchFixtures
import tool.arenasizer.Stadium
import java.sql.Timestamp
import java.time.temporal.ChronoUnit

private const val referenceDate = "2026-06-30 14:00:00"

private var hrfs = mutableMapOf(
    42 to HRF(42,HODateTime.fromHT(referenceDate) ),
    43 to HRF(43, HODateTime.fromHT(referenceDate).plus(4, ChronoUnit.DAYS) ),
)

private const val DAYS_PER_SEASON = 112

class PlayerBuilder {
    private var birthday = HODateTime.fromHT(referenceDate).minus(17 * DAYS_PER_SEASON, ChronoUnit.DAYS)
    private var injuryLevel: Int = -1
    private var tsi:Int = 1000
    private var hrfId:Int = 42

    fun hrfId(id: Int): PlayerBuilder {
        hrfId = id
        return this
    }

    fun tsi(tsi: Int): PlayerBuilder {
        this.tsi = tsi
        return this
    }

    fun age(years:Int, days:Int): PlayerBuilder {
        this.birthday = HODateTime.fromHT(referenceDate).minus(years * DAYS_PER_SEASON + days, ChronoUnit.DAYS)
        return this
    }

    fun injuryLevel(injuryLevel: Int): PlayerBuilder {
        this.injuryLevel = injuryLevel
        return this
    }

    fun build(): Player {
        var ret = Player()
        ret.hrfId = hrfId
        var hrfDate = hrfs[hrfId]?.datum
        var age = HODateTime.HODuration.between(this.birthday, hrfDate)
        ret.age = age.seasons
        ret.ageDays = age.days
        ret.injuryWeeks = injuryLevel
        ret.tsi = tsi
        return ret
    }
}

class TestPersistenceManager : PersistenceManager {

    private var clubs = mutableMapOf(
        42 to TestClub(42),
        43 to TestClub(43),
    )

    private var teams = mutableMapOf(
        42 to Team()
    )
    private var basics = mutableMapOf(42 to Basics())
    private var players = mutableMapOf(42 to TestPlayers42())
    private var leagues = mutableMapOf(42 to Liga())
    private var stadiums = mutableMapOf(42 to Stadium())
    private var economies = mutableMapOf(42 to Economy())
    private var xtras = mutableMapOf(42 to XtraData())
    private var staffMembers = mutableMapOf(42 to StaffMember(42))
    private fun TestClub(hrfId: Int): Verein {
        var ret = Verein()
        ret.aerzte = 5
        ret.hrfId = hrfId
        ret.date = hrfs[hrfId]?.datum
        return ret
    }

    private fun TestPlayers42(): List<Player> {
        var ret = mutableListOf(
            PlayerBuilder().hrfId(42).build(),
            PlayerBuilder().hrfId(42).age(27, 0).build(),
            PlayerBuilder().hrfId(42).age(37, 0).build(),
        )
        return ret
    }

    private fun StaffMember(hrfId: Int) : List<StaffMember>{
        var doctor = StaffMember()
        doctor.hrfId = hrfId
        doctor.level = 5
        doctor.staffType = StaffType.MEDIC
        var ret = mutableListOf(
            doctor
        )
        return ret
    }

    override fun getMaxIdHrf(): HRF? {
        TODO("Not yet implemented")
    }

    override fun loadHRF(id: Int): HRF? {
        return hrfs[id]
    }

    override fun loadLatestHRFDownloadedBefore(date: Timestamp?): HRF? {
        return null
    }

    override fun getBasics(hrfId: Int): Basics? {
        return basics[hrfId]
    }

    override fun getVerein(hrfId: Int): Verein? {
        return clubs[hrfId]
    }

    override fun getTeam(hrfId: Int): Team? {
        return teams[hrfId]
    }

    override fun getEconomy(hrfId: Int): Economy? {
        return economies[hrfId]
    }

    override fun getLiga(hrfId: Int): Liga? {
        return leagues[hrfId]
    }

    override fun getStadion(hrfId: Int): Stadium? {
        return stadiums[hrfId]
    }

    override fun getXtraDaten(hrfId: Int): XtraData? {
        return xtras[hrfId]
    }

    override fun loadAllPlayers(): List<Player?>? {
        return getSpieler(42)
    }

    override fun getSpieler(hrfId: Int): List<Player?>? {
        return players[hrfId]
    }

    override fun loadHOConfigurationParameter(key: String?): String? {
        TODO("Not yet implemented")
    }

    override fun loadPreviousMatchLineup(teamId: Int): MatchLineupTeam? {
        return null
    }

    override fun loadNextMatchLineup(teamId: Int): MatchLineupTeam? {
        return null
    }

    override fun getLatestSpielplan(): MatchFixtures? {
        return null
    }

    override fun getStaffByHrfId(hrfId: Int): List<StaffMember?>? {
        return staffMembers[hrfId]
    }

    override fun getLatestPlayerDownloadBefore(
        playerId: Int,
        before: Timestamp?
    ): Player? {
        TODO("Not yet implemented")
    }
}

class HOModelBuilder {

    private var hrfId:Int = -1
    private var persistenceManager:PersistenceManager = TestPersistenceManager()

    fun hrfId(id: Int): HOModelBuilder {
        hrfId = id
        return this
    }

    fun persistenceManager(persistenceManager: PersistenceManager): HOModelBuilder {
        this.persistenceManager = persistenceManager
        return this
    }

    fun build():HOModel {
        return HOModel(hrfId, persistenceManager)
    }
}
