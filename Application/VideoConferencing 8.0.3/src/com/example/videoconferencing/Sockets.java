package com.example.videoconferencing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class Sockets 
{
	static ServerSocket chatServerSocket;
	static Socket chatClientSocket;
	private static final int chatPORT = 2222;
	static Activity activity;
	public static ObjectOutputStream Soos;
	public static ObjectInputStream Sois;
	
	public static void setActivity(Activity act)
	{
		activity = act;
	}
	public static void createChatSocket()
	{
		try 
		{
			chatServerSocket = new ServerSocket(chatPORT,1);
			while(true)
			{
				chatClientSocket = chatServerSocket.accept();
				Intent chat = new Intent(activity.getApplicationContext(),ChatWindow.class).putExtra("called",true);//called is true
				//Toast.makeText(activity.getApplicationContext(), "IP:"+chatClientSocket.getInetAddress().getHostAddress(), Toast.LENGTH_LONG).show();
				activity.startActivity(chat);
				
			}
		} 
		catch (IOException e) 
		{
			Log.d("chatIOE", "exception in creating chat socket");
			e.printStackTrace();
		}
		catch (Exception e) 
		{
			Log.d("chatE", "exception in creating chat socket");
			e.printStackTrace();
		} 
		
	}
	public static Socket getChatSocket()
	{
		return chatClientSocket;
	}
	public static ServerSocket getServerChatSocket()
	{
		return chatServerSocket;
	}
	public static void closeChatSocket()
	{
		try 
		{
			chatClientSocket.close();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
