package com.example.videoconferencing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.example.videoconferencing.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChatWindow extends Activity implements OnClickListener
{
	TextView tv_message;
	EditText et_message;
	Socket socket;
	String callieIP;
	String ip_address;
	Button btn_send;
	String message;
	int SERVER_PORT;
	boolean isCalled,stop=true;
	/**/
	ObjectOutputStream oos=null;
	ObjectInputStream ois=null;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatretrieval);
		tv_message = (TextView) findViewById(R.id.tv_retrieval);
		et_message = (EditText) findViewById(R.id.et_message);
		btn_send = (Button) findViewById(R.id.bt_send);
		btn_send.setOnClickListener(this);
		//Toast.makeText(getApplicationContext(), "IP: "+callieIP, Toast.LENGTH_LONG).show();
		SERVER_PORT = 2222; //file sharing is on 4444
		isCalled = getIntent().getBooleanExtra("called", false);
		if(!isCalled)
		{

			callieIP = getIntent().getStringExtra("callieIP");
			new createConnection().execute(callieIP);
		}
		else
		{
			try
			{
				oos = new ObjectOutputStream(Sockets.getChatSocket().getOutputStream());
	 			ois = new ObjectInputStream(Sockets.getChatSocket().getInputStream());
	 			if(ois == null)
	 			{
	 				Log.i("err in OIS","null OIS");
	 			}
			}
			catch(Exception e)
			{
				
			}
		}
		
		
		// Thread for incoming messages
		Analyse incoming_messages = new Analyse();
		Thread x = new Thread(incoming_messages);
		x.start();
	}
	
	public class createConnection extends AsyncTask<String, String, Void>
	{
		protected Void doInBackground(String... params) 
		{
        	try
	 		{
	 			socket = new Socket(params[0],SERVER_PORT);
	 			oos = new ObjectOutputStream(socket.getOutputStream());
	 			ois = new ObjectInputStream(socket.getInputStream());
	 		} 
	 		catch (UnknownHostException e) 
	 		{
	 			Log.i("Object","oos and ois not created");
	 		} 
	 		catch (IOException e) 
	 		{
	 			e.printStackTrace();
	 		}
			
			return null;
		}
		
	}
	
	
	@Override
	public void onClick(View v) 
	{
		if(v.getId() == R.id.bt_send)
		{
			if (et_message.getText().equals(""))
			{
				Toast.makeText(getApplicationContext(), "Enter message !!!!", Toast.LENGTH_SHORT).show();
			}
			else 
			{
				String compose_message = et_message.getText().toString()+" \n";
				tv_message.append(compose_message);
				et_message.setText("");
				try
		 		{
		 			oos.writeObject(compose_message);
		 			oos.flush();
		 		}
		 		catch (IOException e) 
		 		{
		 			e.printStackTrace();
		 		} 
			}
		}
	}
    public class Analyse implements Runnable
	{
		@Override
		public void run() 
		{
			while(stop)
			{
				try 
				{
					saveMessages_append();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private void saveMessages_append() throws Exception  //append input msg in tv_message 
	{
		final Object o  =  (Object) ois.readObject() ;
		Log.i("Received",o.toString());
		if (o instanceof String) 
		{  
            Runnable append_mes = new Runnable() 
            {
				@Override
				public void run() 
				{
					tv_message.append(o.toString());
					final int scrollAmount = tv_message.getLayout().
							getLineTop(tv_message.getLineCount()) - tv_message.getHeight();
				    // if there is no need to scroll, scrollAmount will be <=0
				    if (scrollAmount > 0)
				        tv_message.scrollTo(0, scrollAmount);
				    else
				        tv_message.scrollTo(0, 0);
					
				}
			};
            runOnUiThread(append_mes);
  		} 
		else
		{  
            Log.i("Something is wrong","Exception");  
        }
	}
	@Override
	public void onBackPressed() 
	{
		stop=false;
		super.onBackPressed();
		try
		{
			ois.close();
			oos.close();
		}
		catch(Exception e)
		{
			
		}
		if(!isCalled)
		{
			try 
			{
				socket.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			Sockets.closeChatSocket();
		}
	}

}
