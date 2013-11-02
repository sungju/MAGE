/*
 * ServerBroker
 * - Act a s main server for collect service information and deliver this information to the requestor
 * - It sends a heart-beat signal to the every ServerProxy which alreay known by ServerBroker
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
import java.util.StringTokenizer;
import java.util.Vector;

import mage.agent.TaskAgent;
import mage.network.Message;
import mage.network.NetworkAgentManager;
import mage.network.PeerNetworkAgent;
import mage.network.ServerNetworkAgent;
import mage.ubi.ServiceManager;
import mage.utils.Base64;

public class ServerBroker extends TaskAgent {
	HashMap nodeList = new HashMap();
	HashMap serverProxyList = new HashMap();
	
	public ServerBroker() {
		new ServerBrokerThread().start();
	}
	
	public void setArguments(HashMap data) {
		String proxyListStr = (String)data.get("ServerProxyList");
		StringTokenizer st = new StringTokenizer(proxyListStr, "\n", false);
		String nodeName;
		String nodeAddress;
		
		while (st.countTokens() > 1) {
			nodeName = st.nextToken();
			nodeAddress = st.nextToken();
			serverProxyList.put(nodeName, nodeAddress);
		}
	}	
	
	public Message invokeService(String command, int code, String message,
			HashMap arguments) {
		
		Message resultMsg = null;
		
		if (command.equalsIgnoreCase("NodeListRequest"))
			resultMsg = getNodeListMessage(arguments);
		else if (command.equalsIgnoreCase("NodeInfoRequest"))
			resultMsg = getNodeInfoMessage(arguments);
		else if (command.equalsIgnoreCase("ServiceDeliverRequest"))
			resultMsg = executeServiceRequestToNode(message, arguments);
		else if (command.equalsIgnoreCase("RegisterNode"))
			resultMsg = registerNode(message, arguments);
		
		return resultMsg;
	}
	
	private Message getNodeListMessage(HashMap arguments) {
		Message resultMsg = new Message();
		StringBuffer sb = new StringBuffer();
		int size = nodeList.size();
		Iterator nodeKey = nodeList.keySet().iterator();
		NodeInfo ni;
		int i;
		
		sb.append("NodeCount:");
		sb.append(size);
		sb.append("\n");
		
		i = 0;
		while (nodeKey.hasNext()) {
			ni = (NodeInfo)nodeList.get(nodeKey.next());
			if (ni == null) continue;

			sb.append("Node"); sb.append(i); sb.append(":");
			sb.append(Base64.encode((ni.getNodeName() + "\n" + ni.getNodeAddress()).getBytes()));
			sb.append("\n");			
			i++;
		}
		
		resultMsg.setCommand("NodeListResponse");
		resultMsg.setCode(200);
		resultMsg.setQueryType("HeartBeat");
		resultMsg.setMessage(sb.toString());
		
		
		return resultMsg;
	}
	
	private Message getNodeInfoMessage(HashMap arguments) {
		Message resultMsg = new Message();
		StringBuffer sb = new StringBuffer();
		NodeInfo ni;
		String nodeName = (String)arguments.get("NODENAME");
		
		resultMsg.setCommand("NodeInfoResponse");
		resultMsg.setQueryType("HeartBeat");
		
		if (nodeName == null) {
			resultMsg.setCode(500);
			resultMsg.setMessage("Status: Node not found");
			
			return resultMsg;
		}
		
		ni = (NodeInfo)nodeList.get(nodeName);
		sb.append("NodeName:"); sb.append(ni.getNodeName()); sb.append("\n");
		sb.append("NodeAddress:"); sb.append(ni.getNodeAddress()); sb.append("\n");
		
		HashMap protocolList = ni.getProtocolList();
		HashMap queryList = ni.getQueryList();
		HashMap providerList = ni.getProviderList();
		
		Iterator iter = protocolList.keySet().iterator();
		String protocolName;
		HashMap argData;
		StringBuffer sbProtocol = new StringBuffer();
		while (iter.hasNext()) {
			protocolName = (String)iter.next();
			argData = (HashMap)protocolList.get(protocolName);
			sbProtocol.append(protocolName); sbProtocol.append(":");
			sbProtocol.append(Base64.encode(getProtocolArguments(argData).getBytes()));
			sbProtocol.append("\n");
		}
		sb.append("ProtocolList:"); sb.append(Base64.encode(sbProtocol.toString().getBytes()));
		sb.append("\n");
		
		iter = queryList.keySet().iterator();
		String queryName;
		StringBuffer sbQuery = new StringBuffer();
		while (iter.hasNext()) {
			queryName = (String)iter.next();
			sbQuery.append(queryName); sbQuery.append("\n");
		}
		sb.append("QueryList:"); sb.append(Base64.encode(sbQuery.toString().getBytes()));
		sb.append("\n");
		
		iter = providerList.keySet().iterator();
		String providerName, providerData;
		StringBuffer sbProvider = new StringBuffer();
		while (iter.hasNext()) {
			providerName = (String)iter.next();
			providerData = (String)providerList.get(providerName);
			sbProvider.append(providerName); sbProvider.append(":");
			sbProvider.append(providerData);
			sbProvider.append("\n");
		}
		sb.append("ProviderList:"); sb.append(Base64.encode(sbProvider.toString().getBytes()));
		sb.append("\n");
		
		resultMsg.setCode(200);
		resultMsg.setMessage(sb.toString());
		
		return resultMsg;
	}
	
	private String getProtocolArguments(HashMap argList) {
		StringBuffer sb = new StringBuffer();
		Iterator iter = argList.keySet().iterator();
		String argName, argData;
		
		while (iter.hasNext()) {
			argName = (String)iter.next();
			argData = (String)argList.get(argName);
			
			sb.append(argName); sb.append(":"); sb.append(argData); sb.append("\n");
		}
		
		return sb.toString();
	}
	
	private Message executeServiceRequestToNode(String message, HashMap arguments) {
		Message resultMsg = new Message();
		
		return resultMsg;
	}
	
	private Message registerNode(String message, HashMap arguments) {
		Message resultMsg = new Message();
		String activeProtocolListEnc = (String)arguments.get("PROTOCOLLIST");
		String queryListEnc = (String)arguments.get("QUERYLIST");
		String providerListEnc = (String)arguments.get("PROVIDERLIST");
		NodeInfo nodeInfo = new NodeInfo();
		
		String activeProtocolList;
		String queryList;
		String providerList;
		
		activeProtocolList = new String(Base64.decode(activeProtocolListEnc));
		queryList = new String(Base64.decode(queryListEnc));
		providerList = new String(Base64.decode(providerListEnc));
		
		nodeInfo.setNodeName((String)arguments.get("NODENAME"));
		nodeInfo.setNodeAddress((String)arguments.get("NODEADDRESS"));
		
		nodeInfo.addProtocolList(activeProtocolList);
		nodeInfo.addQueryList(queryList);
		nodeInfo.addProviderList(providerList);
		
		nodeList.put(nodeInfo.getNodeName(), nodeInfo);
		
		resultMsg.setCode(200);
		resultMsg.setCommand("REGIST OK");
		resultMsg.setQueryType("HeartBeat");
		resultMsg.setMessage(nodeInfo.getNodeName() + ":" + nodeInfo.getNodeAddress());
		
		return resultMsg;
	}
	
	private class NodeInfo {
		private HashMap protocolList = new HashMap();
		private HashMap queryList = new HashMap();
		private HashMap providerList = new HashMap();
		private String nodeName;
		private String nodeAddress;
		
		public void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}
		
		public String getNodeName() {
			return nodeName;
		}
		
		public void setNodeAddress(String nodeAddress) {
			this.nodeAddress = nodeAddress;
		}
		
		public String getNodeAddress() {
			return nodeAddress;
		}
		
		public void addProtocol(String managerName, String data) {
			StringTokenizer st = new StringTokenizer(data, "\n", false);
			HashMap dataMap = new HashMap();
			String argName, argData;
			while (st.hasMoreTokens()) {
				StringTokenizer stData = new StringTokenizer(st.nextToken(), ":", false);
				if (stData.countTokens() < 2) continue;
				argName = stData.nextToken();
				argData = stData.nextToken();
				dataMap.put(argName, argData);
			}
			protocolList.put(managerName, dataMap);
		}
		
		public void addProtocolList(String message) {
			StringTokenizer st = new StringTokenizer(message, "\n", false);
			String managerName;
			String data;
			while (st.hasMoreTokens()) {
				StringTokenizer stParse = new StringTokenizer(st.nextToken(), ":", false);
				if (stParse.countTokens() < 2) continue;
				managerName = stParse.nextToken();
				data = new String(Base64.decode(stParse.nextToken()));
				addProtocol(managerName, data);
			}
		}
		
		public void addQuery(String query) {
			queryList.put(query, query); // Simple but will be upgrade to include other information
		}
		
		public void addQueryList(String message) {
			StringTokenizer st = new StringTokenizer(message, "\n", false);
			
			while (st.hasMoreTokens()) {
				addQuery(st.nextToken());
			}
		}
		
		public void addProvider(String provider) {
			StringTokenizer st = new StringTokenizer(provider, ":", false);
			String providerName;
			String providerData;
			
			if (st.countTokens() < 2) return;
			
			providerName = st.nextToken();
			providerData = st.nextToken();
			
			if (providerName.equals("ServerProxy"))
				serverProxyList.put(nodeName, nodeAddress);
			
			providerList.put(providerName, providerData);
		}
		
		public void addProviderList(String providerList) {
			StringTokenizer st = new StringTokenizer(providerList, "\n", false);
			
			while (st.hasMoreTokens()) {
				addProvider(st.nextToken());
			}
		}

		public HashMap getProtocolList() {
			return protocolList;
		}

		public void setProtocolList(HashMap protocolList) {
			this.protocolList = protocolList;
		}

		public HashMap getProviderList() {
			return providerList;
		}

		public void setProviderList(HashMap providerList) {
			this.providerList = providerList;
		}

		public HashMap getQueryList() {
			return queryList;
		}

		public void setQueryList(HashMap queryList) {
			this.queryList = queryList;
		}
	}
	
	private class ServerBrokerThread extends Thread {
		public boolean isCont;
		PeerNetworkAgent peer;
		
		public ServerBrokerThread() {
			peer = ((ServerNetworkAgent)(NetworkAgentManager.findNetworkAgent("HTTP"))).getPeerInstance();			
		}
		
		public void run() {
			Message msg = new Message();
			String nodeName;
			String nodeAddress;
			
			isCont = true;
			msg.setQueryType("HeartBeat");
			msg.setCommand("SEND PING");
			msg.setCode(100);
			msg.setMessage("Service: ServerProxy\nQuery_Type: HeartBeat\n" +
				     "Protocol: HTTP\nAddress: " + (String)ServiceManager.getData("NodeAddress") +
				     "\nPort: 8080\n" +
				     "ServerService: ServerBroker");
			
			nodeList.clear();
			while (isCont) {
				Iterator nodeIterator = serverProxyList.keySet().iterator();
				Vector delNodeName = new Vector();
				while (nodeIterator.hasNext()) {
					nodeName = (String)nodeIterator.next();
					nodeAddress = (String)serverProxyList.get(nodeName);
					
					if (nodeAddress == null) {
						delNodeName.add(nodeName);
						continue;
					}
					sendMessage(nodeName, nodeAddress, msg);
				}
				Iterator delIterator = delNodeName.iterator();
				while (delIterator.hasNext()) {
					serverProxyList.remove((String)delIterator.next());
				}
				
				try {
					sleep(5000);
				} catch (Exception ex) {
					isCont = false;
				}
			}
		}
		
		private void sendMessage(String nodeName, String nodeAddress, Message msg) {
			HashMap data = new HashMap();
			data.put("ADDRESS", nodeAddress);
			data.put("PORT", "8080");
			peer.setArguments(data);
			
			if (!peer.connect()) {
				System.out.println("Can't connect to " + nodeAddress);
				serverProxyList.remove(nodeName);
				return;
			}
			
			System.out.println("SEND : " + nodeName);
			
			peer.sendMessage(msg);
			peer.receiveMessage();
			peer.close();			
		}
	}

	public HashMap stopService() {
		return null;
	}

	public void resumeService(HashMap data) {
	}
}
