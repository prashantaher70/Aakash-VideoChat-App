package com.example.videoconferencing;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.example.videoconferencing.R;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsFrag extends Fragment implements OnClickListener
{
	EditText oldPwd,newPwd,cnfPwd;
	String OldPwd,NewPwd,CnfPwd;
	Button submit;
	
	
	//carries messages from client to server
		private DatagramPacket sendP;
		//stores received messages from server 
		private DatagramPacket receiveP;
		
		//socket on which to receive the messages from server
		private DatagramSocket socket;
		
		
		
		private int serverPort;
		
		
		//login and time messages to be sent to server
		private String send;
		//IP address of server
		private String serverIP;

		private byte data2[];
		
		//to fetch the server IP and sampling rate saved in the device
	    private SharedPreferences settings;
	    
	    String rdata;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		oldPwd = (EditText)getView().findViewById(R.id.etOldPwd);
		newPwd = (EditText)getView().findViewById(R.id.etNewPwd);
		cnfPwd = (EditText)getView().findViewById(R.id.etConfPwd);
		submit = (Button)getView().findViewById(R.id.bSubmit);
		submit.setOnClickListener(this);
		
		data2 = new byte[100];
		serverPort = 6500;
		
		//retrieve default settings saved in device
        settings = PreferenceManager.getDefaultSharedPreferences(Home.current);
		//get the server IP saved in device
		serverIP = settings.getString("serverIP", "10.105.14.225");
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.settings, container, false);
		return view;
	}
	/**
     * method opens a socket on device to receive to DataGram packets sent by server
     * it also sets the time out to socketTimeout
     */
    public void formSocket()
    {
    	try
		{
    		Log.i("test","UDP socket formed");
    		//open a socket with any available port on device
    		socket = new DatagramSocket();
    		//wait for a packet from server for socketTimeout no of milliseconds
    		socket.setSoTimeout(7000);
		}
		
		catch( SocketException socketException )
		{
			Log.d("","");
		}
    }
    public void closeSocket()
    {
    	socket.close();
    }
	@Override
	public void onClick(View v) 
	{
		if(v.getId() == R.id.bSubmit)
		{
			Log.i("test","Passwords"+OldPwd+" "+NewPwd+" "+CnfPwd);
			OldPwd = oldPwd.getText().toString();
			NewPwd = newPwd.getText().toString();
			CnfPwd = cnfPwd.getText().toString();
			Log.i("test","Passwords"+OldPwd+" "+NewPwd+" "+CnfPwd);
			new Thread(new Runnable() 
			{
				public void run() 
				{
					formSocket();
					try
					{
						send = "password"+" "+Login.uname+" "+OldPwd+" "+NewPwd+" "+CnfPwd+" ";
						sendP = new DatagramPacket(send.getBytes(),send.getBytes().length,InetAddress.getByName(serverIP),serverPort);
						socket.send(sendP);
					
				
						//check whether user is registered onto the server or not
						receiveP = new DatagramPacket(data2,data2.length);		
						socket.receive(receiveP);
						
						//get the string from received bytes
						rdata = new String(receiveP.getData(),0,receiveP.getLength());
						Log.i("test","sending free msg");
						publishProgress(rdata);
					}
					catch(Exception e)
					{
						Log.d("err in sending free msg..","");
					}
				}
			}).start();
			
		}
	}
	
	public void publishProgress(String value)
	{
		final String values = value;
		SettingsFrag.this.getActivity().runOnUiThread(new Runnable() 
		{
			
			public void run()
			{
				if(values.equalsIgnoreCase("mismatch"))
				{
					Toast.makeText(SettingsFrag.this.getActivity(),"new and confirm password mismatch" , Toast.LENGTH_SHORT).show();
				}
				else if(values.equalsIgnoreCase("change"))
				{
					Toast.makeText(SettingsFrag.this.getActivity(),"password has successfully changed" , Toast.LENGTH_SHORT).show();
				}
				else if(values.equalsIgnoreCase("wrongold"))
				{
					Toast.makeText(SettingsFrag.this.getActivity(),"enter correct old password" , Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}
