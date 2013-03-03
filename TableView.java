import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Creates a view of a data model as a table.
 * 
 * First column will be the symbol, and second column will be the codeword. If
 * frequency data is available, then it will be given in a third column.
 * 
 */
@SuppressWarnings("serial")
class TableView extends JComponent implements ModelView, Resizable {
	private static int MARGIN = 1;
	
	private ArrayList<Item> entries;
	boolean showFrequencies;
	private JTable table;
	private AbstractTableModel tableModel;
	private HashSet<SymbolSelectionListener> listeners;

	/**
	 * Inner class to represent information for a single row.
	 */
	private class Item {
		public String symbol;
		public String codeword;
		public int freq;

		public Item(String sym, String code, int freq) {
			symbol = sym;
			codeword = code;
			this.freq = freq;
		}
	}

	/**
	 * Constructs a new Table View, but with an empty data model.
	 */
	public TableView(int height) {
		entries = new ArrayList<Item>();
		listeners = new HashSet<SymbolSelectionListener>();
		showFrequencies = true;

		final String[] header = { "Symbol", "Codeword", "Frequency" };
		tableModel = new AbstractTableModel() {
			@Override
			public String getColumnName(int col) {
				return header[col];
			}

			public int getColumnCount() {
				return (showFrequencies ? 3 : 2);
			}

			public int getRowCount() {
				return entries.size();
			}

			public Object getValueAt(int row, int col) {
				Item it = entries.get(row);
				switch (col) {
				case 0:
					return HuffmanDemo.printableSymbol(it.symbol);
				case 1:
					return it.codeword;
				case 2:
					return (showFrequencies ? it.freq : null);
				}
				return null;
			}

			@Override
			public Class getColumnClass(int c) {
				return (c < 2 ? String.class : Integer.class);
			}
		};

		table = new JTable(tableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setAutoCreateRowSorter(true);
		tableModel.addTableModelListener(table);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setGridColor(Color.black);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);

		table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (!e.getValueIsAdjusting()) {
							int k = table.getSelectedRow();
							String sym = (k == -1 ? null : entries.get(table.convertRowIndexToModel(k)).symbol);
							for (SymbolSelectionListener l : listeners)
								l.selectionChanged(new SymbolSelectionEvent(this, sym));
						}
					}
				});

		setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane, BorderLayout.CENTER);
		reFit();
	}

	/**
	 * Sets or resets the data model for the table view.
	 */
	public void setModel(DataModel m) {
		setSymbolSelection(null);
		entries.clear();
		Map<String, String> codeMap = m.getCodebookMap();
		Map<String, Integer> freqMap = m.getFrequencyMap();
		showFrequencies = m.hasFrequencyData();
		
		for (Map.Entry<String, String> entry : codeMap.entrySet()) {
			String sym = entry.getKey();
			String code = entry.getValue();
			int f = (showFrequencies ? freqMap.get(sym) : 0);
			Item it = new Item(sym, code, f);
			entries.add(it);
		}
		tableModel.fireTableStructureChanged();
		reFit();
		table.setSize(table.getPreferredScrollableViewportSize());
	}

	/**
	 * Registers a SymbolSelectionListener with this TableView.
	 * 
	 * @param listener
	 */
	public void addSymbolSelectionListener(SymbolSelectionListener listener) {
		listeners.add(listener);
	}

	/**
	 * method to change size of font
	 * 
	 * @param size
	 */
	public void setFontSize(int size) {
		Font currentFont = table.getFont();
		table.setFont(new Font(currentFont.getName(), currentFont.getStyle(), size));
		// make header font halfsize relative to contents
		table.getTableHeader().setFont(new Font(currentFont.getName(), currentFont.getStyle(), size/2));
	}

	/**
	 * method that returns font size of table
	 */
	public int getFontSize() {
		int size;
		size = table.getFont().getSize();
		return size;
	}

	public void reFit() {
		for (int column = table.getColumnCount() - 1; column >= 0; column--)
			setColumnWidth(column);
		for (int row = table.getRowCount() - 1; row >= 0; row--)
			setRowHeight(row);
		table.setPreferredScrollableViewportSize(new Dimension(table.getPreferredSize().width,
															   getBounds().height * 19/20));
		table.revalidate();
		revalidate();
	//	paint(getGraphics());
	}
	
	public void setColumnWidth(int column) {
		TableColumn tc = table.getColumnModel().getColumn(column);
		TableCellRenderer renderer = tc.getHeaderRenderer();
		if (renderer == null)
			renderer = table.getTableHeader().getDefaultRenderer();
		Component comp = renderer.getTableCellRendererComponent(
				table, tc.getHeaderValue(), false, false, 0, 0);
		int pref = comp.getPreferredSize().width + 20;  // offset to leave room for arrow when sorting
		
		for (int row = table.getRowCount() - 1; row >= 0; row--) {
			renderer = table.getCellRenderer(row, column);
			comp = table.prepareRenderer(renderer, row, column);
			pref = Math.max(pref, comp.getPreferredSize().width);
		}
		tc.setPreferredWidth(pref + 2*MARGIN);
	}

	public void setRowHeight(int row) {
		int pref = 0;
		for (int column = table.getColumnCount() - 1; column >= 0; column--) {
			TableCellRenderer renderer = table.getCellRenderer(row, column);
			Component comp = table.prepareRenderer(renderer, row, column);
			pref = Math.max(pref, comp.getPreferredSize().height);
		}
		table.setRowHeight(pref + 2*MARGIN);
	}

	/**
	 * Designates a particular symbol for highlighting.
	 * 
	 * @param s
	 *            symbol to highlight (or null if no highlighting is desired)
	 */
	public void setSymbolSelection(String s) {
		if (s == null) {
			table.getSelectionModel().clearSelection();
		} else {
			for (int k = 0; k < entries.size(); k++)
				if (entries.get(k).symbol.equals(s)) {
					int real = table.convertRowIndexToView(k);
					table.getSelectionModel().setSelectionInterval(real, real);
					break;
				}
		}
	}

	/*
	 * method returns hash table with symbols as keys to the code
	 */
	public Hashtable<String, String> symbolToCodeHash() {
		Hashtable<String, String> htable = new Hashtable<String, String>();
		for (int i = 0; i < table.getRowCount(); i++) {
			htable.put((String) (table.getModel().getValueAt(i, 0)),
					(String) table.getModel().getValueAt(i, 1));
		}

		return htable;
	}

	/*
	 * method returns has table with code as keys to symbol.
	 */
	public Hashtable<String, String> codeToSymbolHash() {
		Hashtable<String, String> htable = new Hashtable<String, String>();
		for (int i = 0; i < table.getRowCount(); i++) {
			htable.put((String) (table.getModel().getValueAt(i, 1)),
					(String) table.getModel().getValueAt(i, 0));
		}

		return htable;
	}

}
