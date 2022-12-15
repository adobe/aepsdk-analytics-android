package com.adobe.analyticstestapp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Analytics;
import com.adobe.marketing.mobile.Assurance;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;


public class AnalyticsTrackingReceiver extends BroadcastReceiver {
	private static String TAG = "AnalyticsTrackingReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Intent received, initializing SDK and sending Analytics hit.");

		/// Initialize Mobile SDK
		MobileCore.setLogLevel(LoggingMode.VERBOSE);

		try {
			Analytics.registerExtension();
			Identity.registerExtension();
			Lifecycle.registerExtension();
			Assurance.registerExtension();
		} catch (Exception e) {}

		MobileCore.start(new AdobeCallback() {
			@Override
			public void call(Object o) {
				MobileCore.configureWithAppID(AnalyticsTestApp.APP_ID);
			}
		});

		// Send Analytics ping
		MobileCore.trackAction("ActionTriggeredByAlarm", null);
	}
}