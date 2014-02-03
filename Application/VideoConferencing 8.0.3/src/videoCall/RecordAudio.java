package videoCall;

import videoCall.PacketAll;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

public class RecordAudio extends Thread{

	private AudioRecord audioRecorder=null;
	private int FREQUENCY=22050,BUFFERSIZE=512;
	private static boolean record=false;
	private int readSize=0;
	private byte[] audioRecordBuf;
	private PacketAll packet;
	public RecordAudio(int frequency,PacketAll packet)
	{
		this.packet=packet;
		FREQUENCY=frequency;
		int bufferSize=AudioTrack.getMinBufferSize(FREQUENCY, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		if(bufferSize==AudioRecord.ERROR_BAD_VALUE)
        {
            Log.i("RECORD AUDIO","BAD VALUE");
        }
		audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,FREQUENCY, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,4*bufferSize);
        audioRecordBuf=new byte[BUFFERSIZE];
	}
	
	public void stopAudioRecoding()
	{
		if(record ==true)
		{	
			audioRecorder.stop();
			record=false;
			packet.stopPackagingSending();
		}
	}
	
	public void startAudioRecording()
	{
		if(record ==false)
		{	
			packet.startPackagingSending();
			audioRecorder.startRecording();
			record=true;
		}
	}
	@Override
	public void run() {
		while(record)
		{
			//Log.i("RECORD AUDIO","RECORDING");
			readSize = audioRecorder.read(audioRecordBuf,0,BUFFERSIZE);
			if(packet.interrupted == false) packet.accumulateAudio(audioRecordBuf);
		}
	}
	
	public void releaseRecorder()
	{
		if(record==true) audioRecorder.stop();
		audioRecorder.release();
	}
}
