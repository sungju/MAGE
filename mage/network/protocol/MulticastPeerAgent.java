/*
 * MulticastPeer
 * - Communication component for Multicast
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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.StringTokenizer;

import mage.network.Message;
import mage.network.PeerNetworkAgent;

public class MulticastPeerAgent extends PeerNetworkAgent {
	private String groupAddr = null;
	private int groupPort = 0;
	private MulticastSocket socket = null;
	private InetAddress group = null;
	private boolean connectFlag = false;
	private int maxBufSize = 512;
	
	private int parseInt(String data) {
		int result = 0;
		try {
			result = Integer.parseInt(data);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
	public void setArguments(HashMap data) {
		super.setArguments(data);
		
		groupAddr = (String)data.get("GROUP");
		groupPort = parseInt((String)data.get("PORT"));
		maxBufSize = parseInt((String)data.get("MAX_BUF"));
	}

	public boolean connect() {
		connectFlag = false;
		
		try {
			socket = new MulticastSocket(groupPort);
			group = InetAddress.getByName(groupAddr);
			socket.joinGroup(group);
			connectFlag = true;
		} catch (Exception ex) {
			System.out.println("connect error in MulticastPeer : " + groupAddr + "," + groupPort);
			ex.printStackTrace();
		}
		return connectFlag;
	}

	public boolean isConnected() {
		if (socket == null) connectFlag = false;
		
		return connectFlag;
	}

	public void sendMessage(Message msg) {
		StringBuffer sb = new StringBuffer();
        	DatagramPacket packet;

		sb.append(msg.getCommand()); sb.append("\n");
		sb.append(msg.getCode()); sb.append("\n");
		sb.append(msg.getQueryType()); sb.append("\n");
		sb.append(msg.getMessage());
		
		byte[] buf = sb.toString().getBytes();
        	packet = new DatagramPacket(buf, buf.length, group, groupPort);
        	try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Message receiveMessage() {
	    byte[] buf = new byte[maxBufSize];
	    DatagramPacket packet = new DatagramPacket(buf, buf.length);
	    Message resultMsg = null;
	    
	    try {
			socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}

	    String received = new String(packet.getData());
	    StringTokenizer st = new StringTokenizer(received, "\n", false);
	    if (st.countTokens() >= 4) {
	    		resultMsg = new Message();
	    		resultMsg.setCommand(st.nextToken());
	    		resultMsg.setCode(parseInt(st.nextToken()));
	    		resultMsg.setQueryType(st.nextToken());
	    		StringBuffer sb = new StringBuffer();
	    		while (st.hasMoreTokens()) {
	    			sb.append(st.nextToken());
	    			sb.append("\n");
	    		}
	    		resultMsg.setMessage(sb.toString());
	    	}
		return resultMsg;
	}

	public void flush() {
	}

	public void close() {
		try {
			socket.leaveGroup(group);
		} catch (IOException e) {
			e.printStackTrace();
		}
		socket.close();
	}

	public synchronized Message sendAndReceive(Message msg) {
		sendMessage(msg);
		return receiveMessage();
	}
}
