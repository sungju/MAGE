/*
 * Message
 * - contains message for internal usage
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
 */package mage.network;

import mage.agent.TaskAgent;
import mage.lang.QueryAgent;

public class Message {
	private String user_id = null;
	private String password = null;
	private String token = null;
	
	private String queryType = null;
	private String command = null;
	private int code = -1;
	private String message = null;
	
	private PeerNetworkAgent peerNetwork = null;
	private QueryAgent queryAgent = null;
	private TaskAgent taskAgent = null;
	
	public static final int KIND_REQUEST = 0;
	public static final int KIND_RESPOND = 1;
	
	private int messageKind = Message.KIND_REQUEST;
	
	public int getMessageKind() {
		return messageKind;
	}
	public void setMessageKind(int messageKind) {
		this.messageKind = messageKind;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getQueryType() {
		return queryType;
	}
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
	public PeerNetworkAgent getPeerNetwork() {
		return peerNetwork;
	}
	public void setPeerNetwork(PeerNetworkAgent peerNetwork) {
		this.peerNetwork = peerNetwork;
	}
	public QueryAgent getQueryAgent() {
		return queryAgent;
	}
	public void setQueryAgent(QueryAgent queryAgent) {
		this.queryAgent = queryAgent;
	}
	public TaskAgent getTaskAgent() {
		return taskAgent;
	}
	public void setTaskAgent(TaskAgent taskAgent) {
		this.taskAgent = taskAgent;
	}
}
