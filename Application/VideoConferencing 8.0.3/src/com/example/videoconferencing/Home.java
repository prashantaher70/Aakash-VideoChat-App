package com.example.videoconferencing;


import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.example.videoconferencing.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends Activity implements OnClickListener
{

	String[] classes={"Home","Contacts","Preferences","Log Out"};
	String[] status={"Available","Busy","Away"};
    private LinearLayout MenuList;
    private LinearLayout DynamicFragParent;
    private ImageButton btnToggleMenuList;
    private ImageButton addContact;
    private int screenWidth;
    private boolean isExpanded=false;
    
    TextView title;
    //new uid to be added to contact book
    private EditText uid;
    //new name to be added to contact book
    private EditText name;
    
  //address book class
  	private DatabaseHandler db ;
  	
  	//contact list for the address book
  	private ArrayList<Contact> contactList;
  	
    //view to display the contact list
  	private ListView listView ;
  	static Activity current;
  	
  	/*
	 * variables to include tcp activity
	 * */
	//
	private ProgressDialog progressBar;
	private RejectCallThread rejectCallThread;
	private AcceptCallThread acceptCallThread;
	
	private RejectCallThreadV rejectCallThreadV;
	private AcceptCallThreadV acceptCallThreadV;

	private RejectCallThreadF rejectCallThreadF;
	private AcceptCallThreadF acceptCallThreadF;
	
	private CountDownTimer countDownTimer;
	//tablet port to receive call
		
		static int tcpPort;
		static int tcpPortV;
		static int tcpPortF;
		
		//sound id to play the ring tone
		public static int playSid;
		//stream id to stop the ring tone
		public static int stopSid;
		//flag set if user is not answering the phone
		private int calleeAvailable;
		
		//IP address of tablet in integer format
		//private int ip_add; use Login.ip_add
		//show the incoming call notification in notification bar when app is in background
		private boolean showNotification;
		//to close the send out stream opened at caller side
		private boolean missedCall;
		//called person is busy or not
		public static boolean busy;
		static String ip_user;
		
		//notification title in notification bar when app is in background
		private String notificationTitle;
		//notification text in notification bar when app is in background
		private String notificationText;
		//messages received during call initiation
		private String msg;
		
		//messages received during video call initiation
		private String mssg;
		
		//server time received
		private String temp;
		
		//application context
		private Context context;
		
		//manages the incoming call notification in notification bar
		private NotificationManager notificationManager;
		//shows the notifications in notification bar
		private Notification myNotification;
		
		//intent to start the app when user clicks the notification
		private Intent myIntent;
		//to dismiss missed call notification
		private Intent missedCallIntent;
		//to resume the app
		private PendingIntent pendingIntent;
		private PendingIntent pendingMissedCallIntent;
	    
		//socket opened at client side
		public static ServerSocket TcpSocket;
		public static ServerSocket TcpSocketV;
		public static ServerSocket TcpSocketF;
		
		//socket on which to receive call initiation messages
		private Socket receivePTcp;
		private Socket receivePTcpV;
		private Socket receivePTcpF;
		
		//socket to send  call initiation messages
		static Socket sendPTcp;
		static Socket sendPTcpV;
		static Socket sendPTcpF;
		
		//stream to receive messages from other user
		static ObjectInputStream receiveIn;
		static ObjectInputStream receiveInV;
		static ObjectInputStream receiveInF;
		
		//stream to send messages to other user
		static ObjectOutputStream sendOut;
		static ObjectOutputStream sendOutV;
		static ObjectOutputStream sendOutF;
		
		//call initiation class 
		private TcpActivity tcpActivity;
		private TcpActivityVideo tcpActivityVideo;
		private TcpActivityFile tcpActivityFile;
		
		//to play a ring tone during a call 
		public static SoundPool soundPool;
		
		TextView StatusText;
		
		
		/*static boolean audioFlag;
		static boolean videoFlag;
		static boolean fileFlag;*/
		
		String[] messageArray;
		/*ends*/
		
		//Group chat array variable -------------------------------------------
		public static String group_chat_code="",left_user="",set_notification;
		public static Map<String,String> UID_IP=new HashMap<String, String>();
		public static ArrayList<String> PREV_GRP_MSG=new ArrayList();
		public static boolean IP_set_flag; 
		public static boolean home_flag_status;  //on create true-------on pause false-------
		public static boolean IP_UID_SET_FLAG=false;
		public static Handler mHandler;
		public static ReceiveFile_Group_Thread rfg;
		public static Thread r_file_group_thread;		
		
    
		// AudioConferenceCall Initializations
		
		public static AudioTrack audioTrack=null;
		public static AudioRecord audioRecord=null;
		public static boolean record = false;
		public static DatagramSocket socketS;
		public static DatagramSocket sockR;
		public static int bufferSize;
		public static AlertDialog.Builder dialog;			//dialog box to show incoming call
		public static AlertDialog.Builder misDialog;		//dialog box to show missed call
		public static AlertDialog alert;
		public static Handler maudioHandler;
		public static Map<String,String> IP_Accept_Flag=new HashMap<String,String>();
		public static String calling_user;
		
    @Override
    public void onCreate(Bundle savedInstanceState) 
   {
        super.onCreate(savedInstanceState);
        
        messageArray = new String[10];
        IP_UID_SET_FLAG=false;
        showNotification = false;
        setContentView(R.layout.home);
        
        title = (TextView)findViewById(R.id.title);
        title.setText(Login.uname);
        
        current = this;
        context = this;
     // gets the activity's default ActionBar
        ActionBar actionBar = getActionBar();
        actionBar.hide();
        MenuList = (LinearLayout) findViewById(R.id.menuList);
        DynamicFragParent = (LinearLayout) findViewById(R.id.linearLayout3);
        
        btnToggleMenuList = (ImageButton) findViewById(R.id.togBt);
        
        addContact = (ImageButton)findViewById(R.id.addContact);
        
        
        
        db = new DatabaseHandler(this);
        
        
        
        
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        
        btnToggleMenuList.setOnClickListener(this);
        
        LayoutParams lyp =  MenuList.getLayoutParams();
        lyp.width=(int)(screenWidth*0.3);
        DynamicFragParent.setX((float) 0.0);
        ProfileFrag profile =new ProfileFrag();
        MenuList.setVisibility(View.GONE);
        android.app.FragmentManager fragmentManager = getFragmentManager();  
        android.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.contents,profile);  
        fragmentTransaction.commit();
        
        
        addContact.setOnClickListener(this);
        
        
        
        
        Log.d("msg","entered in oncreate");
		/*variables initialization of tcp activity*/
		 tcpPort = 8000;
		 tcpPortV = 8001;
		 tcpPortF = 8002;
		//application context
	        context = this;
	        
	        missedCall = true;
	        
	        busy = false;
	        //login = false;
	        showNotification = false;
	        
	      //get notification service of the system
	        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
	      //notification to be shown when activity is in background
	        myNotification = new Notification(R.drawable.ic_launcher,"Incoming Call",System.currentTimeMillis());
	        
	        Log.d("msg","notification manager created");
	      
	        //intent to resume the activity when user clicks the notification
	        //check this
	        myIntent = new Intent(this, Home.class); 
	        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	        
	        Log.d("msg","creating pending intent");
	        
	        home_flag_status=true;
	        
	        // Handler for Audio conference Call 
	        maudioHandler = new Handler() {
	            public void handleMessage(Message msg) {
	            
	            	if(msg.what==0){
	            		//show_Notification_Group_chat();
	            		//Toast.makeText(getApplicationContext(), "Audio Conferencing Call Request Receiving",Toast.LENGTH_SHORT).show();
	            		Log.i("msg", "dialog.setmessage()");
	            		dialog.setMessage(calling_user + " wants to have a audio conference call...\n ");
	            		dialog.setTitle("Audio Conference Call");
	            		Log.i("msg", "now dialog wl b created");
	            		alert = dialog.create();
	            		Log.i("msg", "now dialog wl b shown");
						
	            		// To play sound on Dialog appearance
	            		//start the ringing tone to indicate the incoming call
						AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
				        float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				        stopSid=soundPool.play(playSid, volume, volume, 1,0,1f);
				        alert.show();
				        
	            	}
	            	else if(msg.what==1){
	            		Toast.makeText(getApplicationContext(), "User "+left_user+" left the Audio Conferencing!!!", Toast.LENGTH_SHORT).show();
	            		
	            	}
	            		
	            		
	           	}
	        };
	    	
	        mHandler = new Handler() {
	            public void handleMessage(Message msg) {
	            
	            	if(msg.what==0){
	            		show_Notification_Group_chat();	
	            	}
	            	else if(msg.what==1){
	            		Toast.makeText(getApplicationContext(), "User "+left_user+" left the Group Chat!!!", Toast.LENGTH_SHORT).show();
	            	}
	            	else if(msg.what==2){
	            		Toast.makeText(getApplicationContext(), "Something Happend"+"User "+left_user+" left the Group Chat!!!", Toast.LENGTH_SHORT).show();
	            	}
	            	else if(msg.what==3){
	            		Toast.makeText(getApplicationContext(), "RECEIVINGFILE \n \t"+set_notification, Toast.LENGTH_SHORT).show();
	            	}
	            	else if(msg.what==999){
	            		Log.i("Home Handler for logout ","calling finish in handler");
	            		finish();
	            	}
	            		
	            		
	           	}
	        };        
	        StatusText = (TextView)findViewById(R.id.tvStatus);
	        Log.d("msg","creating countdown");
	        
	        countDownTimer = new CountDownTimer(60000, 2000) {
				
				@Override
				public void onTick(long millisUntilFinished) 
				{
					
				}	
				
				@Override
				public void onFinish() {
					
					//if user receives the call
					if(calleeAvailable!=1)
					{
						
					}
					//if user is not answering the call
					else if(busy)
					{
						StatusText.setText("User Not\nAnswering!!");
					}
				}
			};
	        
			//initialize the socket for call initiation at tcpPort with one user waiting in queue
			try
	        {
				Log.d("msg","creating server socket");
				
				TcpSocket = new ServerSocket(tcpPort,1);/*for audio call*/
				TcpSocketV = new ServerSocket(tcpPortV,1);/*for video call*/
				TcpSocketF = new ServerSocket(tcpPortF,1);/*for file request*/
				
				Log.d("msg","created server socket");
	        }
	        catch ( EOFException eof ) 
	        {
	        	 Toast.makeText(getApplicationContext(),"ERROR!!!\nApplication will now exit.", Toast.LENGTH_LONG).show();
	        	 //timer(6000);
	        }
	        catch ( IOException ioe ) 
	        {
	        	Toast.makeText(getApplicationContext(), ioe.toString(), Toast.LENGTH_LONG).show(); 
	        	//Toast.makeText(getApplicationContext(), "ERROR!!!\nApplication will now exit.1", Toast.LENGTH_LONG).show();
	        	 //timer(6000);
	        	 
	        }
			//Log.d("msg","creating udp socket");
			//create a DataGram socket for communication with server
			
			soundActivate();
             
		/*ends*/
        
			//user now waits for a call
			tcpActivity = new TcpActivity(context);           /*for audio call*/
			Thread tcpThread=new Thread(tcpActivity);
			tcpThread.start();
			
			tcpActivityVideo = new TcpActivityVideo(context);   /*for video call*/
			Thread tcpThreadVideo=new Thread(tcpActivityVideo);
			tcpThreadVideo.start();
			
			tcpActivityFile = new TcpActivityFile(context);           /*for file transfer*/
			Thread tcpThreadFile=new Thread(tcpActivityFile);
			tcpThreadFile.start();

		ReceiveFile rF=new ReceiveFile();
		Thread x=new Thread(rF);
		x.start();
		
		group_message_receive_thread gmr=new group_message_receive_thread(35012, this.getApplicationContext());
		Thread gmr_t=new Thread(gmr);
		gmr_t.start();
		
		// Starting a new Thread for Audio Conference Call request receiving 
		
				Group_audio_conference_receive gac=new Group_audio_conference_receive(35015, this.getApplicationContext());
				Thread gac_t=new Thread(gac);
				gac_t.start();
		
		rfg = new ReceiveFile_Group_Thread(35021);
		r_file_group_thread = new Thread(rfg);
		r_file_group_thread.start();
		
   }
    
    
    //-----------------------------------------Notication for group chat
    public void show_Notification_Group_chat(){
    	NotificationManager nm= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notify_msg = new Notification(android.R.drawable.stat_notify_chat,"New Message",System.currentTimeMillis());
        Context context = Home.this;
        CharSequence title = "Group Message";
        CharSequence message = "Open to see Message";
        
        Intent intent = new Intent(context,GroupchatActivity.class);
//        intent.putExtra("msg_to",set_notification_uid);
//        intent.putExtra("callieIP",set_notification_ip);
//        intent.putExtra("flag_notification",true);
        
        
        PendingIntent pending = PendingIntent.getActivity(context, 0, intent, 0);
        notify_msg.setLatestEventInfo(context, title, message, pending);
        notify_msg.sound = Uri.parse("android.resource://com.example.videoconferencing/"+R.raw.notification);
        nm.notify(0, notify_msg);
    	
    }
    
   
    
    //Function to setup the soundpool for playing the sound when there is a incoming call
    public void soundActivate()
    {
    	 soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC,10);
    	 playSid = soundPool.load(this, R.raw.ringtone_audio, 0);
    }
	@Override
	public void onClick(View v) 
	{
		if(v.getId() == R.id.addContact)
		{
			//Creating a custom layout for a dialog box
			LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.addc, null);
            uid = (EditText)textEntryView.findViewById(R.id.etUID);
            name = (EditText)textEntryView.findViewById(R.id.etNAME);
            //Creating the dialog box
            new AlertDialog.Builder(this)
            .setTitle("Add Contact")
            .setView(textEntryView)
            .setPositiveButton("Add", new DialogInterface.OnClickListener() 
            {
                public void onClick(DialogInterface dialog, int whichButton) 
                {
                	
                	String i = uid.getText().toString();//Fetching the roll from the text box
            		
            		String Name = name.getText().toString();//Fetching the name from the text box
            		
            		if(!Pattern.matches("[a-zA-Z0-9_]+",i))	
            			Toast.makeText(getApplicationContext(), "Enter Proper Roll", Toast.LENGTH_LONG).show();
            	
            		//Checking that name is valid or not
            		else if(Name.length() == 0)
            			Toast.makeText(getApplicationContext(), "Enter Proper Name", Toast.LENGTH_LONG).show();
            		
            		else if(db.uidexist(i) == 0)//Checking whether the roll entered is already present or not
            		{
            			try
            			{
            			db.addContact(new Contact(i, Name));//adding the contact to database
            			}
            			catch(Exception e)
            			{
            				Toast.makeText(getApplicationContext(), "err to add in database", Toast.LENGTH_LONG).show();
            			}
            		}
            		
            		else
            			Toast.makeText(getApplicationContext(), "Roll Exist", Toast.LENGTH_LONG).show();
                	
	        		
	        		
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                	
                }
            })
            .show();
		}
		else
		{
			if (isExpanded) 
			{
	            isExpanded = false;
	            MenuList.setVisibility(View.GONE);
	            MenuList.startAnimation(new CollapseAnimation(DynamicFragParent,(int)(screenWidth*0.3), 0.01));
	        }
			else 
			{
	            isExpanded = true;
	            MenuList.setVisibility(View.VISIBLE);
	            MenuList.startAnimation(new ExpandAnimation(DynamicFragParent,(int)(screenWidth*0.3), 0.01));
	        }
		}
	}
	
	
	
