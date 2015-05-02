package main.exception;

public class FileNotPresentException extends Exception{
	public FileNotPresentException(){
		super("Exception::Not a valid file or File not present!!!");
	}
	public String toString(){
		return ("Packet not Valid Exception") ;
	}
}
