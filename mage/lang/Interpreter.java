/*
 * Interpreter
 * - deliver protocol's Message object to the proper query component
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
package mage.lang;

import java.util.HashMap;
import java.util.Iterator;

import mage.agent.*;
import mage.network.Message;
import mage.ubi.*;

public class Interpreter {
	public static Message processMessage(Message msg) {
		Message resultMsg = null;
		String myDomain = (String)ServiceManager.getData("NodeName");
		HashMap arguments = null;
		TaskAgent pi = null;
		
		String command = msg.getCommand();
		int code = msg.getCode();
		String message = msg.getMessage();
		
		QueryAgent query = null;
		query = getQuery(msg.getQueryType());
		if (query == null) return resultMsg;
		arguments = query.parse(msg);
		msg.setQueryAgent(query); // 파싱에 사용된 쿼리 객체를 저장함(필요할지도 모르기 때문)
		
		String domain = (String)arguments.get("DOMAIN");
		String service = (String)arguments.get("SERVICE");

		if (service == null) {
			System.out.println("ERROR: Message can't parse : <" + message + ">");
			return resultMsg;
		}
		if (domain != null && !domain.equals(myDomain)) {
			// TODO: Must implement remote gathering
			System.out.println("It must get information from another node.");
			System.out.println("It is not implemented...");
		} else {
			pi = TaskAgentManager.findTaskAgent(service);
			if (pi == null) {
				System.out.println("ERROR: Unknown services : <" + service + ">");
			}
		}
		System.out.println("Service Name : " + pi);
		resultMsg = query.doService(command, code, domain, service, msg, arguments, pi);
		
		resultMsg.setMessageKind(Message.KIND_RESPOND);
		
		return resultMsg;
	}
	
	public static QueryAgent getQuery(String name) {
		
		QueryAgent qai = QueryAgentManager.findQueryAgent(name);
		if (qai == null) return null;
		return qai;
	}
}
