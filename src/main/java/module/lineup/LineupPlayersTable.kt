package module.lineup

import core.db.DBManager
import core.gui.HOMainFrame
import core.gui.RefreshManager
import core.gui.Refreshable
import core.gui.comp.renderer.BooleanTableCellRenderer
import core.gui.comp.renderer.HODefaultTableCellRenderer
import core.gui.comp.table.FixedColumnsTable
import core.gui.model.UserColumnController
import core.gui.model.UserColumnFactory
import core.model.HOVerwaltung
import core.model.player.Player
import core.net.HattrickLink
import module.playerOverview.PlayerTable
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

/**
 * Table displaying the players' details in Lineup tab.
 * which is the same table class used in the Squad tab
 */
class LineupPlayersTable internal constructor() : FixedColumnsTable(UserColumnController.instance().lineupModel, 1),
    Refreshable, PlayerTable {
    @JvmField
	val tableModel: LineupTableModel = scrollTable.model as LineupTableModel

    init {
        tableModel.setValues(HOVerwaltung.instance().model.currentPlayers)
        tableModel.initTable(this)
        setDefaultRenderer(Any::class.java, HODefaultTableCellRenderer())
        setDefaultRenderer(Boolean::class.java, BooleanTableCellRenderer())
        RefreshManager.instance().registerRefreshable(this)
        initListeners()
    }

    override fun setPlayer(iPlayerID: Int) {
        val rowIndex = tableModel.getRowIndexOfPlayer(iPlayerID)
        if (rowIndex >= 0) {
            this.setRowSelectionInterval(rowIndex, rowIndex)
        }
    }

    override fun getPlayer(row: Int): Player? {
        return tableModel.getPlayerAtRow(row)
    }

    override fun reInit() {
        resetPlayers()
        repaint()
    }

    override fun refresh() {
        resetPlayers()
        repaint()
    }

    fun saveColumnOrder() {
        tableModel.storeUserSettings()
    }

    private fun resetPlayers() {
        tableModel.setValues(HOVerwaltung.instance().model.currentPlayers)
    }

    private fun initListeners() {
        addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                val rowindex = selectedRow
                if (rowindex >= 0) {
                    // Last match column
                    val selectedPlayer = tableModel.getPlayerAtRow(rowindex)
                    if (selectedPlayer != null) {
                        val scrollTable = scrollTable
                        val viewColumn = scrollTable.columnAtPoint(e.point)
                        if (viewColumn > -1) {
                            val column = scrollTable.getColumn(viewColumn).modelIndex
                            if (column + fixedColumnsCount == tableModel.getPositionInArray(UserColumnFactory.LAST_MATCH_RATING)) {
                                if (e.isShiftDown) {
                                    val matchId = selectedPlayer.lastMatchId
                                    val matchType = selectedPlayer.lastMatchType
                                    val info = DBManager.instance().getMatchesKurzInfoByMatchID(matchId, matchType)
                                    HattrickLink.showMatch(matchId.toString() + "", info.matchType.isOfficial)
                                } else if (e.clickCount == 2) {
                                    HOMainFrame.instance().showMatch(selectedPlayer.lastMatchId)
                                }
                            }
                        }
                    }
                }
            }
        })
    }
}