package mage.job;

import java.util.HashMap;

import mage.agent.TaskAgent;
import mage.network.Message;

public abstract class JobAdaptorAgent extends TaskAgent {

	public Message invokeService(String command, int code, String message,
			HashMap arguments) {
		Message msg = null;
		
		System.out.println("CMD : " + command);
		
		if (command.equals("job_start")) {
			msg = jobStart(command, arguments);
		} else if (command.equals("job_status")) {
			msg = jobStatus(command, arguments);
		} else if (command.equals("job_stop")) {
			msg = jobStop(command, arguments);
		} else if (command.equals("job_result")) {
			msg = jobResult(command, arguments);
		} else if (command.equals("result_viewer")) {
			msg = resultViewer(command, arguments);
		}
		return msg;
	}

	public void resumeService(HashMap data) {
	}

	public HashMap stopService() {
		return null;
	}

	public abstract Message jobStart(String command, HashMap arguments);
	public abstract Message jobStatus(String command, HashMap arguments);
	public abstract Message jobStop(String command, HashMap arguments);
	public abstract Message jobResult(String command, HashMap arguments);
	public abstract Message resultViewer(String command, HashMap arguments);

}
