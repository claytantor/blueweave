package com.dronze.blueweave.util;

import java.text.SimpleDateFormat;
import java.util.*;

public class DateUtils {



    public static final SimpleDateFormat DRONZE_DATE = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    public static final long MILLIS_IN_ONE_DAY = 86400000000l;

    public static Date getDateOffset(Date thisDate, int offset){
        //get eps measures going back one year trailing
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(thisDate);
        calendar.add(Calendar.DAY_OF_YEAR,offset);
        return calendar.getTime();
    }

    public static Date max(Set<Date> dateSet){
        return (Date) new ArrayList(dateSet).stream().max(new Comparator<Date>() {
            @Override
            public int compare(Date o1, Date o2) {
                return o1.compareTo(o2);
            }
        }).get();
    }

    public static Date min(Set<Date> dateSet){
        return (Date) new ArrayList(dateSet).stream().min(new Comparator<Date>() {
            @Override
            public int compare(Date o1, Date o2) {
                return o1.compareTo(o2);
            }
        }).get();
    }




}
