package com.b2bpo.android.chooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class PhotoDH extends DefaultHandler { 
	  private String TAG = "PhotoDH";
	  // booleans that check whether it's in a specific tag or not 
	  private boolean _inphoto, _inalbum, _intime; 
	 
	  // this holds the data 
	  private DataP _data;
	  private static String Filename = "photo_time.json";
	  private  File filesDir;
	  private FileWriter fstream;
	  private BufferedWriter out;
	  
	  public PhotoDH(File file){
		  //TODO if the file exists, delete it
		  this.filesDir = file;
		  		
	  }	  
	 
	  /** 
	   * Returns the data object 
	   * 
	   * @return 
	   */ 
	  public String getData() { 
	    return _data.toString(); 
	  } 
	 
	  /** 
	   * This gets called when the xml document is first opened 
	   * 
	   * @throws SAXException 
	   */ 
	  @Override 
	  public void startDocument() throws SAXException { 
	    _data = new DataP();
		try {
//			 _env.getExternalStorageDirectory(); 
			fstream = new FileWriter(new File(filesDir, Filename));
			out = new BufferedWriter(fstream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  } 
	 
	  /** 
	   * Called when it's finished handling the document 
	   * 
	   * @throws SAXException 
	   */ 
	  @Override 
	  public void endDocument() throws SAXException { 
		  try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  } 
	 
	  /** 
	   * This gets called at the start of an element. Here we're also setting the booleans to true if it's at that specific tag. (so we 
	   * know where we are) 
	   * 
	   * @param namespaceURI 
	   * @param localName 
	   * @param qName 
	   * @param atts 
	   * @throws SAXException 
	   */ 
	  @Override 
	  public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException { 
	 
	    if(qName.equals("gphoto:id")) { 
	      _inphoto = true; 
	 
//	      _data.sectionId = atts.getValue("id"); 
	    } else if(qName.equals("gphoto:albumid")) { 
	      _inalbum = true; 
	    } else if  (qName.equals("gphoto:timestamp")) {
	    	_intime = true;
	    }
	  } 
	 
	  /** 
	   * Called at the end of the element. Setting the booleans to false, so we know that we've just left that tag. 
	   * 
	   * @param namespaceURI 
	   * @param localName 
	   * @param qName 
	   * @throws SAXException 
	   */ 
	  @Override 
	  public void endElement(String namespaceURI, String localName, String qName) throws SAXException { 
//	    Log.v("endElement", localName +" " +qName); 
	 
	    if(qName.equals("gphoto:id")) { 
	      _inphoto = false; 
	    } else if(qName.equals("gphoto:albumid")) { 
	      _inalbum = false; 
	    } else if(qName.equals("gphoto:timestamp")){
	    	_intime = false;
	    } else if (localName.equals("entry")){
//	    	Log.v(TAG, "{ \"id\": \"" +_data.photoId +"\", \"albumId\": \"" +_data.albumId +"\", \"time\": \"" +_data.time +"\"},");
	    		    	
	    	try {
				JSONObject object =  Photo_meta.setLinePh(Photo_meta.fmtLinePh(_data.toString()));
				out.write(object.toString() +"\n");
				Log.v(TAG, "time: " +object.getString("time"));
//				Log.v(TAG, object.toString());
				
			} catch (InvalidParameterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	    	catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	    }
	  } 
	 
	  /** 
	   * Calling when we're within an element. Here we're checking to see if there is any content in the tags that we're interested in 
	   * and populating it in the Config object. 
	   * 
	   * @param ch 
	   * @param start 
	   * @param length 
	   */ 
	  @Override 
	  public void characters(char ch[], int start, int length) { 
	    String chars = new String(ch, start, length); 
	    chars = chars.trim(); 
	 
	    if(_inphoto) { 
	      _data.setPhotoId(chars); 
	    } else if(_inalbum) { 
	      _data.setAlbumId(chars); 
	    }  else if(_intime){
//	    	_data.time = chars;
	    	//TODO handle timezone of locale and convert Date from local to UTC  before convrt to String
	    	_data.setTime(chars);
	    	
	    }
	  } 
	} 
