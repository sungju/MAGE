/*
 * MyQuery
 * - query component that use SQL style syntax
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
package mage.lang;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import mage.agent.TaskAgent;
import mage.network.Message;
import mage.ubi.ServiceManager;

public class MyQueryAgent extends QueryAgent {

	private final static int SELECT = 0;
	private final static int FROM = 1;
	private final static int WHERE = 2;
	private final static int DOMAIN = 3;
	private final static int USING = 4;
	
	public HashMap parse(String message) {
		return null;
	}

	public HashMap parse(Message msg) {
		if (msg.getCommand().equals("MONITORING")) {
			return parseMonitoring(msg);
		} else if (msg.getCommand().equals("SERVICE_INFO")) {
			return parseServiceInfo(msg);
		}
		return null;
	}
	
	private HashMap parseMonitoring(Message msg) {
		HashMap result = new HashMap();
		String tokenStr = null;
		int parseState = 0;
		Vector resultFields = new Vector();
		HashMap constraints = new HashMap();
		String domainStr = null;
		String serviceStr = null;
		String usingStr = null;
		
		String whereStr = "";
		Vector whereCond = null;
		int whereCnt = 0;
		
		StringTokenizer st = new StringTokenizer(msg.getMessage(), " ,;", false);
		
		if (st.countTokens() == 0) {
			return result;
		}
		
		tokenStr = st.nextToken();
		if (!tokenStr.equalsIgnoreCase("SELECT")) {
			return result; // Error
		}
		
		parseState = MyQueryAgent.SELECT;
		while (st.hasMoreElements()) {
			tokenStr = st.nextToken();
			if (tokenStr.equalsIgnoreCase("FROM"))
				parseState = MyQueryAgent.FROM;
			else if (tokenStr.equalsIgnoreCase("WHERE"))
				parseState = MyQueryAgent.WHERE;
			else if (tokenStr.equalsIgnoreCase("DOMAIN"))
				parseState = MyQueryAgent.DOMAIN;
			else if (tokenStr.equalsIgnoreCase("USING"))
				parseState = MyQueryAgent.USING;
			
			switch (parseState) {
			case MyQueryAgent.SELECT: resultFields.add(tokenStr); break;
			case MyQueryAgent.FROM: serviceStr = tokenStr; break;
			case MyQueryAgent.DOMAIN: domainStr = tokenStr; break;
			case MyQueryAgent.USING:
				usingStr = msg.getMessage();
				usingStr = usingStr.substring(usingStr.indexOf(tokenStr) + 5);
				break;
			case MyQueryAgent.WHERE:
					// TODO: Need to reconfigure to reveal the conditional service
					if (whereCond == null)
						whereCond = new Vector();
					
					if (tokenStr.equalsIgnoreCase("OR")) {
						whereCond.add(whereStr);
					} else if (tokenStr.equalsIgnoreCase("AND")) {
						constraints.put(new Integer(whereCnt++), whereCond);
						whereCond = null;
					} else {
						whereStr = whereStr + tokenStr;
					}
					break;
			}
			if (parseState == MyQueryAgent.USING) break;
		}
		result.put("RESULT", resultFields);
		result.put("DOMAIN", domainStr);
		result.put("SERVICE", serviceStr);
		result.put("CONSTRAINTS", constraints);
		result.put("USING", usingStr);
		
		return result;
	}
	
	private HashMap parseServiceInfo(Message msg) {
		HashMap result = new HashMap();
		String message = msg.getMessage();
		StringTokenizer st = new StringTokenizer(message, "\n", false);
		String cmdStr;
		
		if (st.countTokens() == 0) return result;
		cmdStr = st.nextToken();
		if (cmdStr.equalsIgnoreCase("ADD CHILD_NODE")) {
			if (st.countTokens() < 2) return result;
			result.put("SUB_COMMAND", cmdStr);
			result.put("NAME", st.nextToken());
			result.put("ADDRESS", st.nextToken());
		}
		
		return result;
	}

	
	public Message doService(String command, int code, String domain, String service, 
			Message msg, HashMap arguments, TaskAgent pi) {
		Message resultMsg = null;
		
		if (command.equals("MONITORING")) {
			String message = (String)msg.getMessage();
			
			resultMsg = pi.invokeService(command, code, message, arguments);
		} else if ((command.equals("EXECUTE"))) {
			
		} else if ((command.equals("SERVICE_INFO"))) {
			String subCmdStr = (String)arguments.get("SUB_COMMAND");
			if (subCmdStr.equalsIgnoreCase("ADD CHILD_NODE")) {
				resultMsg = addChildNodeToServiceManager(arguments);
			} else if (subCmdStr.equalsIgnoreCase("REMOVE CHILD_NODE")) {
				resultMsg = removeChildNodeFromServiceManager(arguments);
			}
		}
		
		return resultMsg;
	}

	private static Message addChildNodeToServiceManager(HashMap arguments) {
		Message result = new Message();
		String name = (String)arguments.get("NAME");
		String address = (String)arguments.get("ADDRESS");
		
		if (name == null || address == null) {
			result.setCode(501);
			result.setCommand("RESULT FAILED");
			result.setMessage("Name or Address is missing");
		} else if (ServiceManager.addChildNode(name, address)) {
			result.setCode(200);
			result.setCommand("RESULT OK");
			result.setMessage("Node added successfully");
		} else {
			result.setCode(502);
			result.setCommand("RESULT FAILED");
			result.setMessage("Node already exist");
		}
		return result;
	}
	
	private static Message removeChildNodeFromServiceManager(HashMap arguments) {
		Message result = new Message();
		String name = (String)arguments.get("NAME");
		
		if (name == null) {
			result.setCode(501);
			result.setCommand("RESULT FAILED");
			result.setMessage("Name is missing");
		} else if (ServiceManager.removeChildNode(name)) {
			result.setCode(200);
			result.setCommand("RESULT OK");
			result.setMessage("Node removed successfully");
		} else {
			result.setCode(502);
			result.setCommand("RESULT FAILED");
			result.setMessage("Node does not exist");
		}
		return result;
	}
}
