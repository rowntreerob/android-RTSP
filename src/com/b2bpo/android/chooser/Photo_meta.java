package com.b2bpo.android.chooser;

import java.security.InvalidParameterException;
import java.util.StringTokenizer;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class Photo_meta {
	static JSONObject line;
	static String TAG = "Photo_meta";

	/*
	 * create a data line from the formatted string containing 3 terms belonging to the record
	 * photo, album, time 
	 * OR
	 * lat, lon, time
	 */
	public  static JSONObject setLinePh(String json) {
		JSONObject obj = null;
		try {
			obj = (JSONObject) new JSONTokener(json).nextValue();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}
	
	/*
	 * Tokenize the line into even number of tokens and 
	 * use K-V pairs to create JSONData record
	 */
	public static String fmtLinePh(String terms) throws InvalidParameterException{
		String lne = "";
		
		StringTokenizer tok = new StringTokenizer(terms);
//		Log.d(TAG, terms +" " +tok.countTokens());
//12 34 567
		 if(tok.countTokens()!=7 ) throw new InvalidParameterException("7 terms needed");
		lne = lne+ "\"" +tok.nextToken() +"\": \"" +tok.nextToken() +"\"";  // 1,2
		lne += ",";			
		lne = lne+ "\"" +tok.nextToken() +"\": \"" +tok.nextToken() +"\""; // 3,4 
		lne += ",";	
		lne = lne+ "\"" +tok.nextToken() +"\": \"" +tok.nextToken() +" " +tok.nextToken() +"\""; // 5,6,7 
		lne = "{ " +lne +"}";
		
		return lne;
	}
	
	public static String fmtLineG(String terms) throws InvalidParameterException{
		String lne = "";
		
		StringTokenizer tok = new StringTokenizer(terms);
//		Log.d(TAG, terms +" " +tok.countTokens());
//12 34 567
		 if(tok.countTokens()!=6 ) throw new InvalidParameterException("6 terms needed");
		lne = lne+ "\"" +tok.nextToken() +"\": \"" +tok.nextToken() +"\"";  // 1,2
		lne += ",";			
		lne = lne+ "\"" +tok.nextToken() +"\": \"" +tok.nextToken() +"\""; // 3,4 
		lne += ",";	
		lne = lne+ "\"" +tok.nextToken() +"\": \"" +tok.nextToken() +"\""; // 5,6 
		lne = "{ " +lne +"}";
		
		return lne;
	}
/* 
 * not sure this will work with the line objects because you get the attrs directly	
 */
	public static String getName(JSONObject obj, String term) {
		try {
//			String query = obj.getString("query");
			JSONObject key;			
			key = obj.getJSONObject(term);
			return key.getString(term);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}

	}
	
	
//	 JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
//	 String query = object.getString("query");
//	 JSONArray locations = object.getJSONArray("locations");
}
