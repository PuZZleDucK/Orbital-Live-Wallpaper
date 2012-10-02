/*
 * Orbital Live Wallpaper
 *
 * Copyright (C) 2012 PuZZleDucK (PuZZleDucK@gmail.com)
 * 
 *	This program is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License version
 *	3 as published by the Free Software Foundation.
 *
 * This live wallpaper was originally based on the target live wallpaper by PuZZleDucK
 *
 */

package orbitlivewallpaperfree.puzzleduck.com;

import java.util.Random;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.graphics.*;
import android.util.*;

public class OrbitalLiveWallpaper extends WallpaperService {

	protected static final String SHARED_PREFS_NAME = "orbital_lwp_settings";
	
    private static final boolean DEBUG = false;
 
    private static float mTouchX = -1;
    private static float mTouchY = -1;

	public static int ORBIT_6_KNOT = 0;
	public static int ORBIT_4_KNOT = 1;
	public static int ORBIT_4_SIMPLE = 2;
	public static int ORBIT_3_SIMPLE = 3;
	public static int ORBIT_5_SIMPLE = 4;
	public static int ORBIT_8 = 5;
	public static String[] orbitNames = {"6 knot","4 knot","4 simple","3 simple","5 simple","Windows8"};
	private float[][] orbitSpeeds = { {-0.07f,-0.03f,0.03f,0.07f},//6knot
		{-0.05f,-0.1f,-0.2f,-0.3f,0.03f,0.05f,0.1f,0.2f}, //4knot
		{-0.1f,-0.2f,-0.3f,-0.5f,0.1f,0.2f,0.3f,0.5f},//4simple 
		{-0.1f,-0.2f,-0.3f,0.1f,0.2f,0.3f}, //3simple
		{-0.1f,-0.2f,-0.3f,-0.5f,0.1f,0.2f,0.3f,0.5f}, //5simple
		{-0.2f,-0.1f,-0.05f,0.05f,0.1f,0.2f} };//win8
	public int[][] orbitalCounts = { {3,4,5,6},//6
		{3,4,5,6,7,8},//4
		{3,4,5,6,7},//4s
		{3,4,5,6,7},//3s
		{3,4},//5
		{3,4,5,6}};//8
	public static int orbitType = orbitNames.length - 1;  
	public int[][] dotSizes = {{1},//6
		{2,3,4,5},//4
		{2,3,4,5},//4$
		{2,3,4,5},//3$
		{2,3,4},//5
		{2,3}};//8
	
	public static int TRANSITION_NO_TRANSITION = -1;
	public static int TRANSITION_SPIN_IN = 0;
	public static int TRANSITION_SPIN_OUT = 1;
	public static int TRANSITION_HALT_AT_12 = 2;
	public static int TRANSITION_HALT_AT_CLOSEST = 3;
	public static String[] transitionNames = {"no transition","Spin in","Spin out"};//,"Halt at 12","Halt at edge"};
	public static int transitionCount = transitionNames.length-1;
	//new split transitions
	public static int currentTransitionAway = TRANSITION_NO_TRANSITION;//spin away to Centre
	public static int currentTransitionBack = TRANSITION_NO_TRANSITION;//spin back from outside

	public static int orbitRadius = 100;
	public static int orbitDiameter = orbitRadius * 2;
	public static float offset;
	
	public static int width = -1;
	public static int height = -1;
    
    @Override
    public Engine onCreateEngine() {
        return new TargetEngine();
    }
    
    class TargetEngine extends Engine {
		private final Handler mHandler = new Handler();
        private final Paint mPaint = new Paint();
        
        private final Runnable mDrawCube = new Runnable() {
            public void run() {
                drawFrame();
            }
        };
        private boolean mVisible;
        