/*---------------------------------TcpActivity for audio call----------------------------------------------*/

	
	public class TcpActivity  implements Runnable
    {
    	//private variables
    	private String [] arr;						//store constituent words in received message
		private AlertDialog.Builder dialog;			//dialog box to show incoming call
		private AlertDialog.Builder misDialog;		//dialog box to show missed call
		private AlertDialog alert;					//shows the dialog created
		private String caller;						//name of the user calling
		private Context context;					//context of main activity
		private Contact c1;							//item in the contact list corresponding to caller
	
		
		//constructor
		public TcpActivity(Context c)
		{
			
			context = c;
			c1 = new Contact();
			
			runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					// TODO Auto-generated method stub
					//dialog box to show incoming call
					dialog = new AlertDialog.Builder(context);
					dialog.setCancelable(false);
					
					dialog.setPositiveButton("ACCEPT",new DialogInterface.OnClickListener() 
							{

								public void onClick(DialogInterface dialog, int which) 
								{
									/*
									 * 
									 user accepts the call by pressing "ACCEPT" button
									 the ringing tone-->stopped
									 it sends back a specific message to indicate to caller that it has accepted the call
									 display updated to show that call is connected
									 
									 * 
									 */
									missedCall=false;
									
									//stop ringing tone
									soundPool.stop(stopSid);
									
									//get the name of caller as saved in contact list
									arr = msg.split(" ");
									
									caller = arr[1];
									
									//set display to indicate call is connected
									//statusImage.setImageResource(R.drawable.connected);
									//StatusText.setText("Connected to \n"+caller);
									progressBar = new ProgressDialog(context);
									progressBar.setCancelable(false);
									progressBar.setMessage("Connecting ...");
									progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
									//progressBar.show();
									acceptCallThread = new AcceptCallThread();
									acceptCallThread.execute();
								}
								
								
							});
				        	
		        	dialog.setNegativeButton("REJECT", new DialogInterface.OnClickListener() 
		        	{

						public void onClick(DialogInterface arg0, int which) 
						{
							rejectCallThread = new RejectCallThread();
							rejectCallThread.execute();
						}
						
						
					});
		        	
				        	//dialog box to show missed call
				        	misDialog = new AlertDialog.Builder(context);
							//misDialog.setCancelable(false);
							
							misDialog.setNegativeButton("OK",new DialogInterface.OnClickListener() 
									{

										public void onClick(DialogInterface arg0, int which) 
										{
											/*
											 * 
											 when user presses "OK" cancel the dialog box and do nothing
											 * 
											 */
													arg0.cancel();
										}
										
									});
				}
			});
				      
		}
		
		@Override
		public void run()
		{
			/*
			 * 
			 while the user is logged-in:
			 accepts connection request from other user
			 create socket and input-output streams for sending and receiving messages to establish a call
			 process the incoming messages, and accordingly reply and perform other operations:
			 		incoming call,missed call-->opens an alert box or shows as a notification
			 		
			 close the streams and sockets when call ends
			 * 
			 */
			try 
			{
				//check if the client is logged-in
				while(true)
				{
				
				//indicate the remote user has not given response to the call request
				calleeAvailable=1;
				missedCall=true;
				
				Log.d("msg","listening incomin connection");
				
				//accept connection from another client
				receivePTcp = TcpSocket.accept();
				Log.d("msg","connection request accepted..");
				//set the sampling rate for the audio-calling to that set by user
				//audioCall.setFrequency(frequency);
				//get IP add of the caller
				temp = receivePTcp.getInetAddress().getHostAddress();
				Log.i("ip of target host",temp);
				//create an input stream on the server socket
				receiveIn = new ObjectInputStream(receivePTcp.getInputStream());
				
				//the user in not calling any other user, OR if user is calling other user then the same user is trying to connect
				if(true)/* || (busy && ip_user.equalsIgnoreCase(temp)) */
				{
		        	ip_user=temp;
		        	do
					{
		        		//get the message received from the input-stream
						msg = receiveIn.readObject().toString();
						Log.d("msg received in do while loop",msg);
						messageArray = msg.split(" ");
						//indicate that callee is available and has response to its call
						calleeAvailable=5;
						
						publishProgress(msg);
			
					}
		        	while(!msg.equalsIgnoreCase("reject call") && !msg.equalsIgnoreCase("end call") && !msg.equalsIgnoreCase("end call2"));
		        	
		        	if(msg.equalsIgnoreCase("end call"))
					{
		        		sendOut.writeObject("end call2");
						sendOut.flush();
						Log.d("test","received and now sending end call..");
					}
		        	if(msg.equalsIgnoreCase("reject call"))/*to set status text of call initiator*/
	        		{
		        		sendOut.writeObject("end call2");
						sendOut.flush();
						Log.d("test","received reject call and now sending end call..");
	        		}
		        	//the other user has aborted the call at its side
		        	//if(msg.equalsIgnoreCase("reject call"))
					//{
						//sendOut.writeObject("end call");
					//}
		        	Log.i("HOME","Call ended");
		        	//close the input-output streams and sockets
					closeConnection();
				}
			}
		}
			
        catch ( EOFException eof ) 
        {
        	closeConnection();
        	Log.d("in while true","EOF exception");
        	
        }
        catch ( IOException ioe ) 
        {
        	closeConnection();
        	Log.d("in while true","IO exception");
        } 
		catch (ClassNotFoundException e) 
        {
			closeConnection();
			Log.d("in while true","CNF exception");
		}
	}
		
		public void closeConnection()
		{
			try 
        	{
				Log.d("close connection","in close connection");
				publishProgress("close connection");
				//indicate that the user is free to start or receive a call
        		busy=false;
        		if(receiveIn !=null)receiveIn.close();
				if(receivePTcp !=null) receivePTcp.close();
				//when the remote user was available and gave response to its incoming call-->send-out socket was created
				receiveIn=null;
				receivePTcp=null;
				
				if(sendOut !=null) sendOut.close();
				if(sendPTcp !=null) sendPTcp.close();
				sendOut=null;
				sendPTcp=null;
			}
			catch (IOException f) 
        	{
				Log.d("in close connection","err closing in IO");
			}
			
		}
		public void publishProgress(String value)
		{
			arr = value.split(" ");
			Log.i("in publish progress with: ",value);
			//user received a call request
			runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					// TODO Auto-generated method stub
					
					if(arr[0].equalsIgnoreCase("call"))
					{
						Log.d("msg","in publish progress file");
							caller = arr[1];
							
							if(showNotification)
							{
								//cancel the notification for missed call from same user
								notificationManager.cancel(arr[1],2);
								//indicate the notification is for incoming call
								notificationTitle = "Incoming Call";
								//set the notification text as name of the caller
								notificationText = caller;
								
					        	//create a new notification
								myNotification.setLatestEventInfo(context,notificationTitle,notificationText, pendingIntent);
								notificationManager.notify(arr[1],1, myNotification);
							}
							//the activity is running -->show the incoming call as alert dialog box
							else
							{
								//start the ringing tone to indicate the incoming call
								AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
						        float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
						        stopSid=soundPool.play(playSid, volume, volume, 1,0,1f);
							}
						/*
						if(fileFlag)
						{
							dialog.setMessage(caller + " Requesting File Transfer...\n     ");
						}
						else if(audioFlag)
						{
							dialog.setMessage(caller + " Requesting Audio Call...\n     ");
						}
						else if(videoFlag)
						{
							dialog.setMessage(caller + " Requesting Video Call...\n     ");
						}*/
							
						dialog.setMessage(caller + " Requesting Audio Call...\n     ");
						alert = dialog.create();
						alert.show();
					
		        	}
					else if(arr[0].equalsIgnoreCase("success"))
					{
						Call.start.setEnabled(true);
						Call.stop.setEnabled(true);
						Call.StatusText.setText("Connected");
						/*if(fileFlag)
						{
							FileShare.bOpen.setEnabled(true);
							FileShare.bSend.setEnabled(true);
							FileShare.tvFileStatus.setText("Connected");
						}
						else if(audioFlag)
						{
							Call.start.setEnabled(true);
							Call.stop.setEnabled(true);
							Call.StatusText.setText("Connected");
						}*/
					}
					//remote user has rejected the call request
		        	else if(arr[0].equalsIgnoreCase("reject"))
		        	{
		        		/*if(fileFlag)
						{
		        			FileShare.tvFileStatus.setText("call has been rejected..");
						}
						else if(audioFlag)
						{
							Call.StatusText.setText("call has been rejected..");
						}*/
		        		Call.StatusText.setText("call has been rejected..");
		        	}
				/*
		        	else if(arr[0].equalsIgnoreCase("end call"))
		        	{
		        		   try
		        		   {
		        			   	sendOut.writeObject("end call");
		        		   }
		        		   catch(Exception e)
		        		   {
		        			    Log.i("msg","err in sending end call in  home");
		        		   }
		        	}*/
					//there is error in app
		        	else if(arr[0].equalsIgnoreCase("Fatal"))
		        	{
		        		        		
		        		Toast.makeText(context, "FAtAl", Toast.LENGTH_LONG).show();
		        		//timer(5000);
		        	}
					
				}
			});
        		
		}
    
    }
	
	private class SendEndAudio extends AsyncTask<Void, String, Void>
	{
		protected Void doInBackground(Void... arg0)
		{
			try
			{
				if(sendOut!=null)
				{
					sendOut.writeObject("end call");
					sendOut.flush();
				}
				else
				{
					Log.d("in SendEndCall","sendOutF is null");
				}
			}
			catch ( EOFException eof ) 
		    {
		    	Log.d("in SendEndCall","EOF exception");
		    	
		    }
		    catch ( IOException ioe ) 
		    {
		    	Log.d("in SendEndCall","IO exception");
		    }
			return null;
		}
	}
	
	
	/*
	 * Accept call thread
	 * */
	
	private class AcceptCallThread extends AsyncTask<Void, String, Void>
	{

		@Override
		protected Void doInBackground(Void... arg0) 
		{
			try 
			{
				//create and connect a TCP socket to reply to caller 
				sendPTcp = new Socket(InetAddress.getByName(ip_user),tcpPort);
				
				//create an Output stream for above socket
				sendOut = new ObjectOutputStream(sendPTcp.getOutputStream());
				sendOut.flush();
				
				//send message to caller to indicate that it has accepted the call
				sendOut.writeObject("success");
				Log.i("in accept call thread","Success sent");
				
				Intent i = new Intent(getApplicationContext(),Call.class).putExtra("callieIP",temp).putExtra("isCalled", true);
	        	startActivity(i);
				
	        	/*code to start the audio intent
				if(audioFlag && !videoFlag && !fileFlag)
				{
					Intent i = new Intent(getApplicationContext(),Call.class).putExtra("callieIP",temp).putExtra("isCalled", true);
		        	startActivity(i);
				}
				*/
			}
			catch (IOException e) 
			{
				Log.d("in accept call thread of audio","err");
				publishProgress("error");
			}
			
			
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) 
		{
			super.onProgressUpdate(values);
			if(values[0].equalsIgnoreCase("error"))
			{
				progressBar.dismiss();
				progressBar = null;
				Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_LONG).show();
			}
			
			else if(values[0].equalsIgnoreCase("call"))
			{
				progressBar.dismiss();
				progressBar = null;
				//disable the call button until the call ends
				//call.setEnabled(false);
				//enable end-call button
				//endCall.setEnabled(true);
			}
		}
		
	}
	/*
	 * reject call thread
	 * */
	private class RejectCallThread extends AsyncTask<Void, String, Void>
	{

		@Override
		protected Void doInBackground(Void... arg0) 
		{
			/*
			 * 
			 user rejects the call by pressing "REJECT" button
			 the ringing tone-->stopped
			 it sends back a specific message to indicate to caller that it has rejected the call
			 
			 * 
			 */
			try 
			{
				 missedCall=false;
				//create and connect a TCP socket to reply to caller 
				sendPTcp = new Socket(InetAddress.getByName(ip_user),tcpPort);
				//create an Output stream for above socket
				sendOut = new ObjectOutputStream(sendPTcp.getOutputStream());
				sendOut.flush();
				
				//send message to indicate to caller that it has rejected the call
				sendOut.writeObject("reject call");
				//stop the ringing tone
				soundPool.stop(stopSid);
			}
			catch (IOException e) 
			{
				publishProgress("error");
			}
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) 
		{
			
			super.onProgressUpdate(values);
			
			if(values[0].equalsIgnoreCase("OK"))
			{
				progressBar.dismiss();
				progressBar = null;
				
			}
			else if(values[0].equalsIgnoreCase("error"))
			{
				
				progressBar.dismiss();
				progressBar = null;
				
				Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_LONG).show();
			}
		}
		
	}
	
	
	
