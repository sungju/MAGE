/*
 * CPUInfo
 * - Get a cpu information from node and result back to requestor as a form of Message object
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;

import mage.agent.TaskAgent;
import mage.network.Message;

public class CPUInfo extends TaskAgent {
	public void setArguments(HashMap data) {
		
	}
	
	public Message invokeService(String command, int code, String message,
			HashMap arguments) {
		
		return getGeneralInfo();
	}
	
	private Message getGeneralInfo() {
		Message msg = new Message();
		StringBuffer sb = new StringBuffer();
		int user = 0, sys = 0, idle = 0;
		
		try {
			Process p = Runtime.getRuntime().exec("iostat");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

			in.readLine();
			in.readLine();
			String resultStr = in.readLine();
			StringTokenizer st = new StringTokenizer(resultStr, " ", false);
			while (st.countTokens() > 3)
				st.nextToken();
			
			user = Integer.parseInt(st.nextToken());
			sys = Integer.parseInt(st.nextToken());
			idle = Integer.parseInt(st.nextToken());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sb.append("User:" + user);
		sb.append("\nSys:" + sys);
		sb.append("\nIdle:" + idle);
		
		msg.setCode(200);
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
