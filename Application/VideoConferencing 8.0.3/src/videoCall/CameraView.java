package videoCall;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import videoCall.PacketAll;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraView implements SurfaceHolder.Callback{
	
	private SurfaceHolder holder;
	private SurfaceView surface;
	private Camera camera=null;
	private int PrevWidth=0,PrevHeight=0;
	private PacketAll packet;
	PowerManager.WakeLock wl;
	@SuppressLint("Wakelock")
	@SuppressWarnings("deprecation")
	public CameraView(SurfaceView surfaceView,PacketAll packet,Context app)
	{
		this.packet=packet;
		this.surface=surfaceView;
		holder=surface.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);
        
        PowerManager pm = (PowerManager) app.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        wl.acquire();

	}
	
	 public Camera.Size getBestPreviewSize(Camera.Parameters parameters, int w, int h)
	    {
	        Camera.Size result = null;

	        for (Camera.Size size : parameters.getSupportedPreviewSizes())
	        {
	            if (size.width <= w && size.height <= h)
	            {
	            	
	                if (null == result)
	                {
	                	result = size;
	                }
	                else
	                {
	                    int resultDelta = w - result.width + h - result.height;
	                    int newDelta    = w - size.width   + h - size.height;

	                    if (newDelta < resultDelta)
	                        result = size;
	                }
	            }
	        }
	        return result;
	    }
	
	 public void startPreview()
	 {
		 camera.startPreview();
	 }
	 public void stopPreview()
	 {
		 camera.stopPreview();
	 }
	 
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w,
			int h) {
		// TODO Auto-generated method stub
		Camera.Parameters p = camera.getParameters();
        Camera.Size       size   = getBestPreviewSize(p,w,h);
        if (size != null)
        {
        	p.setPreviewSize(size.width, size.height);
        	PrevWidth=size.width;
	        PrevHeight=size.height;
        }
        else
        {
        	Log.i("CAMERA VIEW","ELSE SIZE");
        	p.setPreviewSize(350,400);
        	PrevWidth=350;
	        PrevHeight=400;
        }
		/*Camera.Size procSize;
		List<Camera.Size> supportedSizes;
		supportedSizes = p.getSupportedPreviewSizes();
        procSize = supportedSizes.get( supportedSizes.size()/2 );
        p.setPreviewSize(procSize.width, procSize.height);
        camera.setParameters(p);*/
	}

	@SuppressLint("NewApi")
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for ( int camIdx = 0; camIdx < cameraCount; camIdx++ )
        {
            Camera.getCameraInfo( camIdx, cameraInfo );
            if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    camera = Camera.open(camIdx);
                    break;
                } catch (RuntimeException e) {
                    Log.e("Cam Error", "Camera failed to open: " + e.getLocalizedMessage());
                  }
            }
        }
        final Camera.Size size =camera.getParameters().getPreviewSize();
        //Log.i("CAMERA WIDTH+ CAMERA HHEIGHT",((Integer)size.width).toString()+((Integer)size.height).toString());
        PreviewCallback Cb = new PreviewCallback() {

            public void onPreviewFrame(byte[] frame, Camera c) {
            	
            	ByteArrayOutputStream out = new ByteArrayOutputStream();
            	YuvImage yuvImage = new YuvImage(frame,ImageFormat.NV21,size.width,size.height, null);
                yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 40, out);
                byte[] imageBytes = out.toByteArray();
                if(imageBytes.length <= 20000 && packet.interrupted ==false) packet.getFrameAndPacketIt(imageBytes);
            }
        };
        camera.setPreviewCallback(Cb);
        
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		wl.release();

		if(camera !=null)
		{
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera=null;
		}
		
     }
	public boolean cameraReleased()
	{
		if(camera == null ) 
		{	
			return true;
		}
		else
		{
			return false;
		}
	}
}
