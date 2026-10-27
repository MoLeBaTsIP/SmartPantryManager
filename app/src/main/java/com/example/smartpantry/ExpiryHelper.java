package com.example.smartpantry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Small helpers for the optional "yyyy-MM-dd" expiry date. No Android dependencies. */
public final class ExpiryHelper {

    /** Items expiring within this many days are flagged as "expiring soon". */
    public static final int SOON_DAYS = 3;

    private ExpiryHelper() { }

    private static SimpleDateFormat newFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false); // reject impossible dates such as 2026-02-31
        return format;
    }

    /** Parses "yyyy-MM-dd"; returns null when blank or not a real date. */
    public static Date parse(String text) {
        if (text == null) {
            return null;
        }
        String value = text.trim();
        if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return null;
        }
        try {
            return newFormat().parse(value);
        } catch (ParseException e) {
            return null;
        }
    }

    public static boolean isValidDate(String text) {
        return parse(text) != null;
    }

    /** Whole days from today until the expiry date (negative = already expired), or null. */
    public static Integer daysUntil(String expiryText, Date today) {
        Date expiry = parse(expiryText);
        if (expiry == null || today == null) {
            return null;
        }
        long diffMillis = startOfDay(expiry) - startOfDay(today);
        return (int) Math.round(diffMillis / 86400000.0);
    }

    public static boolean isExpired(String expiryText, Date today) {
        Integer days = daysUntil(expiryText, today);
        return days != null && days < 0;
    }

    /** True when the item is not yet expired but expires within SOON_DAYS days. */
    public static boolean isExpiringSoon(String expiryText, Date today) {
        Integer days = daysUntil(expiryText, today);
        return days != null && days >= 0 && days <= SOON_DAYS;
    }

    private static long startOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
