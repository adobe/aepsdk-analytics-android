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

package com.adobe.marketing.mobile.analytics;

import com.adobe.marketing.mobile.util.StringUtils;

class AnalyticsVersionProvider {

    private static String analyticsVersion;
    private static final String FALLBACK_VERSION = "unknown";

    private AnalyticsVersionProvider() {
    }

    /**
     * Sets the Analytics extension version
     * <p>
     * <p>
     * This is set by the platform API layer
     * <p>
     * The format is {@literal  "AND<wrappertype><analyticsversion><coreversion>"}
     *
     * @param version The Analytics Extension version
     */
    static void setVersion(final String version) {
        analyticsVersion = version;
    }


    /**
     * Returns the version string to be used by the {@code AnalyticsExtension}.
     * <p>
     *
     * @return The combined version
     */
    static String getVersion() {
        if (!StringUtils.isNullOrEmpty(analyticsVersion)) {
            return analyticsVersion;
        } else {
            return FALLBACK_VERSION;
        }
    }
}
