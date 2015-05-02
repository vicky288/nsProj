package main.exception;

public class NotValidPacketException extends Exception{
	public NotValidPacketException(){
		super("Exception::Packet not Valid!!!!!!");
	}
	public String toString(){
		return ("Packet not Valid Exception") ;
	}
}
