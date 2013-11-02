/*
 * DoExec
 * - execute application depend on user request and deliver the result back as a form of Message object
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
import java.util.Iterator;
import java.util.Vector;

import mage.agent.TaskAgent;
import mage.network.Message;

public class DoExec extends TaskAgent {
	public void setArguments(HashMap data) {
		
	}
	
	public Message invokeService(String command, int code, String message,
			HashMap arguments) {
		Message resultMsg = new Message();
		String resultStr;
		Vector result = (Vector)arguments.get("RESULT");		
		Iterator values = result.iterator();
		
		while (values.hasNext()) {
			resultStr = (String)values.next();
			System.out.println(resultStr);
		}
		Vector resultData = executeProcess((String)arguments.get("USING"));
		if (resultData == null) {
			resultMsg.setCode(500);
			resultMsg.setCommand("FAIL");
			resultMsg.setMessage("Execution failed.");
		} else {
			resultMsg.setCode(200);
			resultMsg.setCommand("OK");
			resultMsg.setMessage(selectOutput(resultData, result));
		}
		
		return resultMsg;
	}

	private String selectOutput(Vector outputData, Vector selectLine) {
		StringBuffer sb = new StringBuffer();
		Iterator values = selectLine.iterator();
		int lineNo;
		String symbol;
		int idx;
		
		while (values.hasNext()) {
			symbol = ((String)values.next()).trim();
			if (symbol.equals("*")) {
				values = outputData.iterator();
				while (values.hasNext()) {
					sb.append((String)values.next());
					sb.append("\n");
				}
				break;
			} else if ((idx = symbol.indexOf("-")) != -1) {
				int outputSize = outputData.size();
				int startIdx = 0, endIdx = outputSize - 1;
				int tmpIdx;
				if (idx == 0) { // Case : -5
					tmpIdx = parseInt(symbol.substring(idx + 1));
					if (tmpIdx < 0) continue;
					endIdx = tmpIdx;
				} else if (idx == symbol.length() - 1) { // Case : 5-
					tmpIdx = parseInt(symbol.substring(0, idx));
					if (tmpIdx < 0) continue;
					startIdx = tmpIdx;
				} else { // Case : 3-9
					tmpIdx = parseInt(symbol.substring(0, idx));
					if (tmpIdx < 0) continue;
					startIdx = tmpIdx;
					tmpIdx = parseInt(symbol.substring(idx + 1));
					if (tmpIdx < 0) continue;
					endIdx = tmpIdx;
				}
				while (startIdx <= endIdx) {
					if (startIdx >= outputSize) break;
			
					sb.append((String)outputData.elementAt(startIdx));
					sb.append("\n");
					startIdx++;
				}
			} else {
				lineNo = Integer.parseInt(symbol);
				if (lineNo < 0) continue;
				if (lineNo >= outputData.size()) continue;
				sb.append((String)outputData.elementAt(lineNo));
				sb.append("\n");
			}
		}	
		
		return sb.toString();
	}
	
	private int parseInt(String valueStr) {
		int retVal = -1;
		try {
			retVal = Integer.parseInt(valueStr);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return retVal;
	}
	
	private Vector executeProcess(String command) {
		Vector resultData = new Vector();
		
		try { 
		     Process pinfo = Runtime.getRuntime().exec(command);
		     BufferedReader in = new BufferedReader(new InputStreamReader(pinfo.getInputStream()));
		     String currentLine = null;
		     while ((currentLine = in.readLine()) != null)
		    	 	resultData.add(currentLine);
		     
		 } catch (IOException e) { 
		     System.out.println(e.toString()); 
		     resultData = null;
		 } 
		 return resultData;
	}

	public HashMap stopService() {
		return null;
	}

	public void resumeService(HashMap data) {
	}
}
