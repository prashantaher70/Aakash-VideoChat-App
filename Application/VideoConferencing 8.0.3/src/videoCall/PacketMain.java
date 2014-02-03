package videoCall;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.example.videoconferencing.Home;
import com.example.videoconferencing.Login;
import com.example.videoconferencing.R;
import com.example.videoconferencing.SendEndMsg;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class PacketMain extends Activity implements OnClickListener{

    private SurfaceView surfaceView;
    public static Button start,stop,receive;
    public static TextView StatusText;
    private LinearLayout customContainer;
    private boolean calling =true;
    private MySurfaceView mySurfaceView,mySurfaceView2;
    private PacketAll packet=null;
    private CameraView cameraView=null;
    private RecordAudio recordAudio;
    private ReceivePlayPacket receivePlayPacket;
    private DatagramSocket ReceivingSocket,SendingSocket;
    private String IP="";
    
    String callieIP;
	boolean isCalled;
	String msg;
	Socket sendPTcpV;
	int tcpPortV;
	ObjectOutputStream sendOutV;
    
    //private EditText ipField;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_call);
        
        ActionBar actionBar=getActionBar();
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.navbar));
        
        tcpPortV = 8001;
        callieIP = getIntent().getStringExtra("callieIP");
		isCalled = getIntent().getBooleanExtra("isCalled",false);
		Log.d("msg","isCalled="+isCalled+"\n"+"callieIP="+callieIP);
		//com.example.videoconferencing.Home.busy = true;
		
		IP = callieIP;
        Log.d("in PacketMain","callieIP = "+callieIP+"isCalled = "+isCalled);
        StatusText = (TextView)findViewById(R.id.StatusTextV);
        surfaceView=(SurfaceView)findViewById(R.id.surView);
        //ipField=(EditText)findViewById(R.id.ip);
        start=(Button)findViewById(R.id.startV);
        stop=(Button)findViewById(R.id.stopV);
        receive=(Button)findViewById(R.id.receive);
        customContainer=(LinearLayout)findViewById(R.id.CustomContainer);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        receive.setOnClickListener(this);
        
        LayoutParams params=new LayoutParams(500, 350);
        mySurfaceView = new MySurfaceView(getApplicationContext());
        mySurfaceView.setLayoutParams(params);
        customContainer.addView(mySurfaceView);
        
       
        
        try 
        {
			ReceivingSocket=new DatagramSocket(9999);
		} 
        catch (SocketException e) 
        {
			e.printStackTrace();
		}

		try 
		{
			SendingSocket=new DatagramSocket();
		} 
		catch (SocketException e) 
		{
			e.printStackTrace();
		}
		stop.setEnabled(false);
		packet=new PacketAll(SendingSocket);
		cameraView=new CameraView(surfaceView,packet,this);
		
		
		
		if(!isCalled)
		{
			stop.setEnabled(false);
			start.setEnabled(false);
			StatusText.setText("Connecting......");
			TcpConnection tcp = new TcpConnection();
			tcp.execute();
		}
		else
		{
			StatusText.setText("Connected");
			stop.setEnabled(false);
		}
    }

    private class TcpConnection extends AsyncTask<String, String, Void>
	{

		@Override
		protected Void doInBackground(String... params) 
		{
			try
			{
				//caller now busy
				Home.busy=true;
			
			Log.d("in Tcp Connection","creating  tcp connection to callie");
			
			//form a socket at caller's side
			sendPTcpV = new Socket(InetAddress.getByName(callieIP),tcpPortV);
			
			//open a stream to send the message
			sendOutV = new ObjectOutputStream(sendPTcpV.getOutputStream());
			Log.d("created ","op stream of video call");
			
			
			msg = "call"+" "+Login.uname+" "+"video";
			Log.i("MESSAGE",msg);
			//send the message
			sendOutV.writeObject(msg);
			sendOutV.flush();
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
		
	}
	@Override
	public void onClick(View v) 
	{
		// TODO Auto-generated method stub
		//IP=(ipField.getText()).toString();
		//IP="192.168.43.89";
		packet.setIP(IP);
		if(v.getId() ==R.id.startV)
		{
			//Log.i("PACKET MAIN","START PRESSED");
			cameraView.startPreview();
			
			packet.reset();
			recordAudio=new RecordAudio(22050,packet);
			recordAudio.startAudioRecording();
			recordAudio.start();
			
			stop.setEnabled(true);
			start.setEnabled(false);
		}
		else if(v.getId() == R.id.stopV)
		{
			//Log.i("PACKET MAIN","STOP PRESSED");
			cameraView.stopPreview();
			Home.busy = false;
			//
			try
			{
				sendOutV.writeObject("end call");
				sendOutV.flush();
			}
			catch(Exception e)
			{
				Log.i("msg","err in sending end call");
			}
			//
			if(recordAudio!=null) recordAudio.stopAudioRecoding();
			if(receivePlayPacket!=null) receivePlayPacket.stopReceiving();
			
			if(recordAudio !=null)
				if(recordAudio.isAlive())
				{
					recordAudio.interrupt();
				}
			if(receivePlayPacket!=null && receivePlayPacket.isAlive()) receivePlayPacket.interrupt();
			start.setEnabled(true);
			receive.setEnabled(true);
			stop.setEnabled(false);
		}
		else
		{
			//Log.i("PACKET MAIN","RECEIVED PRESSED");
			receivePlayPacket=new ReceivePlayPacket(22050, mySurfaceView,ReceivingSocket);
			receivePlayPacket.startReceiving();
			receivePlayPacket.start();
			receive.setEnabled(false);
			stop.setEnabled(true);
		}
	}

	@Override
	protected void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		if(recordAudio !=null) recordAudio.stopAudioRecoding();
		if(receivePlayPacket !=null) receivePlayPacket.stopReceiving();
		if(receivePlayPacket !=null && receivePlayPacket.isAlive()) receivePlayPacket.interrupt();
		if(SendingSocket !=null)
		{
			SendingSocket.close();
		}
		if(ReceivingSocket !=null)
		{
			ReceivingSocket.close();
		}

		new Thread(new SendEndMsg(getApplicationContext())).start();
	}
	
	@Override
	public void onBackPressed() 
	{
		super.onBackPressed();
		//com.example.videoconferencing.Home.busy = false;
		finish();
	}
}