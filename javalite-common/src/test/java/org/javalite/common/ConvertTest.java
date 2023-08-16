/*
Copyright 2009-2019 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.javalite.common;

import org.javalite.test.jspec.JSpecSupport;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;


/**
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
public class ConvertTest implements JSpecSupport {

    @Test
    public void shouldCovertToSqlDate() throws ParseException {
        long time = 1416127392928L;
        //long
        Date date = new Date(time);
        Date date1 = Convert.toSqlDate(time);
        a(date).shouldBeEqual(date1);
        a(date1.toString()).shouldBeEqual("2014-11-16");

        //java.util.Date
        java.util.Date utilDate = new java.util.Date(time);
        java.util.Date utilDate1 = Convert.toSqlDate(date);
        a(utilDate).shouldBeEqual(utilDate1);
        a(utilDate1.toString()).shouldBeEqual("2014-11-16");

        //java.sql.Date
        date = new Date(time);
        date1 = Convert.toSqlDate(date);
        a(date).shouldBeEqual(date1);
        a(date1.toString()).shouldBeEqual("2014-11-16");

        //String
        String dateStr = "2001-01-01";
        date = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(dateStr).getTime());
        date1 = Convert.toSqlDate(dateStr);
        a(date).shouldBeEqual(date1);

        //LocalDateTime
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        date = new Date(time);
        date1 = Convert.toSqlDate(ldt);
        a(date).shouldBeEqual(date1);

    }

    @Test
    public void shouldTruncateToSqlDate() {

        //util date
        java.util.Date now = new java.util.Date();
        java.sql.Date today = Convert.truncateToSqlDate(now);
        String string = new java.util.Date(today.getTime()).toString();   //format: Fri Jun 17 12:55:47 CDT 2011
        a(string.contains("00:00:00")).shouldBeTrue();

        //util Timestamp
        Timestamp t = new Timestamp(System.currentTimeMillis());
        today = Convert.truncateToSqlDate(t);
        string = new java.util.Date(today.getTime()).toString();   //format: Fri Jun 17 12:55:47 CDT 2011
        a(string.contains("00:00:00")).shouldBeTrue();

        //util Time
        Time tm = new Time(System.currentTimeMillis());
        today = Convert.truncateToSqlDate(tm);
        string = new java.util.Date(today.getTime()).toString();   //format: Fri Jun 17 12:55:47 CDT 2011
        a(string.contains("00:00:00")).shouldBeTrue();

        //Long
        java.sql.Date date = Convert.truncateToSqlDate(1416127392928L);
        string = new java.util.Date(date.getTime()).toString();
        a(string.contains("00:00:00")).shouldBeTrue();

    }


    @Test
    public void shouldCovertToTimestamp() {

        //string
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Timestamp ts1 = Convert.toTimestamp(ts.toString());
        a(ts).shouldBeEqual(ts1);

        //java.sql.Date
        java.sql.Date date = new Date(System.currentTimeMillis());
        ts1 = new Timestamp(date.getTime());
        ts = Convert.toTimestamp(date);
        a(ts).shouldBeEqual(ts1);

        //java.util.Date
        java.util.Date date1 = new java.util.Date(System.currentTimeMillis());
        ts1 = new Timestamp(date1.getTime());
        ts = Convert.toTimestamp(date1);
        a(ts).shouldBeEqual(ts1);

        //Long
        long time = 1416127392928L;
        ts = Convert.toTimestamp(time);
        ts1 = new Timestamp(time);
        a(ts).shouldBeEqual(ts1);

        //LocalDateTime
        LocalDateTime ldt = LocalDateTime.now();
        ts = Convert.toTimestamp(ldt);
        ts1 = Timestamp.valueOf(ldt);
        a(ts).shouldBeEqual(ts1);
    }

    @Test
    public void shouldCovertToBigDecimal() {

        //integer
        Object object = Convert.toBigDecimal(1);
        the(object).shouldBeA(BigDecimal.class);
        a(object).shouldBeEqual(1);

        //string
        object = Convert.toBigDecimal("1");
        the(object).shouldBeA(BigDecimal.class);
        a(object).shouldBeEqual(1);

        //double
        object = Convert.toBigDecimal(1d);
        the(object).shouldBeA(BigDecimal.class);
        a(object).shouldBeEqual(1);

        //long
        object = Convert.toBigDecimal(1L);
        the(object).shouldBeA(BigDecimal.class);
        a(object).shouldBeEqual(1);
    }

    @Test
    public void shouldCovertToLong() {

        //integer
        Object object = Convert.toLong(1);
        the(object).shouldBeA(Long.class);
        a(object).shouldBeEqual(1);

        //string
        object = Convert.toLong("1");
        the(object).shouldBeA(Long.class);
        a(object).shouldBeEqual(1);

        //double
        object = Convert.toLong(1d);
        the(object).shouldBeA(Long.class);
        a(object).shouldBeEqual(1);

        //BigDecimal
        object = Convert.toLong(new BigDecimal(1));
        the(object).shouldBeA(Long.class);
        a(object).shouldBeEqual(1);

        //java.util.Date
        long time = 1416127392928L;
        Date date = new Date(time);
        object = Convert.toLong(date);
        the(object).shouldBeA(Long.class);
        a(object).shouldBeEqual(time);

        //LocalDateTime
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        object = Convert.toLong(ldt);
        the(object).shouldBeA(Long.class);
        a(object).shouldBeEqual(time);

    }


    @Test
    public void shouldCovertToDouble() {

        //integer
        Object object = Convert.toDouble(1);
        the(object).shouldBeA(Double.class);
        a(object).shouldBeEqual(1);

        //string
        object = Convert.toDouble("1");
        the(object).shouldBeA(Double.class);
        a(object).shouldBeEqual(1);

        //long
        object = Convert.toDouble(1L);
        the(object).shouldBeA(Double.class);
        a(object).shouldBeEqual(1);

        //BigDecimal
        object = Convert.toDouble(new BigDecimal(1));
        the(object).shouldBeA(Double.class);
        a(object).shouldBeEqual(1);
    }

    @Test
    public void shouldCovertToFloat() {

        //float
        Object object = Convert.toFloat(1F);
        the(object).shouldBeA(Float.class);
        a(object).shouldBeEqual(1);

        //string
        object = Convert.toFloat("1");
        the(object).shouldBeA(Float.class);
        a(object).shouldBeEqual(1);

        //long
        object = Convert.toFloat(1L);
        the(object).shouldBeA(Float.class);
        a(object).shouldBeEqual(1);

        //BigDecimal
        object = Convert.toFloat(new BigDecimal(1));
        the(object).shouldBeA(Float.class);
        a(object).shouldBeEqual(1);
    }

    @Test
    public void shouldCovertToShort() {

        //float
        Object object = Convert.toShort(1F);
        the(object).shouldBeA(Short.class);
        a(object).shouldBeEqual(1);

        //string
        object = Convert.toShort("1");
        the(object).shouldBeA(Short.class);
        a(object).shouldBeEqual(1);

        //long
        object = Convert.toShort(1L);
        the(object).shouldBeA(Short.class);
        a(object).shouldBeEqual(1);

        //BigDecimal
        object = Convert.toShort(new BigDecimal(1));
        the(object).shouldBeA(Short.class);
        a(object).shouldBeEqual(1);

    }

    @Test
    public void shouldCovertToBoolean() {

        a(Convert.toBoolean(null)).shouldBeFalse();

        a(Convert.toBoolean("true")).shouldBeTrue();
        a(Convert.toBoolean("false")).shouldBeFalse();

        a(Convert.toBoolean("TRUE")).shouldBeTrue();
        a(Convert.toBoolean("FALSE")).shouldBeFalse();

        a(Convert.toBoolean("True")).shouldBeTrue();
        a(Convert.toBoolean("False")).shouldBeFalse();

        a(Convert.toBoolean("yes")).shouldBeTrue();
        a(Convert.toBoolean("no")).shouldBeFalse();

        a(Convert.toBoolean("YES")).shouldBeTrue();
        a(Convert.toBoolean("NO")).shouldBeFalse();

        a(Convert.toBoolean("Yes")).shouldBeTrue();
        a(Convert.toBoolean("No")).shouldBeFalse();

        a(Convert.toBoolean("t")).shouldBeTrue();
        a(Convert.toBoolean("f")).shouldBeFalse();

        a(Convert.toBoolean("T")).shouldBeTrue();
        a(Convert.toBoolean("F")).shouldBeFalse();

        a(Convert.toBoolean("y")).shouldBeTrue();
        a(Convert.toBoolean("n")).shouldBeFalse();

        a(Convert.toBoolean("Y")).shouldBeTrue();
        a(Convert.toBoolean("N")).shouldBeFalse();

        a(Convert.toBoolean("1")).shouldBeTrue();
        a(Convert.toBoolean("0")).shouldBeFalse();

        a(Convert.toBoolean('t')).shouldBeTrue();
        a(Convert.toBoolean('f')).shouldBeFalse();

        a(Convert.toBoolean('T')).shouldBeTrue();
        a(Convert.toBoolean('F')).shouldBeFalse();

        a(Convert.toBoolean('y')).shouldBeTrue();
        a(Convert.toBoolean('n')).shouldBeFalse();

        a(Convert.toBoolean('Y')).shouldBeTrue();
        a(Convert.toBoolean('N')).shouldBeFalse();

        a(Convert.toBoolean('1')).shouldBeTrue();
        a(Convert.toBoolean('0')).shouldBeFalse();

        a(Convert.toBoolean(1)).shouldBeTrue();
        a(Convert.toBoolean(0)).shouldBeFalse();

        a(Convert.toBoolean(1L)).shouldBeTrue();
        a(Convert.toBoolean(0L)).shouldBeFalse();

        a(Convert.toBoolean(1f)).shouldBeTrue();
        a(Convert.toBoolean(0f)).shouldBeFalse();

        a(Convert.toBoolean(1d)).shouldBeTrue();
        a(Convert.toBoolean(0d)).shouldBeFalse();

        short t = (short)1;
        short f = (short)0;
        a(Convert.toBoolean(t)).shouldBeTrue();
        a(Convert.toBoolean(f)).shouldBeFalse();
    }


    @Test
    public void shouldCovertToIsoString() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(1970, 0, 1, 0, 0, 0);
        the(Convert.toIsoString(cal.getTime())).shouldBeEqual("1970-01-01T00:00:00Z");
        cal.set(1912, 5, 23, 2, 15, 47);
        the(Convert.toIsoString(cal.getTime())).shouldBeEqual("1912-06-23T02:15:47Z");
        cal.set(2014, 11, 31, 23, 59, 59);
        the(Convert.toIsoString(cal.getTime())).shouldBeEqual("2014-12-31T23:59:59Z");
    }

    @Test
    public void shouldToBytesCovertNull() {
        the(Convert.toBytes((Object) null)).shouldBeNull();
    }

    @Test
    public void shouldConvertToLocalDate(){
        java.util.Date d = new java.sql.Date(System.currentTimeMillis());
        LocalDate lDate = Convert.toLocalDate(d);
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();

        the(lDate.getYear()).shouldBeEqual(calendar.get(Calendar.YEAR));
        the(lDate.getMonthValue()).shouldBeEqual(calendar.get(Calendar.MONTH) + 1); // thanks, Java!
        the(lDate.getDayOfMonth()).shouldBeEqual(calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void shouldConvertToLocalDateNull (){
        java.util.Date dateNull = null;
        Object objNull = null;

        the(Convert.toLocalDate(dateNull)).shouldBeNull();
        the(Convert.toLocalDate(objNull)).shouldBeNull();
    }


    @Test
    public void shouldConvertToLocalDateTime(){

        java.util.Date d = new java.util.Date(System.currentTimeMillis());
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();

        //date
        LocalDateTime lTime  = Convert.toLocalDateTime(d);
        testLDT(lTime, calendar);

        //long as object
        lTime  = Convert.toLocalDateTime((Object)d.getTime());
        testLDT(lTime, calendar);

        //String
        lTime  = Convert.toLocalDateTime(lTime.toString());
        testLDT(lTime, calendar);

        //long
        lTime  = Convert.toLocalDateTime(d.getTime());
        testLDT(lTime, calendar);
    }

    @Test
    public void shouldCovertToTime() {

        //Time
        Time time = new Time(System.currentTimeMillis());
        Time time2 = Convert.toTime(time);
        a(time).shouldBeEqual(time2);

        //java.sql.Date
        java.sql.Date date = new Date(System.currentTimeMillis());
        time = new Time(date.getTime());
        time2 = Convert.toTime(date);
        a(time).shouldBeEqual(time2);

        //java.util.Date
        java.util.Date date1 = new java.util.Date(System.currentTimeMillis());
        time = new Time(date.getTime());
        time2 = Convert.toTime(date);
        a(time).shouldBeEqual(time2);

        //Number
        Long number = 1416127392928L;
        time = new Time(number);
        time2 = Convert.toTime(number);
        a(time).shouldBeEqual(time2);

        //LocalDateTime
        long mills = 1416127392928L;
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(mills), ZoneId.systemDefault());
        time = new Time(mills);
        time2 = Convert.toTime(ldt);
        a(time).shouldBeEqual(time2);
    }

    private void testLDT(LocalDateTime localDateTime, Calendar calendar){
        the(localDateTime.getYear()).shouldBeEqual(calendar.get(Calendar.YEAR));
        the(localDateTime.getMonthValue()).shouldBeEqual(calendar.get(Calendar.MONTH) + 1); // thanks, Java!
        the(localDateTime.getDayOfMonth()).shouldBeEqual(calendar.get(Calendar.DAY_OF_MONTH));
        the(localDateTime.getHour()).shouldBeEqual(calendar.get(Calendar.HOUR_OF_DAY));
        the(localDateTime.getMinute()).shouldBeEqual(calendar.get(Calendar.MINUTE));
        the(localDateTime.getSecond()).shouldBeEqual(calendar.get(Calendar.SECOND));
    }

}
