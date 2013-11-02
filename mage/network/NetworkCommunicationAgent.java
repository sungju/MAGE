package mage.network;

import java.util.*;
import mage.agent.Agent;

public abstract class NetworkCommunicationAgent extends Agent {
	abstract public void setManagerName(String managerName);
	abstract public void setServerProtocol(String serverProtocol);
	abstract public void setArguments(HashMap args);
	
	abstract public void setData(String managerName, String serverProtocol, HashMap args);
	
	abstract public void start();
}
