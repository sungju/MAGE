/*
 * AgentLoader
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
package mage.agent;

import java.net.*;

import mage.lang.QueryAgentManager;
import mage.network.NetworkAgentManager;
import mage.network.NetworkCommManager;

import org.w3c.dom.*;
import javax.xml.parsers.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

public class AgentLoader {
	public static void loadAgentList(String path) {
		loadAgentList(path, false);
	}
	
	public static void loadAgentList(String path, boolean loadAll) {
		if (!path.endsWith(File.separator))
			path = path + File.separator;
		
		try {
			String[] jarList = new File(path).list(new java.io.FilenameFilter() {
				public boolean accept(File dir, String filename) {
					if (filename.endsWith(".jar"))
						return true;
					else
						return false;
				}
			});
			
			if (jarList.length == 0)
				return;
			
			for (int i = 0; i < jarList.length; i++) {
				System.out.println("Reading Agent Info : " + path + jarList[i]);
				loadAgent(path + jarList[i], loadAll);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}	
	
	public static Vector<AgentInfo> loadAgent(String fileName, boolean loadNow) {
		return readJarInfo(fileName, loadNow, true);
	}
	
	public static Vector<AgentInfo> readJarInfo(String fileName, boolean isLoadNow, boolean isAddToManager) {
		Vector<AgentInfo> agentInfoList = new Vector<AgentInfo>();
		
		try {
			JarFile jarFile = new JarFile(fileName);
			ZipEntry xmlDesc = getAgentDescriptor(fileName);
			if (xmlDesc == null) return null;
			
			InputStream is = jarFile.getInputStream(xmlDesc);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(is);
			NodeList agentList = null;
			try {
				agentList = doc.getElementsByTagName("agent"); 
			} catch (Exception ex_one) {
				ex_one.printStackTrace();
			}
			if (agentList == null || agentList.getLength() == 0) return null;
			
			HashMap agentArgs;
			String agentName;
			String agentClass;
			String agentMode;
			String agentVersion;
			String agentPath = jarFile.getName();
			String agentType;
			
			NodeList argNodes;
			
			for (int j = 0; j < agentList.getLength(); j++) {
				AgentInfo agentInfo = new AgentInfo();
								
				agentArgs = new HashMap();
				Node tmpNode = agentList.item(j);
				NamedNodeMap nnm = tmpNode.getAttributes();
				agentName = nnm.getNamedItem("name").getNodeValue();
				agentClass = nnm.getNamedItem("class").getNodeValue();
				agentMode = "now";
				if (nnm.getNamedItem("mode") != null)
					agentMode = nnm.getNamedItem("mode").getNodeValue();
				agentVersion = "1.0";
				if (nnm.getNamedItem("version") != null)
					agentVersion = nnm.getNamedItem("version").getNodeValue();

				agentType = "agent";
				if (nnm.getNamedItem("type") != null)
					agentType = nnm.getNamedItem("type").getNodeValue();
				
				argNodes = tmpNode.getChildNodes();
				if (argNodes != null) {
					for (int k = 0; k < argNodes.getLength(); k++) {
						if (argNodes.item(k).getNodeName().equals("arg")) {
							NamedNodeMap nnmArg = argNodes.item(k).getAttributes();
							try {
								agentArgs.put(nnmArg.getNamedItem("name").getNodeValue(),
											  nnmArg.getNamedItem("value").getNodeValue());
							} catch (Exception ex) {
								System.out.println("ERROR - unknown arguments");
							}
						}
					}
				}
				System.out.println("Agent : " + agentName + "(" + agentClass + ")" +
								" = " + agentPath + " : version = " + agentVersion + " : mode = " + agentMode);
				
				if (isLoadNow)
					agentMode = "now";
				
				agentInfo.setData(agentName, agentClass, agentMode, agentVersion,
								agentPath, agentType, agentArgs);
				
				agentInfoList.add(agentInfo);
				
				if (isAddToManager)
					addToManager(agentInfo);
			}
		} catch (Exception ex_jar) {
			ex_jar.printStackTrace();
		}
		return agentInfoList;
	}
	
	public static void addToManager(AgentInfo agentInfo) {
		String agentType = agentInfo.getAgentType();
		String agentMode = agentInfo.getActiveMode();
		
		System.out.println("Agent TYPE : " + agentType);
		if (agentType.equals("agent")) {
			TaskAgentManager.addTaskAgent(agentInfo);
			if (agentMode.equals("now")) {
				TaskAgentManager.findTaskAgent(agentInfo.getName());
			}
		} else if (agentType.equals("network")) {
			NetworkAgentManager.addNetworkAgent(agentInfo);
			if (agentMode.equals("now")) {
				NetworkAgentManager.findNetworkAgent(agentInfo.getName());
			}
		} else if (agentType.equals("query")) {
			QueryAgentManager.addQueryAgent(agentInfo);
			if (agentMode.equals("now")) {
				QueryAgentManager.findQueryAgent(agentInfo.getName());
			}
		} else if (agentType.equals("net_manager")) {
			NetworkCommManager.addNetworkComm(agentInfo);
			if (agentMode.equals("now")) {
				NetworkCommManager.startNetworkComm(agentInfo.getName());
			}
		}
	}
	
	public static void removeFromManager(AgentInfo agentInfo) {
		String agentType = agentInfo.getAgentType();
		
		if (agentType.equals("agent")) {
			TaskAgentManager.removeTaskAgent(agentInfo.getName());
		} else if (agentType.equals("network")) {
			NetworkAgentManager.removeNetworkAgent(agentInfo.getName());
		} else if (agentType.equals("net_manager")) {
			NetworkCommManager.removeNetworkComm(agentInfo.getName());
		} else if (agentType.equals("query")) {
			QueryAgentManager.removeQueryAgent(agentInfo.getName());
		}		
	}
	
	private static ZipEntry getAgentDescriptor(String filename) {
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(filename);
			
			Enumeration en = jarFile.entries();
			while (en.hasMoreElements()) {
				JarEntry jarEntry = (JarEntry)en.nextElement();
				String fileName = jarEntry.getName();
				if (fileName.endsWith("descriptor.xml"))
					return jarEntry;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (jarFile != null) {
				try { jarFile.close(); } catch (Exception ioe) { }
			}
		}
		return null;
	}
	
	public static Object loadAgent(AgentInfo agentInfo) {
		Object result = null;
		
		try {
			URL url[] = { new URL("file:/" + agentInfo.getFilePath()) };
			URLClassLoader classLoader = new URLClassLoader(url);
			result = classLoader.loadClass(agentInfo.getClassName()).newInstance();
		} catch (Exception ex) {
			try {
				result = Class.forName(agentInfo.getFilePath()).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (result == null) ex.printStackTrace();
		}
		System.out.println(result);
		return result;
	}
	
	public static Object activateAgent(AgentInfo agentInfo) {
		Object obj = null;
		obj = AgentLoader.loadAgent(agentInfo);
		if (obj != null) {
			agentInfo.setInstance(obj);
			agentInfo.setActive(true);
			System.out.println(agentInfo);
			System.out.println(agentInfo.getArguments());
			((Agent)obj).setArguments(agentInfo.getArguments());
		}
		return obj;
	}
}
