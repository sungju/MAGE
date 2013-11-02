/*
 * RequestBroker
 * - Receive and Invoke user request to appropriate actions
 * - It parse the SQL-style commands and retrieve related information to the user
 * - It also can invoke some applications based on user request.
 * - Invocation include remote installation and uninstallation.
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
package mage.ubi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import mage.agent.AgentLoader;
import java.io.*;

public class WorkManager {
	private String configPath = "." + File.separator + "config.xml";
	
	public WorkManager() {		
	}
	
	public void start() {
		setConfig();
		AgentLoader.loadAgentList(ConfigManager.getEnvData("AGENT_DIR"));
	}
	
	public void setConfig() {
		InetAddress localAddr = null;
		String nodeName = "localhost";
		String nodeAddress = "127.0.0.1";
				
		try {
			localAddr = InetAddress.getLocalHost();
			nodeName = localAddr.getHostName();
			nodeAddress = localAddr.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		ServiceManager.putData("NodeName", nodeName);
		ServiceManager.putData("NodeAddress", nodeAddress);
		
		ConfigManager.loadConfig(configPath);		
	}

	public static void main(String[] args) {
		WorkManager rb = new WorkManager();
		
		if (args.length > 1)
			rb.setConfigPath(args[1]);
		
		rb.start();
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}
}
