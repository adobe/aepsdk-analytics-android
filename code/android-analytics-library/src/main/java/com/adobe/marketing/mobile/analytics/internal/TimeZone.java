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
package com.adobe.marketing.mobile.analytics.internal;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

class TimeZone {
    /**
     * Retrieves a correctly-formatted timestamp string; this function returns an all 0 string except for the timezoneOffset
     * backend platform only processes timezone offset from this string and it is wasted cycles to provide the rest of the data.
     */
    public static final String TIMESTAMP_TIMEZONE_OFFSET;

    static {
        Calendar cal = Calendar.getInstance();
        TIMESTAMP_TIMEZONE_OFFSET = "00/00/0000 00:00:00 0 " + TimeUnit.MILLISECONDS.toMinutes((long) (cal.get(Calendar.ZONE_OFFSET) * -1) - cal.get(Calendar.DST_OFFSET));
    }
}
