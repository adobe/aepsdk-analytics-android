package com.adobe.analyticstestapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Starts an alarm that runs every 60 seconds, initializing the SDK and sending Analytics pings.
 * This job can run when the app is in background, so use it cautiously and always cancel the alarm after
 * you finished testing or uninstall the app.
 * Can be initialized or canceled from command line:
 * adb shell am broadcast -a com.adobe.analyticstestapp.alarm.INIT -n com.adobe.analyticstestapp/.RepeatAlarmReceiver
 * adb shell am broadcast -a com.adobe.analyticstestapp.alarm.CANCEL -n com.adobe.analyticstestapp/.RepeatAlarmReceiver
 *
 */
public class RepeatAlarmReceiver extends BroadcastReceiver {
	private static String TAG = "RepeatAlarmReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Booted RepeatAlarmReceiver");

		if (intent.getAction() != null)
			if (intent.getAction().contains("alarm.INIT")) {
				initialize(context);
			} else if (intent.getAction().contains("alarm.CANCEL")) {
				cancel(context);
			}
	}

	public static void initialize(Context context) {
		Toast.makeText(context, "Initialized RepeatAlarmReceiver", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "Initialized AnalyticsTrackingReceiver to run every 60 seconds");
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context,
									  AnalyticsTrackingReceiver.class), 0);
		AlarmManager manager = (AlarmManager)(context.getSystemService(Context.ALARM_SERVICE));

		// Repeat every minute
		manager.setRepeating(AlarmManager.RTC_WAKEUP,
							 System.currentTimeMillis(), 60000, pendingIntent);
	}

	public static void cancel(Context context) {
		Toast.makeText(context, "Canceled RepeatAlarmReceiver", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "Canceled RepeatAlarmReceiver");
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent myIntent = new Intent(context, AnalyticsTrackingReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
										  context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager.cancel(pendingIntent);
	}
}