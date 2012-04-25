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
public class TargetLiveWallpaper extends WallpaperService {
    @Override
	public void onConfigurationChanged(Configuration newConfig) {

    	super.onConfigurationChanged(newConfig);
		this.onCreate();
	}

	public static final String SHARED_PREFS_NAME="target_lwp_settings";
    private static final String TAG = "TargetLiveWallpaper";

    private ArrayList<FlareData> flareList;
    

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
		
        ThreeDPoint [] mOriginalPoints;
        ThreeDPoint [] mRotatedPoints;
        ThreeDLine [] mLines;
        ThreeDTriangle [] mTriangles;
        ThreeDSquare [] mSquares;

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
            mPrefs = TargetLiveWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
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
            readWavefrontModelFile("/sdcard/whatever.obj");
            
            
            //from sdk... think i get it now
            Resources myResources;
            myResources = getBaseContext().getResources();
            mCursorImage = BitmapFactory.decodeResource(myResources, getResources().getIdentifier( getPackageName() + ":drawable/"+cursor, null, null));

            //restart engine here
            TargetLiveWallpaper.this.onDestroy();
            TargetLiveWallpaper.this.onCreate();
            TargetLiveWallpaper.TargetEngine.this.onDestroy();
            TargetLiveWallpaper.TargetEngine.this.onCreate(getSurfaceHolder());
//            TargetLiveWallpaper.TargetEngine.this.onSurfaceChanged(getSurfaceHolder(), 0, 0, 0);
            drawFrame();
            
