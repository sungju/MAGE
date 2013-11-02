package mage.rm;

public class WorkNodeInfo {
	private String address;
	private int port;
	private String os;
	private String speed;
	private int memTotal;
	private int memFree;
	private int cpuUsage;
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getCpuUsage() {
		return cpuUsage;
	}
	public void setCpuUsage(int cpuUsage) {
		this.cpuUsage = cpuUsage;
	}
	public int getMemFree() {
		return memFree;
	}
	public void setMemFree(int memFree) {
		this.memFree = memFree;
	}
	public int getMemTotal() {
		return memTotal;
	}
	public void setMemTotal(int memTotal) {
		this.memTotal = memTotal;
	}
	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getSpeed() {
		return speed;
	}
	public void setSpeed(String speed) {
		this.speed = speed;
	}
	
}
