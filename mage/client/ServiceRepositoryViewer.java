package mage.client;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.TableModelEvent;

import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.table.*;

import mage.agent.AgentInfo;
import mage.network.Message;
import mage.network.NetworkAgentManager;
import mage.network.PeerNetworkAgent;
import mage.network.ServerNetworkAgent;
import mage.ubi.GridSpaceClient;
import mage.ubi.ServiceManager;
import mage.utils.Base64;

public class ServiceRepositoryViewer extends JPanel {
	private static final long serialVersionUID = -7102210757265761126L;
	
	GridSpaceClient clientMain = null;
	// UI
	// Directory Control Components
	private JComboBox cbDirList;
	private JTable tblDir;
	protected String dirData[][] = new String[1][4];
	private String currentDir = "/";

	private JButton btnUpDir = new JButton("Up");
	private JButton btnCopy = new JButton("Copy");
	private JButton btnCut = new JButton("Cut");
	private JButton btnPaste = new JButton("Paste");
	private JButton btnUpload = new JButton("Upload");
	private JButton btnDownload = new JButton("Download");
	private JButton btnChdir = new JButton("Chdir");
	private JButton btnDelete = new JButton("Delete");
	private JButton btnMkdir = new JButton("Make Dir");
	private JButton btnRmdir = new JButton("Remove Dir");
	private JButton btnMount = new JButton("Mount");
	private JButton btnUnmount = new JButton("Unmount");
	private JButton btnRefresh = new JButton("Refresh");
	private JButton btnInfo = new JButton("Info");
	
	// Execute Jobs Components
	JTextField tfJobName = new JTextField("Job #1");
	JTextField tfJobData = new JTextField();
	JButton btnJobDataChooser = new JButton("...");
	JTextField tfWorkCount = new JTextField("1");
	JCheckBox checkAutoSelect = new JCheckBox("Auto Select", true);
	JButton btnExecuteJob = new JButton("Execute");
	
	// File Copys
	private static final int MODE_NONE = 0;
	private static final int MODE_COPY = 1;
	private static final int MODE_CUT = 2;
	private int pasteMode = MODE_NONE;
	private String pasteFileNameWithPath = null;
	private String pasteFileName = null;
	
	private Random random = new Random(System.currentTimeMillis());
	
	public ServiceRepositoryViewer(GridSpaceClient clientMain) {
		this.clientMain = clientMain;
		
		setUI();
		setEvents();
	}
	
