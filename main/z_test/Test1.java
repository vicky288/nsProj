package main.z_test;

import java.io.Serializable;

public class Test1 implements Serializable{
	String color;
	
	public Test1(String x){
		color=x;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
}
