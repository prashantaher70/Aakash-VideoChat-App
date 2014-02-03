package com.example.videoconferencing;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import com.example.videoconferencing.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactsFrag extends Fragment
{
	//address book class
	private DatabaseHandler db ;
	
	//contact list for the address book
	private ArrayList<Contact> contactList;
	
	//view to display the contact list
	private ListView listView ;
	
	static String callieUID;
	
	String rdata;

	 //confirmation dialog before deleting a contact
    private AlertDialog.Builder deldialog;
	
		//stores the time fetched from server
		private long serverTime;
		//stores the time stamp of the message
		private long currentTime;
		//time for which a message is valid
		private long replayTime;
		
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
		public ContactsFrag current;
		
		//to fetch the server IP and sampling rate saved in the device
	    private SharedPreferences settings;
	    
	    
	    /*variables to include audio/video*/
	    String ip_user;
		String typeOfComm;
		
		String UIDself;
		
		String msg; //control signal to other client
		Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		
		super.onCreate(savedInstanceState);
		current = this;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		
		/**/
		UIDself = Login.uname;
		
		
		

		//retrieve default settings saved in device
        settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		//get the server IP saved in device
		serverIP = settings.getString("serverIP", "10.105.14.225");
		
		
		//retrieve default settings saved in device
        settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		data2 = new byte[100];
		serverPort = 6500;
		
		listView=(ListView)this.getView().findViewById(R.id.onlineUsersList);
		contactList = new ArrayList<Contact>();
		db = new DatabaseHandler(this.getActivity());
		List<Contact> contacts = db.getAllContacts();//Fetching all the contacts from the database
		
		if(db.getContactsCount() == 0)//If there is no contact
        	{
				
        	}
        else
        {	
        	for (Contact cn : contacts)//Looping through all the contacts
        	{
	            	contactList.add(cn);//Adding to the contact list
        	}
        }
		
		contactAdapter ad = new contactAdapter(this.getActivity(),R.layout.onlineuser_row,contactList);
        listView.setAdapter(ad);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.contacts, container, false);
		
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
    		//open a socket with any available port on device
    		socket = new DatagramSocket();
    		//wait for a packet from server for socketTimeout no of milliseconds
    		socket.setSoTimeout(7000);
		}
		
		catch( SocketException socketException )
		{
			Toast.makeText(this.getActivity().getApplicationContext(),"Error!!!\nApplication will now exit.2", Toast.LENGTH_LONG).show();
			//timer(5000);
		}
    }
    public void closeSocket()
    {
    	socket.close();
    }
	
	
