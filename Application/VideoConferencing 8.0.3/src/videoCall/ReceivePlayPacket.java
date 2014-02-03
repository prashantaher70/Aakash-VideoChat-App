package videoCall;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import videoCall.MySurfaceView;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

public class ReceivePlayPacket extends Thread{
	
	private AudioTrack track;
	private int FREQUENCY=22050;
	private static boolean called=false;
	private DatagramSocket ReceivingSocket;
	private MySurfaceView mySurfaceView;
	public ReceivePlayPacket(int frequency,MySurfaceView mySurfaceView,DatagramSocket ReceivingSocket) {
		
		this.ReceivingSocket=ReceivingSocket;
		FREQUENCY=frequency;
		this.mySurfaceView=mySurfaceView;
		int bufferSize=AudioTrack.getMinBufferSize(FREQUENCY, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if(bufferSize==AudioRecord.ERROR_BAD_VALUE)
        {
            Log.i("RECEIVE PLAY","BAD VALUE");
        }
		track= new AudioTrack(AudioManager.STREAM_VOICE_CALL,FREQUENCY,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT,4*bufferSize,AudioTrack.MODE_STREAM);
	       
        track.setPlaybackRate(FREQUENCY);
        called=true;
       
	}
	
	public void stopReceiving()
	{
		track.stop();
		called=false;
	}
	public void startReceiving()
	{
		track.play();
		called=true;
	}
	public void run()
	{
		byte[] packet = new byte[40100];
        byte[] video=new byte[20000];
        byte[] audio=new byte[20000];
        int frameLength=0,audioLength=0;
        int frameNo;
        DatagramPacket recP=new DatagramPacket(packet,packet.length);
        while(called)
        {
        	//int played=0;
            try
            {
                ReceivingSocket.receive(recP);
                packet=recP.getData();
           
                audioLength=(packet[20003] & 0xFF) + ((packet[20002] & 0xFF) << 8) + ((packet[20001] & 0xFF) << 16) + ((packet[20000] & 0xFF) << 24);
                System.arraycopy(packet, 20004, audio, 0, audioLength);
                /*while(played <= audioLength-5000)
                {
                	track.write(audio,played,played+5000);
                	track.flush();
                	Log.i("RECEIVE PLAY","Playing");
                	played=played + 5000;
                }*/
                Log.i("msg","audio length"+((Integer)audioLength));
                track.write(audio,0,audioLength);
                track.flush();
                frameLength=(packet[3] & 0xFF) + ((packet[2] & 0xFF) << 8) + ((packet[1] & 0xFF) << 16) + ((packet[0] & 0xFF) << 24);
                frameNo=(packet[40099] & 0xFF) + ((packet[40098] & 0xFF) << 8) + ((packet[40097] & 0xFF) << 16) + ((packet[40096] & 0xFF) << 24);
                System.arraycopy(packet, 4, video, 0, frameLength);
                Log.i("RECEIVE PLAY","received frame no"+((Integer)frameNo));
                mySurfaceView.drawOnSurface(video,frameLength);
                //Log.i("RECEIVE PLAY","PACKET RECEIVED AND PROCESSED");
            } catch (IOException e) {
                // TODO Auto-generated catch block
            	//Log.i("msg","receive catch");
                e.printStackTrace();
                break;
            }

        }
	}
}