package main.utilities;

import java.sql.Timestamp;
import java.util.Date;

public class FunctionUtilities {
	public static boolean checkFreshness(Timestamp timeStampSender, long maxDelay){
		boolean var= false;
		long time_millisecs_sender = (long) timeStampSender.getTime();
		System.out.println("Sender Time Stamp: "+timeStampSender);
		System.out.println("Sender Time Stamp in milli seconds: "+time_millisecs_sender);

		
		Date date = new Date();
		Timestamp currentEntityTimestamp = new Timestamp(date.getTime());
		long time_millisecs_currentEntity = (long) currentEntityTimestamp.getTime();
		System.out.println("Receiver Time Stamp: "+timeStampSender);
		System.out.println("Receiver Time Stamp in milli seconds: "+time_millisecs_sender);
		

		if (time_millisecs_currentEntity > time_millisecs_sender &&  time_millisecs_currentEntity - time_millisecs_sender < maxDelay){
			return true;
		}
		return var;
	}
	
	public static Timestamp findCurrentTimeStamp(){
		Date date = new Date();
		Timestamp currentEntityTimestamp = new Timestamp(date.getTime());
		return currentEntityTimestamp;		
	}

}
