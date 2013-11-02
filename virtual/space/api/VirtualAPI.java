/*
 * VirtualAPI
 * - provide interfaces for easy access to the Grid Virtual Space.
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
package virtual.space.api;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class VirtualAPI {

	public boolean initialize() {
		
		return true;
	}
	
	public String login(String userID, String password) {
		return "Grid Virtual Space v1.0\nJan 10, 2006\nWelcome " + userID;
	}
	
	public String getPWD() {
		return "/home/admin";
	}
	
	/*
	public List<String> getShortFileList(String dir) {
		List<String> list = new LinkedList<String>();
		
		list.add("FileAgent");
		list.add("ExecAgent");
		list.add("NetAgent");
		list.add("PluginAgent");
		list.add("CalcNumList");
		
		return list;
	}
	*/
	
	public String processCommand(String command) {
		if (command == null) return null;
		StringTokenizer st = new StringTokenizer(command, " ", false);
		if (st.countTokens() == 0) return null;
		String cmdStr = st.nextToken();
		if (cmdStr.equals("ls")) {
		//	return getShortFileList(getPWD()).toString();
		} else if (cmdStr.equals("help")) {
			return "Help <command>\n\nls\tmv\tcp\tmkdir\trmdir\tcd\n";
		} else if (cmdStr.equals("quit")) {
			System.exit(0);
		} else if (cmdStr.equals("cd")) {
			if (st.countTokens() == 1)
				System.out.println("Change Directory to " + st.nextToken());
			else
				System.out.println("Move to home directory");
		} else if (cmdStr.equals("mkdir")) {
			if (st.countTokens() == 1) {
				System.out.println("Make Directory " + st.nextToken());
			} else {
				System.out.println("Parameter missed.");
			}
		} else if (cmdStr.equals("rmdir")) {
			if (st.countTokens() == 1) {
				System.out.println("Remove Directory " + st.nextToken());
			} else {
				System.out.println("Parameter missed.");
			}
		} else if (cmdStr.equals("mv")) {
			if (st.countTokens() == 2) {
				System.out.println("Move " + st.nextToken() + " to " + st.nextToken());
			} else {
				System.out.println("Parameter missed.");
			}
		} else if (cmdStr.equals("rm")) {
			if (st.countTokens() > 0) {
				System.out.println("Delete File(s) : ");
				while (st.hasMoreTokens()) {
					System.out.print(st.nextToken() + "\t");
				}
				System.out.println();
			} else {
				System.out.println("Parameter missed.");
			}
		} else if (cmdStr.equals("cp")) {
			if (st.countTokens() == 2) {
				System.out.println("Copy " + st.nextToken() + " to " + st.nextToken());
			} else {
				System.out.println("Parameter missed.");
			}
		} else {
			// Execute commands
			System.out.println("Execute command" + cmdStr);
		}
		
		return "OK";
	}
	
	public static void main(String args[]) {
		VirtualAPI vapi = new VirtualAPI();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		vapi.initialize();
		System.out.println(vapi.login("admin", "admin"));
		System.out.println("$> ls");
		System.out.println("Working directory : " + vapi.getPWD());
		/*
		List<String> list = vapi.getShortFileList(vapi.getPWD());
		for (String i: list) {
			System.out.println(i);
		}
		*/
		try {
			String line;
			
			System.out.print("$> ");
			while ((line = in.readLine()) != null) {
				System.out.println(vapi.processCommand(line.trim()));
				System.out.print("$> ");				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}