/*--------------------------------TcpActivity for video call----------------------------------------------*/	
	
	
	
	
	
	
	public class TcpActivityVideo  implements Runnable
	{
		//private variables
		private String [] arr;						//store constituent words in received message
		private AlertDialog.Builder dialog;			//dialog box to show incoming call
		private AlertDialog.Builder misDialog;		//dialog box to show missed call
		private AlertDialog alert;					//shows the dialog created
		private String caller;						//name of the user calling
		private Context context;					//context of main activity
		private Contact c1;							//item in the contact list corresponding to caller

		
		//constructor
		public TcpActivityVideo(Context c)
		{
			
			context = c;
			c1 = new Contact();
			
			runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					// TODO Auto-generated method stub
					//dialog box to show incoming call
					dialog = new AlertDialog.Builder(context);
					dialog.setCancelable(false);
					
					dialog.setPositiveButton("ACCEPT",new DialogInterface.OnClickListener() 
							{

								public void onClick(DialogInterface dialog, int which) 
								{
									/*
									 * 
									 user accepts the call by pressing "ACCEPT" button
									 the ringing tone-->stopped
									 it sends back a specific message to indicate to caller that it has accepted the call
									 display updated to show that call is connected
									 
									 * 
									 */
									missedCall=false;
									
									//stop ringing tone
									soundPool.stop(stopSid);
									
									//get the name of caller as saved in contact list
									arr = mssg.split(" ");
									
									caller = arr[1];
									
									acceptCallThreadV = new AcceptCallThreadV();
									acceptCallThreadV.execute();
								}
								
								
							});
				        	
		        	dialog.setNegativeButton("REJECT", new DialogInterface.OnClickListener() 
		        	{

						public void onClick(DialogInterface arg0, int which) 
						{
							rejectCallThreadV = new RejectCallThreadV();
							rejectCallThreadV.execute();
						}
						
						
					});
		        	
				        	//dialog box to show missed call
				        	misDialog = new AlertDialog.Builder(context);
							//misDialog.setCancelable(false);
							
							misDialog.setNegativeButton("OK",new DialogInterface.OnClickListener() 
									{

										public void onClick(DialogInterface arg0, int which) 
										{
											/*
											 * 
											 when user presses "OK" cancel the dialog box and do nothing
											 * 
											 */
													arg0.cancel();
										}
										
									});
				}
			});
				      
		}
		
		@Override
		public void run()
		{
			/*
			 * 
			 while the user is logged-in:
			 accepts connection request from other user
			 create socket and input-output streams for sending and receiving messages to establish a call
			 process the incoming messages, and accordingly reply and perform other operations:
			 		incoming call,missed call-->opens an alert box or shows as a notification
			 		
			 close the streams and sockets when call ends
			 * 
			 */
			try 
			{
				//check if the client is logged-in
				while(true)
				{
				
				//indicate the remote user has not given response to the call request
				calleeAvailable=1;
				missedCall=true;
				
				Log.d("mssg","listening incoming connection for video call");
				
				//accept connection from another client
				receivePTcpV = TcpSocketV.accept();
				Log.d("mssg","video call request accepted..");
				//set the sampling rate for the audio-calling to that set by user
				//audioCall.setFrequency(frequency);
				//get IP add of the caller
				temp = receivePTcpV.getInetAddress().getHostAddress();
				Log.i("ip of target host",temp);
				//create an input stream on the server socket
				receiveInV = new ObjectInputStream(receivePTcpV.getInputStream());
				
				//the user in not calling any other user, OR if user is calling other user then the same user is trying to connect
				if(true)/* || (busy && ip_user.equalsIgnoreCase(temp)) */
				{
		        	ip_user=temp;
		        	do
					{
		        		//get the message received from the input-stream
						mssg = receiveInV.readObject().toString();
						Log.d("mssg received in do while loop video",mssg);
						if(mssg.equalsIgnoreCase("success"))
							Log.d("in do/while","success received");
						messageArray = mssg.split(" ");
						
						//indicate that callee is available and has response to its call
						//calleeAvailable=5;
						
						publishProgress(mssg);
						
		        		
					}
		        	while(!mssg.equalsIgnoreCase("reject call") && !mssg.equalsIgnoreCase("end call") && !mssg.equalsIgnoreCase("end call2"));
		        	
		        	if(mssg.equalsIgnoreCase("end call"))
					{
		        		sendOutV.writeObject("end call2");
						sendOutV.flush();
						Log.d("test","received and now sending end call..");
					}
		        	else if(mssg.equalsIgnoreCase("reject call"))/*to set status text of call initiator*/
	        		{
		        		publishProgress(mssg);
	        		}
		        	//the other user has aborted the call at its side
		        	//if(mssg.equalsIgnoreCase("reject call"))
					//{
						//sendOutV.writeObject("end call");
					//}
		        	Log.i("HOME","Video Call ended");
		        	//close the input-output streams and sockets
					closeConnectionVideo();
				}
			}
		}
			
	    catch ( EOFException eof ) 
	    {
	    	closeConnectionVideo();
	    	Log.d("in while true","EOF exception");
	    	
	    }
	    catch ( IOException ioe ) 
	    {
	    	closeConnectionVideo();
	    	Log.d("in while true","IO exception");
	    } 
		catch (ClassNotFoundException e) 
	    {
			closeConnectionVideo();
			Log.d("in while true","CNF exception");
		}
	}
		
	
		public void closeConnectionVideo()
		{
			try 
	    	{
				Log.d("close connection","in close connection");
				//publishProgress("close connection");
				//indicate that the user is free to start or receive a call
	    		busy=false;
	    		if(receiveInV !=null)receiveInV.close();
				if(receivePTcpV !=null) receivePTcpV.close();
				//when the remote user was available and gave response to its incoming call-->send-out socket was created
				receiveInV=null;
				receivePTcpV=null;
				
				if(sendOutV !=null) sendOutV.close();
				if(sendPTcpV !=null) sendPTcpV.close();
				sendOutV = null;
				sendPTcpV = null;
			}
			catch (IOException f) 
	    	{
				Log.d("in close connection","err closing in IO");
			}
			
		}
		
		
		public void publishProgress(String value)
		{
			arr = value.split(" ");
			Log.i("in publish progress with: ",value);
			//user received a call request
			runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					// TODO Auto-generated method stub
					
					if(arr[0].equalsIgnoreCase("call"))
					{
						Log.d("mssg","in publish progress file");
							caller = arr[1];
							
							if(showNotification)
							{
								//cancel the notification for missed call from same user
								notificationManager.cancel(arr[1],2);
								//indicate the notification is for incoming call
								notificationTitle = "Incoming Call";
								//set the notification text as name of the caller
								notificationText = caller;
								
					        	//create a new notification
								myNotification.setLatestEventInfo(context,notificationTitle,notificationText, pendingIntent);
								notificationManager.notify(arr[1],1, myNotification);
							}
							//the activity is running -->show the incoming call as alert dialog box
							else
							{
								//start the ringing tone to indicate the incoming call
								AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
						        float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
						        stopSid=soundPool.play(playSid, volume, volume, 1,0,1f);
							}
						/*
						if(fileFlag)
						{
							dialog.setMessage(caller + " Requesting File Transfer...\n     ");
						}
						else if(audioFlag)
						{
							dialog.setMessage(caller + " Requesting Audio Call...\n     ");
						}
						else if(videoFlag)
						{
							dialog.setMessage(caller + " Requesting Video Call...\n     ");
						}*/
							
						dialog.setMessage(caller + " Requesting Video Call...\n     ");
						alert = dialog.create();
						alert.show();
					
		        	}
					else if(arr[0].equalsIgnoreCase("success"))
					{
						videoCall.PacketMain.start.setEnabled(true);
						videoCall.PacketMain.stop.setEnabled(false);
						videoCall.PacketMain.StatusText.setText("Connected");
						/*if(fileFlag)
						{
							FileShare.bOpen.setEnabled(true);
							FileShare.bSend.setEnabled(true);
							FileShare.tvFileStatus.setText("Connected");
						}
						else if(audioFlag)
						{
							Call.start.setEnabled(true);
							Call.stop.setEnabled(true);
							Call.StatusText.setText("Connected");
						}*/
					}
					//remote user has rejected the call request
		        	else if(arr[0].equalsIgnoreCase("reject"))
		        	{
		        		/*if(fileFlag)
						{
		        			FileShare.tvFileStatus.setText("call has been rejected..");
						}
						else if(audioFlag)
						{
							Call.StatusText.setText("call has been rejected..");
						}*/
		        		videoCall.PacketMain.StatusText.setText("call has been rejected..");
		        	}/*
		        	else if(arr[0].equalsIgnoreCase("end call"))
		        	{
		        		   try
		        		   {
		        			   	sendOutV.writeObject("end call");
		        		   }
		        		   catch(Exception e)
		        		   {
		        			    Log.i("mssg","err in sending end call in  home");
		        		   }
		        	}*/
					//there is error in app
		        	else if(arr[0].equalsIgnoreCase("Fatal"))
		        	{
		        		        		
		        		Toast.makeText(context, "FAtAl", Toast.LENGTH_LONG).show();
		        		//timer(5000);
		        	}
					
				}
			});
	    		
		}

	}

	private class SendEndVideo extends AsyncTask<Void, String, Void>
	{
		protected Void doInBackground(Void... arg0)
		{
			try
			{
				if(sendOutV!=null)
				{
					sendOutV.writeObject("end call2");
					sendOutV.flush();
				}
				else
				{
					Log.d("in SendEndCall","sendOutF is null");
				}
			}
			catch ( EOFException eof ) 
		    {
		    	Log.d("in SendEndCall","EOF exception");
		    	
		    }
		    catch ( IOException ioe ) 
		    {
		    	Log.d("in SendEndCall","IO exception");
		    }
			return null;
		}
	}
	
	/*
	 * Accept call thread
	 * */

	private class AcceptCallThreadV extends AsyncTask<Void, String, Void>
	{

		@Override
		protected Void doInBackground(Void... arg0) 
		{
			try 
			{
				//create and connect a TCP socket to reply to caller 
				sendPTcpV = new Socket(InetAddress.getByName(ip_user),tcpPortV);
				
				//create an Output stream for above socket
				sendOutV = new ObjectOutputStream(sendPTcpV.getOutputStream());
				sendOutV.flush();
				
				//send message to caller to indicate that it has accepted the call
				sendOutV.writeObject("success");
				Log.i("in accept call thread","Success sent");
				
				Intent i = new Intent(getApplicationContext(),videoCall.PacketMain.class).putExtra("callieIP",temp).putExtra("isCalled", true);
	        	startActivity(i);
				
	        	/*code to start the audio intent
				if(audioFlag && !videoFlag && !fileFlag)
				{
					Intent i = new Intent(getApplicationContext(),Call.class).putExtra("callieIP",temp).putExtra("isCalled", true);
		        	startActivity(i);
				}
				*/
			}
			catch (IOException e) 
			{
				publishProgress("error");
			}
			
			
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) 
		{
			super.onProgressUpdate(values);
			if(values[0].equalsIgnoreCase("error"))
			{
				progressBar.dismiss();
				progressBar = null;
				Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_LONG).show();
			}
			
			else if(values[0].equalsIgnoreCase("call"))
			{
				progressBar.dismiss();
				progressBar = null;
				//disable the call button until the call ends
				//call.setEnabled(false);
				//enable end-call button
				//endCall.setEnabled(true);
			}
		}
		
	}
	/*
	 * reject call thread
	 * */
	private class RejectCallThreadV extends AsyncTask<Void, String, Void>
	{

		@Override
		protected Void doInBackground(Void... arg0) 
		{
			/*
			 * 
			 user rejects the call by pressing "REJECT" button
			 the ringing tone-->stopped
			 it sends back a specific message to indicate to caller that it has rejected the call
			 
			 * 
			 */
			try 
			{
				 missedCall=false;
				//create and connect a TCP socket to reply to caller 
				sendPTcpV = new Socket(InetAddress.getByName(ip_user),tcpPortV);
				//create an Output stream for above socket
				sendOutV = new ObjectOutputStream(sendPTcpV.getOutputStream());
				sendOutV.flush();
				
				//send message to indicate to caller that it has rejected the call
				sendOutV.writeObject("reject call");
				//stop the ringing tone
				soundPool.stop(stopSid);
			}
			catch (IOException e) 
			{
				publishProgress("error");
			}
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) 
		{
			
			super.onProgressUpdate(values);
			
			if(values[0].equalsIgnoreCase("OK"))
			{
				progressBar.dismiss();
				progressBar = null;
				
			}
			else if(values[0].equalsIgnoreCase("error"))
			{
				
				progressBar.dismiss();
				progressBar = null;
				
				Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_LONG).show();
			}
		}
		
	}
	
	
	
	