	private void setUI() {
	    EmptyBorder border10 = new EmptyBorder(10, 10, 10, 10);		
	    EmptyBorder border5 = new EmptyBorder(5, 5, 5, 5);
	    
	    cbDirList = new JComboBox();
		tblDir = new JTable(new DirTableModel(this));
		setLayout(new BorderLayout());
		
		JPanel pnlBottom = new JPanel();
		JPanel pnlDirList = new JPanel();
		pnlDirList.setLayout(new BorderLayout());
		pnlDirList.setBorder(new CompoundBorder(new TitledBorder(null, "Directory List",
				TitledBorder.LEFT, TitledBorder.TOP), border5));
				
		JPanel pnlNorth = new JPanel();
		pnlNorth.setLayout(new BorderLayout());
		pnlNorth.add(cbDirList, BorderLayout.CENTER);
		pnlNorth.add(btnUpDir, BorderLayout.EAST);
		
		resizeTable();	
		pnlDirList.add(pnlNorth, BorderLayout.NORTH);
		pnlDirList.add(new JScrollPane(tblDir), BorderLayout.CENTER);
		add(pnlDirList, BorderLayout.CENTER);
		add(pnlBottom, BorderLayout.SOUTH);
		
		pnlBottom.setLayout(new BoxLayout(pnlBottom, BoxLayout.Y_AXIS));
		
		JPanel pnlDirCommand = new JPanel();
		pnlDirCommand.setLayout(new GridLayout(5, 3));
		pnlDirCommand.setBorder(new CompoundBorder(new TitledBorder(null, "Directory Management",
				TitledBorder.LEFT, TitledBorder.TOP), border10));
		pnlDirCommand.add(btnCopy);
		pnlDirCommand.add(btnCut);
		pnlDirCommand.add(btnPaste);
		pnlDirCommand.add(btnUpload);
		pnlDirCommand.add(btnDownload);
		pnlDirCommand.add(btnDelete);
		pnlDirCommand.add(btnChdir);
		pnlDirCommand.add(btnMkdir);
		pnlDirCommand.add(btnRmdir);
		pnlDirCommand.add(btnMount);
		pnlDirCommand.add(btnUnmount);
//		pnlDirCommand.add(btnRefresh);
		pnlDirCommand.add(btnInfo);
	    
		JPanel pnlJobExec = new JPanel();
		pnlJobExec.setBorder(new CompoundBorder(new TitledBorder(null, "Job Executor",
							TitledBorder.LEFT, TitledBorder.TOP), border10));
		pnlJobExec.setLayout(new BorderLayout());
		
		JPanel pnlJobDesc = new JPanel();
		JPanel pnlJobInput = new JPanel();
		
		pnlJobDesc.setLayout(new GridLayout(3, 1, 3, 3));
		pnlJobDesc.add(new JLabel("Job Name "));
		pnlJobDesc.add(new JLabel("Job Data "));
		pnlJobDesc.add(new JLabel("Work No. "));
		
		pnlJobInput.setToolTipText("Job Executor");
		pnlJobInput.setLayout(new GridLayout(3, 1, 3, 3));
		JPanel pnlJobName = new JPanel();
		pnlJobName.setLayout(new BorderLayout());
		pnlJobName.add(tfJobName, BorderLayout.CENTER);
		
		JPanel pnlJobData = new JPanel();
		pnlJobData.setLayout(new BorderLayout());
		pnlJobData.add(tfJobData, BorderLayout.CENTER);
		pnlJobData.add(btnJobDataChooser, BorderLayout.EAST);

		JPanel pnlTmp = new JPanel();
		pnlTmp.setLayout(new BorderLayout());
		pnlTmp.add(tfWorkCount, BorderLayout.CENTER);
		pnlTmp.add(checkAutoSelect, BorderLayout.EAST);
		
		JPanel pnlExecuteJob = new JPanel();
		pnlExecuteJob.setLayout(new BorderLayout());
		pnlExecuteJob.add(pnlTmp, BorderLayout.CENTER);
		pnlExecuteJob.add(btnExecuteJob, BorderLayout.EAST);
		
		pnlJobInput.add(pnlJobName);
		pnlJobInput.add(pnlJobData);
		pnlJobInput.add(pnlExecuteJob);
		
		pnlJobExec.add(pnlJobDesc, BorderLayout.WEST);
		pnlJobExec.add(pnlJobInput, BorderLayout.CENTER);
		
		pnlBottom.add(pnlDirCommand);
		pnlBottom.add(pnlJobExec);
		
		cbDirList.addItem("/");
	}
	
	private void resizeTable() {
		TableColumn column = null;
		for (int i = 0; i < tblDir.getColumnCount(); i++) {
			column = tblDir.getColumnModel().getColumn(i);
			if (i == 0)
				column.setPreferredWidth(5);
			else if (i == 1)
				column.setPreferredWidth(150);
			else if (i == 2)
				column.setPreferredWidth(30);
			else if (i == 3)
				column.setPreferredWidth(70);

		}
	}
	
