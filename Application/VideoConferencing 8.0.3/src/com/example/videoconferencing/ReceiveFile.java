package com.example.videoconferencing;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public class ReceiveFile implements Runnable
{
	private static final int PORT = 4444;
	private final Integer BUFFER_SIZE = 65000;
	public static ServerSocket socket;
	Socket fileSocket;
	
	public ReceiveFile()
	{
		try 
		{
			socket = new ServerSocket(PORT,1);
		} 
		catch (IOException e) 
		{
			Log.d("receive file server socket","can't be created");
		}
	}
	@Override
	public void run() 
	{
		try 
		{
			if(socket!=null && !socket.isClosed())
			{		
				while(true)
				{	
					Log.i("Server Started","Server Started");
					fileSocket = socket.accept();
					Log.i("RF Sever Accepted","accepted saving now");
					saveFile(fileSocket);
					
					new Thread(new SendEndMsg(Home.current)).start();/*to send free me msg*/
				}
			}
			else
				Log.i("Error","file Port Already Bind Close Reconnect");
		} 
		catch (IOException e) 
		{
			Log.i("RFSever Accepted","IO");
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			Log.i("RFSever Accepted","EX");
			e.printStackTrace();
		}
		{
			Log.i("RF FAIL","Is closed check");
			if(socket!=null && !socket.isClosed())
			{
				try 
				{
					Log.i("FAIL","Closing");
					socket.close();
				} 
				catch (IOException e) 
				{
					
					Log.i("FAIL","Fail to close socket");
					e.printStackTrace();
					
				}
			}
		}
		
	}
	 private void saveFile(Socket socket) throws Exception 
	 {  
	        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());  
	        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());  
		/*PrintWriter oos = new PrintWriter(socket.getOutputStream());
		
		 BufferedReader ois = 
		     new BufferedReader(new InputStreamReader(socket.getInputStream()));*/
	        FileOutputStream fos = null;  
	        byte [] buffer = new byte[BUFFER_SIZE];  
	  
	        // 1. Read file name.  
	        Object o = ( (ObjectInput) ois).readObject();  
	  
	        if (o instanceof String) {  
	            fos = new FileOutputStream("/mnt/sdcard/"+o.toString());
	        } else {  
	            throwException("Something is wrong");  
	        }  
	  
	        // 2. Read file to the end.  
	        Integer bytesRead = 0;  
	  
	        do {  
	            o = ( (ObjectInput) ois).readObject();  
	  
	            if (!(o instanceof Integer)) {  
	                throwException("Something is wrong");  
	            }  
	  
	            bytesRead = (Integer)o;  
	            Log.i("bytesRead","bytes"+bytesRead.toString());
	            o = ( (ObjectInput) ois).readObject();  
	  
	            if (!(o instanceof byte[])) {  
	                throwException("Something is wrong");  
	            }  
	  
	            buffer = (byte[])o;  
	  
	            // 3. Write data to output file.
	            Log.i("File Transfer","Receiving file");
	            fos.write(buffer, 0, bytesRead); 
	            fos.flush();
	            
	            Log.i("after write","writing to disk");
	            if(bytesRead<65000)
	            {
	            	break;
	            }
	          //  Log.i("bytesRead",getString(bytesRead));
	        } while (true);  
	         Log.i("MSG","FILE SENT" );
	   
	        fos.close();  
	  
	        ois.close();  
	        oos.close();
	        fileSocket.close();
	    }  
	 	public void throwException(String message) throws Exception 
	 	{  
	        throw new Exception(message);  
	    }
}