package main.server;

import java.sql.Timestamp;
import java.util.Date;
import java.util.TreeMap;

import javax.crypto.SealedObject;

import main.communication.Communication;
import main.exception.NotValidPacketException;
import main.exception.PacketNotFreshException;
import main.utilities.FunctionUtilities;
import main.utilities.Ticket;
import main.utilities.VarriableNames;

public class EntServer extends Servers{

	Communication communicationClient;
	private String clientName;
	private String deptName;
	private int departmentValue;
	private String entName= VarriableNames.ENT_NAME;	
	private String sessionKeyEntDept;	
	private String sessionKeyEntClient;
	private SealedObject ticketClientDept;
	private SealedObject ticketAuthDept;

	private TreeMap<String, Object> generatePacketEntDept(SealedObject ticketAuthDept,SealedObject ticketClientDept) throws Exception{
		TreeMap<String, Object> map = new TreeMap<String, Object>();
		map.put(VarriableNames.MAP_KEY_UNSEAL_TICKET_1, ticketAuthDept);
		map.put(VarriableNames.MAP_KEY_UNSEAL_TICKET_2, generateSealedObjectEntDept(ticketClientDept));
		return map;
	}
	private SealedObject generateSealedObjectEntDept(SealedObject ticketClientDept) throws Exception{
		//Inner Ticket
		TreeMap<String, Object> inner_map = new TreeMap<String, Object>();
		inner_map.put(VarriableNames.MAP_KEY_ENT_NAME, entName);
		inner_map.put(VarriableNames.MAP_KEY_TICKET, ticketClientDept);
		inner_map.put(VarriableNames.MAP_KEY_TIMESTAMP, FunctionUtilities.findCurrentTimeStamp());	
		SealedObject ticketEntDept = Ticket.sealObject(inner_map, sessionKeyEntDept);
		return ticketEntDept;
	}

