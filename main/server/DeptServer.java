package main.server;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

import main.client.StartupClient;
import main.communication.Communication;
import main.exception.FileNotPresentException;
import main.exception.NotValidPacketException;
import main.exception.PacketNotFreshException;
import main.utilities.FunctionUtilities;
import main.utilities.SessionIdentifierGenerator;
import main.utilities.Ticket;
import main.utilities.VarriableNames;

public class DeptServer extends Servers {

	Communication communication;
	private String deptServerName = VarriableNames.SERVER_NAME;
	private int deptServValue;
	private int deptServerPort;
	private String clientName;
	private String fileName;
	private String sessionKeyEntDept;
	private String sessionKeyClientDept;
	private Timestamp timeStampAuthServer;


	//Create the Ticket between the Department and Client
	private SealedObject generateSealedObjectClientDept() throws Exception{
		TreeMap<String, Object> map = new TreeMap<String, Object>();
		//Requires a File Path and Name to insert
		map.put(VarriableNames.MAP_KEY_FILE, retrieveFile(fileName));
		map.put(VarriableNames.MAP_KEY_TIMESTAMP, timeStampAuthServer);
		return Ticket.sealObject(map, sessionKeyClientDept);
	}
	//Create the Ticket between the Enterprise and Department Servers
	private SealedObject generateSealedObjectDeptEnt() throws Exception{
		TreeMap<String, Object> map = new TreeMap<String, Object>();
		map.put(VarriableNames.CLIENT_NAME, clientName);
		map.put(VarriableNames.MAP_KEY_DEPT_NAME, deptServerName);
		//Ticket between Client and Dept
		SealedObject sObject = generateSealedObjectClientDept();
		map.put(VarriableNames.MAP_KEY_TICKET, sObject);
		Date date = new Date();
		Timestamp timestampDeptServer = new Timestamp(date.getTime());
		map.put(VarriableNames.MAP_KEY_TIMESTAMP, timestampDeptServer);		
		return Ticket.sealObject(map, sessionKeyEntDept);
	}
	//Retrieves file from Departments set folder
	//Must be in byte array
	public byte[] retrieveFile(String fileName) throws Exception{
		
		String filePath = "";
		try{
			switch(this.deptServValue){
			case 1: filePath = VarriableNames.DIRECTORY_DEP_01; 
				break;
			case 2: filePath = VarriableNames.DIRECTORY_DEP_02; 
				break;
			case 3: filePath = VarriableNames.DIRECTORY_DEP_03; 
				break;
			}
			System.out.println("******************");
			System.out.println(filePath+fileName);
			System.out.println("******************");
			File file = new File(filePath + fileName);
			byte[] contentsOfFile = Files.readAllBytes(file.toPath());
			return contentsOfFile;
		}catch(Exception ex){
			throw new FileNotPresentException();
		}
	}