/*--------------------------------TcpActivity for file transfer-------------------------------------------*/
	
	//static boolean flag;
	
	public class TcpActivityFile  implements Runnable
	{
		//private variables
		private String [] arr;						//store constituent words in received message
		private AlertDialog.Builder dialog;			//dialog box to show incoming call
		private AlertDialog.Builder misDialog;		//dialog box to show missed call
		private AlertDialog alert;					//shows the dialog created
		private String caller;						//name of the user calling
		private Context context;					//context of main activity
		private Contact c1;							//item in the contact list corresponding to caller

		
		//constructor
		public TcpActivityFile(Context c)
		{
			
			context = c;
			c1 = new Contact();
			
			runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					// TODO Auto-generated method stub
					//dialog box to show incoming call
					dialog = new AlertDialog.Builder(context);
					dialog.setCancelable(false);
					
					dialog.setPositiveButton("ACCEPT",new DialogInterface.OnClickListener() 
							{

								public void onClick(DialogInterface dialog, int which) 
								{
									//stop ringing tone
									soundPool.stop(stopSid);
									
									//get the name of caller as saved in contact list
									arr = mssg.split(" ");
									
									caller = arr[1];
									
									acceptCallThreadF = new AcceptCallThreadF();
									acceptCallThreadF.execute();
									//flag = true;
								}
								
								
							});
				        	
		        	dialog.setNegativeButton("REJECT", new DialogInterface.OnClickListener() 
		        	{

						public void onClick(DialogInterface arg0, int which) 
						{
							rejectCallThreadF = new RejectCallThreadF();
							rejectCallThreadF.execute();
						}
						
						
					});
		        	
		        	//dialog box to show missed call
		        	misDialog = new AlertDialog.Builder(context);
					//misDialog.setCancelable(false);
					
					misDialog.setNegativeButton("OK",new DialogInterface.OnClickListener() 
									{

										public void onClick(DialogInterface arg0, int which) 
										{
											/*
											 * 
											 when user presses "OK" cancel the dialog box and do nothing
											 * 
											 */
													arg0.cancel();
										}
										
									});
				}
			});
				      
		}
		
		@Override
		public void run()
		{
			/*
			 * 
			 while the user is logged-in:
			 accepts connection request from other user
			 create socket and input-output streams for sending and receiving messages to establish a call
			 process the incoming messages, and accordingly reply and perform other operations:
			 		incoming call,missed call-->opens an alert box or shows as a notification
			 		
			 close the streams and sockets when call ends
			 * 
			 */
			try 
			{
				//check if the client is logged-in
				while(true)
				{
				
				//indicate the remote user has not given response to the call request
				//calleeAvailable=1;
				//missedCall=true;
				
				Log.d("mssg","listening incoming connection for file transfer");
				
				//accept connection from another client
				receivePTcpF = TcpSocketF.accept();
				Log.d("mssg","file transfer request accepted..");
				//set the sampling rate for the audio-calling to that set by user
				//audioCall.setFrequency(frequency);
				//get IP add of the caller
				temp = receivePTcpF.getInetAddress().getHostAddress();
				Log.i("ip of target host",temp);
				//create an input stream on the server socket
				receiveInF = new ObjectInputStream(receivePTcpF.getInputStream());
				
				//the user in not calling any other user, OR if user is calling other user then the same user is trying to connect
				if(true)/* || (busy && ip_user.equalsIgnoreCase(temp)) */
				{
		        	ip_user=temp;
		        	do
					{
		        		//get the message received from the input-stream
						mssg = receiveInF.readObject().toString();
						Log.d("mssg received in do while loop file : ",mssg);
						
						messageArray = mssg.split(" ");
						
						//indicate that callee is available and has response to its call
						//calleeAvailable=5;
						
						publishProgress(mssg);
					}
		        	while(!mssg.equalsIgnoreCase("reject call") && !mssg.equalsIgnoreCase("end call") && mssg.equalsIgnoreCase("end call"));
		        	
		        	if(mssg.equalsIgnoreCase("end call") || mssg.equalsIgnoreCase("reject call"))
					{
		        		SendEndCall send = new SendEndCall();
		        		send.execute();
		        		/*
		        		Call.StatusText.setText("call has been ended by remote user");
						//send message to indicate to other user that it has also aborted the call this side
		        		Call.stop.setEnabled(false);*/
						//sendOutF.writeObject("end call2");
						//sendOutF.flush();
						//Log.d("test","received and now sending end call..");
					}
		        	else if(mssg.equalsIgnoreCase("reject call"))/*to set status text of call initiator*/
	        		{
		        		publishProgress(mssg);
	        		}
		        	//the other user has aborted the call at its side
		        	//if(mssg.equalsIgnoreCase("reject call"))
					//{
						//sendOutV.writeObject("end call");
					//}
		        	Log.i("HOME","Video Call ended");
		        	//close the input-output streams and sockets
					closeConnectionFile();
				}
			}
		}
			
	    catch ( EOFException eof ) 
	    {
	    	closeConnectionFile();
	    	Log.d("in while true","EOF exception");
	    	
	    }
	    catch ( IOException ioe ) 
	    {
	    	closeConnectionFile();
	    	Log.d("in while true","IO exception");
	    } 
		catch (ClassNotFoundException e) 
	    {
			closeConnectionFile();
			Log.d("in while true","CNF exception");
		}
	}
		
		
		
		public void closeConnectionFile()
		{
			try 
	    	{
				Log.d("close connection","in close connection");
				//publishProgress("close connection");
				//indicate that the user is free to start or receive a call
	    		busy=false;
	    		if(receiveInF !=null)receiveInF.close();
				if(receivePTcpF !=null) receivePTcpF.close();
				//when the remote user was available and gave response to its incoming call-->send-out socket was created
				receiveInF=null;
				receivePTcpF=null;
				
				if(sendOutF !=null) sendOutF.close();
				if(sendPTcpF !=null) sendPTcpF.close();
				sendOutF = null;
				sendPTcpF = null;
			}
			catch (IOException f) 
	    	{
				Log.d("in close connection","err closing in IO");
			}
			
		}
		
		
		public void publishProgress(String value)
		{
			arr = value.split(" ");
			Log.i("in publish progress with: ",value);
			//user received a call request
			runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					// TODO Auto-generated method stub
					
					if(arr[0].equalsIgnoreCase("call"))
					{
						Log.d("mssg","in publish progress file");
							caller = arr[1];
							
							if(showNotification)
							{
								//cancel the notification for missed call from same user
								notificationManager.cancel(arr[1],2);
								//indicate the notification is for incoming call
								notificationTitle = "Incoming Call";
								//set the notification text as name of the caller
								notificationText = caller;
								
					        	//create a new notification
								myNotification.setLatestEventInfo(context,notificationTitle,notificationText, pendingIntent);
								notificationManager.notify(arr[1],1, myNotification);
							}
							//the activity is running -->show the incoming call as alert dialog box
							else
							{
								//start the ringing tone to indicate the incoming call
								AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
						        float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
						        stopSid=soundPool.play(playSid, volume, volume, 1,0,1f);
							}
						dialog.setMessage(caller + " wants to transfer a file...\n     ");
						alert = dialog.create();
						alert.show();
					
		        	}
					else if(arr[0].equalsIgnoreCase("success"))
					{
						FileShare.bOpen.setEnabled(true);
						FileShare.bSend.setEnabled(false);
						FileShare.tvFileStatus.setText("Connected");
						/*if(fileFlag)
						{
							FileShare.bOpen.setEnabled(true);
							FileShare.bSend.setEnabled(true);
							FileShare.tvFileStatus.setText("Connected");
						}
						else if(audioFlag)
						{
							Call.start.setEnabled(true);
							Call.stop.setEnabled(true);
							Call.StatusText.setText("Connected");
						}*/
					}
					//remote user has rejected the call request
		        	else if(arr[0].equalsIgnoreCase("reject"))
		        	{
		        		/*if(fileFlag)
						{
		        			FileShare.tvFileStatus.setText("call has been rejected..");
						}
						else if(audioFlag)
						{
							Call.StatusText.setText("call has been rejected..");
						}*/
		        		FileShare.bOpen.setEnabled(false);
						FileShare.bSend.setEnabled(false);
		        		FileShare.tvFileStatus.setText("call has been rejected..");
		        	}/*
		        	else if(arr[0].equalsIgnoreCase("end call"))
		        	{
		        		   try
		        		   {
		        			   	sendOutV.writeObject("end call");
		        		   }
		        		   catch(Exception e)
		        		   {
		        			    Log.i("mssg","err in sending end call in  home");
		        		   }
		        	}*/
					//there is error in app
		        	else if(arr[0].equalsIgnoreCase("Fatal"))
		        	{
		        		        		
		        		Toast.makeText(context, "FAtAl", Toast.LENGTH_LONG).show();
		        		//timer(5000);
		        	}
					
				}
			});
	    		
		}

	}

	private class SendEndCall extends AsyncTask<Void, String, Void>
	{
		protected Void doInBackground(Void... arg0)
		{
			try
			{
				if(sendOutF!=null)
				{
					sendOutF.writeObject("end call2");
					sendOutF.flush();
				}
				else
				{
					Log.d("in SendEndCall","sendOutF is null");
				}
			}
			catch ( EOFException eof ) 
		    {
		    	Log.d("in SendEndCall","EOF exception");
		    	
		    }
		    catch ( IOException ioe ) 
		    {
		    	Log.d("in SendEndCall","IO exception");
		    }
			return null;
		}
	}

	/*
	 * Accept call thread
	 * */

	private class AcceptCallThreadF extends AsyncTask<Void, String, Void>
	{

		@Override
		protected Void doInBackground(Void... arg0) 
		{
			try 
			{
				//create and connect a TCP socket to reply to caller 
				sendPTcpF = new Socket(InetAddress.getByName(ip_user),tcpPortF);
				
				//create an Output stream for above socket
				sendOutF = new ObjectOutputStream(sendPTcpF.getOutputStream());
				sendOutF.flush();
				
				//send message to caller to indicate that it has accepted the call
				sendOutF.writeObject("success");
				Log.i("in accept call thread","Success sent");
				
				
				sendOutF.flush();
				sendOutF.writeObject("end call");
				//flag = true;
				//sendOutF.flush();
				Log.i("in accept call thread","end call sent");
				
				
			}
			catch (IOException e) 
			{
				publishProgress("error");
			}
			
			
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) 
		{
			super.onProgressUpdate(values);
			if(values[0].equalsIgnoreCase("error"))
			{
				progressBar.dismiss();
				progressBar = null;
				Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_LONG).show();
			}
			
			else if(values[0].equalsIgnoreCase("call"))
			{
				progressBar.dismiss();
				progressBar = null;
				//disable the call button until the call ends
				//call.setEnabled(false);
				//enable end-call button
				//endCall.setEnabled(true);
			}
		}
		
	}
	/*
	 * reject call thread
	 * */
	private class RejectCallThreadF extends AsyncTask<Void, String, Void>
	{

		@Override
		protected Void doInBackground(Void... arg0) 
		{
			/*
			 * 
			 user rejects the call by pressing "REJECT" button
			 the ringing tone-->stopped
			 it sends back a specific message to indicate to caller that it has rejected the call
			 
			 * 
			 */
			try 
			{
				// missedCall=false;
				//create and connect a TCP socket to reply to caller 
				sendPTcpF = new Socket(InetAddress.getByName(ip_user),tcpPortF);
				//create an Output stream for above socket
				sendOutF = new ObjectOutputStream(sendPTcpF.getOutputStream());
				sendOutF.flush();
				
				//send message to indicate to caller that it has rejected the call
				sendOutF.writeObject("reject call");
				sendOutF.flush();
				//stop the ringing tone
				soundPool.stop(stopSid);
			}
			catch (IOException e) 
			{
				//publishProgress("error");
				Log.d("in reject all thread","err ");
			}
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) 
		{
			
			super.onProgressUpdate(values);
			
			if(values[0].equalsIgnoreCase("OK"))
			{
				progressBar.dismiss();
				progressBar = null;
				
			}
			else if(values[0].equalsIgnoreCase("error"))
			{
				
				progressBar.dismiss();
				progressBar = null;
				
				Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_LONG).show();
			}
		}
		
	}

	
