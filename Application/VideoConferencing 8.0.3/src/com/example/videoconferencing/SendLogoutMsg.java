package com.example.videoconferencing;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class SendLogoutMsg implements Runnable 
{
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
    
    public SendLogoutMsg(Context context)
    {
    	Log.d("test","in SendlogoutMsg constructor");
    	//retrieve default settings saved in device
        settings = PreferenceManager.getDefaultSharedPreferences(context);
		data2 = new byte[100];
		serverPort = 6500;
		
		//retrieve default settings saved in device
        settings = PreferenceManager.getDefaultSharedPreferences(context);
		//get the server IP saved in device
		serverIP = settings.getString("serverIP", "10.105.14.225");
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
	public void run() 
	{
		formSocket();
		try
		{
			send = "logout"+" "+Login.uname;
			sendP = new DatagramPacket(send.getBytes(),send.getBytes().length,InetAddress.getByName(serverIP),serverPort);
			socket.send(sendP);
		
	
			//check whether user is registered onto the server or not
			receiveP = new DatagramPacket(data2,data2.length);		
			socket.receive(receiveP);
			
			//get the string from received bytes
			rdata = new String(receiveP.getData(),0,receiveP.getLength());
			if(rdata.equalsIgnoreCase("logout"))
				Log.d("test","send and received logout msg");
		}
		catch(Exception e)
		{
			Log.d("err in sending end call msg..","");
		}
	}
}
