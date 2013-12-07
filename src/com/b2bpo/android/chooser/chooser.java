package com.b2bpo.android.chooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
// mimetypes for file open


import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.MimeTypeParser;
import org.openintents.filemanager.util.MimeTypes;
import org.xmlpull.v1.XmlPullParserException;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

import android.provider.MediaStore.Video.Media;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class chooser extends Activity {
    // Request code for the contact picker activity
    private static final int PICK_CONTACT_REQUEST = 1;
    private static final int SELECT_AUDIO = 2;
    private RecordButton mRecordButton = null;
    private CntctButton mCntctButton = null;
    private AudioButton mAudButton = null;
    private LocationButton mLocButton = null;
    private JSONButton mJSONButton = null;
    private MediaRecorder mRecorder = null;
    private static String mFileName = "chooser.mp4";
    private static final String LOG_TAG = "chooser";
    private Activity _mact = null;
    private Context mctx =null; 
    private static final String TAG = "chooser";
    public static final String ACTION_PURCHASE_SKU = "com.b2bpo.action.PURCHASE_SKU";
    public static final String ACTION_PLAY_AUDIO = "com.b2bpo.action.PLAY_AUDIO";
    public static final String ACTION_UPLOAD_3GP = "com.b2bpo.action.UPLOAD_3GP";
    
    private MimeTypes mMimeTypes;
    
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;
    String state = Environment.getExternalStorageState();
    
    FileOutputStream f = null;



    /**
     * An SDK-specific instance of {@link ContactAccessor}.  The activity does not need
     * to know what SDK it is running in: all idiosyncrasies of different SDKs are
     * encapsulated in the implementations of the ContactAccessor class.
     */
//    private final ContactAccessor mContactAccessor = ContactAccessor.getInstance();
    Toast mToast;
    ResultDisplayer mPendingResult;
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }
    
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "record msg prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }
    
    class ResultDisplayer implements OnClickListener {
        String mMsg;
        String mMimeType;
        
        ResultDisplayer(String msg, String mimeType) {
            mMsg = msg;
            mMimeType = mimeType;
        }
        
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(mMimeType);
            mPendingResult = this;
            startActivityForResult(intent, 1);
        }
    }
    
    class AudioDisplayer implements OnClickListener{
 //   	String mMimeType ="video/3gp";
  //  	AudioDisplayer mPendingResult;
        public void onClick(View v) { 

 
        	
  //      	String extension = MimeTypeMap.getFileExtensionFromUrl(url);
  //      	String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

  //      	Intent intent = new Intent(Intent.ACTION_VIEW);
  //      	intent.setDataAndType(Uri.parse("file:///mnt/sdcard/audio-AMR-2.3gpp"), "audio/amr"); // NG sorry the player does not suppor this type of file
//        	intent.setDataAndType(Uri.parse("file:///mnt/sdcard/test_audio_only_rtsp.3gpp"), "audio/amr");  // this works from cpp  
        	// start another activity that will launch rtsp - rtp streams to the phone
        	Intent intent = new Intent(Intent.ACTION_VIEW);
//        	intent.setComponent(new ComponentName("com.b2bpo.android.chooser","com.b2bpo.android.chooser.Chooseit"));
        	intent.setComponent(new ComponentName("com.b2bpo.android.chooser","com.b2bpo.android.chooser.gpsxml"));
        	startActivity(intent);        	
        	
        }
   //        Intent atent = new Intent(ACTION_UPLOAD_3GP); 
           // tie the button to context for new message using com.android.mms.ui.ComposeMessageActivity
        	
//        	Intent atn = new Intent("android.intent.action.MAIN");
//        	atn.setComponent(new ComponentName("com.android.mms","com.android.mms.ui.ConversationList"));
//        	startActivity(atn);	

              // TODO add 'audio' parm value to the data uri
     //      atent.setData(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
   //        atent.setType(mMimeType);
 //          mPendingResult = this;
  //          startActivityForResult(atent, SELECT_AUDIO);
      	
    }
    
    class LocationServiceMgr {

        private boolean mIsGpsEnabled;
        private boolean mIsRunning;
        private boolean mIsNetworkProviderAvailable;
        private boolean mIsGpsProviderAvailable;
    	private LocationManager locationManager = null;
    	private LocationListener locationListener = null;

        private void onLocation(boolean start) {
            if (start) {
                startRecording();
            } else {
                stopRecording();
            }
        }

    	private void start(){
        	// Location section    	
      		// Acquire a reference to the system Location Manager
      		locationManager = (LocationManager) mctx.getSystemService(Context.LOCATION_SERVICE);        		
    		// Register the listener with the Location Manager to receive location updates
    		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 180000L, 500, locationListener);    		
    	    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){  
    	    	 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 180000L, 500, locationListener);
                    mIsGpsProviderAvailable = true;   
    	    } else {
    	    	 createGpsDisabledAlert(); 
    	    }
    	}
    	
    	private void stop(){
      		locationManager.removeUpdates(locationListener);
    	}
    	
    	public LocationServiceMgr(){
    		// Define a listener that responds to location updates
        	locationListener = new LocationListener() {
    		    public void onLocationChanged(Location location) {
    		      // Called when a new location is found by the network location provider.		    			    	
    		      try {
    				f.write(location.toString().getBytes("UTF8"));

    		      } catch (UnsupportedEncodingException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    		      } catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    		      }
    		    }		
    		    public void onStatusChanged(String provider, int status, Bundle extras) {}
    		    public void onProviderEnabled(String provider) {}
    		    public void onProviderDisabled(String provider) {}
    		  };
    	}
    }
        
 /**   
  * @author rob
  *
  */   
    class LocationButton extends Button {
        boolean mLocRecording = true;
        LocationServiceMgr mLocSrvMgr = null;
        OnClickListener locatclicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mLocRecording);
                // TODO location service start | stop
                if (mLocRecording) {
                	mLocSrvMgr.start();
                    setText("Stop recording");
                } else {
                	mLocSrvMgr.stop();
                    setText("Start recording");
                }
                mLocRecording = !mLocRecording;
            }
        };

        public LocationButton(Context ctx) {
            super(ctx);
            mLocSrvMgr = new LocationServiceMgr();
            setText("Start rept location");
            setOnClickListener(locatclicker);
        }
    }
    
    class CntctButton extends Button {
        boolean mStartPlaying = true;

        public CntctButton(Context ctx) {
            super(ctx);
            setText("Find Contact");
            setOnClickListener(new ResultDisplayer(
            		"Selected phone",
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE));
        }
    }
    
    class AudioButton extends Button {
        boolean mStartPlaying = true;

        public AudioButton(Context ctx) {
            super(ctx);
            setText("Find Audio");
            setOnClickListener(new AudioDisplayer());
        }
    }
      
    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }
    
    class JSONButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clickjs = new OnClickListener() {
            public void onClick(View v) {

            	String resp=connect("http://www.b2bpo.com/json.txt");
//TODO do something with the payload from the recordID selected in the array


//                mStartRecording = !mStartRecording;
            }
        };

        public JSONButton(Context ctx) {
            super(ctx);
            setText("json Catalog");
            setOnClickListener(clickjs);
        }
    }

    /**
     * Called with the activity is first created.
     */
    @Override
    //TODO add button for location control 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        
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
        if (this.getIntent().getAction().equalsIgnoreCase(ACTION_PURCHASE_SKU) ){
        	Log.i(TAG, "hav sku : d : ex  " +this.getIntent().getData() +" " +this.getIntent().getStringExtra("sku"));
        }
        
        File root =  getExternalFilesDir(null);
        try {
			f = new FileOutputStream(new File(root, "location.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(1);
        mRecordButton = new RecordButton(this);
        ll.addView(mRecordButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        mCntctButton = new CntctButton(this);
        ll.addView(mCntctButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        
        mAudButton = new AudioButton(this);
        ll.addView(mAudButton,
                new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    0));
        mLocButton = new LocationButton(this);
        ll.addView(mLocButton,
                new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    0));        
        mJSONButton = new JSONButton(this);
        ll.addView(mJSONButton,
                new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    0));                                
            //TODO hide view ll
            // find intent for play mp3
            //  call play audio 'audio'
            if (this.getIntent().getAction().equalsIgnoreCase(ACTION_PLAY_AUDIO) ) {
            	findViewById(android.R.id.content).getRootView().setVisibility(View.GONE);
            	getMimeTypes();
                playAudio( this.getIntent().getStringExtra("audio"));
                //TODO close intent and resources
            } else{
            	setContentView(ll);
            
            }
       
    }
    /**
     * play the file using logic from OI FileManager that encapsulates player logic 
     * @param audio the fileName (without file type of the media asset passed in from html link)
     */
    private void playAudio(String audio){

    	 File mydir = getExternalFilesDir(null);
    	
    	 for (String mf : mydir.list()){
    		 Log.i(TAG, "file: " +mf);
    	 } 
    	 Log.i(TAG, "opn dir fname " +mydir.getAbsolutePath() +"/" +audio+".mp3");
    	 openFile(new File(getExternalFilesDir(null), audio +".mp3"));
 //   	 checkExternalMedia();
     }
    
    /**
     * handle clicks on links to media files of indeterminant MIME type
     * when pics are linked to a media file, click event will get a File from the Link
     * Then call open on the file, thus playing the  media
     * @param aFile
     */
    
    private void openFile(File aFile) { 
   	 if (!aFile.exists()) {
   		 Log.e(TAG, "opnfile - file no exists");
//   		 Toast.makeText(this, R.string.error_file_does_not_exists, Toast.LENGTH_SHORT).show();
   		 return;
   	 }
   	 
         Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

         Uri data = FileUtils.getUri(aFile);
         String type = mMimeTypes.getMimeType(aFile.getName());
         intent.setDataAndType(data, type);
    	    
    	Log
		.v(
				TAG,
				"openFile " +
				intent.getAction() +" " +intent.getDataString());
         
         try {
       	  startActivity(intent); 
         } catch (ActivityNotFoundException e) {
        	 Log.e(TAG, "OpenFile Activity not available");
//       	  Toast.makeText(this, R.string.application_not_available, Toast.LENGTH_SHORT).show();
         };
    }
    
    private void getMimeTypes() {
   	 MimeTypeParser mtp = new MimeTypeParser();

   	 XmlResourceParser in = getResources().getXml(R.xml.mimetypes);

   	 try {
   		 mMimeTypes = mtp.fromXmlResource(in);
   	 } catch (XmlPullParserException e) {
   		 Log
   		 .e(
   				 TAG,
   				 "PreselectedChannelsActivity: XmlPullParserException",
   				 e);
   		 throw new RuntimeException(
   		 "PreselectedChannelsActivity: XmlPullParserException");
   	 } catch (IOException e) {
   		 Log.e(TAG, "PreselectedChannelsActivity: IOException", e);
   		 throw new RuntimeException(
   		 "PreselectedChannelsActivity: IOException");
   	 }
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
    
	private void createGpsDisabledAlert(){  
		AlertDialog.Builder builder = new AlertDialog.Builder(this);  
		builder.setMessage("Your GPS is disabled! Would you like to enable it?")  
		     .setCancelable(false)  
		     .setPositiveButton("Enable GPS",  
		          new DialogInterface.OnClickListener(){  
		          public void onClick(DialogInterface dialog, int id){  
		               showGpsOptions();  
		          }  
		     });  
		     builder.setNegativeButton("Do nothing",  
		          new DialogInterface.OnClickListener(){  
		          public void onClick(DialogInterface dialog, int id){  
		               dialog.cancel();  
		          }  
		     });  
		AlertDialog alert = builder.create();  
		alert.show();  
		}
	
	private void showGpsOptions(){  
        Intent gpsOptionsIntent = new Intent(  
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
        startActivity(gpsOptionsIntent);  
} 

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	int mreq = requestCode; 
        if (data != null) {
        	Log.i(LOG_TAG, "B.oar getData");
            Uri uri = data.getData();
            if (uri != null) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(uri, new String[] { BaseColumns._ID },
                            null, null, null);
                    if (c != null && c.moveToFirst()) {
                        int id = c.getInt(0);
                        if (mToast != null) {
                            mToast.cancel();
                        }
//                        String txt = mPendingResult.mMsg + ":\n" + uri + "\nid: " + id;
                        // uri is the selection 
                        // id is just the last node of the uri  
                        // the int value diff contacts select (1)  from audio select (2)
                        String txt = new Integer(mreq).toString() + ":\n" + uri + "\nid: " + id;
                        mToast = Toast.makeText(this, txt, Toast.LENGTH_LONG);
                        mToast.show();
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }
    
	private String connect(String url){
		

		// Create the httpclient
		HttpClient httpclient = new DefaultHttpClient();

        // Prepare a request object
        HttpGet httpget = new HttpGet(url); 

        // Execute the request
        HttpResponse response;

        // return string
        String returnString = null;

        try {
        	
        	// Open the webpage.
            response = httpclient.execute(httpget);
            

            if(response.getStatusLine().getStatusCode() == 200){
            
            	// Connection was established. Get the content. 

            	HttpEntity entity = response.getEntity();
                // If the response does not enclose an entity, there is no need
                // to worry about connection release

                if (entity != null) {
            
                    // A Simple JSON Response Read
                    InputStream instream = entity.getContent();

                    // Load the requested page converted to a string into a JSONObject.
                    JSONObject myAwway = new JSONObject(convertStreamToString(instream));

                    // Make array of the suggestions
                    JSONArray suggestions = myAwway.getJSONArray("suggestions");
                   //TODO get new array of sugg.lngth
                    for (int i = 0; i < suggestions.length(); i++) {
                    	
                    	JSONObject line = suggestions.getJSONObject(i);
                    	String id = line.getString("_ID");
                    	String sku = line.getString("sku");
                    	String payload = line.getString("payload");
                    	String desc = line.getString("description");
                    	String status = line.getString("status");
                    	String date = line.getString("date");
                    	returnString += "\n\t" + "id " + id +" sku " +sku +" payl " +payload + " dsc ";
                    	returnString += desc + " sts " +status +" dte " +date;
    				}
                  
                    // Close the stream.
                    instream.close();
              
            }
            else {
            	// code here for a response othet than 200.  A response 200 means the webpage was ok
            	// Other codes include 404 - not found, 301 - redirect etc...
            	// Display the response line.
            	returnString = "Unable to load page - " + response.getStatusLine();
            }
            }
            }
        catch (IOException  ex) {
        	// thrown by line 80 - getContent();
        	// Connection was not established
        	ex.printStackTrace();
        	returnString = "Connection failed; " + ex.getMessage();
        }
        catch (JSONException ex){
        	// JSON errors
        	returnString = "JSON failed; " + ex.getMessage();
        }
        return returnString;
	}

	private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CALL) {
            // a long press of the call key.
            // do our work, returning true to consume it.  by
            // returning true, the framework knows an action has
            // been performed on the long press, so will set the
            // canceled flag for the following up event.
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }
}
