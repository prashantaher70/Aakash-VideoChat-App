package com.example.videoconferencing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.example.videoconferencing.R;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FileShare extends Activity implements View.OnClickListener
{
	//private static ServerSocket socket;
	private Boolean file_name_flag=false;
	static TextView tvFilename,tvFileStatus;
	static Button bOpen,bSend;
	//TextView etTo;
	String filePath=null,toSend=null;
	String callieIP;
	String msg;
	
	Socket sendPTcpF;
	int tcpPortF;
	ObjectOutputStream sendOutF;
	ObjectInputStream receiveInF;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		tcpPortF = 8002;
		setContentView(R.layout.filetransfer);
		
		ActionBar actionBar=getActionBar();
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.navbar));
		
		bOpen=(Button)findViewById(R.id.bOpen);
		bSend=(Button)findViewById(R.id.BSend);
		//etTo = ((TextView)findViewById(R.id.etTo));
		//etFrom=(EditText)findViewById(R.id.etFrom);
		tvFilename=(TextView)findViewById(R.id.tvFileName);
		callieIP = getIntent().getStringExtra("callieIP");
		//etTo.setText(callieIP);
		bOpen.setOnClickListener(this);
		bSend.setOnClickListener(this);
		
		tvFileStatus = (TextView)findViewById(R.id.tvFileStatus);
		bOpen.setEnabled(false);
		bSend.setEnabled(false);
		tvFileStatus.setText("Connecting....");
		
		TcpConnection tcp = new TcpConnection();
		tcp.execute();
	}	
	
	private class TcpConnection extends AsyncTask<String, String, Void>
	{

		@Override
		protected Void doInBackground(String... params) 
		{
			try
			{
			Home.busy = true;
			
			Log.d("msg","creating a tcp connection to callie..");
			
			//form a socket at caller's side
			sendPTcpF = new Socket(InetAddress.getByName(callieIP),tcpPortF);
			
			//open a stream to send the message
			sendOutF = new ObjectOutputStream(sendPTcpF.getOutputStream());
			Log.d("asfas","op stream");
			
			
			msg = "call"+" "+Login.uname+" "+"file";
			Log.i("MESSAGE",msg);
			//send the message
			sendOutF.writeObject(msg);
			sendOutF.flush();
			Log.d("msg","sending "+msg);
			
			}
			catch(UnknownHostException e)
			{
				Log.d("msg","unknown host");
			}
			catch(IOException e)
			{
				Log.d("msg","I/o excepton");
			} 
			return null;
		}
		@Override
		protected void onProgressUpdate(String... values) 
		{
			super.onProgressUpdate(values);
			if(values[0].equals("success"))
			{
				bOpen.setEnabled(true);
				bSend.setEnabled(true);
				tvFileStatus.setText("Connected..");
					try
					{
						sendOutF.close();
						receiveInF.close();
						sendPTcpF.close();
					}
					catch(Exception e)
					{
						
					}
			}
		}
	}
	/*
	@Override
	protected void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i("FAIL","Destroy");
		if(socket!=null)
		{
			try 
			{
				Log.i("FAIL","Closing");
				socket.close();
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				Log.i("FAIL","Fail to close socket");
				e.printStackTrace();
			}
		}
	}
	*/
	@SuppressLint("ShowToast")
	@Override
	public void onClick(View v) 
	{
		
		if(v.getId()==bOpen.getId())
		{
			bSend.setEnabled(true);
			Intent file_Manager=new Intent(this,com.example.videoconferencing.Explorer.class);
			//startActivity(file_Manager);
			startActivityForResult(file_Manager, 1);
		}
		
		else if(v.getId()==bSend.getId())
		{
			if(!file_name_flag){
				Log.i("lll","ppp");
				//Toast.makeText(getApplicationContext(), "No File Selected", Toast.LENGTH_SHORT).show();
			}
			else
			{
				String send_data[]={filePath,callieIP.toString()};
				new SendAsyncFile().execute(send_data);
			}
	}
}
/*	
	public void onBackPressed() 
	{
		super.onBackPressed();
		try
		{
			sendOutF.close();
			receiveInF.close();
			sendPTcpF.close();
		}
		catch(Exception e)
		{
			Log.i("msg","Error closing sockets in Call.java");
		}
	}
*/	
	
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) 
{
	// TODO Auto-generated method stub
	super.onActivityResult(requestCode, resultCode, data);

		
		if(resultCode==RESULT_OK){
		if(requestCode==1){
				file_name_flag=true;
					tvFilename=(TextView)findViewById(R.id.tvFileName);
					filePath=data.getStringExtra("filePath");
					tvFilename.setText(filePath);			
		}
		else
			tvFilename.setText("No File Selected");
		}
	
}


private class SendAsyncFile extends AsyncTask<String, Integer, Void> {
	ProgressDialog dialog;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	
	protected void onPreExecute()
	{
			dialog =new ProgressDialog(FileShare.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setIndeterminate(false);
			dialog.setMessage("Sending Data");
			dialog.setCancelable(false);
			dialog.setProgress(0);
			dialog.setMax(100);
			dialog.show();
			
		
	}
	
	     protected Void doInBackground(String... s) 
	     {
	    	//tosend,filelegth,filepath
	    	 	File file = new File(s[0]);
	    	 	String toSend=s[1];
		        Socket socket=null;
				Long filelength = file.length();
				Long sent=0L;
				//int currentPos = 0;
				try {
					//Log.i(toSend,"111111");
					
					
					socket = new Socket(toSend, 4444);
					//Log.i("Creating Created","222222");
					 ois= new ObjectInputStream(socket.getInputStream());  
				     oos = new ObjectOutputStream(socket.getOutputStream());
				    
				   //  Log.i("ip/op stream",ois.toString()+" and cheena "+oos.toString());
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					Log.i("1","1");
				} catch (IOException e) {
					// TODO Auto-generated catch block
		
					e.printStackTrace();
				}  
		         
		  
    try {
    	
    	
    				
					Log.i("Async","In try block of send");
					 oos.writeObject(file.getName());
					 FileInputStream fis = new FileInputStream(file);  
				     byte [] buffer = new byte[65000];
				     Integer bytesRead = 0; 
				     
				        while ((bytesRead = fis.read(buffer)) > 0) {
				        	sent+=bytesRead;
				            oos.writeObject(bytesRead);  
				            oos.writeObject(Arrays.copyOf(buffer, buffer.length));  
				            oos.flush();
				            Log.i("Async","While loop"+"   "+(int)((sent/(float)filelength)*100));
				           publishProgress((int)((sent/(float)filelength)*100));
				            Log.i("sending", "sending"+bytesRead.toString());
				        }
				        	oos.close();
							ois.close();
						
				  
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.i("3","3");
					e.printStackTrace();
				}
    			finally{
    				
    				if(!socket.isClosed()){
						dialog.dismiss();
						try {
							socket.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}							   
					}
    			}
	    	 
			return null;
	     }

	     protected void onProgressUpdate(Integer... progress) {
	    	 
	    	 dialog.setProgress(progress[0]);

	     }
}

@Override
protected void onDestroy()
{
	super.onStop();
	new Thread(new SendEndMsg(getApplicationContext())).start();/*to send free me msg*/
}


public void onBackPressed()
{
	super.onBackPressed();
	finish();
}
}
	
	

	