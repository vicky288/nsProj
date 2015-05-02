package main.z_test;

import java.sql.Timestamp;
import java.util.Date;

public class Times {
	public static void main(String[] args) throws Exception {
		Date date = new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		int time_prefix = (int) timestamp.getTime();
		int x = (int) System.currentTimeMillis();
		System.out.println(time_prefix + "  " + x);
//		Thread.currentThread().sleep(5000);
		int i = 2;
		while (i>1)
		{
			i--;
			Thread.currentThread().sleep(1000);
			System.out.print(" =");
		}
		System.out.println();
		if (i > 0){ 
		timestamp = new Timestamp(date.getTime());
		time_prefix = (int) timestamp.getTime();
		System.out.println(time_prefix + " " +(int) System.currentTimeMillis());
		}
		///////////
	}
}
