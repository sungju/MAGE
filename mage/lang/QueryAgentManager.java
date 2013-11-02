package mage.lang;

import java.util.HashMap;
import java.util.Iterator;
import mage.agent.AgentInfo;
import mage.agent.AgentLoader;

public class QueryAgentManager {
	private static HashMap queryAgentInfoList = new HashMap();

	public static void addQueryAgent(AgentInfo query) {
		QueryAgentManager.queryAgentInfoList.put(query.getName(), query);
	}
		
	public static void removeQueryAgent(String name) {
		if (!queryAgentInfoList.containsKey(name)) {
			System.out.println("ERROR: " + name + " does not exist");
			return;
		}
		queryAgentInfoList.remove(name);
	}
	
	public static QueryAgent findQueryAgent(String name) {
		QueryAgent qi = null;
		AgentInfo ainfo = (AgentInfo)queryAgentInfoList.get(name);
		if (ainfo == null) {
			System.out.println("AgentInfo not found");
			return null;
		}
		qi = (QueryAgent)ainfo.getInstance();
		if (qi == null || !ainfo.isActive()) {
			qi = (QueryAgent)AgentLoader.activateAgent(ainfo);
		}
		return qi;
	}
	
	public static Iterator getQueryAgentNames() {
		return queryAgentInfoList.keySet().iterator();
	}
	
	public static String getQueryAgentClassName(String name) {
		AgentInfo ainfo = (AgentInfo)queryAgentInfoList.get(name);
		if (ainfo == null) return "";
		return ainfo.getClassName();
	}	
}
