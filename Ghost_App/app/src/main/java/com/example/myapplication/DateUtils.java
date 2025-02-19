package com.example.myapplication;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    public static String getTimeAgo(Date date) {

        if (date == null) {
            return "Bilinmeyen tarih"; // Tarih null ise
        }
        long currentTime = System.currentTimeMillis();
        long postTime = date.getTime();
        long diffInMillis = currentTime - postTime;

        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
        long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

        if (diffInMinutes < 60) {
            return diffInMinutes + " dk önce";
        } else if (diffInHours < 24) {
            return diffInHours + " saat önce";
        } else if (diffInDays < 7) {
            return diffInDays + " gün önce";
        } else {
            long diffInWeeks = diffInDays / 7;
            return diffInWeeks + " hafta önce";
        }
    }
}