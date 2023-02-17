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
package com.adobe.analyticstestapp;

import com.adobe.marketing.mobile.Analytics;
import com.adobe.marketing.mobile.*;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobileCore.setApplication(getApplication());
		MobileCore.lifecycleStart(null);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobileCore.lifecyclePause();
	}

	public void trackAction(View view) {
		Map<String, String> additionalContextData = new HashMap<String, String>();
		additionalContextData.put("product", "pokemon");
		MobileCore.trackAction("myaction", additionalContextData);
	}

	public void trackState(View view) {
		Map<String, String> additionalContextData = new HashMap<String, String>();
		additionalContextData.put("mystateKey", "stateValue");
		MobileCore.trackState("mystate", additionalContextData);
	}

	public void getQueueSize(View view) {
		Analytics.getQueueSize(queueSize -> { Log.i(TAG, "getQueueSize: " + queueSize); });
	}

	public void sendQueueHit(View view) {
		Analytics.sendQueuedHits();
	}

	public void clearQueue(View view) {
		Analytics.clearQueue();
	}

	public void optIn(View view) {
		MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_IN);
	}

	public void optOut(View view) {
		MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT);
	}

	public void optUnknown(View view) {
		MobileCore.setPrivacyStatus(MobilePrivacyStatus.UNKNOWN);
	}

	public void resetIdentities(View view) {
		MobileCore.resetIdentities();
	}

	public void setVisitorIdentifier(View view) {
		Analytics.setVisitorIdentifier("test_identifier");
	}

	public void getVisitorIdentifier(View view) {
		Analytics.getVisitorIdentifier(s -> Log.i(TAG, "getVisitorIdentifier (VID): " + s));
	}

	public void getTrackingIdentifier(View view) {
		Analytics.getTrackingIdentifier(s -> Log.i(TAG, "getTrackingIdentifier (AID): " + s));
	}

	public void syncCustomIdentifiers(View view) {
		Map<String, String> customIds = new HashMap<>();
		customIds.put("id1", "test1");
		customIds.put("id2", "test2");
		Identity.syncIdentifiers(customIds, VisitorID.AuthenticationState.AUTHENTICATED);
	}

	public void openConfigurationPage(View view) {
		Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
		MainActivity.this.startActivity(myIntent);
	}
}


