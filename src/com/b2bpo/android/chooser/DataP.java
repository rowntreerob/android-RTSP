package com.b2bpo.android.chooser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class DataP { 
	  // I know this could be an int, but this is just to show you how it works 
	public String albumId; 
	public String photoId; 
	public String time;
	private String TAG = "DataP";
	  
	  public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public String getPhotoId() {
		return photoId;
	}

	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}

	public String getTime() {
		return time;
	}
/*
 * parm should be a string suitable for parseing to a type long suitable for Date constructor 
 * date is then converted to Zone=UTC where it matches zone used on GPS (all UTC)
 * sample parm:  <gphoto:timestamp>1305284170000
 * Note: the phone and the camera BOTH need to be configged on the same timeZone
 */
	public void setTime(String time) {
		Log.d(TAG, "setTime arg " +time);
//		this.time = time;
		Date dte = new Date(Long.parseLong(time));
		// belo converts Date to UTC in 'gmt' timezone ; try just switching the sdf zone
		Date ndte = cvtToGmt(dte);
		// line belo saves iso8601 type string w/ value of the GMT zone equivalent of photo's timestamp passed in as string
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
		sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
    	this.time = (sdf).format(ndte);
	}
	
	/* 
	 * handles string of type "yyyy-MM-dd'T'HH:mm:ss z" 
	 */
	
	public void setTime2(String time) {
	    	
		this.time = time;
	}

	 
	  public DataP() { 
		  albumId = "";
		  photoId = "";
		  time = "";
	  }
	  
	  public DataP(JSONObject json){
		  
		  try {
			  Log.d("dataP", "2nd construct tm" +json.getString("time"));
			setTime2(json.getString("time"));
			setPhotoId(json.getString("id"));
			setAlbumId(json.getString("albumId"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	  
	  
	  public String toString(){
		  return "id " + getPhotoId() + " albumId " +getAlbumId() + " time " +getTime(); 
		  
	  }
}
