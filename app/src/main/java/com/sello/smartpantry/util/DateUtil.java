package com.sello.smartpantry.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Date helpers for expiry dates, which are stored as "yyyy-MM-dd" text.
 */
public final class DateUtil {

    private DateUtil() { }

    /**
     * Whole days from today until the given date.
     * Negative means the date has passed; null means the text could not be read.
     */
    public static Integer daysUntil(String isoDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            Date target = format.parse(isoDate);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long diff = target.getTime() - today.getTimeInMillis();
            return (int) Math.round(diff / 86400000.0);
        } catch (ParseException | NullPointerException e) {
            return null;
        }
    }
}
