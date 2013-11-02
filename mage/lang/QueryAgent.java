/*
 * QueryInterface
 * - Interface for query component
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

import mage.agent.Agent;
import mage.agent.TaskAgent;
import mage.network.Message;

public abstract class QueryAgent extends Agent {
	abstract public HashMap parse(String message);
	abstract public HashMap parse(Message msg);
	abstract public Message doService(String command, int code, String domain, String service, 
								Message msg, HashMap arguments, TaskAgent pi);
}
