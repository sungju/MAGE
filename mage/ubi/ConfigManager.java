package mage.ubi;

import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class ConfigManager {
	private static Hashtable configData = new Hashtable();
	private static Hashtable agentData = new Hashtable();
	
	public static void loadConfig(String path) {
	    try {
			DocumentBuilderFactory dbf = 
				DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new FileInputStream(path));
			Element root = doc.getDocumentElement();
			NodeList node = root.getElementsByTagName("env_var");
			for (int i = 0; i < node.getLength(); i++) {
				NamedNodeMap nnm = node.item(i).getAttributes();
				addConfigData(nnm.getNamedItem("name").getNodeValue(), 
							  nnm.getNamedItem("value").getNodeValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void addConfigData(String name, String value) {
		System.out.println("CONFIG : " + parseEnvTag(name) + ":" + parseEnvTag(value));
		configData.put(parseEnvTag(name), parseEnvTag(value));
	}

	public static void addAgentData(String name, String value, 
									String agentMode, Hashtable agentArgs) {
		System.out.println("AGENT : " + parseEnvTag(name) + ":" + parseEnvTag(value));
		System.out.println("\tMode = " + agentMode + "\t" + agentArgs.size());
		
		agentData.put(parseEnvTag(name), parseEnvTag(value));
	}

	public static String getEnvData(String key) {
		return (String)configData.get(key);
	}
	
	public static String parseEnvTag(String value) {
		int startPos, endPos;
		String envStr, realStr;
		StringBuffer resultStr = new StringBuffer();

		do {
			startPos = value.indexOf("$[");
			if (startPos >= 0) {
				endPos = value.indexOf("]", startPos + 2);
				if (endPos == -1) break;
				envStr = value.substring(startPos + 2, endPos);
				realStr = System.getProperty(envStr);
				
				resultStr.append(value.substring(0, startPos));
				resultStr.append(realStr);
				value = value.substring(endPos + 1);
			} else {
				startPos = value.indexOf("${");
				if (startPos == -1) break;
				endPos = value.indexOf("}", startPos + 2);
				envStr = value.substring(startPos + 2, endPos);
				realStr = (String)configData.get(envStr);
				
				if (realStr == null) {
					resultStr.append(value.substring(0, endPos));
				} else {
					resultStr.append(value.substring(0, startPos));
					resultStr.append(realStr);
				}
				value = value.substring(endPos + 1);
			}
		} while (true);
		
		resultStr.append(value);
		while ((startPos = resultStr.indexOf("//")) != -1) {
			resultStr.replace(startPos, startPos + 2, File.separator);
		}
		return resultStr.toString();
	}
	
	public static void saveConfig(String path) {
	}	
}