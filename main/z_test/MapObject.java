package main.z_test;

import java.io.Serializable;
import java.util.HashMap;

public class MapObject implements Serializable{
	HashMap<String, Object> map = new HashMap<String, Object>();

	public HashMap<String, Object> getMap() {
		return map;
	}

	public void setMap(HashMap<String, Object> map) {
		this.map = map;
	}
	
}
