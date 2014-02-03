package com.example.videoconferencing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.example.videoconferencing.R;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GroupchatActivity extends Activity implements OnClickListener{
	//TextView tv_Users;
	EditText et_message;
	String callieIP;
	Button btn_send,btn_browse,bGroupSend;
	//ImageButton bGroupSend;
	String message,filePath;
	public static Handler mHandler;
	boolean isCalled,stop=true,file_name_flag;
	public static String grp_message="",new_message,left_user="",set_notification;
	public static boolean groupchat_activity_flag;
	public String compose_message,dt;
	private static int no_of_files_send_threads=0;
	String user_in_group="";
	private static ListView chat_list,onlineChatList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.groupchat);
		
		//tv_Users = (TextView) findViewById(R.id.tv_recvr_name_group);
		et_message = (EditText) findViewById(R.id.et_message_group);
		btn_send = (Button) findViewById(R.id.bt_send_group);
		btn_browse = (Button) findViewById(R.id.bBrowse);
		bGroupSend= (Button) findViewById(R.id.file_send_group);
		btn_send.setOnClickListener(this);
		btn_browse.setOnClickListener(this);
		bGroupSend.setOnClickListener(this);
		
		
		chat_list=(ListView)findViewById(R.id.chatList);
		onlineChatList=(ListView)findViewById(R.id.onlineChatList);
		
		ArrayList<msgData> msgList =new ArrayList<GroupchatActivity.msgData>();
		msgAdapter ad = new msgAdapter(this,R.layout.msg_row,msgList);
		chat_list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		chat_list.setStackFromBottom(true);
        chat_list.setAdapter(ad);
        
        ArrayList<String> onlineUserList =new ArrayList<String>();
        
		for (Map.Entry<String, String> entry : Home.UID_IP.entrySet())
		{
			user_in_group+=entry.getKey()+" ";
			onlineUserList.add(entry.getKey());
		}
		ArrayAdapter<String> onlineUsers=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,onlineUserList);
		
		user_in_group="ONLINE  : " +user_in_group;
		//tv_Users.setTypeface(null, Typeface.BOLD_ITALIC);
		//tv_Users.setText(user_in_group);
		//Toast.makeText(getApplicationContext(), "IP: "+callieIP, Toast.LENGTH_LONG).show();
		onlineChatList.setAdapter(onlineUsers);
		
		groupchat_activity_flag=true;
		
		Log.i("GRPChat "," "+groupchat_activity_flag);
		
		for (String entry : Home.PREV_GRP_MSG)
		{
			String[] dt_uid=entry.split("\\s", 4);
			ArrayAdapter<GroupchatActivity.msgData> adapter=(ArrayAdapter<GroupchatActivity.msgData>)chat_list.getAdapter();
			adapter.add(new msgData(dt_uid[2],dt_uid[3]));
			adapter.setNotifyOnChange(true);
		}
		
		
		
		
		//tv_message.append(grp_message);		old messages appending-----------------------------------
		mHandler = new Handler() {
            public void handleMessage(Message msg) {
            	Date date =new Date();
				SimpleDateFormat sdf=new SimpleDateFormat("MMM d,HH:mm");
				dt=sdf.format(date);//dt is stored here
				
				
            
            	if(msg.what==0){
            		if(new_message!=null){
            			Log.i("Group chat act"," "+new_message);
            			String[] token = new_message.split("\\s",4);
            			//set my date here
            			ArrayAdapter<GroupchatActivity.msgData> adapter=(ArrayAdapter<GroupchatActivity.msgData>)chat_list.getAdapter();
            			adapter.add(new msgData(token[2],token[3]));
            			adapter.setNotifyOnChange(true);
            		}
            	}
            	else if(msg.what == 1){                   // User leaving group chat room
            		ArrayAdapter<GroupchatActivity.msgData> adapter=(ArrayAdapter<GroupchatActivity.msgData>)chat_list.getAdapter();
        			adapter.add(new msgData(dt+" "+left_user,"Left the chat Room"));
        			
        			ArrayAdapter<String> ad=(ArrayAdapter<String>)onlineChatList.getAdapter();
        			ad.remove(left_user);
        			ad.notifyDataSetChanged();
        			
        			//user_in_group=user_in_group.replace(left_user+" ", "");
        			Home.PREV_GRP_MSG.add(dt+" "+left_user+" "+"Left the chat Room");
            		//tv_Users.setText(user_in_group);
        			adapter.setNotifyOnChange(true);
            	}
            	else if(msg.what==2){
            		ArrayAdapter<GroupchatActivity.msgData> adapter=(ArrayAdapter<GroupchatActivity.msgData>)chat_list.getAdapter();
        			adapter.add(new msgData(dt+" "+"Error:"+left_user,"Left the chat Room"));
        			adapter.setNotifyOnChange(true);
        			
        			ArrayAdapter<String> ad=(ArrayAdapter<String>)onlineChatList.getAdapter();
        			ad.remove(left_user);
        			ad.notifyDataSetChanged();
        			
        			Home.PREV_GRP_MSG.add(dt+" "+"Error:"+left_user+" "+"Left the chat Room");
            	}
            	else if(msg.what==3){
            		ArrayAdapter<GroupchatActivity.msgData> adapter=(ArrayAdapter<GroupchatActivity.msgData>)chat_list.getAdapter();
        			adapter.add(new msgData(dt+" "+"RECEIVINGFILE",set_notification));
        			adapter.setNotifyOnChange(true);
            	}
           	}
        };
     }


	@Override
	public void onClick(View v) {
		
		if(v.getId() == R.id.bt_send_group)
		{
			
			if(Home.UID_IP.size()>0){
				if (et_message.getText().toString().equals("")||et_message.getText().toString().equals(null))
				{
					Toast.makeText(getApplicationContext(), "Enter message !!!!", Toast.LENGTH_SHORT).show();
				}
				else 
				{
					Date date =new Date();
					SimpleDateFormat sdf=new SimpleDateFormat("MMM d,HH:mm");
					dt=sdf.format(date);
					String sent_msg=et_message.getText().toString().trim();
					compose_message=dt+" "+Login.uname+" "+sent_msg;
					Log.i("GCA----",compose_message);
					message = "MSG "+Home.group_chat_code+" "+compose_message;
					
	
						//Appending message to text view
							//tv_message.append(compose_message);
							ArrayAdapter<GroupchatActivity.msgData> adapter=(ArrayAdapter<GroupchatActivity.msgData>)chat_list.getAdapter();
	            			adapter.add(new msgData(Login.uname,sent_msg));
	            			adapter.setNotifyOnChange(true);
	            			Home.PREV_GRP_MSG.add(dt+" "+Login.uname+" "+sent_msg);
	            			et_message.setText("");
							
							
					//Sending the message to each thread to deleiver to online user		
					for (Map.Entry<String, String> entry : Home.UID_IP.entrySet()) 
					{
						if(!entry.getKey().equals(Login.uname))
							{						
							SendMessage outoing_messages = new SendMessage(entry.getValue(),message,35012);
							Thread y = new Thread(outoing_messages);
							y.start(); 
				
				    	}else {
				    		Log.i("GroupChat", "UID null!!!!!");
				    	}	
					}//lood for sending message to each user		
				}//
			}// First if to check no user online or every one left
			else
				Toast.makeText(getApplicationContext(), "It seems that Everyone Left the Chat.", Toast.LENGTH_LONG).show();
		}
		
		if(v.getId()==R.id.bBrowse){
			Intent file_Manager=new Intent(this,com.example.videoconferencing.Explorer.class);
			startActivityForResult(file_Manager, 1);
			
			
			
		}
		if(v.getId()==R.id.file_send_group){
			Date date =new Date();
			SimpleDateFormat sdf=new SimpleDateFormat("MMM d,HH:mm");
			dt=sdf.format(date);
			if(no_of_files_send_threads==0){
				ArrayAdapter<GroupchatActivity.msgData> adapter=(ArrayAdapter<GroupchatActivity.msgData>)chat_list.getAdapter();
				adapter.add(new msgData("SENDING FILE",filePath));
				adapter.setNotifyOnChange(true);
				Home.PREV_GRP_MSG.add(dt+" "+"SENDING FILE"+" "+filePath);

					for (Map.Entry<String, String> entry : Home.UID_IP.entrySet()) 
					{
						if(!entry.getKey().equals(Login.uname))
						{	
							no_of_files_send_threads++;
							SendFile_Group_Thread outoing_file = new SendFile_Group_Thread(entry.getValue(),filePath,35021);
							Thread z = new Thread(outoing_file);
							z.start(); 
				
				    	}else {
				    		Log.i("GroupChat", "UID null!!!!!");
				    	}	
					}
			}
			else
				Toast.makeText(this, "Already Sending a Files!!!", Toast.LENGTH_LONG).show();
		}
}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

			
			if(resultCode==RESULT_OK){
				if(requestCode==1){
						file_name_flag=true;
							filePath=data.getStringExtra("filePath");
							bGroupSend.setVisibility(1);
							Toast.makeText(getApplicationContext(), "Selected : "+filePath, Toast.LENGTH_SHORT).show();
							bGroupSend.setVisibility(1);
				}
				else{
					file_name_flag=false;
					Toast.makeText(getApplicationContext(), "No File Selected", Toast.LENGTH_SHORT).show();
					bGroupSend.setVisibility(0);
				}
			}
		
	}
	synchronized public static void chng_when_send_done() {
		
		no_of_files_send_threads--;
	
		Log.i("RFGTHREAD completed"," "+no_of_files_send_threads);
		
		if(no_of_files_send_threads<=0){
			no_of_files_send_threads=0;
			//successful message
		}
	}


