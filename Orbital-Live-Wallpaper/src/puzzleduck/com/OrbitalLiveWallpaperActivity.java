package puzzleduck.com;

import android.app.Activity;
import android.os.Bundle;
//pc mod
public class OrbitalLiveWallpaperActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle("Orbital Live Wallpaper");
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
