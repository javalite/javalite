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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Calendar;
import java.util.TimeZone;

import static org.javalite.common.Util.*;

/**
 * Convenience class for type conversions.
 *
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
public final class Convert {
    // Calendar and DateFormat are not thread safe: http://www.javacodegeeks.com/2010/07/java-best-practices-dateformat-in.html
    private static final ThreadLocal<Calendar> THREADLOCAL_CAL_UTC = new ThreadLocal<Calendar>() {
        @Override protected Calendar initialValue(){
            return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        }
    };
    private static final ThreadLocal<Calendar> THREADLOCAL_CAL_DEFAULT = new ThreadLocal<Calendar>() {
        @Override protected Calendar initialValue(){
            return Calendar.getInstance();
        }
    };

    private Convert() {
        // not instantiable
    }

    /**
     * Returns string representation of an object, including {@link java.sql.Clob}.
     * For large CLOBs, be careful because this will load an entire CLOB in memory as <code>java.lang.String</code>.
     *
     * @param value value to convert.
     * @return string representation of an object, including {@link java.sql.Clob}.
     */
    public static String toString(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Clob) {
            return clobToString((Clob) value);
        } else {
            return value.toString();
        }
    }

    /*
     * Converts clob to string
     */
    private static String clobToString(Clob clob) {
        Reader r = null;
        StringWriter sw = null;
        try {
            r = clob.getCharacterStream();
            sw = new StringWriter();
            copyStream(r, sw);
            return sw.toString();
        } catch (Exception e) {
            throw new ConversionException(e);
        } finally {
            closeQuietly(sw);
            closeQuietly(r);
        }
    }

    /*
     * Copying stream
     */
    private static void copyStream(Reader r, Writer w) throws IOException {
        char[] buffer = new char[4096];
        for (int n = 0; -1 != (n = r.read(buffer));) {
            w.write(buffer, 0, n);
        }
    }

    /**
     * Returns true if the value is any numeric type and has a value of 1, or
     * if string type has a value of '1', 't', 'y', 'true' or 'yes'. Otherwise, return false.
     *
     * @param value value to convert
     * @return true if the value is any numeric type and has a value of 1, or
     * if string type has a value of '1', 't', 'y', 'true' or 'yes'. Otherwise, return false.
     */
    public static Boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        } else if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof BigDecimal) {
            return value.equals(BigDecimal.ONE);
        } else if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        } else if (value instanceof Character) {
            char c = (Character) value;
            return c == 't' || c == 'T' || c == 'y' || c == 'Y' || c == '1';
        } else {
            String str = value.toString();
            return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("t")
                    || str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("y")
                    || str.equals("1") || Boolean.parseBoolean(str);
        }
    }


    /**
     * Expects a <code>java.sql.Date</code>, <code>java.sql.Timestamp</code>, <code>java.sql.Time</code>, <code>java.util.Date</code>,
     * <code>Long</code> or any object whose toString method has this format: <code>yyyy-mm-dd</code>.
     *
     * @param value argument that is possible to convert to <code>java.sql.Date</code>.
     * @return <code>java.sql.Date</code> instance representing input value.
     */
    public static java.sql.Date toSqlDate(Object value){
        if (value == null) {
            return null;
        } else if (value instanceof java.sql.Date) {
            return (java.sql.Date) value;
        } else if (value instanceof java.util.Date) {
            return new java.sql.Date(((java.util.Date) value).getTime());
        } else if (value instanceof Number) {
            return new java.sql.Date(((Number) value).longValue());
        } else {
            try {
                return java.sql.Date.valueOf(value.toString().trim());
            } catch (IllegalArgumentException e) {
                throw new ConversionException("failed to convert: '" + value + "' to java.sql.Date", e);
            }
        }
    }

    /**
     * Expects a <code>java.sql.Date</code>, <code>java.sql.Timestamp</code>, <code>java.sql.Time</code>, <code>java.util.Date</code>,
     * <code>Long</code> or string with format "yyyy-MM-dd". This method will truncate hours, minutes, seconds and
     * milliseconds to zeros, to conform with JDBC spec:
     * <a href="http://download.oracle.com/javase/6/docs/api/java/sql/Date.html">http://download.oracle.com/javase/6/docs/api/java/sql/Date.html</a>.
     *
     * <p>This method is tread-safe.
     *
     * @param value argument that is possible to convert to <code>java.sql.Date</code>: <code>java.sql.Date</code>,
     * <code>java.sql.Timestamp</code>, <code>java.sql.Time</code>, <code>java.util.Date</code>, <code>Long</code> or any object with toString() == <code>yyyy-mm-dd</code>.
     * @return <code>java.sql.Date</code> instance representing input value.
     */
    public static java.sql.Date truncateToSqlDate(Object value){
        if (value == null) {
            return null;
        } else if (value instanceof java.sql.Date) {
            return (java.sql.Date) value;
        } else if (value instanceof java.util.Date) {
            return truncateToSqlDate(((java.util.Date) value).getTime());
        } else if (value instanceof Number) {
            return truncateToSqlDate(((Number) value).longValue());
        } else {
            try {
                return java.sql.Date.valueOf(value.toString().trim());
            } catch (IllegalArgumentException e) {
                throw new ConversionException("failed to convert: '" + value + "' to java.sql.Date", e);
            }
        }
    }

    /**
     * This method will truncate hours, minutes, seconds and milliseconds to zeros, to conform with JDBC spec:
     * <a href="http://download.oracle.com/javase/6/docs/api/java/sql/Date.html">http://download.oracle.com/javase/6/docs/api/java/sql/Date.html</a>.
     *
     * @param time time in UTC milliseconds from the epoch
     * @return java.sql.Date instance representing time value
     */
    public static java.sql.Date truncateToSqlDate(long time) {
        Calendar calendar = THREADLOCAL_CAL_DEFAULT.get();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new java.sql.Date(calendar.getTimeInMillis());
    }

    /**
     * Converts a <code>java.util.Date</code> to a <code>String</code> in ISO 8601 format: "yyyy-MM-dd'T'HH:mm:ss'Z'"
     * in UTC timezone for timestamps, and "yyyy-MM-dd" for instances of <code>java.sql.Date</code>.
     *
     * <p>This method is tread-safe.
     *
     * @param date date to convert
     * @return String in ISO 8601 format
     */
    public static String toIsoString(java.util.Date date) {
        if (date == null) {
            return null;
        } else if (date instanceof java.sql.Date) {
            return date.toString();
        }
        Calendar cal = THREADLOCAL_CAL_UTC.get();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        char[] buf = new char[20];
        buf[0] = Character.forDigit(year/1000, 10);
        buf[1] = Character.forDigit((year/100)%10, 10);
        buf[2] = Character.forDigit((year/10)%10, 10);
        buf[3] = Character.forDigit(year%10, 10);
        buf[4] = '-';
        buf[5] = Character.forDigit(month/10, 10);
        buf[6] = Character.forDigit(month%10, 10);
        buf[7] = '-';
        buf[8] = Character.forDigit(day/10, 10);
        buf[9] = Character.forDigit(day%10, 10);
        buf[10] = 'T';
        buf[11] = Character.forDigit(hour/10, 10);
        buf[12] = Character.forDigit(hour%10, 10);
        buf[13] = ':';
        buf[14] = Character.forDigit(minute/10, 10);
        buf[15] = Character.forDigit(minute%10, 10);
        buf[16] = ':';
        buf[17] = Character.forDigit(second/10, 10);
        buf[18] = Character.forDigit(second%10, 10);
        buf[19] = 'Z';
        return new String(buf);
    }

    /**
     * Converts any value to <code>Double</code>.
     * @param value value to convert.
     *
     * @return converted double.
     */
    public static Double toDouble(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else {
            try {
                return Double.valueOf(value.toString().trim());
            } catch (NumberFormatException e) {
                throw new ConversionException("failed to convert: '" + value + "' to Double", e);
            }
        }
    }

    /**
     * If the value is instance of java.sql.Time, returns it, else tries to convert java.util.Date or Long to
     * Time, else tries to convert using {@link java.sql.Time#valueOf(String)}.
     * This method might trow <code>IllegalArgumentException</code> if fails at conversion.
     *
     * @see java.sql.Time#valueOf(String)
     * @param value value to convert
     * @return instance of java.sql.Time
     */
    public static java.sql.Time toTime(Object value) {
       if (value == null) {
            return null;
        } else if (value instanceof java.sql.Time) {
            return (java.sql.Time) value;
        } else if (value instanceof java.util.Date) {
           return new java.sql.Time(((java.util.Date) value).getTime());
        } else if (value instanceof Number) { // SQLite returns TIME as Integer
           return new java.sql.Time(((Number) value).longValue());
        } else {
           return java.sql.Time.valueOf(value.toString().trim());
        }
    }

    /**
     * If the value is instance of java.sql.Timestamp, returns it, else tries to convert java.util.Date or Long to
     * Timestamp, else tries to convert using {@link java.sql.Timestamp#valueOf(String)}.
     * This method might trow <code>IllegalArgumentException</code> if fails at conversion.
     *
     * @see java.sql.Timestamp#valueOf(String)
     * @param value value to convert.
     * @return instance of Timestamp.
     */
    public static java.sql.Timestamp toTimestamp(Object value) {
       if (value == null) {
            return null;
        } else if (value instanceof java.sql.Timestamp) {
            return (java.sql.Timestamp) value;
        } else if (value instanceof java.util.Date) {
           return new java.sql.Timestamp(((java.util.Date) value).getTime());
        } else if (value instanceof Number) {
           return new java.sql.Timestamp(((Number) value).longValue());
       } else {
           return java.sql.Timestamp.valueOf(value.toString().trim());
        }
    }

    /**
     * Converts value to Float if it can. If value is a Float, it is returned, if it is a Number, it is
     * promoted to Float and then returned, in all other cases, it converts the value to String,
     * then tries to parse Float from it.
     *
     * @param value value to be converted to Float.
     * @return value converted to Float.
     */
    public static Float toFloat(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof Number) {
            return ((Number) value).floatValue();
        } else {
            return Float.valueOf(value.toString().trim());
        }
    }


    /**
     * Converts value to <code>Long</code> if it can. If value is a <code>Long</code>, it is returned, if it is a <code>Number</code>, it is
     * promoted to <code>Long</code> and then returned, if it is a <code>Date</code>, returns its getTime() value, in all other cases, it converts the value to String,
     * then tries to parse Long from it.
     *
     * @param value value to be converted to Long.
     * @return value converted to Long.
     */
    public static Long toLong(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof java.util.Date) {
            return ((java.util.Date) value).getTime();
        } else {
            try {
                return Long.valueOf(value.toString().trim());
            } catch (NumberFormatException e) {
                throw new ConversionException("failed to convert: '" + value + "' to Long", e);
            }
        }
    }

   /**
     * Converts value to Integer if it can. If value is a Integer, it is returned, if it is a Number, it is
     * promoted to Integer and then returned, in all other cases, it converts the value to String,
     * then tries to parse Integer from it.
     *
     * @param value value to be converted to Integer.
     * @return value converted to Integer.
     */
    public static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            try {
                return Integer.valueOf(value.toString().trim());
            } catch (NumberFormatException e) {
                throw new ConversionException("failed to convert: '" + value + "' to Integer", e);
            }
        }
    }

    /**
     * Converts value to BigDecimal if it can. If value is a BigDecimal, it is returned, if it is a BigDecimal, it is
     * promoted to BigDecimal and then returned, in all other cases, it converts the value to String,
     * then tries to parse BigDecimal from it.
     *
     * @param value value to be converted to Integer.
     * @return value converted to Integer.
     */
    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else {
            try {
                return new BigDecimal(value.toString().trim());
            } catch (NumberFormatException e) {
                throw new ConversionException("failed to convert: '" + value + "' to BigDecimal", e);
            }
        }
    }

    /**
     * Converts value to bytes array if it can. If the value is byte array, it is simply returned,
     * if it is a <code>java.sql.Blob</code>, then data is read from it as bytes.
     * In all other cases the object is converted to <code>String</code>, then bytes are read from it.
     *
     * @param value value to be converted.
     * @return value converted to byte array.
     */
    public static byte[] toBytes(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof byte[]) {
            return (byte[]) value;
        } else if (value instanceof Blob) {
            return toBytes((Blob) value);
        } else {
            return toString(value).getBytes();
        }
    }

    /**
     * Converts <code>java.sql.Blob</code> to bytes array.
     *
     * @param blob Blob to be converted
     * @return blob converted to byte array
     */
    public static byte[] toBytes(Blob blob) {
        InputStream is = null;
        try {
            is = blob.getBinaryStream();
            return bytes(is);
        } catch (Exception e) {
            throw new ConversionException(e);
        } finally {
            closeQuietly(is);
        }
    }

    /**
     * Converts value to Short if it can. If value is a Short, it is returned, if it is a Number, it is
     * promoted to Short and then returned, in all other cases, it converts the value to String,
     * then tries to parse Short from it.
     *
     * @param value value to be converted to Integer.
     * @return value converted to Integer.
     */
    public static Short toShort(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Short) {
            return (Short) value;
        } else if (value instanceof Number) {
            return ((Number) value).shortValue();
        } else {
            try {
                return Short.valueOf(value.toString().trim());
            } catch (NumberFormatException e) {
                throw new ConversionException("failed to convert: '" + value + "' to Short", e);
            }
        }
    }
}
