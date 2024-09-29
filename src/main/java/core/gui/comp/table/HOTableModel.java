package core.gui.comp.table;

import core.db.DBManager;
import core.gui.model.UserColumnController;
import core.model.HOVerwaltung;
import core.util.Helper;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Basic ColumnModel for all UserColumnModels
 * 
 * @author Thorsten Dietz
 * @since 1.36
 */
public abstract class HOTableModel extends AbstractTableModel {

	@Serial
	private static final long serialVersionUID = -207230110294902139L;

	/**
	 * Identifier of the column model.
	 * It is used for saving columns in db
	 */
	private final int id;

	/** Name of the column model, shows in OptionsPanel **/
	private final String name;

	/** count of displayed column **/
	private int displayedColumnsCount;

	/** all columns from this model **/
	protected UserColumn[] columns;

	/** only displayed columns **/
	protected UserColumn[] displayedColumns;

	/** data of table **/
	protected Object[][] m_clData;

	/** instance of the same class **/
	protected int instance;

	public TableRowSorter<HOTableModel> getRowSorter() {
		if ( table != null) return (TableRowSorter<HOTableModel>) table.getRowSorter();
		if ( fixedColumnsTable != null) return fixedColumnsTable.getTableSorter();
		return null;
	}

//	private TableRowSorter<HOTableModel> rowSorter; //  = new TableRowSorter<>(this) ;
	private JTable table;
	private FixedColumnsTable fixedColumnsTable;

	/**
	 * constructor
	 * 
	 * @param id model id
	 * @param name model name
	 */
	protected HOTableModel(UserColumnController.ColumnModelId id, String name) {
		this.id = id.getValue();
		this.name = name;
	}

	/**
	 * return all columns of the model
	 * 
	 * @return UserColumn[]
	 */
	public final UserColumn[] getColumns() {
		return columns;
	}

	/**
	 * 
	 * @return id
	 */
	public final int getId() {
		return id;
	}

	/**
	 * return the language dependent name of this model
	 */
	@Override
	public String toString() {
		String tmp = HOVerwaltung.instance().getLanguageString(name);
		return (instance == 0) ? tmp : (tmp + instance);
	}

	/**
	 * return all columnNames of displayed columns
	 * 
	 * @return String[]
	 */
	public String[] getColumnNames() {
		final String[] columnNames = new String[getDisplayedColumnCount()];
		for (int i = 0; i < getDisplayedColumns().length; i++)
			columnNames[i] = getDisplayedColumns()[i].getColumnName();

		return columnNames;
	}

	/**
	 * return all tooltips of displayed columns
	 * 
	 * @return String[]
	 */
	public String[] getTooltips() {
		final String[] tooltips = new String[getDisplayedColumnCount()];
		for (int i = 0; i < getDisplayedColumns().length; i++)
			tooltips[i] = getDisplayedColumns()[i].getTooltip();
		return tooltips;
	}

	/**
	 * return all displayed columns
	 * 
	 * @return UserColumn[]
	 */
	public UserColumn[] getDisplayedColumns() {

		if (displayedColumns == null) {
			final int columncount = getDisplayedColumnCount();
			displayedColumns = new UserColumn[columncount];
			int currentIndex = 0;
			for (UserColumn column : columns) {

				if (column.isDisplay()) {
					displayedColumns[currentIndex] = column;

					if (column.getIndex() >= columncount)
						displayedColumns[currentIndex].setIndex(columncount - 1);
					currentIndex++;
				} // column is displayed
			} // for
		}

		return displayedColumns;
	}

	/**
	 * return count of displayed columns
	 * 
	 * @return int
	 */
	private int getDisplayedColumnCount() {
		if (displayedColumnsCount == 0) {
			for (UserColumn column : columns) {
				if (column.isDisplay())
					displayedColumnsCount++;
			}
		}
		return displayedColumnsCount;
	}

	/**
	 * Returns count of displayed columns redundant method, but this is
	 * overwritten method from AbstractTableModel
	 */
	@Override
	public int getColumnCount() {
		return getDisplayedColumnCount();
	}

	/**
	 * return value
	 * 
	 * @param row Row number
	 * @param column Column number
	 * 
	 * @return Object
	 */
	@Override
	public final Object getValueAt(int row, int column) {
		if (m_clData != null && m_clData.length>row) {
			return m_clData[row][column];
		}

		return null;
	}

