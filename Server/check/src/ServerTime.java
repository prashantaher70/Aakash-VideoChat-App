import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import javax.swing.JTextArea;

/*
 * responds to time requests from the clients and
 * sends the current time of the server to the clients
 */
public class ServerTime implements Runnable
{
	
	//UDP socket to receive & reply to requests for current-time
	public DatagramSocket socketT;             
	//packet to be sent to client
	private DatagramPacket sendTS;              
	//packet received from client
	private DatagramPacket recvTS;              
	//buffer to store data from  received packet
	private byte dataTS1[]=new byte[100];       
	//buffer to store data in reply packet
	private byte dataTS2[]=new byte[200];
	//thread to process time-stamp requests
	private Thread tTimeStamp;
	//server log
	private JTextArea log;
	
	public ServerTime(JTextArea log)
	{
		this.log = log;
		
		try
		{
			//create a UDP socket and bind it to port 5500
			socketT=new DatagramSocket(5500);
		}
		catch(SocketException socketException)
		{
			log.append("ERROR : " + socketException.getMessage() + "\nRestart the server or try to free the port : 5500\n");
		}
		
		//create and execute a new thread
		tTimeStamp=new Thread(this);
		tTimeStamp.start();
	}
	
    @Override
	public void run()
	{
		
		//store server time in milliseconds
		long serverTime;            
		//temporary string variable
		String temp;                
		
		//form the receive data gram
		recvTS=new DatagramPacket(dataTS1,dataTS1.length);
		
		//continuously wait for new requests and reply
		while(true)
		{
			try
			{
				//always the set the length of the diagram to be the length of array
				recvTS.setLength(dataTS1.length);
				//receive the message from client
				socketT.receive(recvTS);
				
				//checks if the message received requests time  
				if(recvTS.getData().toString().equalsIgnoreCase("time"));
				{
					//get the current time of server
					serverTime=System.currentTimeMillis();
					//get the string
					temp=String.valueOf(serverTime);
					//get bytes 
					dataTS2=temp.getBytes();
					//create and send data packet containing server time 
					sendTS=new DatagramPacket(dataTS2,dataTS2.length,recvTS.getAddress(),recvTS.getPort());
					//send the packet
					socketT.send(sendTS);
                                        
				}
			}
			catch(IOException ioException)
			{
				log.append("ERROR : " + ioException.getMessage() + "\n");
			}
		}
	}	
}