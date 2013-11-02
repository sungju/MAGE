package mage.rm;

import java.util.StringTokenizer;
import java.util.Vector;

public class JobGroup {
	private String agentName = null;
	private String agentBinary = null;
	private String jobData = null;
	private Vector<String> workNodes = null;
	
	private Vector<JobInfo> jobInfoList = new Vector<JobInfo>();
	private int execCount = 0;
	
	public JobGroup(String agentName, String agentBinary, int execCount,
					String jobData, Vector<String> workNodes) {
		this.agentName = agentName;
		this.agentBinary = agentBinary;
		this.execCount = execCount;
		this.jobData = jobData;
		this.workNodes = workNodes;
	}
	
	public void startJob() {
		JobInfo jobInfo = null;
		int jobNo = 0;
		for (String nodeStr : workNodes) {
			StringTokenizer st = new StringTokenizer(nodeStr, ":", false);
			if (st.countTokens() != 2) continue;
			jobNo++;
			jobInfo = new JobInfo(this, jobNo);
			jobInfo.setAddress(st.nextToken());
			jobInfo.setPort(parseInt(st.nextToken()));
			jobInfoList.add(jobInfo);
			jobInfo.startJob();
		}
	}
	
	public void stopJob() {
		for (JobInfo jobInfo : jobInfoList) {
			jobInfo.stopJob();
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

	public String getAgentBinary() {
		return agentBinary;
	}

	public void setAgentBinary(String agentBinary) {
		this.agentBinary = agentBinary;
	}

	public String getJobData() {
		return jobData;
	}

	public void setJobData(String jobData) {
		this.jobData = jobData;
	}

	public Vector<JobInfo> getJobInfoList() {
		return jobInfoList;
	}

	public void setJobInfoList(Vector<JobInfo> jobInfoList) {
		this.jobInfoList = jobInfoList;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public int getExecCount() {
		return execCount;
	}

	public void setExecCount(int execCount) {
		this.execCount = execCount;
	}	


	int threadCount = 0;
	public synchronized void waitUntilAllReceiveResult() {
		threadCount = 0;
		for (JobInfo jobInfo : jobInfoList) {
			new JobResultReceiveThread(jobInfo).start();			
		}
		
		try {
			while (threadCount > 0)
				Thread.sleep(500);
		} catch (Exception ex) {
		}
	}
	
	class JobResultReceiveThread extends Thread {
		JobInfo jobInfo = null;
		public JobResultReceiveThread(JobInfo jobInfo) {
			this.jobInfo = jobInfo;
			threadCount++;
		}
		public void run() {
			jobInfo.receiveJobResult();
			threadCount--;
		}
	}
}
