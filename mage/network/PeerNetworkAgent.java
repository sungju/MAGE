/*
 * PeerNetwork
 *  - Interface for network communication as a Client (peer)
 *  - Provide a connection and communication interface
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

import mage.agent.Agent;

public abstract class PeerNetworkAgent extends Agent {
	abstract public boolean connect();
	abstract public boolean isConnected();
	abstract public void sendMessage(Message msg);
	abstract public Message receiveMessage();
	abstract public void flush();
	abstract public void close();
	abstract public Message sendAndReceive(Message msg);
}
