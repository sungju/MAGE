package mage.ubi;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import mage.agent.AgentInfo;
import mage.agent.AgentLoader;
import mage.client.ConnectionNotifier;
import mage.client.JobMonitorThread;
import mage.client.MonitorPanel;
import mage.client.ResourceMonitorPanel;
import mage.client.ServiceRepositoryViewer;
import mage.network.Message;
import mage.network.NetworkAgentManager;
import mage.network.PeerNetworkAgent;
import mage.network.ServerNetworkAgent;

public class GridSpaceClient extends JFrame {
	private static final long serialVersionUID = 2583944838645509398L;
	
	private String configPath = "." + File.separator + "client_config.xml";
	
	// GUI
	// Menu
	private JMenuBar menuBar;
	private JMenu mConnection, mWindows;
	private JMenuItem miLogOn, miExit;
	private JMenu mJobs, mMonitors;	
	
	// ToolBar
	private JToolBar toolBar = null;
	private JButton btnLogOn;
	private JButton btnExit;
	
	// Internal Frame
	public JDesktopPane deskPane;
	private JInternalFrame iframe;	
	private MonitorPanel monitorPanel;

	
	public Vector<ResourceMonitorPanel> resourceMonitorPanelList = 
		new Vector<ResourceMonitorPanel>();
	// Layout
	private ServiceRepositoryViewer serviceRepViewer;
	private JPanel pnlInnerFrame;
	
	// Network
	public PeerNetworkAgent peerNetwork = null;
	private Vector<ConnectionNotifier> connNotifierList = new Vector<ConnectionNotifier>();
	private static final int CONNECTED = 1;
	private static final int DISCONNECTED = 2;
	public String serviceRepAddr = "";
	
	// Actions
	private Action actionLogOn;
	private Action actionExit;

		
	public GridSpaceClient() {
		readConfig();
		
		setUI();
		setEvents();
	}
	
	private void setUI() {
		setTitle("Grid Space Client");
		Toolkit tk = Toolkit.getDefaultToolkit();
		Image logo = tk.getImage("logo.jpg");
		setIconImage(logo);
		
		setToolbars();
		setMenus();
		
		setLayout(new BorderLayout());
		serviceRepViewer = new ServiceRepositoryViewer(this);
		serviceRepViewer.setPreferredSize(new Dimension(350, 100));
		serviceRepViewer.setBackground(new Color(100, 50, 100));
		deskPane = new JDesktopPane();
		
		iframe = new JInternalFrame("Information Panel for each Node", true, false, true, true);
		deskPane.add(iframe);
		iframe.setSize(550, 400);
		iframe.setVisible(true);
		iframe.setLayout(new BorderLayout());
		monitorPanel = new MonitorPanel(this);
		iframe.add(monitorPanel, BorderLayout.CENTER);
		
		//pnlInnerFrame.add(deskPane, BorderLayout.CENTER);
		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		sp.setLeftComponent(new JScrollPane(serviceRepViewer));
		pnlInnerFrame = new JPanel();
		sp.setRightComponent(new JScrollPane(deskPane));
		add(sp, BorderLayout.CENTER);
		add(toolBar, BorderLayout.NORTH);

		setSizeAndLocation();
	}
	
	private void setSizeAndLocation() {
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();		
//		setSize(screenSize.width, screenSize.height - 30);
	    setSize(1000, 700);
	    setLocation(screenSize.width / 2 - getWidth() / 2,
	    			screenSize.height / 2 - getHeight() / 2);
		setVisible(true);
	}
	
	private void setMenus() {
		menuBar = new JMenuBar();
		mConnection = new JMenu("Connection");
		mConnection.setMnemonic('C');
		
		menuBar.add(mConnection);
		
		miLogOn = new JMenuItem("Log On");
		miLogOn.setAction(actionLogOn);		
		mConnection.add(miLogOn);
		
		miExit = new JMenuItem("Exit");
		miExit.setAction(actionExit);		
		mConnection.addSeparator();
		mConnection.add(miExit);
		
		mWindows = new JMenu("Windows");
		mWindows.setMnemonic('W');
		
		menuBar.add(mWindows);
		
		mJobs = new JMenu("Jobs");
		mJobs.setMnemonic('J');
		mWindows.add(mJobs);
		
		mMonitors = new JMenu("Monitor");
		mMonitors.setMnemonic('M');
		mWindows.add(mMonitors);
		
		setJMenuBar(menuBar);
	}
	
