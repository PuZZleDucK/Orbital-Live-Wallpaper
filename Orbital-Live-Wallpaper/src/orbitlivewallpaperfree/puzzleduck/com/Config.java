package orbitlivewallpaperfree.puzzleduck.com;
import android.os.*;
import android.app.*;
import android.content.*;
import android.appwidget.*;
import android.widget.*;

import android.view.View.OnClickListener;
import android.view.*;

public class Config extends Activity implements OnClickListener
{
    private int newIdTemp = 0;
	private int runNumber;
	
	public void onClick(View p1)
	{
		// TODOne: Implement this method
		
		//$#ared prefs... check if exist
		SharedPreferences settings = getSharedPreferences ( OrbitalLiveWallpaper.SHARED_PREFS_NAME , 0 );//multi... just in case  .. removing now MODE_MULTI_PROCESS 
		//boolean firstRun = settings.getBoolean("firstRun", true);
		
		//write
		runNumber += 1;
		SharedPreferences.Editor editor = settings . edit ();
		editor.putInt( "runNumber" , runNumber );
        // Commit the edits!
		editor.commit ();
		
		
		
		// i think context is baf
		
		final Context context =	Config.this;
		
		System.out.println("start button");

		AppWidgetManager awMan = AppWidgetManager.getInstance(context);

		RemoteViews rViews = new RemoteViews(this.getPackageName(), R.layout.widget);

		awMan.updateAppWidget(newIdTemp, rViews);

		Intent returnIntent = new Intent();
		returnIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newIdTemp);
		
		
		if(p1.getId() == R.id.cancelbutton)
		{
			setResult(RESULT_CANCELED , returnIntent);
		}else{
			setResult(RESULT_OK, returnIntent);
		}
		
		
		
		System.out.println("post button");
		finish();
	}
	
		@Override
  public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

	  setContentView(R.layout.config);
	  
			//get prefs here

			
	      //$#ared prefs... check if exist
	       SharedPreferences settings = getSharedPreferences ( OrbitalLiveWallpaper.SHARED_PREFS_NAME , 0 );//multi... just in case
	       runNumber = settings.getInt("runNumber", 0);

	       EditText t = (EditText)findViewById(R.id.configtext);
	        t.append("you have run config " + runNumber + " timea");
			
	  
	  newIdTemp = 0;
			
			Intent widgetIntent = getIntent();
			Bundle widgetExtras = widgetIntent.getExtras();
			if(widgetExtras != null)
			{
				newIdTemp = widgetExtras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			}else
			{
				//bail
				finish();
			}
			
			// do config here
		System.out.println("pre button");
			Button b = (Button)findViewById( R.id.okbutton );
	        b.setOnClickListener( this );
	        //duplicate work... should remove one
			findViewById(R.id.cancelbutton).setOnClickListener(this);
			
			
			
			
			
  }

}//class