public class SendMessage implements Runnable{
		String ip_address;
		ObjectOutputStream oos=null;
		ObjectInputStream ois=null;
		Socket socket = null;
		int SENDMESSAGEPORT;
    	String message;
    	
    	//CONSTRUCTOR FOR THREAD
		public SendMessage(String ip,String msg,int port) {
			this.ip_address = ip;
			this.message = msg;
			this.SENDMESSAGEPORT = port;
	}

		public void run() {
	 		try{
	 			Log.i("GroupChatActivity Thread", " Inside Socket");
	 			socket = new Socket(ip_address,SENDMESSAGEPORT);
	 			Log.i("GroupChatActivity Thread", " Socket Created oos and ois "+socket);
	 		} catch (UnknownHostException e) {
	 			Log.i("Object","oos and ois not created");
	 		} catch (IOException e) {
	 			e.printStackTrace();
	 		} 
	 		if(socket!=null){
	 			try{
		 			oos = new ObjectOutputStream(socket.getOutputStream());
		 			ois = new ObjectInputStream(socket.getInputStream());
		 			Log.i("ChatGroupActivity Thread", " OOS"+message+" with IP: "+ip_address);
		 			oos.writeObject(message);
		 			oos.flush();
		 			oos.close();
		 			ois.close();
	 			}
	 			
		 		catch (IOException e) {
		 			e.printStackTrace();
		 			}
		 		finally{
		 			if(!socket.isClosed()){
		 				try {
		 					Log.i("groupchat_activity", "Closing socket");
		 					socket.close();
		 				} catch (IOException e) {
		 					e.printStackTrace();
		 				}							   
		 			}
		 		}//FINALLY
	 		}//IF SOCKET NOT NULL CLOSED
		}//RUN METHOD CLOSED
}//CLASS CLOSED FOR SENDING MESSAGE	
	
