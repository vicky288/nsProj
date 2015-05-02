package main.z_test;

import java.io.File;
import java.nio.file.Files;

public class File123 {
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		// Begin File Creation///////////////////////////////////
		String fileName = "id.txt";
//		File file = new File("C:\\Department\\" + fileName);
		File file = new File("~/Proj/Dir_01/" + fileName);
		byte[] contentOfFile = Files.readAllBytes(file.toPath());
		// //////////////////////////////////////////////////////
		System.out.println(contentOfFile);


	}
}
