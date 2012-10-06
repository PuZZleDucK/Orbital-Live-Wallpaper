package orbitlivewallpaperfree.puzzleduck.com;
import android.appwidget.*;
import android.content.*;
import android.widget.*;
import android.app.*;
import android.util.*;

public class WidgetUpdater extends AppWidgetProvider
{
	private static int uCount = 0;

  @Override
  public void onUpdate(Context c, AppWidgetManager awMan, int[] widgetIds)
  {
	  Log.d("meeting","update");
	  ComponentName widgetTypeName = new ComponentName(c, WidgetUpdater.class);
	  int[] meetingWidgetIds = awMan.getAppWidgetIds(widgetTypeName);
	  
	  for(int thisWidget : meetingWidgetIds)
	  {
		 // Log.d("meeting","for");
		  
		  RemoteViews rView = new RemoteViews(c.getPackageName(), R.layout.widget);
		 //do stuff with the view... set image for example? huh?


		 
		  
		  
		  
		  
		  
		  
		  
		  uCount += 1;
		  
		  
		  
		  
		  
		  Intent myIntent = new Intent(c, this.getClass());
		  myIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		  myIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, meetingWidgetIds);
		 // myIntent.setFlags();
		// AppWidgetManager.EXTRA_CUSTOM_EXTRAS
		  
		  PendingIntent pIntent = PendingIntent.getBroadcast(c, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		  
		  //set button handlers if needed :D hint hint ;D
//		  rView.setOnClickPendingIntent(R.id.updateText, pIntent);
		  
		  awMan.updateAppWidget(thisWidget, rView);
		  
		  
		  
	  }
	  
	  
	  
	  
	  
	  
	  
	  
//	  Log.d("meeting","end");
	  
	  
	  
	  
	  
  }


}
