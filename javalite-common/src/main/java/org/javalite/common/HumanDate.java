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


import java.util.Date;



/**
 * This is a helper class for converting distance bewteen two dates to a human format, like:
 * "two days", "three months", etc.
 * <p></p>
 * It was inspired by a Rails helper method:
 * <a href="http://api.rubyonrails.org/classes/ActionView/Helpers/DateHelper.html#M001006">distance_of_time_in_words</a>
 *
 * @author Igor Polevoy
 */
public final class HumanDate
{
    // private constructor because this is a utility class
    private HumanDate(){}


    /**
     * Generates a human representation of distance in time between two time stamps. This could take a form: "less than
     * a minute", or "about a year".
     *
     * @param fromTime start timestamp. This is a representation of time in milliseconds from January 1 1970.
     * @param toTime end timestamp. This is a representation of time in milliseconds from January 1 1970.
     * @return human representation if distance in time between <code>fromTime</code> and <code>toTime</code>.
     *
     */
    public static String toHumanFormat(long fromTime, long toTime)
    {
        if(! (toTime >= fromTime))
        {
            throw new IllegalArgumentException("toTime must be >= fromTime");
        }

        long distanceInSeconds = (toTime - fromTime) / 1000;
        long distanceInMinutes = distanceInSeconds / 60;

        if (distanceInMinutes == 0) { return "less than a minute"; }
        if (distanceInMinutes == 1) { return "a minute"; }
        if(inRange(2, 44, distanceInMinutes)) { return distanceInMinutes + " minutes"; }
        if(inRange(45, 89, distanceInMinutes)) { return "about 1 hour"; }
        if(inRange(90, 1439, distanceInMinutes)) { return "about " + Math.round(distanceInMinutes/60f) + " hours" ; }
        if(inRange(1440, 2879, distanceInMinutes)) { return "1 day"; }
        if(inRange(2880, 43199, distanceInMinutes)) { return Math.round(distanceInMinutes/1440f) + " days"; }
        if(inRange(43200, 86399, distanceInMinutes)) { return "about 1 month"; }
        if(inRange(86400, 525599, distanceInMinutes)) { return Math.round(distanceInMinutes/43200f) + " months"; }
        if(inRange(525600, 1051199, distanceInMinutes)) { return "about 1 year"; }

        return "about " + Math.round(distanceInMinutes/525600f) + " years";
    }

    /**
     * This is a convenience method in addition to {@link #toHumanFormat(long, long)}, except the second parameter is
     * always now.
     *
     * @param fromTime start date. This is a representation of time in milliseconds from January 1 1970.
     * @return human imprecise representation of time difference between the <code>fromTime</code> and now.
     */
    public static String toHumanFormat(long fromTime)
    {
        return toHumanFormat(fromTime, new Date().getTime());
    }


    /**
     * Returns <code>true</code> if the <code>val</code> is between <code>min</code> and <code>max</code>,
     * inclusively. Otherwise returns <code>false</code>. This is implemented because Java does not have native
     * support for ranges.
     *
     * @param min minimum range boundary
     * @param max maximum range boundary
     * @param val value in question.
     * @return <code>true</code> if the <code>val</code> is between <code>min</code> and <code>max</code>,
     * inclusively. Otherwise returns <code>false</code>.
     */
    private static boolean inRange(long min, long max, long val)
    {
        return val >= min && val <= max;
    }

}
