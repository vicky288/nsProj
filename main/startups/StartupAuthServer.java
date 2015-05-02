package main.startups;

import main.exception.PacketNotFreshException;
import main.server.AuthServer;

public class StartupAuthServer {
	public static void main(String[] args) {
		try{
			AuthServer authServer = new AuthServer();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
