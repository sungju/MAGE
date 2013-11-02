/*
 * MemInfo
 * - return Message that is contain memory information of node
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
import java.util.Iterator;
import java.util.Vector;

import mage.agent.TaskAgent;
import mage.network.Message;

public class MemInfo extends TaskAgent {
	public void setArguments(HashMap data) {
		
	}
	
	public Message invokeService(String command, int code, String message,
			HashMap arguments) {
		HashMap constraints = (HashMap)arguments.get("CONSTRAINTS");		
		Iterator whereList = constraints.values().iterator();
		Vector data;
		
		// TODO: Need to reconstruct to reveal conditional process
		while (whereList.hasNext()) {
			data = (Vector)whereList.next();
			System.out.println("Where : " + data);
		}
		
		return oneshotInfo();
	}
	
	private Message oneshotInfo() {
		Message msg = new Message();
		StringBuffer sb = new StringBuffer();
		long memTotal = 0, memUsed = 0, memFree = 0, memBuffers = 0;
		
		memTotal = Runtime.getRuntime().totalMemory();
		memFree = Runtime.getRuntime().freeMemory();
		memUsed = memTotal - memFree;
		
		sb.append("MemTotal:" + memTotal + "\n");
		sb.append("MemUsed:" + memUsed + "\n");
		sb.append("MemFree:" + memFree + "\n");
		sb.append("MemBuffers:" + memBuffers + "\n");
		
		msg.setCode(100);
		msg.setCommand("MON_RESULT");
		msg.setMessage(sb.toString());
		
		return msg;
	}

	public HashMap stopService() {
		return null;
	}

	public void resumeService(HashMap data) {
	}

}
