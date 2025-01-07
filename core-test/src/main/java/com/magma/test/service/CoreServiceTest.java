package com.magma.test.service;

import com.magma.dmsdata.util.Granularity;
import com.magma.util.MagmaTime;
import org.joda.time.DateTime;
import org.junit.Test;

public class CoreServiceTest {

    public static void getGraphDataOfKitByNumber(String from, String to, Granularity granularity) {

        if (to == null && from == null) {
            to = MagmaTime.format(MagmaTime.now());
            from = MagmaTime.format(MagmaTime.now().minusDays(1));
        } else if (to == null) {
            to = from;
            from += " 00:00";
            to += " 23:59";
        } else if (from == null) {
            from = to;
            from += " 00:00";
            to += " 23:59";
        } else {
            from += " 00:00";
            to += " 23:59";
        }


        DateTime fromDateTimeStart = MagmaTime.parse(from);
        DateTime toDateTimeEnd = MagmaTime.parse(to);


        for (int i = 0; i < Integer.MAX_VALUE; i++) {

            DateTime fromDateTime = granularity == Granularity.DAY ? fromDateTimeStart.plusDays(i) : fromDateTimeStart.plusHours(i);
            DateTime toDateTime = granularity == Granularity.DAY ? fromDateTimeStart.plusDays(i + 1) : fromDateTimeStart.plusHours(i + 1);

            DateTime toDateTimeEndTime = granularity == Granularity.DAY ? toDateTimeEnd.plusDays(1) : toDateTimeEnd.plusHours(1);

            if (toDateTime.isAfter(toDateTimeEndTime)) {
                break;
            }

            System.out.println(fromDateTime + " " + toDateTime);

        }


    }

    @Test
    public void getGraphDataOfKitByNumberTest() {

        CoreServiceTest.getGraphDataOfKitByNumber("2023-01-19", "2023-01-25", Granularity.DAY);

    }


}