/*----------------------------------------TcpActivity ends------------------------------------------------------*/	
/*>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.....AUDIO CONF.....<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/
	
	public static void initialize_udp_sockets(){
		//variables for UDP sending audio
  		try
  		{
  			//if(!socketS.isConnected())
  				socketS=new DatagramSocket(); //Create a sending Socket at any free port
  			Log.i("GCC", "SocketS created==============================");
  		}
  		catch (SocketException e)
  		{
  			e.printStackTrace();
  			Log.i("msg", "socketS exception");
  		} 
  		
  		try
  		{   
  			sockR=new DatagramSocket(7777); // Receiving DatagramSocket at any free port
  			Log.i("GCC", "SocketR created==============================");
  		}
  		catch (SocketException e)
  		{
  			e.printStackTrace();
  			Log.i("msg", "sockR exception");
  		}

	}
	
	
	public static void ini()
	{
		Log.i("msg", "variables initialized");
		bufferSize = AudioTrack.getMinBufferSize(22050, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		Log.i("--------Buffersize------", ""+bufferSize);
		if(bufferSize == AudioRecord.ERROR_BAD_VALUE)
		{
			Log.i("Bad ","Bad value");
			//this.finish();
		}
		else
		{
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,22050,
					AudioFormat.CHANNEL_IN_MONO,  MediaRecorder.AudioEncoder.AMR_NB ,4*bufferSize);
			audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,22050,  AudioFormat.CHANNEL_OUT_MONO,
					MediaRecorder.AudioEncoder.AMR_NB ,4*bufferSize,AudioTrack.MODE_STREAM);
			audioTrack.setPlaybackRate(22050);
		} 
	}
	
	
	
	/*.................................................. RecordSend...............................................*/
	 public static class RecordSend implements Runnable
	    {

	    	DatagramPacket packet;
	    	String Ip;
	    	
	    	@Override
	    	public void run()
	    	{
	    		//Log.i("Groupchatcall", "inside recoerd send ");
	    		byte[] buf = new byte[1024];
	    		audioRecord.startRecording();
	    		while(record)
	    		{
	    			//Log.i("Groupchatcall", "inside while of recording ");
	    			int brr = audioRecord.read(buf,0, 1024);
	    			try	
	    			{
	    				//Log.i("Groupchatcall", "inside try of recording ");
	    				for (Map.Entry<String, String> entry : Home.IP_Accept_Flag.entrySet()) 
	    				{	//Log.i("Groupchatcall", "inside for of recording ");
	    					//Log.i("Groupchatcall", " " + entry.getKey() + " " + entry.getValue());
	    					if(!entry.getKey().equals(Login.ip_self) && entry.getValue().equals("true"))
	    						{						
	   	    						
	    							packet =new DatagramPacket(buf,brr,InetAddress.getByName(entry.getKey()),7777);
	    							//Log.i("Groupchatcall", "after datagram packet of recording ");
	    							socketS.send(packet);
	    						
	    							//Log.i("Sending packet size", " " + packet.getLength());
	    						}
	    				}
	    			
	    			}
	    			catch (UnknownHostException e)	
	    			{
	    				Log.d("socket send","------exception------");
	    				e.printStackTrace();
	    			}
	    			catch (IOException e)
	    			{
	    				e.printStackTrace();
	    			}
	    		}
	    		
	    	}
	    }
	 