		private float now = 0;
		private int dotColor = Color.WHITE;
		private int currentScheme = 0;
		private int colorSchemes[][] = { 
			{ Color.argb(255,255,255,255), Color.argb(255,255,255,255), Color.argb(255,255,255,255), Color.argb(255,255,255,255), Color.argb(255,255,255,255), Color.argb(255,255,255,255)},//white
			{ Color.argb(255,195,160,20), Color.argb(255,66,41,8), Color.argb(255,66,41,8), Color.argb(255,66,41,8), Color.argb(255,66,41,8), Color.argb(255,255,255,255)},//xda
			{ Color.argb(255,98,215,230), Color.argb(255,150,195,200), Color.argb(255,98,215,230), Color.argb(255,60,170,180), Color.argb(255,98,215,230), Color.argb(255,255,255,255)},//cyanogen
			{ Color.argb(255,220,115,20), Color.argb(255,220,115,20), Color.argb(255,90,175,200), Color.argb(255,250,245,10), Color.argb(255,220,115,20), Color.argb(255,5,40,90)},//fire Fox
		//	{ Color.argb(255,,,), Color.argb(255,,,), Color.argb(255,,,), Color.argb(255,,,), Color.argb(255,,,), Color.argb(255,,,)},//w
			{ Color.argb(255,220,200,20), Color.argb(255,80,30,120), Color.argb(255,160,50,200), Color.argb(255,190,50,150), Color.argb(255,230,10,30), Color.argb(255,240,50,5)},//Apache
			{ Color.argb(255,255,255,255), Color.argb(255,45,128,124), Color.argb(255,45,128,124), Color.argb(255,45,128,124), Color.argb(255,45,128,124), Color.argb(255,45,128,124)},//slash.
			{ Color.argb(255,255,99,9), Color.argb(255,201,0,22), Color.argb(255,255,181,21), Color.argb(255,255,99,9), Color.argb(255,201,0,22), Color.argb(255,255,181,21)},//ubuntu classic
			{ Color.argb(255,130,230,255), Color.argb(255,130,230,255), Color.argb(255,100,200,255), Color.argb(255,100,200,255), Color.argb(255,160,260,255), Color.argb(255,160,260,255)},//cy-gen
			{ Color.argb(255,255,0,255), Color.argb(255,255,0,90), Color.argb(255,255,0,90), Color.argb(255,255,0,255), Color.argb(255,255,0,90), Color.argb(255,255,0,255)},//ferretcheery
			{ Color.argb(255,101,16,89), Color.argb(255,255,99,9), Color.argb(255,201,0,22), Color.argb(255,101,16,89), Color.argb(255,255,99,9), Color.argb(255,201,0,22) }//Ubuntu purple
		};
		private String[] colorSchemeNames = {"White","XDA","Cyanogen","FireFox","Apache","/.","Ubuntu1","Cyanogen","Cherry","Ubuntu2"};
	
		private float orbitSpeed = 0.1f;//start average screen

		private float orbitalCompression = 0.125f;//2.5f * orbitSpeed;
		private int orbitalCount = 0;
		private float dotSizeIncrement = 1.0f;
		
		private int trailCount = 6;
		private int setCount = 3;
		
		private int defaultRadius = 100;
		private int offScreenRadius = 1200;
		private int fastSpeed = 20;
		private int slowSpeed = 7;
		int speedIndex = 0;
		int dotSize = 3;

		private Random rng = new Random();
		
        
        TargetEngine() {
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(2);//increased stroke... better thin
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStyle(Paint.Style.STROKE); 
			mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        }
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);
           
