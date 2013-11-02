/*
 * FileInfo
 * - File attribute class.
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

import java.io.Serializable;

public class FileInfo implements Serializable {
	private static final long serialVersionUID = 2131456847864082529L;
	
	public enum FileType { NONE, FILE, DIR };
	
	private String fileName = null; // 파일 이름
	private FileType fileType = FileType.NONE; // 파일의 종류
	private int fileSize = 0; // 포함하는 파일의 크기
	private FileData data = null; // 파일 내용
	private String remoteAddr = null; // Mount 파일의 경우, IP 주소
	private int remotePort = 0; // Mount 파일의 경우, Port
	private boolean mount = false; // Mount 되어 있는 경우 true
	private DirInfo childDir = null; // 가리키고 있는 자식 노드
	private DirInfo parentDir = null; // 부모 디렉토리
	
	public DirInfo getParentDir() {
		return parentDir;
	}
	public void setParentDir(DirInfo parentDir) {
		this.parentDir = parentDir;
	}
	public FileData getData() {
		return data;
	}
	public void setData(FileData data) {
		this.data = data;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public int getFileSize() {
		return fileSize;
	}
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	public FileType getFileType() {
		return fileType;
	}
	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}
	public String getRemoteAddr() {
		return remoteAddr;
	}
	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}
	public int getRemotePort() {
		return remotePort;
	}
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}
	public DirInfo getChildDir() {
		return childDir;
	}
	public void setChildDir(DirInfo childDir) {
		this.childDir = childDir;
	}
	public boolean isMount() {
		return mount;
	}
	public void setMount(boolean mount) {
		this.mount = mount;
	}
	
	public String toString() {
		return fileName + "(" + fileSize + ") = " + data;
	}
}