	private void setEvents() {
		cbDirList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isConnected()) {
					int selIdx = cbDirList.getSelectedIndex();
					if (selIdx == -1) return;
					getDirList((String)cbDirList.getSelectedItem());
				}
			}
		});
		
		tblDir.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (!isConnected()) return;
				
				changeDirectory();
			}
		});
		
		btnUpDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isConnected()) return;
								
				if (currentDir.equals("/")) return;
				int idx = currentDir.substring(0, currentDir.length() - 1).lastIndexOf('/');
				String dirStr = currentDir.substring(0, idx);
				if (dirStr == null || dirStr.trim().length() == 0)
					dirStr = "/";
				getDirList(dirStr);
			}
		});
		
		btnCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				copyFileName();
			}
		});
		
		btnCut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				cutFileName();
			}
		});
		
		btnPaste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				pasteFile();
			}
		});
		
		btnUpload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				uploadFile();
			}
		});
		
		btnDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				downloadFile();
			}
		});
		
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				deleteFile();
			}
	});
		
		btnChdir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				changeDirectory();
			}
		});
		
		btnMkdir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				makeDirectory();
			}
		});
		
		btnRmdir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				removeDirectory();
			}
		});
		
		btnMount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JOptionPane.showMessageDialog(clientMain, "Mount not implemented yet", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		});
		btnUnmount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JOptionPane.showMessageDialog(clientMain, "Unmount not implemented yet", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		});
		btnInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				showFileInfo();
			}
		});
		
		// Job Related events
		btnJobDataChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				File file = chooseFile(JFileChooser.OPEN_DIALOG, "");
				if (file == null) return;
				tfJobData.setText(file.getAbsolutePath());
			}
		});
		
		checkAutoSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				checkAndSetEditable();
			}
		});
		
		btnExecuteJob.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// TODO: 작업 제출 및 모니터링 윈도우 활성화
				jobSubmit();
			}
		});
		
		checkAndSetEditable();
	}
	
	private void checkAndSetEditable() {
		if (checkAutoSelect.isSelected()) {
			tfWorkCount.setEditable(false);
			tfWorkCount.setEnabled(false);
		} else {
			tfWorkCount.setEditable(true);
			tfWorkCount.setEnabled(true);
		}
	}
	
	private void jobSubmit() {
		String jobName = tfJobName.getText();
		String jobDataPath = tfJobData.getText();
		int execCount = parseInt(tfWorkCount.getText());
		boolean isAutoSelect = checkAutoSelect.isSelected();
		
		if (jobName == null || jobName.trim().length() == 0 || 
			execCount == 0) {
			JOptionPane.showMessageDialog(clientMain, "Input job fields", "Job Submission failed", JOptionPane.ERROR_MESSAGE);
			return;
		}
		/*
		 * TODO: AutoSelect가 아닌 경우 사용자가 선택한 작업 노드들 정보를 같이 보내줘야 함.
		if (!isAutoSelect) {
			
		}
		*/
		
		int idx = tblDir.getSelectedRow();
		if (idx < 0 || idx >= dirData.length || dirData[idx][0].trim().endsWith("D")) {
			JOptionPane.showMessageDialog(clientMain, "Select executetable agent", "Job submission failed", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String fileName = dirData[idx][1];
		String jobData = "";
		if (jobDataPath != null) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(currentDir + fileName);
				byte[] data = new byte[30];
				int cnt = 0;
				while ((cnt = fis.read(data)) > 0) {
					byte[] tmp = new byte[cnt];
					System.arraycopy(data, 0, tmp, 0, cnt);
					jobData += Base64.encode(tmp);
				}
			} catch (Exception ex) {
			} finally {
				if (fis != null)
					try { fis.close(); } catch (Exception e) { }
			}
		}
		String workNodeListStr = "";
		
		// Directory: Agent 파일의 디렉토리
		// Filename: Agent 파일 이름
		// ExecCount: 실행할 작업의 수
		// AutoSelect: 자동 선택인지의 여부를 알려줌(1: 자동 선택, 0: 사용자 지정)
		// JobData: 작업 데이타(여러 작업에 분할될 수 있는 형태로 발전시켜야 함)
		// WorkNodeList: 작업 노드들의 주소
		
		int rm_count = 0;
		if ((rm_count = clientMain.resourceMonitorPanelList.size()) == 0) {
			JOptionPane.showMessageDialog(clientMain, "Resource Manager unknown");
			return;			
		}
		int rm_sel = random.nextInt() % rm_count;
		ResourceMonitorPanel rmp = clientMain.resourceMonitorPanelList.elementAt(rm_sel);
		String resourceManagerAddr = rmp.getAddress();
		int resourceManagerPort = rmp.getPort();
		
		PeerNetworkAgent peerNetwork = connectTo(resourceManagerAddr, resourceManagerPort);
		StringBuffer sb = new StringBuffer();
		sb.append("Directory: ");
		sb.append(currentDir);
		sb.append("\n");
		sb.append("Filename: ");
		sb.append(fileName);
		sb.append("\n");
		sb.append("ExecCount: ");
		sb.append(execCount);
		sb.append("\n");
		sb.append("AutoSelect: ");
		sb.append(isAutoSelect ? 1 : 0);
		sb.append("\n");
		sb.append("JobData: ");
		sb.append(jobData);
		sb.append("\n");
		sb.append("WorkNodeList: ");
		sb.append(workNodeListStr);
		sb.append("\n");
		
		Message msg = buildMessage("execute_job", "");
		msg.setMessage("Service: ResourceManagerAgent\n" + sb.toString());
		msg = peerNetwork.sendAndReceive(msg);
		if (msg.getCode() != 0) {
			clientMain.createJobWindow(resourceManagerAddr, resourceManagerPort, msg.getMessage());
		} else {
			JOptionPane.showMessageDialog(clientMain, "Job creation failed", 
									"Job Submission Failed", JOptionPane.ERROR_MESSAGE);
		}
		
		peerNetwork.close();		
	}

	private PeerNetworkAgent connectTo(String addr, int port) {
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setName("LITH_Talk");
		agentInfo.setClassName("mage.network.protocol.LITHTalk");
		
		PeerNetworkAgent peerNetwork = ((ServerNetworkAgent)(NetworkAgentManager.findNetworkAgent("LITH_Talk"))).getPeerInstance();
		
		HashMap data = new HashMap();
		data.put("ADDRESS", addr);
		data.put("PORT", "" + port);
		peerNetwork.setArguments(data);
		
		if (!peerNetwork.connect()) {
			return null;
		}	
		return peerNetwork;
	}
	
	private void showFileInfo() {
		if (!isConnected()) return;
		int idx = tblDir.getSelectedRow();
		if (idx < 0 || idx >= dirData.length) return;
/*
		if (!dirData[idx][0].trim().equals("D")) {
			JOptionPane.showMessageDialog(clientMain, "Cannot v", "Illegal operation", JOptionPane.ERROR_MESSAGE);
			return;			
		}
*/		
		StringBuffer sb = new StringBuffer();
		sb.append("Directory: " + currentDir + "\n");
		sb.append("Filename: " + dirData[idx][1]);
		
		Message msg = buildMessage("info", sb.toString());
		msg = clientMain.peerNetwork.sendAndReceive(msg);
		JOptionPane.showMessageDialog(clientMain, msg.getMessage(), "File Info", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void makeDirectory() {
		if (!isConnected()) return;
		String dirName =  JOptionPane.showInputDialog("Input Directory Name", "");
		if (dirName == null || dirName.length() == 0) return;
		StringBuffer sb = new StringBuffer();
		sb.append("Directory: " + currentDir + "\n");
		sb.append("DirName: " + dirName + "\n");
		
		Message msg = buildMessage("mkdir", sb.toString());
		msg = clientMain.peerNetwork.sendAndReceive(msg);
		if (!msg.getMessage().equals("SUCCESS")) {
			JOptionPane.showMessageDialog(clientMain, msg.getMessage(), "Operation Failed", JOptionPane.ERROR_MESSAGE);
			return;			
		}
		getDirList(currentDir);
	}
	
	private void removeDirectory() {
		if (!isConnected()) return;
		int idx = tblDir.getSelectedRow();
		if (idx < 0 || idx >= dirData.length) return;
		if (!dirData[idx][0].trim().equals("D")) {
			JOptionPane.showMessageDialog(clientMain, "Cant delete file with this command", "Illegal operation", JOptionPane.ERROR_MESSAGE);
			return;			
		}
		StringBuffer sb = new StringBuffer();
		sb.append("Directory: " + fullPathOfFile(idx));
		
		Message msg = buildMessage("rmdir", sb.toString());
		msg = clientMain.peerNetwork.sendAndReceive(msg);
		if (!msg.getMessage().equals("SUCCESS")) {
			JOptionPane.showMessageDialog(clientMain, msg.getMessage(), "Operation Failed", JOptionPane.ERROR_MESSAGE);
			return;
		}
		getDirList(currentDir);
	}
	
	private void deleteFile() {
		if (!isConnected()) return;
		int idx = tblDir.getSelectedRow();
		if (idx < 0 || idx >= dirData.length) return;
		if (dirData[idx][0].trim().equals("D")) {
			JOptionPane.showMessageDialog(clientMain, "Directory can't delete with this command", "Illegal operation", JOptionPane.ERROR_MESSAGE);
			return;			
		}
		StringBuffer sb = new StringBuffer();
		sb.append("Directory: " + currentDir + "\n");
		sb.append("Filename: " + dirData[idx][1]);
		
		Message msg = buildMessage("rm", sb.toString());
		msg = clientMain.peerNetwork.sendAndReceive(msg);
		if (!msg.getMessage().equals("SUCCESS")) {
			JOptionPane.showMessageDialog(clientMain, msg.getMessage(), "Operation Failed", JOptionPane.ERROR_MESSAGE);
			return;
		}
		getDirList(currentDir);
	}

	private boolean isConnected() {
		return clientMain.peerNetwork != null && clientMain.peerNetwork.isConnected();
	}
	
	private void uploadFile() {
		if (!isConnected()) return;
		File file = chooseFile(JFileChooser.OPEN_DIALOG, "");
		if (file == null) return;
		if (!file.exists()) return;

		StringBuffer fileContent = new StringBuffer();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			int cnt = 0;
			byte[] data = new byte[fis.available()];
			fis.read(data);
			fileContent.append(Base64.encode(data));
			/*
			int total = 0;
			while ((cnt = fis.read(data)) != -1) {
				total += cnt;
				byte[] encData = new byte[cnt];
				System.arraycopy(data, 0, encData, 0, cnt);
				fileContent.append(Base64.encode(encData));
			}
			System.out.println("READ :" + total + " Bytes");
			*/
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(clientMain, "File read error", "Illegal operation", JOptionPane.ERROR_MESSAGE);
			return;		
		} finally {
			if (fis != null) 
				try { fis.close(); } catch (Exception ex) { }
		}
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("Directory: " + currentDir + "\n");
		sb.append("Filename: " + file.getName() + "\n");
		sb.append("Encoding: base64\n");
		sb.append("FileContent: " + fileContent.toString());
		sb.append("\n");
		
		Message msg = buildMessage("upload", sb.toString());
		msg = clientMain.peerNetwork.sendAndReceive(msg);
		if (!msg.getMessage().equals("SUCCESS")) {
			JOptionPane.showMessageDialog(clientMain, "Upload operation failed", "File Operation", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		getDirList(currentDir);
	}
	
	private void downloadFile() {
		if (!isConnected()) return;
		int idx = tblDir.getSelectedRow();
		if (idx < 0 || idx >= dirData.length) return;
		
		String selectedFileName = dirData[idx][1];
		File file = chooseFile(JFileChooser.SAVE_DIALOG, selectedFileName);
		if (file == null) return;
		
		StringBuffer sb = new StringBuffer();
		sb.append("Directory: " + currentDir + "\n");
		sb.append("Filename: " + selectedFileName + "\n");
		sb.append("Encoding: base64\n");
		
		Message msg = buildMessage("download", sb.toString());
		msg = clientMain.peerNetwork.sendAndReceive(msg);
		if (msg.getCode() == 0) {
			JOptionPane.showMessageDialog(clientMain, msg.getMessage(), "File download failed", JOptionPane.ERROR_MESSAGE);
			return;			
		}
		
		StringTokenizer st = new StringTokenizer(msg.getMessage(), "\n");
		if (st.countTokens() < 3) {
			JOptionPane.showMessageDialog(clientMain, "File contetns are missing", "File download failed", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String downloadFileName = st.nextToken();
		String encode = st.nextToken();
		byte[] contents;
		if (encode.equals("base64")) {
			contents = Base64.decode(st.nextToken());
		} else {
			contents = st.nextToken().getBytes();
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(contents);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(clientMain, "File write error", "File download failed", JOptionPane.ERROR_MESSAGE);
			return;
		} finally {
			if (fos != null)
				try { fos.close(); } catch (Exception ex) { }
		}
	}
	
	private File chooseFile(int mode, String defaultName) {
		JFileChooser fc = new JFileChooser();
		File curDir = new File(".");
		fc.setCurrentDirectory(curDir);
		fc.setSelectedFile(new File(defaultName));
		int retVal = mode == JFileChooser.OPEN_DIALOG ? fc.showOpenDialog(clientMain) :
														fc.showSaveDialog(clientMain);
		if (retVal == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		}
		return null;
	}
	
	private String fullPathOfFile(int idx) {
		String pathStr = currentDir + (currentDir.equals("/") ? "" : "/");
		if (idx < 0 || idx >= dirData.length) return null;
		return pathStr + dirData[idx][1];
	}
	
	private void copyFileName() {
		if (!isConnected()) return;
		
		int idx = tblDir.getSelectedRow();
		String tmpStr = fullPathOfFile(idx);
		if (tmpStr == null) return;		
		pasteFileNameWithPath = tmpStr;
		pasteFileName = dirData[idx][1];
		pasteMode = MODE_COPY;
	}
	
	private void cutFileName() {
		if (!isConnected()) return;
				
		int idx = tblDir.getSelectedRow();
		String tmpStr = fullPathOfFile(idx);
		if (tmpStr == null) return;		
		pasteFileNameWithPath = tmpStr;
		pasteFileName = dirData[idx][1];
		pasteMode = MODE_CUT;
	}
	
	private Message buildMessage(String cmdStr, String msgStr) {
		Message msg = new Message();
		msg.setQueryType("HeartBeat");
		msg.setCommand(cmdStr);
		msg.setCode(100);
		msg.setMessage("Service: ServiceRepositoryAgent\n" + msgStr);

		return msg;
	}
	
	private void pasteFile() {
		if (!isConnected()) return;
		
		if (pasteMode == MODE_NONE) {
			JOptionPane.showMessageDialog(clientMain, "You must choose file to paste", "Illegal operation", JOptionPane.ERROR_MESSAGE);
			return;			
		}
		String destFileWithPath = currentDir;
		if (!destFileWithPath.equals("/"))
			destFileWithPath += "/";
		destFileWithPath += pasteFileName;
	
		Message msg = buildMessage(pasteMode == MODE_COPY ? "cp" : "mv",
				                   "Source: " + pasteFileNameWithPath + "\nDestination: " + destFileWithPath);

		msg = clientMain.peerNetwork.sendAndReceive(msg);
		
		if (!msg.getCommand().equals("SR_RESULT") ||
			!msg.getMessage().equals("SUCCESS")) {
			JOptionPane.showMessageDialog(clientMain, "Paste Failed for " + pasteFileNameWithPath, "Failure", JOptionPane.ERROR_MESSAGE);
			return;
		}
		pasteMode = MODE_NONE;
		getDirList(currentDir);
	}
	
	private void changeDirectory() {
		int idx = tblDir.getSelectedRow();
		if (idx > dirData.length || idx < 0) return;
		String typeStr = dirData[idx][0].trim();
		if (typeStr.equals("D")) {
			String prevDirStr = (String)cbDirList.getSelectedItem();
			if (prevDirStr == null) prevDirStr = "";
			getDirList(prevDirStr + dirData[idx][1]);
		}
	}
	
	public void getDirList(String dirStr) {
		if (!isConnected()) return;
		
		Message msg = buildMessage("ls", "Directory: " + dirStr);
		
		msg = clientMain.peerNetwork.sendAndReceive(msg);
		if (!msg.getCommand().equals("SR_RESULT")) return;
		if (msg.getCode() == 0) {
			JOptionPane.showMessageDialog(clientMain, dirStr + "\n" + msg.getMessage(), "Failed to obtain file list", JOptionPane.ERROR_MESSAGE);
			return;
		}
		StringTokenizer st = new StringTokenizer(msg.getMessage(), "\n", false);
		dirData = new String[st.countTokens()][4];
		int i = 0;
		while (st.hasMoreTokens()) {
			StringTokenizer stOne = new StringTokenizer(st.nextToken(), ":", false);
			dirData[i][0] = stOne.nextToken().equals("Dir") ? "D": " ";
			dirData[i][1] = stOne.nextToken();
			dirData[i][2] = stOne.nextToken();
			if (stOne.hasMoreTokens())
				dirData[i][3] = stOne.nextToken();
			
			i++;
		}
		tblDir.revalidate();
		tblDir.repaint();

		StringTokenizer str = new StringTokenizer(dirStr, "/", false);
		cbDirList.removeAllItems();
		cbDirList.addItem("/");
		String tmpStr = "/";
		while (str.hasMoreTokens()) {
			tmpStr += str.nextToken() + "/";
			cbDirList.addItem(tmpStr);
		}
		cbDirList.setSelectedIndex(cbDirList.getItemCount() - 1);
		
		currentDir = dirStr;
	}
	private int parseInt(String data) {
		int result = 0;
		try {
			result = Integer.parseInt(data);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
	class DirTableModel extends AbstractTableModel {
		String columnNames[] = {"type", "Name", "Size", "Desc"};
		
		private ServiceRepositoryViewer srv;
		public DirTableModel(ServiceRepositoryViewer srv) {
			this.srv = srv;
		}
		
		public int getColumnCount() { return columnNames.length; }
		public int getRowCount() { return srv.dirData.length; }
		public Object getValueAt(int row, int col) {
			return srv.dirData[row][col];
		}
		public String getColumnName(int col) {
			return columnNames[col];
		}
	}
}
