package mage.client;

import java.awt.*;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import mage.network.Message;
import mage.ubi.GridSpaceClient;

public class MonitorPanel extends JPanel {
	private GridSpaceClient clientMain = null;
    private EmptyBorder border5 = new EmptyBorder(5, 5, 5, 5);	
	// GUI
	private JLabel lblServiceRepository;
	public JPanel pnlResourceManager;
	
	// Thread
	private Thread myThread = null;
	private boolean contThread = false;
	private boolean threadFinished = false;
	private int resourceGatherPeriod = 5000; // 5 seconds
	
	public MonitorPanel(GridSpaceClient clientMain) {
		this.clientMain = clientMain;
		
		setUI();
		setEvents();
	}
	
	private void setUI() {
		setLayout(new BorderLayout());
		lblServiceRepository = new JLabel("", JLabel.LEFT);
		JPanel pnlServRep = new JPanel();
		pnlServRep.setLayout(new BorderLayout());
		pnlServRep.add(lblServiceRepository, BorderLayout.NORTH);
		pnlServRep.setBorder(new CompoundBorder(new TitledBorder(null, "Service Repository",
				TitledBorder.LEFT, TitledBorder.TOP), border5));	
		
		JPanel pnlResourceManagerOuter = new JPanel();
		pnlResourceManagerOuter.setLayout(new BorderLayout());
		pnlResourceManagerOuter.setBorder(new CompoundBorder(new TitledBorder(null, "Resource Manager",
				TitledBorder.LEFT, TitledBorder.TOP), border5));
		pnlResourceManagerOuter.setLayout(new BorderLayout());
		pnlResourceManager = new JPanel();
		pnlResourceManager.setLayout(new BoxLayout(pnlResourceManager, BoxLayout.X_AXIS));
		pnlResourceManagerOuter.add(new JScrollPane(pnlResourceManager), BorderLayout.CENTER);

		add(pnlServRep, BorderLayout.NORTH);
		add(pnlResourceManagerOuter, BorderLayout.CENTER);
		
		clientMain.addConnectionNotifier(new ConnectionNotifier() {
			public void connected() {
				startResourceGatheringThread();
			}
			
			public void disconnected() {
				stopResourceGatheringThread();
			}
		});
	}
	
	private void startResourceGatheringThread() {
		if (contThread == true) return;
		contThread = true;
		myThread = new Thread() {
			public void run() {
				while (contThread) {
					getResourceInformation();
					try {
						sleep(resourceGatherPeriod);
					} catch (Exception ex) {
					}
				}
				stopResourceMonitorThreads();
				threadFinished = true;
			}
		};
		myThread.start();
	}
	
	private void stopResourceMonitorThreads() {
		for (ResourceMonitorPanel rmp : clientMain.resourceMonitorPanelList) {
			rmp.setContThread(false);
		}
		for (ResourceMonitorPanel rmp : clientMain.resourceMonitorPanelList) {
			while (rmp.isFinishedThread() == false)
				;
		}
	}
	
	private Message buildMessage(String cmdStr, String msgStr) {
		Message msg = new Message();
		msg.setQueryType("HeartBeat");
		msg.setCommand(cmdStr);
		msg.setCode(100);
		msg.setMessage("Service: ServiceRepositoryAgent\n" + msgStr);

		return msg;
	}	
	private void getResourceInformation() {
		lblServiceRepository.setText(clientMain.serviceRepAddr);
		
		// TODO: Gather Resource Information (GUI)
		// Step 1. Gather resource manager list
		Message msg = buildMessage("resource_manager_list", "");
		msg = clientMain.peerNetwork.sendAndReceive(msg);
		StringTokenizer st = new StringTokenizer(msg.getMessage(), "\n", false);
		boolean isChanged = false;
		while (st.hasMoreTokens()) {
			StringTokenizer stHost = new StringTokenizer(st.nextToken(), ":", false);
			String hostAddr = stHost.nextToken();
			int port = parseInt((String)stHost.nextToken());
			ResourceMonitorPanel rmt = findResourceMonitorPanel(hostAddr, port);
			if (rmt == null) {
				rmt = new ResourceMonitorPanel(this, hostAddr, port);
				clientMain.resourceMonitorPanelList.add(rmt);
				isChanged = true;
			}
		}
		if (isChanged) {
			this.revalidate();
			this.invalidate();
			this.repaint();
			
			pnlResourceManager.revalidate();
			pnlResourceManager.invalidate();
			pnlResourceManager.repaint();                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
		}
		
		// Step 2. Display data
	}
	
	private ResourceMonitorPanel findResourceMonitorPanel(String addr, int port) {
		ResourceMonitorPanel pnl = null;
		for (ResourceMonitorPanel tmpPnl : clientMain.resourceMonitorPanelList) {
			if (tmpPnl.getAddress().equals(addr) && tmpPnl.getPort() == port) {
				pnl = tmpPnl;
				break;
			}
		}
		return pnl;
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
	
	private void stopResourceGatheringThread() {
		if (contThread == false) return;
		contThread = false;
		myThread.interrupt();
		threadFinished = false;
		
		while (threadFinished == false)
			;
	}
	
	private void setEvents() {
		
	}
}
