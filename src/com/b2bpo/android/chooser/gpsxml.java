package com.b2bpo.android.chooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.namespace.QName;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class gpsxml extends Activity {
	 private static final String TAG = "gpsxml";	
	 String state = Environment.getExternalStorageState();
	 boolean mExternalStorageAvailable = false;
	 boolean mExternalStorageWriteable = false;
	 FileOutputStream f = null;
	 FileInputStream is = null;
	 static Context _context;
	 Fileprocess fp = null;	
	 int ctr = 0;

	 public static final String ACTION_GET_GPX = "com.b2bpo.action.GET_GPX";
	 
	 public  static Context getContext() {
	        return _context;
	    }	 

	 /*
	  * each xml type will have it own Data class and its own Handler
	  * for now . the xml comes from fileHandles 
	  */
	 public class XmlReader {
		 private DataP _data;
		 
		 private String _parseXml(org.xml.sax.helpers.DefaultHandler dataHandler, String path ) { 
			  String data = null; 
			 
			  // sax parser stuff 
			  try { 
			    SAXParserFactory spf = SAXParserFactory.newInstance(); 
			    SAXParser sp = spf.newSAXParser(); 			 
			    XMLReader xr = sp.getXMLReader(); 			 
//			    PhotoDH dataHandler = new PhotoDH(); 
			    xr.setContentHandler(dataHandler); 
		    	File mydir = getExternalFilesDir(null);       	       	                                      
			    xr.parse(new InputSource(new FileInputStream( new File(mydir, path)))); 			 
//			    data = dataHandler.getData(); 
			 
			  } catch(ParserConfigurationException pce) { 
			    Log.e("SAX XML", "sax parse error", pce); 
			  } catch(SAXException se) { 
			    Log.e("SAX XML", "sax error", se); 
			  } catch(IOException ioe) { 
			    Log.e("SAX XML", "sax parse io error", ioe); 
			  } 
			 
			  return data; 
			}
	 }
    
	public class XPathReader {
	    
//	    private String xmlFile;
//	    private Uri uri;
	    private String expression;
	    private Document xmlDocument;
	    private XPath xPath;
	    private FileInputStream fis;
    	private String lat = "";
    	private String lon = "";
	    
	    public XPathReader(String xmlFile, String expression) {

	    	try {
				fis = new FileInputStream(new File(xmlFile));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        this.expression = expression;
	        initObjects();
	        process();
	    }
	    
	    private void initObjects(){        
	        try {
	            xmlDocument = DocumentBuilderFactory.
				newInstance().newDocumentBuilder().
				parse(fis);            
	            xPath =  XPathFactory.newInstance().
				newXPath();
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        } catch (SAXException ex) {
	            ex.printStackTrace();
	        } catch (ParserConfigurationException ex) {
	            ex.printStackTrace();
	        }       
	    }
	    
	    private void process(){
       	 NodeList thirdProject = (NodeList)this.read(expression, 
    				XPathConstants.NODESET);
    	        this.traverse(thirdProject);
	    } 
	    
	    public Object read(String expression, 
				QName returnType){
	        try {
	            XPathExpression xPathExpression = 
				xPath.compile(expression);
		        return xPathExpression.evaluate
				(xmlDocument, returnType);
	        } catch (XPathExpressionException ex) {
	            ex.printStackTrace();
	            return null;
	        }
	    }
	    
	    private  void traverse(NodeList rootNode){

	        for(int index = 0; index < rootNode.getLength();
			index ++){
	            Node aNode = rootNode.item(index);
	            if (aNode.getNodeType() == Node.ELEMENT_NODE){

	            	
	                NodeList childNodes = aNode.getChildNodes();

	                if (childNodes.getLength() > 0){
	                	if (  aNode.getNodeName().equalsIgnoreCase("trkpt") ) {
	                	 	NamedNodeMap map = aNode.getAttributes();
	    	            	Node a1 = map.getNamedItem("lat");
	    	            	lat = "{ \"lat\": \"" +a1.getNodeValue() +"\"," ;
 //   	            	Log.d(TAG, lat );
	    	            	Node a2 = map.getNamedItem("lon");
	    	            	lon = " \"lon\": \"" +a2.getNodeValue() +"\"," ;
//	    	            	Log.d(TAG, lon);
	                		
	                	} else if ( aNode.getNodeName().equalsIgnoreCase("time") ){
						Log.d(TAG, lat + lon +" \"time\": \"" +aNode.getTextContent() +"\"},"); 
	 	               lat = "";
		               lon = "";

					}}
	                traverse(aNode.getChildNodes());                
		        }
			}        
	    }
	}
	
	
	class Fileprocess implements Comparator<Date>{
		private BufferedReader filA; //photo
		private BufferedReader filB; //gps
		private FileWriter filC; //lat lon photo
		private File path;
		private boolean needfilA;
		private boolean needfilB;
		
		DataP dataP=null;
		DataG dataG=null;
		Date dateA=null;
		Date dateB=null;
		Date dateC=null;
		String strA=null;
		String strB=null;
		int flag=1;


		//TODO open out file here and connect write statemnent , w/ the log statement is in key compare area.
		public Fileprocess (String nameA, String nameB, String nameC){
			path = getExternalFilesDir(null);
			needfilA = true;
			needfilB = true;
			try {
				filA = new BufferedReader(new FileReader(new File(getExternalFilesDir(null), nameA)));
				filB = new BufferedReader(new FileReader(new File(getExternalFilesDir(null), nameB)));
				filC = new FileWriter(new File(path, nameC));
				Log.d(TAG, " construct new Fileprocess");
				flag = getInput();
				while (needfilA || needfilB){				
					process();
					flag= getInput();
				}
				filA.close();
				filB.close();
				filC.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}
		
		
		/*
		 * filA  read
		 * filB  read
		 * filC 
		 * return -999 is both infiles at EOF
		 */
		private int getInput(){

			if (needfilA){
				// TODO handle EOF on A
				try {
					strA = filA.readLine();
					if (strA != null){ 
						ctr++;
						
//						dataP = new DataP(filA.readLine());
	//					String dud = filA.readLine();
//						Log.d(TAG, dud);
						dataP= new DataP(new JSONObject(strA));
						
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
//						sdf.getTimeZone().getOffset(time)
						
//						Log.d(TAG, "dataP:time " +dataP.getTime() + " " +dataP.getAlbumId() + " " +dataP.getPhotoId());
					    dateA = sdf.parse(dataP.getTime());
					    dateC = cvtToGmt(dateA);
					    if(ctr < 11) Log.d(TAG, "dataP:time " +dataP.getTime() + " " + dateA.getTime() +" " +dateC.getTime() + " " +dataP.getPhotoId());
					    // TODO have to chg timezone to zulu from -0070
//						dateA = new Date(dataP.getTime());// cant do this
						Log.d(TAG, "getinput rd A ");
					};
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			};
			
			if (needfilB){
				
				try {
					strB = filB.readLine();
					if (strB != null) {
//						ctr++;
//						dataG = new DataG(filB.readLine());
						
//						Log.d(TAG, dub);
						dataG = new DataG(new JSONObject(strB));
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
//						Log.d(TAG, "dataG:time " +dataG.getTime() + " " +dataG.getLat() + " " +dataG.getLon());
						sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
						dateB = sdf.parse(dataG.getTime());
//					    if(ctr > 650) Log.d(TAG, "dataG:time " +dataG.getTime() + " " + dateB.getTime() +" " +dateB.getTimezoneOffset());
					   
//						dateB = new Date(dataG.getTime());
					
					};
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			};
//			if(strA == null && strB == null) return -999;
			if(strA == null && strB == null){
				needfilA = false;
				needfilB = false;
				return 0;
			}
			if(strA == null) return 1;
			if(strB == null) return -1;
			return compare (dateA, dateB);
			
		}

		@Override
		public int compare(Date arg0, Date arg1) {
			// TODO Auto-generated method stub
			return arg0.compareTo(arg1);
		}
		
		
		/*
		 * joins 2 sets based on the value of the timestamp. both gps set and photo set have TS attribute.
		 * now, the photo will get exact coordinates of the either the first gps row having TS that is greater OR
		 * the last GPS read in the set
		 * 
		 */
		//TODO  change the gps coordinates to an interpolated value of the 2 gps nearest ( one LT , one GT ) the photo's timestamp 
		public void process(){
												
			if (flag < 0){				
					try {
						filC.write( dataP.getAlbumId() +" " +dataP.getPhotoId() + " " +dataG.getLat() + " " +dataG.getLon() +"\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
//				Log.d(TAG, dataP.getAlbumId() +" " +dataP.getPhotoId() + " " +dataG.getLat() + " " +dataG.getLon());
				needfilB = false;
				needfilA = true;
			} else if ( flag == 0) {
//				Log.d(TAG, dataP.getAlbumId() +" " +dataG.getLat() + " " +dataG.getLon());
				needfilB = true;
				needfilA = true;
			} else if (flag > 0) {				
				needfilB = true;
				needfilA = false;
			}
			flag = getInput();			
		}
		
		private  Date cvtToGmt( Date date )
		{
		   TimeZone tz = TimeZone.getDefault();
		   Date ret = new Date( date.getTime() - tz.getRawOffset() );

		   // if we are now in DST, back off by the delta.  Note that we are checking the GMT date, this is the KEY.
		   if ( tz.inDaylightTime( ret ))
		   {
		      Date dstDate = new Date( ret.getTime() - tz.getDSTSavings() );

		      // check to make sure we have not crossed back into standard time
		      // this happens when we are on the cusp of DST (7pm the day before the change for PDT)
		      if ( tz.inDaylightTime( dstDate ))
		      {
		         ret = dstDate;
		      }
		   }

		   return ret;
		}
	}
	

    /**
     * Called with the activity is first created.
     */
    @Override
    //TODO add button for location control 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        _context = this.getContext();
        
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        Log.i(TAG, getIntent().getAction());

    	// test file on sdcard root, later will be in myTracks Data dir on sd
 //  	File mydir = getExternalFilesDir(null);       	       	
 //   	XPathReader reader = new XPathReader(mydir.getAbsolutePath() +"/" +"test7.gpx", "/gpx/trk/trkseg/trkpt");
        // get instance of handler for the path and call the parser 
// one or the other or both types of data in sequence
//        String ph_rdr = new XmlReader()._parseXml(new PhotoDH(getExternalFilesDir(null)), "gps_photofeed.xml");
        // temp gps = /mnt/sdcard/Android/data/com.b2bpo.android.chooser/files/Gps_time.json
       
        String ph_rdr = new XmlReader()._parseXml(new GpsDH(getExternalFilesDir(null)), "test7.gpx");
        
        fp = new Fileprocess( "photo_time.json" ,"Gps_time.json",  "photo_upd_gps" );
        		
        		//photo_time.json Gps_time.json dummy
       
    }
    
    private boolean checkExternalMedia(){
    	boolean mExternalStorageAvailable = false;
    	boolean mExternalStorageWriteable = false;
    	String state = Environment.getExternalStorageState();

    	if (Environment.MEDIA_MOUNTED.equals(state)) {
    	// We can read and write the media
    	mExternalStorageAvailable = mExternalStorageWriteable = 
    	true;
    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
    	// We can only read the media
    	mExternalStorageAvailable = true;
    	mExternalStorageWriteable = false;
    	} else {
    	// Something else is wrong. It may be one of many other states,but all we need
    	// to know is we can neither read nor write
    	Log.i(TAG,"State="+state+" Not good");
    	mExternalStorageAvailable = mExternalStorageWriteable = false;
    	}

    	Log.i(TAG,"Available="+mExternalStorageAvailable+"Writeable="+mExternalStorageWriteable+" State"+state);
    	return (mExternalStorageAvailable && mExternalStorageWriteable);
    	}

}
