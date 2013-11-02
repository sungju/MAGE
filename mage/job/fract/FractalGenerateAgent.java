package mage.job.fract;

import java.util.HashMap;

import mage.job.JobAdaptorAgent;
import mage.job.JobState;
import mage.network.Message;

public class FractalGenerateAgent extends JobAdaptorAgent {
	static FractalGenerator fractalGenerator = null;

	@Override
	public Message jobResult(String command, HashMap arguments) {
		Message msg = new Message();
		
		if (fractalGenerator == null) {
			msg.setCode(0);
			msg.setMessage("Job is not running");
			return msg;
		}
		
		msg.setCode(fractalGenerator.getResultCode());
		msg.setMessage(fractalGenerator.getResultData());
		
		return msg;
	}

	@Override
	public Message jobStart(String command, HashMap arguments) {
		Message msg = new Message();
		
		if (fractalGenerator != null) {
			msg.setCode(0);
			msg.setMessage("Already Start");
			return msg;
		}
		
		fractalGenerator = new FractalGenerator(arguments);
		fractalGenerator.start();

		msg.setCode(100);
		msg.setMessage("Job started");
		
		return msg;
	}

	@Override
	public Message jobStatus(String command, HashMap arguments) {
		Message msg = new Message();
		
		if (fractalGenerator == null) {
			msg.setCode(0);
			msg.setMessage("Job is not created");
			return msg;
		}
		
		msg.setCode(100);
		msg.setMessage(JobState.getJobStateStr(fractalGenerator.getJobState()));
		return msg;
	}

	@Override
	public Message jobStop(String command, HashMap arguments) {
		Message msg = new Message();
		if (fractalGenerator == null) {
			msg.setCode(0);
			msg.setMessage("Job is not running");
			return msg;
		}
		fractalGenerator.interrupt();
		
		msg.setCode(100);
		msg.setMessage("Job Stopped");
		fractalGenerator = null;
		
		return msg;
	}

	@Override
	public Message resultViewer(String command, HashMap arguments) {
		Message msg = new Message();
		
		msg.setCode(100);
		msg.setMessage("mage.client.FractalViewer");
		return msg;
	}
}
