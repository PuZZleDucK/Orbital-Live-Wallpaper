
 
package puzzleduck.com;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class OrbitalLiveWallpaper extends WallpaperService {
    @Override
	public void onConfigurationChanged(Configuration newConfig) {

    	super.onConfigurationChanged(newConfig);
		this.onCreate();
	}

	public static final String SHARED_PREFS_NAME="orbital_lwp_settings";
    private static final String TAG = "OrbitalLiveWallpaper";

	public static int ORBIT_6_KNOT = 0;
	public static int ORBIT_4_KNOT = 1;
	public static int ORBIT_4_SIMPLE = 2;
	public static int ORBIT_3_SIMPLE = 3;
	public static int ORBIT_5_SIMPLE = 4;
	public static int ORBIT_8 = 5;
	public static String[] orbitNames = {"3 knot","4 knot","4 simple","3 simple","5 simple","Windows8"};
	public static int orbitType = orbitNames.length - 1;  
    

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new TargetEngine();
    }
    
    class TargetEngine extends Engine 
        implements SharedPreferences.OnSharedPreferenceChangeListener {
       
		private final Handler mHandler = new Handler();

		
        private final Paint mPaint = new Paint();
        private float mTouchX = -1;
        private float mTouchY = -1;

        private float mCenterX1;
        private float mCenterY1;

        private float mLeftTargetX;
        private float mLeftTargetY;

        private float mTopTargetX;
        private float mTopTargetY;
        
        private float mLastTouchX = 239;//indent initial display
        private float mLastTouchY = 239;
        
        private final Runnable mDrawCube = new Runnable() {
            public void run() {
                drawFrame();
            }
        };
        private boolean mVisible;
        private SharedPreferences mPrefs;
		


        public SharedPreferences.OnSharedPreferenceChangeListener listener;
        
        TargetEngine() {
            // Create a Paint to draw the lines for our 3D shape
            final Paint paint = mPaint;
            paint.setColor(0xffffffff);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(2);//increased stroke... better thin
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStyle(Paint.Style.STROKE); 

//    		Log.d(TAG, "set prefs listener" );
            mPrefs = OrbitalLiveWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
            listener = (SharedPreferences.OnSharedPreferenceChangeListener)this;
            mPrefs.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(mPrefs, null);
        }

        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        	//
//    		Log.d(TAG, " prefs change" );
            //flare settings:
            //flareOn = prefs.getBoolean("target_flare_on", true);

            Resources myResources;
            myResources = getBaseContext().getResources();
           // mCursorImage = BitmapFactory.decodeResource(myResources, getResources().getIdentifier( getPackageName() + ":drawable/"+cursor, null, null));

            //restart engine here
            OrbitalLiveWallpaper.this.onDestroy();
            OrbitalLiveWallpaper.this.onCreate();
            OrbitalLiveWallpaper.TargetEngine.this.onDestroy();
            OrbitalLiveWallpaper.TargetEngine.this.onCreate(getSurfaceHolder());
//            TargetLiveWallpaper.TargetEngine.this.onSurfaceChanged(getSurfaceHolder(), 0, 0, 0);
            drawFrame();
            
            onDestroy();
            onCreate(getSurfaceHolder());
        }


        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);
           
            //maybe just if null??? .. using mPrefs now... hopefully this will be resolved now
            SharedPreferences prefs = mPrefs;            
        	//3d targets
            //flare settings:
           // flareOn = prefs.getBoolean("target_flare_on", true);
 
            
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacks(mDrawCube);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (visible) {
                drawFrame();
            } else {
                mHandler.removeCallbacks(mDrawCube);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mCenterX1 = width/2; 
            mCenterY1 = height/2;
            mLeftTargetX = width/2;
            mLeftTargetY = height/2;
            mTopTargetX = width/2;
            mTopTargetY = height/2;
            drawFrame();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            mHandler.removeCallbacks(mDrawCube);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xStep, float yStep, int xPixels, int yPixels) {
//            mOffset = xOffset;
            drawFrame();
        }

        /*
         * Store the position of the touch event so we can use it for drawing later
         */
        @Override
        public void onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                mTouchX = event.getX();
                mTouchY = event.getY();
            } else {
                mTouchX = -1;
                mTouchY = -1;
            }
			
			orbitType = (orbitType +1) % orbitNames.length;
			
            super.onTouchEvent(event);
        }

        void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();

            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                updateTouchPoint(c);
                    	drawOrbital(c);
						
                  
                }
            } finally { 
                if (c != null) holder.unlockCanvasAndPost(c);
            }

            mHandler.removeCallbacks(mDrawCube);
            if (mVisible) {
                mHandler.postDelayed(mDrawCube, 1000 / 25);
            }
        }
        
        
        void updateTouchPoint(Canvas c) {
        	   if (mTouchX >=0 && mTouchY >= 0) {                

	        	// get relative dirs
	                float diffX = mTouchX - mLastTouchX;
	                float diffY = mTouchY - mLastTouchY;
	                mCenterY1 = mCenterY1 + diffY;
	                mCenterX1 = mCenterX1 + diffX;
	                
	                //store for next
	                mLastTouchX = mTouchX;
	                mLastTouchY = mTouchY;            
        	   }
        	   //pre draw canvas clearing.... do not remove (again).
        	   c.drawColor(0xff000000);   
        }
        

		float orbitalCompression = 0.2f;

        void drawOrbital(Canvas c) {
        	float rotationSpeed = 0.001f;
            float now = SystemClock.elapsedRealtime()*rotationSpeed;

//            int orbitalCount = 3;
//            int orbitalSeperation = 45;

        	if(orbitType == ORBIT_6_KNOT) 
        	{
        		int orbitalCount = 9;
                float orbitalSeperation = 90f;
                
	            for(int i = 0; i < orbitalCount; i++)
	            {
	            	if(i%3 == 0)
	            	{
	                    mPaint.setARGB(255, 255, 0, 0);            		
	            	}
	            	if(i%3 == 1)
	            	{
	                    mPaint.setARGB(255, 0, 255, 0);            		
	            	}
	            	if(i%3 == 2)
	            	{
	                    mPaint.setARGB(255, 0, 0, 255);            		
	            	}
	            	
	                c.drawCircle( mLastTouchX + (float) ( (2+Math.cos( 3*now ) * Math.cos(2*now)) *100)-200, 
	           		     mLastTouchY + (float) ( (2+Math.cos( 3*now ) * Math.sin(2*now)) *100)-200, 
	           		     1+i*2, mPaint);//SystemClock.elapsedRealtime()
	                now -= orbitalSeperation ;
	            }
        	}
        	if(orbitType == ORBIT_4_KNOT)
        	{
        		int orbitalCount = 8;
                float orbitalSeperation = 67.5f;
                
	            for(int i = 0; i < orbitalCount; i++)
	            {
	            	if(i%4 == 0)
	            	{
	                    mPaint.setARGB(255, 255, 0, 0);            		
	            	}
	            	if(i%4 == 1)
	            	{
	                    mPaint.setARGB(255, 0, 255, 0);            		
	            	}
	            	if(i%4 == 2)
	            	{
	                    mPaint.setARGB(255, 0, 0, 255);           		
	            	}
	            	if(i%4 == 3)
	            	{
	                    mPaint.setARGB(255, 255, 255, 255);          		
	            	}
	            	
	                c.drawCircle( mLastTouchX + (float) ( (2+Math.cos( 2*now ) * Math.cos(now)) *100)-200, 
	           		     mLastTouchY + (float) ( (2+Math.cos( 2*now ) * Math.sin(now)) *100)-200, 
	           		     1+i, mPaint);//SystemClock.elapsedRealtime()
	                now -= orbitalSeperation ;
	            }
        	}

			if(orbitType == ORBIT_4_SIMPLE)
        	{
        		int orbitalCount = 8;
                float orbitalSeperation = 67.5f;

	            for(int i = 0; i < orbitalCount; i++)
	            {
	            	if(i%4 == 0)
	            	{
	                    mPaint.setARGB(255, 255, 0, 0);            		
	            	}
	            	if(i%4 == 1)
	            	{
	                    mPaint.setARGB(255, 0, 255, 0);            		
	            	}
	            	if(i%4 == 2)
	            	{
	                    mPaint.setARGB(255, 0, 0, 255);           		
	            	}
	            	if(i%4 == 3)
	            	{
	                    mPaint.setARGB(255, 255, 255, 255);          		
	            	}

	                c.drawCircle( mLastTouchX + (float) ( (Math.sin( now ) ) *100), 
								 mLastTouchY + (float) ( (Math.cos( now ) ) *100), 
								 1+i, mPaint);//SystemClock.elapsedRealtime()
	                now -= orbitalSeperation ;
	            }
				
				
				
				
				
				
        	}
			
			

			if(orbitType == ORBIT_3_SIMPLE)
        	{
        		//int orbitalCount = 8;
                //float orbitalSeperation = 67.5f;


        		int orbitalCount = 9;
                float orbitalSeperation = 90f;
				
				
	            for(int i = 0; i < orbitalCount; i++)
	            {

	            	if(i%3 == 0)
	            	{
	                    mPaint.setARGB(255, 255, 0, 0);            		
	            	}
	            	if(i%3 == 1)
	            	{
	                    mPaint.setARGB(255, 0, 255, 0);            		
	            	}
	            	if(i%3 == 2)
	            	{
	                    mPaint.setARGB(255, 0, 0, 255);            		
	            	}
	                c.drawCircle( mLastTouchX + (float) ( (Math.sin( now ) ) *100), 
								 mLastTouchY + (float) ( (Math.cos( now ) ) *100), 
								 1+i, mPaint);//SystemClock.elapsedRealtime()
	                now -= orbitalSeperation ;
	            }
			
			}//simple3
			


			if(orbitType == ORBIT_5_SIMPLE)
        	{
        		//int orbitalCount = 8;
                //float orbitalSeperation = 67.5f;


        		int orbitalCount = 5;
                float orbitalSeperation = 5f;


	            for(int i = 0; i < orbitalCount; i++)
	            {

	            	if(i%3 == 0)
	            	{
	                    mPaint.setARGB(255, 255, 0, 0);            		
	            	}
	            	if(i%3 == 1)
	            	{
	                    mPaint.setARGB(255, 0, 255, 0);            		
	            	}
	            	if(i%3 == 2)
	            	{
	                    mPaint.setARGB(255, 0, 0, 255);            		
	            	}
	                c.drawCircle( mLastTouchX + (float) ( (Math.sin( now + (10*i)) ) *100), 
								 mLastTouchY + (float) ( (Math.cos( now + (10*i)) ) *100), 
								 5, mPaint);//SystemClock.elapsedRealtime()

					//orbitalSeperation = (0.5f * (float) Math.sin((SystemClock.elapsedRealtime()*rotationSpeed)%360));
	                now -= orbitalSeperation;
	            }

			}//windows late

			if(orbitType == ORBIT_8)
        	{
        		//int orbitalCount = 8;
                //float orbitalSeperation = 67.5f;


        		int orbitalCount = 5;


				orbitalCompression += Math.sin(SystemClock.elapsedRealtime()/500)/100;
				
	            for(int i = 0; i < orbitalCount; i++)
	            {
	            	//mPaint.setStrokeWidth(10);
	            	
	            	if(i%3 == 0)
	            	{
	                    mPaint.setARGB(255, 255, 0, 0);            		
	            	}
	            	if(i%3 == 1)
	            	{
	                    mPaint.setARGB(255, 0, 255, 0);            		
	            	}
	            	if(i%3 == 2)
	            	{
	                    mPaint.setARGB(255, 0, 0, 255);            		
	            	}
	            	
	                c.drawCircle( mLastTouchX + (float) ( (Math.sin( -1.5 + now + ((orbitalCompression+0.1)*i)) ) *100), 
								 mLastTouchY + (float) ( (Math.cos( -1.5 +  now + ((orbitalCompression+0.1)*i)) ) *100), 
								 5, mPaint);//SystemClock.elapsedRealtime()
					orbitalCompression = 0.2f + (float)Math.sin( now )*0.2f;
					//orbitalSeperation = (0.5f * (float) Math.sin((SystemClock.elapsedRealtime()*rotationSpeed)%360));
	            }

			}//windows late
			
    }
        
}//class
		


    
}
