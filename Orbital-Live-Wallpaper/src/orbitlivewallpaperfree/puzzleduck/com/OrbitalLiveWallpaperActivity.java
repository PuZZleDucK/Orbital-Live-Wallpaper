
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

package orbitlivewallpaperfree.puzzleduck.com;

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
