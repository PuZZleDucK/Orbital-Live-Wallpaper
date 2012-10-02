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
	       SharedPreferences settings = getSharedPreferences ( "meetingWidgetPrefs" , 0 );//multi... just in case
	       runNumber = settings.getInt("runNumber", 0);

	       EditText t = (EditText)findViewById(R.id.configtext);
	//	   t.beginBatchEdit();
	        t.append("you have run config " + runNumber + " timea");
			
	  //t.endBatchEdit();
		//	t.setText("test");
		//	t.append("7est");
		//t.beginBatchEdit()
			
	  
	  
	  
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
			
			// set result to camcel to alliw backout
			
			// do config here
			
		//	Button cancelButton = this.getResources().getr getResourceById();
		//	Button okButton = (Button)
		System.out.println("pre button");
		//	findViewById(R.id.okbutton).setOnClickListener(this);
			Button b = (Button)findViewById( R.id.okbutton );
		//	b.setOnClickListener( (OnClickListener) this);

	        b.setOnClickListener( this );
			
			findViewById(R.id.cancelbutton).setOnClickListener(this);
			//going inner clasd
			
			
			
			
			
  }
	

	
	
	//inner class
//	class ButtonClicker implements Button.OnClickListener
//	{
//		@Override
//		public void onClick
//		(View v) {
//			
//                Toast.
//					makeText(v.getContext(),
//							 "Hello!! button Clicked",
//							 Toast.LENGTH_SHORT).show();
//		
//		}
//		}//inner
	
	

}//class
