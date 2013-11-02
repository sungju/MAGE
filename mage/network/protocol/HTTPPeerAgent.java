/*
 * HTTPPeer
 * - Implemented basic features of HTTP protocol
 * - Used by client to communicate with server
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.StringTokenizer;

import mage.network.Message;
import mage.network.PeerNetworkAgent;
import mage.utils.Base64;

public class HTTPPeerAgent extends PeerNetworkAgent {
	Socket socket = null;
	String addr = null;
	int port = 0;
	
	PrintWriter outBuffer = null;
	BufferedReader inBuffer = null;
	
	public HTTPPeerAgent() {	
	}
	
	public HTTPPeerAgent(Socket socket) {
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

	public synchronized boolean connect() {
		try {
			if (addr == null || port == 0) return false;
			setSocket(new Socket(addr, port));
			return true;
		} catch (Exception ex) {
			socket = null;
			System.out.println("Connection error in HTTPPeer");
			ex.printStackTrace();
		}
		return false;
	}

	public boolean isConnected() {
		return (socket != null && socket.isConnected());
	}

	public synchronized void sendMessage(Message msg) {
		try {
			if (msg.getMessageKind() == Message.KIND_RESPOND)
				outBuffer.println("HTTP/1.0 " + msg.getCode() + " " + msg.getCommand());
			else
				outBuffer.println("GET " + msg.getCommand() + " HTTP/1.0");
			
			outBuffer.println("CODE: " + msg.getCode());
			outBuffer.println("TYPE: " + msg.getQueryType());
			outBuffer.println("COMMAND: " + msg.getCommand());
			outBuffer.println("MESSAGE: " + Base64.encode(msg.getMessage().getBytes()));
			outBuffer.println();
			if (msg.getMessageKind() == Message.KIND_RESPOND) {
				outBuffer.println(msg.getMessage());
				outBuffer.println();	
			}
			flush();
		} catch (Exception ex) {
			close();
			ex.printStackTrace();
		}
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

	public synchronized Message receiveMessage() {
		Message msg = new Message();
		try {
			String rcvStr, tmpStr;
			boolean isResult = false;
			while ((rcvStr = inBuffer.readLine()) != null) {
				System.out.println("RCV: " + rcvStr);
				if (rcvStr.equals("")) break;
				
				if (rcvStr.startsWith("HTTP/")) {
					StringTokenizer st = new StringTokenizer(rcvStr);
					if (st.countTokens() > 1)
						st.nextToken();
					tmpStr = st.nextToken();
					msg.setCode(parseInt(tmpStr));
					msg.setMessage(
							rcvStr.substring(
									rcvStr.indexOf(tmpStr) + tmpStr.length() + 1
									).trim()
							);	
					isResult = true;
				} else if (rcvStr.startsWith("CODE:")) {
					rcvStr = rcvStr.substring(rcvStr.indexOf(":") + 1).trim();
					msg.setCode(parseInt(rcvStr));
				} else if (rcvStr.startsWith("COMMAND:")){
					rcvStr = rcvStr.substring(rcvStr.indexOf(":") + 1).trim();
					msg.setCommand(rcvStr);
				} else if (rcvStr.startsWith("TYPE:")) {
					rcvStr = rcvStr.substring(rcvStr.indexOf(":") + 1).trim();
					msg.setQueryType(rcvStr);
				} else if (rcvStr.startsWith("GET ")) {
					StringTokenizer st = new StringTokenizer(rcvStr);
					if (st.countTokens() > 1)
						st.nextToken();
					rcvStr = st.nextToken();
					msg.setCommand(rcvStr);
				} else if (rcvStr.startsWith("MESSAGE: ")) {
					msg.setMessage(new String(Base64.decode(
										rcvStr.substring(rcvStr.indexOf(":") + 1).trim()
										)));
				}
			}
			if (isResult == true) {
				StringBuffer msgStr = new StringBuffer();
				while ((rcvStr = inBuffer.readLine()) != null) {
					System.out.println("RCV : " + rcvStr);
					if (rcvStr.equals("")) break;
					msgStr.append(rcvStr);
					msgStr.append("\n");
				}
				msg.setMessage(msgStr.toString());
			}
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
