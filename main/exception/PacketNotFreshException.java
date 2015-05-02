package main.exception;

public class PacketNotFreshException extends Exception{

	public PacketNotFreshException(){
		super("Exception::Packet not Fresh!!!!!!");
		
	}
	public String toString(){
		return ("Packet not Fresh Exception") ;
	}

}