		    setWindowProperties();
			rng.setSeed(SystemClock.elapsedRealtime());
        }

		private void setWindowProperties()
		{
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            width = display.getWidth();
            height = display.getHeight();
            mTouchX =  width/2;
            mTouchY = height/2;
			offScreenRadius = Math.max(width, height);
			//should also make default radius relative to Max extremity
			
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
					
		 //  setWindowProperties();
		 // check for layout change First
		// Rect surfaceRect = this.getSurfaceHolder().getSurfaceFrame();
		 
	//	 float temp = mTouchX;
	//	 mTouchX = mTouchY;
	//	 mTouchY = temp;
		 // actually rotates ok
		 
		 
            drawFrame();
        }

		
        @Override
        public void onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                mTouchX = event.getX();
                mTouchY = event.getY();
            } 
	
			if(	currentTransitionAway == TRANSITION_NO_TRANSITION && currentTransitionBack == TRANSITION_NO_TRANSITION)
			{
				triggerNewTransitionAway();
			}
            super.onTouchEvent(event);
        }

		private void triggerNewTransitionAway()
		{
			currentTransitionAway = rng.nextInt(2); // rng.nextInt(transitionCount);
			orbitRadius = defaultRadius;
		}

		
		private void triggerNewTransitionBack()
		{
			offscreenSetNewOrbit();
			
			currentTransitionAway = TRANSITION_NO_TRANSITION;
			currentTransitionBack = rng.nextInt(2); // rng.nextInt(transitionCount);
			if(currentTransitionBack == TRANSITION_SPIN_IN)
			{
				orbitRadius = offScreenRadius;
			}

			if(currentTransitionBack == TRANSITION_SPIN_OUT)
			{
				orbitRadius = 0;
			}
			
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
        	   //pre draw canvas clearing.... do not remove (again).
        	   c.drawColor(0xff000000);   
        }
        


        void drawOrbital(Canvas c) 
        {
			if(DEBUG)
			{
				mPaint.setTextSize(24);
				mPaint.setColor(Color.WHITE);
				c.drawText("debug now; " + now, 30,height-460,mPaint);
				c.drawText("      Trans away: " + transitionNames[currentTransitionAway+1], 30,height-430,mPaint);
				c.drawText("      Trans back: " + transitionNames[currentTransitionBack+1], 30,height-400,mPaint);
				
				c.drawText("      Radius : " + orbitRadius, 30,height-380,mPaint);
				c.drawText("      Default: " + defaultRadius, 30,height-360,mPaint);
				
				c.drawText("      a.o > d: " + (Math.abs(orbitRadius) > defaultRadius), 30,height-330,mPaint);
				c.drawText("      a.o < d: " + (Math.abs(orbitRadius) < defaultRadius) , 30,height-300,mPaint);
				

				c.drawText("      orbit: " + orbitNames[orbitType] , 30,height-270,mPaint);
				c.drawText("      speed: " +  orbitSpeed, 30,height-240,mPaint);
				c.drawText("      count: " +  setCount +"/"+trailCount, 30,height-210,mPaint);
				c.drawText("      offset 1: " +  offset, 30,height-180,mPaint);
				
				mPaint.setARGB(255,255,0,0);
				c.drawLine(mTouchX,mTouchY,mTouchX+(300f*(float)Math.sin(offset)),mTouchY+(300f*(float)Math.cos(offset)),mPaint);
				c.drawLine(mTouchX,mTouchY,mTouchX+(300f*(float)Math.sin(now)),mTouchY+(300f*(float)Math.cos(now)),mPaint);
				
			}

			dotColor = 0;

			if(orbitRadius <= 0 && currentTransitionAway == TRANSITION_SPIN_IN)
			{
				triggerNewTransitionBack();
			}// rad = 0 or off;
			
			if(orbitRadius > offScreenRadius && currentTransitionAway == TRANSITION_SPIN_OUT)
			{
				triggerNewTransitionBack();
			}// rad = 0 or off;
			
			if(currentTransitionAway == TRANSITION_SPIN_OUT )
			{
				orbitRadius += fastSpeed;
				orbitDiameter = orbitRadius*2;
			}

			if(currentTransitionAway == TRANSITION_SPIN_IN )
			{
				orbitRadius -= slowSpeed;
				orbitDiameter = orbitRadius*2;
			}
			
			if(currentTransitionBack == TRANSITION_SPIN_OUT )
			{
				orbitRadius += slowSpeed ;
				orbitDiameter = orbitRadius*2;
			}

			if(currentTransitionBack == TRANSITION_SPIN_IN )
			{
				orbitRadius -= fastSpeed ;
				orbitDiameter = orbitRadius*2;
			}

			if( (Math.abs(orbitRadius) > offScreenRadius) && (currentTransitionAway == TRANSITION_SPIN_OUT) ) //inTransition)
			{
				triggerNewTransitionBack();
			}

			if( (Math.abs(orbitRadius) <= defaultRadius) && (currentTransitionBack == TRANSITION_SPIN_IN) )
			{
				currentTransitionBack = TRANSITION_NO_TRANSITION;
			}

			if( (Math.abs(orbitRadius) >= defaultRadius) && (currentTransitionBack == TRANSITION_SPIN_OUT) ) //inTransition)
			{
				currentTransitionBack = TRANSITION_NO_TRANSITION;
			}

			if( (Math.abs(orbitRadius) < 1) && (currentTransitionAway == TRANSITION_SPIN_IN) )
			{
				triggerNewTransitionBack();
			}
			
			
			//reset other transitions irregularity
			if(currentTransitionAway == TRANSITION_HALT_AT_12)
			{
				currentTransitionAway = TRANSITION_NO_TRANSITION;
				//orbitRadius = defaultRadius;
			}
			if(currentTransitionAway == TRANSITION_HALT_AT_CLOSEST)
			{
				currentTransitionAway = TRANSITION_NO_TRANSITION;
				//orbitRadius = defaultRadius;
			}

			if(currentTransitionBack == TRANSITION_HALT_AT_12)
			{
				currentTransitionBack = TRANSITION_NO_TRANSITION;
				//orbitRadius = defaultRadius;
			}
			if(currentTransitionBack == TRANSITION_HALT_AT_CLOSEST)
			{
				currentTransitionBack = TRANSITION_NO_TRANSITION;
				//orbitRadius = defaultRadius;
			}
			
        	if(orbitType == ORBIT_6_KNOT) 
        	{
				setCount = 6;
        		orbitalCount = setCount*trailCount;
                
	            for(int i = 0; i < orbitalCount; i++)
	            {
					mPaint.setColor(colorSchemes[currentScheme][dotColor]);
					dotColor = (dotColor + 1)%(setCount);
					offset = now-(i*45);//to split out of function
				//	int dotSize = i/setCount;
					
					
	                c.drawCircle( mTouchX + (float) ( (2+Math.cos( 3*offset ) * Math.cos(2*offset )) * orbitRadius )-orbitDiameter, 
								 mTouchY + (float) ( (2+Math.cos( 3*offset ) * Math.sin(2*offset )) *orbitRadius)-orbitDiameter, 
	           		     (1+dotSize)*dotSizeIncrement, mPaint);
	            }
        	}//ORBIT_6_KNOT
        	
        	if(orbitType == ORBIT_4_KNOT)
        	{
				setCount=4;
        		orbitalCount = setCount*trailCount;
	            for(int i = 0; i < orbitalCount; i++)
	            {
					mPaint.setColor(colorSchemes[currentScheme][dotColor]);
					dotColor = (dotColor + 1)%setCount;
				//	int dotSize = i/setCount;
					offset = now-(i*67.5f);
	            	
	                c.drawCircle( mTouchX + (float) ( (2+Math.cos( 2*offset ) * Math.cos(offset)) *orbitRadius )-orbitDiameter, 
								 mTouchY + (float) ( (2+Math.cos( 2*offset ) * Math.sin(offset)) *orbitRadius )-orbitDiameter, 
	           		     (1+dotSize)*dotSizeIncrement, mPaint);
	            }
        	}//ORBIT_4_KNOT

			if(orbitType == ORBIT_4_SIMPLE)
        	{
				setCount = 4;
        		orbitalCount = setCount * trailCount;
	            for(int i = 0; i < orbitalCount; i++)
	            {
					mPaint.setColor(colorSchemes[currentScheme][dotColor]);
					dotColor = (dotColor + 1)%setCount;
				//	int dotSize = i/setCount;
					offset = now-(i*67.5f);
					
	                c.drawCircle( mTouchX + (float) ( (Math.sin( offset ) ) *orbitRadius), 
								 mTouchY + (float) ( (Math.cos( offset ) ) *orbitRadius), 
								 1+(dotSize*dotSizeIncrement), mPaint);
	            }
        	}//ORBIT_4_SIMPLE
			
			if(orbitType == ORBIT_3_SIMPLE)
        	{
				setCount = 3;
        		orbitalCount = setCount * trailCount;
               
	            for(int i = 0; i < orbitalCount; i++)
	            {
					mPaint.setColor(colorSchemes[currentScheme][dotColor]);
					dotColor = (dotColor + 1)%setCount;
				//	int dotSize = i/setCount;
					offset = now-(i*90f);
					
	                c.drawCircle( mTouchX + (float) ( (Math.sin( offset ) ) *orbitRadius), 
								 mTouchY + (float) ( (Math.cos( offset ) ) *orbitRadius), 
								 1+(dotSize*dotSizeIncrement), mPaint);
	            }
			}//simple3
			
			if(orbitType == ORBIT_5_SIMPLE)
        	{
				setCount = 5;
        		orbitalCount = setCount*trailCount;
	            for(int i = 0; i < orbitalCount; i++)
	            {
					mPaint.setColor(colorSchemes[currentScheme][dotColor]);
					dotColor = (dotColor + 1)%setCount;
				//	int dotSize = i/setCount;
					offset = now-(i*5f);
					
	                c.drawCircle( mTouchX + (float) ( (Math.sin( offset ) ) *orbitRadius), 
								 mTouchY + (float) ( (Math.cos( offset ) ) *orbitRadius), 
								 (1+dotSize)*dotSizeIncrement, mPaint);
	            }
			}//5 simple

			if(orbitType == ORBIT_8)
        	{
			//	setCount = trailCount;
        		orbitalCount = trailCount;

				if(Math.sin(now) < -0.1 || Math.sin(now) > 0.1)//compensate for orientation (or better yet gyro) in next release instead of hard coding 179
				{
					orbitalCompression +=  ((float) Math.sin( now ) * 0.2f)*orbitSpeed;//* (orbitSpeed/5)  //orbitSpeed
				}
				
	            for(int i = 0; i < orbitalCount; i++)
	            {
					mPaint.setColor(colorSchemes[currentScheme][dotColor]);
					dotColor = (dotColor + 1)%setCount;
					offset = now + (i * Math.abs(orbitalCompression)) ;  //+ (5) add rotation offset
					
					c.drawCircle( mTouchX + (float) ( (Math.sin( offset+179f) ) *orbitRadius), 
								 mTouchY + (float) ( (Math.cos( offset+179f) ) *orbitRadius), 
								 2+dotSize, mPaint);
	            } 
			}//windows late

			now += orbitSpeed;
        }

		private void offscreenSetNewOrbit()
		{
			currentTransitionAway = TRANSITION_NO_TRANSITION;

			orbitType =  rng.nextInt(orbitNames.length);
			trailCount = orbitalCounts[orbitType][rng.nextInt(orbitalCounts[orbitType].length)];
			speedIndex = rng.nextInt( orbitSpeeds[orbitType].length );
			orbitSpeed = orbitSpeeds[orbitType][speedIndex] ;
			dotSize = dotSizes[orbitType][rng.nextInt(dotSizes[orbitType].length)];
			currentScheme = rng.nextInt(colorSchemes.length);
			
			now = 0;//removed for continue//replaced for reliability
			orbitalCompression = 0.125f;
		}//setup new orbit
        
    }//class TargetEngine
		
}//class OrbitalLiveWallpaper	