	public void processRequestfromClient() throws Exception{
		System.out.println("waiting for packet from Client.....");
		Object packetReceived = null;
		packetReceived = communicationClient.retrivePacket();
		if(packetReceived != null){
			System.out.println("Received Packet from Client");
			System.out.println("Packet is :" + packetReceived.toString());
		}
		if(packetReceived instanceof Exception){
			Exception exception = (Exception) packetReceived;
			System.out.println("##############################");
			System.out.println("Client::" +exception.getMessage());
			System.out.println("##############################");
			throw exception;
		}

		TreeMap<String,Object> ticketClientEnt = null;
		if(packetReceived instanceof TreeMap){
			ticketClientEnt =  (TreeMap<String, Object>) packetReceived;
		}

		System.out.println("Ent Server received object from Client");		

		//Unpack ticket Auth-Ent
		TreeMap<String, Object> unsealedMap_AuthEnt = null;
		try{
			unsealedMap_AuthEnt = (TreeMap<String, Object>) Ticket.unsealObject((SealedObject)ticketClientEnt.get(VarriableNames.MAP_KEY_UNSEAL_TICKET_1), VarriableNames.SECRET_KEY_AUTH_ENT);
		}catch(Exception ex){
			throw new NotValidPacketException();
		}		
		this.clientName = (String) unsealedMap_AuthEnt.get(VarriableNames.MAP_KEY_CLIENT_NAME);
		this.deptName = (String) unsealedMap_AuthEnt.get(VarriableNames.MAP_KEY_DEPT_NAME);
		
		//Add department number**
		this.departmentValue = (Integer) unsealedMap_AuthEnt.get(VarriableNames.MAP_DIRECTORY_VALUE);
		
		//
		this.sessionKeyEntClient = (String) unsealedMap_AuthEnt.get(VarriableNames.MAP_KEY_SESSION_KEY_CLIENT_ENT);
		this.sessionKeyEntDept = (String) unsealedMap_AuthEnt.get(VarriableNames.MAP_KEY_SESSION_KEY_ENT_DEPT);
		this.ticketAuthDept = (SealedObject) unsealedMap_AuthEnt.get(VarriableNames.MAP_KEY_TICKET);
		Timestamp timeStampAuth = (Timestamp) unsealedMap_AuthEnt.get(VarriableNames.MAP_KEY_TIMESTAMP);

		//Retrieve ticket Client-Dept
		TreeMap<String, Object> unsealedMap_ClientDept = null;
		try{
			unsealedMap_ClientDept = (TreeMap<String, Object>) Ticket.unsealObject((SealedObject)ticketClientEnt.get(VarriableNames.MAP_KEY_UNSEAL_TICKET_2), sessionKeyEntClient);
		}catch(Exception ex){
			throw new NotValidPacketException();
		}				
		this.ticketClientDept = (SealedObject) unsealedMap_ClientDept.get(VarriableNames.MAP_KEY_TICKET);
		Timestamp timeStampClient = (Timestamp) unsealedMap_ClientDept.get(VarriableNames.MAP_KEY_TIMESTAMP);

		System.out.println("Ent Server::Client Name: "+clientName);
		System.out.println("Ent Server::Dept Name: "+deptName);
		System.out.println("Ent Server::Auth Time Stamp: "+timeStampAuth);

		if(!(FunctionUtilities.checkFreshness(timeStampAuth, VarriableNames.ALLOWED_DELAY_AUTH_ENT))){
			throw new PacketNotFreshException();
		}
	}
	public SealedObject generatePacketEntClient(SealedObject packetDeptEnt) throws Exception{
		System.out.println("Unpacking repsonse from Department Packet");
		TreeMap<String, Object> unsealedMapDeptEnt = null;
		try{
			unsealedMapDeptEnt = (TreeMap<String, Object>) Ticket.unsealObject(packetDeptEnt, sessionKeyEntDept);
		}catch(Exception ex){
			throw new NotValidPacketException();
		}

		String clientName = (String) unsealedMapDeptEnt.get(VarriableNames.MAP_KEY_CLIENT_NAME);
		String deptName = (String) unsealedMapDeptEnt.get(VarriableNames.MAP_KEY_DEPT_NAME);
		SealedObject ticketKCD = (SealedObject) unsealedMapDeptEnt.get(VarriableNames.MAP_KEY_TICKET);
		Timestamp timestampDept = (Timestamp) unsealedMapDeptEnt.get(VarriableNames.MAP_KEY_TIMESTAMP);
		System.out.println("Client : " + clientName);
		System.out.println("Dept : " + deptName);
		System.out.println("ticketKCD :" + ticketKCD);
		System.out.println("timestamp : " + timestampDept);

		if(!(FunctionUtilities.checkFreshness(timestampDept, VarriableNames.ALLOWED_DELAY_ENT_DEPT ))){
			throw new Exception("Exception:: Packet received from Dept is not fresh for Ent Server!!!");
		}
		
		System.out.println("Packing response for Client");
		TreeMap<String, Object> mapEntClient = new TreeMap<String, Object>();
		//Put Packet
		mapEntClient.put(VarriableNames.MAP_KEY_TICKET, ticketKCD);
		//Put Time stamp
		mapEntClient.put(VarriableNames.MAP_KEY_TIMESTAMP, FunctionUtilities.findCurrentTimeStamp());
		SealedObject packet = Ticket.sealObject(mapEntClient, sessionKeyEntClient);
		return packet;
	}
	
	
	public void sendToDep(Communication communicationDept) throws Exception
	{
		//Initialize port with Department
		System.out.println("Ent Server sending Packet to Department");

		//Create Packet for Department
		TreeMap<String, Object> map = generatePacketEntDept(this.ticketAuthDept,this.ticketClientDept);

		//Sending packet to the Department		
		communicationDept.sendTickets(map);

	}
	private SealedObject recieveFromDep(Communication communicationDept) throws Exception{
		Object packetReceived = null;
		packetReceived = communicationDept.retrivePacket();
		if(packetReceived != null){
			System.out.println("Received Packet from Dept Server");
			System.out.println("Packet is :" + packetReceived.toString());
		}
		if(packetReceived instanceof Exception){
			Exception exception = (Exception) packetReceived;
			System.out.println("##############################");
			System.out.println("Dept Server::" +exception.getMessage());
			System.out.println("##############################");
			communicationDept.terminate();
			throw exception;
		}
		SealedObject packetDeptEnt = null;
		if(packetReceived instanceof SealedObject){
			packetDeptEnt = (SealedObject) packetReceived;
		}
		communicationDept.terminate();
		return packetDeptEnt;
	}
	public EntServer() throws Exception{		
		communicationClient = Communication.getInstance();
		communicationClient.serverInitialize(VarriableNames.PORT_ENT_SERVER);
		try{
			//process packet from Client
			processRequestfromClient();

			// create a new pipe to dept
			Communication communicationDept = new Communication();
			
			int port = 0;
			if(this.departmentValue == 1){
				port = VarriableNames.PORT_DEPT_SERVER1;
			}
			else if(this.departmentValue == 2){
				port = VarriableNames.PORT_DEPT_SERVER2;
			}
			else if(this.departmentValue == 3){
				port = VarriableNames.PORT_DEPT_SERVER3;
			}
			System.out.println(deptName);
			System.out.println(port);
				
				
			communicationDept.clientInitialize(this.deptName,port);
			//send packet to dep
			sendToDep(communicationDept);

			// receive packet from Dept
			SealedObject packetDeptEnt = recieveFromDep(communicationDept);

			//Generate Packet for Client
			SealedObject packetEntClient = generatePacketEntClient(packetDeptEnt);

			//send response to client
			communicationClient.send(packetEntClient);
			System.out.println("Ent Server sent Packet to Client");
		}catch(Exception ex){
			System.out.println(ex.getMessage());
			communicationClient.sendError(ex);
		}
	}
}