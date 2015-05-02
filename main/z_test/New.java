package main.z_test;

import java.util.Scanner;

public class New {
	public static void main(String[] args) {
		String deptNumInput = null;
		Scanner in = new Scanner(System.in);
		while( deptNumInput == null || deptNumInput.equals("")){
			System.out.println("Input a Department Number(1.CS 2.CE 3.EE) :>");
			deptNumInput = in.nextLine();
			try{
				int deptNum = Integer.parseInt(deptNumInput);
				if (deptNum == 1 || deptNum == 2 ||deptNum == 3 ){
					break;
				}else{
					System.out.println("Only input 1, 2, 3 is allowed. Try Again.");
					deptNumInput = null;
				}
			}catch(NumberFormatException ne){
				System.out.println("Only input 1, 2, 3 is allowed. Try Again.");
			}
			deptNumInput = null;
		}
	}
}