/*----------------------------------------file transfer connection----------------------------------------*/    
    
    
    private class ConnectionProgress extends AsyncTask<String, String, Void>
	{
		@Override
		protected Void doInBackground(String... id) 
		{
			try
			{
				Log.d("msg","entered in connection progress");
	    		formSocket();
	    		String callieID = id[0];
				//send the login  message to server and set device's IP in the server
				currentTime=serverTime+replayTime;
				send = "file"+" "+Login.uname+" "+callieID+" "+String.valueOf(currentTime);
				sendP = new DatagramPacket(send.getBytes(),send.getBytes().length,InetAddress.getByName(serverIP),serverPort);
				socket.send(sendP);
				
			
					//check whether user is registered onto the server or not
					receiveP = new DatagramPacket(data2,data2.length);		
					socket.receive(receiveP);
					
					//get the string from received bytes
					rdata = new String(receiveP.getData(),0,receiveP.getLength());
					/*String[] pdata = new String[10];
					pdata = rdata.split(" ");*/
					
					//if user not online
					if(rdata.equalsIgnoreCase("dull"))
					{
						Log.d("test", "offline");
						publishProgress("dull",callieID);
					}
					
					//if the roll number given by user is not in server database
					else if(rdata.equalsIgnoreCase("not"))
					{
						Log.d("test", "not registered");
						publishProgress("not");
					}
					else if(rdata.equalsIgnoreCase("busy"))
					{
						publishProgress("busy",callieID);
					}
					//if called person is online
					else
					{
						Log.d("test", "got IP");
						//publishProgress("ok",callieID,rdata);  //id[0] is receiver rdata is ip of receiver
						Intent i = new Intent(getActivity(),FileShare.class).putExtra("callieIP", rdata);
						startActivity(i);
					}
					//if called person is online
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
			super.onProgressUpdate(values);
			if(values[0].equalsIgnoreCase("dull"))
			{
				Toast.makeText(current.getActivity().getApplicationContext(),values[1] + " Not online" ,Toast.LENGTH_SHORT).show();
			}
			else if(values[0].equalsIgnoreCase("not"))
			{
				Toast.makeText(current.getActivity().getApplicationContext(),"Contact is not registered on server" ,Toast.LENGTH_LONG).show();
			}
			else if(values[0].equalsIgnoreCase("busy"))
			{
				Toast.makeText(current.getActivity().getApplicationContext(),values[1] + " is busy!!!" ,Toast.LENGTH_SHORT).show();
			}
			else if(values[0].equalsIgnoreCase("ok"))
			{
				Intent i = new Intent(getActivity(),FileShare.class).putExtra("callieIP", rdata);
				startActivity(i);
			}
		}
	}
    
/*----------------------------------------chat connection-------------------------------------------------*/
    
    private class ChatConnectionProgress extends AsyncTask<String, String, Void>
	{
		@Override
		protected Void doInBackground(String... id) 
		{
			try
			{
	    		formSocket();
	    		String callieID = id[0];
				//send the login  message to server and set device's IP in the server
				currentTime=serverTime+replayTime;
				send = "file"+" "+Login.uname+" "+callieID+" "+String.valueOf(currentTime);
				sendP = new DatagramPacket(send.getBytes(),send.getBytes().length,InetAddress.getByName(serverIP),serverPort);
				socket.send(sendP);
				
			
					//check whether user is registered onto the server or not
					receiveP = new DatagramPacket(data2,data2.length);		
					socket.receive(receiveP);
					
					//get the string from received bytes
					rdata = new String(receiveP.getData(),0,receiveP.getLength());
					/*String[] pdata = new String[10];
					pdata = rdata.split(" ");*/
					
					//if user not online
					if(rdata.equalsIgnoreCase("dull"))
					{
						Log.d("test", "offline");
						publishProgress("dull",callieID);
					}
					
					//if the roll number given by user is not in server database
					else if(rdata.equalsIgnoreCase("not"))
					{
						Log.d("test", "not registered");
						publishProgress("not");
					}
					else if(rdata.equalsIgnoreCase("busy"))
					{
						publishProgress("busy",callieID);
					}
					//if called person is online
					else
					{
						Log.d("test", "got IP");
						publishProgress("ok",callieID,rdata);  //id[0] is receiver rdata is ip of receiver
					}
					//if called person is online
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
			super.onProgressUpdate(values);
			if(values[0].equalsIgnoreCase("dull"))
			{
				Toast.makeText(current.getActivity().getApplicationContext(),values[1] + " Not online" ,Toast.LENGTH_SHORT).show();
			}
			else if(values[0].equalsIgnoreCase("not"))
			{
				Toast.makeText(current.getActivity().getApplicationContext(),"Contact is not registered on server" ,Toast.LENGTH_LONG).show();
			}
			else if(values[0].equalsIgnoreCase("busy"))
			{
				Toast.makeText(current.getActivity().getApplicationContext(),values[1] + " is busy!!!" ,Toast.LENGTH_SHORT).show();
			}
			else if(values[0].equalsIgnoreCase("ok"))
			{
				Intent chatWindow=new Intent(getActivity(),ChatWindow.class).putExtra("callieIP", rdata).putExtra("called", false);
	        	startActivity(chatWindow);
			}
		}
	}
    
 /*----------------------------------------Audio Call connection----------------------------------------*/   
    
    private class AudioConnectionProgress extends AsyncTask<String, String, Void>
	{
		@Override
		protected Void doInBackground(String... id) 
		{
			try
			{
				Log.d("msg","entered in audio connection");
	    		formSocket();
	    		String callieID = id[0];
	    		typeOfComm = id[1];
				//send the login  message to server and set device's IP in the server
				currentTime=serverTime+replayTime;
				send = "file"+" "+Login.uname+" "+callieID+" "+String.valueOf(currentTime);
				sendP = new DatagramPacket(send.getBytes(),send.getBytes().length,InetAddress.getByName(serverIP),serverPort);
				socket.send(sendP);
				
				Log.d("msg","data sent to server : "+send);
			
					//check whether user is registered onto the server or not
					receiveP = new DatagramPacket(data2,data2.length);		
					socket.receive(receiveP);
					
					
					//get the string from received bytes
					rdata = new String(receiveP.getData(),0,receiveP.getLength());
					/*String[] pdata = new String[10];
					pdata = rdata.split(" ");*/
					Log.d("msg","data received from server : "+rdata);
					//if user not online
					if(rdata.equalsIgnoreCase("dull"))
					{
						Log.d("test", "offline");
						publishProgress("dull",callieID);
					}
					
					//if the roll number given by user is not in server database
					else if(rdata.equalsIgnoreCase("not"))
					{
						Log.d("test", "not registered");
						publishProgress("not");
					}
					
					else if(rdata.equalsIgnoreCase("busy"))
					{
						Log.i("busy","received");
						publishProgress("busy",callieID);
					}
					//if called person is online
					else
					{
						Log.d("test", "got IP");
						//publishProgress("ok",callieID,rdata);  //id[0] is receiver rdata is ip of receiver
						Log.i("passing ip of remote",rdata);
						Intent i = new Intent(getActivity(),Call.class).putExtra("callieIP", rdata).putExtra("isCalled", false);
						startActivity(i);
					}
					//if called person is online
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
			super.onProgressUpdate(values);
			if(values[0].equalsIgnoreCase("dull"))
			{
				Toast.makeText(current.getActivity().getApplicationContext(),values[1] + " Not online" ,Toast.LENGTH_SHORT).show();
			}
			else if(values[0].equalsIgnoreCase("not"))
			{
				Toast.makeText(current.getActivity().getApplicationContext(),"Contact is not registered on server" ,Toast.LENGTH_LONG).show();
			}
			else if(values[0].equalsIgnoreCase("busy"))
			{
				Toast.makeText(current.getActivity().getApplicationContext(),values[1] + " is Busy :( !!!" ,Toast.LENGTH_SHORT).show();
			}
			else if(values[0].equalsIgnoreCase("ok"))
			{
				Intent i = new Intent(getActivity(),Call.class).putExtra("callieIP", rdata).putExtra("isCalled",false);
				startActivity(i);
			}
		}
	}
    
 /*----------------------------------------Video Call connection----------------------------------------*/   
    
    private class VideoConnectionProgress extends AsyncTask<String, String, Void>
	{
		@Override
		protected Void doInBackground(String... id) 
		{
			try
			{
				Log.d("msg","entered in audio connection");
	    		formSocket();
	    		String callieID = id[0];
	    		typeOfComm = id[1];
				//send the login  message to server and set device's IP in the server
				currentTime=serverTime+replayTime;
				send = "file"+" "+Login.uname+" "+callieID+" "+String.valueOf(currentTime);
				sendP = new DatagramPacket(send.getBytes(),send.getBytes().length,InetAddress.getByName(serverIP),serverPort);
				socket.send(sendP);
				
				Log.d("msg","data sent to server : "+send);
			
					//check whether user is registered onto the server or not
					receiveP = new DatagramPacket(data2,data2.length);		
					socket.receive(receiveP);
					
					
					//get the string from received bytes
					rdata = new String(receiveP.getData(),0,receiveP.getLength());
					/*String[] pdata = new String[10];
					pdata = rdata.split(" ");*/
					Log.d("msg","data received from server : "+rdata);
					//if user not online
					if(rdata.equalsIgnoreCase("dull"))
					{
						Log.d("test", "offline");
						publishProgress("dull",callieID);
					}
					
					//if the roll number given by user is not in server database
					else if(rdata.equalsIgnoreCase("not"))
					{
						Log.d("test", "not registered");
						publishProgress("not");
					}
					else if(rdata.equalsIgnoreCase("busy"))
					{
						publishProgress("busy",callieID);
					}
					//if called person is online
					else
					{
						Log.d("test", "got IP");
						//publishProgress("ok",callieID,rdata);  //id[0] is receiver rdata is ip of receiver
						Log.i("passing ip of remote",rdata);
						Intent i = new Intent(getActivity(),videoCall.PacketMain.class).putExtra("callieIP", rdata).putExtra("isCalled", false);
						startActivity(i);
					}
					//if called person is online
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
			super.onProgressUpdate(values);
			if(values[0].equalsIgnoreCase("dull"))
			{
				Toast.makeText(current.getActivity().getApplicationContext(),values[1] + " not online" ,Toast.LENGTH_SHORT).show();
			}
			else if(values[0].equalsIgnoreCase("not"))
			{
				Toast.makeText(current.getActivity().getApplicationContext(),"contact is not registered on server" ,Toast.LENGTH_LONG).show();
			}
			else if(values[0].equalsIgnoreCase("busy"))
			{
				Toast.makeText(current.getActivity().getApplicationContext(),values[1] + " is busy" ,Toast.LENGTH_SHORT).show();
			}
			else if(values[0].equalsIgnoreCase("ok"))
			{
				/*Intent i = new Intent(getActivity(),Call.class).putExtra("callieIP", rdata).putExtra("isCalled",false);
				startActivity(i);*/
			}
		}
	}
    
    


	public void showOptionDialog(String callieUID) {
		// TODO Auto-generated method stub
		final String callerUID=callieUID;
		AlertDialog.Builder optionDialog=new AlertDialog.Builder(this.getActivity());
		optionDialog.setTitle("Pick an action");
        optionDialog.setItems(R.array.option_pane, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	switch(which)
            	{
            	case 0:
            		ConnectionProgress thFileShare = new ConnectionProgress();
    	        	thFileShare.execute(callerUID);
            		break;
            	case 1:
            		AudioConnectionProgress thAudio = new AudioConnectionProgress();
    	        	thAudio.execute(callerUID,"audio");
            		break;
            	case 2:
            		VideoConnectionProgress thVideo = new VideoConnectionProgress();
    	        	thVideo.execute(callerUID,"video");
            		break;
            	}
            }
        });
        optionDialog.show();

	} 

  //This is a custom adapter to modify the list view appearance

  private class contactAdapter extends ArrayAdapter<Contact>
  {
  	Context context; // Stores the context where the list is to be shown
      int layoutResourceId;// Stores the layout ID of the custom layout of the row
      ArrayList<Contact> data = null;// stores the data for the list
      
      //Constructor for the class
      public contactAdapter(Context context, int layoutResourceId, ArrayList<Contact> data) 
      {
          super(context, layoutResourceId, data);//Calls the super class constructor
          this.layoutResourceId = layoutResourceId;
          this.context = context;
          this.data = data;
      }
      
      
      @SuppressLint("ResourceAsColor")
  	@Override
      //Over riding the get view function so the appearance looks as we want
      public View getView(int position, View convertView, ViewGroup parent) 
      {
          View row = convertView;
          ContactHolder holder = null;
          
          if(row == null)
          {
          	//Caliing the layput inflater to get the row layout from the
          	//XML file which is specified in the layoutResourceId
              LayoutInflater inflater = ((Activity)context).getLayoutInflater();
              row = inflater.inflate(layoutResourceId, parent, false);
              
              
              
              holder = new ContactHolder();
              holder.name = (TextView)row.findViewById(R.id.name);
              holder.uid = (TextView)row.findViewById(R.id.uid);
              holder.optBt=(ImageButton)row.findViewById(R.id.optBt);
              holder.delete=(ImageButton)row.findViewById(R.id.delete);
              //as row is null therefore sets the view with the text box id's
              row.setTag(holder);
              holder.optBt.setOnClickListener(new View.OnClickListener() { 
	                public void onClick(View v) { 
		                 ImageButton cb = (ImageButton) v ; 
		                 Contact contact = (Contact) cb.getTag();
		                 callieUID=contact.getuid();
		                 showOptionDialog(callieUID);
		                }
		               }); 
              holder.delete.setOnClickListener(new View.OnClickListener() { 
	                public void onClick(View v) { 
		                 ImageButton cb = (ImageButton) v ; 
		                 Contact contact = (Contact) cb.getTag();
		                 callieUID=contact.getuid();
		                 deldialog = new AlertDialog.Builder(Home.current);
			 				deldialog.setCancelable(false);
			 				deldialog.setMessage("Do you really want to delete?");
			 				deldialog.setPositiveButton("YES",new DialogInterface.OnClickListener() 
			 						{
	
			 							public void onClick(DialogInterface dialog, int which) 
			 							{
			 								db.deleteContact(callieUID);//removing the contact from the database
			 								Toast.makeText(Home.current.getApplicationContext(), "Contact Deleted", Toast.LENGTH_LONG).show();
			 								dialog.dismiss();
			 								
			 							}
			 							
			 							
			 						});
			 			        	
			 			        	deldialog.setNegativeButton("NO", new DialogInterface.OnClickListener() 
			 			        	{
	
			 							public void onClick(DialogInterface arg0, int which) 
			 							{
			 								
			 								arg0.cancel();
			 								
			 							}
			 							
			 							
			 						});
			 			        	
			 			       deldialog.show();
		                }
		               }); 
              
          }
          else
          {
          	//as row is already set , therefore getting back the row's holder 
              holder = (ContactHolder)row.getTag();
          }
          
          //setting the holder with data
          Contact contact = data.get(position);
          holder.name.setText(contact.name);
          holder.uid.setText(contact.uid);
          holder.optBt.setTag(contact);
          holder.delete.setTag(contact);
          //row.setBackgroundResource(R.color.list_color_normal);
          View colorStrip=(View) row.findViewById(R.id.colorStrip);
          if(position%6 ==0){
          	colorStrip.setBackgroundResource(R.color.color1);
          }
          else if(position%6 ==1){
          	colorStrip.setBackgroundResource(R.color.color2);
          }
          else if(position%6 ==2){
          	colorStrip.setBackgroundResource(R.color.color3);
          }
          else if(position%6 ==3){
            	colorStrip.setBackgroundResource(R.color.color4);
            }
          else if(position%6 ==4){
            	colorStrip.setBackgroundResource(R.color.color5);
            }
          else{
          	colorStrip.setBackgroundResource(R.color.color6);
          }
          return row;
      }
      
      
      
  }
  
	  //Holder class
	  static class ContactHolder
	  {
	  	TextView name;
	    TextView uid;
	    ImageButton optBt;
	    ImageButton delete;
	  }

}