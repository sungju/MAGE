/*
 * SimpleMonitor
 * - Protocol test application
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
package old;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import javax.swing.*;

import mage.agent.AgentInfo;
import mage.agent.AgentLoader;
import mage.network.*;
import mage.ubi.ConfigManager;

public class SimpleMonitor extends JFrame {
	private static final long serialVersionUID = 2049711555453407584L;
	private String configPath = "." + File.separator + "client_config.xml";
	
	Container cp;
	JTextField txtCommand;
	JTextField txtCode;
	JButton btnSend;
	JTextPane taMessage;
	JTextPane taResult;
	
	private PeerNetworkAgent peer = null;
	public SimpleMonitor() {
		super("SimpleMonitor");
				
		ConfigManager.loadConfig(configPath);
		AgentLoader.loadAgentList(ConfigManager.getEnvData("AGENT_DIR"));
		
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setName("LITH_Talk");
		agentInfo.setClassName("mage.network.protocol.LITHTalk");
		
		//NetworkAgentManager.addNetworkAgent("LITH_Talk", "mage.network.protocol.LITHTalk");
		peer = ((ServerNetworkAgent)(NetworkAgentManager.findNetworkAgent("LITH_Talk"))).getPeerInstance();
		
		setUI();
		setEvents();
	}
	
	private void setUI() {
		cp = getContentPane();
		cp.setLayout(new BorderLayout());
		JPanel pnlTop = new JPanel();
		pnlTop.setLayout(new FlowLayout());
		cp.add(pnlTop, BorderLayout.NORTH);
		pnlTop.add(new JLabel("Command: "));
		pnlTop.add(txtCommand = new JTextField("MONITORING", 15));
		pnlTop.add(new JLabel("   Code: "));
		pnlTop.add(txtCode = new JTextField("100", 5));
		pnlTop.add(new JLabel("   "));
		pnlTop.add(btnSend = new JButton("SEND"));
		cp.add(new JScrollPane(taMessage = new JTextPane()), BorderLayout.CENTER);
		cp.add(new JScrollPane(taResult = new JTextPane()), BorderLayout.SOUTH);
		taResult.setPreferredSize(new Dimension(50, 200));
		taMessage.setText("SELECT 0,1,5-10 FROM DoExec USING cat /etc/rc");
		
		setForHeartBeat();
		
		setSize(600, 400);
		setLocation(100, 100);
		setVisible(true);
	}
	
	private void setForHeartBeat() {
		txtCommand.setText("NodeListRequest");
		taMessage.setText("Service: ServerBroker");
	}
	
	private void setEvents() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				sendMessage();
			}
		});
	}
	
	private void sendMessage() {
		HashMap data = new HashMap();
		Message msg = new Message();
		data.put("ADDRESS", "localhost");
		data.put("PORT", "2005");
		peer.setArguments(data);
		
		msg.setQueryType("HeartBeat");
		msg.setCommand(txtCommand.getText());
		msg.setCode(Integer.parseInt(txtCode.getText()));
		msg.setMessage(taMessage.getText());
		
		if (!peer.connect()) {
			JOptionPane.showMessageDialog(this, "Failed to connect", "SimpleMonitor", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		msg = peer.sendAndReceive(msg);
		/*
		if (msg != null) 
			taResult.setText(parseMessage(msg.getMessage()));
		*/
		taResult.setText(msg.getMessage());
		
		peer.close();
	}
	
	/*
	private String parseMessage(String msg) {
		StringTokenizer st = new StringTokenizer(msg, "\n", false);	
		String line;
		String tokenStr, tokenData;
		StringBuffer sb = new StringBuffer();
		
		while (st.hasMoreTokens()) {
			line = st.nextToken();
			StringTokenizer stLine = new StringTokenizer(line, ":", false);
			tokenStr = stLine.nextToken();
			tokenData = stLine.nextToken();
			
			sb.append(tokenStr);

			if (tokenStr.equalsIgnoreCase("NodeCount")) {
				sb.append(" = ");
				sb.append(tokenData);
				sb.append("\n");
			} else {
				sb.append(" = {\n");
			    sb.append(new String(Base64.decode(tokenData)));
			    sb.append("\n}\n");
			}
		}
		
		return sb.toString();
	}
	*/
	
	
	public static void main(String[] args) {
		new SimpleMonitor();
	}
}
