/*
 * ProviderManager
 * - Control providers which act as a service.
 *  
 *  (c)Copyright 2005,2006
 *  Written by Sungju Kwon
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package mage.agent;

import java.util.HashMap;
import java.util.Iterator;


public class TaskAgentManager {
	private static HashMap taskAgentInfoList = new HashMap();

	public static void addTaskAgent(AgentInfo agent) {
		TaskAgentManager.taskAgentInfoList.put(agent.getName(), agent);
	}
		
	public static void removeTaskAgent(String name) {
		if (!taskAgentInfoList.containsKey(name)) {
			System.out.println("ERROR: " + name + " does not exist");
			return;
		}
		taskAgentInfoList.remove(name);
	}
	
	public static TaskAgent findTaskAgent(String name) {
		TaskAgent pi = null;
		AgentInfo pinfo = (AgentInfo)taskAgentInfoList.get(name);
		if (pinfo == null) {
			System.out.println("ProviderInfo not found");
			return null;
		}
		pi = (TaskAgent)pinfo.getInstance();
		if (pi == null || !pinfo.isActive()) {
			pi = (TaskAgent)AgentLoader.activateAgent(pinfo);
		}
		return pi;
	}
	
	public static Iterator getTaskAgentNames() {
		return taskAgentInfoList.keySet().iterator();
	}
	
	public static String getTaskAgentClassName(String name) {
		AgentInfo pinfo = (AgentInfo)taskAgentInfoList.get(name);
		if (pinfo == null) return "";
		return pinfo.getClassName();
	}
}
