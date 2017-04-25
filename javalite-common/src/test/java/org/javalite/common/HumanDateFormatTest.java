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


import junit.framework.TestCase;

import java.util.GregorianCalendar;


public class HumanDateFormatTest extends TestCase
{

    public void testSecondsMinutesDays()
    {
        try
        {
            HumanDate.toHumanFormat(2, 1);
        }
        catch(IllegalArgumentException goodException){/*we expect IllegalArgumentException here*/}

        //test days
        long fromTime = new GregorianCalendar(1978, 0, 12).getTime().getTime();
        long toTime = new GregorianCalendar(1978, 0, 14).getTime().getTime();
        assertEquals("2 days", HumanDate.toHumanFormat(fromTime, toTime));

        //test seconds
        fromTime = new GregorianCalendar(1970, 0, 12, 12, 22, 23).getTime().getTime();
        toTime   = new GregorianCalendar(1970, 0, 12, 12, 22, 25).getTime().getTime();
        assertEquals("less than a minute", HumanDate.toHumanFormat(fromTime, toTime));

        //test one minute
        fromTime = new GregorianCalendar(1970, 0, 12, 12, 22, 0).getTime().getTime();
        toTime   = new GregorianCalendar(1970, 0, 12, 12, 23, 0).getTime().getTime();
        assertEquals("a minute", HumanDate.toHumanFormat(fromTime, toTime));

        //test minutes
        fromTime = new GregorianCalendar(1970, 0, 12, 12, 2, 0).getTime().getTime();
        toTime   = new GregorianCalendar(1970, 0, 12, 12, 35, 0).getTime().getTime();
        assertEquals("33 minutes", HumanDate.toHumanFormat(fromTime, toTime));

        //test one hour
        fromTime = new GregorianCalendar(1970, 0, 12, 12, 2, 0).getTime().getTime();
        toTime   = new GregorianCalendar(1970, 0, 12, 12, 55, 0).getTime().getTime();
        assertEquals("about 1 hour", HumanDate.toHumanFormat(fromTime, toTime));

        //test hours
        fromTime = new GregorianCalendar(1970, 0, 12, 12, 2, 0).getTime().getTime();
        toTime   = new GregorianCalendar(1970, 0, 12, 14, 55, 0).getTime().getTime();
        assertEquals("about 3 hours", HumanDate.toHumanFormat(fromTime, toTime));


        //test a day
        fromTime = new GregorianCalendar(1970, 0, 12, 12, 2, 0).getTime().getTime();
        toTime   = new GregorianCalendar(1970, 0, 13, 12, 55, 0).getTime().getTime();
        assertEquals("1 day", HumanDate.toHumanFormat(fromTime, toTime));


        //test days
        fromTime = new GregorianCalendar(1970, 0, 12, 12, 2, 0).getTime().getTime();
        toTime   = new GregorianCalendar(1970, 0, 15, 12, 55, 0).getTime().getTime();
        assertEquals("3 days", HumanDate.toHumanFormat(fromTime, toTime));

    }

    public void testMonthsYears()
    {
        //test a month
        long fromTime = new GregorianCalendar(1970, 0, 12, 12, 2, 0).getTime().getTime();
        long toTime   = new GregorianCalendar(1970, 1, 15, 12, 55, 0).getTime().getTime();
        assertEquals("about 1 month", HumanDate.toHumanFormat(fromTime, toTime));


        //test  months
        fromTime = new GregorianCalendar(1970, 0, 12, 12, 2, 0).getTime().getTime();
        toTime   = new GregorianCalendar(1970, 5, 15, 12, 55, 0).getTime().getTime();
        assertEquals("5 months", HumanDate.toHumanFormat(fromTime, toTime));

        //test  year
        fromTime = new GregorianCalendar(1970, 0, 12, 12, 2, 0).getTime().getTime();
        toTime   = new GregorianCalendar(1971, 0, 15, 12, 55, 0).getTime().getTime();
        assertEquals("about 1 year", HumanDate.toHumanFormat(fromTime, toTime));


        //test years
        fromTime = new GregorianCalendar(1970, 0, 12).getTime().getTime();
        toTime = new GregorianCalendar(1978, 0, 12).getTime().getTime();
        assertEquals("about 8 years", HumanDate.toHumanFormat(fromTime, toTime));

    }
}

