/*
 * ServiceManager
 *  - Contains All service information of this node.
 *  - Domain Name, Service List and related component Name.
 *    
 *    (c)Copyright 2005,2006
 *    Written by Sungju Kwon
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

import java.util.HashMap;

public class ServiceManager {
	private static HashMap keyRepository = new HashMap();
	private static HashMap childInfoRepository = new HashMap(); // For child node.
	private static HashMap serviceInfoRepository = new HashMap(); // repository for service information
	
	public static Object getData(String key) {
		return keyRepository.get(key);
	}
	
	public static void putData(String key, Object data) {
		keyRepository.put(key, data);
	}
	
	public static boolean addChildNode(String name, String address) {
		if (findChildNode(name) != null)
			return false;
		childInfoRepository.put(name, address);
		return true;
	}
	
	public static boolean removeChildNode(String name) {
		if (findChildNode(name) == null)
			return false;
		childInfoRepository.remove(name);
		return true;
	}
	
	public static String findChildNode(String name) {
		String address = (String)childInfoRepository.get(name);
		return address;
	}
	
	public static boolean addServiceInfo(String name, String classPath,
												String description) {
		if (findServiceInfo(name) != null)
			return false;
		HashMap data = new HashMap();
		data.put("classPath", classPath);
		data.put("description", description);
		
		serviceInfoRepository.put(name, data);
		return true;
	}
	
	public static boolean deleteService(String name) {
		HashMap data = findServiceInfo(name);
		if (data == null)
			return false;
		
		serviceInfoRepository.remove(name);
		return true;
	}
	
	public static HashMap findServiceInfo(String name) {
		HashMap info = (HashMap)serviceInfoRepository.get(name);
		return info;
	}
}
