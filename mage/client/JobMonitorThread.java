package mage.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import mage.agent.AgentInfo;
import mage.network.Message;
import mage.network.NetworkAgentManager;
import mage.network.PeerNetworkAgent;
import mage.network.ServerNetworkAgent;
import mage.ubi.GridSpaceClient;

public class JobMonitorThread extends Thread {
	private JInternalFrame iframe = null;
	
	private GridSpaceClient clientMain = null;
	
	private long jobId = 0;
	private String address = null;
	private int port = 0;
	
	// Thread
	private boolean contThread = false;
	private int jobMonitorInterval = 5000; // 5 sec.
	
	// GUI
	private JTable tblJobList;
	private JButton btnViewResult = new JButton("View Result");
	private JButton btnSaveResult = new JButton("Save Result To File");
    private JButton btnClose = new JButton("Close");
	private EmptyBorder border5 = new EmptyBorder(5, 5, 5, 5);
    
    // Tasks
    private Vector<String[]> workDataList = new Vector<String[]>();
    
	public JobMonitorThread(GridSpaceClient clientMain, JInternalFrame iframe, String address, int port, long jobId) {
		this.clientMain = clientMain;
		this.iframe = iframe;
		this.address = address;
		this.port = port;
		this.jobId = jobId;
		
		setUI();
		setEvents();
	}
	
	private void setUI() {
		iframe.setSize(400, 300);
		iframe.setVisible(true); 
		iframe.setLayout(new BorderLayout());	
		JPanel pnlCenter = new JPanel();
		JPanel pnlBottom = new JPanel();
		
		tblJobList = new JTable(new WorkListTableModel(this));
		resizeTable();
		
		pnlCenter.setLayout(new BorderLayout());
		pnlCenter.setBorder(new CompoundBorder(new TitledBorder(null, "Work Node Status",
				TitledBorder.LEFT, TitledBorder.TOP), border5));		
		pnlCenter.add(new JScrollPane(tblJobList), BorderLayout.CENTER);
		
		pnlBottom.setLayout(new BoxLayout(pnlBottom, BoxLayout.X_AXIS));
		pnlBottom.setBorder(new CompoundBorder(new TitledBorder(null, "",
				TitledBorder.LEFT, TitledBorder.TOP), border5));		

		pnlBottom.add(btnViewResult);
		pnlBottom.add(btnSaveResult);
		pnlBottom.add(new JLabel("            "));
		pnlBottom.add(btnClose);
		
		iframe.add(pnlCenter, BorderLayout.CENTER);
		iframe.add(pnlBottom, BorderLayout.SOUTH);	
	}
	
