package org.javalite.activejdbc;

import org.javalite.common.Convert;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalField;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

public class DateUtil {

    /**
     * @param dateString expected format: 2020-12-31
     * @return long time
     */
    public static Date toDate(String dateString){
        StringTokenizer st = new StringTokenizer(dateString, "-");
        int year = Convert.toInteger(st.nextToken());
        int month = Convert.toInteger(st.nextToken());
        int day = Convert.toInteger(st.nextToken());
        GregorianCalendar calendar = new GregorianCalendar(year, month - 1, day);
        return new Date((calendar.getTime().getTime()));
    }

    public static void main(String[] args) {
        System.out.println(toDate("1962-06-13"));
    }
}
