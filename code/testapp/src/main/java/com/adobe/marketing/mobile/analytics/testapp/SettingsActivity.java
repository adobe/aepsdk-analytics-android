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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.adobe.marketing.mobile.Assurance;
import com.adobe.marketing.mobile.MobileCore;
import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {
    static final String ANALYTICS_CONFIG_BATCH_LIMIT = "analytics.batchLimit";
    static final String ANALYTICS_CONFIG_SERVER = "analytics.server";

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

        String serverURL =
                serverURLEditText.getText() != null ? serverURLEditText.getText().toString() : "";
        int batchLimit =
                batchLimitEditText.getText() != null
                        ? (batchLimitEditText.getText().toString().equals("")
                                ? -1
                                : Integer.parseInt(batchLimitEditText.getText().toString()))
                        : -1;

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
        persistIdentifier(
                "ADOBEMOBILE_STOREDDEFAULTS_AID", identifierEditText.getText().toString());
    }

    public void persistVid(View view) {
        EditText identifierEditText = (EditText) findViewById(R.id.editTextIdentifier);
        persistIdentifier(
                "ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER",
                identifierEditText.getText().toString());
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
