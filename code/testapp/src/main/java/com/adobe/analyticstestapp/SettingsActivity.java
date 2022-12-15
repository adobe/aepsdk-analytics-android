
package com.adobe.analyticstestapp;

import com.adobe.marketing.mobile.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;


public class SettingsActivity extends AppCompatActivity {
	static final String ANALYTICS_CONFIG_BATCH_LIMIT      = "analytics.batchLimit";
	static final String ANALYTICS_CONFIG_SERVER           = "analytics.server";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
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

	public void connectAssurance(final View view) {
		EditText assuranceURL = findViewById(R.id.etAssuranceUrl);
		String url = assuranceURL.getText().toString();
		Assurance.startSession(url);
	}

	public void updateConfig(View view) {
		HashMap<String, Object> config = new HashMap<>();
		EditText serverURLEditText = (EditText) findViewById(R.id.editTextTextServerURL);
		EditText batchLimitEditText = (EditText) findViewById(R.id.editTextBatchLimit);

		String serverURL = serverURLEditText.getText() != null ? serverURLEditText.getText().toString() : "";
		int batchLimit = batchLimitEditText.getText() != null ? (batchLimitEditText.getText().toString().equals("") ? -1 :
						 Integer.parseInt(batchLimitEditText.getText().toString())) : -1;

		if (!serverURL.isEmpty()) {
			config.put(ANALYTICS_CONFIG_SERVER, serverURL);
		}

		if (batchLimit != -1) {
			config.put(ANALYTICS_CONFIG_BATCH_LIMIT, batchLimit);
		}

		if (config.size() > 0) {
			MobileCore.updateConfiguration(config);
		}
	}

	public void persistAid(View view) {
		EditText identifierEditText = (EditText) findViewById(R.id.editTextIdentifier);
		persistIdentifier("ADOBEMOBILE_STOREDDEFAULTS_AID", identifierEditText.getText().toString());
	}

	public void persistVid(View view) {
		EditText identifierEditText = (EditText) findViewById(R.id.editTextIdentifier);
		persistIdentifier("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER", identifierEditText.getText().toString());
	}

	public void backToMain(View view) {
		Intent myIntent = new Intent(SettingsActivity.this, MainActivity.class);
		SettingsActivity.this.startActivity(myIntent);
	}

	private void persistIdentifier(final String key, final String id) {
		SharedPreferences sharedPreferences = getSharedPreferences("AnalyticsDataStorage", 0);
		SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
		sharedPreferencesEditor.putString(key, id);
		sharedPreferencesEditor.apply();
	}
}
