package mage.ubi;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import mage.agent.AgentLoader;

public class ServiceRepository {
	private String configPath = "." + File.separator + "sr_config.xml";
	
	public ServiceRepository() {
		
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
		ServiceRepository sr = new ServiceRepository();
				
		sr.start();
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}
}
