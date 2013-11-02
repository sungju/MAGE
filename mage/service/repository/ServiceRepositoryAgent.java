/*
 * ServiceRepositoryAgent
 * - Agent who communicate with users about Service Information.
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
package mage.service.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import mage.agent.TaskAgent;
import mage.network.Message;
import mage.ubi.ConfigManager;

public class ServiceRepositoryAgent extends TaskAgent {
	private String rootDir = null;
	private Vector<ResourceManagerInfo> rmInfoList = new Vector<ResourceManagerInfo>();
	
	public Message invokeService(String command, int code, String message,
			HashMap arguments) {
		Message msg = null;
		
		System.out.println("CMD: " + command);
		System.out.println("MSG: " + message);
		System.out.println("ARGS: " + arguments);
		if (command.equals("upload")) {
			// 파일을 서비스 저장소에 올리는 역할을 수행
			msg = doUpload(command, arguments);
		} else if (command.equals("download")) { // 파일을 클라이언트가 다운받고자 할 때 사용
			msg = doDownload(command, arguments);
		} else if (command.equals("mount")) { // 특정 디렉토리에 대해서 다른 서비스 저장소를 연결하고자 할 경우 사용
			msg = doMount(command, arguments);
		} else if (command.equals("unmount")) { // 디렉토리 연결을 해제할 경우 사용
			msg = doUnmount(command, arguments);
		} else if (command.equals("ls")) { // 지정한 디렉토리의 파일 리스트를 보고자 할 경우 사용
			msg = doList(command, arguments);			
		} else if (command.equals("info")) { // 특정 파일 및 디렉토리에 대한 정보를 얻고자 할 때 사용
			msg = doInfo(command, arguments);
		} else if (command.equals("mkdir")) { // 디렉토리를 새로 만들 경우 사용
			msg = doMkdir(command, arguments);
		} else if (command.equals("rmdir")) { // 디렉토리를 제거하고자 할 경우 사용
			msg = doRmdir(command, arguments);
		} else if (command.equals("rm")) { // 파일을 제거할 경우에 사용
			msg = doRemove(command, arguments);			
		} else if (command.equals("cp")) { // 디렉토리간에 파일을 복사할 경우에 사용
			msg = doCopy(command, arguments);
		} else if (command.equals("mv")) { // 파일을 다른 곳으로 이동하고자 할 경우에 사용
			msg = doMove(command, arguments);
		} else if (command.equals("add_resource_manager")) {
			msg = doAddResourceManager(command, arguments);
		} else if (command.equals("remove_resource_manager")) {
			msg = doRemoveResourceManager(command, arguments);
		} else if (command.equals("resource_manager_list")) {
			msg = doResourceManagerList(command, arguments);
		}
		
		if (msg == null) {
			msg = new Message();
			msg.setCode(0);
			msg.setMessage("Unknown command");
		}
		
		msg.setQueryType("HeartBeat");
		msg.setCommand("SR_RESULT");

		return msg;
	}
	
	private Message doAddResourceManager(String command, HashMap arguments) {
		// Arguments
		// Address: IP주소
		// Port: Port
		// Description: 설명 
		Message msg = new Message();
		msg.setCode(0);
		
		String address = (String)arguments.get("ADDRESS");
		int port = parseInt((String)arguments.get("PORT"));
		String queryType = (String)arguments.get("QUERYTYPE");
		
		ResourceManagerInfo rmi = new ResourceManagerInfo();
		rmi.setAddress(address);
		rmi.setPort(port);
		rmi.setQueryType(queryType);
		
		if (findResourceManager(rmi) == null)
			rmInfoList.add(rmi);
		
		msg.setCode(100);
		msg.setMessage("SUCCESS");
		
		return msg;
	}
	
	private ResourceManagerInfo findResourceManager(ResourceManagerInfo rmi) {
		for (ResourceManagerInfo tmpInfo : rmInfoList) {
			if (tmpInfo.equals(rmi))
				return tmpInfo;
		}
		return null;
	}
	
	private Message doRemoveResourceManager(String command, HashMap arguments) {
		// Arguments
		// Address: IP주소
		// Port: Port
		Message msg = new Message();
		msg.setCode(0);
		
		String address = (String)arguments.get("ADDRESS");
		int port = parseInt((String)arguments.get("PORT"));
		
		ResourceManagerInfo rmi = new ResourceManagerInfo();
		rmi.setAddress(address);
		rmi.setPort(port);
	
		ResourceManagerInfo tmpRmi = findResourceManager(rmi);
		if (tmpRmi != null)
			rmInfoList.remove(tmpRmi);
		
		msg.setCode(100);
		msg.setMessage("SUCCESS");
		
		return msg;
	}
	
	private Message doResourceManagerList(String command, HashMap arguments) {
		// Arguments
		// None
		Message msg = new Message();
		msg.setCode(100);
		StringBuffer sb = new StringBuffer();
		
		for (ResourceManagerInfo rmi : rmInfoList) {
			sb.append(rmi.getAddress());
			sb.append(":");
			sb.append(rmi.getPort());
			sb.append("\n");
		}
		msg.setMessage(sb.toString());
		
		return msg;
	}

	private Message doRmdir(String command, HashMap arguments) {
		// Arguments
		// Directory : 지우고자 하는 디렉토리의 위치
		Message msg = new Message();
		msg.setCode(0);
		
		String directory = (String)arguments.get("DIRECTORY");
		
		FileInfo fileInfo = FileManager.findFile(directory);
		if (fileInfo == null) {
			msg.setMessage("Directory Not Found");
			return msg;
		}
		if (fileInfo.getFileType() != FileInfo.FileType.DIR) {
			msg.setMessage(directory + " is not a directory");
			return msg;
		}
		DirInfo containDir = fileInfo.getChildDir();
		if (containDir.fileCount() != 0) {
			msg.setMessage("Directory contains file(s)");
			return msg;
		}
		DirInfo parentDir = fileInfo.getParentDir();
		parentDir.removeDir(fileInfo.getFileName());
		
		msg.setMessage("SUCCESS");
		
		return msg;
	}

	private Message doMkdir(String command, HashMap arguments) {
		// Arguments
		// Directory : 디렉토리 위치
		// DirName: 만들고자 하는 디렉토리 이름
		Message msg = new Message();
		msg.setCode(0);
		
		String directory = (String)arguments.get("DIRECTORY");
		String dirName = (String)arguments.get("DIRNAME");
		
		System.out.println(directory);
		FileInfo fileInfo = FileManager.findFile(directory);
		if (fileInfo == null) {
			msg.setMessage("Directory Not Found");
			return msg;
		}
		if (fileInfo.getFileType() != FileInfo.FileType.DIR) {
			msg.setMessage(directory + " is not a directory");
			return msg;
		}
		DirInfo parentDir = fileInfo.getChildDir();
		if (parentDir.findFile(dirName) != null) {
			msg.setMessage(dirName + " already exist");
			return msg;
		}
		DirInfo childDir = new DirInfo();
		FileInfo newFile = new FileInfo();
		newFile.setFileName(dirName);
		newFile.setFileType(FileInfo.FileType.DIR);
		newFile.setChildDir(childDir);
		parentDir.addFile(newFile, false);
		
		msg.setMessage("SUCCESS");
		return msg;
	}

	private Message doRemove(String command, HashMap arguments) {
		// Arguments
		// Directory: 디렉토리 위치
		// FileName: 파일 이름
		Message msg = new Message();
		msg.setCode(0);
		
		String directory = (String)arguments.get("DIRECTORY");
		String fileName = (String)arguments.get("FILENAME");
		
		String filePath = directory + (directory.endsWith("/") ? "" : "/") + fileName;
		FileInfo fileInfo = FileManager.findFile(filePath);
		if (fileInfo == null) {
			msg.setMessage("File Not Found");
			return msg;
		}
		if (fileInfo.getFileType() == FileInfo.FileType.DIR) {
			msg.setMessage("Directory can not remove with this command");
			return msg;
		}
		DirInfo parentDir = fileInfo.getParentDir();
		System.out.println("Parent DIR = " + parentDir);
		System.out.println("FileInfo = " + fileInfo);
		
		parentDir.removeFile(fileInfo, fileInfo.getFileType());
		msg.setMessage("SUCCESS");

		return msg;
	}

	private Message doMove(String command, HashMap arguments) {
		// Arguments
		// Source: 원본 파일의 위치(디렉토리 포함)
		// Destination: 목적 파일의 위치(디렉토리 포함)
		Message msg = new Message();
		msg.setCode(0);
		
		String source = (String)arguments.get("SOURCE");
		String destination = (String)arguments.get("DESTINATION");
		
		FileInfo srcFileInfo = FileManager.findFile(source);
		FileInfo dstFileInfo = FileManager.findFile(destination);
		
		if (srcFileInfo == null) {
			msg.setMessage("Source File Not Found");
			return msg;
		}
		if (dstFileInfo == null) {
			int idx = destination.lastIndexOf("/");
			dstFileInfo = FileManager.findFile(destination.substring(0, idx));
			if (dstFileInfo == null) {
				msg.setMessage("Destination Directory Not Found");
				return msg;
			}
		}
		DirInfo dstDirInfo = null;
		if (dstFileInfo.getFileType() == FileInfo.FileType.DIR) {
			dstDirInfo = dstFileInfo.getChildDir();
		} else {
			dstDirInfo = dstFileInfo.getParentDir();
		}
		FileInfo newFileInfo = copyFileInfo(srcFileInfo);
		DirInfo srcDirInfo = srcFileInfo.getParentDir();
		srcDirInfo.removeFile(srcFileInfo, srcFileInfo.getFileType());
		dstDirInfo.addFile(newFileInfo, true);
		
		msg.setMessage("SUCCESS");
		
		return msg;
	}

	private Message doCopy(String command, HashMap arguments) {
		// Arguments
		// Source: 원본 파일의 위치(디렉토리 포함)
		// Destination: 타겟 파일의 위치(디렉토리 포함)
		Message msg = new Message();
		msg.setCode(0);
		
		String source = (String)arguments.get("SOURCE");
		String destination = (String)arguments.get("DESTINATION");
		
		FileInfo srcFileInfo = FileManager.findFile(source);
		FileInfo dstFileInfo = FileManager.findFile(destination);
		
		if (srcFileInfo == null) {
			msg.setMessage("Source File Not Found");
			return msg;
		}
		
		if (srcFileInfo.getFileType() == FileInfo.FileType.DIR) {
			msg.setMessage("Directory can not copy");
			return msg;
		}
		if (dstFileInfo == null) {
			int idx = destination.lastIndexOf("/");
			dstFileInfo = FileManager.findFile(destination.substring(0, idx));
			if (dstFileInfo == null) {
				msg.setMessage("Destination Directory Not Found");
				return msg;
			}
		}
		
		DirInfo dstDirInfo = null;
		if (dstFileInfo.getFileType() == FileInfo.FileType.DIR) {
			dstDirInfo = dstFileInfo.getChildDir();
		} else {
			dstDirInfo = dstFileInfo.getParentDir();
		}
		FileInfo newFileInfo = copyFileInfo(srcFileInfo);
		dstDirInfo.addFile(newFileInfo, true);
		
		msg.setMessage("SUCCESS");
		return msg;
	}
	
	private FileInfo copyFileInfo(FileInfo orgFileInfo) {
		FileInfo newFileInfo = new FileInfo();
		
		newFileInfo.setChildDir(orgFileInfo.getChildDir());
		newFileInfo.setData(copyFileData(orgFileInfo.getData()));
		newFileInfo.setFileName(orgFileInfo.getFileName());
		newFileInfo.setFileSize(orgFileInfo.getFileSize());
		newFileInfo.setFileType(orgFileInfo.getFileType());
		newFileInfo.setMount(orgFileInfo.isMount());
		newFileInfo.setParentDir(orgFileInfo.getParentDir());
		newFileInfo.setRemoteAddr(orgFileInfo.getRemoteAddr());
		newFileInfo.setRemotePort(orgFileInfo.getRemotePort());
		
		return newFileInfo;
	}
	
	private FileData copyFileData(FileData orgFileData) {
		FileData newFileData = new FileData();
		
		newFileData.setData(orgFileData.getData());
		return newFileData;
	}

	private Message doList(String command, HashMap arguments) {
		// Arguments
		// Directory: Directory 위치
		Message msg = new Message();
		msg.setCode(0);
		
		String directory = (String)arguments.get("DIRECTORY");
		
		FileInfo fileInfo = FileManager.findFile(directory);
		if (fileInfo == null) {
			msg.setMessage("Directory Not Found");
			return msg;
		}
		if (fileInfo.getFileType() != FileInfo.FileType.DIR) {
			msg.setMessage(directory + "is not a directory");
			return msg;
		}
		DirInfo dirInfo = fileInfo.getChildDir();
		Collection<FileInfo> fileList = dirInfo.getFileList();
		StringBuffer msgStr = new StringBuffer();
		for (FileInfo fInfo : fileList) {
			msgStr.append(fInfo.getFileType() == FileInfo.FileType.DIR ? "Dir" : "File");
			msgStr.append(":");
			msgStr.append(fInfo.getFileName());
			msgStr.append(":");
			msgStr.append(fInfo.getFileSize());
			msgStr.append(":");
			msgStr.append(fInfo.getRemoteAddr() == null ? "" : fInfo.getRemoteAddr());
			msgStr.append("\n");
		}
		msg.setMessage(msgStr.toString());
		msg.setCode(100);

		return msg;
	}

	private Message doInfo(String command, HashMap arguments) {
		// Arguments
		// Directory: Directory 위치
		// FileName: 보고자 하는 파일이나 디렉토리의 이름
		Message msg = new Message();
		msg.setCode(0);
		
		String directory = (String)arguments.get("DIRECTORY");
		String fileName = (String)arguments.get("FILENAME");
		
		String filePath = directory + (directory.endsWith("/") ? "" : "/") + fileName;
		FileInfo fileInfo = FileManager.findFile(filePath);
		if (fileInfo == null) {
			msg.setMessage("File Not Found");
			return msg;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(fileName); sb.append("\n");
		sb.append(fileInfo.getFileSize()); sb.append("\n");
		sb.append(fileInfo.getFileType() == FileInfo.FileType.DIR ? "DIR" : "FILE");
		sb.append("\n");
		sb.append(fileInfo.isMount() ? 
				fileInfo.getRemoteAddr() + "(" + fileInfo.getRemotePort() + ")" :
				"");
		
		msg.setMessage(sb.toString());
		
		return msg;
	}

	private Message doUnmount(String command, HashMap arguments) {
		// Arguments
		// Directory: Directory 위치
		Message msg = new Message();
		msg.setCode(0);
		
		String directory = (String)arguments.get("DIRECTORY");
		
		FileInfo fileInfo = FileManager.findFile(directory);
		if (fileInfo == null) {
			msg.setMessage("Directory Not Found");
			return msg;
		}
		if (!fileInfo.isMount()) {
			msg.setMessage(directory + "is not mounted");
			return msg;
		}
		
		fileInfo.setMount(false);
		
		msg.setMessage("SUCCESS");
		return msg;
	}

	private Message doMount(String command, HashMap arguments) {
		// Arguments
		// Directory: Directory 위치
		// RemoteAddr : 마운트할 사이트의 IP 주소
		// RemotePort : 마운트할 사이트의 포트번호
		Message msg = new Message();
		msg.setCode(0);
		
		String directory = (String)arguments.get("DIRECTORY");
		String remoteAddr = (String)arguments.get("REMOTEADDR");
		String remotePortStr = (String)arguments.get("REMOTEPORT");
		int remotePort = parseInt(remotePortStr);
		
		FileInfo fileInfo = FileManager.findFile(directory);
		if (fileInfo == null) {
			msg.setMessage("Directory Not Found");
			return msg;
		}
		if (fileInfo.getFileType() != FileInfo.FileType.DIR) {
			msg.setMessage(directory + " is not a directory");
			return msg;
		}
		if (fileInfo.isMount()) {
			msg.setMessage("Already mounted on " + 
					       fileInfo.getRemoteAddr() + 
					       "(" + fileInfo.getRemotePort() + ")");
			return msg;
		}
		fileInfo.setMount(true);
		fileInfo.setRemoteAddr(remoteAddr);
		fileInfo.setRemotePort(remotePort);
		
		msg.setMessage("SUCCESS");		

		return msg;
	}
	
	private int parseInt(String numStr) {
		int num = 0;
		try {
			num = Integer.parseInt(numStr);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return num;
	}

	private Message doDownload(String command, HashMap arguments) {
		// Arguments
		// Directory: Directory 위치
		// FileName: 파일 이름
		// Encoding : 인코딩 유무(base64, none)
		Message msg = new Message();
		msg.setCode(0);
		
		String directory = (String)arguments.get("DIRECTORY");
		String fileName = (String)arguments.get("FILENAME");
		String encoding = (String)arguments.get("ENCODING");
		
		String filePath = directory + (directory.endsWith("/") ? "" : "/") + fileName;
		FileInfo fileInfo = FileManager.findFile(filePath);
		if (fileInfo == null) {
			msg.setMessage("File Not Found");
			return msg;
		}
		if (fileInfo.getFileType() != FileInfo.FileType.FILE) {
			msg.setMessage(filePath + " Directory can not downloaded");
			return msg;
		}
		FileData fileData = fileInfo.getData();
		msg.setMessage(fileName + "\n" + 
						encoding + "\n" +
				(encoding.equalsIgnoreCase("base64") ? fileData.getBase64Data() : 
													   new String(fileData.getData()))
	                  );		
		msg.setCode(100);
		return msg;
	}

	private Message doUpload(String command, HashMap arguments) {
		// Arguments
		// Directory : 해당 파일을 올리려고 하는 디렉토리 위치
		// FileName : 올리려는 파일의 이름
		// FileContent : 파일 내용
		// Encoding : base64, none
		Message msg = new Message();
		msg.setCode(0);
		
		String directory = (String)arguments.get("DIRECTORY");
		String fileName = (String)arguments.get("FILENAME");
		String fileContent = (String)arguments.get("FILECONTENT");
		String encoding = (String)arguments.get("ENCODING");
		
		FileInfo fileDir = FileManager.findFile(directory);
		if (fileDir == null) {
			msg.setMessage("DIRECTORY NOT FOUND");
			return msg;
		}
		
		FileInfo fileInfo = new FileInfo();
		fileInfo.setFileName(fileName);
		FileData fileData = new FileData();
		if (encoding.equalsIgnoreCase("base64"))
			fileData.setBase64Data(fileContent);
		else
			fileData.setData(fileContent.getBytes());
		fileInfo.setData(fileData);
		fileInfo.setFileSize(fileData.getFileSize());
		fileInfo.setFileType(FileInfo.FileType.FILE);
		fileDir.getChildDir().addFile(fileInfo, true);
		
		msg.setMessage("SUCCESS");
		msg.setCode(100);
		return msg;
	}

	public HashMap stopService() {
		return null;
	}

	public void resumeService(HashMap data) {
	}

	public void setArguments(HashMap data) {
		super.setArguments(data);
		
		rootDir = ConfigManager.parseEnvTag((String)arguments.get("root"));
		System.out.println("root = " + rootDir);
		
		DirInfo dirInfo = FileManager.loadData(rootDir);
		if (dirInfo == null) dirInfo = new DirInfo();
		FileManager.setRootDir(dirInfo);
		
		FileData fileData = new FileData();
		fileData.setData("Sample Text".getBytes());
		
		FileInfo tmpUser = new FileInfo();
		tmpUser.setFileName("sample");
		tmpUser.setFileType(FileInfo.FileType.FILE);
		tmpUser.setData(fileData);
		dirInfo.addFile(tmpUser, true);
		
		DirInfo childDir = new DirInfo();
		FileInfo tmpVar = new FileInfo();
		tmpVar.setFileName("var");
		tmpVar.setFileType(FileInfo.FileType.DIR);
		tmpVar.setChildDir(childDir);
		dirInfo.addFile(tmpVar, false);
		
		DirInfo grandChildDir = new DirInfo();
		FileInfo tmpLog = new FileInfo();
		tmpLog.setFileName("log");
		tmpLog.setFileSize(100);
		tmpLog.setFileType(FileInfo.FileType.DIR);
		tmpLog.setChildDir(grandChildDir);
		childDir.addFile(tmpLog, false);
		
		HashMap<String, String> hashData = new HashMap<String, String>();
		hashData.put("DIRECTORY", "/var/log");
		hashData.put("FILENAME", "ftp_babo");
		hashData.put("FILECONTENT", "Log fNew Data\n");
		hashData.put("ENCODING", "none");
		doUpload("upload", hashData);
		
		FileInfo tmpRoot = FileManager.findFile("/var/log/ftp_babo");
		System.out.println("ROOT FILE / = " + tmpRoot.getFileName() + ", size = " + tmpRoot.getFileSize());
		
		FileManager.saveData(rootDir, dirInfo);
	}
}
