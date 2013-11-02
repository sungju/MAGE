package mage.client;

import javax.swing.JFrame;

public abstract class ResultViewerAdaptor {
	JFrame mainFrame = null;
	String jobId = null;
	String agentName = null;
	String resultStr = null;
	
	public ResultViewerAdaptor() {
	}
	
	public void setData(JFrame mainFrame, String jobId, String agentName,
						String resultStr) {
		this.mainFrame = mainFrame;
		this.jobId = jobId;
		this.agentName = agentName;
		this.resultStr = resultStr;
	}
	
	public abstract void viewResult();
}
