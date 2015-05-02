package main.server;

import javax.crypto.SealedObject;



public class Servers {
	protected String serverName;
	protected String serverIp;

	protected SealedObject receivedPayLoad;
	protected SealedObject transmitPayLoad;

	/*Getters Setters*/
	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	public SealedObject getReceivedPayLoad() {
		return receivedPayLoad;
	}

	public void setReceivedPayLoad(SealedObject receivedPayLoad) {
		this.receivedPayLoad = receivedPayLoad;
	}

	public SealedObject getTransmitPayLoad() {
		return transmitPayLoad;
	}

	public void setTransmitPayLoad(SealedObject transmitPayLoad) {
		this.transmitPayLoad = transmitPayLoad;
	}

	/*Methods*/

	
	public void startServer(){

	}

	public void stopServer(){

	}

	public void listen(){

	}

	public void receivePayLoad(){
		
	}

}
