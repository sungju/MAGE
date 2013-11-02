package mage.service.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

public class DirInfo implements Serializable {
	private static final long serialVersionUID = 1331823183813955520L;
	
	private HashMap<String, FileInfo> fileList = new HashMap<String, FileInfo>(); // 현재 디렉토리의 내용들을 관리함
	private DirInfo parent = null; // 부모 디렉토리를 대변함
	
	public DirInfo() {
		this(null);
	}
	
	public DirInfo(DirInfo parent) {
		setParent(parent);
	}
	
	public DirInfo getParent() {
		return parent;
	}
	
	public void setParent(DirInfo parent) {
		this.parent = parent;
	}
	
	public boolean addFile(FileInfo fileInfo, boolean force) {
		if (fileList.get(fileInfo.getFileName()) != null) {
			if (!force) return false;
			removeFile(fileInfo, FileInfo.FileType.FILE);
		}
//		if (fileInfo.getFileType() == FileInfo.FileType.DIR)
		fileInfo.setParentDir(this);
	
		fileList.put(fileInfo.getFileName(), fileInfo);
		return true;
	}
	
	public boolean removeFile(FileInfo fileInfo, FileInfo.FileType fileType) {
		FileInfo fi = fileList.get(fileInfo.getFileName());		
		if (fi == null || fi.getFileType() != fileType) return false;
		
		fileList.remove(fileInfo.getFileName());
		return true;
	}
	
	public FileInfo findFile(String fileName) {
		return fileList.get(fileName);
	}
	
	public DirInfo makeDir(String name) {
		DirInfo dirInfo = new DirInfo(this);
		FileInfo newEntry = new FileInfo();
		newEntry.setFileName(name);
		newEntry.setChildDir(dirInfo);
		newEntry.setFileType(FileInfo.FileType.DIR);
		if (addFile(newEntry, false) == false) return null;
		
		return dirInfo;
	}
	
	public DirInfo removeDir(String name) {
		DirInfo dirInfo = null;
		FileInfo fileEntry = fileList.get(name);
		if (fileEntry == null) return null;
		if (fileEntry.getFileType() != FileInfo.FileType.DIR) return null;
		dirInfo = fileEntry.getChildDir();
		if (dirInfo.fileCount() != 0) return null;
		removeFile(fileEntry, FileInfo.FileType.DIR);

		return dirInfo;
	}
	
	public int fileCount() {
		return fileList.size();
	}
	
	public Collection<FileInfo> getFileList() {
		return fileList.values();
	}
	
	public FileInfo mountDir(String name, String addr, int port) {
		FileInfo fileEntry = fileList.get(name);
		if (fileEntry == null) return null;
		if (fileEntry.getFileType() != FileInfo.FileType.DIR) return null;
		if (fileEntry.isMount()) return null;
		fileEntry.setMount(true);
		fileEntry.setRemoteAddr(addr);
		fileEntry.setRemotePort(port);
		
		return fileEntry;
	}
	
	public FileInfo unmountDir(String name) {
		FileInfo fileEntry = fileList.get(name);
		if (fileEntry == null) return null;
		if (fileEntry.getFileType() != FileInfo.FileType.DIR) return null;
		if (!fileEntry.isMount()) return null;
		fileEntry.setRemoteAddr(null);
		fileEntry.setRemotePort(0);
		fileEntry.setMount(false);
		
		return fileEntry;
	}
	
	public String toString() {
		return fileList.toString();
	}
}
