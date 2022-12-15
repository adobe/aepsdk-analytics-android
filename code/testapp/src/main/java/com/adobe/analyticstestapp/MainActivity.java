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

	public void setIdentifier(View view) {
		Analytics.setVisitorIdentifier("test_identifier");
	}

	public void trackQueue(View view) {
		Analytics.getQueueSize(new AdobeCallback<Long>() {
			@Override
			public void call(final Long queueSize) {
			}
		});
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

	public void getVisitorIdentifier(View view) {
		Analytics.getVisitorIdentifier(new AdobeCallback<String>() {
			@Override
			public void call(String s) {
				Log.i("Main", "getVisitorIdentifier (VID): " + s);
			}
		});
	}

	public void getTrackingIdentifier(View view) {
		Analytics.getTrackingIdentifier(new AdobeCallback<String>() {
			@Override
			public void call(String s) {
				Log.i("Main", "getTrackingIdentifier (AID): " + s);
			}
		});
	}

	public void openConfigurationPage(View view) {
		Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
		MainActivity.this.startActivity(myIntent);
	}
}


