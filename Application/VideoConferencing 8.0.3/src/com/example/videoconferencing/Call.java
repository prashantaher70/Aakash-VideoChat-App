package com.example.videoconferencing;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.example.videoconferencing.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

public class Call extends Activity implements OnClickListener 
{

	static DatagramSocket socketS;
	static DatagramSocket sockR;
	static boolean record=true;
	//TextView ip,ip_2;
	static Button start,stop;
	AudioTrack audioTrack=null;
	AudioRecord audioRecord=null;      
	//String ip_address_1,ip_address_2;
	int bufferSize;
	static TextView StatusText,username;
	String callieIP;
	boolean isCalled;
	
	Socket sendPTcp;
	int tcpPort;
	ObjectOutputStream sendOut;
	//ObjectInputStream receiveIn;
	String msg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call);
		tcpPort = 8000;
		ActionBar actionBar=getActionBar();
		actionBar.hide();
		/*
		Home.audioFlag = true;
		Home.fileFlag = false;
		Home.videoFlag=false;
		*/
		Log.d("msg","in Call.java");
		callieIP = getIntent().getStringExtra("callieIP");
		isCalled = getIntent().getBooleanExtra("isCalled",false);
		
		Log.d("msg","isCalled="+isCalled+"\n"+"callieIP="+callieIP);
		
		//ip=(TextView)findViewById(R.id.Etip);
		//ip.setText(callieIP);
		//ip_2=(TextView)findViewById(R.id.et_ip_2);
		
		stop=(Button)findViewById(R.id.BStop);
		start=(Button)findViewById(R.id.BStart);
		StatusText = (TextView)findViewById(R.id.tvStatus);
		username = (TextView ) findViewById(R.id.username);
		stop.setOnClickListener(this);
		start.setOnClickListener(this);
		username.setText(Login.uname);
		try
		{
			socketS=new DatagramSocket(); //Sending Datagram socket
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		} 
		
		try
		{
			sockR=new DatagramSocket(7777); // Receiving Datagram Socket
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
		if(!isCalled)
		{
			stop.setEnabled(false);
			start.setEnabled(false);
			StatusText.setText("Connecting......");
			TcpConnection tcp = new TcpConnection();
			tcp.execute();
		}
		else
		{
			StatusText.setText("Connected");
			stop.setEnabled(false);
		}
	}

	private class TcpConnection extends AsyncTask<String, String, Void>
	{

		@Override
		protected Void doInBackground(String... params) 
		{
			try
			{
				
			//caller now busy
			Home.busy=true;
			
			Log.d("msg","sending file signal to callie....");
			
			//form a socket at caller's side
			sendPTcp = new Socket(InetAddress.getByName(callieIP),tcpPort);
			
			//open a stream to send the message
			sendOut = new ObjectOutputStream(sendPTcp.getOutputStream());
			Log.d("asfas","op stream");
			
			
			msg = "call"+" "+Login.uname+" "+"audio";
			Log.i("MESSAGE",msg);
			//send the message
			sendOut.writeObject(msg);
			sendOut.flush();
			Log.d("msg","sending "+msg);
			
			}
			catch(UnknownHostException e)
			{
				Log.d("msg","unknown host");
			}
			catch(IOException e)
			{
				Log.d("msg","I/o excepton");
			} 
			return null;
		}
		
	}
	
	private void ini()
	{
		bufferSize = AudioTrack.getMinBufferSize(22050, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		Log.i("--------Buffersize------", ""+bufferSize);
		if(bufferSize == AudioRecord.ERROR_BAD_VALUE){
			Log.i("Bad ","Bad value");
			this.finish();
		}else{
			Log.i("msg", "variables initialized");
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,22050,
					AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT ,4*bufferSize);
			audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,22050,  AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT ,4*bufferSize,AudioTrack.MODE_STREAM);
			audioTrack.setPlaybackRate(22050);
		} 
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, 0, 0, "Exit");
        return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()){
        	case 0: 
        			this.finish();
        		 	break;
        	default:break;
        }
        return super.onOptionsItemSelected(item);
    }
  

	
	public class RecordSend implements Runnable{

		DatagramPacket packet;
		String Ip;
		
		
		public RecordSend(String ip)
		{
			Ip = ip;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			byte[] buf = new byte[8192];
			audioRecord.startRecording();
			while(record){
				int brr = audioRecord.read(buf,0, 8192);
				try	{
					packet =new DatagramPacket(buf,brr,InetAddress.getByName(Ip),7777);
					Log.i("Sending to",Ip);
					socketS.send(packet);
					Log.i("Sending packet size", " " + packet.getLength());
				
				}catch (UnknownHostException e)	{
					Log.d("socket send","exception");
					e.printStackTrace();
				}catch (IOException e){
					e.printStackTrace();
				}
			}
			
		}
	}
		
	public class PlayAudio implements Runnable{

		DatagramPacket recP;
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			Log.i("playing", "Audio");	
			audioTrack.play();
			byte[] buf = new byte[8192];
			recP=new DatagramPacket(buf,buf.length);
			while(record){
				Log.i("playing", "into loop");
				try{
					Log.i("receiving","------------");
					recP.setLength(8192);
					sockR.receive(recP);
					Log.i("receiving1", "--------");
				}catch (IOException e){
					e.printStackTrace();
					break;
				}
				
				Log.i("Receive Packet size", " " + recP.getLength());
				audioTrack.write(recP.getData(), 0, recP.getLength());
				audioTrack.flush();
			}
			
			
		}
	}
	
	@Override
	public void onClick(View v)
	{
		
		if(v.getId()==R.id.BStart)
		{
			
			ini();
			//ip_address_1 = ip.getText().toString();
			
			record=true;
			stop.setEnabled(true);
			start.setEnabled(false);
			PlayAudio playTrack=new PlayAudio();
			Log.i("OnClick","PlayAudio");
			Thread p1=new Thread(playTrack);
			p1.start();
			
			
			RecordSend rec=new RecordSend(callieIP);
			Log.i("OnClick","RecordAudio");
			Thread r=new Thread(rec);
			r.start();
			
			
		}
		else if(v.getId()==R.id.BStop)
		{
			
			record=false;
			Home.busy = false;
			try
			{
				sendOut.writeObject("end call");
				sendOut.flush();
			}
			catch(Exception e)
			{
				Log.i("msg","err in sending end call");
			}
			audioRecord.stop();
			audioTrack.stop();
			stop.setEnabled(false);
			start.setEnabled(false);
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onStop();
		Log.i("MSG","-----------DESTROY");
		if(audioTrack !=null){
			audioTrack.stop();
			Log.d("closing audiotrack","destroyed...");
			audioTrack.release();
		}
		if(audioRecord !=null){
			audioRecord.stop();
			Log.d("destroyed..","audioRecord closed...");
			audioRecord.release();
		}
		if(sockR.isBound()){
			Log.d("socket received","socket unbounded destroyed...");
			sockR.close();
		}
		if(socketS.isBound()){
			Log.d("socket send","socket unbounded destroyed...");
			socketS.close();
		}
		new Thread(new SendEndMsg(getApplicationContext())).start();/*to send free me msg*/
		
	}
	
	
	public void onBackPressed()
	{
		super.onBackPressed();
		finish();
	}
}