/*
Copyright 2009-2016 Igor Polevoy

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
import java.util.Calendar;
import java.util.TimeZone;


/**
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
public class ConvertTest implements JSpecSupport {

    @Test
    public void shouldCovertToSqlDate() throws ParseException {
        Date d = new Date(new SimpleDateFormat("yyyy-MM-dd").parse("2001-01-01").getTime());
        Date d1 = Convert.toSqlDate("2001-01-01");
        a(d).shouldBeEqual(d1);

        Date date = Convert.toSqlDate(1416127392928L);
        a(date.toString()).shouldBeEqual("2014-11-16");
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
        ts = Convert.toTimestamp(1416127392928L);
        a(ts).shouldBeEqual(new Timestamp(1416127392928L));
    }

    @Test
    public void shouldCovertToBigDecimal() {

        //integer
        Object o = Convert.toBigDecimal(1);
        a(o instanceof BigDecimal).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //string
        o = Convert.toBigDecimal("1");
        a(o instanceof BigDecimal).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //double
        o = Convert.toBigDecimal(1d);
        a(o instanceof BigDecimal).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //long
        o = Convert.toBigDecimal(1L);
        a(o instanceof BigDecimal).shouldBeTrue();
        a(o).shouldBeEqual(1);
    }

    @Test
    public void shouldCovertToLong() {

        //integer
        Object o = Convert.toLong(1);
        a(o instanceof Long).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //string
        o = Convert.toLong("1");
        a(o instanceof Long).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //double
        o = Convert.toLong(1d);
        a(o instanceof Long).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //BigDecimal
        o = Convert.toLong(new BigDecimal(1));
        a(o instanceof Long).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //java.util.Date
        Date date = new Date(1416127392928L);
        o = Convert.toLong(date);
        a(o instanceof Long).shouldBeTrue();
        a(o).shouldBeEqual(1416127392928L);

    }


    @Test
    public void shouldCovertToDouble() {

        //integer
        Object o = Convert.toDouble(1);
        a(o instanceof Double).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //string
        o = Convert.toDouble("1");
        a(o instanceof Double).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //long
        o = Convert.toDouble(1L);
        a(o instanceof Double).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //BigDecimal
        o = Convert.toDouble(new BigDecimal(1));
        a(o instanceof Double).shouldBeTrue();
        a(o).shouldBeEqual(1);
    }

    @Test
    public void shouldCovertToFloat() {

        //float
        Object o = Convert.toFloat(1F);
        a(o instanceof Float).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //string
        o = Convert.toFloat("1");
        a(o instanceof Float).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //long
        o = Convert.toFloat(1L);
        a(o instanceof Float).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //BigDecimal
        o = Convert.toFloat(new BigDecimal(1));
        a(o instanceof Float).shouldBeTrue();
        a(o).shouldBeEqual(1);
    }

    @Test
    public void shouldCovertToShort() {

        //float
        Object o = Convert.toShort(1F);
        a(o instanceof Short).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //string
        o = Convert.toShort("1");
        a(o instanceof Short).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //long
        o = Convert.toShort(1L);
        a(o instanceof Short).shouldBeTrue();
        a(o).shouldBeEqual(1);

        //BigDecimal
        o = Convert.toShort(new BigDecimal(1));
        a(o instanceof Short).shouldBeTrue();
        a(o).shouldBeEqual(1);

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
}
