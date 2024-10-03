package core.gui.comp.table

import core.db.DBManager
import core.gui.model.UserColumnController.ColumnModelId
import core.model.HOVerwaltung
import core.util.Helper
import java.io.Serial
import java.util.*
import javax.swing.JTable
import javax.swing.RowSorter
import javax.swing.SortOrder
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableColumnModel
import javax.swing.table.TableRowSorter

/**
 * Basic ColumnModel for all UserColumnModels
 *
 * @author Thorsten Dietz
 * @since 1.36
 */
abstract class HOTableModel protected constructor(
    id: ColumnModelId,
    /** Name of the column model, shows in OptionsPanel  */
    private val name: String
) : AbstractTableModel() {
    /**
     *
     * @return id
     */
    /**
     * Identifier of the column model.
     * It is used for saving columns in db
     */
	@JvmField
	val id: Int = id.value

    /** count of displayed column  */
    private var displayedColumnsCount = 0

    /**
     * return all columns of the model
     *
     * @return UserColumn[]
     */
    /** all columns from this model  */
    lateinit var columns: Array<UserColumn>
        protected set

    /** only displayed columns  */
    protected var _displayedColumns: Array<UserColumn?>? = null

    /** data of table  */
    @JvmField
	protected var m_clData: Array<Array<Any>>? = null

    /** instance of the same class  */
    @JvmField
	protected var instance: Int = 0

    val rowSorter: TableRowSorter<HOTableModel>?
        get() {
            if (table != null) return table!!.rowSorter as TableRowSorter<HOTableModel>
            if (fixedColumnsTable != null) return fixedColumnsTable!!.tableRowSorter
            return null
        }

    private var table: JTable? = null
    private var fixedColumnsTable: FixedColumnsTable? = null

    /**
     * return the language dependent name of this model
     */
    override fun toString(): String {
        val tmp = HOVerwaltung.instance().getLanguageString(name)
        return if ((instance == 0)) tmp else (tmp + instance)
    }

    val columnNames: Array<String?>
        /**
         * return all columnNames of displayed columns
         *
         * @return String[]
         */
        get() {
            val columnNames = arrayOfNulls<String>(displayedColumnCount)
            for (i in getDisplayedColumns().indices) columnNames[i] = getDisplayedColumns()[i]!!.getColumnName()

            return columnNames
        }

    val tooltips: Array<String?>
        /**
         * return all tooltips of displayed columns
         *
         * @return String[]
         */
        get() {
            val tooltips = arrayOfNulls<String>(displayedColumnCount)
            for (i in getDisplayedColumns().indices) tooltips[i] = getDisplayedColumns()[i]!!.getTooltip()
            return tooltips
        }

    /**
     * return all displayed columns
     *
     * @return UserColumn[]
     */
    fun getDisplayedColumns(): Array<UserColumn?> {
        if (_displayedColumns == null) {
            val columncount = displayedColumnCount
            _displayedColumns = arrayOfNulls(columncount)
            var currentIndex = 0
            for (column in columns) {
                if (column.isDisplay) {
                    _displayedColumns!![currentIndex] = column

                    if (column.getIndex() >= columncount) _displayedColumns!![currentIndex]!!.setIndex(columncount - 1)
                    currentIndex++
                } // column is displayed
            } // for
        }

        return _displayedColumns!!
    }

    private val displayedColumnCount: Int
        /**
         * return count of displayed columns
         *
         * @return int
         */
        get() {
            if (displayedColumnsCount == 0) {
                for (column in columns) {
                    if (column.isDisplay) displayedColumnsCount++
                }
            }
            return displayedColumnsCount
        }

    /**
     * Returns count of displayed columns redundant method, but this is
     * overwritten method from AbstractTableModel
     */
    override fun getColumnCount(): Int {
        return displayedColumnCount
    }

    /**
     * return value
     *
     * @param row Row number
     * @param column Column number
     *
     * @return Object
     */
    override fun getValueAt(row: Int, column: Int): Any? {
        if (m_clData != null && m_clData!!.size > row) {
            return m_clData!![row][column]
        }

        return null
    }

    override fun getRowCount(): Int {
        return if ((m_clData != null)) m_clData!!.size else 0
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        val obj = getValueAt(0, columnIndex)

        if (obj != null) {
            return obj.javaClass
        }

        return "".javaClass
    }

    override fun getColumnName(columnIndex: Int): String? {
        if (displayedColumnCount > columnIndex) {
            return getDisplayedColumns()[columnIndex]!!.getColumnName()
        }

        return null
    }

    fun getValue(row: Int, columnName: String): Any? {
        if (m_clData != null) {
            var i = 0

            while ((i < columnNames.size) && columnNames[i] != columnName) {
                i++
            }

            return m_clData!![row][i]
        }

        return null
    }

    override fun setValueAt(value: Any, row: Int, column: Int) {
        m_clData!![row][column] = value
        fireTableCellUpdated(row, column)
    }

    private val columnOrder: Array<IntArray>
        /**
         * return the order of the column like old method getSpaltenreihenfolge
         *
         * @return
         */
        get() {
            val tmp = getDisplayedColumns()
            val order = Array(tmp.size) { IntArray(2) }
            for (i in order.indices) {
                order[i][0] = tmp[i]!!.getId()
                order[i][1] = tmp[i]!!.getIndex()
            }
            return order
        }

    /**
     * Move the columns of the table to their correct places
     * @param table JTable
     */
    fun initColumnOrder(table: JTable) {
        var order: Array<IntArray>? = columnOrder
        // Sort according to [x][1]
        order = Helper.sortintArray(order, 1)

        if (order != null) {
            for (ints in order) {
                table.moveColumn(table.columnModel.getColumnIndex(ints[0]), ints[1])
            }
        }
    }

    /**
     * sets size in JTable
     *
     * @param tableColumnModel
     */
    fun setColumnsSize(tableColumnModel: TableColumnModel) {
        val tmpColumns = getDisplayedColumns()
        for (tmpColumn in tmpColumns) {
            val id = tmpColumn!!.getId()
            tmpColumn.setSize(tableColumnModel.getColumn(tableColumnModel.getColumnIndex(id)))
        }
    }

    protected abstract fun initData()

    /**
     * return the array index from a Column id
     *
     * @param searchid
     * @return
     */
    fun getPositionInArray(searchid: Int): Int {
        val tmpColumns = getDisplayedColumns()
        for (i in tmpColumns.indices) {
            if (tmpColumns[i]!!.getId() == searchid) return i
        }
        return -1
    }

    fun setCurrentValueToColumns(tmpColumns: Array<UserColumn>) {
        for (tmpColumn in tmpColumns) {
            for (column in columns) {
                if (column.getId() == tmpColumn.getId()) {
                    column.setIndex(tmpColumn.getIndex())
                    column.setPreferredWidth(tmpColumn.getPreferredWidth())
                    break
                }
            }
        }
    }

    private fun getUserColumnSettings(table: JTable, offset: Int) {
        // Restore column order and width settings
        Arrays.stream(this.columns)
            .skip(offset.toLong())
            .limit(table.columnCount.toLong())
            .filter { obj: UserColumn -> obj.isDisplay }
            .sorted(Comparator.comparingInt { obj: UserColumn -> obj.getIndex() })
            .forEach { i: UserColumn -> getColumnSettings(i, table, offset) }
    }

    /**
     * Set column order and width
     *
     * @param userColumn user column holding user's settings
     * @param table      the table object
     */
    private fun getColumnSettings(userColumn: UserColumn, table: JTable, offset: Int) {
        val column = table.getColumn(userColumn.getId())
        column.preferredWidth = userColumn.getPreferredWidth()
        val index = table.columnModel.getColumnIndex(userColumn.getId())
        if (index != userColumn.getIndex() - offset) {
            table.moveColumn(index, userColumn.getIndex() - offset)
        }
    }

    /**
     * Save the user settings of the table. User selected width and column indexes are saved in user column model
     * which is stored in database table UserColumnTable
     *
     * @param table table object
     */
    private fun setUserColumnSettings(table: JTable): Boolean {
        return setUserColumnSettings(table, 0)
    }

    private fun setUserColumnSettings(table: JTable, offset: Int): Boolean {
        var changed = false
        // column order and width
        val tableColumnModel = table.columnModel
        val modelColumnCount = this.columnCount
        for (i in 0 until modelColumnCount) {
            if (i < offset) continue  // skip fixed columns in case of scroll table

            if (offset == 0 && i >= table.columnCount) break // fixed columns exceeded


            val column = columns[i]
            val index = table.convertColumnIndexToView(i)
            if (column.isDisplay) {
                if (column.getIndex() != index + offset) {
                    changed = true
                    column.setIndex(index + offset)
                }
                if (column.getPreferredWidth() != tableColumnModel.getColumn(index).width) {
                    changed = true
                    column.setPreferredWidth(tableColumnModel.getColumn(index).width)
                }
            }
        }
        return changed
    }

    private fun setUserColumnSettings(table: FixedColumnsTable): Boolean {
        var changed = setUserColumnSettings(table.fixedTable, 0)
        changed = changed || setUserColumnSettings(table.scrollTable, table.fixedColumnsCount)
        return changed
    }

    open fun userCanDisableColumns(): Boolean {
        return false
    }

    fun initTable(table: FixedColumnsTable) {
        this.fixedColumnsTable = table
        getUserColumnSettings(table.fixedTable, 0)
        getUserColumnSettings(table.scrollTable, table.fixedColumnsCount)
        getRowOrderSettings(table.tableRowSorter)
    }

    fun initTable(table: JTable) {
        this.table = table
        val columnModel = table.columnModel
        val header = ToolTipHeader(columnModel)
        header.setToolTipStrings(tooltips)
        header.toolTipText = ""
        table.tableHeader = header
        table.model = this


        for (i in 0 until columnModel.columnCount) {
            val tm = columns[i]
            val cm = table.columnModel.getColumn(i)
            cm.identifier = tm.getId()
        }

        getUserColumnSettings(table, 0)

        val rowSorter = TableRowSorter(this)
        getRowOrderSettings(rowSorter)
        table.rowSorter = rowSorter
    }

    fun storeUserSettings() {
        var changed = false
        var sorter: RowSorter<HOTableModel?>? = null
        if (table != null) {
            changed = setUserColumnSettings(table!!)
            sorter = table!!.rowSorter as TableRowSorter<HOTableModel?>
        } else if (fixedColumnsTable != null) {
            changed = setUserColumnSettings(fixedColumnsTable!!)
            sorter = fixedColumnsTable!!.tableRowSorter
        }
        if (sorter != null) {
            changed = changed || setRowOrderSettings(sorter)
        }
        if (changed) {
            DBManager.instance().saveHOColumnModel(this)
        }
    }

    private fun getRowOrderSettings(rowSorter: RowSorter<HOTableModel>) {
        // Restore row order setting
        val sortKeys = ArrayList<RowSorter.SortKey>()
        val sortColumns = Arrays.stream(this.columns).filter { i: UserColumn -> i.sortPriority != null }
            .sorted(Comparator.comparingInt { obj: UserColumn -> obj.getSortPriority() }).toList()
        if (!sortColumns.isEmpty()) {
            val userColumns = Arrays.stream(this.columns).toList()
            for (col in sortColumns) {
                val index = userColumns.indexOf(col)
                val sortKey = RowSorter.SortKey(index, col.getSortOrder())
                sortKeys.add(sortKey)
            }
        }
        rowSorter.sortKeys = sortKeys
    }

    private fun setRowOrderSettings(sorter: RowSorter<HOTableModel?>): Boolean {
        var changed = false
        val rowSortKeys = sorter.sortKeys
        for (i in columns.indices) {
            val finalI = i
            val rowSortKey = rowSortKeys.stream().filter { k: RowSorter.SortKey -> k.column == finalI }.findFirst()
            val userColumn = columns[i]
            if (rowSortKey.isPresent && rowSortKey.get().sortOrder != SortOrder.UNSORTED) {
                val k = rowSortKey.get()
                val priority = rowSortKeys.indexOf(k)
                if (userColumn.getSortPriority() == null || userColumn.getSortPriority() != priority ||
                    userColumn.getSortOrder() != k.sortOrder
                ) {
                    userColumn.setSortOrder(k.sortOrder)
                    userColumn.setSortPriority(priority)
                    changed = true
                }
            } else if (userColumn.getSortPriority() != null) {
                userColumn.setSortPriority(null)
                userColumn.setSortOrder(null)
                changed = true
            }
        }
        return changed
    }

    companion object {
        @Serial
        private val serialVersionUID = -207230110294902139L
    }
}