package main.z_test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import main.communication.Communication;
import main.utilities.VarriableNames;

public class Client {
	private Socket clientSocket;
	private PrintWriter sendStream;
	private ServerSocket serverSocket;
	private ObjectInputStream retrieveStream;
	private String hostName="10.21.40.242";
	private int portNumber=55555;

 public void test() throws Exception{
		clientSocket = new Socket(hostName,portNumber);
		OutputStream os= clientSocket.getOutputStream();
		ObjectOutputStream objos = new ObjectOutputStream(os);
		Test1 obj =new Test1("hello");
		objos.writeObject(obj);

 }
public static void main(String[] args) throws Exception {
	Client  cl = new Client();
	cl.test();
}
}
