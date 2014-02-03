package com.example.videoconferencing;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.example.videoconferencing.R;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends Activity implements OnClickListener
{
	TextView label;
	
	
	private Button bLogin;
	private EditText Uname;
	private EditText Pwd;
	public static String uname;
	private String pwd;
	static long replayTime;
	
	
	
	private SharedPreferences settings;
	public static String newServerIP;
	private String frequency;
	
	private ConnectivityManager connManager;
	private NetworkInfo mWifi;
	private ConnectionProgress connection;
	
	private WifiManager wimanager;
	private String mac_self;
	private DhcpInfo ip;
	private int ip_add;
	public static  String ip_self;
	
	//flag to indicate whether user has registered in server database
	private boolean userReg;
	
	DatagramPacket sendP;  //packet to send
	String send;           //first string of send packet
	
	String rdata;
	String[] pdata;
	
	DatagramPacket receiveP;
	
	
	static int serverTSPort = 5500;  //for TimeStamp message
	static int serverPort = 6500; // client will send login/call/  through this port of server
	
	//socket on which to receive the messages from server
	static DatagramSocket socket;
	
	byte[] data = new byte[100];
	byte[] data1 = new byte[100];
	byte[] data2 = new byte[100];  //it stores the received datagram packet
	Activity current;
	
	TextView tvErr;
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		replayTime = 9000000;
		setContentView(R.layout.login);
		bLogin = (Button)findViewById(R.id.bLogin);
		bLogin.setOnClickListener(this);
		Uname=(EditText)findViewById(R.id.et_Uname);
		Pwd = (EditText)findViewById(R.id.et_password);
		current = this;
		tvErr = (TextView)findViewById(R.id.tvERR);
		tvErr.setText("");
		
		
		ActionBar actionBar=getActionBar();
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.navbar));
	}
	
	
	@Override
	protected void onPause() 
	{
		super.onPause();
	}


	/**
     * method checks if there is a WiFi on device
     * if so it retrieves the IP and MAC address of the device 
     */
    public void wifiInit()
    {
    	//get the WiFi service of the device
    	wimanager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        
    	//if there is no WiFi 
        if(wimanager.equals("null"))
        {
        	Toast.makeText(getApplicationContext(),"Device doesn't have WiFi!!!\nApplication will now exit", Toast.LENGTH_LONG).show();
        	//timer(6000);
        }
        
        else
        {
        	//retrieve the MAC address of the device
        	mac_self = wimanager.getConnectionInfo().getMacAddress();
        	ip = wimanager.getDhcpInfo();
        	//IP address of device in an integer format
        	ip_add = ip.ipAddress;
        	//format this IP address to a string
        	ip_self = Formatter.formatIpAddress(ip_add);
        	
        }
    }
    
    /**
     * method opens a socket on device to receive to DataGram packets sent by server
     * it also sets the time out to socketTimeout
     */
    public void formSocket()
    {
    	try
		{
    		//open a socket with any available port on device
    		socket = new DatagramSocket();
    		//wait for a packet from server for socketTimeout no of milliseconds
    		socket.setSoTimeout(3000);
		}
		
		catch( SocketException socketException )
		{
			Toast.makeText(getApplicationContext(),"Error!!!\nApplication will now exit.2", Toast.LENGTH_LONG).show();
			//timer(5000);
		}
    }
	@Override
	public void onClick(View v) 
	{
		if(v.getId() == R.id.bLogin)
		{
			
			try
			{
				uname = (Uname.getText()).toString();
				pwd = (Pwd.getText()).toString();
				
				//retrieve default settings saved in device
		        settings = PreferenceManager.getDefaultSharedPreferences(this);
				//get the server IP saved in device
				newServerIP = settings.getString("serverIP", "10.105.14.225");
				//get the frequency saved in device
				frequency = settings.getString(frequency, "11025");
				//set the sampling rate of device to frequency
				
				//get the ConnectivityManager of device
				connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				
				//get the network information of device
				mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					
				if(mWifi.isConnected())
				{
					//wifiInit();
					try
					{
						wifiInit();
					}
					catch(Exception e)
					{
						Toast.makeText(getApplicationContext(), "err in wifi initialization" , Toast.LENGTH_SHORT).show();
					}
					//get the server time
					//connection = new ConnectionProgress();
					//connection.execute();
					try
					{
						connection = new ConnectionProgress();
						connection.execute();
					}
					catch(Exception e)
					{
						Toast.makeText(getApplicationContext(), "err in connection progress" , Toast.LENGTH_SHORT).show();
					}
				}
				else
				{
					Toast.makeText(getApplicationContext(), "Please turn on your WiFi" , Toast.LENGTH_SHORT).show();
				}
			}
			catch(Exception e)
			{
				Toast.makeText(getApplicationContext(), "error in initialization" , Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private class ConnectionProgress extends AsyncTask<Void, String, Void>
	{
		@Override
		protected Void doInBackground(Void... params) 
		{
			try
			{
				formSocket();
	    		//time request to be sent to server
	        	send="time";
	        	
	        	//DataGram packet to be sent to server
	          	sendP = new DatagramPacket(send.getBytes(),send.getBytes().length,InetAddress.getByName(newServerIP),serverTSPort);  //port 5500
	          	
	          	//send the DataGram packet to server
				socket.send(sendP);
				
				//form the DataGram to receive the packet from server
				receiveP = new DatagramPacket(data2,data2.length);
				
				//wait for the packet containing the server time
				socket.receive(receiveP);
				
				
				//convert the bytes received from server into string
				String temp=new String(receiveP.getData(),0,receiveP.getLength());
				
				//parse this server time into a long type integer in milliseconds 
				Long serverTime = Long.parseLong(temp);
				
				//send the login  message to server and set device's IP in the server
				//replay time is 90 second to consider delay  
				Long currentTime = serverTime + replayTime;
				send = "login"+" "+ip_self+" "+mac_self+" "+uname.trim()+" "+pwd.trim()+" "+String.valueOf(currentTime); /*String.toValue(currentTime)*/
				sendP = new DatagramPacket(send.getBytes(),send.getBytes().length,InetAddress.getByName(newServerIP),serverPort);  //port 6500
				
				socket.send(sendP);
				Log.i("msg","login request sent");
				//check whether user is registered onto the server or not
					receiveP = new DatagramPacket(data2,data2.length);		
					socket.receive(receiveP);
					Log.i("msg","received msg from server: ");
					//dismiss the progress dialog
					publishProgress("dismiss");
					//get the string from received bytes
					rdata = new String(receiveP.getData(),0,receiveP.getLength());
					Log.i("msg","received msg from server: "+rdata);
					pdata = rdata.split(" ");
					
					if(pdata[0].equalsIgnoreCase("registered")) //if user is registered
					{
						finish();
						//publishProgress("in");
						Intent i = new Intent(current,Home.class);
						startActivity(i);
					}
					else if(pdata[0].equalsIgnoreCase("wp"))  //if password is incorrect
					{
						publishProgress("wp");
						//tvErr.setText("*wrong password");
					}
					else if(pdata[0].equalsIgnoreCase("not")) //if user is not registered
					{
						publishProgress("not");
						//tvErr.setText("*not registered");
					}
					else if(pdata[0].equalsIgnoreCase("loggedin"))
					{
						Log.i("test","duplicate entry received");
						publishProgress("loggedin");
					}
		}
        
        catch(SocketTimeoutException ste)
        {
        	publishProgress("timeout");	
        }
        
		catch( IOException exception ) 
		{
			publishProgress("error");
		}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) 
		{
			if(values[0].equalsIgnoreCase("wp"))
			{
				tvErr.setText("*wrong password");
				//Toast.makeText(getApplicationContext(),"*wrong password",Toast.LENGTH_LONG).show();
			}
			else if(values[0].equalsIgnoreCase("not"))
			{
				tvErr.setText("*not registered");
				//Toast.makeText(getApplicationContext(),"*not registered",Toast.LENGTH_LONG).show();
			}
			else if(values[0].equalsIgnoreCase("loggedin"))
			{
				tvErr.setText("*you are already logged in");
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		menu.add(0,0,1,"Preferences");
		menu.add(0,1,2,"User Manual");
		menu.add(0,2,3,"About Us");
		menu.add(0,3,4,"Exit");
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// TODO Auto-generated method stub
		super.onOptionsItemSelected(item);
		switch(item.getItemId())
		{
		case 0:
			try 
			{
				Class ourClass = Class.forName("com.example.videoconferencing.PrefActivity");
				Intent ourIntent = new Intent(Login.this , ourClass);
				startActivity(ourIntent);
			} 
			catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
			}
			break;
			
		case 3:
			finish();
			break;
		}
		return  true;
	}


	@Override
	public void onBackPressed() 
	{
		super.onBackPressed();
		System.exit(0);
	}
	
}