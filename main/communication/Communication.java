package main.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.TreeMap;

import javax.crypto.SealedObject;
public class Communication {
	private static Communication instance = null;
	private int portNumber;
	private Socket clientSocket;
	private PrintWriter sendStream;
	private ServerSocket serverSocket;
	private ObjectInputStream retrieveStream;

	public static Communication getInstance() {
		if(instance == null) {
			instance = new Communication();
		}
		return instance;
	}

	public Communication(){
	}

	public void serverInitialize(int portNumber) throws Exception{
		System.out.println("Server initializing");
		serverSocket = new ServerSocket(portNumber);
		clientSocket = serverSocket.accept();

	}
	public void clientInitialize(String serverName, int portNumber) throws Exception{
		System.out.println("Client initializing");
		clientSocket = new Socket(serverName,portNumber);
	}

	public void send(SealedObject sealedObject) throws Exception{
		System.out.println("sending 1 sealed Object");
		OutputStream os= clientSocket.getOutputStream();
		ObjectOutputStream objos = new ObjectOutputStream(os);
		objos.writeObject(sealedObject);
	}
	public void sendTickets(TreeMap<String,Object> map) throws Exception{
		System.out.println("sending 1 map containg 2 sealded object");
		OutputStream os= clientSocket.getOutputStream();
		ObjectOutputStream objos = new ObjectOutputStream(os);
		objos.writeObject(map);
	}
	public void sendError(Exception ex) throws Exception{
		System.out.println("sending Exception");
		OutputStream os= clientSocket.getOutputStream();
		ObjectOutputStream objos = new ObjectOutputStream(os);
		objos.writeObject(ex);
	}


	public SealedObject retrive() throws Exception{
		System.out.println("receiving");
		InputStream is = clientSocket.getInputStream();
		retrieveStream = new ObjectInputStream(is);
		SealedObject retrievedObject = (SealedObject) retrieveStream.readObject();
		return retrievedObject;
	}
	public Object retrivePacket() throws Exception{
		System.out.println("receiving");
		InputStream is = clientSocket.getInputStream();
		retrieveStream = new ObjectInputStream(is);
		Object retrievedObject = (Object) retrieveStream.readObject();
		return retrievedObject;
	}

	public TreeMap<String,Object> receiveTickets() throws Exception{
		System.out.println("receiving");
		InputStream is = clientSocket.getInputStream();
		retrieveStream = new ObjectInputStream(is);
		TreeMap<String,Object> retrievedObject = (TreeMap<String,Object>) retrieveStream.readObject();
		return retrievedObject;
	}


	public void terminate() throws Exception{
		System.out.println("Connection closing");
		clientSocket.close();
	}
}
