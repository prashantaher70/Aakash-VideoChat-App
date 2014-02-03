package videoCall;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.util.Log;

public class SendPacket extends Thread{
    byte[] packet;
    int frameNumber;
    DatagramSocket SendingSocket=null;
    String IP;
    public boolean sending=true;
    private boolean changed=false;
    public SendPacket(String IP,DatagramSocket SendingSocket)
    {
        //packet=new byte[40100];
        this.IP=IP;
        this.SendingSocket=SendingSocket;
        //Log.i("SEND PACKET","CONSTRUCTOR");
    }
   
    public void send(byte[] packet)
    {
    	this.packet=packet;
        changed=true;
    }
   
    public void stopSending()
    {
        sending=false;
        changed=false;
    }
    public void startSending()
    {
        sending=true;
    }
    public void run()
    {
        while(sending)
        {   
            while(changed)
            {
                changed=false;
                DatagramPacket toSendPacket;
                try {
                    //frameNumber=(packet[40099] & 0xFF) + ((packet[40098] & 0xFF) << 8) + ((packet[40097] & 0xFF) << 16) + ((packet[40096] & 0xFF) << 24);
                    toSendPacket=new DatagramPacket(packet, packet.length, InetAddress.getByName(IP),9999);
                    SendingSocket.send(toSendPacket);
                    //Log.i("SEND PACKET","PACKET SENT no"+((Integer)frameNumber));
                    //Log.i("SEND PACKET","PACKET SENT");
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
               
            }
        }
    }

}