@Override
public void onBackPressed() {
	super.onBackPressed();	
}	

	@Override
	protected void onResume() {
		super.onResume();
		groupchat_activity_flag=true;
	}




	@Override
	protected void onPause() {

		
		super.onPause();
		groupchat_activity_flag=false;
	}
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		groupchat_activity_flag=false;
	}
	
	

	
	public class msgAdapter extends ArrayAdapter<msgData>
	  {
	  		Context context; // Stores the context where the list is to be shown
	  		int layoutResourceId;// Stores the layout ID of the custom layout of the row
	  		ArrayList<msgData> data = null;// stores the data for the list
	      
	      //Constructor for the class
	      public msgAdapter(Context context, int layoutResourceId, ArrayList<msgData> msgList) 
	      {
	          super(context, layoutResourceId, msgList);//Calls the super class constructor
	          this.layoutResourceId = layoutResourceId;
	          this.context = context;
	          this.data = msgList;
	      }
	      
	      
	      @SuppressLint("ResourceAsColor")
	  	@Override
	      //Over riding the get view function so the appearance looks as we want
	      public View getView(int position, View convertView, ViewGroup parent) 
	      {
	          View row = convertView;
	          msgHolder holder = null;
	          
	          if(row == null)
	          {
	          	//Caliing the layput inflater to get the row layout from the
	          	//XML file which is specified in the layoutResourceId
	              LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	              row = inflater.inflate(layoutResourceId, parent, false);
	              
	              holder = new msgHolder();
	              holder.uname = (TextView)row.findViewById(R.id.tvuname);
	              holder.msg = (TextView)row.findViewById(R.id.tvmsg);
	              row.setTag(holder);
	           }
	          else
	          {
	              holder = (msgHolder)row.getTag();
	          }
	          msgData msg = data.get(position);
	          holder.uname.setText(msg.getUname());
	          holder.msg.setText(msg.getMsg());
	          row.setBackgroundResource(R.drawable.bubble_yellow);
			return row;
	          
	      }
	      
	      
	      
	  }
	  
	  //Holder class
	  static class msgHolder
	  {
	  	TextView uname;
	    TextView msg;
	  }
	  
	  public class msgData
	  {
		  private String uname;
		  private String msg;
		  public void setMsg(String msg)
		  {
			  this.msg=msg;
		  }
		  public String getMsg()
		  {
			  return this.msg;
		  }
		  public void setUname(String uname)
		  {
			  this.uname=uname;
		  }
		  public String getUname()
		  {
			  return this.uname;
		  }
		  public msgData(String Uname,String msg)
		  {
			  this.msg=msg;
			  this.uname=Uname;
		  }
	  }
	
	

}