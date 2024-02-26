/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 */
package com.adobe.marketing.mobile.analytics.testapp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.adobe.marketing.mobile.AdobeCallbackWithError;
import com.adobe.marketing.mobile.AdobeError;
import com.adobe.marketing.mobile.Analytics;
import com.adobe.marketing.mobile.Assurance;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;

import java.util.ArrayList;
import java.util.List;


public class AnalyticsTrackingReceiver extends BroadcastReceiver {
	private static String TAG = "AnalyticsTrackingReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Intent received, initializing SDK and sending Analytics hit.");

		/// Initialize Mobile SDK
		MobileCore.setLogLevel(LoggingMode.VERBOSE);

		List<Class<? extends Extension>> extensions = new ArrayList<>();
		extensions.add(Lifecycle.EXTENSION);
		extensions.add(Identity.EXTENSION);
		extensions.add(Analytics.EXTENSION);
		extensions.add(Assurance.EXTENSION);
		MobileCore.registerExtensions(extensions, new AdobeCallbackWithError<Object>() {
			@Override
			public void call(Object o) {
				MobileCore.configureWithAppID(AnalyticsTestApp.ENVIRONMENT_FILE_ID);
			}

			@Override
			public void fail(AdobeError adobeError) {
				Log.e("AEPSDKInitError", adobeError.getErrorName());
			}
		});

		// Send Analytics ping
		MobileCore.trackAction("ActionTriggeredByAlarm", null);
	}
}