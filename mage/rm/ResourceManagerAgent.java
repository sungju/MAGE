package mage.rm;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import mage.agent.AgentInfo;
import mage.agent.TaskAgent;
import mage.network.Message;
import mage.network.NetworkAgentManager;
import mage.network.PeerNetworkAgent;
import mage.network.ServerNetworkAgent;
import mage.ubi.ConfigManager;
import mage.ubi.ServiceManager;
import mage.utils.Base64;

public class ResourceManagerAgent extends TaskAgent {
	private String servRepAddr;
	private int servRepPort;
	
	private HashMap<String,WorkNodeInfo> workNodeInfoList = new HashMap<String, WorkNodeInfo>();
	
	private HashMap<String,JobGroup> jobGroupList = new HashMap<String,JobGroup>();
	
	private static final String rm_queryType = "HeartBeat";
	private static final int rm_port = 5252;

	public Message invokeService(String command, int code, String message,
			HashMap arguments) {
		Message msg = null;
		
		
		System.out.println("ResourceManagerAgent : " + command);
		
		if (command.equals("workinfo")) {
			// 작업 노드의 정보를 업데이트함
			msg = doWorkInfo(command, arguments);
		} else if (command.equals("worklist")) {
			// 작업 노드들의 정보를 반환함
			msg = doWorkList(command, arguments);
		} else if (command.equals("execute_job")) {
			msg = doExecuteJob(command, arguments);
		} else if (command.equals("job_info")) {
			msg = doJobInfo(command, arguments);
		} else if (command.equals("job_result")) {
			msg = doJobResult(command, arguments);
		} else if (command.equals("job_cancel")) {
			msg = doJobCancel(command, arguments);
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
	
	private Message doExecuteJob(String command, HashMap arguments) {
		// Arguments
		// Directory: Agent 파일의 디렉토리
		// Filename: Agent 파일 이름
		// ExecCount: 실행할 작업의 수
		// AutoSelect: 자동 선택인지의 여부를 알려줌(1: 자동 선택, 0: 사용자 지정)
		// JobData: 작업 데이타(여러 작업에 분할될 수 있는 형태로 발전시켜야 함)
		// WorkNodeList: 작업 노드들의 주소
		Message msg = new Message();
		msg.setCode(0);
		
		String directory = (String)arguments.get("DIRECTORY");
		String fileName = (String)arguments.get("FILENAME");
		int execCount = parseInt((String)arguments.get("EXECCOUNT"));
		int autoSelect = parseInt((String)arguments.get("AUTOSELECT"));
		String jobData = (String)arguments.get("JOBDATA");
		String workNodeList = (String)arguments.get("WORKNODELIST");
		
		String agentBinary = getAgentFromServRep(servRepAddr, servRepPort,
												directory, fileName);
		if (agentBinary == null || agentBinary.length() == 0) {
			msg.setMessage("AGENT NOT FOUND");
			return msg;
		}
		
		long jobId = executeJob(fileName, agentBinary, execCount, 
							jobData, autoSelect == 1, workNodeList);
		if (jobId == -1) {
			msg.setMessage("EXECUTION FAILED");
			return msg;
		}
		
		msg.setCode(100);
		msg.setMessage("JobID: " + jobId);
		
		return msg;
	}
	
	private String getAgentFromServRep(String addr, int port, String dir, String fileName) {
		StringBuffer sb = new StringBuffer();
		PeerNetworkAgent peerNetwork = connectTo(servRepAddr, servRepPort);

		sb.append("Directory: " + dir + "\n");
		sb.append("Filename: " + fileName + "\n");
		sb.append("Encoding: base64\n");
		
		Message msg = buildMessage("download", sb.toString());
		msg = peerNetwork.sendAndReceive(msg);
		if (msg.getCode() == 0) {
			return null;			
		}
		
		StringTokenizer st = new StringTokenizer(msg.getMessage(), "\n");
		if (st.countTokens() < 3) {
			return null;
		}
		
		String downloadFileName = st.nextToken();
		String encode = st.nextToken();
		
		peerNetwork.close();		
		String agentBinary = st.nextToken();
		
		return agentBinary;
	}
	
	private long executeJob(String agentName, String agentBinary, int execCount, 
						String jobData, boolean autoSelect, String workNodeList) {
		long jobId = -1;
		
		if (workNodeInfoList.isEmpty()) return jobId;
		Vector<String> workNodes = new Vector<String>();

		int cnt = 0;
		if (autoSelect) { // AutoSelect Mode
			for (WorkNodeInfo wni : workNodeInfoList.values()) {
				workNodes.add(wni.getAddress() + ":" + wni.getPort());
				cnt++;
//				if (cnt >= execCount) break;
			}
			execCount = cnt;
		} else {
			StringTokenizer st = new StringTokenizer(workNodeList, ",", false);
			if (st.countTokens() == 0) return jobId;
			while (st.hasMoreTokens()) {
				workNodes.add(st.nextToken());
				cnt++;
				if (cnt >= execCount) break;
			}
		}
		
		JobGroup jobGroup = new JobGroup(agentName, agentBinary, execCount, jobData, workNodes);
		jobId = addJobGroup(jobGroup);
		jobGroup.startJob();
		
		return jobId;
	}
	
	private long addJobGroup(JobGroup jobGroup) {
		long jobId = -1;
		
		do {
			jobId = System.currentTimeMillis();			
		} while (jobGroupList.containsKey("" + jobId));
		jobGroupList.put("" + jobId, jobGroup);
		
		return jobId;
	}

	
	private Message doJobInfo(String command, HashMap arguments) {
		// Arguments
		// JobID: 작업 ID
		Message msg = new Message();
		msg.setCode(0);
		
		long jobId = parseLong((String)arguments.get("JOBID"));
		JobGroup jobGroup = jobGroupList.get("" + jobId);
		if (jobGroup == null) {
			msg.setMessage("Unknown Job Group");
			return msg;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("ID: " + jobId);
		sb.append("\n");
		sb.append("AgentName: " + jobGroup.getAgentName());
		sb.append("\n");
		for (JobInfo jobInfo : jobGroup.getJobInfoList()) {
			sb.append(jobInfo.getJobNo());
			sb.append(":");
			sb.append(jobInfo.getAddress());
			sb.append(":");
			sb.append(jobInfo.getPort());
			sb.append(":");
			sb.append(jobInfo.getStateStr());
			sb.append("\n");
		}
		msg.setCode(100);
		msg.setMessage(sb.toString());
		
		return msg;
	}
	
	private Message doJobResult(String command, HashMap arguments) {
		// Arguments
		// JobID: 작업 ID
		Message msg = new Message();
		msg.setCode(0);
		
		long jobId = parseLong((String)arguments.get("JOBID"));
		JobGroup jobGroup = jobGroupList.get("" + jobId);
		if (jobGroup == null) {
			msg.setMessage("Unknown Job Group");
			return msg;
		}
		
		jobGroup.waitUntilAllReceiveResult();
		
		StringBuffer sb = new StringBuffer();
		sb.append("ID: " + jobId);
		sb.append("\n");
		sb.append("AgentName: " + jobGroup.getAgentName());
		sb.append("\n");
		boolean flag = false;
		for (JobInfo jobInfo : jobGroup.getJobInfoList()) {
			if (flag == false) {
				sb.append("Job_Result_Viewer: ");
				sb.append(jobInfo.getJobViewerName());
				sb.append("\n");
				flag = true;
			}
			sb.append(jobInfo.getJobNo());
			sb.append(":");
			sb.append(jobInfo.getAddress());
			sb.append(":");
			sb.append(jobInfo.getPort());
			sb.append(":");
			sb.append(jobInfo.getJobResult());
			sb.append("\n");
		}
		
		msg.setCode(100);
		msg.setMessage(sb.toString());
		
		return msg;
	}
	
	private Message doJobCancel(String command, HashMap arguments) {
		// Arguments
		// JobID: 작업 ID
		Message msg = new Message();
		msg.setCode(0);
		
		long jobId = parseLong((String)arguments.get("JOBID"));
		JobGroup jobGroup = jobGroupList.get("" + jobId);
		if (jobGroup == null) {
			msg.setMessage("Unknown Job Group");
			return msg;
		}
		jobGroup.stopJob();
		jobGroupList.remove("" + jobId);
		
		msg.setCode(100);
		msg.setMessage("SUCCESS");
		
		return msg;
	}
	
	private Message doWorkInfo(String command, HashMap arguments) {
		// Arguments
		// Address: 작업 노드의 IP Address
		// Port: 작업 노드의 Port 번호
		// OS: 사용중인 운영체제
		// Speed: CPU 속도
		// MemTotal: 메모리 총 량
		// MemFree: 사용하지 않고 있는 메모리
		// CpuUsage: CPU 사용량
		Message msg = new Message();
		
		String address = (String)arguments.get("ADDRESS");
		int port = parseInt((String)arguments.get("PORT"));
		String os = (String)arguments.get("OS");
		String speed = (String)arguments.get("SPEED");
		int memTotal = parseInt((String)arguments.get("MEMTOTAL"));
		int memFree = parseInt((String)arguments.get("MEMFREE"));
		int cpuUsage = parseInt((String)arguments.get("CPUUSAGE"));
		
		WorkNodeInfo wni = findWorkNodeInfo(address + ":" + port);
		if (wni == null)
			wni = new WorkNodeInfo();
		
		wni.setAddress(address);
		wni.setPort(port);
		wni.setOs(os);
		wni.setSpeed(speed);
		wni.setMemTotal(memTotal);
		wni.setMemFree(memFree);
		wni.setCpuUsage(cpuUsage);
		
		addWorkNodeInfo(address + ":" + port, wni);
		
		msg.setCode(100);
		msg.setMessage("SUCCESS");
		
		return msg;
	}
	
	private void addWorkNodeInfo(String key, WorkNodeInfo wni) {
		workNodeInfoList.put(key, wni);
	}
	
	private WorkNodeInfo findWorkNodeInfo(String key) {
		return workNodeInfoList.get(key);
	}
	
	private Message doWorkList(String command, HashMap arguments) {
		// Arguments
		// None
		Message msg = new Message();
		StringBuffer sb = new StringBuffer();
		
		for (WorkNodeInfo wni : workNodeInfoList.values()) {
			sb.append(wni.getAddress());
			sb.append(":");
			sb.append(wni.getPort());
			sb.append(":");
			sb.append(wni.getOs());
			sb.append(":");
			sb.append(wni.getSpeed());
			sb.append(":");
			sb.append(wni.getMemTotal());
			sb.append(":");
			sb.append(wni.getMemFree());
			sb.append(":");
			sb.append(wni.getCpuUsage());
			sb.append("\n");			
		}
		msg.setCode(100);
		msg.setMessage(sb.toString());
		
		return msg;
	}

	public void resumeService(HashMap data) {
	}

	public HashMap stopService() {
		return null;
	}

	public void setArguments(HashMap data) {
		super.setArguments(data);

		try {
			servRepAddr = (String)arguments.get("ServiceRepositoryAddress");
			servRepPort = parseInt((String)arguments.get("ServiceRepositoryPort"));
		} catch (Exception ex) {
			servRepAddr = null;
		}
		if (servRepAddr != null && servRepPort != 0) {
			registerToServiceRepository();
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
	
	private void registerToServiceRepository() {
		PeerNetworkAgent peerNetwork = connectTo(servRepAddr, servRepPort);
		StringBuffer sb = new StringBuffer();
		sb.append("ADDRESS: ");
		sb.append(ServiceManager.getData("NodeAddress"));
		sb.append("\n");
		sb.append("PORT: ");
		sb.append(rm_port);
		sb.append("\n");
		sb.append("QUERYTYPE: ");
		sb.append(rm_queryType);
		sb.append("\n");
		
		Message msg = buildMessage("add_resource_manager", sb.toString());
		msg = peerNetwork.sendAndReceive(msg);	
		
		peerNetwork.close();
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
}
