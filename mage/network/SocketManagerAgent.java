/*
 * NetworkManager
 *  - Invoke and control the network protocols
 *  - Basically, we use only one channel but it will be extend to more channels
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

import mage.lang.Interpreter;

import java.util.*;

public class SocketManagerAgent extends NetworkCommunicationAgent implements Runnable{
	private String managerName = null;
	private ServerNetworkAgent server = null;
	private String serverProtocol = null;
	private HashMap args = null;

	public boolean isCont = true;
	
	public SocketManagerAgent() {
	}
	
	public SocketManagerAgent(String managerName, String serverProtocol, HashMap data) {
		setManagerName(managerName);
		setServerProtocol(serverProtocol);
		setArguments(args);
	}
	
	public void run() {		
		server = NetworkAgentManager.findNetworkAgent(this.serverProtocol);
		if (server == null) {
			System.out.println("Error Loading Protocol");
			return;
		}
		
		isCont = true;
		server.setArguments(this.args);
		server.start();
		
		while (isCont) {
			PeerNetworkAgent peer = server.accept();
			new NetworkListener(peer).start();
		}
	}
	
	class NetworkListener extends Thread {
		private PeerNetworkAgent peer = null;
		public NetworkListener(PeerNetworkAgent peer) {
			this.peer = peer;
		}
		
		public void run() {
			Message requestMsg = null;
			Message responseMsg = null;
			
			if (peer == null) return;
			while (peer.isConnected()) {
				requestMsg = peer.receiveMessage();
				if (!peer.isConnected()) break;
				requestMsg.setPeerNetwork(peer);
				responseMsg = processMessage(requestMsg);
				peer.sendMessage(responseMsg);
			}
		}
		
		public Message processMessage(Message reqMsg) {
			Message msg;
			
			msg = Interpreter.processMessage(reqMsg);
			
			return msg;
		}
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
