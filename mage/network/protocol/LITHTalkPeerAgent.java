/*
 * LITHTalkPeer
 * - Customized communication protocol component
 * - Used to exchange monitoring data
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

import java.util.HashMap;
import java.net.*;
import java.io.*;

import mage.network.Message;
import mage.network.PeerNetworkAgent;

public class LITHTalkPeerAgent extends PeerNetworkAgent {
	Socket socket = null;
	String addr = null;
	int port = 0;
	
	PrintWriter outBuffer = null;
	BufferedReader inBuffer = null;
	
	public LITHTalkPeerAgent() {	
	}
	
	public LITHTalkPeerAgent(Socket socket) {
		setSocket(socket);
	}
	
	private void setSocket(Socket socket) {
		try {
			this.socket = socket;
			outBuffer = new PrintWriter(this.socket.getOutputStream());
			inBuffer = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		} catch (Exception ex) {
			this.socket = null;
			outBuffer = null;
			inBuffer = null;
			
			ex.printStackTrace();
		}		
	}
	
	public void setArguments(HashMap data) {
		super.setArguments(data);
		
		addr = (String)data.get("ADDRESS");
		port = parseInt((String)data.get("PORT"));
	}
	
	private int parseInt(String msg) {
		int result = 0;
		try {
			result = Integer.parseInt(msg);
		} catch (Exception e) {
			System.out.println("Error to convert data in LITHTalkPeer");
		}
		return result;
	}

	public synchronized boolean connect() {
		try {
			if (addr == null || port == 0) return false;
			setSocket(new Socket(addr, port));
			return true;
		} catch (Exception ex) {
			socket = null;
			System.out.println("Connection error in LITHTalkPeer");
			ex.printStackTrace();
		}
		return false;
	}

	public boolean isConnected() {
		return (socket != null && socket.isConnected());
	}

	public synchronized void sendMessage(Message msg) {
		try {
			outBuffer.println("HELO");
			outBuffer.println(msg.getQueryType());
			outBuffer.println(msg.getCommand());
			outBuffer.println(msg.getCode());
			if (msg.getMessage().length() > 0)
				outBuffer.println(mage.utils.Base64.encode(msg.getMessage().getBytes()));
			else
				outBuffer.println("");
			flush();
		} catch (Exception ex) {
			close();
			ex.printStackTrace();
		}
	}

	public synchronized Message receiveMessage() {
		Message msg = new Message();
		try {			
			if (!inBuffer.readLine().equals("HELO"))
				return null;
			msg.setQueryType(inBuffer.readLine());
			msg.setCommand(inBuffer.readLine());
			msg.setCode(Integer.parseInt(inBuffer.readLine()));
			String line = inBuffer.readLine();
			if (line.length() > 0)
				msg.setMessage(new String(mage.utils.Base64.decode(line)));
			else
				msg.setMessage("");
		} catch (Exception ex) {
			close();
			ex.printStackTrace();
		}
		return msg;
	}

	public synchronized void flush() {
		outBuffer.flush();
	}

	public synchronized void close() {
		if (socket == null) return;
		outBuffer.flush();
		try {
			socket.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		socket = null;
		outBuffer = null;
		inBuffer = null;
	}

	public synchronized Message sendAndReceive(Message msg) {
		sendMessage(msg);
		return receiveMessage();
	}
}
