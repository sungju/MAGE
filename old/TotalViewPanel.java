/*
 * TotalViewPanel
 * - Display monitoring information as a table style
 *  
 *  (c)Copyright 2005,2006
 *  Written by Sungju Kwon
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package old;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class TotalViewPanel extends JPanel {
	private static final long serialVersionUID = -2179451881760260566L;

	private static final int MAX = 100;
	private static final int MIN = 0;
	
	private DefaultTableModel dm;
	private JTable table;
	private Object[] titleData = new Object[] { "Node", "Description", "CPU", "Memory", "Disk" };

	public TotalViewPanel() {
		setLayout(new BorderLayout());

		dm = new DefaultTableModel() {
			private static final long serialVersionUID = 6043507270941056908L;

			public Class getColumnClass(int col) {
				switch (col) {
				case 0:
				case 1:
					return String.class;
				default:
					return Integer.class;
				}
			}

			public boolean isCellEditable(int row, int col) {
				return false;
			}

			public void setValueAt(Object obj, int row, int col) {
				super.setValueAt(obj, row, col);
				return;
				/*
				if (col != 1) {
					super.setValueAt(obj, row, col);
					return;
				}
				try {
					Integer integer = new Integer(obj.toString());
					super.setValueAt(checkMinMax(integer), row, col);
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
				}
				*/
			}
		};
		dm.setDataVector(new Object[][] {
				{ "apple1", "Sample Node", new Integer(30), new Integer(30), new Integer(90)  },
				{ "apple2", "Babo", new Integer(95), new Integer(51), new Integer(60) },
				{ "comdoct", "Merong", new Integer(80), new Integer(76), new Integer(20) },
				{ "phantom", "Good", new Integer(12), new Integer(12), new Integer(80) } },
				titleData);

		table = new JTable(dm);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		IndicatorCellRenderer renderer = new IndicatorCellRenderer(MIN, MAX);
		renderer.setStringPainted(true);
		renderer.setBackground(table.getBackground());

		// set limit value and fill color
		Hashtable limitColors = new Hashtable();
		limitColors.put(new Integer(0), Color.green);
		limitColors.put(new Integer(60), Color.yellow);
		limitColors.put(new Integer(80), Color.red);
		renderer.setLimits(limitColors);
		table.getColumnModel().getColumn(2).setCellRenderer(renderer);
		table.getColumnModel().getColumn(3).setCellRenderer(renderer);
		table.getColumnModel().getColumn(4).setCellRenderer(renderer);	
		
		JScrollPane pane = new JScrollPane(table);
		add(pane, BorderLayout.CENTER);
	}
	
	public void setSelectedIndex(int idx) {
	}

	public void setTableData(Object data[][]) {
		int i = (int)(Math.random() * (data.length - 1)) + 1;
		int j;
		
		for (j = 0; j < i; j++) {
			if (dm.getRowCount() <= j)
				dm.addRow(data[j]);
			else
				for (int k = 0; k < 5; k++)
					dm.setValueAt(data[j][k], j, k);
		}
		j = dm.getRowCount();
		
		while (j > i) {
			j--;
			dm.removeRow(j);
		}
	}
}

class IndicatorCellRenderer extends JProgressBar implements TableCellRenderer {
	private static final long serialVersionUID = 4536194528440219402L;

	private Hashtable limitColors;

	private int[] limitValues;

	public IndicatorCellRenderer() {
		super(JProgressBar.HORIZONTAL);
		setBorderPainted(false);
	}

	public IndicatorCellRenderer(int min, int max) {
		super(JProgressBar.HORIZONTAL, min, max);
		setBorderPainted(false);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		int n = 0;
		if (!(value instanceof Number)) {
			String str;
			if (value instanceof String) {
				str = (String) value;
			} else {
				str = value.toString();
			}
			try {
				n = Integer.valueOf(str).intValue();
			} catch (NumberFormatException ex) {
			}
		} else {
			n = ((Number) value).intValue();
		}
		Color color = getColor(n);
		if (color != null) {
			setForeground(color);
		}
		setValue(n);
		return this;
	}

	public void setLimits(Hashtable limitColors) {
		this.limitColors = limitColors;
		int i = 0;
		int n = limitColors.size();
		limitValues = new int[n];
		Enumeration e = limitColors.keys();
		while (e.hasMoreElements()) {
			limitValues[i++] = ((Integer) e.nextElement()).intValue();
		}
		sort(limitValues);
	}

	private Color getColor(int value) {
		Color color = null;
		if (limitValues != null) {
			int i;
			for (i = 0; i < limitValues.length; i++) {
				if (limitValues[i] < value) {
					color = (Color) limitColors
							.get(new Integer(limitValues[i]));
				}
			}
		}
		return color;
	}

	private void sort(int[] a) {
		int n = a.length;
		for (int i = 0; i < n - 1; i++) {
			int k = i;
			for (int j = i + 1; j < n; j++) {
				if (a[j] < a[k]) {
					k = j;
				}
			}
			int tmp = a[i];
			a[i] = a[k];
			a[k] = tmp;
		}
	}
}