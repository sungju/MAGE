/*
 * Network
 *  - Search the appropriate communication methods based on user query.
 *    
 * (c)Copyright 2005,2006
 * Written by Sungju Kwon
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
package mage.network;

import java.util.*;

import mage.agent.AgentInfo;
import mage.agent.AgentLoader;

public class NetworkAgentManager {
	private static HashMap networkAgentInfoList = new HashMap();
	
	public static void addNetworkAgent(AgentInfo agentInfo) {
		networkAgentInfoList.put(agentInfo.getName(), agentInfo);
	}
	
	public static void removeNetworkAgent(String name) {
		if (!networkAgentInfoList.containsKey(name)) {
			System.out.println("ERROR: " + name + " does not exist");
			return;
		}
		networkAgentInfoList.remove(name);
	}
	
	public static ServerNetworkAgent findNetworkAgent(String name) {
		ServerNetworkAgent net = null;
		AgentInfo ainfo = (AgentInfo)networkAgentInfoList.get(name);
		if (ainfo == null) {
			System.out.println("Network AgentInfo not found");
			return null;
		}
		net = (ServerNetworkAgent)ainfo.getInstance();
		if (net == null || !ainfo.isActive()) {
			net = (ServerNetworkAgent)AgentLoader.activateAgent(ainfo);
		}
		return net;
	}
	
	public static Iterator getNetworkAgentNames() {
		return networkAgentInfoList.keySet().iterator();
	}
	
	public static String getNetworkAgentClassName(String name) {
		AgentInfo ainfo = (AgentInfo)networkAgentInfoList.get(name);
		if (ainfo == null) return "";
		return ainfo.getClassName();
	}
}