	@Override
	public final int getRowCount() {
		return (m_clData != null) ? m_clData.length : 0;
	}

    @Override
	public final Class<?> getColumnClass(int columnIndex) {
		final Object obj = getValueAt(0, columnIndex);

		if (obj != null) {
			return obj.getClass();
		}

		return "".getClass();
	}

	@Override
	public final String getColumnName(int columnIndex) {
		if (getDisplayedColumnCount() > columnIndex) {
			return getDisplayedColumns()[columnIndex].getColumnName();
		}

		return null;
	}

	public final Object getValue(int row, String columnName) {
		if (m_clData != null) {
			int i = 0;

			while ((i < getColumnNames().length) && !getColumnNames()[i].equals(columnName)) {
				i++;
			}

			return m_clData[row][i];
		}

		return null;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		m_clData[row][column] = value;
		fireTableCellUpdated(row,column);
	}

	/**
	 * return the order of the column like old method getSpaltenreihenfolge
	 * 
	 * @return
	 */
	private int[][] getColumnOrder() {
		UserColumn[] tmp = getDisplayedColumns();
		int[][] order = new int[tmp.length][2];
		for (int i = 0; i < order.length; i++) {
			order[i][0] = tmp[i].getId();
			order[i][1] = tmp[i].getIndex();
		}
		return order;
	}

	/**
	 * Move the columns of the table to their correct places
	 * @param table JTable
	 */
	public void initColumnOrder(JTable table)
	{
		var order = getColumnOrder();
		// Sort according to [x][1]
		order = Helper.sortintArray(order, 1);

		if (order != null) {
			for (int[] ints : order) {
				table.moveColumn(table.getColumnModel().getColumnIndex(ints[0]), ints[1]);
			}
		}
	}

	/**
	 * sets size in JTable
	 * 
	 * @param tableColumnModel
	 */
	public void setColumnsSize(TableColumnModel tableColumnModel) {
		final UserColumn[] tmpColumns = getDisplayedColumns();
        for (UserColumn tmpColumn : tmpColumns) {
            var id = tmpColumn.getId();
            tmpColumn.setSize(tableColumnModel.getColumn(tableColumnModel.getColumnIndex(id)));
        }
	}

	protected abstract void initData();

	/**
	 * return the array index from a Column id
	 * 
	 * @param searchid
	 * @return
	 */
	public int getPositionInArray(int searchid) {
		final UserColumn[] tmpColumns = getDisplayedColumns();
		for (int i = 0; i < tmpColumns.length; i++) {
			if (tmpColumns[i].getId() == searchid)
				return i;
		}
		return -1;
	}

	public void setCurrentValueToColumns(UserColumn[] tmpColumns) {
		for (UserColumn tmpColumn : tmpColumns) {
			for (UserColumn column : columns) {
				if (column.getId() == tmpColumn.getId()) {
					column.setIndex(tmpColumn.getIndex());
					column.setPreferredWidth(tmpColumn.getPreferredWidth());
					break;
				}
			}
		}
	}

	/**
	 * stored user settings of table columns order and columns width are set to the table
	 *
	 * @param table the table object
	 */
	private void restoreUserSettings(JTable table) {
		restoreUserSettings(table, 0);
	}

	private void restoreUserSettings(JTable table, int offset) {
		// Restore column order setting
		Arrays.stream(this.columns)
				.skip(offset)
				.limit(table.getColumnCount())
				.filter(UserColumn::isDisplay)
				.sorted(Comparator.comparingInt(UserColumn::getIndex))
				.forEach(i -> setColumnSettings(i, table, offset));

		// TODO: Restore row order setting

		var rowSorter = getRowSorter();
		if ( rowSorter == null){
			List<RowSorter.SortKey> sortKeys = new  ArrayList<>();
			Arrays.stream(this.columns).filter(i->i.sortPriority != null).sorted(Comparator.comparingInt(UserColumn::getSortPriority)).forEach(i->sortKeys.add(new RowSorter.SortKey(i.index, i.sortOrder)));

			rowSorter = new TableRowSorter<>(this);
			rowSorter.setSortKeys(sortKeys);
			table.setRowSorter(rowSorter);
		}
	}

