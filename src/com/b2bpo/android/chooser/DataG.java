package com.b2bpo.android.chooser;

import org.json.JSONException;
import org.json.JSONObject;

public class DataG {
	public String lat;
	public String lon;
	public String time;
	
	  public DataG(JSONObject json){
		  try {
			setTime(json.getString("time"));
			setLat(json.getString("lat"));
			setLon(json.getString("lon"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	  
	  public DataG(String str){

			try {
				new DataG( new JSONObject(str));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
	  }
	  
	  public DataG() { 
		  lat = "";
		  lon = "";
		  time = "";
	  }
	
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLon() {
		return lon;
	}
	public void setLon(String lon) {
		this.lon = lon;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
	public String toString(){
		return "lat " +getLat() + " lon " +getLon() + " time " +getTime();
	}
}
