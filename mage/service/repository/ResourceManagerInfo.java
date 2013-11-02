package mage.service.repository;

public class ResourceManagerInfo {
	private String address;
	private int port;
	private String queryType;
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getQueryType() {
		return queryType;
	}
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	public boolean equals(Object rmiObj) {
		ResourceManagerInfo rmi = (ResourceManagerInfo)rmiObj;
		return (rmi.getAddress().equals(getAddress()) && rmi.getPort() == getPort()); 
	}
}
