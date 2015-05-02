package main.client;

import java.io.*;
import java.nio.file.Files;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Scanner;
import java.util.TreeMap;

import javax.crypto.SealedObject;

import main.communication.Communication;
import main.exception.NotValidPacketException;
import main.exception.PacketNotFreshException;
import main.server.AuthServer;
import main.utilities.FunctionUtilities;
import main.utilities.Ticket;
import main.utilities.VarriableNames;

public class StartupClient {
	private String kEC ;
	private String kCD ;
	private SealedObject sealedObjectEntAuth;
	private static String fileName;
	private static Communication communication;
	private static String deptName;
	private static int departmentValue;


	private SealedObject generateSealedObjectClientAuth()throws Exception{
		System.out.println("Genrating Packet for Client to Auth");
		TreeMap<String, Object> map = new TreeMap<String, Object>();
		map.put(VarriableNames.MAP_KEY_CLIENT_NAME,VarriableNames.CLIENT_NAME);
		map.put(VarriableNames.MAP_KEY_DEPT_NAME,deptName);
		//Directory static value being sent to Auth
		map.put(VarriableNames.MAP_DIRECTORY_VALUE,departmentValue);
		map.put(VarriableNames.MAP_KEY_TIMESTAMP, FunctionUtilities.findCurrentTimeStamp());	
		return Ticket.sealObject(map, VarriableNames.SECRET_KEY_AUTH_CLIENT);
	}

	private TreeMap<String, Object> generateMapTreeClientEnt(String fileName) throws Exception{
		System.out.println("Genrating Packet for Client to Ent");
		TreeMap<String, Object> map = new TreeMap<String, Object>();
		map.put(VarriableNames.MAP_KEY_UNSEAL_TICKET_1, sealedObjectEntAuth);
		map.put(VarriableNames.MAP_KEY_UNSEAL_TICKET_2, generateSealedObjectsClientDeptEnt(fileName));
		return map;
	}
	private SealedObject generateSealedObjectsClientDeptEnt(String fileName) throws Exception{
		System.out.println("Genrating Ticket for Client to Dept");
		//Inner Ticket
		TreeMap<String, Object> inner_map = new TreeMap<String, Object>();
		inner_map.put(VarriableNames.MAP_KEY_FILENAME, fileName);
		inner_map.put(VarriableNames.MAP_KEY_TIMESTAMP, FunctionUtilities.findCurrentTimeStamp());
		SealedObject ticketClientDept = Ticket.sealObject(inner_map, kCD);
		//Outer Ticket
		System.out.println("Genrating Ticket02 for Client to Ent");
		TreeMap<String, Object> outer_map = new TreeMap<String, Object>();
		outer_map.put(VarriableNames.MAP_KEY_TICKET, ticketClientDept);
		outer_map.put(VarriableNames.MAP_KEY_TIMESTAMP, FunctionUtilities.findCurrentTimeStamp());
		SealedObject sObject = Ticket.sealObject(outer_map, kEC);
		return sObject;
	}
	private void recieveFromAuth() throws Exception{

		Object packetReceived = null;
		packetReceived = communication.retrivePacket();
		if(packetReceived != null){
			System.out.println("Received Packet from Auth Server");
			System.out.println("Packet is :" + packetReceived.toString());
		}
		if(packetReceived instanceof Exception){
			Exception exception = (Exception) packetReceived;
			System.out.println("##############################");
			System.out.println("Auth Server::" +exception.getMessage());
			System.out.println("##############################");
			communication.terminate();
			throw exception;
		}
		SealedObject ticketClientAuth = null;
		if(packetReceived instanceof SealedObject){
			ticketClientAuth = (SealedObject) packetReceived;
		}

		//Closing the pipe with Server
		communication.terminate();

		//Processing the Sealed Object
		System.out.println("Unsealing the Packet from Auth");
		TreeMap<String, Object> unsealedMapAuthClient = null;
		try{
			unsealedMapAuthClient = (TreeMap<String, Object>) Ticket.unsealObject(ticketClientAuth, VarriableNames.SECRET_KEY_AUTH_CLIENT);
		}catch(Exception ex){
			communication.terminate();
			throw new NotValidPacketException();
		}		
		this.kEC = (String) unsealedMapAuthClient.get(VarriableNames.MAP_KEY_SESSION_KEY_CLIENT_ENT);
		this.kCD = (String) unsealedMapAuthClient.get(VarriableNames.MAP_KEY_SESSION_KEY_CLIENT_DEPT);
		this.sealedObjectEntAuth = (SealedObject) unsealedMapAuthClient.get(VarriableNames.MAP_KEY_TICKET);
		Timestamp timeStampServer = (Timestamp) unsealedMapAuthClient.get(VarriableNames.MAP_KEY_TIMESTAMP);
		System.out.println("Packet::kEC:" + kEC);
		System.out.println("Packet::kCD:" + kCD);
		System.out.println("Packet::sealedObjectEntAuth:" + sealedObjectEntAuth);
		System.out.println("Packet::Server Time:"+timeStampServer);

		if (!(FunctionUtilities.checkFreshness(timeStampServer, VarriableNames.ALLOWED_DELAY_AUTH_CLIENT))) {
			throw new Exception("Exception :: Packet received from Auth Server is not fresh!!!");
		}
	}
	//
	private void initializeClient() throws Exception{
		System.out.println("++++++++++++++++++++++");
		System.out.println();
		communication = Communication.getInstance();
		communication.clientInitialize(VarriableNames.SERVER_NAME,VarriableNames.PORT_AUTH_SERVER);
		System.out.println("client initialized");
	}
	private void sendToEnt(TreeMap<String, Object> map) throws Exception{
		//Sending Packet to the Department
		System.out.println("Sending packet Client to Ent");
		communication.clientInitialize(VarriableNames.ENT_NAME , VarriableNames.PORT_ENT_SERVER);
		communication.sendTickets(map);
	}
	

