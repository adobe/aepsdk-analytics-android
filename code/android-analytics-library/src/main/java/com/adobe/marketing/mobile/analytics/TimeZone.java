package com.adobe.marketing.mobile.analytics;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class TimeZone {
    public static final String TIMESTAMP_TIMEZONE_OFFSET;

    static {
        Calendar cal = Calendar.getInstance();
        TIMESTAMP_TIMEZONE_OFFSET = "00/00/0000 00:00:00 0 " + TimeUnit.MILLISECONDS.toMinutes((long) (cal.get(Calendar.ZONE_OFFSET) * -1) - cal.get(Calendar.DST_OFFSET));
    }
}