            onDestroy();
            onCreate(getSurfaceHolder());
        }


        private void readWavefrontModelFile(String fileName) {
            // Read the model definition in from a resource.
			Log.d(TAG, " read wavefront" );

			//replace this with xml parse of file:
			String [] p = {"571.500000 571.500000 882.000000", "571.500000 455.000000 660.000000", "677.000000 455.000000 660.000000", "455.000000 455.000000 660.000000", "571.500000 560.500000 660.000000", "677.000000 571.500000 660.000000", "455.000000 571.500000 660.000000", "566.000000 566.000000 660.000000", "455.000000 338.500000 660.000000", "338.500000 338.500000 1077.750000", "338.500000 455.000000 660.000000", "566.000000 566.000000 438.000000", "566.000000 566.000000 216.000000", "571.500000 571.500000 1104.000000", "677.000000 677.000000 660.000000", "571.500000 571.500000 660.000000", "455.000000 677.000000 660.000000", "560.500000 571.500000 660.000000", "571.500000 338.500000 660.000000", "338.500000 338.500000 660.000000", "338.500000 338.500000 0.000000", "338.500000 338.500000 1293.750000", "396.750000 396.750000 812.000000", "222.000000 455.000000 660.000000", "571.500000 677.000000 660.000000", "338.500000 571.500000 660.000000", "338.500000 338.500000 330.000000", "338.500000 338.500000 964.000000", "455.000000 222.000000 660.000000", "338.500000 222.000000 660.000000", "367.625000 367.625000 944.875000", "571.500000 222.000000 660.000000", "222.000000 338.500000 660.000000", "222.000000 571.500000 660.000000", "105.500000 222.000000 660.000000", "105.500000 455.000000 660.000000", "280.250000 396.750000 812.000000", "367.625000 309.375000 944.875000", "309.375000 309.375000 944.875000", "396.750000 280.250000 812.000000", "105.500000 338.500000 660.000000", "222.000000 222.000000 660.000000", "455.000000 105.500000 660.000000", "222.000000 105.500000 660.000000", "309.375000 367.625000 944.875000", "280.250000 280.250000 812.000000", "338.500000 105.500000 660.000000", "571.500000 105.500000 882.000000", "677.000000 222.000000 660.000000", "571.500000 105.500000 438.000000", "0.000000 455.000000 660.000000", "105.500000 571.500000 882.000000", "105.500000 571.500000 438.000000", "105.500000 105.500000 882.000000", "0.000000 222.000000 660.000000", "105.500000 105.500000 660.000000", "571.500000 105.500000 1104.000000", "571.500000 105.500000 660.000000", "105.500000 105.500000 438.000000", "455.000000 0.000000 660.000000", "571.500000 105.500000 216.000000", "105.500000 571.500000 660.000000", "105.500000 571.500000 216.000000", "222.000000 677.000000 660.000000", "105.500000 105.500000 1104.000000", "0.000000 105.500000 660.000000", "677.000000 105.500000 660.000000", "222.000000 0.000000 660.000000", "105.500000 105.500000 216.000000", "105.500000 0.000000 660.000000", "677.000000 0.000000 660.000000", "571.500000 0.000000 660.000000", "105.500000 571.500000 1104.000000", "0.000000 571.500000 660.000000", "105.500000 677.000000 660.000000", "0.000000 677.000000 660.000000", "0.000000 0.000000 660.000000"};
////                //points
        	//mesh1-geometry -> mesh1-geometry-position-array  and "mesh2-geometry"
            //for each mesh#-geometry
            
        	int numpoints = p.length;
            mOriginalPoints = new ThreeDPoint[numpoints];
            mRotatedPoints = new ThreeDPoint[numpoints];
            Log.d(TAG, "numpoints = " + numpoints);
            
            for (int i = 0; i < numpoints; i++) {
                mOriginalPoints[i] = new ThreeDPoint();
                mRotatedPoints[i] = new ThreeDPoint();
                String [] coord = p[i].split(" ");
                
                // Instead of scaling hard... store max/min x/y/z values and scale to 'uniform' size
                
                mOriginalPoints[i].x = Float.valueOf(coord[1])*TD_SCALE;
                mOriginalPoints[i].y = Float.valueOf(coord[0])*TD_SCALE;
                mOriginalPoints[i].z = Float.valueOf(coord[2])*TD_SCALE;
//	            if (i == (numpoints-1)) 
//	            {
//					Log.d(TAG, "       x = " + coord[0]);
//					Log.d(TAG, "       y = " + coord[1]);
//					Log.d(TAG, "       z = " + coord[2]);
//				}
            }
            String [] t = {"0 0 1 0 2 0", "1 0 0 0 3 0", "3 1 2 1 1 1", "2 2 1 2 4 2", "0 3 4 3 2 3", "5 4 0 4 2 4", "0 5 6 5 3 5", "0 6 7 6 3 6", "7 2 1 2 3 2", "1 2 8 2 3 2", "3 7 9 7 1 7", "1 8 10 8 3 8", "2 1 3 1 11 1", "1 2 7 2 4 2", "4 9 12 9 1 9", "13 10 4 10 1 10", "11 11 4 11 2 11", "4 2 5 2 2 2", "4 3 0 3 7 3", "0 4 5 4 14 4", "0 12 5 12 15 12", "5 12 0 12 13 12", "11 13 5 13 2 13", "6 5 0 5 16 5", "10 2 6 2 3 2", "8 14 6 14 3 14", "17 2 3 2 6 2", "9 15 3 15 6 15", "6 16 11 16 3 16", "7 3 0 3 17 3", "7 6 0 6 15 6", "3 2 17 2 7 2", "11 6 3 6 7 6", "8 2 1 2 18 2", "19 2 3 2 8 2", "8 17 20 17 3 17", "8 18 21 18 3 18", "9 15 8 15 3 15", "8 19 22 19 3 19", "10 7 9 7 3 7", "19 20 1 20 9 20", "9 21 18 21 1 21", "1 8 23 8 10 8", "3 22 23 22 10 22", "3 2 19 2 10 2", "22 23 10 23 3 23", "21 24 10 24 3 24", "4 11 11 11 7 11", "4 2 7 2 15 2", "12 9 4 9 15 9", "4 10 13 10 15 10", "5 2 4 2 15 2", "5 2 15 2 14 2", "5 13 11 13 14 13", "14 6 15 6 0 6", "24 25 0 25 14 25", "5 26 17 26 15 26", "15 10 13 10 0 10", "15 12 13 12 0 12", "0 10 24 10 15 10", "24 10 0 10 13 10", "17 3 0 3 16 3", "0 25 24 25 16 25", "17 2 6 2 16 2", "11 16 6 16 16 16", "6 2 10 2 25 2", "8 14 26 14 6 14", "5 26 6 26 17 26", "13 12 17 12 6 12", "27 27 6 27 9 27", "25 28 9 28 6 28", "15 2 7 2 17 2", "7 11 16 11 17 11", "15 6 11 6 7 6", "16 11 7 11 11 11", "18 29 26 29 1 29", "18 2 28 2 8 2", "19 30 18 30 8 30", "27 30 8 30 18 30", "26 31 3 31 19 31", "19 31 3 31 27 31", "8 30 27 30 19 30", "8 2 29 2 19 2", "20 17 8 17 28 17", "23 22 3 22 20 22", "3 31 26 31 20 31", "21 18 8 18 28 18", "21 31 30 31 3 31", "9 15 28 15 8 15", "8 19 27 19 22 19", "3 31 30 31 22 31", "27 31 3 31 22 31", "23 7 9 7 10 7", "1 20 19 20 26 20", "19 20 9 20 27 20", "9 32 26 32 19 32", "9 32 19 32 27 32", "19 33 31 33 9 33", "19 33 9 33 27 33", "18 21 9 21 31 21", "23 8 1 8 26 8", "10 2 32 2 23 2", "10 23 22 23 23 23", "10 24 21 24 23 24", "23 2 25 2 10 2", "32 2 10 2 19 2", "19 10 25 10 10 10", "10 10 26 10 19 10", "12 9 15 9 24 9", "17 12 13 12 15 12", "11 6 15 6 14 6", "14 2 15 2 24 2", "14 34 16 34 11 34", "16 34 14 34 24 34", "17 2 24 2 15 2", "24 2 17 2 16 2", "20 10 10 10 25 10", "6 35 33 35 25 35", "28 14 26 14 8 14", "6 27 19 27 26 27", "33 35 6 35 26 35", "6 26 5 26 12 26", "19 27 6 27 27 27", "9 10 25 10 27 10", "34 36 27 36 9 36", "33 37 27 37 9 37", "35 38 27 38 9 38", "36 39 9 39 27 39", "37 40 27 40 9 40", "38 31 27 31 9 31", "30 31 9 31 27 31", "27 10 21 10 9 10", "21 30 27 30 9 30", "26 41 9 41 27 41", "27 30 21 30 9 30", "25 10 9 10 21 10", "9 28 25 28 33 28", "18 30 26 30 20 30", "26 30 18 30 19 30", "26 29 18 29 31 29", "28 2 18 2 31 2", "29 2 8 2 28 2", "39 19 8 19 28 19", "8 19 39 19 27 19", "21 30 27 30 18 30", "23 42 19 42 26 42", "19 36 34 36 26 36", "27 43 26 43 19 43", "19 37 33 37 26 37", "19 38 35 38 26 38", "27 31 26 31 19 31", "20 10 19 10 26 10", "40 30 26 30 19 30", "31 33 19 33 26 33", "26 41 27 41 19 41", "19 42 23 42 27 42", "34 36 19 36 27 36", "27 37 33 37 19 37", "35 38 19 38 27 38", "25 10 19 10 27 10", "29 10 27 10 19 10", "27 30 32 30 19 30", "41 2 19 2 29 2", "20 10 29 10 19 10", "20 43 39 43 28 43", "28 44 41 44 20 44", "20 45 32 45 23 45", "23 46 26 46 20 46", "27 43 20 43 26 43", "26 30 40 30 20 30", "26 10 10 10 20 10", "26 31 41 31 20 31", "39 47 21 47 28 47", "29 48 21 48 28 48", "30 31 21 31 9 31", "9 49 28 49 31 49", "28 49 9 49 29 49", "28 15 9 15 42 15", "22 31 30 31 27 31", "36 23 22 23 27 23", "9 7 23 7 35 7", "26 32 9 32 43 32", "9 41 26 41 42 41", "26 50 32 50 23 50", "33 50 26 50 23 50", "23 51 26 51 35 51", "32 52 33 52 23 52", "40 2 23 2 32 2", "27 53 32 53 23 53", "21 54 32 54 23 54", "23 23 22 23 36 23", "21 11 44 11 23 11", "25 2 23 2 33 2", "19 2 41 2 32 2", "40 30 19 30 32 30", "26 14 28 14 42 14", "9 55 40 55 34 55", "41 49 9 49 34 49", "32 52 9 52 33 52", "40 55 9 55 35 55", "9 39 36 39 44 39", "27 42 23 42 36 42", "27 53 23 53 36 53", "27 40 37 40 39 40", "21 47 37 47 9 47", "30 56 9 56 37 56", "9 57 38 57 37 57", "27 31 38 31 45 31", "21 31 38 31 9 31", "44 58 38 58 9 58", "9 59 44 59 30 59", "21 10 27 10 46 10", "27 30 21 30 40 30", "44 11 21 11 9 11", "45 31 26 31 27 31", "41 60 31 60 26 60", "47 61 31 61 28 61", "31 2 42 2 28 2", "48 62 28 62 31 62", "31 60 29 60 28 60", "41 44 28 44 29 44", "29 63 45 63 28 63", "28 2 46 2 29 2", "28 63 45 63 39 63", "39 43 20 43 27 43", "39 63 45 63 27 63", "40 64 26 64 34 64", "41 65 26 65 34 65", "26 64 40 64 35 64", "27 10 29 10 46 10", "45 53 32 53 27 53", "32 30 27 30 40 30", "45 63 29 63 41 63", "29 49 9 49 41 49", "31 60 41 60 29 60", "21 48 29 48 41 48", "29 2 43 2 41 2", "29 10 20 10 46 10", "32 45 20 45 41 45", "26 31 45 31 41 31", "32 50 26 50 41 50", "26 66 43 66 41 66", "21 47 39 47 37 47", "9 67 46 67 42 67", "49 68 42 68 28 68", "42 69 47 69 28 69", "46 2 28 2 42 2", "23 2 40 2 35 2", "35 2 33 2 23 2", "50 70 23 70 35 70", "51 71 35 71 23 71", "9 52 41 52 43 52", "46 67 9 67 43 67", "42 72 43 72 26 72", "52 73 33 73 23 73", "33 74 51 74 23 74", "32 2 34 2 40 2", "32 54 21 54 41 54", "23 11 44 11 36 11", "41 52 9 52 32 52", "34 2 32 2 41 2", "32 53 45 53 41 53", "34 75 53 75 41 75", "41 76 54 76 34 76", "41 2 55 2 34 2", "38 2 30 2 37 2", "21 31 45 31 38 31", "30 2 38 2 44 2", "31 61 47 61 48 61", "31 10 47 10 56 10", "47 10 31 10 57 10", "28 77 57 77 47 77", "42 2 31 2 57 2", "28 62 48 62 49 62", "48 2 57 2 31 2", "43 2 29 2 46 2", "45 31 21 31 41 31", "53 78 43 78 41 78", "43 79 58 79 41 79", "55 2 41 2 43 2", "43 72 42 72 46 72", "42 68 49 68 59 68", "42 12 49 12 60 12", "49 12 42 12 57 12", "57 77 28 77 49 77", "47 69 42 69 59 69", "33 2 35 2 61 2", "23 70 50 70 52 70", "50 2 61 2 35 2", "35 71 51 71 50 71", "51 80 52 80 23 80", "33 30 52 30 62 30", "52 30 33 30 61 30", "33 73 52 73 63 73", "51 74 33 74 63 74", "53 75 34 75 54 75", "34 81 53 81 55 81", "53 81 34 81 64 81", "55 82 41 82 53 82", "54 76 41 76 58 76", "34 2 65 2 54 2", "41 82 55 82 58 82", "65 2 34 2 55 2", "58 81 34 81 55 81", "48 83 57 83 47 83", "47 84 66 84 48 84", "57 10 56 10 47 10", "66 85 47 85 56 85", "57 12 56 12 47 12", "31 10 49 10 57 10", "47 85 66 85 57 85", "49 86 47 86 57 86", "57 87 59 87 47 87", "57 2 59 2 42 2", "56 12 57 12 42 12", "66 88 49 88 48 88", "57 83 48 83 49 83", "57 2 48 2 66 2", "43 78 53 78 67 78", "53 12 43 12 55 12", "43 12 53 12 64 12", "58 79 43 79 67 79", "58 12 43 12 68 12", "43 12 58 12 55 12", "43 2 69 2 55 2", "70 89 59 89 49 89", "59 87 57 87 49 87", "49 85 66 85 60 85", "49 10 31 10 60 10", "60 10 57 10 49 10", "66 85 49 85 57 85", "71 90 47 90 59 90", "62 81 61 81 35 81", "61 81 72 81 35 81", "61 2 63 2 33 2", "72 30 61 30 33 30", "52 91 73 91 50 91", "51 92 52 92 50 92", "61 2 50 2 73 2", "51 93 73 93 50 93", "52 80 51 80 61 80", "52 92 51 92 61 92", "61 81 62 81 52 81", "52 30 73 30 62 30", "74 81 52 81 62 81", "73 30 52 30 61 30", "61 80 75 80 52 80", "52 81 74 81 61 81", "63 94 61 94 52 94", "75 95 63 95 52 95", "61 94 63 94 51 94", "74 96 51 96 63 96", "55 97 54 97 53 97", "65 98 53 98 54 98", "55 12 64 12 53 12", "53 31 58 31 55 31", "55 99 67 99 53 99", "64 81 55 81 53 81", "58 100 65 100 54 100", "54 97 55 97 58 97", "55 81 68 81 58 81", "67 99 55 99 58 99", "58 12 65 12 55 12", "64 12 55 12 65 12", "55 2 76 2 65 2", "34 81 58 81 68 81", "66 84 47 84 70 84", "56 10 57 10 71 10", "66 2 71 2 57 2", "47 86 49 86 70 86", "59 2 57 2 71 2", "49 88 66 88 70 88", "53 101 69 101 67 101", "69 2 43 2 67 2", "76 102 67 102 58 102", "65 12 58 12 68 12", "68 81 55 81 69 81", "76 2 55 2 69 2", "55 81 64 81 69 81", "59 89 70 89 71 89", "57 10 60 10 71 10", "47 90 71 90 70 90", "61 30 72 30 51 30", "72 81 61 81 51 81", "63 2 61 2 74 2", "73 91 52 91 75 91", "73 2 74 2 61 2", "51 30 73 30 61 30", "73 30 51 30 72 30", "73 93 51 93 75 93", "75 80 61 80 51 80", "74 81 51 81 61 81", "63 95 75 95 74 95", "51 81 74 81 72 81", "51 96 74 96 75 96", "53 98 65 98 76 98", "58 31 53 31 76 31", "65 100 58 100 76 100", "71 2 66 2 70 2", "69 101 53 101 76 101", "67 102 76 102 69 102", "74 2 73 2 75 2"};
            int numTriangles = t.length;
//            Log.d(TAG, "numTriangles = " + numTriangles);
            mTriangles 		 = new ThreeDTriangle[numTriangles];

            for (int i = 0; i < numTriangles; i++) {
              mTriangles[i] = new ThreeDTriangle();
              String [] idx = t[i].split(" ");

              mTriangles[i].point1 = Integer.valueOf(idx[0]);
              mTriangles[i].point2 = Integer.valueOf(idx[2]);
              mTriangles[i].point3 = Integer.valueOf(idx[4]);
              
//	            if (true)//i == (numTriangles-1) 
//	            {
//					Log.d(TAG, i + ") \tp1 = " + mTriangles[i].point1 + " \tp2 = " + mTriangles[i].point2 + "   p3 = " + mTriangles[i].point3);
//				}
          }
        }
        
        private void readModel(String prefix) {
            // Read the model definition in from a resource.
			Log.d(TAG, " read resource" );

            // get the resource identifiers for the arrays for the selected shape
            int pid = getResources().getIdentifier(prefix + "points", "array", getPackageName());
            int lid = getResources().getIdentifier(prefix + "lines", "array", getPackageName());
//            int pid = getResources().getIdentifier("diamondpoints", "array", getPackageName());

            String [] p = getResources().getStringArray(pid);
            int numpoints = p.length;
            mOriginalPoints = new ThreeDPoint[numpoints];
            mRotatedPoints = new ThreeDPoint[numpoints];

            for (int i = 0; i < numpoints; i++) {
                mOriginalPoints[i] = new ThreeDPoint();
                mRotatedPoints[i] = new ThreeDPoint();
                String [] coord = p[i].split(" ");
                mOriginalPoints[i].x = Float.valueOf(coord[0]);
                mOriginalPoints[i].y = Float.valueOf(coord[1]);
                mOriginalPoints[i].z = Float.valueOf(coord[2]);
            }

            String [] l = getResources().getStringArray(lid);
            int numlines = l.length;
            
            mLines 		 = new ThreeDLine[numlines];

            for (int i = 0; i < numlines; i++) {
                mLines[i] = new ThreeDLine();
                String [] idx = l[i].split(" ");
                mLines[i].startPoint = Integer.valueOf(idx[0]);
                mLines[i].endPoint = Integer.valueOf(idx[1]);
            }
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);
           
            //init flare list
            flareList = new ArrayList<FlareData>(1);
            
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
                if(pulseOn)
                  {
//                        drawTouchPointPulse(c);
                  }
                  if(discOn)
                  {
//                      drawTouchDisc(c);
                  }   
                  
                    if(topOn)
                    {
//                    	drawTopTarget(c);
                    }
                    if(leftOn)
                    {
                    	drawLeftTarget(c);
                    }

                    if(quadOn)
                    {
//                    	drawQuadTarget(c);	
                    }
                   

                    if(mouseOn)
                    {
//                    	drawStaticTarget(c);	
                    }

                    if(flareOn)
                    {
                    	drawTouchPointFlare(c);
                    }
                    
                    if(true)
                    {
                    	drawOrbital(c);
                    }
                }
            } finally { 
                if (c != null) holder.unlockCanvasAndPost(c);
            }

            mHandler.removeCallbacks(mDrawCube);
            if (mVisible) {
                mHandler.postDelayed(mDrawCube, 1000 / 25);
            }
        }
        
        void drawQuadTarget(Canvas c) {
            c.save();
            c.drawColor(0x00000000);
            int oldColor = mPaint.getColor();
            mPaint.setColor(0xff00ff00);

            long now = SystemClock.elapsedRealtime();

            float xrot = (float) now/400;
            float yrot = (float) now/400;
//            rotateAndProjectPoints3(xrot, yrot);
            
            mPaint.setColor(0xFFFFFFFF-(0x000001 * ((int)now)/5 ));
            //mPaint.setColor(Color.argb(255,255-((int)now/5%200),0,0));       //0-255 or 0xAARRGGBB
            //argb(int alpha, int red, int green, int blue)
            
            mLeftTargetX = mLastTouchX;
            mLeftTargetY = mLastTouchY;
            int targeDistance = 190;
	        

	        rotateAndProjectPointsRight(xrot, 0);            
            c.translate(mLeftTargetX+targeDistance, mLeftTargetY);
	        drawLines(c);//RIGHT            

	        rotateAndProjectPointsLeft(xrot, 0);            
            c.translate(-targeDistance*2, 0);
	        drawLines(c);//left
	        

	        rotateAndProjectPointsBottom(0, yrot);            
            c.translate(targeDistance, targeDistance);
	        drawLines(c);//low
	        rotateAndProjectPointsTop(0, yrot);            
            c.translate(0, -targeDistance*2);
	        drawLines(c);//high
	        

            mPaint.setColor(oldColor);
          	c.restore(); 
       }
        
        void drawTopTarget(Canvas c) {
            c.save();
            c.drawColor(0x00000000);
            int oldColor = mPaint.getColor();
            long now = SystemClock.elapsedRealtime();
            
            if(!pulse3dOn)
            {
                //static
            	mPaint.setColor(0xff00ff00);
            }else{
                //pulse
                mPaint.setColor(Color.argb(255,255-((int)now/5%200),0,0));     	
            }
            float xrot = (float)0;
            float yrot = (float)now/400;
            rotateAndProjectPointsTop(xrot, yrot);

            mTopTargetX = mLastTouchX;
            mTopTargetY = 60;
	        c.translate(mTopTargetX, mTopTargetY);
	        drawLines(c);
            mPaint.setColor(oldColor);
          	c.restore(); 
       }
        
        
        void drawLeftTarget(Canvas c) {

            c.save();
            c.drawColor(0x00000000);
            int oldColor = mPaint.getColor();

            long now = SystemClock.elapsedRealtime();
//            float xrot = (float) now/400;
            float xrot = (float) now/400;//stop rotation for debug
            float yrot = (float) 0;//now/40000
            rotateAndProjectPointsLeft(xrot, yrot);


            if(!pulse3dOn)
            {
                //static
            	mPaint.setColor(0xff00ff00);
            }else{
                //pulse
                mPaint.setColor(Color.argb(255,255-((int)now/5%200),0,0));     	
            }
            
            
            mLeftTargetX = TD_OFFSET_Y;
            mLeftTargetY = mLastTouchY;
	        c.translate(mLeftTargetX, mLeftTargetY);
//	        drawLines(c);
	        drawTriangles(c);
            mPaint.setColor(oldColor);
          	c.restore();

       }

        
        
        void rotateAndProjectPointsTop(float xrot, float yrot) {
            int n = mOriginalPoints.length;
            for (int i = 0; i < n; i++) {
                // rotation around X-axis
                ThreeDPoint p = mOriginalPoints[i];
                float x = p.x;
                float y = p.y;
                float z = p.z;
                float newy = (float)(Math.sin(xrot) * z + Math.cos(xrot) * y);
                float newz = (float)(Math.cos(xrot) * z - Math.sin(xrot) * y);

                // rotation around Y-axis
                float newx = (float)(Math.sin(yrot) * newz + Math.cos(yrot) * x);
                newz = (float)(Math.cos(yrot) * newz - Math.sin(yrot) * x);

                // 3D-to-2D projection
                float screenX = newx / (4 - newz / 400);
                float screenY = newy / (4 - newz / 400);

                mRotatedPoints[i].x = screenX;
                mRotatedPoints[i].y = screenY;
                mRotatedPoints[i].z = 0;
            }
        }                
        void rotateAndProjectPointsBottom(float xrot, float yrot) {
            int n = mOriginalPoints.length;
//            xrot-=1.0f;
            for (int i = 0; i < n; i++) {
                // rotation around X-axis
                ThreeDPoint p = mOriginalPoints[i];
                float x = p.x;
                float y = 1-p.y;
                float z = p.z;
                float newy = (float)(Math.sin(xrot) * z + Math.cos(xrot) * y);
                float newz = (float)(Math.cos(xrot) * z - Math.sin(xrot) * y);

                // rotation around Y-axis
                float newx = (float)(Math.sin(yrot) * newz + Math.cos(yrot) * x);
                newz = (float)(Math.cos(yrot) * newz - Math.sin(yrot) * x);

                // 3D-to-2D projection
                float screenX = newx / (4 - newz / 400);
                float screenY = newy / (4 - newz / 400);

                mRotatedPoints[i].x = screenX;
                mRotatedPoints[i].y = screenY;
                mRotatedPoints[i].z = 0;
            }
        }        
        void rotateAndProjectPointsLeft(float xrot, float yrot) {
            int n = mOriginalPoints.length;
            for (int i = 0; i < n; i++) {
                // rotation around X-axis
                ThreeDPoint p = mOriginalPoints[i];
                float y = p.x;
                float x = p.y;
                float z = p.z;
                float newy = (float)(Math.sin(xrot) * z + Math.cos(xrot) * y);
                float newz = (float)(Math.cos(xrot) * z + Math.sin(xrot) * y);

                // rotation around Y-axis
                float newx = (float)(Math.sin(yrot) * newz + Math.cos(yrot) * x);
                newz = (float)(Math.cos(yrot) * newz + Math.sin(yrot) * x);

                // 3D-to-2D projection
                float screenX = newx / (4 - newz / 400);
                float screenY = newy / (4 - newz / 400);

                mRotatedPoints[i].x = screenX;
                mRotatedPoints[i].y = screenY;
                mRotatedPoints[i].z = 0;
            }
        }        
        void rotateAndProjectPointsRight(float xrot, float yrot) {
            int n = mOriginalPoints.length;
            for (int i = 0; i < n; i++) {
                // rotation around X-axis
                ThreeDPoint p = mOriginalPoints[i];
                float y = p.x;
                float x = 1-p.y;
                float z = 1-p.z;
                float newy = (float)(Math.sin(xrot) * z + Math.cos(xrot) * y);
                float newz = (float)(Math.cos(xrot) * z + Math.sin(xrot) * y);

                // rotation around Y-axis
                float newx = (float)(Math.sin(yrot) * newz + Math.cos(yrot) * x);
                newz = (float)(Math.cos(yrot) * newz + Math.sin(yrot) * x);

                // 3D-to-2D projection
                float screenX = newx / (4 - newz / 400);
                float screenY = newy / (4 - newz / 400);

                mRotatedPoints[i].x = screenX;
                mRotatedPoints[i].y = screenY;
                mRotatedPoints[i].z = 0;
            }
        }        
        void rotateAndProjectPoints3(float xrot, float yrot) {
            int n = mOriginalPoints.length;
            for (int i = 0; i < n; i++) {
                // rotation around X-axis
                ThreeDPoint p = mOriginalPoints[i];
                float y = p.x;
                float x = p.y;
                float z = p.z;
                float newy = (float)(Math.sin(xrot) * z + Math.cos(xrot) * y);
                float newz = (float)(Math.cos(xrot) * z + Math.sin(xrot) * y);

                // rotation around Y-axis
                float newx = (float)(Math.sin(yrot) * newz + Math.cos(yrot) * x);
                newz = (float)(Math.cos(yrot) * newz + Math.sin(yrot) * x);

                // 3D-to-2D projection
                float screenX = newx / (4 - newz / 400);
                float screenY = newy / (4 - newz / 400);

                mRotatedPoints[i].x = screenX;
                mRotatedPoints[i].y = screenY;
                mRotatedPoints[i].z = 0;
            }
        }

        void drawLines(Canvas c) {
            int n = mLines.length;
//            Log.d(TAG, "mLines         length = " + mLines.length);
//            Log.d(TAG, "mRotatedPoints length = " + mRotatedPoints.length);
//            Log.d(TAG, "=====================================================");
            for (int i = 0; i < n; i++) {
                ThreeDLine l = mLines[i];
//                Log.d(TAG, " line " + i + " : "  + l.startPoint +"-"+ l.endPoint);
                
                if (l.startPoint < mRotatedPoints.length && l.endPoint < mRotatedPoints.length) 
                {
					ThreeDPoint start = mRotatedPoints[l.startPoint];
					ThreeDPoint end = mRotatedPoints[l.endPoint];//crash ?!?!
					c.drawLine(start.x, start.y, end.x, end.y, mPaint);
				}
            }
        }
        
        void drawTriangles(Canvas c) {
        	//debug points:
        	for(int i = 0; i < mOriginalPoints.length; i++ )
        	{
        		mPaint.setColor(Color.rgb(0, 255, 0));
				c.drawText("p"+i+": (" + mOriginalPoints[i].x +","+ mOriginalPoints[i].y +","+ mOriginalPoints[i].z +")", mOriginalPoints[i].x+10, mOriginalPoints[i].y+10, mPaint);
        	}
        	
        	//debug rotated points:
//        	for(int i = 0; i < mRotatedPoints.length; i++ )
//        	{
//        		mPaint.setColor(Color.rgb(255, 0, 0));
//				c.drawText("r"+i+": (" + mRotatedPoints[i].x +","+ mRotatedPoints[i].y +","+ mRotatedPoints[i].z +")", mRotatedPoints[i].x+10, mRotatedPoints[i].y+10, mPaint);
//        	}
        	
            int numTriangles = mTriangles.length;
//            Log.d(TAG, "mLines         length = " + mLines.length);
//            Log.d(TAG, "mRotatedPoints length = " + mRotatedPoints.length);
//            Log.d(TAG, "=====================================================");
            for (int i = 0; i < numTriangles; i++) {
                ThreeDTriangle thisTriangle = mTriangles[i];
//                Log.d(TAG, " line " + i + " : "  + l.startPoint +"-"+ l.endPoint);
                
                if (thisTriangle.point1 < mRotatedPoints.length && thisTriangle.point2 < mRotatedPoints.length && thisTriangle.point3 < mRotatedPoints.length) 
                {
                	ThreeDPoint point1 = mRotatedPoints[thisTriangle.point1];
					ThreeDPoint point2 = mRotatedPoints[thisTriangle.point2];
					ThreeDPoint point3 = mRotatedPoints[thisTriangle.point3];
//					c.drawLine(start.x, start.y, end.x, end.y, mPaint);
//					c.drawVertices(mode, vertexCount, verts, vertOffset, texs, texOffset, colors, colorOffset, indices, indexOffset, indexCount, paint)
//					  Path path = new Path();



					mPaint.setColor(Color.rgb(255, i*10, i*10));
//					 if(i==1)
//					 {
//							mPaint.setColor(Color.rgb(255, 0, 0));
//							c.drawText("Point 1: " + point1.x +","+point1.y, point1.x+10, point1.y+10, mPaint);
//							mPaint.setColor(Color.rgb(0, 255, 0));
//							c.drawText("Point 2: " + point2.x +","+point2.y, point2.x+10, point2.y+30, mPaint);
//							mPaint.setColor(Color.rgb(0, 0, 255));
//							c.drawText("Point 3: " + point3.x +","+point3.y, point3.x+10, point3.y+50, mPaint);
//					 }
					
//					c.drawLine(point1.x,point1.y,point2.x,point2.y,mPaint);
//					c.drawLine(point2.x,point2.y,point3.x,point3.y,mPaint);
//					c.drawLine(point3.x,point3.y,point1.x,point1.y,mPaint);
					
					Path drawPath = new Path();
					drawPath.moveTo(point1.x,point1.y);
					drawPath.lineTo(point2.x,point2.y);
					drawPath.lineTo(point3.x,point3.y);
					c.drawPath(drawPath, mPaint);
					
//					Log.d(TAG, " triangle " + i + " 1: "  + thisTriangle.point1 +" 2: "  + thisTriangle.point2 +" 3: "  + thisTriangle.point3 );
//					Log.d(TAG, " triangle " + i + " : "  + point1.x +","+ point1.y +" - "+ point2.x +","+ point2.y +" - "+ point3.x +","+ point3.y );
					 
//					  g.drawPath(path, p);
				}
            }
        }

        
