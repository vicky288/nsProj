package main.z_test;

import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.TreeMap;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import main.communication.Communication;
import main.exception.PacketNotFreshException;
import main.server.AuthServer;
import main.utilities.Ticket;

public class Test implements Serializable{

	String str;
	int inum;
	Test1 test1;
	Test (String x,int y,Test1 t){
		str= x;
		inum = y;
		test1 =t;
	}
	public String getStr() {
		return str;
	}
	public void setStr(String str) {
		this.str = str;
	}
	public int getInum() {
		return inum;
	}
	public void setInum(int inum) {
		this.inum = inum;
	}
	public Test1 getTest1() {
		return test1;
	}
	public void setTest1(Test1 test1) {
		this.test1 = test1;
	}
	
	public static void main(String[] args) throws Exception{
		Test1 test1=new Test1("blavcl");
		Test t= new Test("xyz", 12, test1);
		
		TreeMap<String, Object> map = new TreeMap<String, Object>();
		map.put("abd", "xyz");
		map.put("objecyt", t);
//		SealedObject so = Ticket.sealObject(map, "fghjfhjfhfjh");
		Object[] keys = map.keySet().toArray();
		Object[] values = map.values().toArray();
		
//		byte[] keyBytes = 
		
		
//		TreeMap<String, Object> map1 = new TreeMap<String, Object>();
//		map1 = null;
//		System.out.println(ar);
		
/*	    final char[] password = "fghjfhjfhfjh".toCharArray();
        final byte[] salt = "a9v5n38s".getBytes();
        
        // Create key
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password, salt, 1024, 128);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

	       Cipher dcipher = Cipher.getInstance("AES");
	        dcipher.init(Cipher.DECRYPT_MODE, secret);
	        if(dcipher==null){
	        	System.out.println("lllll");
	        }
	        
	        TreeMap<String, Object> decryptedPacket = (TreeMap<String, Object>) so.getObject(dcipher);	
	        System.out.println(decryptedPacket.get("abd"));;
	        
	        Test t1=(Test)decryptedPacket.get("objecyt");
	        System.out.println(t1.getStr()+"======="+t1.getInum());
	        t1.getStr();
	        t1.getInum();
	        Test1 test12=t1.getTest1();
	        System.out.println(test12.getColor());
	        
	        System.out.println("####################################");
	        System.out.println("Server Window");
	        try{
	        AuthServer authServer = new AuthServer();
	        } catch (PacketNotFreshException pe){
	        	System.out.println(pe.getMessage());
	        }
*/	        
//	        ServerSocket serverSocket = new ServerSocket(4444);
//	        Socket clientSocket = serverSocket.accept();
//	        Communication communication = new Communication();
//	        communication.initialize("localhost", 4444);
//	        communication.terminate();
	}
}
