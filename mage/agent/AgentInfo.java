/*
 * ProviderInfo
 * - Contains the provider's information to control
 * - Used by ProviderManager
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

import java.util.HashMap;

public class AgentInfo {
	private String name = null;
	private String className = null;
	private String activeMode = null;
	private String version = null;
	private String filePath = null;
	private String agentType = null;
	private HashMap arguments = null;
	
	private Object instance = null;
	private boolean isActive = false;
	
	public AgentInfo() {
		clearInfo();
	}
	
	public void clearInfo() {
		setName(null);
		setClassName(null);
		setActiveMode(null);
		setVersion(null);
		setFilePath(null);
		setAgentType(null);
		setArguments(null);
		
		setInstance(null);
		setActive(false);
	}
	
	public void setData(String name, String className, String activeMode, String version,
						String filePath, String agentType, HashMap arguments) {
		setName(name);
		setClassName(className);
		setActiveMode(activeMode);
		setVersion(version);
		setFilePath(filePath);
		setAgentType(agentType);
		setArguments(arguments);
	}
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String classPath) {
		this.className = classPath;
	}
	public Object getInstance() {
		return instance;
	}
	public void setInstance(Object instance) {
		this.instance = instance;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getActiveMode() {
		return activeMode;
	}

	public void setActiveMode(String activeMode) {
		this.activeMode = activeMode;
	}

	public HashMap getArguments() {
		return arguments;
	}

	public void setArguments(HashMap arguments) {
		this.arguments = arguments;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAgentType() {
		return agentType;
	}

	public void setAgentType(String agentType) {
		this.agentType = agentType;
	}
}
