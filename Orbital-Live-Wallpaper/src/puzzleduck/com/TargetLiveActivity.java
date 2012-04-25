


package puzzleduck.com;


import java.net.URL;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableRow;



import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;


//This is the "app" part of the LWP
public class TargetLiveActivity extends Activity implements OnSharedPreferenceChangeListener   {
    public static final String SHARED_PREFS_NAME="target_lwp_settings";


    private static final String TAG = "TargetLiveActivity";


	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.dispatchTouchEvent(ev);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
//		this.getApplicationContext().setWallpaper(data)
		
	
		this.setContentView(R.layout.activity);

//		Button b = (Button) findViewById(R.id.button1);

//		Preference p = new Preference(this);
//		PreferenceManager pm = p.getPreferenceManager();
//		PreferenceManager pm = this.getPreferenceManager();
//		getApplicationContext().startActivity();
//		PreferenceManager pm = ((PreferenceActivity) getApplicationContext()).getPreferenceManager();

		
//		pm.setSharedPreferencesName(TargetLiveWallpaper.SHARED_PREFS_NAME);
//		pm.setSharedPreferencesName("target_lwp_settings");
//		pm.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		
		
        final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	

//change prefs
//                SharedPreferences mPrefs;
//                mPrefs = this.getSharedPreferences(SHARED_PREFS_NAME, 0);
//                mPrefs = TargetLiveActivity.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
//                mPrefs.registerOnSharedPreferenceChangeListener(this);
//                onSharedPreferenceChanged(mPrefs, null);
//                SharedPreferences.Editor tempEd = mPrefs.edit();
//                tempEd.putBoolean("target_quad_on", true);
//                tempEd.commit();
//                mPrefs.registerOnSharedPreferenceChangeListener(listener)
//                TargetLiveWallpaper.class.
//                TargetLiveActivity.this.getApplication().;
//                TargetLiveActivity.this.getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_WALLPAPER_CHANGED));
                
                //find pdi
            	Intent intent = new Intent(Intent.ACTION_VIEW);
            	intent.setData(Uri.parse("market://search?q=PuZZleDucK Industries"));
            	startActivity(intent);
                
            }
        });

        
        final Button selectButton = (Button) findViewById(R.id.lwpSelectButton);
        selectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	

//launch chooser      
                Intent intent = new Intent();
                intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
            	startActivity(intent);
                
            }
        });

        
        
        final Button defaultButton = (Button) findViewById(R.id.defaultSettingsButton);
        defaultButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	
				Log.d(TAG, "\n\n    BUTTON PRESS"  );

//change prefs
                SharedPreferences mPrefs;
////                mPrefs = this.getSharedPreferences(SHARED_PREFS_NAME, 0);
                mPrefs = TargetLiveActivity.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
//                mPrefs.registerOnSharedPreferenceChangeListener((OnSharedPreferenceChangeListener) TargetLiveActivity.this);
//                onSharedPreferenceChanged(mPrefs, null);
                SharedPreferences.Editor tempEd = mPrefs.edit();
                tempEd.putBoolean("target_quad_on", true);
                tempEd.commit();
                
//                mPrefs.registerOnSharedPreferenceChangeListener(TargetLiveWallpaper.TargetEngine.class.newInstance());
//                mPrefs.registerOnSharedPreferenceChangeListener(listener)
//                TargetLiveWallpaper.class.
//                TargetLiveActivity.this.getApplication().;
                TargetLiveActivity.this.getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_WALLPAPER_CHANGED));
                
//                WallpaperManager wm = WallpaperManager.getInstance(TargetLiveActivity.this);
//                wm.setBitmap(bitmap)
                
//                WallpaperService ws = (WallpaperService)TargetLiveWallpaper;
                
                
                Intent intent = new Intent();
                intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
            	startActivityForResult(intent, 5555);
            	finishActivity(5555);
            }
        });

        
//        presetButton1


        final ImageButton presetButton1debian = (ImageButton) findViewById(R.id.presetButton1);
        presetButton1debian.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	
				Log.d(TAG, "\n\n   presetButton1 BUTTON PRESS"  );

//change prefs
                SharedPreferences mPrefs;
////                mPrefs = this.getSharedPreferences(SHARED_PREFS_NAME, 0);
                mPrefs = TargetLiveActivity.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
//                mPrefs.registerOnSharedPreferenceChangeListener((OnSharedPreferenceChangeListener) TargetLiveActivity.this);
//                onSharedPreferenceChanged(mPrefs, null);
                SharedPreferences.Editor tempEd = mPrefs.edit();
//                tempEd.putBoolean("target_quad_on", true);
                
                tempEd.putBoolean("target_disc_on", false);
//                tempEd.putInt("target_disc_type", 1);
                
                tempEd.commit();

                Intent intent = new Intent(Intent.ACTION_WALLPAPER_CHANGED );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	    
                startActivity(intent);
//                TargetLiveWallpaper.class;
//                WallpaperService.this.onCreate();
//                TargetLiveWallpaper.Engine();
//                WallpaperService.Engine();
          
            }
        });
		

        final ImageButton prese2Button1debian = (ImageButton) findViewById(R.id.presetButton1);
        prese2Button1debian.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	
				Log.d(TAG, "\n\n   presetButton1 BUTTON PRESS"  );

//change prefs
                SharedPreferences mPrefs;
////                mPrefs = this.getSharedPreferences(SHARED_PREFS_NAME, 0);
                mPrefs = TargetLiveActivity.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
//                mPrefs.registerOnSharedPreferenceChangeListener((OnSharedPreferenceChangeListener) TargetLiveActivity.this);
//                onSharedPreferenceChanged(mPrefs, null);
                SharedPreferences.Editor tempEd = mPrefs.edit();
//                tempEd.putBoolean("target_quad_on", true);
                
                tempEd.putBoolean("target_disc_on", true);
//                tempEd.putInt("target_disc_type", 1);
                
                tempEd.commit();

                
                Intent intent = new Intent( );
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                Intent i = new Intent();
                intent.setAction(Intent.ACTION_WALLPAPER_CHANGED);
                sendBroadcast(intent);
                
//                startActivity(intent);
//                TargetLiveWallpaper.class;
//                WallpaperService.this.onCreate();
//                TargetLiveWallpaper.Engine();
//                WallpaperService.Engine();
          
            }
        });
		
		
//		WebView thisWebView = (WebView)findViewById(R.id.webView1);
//		thisWebView.getSettings().setJavaScriptEnabled(true);
//	    thisWebView.loadUrl("www.google.com.au/search?q=%22target+live+wallpaper%22+%22puzzleduck+Industries%22");
//		thisWebView.reload();
		
	}//onCreate

	//     * Invoked when the Activity loses user focus.
	@Override
	protected void onPause() {
		super.onPause();
		//        mLunarView.getThread().pause(); // pause game when Activity pauses
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}


	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		return super.onMenuItemSelected(featureId, item);
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		

//		this.setContentView(R.layout.activity);

//		WebView thisWebView = (WebView)findViewById(R.id.webView1);
////		thisWebView.reload();		
//	    thisWebView.loadUrl("www.google.com.au/search?q=%22target+live+wallpaper%22+%22puzzleduck+Industries%22");

		
		return super.onTouchEvent(event);


	
	}


	@Override
	public Drawable peekWallpaper() {
		// TODO Auto-generated method stub
		return super.peekWallpaper();
	}


	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		
	}

    
}//class    
