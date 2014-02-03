package com.example.videoconferencing;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

import com.example.videoconferencing.Home.PlayAudio;
import com.example.videoconferencing.Home.RecordSend;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

public class Group_audio_conference_receive extends Thread {
	boolean called = false;
	public static Context context_home;
	static ServerSocket GroupchatServerSocket;
	static Socket Group_chat_socket;
	int audio_Port;
	static Activity activity;
	static ObjectOutputStream oos = null;
	static ObjectInputStream ois = null;
	public static String receiver_name="";

	public Group_audio_conference_receive(int audio_Port, Context context) {
		this.audio_Port = audio_Port;
		this.context_home = context;

	}

	public void run() {

		Home.current.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.i("msg", "in runonui");
				
				// Dialog box on show incoming call
				
				Home.dialog = new AlertDialog.Builder(Home.current);
				Home.dialog.setCancelable(false);
				Home.dialog.setPositiveButton("Accept",
				new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								/*
								 * 
								 * user accepts the call by pressing "ACCEPT"
								 * button the ringing tone-->stopped it sends
								 * back a specific message to indicate to caller
								 * that it has accepted the call display updated
								 * to show that call is connected
								 */
								// To stop ringtone on click of accept
								Home.soundPool.stop(Home.stopSid);
								
								for (Map.Entry<String, String> entry : Home.IP_Accept_Flag.entrySet()) {

									Log.i("msg",
											"loop before starting flag send");
									if (!entry.getKey().equals(Login.ip_self)) {
										Log.i("GACR", "-----------------------"
												+ entry.getKey() + "true" + " "
												+ Login.ip_self
												+ "---------------");
										SendAcceptFlag flag_messages = new SendAcceptFlag(
												entry.getKey(), "true" + " "
														+ Login.ip_self, 35015);
										Thread y1 = new Thread(flag_messages);
										y1.start();
										Log.i("msg", "loop started flag send");
									}

								}

								for (Map.Entry<String, String> entry : Home.IP_Accept_Flag
										.entrySet()) {

									if (!entry.getKey().equals(Login.ip_self)) {
										entry.setValue("true");
										Log.i("GACR", " IP:" +entry.getKey()+" "+"true");
									}
									
								}

								Home.record = true;
								RecordSend rec = new RecordSend();
								Log.i("GACR", "Audio call record send Thread");
								Thread rstart = new Thread(rec);
								rstart.start();

								PlayAudio playTrack3 = new PlayAudio();
								Log.i("GACR", "PlayAudio Thread");
								Thread p3 = new Thread(playTrack3);
								p3.start();
							}
						});

				Home.dialog.setNegativeButton("Reject",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface arg0, int which) {
								
								Home.soundPool.stop(Home.stopSid);
								if (Home.IP_UID_SET_FLAG) {
									Home.IP_UID_SET_FLAG = false;
									Log.i("Inside Reject", " Groupchatcode"+Home.group_chat_code);
									for (Map.Entry<String, String> entry : Home.UID_IP
											.entrySet()) {
										Log.i("GCL EXIT", " " + entry.getKey());
										Send_exit_message s_e_m = new Send_exit_message(
												entry.getValue(),
												Login.ip_self, Login.uname,
												35015);
										Thread sem = new Thread(s_e_m);
										sem.start();
									}
									//changes
									//Home.group_chat_code="";
									Home.UID_IP.clear();
									Home.IP_Accept_Flag.clear();
									Home.socketS.close();
									Home.record=false;
									Home.sockR.close();
									Toast.makeText(context_home,
											"You have Rejected Call",
											Toast.LENGTH_SHORT).show();

								}
							}
						});	
					}
				});
		try {

			GroupchatServerSocket = new ServerSocket(audio_Port, 1);
			Log.i("GACR Started", "GACR Audio Server Started ");
			if (GroupchatServerSocket != null && !GroupchatServerSocket.isClosed()) {
				while (true) {
					Log.i("GACR_Socket", "Waiting");
					Group_chat_socket = GroupchatServerSocket.accept();
					Log.i("GACR_Socket Accepted", "accepted");
					saveMessages_append(Group_chat_socket);
				}
			} else
				Log.i("GACR", "Port Already Bind Close Reconnect");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i("GACR Accepted", "IO");
			e.printStackTrace();
		} catch (Exception e) {

			Log.i("GACR Accepted", "EX");
			e.printStackTrace();
		} finally {
			Log.i("GACR", "Is closed check");
			if (GroupchatServerSocket != null) {
				try {
					Log.i("GACR", "Closing");
					GroupchatServerSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.i("GACR", "Fail to close socket");
					e.printStackTrace();

				}
			}
		}
	}

	public class SendAcceptFlag implements Runnable {

		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		Socket socket;
		int SENDMESSAGEPORT;
		String message, ip_address;

		// CONSTRUCTOR FOR THREAD
		public SendAcceptFlag(String ip, String msg, int port) {
			this.ip_address = ip;
			this.message = msg;
			this.SENDMESSAGEPORT = port;
		}

		public void run() {
			socket = null;
			try {
				Log.i("flag send thread", " Inside Socket");
				socket = new Socket(ip_address, SENDMESSAGEPORT);
				Log.i("flag send thread", " Socket Created oos and ois "
						+ socket);
			} catch (UnknownHostException e) {
				Log.i("Object", "oos and ois not created");
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (socket != null) {
				try {
					oos = new ObjectOutputStream(socket.getOutputStream());
					ois = new ObjectInputStream(socket.getInputStream());
					Log.i("msg", "sending flag to" + " " + ip_address);
					oos.writeObject(message);
					oos.flush();
					oos.close();
					ois.close();
				}

				catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (!socket.isClosed()) {
						try {
							Log.i("groupchat_activity", "Closing socket");
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}// FINALLY
			}// IF SOCKET NOT NULL CLOSED
		}// RUN METHOD CLOSED
	}// CLASS CLOSED FOR SENDING MESSAGE

	private void saveMessages_append(Socket chat_client) throws Exception {

		//new
		// Declaring ObjectOutputStream to write data and ObjectInputStream to
/*		// read data
		Log.i(" msg", "inside save_messgaes append");
		oos = new ObjectOutputStream(chat_client.getOutputStream());
		ois = new ObjectInputStream(chat_client.getInputStream());
		Log.i("msg", "save_messages oos and ois created");
		// 1. Read whether the client wants to chat or share file.
		final Object o = ((ObjectInput) ois).readObject();
		Log.i("msg", "save_messages " + o.toString());
		if (o instanceof String) {
			if (o.toString().startsWith("LIST")) { // if it is request for new
													// group chat and i am not
													// envolved into a group
				Log.i("msg", "LIST message"); // chat

				if (!Home.IP_UID_SET_FLAG) {
					Home.IP_UID_SET_FLAG = true;
					Log.i("GMR_LIST", "in list ip ");
					String[] token = o.toString().split("\\s", 22);
					for (int i = 0; i < token.length; i++)
						Log.i("GACR", token[i]);

					Home.group_chat_code = token[1];
					Log.i("In LIST receive: ", " "+Home.group_chat_code);

					for (int i = 2; i < token.length; i++) {
						Home.UID_IP.put(token[i], token[++i]);
						Home.IP_Accept_Flag.put(token[i], "false");
					}
				}
				// set the user name and ip in the static variable of
				// Home.UID_IP HASHMAP such that there is no activity going on
				// else reply with an exit message
			} else if (o.toString().startsWith("AUDIO")) {

				// Audio Call
				Log.i("msg", "AUDIO message");
				Log.i("GACR", "Calling initialize_udp_sockets()");
				Home.initialize_udp_sockets();
				Log.i("GACR", "Calling initialize_udp_sockets()");
				Log.i("GACR", "Calling ini()");
				Home.ini();
				Log.i("GACR", "Called ini()");

				Log.i("GACR_AUDIO", "in AUDIO ");
				// dt(2,3) from(4) ,msg(5)
				// message, date not required..
				// Audio, groupchatcode, from_uid

				String[] token = o.toString().split("\\s", 3);
				if (Home.group_chat_code.equals(token[1])) {
					Log.i("MSG", token[2]);
					Log.i("GACR THREAD", " "
							+ GroupchatActivity.groupchat_activity_flag);
					// if(Home.home_flag_status == true){
					Log.i("In Audio receive: ", " "+Home.group_chat_code);
					GroupchatActivity.grp_message += token[2];
					Home.calling_user = token[2];
					Home.maudioHandler.sendEmptyMessage(0);

				}// MSG from
			} else if (o.toString().startsWith("EXIT")) // when a user left
														// group chat
			{
				Log.i("msg", "EXIT message");
				final String[] token = o.toString().split("\\s", 4);
				
				Log.i("In exit receive: ", " "+Home.group_chat_code);
				// if(Home.IP_UID_SET_FLAG){
				if (Home.group_chat_code.equals(token[1])) {
					Log.i("GACR", "USER" + token[2] + token[3] + " Left");
					Log.i("In exit receive: ", " "+Home.group_chat_code);
					// if(Home.home_flag_status == true){
					Home.left_user = token[2];
					Home.UID_IP.remove(token[2]);
					Home.IP_Accept_Flag.remove(token[3]);
					// changes
					if (Home.IP_Accept_Flag.size() == 0) {
						Log.i("Inside GACR", "No user left!!!!!!!!!!!!!!!!!!!!!!");
						Home.maudioHandler.sendEmptyMessage(1);
						Log.i("GACR", "No user left");
						Home.current.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								
								Toast.makeText(context_home,
										"No more users left on Audio Conference Call.\n\tEnding Audio Conference Call",
										Toast.LENGTH_SHORT).show();
								

							}

							
						});
						Home.record = false;
						// changes
					}else
					{
						Log.i("EXIT message Else ","audio handler 1");
						Home.maudioHandler.sendEmptyMessage(1);
				
					}
				
					Home.group_chat_code="";
					//change p
					//Home.record = false;
					Home.audioRecord.stop();
					Home.audioRecord.release();
					Home.audioTrack.stop();
					Home.audioTrack.release();
					Home.socketS.close();
					Home.sockR.close();
					Home.IP_UID_SET_FLAG = false;

				}	
*/	
		// Declaring ObjectOutputStream to write data and ObjectInputStream to
				// read data
				oos = new ObjectOutputStream(chat_client.getOutputStream());
				ois = new ObjectInputStream(chat_client.getInputStream());

				// 1. Read whether the client wants to chat or share file.
				final Object o = ((ObjectInput) ois).readObject();
				if (o instanceof String) {
					if (o.toString().startsWith("LIST")) { // if it is request for new
															// group chat and i am not
															// envolved into a group
															// chat

						if (!Home.IP_UID_SET_FLAG) {
							Home.IP_UID_SET_FLAG = true;
							Log.i("GMR_LIST", "in list ip ");
							String[] token = o.toString().split("\\s", 22);
							for (int i = 0; i < token.length; i++)
								Log.i("GACR", token[i]);

							Home.group_chat_code = token[1];

							for (int i = 2; i < token.length; i++) {
								Home.UID_IP.put(token[i], token[++i]);
								Home.IP_Accept_Flag.put(token[i], "false");
							}
						}
						// set the user name and ip in the static variable of
						// Home.UID_IP HASHMAP such that there is no activity going on
						// else reply with an exit message
					} else if (o.toString().startsWith("AUDIO")) {

						// Audio Call
						//Home.record = true;
						Log.i("GACR", "Calling initialize_udp_sockets()");
						Home.initialize_udp_sockets();
						Log.i("GACR", "Calling initialize_udp_sockets()");
						Log.i("GACR", "Calling ini()");
						Home.ini();
						Log.i("GACR", "Called ini()");

						Log.i("GACR_AUDIO", "in AUDIO ");
						// dt(2,3) from(4) ,msg(5)
						// message, date not required..
						// Audio, groupchatcode, from_uid

						String[] token = o.toString().split("\\s", 3);
						if (Home.group_chat_code.equals(token[1])) {
							Log.i("MSG", token[2]);
							
							// if(Home.home_flag_status == true){
							Home.calling_user = token[2];
							// to print message that user __ is calling!!!!!
							Home.maudioHandler.sendEmptyMessage(0);
							

							/*Home.ini();
							RecordSend rec = new RecordSend();
							Log.i("GACR", "Audio call record send Thread");
							Thread rstart = new Thread(rec);
							rstart.start();

							PlayAudio playTrack3 = new PlayAudio();
							Log.i("GACR", "PlayAudio Thread");
							Thread p3 = new Thread(playTrack3);
							p3.start();*/

							// }
							/*
							 * else if(groupchat_activity.groupchat_activity_flag==true)
							 * { Log.i("Chat_Socket","Chat window flag true");
							 * groupchat_activity
							 * .grp_message+=token[2]+" "+token[3]+" "+
							 * token[4]+" "+token[5];
							 * groupchat_activity.new_message=token
							 * [2]+" "+token[3]+" "+token[4]+" "+token[5];
							 * groupchat_activity.maudioHandler.sendEmptyMessage(0); }
							 */
						}// MSG from
					} else if (o.toString().startsWith("EXIT")) // when a user left
																// group chat
					{
						String[] token = o.toString().split("\\s", 4);
						// if(Home.IP_UID_SET_FLAG){
						if (Home.group_chat_code.equals(token[1])) {
							Log.i("GACR", "USER" + token[2] + token[3] + " Left");

							// if(Home.home_flag_status == true){
							Home.left_user = token[2];
							Home.UID_IP.remove(token[2]);
							Home.IP_Accept_Flag.remove(token[2]);
							
						
						// else if(groupchat_activity.groupchat_activity_flag==true)
						// {
						// groupchat_activity.left_user = token[2];
						// groupchat_activity.maudioHandler.sendEmptyMessage(1);
						// }

						// //////////////
						if (Home.IP_Accept_Flag.size() == 0) {
							Log.i("GACR", "No user left");
							// to show that user __ has left the audio call
							Home.maudioHandler.sendEmptyMessage(1);
							Home.current.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(context_home,
											"No more users left on Audio Conference Call.\n\tEnding Audio Conference Call",
											Toast.LENGTH_SHORT).show();

								}

							});
							Home.record = false;
							Home.group_chat_code="";
							Home.audioRecord.stop();
							Home.audioRecord.release();
							Home.audioTrack.stop();
							Home.audioTrack.release();
							Home.socketS.close();
							Home.sockR.close();
							Home.IP_UID_SET_FLAG = false;
						}else{
							Log.i("EXIT message Else ","audio handler 1");
							Home.maudioHandler.sendEmptyMessage(1);
						}
					}
				}
				
		
		 else if (o.toString().startsWith("true")) {
				Log.i("msg", "true message");
				final String[] token = o.toString().split("\\s", 2);
				Log.i("msg", "true accept flag received");
				
				
				// Loop to get name of Caller
				for (Map.Entry<String, String> entry : Home.UID_IP
						.entrySet()) {
					if(entry.getValue().equals(token[1])){
						receiver_name = entry.getKey();
					}
				}
				
				for (Map.Entry<String, String> entry : Home.IP_Accept_Flag.entrySet()) {

					if (entry.getKey().equals(token[1])) {
						Log.i("msg", entry.getKey() + " " + entry.getValue()
								+ " " + "now setting to true");
						entry.setValue("true");
						Home.current.runOnUiThread(new Runnable() {
						
							@Override
							public void run() {
								Toast.makeText(context_home,
										receiver_name +" accepted your Audio Conference Call Request.",
										Toast.LENGTH_SHORT).show();

							}
						});

						Log.i("msg", entry.getKey() + " " + entry.getValue()
								+ " " + "now set to true");
					}
				}
			}
		}else {
			Log.i("GACR_Something is wrong", "Exception");
		}
		oos.close();
		ois.close();
		chat_client.close();
	}

	public class Send_exit_message implements Runnable {
		String self_uid, self_ip, to_send_ip;
		Socket socket;
		int SERVERPORT;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;

		public Send_exit_message(String t_s_ip, String self_ip,
				String self_uid, int port) {
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
				Log.i("GCL EXIT Socket error", " " + socket);
				e.printStackTrace();

			}
			Log.i("GCLIST", "---------" + socket);
			if (socket != null) {
				try {
					oos = new ObjectOutputStream(socket.getOutputStream());
					ois = new ObjectInputStream(socket.getInputStream());
					oos.writeObject("EXIT" + " " + Home.group_chat_code + " "
							+ self_uid + " " + self_ip);
					Log.i("Inside Reject send exit", "EXIT" + " " + Home.group_chat_code + " "
							+ self_uid + " " + self_ip);
					oos.flush();
					oos.close();
					ois.close();

					// closing connections of conferencing

					/*
					 * Home.audioRecord.stop(); Home.audioRecord.release();
					 * Home.audioTrack.stop(); Home.audioTrack.release();
					 */
				} catch (IOException e) {
					Log.i("GCList", "Failed");
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
					if (!Home.socketS.isClosed()) {
						try {
							Log.i("groupchat_list", "Closing socket");
							Home.socketS.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (!Home.sockR.isClosed()) {
						try {
							Log.i("groupchat_list", "Closing socket");
							Home.sockR.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				Log.i("GroupChatList", "Socket Not Connected!!!!!!");
			}
		}
	}
}
