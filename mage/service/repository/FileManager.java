/*
 * FileManager
 * - Virtual File Manager.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.StringTokenizer;

public class FileManager {
	private static DirInfo rootDir = null;
	private static FileInfo rootFileInfo = new FileInfo();
	private static DirInfo dirRep = new DirInfo();
	
	private static void setRootFile(DirInfo rootDir) {
		rootFileInfo.setFileName("/");
		rootFileInfo.setChildDir(rootDir);
		rootFileInfo.setFileType(FileInfo.FileType.DIR);
		dirRep.addFile(rootFileInfo, true);
	}
	
	public static String getPath(String name) {
		int idx = name.lastIndexOf(File.separator);
		return name.substring(0, idx);
	}
	
	public static void checkDirectory(String path) {
		File file = new File(path);
		if (file.exists()) return;
		file.mkdirs();
		return;
	}
	
	public static DirInfo loadData(String path) {
		DirInfo dirInfo = null;
		
		checkDirectory(getPath(path));
		try {
			FileInputStream fin = new FileInputStream(path);
			ObjectInputStream is = new ObjectInputStream(fin);
			dirInfo = (DirInfo)is.readObject();
			setRootFile(dirInfo);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return dirInfo;
	}
	
	public static boolean saveData(String path, DirInfo dirInfo) {
		boolean result = true;

		checkDirectory(getPath(path));		
		try {
			FileOutputStream fout = new FileOutputStream(path);
			ObjectOutputStream os = new ObjectOutputStream(fout);
			os.writeObject(dirInfo);
			os.close();
		} catch (IOException e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}

	public static DirInfo getRootDir() {
		return rootDir;
	}

	public static void setRootDir(DirInfo rootDir) {
		FileManager.rootDir = rootDir;
		setRootFile(rootDir); 
	}
	
	public static FileInfo findFile(String path) {
		DirInfo dirInfo = null;
		FileInfo fileInfo = null;
		StringTokenizer st = new StringTokenizer(path, "/", false);
		String sepPath;
		
		dirInfo = rootDir;
		while (st.hasMoreTokens()) {
			sepPath = st.nextToken();
			System.out.println("PATH = " + sepPath);
			fileInfo = dirInfo.findFile(sepPath);
			if (fileInfo == null) break;
			if (fileInfo.getFileType() != FileInfo.FileType.DIR) {
				break;
			}
			dirInfo = fileInfo.getChildDir();
		}
		if (st.hasMoreTokens()) return null;
		if (path.equals("/")) fileInfo = rootFileInfo;
		return fileInfo;
	}
}
