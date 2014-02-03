package videoCall;

import java.net.DatagramSocket;

import android.util.Log;

public class PacketAll {
	
	private int frameNumber=1;
	private byte[] accumulatedAudio; 
	private int totalAccumulationSize=20000;
	private int accumulated=0,BUFFERSIZE=512,PACKETSIZE=40100;
	private byte[] packet;
	private String IP;
	private SendPacket sendingThread;
	public boolean interrupted=false;
	DatagramSocket SendingSocket;
	public PacketAll(DatagramSocket SendingSocket)
	{
		this.SendingSocket=SendingSocket;
		accumulatedAudio=new byte[totalAccumulationSize];
		packet=new byte[PACKETSIZE];
		//Log.i("PACKET ALL","CONSTRUCTOR");
	}
	public void setIP(String IP)
	{
		this.IP=IP;
	}
	public void accumulateAudio(byte[] recordeBuf)
	{
		if(accumulated <= (totalAccumulationSize-BUFFERSIZE))
		{
			System.arraycopy(recordeBuf, 0, accumulatedAudio, accumulated, BUFFERSIZE);
			accumulated =accumulated + BUFFERSIZE;
			//Log.i("PACKET ALL","ACCUMULATING AUDIO");
		}
		else{
			//Should not happen
			accumulated=0;
			Log.i("RECORD AUDIO","PACKET LOSS");
		}
	}
	
	public void getFrameAndPacketIt(byte[] frame)
	{
		
		packet[3] = (byte) (frame.length & 0xFF);
        packet[2] = (byte) ((frame.length >> 8) & 0xFF);  
        packet[1] = (byte) ((frame.length >> 16) & 0xFF);  
        packet[0] = (byte) ((frame.length >> 24) & 0xFF);
        System.arraycopy(frame, 0, packet, 4, frame.length);
		
        packet[20003] = (byte) (accumulated & 0xFF);
        packet[20002] = (byte) ((accumulated >> 8) & 0xFF);  
        packet[20001] = (byte) ((accumulated >> 16) & 0xFF);  
        packet[20000] = (byte) ((accumulated >> 24) & 0xFF);
        System.arraycopy(accumulatedAudio, 0, packet, 20004, accumulated);
        
        packet[40099] = (byte) (frameNumber & 0xFF);
        packet[40098] = (byte) ((frameNumber >> 8) & 0xFF);  
        packet[40097] = (byte) ((frameNumber >> 16) & 0xFF);  
        packet[40096] = (byte) ((frameNumber >> 24) & 0xFF);
        Log.i("PACKET ALL SENT SIZE","SENT SIZE"+((Integer)accumulated).toString());
        //Log.i("PACKET ALL","GOT FRAME");
        frameNumber++;
        accumulated=0;
        sendingThread.send(packet);
	}
	
	public void stopPackagingSending()
	{
		interrupted=true;
		sendingThread.stopSending();
		if(sendingThread.isAlive()) sendingThread.interrupt();
	}
	
	public void startPackagingSending()
	{
		interrupted=false;
		sendingThread=new SendPacket(this.IP,SendingSocket);
		sendingThread.startSending();
		sendingThread.start();
	}
	public void reset()
	{
		interrupted=false;
	}
}
