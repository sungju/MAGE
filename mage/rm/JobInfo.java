package mage.rm;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import mage.agent.AgentInfo;
import mage.job.JobState;
import mage.network.Message;
import mage.network.NetworkAgentManager;
import mage.network.PeerNetworkAgent;
import mage.network.ServerNetworkAgent;
import mage.ubi.ServiceManager;
import mage.utils.Base64;

public class JobInfo {
	private static final int JOB_INIT = 0;
	private static final int JOB_CONNECTING = 1;
	private static final int JOB_CONNECTED = 2;
	private static final int JOB_CONNECTION_FAILED = 3;
	
	private static final int JOB_UPLOADING = 4;
	private static final int JOB_UPLOADED = 5;
	
	private static final int JOB_REQUEST = 6;
	private static final int JOB_PROGRESS = 7;
	private static final int JOB_FINISHED = 8;
	
	private static final int JOB_REMOVE_AGENT = 9;
	private static final int JOB_CLOSED = 10;
	
	private static final int JOB_CANCELED = 11;
	private static final int JOB_FAILED = 12;
	
	private static final String[] jobStateStr = {
		"Init",
		"Connecting",
		"Connected",
		"Connection failed",
		"Upload",
		"Uploaded",
		"Request",
		"Progress",
		"Finished",
		"Agent remove",
		"Closed",
		"Canceled",
		"Failed"
	};
	
	private String address = null;
	private int port = 0;
	private JobGroup jobGroup = null;
	private int state = JOB_INIT;
	private int jobNo = 0;
	
	private boolean resultReceived = false;
	
	// Thread
	private Thread myThread = null;
	
	// Work Node
	private PeerNetworkAgent peerNetwork = null;
	
	// Work Service Names
	Vector<String> workServiceNames = null;
	Vector<Message> resultMsgList = null;
	
	public String getStateStr() {
		return jobStateStr[state];
	}
	
	public JobInfo(JobGroup jobGroup, int jobNo) {
		this.jobGroup = jobGroup;
		this.jobNo = jobNo;
	}
	
	public void startJob() {
		myThread = new Thread() {
			public void run() {
				try {
					peerNetwork = connectToNode();
					if (peerNetwork != null) { 
						uploadAgent();
						if (state != JOB_FAILED)
							executeJob();
					}
				} catch (Exception ex) {
					state = JOB_CANCELED;
					peerNetwork = null;
				} finally {
					if (peerNetwork != null) {
						waitUntilFinish();
						receiveJobResult();
	//					closeConnection();
					}
				}
			}
		};
		myThread.start();
	}
	
	public void stopJob() {
		if (myThread != null) {
			myThread.interrupt();
			myThread = null;
		}
		closeConnection();
	}
	
	private PeerNetworkAgent connectToNode() {
		state = JOB_CONNECTING;
		
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setName("LITH_Talk");
		agentInfo.setClassName("mage.network.protocol.LITHTalk");
		
		PeerNetworkAgent peerNetwork = ((ServerNetworkAgent)(NetworkAgentManager.findNetworkAgent("LITH_Talk"))).getPeerInstance();
		
		HashMap data = new HashMap();
		data.put("ADDRESS", address);
		data.put("PORT", "" + port);
		peerNetwork.setArguments(data);
		
		if (!peerNetwork.connect()) {
			state = JOB_CONNECTION_FAILED;
			return null;
		}	
		state = JOB_CONNECTED;
		return peerNetwork;		
	}
	
	private void uploadAgent() {
		state = JOB_UPLOADING;
		
		StringBuffer sb = new StringBuffer();
		sb.append("AgentName: ");
		sb.append(jobGroup.getAgentName());
		sb.append("\n");
		sb.append("AgentBinary: ");
		sb.append(jobGroup.getAgentBinary());
		sb.append("\n");
				
		Message msg = buildMessage("upload_agent", sb.toString());
		msg = peerNetwork.sendAndReceive(msg);
		if (msg.getCode() == 0) {
			state = JOB_FAILED;
		} else {
			state = JOB_UPLOADED;
			workServiceNames = new Vector<String>();
			StringTokenizer st = new StringTokenizer(msg.getMessage());
			st.nextToken();
			while (st.hasMoreTokens()) {
				workServiceNames.add(st.nextToken());
			}
		}
	}
	
