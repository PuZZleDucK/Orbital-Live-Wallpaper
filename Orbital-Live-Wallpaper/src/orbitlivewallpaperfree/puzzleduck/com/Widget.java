package orbitlivewallpaperfree.puzzleduck.com;


import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {

			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget);

			// Open alarm clock on click
//			Intent alarmClockIntent = getAlarmClockIntent(context);
//			if (alarmClockIntent != null) {
//				PendingIntent pendingIntent = PendingIntent.getActivity(
//						context, 0, alarmClockIntent, 0);
//				views.setOnClickPendingIntent(R.id.Widget, pendingIntent);
//			}

			AppWidgetManager
					.getInstance(context)
					.updateAppWidget(
							intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS),
							views);
		}
	}

//	private Intent getAlarmClockIntent(Context context) {
//		PackageManager packageManager = context.getPackageManager();
//
//		String clockImpls[][] = {
//				{ "HTC Alarm Clock", "com.htc.android.worldclock",
//						"com.htc.android.worldclock.WorldClockTabControl" },
//				{ "Standar Alarm Clock", "com.android.deskclock",
//						"com.android.deskclock.AlarmClock" },
//				{ "Froyo Nexus Alarm Clock", "com.google.android.deskclock",
//						"com.android.deskclock.DeskClock" },
//				{ "Moto Blur Alarm Clock", "com.motorola.blur.alarmclock",
//						"com.motorola.blur.alarmclock.AlarmClock" },
//				{ "Samsung Galaxy Clock", "com.sec.android.app.clockpackage",
//						"com.sec.android.app.clockpackage.ClockPackage" } };
//
//		for (int i = 0; i < clockImpls.length; i++) {
//			String vendor = clockImpls[i][0];
//			String packageName = clockImpls[i][1];
//			String className = clockImpls[i][2];
//			try {
//				ComponentName cn = new ComponentName(packageName, className);
//				ActivityInfo aInfo = packageManager.getActivityInfo(cn,
//						PackageManager.GET_META_DATA);
//				Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN)
//						.addCategory(Intent.CATEGORY_LAUNCHER);
//				alarmClockIntent.setComponent(cn);
//				return alarmClockIntent;
//			} catch (NameNotFoundException e) {
//			}
//		}
//
//		return null;
//	}
}
