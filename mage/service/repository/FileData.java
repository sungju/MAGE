/*
 * FileData
 * - File Content.
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

import mage.utils.Base64;

public class FileData implements Serializable {
	private static final long serialVersionUID = -3837790518175945675L;
	
	private byte[] data = null;

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public String getBase64Data() {
		return Base64.encode(data);
	}
	
	public void setBase64Data(String data) {
		setData(Base64.decode(data));
	}
	
	public int getFileSize() {
		return data.length;
	}
	
	public String toString() {
		return new String(data);
	}
}
