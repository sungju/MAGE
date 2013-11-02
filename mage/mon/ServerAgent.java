/*
 * ServerAgent
 * - Send a node information to the ServerBroker when heart-beat signal received
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
package mage.mon;

import java.util.HashMap;
import java.util.Iterator;

import mage.agent.TaskAgent;
import mage.agent.TaskAgentManager;
import mage.lang.Interpreter;
import mage.network.Message;
import mage.network.NetworkAgentManager;
import mage.network.PeerNetworkAgent;
import mage.network.ServerNetworkAgent;
import mage.ubi.ServiceManager;
import mage.utils.Base64;

public class ServerAgent extends TaskAgent {

	public Message invokeService(String command, int code, String message,
			HashMap arguments) {
		Message resultMsg = new Message();
		String queryType = null;
		String protocol = null;
		String serverServiceName = null;
		HashMap messageData = new HashMap();
		Iterator keys = arguments.keySet().iterator();
		String keyStr, dataStr;
		
		while (keys.hasNext()) {
			keyStr = (String)keys.next();
			dataStr = (String)arguments.get(keyStr);
			if (keyStr.equalsIgnoreCase("Query_Type"))
				queryType = dataStr;
			else if (keyStr.equalsIgnoreCase("Protocol"))
				protocol = dataStr;
			else if (keyStr.equalsIgnoreCase("ServerService"))
				serverServiceName = dataStr;
	/*
			else
				messageData.put(keyStr, dataStr);
*/
		}
		
		System.out.println("MSG: " + message);
		
		sendMessageToServer(queryType, protocol, serverServiceName, messageData);
		
		resultMsg.setCode(100);
		resultMsg.setCommand("OK");
		resultMsg.setMessage("");
		
		return resultMsg;
	}
	
	private void sendMessageToServer(String queryType, String protocol, 
									String serverServiceName, HashMap messageData) {
		PeerNetworkAgent peer;
		Message sendMsg = new Message();
		String messageStr = null;
		
		peer = ((ServerNetworkAgent)(NetworkAgentManager.findNetworkAgent(protocol))).getPeerInstance();
		peer.setArguments(messageData);
		System.out.println("Host: " + (String)messageData.get("ADDRESS"));
		System.out.println("Port: " + (String)messageData.get("PORT"));
		peer.connect();
		
		messageStr = "Service: " + serverServiceName + "\n" + buildMessageAboutMe();
		sendMsg.setCode(100);
		sendMsg.setCommand("RegisterNode");
		sendMsg.setQueryType(queryType);
		sendMsg.setMessage(messageStr);
		
		System.out.println(messageStr);
		
		peer.sendMessage(sendMsg);
		peer.receiveMessage(); // Just call it for follow sequence
	}

	private String buildMessageAboutMe() {
		StringBuffer resultBuffer = new StringBuffer();
		StringBuffer protocolList = new StringBuffer();
		StringBuffer queryList = new StringBuffer();
		StringBuffer providerList = new StringBuffer();
		Iterator data;
		
		HashMap dataMap;
		HashMap protocolData;
		Iterator protocolKey;
		String netManagerName = null;
		
		resultBuffer.append("NodeName:");
		resultBuffer.append((String)ServiceManager.getData("NodeName"));
		resultBuffer.append("\n");
		resultBuffer.append("NodeAddress:");
		resultBuffer.append((String)ServiceManager.getData("NodeAddress"));
		resultBuffer.append("\n");
	/*	
		dataMap = NetworkAgentManager.getActiveProtocolList();
		data = dataMap.keySet().iterator();
		while (data.hasNext()) {
			StringBuffer protocolDataStrBuff = new StringBuffer();
			String protocolDataName = null;
			
			netManagerName = (String)data.next();
			protocolList.append(netManagerName);
			protocolData = (HashMap)dataMap.get(netManagerName);
			protocolKey = protocolData.keySet().iterator();

			while (protocolKey.hasNext()) {
				protocolDataName = (String)protocolKey.next();
				protocolDataStrBuff.append(protocolDataName);
				protocolDataStrBuff.append(":");
				protocolDataStrBuff.append((String)protocolData.get(protocolDataName));
				protocolDataStrBuff.append("\n");
			}
			protocolList.append(":");
			protocolList.append(Base64.encode(protocolDataStrBuff.toString().getBytes()));
			protocolList.append("\n");
		}
		resultBuffer.append("ProtocolList:");
		resultBuffer.append(Base64.encode(protocolList.toString().getBytes()));
		resultBuffer.append("\n");
		*/
		
		/*
		data = Interpreter.getQueryNames();
		while (data.hasNext()) {
			queryList.append((String)data.next());
			queryList.append("\n");
		}
		resultBuffer.append("QueryList:");
		resultBuffer.append(Base64.encode(queryList.toString().getBytes()));
		resultBuffer.append("\n");
		
		data = TaskAgentManager.getTaskAgentNames();
		String providerName;
		while (data.hasNext()) {
			providerName = (String)data.next();
			providerList.append(providerName);
			providerList.append(":");
			providerList.append(TaskAgentManager.getTaskAgentClassName(providerName));
			providerList.append("\n");
		}
		resultBuffer.append("ProviderList:");
		resultBuffer.append(Base64.encode(providerList.toString().getBytes()));
		resultBuffer.append("\n");
		*/
		return resultBuffer.toString();		
	}
	
	public void setArguments(HashMap data) {
		
	}

	public HashMap stopService() {
		return null;
	}

	public void resumeService(HashMap data) {
	}

}
