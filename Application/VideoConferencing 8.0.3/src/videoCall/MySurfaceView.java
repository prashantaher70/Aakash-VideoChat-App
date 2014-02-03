package videoCall;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView implements Callback{


    private SurfaceHolder mHolder;
    private Bitmap image;
    protected final Paint rectanglePaint = new Paint();
    
    public MySurfaceView(Context context) {
    	super(context);

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }
    

    @SuppressLint("DrawAllocation")
	@Override
    protected void onDraw(Canvas canvas) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
        synchronized (this) {
            this.setWillNotDraw(false); // This allows us to make our own draw
                                    // calls to this canvas
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
       
    }

    public void drawOnSurface(byte[] data,int length) {
        Log.d("Camera", "Got a camera frame");

        Canvas c = null;
        
        if(mHolder == null){
            return;
        }

        try {
            synchronized (mHolder) {
                c = mHolder.lockCanvas(null);
                image = BitmapFactory.decodeByteArray(data, 0, length);
                /*width = image.getWidth();
                height = image.getHeight();
                scaleWidth = ((float) 350) / width;
                scaleHeight = ((float) 400) / height;
                matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                resizedBitmap = Bitmap.createBitmap(image, 0, 0, width, height,
                        matrix, false);
                if(image !=null)c.drawBitmap(resizedBitmap, 0, 0,null);*/
                if(image !=null)c.drawBitmap(image, new Rect(0, 0, image.getWidth(), image.getHeight()), new Rect(0, 0,500,350),new Paint());
        
            }
        }catch(Exception e)
        {
        	
        }finally {
            // do this in a finally so that if an exception is thrown
            // during the above, we don't leave the Surface in an
            // inconsistent state
            if (c != null) {
                mHolder.unlockCanvasAndPost(c);
            }
        }
    }

}
