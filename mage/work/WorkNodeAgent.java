package mage.work;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import mage.agent.AgentInfo;
import mage.agent.AgentLoader;
import mage.agent.TaskAgent;
import mage.network.Message;
import mage.network.NetworkAgentManager;
import mage.network.PeerNetworkAgent;
import mage.network.ServerNetworkAgent;
import mage.ubi.ConfigManager;
import mage.ubi.ServiceManager;
import mage.utils.Base64;

public class WorkNodeAgent extends TaskAgent {
	private String resourceManagerAddress;
	private int resourceManagerPort;
	
	private Runtime myRuntime = Runtime.getRuntime();
	
	private String tmpDirPath = null;
	
	// Thread
	private Thread myThread;
	private boolean contThread = false;
	private int heartBeatInterval = 10000; // 10초에 한번씩  

	public Message invokeService(String command, int code, String message,
			HashMap arguments) {
		Message msg = null;
		
		if (command.equals("upload_agent")) {
			msg = doUploadAgent(command, arguments);
		} else if (command.equals("remove_agent")) {
			msg = doRemoveAgent(command, arguments);
		}

		if (msg == null) {
			msg = new Message();
			msg.setCode(0);
			msg.setMessage("Unknown command");
		}
		
		msg.setQueryType("HeartBeat");
		msg.setCommand("RM_RESULT");

		return msg;	
	}
	
	private Message doUploadAgent(String command, HashMap arguments) {
		// Arguments
		// AgentName: Agent의 파일 이름
		// AgentBinary: Agent의 실행 파일 내용
		Message msg = new Message();
		msg.setCode(0);

		String agentName = (String)arguments.get("AGENTNAME");
		String agentBinary = (String)arguments.get("AGENTBINARY");
		
		String agentFileName = tmpDirPath + agentName;
	
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(agentFileName);
			byte[] contents;
			contents = Base64.decode(agentBinary);
			fos.write(contents);
			msg.setCode(100);
		} catch (Exception ex) {
			ex.printStackTrace();
			msg.setMessage("FAILED");
		} finally {
			if (fos != null)
				try { fos.close(); } catch (Exception e) { }
		}
		if (msg.getCode() == 100) {
			Vector<AgentInfo> agentInfoList = AgentLoader.loadAgent(agentFileName, true);
			StringBuffer sb = new StringBuffer();
			sb.append("SUCCESS\n");
			for (AgentInfo agentInfo : agentInfoList) {
				sb.append(agentInfo.getName());
				sb.append("\n");
			}
			msg.setMessage(sb.toString());
		}
		
