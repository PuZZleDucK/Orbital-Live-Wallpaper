
 
package puzzleduck.com;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


import android.app.Activity;
import android.content.Context;
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
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.graphics.*;

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
	
	public static int orbitRadius = 100;
	public static int orbitDiameter = orbitRadius * 2;
    

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
		

		private float now = 0;
		private float nowOffset = 0;
//		private float orbitalCompression = 0.0f;
		private int dotColor = Color.WHITE;
		private int dotColors[] = {Color.WHITE, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW  };
		
		private int currentScheme = 0;
		private int colorSchemes[][] = {
			{ Color.argb(255,255,255,255), Color.argb(255,255,255,255), Color.argb(255,255,255,255), Color.argb(255,255,255,255), Color.argb(255,255,255,255), Color.argb(255,255,255,255)},//white
			{ Color.argb(255,220,200,20), Color.argb(255,80,30,120), Color.argb(255,160,50,200), Color.argb(255,190,50,150), Color.argb(255,230,10,30), Color.argb(255,240,50,5)},//Apache
			{ Color.argb(255,255,255,255), Color.argb(255,45,128,124), Color.argb(255,45,128,124), Color.argb(255,45,128,124), Color.argb(255,45,128,124), Color.argb(255,45,128,124)},//slash.
			{ Color.argb(255,255,99,9), Color.argb(255,201,0,22), Color.argb(255,255,181,21), Color.argb(255,255,99,9), Color.argb(255,201,0,22), Color.argb(255,255,181,21)},//ubuntu classic
			{ Color.argb(255,101,16,89), Color.argb(255,255,99,9), Color.argb(255,201,0,22), Color.argb(255,101,16,89), Color.argb(255,255,99,9), Color.argb(255,201,0,22) }//Ubuntu purple
		};
		private boolean inTransition = false;
		private int orbitRadiusDiff = - 5;
		private float orbitSpeed = 0.05f;

		private float orbitalCompression = 0.125f;//2.5f * orbitSpeed;
		private int orbitalCount = 0;
		private float dotSizeIncrement = 1.0f;
		
		private int trailCount = 6;
		private int setCount = 3;
		//private int[] colortArray = new int{0,1};
		//colorArray[0] = Color.WHITE;
		//colorArray[1] =  Color.RED;

        public SharedPreferences.OnSharedPreferenceChangeListener listener;
        
        TargetEngine() {
            // Create a Paint to draw the lines for our 3D shape
           // final Paint paint = mPaint;
            mPaint.setColor(0xffffffff);
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(2);//increased stroke... better thin
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStyle(Paint.Style.STROKE); 
			mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

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
            
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();

           // Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();  
            int width = display.getWidth();
            int height = display.getHeight();

            
            mLastTouchX =  width/2;
            mLastTouchY = height/2;
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
			

			
			if(!inTransition)
			{
				inTransition = true;
				orbitRadiusDiff = -5;
			}
			
			
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
        


        void drawOrbital(Canvas c) {
        	float rotationSpeed = 0.00001f;
            now += rotationSpeed; 
			
			dotColor = 0;

			if(orbitRadius == 0)
			{

				orbitType = (orbitType +1) % orbitNames.length;
				orbitSpeed = orbitSpeed + 0.03f;
				if(orbitSpeed > 5f)
				{
					orbitSpeed = 0.01f;
				}
				//
				now = 0;
				orbitalCompression = 0.125f;
				orbitRadiusDiff = 5;
				
				currentScheme = ( currentScheme + 1 ) % colorSchemes.length;
				
			}// rad = 0;

			if(inTransition)
			{
				orbitRadius += orbitRadiusDiff;
				orbitDiameter = orbitRadius*2;
			}

			if(orbitRadius == 100 && inTransition)
			{
				inTransition = false;

			}
			
			
        	if(orbitType == ORBIT_6_KNOT) 
        	{
				setCount = 4;
        		orbitalCount = setCount*trailCount;
                
	            for(int i = 0; i < orbitalCount; i++)
	            {
					
				//	mPaint.setColor(dotColors[dotColor]);
					mPaint.setColor(colorSchemes[currentScheme][dotColor]);
					
					dotColor = (dotColor + 1)%(setCount);
	            	
					float offset = now-(i*45);//to split out of function
					int dotSize = i/setCount;
					
	                c.drawCircle( mLastTouchX + (float) ( (2+Math.cos( 3*offset ) * Math.cos(2*offset )) * orbitRadius )-orbitDiameter, 
								 mLastTouchY + (float) ( (2+Math.cos( 3*offset ) * Math.sin(2*offset )) *orbitRadius)-orbitDiameter, 
	           		     (1+dotSize)*dotSizeIncrement, mPaint);
	            }
        	}
        	if(orbitType == ORBIT_4_KNOT)
        	{
				setCount=4;
        		orbitalCount = setCount*trailCount;
	            for(int i = 0; i < orbitalCount; i++)
	            {

					//mPaint.setColor(dotColors[dotColor]);

					mPaint.setColor(colorSchemes[currentScheme][dotColor]);

					dotColor = (dotColor + 1)%setCount;
					int dotSize = i/setCount;
					
					float offset = now-(i*67.5f);
	            	
	                c.drawCircle( mLastTouchX + (float) ( (2+Math.cos( 2*offset ) * Math.cos(offset)) *orbitRadius )-orbitDiameter, 
								 mLastTouchY + (float) ( (2+Math.cos( 2*offset ) * Math.sin(offset)) *orbitRadius )-orbitDiameter, 
	           		     (1+dotSize)*dotSizeIncrement, mPaint);
	            }
        	}

			if(orbitType == ORBIT_4_SIMPLE)
        	{
				setCount = 4;
        		orbitalCount = setCount * trailCount;
	            for(int i = 0; i < orbitalCount; i++)
	            {
					//mPaint.setColor(dotColors[dotColor]);

					mPaint.setColor(colorSchemes[currentScheme][dotColor]);
					dotColor = (dotColor + 1)%setCount;
			
					int dotSize = i/setCount;
					float offset = now-(i*67.5f);
					
	                c.drawCircle( mLastTouchX + (float) ( (Math.sin( offset ) ) *orbitRadius), 
								 mLastTouchY + (float) ( (Math.cos( offset ) ) *orbitRadius), 
								 1+(dotSize*dotSizeIncrement), mPaint);
	            }
				
        	}
			
			

			if(orbitType == ORBIT_3_SIMPLE)
        	{
				setCount = 3;
        		orbitalCount = setCount * trailCount;
               
	            for(int i = 0; i < orbitalCount; i++)
	            {

					//mPaint.setColor(dotColors[dotColor]);

					mPaint.setColor(colorSchemes[currentScheme][dotColor]);
					dotColor = (dotColor + 1)%setCount;

					int dotSize = i/setCount;
					float offset = now-(i*90f);
					
	                c.drawCircle( mLastTouchX + (float) ( (Math.sin( offset ) ) *orbitRadius), 
								 mLastTouchY + (float) ( (Math.cos( offset ) ) *orbitRadius), 
								 1+(dotSize*dotSizeIncrement), mPaint);
	            }
			}//simple3
			


			if(orbitType == ORBIT_5_SIMPLE)
        	{
				setCount = 5;
        		orbitalCount = setCount*trailCount;
	            for(int i = 0; i < orbitalCount; i++)
	            {

					//mPaint.setColor(dotColors[dotColor]);

					mPaint.setColor(colorSchemes[currentScheme][dotColor]);
					dotColor = (dotColor + 1)%setCount;

					int dotSize = i/setCount;
					float offset = now-(i*5f);
					
	                c.drawCircle( mLastTouchX + (float) ( (Math.sin( offset ) ) *orbitRadius), 
								 mLastTouchY + (float) ( (Math.cos( offset ) ) *orbitRadius), 
								 (1+dotSize)*dotSizeIncrement, mPaint);
	            }
			}//5 simple

			if(orbitType == ORBIT_8)
        	{
				setCount = 5;
				//mPaint.setARGB(255, 255, 255, 255);  
        		orbitalCount = 5;

				if(Math.sin(now) < -0.1)
				{
					orbitalCompression +=  ((float) Math.sin( now ) * 0.2f)*orbitSpeed;//* (orbitSpeed/5)  //orbitSpeed
//					orbitalCompression +=  ((float) Math.sin( now )*(0.2f * orbitSpeed));
				}

				if(Math.sin(now) > 0.1)
				{
					orbitalCompression +=  ((float) Math.sin( now ) * 0.2f)*orbitSpeed;
				}
				
	            for(int i = 0; i < orbitalCount; i++)
	            {

					mPaint.setColor(colorSchemes[currentScheme][dotColor]);
					dotColor = (dotColor + 1)%setCount;
					
					float offset = now + (i * Math.abs(orbitalCompression)) ;  //+ (5) add rotation offset
					c.drawCircle( mLastTouchX + (float) ( (Math.sin( offset+179f) ) *orbitRadius), 
								 mLastTouchY + (float) ( (Math.cos( offset+179f) ) *orbitRadius), 
								 5, mPaint);//SystemClock.elapsedRealtime()
					
	            } 
			}//windows late

			now += orbitSpeed;
    }
        
}//class
		


    
}
