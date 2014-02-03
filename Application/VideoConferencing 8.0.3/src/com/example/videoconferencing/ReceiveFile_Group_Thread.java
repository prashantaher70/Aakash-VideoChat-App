package com.example.videoconferencing;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class ReceiveFile_Group_Thread extends Thread {

	int Port;
	static int no_of_files_simultaneously_receive=0;

	public ReceiveFile_Group_Thread(int port) {
		this.Port = port;
	}

	private final Integer BUFFER_SIZE = 65000;
	public static ServerSocket server_socket;
	Socket fileSocket;
	
	@Override
	public void run() {
		
		try {
			server_socket = new ServerSocket(Port, 1);
			
			if (server_socket != null) {
				while (true) {
					Log.i("RFGT start", " "+no_of_files_simultaneously_receive);
					if(no_of_files_simultaneously_receive>=0 && no_of_files_simultaneously_receive<=2){
						Log.i("FILE Server Started", "Server Started");
						fileSocket = server_socket.accept();
						Log.i("FILE Server Started", "Server Started");
						Log.i("Sever Accepted", "accepted starting thread for"+fileSocket);
						savefileThread sft=new savefileThread(fileSocket);
						Thread s_f_t=new Thread(sft);
						s_f_t.start();
						no_of_files_simultaneously_receive++;
					}
					else{
						Log.i("RFG THREAD", "max_no reached"+no_of_files_simultaneously_receive );
						if(no_of_files_simultaneously_receive<0)
							no_of_files_simultaneously_receive=0;
					}
				}
					
			} else
				Log.i("Error", "file Port Already Bind Close Reconnect");
		} catch (IOException e) {
			Log.i("RFGT FIle receive Server", "IO");
			e.printStackTrace();
		} catch (Exception e) {
			Log.i("RFGT File Receive Server", "EX");
			e.printStackTrace();
		} finally {
			Log.i("RFGTFAIL", "Is closed check");
			fileSocket=null;
			if (server_socket != null && !server_socket.isClosed()) {
				try {
					Log.i("RFGTFAIL", "Closing");
					server_socket.close();
					Log.i("RFGTFAIL", "Closed");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.i("FAIL", "Fail to close socket");
					e.printStackTrace();

				}
			}
		}

	}
	synchronized public static void chng_when_done_receiving() {
		no_of_files_simultaneously_receive--;
		Log.i("RFGTHREAD completed"," "+no_of_files_simultaneously_receive);
	}
	

	
	private class savefileThread extends Thread{
		Socket filesocket;
		ObjectInputStream ois=null;
		ObjectOutputStream oos=null;
		public savefileThread(Socket filesock) {
			this.filesocket=filesock;
			
		}
		
		public void run() {
			Log.i("RFGTHREAD"," "+fileSocket);
			Date date =new Date();
			SimpleDateFormat sdf=new SimpleDateFormat("MMM d,HH:mm");
			String dt=sdf.format(date);//dt is stored here
			String filename="";
			boolean exception_flag=false;
			try {
				
				oos = new ObjectOutputStream(fileSocket.getOutputStream());  
		        ois = new ObjectInputStream(fileSocket.getInputStream());

				Log.i("RFGTHREAD"," " +filesocket+"   "+ois+ " ============"+oos);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FileOutputStream fos = null;
			byte[] buffer = new byte[BUFFER_SIZE];

			try	{				// 1. Read file name.
								Object o = ((ObjectInput) ois).readObject();
								if (o instanceof String) {
									String[] token = o.toString().split("\\s",3);
									filename=token[2];
									Log.i("RFGT", " O.tostring  :"+o.toString());
									if(Home.group_chat_code.equals(token[0]))
									{
										fos = new FileOutputStream("/mnt/sdcard/" + token[2]);
										Home.PREV_GRP_MSG.add(dt+" "+token[1]+":RECEIVINGFILE"+" "+token[2]);
										if(Home.home_flag_status == true){
											Home.set_notification=token[1]+":"+token[2];
											Home.mHandler.sendEmptyMessage(3);							
										}
										else if(GroupchatActivity.groupchat_activity_flag==true)
										{
												Log.i("Chat_Socket","Chat window flag true");
												GroupchatActivity.set_notification=token[2];
												GroupchatActivity.mHandler.sendEmptyMessage(3);						
										}
									}
									else{
										Log.i("RFGTHREAD"," "+fileSocket+"Closing");
											ois.close();
											fileSocket.close();
											return;
									}
								} else {
									Log.i("ReceiveFile_Group", "Something is wrong");
								}
					
								// 2. Read file to the end.
								Integer bytesRead = 0;
					
								do {
									o = ((ObjectInput) ois).readObject();
					
									if (!(o instanceof Integer)) {
										Log.i("ReceiveFile_Group", "Something is wrong");
									}
					
									bytesRead = (Integer) o;
									Log.i("bytesRead", "bytes" + bytesRead.toString());
									o = ((ObjectInput) ois).readObject();
					
									if (!(o instanceof byte[])) {
										Log.i("ReceiveFile_Group", "Something is wrong");
									}
					
									buffer = (byte[]) o;
					
									// 3. Write data to output file.
									Log.i("GRoup File Transfer", "Receiving file");
									try {
										fos.write(buffer, 0, bytesRead);
										fos.flush();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
					
									Log.i("after write", "writing to disk");
									if (bytesRead < 65000) {
										break;
									}
									// Log.i("bytesRead",getString(bytesRead));
								} while (true);
								Log.i("MSG", "FILE SENT");
					
								try {
									fos.close();
									ois.close();
									fileSocket.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
			}catch(Exception e)
			{
				//if there is an exception show file receiving failed
				exception_flag=true;
				if(fileSocket!=null){
					try {
						Log.i("RFGTHREAD"," "+fileSocket+"Closing");
						fileSocket.close();
						Log.i("RFGTHREAD"," "+fileSocket+"Closed");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				Log.i("G_FILE_RECEIVE","FAILED");
				Home.PREV_GRP_MSG.add(dt+" "+"RECEIVINGFILE:"+filename+" "+"Failed!!!");
				if(Home.home_flag_status == true){
					Home.set_notification="Failed!!!";
					
					Home.mHandler.sendEmptyMessage(3);							
				}
				else{
					GroupchatActivity.set_notification=filename+"Failed!!!";
					GroupchatActivity.mHandler.sendEmptyMessage(3);
				}
				ReceiveFile_Group_Thread.chng_when_done_receiving();
			}
			
			//if there is no exception show file receiving successful 
			if(!exception_flag){
				Home.PREV_GRP_MSG.add(dt+" "+"RECEIVINGFILE:"+filename+" "+"SUCCESSFUL");
				if(Home.home_flag_status == true){
					Home.set_notification=dt+" "+filename+"---Failed!!!";
					Home.mHandler.sendEmptyMessage(3);							
				}
				else{
					
					GroupchatActivity.set_notification=filename+"SUCCESSFUL";
					GroupchatActivity.mHandler.sendEmptyMessage(3);
				}
				ReceiveFile_Group_Thread.chng_when_done_receiving();
			}
		}
		
	}
}
