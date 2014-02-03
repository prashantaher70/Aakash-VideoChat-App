package com.example.videoconferencing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import android.util.Log;

public class SendFile_Group_Thread extends Thread {

	String to_send_file_ip, file_path;
	int Port;
	ObjectOutputStream oos;
	ObjectInputStream ois;

	public SendFile_Group_Thread(String ip, String file_path, int Port) {
		this.to_send_file_ip = ip;
		this.file_path = file_path;
		this.Port = Port;
	}

	@Override
	public void run() {

		// to_send_file_ip,filelegth,filepath
		File file = new File(file_path);

		Socket socket = null;
		Long filelength = file.length();
		Long sent = 0L;
		// int currentPos = 0;
		try {
			Log.i("SendFile_Group" + to_send_file_ip, "Creating socket");

			socket = new Socket(to_send_file_ip, Port);
			// Log.i("Creating Created","222222");
			Log.i("SendFile_Group" + to_send_file_ip, "Stream Init");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			Log.i("1", "1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i("SendFile_Group" + to_send_file_ip, "Creating socket");
			e.printStackTrace();
		}
		if (socket != null) {
			if (!Home.group_chat_code.equals("")) {
				try {
					Log.i("SendFile_Group", "In try block of send");
					ois = new ObjectInputStream(socket.getInputStream());
					oos = new ObjectOutputStream(socket.getOutputStream());
					oos.writeObject(Home.group_chat_code + " "+Login.uname+" " + file.getName());
					FileInputStream fis = new FileInputStream(file);
					Log.i("SendFile_Group", "File Read");
					byte[] buffer = new byte[65000];
					Integer bytesRead = 0;
					Log.i("SendFile_Group", "Sending");
					while ((bytesRead = fis.read(buffer)) > 0) {
						sent += bytesRead;
						oos.writeObject(bytesRead);
						oos.writeObject(Arrays.copyOf(buffer, buffer.length));
						oos.flush();
						Log.i("SendFile_Group", "While loop" + "   "
								+ (int) ((sent / (float) filelength) * 100));

						Log.i("sending", "sending" + bytesRead.toString());
					}
					oos.close();
					ois.close();
					Log.i("SendFile_Group", "OIS OOS CLOSED");

				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.i("3", "3");
					e.printStackTrace();
				} finally {
					Log.i("SEND FILE GROUP THREAD  ","Changing the send flag");
					GroupchatActivity.chng_when_send_done();
					if (!socket.isClosed()) {
						try {
							Log.i("SendFile_Group", "Soclet closed");
							socket.close();
							Log.i("SendFile_Group", "Closed");
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		
	}
}