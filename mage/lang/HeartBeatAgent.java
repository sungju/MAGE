/*
 * HeartBeat
 * - 'Key=Value' style message interpreter
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

import mage.agent.TaskAgent;
import mage.network.Message;

public class HeartBeatAgent extends QueryAgent {	
	public HashMap parse(String message) {
		return null;
	}

	public HashMap parse(Message msg) {
		StringTokenizer st = new StringTokenizer(msg.getMessage(), "\n", false);
		HashMap resultMap = new HashMap();
		String keyStr, dataStr;
		
		while (st.hasMoreTokens()) {
			String lineStr = st.nextToken();
			StringTokenizer stLine = new StringTokenizer(lineStr, ":", false);
			if (stLine.countTokens() <= 1)
				continue;
			keyStr = stLine.nextToken().trim().toUpperCase();
			dataStr = stLine.nextToken().trim();
			resultMap.put(keyStr, dataStr);
		}
		return resultMap;
	}

	public Message doService(String command, int code, String domain, String service, 
			Message msg, HashMap arguments, TaskAgent pi) {
		Message resultMsg = null;
		String message = (String)msg.getMessage();
			
		resultMsg = pi.invokeService(command, code, message, arguments);
		
		return resultMsg;
	}
}