/*---------------------------------	 Playaudio class for audio conferencing ---------------------------------------*/
	    
	    public static class PlayAudio implements Runnable
		{

			DatagramPacket recP;
			
			@Override
			public void run() 
			{
				//Log.i("playing", "Audio");	
				
				audioTrack.play();
				byte[] buf = new byte[8192];
				recP=new DatagramPacket(buf,buf.length);
				while(record)
				{
					//Log.i("-------playing--------", "--------into loop--------");
					
					try
					{
						//Log.i("receiving","--------Audio----");
						recP.setLength(8192);
						//paste in this class initialization of this
						sockR.receive(recP);
					}
					catch (IOException e)
					{
						e.printStackTrace();
						break;
					}
					
					//Log.i("Receive Packet size", " " + recP.getLength());
					//audioTrack.flush();
					audioTrack.write(recP.getData(), 0, recP.getLength());
					audioTrack.flush();
				}
				
				
			}
		}
	
	
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		home_flag_status=false;
		new Thread(new SendLogoutMsg(getApplicationContext())).start();
			Log.i("Home","ondestroy-----------------");
			if(Home.TcpSocket != null)
				try {Log.i("1","1");
					Home.TcpSocket.close();
					Log.i("TCPS","Close-----------------");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if(Home.TcpSocketV != null)
				try {Log.i("2","2");
					Home.TcpSocketV.close();
					Log.i("TCPSV","Close-----------------");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if(Home.TcpSocketF != null)
				try {Log.i("3","3");
					Home.TcpSocketF.close();
					Log.i("TCPF","Close-----------------");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if(ReceiveFile.socket != null){
				try {Log.i("4","4");
					ReceiveFile.socket.close();
					Log.i("RFILE","Close-----------------");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//ReceiveFile.socket.notifyAll();
				Log.i("RF file ","cosed hopefully");
				ReceiveFile.socket=null;
			}
			if(ReceiveFile_Group_Thread.server_socket!=null){
				try {Log.i("5","5");
					ReceiveFile_Group_Thread.server_socket.close();
					Log.i("RFGT","Close-----------------");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ReceiveFile_Group_Thread.server_socket=null;
			}
			if(group_message_receive_thread.GroupchatServerSocket==null){
				try {Log.i("6","6");
					group_message_receive_thread.GroupchatServerSocket.close();
					Log.i("RMGT","Close-----------------");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//group_message_receive_thread.Group_chat_socket.notifyAll();
				Log.i("GMR thread ","cosed hopefully");
				group_message_receive_thread.GroupchatServerSocket=null;
			}
		
				
		if(audioTrack !=null){
			audioTrack.stop();
			Log.d("closing audiotrack","destroyed...");
			audioTrack.release();
		}
		if(audioRecord !=null){
			audioRecord.stop();
			Log.d("destroyed..","audioRecord closed...");
			audioRecord.release();
		}
		if(sockR!=null){
			Log.d("socket received","socket unbounded destroyed...");
			sockR.close();
		}
		if(socketS!=null){
			Log.d("socket send1","socket unbounded destroyed...");
			socketS.close();
		}
		
		
		System.exit(0);
		//android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	
	
	public void onPause()
	{
		
		super.onPause();
		//show notifications when activity is in background
		showNotification = true;
		home_flag_status=false;
		Log.i("Home","Pause");
	}
	
	public void onResume()
	{
		super.onResume();
		notificationManager.cancelAll();
		home_flag_status=true;
		showNotification = false;
		this.record=false;
		Log.i("Home","Resume");
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.i("Home","Backpress");
		/*home_flag_status=false;
		try
		{
			Log.i("home","on back pressed");
			if(Home.TcpSocket != null)
				Home.TcpSocket.close();
			if(Home.TcpSocketV != null)
				Home.TcpSocketV.close();
			if(Home.TcpSocketF != null)
				Home.TcpSocketF.close();
			if(ReceiveFile.socket != null){
				ReceiveFile.socket.close();
				ReceiveFile.socket.notifyAll();
				ReceiveFile.socket=null;
			}
			if(ReceiveFile_Group_Thread.server_socket!=null){
				ReceiveFile_Group_Thread.server_socket.close();
				ReceiveFile_Group_Thread.server_socket=null;
			}
			if(group_message_receive_thread.Group_chat_socket==null){
				group_message_receive_thread.Group_chat_socket.close();
				group_message_receive_thread.Group_chat_socket=null;
			}
		}
		catch(Exception e)
		{
			Log.d("err in closing server sockets","onDestroy of Home");
		}
		//closing sockets and audio elements on destroy
		
		if(audioTrack !=null){
			audioTrack.stop();
			Log.d("closing audiotrack","destroyed...");
			audioTrack.release();
		}
		if(audioRecord !=null){
			audioRecord.stop();
			Log.d("destroyed..","audioRecord closed...");
			audioRecord.release();
		}
		if(sockR!=null){
			Log.d("socket received","socket unbounded destroyed...");
			sockR.close();
		}
		if(socketS!=null){
			Log.d("socket send1","socket unbounded destroyed...");
			socketS.close();
		}
		new Thread(new SendLogoutMsg(getApplicationContext())).start();
	*/	
		
		finish();
		
		
	
	}
	
}