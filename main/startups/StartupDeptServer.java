package main.startups;

import main.server.DeptServer;

public class StartupDeptServer {
	public static void main(String args[]){
		try{
			DeptServer dept = new DeptServer();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}