//        void drawSquares(Canvas c) {
//            int numSquares = mSquares.length;
////            mPaint.setColor(Color.CYAN);
//          	 
////            Log.d(TAG, "draw suares strt = " + numSquares);
//            for (int i = 0; i < numSquares; i++) {
//                ThreeDSquare thisSquare = mSquares[i];
////                Log.d(TAG, " line " + i + " : "  + l.startPoint +"-"+ l.endPoint);
//                
//                if (thisSquare.point1 < mRotatedPoints.length && thisSquare.point2 < mRotatedPoints.length && thisSquare.point3 < mRotatedPoints.length && thisSquare.point4 < mRotatedPoints.length) 
//                {
////                	Log.d(TAG, "valid = ");
//
////					ThreeDPoint point1 = mRotatedPoints[thisSquare.point1];
////					ThreeDPoint point2 = mRotatedPoints[thisSquare.point2];
////					ThreeDPoint point3 = mRotatedPoints[thisSquare.point3];
////					ThreeDPoint point4 = mRotatedPoints[thisSquare.point4];
//					ThreeDPoint point1 = mRotatedPoints[thisSquare.point1];
//					ThreeDPoint point2 = mRotatedPoints[thisSquare.point2];
//					ThreeDPoint point3 = mRotatedPoints[thisSquare.point3];
//					ThreeDPoint point4 = mRotatedPoints[thisSquare.point4];
//					c.drawLine(point1.x, point1.y, point2.x, point2.y, mPaint);
//					c.drawLine(point2.x, point2.y, point3.x, point3.y, mPaint);
//					c.drawLine(point3.x, point3.y, point4.x, point4.y, mPaint);
//					c.drawLine(point4.x, point4.y, point1.x, point1.y, mPaint);
////					c.drawVertices(mode, vertexCount, verts, vertOffset, texs, texOffset, colors, colorOffset, indices, indexOffset, indexCount, paint)
////					Path drawPath = new Path();
////					  Path path = new Path();
//					  
////					drawPath.moveTo(point1.x,point1.y);
////					drawPath.lineTo(point3.x,point3.y);
////					drawPath.lineTo(point2.x,point2.y);
//					 
//					 
////					  g.drawPath(path, p);
////					c.drawPath(drawPath, mPaint);
//				}
//            }
//        }
        
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
        

        
        void drawTouchPointPulse(Canvas c) {
            for(int i = 0; i <= numberOfRings; i++)
            {// want to do configurable color one day...
                mPaint.setColor(0xffff0000-(0x09000000 * ((i-mPulseN)%numberOfRings) ));
                c.drawCircle(mLastTouchX, mLastTouchY, spacingOfRings * i, mPaint);
            }
// conditional to finish animation.
            if (mPulseN > 0)
            {
            	--mPulseN;
            }
            
            if (mTouchX >=0 && mTouchY >= 0) {         
            	if(mPulseN <= 0)
            		mPulseN = numberOfRings;
            }
        	

        }//pulse


        void drawTouchPointFlare(Canvas c) {

			Random rng = new Random();
//            Log.d(TAG, "Start Flare");
        	//add after flare...viral...crack
//new flare 
            if (mTouchX >=0 && mTouchY >= 0) {
                //max 5 flares
            	if (flareList.size() < MAX_FLARE_COUNT) {
            		//1 second delay
                	if (flareList.size() > 0 ) {
                		if(flareList.get(flareList.size()-1).getTime() > 10)
                		{
                			flareList.add(new FlareData(mTouchX, mTouchY, rng.nextInt(10)-5, 0xDD00FF00, 0xDDFF0000, 0, 30 + rng.nextInt(20), 15 + rng.nextInt(10), 30+rng.nextInt(20)));
                		}
                	}else
                	{
                		flareList.add(new FlareData(mTouchX, mTouchY, rng.nextInt(10)-5, 0xDD0000FF, 0xDDFF0000, 0, 30 + rng.nextInt(20), 15 + rng.nextInt(10), 30+rng.nextInt(20)));
                			
                	}
                }
            }

        	//flare list:   now incorporating move old flare/virs
        		FlareData thisFlare;
				for (Iterator<FlareData> fIterator = flareList.iterator(); fIterator.hasNext();) 
				{
					thisFlare = fIterator.next();

					//move  old flare/virs
					if (thisFlare.getTime() < thisFlare.getStage1Time()) 
					{
						thisFlare.setY((float) (thisFlare.getY()
								+ Math.sin(SystemClock.elapsedRealtime()) - 0.2 * thisFlare.getTime() ));
						thisFlare.setX((float) thisFlare.getX()
								+ (float) Math.sin(SystemClock.elapsedRealtime()) + (( thisFlare.getTilt() * thisFlare.getTime())/80)  );
					} 
//					else {
//					}
//						if (thisFlare.getTime() > thisFlare.getStage1Time()*3) 
//						{
//							//
//						}
						
					thisFlare.setTime(thisFlare.getTime() + 1);

					//render
					if (thisFlare.getTime() < thisFlare.getStage1Time()) {
						//stage 1
//			            Log.d(TAG, "S1");
//						mPaint.setColor(0xFF00FF00);
						mPaint.setColor(0xCCDDDD00);
//						c.drawCircle(thisFlare.getX(), thisFlare.getY(), 3, mPaint);
						c.drawRoundRect(new RectF(thisFlare.getX(), thisFlare.getY(), thisFlare.getX()+1, thisFlare.getY()+5), 0, 0, mPaint);
					} else {
						if (thisFlare.getTime() < thisFlare.getStage1Time() + thisFlare.getStage2Time()) {
							//stage 2
//				            Log.d(TAG, "S2");
							
							if(thisFlare.getExplosionCount() == 0) //new explosion
							{
								thisFlare.setExplosionCount(6 + rng.nextInt(20) );
//								Log.d(TAG, "\n\nFlare count: " + thisFlare.getExplosionCount());

//								for(int i = thisFlare.getExplosionCount(); i > 0; i--)
//								{	
//									Log.d(TAG, "   -flare" + i +": " + thisFlare.getExplosionCount());
//									Log.d(TAG, "        i/count " + (double)i/(double)thisFlare.getExplosionCount() );
//									Log.d(TAG, "        i/count*360 " + (double)((double)i/(double)thisFlare.getExplosionCount())*360.0 );
//									Log.d(TAG, "        rad sin i/count*360 " + (float)((double)thisFlare.getExplosionRadius()+20) * Math.sin((double)((double)i/(double)thisFlare.getExplosionCount())*359.0));
//									Log.d(TAG, "        rad cos i/count*360 " + (float)((double)thisFlare.getExplosionRadius()+20) * Math.cos((double)((double)i/(double)thisFlare.getExplosionCount())*359.0));
//								}
							}
							
							mPaint.setColor(thisFlare.getColor1());
							thisFlare.incrementExplosionRadius();
							thisFlare.incrementExplosionRadius();
							//fade colors
							if(rng.nextInt(4) == 1)
							{
								thisFlare.setColor1(thisFlare.getColor1()-0x01000000);
							}
							//drift down
							if(rng.nextInt(40) == 1)
							{
								thisFlare.setY(thisFlare.getY()+1);
							}
							
							for(int i = thisFlare.getExplosionCount(); i > 0; i--)
							{
								c.drawCircle(
										(float)((double)thisFlare.getExplosionRadius() * (Math.sin((double)((double)i/(double)thisFlare.getExplosionCount())*0.017453293*360)) + thisFlare.getX() + rng.nextInt(2)-4),
										(float)((double)thisFlare.getExplosionRadius() * (Math.cos((double)((double)i/(double)thisFlare.getExplosionCount())*0.017453293*360)) + thisFlare.getY() + rng.nextInt(2)-4),
										1,
										mPaint);
//								c.drawCircle(
//										(float) (thisFlare.getX() + (Math.cos( (i/thisFlare.getExplosionCount())*360 ) * thisFlare.getExplosionRadius())),
//										(float) (thisFlare.getY() + (Math.sin((i/thisFlare.getExplosionCount())*360) * thisFlare.getExplosionRadius())),
//										2,
//										mPaint);
							}
						}else
						{
							if(thisFlare.getTime() < thisFlare.getStage1Time() + thisFlare.getStage2Time() + thisFlare.getStage3Time())
							{
								//stage 3
								mPaint.setColor(thisFlare.getColor2());
								thisFlare.incrementExplosion2Radius();
								
								if(thisFlare.getExplosion2Count() == 0) //new explosion
								{
									thisFlare.setExplosion2Count(4 + rng.nextInt(8) );
	//								Log.d(TAG, "\n\nFlare2 count: " + thisFlare.getExplosionCount());
	
								}

								
								
								for(int i = thisFlare.getExplosionCount(); i > 0; i--)
								{
									for(int j = thisFlare.getExplosion2Count(); j > 0; j--)
									{

										switch(rng.nextInt(5))
										{
											case(0):
												mPaint.setColor(thisFlare.getColor2()-0x2200000);
											
											case(2):

												mPaint.setColor(thisFlare.getColor2()+0x22000000);
											break;
											default:
												mPaint.setColor(thisFlare.getColor2());
												break;
										}
										c.drawPoint(
												(float)( (double)thisFlare.getExplosion2Radius() * (Math.sin((double)((double)j/(double)thisFlare.getExplosion2Count())*0.017453293*360)) +     (double)thisFlare.getExplosionRadius() * (Math.sin((double)((double)i/(double)thisFlare.getExplosionCount())*0.017453293*360)) + thisFlare.getX() + rng.nextInt(2)-4),
												(float)( (double)thisFlare.getExplosion2Radius() * (Math.cos((double)((double)j/(double)thisFlare.getExplosion2Count())*0.017453293*360)) +     (double)thisFlare.getExplosionRadius() * (Math.cos((double)((double)i/(double)thisFlare.getExplosionCount())*0.017453293*360)) + thisFlare.getY() + rng.nextInt(2)-4),
												mPaint);
										
									}
									
									//fade colors
									if(rng.nextInt(5) == 1)
									{
										thisFlare.setColor2(thisFlare.getColor2()-0x01000000);
									}
									
									//drift down
									if(rng.nextInt(40) == 1)
									{
										thisFlare.setY(thisFlare.getY()+1);
									}
								}

								
								
								
							}else
							{
								//expiring:   ... working :)
								fIterator.remove();
								
							}
						}

					}//else
				}//for

        }//flare
        
        void drawStaticTarget(Canvas c) {
                //what about icons??? duhh... removing cursors and centering target
            c.drawBitmap(mCursorImage, mLastTouchX - (mCursorImage.getHeight()/2), mLastTouchY - (mCursorImage.getWidth()/2), mPaint);
        }

        void drawOrbital(Canvas c) {
        	float rotationSpeed = 0.001f;
            float now = SystemClock.elapsedRealtime()*rotationSpeed;

//            int orbitalCount = 3;
//            int orbitalSeperation = 45;
            int ORBIT_6_KNOT = 0;
            int ORBIT_4_KNOT = 1;
            int orbitType = ORBIT_4_KNOT;  

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

//            c.drawCircle( mLastTouchX + (float) ( (2+Math.cos( 3*now ) * Math.cos(2*now)) *100)-200, 
//       		     mLastTouchY + (float) ( (2+Math.cos( 3*now ) * Math.sin(2*now)) *100)-200, 
//       		     5, mPaint);//SystemClock.elapsedRealtime()
//            now -= 50;
//            c.drawCircle( mLastTouchX + (float) ( (2+Math.cos( 3*now ) * Math.cos(2*now)) *100)-200, 
//       		     mLastTouchY + (float) ( (2+Math.cos( 3*now ) * Math.sin(2*now)) *100)-200, 
//       		     5, mPaint);//SystemClock.elapsedRealtime()
    }
        
        
        void drawTouchDisc(Canvas c) {
        	//case: discStyle.... rgb the hard way
            int oldColor = mPaint.getColor();

            int startRings = 48;
            int widthOfRings = 8;
            int widthOfOutline = 1;
            for(int i = startRings-widthOfOutline; i < startRings+widthOfRings+widthOfOutline; i++)
            {
//                mPaint.setColor(0xffff0000-(0x09000000 * ((i-mPulseN)%numberOfRings) ));
//                c.drawCircle(mLastTouchX, mLastTouchY, 8 + i, mPaint);

            	float rotationSpeed = 0.1f;
            	float sweepAngle = 45.00f;
            	if(i <= startRings || i >= startRings+widthOfRings)
            	{//outline
                    mPaint.setARGB(255, 255, 255, 255);
//                	rotationSpeed = 0.05f;
//                	sweepAngle = 45.00f+(2*widthOfOutline);
            	}else
            	{//red
                    mPaint.setARGB(255, 255, 0, 0);
//                    rotationSpeed = 0.1f;
//                	sweepAngle = 45.00f;
                    switch(discStyle)
                    {
                    case 1:
                    	mPaint.setARGB(255, 255, 0, 0);
                    	break;
                    case 2:
                    	mPaint.setARGB(255, 0, 255, 0);
                    	break;
                    case 3:
                    	mPaint.setARGB(255, 0, 0, 255);
                    	break;
                    default:
                    	break;
                    }
            	

            	}
            	boolean useCenter = false;
                //inner.. counter
                c.drawArc(new RectF(mLastTouchX - (i-30),mLastTouchY - (i-30), mLastTouchX + (i-30), mLastTouchY + (i-30)), 360-(rotationSpeed*SystemClock.uptimeMillis()*2%360), sweepAngle*4, useCenter, mPaint);
            	//inner
                c.drawArc(new RectF(mLastTouchX - i,mLastTouchY - i, mLastTouchX + i, mLastTouchY + i), rotationSpeed*SystemClock.uptimeMillis()%360, sweepAngle, useCenter, mPaint);
                //mid
                c.drawArc(new RectF(mLastTouchX - i,mLastTouchY - i, mLastTouchX + i, mLastTouchY + i), (rotationSpeed*SystemClock.uptimeMillis()+180)%360, sweepAngle*2, useCenter, mPaint);
                //outer.. counter
                c.drawArc(new RectF(mLastTouchX - (i+30),mLastTouchY - (i+30), mLastTouchX + (i+30), mLastTouchY + (i+30)), 360-(rotationSpeed*SystemClock.uptimeMillis()*2%360), sweepAngle*4, useCenter, mPaint);
            }
            mPaint.setColor(oldColor);
        }
         

        
        void drawConkey(Canvas c) {
            c.drawColor(0x00000000);

            int oldColor = mPaint.getColor();

            mPaint.setColor(0xffff0000);
            //mPaint.setTypeface(null);
            
            c.drawText("Last touch point: (" + (int)mLastTouchX + "," + (int)mLastTouchX + ")", 		5, 100, mPaint);
            c.drawText("Up: " + SystemClock.uptimeMillis(), 		5, 120, mPaint);
            c.drawText("Now: " + SystemClock.elapsedRealtime(), 		5, 140, mPaint);
            c.drawText("This thread: " + SystemClock.currentThreadTimeMillis(), 		5, 160, mPaint);
//            c.drawText("This is preview: " + this.isPreview(), 		5, 180, mPaint);
//            c.drawText("This is viible: " + this.isVisible(), 		5, 200, mPaint);
//            c.drawText("This is viible: " , 		5, 220, mPaint);
            
//        	c.drawText("color = " + mPaint.getColor(), 		5, 590, mPaint);
//        	c.drawText("mTouchX = " + mTouchX, 		5, 610, mPaint);
//            c.drawText("mTouchY = " + mTouchY, 		5, 630, mPaint);
//            c.drawText("mCenterX1= " + mCenterX1, 5, 690, mPaint);
//            c.drawText("mCenterY1= " + mCenterY1, 5, 710, mPaint);
            c.drawText("flareCount= " + flareList.size(), 5, 210, mPaint);
            
            mPaint.setColor(oldColor);

//          c.drawText("diffX = " + diffX, 5, 650, mPaint);
//          c.drawText("diffY = " + diffY, 5, 670, mPaint); //oops... this should be in conkey if anywhere.
        }
        
    }

//	public static void changeSettings() {
//		if(SHARED_PREFS_NAME != null)
//		{
//			WallpaperService ws = (WallpaperService)TargetLiveWallpaper;
//		}
//		
//	}
    

    static class ThreeDPoint {
        float x;
        float y;
        float z;
    }

    static class ThreeDLine {
        int startPoint;
        int endPoint;
    }
    
    static class ThreeDTriangle{
        int point1;
        int point2;
        int point3;
    }

    static class ThreeDSquare{
        int point1;
        int point2;
        int point3;
        int point4;
    }

    
}
