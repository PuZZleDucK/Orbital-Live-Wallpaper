/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//
//Sweet... modding by PuZZleDucK, I'll take some credit but most goes to 
//the fantastic ppls at the big G.
//
//V.1.0: tracking at top and left... more to come surely
//
// might try to change models now...
//

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

//This animated wallpaper draws many user selectable items... target, trackers, cursor, etc
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
	public static int ORBIT_SIMPLE = 2;
	public static String[] orbitNames = {"6 knot","4 knot","simple"};
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
        private static final int MAX_FLARE_COUNT = 1;

		private final Handler mHandler = new Handler();

		private int TD_SCALE = 1;//100
		private final static int TD_OFFSET_X = 0;
		private final static int TD_OFFSET_Y = 0;
		private final static int TD_OFFSET_Z = 0;
		
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
        
        private boolean leftOn = true;
        private boolean topOn = true;
        private boolean quadOn = true;
        private boolean pulse3dOn = true;
    	private String shape = "diamond";
        
        private boolean discOn = true;
        private int discStyle = 1;
        private boolean pulseOn = true;
        private int spacingOfRings = 15;
        private int numberOfRings = 16;
        private int mPulseN = 0;
        
        private boolean flareOn = true;
        
        private boolean mouseOn = false;
        String cursor = "debianswirl";//cursor_typenames
        private Bitmap mCursorImage;

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
        	//3d targets
//    		Log.d(TAG, " prefs change" );
        	shape = prefs.getString("target_shape", "diamond");
            quadOn = prefs.getBoolean("target_quad_on", true);
            leftOn = prefs.getBoolean("target_left_on", true);
            topOn = prefs.getBoolean("target_top_on", true);
            pulse3dOn =  prefs.getBoolean("target_dpulse_on", true);
            
            //rotating targets
            discOn = prefs.getBoolean("target_disc_on", true);
            discStyle = prefs.getInt("target_disc_type", 1);
//            discStyle = Integer.valueOf(prefs.getString("target_disc_type", "1"));
            discStyle = prefs.getInt("target_disc_type", 1);
            
            //static targets
            mouseOn = prefs.getBoolean("target_mouse_on", false);
            cursor = prefs.getString("cursor_type", "debianswirl");//cursor_typenames
            
            //pulse settings:
            pulseOn = prefs.getBoolean("target_pulse_on", true);
            spacingOfRings = Integer.valueOf(prefs.getString("target_pulse_width", "15"));
            numberOfRings = Integer.valueOf(prefs.getString("target_pulse_number", "16"));
            
            //flare settings:
            flareOn = prefs.getBoolean("target_flare_on", true);

           // read the 3D model from the resource
//            readModel(shape);
            //read model from file
//            readModelFile("/sdcard/model.dae");
//			Log.d(TAG, " about to read" );
          //  readWavefrontModelFile("/sdcard/whatever.obj");
            
            
            //from sdk... think i get it now
            Resources myResources;
            myResources = getBaseContext().getResources();
            mCursorImage = BitmapFactory.decodeResource(myResources, getResources().getIdentifier( getPackageName() + ":drawable/"+cursor, null, null));

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
           
            //init flare list
           // flareList = new ArrayList<FlareData>(1);
            
            //maybe just if null??? .. using mPrefs now... hopefully this will be resolved now
            SharedPreferences prefs = mPrefs;            
        	//3d targets
        	shape = prefs.getString("target_shape", "diamond");
            quadOn = prefs.getBoolean("target_quad_on", true);
            leftOn = prefs.getBoolean("target_left_on", true);
            topOn = prefs.getBoolean("target_top_on", true);
            pulse3dOn =  prefs.getBoolean("target_dpulse_on", true);
            
            //rotating targets
            discOn = prefs.getBoolean("target_disc_on", true);
//            discStyle = Integer.valueOf(prefs.getString("target_disc_type", "1"));
            discStyle = prefs.getInt("target_disc_type", 1);
            
            //static targets
            mouseOn = prefs.getBoolean("target_mouse_on", false);
            cursor = prefs.getString("cursor_type", "debianswirl");//cursor_typenames

            //pulse settings:
            pulseOn = prefs.getBoolean("target_pulse_on", true);
            spacingOfRings = Integer.valueOf(prefs.getString("target_pulse_width", "15"));
            numberOfRings = Integer.valueOf(prefs.getString("target_pulse_number", "16"));
 

            //flare settings:
            flareOn = prefs.getBoolean("target_flare_on", true);
 
            
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

        /*
         * Draw one frame of the animation. This method gets called repeatedly
         * by posting a delayed Runnable. You can do any drawing you want in
         * here. This example draws a wireframe cube.
         */
        void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();

            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                updateTouchPoint(c);
//DEBUG
//                drawConkey(c);
//Select modes   
                    	drawOrbital(c);
						
                   // }
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

			if(orbitType == ORBIT_SIMPLE)
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
			
			
//            c.drawCircle( mLastTouchX + (float) ( (2+Math.cos( 3*now ) * Math.cos(2*now)) *100)-200, 
//       		     mLastTouchY + (float) ( (2+Math.cos( 3*now ) * Math.sin(2*now)) *100)-200, 
//       		     5, mPaint);//SystemClock.elapsedRealtime()
//            now -= 50;
//            c.drawCircle( mLastTouchX + (float) ( (2+Math.cos( 3*now ) * Math.cos(2*now)) *100)-200, 
//       		     mLastTouchY + (float) ( (2+Math.cos( 3*now ) * Math.sin(2*now)) *100)-200, 
//       		     5, mPaint);//SystemClock.elapsedRealtime()
    }
        
        }
		


    
}
