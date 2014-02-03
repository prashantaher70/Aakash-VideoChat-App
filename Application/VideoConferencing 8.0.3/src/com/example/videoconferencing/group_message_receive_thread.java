package com.example.videoconferencing;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class group_message_receive_thread extends Thread
{
	boolean called = false;
	private Context context_home;
	static ServerSocket GroupchatServerSocket;
	static Socket Group_chat_socket;
	private static int chatPORT;
	static Activity activity;
	static ObjectOutputStream oos=null;
	static ObjectInputStream ois=null;
	
	public group_message_receive_thread(int CHATPORT,Context context){
		this.chatPORT=CHATPORT;
		this.context_home = context; 
	}
	public static void setActivity(Activity act)
	{
		activity = act;
	}

	public static ObjectInputStream getOis()
	{
		return ois;
	}
	public static ObjectOutputStream getOos()
	{
		return oos;
	}
	
	public void run(){
		
		
		try {
			
			GroupchatServerSocket = new ServerSocket(chatPORT,1);
			Log.i("GMR_Server Started","GMR_Chat Server Started ");
			if(GroupchatServerSocket!=null && !GroupchatServerSocket.isClosed()){		
				while(true)
				{
					Group_chat_socket = GroupchatServerSocket.accept();
					Log.i("GMR_Sever Accepted","accepted");
					saveMessages_append(Group_chat_socket);
				}
		}
			else
				Log.i("GMR_Error","Port Already Bind Close Reconnect");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i("GMR_Sever Accepted","IO");
			e.printStackTrace();
		} catch (Exception e) {
			
			Log.i("GMR_Sever Accepted","EX");
			e.printStackTrace();
		}
		finally{
			Log.i("GMR_FAIL","Is closed check");
			Group_chat_socket=null;
			if(GroupchatServerSocket!=null && !GroupchatServerSocket.isClosed()){
				try {
					Log.i("GMR_FAIL","Closing");
					GroupchatServerSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.i("GMR_FAIL","Fail to close socket");
					e.printStackTrace();
					
				}
			}
		}		
	}
	
	private void saveMessages_append(Socket chat_client) throws Exception {
		
		// Declaring ObjectOutputStream to write data and ObjectInputStream to read data
		oos = new ObjectOutputStream(chat_client.getOutputStream());
		ois = new ObjectInputStream(chat_client.getInputStream());
	    
		// 1. Read whether the client wants to chat or share file.
		final Object o = ( (ObjectInput) ois).readObject();  
		if (o instanceof String) {
						if(o.toString().startsWith("LIST")){    //if it is request for new group chat and i am not envolved into a group chat
							if(!Home.IP_UID_SET_FLAG){
								Home.IP_UID_SET_FLAG= true;
								
								String[] token = o.toString().split("\\s",22);
								final String starter=token[token.length-2];
								Home.current.runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										Toast.makeText(Home.current,starter+" : Started a Group Chat. Press Show Chat to Reply.\nPress exit chat to End!!!" ,Toast.LENGTH_LONG).show();
										
									}
								});
								
								Log.i("GMR_LIST","in list ip ");
									Home.group_chat_code=token[1];
								for(int i=2;i<token.length;i++){
									Home.UID_IP.put(token[i], token[++i]);
								}
							}
							//set the user name and ip in the static variable of Home.UID_IP HASHMAP such that there is no activity going on
							//else reply with an exit message
						}					
						else if(o.toString().startsWith("MSG")){
							Log.i("GMR_MSG","in MSG---- "+o.toString());
							Log.i("GMR_MSG","in MSG---- Chat code----"+Home.group_chat_code+" Home flag"+Home.home_flag_status+"GCA flag"+GroupchatActivity.groupchat_activity_flag);
							//dt(2,3) from(4) ,msg(5)
							String[] token = o.toString().split("\\s",6);
							if(Home.group_chat_code.equals(token[1])){
								if(Home.home_flag_status == true){
									Home.PREV_GRP_MSG.add(token[2]+" "+token[3]+" "+token[4]+" "+token[5]);
									Home.mHandler.sendEmptyMessage(0);							
								}
								else if(GroupchatActivity.groupchat_activity_flag==true)
								{
										Log.i("Chat_Socket","Chat window flag true");
										Home.PREV_GRP_MSG.add(token[2]+" "+token[3]+" "+token[4]+" "+token[5]);
										GroupchatActivity.grp_message+=token[2]+" "+token[3]+" "+token[4]+" "+token[5];
										GroupchatActivity.new_message=token[2]+" "+token[3]+" "+token[4]+" "+token[5];
										GroupchatActivity.mHandler.sendEmptyMessage(0);						
								}
								else if(Home.group_chat_code.equals(false) && !GroupchatActivity.groupchat_activity_flag)
								{
									Home.PREV_GRP_MSG.add(token[2]+" "+token[3]+" "+token[4]+" "+token[5]);
								}
							}//MSG from
							else
							{
								//the message code does not match and user is already present in the online list of the chat
									//something wrong happened may be the receiving thread were close by the system
									Send_exit_message s_e_m = new Send_exit_message(chat_client.getInetAddress().getHostAddress(),Login.ip_self,Login.uname,35012);
									Thread sem = new Thread(s_e_m);
									sem.start();
									if(Home.UID_IP.containsKey(token[2]));
										Home.UID_IP.remove(token[2]);
							}
						}
						else if(o.toString().startsWith("EXIT"))   //when a user left group chat
						{
							String[] token = o.toString().split("\\s",4);
							if(Home.IP_UID_SET_FLAG){
								if(Home.group_chat_code.equals(token[1])){
									Log.i("GMR_EXIT","USER"+token[2]+token[3]+" Left");
									
									if(Home.home_flag_status == true){
										Home.left_user = token[2];
										Home.mHandler.sendEmptyMessage(1);							
									}
									else if(GroupchatActivity.groupchat_activity_flag==true)
									{
										GroupchatActivity.left_user = token[2];	
										GroupchatActivity.mHandler.sendEmptyMessage(1);						
									}
									Home.UID_IP.remove(token[2]);									
									}
								else if(Home.UID_IP.containsKey(token[2]))
								{
									// irrespective of the group code an exit comes and the user is still in my list give an error toast
									if(Home.home_flag_status == true){
										Home.left_user = token[2];
										Home.mHandler.sendEmptyMessage(2);							
									}
									else if(GroupchatActivity.groupchat_activity_flag==true)
									{
										GroupchatActivity.left_user = token[2];	
										GroupchatActivity.mHandler.sendEmptyMessage(2);						
									}
									Home.UID_IP.remove(token[2]);									
									}
								}
							}
						}
  		else{  
            Log.i("GMR_Something is wrong","Exception");  
        }
		oos.close();
		ois.close();
		chat_client.close();
	}
	
	public static void closeChatSocket()
	{
		try 
		{
			Group_chat_socket.close();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}