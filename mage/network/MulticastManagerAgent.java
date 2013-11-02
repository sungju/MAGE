/*
 * MulticastManager
 * - Multicase need another kinds of NetworkManager
 * - This manager only need for multicase communication
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
package mage.network;

import java.util.HashMap;

import mage.lang.Interpreter;

public class MulticastManagerAgent extends NetworkCommunicationAgent implements Runnable{
	private String managerName = null;
	private String serverProtocol = null;
	private HashMap args = null;
	private PeerNetworkAgent multicastPeer = null;

	public boolean isCont = true;
	
	public MulticastManagerAgent() {	
	}
	
	public MulticastManagerAgent(String managerName, String serverProtocol, HashMap data) {
		setManagerName(managerName);
		setServerProtocol(serverProtocol);
		setArguments(args);
	}
	
	public void run() {
		Message requestMsg = null;
		
		multicastPeer = ((ServerNetworkAgent)(NetworkAgentManager.findNetworkAgent(serverProtocol))).getPeerInstance();			
		if (multicastPeer == null) return;
		
		multicastPeer.setArguments(args);
		multicastPeer.connect();

		while (multicastPeer.isConnected()) {
			requestMsg = multicastPeer.receiveMessage();
			if (!multicastPeer.isConnected()) break;
			requestMsg.setPeerNetwork(multicastPeer);
			processMessage(requestMsg);
		}
	}
	
	public Message processMessage(Message reqMsg) {
		Message msg;
		
		msg = Interpreter.processMessage(reqMsg);
		
		return msg;
	}
	
	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	public void setServerProtocol(String serverProtocol) {
		this.serverProtocol = serverProtocol;	
	}

	public void setArguments(HashMap args) {
		this.args = args;
	}

	public void setData(String managerName, String serverProtocol, HashMap args) {
		setManagerName(managerName);
		setServerProtocol(serverProtocol);
		setArguments(args);
	}

	public void start() {
		new Thread(this).start();
	}	
}
