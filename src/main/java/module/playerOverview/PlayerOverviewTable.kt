package module.playerOverview

import core.db.DBManager
import core.gui.HOMainFrame
import core.gui.RefreshManager
import core.gui.Refreshable
import core.gui.comp.renderer.HODefaultTableCellRenderer
import core.gui.comp.table.FixedColumnsTable
import core.gui.model.PlayerOverviewTableModel
import core.gui.model.UserColumnController
import core.model.HOVerwaltung
import core.model.player.Player
import core.net.HattrickLink
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.Serial

/**
 * The Squad table, listing all the players on the team.
 *
 *
 * The actual model for that table is defined in [PlayerOverviewTableModel], which defines
 * all the columns to be displayed; the columns are initiated by a factory, [UserColumnFactory],
 * which in particular sets their preferred width.
 *
 *
 * Sorting in the table is handled by [TableSorter] which decorates the model, and is set
 * as the [javax.swing.table.TableModel] for this table.  Triggering sorting by a click sorts
 * the entries in the table model itself.  The new sorting order is then used by re-displaying the
 * table.  This approach differs from the “normal” Swing approach of using
 * [JTable.setRowSorter].
 *
 * @author Thorsten Dietz
 */
class PlayerOverviewTable : FixedColumnsTable(UserColumnController.instance().playerOverviewModel, 1), Refreshable {
    val playerTableModel: PlayerOverviewTableModel = scrollTable.model as PlayerOverviewTableModel

    init {
        playerTableModel.setValues(HOVerwaltung.instance().model.currentPlayers)
        playerTableModel.initTable(this)
        isOpaque = false
        setDefaultRenderer(Any::class.java, HODefaultTableCellRenderer())
        RefreshManager.instance().registerRefreshable(this)

        // Add a mouse listener that, when clicking on the “Last match” column
        // - opens the Hattrick page for the player if you shift-click,
        // - or opens the match in HO if you double-click.
        addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                val player: Player? = selectedPlayer
                if (player != null) {
                    // Last match column
                    val columnAtPoint = columnAtPoint(e.point)
                    // Get name of the actual column at columnAtPoint, i.e. post-ordering of the columns
                    // based on preferences.
                    val columnName = playerTableModel.getColumnName(columnAtPoint)
                    //					String columnName = PlayerOverviewTable.this.getColumnName(columnAtPoint);
                    val lastMatchRating = (HOVerwaltung.instance().getLanguageString("LastMatchRating"))

                    if (columnName != null && columnName.equals(lastMatchRating, ignoreCase = true)) {
                        if (e.isShiftDown) {
                            val matchId = player.lastMatchId
                            // TODO: get match type ?
                            val info = DBManager.instance().getMatchesKurzInfoByMatchID(matchId, null)
                            HattrickLink.showMatch(matchId.toString(), info.matchType.isOfficial)
                        } else if (e.clickCount == 2) {
                            HOMainFrame.instance().showMatch(player.lastMatchId)
                        }
                    }
                }
            }
        })
    }

    private fun columnAtPoint(point: Point): Int {
        val ret = scrollTable.columnAtPoint(point)
        if (ret > -1) {
            return ret + fixedColumnsCount
        }
        return fixedTable.columnAtPoint(point)
    }

    val selectedPlayer: Player?
        get() {
            val rowIndex = selectedRow
            if (rowIndex >= 0) {
                return playerTableModel.players[scrollTable.convertRowIndexToModel(rowIndex)]
            }
            return null
        }

    fun selectPlayer(playerId: Int) {
        val index = playerTableModel.getPlayerIndex(playerId)
        if (index >= 0) {
            this.setRowSelectionInterval(index, index)
        }
    }

    override fun reInit() {
        resetPlayers()
        repaint()
    }

    fun reInitModel() {
        playerTableModel.reInitData()
    }

    fun reInitModelHRFComparison() {
        playerTableModel.reInitDataHRFComparison()
    }

    override fun refresh() {
        reInitModel()
        repaint()
    }

    fun refreshHRFComparison() {
        reInitModelHRFComparison()
        repaint()
    }

    private fun resetPlayers() {
        playerTableModel.setValues(HOVerwaltung.instance().model.currentPlayers)
    }

    companion object {
        @Serial
        private val serialVersionUID = -6074136156090331418L
    }
}
