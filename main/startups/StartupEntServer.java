package main.startups;

import main.server.EntServer;

public class StartupEntServer {
	public static void main(String args[]){
		try{
			EntServer ent = new EntServer();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
