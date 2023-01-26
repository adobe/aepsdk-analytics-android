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

package com.adobe.marketing.mobile.analytics.internal

import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.SharedStateResolution

internal typealias SharedStateMonitor = (sharedState: Map<String, Any>) -> Unit

internal class MonitorExtension(extensionApi: ExtensionApi) : Extension(extensionApi) {
    companion object {
        private var configurationMonitor: SharedStateMonitor? = null
        private var identityMonitor: SharedStateMonitor? = null
        internal fun configurationAwareness(callback: SharedStateMonitor) {
            configurationMonitor = callback
        }

        internal fun identityAwareness(callback: SharedStateMonitor) {
            identityMonitor = callback
        }
    }

    override fun getName(): String {
        return "MonitorExtension"
    }

    override fun onRegistered() {
        api.registerEventListener(EventType.WILDCARD, EventSource.WILDCARD) { event ->
            val configuration = api.getSharedState(
                "com.adobe.module.configuration",
                event,
                false,
                SharedStateResolution.LAST_SET
            )?.value
            val identity = api.getSharedState(
                "com.adobe.module.identity",
                event,
                false,
                SharedStateResolution.LAST_SET
            )?.value
            configuration?.let {
                configurationMonitor?.let { it(configuration) }
            }
            identity?.let {
                identityMonitor?.let { it(identity) }
            }
        }
    }
}
