/*
 * ServerProxy
 * - Recieve heart-beat signal from ServerBroker and multicast this message to every node
 *   in the multicast group
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
import java.util.StringTokenizer;

import mage.agent.TaskAgent;
import mage.network.Message;
import mage.network.NetworkAgentManager;
import mage.network.PeerNetworkAgent;
import mage.network.ServerNetworkAgent;

public class ServerProxy extends TaskAgent {
	PeerNetworkAgent peer;

	public void setArguments(HashMap data) {
		peer = ((ServerNetworkAgent)(NetworkAgentManager.findNetworkAgent("Multicast"))).getPeerInstance();
		peer.setArguments(data);
		peer.connect();
	}
	
	public Message invokeService(String command, int code, String message,
			HashMap arguments) {
		Message resultMsg = null;
		
		if (command.equalsIgnoreCase("SEND PING"))
			resultMsg = doSendPing(message, arguments);
		return resultMsg;
	}
	
	private Message doSendPing(String message, HashMap arguments) {
		Message resultMsg = new Message();
		Message sendMsg = new Message();
		StringBuffer sb = new StringBuffer();
		StringTokenizer st = new StringTokenizer(message, "\n", false);
		String tmpStr;
		
		sb.append("Service: ServerAgent\n");
		while (st.hasMoreTokens()) {
			tmpStr = st.nextToken();
			if (tmpStr.startsWith("Service:")) continue;
			sb.append(tmpStr);
			sb.append("\n");
		}
		
		sendMsg.setCode(200);
		sendMsg.setCommand("SERVER PING");
		sendMsg.setQueryType("HeartBeat");
		sendMsg.setMessageKind(Message.KIND_REQUEST);
		sendMsg.setMessage(sb.toString());
		
		sendMessageUsingMulticast(sendMsg);
		
		resultMsg.setCode(200);
		resultMsg.setCommand("OK");
		resultMsg.setMessage("PING OK");
		
		return resultMsg;
	}
	
	private void sendMessageUsingMulticast(Message sendMsg) {
		peer.sendMessage(sendMsg);
	}

	public HashMap stopService() {
		return null;
	}

	public void resumeService(HashMap data) {
	}
}
