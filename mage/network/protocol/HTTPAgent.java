/*
 * HTTP
 * - Basic implementation of HTTP protocol
 * - Act as a server to accept connection request of client
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
package mage.network.protocol;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import mage.network.PeerNetworkAgent;
import mage.network.ServerNetworkAgent;

public class HTTPAgent extends ServerNetworkAgent {
	ServerSocket serverSocket = null;
	
	int bindPort = 0;
	
	public void setArguments(HashMap data) {
		super.setArguments(data);
		
		if (data == null || data.get("PORT") == null)
			return;
		bindPort = parseInt((String)data.get("PORT"));
	}
	
	private int parseInt(String msg) {
		int retVal = 0;
		try {
			retVal = Integer.parseInt(msg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return retVal;
	}
	
	public void start() {
		try {
			serverSocket = new ServerSocket(bindPort);
		} catch (Exception ex) {
			serverSocket = null;
			System.out.println("Invalid PORT in HTTP: " + bindPort);
			ex.printStackTrace();
		}
	}

	public PeerNetworkAgent accept() {
		Socket socket = null;
		HTTPPeerAgent peer = null;

		if (serverSocket == null || serverSocket.isClosed())
			return null;
		try {
			socket = serverSocket.accept();
			peer = new HTTPPeerAgent(socket);
		} catch (Exception ex) {
			socket = null;
			ex.printStackTrace();
		}
		return peer;
	}
	
	public void close() {
		if (serverSocket == null) return;
		try {
			serverSocket.close();
			serverSocket = null;
		} catch (Exception ex) {
			serverSocket = null;
			ex.printStackTrace();
		}
	}

	public PeerNetworkAgent getPeerInstance() {
		return new HTTPPeerAgent();
	}
}