	private void setToolbars() {
		String mageDirStr = ConfigManager.getEnvData("MAGE_HOME");
		String imgDirStr = mageDirStr + File.separator + "images" + File.separator;
		
		toolBar = new JToolBar();
		actionLogOn = new AbstractAction("Log On", new ImageIcon(imgDirStr + "logon.gif")) {
			public void actionPerformed(ActionEvent ae) {
				if (peerNetwork == null) {
					System.out.println("Error : Can't use protocol");
					return;
				}
				
				if (peerNetwork.isConnected()) {
					closeNetwork();
				} else {
					String result = JOptionPane.showInputDialog("Input Address of Service Repository", 
						"203.253.23.77:2005");
					if (result != null) {
						StringTokenizer st = new StringTokenizer(result, ":");
						String addrStr = st.nextToken();
						int port = 2005;
						if (st.hasMoreTokens())
							port = parseInt(st.nextToken());
                	
						connectToServiceRepository(addrStr, port);
					}
                }
			}
		};
		btnLogOn = toolBar.add(actionLogOn);
		btnLogOn.setMnemonic('L');
		btnLogOn.setToolTipText("Log On Server");
		toolBar.addSeparator();
		
		actionExit = new AbstractAction("Exit", new ImageIcon(imgDirStr + "exit.gif")) {
			public void actionPerformed(ActionEvent ae) {
				closeApplication();
			}
		};
		
		btnExit = toolBar.add(actionExit);
		btnExit.setMnemonic('X');
		btnExit.setToolTipText("Exit Application");
		
		toolBar.setFloatable(true);
	}
	
	private void closeApplication() {
		closeNetwork();
		System.exit(0);
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
	
	private long parseLong(String data) {
		long result = 0;
		try {
			result = Long.parseLong(data);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
	private void closeNetwork() {
		notifyNetConnState(DISCONNECTED);		
		peerNetwork.close();
	}
	
	private void connectToServiceRepository(String addr, int port) {
		HashMap data = new HashMap();
		data.put("ADDRESS", addr);
		data.put("PORT", "" + port);
		peerNetwork.setArguments(data);
		
		if (!peerNetwork.connect()) {
			JOptionPane.showMessageDialog(this, "Failed to connect", "SimpleMonitor", JOptionPane.ERROR_MESSAGE);
			return;
		}
		serviceRepAddr = addr + ":" + port;
		notifyNetConnState(CONNECTED);
		serviceRepViewer.getDirList("/");
	}
	
	private void notifyNetConnState(int state) {
		for (ConnectionNotifier cn : connNotifierList) {
			if (state == CONNECTED)
				cn.connected();
			else if (state == DISCONNECTED)
				cn.disconnected();
		}
	}
	
	public void addConnectionNotifier(ConnectionNotifier conn) {
		if (connNotifierList.contains(conn)) return;
		connNotifierList.add(conn);
	}
	
	public void removeConnectionNotifier(ConnectionNotifier conn) {
		if (!connNotifierList.contains(conn)) return;
		connNotifierList.remove(conn);
	}
	
	private void setEvents() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				closeApplication();
			}
		});
	}
		
	public void readConfig() {
		setConfig();
		AgentLoader.loadAgentList(ConfigManager.getEnvData("AGENT_DIR"));
		
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setName("LITH_Talk");
		agentInfo.setClassName("mage.network.protocol.LITHTalk");
		
		//NetworkAgentManager.addNetworkAgent("LITH_Talk", "mage.network.protocol.LITHTalk");
		peerNetwork = ((ServerNetworkAgent)(NetworkAgentManager.findNetworkAgent("LITH_Talk"))).getPeerInstance();				
	}
	
	public void setConfig() {
		InetAddress localAddr = null;
		String nodeName = "localhost";
		String nodeAddress = "127.0.0.1";
				
		try {
			localAddr = InetAddress.getLocalHost();
			nodeName = localAddr.getHostName();
			nodeAddress = localAddr.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		ServiceManager.putData("NodeName", nodeName);
		ServiceManager.putData("NodeAddress", nodeAddress);
		
		ConfigManager.loadConfig(configPath);		
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}
	
	public static void main(String[] args) {
		GridSpaceClient gsc = new GridSpaceClient();
	}

	public void createJobWindow(String resourceManagerAddr, int resourceManagerPort, 
				String message) {
		StringTokenizer st = new StringTokenizer(message, ":");
		if (st.countTokens() != 2) {
			JOptionPane.showMessageDialog(this, "Job ID is incorrect", 
										"Job Submission Failed", JOptionPane.ERROR_MESSAGE);
			return;
		}
		st.nextToken();
		long jobId = parseLong(st.nextToken().trim());

		iframe = new JInternalFrame("Job ID : " + jobId, true, false, true, true);
		deskPane.add(iframe);
		new JobMonitorThread(this, iframe, resourceManagerAddr, resourceManagerPort, jobId).start();
	}	
}
