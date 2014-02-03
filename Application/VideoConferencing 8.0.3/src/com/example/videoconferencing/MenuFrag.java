package com.example.videoconferencing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.example.videoconferencing.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MenuFrag extends ListFragment
{

	String send, pdata[], rdata;
	DatagramPacket sendP, receiveP;
	ProgressDialog progressBar;
	public MenuFrag current;
	byte[] data1 = new byte[100];
	byte[] data2 = new byte[100];
	
	String[] classes={"Home","Contacts","Group Chat","Audio Conferencing","Settings","Log Out"};
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		setListAdapter(new customList(this.getActivity()));
	}
	
	//------------------------
	@SuppressWarnings("rawtypes")
	class customList extends ArrayAdapter
	{
				
		Activity current;
		@SuppressWarnings("unchecked")
		public customList(Activity context) 
		{
			super(context,R.layout.xml_row_menu,classes);
			// TODO Auto-generated constructor stub
			current= context;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			// TODO Auto-generated method stub
			View reqRow = convertView;
			if(reqRow==null)
			{
				reqRow=current.getLayoutInflater().inflate(R.layout.xml_row_menu,null);
			}
			
			ImageView image=(ImageView)reqRow.findViewById(R.id.row_image);
			TextView txt=(TextView)reqRow.findViewById(R.id.listLabel);
			txt.setText(classes[position]);
			switch(position)
			{
				case 0:
					image.setImageResource(R.drawable.home);
					break;
				case 1:
					image.setImageResource(R.drawable.contacts);
					break;
				case 2:
					image.setImageResource(R.drawable.groupchat);
					break;
				case 3:
					image.setImageResource(R.drawable.audioconf);
					break;	
				
				case 4:
					image.setImageResource(R.drawable.settings);
					break;
				case 5:
					image.setImageResource(R.drawable.logout);
					break;
			}
			return reqRow;
		}
		
	}

	//------------------------
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		
		android.app.FragmentManager fragmentManager = getFragmentManager();  
        android.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		switch(position)
		{
			case 0:
				ProfileFrag newProfFrag=new ProfileFrag();
				fragmentTransaction.replace(R.id.contents,newProfFrag);  
				fragmentTransaction.commit();
				break;
			case 1:
				ContactsFrag newContFrag=new ContactsFrag();
				fragmentTransaction.replace(R.id.contents,newContFrag);  
				fragmentTransaction.commit();
				break;
			case 2:
				Group_chat_list newGroupFrag=new Group_chat_list();
				fragmentTransaction.replace(R.id.contents,newGroupFrag);  
				fragmentTransaction.commit();
				break;
			case 3:
				Group_audio_conference_call newConfFrag=new Group_audio_conference_call();
				fragmentTransaction.replace(R.id.contents,newConfFrag);  
				fragmentTransaction.commit();
				break;
				
			case 4:
				SettingsFrag newSetFrag=new SettingsFrag();
				fragmentTransaction.replace(R.id.contents,newSetFrag);  
				fragmentTransaction.commit();
				break;
			case 5:
				current = this;
				/*if(Home.record)
					Toast.makeText(getActivity(), "OOPS!!! End Audio Conference call first.\n\tThen Logout.", Toast.LENGTH_LONG).show();
				else{
					Logout logout = new Logout();
					logout.execute();
				}*/
				Logout logout = new Logout();
				logout.execute();
				break;
		}

	}
	
	
	public class Logout extends AsyncTask<Void, String, Void>
	{

		@Override
		protected void onPreExecute() 
		{
			// TODO Auto-generated method stub
			super.onPreExecute();
			Log.i("msg", "onPreExecute");
			progressBar = new ProgressDialog(getActivity());
			progressBar.setCancelable(false);
			progressBar.setMessage("Logging Out ...");
			progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressBar.show();
		}
		
		@Override
		protected Void doInBackground(Void... arg0) 
		{
			
			Log.i("msg", "doinback");
			send="time";
        	
        	//DataGram packet to be sent to server
          	try 
          	{
				sendP = new DatagramPacket(send.getBytes(),send.getBytes().length,InetAddress.getByName(Login.newServerIP),Login.serverTSPort);
			} 
          	catch (UnknownHostException e2) 
          	{
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}  //port 5500
          	
          	//send the DataGram packet to server
			try {
				Login.socket.send(sendP);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//form the DataGram to receive the packet from server
			receiveP = new DatagramPacket(data2,data2.length);
			
			//wait for the packet containing the server time
			try {
				Login.socket.receive(receiveP);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			//convert the bytes received from server into string
			String temp=new String(receiveP.getData(),0,receiveP.getLength());

			
			
			Long serverTime = Long.parseLong(temp);
			
			//send the login  message to server and set device's IP in the server
			//replay time is 90 second to consider delay  
			Long currentTime = serverTime + Login.replayTime;
			send = "logout"+" "+Login.uname.trim();
			try {
				sendP = new DatagramPacket(send.getBytes(),send.getBytes().length,InetAddress.getByName(Login.newServerIP),Login.serverPort);
				Login.socket.send(sendP);
				
				Login.socket.receive(receiveP);
				publishProgress("dismiss");
				rdata = new String(receiveP.getData(),0,receiveP.getLength());
				pdata = rdata.split(" ");
				if(pdata[0].equals("logout"))
				{
					
					try
					{
						Login.socket.close();
					}
					catch(Exception e)
					{
						Log.i("test","err in closing UDP socket in logout method");
					}
					Intent i = new Intent(getActivity(), Login.class);
					startActivity(i);
					
					publishProgress("logout");
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
			catch (IOException e) {
				// TODO Auto-generated catch block
				publishProgress("error");
			}
			
			/*
			try
			{
				Log.i("Menufrag Doing","On logout");
				if(Home.TcpSocket != null)
					Home.TcpSocket.close();
				if(Home.TcpSocketV != null)
					Home.TcpSocketV.close();
				if(Home.TcpSocketF != null)
					Home.TcpSocketF.close();
				if(ReceiveFile.socket != null)
					ReceiveFile.socket.close();
				if(ReceiveFile_Group_Thread.server_socket!=null)
					ReceiveFile_Group_Thread.server_socket.close();
				if(group_message_receive_thread.Group_chat_socket==null)
					group_message_receive_thread.Group_chat_socket.close();
				Log.i("Menufrag Doing","On logout done with sockets");
			}
			catch(Exception e)
			{
				Log.d("err in closing server sockets","onDestroy of Home");
			}
			//closing sockets and audio elements on destroy
			
			if(Home.audioTrack !=null){
				Home.audioTrack.stop();
				Log.i("closing audiotrack","destroyed...");
				Home.audioTrack.release();
			}
			if(Home.audioRecord !=null){
				Home.audioRecord.stop();
				Log.i("destroyed..","audioRecord closed...");
				Home.audioRecord.release();
			}
			if(Home.sockR!=null){
				Log.i("socket received","socket unbounded destroyed...");
				Home.sockR.close();
			}
			if(Home.socketS!=null){
				Log.i("socket send1","socket unbounded destroyed...");
				Home.socketS.close();
			}*/
			return null;
		}
	
		
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			
			if(values[0].equalsIgnoreCase("logout"))
			{
				Toast.makeText(getActivity(),"You have Successfully Logged Out",Toast.LENGTH_SHORT).show();
				Home.mHandler.sendEmptyMessage(999);
			}
			else if(values[0].equalsIgnoreCase("dismiss"))
			{
				progressBar.dismiss();
				progressBar = null;
			}
			else if(values[0].equalsIgnoreCase("error"))
			{
				Toast.makeText(getActivity(),"Connection Error\nPlease try again.",Toast.LENGTH_LONG).show();
			}
		}
	}
	
}
	