	private void restoreUserSettings(FixedColumnsTable table) {
		restoreUserSettings(table.getFixedTable(), 0);
		restoreUserSettings(table.getScrollTable(), table.getFixedColumnsCount());
	}

	/**
	 * Set column order and width
	 *
	 * @param userColumn user column holding user's settings
	 * @param table      the table object
	 */
	private void setColumnSettings(UserColumn userColumn, JTable table, int offset) {
		var column = table.getColumn(userColumn.getId());
		column.setPreferredWidth(userColumn.getPreferredWidth());
		var index = table.getColumnModel().getColumnIndex(userColumn.getId());
		if ( index != userColumn.getIndex()-offset) {
			table.moveColumn(index, userColumn.getIndex()-offset);
		}
	}

	/**
	 * Save the user settings of the table. User selected width and column indexes are saved in user column model
	 * which is stored in database table UserColumnTable
	 *
	 * @param table table object
	 */
	private void storeUserSettings(JTable table) {
		var changed = storeUserSettings(table, 0);
		if (changed){
			DBManager.instance().saveHOColumnModel(this);
		}
	}

	private boolean storeUserSettings(JTable table, int offset) {
		boolean changed = false;
		// column order and width
		var tableColumnModel = table.getColumnModel();
		var modelColumnCount = this.getColumnCount();

		var rowSorter = table.getRowSorter();
		List<? extends RowSorter.SortKey> rowSortKeys;
		if (rowSorter != null) {
			rowSortKeys = table.getRowSorter().getSortKeys();
		}
		else {
			rowSortKeys = new ArrayList<>();	// empty
		}

		for (int i = 0; i < modelColumnCount; i++) {
			if (i < offset) continue;                                // skip fixed columns in case of scroll table
			if (offset == 0 && i >= table.getColumnCount()) break;   // fixed columns exceeded

			var column = this.getColumns()[i];
			var index = table.convertColumnIndexToView(i);
			if (column.isDisplay()) {
				if (column.getIndex() != index + offset) {
					changed = true;
					column.setIndex(index + offset);
				}
				if (column.getPreferredWidth() != tableColumnModel.getColumn(index).getWidth()) {
					changed = true;
					column.setPreferredWidth(tableColumnModel.getColumn(index).getWidth());
				}

				// Check row order settings, after column index is evaluated
				var sortKey = rowSortKeys.stream().filter(k -> k.getColumn() == column.getIndex()).findAny();
				if (sortKey.isPresent() && sortKey.get().getSortOrder() != SortOrder.UNSORTED) {
					var sortPriority = rowSortKeys.indexOf(sortKey.get());
					if (sortPriority != column.getSortPriority() || sortKey.get().getSortOrder() != column.getSortOrder()) {
						changed = true;
						column.setSortPriority(sortPriority);
						column.setSortOrder(sortKey.get().getSortOrder());
					}
				} else if (column.getSortPriority() != null) {
					changed = true;
					column.setSortPriority(null);
					column.setSortOrder(null);
				}

			}
		}
		return changed;
	}

	private void storeUserSettings(FixedColumnsTable table) {
		var changed = storeUserSettings(table.getFixedTable(), 0);
		changed = changed || storeUserSettings(table.getScrollTable(), table.getFixedColumnsCount());
		if (changed){
			DBManager.instance().saveHOColumnModel(this);
		}
	}

	public boolean userCanDisableColumns() {
		return false;
	}

	public void initTable(FixedColumnsTable table){
		this.fixedColumnsTable = table;
		restoreUserSettings(table);
//		var columnModel = table.getScrollTable().getColumnModel();
	}

	public void initTable(JTable table) {
		this.table = table;
		var columnModel = table.getColumnModel();
		ToolTipHeader header = new ToolTipHeader(columnModel);
		header.setToolTipStrings(getTooltips());
		header.setToolTipText("");
		table.setTableHeader(header);
		table.setModel(this);


		for (int i=0; i<columnModel.getColumnCount(); i++){
			var tm = this.columns[i];
			var cm = table.getColumnModel().getColumn(i);
			cm.setIdentifier(tm.getId());
		}

		restoreUserSettings(table);

//		initColumnOrder(table);

//		setColumnsSize(columnModel);
	}

	public void storeUserSettings(){
		if (table != null){ storeUserSettings(table);}
		else if ( fixedColumnsTable != null){ storeUserSettings(fixedColumnsTable);}
	}
}
