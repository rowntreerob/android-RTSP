package com.b2bpo.android.chooser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collections;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


// mimetypes for file open

import com.biasedbit.efflux.packet.DataPacket;
import com.biasedbit.efflux.participant.RtpParticipant;
import com.biasedbit.efflux.participant.RtpParticipantInfo;
import com.biasedbit.efflux.session.RtpSession;
import com.biasedbit.efflux.session.RtpSessionDataListener;
import com.biasedbit.efflux.session.SingleParticipantSession;

import br.com.voicetechnology.rtspclient.RTSPClient;
import br.com.voicetechnology.rtspclient.concepts.Client;
import br.com.voicetechnology.rtspclient.concepts.ClientListener;
import br.com.voicetechnology.rtspclient.concepts.Request;
import br.com.voicetechnology.rtspclient.concepts.Response;
import br.com.voicetechnology.rtspclient.headers.SessionHeader;
import br.com.voicetechnology.rtspclient.headers.TransportHeader;


import br.com.voicetechnology.rtspclient.transport.PlainTCP;


import android.app.Activity;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import android.os.Bundle;
import android.os.Environment;



import android.util.Log;
import android.view.KeyEvent;



public class Chooseit extends Activity {
    // Request code for the contact picker activity
    private static final String TAG = "chooseit";
    public static final String ACTION_GET_RTSP = "com.b2bpo.action.GET_RTSP";
 //TODO get the audioTrack class and fill its play buffer
    //               overallBytes += track.write(buffer, 0, nBytesRead);
    
    
    class RTSPDialog implements  ClientListener {
    	BuffSink _sink;
        // for audioTRAK
        final int limit = 10;
        final int TEST_SR = 8000;
//        final int TEST_BUFFR_SZ = 3000;
//        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        int minBufferSize = 1024;
      
         final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
//        final int TEST_STREAM_TYPE = AudioManager.STREAM_VOICE_CALL;
        
        int nBytesRead = 0;
        
//        byte[] buffer = new byte[TEST_BUFFR_SZ];
        
      
        AudioTrack track = null;

        boolean started = false;
        int overallBytes = 0;
    	
        String mMsg;
        String mMimeType;
    	private  List<String> resourceList = null;   	
    	private String controlURI;
    	private int port;
//    	private final static String TARGET_URI ="rtsp://v2.cache4.c.youtube.com/CigLENy73wIaHwmcM3PVR4fLIRMYDSANFEgGUgx1c2VyX3VwbG9hZHMM/0/0/0/video.3gp";
    	private final static String TARGET_URI = "rtsp://v8.cache8.c.youtube.com/CigLENy73wIaHwlcw_gs85OUchMYDSANFEgGUgx1c2VyX3VwbG9hZHMM/0/0/0/video.3gp";
    	
    	SingleParticipantSession session1 = null;
    	private  int port_lc = 0;
    	private  int port_rm = 0;
    	private String LAN_IP_ADDR = "192.168.1.125";
    	private  String source = null;
    	private  String ssrc = null;
    	private  String session = null; 
   	