	private void setEvents() {
		btnViewResult.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				viewJobResult();
			}
		});
		btnSaveResult.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				saveJobResult();
			}
		});
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				closeJob();
			}
		});
	}
	
	private void closeJob() {
		deleteJobFromServer();
		iframe.hide();
		clientMain.deskPane.repaint();
	}
	
	private void viewJobResult() {
		String jobId = null;
		String agentName = null;
		String resultViewer = null;
		
		String result = getJobResultStr();
		
		if (result == null) {
			JOptionPane.showMessageDialog(clientMain, "Can't get job result", "ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		}
		StringTokenizer st = new StringTokenizer(result, "\n", false);
		if (st.countTokens() < 3) {
			JOptionPane.showMessageDialog(clientMain, "Message format incorrect", "ERROR", JOptionPane.ERROR_MESSAGE);
			return;	
		}
		jobId = st.nextToken();
		agentName = st.nextToken();
		resultViewer = st.nextToken();
		

		StringBuffer sb = new StringBuffer();
		while (st.hasMoreTokens()) {
			sb.append(st.nextToken());
			sb.append("\n");
		}
		
		if (resultViewer.length() > 0) {
			ResultViewerAdaptor rv = LoadResultViewer(resultViewer);
			if (rv == null) {
				showInTextArea(jobId, agentName, resultViewer, sb.toString());
			} else {
				rv.setData(clientMain, jobId, agentName, sb.toString());
				rv.viewResult();
			}
		} else {
			showInTextArea(jobId, agentName, resultViewer, sb.toString());
		}
	}
	
	private void showInTextArea(String jobId, String agentName, 
								String resultViewer, String result) {
		JOptionPane.showMessageDialog(clientMain, result);
	}
	
	private ResultViewerAdaptor LoadResultViewer(String className) {
		Object obj = null;
		StringTokenizer st = new StringTokenizer(className, ":", false);
		if (st.countTokens() != 2) return null;
		try {
			st.nextToken();
			obj = Class.forName(st.nextToken().trim()).newInstance();
			if (obj == null) return null;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return (ResultViewerAdaptor)obj;
	}
	
	private int parseInt(String data) {
		int result = 0;
		try {
			result = Integer.parseInt(data);
		} catch (Exception ex) {
		}
		return result;
	}

	private String getJobResultStr() {
		String result = null;
		PeerNetworkAgent peerNetwork = connectTo(address, port);
		if (peerNetwork == null) return null;
		StringBuffer sb = new StringBuffer();
		sb.append("JobID: ");
		sb.append(jobId);
		
		Message msg = buildMessage("job_result", sb.toString());
		msg = peerNetwork.sendAndReceive(msg);
		if (msg.getCode() != 0) {
			result = msg.getMessage();
		}
		
		peerNetwork.close();
		
		return result;
	}
	
	private void deleteJobFromServer() {
		PeerNetworkAgent peerNetwork = connectTo(address, port);
		if (peerNetwork == null) return;
		StringBuffer sb = new StringBuffer();
		sb.append("JobID: ");
		sb.append(jobId);
		
		Message msg = buildMessage("job_cancel", sb.toString());
		msg = peerNetwork.sendAndReceive(msg);
		
		peerNetwork.close();
	}
	
	private void saveJobResult() {
		String result = getJobResultStr();
	}
	
	private void resizeTable() {
		TableColumn column = null;
		for (int i = 0; i < tblJobList.getColumnCount(); i++) {
			column = tblJobList.getColumnModel().getColumn(i);
			if (i == 0)
				column.setPreferredWidth(5);
			else if (i == 1)
				column.setPreferredWidth(100);
			else if (i == 2)
				column.setPreferredWidth(50);
			else if (i == 3)
				column.setPreferredWidth(50);

		}
	}
	
	public void run() {
		contThread = true;
		while (contThread) {
			getJobInfo();
			try {
				sleep(jobMonitorInterval);
			} catch (Exception ex) {
				contThread = false;
			}
		}
	}
	
	private void getJobInfo() {
		PeerNetworkAgent peerNetwork = connectTo(address, port);
		if (peerNetwork == null) return;
		StringBuffer sb = new StringBuffer();
		sb.append("JobID: ");
		sb.append(jobId);
		
		Message msg = buildMessage("job_info", sb.toString());
		msg = peerNetwork.sendAndReceive(msg);
		if (msg.getCode() != 0) {
			StringTokenizer st = new StringTokenizer(msg.getMessage(), "\n", false);
			String jobID = st.nextToken();
			String agentName = st.nextToken();
			
			workDataList.clear();
			String data[] = null;
			while (st.hasMoreTokens()) {
				StringTokenizer stline = new StringTokenizer(st.nextToken(), ":", false);
				if (stline.countTokens() != 4) continue;
				data = new String[4];
				data[0] = stline.nextToken();
				data[1] = stline.nextToken();
				data[2] = stline.nextToken();
				data[3] = stline.nextToken();
				workDataList.add(data);
			}
		}
		
		peerNetwork.close();
		
		tblJobList.invalidate();
		tblJobList.revalidate();
		tblJobList.repaint();
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
	
	private Message buildMessage(String cmdStr, String msgStr) {
		Message msg = new Message();
		msg.setQueryType("HeartBeat");
		msg.setCommand(cmdStr);
		msg.setCode(100);
		msg.setMessage("Service: ResourceManagerAgent\n" + msgStr);

		return msg;
	}
	
	class WorkListTableModel extends AbstractTableModel {
		String columnNames[] = {"Job No", "Address", "Port", "State"};
		
		private JobMonitorThread jobMonitorThread;
		public WorkListTableModel(JobMonitorThread jmt) {
			this.jobMonitorThread = jmt;
		}
		
		public int getColumnCount() { return columnNames.length; }
		public int getRowCount() { return this.jobMonitorThread.workDataList.size(); }
		public Object getValueAt(int row, int col) {
			String[] data = this.jobMonitorThread.workDataList.elementAt(row);
			return data[col];
		}
		public String getColumnName(int col) {
			return columnNames[col];
		}
	}	
}
