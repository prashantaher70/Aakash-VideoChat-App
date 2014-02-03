package com.example.videoconferencing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import android.util.Log;

	public class Send_exit_message implements Runnable{
		String self_uid,self_ip,to_send_ip;
		Socket socket;
		int SERVERPORT;
    	ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
    	
		public Send_exit_message(String t_s_ip,String self_ip,String self_uid,int port) {
			this.self_ip = self_ip;
			this.to_send_ip = t_s_ip;
			this.self_uid = self_uid;
			this.SERVERPORT = port;
		}
		
		
		@Override
		public void run() {
			socket = null;
			try {
				socket = new Socket(to_send_ip, SERVERPORT);
			} catch (UnknownHostException e) {
				Log.i("Object", "oos and ois not created");
			} catch (IOException e) {
				Log.i("GCL EXIT Socket error", " "+socket);
				e.printStackTrace();
				
				}
			Log.i("GCLIST","---------"+socket);
			if (socket!=null) {
				try {
					oos = new ObjectOutputStream(socket.getOutputStream());
					ois = new ObjectInputStream(socket.getInputStream());
					oos.writeObject("EXIT"+" "+Home.group_chat_code+" "+self_uid+" "+self_ip);
					oos.flush();
					oos.close();
					ois.close();
				} catch (IOException e) {
					Log.i("GCList","Failed");
					e.printStackTrace();
				} finally {
					if (!socket.isClosed()) {
						try {
							Log.i("groupchat_list", "Closing socket");
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				Log.i("GroupChatList", "Socket Not Connected!!!!!!");
			}
		}
	}