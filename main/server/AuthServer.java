package main.server;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.TreeMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

import main.client.StartupClient;
import main.communication.Communication;
import main.exception.NotValidPacketException;
import main.exception.PacketNotFreshException;
import main.utilities.FunctionUtilities;
import main.utilities.SessionIdentifierGenerator;
import main.utilities.Ticket;
import main.utilities.VarriableNames;

public class AuthServer extends Servers{
	Communication communication;
	private String sharedKeyAuthClient;
	private String sharedKeyAuthEnt;
	private String sharedKeyAuthDept;	
	private String clientName;
	private String deptName;	
	private int departmentValue;
	private String sessionKeyEntDept;	
	private String sessionKeyEntClient;
	private String sessionKeyClientDept;
	private Timestamp timeStampServer;
	

	private SealedObject generateSealedObjectAuthClient()throws Exception{
		TreeMap<String, Object> map = new TreeMap<String, Object>();
		map.put(VarriableNames.MAP_KEY_SESSION_KEY_CLIENT_ENT,sessionKeyEntClient);
		map.put(VarriableNames.MAP_KEY_SESSION_KEY_CLIENT_DEPT,sessionKeyClientDept);
		SealedObject ticketAuthEnt = generateSealedObjectAuthEnt();		
		map.put(VarriableNames.MAP_KEY_TICKET, ticketAuthEnt);
		map.put(VarriableNames.MAP_KEY_TIMESTAMP, timeStampServer);	
		return Ticket.sealObject(map, sharedKeyAuthClient);
	}
	private SealedObject generateSealedObjectAuthEnt()throws Exception{
		TreeMap<String, Object> map = new TreeMap<String, Object>();
		map.put(VarriableNames.MAP_KEY_CLIENT_NAME,clientName);
		map.put(VarriableNames.MAP_KEY_DEPT_NAME,deptName);
		
		//Put Department value**
		map.put(VarriableNames.MAP_DIRECTORY_VALUE,departmentValue);
		//
		map.put(VarriableNames.MAP_KEY_SESSION_KEY_CLIENT_ENT,sessionKeyEntClient);
		map.put(VarriableNames.MAP_KEY_SESSION_KEY_ENT_DEPT,sessionKeyEntDept);
		SealedObject ticketAuthDept = generateSealedObjectAuthDept();
		map.put(VarriableNames.MAP_KEY_TICKET, ticketAuthDept);
		map.put(VarriableNames.MAP_KEY_TIMESTAMP, timeStampServer);		
		return Ticket.sealObject(map, sharedKeyAuthEnt);
	}
	private SealedObject generateSealedObjectAuthDept()throws Exception{
		TreeMap<String, Object> map = new TreeMap<String, Object>();
		map.put(VarriableNames.MAP_KEY_CLIENT_NAME,clientName);
		map.put(VarriableNames.MAP_KEY_SESSION_KEY_ENT_DEPT,sessionKeyEntDept);
		map.put(VarriableNames.MAP_KEY_SESSION_KEY_CLIENT_DEPT,sessionKeyClientDept);		
		map.put(VarriableNames.MAP_KEY_TIMESTAMP, timeStampServer);	
		return Ticket.sealObject(map, sharedKeyAuthDept);
	}

	private SealedObject createPacket(String clientName, String deptName,int departmentValue, Timestamp serverTimestamp) throws Exception{
		this.generateKeys();
		this.clientName = clientName;
		this.departmentValue = departmentValue;
		this.deptName = deptName;
		this.timeStampServer = serverTimestamp;
		return generateSealedObjectAuthClient();
	}

	private void generateKeys(){
		SessionIdentifierGenerator generator = new SessionIdentifierGenerator();
		this.sessionKeyClientDept = generator.nextSessionId();
		this.sessionKeyEntClient = generator.nextSessionId();
		this.sessionKeyEntDept = generator.nextSessionId();
	}

	public SealedObject processRequest() throws Exception{
		communication.serverInitialize(VarriableNames.PORT_AUTH_SERVER);
		System.out.println("server socket initialized");
		System.out.println("waiting for packet.....");
		SealedObject ticketClientAuth = null;
		while(true){
			ticketClientAuth = communication.retrive();
			if(ticketClientAuth != null){
				break;
			}
		}
		System.out.println("received object from client and unsealing it");
		TreeMap<String, Object> unsealedMap = null;
		try{
			unsealedMap = (TreeMap<String, Object>) Ticket.unsealObject(ticketClientAuth, VarriableNames.SECRET_KEY_AUTH_CLIENT);
		}catch(Exception ex){
			throw new NotValidPacketException();
		}
		String clientName = (String) unsealedMap.get(VarriableNames.MAP_KEY_CLIENT_NAME);
		String deptName = (String) unsealedMap.get(VarriableNames.MAP_KEY_DEPT_NAME);
		//Dept Value
		int departmentValue = (Integer) unsealedMap.get(VarriableNames.MAP_DIRECTORY_VALUE);
		//
		Timestamp timeStampClient = (Timestamp) unsealedMap.get(VarriableNames.MAP_KEY_TIMESTAMP);

		//Choose the AuthClient Shared Secret and set the other shared keys
		this.sharedKeyAuthClient = VarriableNames.SECRET_KEY_AUTH_CLIENT;
		this.sharedKeyAuthEnt = VarriableNames.SECRET_KEY_AUTH_ENT;
		this.sharedKeyAuthDept = VarriableNames.SECRET_KEY_AUTH_DEPT_01;
		

		System.out.println("Packet::Client Name: "+clientName);
		System.out.println("Packet::Dept Name: "+deptName);
		System.out.println("Packet::Client Time Stamp: "+timeStampClient);

		if (FunctionUtilities.checkFreshness(timeStampClient, VarriableNames.ALLOWED_DELAY_CLIENT_AUTH)) {
			SealedObject ticketAuthClient = createPacket(clientName, deptName, departmentValue,FunctionUtilities.findCurrentTimeStamp());
			return ticketAuthClient;
		}else{
			throw new PacketNotFreshException();
		}
	}

	private void sendResponse(SealedObject sealedObject) throws Exception{
		System.out.println("Auth Server sending Packet");
		communication.send(sealedObject);
	}

	///test code TimeStamp...	
	Timestamp currentEntityTimestamp;
	///test code	

	public AuthServer() throws Exception{		

		///test code TimeStamp...
		Date date = new Date();
		currentEntityTimestamp = new Timestamp(date.getTime());
		///test code

		try{
			communication = Communication.getInstance();
			SealedObject ticket = processRequest();
			sendResponse(ticket);	
		} catch (PacketNotFreshException pe){
			System.out.println(pe.getMessage());
			communication.sendError(pe);
		} catch(Exception e) {
			e.printStackTrace();
			communication.sendError(e);
		}
	}
}