        RTSPDialog() {
        	Log.d(TAG, "new RTSPDialog constructor , getMinBfrSz");
    	  if (AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT ) > 0){
    		  minBufferSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT );
    		  Log.d(TAG, "UPD track buffr sz " + minBufferSize);
    	  } else {Log.d(TAG, " no Audio buffr " +AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT ));}
    	  
           track = new AudioTrack(TEST_STREAM_TYPE, 
          		TEST_SR, 
          		TEST_CONF, 
          		TEST_FORMAT, 
          		minBufferSize * 4, TEST_MODE);
        }
        
    	@Override
    	public void requestFailed(Client client, Request request, Throwable cause)
    	{
    		Log.e(TAG, "Request failed \n" + request);    	
    		cause.printStackTrace();
    	}

    	@Override
    	public void response(Client client, Request request, Response response)
    	{
    		String _uri = null;
    		try
    		{
    			Log.d(TAG ,"Got response: \n" + response);
    			Log.d(TAG, "for the request: \n" + request);

    			if(response.getStatusCode() == 200)
    			{
    				switch(request.getMethod())
    				{
    				// you dont want trackID=0 because that is the VIDEO track 
    				// ONLY send setup if its trackID=1
    				case DESCRIBE:
//    					Log.d(TAG, resourceList +"  opn outfile audio-AMR-2.3gpp");
    					
    					
//    					_sink = new BuffSink(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "audio-AMR-2.3gpp", 4096);    					
    				
    					if(resourceList.get(0).equals("*"))
    					{
    						controlURI = request.getURI();
    						resourceList.remove(0);
    					}
    										
    					String token = null;
    					do{ token=resourceList.remove(0);
    					  
    						if (token.endsWith("ID=1"))
    							client.setup(new URI(controlURI), nextPort(), token);
    					}while (resourceList.size() > 0);
    						
    					break;

    				case SETUP:					
    					
    					String line=response.getHeader(SessionHeader.NAME).getRawValue();
    					String[] tokenes = line.split(";");
    					session=tokenes[0];
    					TransportHeader th = new TransportHeader(response.getHeader(TransportHeader.NAME).toString());
    					//sets up next session or ends everything.
    					if(resourceList.size() > 0)
    						client.setup(new URI(controlURI), nextPort(), resourceList
    								.remove(0));
    					else
    						// streams setup after the play
    						getRTPStream(th);
    					Thread.sleep(2000);
    						client.play();
    						
    					
    					break;
    					
    				case PLAY:
    					//TODO
    					// wait for the stream to play the AUDIO before method belo
    					// need to get a listener on the event of RTP 'BYE' packet so the 'teardown' can be issued on the 'bye' event
    					Thread.sleep(20000);
    					client.teardown();
 //   					track.stop();
    					_sink.close();
    					//TODO getting npe lne belo
    					session1.terminate();
    					break;
    				}
    			} else if (response.getStatusCode() == 302){
					if ( client.getRedirect() != "") {
						_uri = client.getRedirect();
						client.setRedirect("");
						Log.d(TAG, "response following redirect " +_uri);
						
//						client.setTransport(new PlainTCP());
	//					client.setClientListener(this);
						
						client.describe(new URI(_uri));    						

					}
    			}
    			else
    				client.teardown();
    		} catch(Throwable t)
    		{
    			generalError(client, t);
    		}
    	}

    	@Override
    	public void generalError(Client client, Throwable error)
    	{
    		error.printStackTrace();
    	}

    	@Override
    	public void mediaDescriptor(Client client, String descriptor)
    	{
    		// searches for control: session and media arguments.
    		final String target = "control:";
    		Log.d(TAG, "Session Descriptor\n" + descriptor);
    		int position = -1;
    		while((position = descriptor.indexOf(target)) > -1)
    		{
    			descriptor = descriptor.substring(position + target.length());
    			resourceList.add(descriptor.substring(0, descriptor.indexOf('\r')));
    		}
    	}
    	private int nextPort()
    	{
    		return (port += 2) - 2;
    	}    	

    	
    	private void getRTPStream(TransportHeader transport){
    		
    		String[] words;
    		// only want 2000 part of 'client_port=2000-2001' in the Transport header in the response
    		
    		words = transport.getParameter("client_port").substring(transport.getParameter("client_port").indexOf("=") +1).split("-");
    		port_lc = Integer.parseInt(words[0]);
    		
    		words = transport.getParameter("server_port").substring(transport.getParameter("server_port").indexOf("=") +1).split("-");
    		port_rm = Integer.parseInt(words[0]);
    				
    		source = transport.getParameter("source").substring(transport.getParameter("source").indexOf("=") +1);			
    		ssrc = transport.getParameter("ssrc").substring(transport.getParameter("ssrc").indexOf("=") +1);
    		// assume dynamic Packet type = RTP , 99
    		getRTPStream(session, source, port_lc, port_rm, 99);
    		//getRTPStream("sessiona", source, port_lc, port_rm, 99);
    		Log.d(TAG, "raw parms " +port_lc +" " +port_rm +" " +source );
//    		String[] words = session.split(";");
    	Log.d(TAG, "session: " +session);	
    	Log.d(TAG, "transport: " +transport.getParameter("client_port") 
    			+" "  +transport.getParameter("server_port") +" "  +transport.getParameter("source") 
    			+" "  +transport.getParameter("ssrc"));
    		 
    	}
    	
    	private void getRTPStream(String session, String source, int portl, int portr, int payloadFormat ){
            // what do u do with ssrc?
    	    InetAddress addr;
    		try {
    			addr = InetAddress.getLocalHost();
    		    // Get IP Address
 //   			LAN_IP_ADDR = addr.getHostAddress();
    			LAN_IP_ADDR = "192.168.1.125";
    			Log.d(TAG, "using client IP addr " +LAN_IP_ADDR);
    		     
    		} catch (UnknownHostException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}

    		
            final CountDownLatch latch = new CountDownLatch(2);

            RtpParticipant local1 = RtpParticipant.createReceiver(new RtpParticipantInfo(1), LAN_IP_ADDR, portl, portl+=1);
     //       RtpParticipant local1 = RtpParticipant.createReceiver(new RtpParticipantInfo(1), "127.0.0.1", portl, portl+=1);
            RtpParticipant remote1 = RtpParticipant.createReceiver(new RtpParticipantInfo(2), source, portr, portr+=1);
                    
           
            remote1.getInfo().setSsrc( Long.parseLong(ssrc, 16));
            session1 = new SingleParticipantSession(session, payloadFormat, local1, remote1);
            
           Log.d(TAG, "remote ssrc " +session1.getRemoteParticipant().getInfo().getSsrc());
            
            session1.init();
            
            session1.addDataListener(new RtpSessionDataListener() {
                @Override
                public void dataPacketReceived(RtpSession session, RtpParticipantInfo participant, DataPacket packet) {
     //               System.err.println("Session 1 received packet: " + packet + "(session: " + session.getId() + ")");
                	//TODO close the file, flush the buffer
//                	if (_sink != null) _sink.getPackByte(packet);
                	getPackByte(packet);
                	
     //           	System.err.println("Ssn 1  packet seqn: typ: datasz "  +packet.getSequenceNumber()  + " " +packet.getPayloadType() +" " +packet.getDataSize());
     //           	System.err.println("Ssn 1  packet sessn: typ: datasz "  + session.getId() + " " +packet.getPayloadType() +" " +packet.getDataSize());
 //                   latch.countDown();
                }

            });
     //       DataPacket packet = new DataPacket();
      //      packet.setData(new byte[]{0x45, 0x45, 0x45, 0x45});
     //       packet.setSequenceNumber(1);
     //       session1.sendDataPacket(packet);


//        try {
       //       latch.await(2000, TimeUnit.MILLISECONDS);
     //     } catch (Exception e) {
   //         fail("Exception caught: " + e.getClass().getSimpleName() + " - " + e.getMessage());
     
 //      }
    	}
 //TODO  below should collaborate with the audioTrack object and should write to the AT buffr
    	// audioTrack write was blocking forever 
    	
   	public void getPackByte(DataPacket packet) {
    		//TODO this is getting called but not sure why only one time 
    		// or whether it is stalling in mid-exec??
    	
    		//TODO on firstPacket write bytes and start audioTrack
			// AMR-nb frames at 12.2 KB or format type 7 frames are handled . 
			// after the normal header, the getDataArray contains extra 10 bytes of dynamic header that are bypassed by 'limit'

			
			// real value for the frame separator comes in the input stream at position 1 in the data array
			// returned by 

//			int newFrameSep = 0x3c;
			// bytes avail = packet.getDataSize() - limit;

//			byte[] lbuf = new byte[packet.getDataSize()];
//			if ( packet.getDataSize() > 0)
//				lbuf = packet.getDataAsArray();
			//first frame includes the 1 byte frame header whose value should be used 
			// to write subsequent frame separators 
    		Log.d(TAG, "getPackByt start and play");
    		
    		if(!started){
				Log.d(TAG, " PLAY  audioTrak");
				track.play();
				started = true;
    		}
			
//			track.write(packet.getDataAsArray(), limit, (packet.getDataSize() - limit));
    		track.write(packet.getDataAsArray(), 0, packet.getDataSize() );
			Log.d(TAG, "getPackByt aft write");

//			if(!started && nBytesRead > minBufferSize){
	//			Log.d(TAG, " PLAY  audioTrak");
		//		track.play();
		//	started = true;}
			nBytesRead += packet.getDataSize();	
			if (nBytesRead % 500 < 375) Log.d(TAG, " getPackByte plus 5K received");
		}    	
    }
    
 // end inner cls
    /**
     * Handle the input RTP/AMR stream in a filesink.
     * This setup works on 2.3.4 and WIFI for all java imple of  youtube rtsp/rtp single media track stream request to fileSink
     * The 10 bytes of bypass needs to be fixed with actual parse of the AMR in RTP TOC struct as defined in 
     * rfc 3267 where a single packet contains multiple frames. The type of TOC is not handled by call to packet.getData()
     * You need to add logic like u see in .\media\libstagefright\rtsp\AAMRAssembler.addPacket() method .
     * @author rob
     *
     */
    private class BuffSink{
		byte[] header = new byte[]{0x23, 0x21, 0x41, 0x4D, 0x52, 0x0A };
		BufferedOutputStream obuff;
		// new File(System.getProperty("user.dir", "audio-AMR-2" ,4096), ;
		public BuffSink (File dir, String filename, int size) {
			
			try {
				obuff = new BufferedOutputStream(new FileOutputStream(new File(dir, filename)) , size);
				// file header : RFC 3267, section 5
//				String header = "#!AMR\n";
				
				obuff.write(header);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void getPackByte(DataPacket packet) {
			// AMR-nb frames need to be inserted at proper interval for 12.2 KB or format type 7 frames
			// every 32 bytes, there is 0x3c
		//	boolean firstPakFrame = true;
			
			// real value for the frame separator comes in the input stream at position 1 in the data array
			// returned by 
			int limit = 10;
			int newFrameSep = 0x3c;
			// bytes avail = packet.getDataSize() - limit;
			// write(lbuf, limit, 32)
			// write(newFrame)
			// limit += 32;
			// check packet.getDataSize() - limit > 31
			byte[] lbuf = new byte[packet.getDataSize()];
			try {
			if ( packet.getDataSize() > 0)

				lbuf = packet.getDataAsArray();
			//first frame includes the 1 byte frame header whose value should be used 
			// to write subsequent frame separators 


					obuff.write(lbuf, limit, 32);

				limit += 32;
			
		    do {

		    		 obuff.write(newFrameSep);  
					obuff.write(lbuf, limit, 31);
					limit += 31;
					
					

		    	 
		       } while (packet.getDataSize() - limit > 31);							
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void close(){
			try {
				this.obuff.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }


    /**
     * Called with the activity is first created.
     */
    @Override
    //TODO add button for location control 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        
        //TODO still hav action VIEW instead of get_rtsp
        Log.i(TAG, getIntent().getAction());
        if (this.getIntent().getAction().equalsIgnoreCase(ACTION_GET_RTSP) ){
        	Log.i(TAG, "hav sku : d : ex  " +this.getIntent().getData() +" " +this.getIntent().getStringExtra("sku"));
        }
        //TODO start the player 'audioTrack' to accept bytesBuffr on predicted codec
        
        RTSPDialog _dial = new RTSPDialog();
		RTSPClient client = new RTSPClient();
		client.setTransport(new PlainTCP());
		client.setClientListener(_dial);
		try {
			client.describe(new URI(_dial.TARGET_URI));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		_dial.resourceList = Collections.synchronizedList(new LinkedList<String>());
		// port is advertised in reply from setup?? why demand 2000
		//port = 2000;
		_dial.port = 49060;
       

       
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

