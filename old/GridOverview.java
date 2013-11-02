/*
 * GridOverview
 * - Simple Monitoring application
 * - It is on the progress!!!
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
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;


public class GridOverview extends JFrame {
	private static final long serialVersionUID = -6328190473922492945L;

	private Container cp;
	private TotalViewPanel totalViewPanel = new TotalViewPanel();
	private JList proxyList = new JList();
	
	private JMenuBar mainMenu;
	
	private JButton btnServerBroker = new JButton("localhost");
	
	private MonitoringChart monitoringChart;
	
	public GridOverview() {
		super("Grid Overview Monitoring");
		
		setMenu();
		setUI();
		setEvent();
		
		startThread();
	}
	
	private class SampleThread extends Thread {
		public void run() {
			while (true) {
				setData("abc");
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void startThread() {
		new SampleThread().start();
	}
	
	private void setMenu() {
		mainMenu = new JMenuBar();
		JMenu menuManage = new JMenu("NodeManage");
		JMenu menuWindow = new JMenu("Window");
		JMenu menuInfo = new JMenu("Info");
		
		menuManage.setMnemonic('N');
		menuWindow.setMnemonic('W');
		menuInfo.setMnemonic('I');
		
		mainMenu.add(menuManage);
		mainMenu.add(menuWindow);
		mainMenu.add(menuInfo);
		
		setJMenuBar(mainMenu);
	}
	
	private void setUI() {
		JPanel pnlMonitoring = new JPanel();
		JPanel pnlUpper = new JPanel();
		JPanel pnlLower = new JPanel();
		JPanel pnlRoot = new JPanel();
		Vector listData = new Vector();
		
		cp = getContentPane();
		
		listData.add("All");
		listData.add("apple1.ssu.ac.kr");
		listData.add("sslab");
		proxyList.setListData(listData);
		proxyList.setSelectedIndex(0);
		
		pnlRoot.setLayout(new BorderLayout());
		pnlRoot.add(btnServerBroker, BorderLayout.NORTH);
		pnlRoot.add(new JScrollPane(proxyList), BorderLayout.CENTER);
		pnlRoot.setPreferredSize(new Dimension(150, 100));
		pnlUpper.setLayout(new BorderLayout());
		pnlUpper.add(new JScrollPane(totalViewPanel), BorderLayout.CENTER);
		pnlUpper.add(pnlRoot, BorderLayout.WEST);
		
		JSplitPane split =
	          new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlUpper, pnlLower);
	    split.setDividerLocation(200);
	    
	    pnlMonitoring.setLayout(new BorderLayout());
		pnlMonitoring.add(split, BorderLayout.CENTER);
		
		pnlLower.setLayout(new BorderLayout());
		/*
		pnlLower.add(new JTextArea("Chart Graph with Basic Information\n" +
									"Chart will contain CPU, Memory, Disk's history in same area\n"),
					BorderLayout.CENTER);
		*/
		
		monitoringChart = new MonitoringChart();
        pnlLower.add(monitoringChart, BorderLayout.CENTER);		
		
		JTabbedPane tabbedPane = new JTabbedPane();
		ImageIcon iconMonitoring = new ImageIcon("images/monitoring.gif");
		tabbedPane.addTab("Monitoring", iconMonitoring, pnlMonitoring, "Monitoring nodes");
		tabbedPane.addTab("Job", null, new JPanel(), "Job execution and control");
		tabbedPane.addTab("Provider management", null, new JPanel(), "Remote provider management");
		
		cp.add(tabbedPane, BorderLayout.CENTER);
/*
		ImageIcon icon = createImageIcon("images/middle.gif");

		tabbedPane.addTab("Tab 1", icon, panel1,
		                  "Does nothing");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		JComponent panel2 = makeTextPanel("Panel #2");
		tabbedPane.addTab("Tab 2", icon, panel2,
		                  "Does twice as much nothing");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		JComponent panel3 = makeTextPanel("Panel #3");
		tabbedPane.addTab("Tab 3", icon, panel3,
		                  "Still does nothing");
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

		JComponent panel4 = makeTextPanel(
		        "Panel #4 (has a preferred size of 410 x 50).");
		panel4.setPreferredSize(new Dimension(410, 50));
		tabbedPane.addTab("Tab 4", icon, panel4,
		                      "Does nothing at all");
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
*/		
		setSize(700, 550);
		setVisible(true);
	}
	
	private void setData(String dataString) {
		totalViewPanel.setTableData(new Object[][] {
				{ "apple1", "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "apple" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "apple" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "apple" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "apple" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "apple" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "apple" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "apple" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "apple" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "apple" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "apple" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				
				{ "barney" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "barney" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "barney" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "barney" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "barney" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "barney" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "barney" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "barney" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "barney" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				{ "barney" + ((int)(Math.random() * 50)), "no desc.", new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)), new Integer((int)(Math.random() * 100)) },			
				});
	}
	
	private void setEvent() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});	
	}
	
	public static void main(String[] args) {
		new GridOverview();
	}
}
