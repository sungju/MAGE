package mage.agent.jobs;

import java.util.HashMap;
import java.util.Random;

import mage.agent.TaskAgent;
import mage.network.Message;

public class SampleAgent extends TaskAgent {

	Random random = new Random(System.currentTimeMillis());
	
	public Message invokeService(String command, int code, String message,
			HashMap arguments) {
		Message msg = new Message();
		msg.setCode(100);
		int total = 0;
		for (int i = 0; i < 100; i++)
			total += random.nextInt();
		msg.setMessage("mage.client.NumberViewer\n" + total);
		System.out.println("<<<<<<<<");
		System.out.println("SampleAgent Result : " + msg.getMessage());
		System.out.println(">>>>>>>>>");
		msg.setQueryType("HeartBeat");
		msg.setCommand("JOB_RESULT");
		
		return msg;
	}

	public void resumeService(HashMap data) {
	}

	public HashMap stopService() {
		return null;
	}

	public void setArguments(HashMap data) {
	}

}