	public void processRequestFromEnt() throws Exception {
		System.out.println("Waiting for packet from Ent.....");
		Object packetReceived = null;
		packetReceived = communication.retrivePacket();
		if(packetReceived != null){
			System.out.println("Received Packet from Ent Server");
			System.out.println("Packet is :" + packetReceived.toString());
		}
		if(packetReceived instanceof Exception){
			Exception exception = (Exception) packetReceived;
			System.out.println("##############################");
			System.out.println("Dept Server::" +exception.getMessage());
			System.out.println("##############################");
			throw exception;
		}

		TreeMap<String,Object> mapEntDept = null;
		if(packetReceived instanceof TreeMap){
			mapEntDept =  (TreeMap<String, Object>) packetReceived;
		}
		System.out.println("Dept Server received object from Ent Server");

		//Begin retrieving tickets from map 
		SealedObject ticketAD = (SealedObject) mapEntDept.get(VarriableNames.MAP_KEY_UNSEAL_TICKET_1);
		SealedObject ticketED = (SealedObject) mapEntDept.get(VarriableNames.MAP_KEY_UNSEAL_TICKET_2);

		//Begin unseal tickets 
		//Unseal first ticket
		System.out.println("Unsealing Packet Auth Department");
		TreeMap<String, Object> unsealedMapAD = null;
		try{
			unsealedMapAD = Ticket.unsealObject(ticketAD, VarriableNames.SECRET_KEY_AUTH_DEPT_01);
		}catch(Exception ex){
			throw new NotValidPacketException();
		}
		this.clientName = (String) unsealedMapAD.get(VarriableNames.MAP_KEY_CLIENT_NAME);
		this.sessionKeyEntDept = (String) unsealedMapAD.get(VarriableNames.MAP_KEY_SESSION_KEY_ENT_DEPT);
		this.sessionKeyClientDept = (String) unsealedMapAD.get(VarriableNames.MAP_KEY_SESSION_KEY_CLIENT_DEPT);
		this.timeStampAuthServer = (Timestamp) unsealedMapAD.get(VarriableNames.MAP_KEY_TIMESTAMP);
		System.out.println("Client :" + clientName);
		System.out.println("kED :" + sessionKeyEntDept);
		System.out.println("kCD : " + sessionKeyClientDept);
		System.out.println("Timestamp" + timeStampAuthServer);

		if(!(FunctionUtilities.checkFreshness(timeStampAuthServer, VarriableNames.ALLOWED_DELAY_AUTH_DEPT))){
			throw new Exception("Exception:: Ticket from Auth server not fresh at Dept server");
		}

		//Unseal Second ticket Ent-Dept
		System.out.println("Unsealing Second Ticket Ent-Dept");
		TreeMap<String, Object> unsealedMapED = null;
		try{
			unsealedMapED = Ticket.unsealObject(ticketED, sessionKeyEntDept);
		}catch(Exception ex){
			throw new NotValidPacketException();
		}		
		String entName = (String) unsealedMapED.get(VarriableNames.MAP_KEY_ENT_NAME);
		SealedObject ticketKCD = (SealedObject) unsealedMapED.get(VarriableNames.MAP_KEY_TICKET);
		Timestamp timeStampEnt = (Timestamp) unsealedMapAD.get(VarriableNames.MAP_KEY_TIMESTAMP);
		System.out.println("entName : " + entName);
		System.out.println("Ent timestamp : " + timeStampEnt);
		System.out.println("Sealed Ticket :" + ticketKCD);

		//Unseal ticket Client-Dept
		TreeMap<String, Object> unsealedMapCD = null;
		try{
			unsealedMapCD = Ticket.unsealObject(ticketKCD, sessionKeyClientDept);
		}catch(Exception ex){
			throw new NotValidPacketException();
		}				
		this.fileName = (String) unsealedMapCD.get(VarriableNames.MAP_KEY_FILENAME);
		Timestamp timeStampClient = (Timestamp) unsealedMapCD.get(VarriableNames.MAP_KEY_TIMESTAMP);
		System.out.println("FileName : " + fileName);
		System.out.println("Client timestamp : " + timeStampClient);

		if(!(FunctionUtilities.checkFreshness(timeStampClient, VarriableNames.ALLOWED_DELAY_CLIENT_DEPT))){
			throw new Exception("Exception:: Ticket from Client not fresh at Dept server");			
		}		
	}

	private void sendResponse(SealedObject sealedObject) throws Exception {
		System.out.println("Dept Server sending Packet");
		communication.send(sealedObject);
	}

	public DeptServer() throws Exception {
		try{
			Scanner in = new Scanner(System.in);
			// Initialize the Server
			communication = Communication.getInstance();
			System.out.println("Enter department number :> ");
			int input = (int) in.nextInt();
			//Sets the port for the department
			switch(input){
				case 1: deptServerPort = VarriableNames.PORT_DEPT_SERVER1;
					break;
				case 2: deptServerPort = VarriableNames.PORT_DEPT_SERVER2;
					break;
				case 3: deptServerPort = VarriableNames.PORT_DEPT_SERVER3;
					break;
			}
			this.deptServValue = input;
			
			communication.serverInitialize(deptServerPort);
			//process packet from Ent
			processRequestFromEnt(); 
			//Generate packet for Ent Server
			SealedObject packetDeptEnt = generateSealedObjectDeptEnt();
			//Send Response to Ent
			sendResponse(packetDeptEnt);
		}catch(Exception ex){
			System.out.println(ex.getMessage());
			communication.sendError(ex);
		}
	}
}
