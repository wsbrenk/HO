package core.gui.comp.table

import core.gui.comp.renderer.HODefaultTableCellRenderer
import core.model.HOConfigurationIntParameter
import java.beans.PropertyChangeEvent
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionListener
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableRowSorter

open class FixedColumnsTable(
    tableModel: HOTableModel,
    /**
     * Number of fixed columns in table
     */
    val fixedColumnsCount: Int
) : JScrollPane() {
    /**
     * Return the number of fixed columns
     * @return int
     */

    private val dividerLocation: HOConfigurationIntParameter

    /**
     * Return the created table sorter
     * @return DefaultTableSorter
     */
    /**
     * Table sorter
     */
    @JvmField
    val tableRowSorter: TableRowSorter<HOTableModel> = TableRowSorter(tableModel)

    /**
     * Returns the Locked LeftTable
     *
     * @return JTable
     */
    /**
     * Fixed table part (left hand side)
     */
    val fixedTable: JTable

    /**
     * Returns the Scrollable RightTable
     *
     * @return JTable
     */
    /**
     * Scrollable table part (right hand side)
     */
    val scrollTable: JTable

    /**
     * Create a fixed columns table
     * Columns and Header tooltips are taken from table model.
     * Column settings are restored from database.
     * Internally two tables are created, "fixed" for the left hand side, "scroll" for the right hand side
     *
     * @param fixedColumns number of fixed columns
     */
    init {
        val table = JTable(tableModel)

        val columnModel = table.columnModel
        val header = ToolTipHeader(columnModel)
        header.setToolTipStrings(tableModel.tooltips)
        header.toolTipText = ""
        table.tableHeader = header

        table.rowSorter = tableRowSorter
        table.autoResizeMode = JTable.AUTO_RESIZE_OFF
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        table.selectionBackground = HODefaultTableCellRenderer.SELECTION_BG
        scrollTable = table
        for (i in 0 until scrollTable.columnCount) {
            val tm = tableModel.columns[i]
            val cm = scrollTable.columnModel.getColumn(i)
            cm.identifier = tm.getId()
            cm.minWidth = tm.minWidth
        }
        fixedTable = JTable(scrollTable.model)
        fixedTable.isFocusable = false
        fixedTable.selectionModel = scrollTable.selectionModel
        fixedTable.rowSorter = scrollTable.rowSorter
        fixedTable.tableHeader.reorderingAllowed = false

        //  Remove the fixed columns from the main table
        var width = 0
        var i = 0
        while (i < fixedColumnsCount) {
            val tm = tableModel.columns[i]
            val cm = fixedTable.columnModel.getColumn(i)
            cm.identifier = tm.getId() // identifier has to be resetted

            val _columnModel = scrollTable.columnModel
            val column = _columnModel.getColumn(0)
            width += column.minWidth
            _columnModel.removeColumn(column)
            i++
        }

        scrollTable.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        fixedTable.selectionModel = scrollTable.selectionModel

        //  Remove the non-fixed columns from the fixed table
        while (fixedTable.columnCount > fixedColumnsCount) {
            val _columnModel = fixedTable.columnModel
            _columnModel.removeColumn(_columnModel.getColumn(fixedColumnsCount))
        }

        val splitPane = JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, JScrollPane(fixedTable), JScrollPane(
                scrollTable
            )
        )


        if (width == 0) width = 60
        this.dividerLocation = HOConfigurationIntParameter("TableDividerLocation_" + tableModel.id, width)
        splitPane.dividerLocation = dividerLocation.getIntValue()
        splitPane.addPropertyChangeListener { evt: PropertyChangeEvent ->
            val propertyName = evt.propertyName
            if (propertyName == "dividerLocation") {
                val pane = evt.source as JSplitPane
                dividerLocation.setIntValue(pane.dividerLocation)
            }
        }

        setViewportView(splitPane)
    }

    fun setRowSelectionInterval(rowIndex0: Int, rowIndex1: Int) {
        fixedTable.setRowSelectionInterval(rowIndex0, rowIndex1)
        scrollTable.setRowSelectionInterval(rowIndex0, rowIndex1)
    }

    val selectedRow: Int
        get() = scrollTable.selectedRow

    /**
     * The provided renderer is set to both internal tables
     * @param columnClass  set the default cell renderer for this columnClass
     * @param renderer default cell renderer to be used for this columnClass
     */
    fun setDefaultRenderer(columnClass: Class<*>?, renderer: TableCellRenderer?) {
        fixedTable.setDefaultRenderer(columnClass, renderer)
        scrollTable.setDefaultRenderer(columnClass, renderer)
    }

    /**
     * Add a list selection listener
     * @param listener ListSelectionListener
     */
    fun addListSelectionListener(listener: ListSelectionListener?) {
        val rowSM = scrollTable.selectionModel
        rowSM.addListSelectionListener(listener)
    }

    val selectionModel: ListSelectionModel
        get() = scrollTable.selectionModel
}
