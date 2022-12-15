package com.adobe.analyticstestapp;

import android.app.Application;
import android.util.Log;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Analytics;
import com.adobe.marketing.mobile.Assurance;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;

import java.util.HashMap;
import java.util.List;

public class AnalyticsTestApp extends Application {
	//Remote conf from AnalyticsMultipleRSID on OBU Mobile5
	static final String APP_ID = "";

	@Override
	public void onCreate() {
		super.onCreate();
		MobileCore.setApplication(this);
		MobileCore.setLogLevel(LoggingMode.VERBOSE);
		try {
			Analytics.registerExtension();
			Identity.registerExtension();
			Lifecycle.registerExtension();
			Assurance.registerExtension();
		} catch (Exception e) {
			Log.e("AEPSDKInitError", e.getLocalizedMessage());
		}

		MobileCore.start(new AdobeCallback() {
			@Override
			public void call(Object o) {
				// MobileCore.configureWithAppID(APP_ID);
			}
		});
		//Using assets ADBMobileConfig.json by default
	}
}