	private byte[] recieveFromEnt() throws Exception{

		Object packetReceived = null;
		packetReceived = communication.retrivePacket();
		if(packetReceived != null){
			System.out.println("Received Packet from Ent Server");
			System.out.println("Packet is :" + packetReceived.toString());
		}
		if(packetReceived instanceof Exception){
			Exception exception = (Exception) packetReceived;
			System.out.println("##############################");
			System.out.println("Ent Server::" +exception.getMessage());
			System.out.println("##############################");
			//Closing the pipe with Ent Server
			communication.terminate();
			throw exception;
		}
		SealedObject packetFromEnt = null;
		if(packetReceived instanceof SealedObject){
			packetFromEnt = (SealedObject) packetReceived;
		}
		
		//Unseal Packet from Ent Server
		TreeMap<String, Object> unsealedMapEntDept = null;
		try{
			unsealedMapEntDept = (TreeMap<String, Object>) Ticket.unsealObject(packetFromEnt, kEC);
		}catch(Exception ex){
			communication.terminate();
			throw new NotValidPacketException();
		}
		SealedObject ticketDeptClient = (SealedObject) unsealedMapEntDept.get(VarriableNames.MAP_KEY_TICKET);
		Timestamp timestamp = (Timestamp) unsealedMapEntDept.get(VarriableNames.MAP_KEY_TIMESTAMP);
		System.out.println("Sealed Ticket :" + ticketDeptClient );
		System.out.println("Ent Server Timestamp : " + timestamp);

		//Unseal Packet from Dept Server
		TreeMap<String, Object> unsealedMapDeptClient = null;
		try{
			unsealedMapDeptClient = (TreeMap<String, Object>) Ticket.unsealObject(ticketDeptClient, kCD);
		}catch(Exception ex){
			communication.terminate();
			throw new NotValidPacketException();
		}
		byte[] file = (byte[]) unsealedMapDeptClient.get(VarriableNames.MAP_KEY_FILE);
		Timestamp timestampAuth = (Timestamp) unsealedMapDeptClient.get(VarriableNames.MAP_KEY_TIMESTAMP);
		
		if (!(FunctionUtilities.checkFreshness(timestampAuth, VarriableNames.ALLOWED_DELAY_TOTAL))) {
			//Closing the pipe with Ent Server
			communication.terminate();
			throw new Exception("Exception :: Packet received from Ent Server is not fresh!!!");
		}
		
		//Closing the pipe with Ent Server
		communication.terminate();
		return file;
	}
	private void saveFile(byte[] file) throws Exception{
		File fileLoc = new File(VarriableNames.DIRECTORY_CLIENT + fileName);
		Files.write(fileLoc.toPath(), file);
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Client Window");
		//Take input from Command Line
		//File Name
		fileName = null;
		Scanner in = new Scanner(System.in);
		while(fileName == null || fileName.equals("")){
			System.out.println("Input a file name :> ");
			fileName = in.nextLine();
		}

		//Dept Name
		String deptNumInput = null;
		int deptNum = 0;
		while( deptNumInput == null || deptNumInput.equals("")){
			System.out.println("Input a Department Number(1.CS 2.CE 3.EE) :>");
			deptNumInput = in.nextLine();
			try{
				deptNum = Integer.parseInt(deptNumInput);
				if (deptNum == 1 || deptNum == 2 ||deptNum == 3 ){
					break;
				}else{
					System.out.println("Only input 1, 2, 3 is allowed. Try Again.");
					deptNumInput = null;
				}
			}catch(NumberFormatException ne){
				System.out.println("Only input 1, 2, 3 is allowed. Try Again.");
			}
			deptNumInput = null;
		}
		departmentValue = deptNum;
		switch (deptNum) {
		case 1:  deptName = VarriableNames.DEPT_NAME_01;
		break;
		case 2:  deptName = VarriableNames.DEPT_NAME_02;
		break;
		case 3:  deptName = VarriableNames.DEPT_NAME_03;
		break;
		}
		
		
		StartupClient startupClient = new StartupClient();
		//Initialize the client
		try{
			startupClient.initializeClient();
			//Creating Packet for Auth Server
			SealedObject sealedObject = startupClient.generateSealedObjectClientAuth();
			//Sending Packet to Auth Server
			communication.send(sealedObject);
			//Receive Packet from Auth Server
			startupClient.recieveFromAuth();


			//Creating Packet for Ent Server
			TreeMap<String, Object> map = startupClient.generateMapTreeClientEnt(fileName);

			//Send the ticket to Ent Server
			startupClient.sendToEnt(map);
			
			//receive Packet from Ent Server
			byte[] file = startupClient.recieveFromEnt();
			
			//save file 
			startupClient.saveFile(file);
			
		}catch(Exception ex){
			System.out.println(ex.getMessage());
//			throw ex;
		}


	}

}
