package mage.network;

import java.util.HashMap;
import java.util.Iterator;
import mage.agent.AgentInfo;
import mage.agent.AgentLoader;

public class NetworkCommManager {
	private static HashMap networkCommInfoList = new HashMap();

	public static void addNetworkComm(AgentInfo netComm) {
		NetworkCommManager.networkCommInfoList.put(netComm.getName(), netComm);
	}
		
	public static void removeNetworkComm(String name) {
		if (!networkCommInfoList.containsKey(name)) {
			System.out.println("ERROR: " + name + " does not exist");
			return;
		}
		networkCommInfoList.remove(name);
	}
	
	public static NetworkCommunicationAgent findNetworkComm(String name) {
		NetworkCommunicationAgent nci = null;
		AgentInfo ainfo = (AgentInfo)networkCommInfoList.get(name);
		if (ainfo == null) {
			System.out.println("AgentInfo not found");
			return null;
		}
		nci = (NetworkCommunicationAgent)ainfo.getInstance();
		if (nci == null || !ainfo.isActive()) {
			nci = (NetworkCommunicationAgent)AgentLoader.activateAgent(ainfo);
		}
		if (nci != null) {
			HashMap args = ainfo.getArguments();
			nci.setManagerName(ainfo.getName());
			if (args.get("protocol") != null) {
				nci.setServerProtocol((String)ainfo.getArguments().get("protocol"));
			}
		}
		return nci;
	}
	
	public static NetworkCommunicationAgent startNetworkComm(String name) {
		NetworkCommunicationAgent nci = findNetworkComm(name);
		if (nci != null){
			nci.start();
		}
		
		return nci;
	}
	
	public static Iterator getNetworkCommNames() {
		return networkCommInfoList.keySet().iterator();
	}
	
	public static String getNetworkCommClassName(String name) {
		AgentInfo ainfo = (AgentInfo)networkCommInfoList.get(name);
		if (ainfo == null) return "";
		return ainfo.getClassName();
	}	
}