	private void executeJob() {
		for (String serviceName : workServiceNames) {
			state = JOB_REQUEST;
			StringBuffer sb = new StringBuffer();
			sb.append("Service: ");
			sb.append(serviceName);
			sb.append("\n");
			sb.append("ExecCount: ");
			sb.append(jobGroup.getExecCount());
			sb.append("\n");
			sb.append("JobNo: ");
			sb.append(jobNo);
			sb.append("\n");
			sb.append("JobData: ");
			sb.append(jobGroup.getJobData());
			sb.append("\n");

			Message msg = buildMessage("job_start", "");
			msg.setMessage(sb.toString());
			peerNetwork.sendAndReceive(msg);
		}
		state = JOB_PROGRESS;		
	}
	
	public synchronized void receiveJobResult() {
		if (resultReceived) return;
		resultMsgList = new Vector<Message>();
		for (String serviceName : workServiceNames) {
			StringBuffer sb = new StringBuffer();
			sb.append("Service: ");
			sb.append(serviceName);
			sb.append("\n");
			
			Message msg = buildMessage("job_result", "");
			msg.setMessage(sb.toString());
			msg = peerNetwork.sendAndReceive(msg);
			resultMsgList.add(msg);
			
			if (msg.getCode() != 0) resultReceived = true;
		}
	}
	
	public String getJobViewerName() {
		StringBuffer resultViewerStr = new StringBuffer();
		
		for (String serviceName : workServiceNames) {
			StringBuffer sb = new StringBuffer();
			sb.append("Service: ");
			sb.append(serviceName);
			sb.append("\n");
			
			Message msg = buildMessage("result_viewer", "");
			msg.setMessage(sb.toString());
			msg = peerNetwork.sendAndReceive(msg);
			resultViewerStr.append(msg.getMessage());
			resultViewerStr.append(":");
		}
		return resultViewerStr.toString();
	}
	
	private void waitUntilFinish(String serviceName) {
		StringBuffer sb = new StringBuffer();
		sb.append("Service: ");
		sb.append(serviceName);
		sb.append("\n");
		
		Message msg = buildMessage("job_status", "");
		msg.setMessage(sb.toString());
		Message resultMsg = null;

		do {			
			resultMsg = peerNetwork.sendAndReceive(msg);
			if (resultMsg == null || resultMsg.getCode() == 0) {
				state = JOB_FAILED;
				return;
			}

			if (resultMsg.getMessage().equals(JobState.getJobStateStr(JobState.JOB_FINISHED))) {
				state = JOB_FINISHED;				
				return;
			}
			
			try {
				Thread.sleep(3000);
			} catch (Exception ex) {
				break;
			}			
		} while (true);
	}
	
	private void waitUntilFinish() {
		for (String serviceName : workServiceNames) {
			waitUntilFinish(serviceName);
		}		
	}

	private Message buildMessage(String cmdStr, String msgStr) {
		Message msg = new Message();
		msg.setQueryType("HeartBeat");
		msg.setCommand(cmdStr);
		msg.setCode(100);
		msg.setMessage("Service: WorkNodeAgent\n" + msgStr);

		return msg;
	}
	
	private void stopAgent() {
		for (String serviceName : workServiceNames) {
			StringBuffer sb = new StringBuffer();
			sb.append("Service: ");
			sb.append(serviceName);
			sb.append("\n");
			
			Message msg = buildMessage("job_stop", "");
			msg.setMessage(sb.toString());
			msg = peerNetwork.sendAndReceive(msg);
		}
	}
	
	private void closeConnection() {
		stopAgent();
		StringBuffer sb = new StringBuffer();
		sb.append("AgentName: ");
		sb.append(jobGroup.getAgentName());
		sb.append("\n");
		
		Message msg = buildMessage("remove_agent", sb.toString());
		msg = peerNetwork.sendAndReceive(msg);
		
		peerNetwork.close();
		peerNetwork = null;
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

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getJobNo() {
		return jobNo;
	}

	public void setJobNo(int jobNo) {
		this.jobNo = jobNo;
	}

	public String getJobResult() {	
		if (!resultReceived)
			receiveJobResult();
		
		StringBuffer sb = new StringBuffer();
		for (Message msg : resultMsgList) {
			sb.append(msg.getCode());
			sb.append(":");
			sb.append(msg.getMessage());
			sb.append("\n");
		}
		return Base64.encode(sb.toString().getBytes());
	}
}
