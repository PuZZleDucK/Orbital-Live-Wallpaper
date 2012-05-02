
/*
 * Orbital Live Wallpaper
 *
 * Copyright (C) 2012 PuZZleDucK (PuZZleDucK@gmail.com)
 * 
 *	This program is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License version
 *	2 as published by the Free Software Foundation.
 *
 * This live wallpaper was originally based on the target live wallpaper by PuZZleDucK
 *
 */

package orbitlivewallpaper.puzzleduck.com;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
//pc mod
public class OrbitalLiveWallpaperActivity extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        findViewById(R.id.appsButton).setOnClickListener( (OnClickListener) this);
        findViewById(R.id.dropboxButton).setOnClickListener( (OnClickListener) this);
        findViewById(R.id.githubButton).setOnClickListener( (OnClickListener) this);
        findViewById(R.id.contactDevButton).setOnClickListener( (OnClickListener) this);
        
    }
    
    

	@Override
	public void onClick(View v) {
		
		
		
		

        if(v.getId() == R.id.contactDevButton)
        {
          Intent intent = new Intent(Intent.ACTION_SEND);
          intent.setType("plain/text");
          intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"puzzleduck+orbitalLWP@gmail.com"});
          intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "User feedback for Orbital Live Wallpaper: ");
          intent = Intent.createChooser(intent, "Thank you for your feedback, please select an app:");
        	startActivity(intent);
        }//if contact button

        
        if(v.getId() == R.id.appsButton)
        {
        	Intent intent = new Intent(Intent.ACTION_VIEW);
        	intent.setData(Uri.parse("market://search?q=PuZZleDucK Industries"));
        	startActivity(intent);
        }//if apps button

        if(v.getId() == R.id.dropboxButton)
        {
        	Intent intent = new Intent(Intent.ACTION_VIEW);
        	intent.setData(Uri.parse("http://db.tt/41Y5NAS"));
        	startActivity(intent);
        }//if apps button
        
        if(v.getId() == R.id.githubButton)
        {
        	Intent intent = new Intent(Intent.ACTION_VIEW);
        	intent.setData(Uri.parse("https://github.com/PuZZleDucK/Orbital-Live-Wallpaper"));
        	startActivity(intent);
        }//if apps button
        
        
        
        
		
	}
    
    
}


//target LWP manifest:


//<manifest
//    android:versionName="2" 
//    package="com.puzzleduck.targetLiveWallpaper" 
//    android:versionCode="2" 
//    xmlns:android="http://schemas.android.com/apk/res/android">
//    <uses-sdk android:targetSdkVersion="9" 
//        android:minSdkVersion="7"/>
//    <uses-feature android:name="android.software.live_wallpaper" />
//
//    
//    
//    <application
//        android:label="@string/target_wallpaper_application_label"
//        android:icon="@drawable/target_lwp_icon" 
//        android:debuggable="false"
//        android:enabled="true">
//   		        
//        
//        <activity android:label="@string/target_wallpaper_activity_label" 
//        	android:name=".TargetLiveActivity" 
//        	android:icon="@drawable/target_lwp_icon"
//        	 android:enabled="true"
//        	 android:exported="true"
//        	 android:process="com.puzzleduck.targetLiveWallpaper.TargetLiveActivity">
//            <intent-filter>
//                <action android:name="android.intent.action.MAIN" />
//                <category android:name="android.intent.category.LAUNCHER" />
//            </intent-filter>
//    
//        </activity>
//
//        <service
//            android:label="@string/target_wallpaper_service_label"
//            android:permission="android.permission.BIND_WALLPAPER"
//            android:name="TargetLiveWallpaper">
//            <intent-filter>
//                <action android:name="android.service.wallpaper.WallpaperService" />
//            </intent-filter>
//            <meta-data android:name="android.service.wallpaper" android:resource="@xml/target_lwp" />
//        </service>
//        <activity
//            android:label="@string/target_settings"
//            android:name="TargetLiveWallpaperSettings"
//            android:theme="@android:style/Theme.Light.WallpaperSettings"
//            android:exported="true"
//            android:layout="@layout/activity">
//        </activity>
//        
//        
//
//    </application>
//
//</manifest>