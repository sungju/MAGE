package mage.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import mage.agent.AgentInfo;
import mage.client.ServiceRepositoryViewer.DirTableModel;
import mage.network.Message;
import mage.network.NetworkAgentManager;
import mage.network.PeerNetworkAgent;
import mage.network.ServerNetworkAgent;

public class ResourceMonitorPanel extends JPanel {
	private String address;
	private int port;
	private MonitorPanel monitorPanel = null;
	private boolean contThread = false;
	private boolean finishedThread = false;
	private int resourceMonitorInterval = 5000;
	private Thread myThread = null;
	
    private EmptyBorder border5 = new EmptyBorder(5, 5, 5, 5);
    
    private JTable tblWorkList;
    private String[][] workDataList;
	
	public boolean isFinishedThread() {
		return finishedThread;
	}
	public void setFinishedThread(boolean finishedThread) {		
		this.finishedThread = finishedThread;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	public ResourceMonitorPanel(MonitorPanel pnl, String host, int port) {
		monitorPanel = pnl;
		this.setBorder(new CompoundBorder(new TitledBorder(null, host + ":" + port,
				TitledBorder.LEFT, TitledBorder.TOP), border5));
		monitorPanel.pnlResourceManager.add(this);	
		tblWorkList = new JTable(new WorkTableModel(this));
		this.setLayout(new BorderLayout());
		resizeTable();
		this.add(new JScrollPane(tblWorkList), BorderLayout.CENTER);
		
		setAddress(host);
		setPort(port);
		
		setContThread(true);
		setFinishedThread(false);
		myThread = new Thread() {
			public void run() {
				while (isContThread()) {
					getWorkingNodeList();
					try {
						sleep(resourceMonitorInterval);
					} catch (Exception ex) {
					}
				}
				monitorPanel.remove(ResourceMonitorPanel.this);
				setFinishedThread(true);
			}
		};
		myThread.start();
	}

	private void resizeTable() {		
		/*
		tblWorkList.setSize(new Dimension(100, 100));
		tblWorkList.setPreferredSize(new Dimension(100, 100));
		*/
		TableColumn column = null;
		for (int i = 0; i < tblWorkList.getColumnCount(); i++) {
			column = tblWorkList.getColumnModel().getColumn(i);
			if (i == 0)
				column.setPreferredWidth(130);
			else if (i == 1)
				column.setPreferredWidth(40);
			else if (i == 2)
				column.setPreferredWidth(70);
			else {
				column.setPreferredWidth(40);
			}
		}	
	}
	
	private Message buildMessage(String cmdStr, String msgStr) {
		Message msg = new Message();
		msg.setQueryType("HeartBeat");
		msg.setCommand(cmdStr);
		msg.setCode(100);
		msg.setMessage("Service: ResourceManagerAgent\n" + msgStr);

		return msg;
	}
	
	private void getWorkingNodeList() {
		PeerNetworkAgent peerNetwork = connectTo(address, port);
		if (peerNetwork == null) return;
		
		Message msg = buildMessage("worklist", "");
		msg = peerNetwork.sendAndReceive(msg);	
		
		peerNetwork.close();		
		StringTokenizer st = new StringTokenizer(msg.getMessage(), "\n", false);
		workDataList = new String[st.countTokens()][7];
		int cnt = 0;
		while (st.hasMoreTokens()) {
			StringTokenizer strow = new StringTokenizer(st.nextToken(), ":", false);
			for (int i = 0; i < 7; i++)
				workDataList[cnt][i] = strow.nextToken();
			cnt++;
		}
		tblWorkList.invalidate();
		tblWorkList.revalidate();
		tblWorkList.repaint();
	}
	
	private PeerNetworkAgent connectTo(String addr, int port) {
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setName("LITH_Talk");
		agentInfo.setClassName("mage.network.protocol.LITHTalk");
		
		PeerNetworkAgent peerNetwork = ((ServerNetworkAgent)(NetworkAgentManager.findNetworkAgent("LITH_Talk"))).getPeerInstance();
		
		HashMap data = new HashMap();
		data.put("ADDRESS", addr);
		data.put("PORT", "" + port);
		peerNetwork.setArguments(data);
		
		if (!peerNetwork.connect()) {
			return null;
		}	
		return peerNetwork;
	}
	
	public boolean isContThread() {
		return contThread;
	}
	
	public void setContThread(boolean contThread) {
		this.contThread = contThread;
		if (myThread != null && contThread == false)
			myThread.interrupt();
	}
	
	class WorkTableModel extends AbstractTableModel {
		String columnNames[] = {"Addr", "Port", "OS", "Memory", "Cpu"};
		
		ResourceMonitorPanel resourceMonitorPanel = null;
		public WorkTableModel(ResourceMonitorPanel rmp) {
			this.resourceMonitorPanel = rmp;
		}
		
		public int getColumnCount() { return columnNames.length; }
		public int getRowCount() { return resourceMonitorPanel.workDataList.length; }
		public Object getValueAt(int row, int col) {
			if (col < 3)
				return resourceMonitorPanel.workDataList[row][col];
			else if (col == 3) {
				int memUsed = 
					(parseInt(resourceMonitorPanel.workDataList[row][5]) * 100) /
					parseInt(resourceMonitorPanel.workDataList[row][4]);
				return memUsed + "%";
			} else {
				return resourceMonitorPanel.workDataList[row][6] + "%";
			}
		}
		public String getColumnName(int col) {
			return columnNames[col];
		}
	}	
	
	private int parseInt(String data) {
		int result = 0;
		try {
			result = Integer.parseInt(data);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}	
}