		return msg;
	}
	
	private Message doRemoveAgent(String command, HashMap arguments) {
		// Arguments
		// AgentName: Agent의 파일 이름
		Message msg = new Message();
		msg.setCode(0);
		
		String agentName = (String)arguments.get("AGENTNAME");
		String agentFileName = tmpDirPath + agentName;
		
		Vector<AgentInfo> agentInfoList = AgentLoader.readJarInfo(agentFileName, true, false);
		for (AgentInfo agentInfo : agentInfoList)
			AgentLoader.removeFromManager(agentInfo);
		
		File file = new File(agentFileName);
		if (file.exists() && file.delete()) {
			msg.setCode(100);
			msg.setMessage("SUCCESS");
		} else {
			msg.setMessage("FAILED");
		}
		
		return msg;
	}

	public void resumeService(HashMap data) {
	}

	public HashMap stopService() {
		return null;
	}

	public void setArguments(HashMap data) {
		super.setArguments(data);
		
		resourceManagerAddress = (String)arguments.get("ResourceManagerAddress");
		resourceManagerPort = parseInt((String)arguments.get("ResourceManagerPort"));
		
		tmpDirPath = ConfigManager.getEnvData("TMP_DIR");
		if (tmpDirPath == null)
			tmpDirPath = ConfigManager.getEnvData("AGENT_DIR");
		if (!tmpDirPath.endsWith("/"))
			tmpDirPath += "/";
		
		contThread = true;
		myThread = new Thread() {
			public void run() {
				while (contThread) {
					sendMonitorDataToResourceManager();
					try {
						sleep(heartBeatInterval);
					} catch (Exception ex) {
					}
				}
			}
		};
		myThread.start();
	}
	
	private Message buildMessage(String cmdStr, String msgStr) {
		Message msg = new Message();
		msg.setQueryType("HeartBeat");
		msg.setCommand(cmdStr);
		msg.setCode(100);
		msg.setMessage("Service: ResourceManagerAgent\n" + msgStr);

		return msg;
	}
	private void sendMonitorDataToResourceManager() {
		PeerNetworkAgent peerNetwork = connectTo(resourceManagerAddress, resourceManagerPort);
		if (peerNetwork == null) return;
		
		StringBuffer sb = new StringBuffer();
		sb.append("Address: ");
		sb.append(getAddress());
		sb.append("\n");
		sb.append("Port: ");
		sb.append(getPort());
		sb.append("\n");
		sb.append("OS: ");
		sb.append(getOs());
		sb.append("\n");
		sb.append("Speed: ");
		sb.append(getSpeed());
		sb.append("\n");
		sb.append("MemTotal: ");
		sb.append(getMemTotal());
		sb.append("\n");
		sb.append("MemFree: ");
		sb.append(getMemFree());
		sb.append("\n");
		sb.append("CpuUsage: ");
		sb.append(getCpuUsage());
		sb.append("\n");

		System.out.println("[[" + sb.toString() + "]]");
		
		Message msg = buildMessage("workinfo", sb.toString());
		msg = peerNetwork.sendAndReceive(msg);	
		
		peerNetwork.close();		
	}
	
	private String getAddress() {
		return (String)ServiceManager.getData("NodeAddress");
	}
	
	private int getPort() {
		return 9001;
	}
	
	private String getOs() {
		// System.getProperty("os.arch");
		return System.getProperty("os.name");
	}
	
	private String getSpeed() {
		return "Unknown";
	}
	
	private long getMemTotal() {
		return myRuntime.totalMemory();
	}
	
	private long getMemFree() {
		return myRuntime.freeMemory();
	}
	
	private int getCpuUsage() {
		String os = System.getProperty("os.name");
		if (os.startsWith("Mac"))
			return getCpuUsageOnMac();
		else if (os.startsWith("Linux"))
			return getCpuUsageOnLinux();
		
		return -1;
	}
	
	private int getCpuUsageOnLinux() {
		int idle = 0;
		
		try {
			Process p = Runtime.getRuntime().exec("iostat -c");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

			in.readLine();
			in.readLine();
			in.readLine();
			String resultStr = in.readLine();
			StringTokenizer st = new StringTokenizer(resultStr, " ", false);

			while (st.countTokens() > 1)
				st.nextToken();
			
			idle = parseIntFromDouble(st.nextToken());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 100 - idle;
	}
	
	private int parseIntFromDouble(String data) {
		int result = 0;
		try {
			StringTokenizer st = new StringTokenizer(data.trim(), ".", false);
			result = Integer.parseInt(st.nextToken());
		} catch (Exception ex) {
			result = 0;
		}
		return result;
	}
	
	private int getCpuUsageOnMac() {
		int user = 0, sys = 0;
		
		try {
			Process p = Runtime.getRuntime().exec("iostat");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

			in.readLine();
			in.readLine();
			String resultStr = in.readLine();
			StringTokenizer st = new StringTokenizer(resultStr, " ", false);
			while (st.countTokens() > 3)
				st.nextToken();
			
			user = Integer.parseInt(st.nextToken());
			sys = Integer.parseInt(st.nextToken());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return user + sys;
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
	
	private int parseInt(String numStr) {
		int num = 0;
		try {
			num = Integer.parseInt(numStr);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return num;
	}
}
