package main.z_test;

import java.io.File;
import java.nio.file.Files;
import java.util.TreeMap;

import javax.crypto.SealedObject;

import main.utilities.Ticket;

public class TestFile {
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		// Begin File Creation///////////////////////////////////
		String fileName = "pic.png";
//		File file = new File("C:\\Department\\" + fileName);
		File file = new File("/Users//vicky288/Desktop/UTD/1 st Sem/Network Security/Project/dir_dept/" + fileName);
		String key = "srf8uuc0f2nucce505ivgc8eo1";
		byte[] contentOfFile = Files.readAllBytes(file.toPath());
		// //////////////////////////////////////////////////////

		// Begin Sealing//////////////////////////////////////////////
		System.out.println("Sealing Object");
		TreeMap<String, Object> map = new TreeMap<String, Object>();
		map.put("file", contentOfFile);
		SealedObject sObject = Ticket.sealObject(map, key);
		// //////////////////////////////////////////////////////////

		// Begin Unsealing///////////////////////////////////////////
		TreeMap<String, Object> map2 = Ticket.unsealObject(sObject, key);
		System.out.println(map.get("file"));
		byte[] unsealedFile = (byte[]) map.get("file");
		// ///////////////////////////////////////////////////////////

		// Write to Location of choice///////////////////////////////
		// Requires a full destination
		// Will only take Byte Array not a "literal" file
		File fileLoc = new File("/Users/vicky288/Desktop/UTD/1 st Sem/Network Security/Project/dir_client/pic.png");
		Files.write(fileLoc.toPath(), unsealedFile);
		//////////////////////////////////////////////////////////

	}
}
