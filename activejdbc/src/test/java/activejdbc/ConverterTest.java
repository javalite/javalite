/*
Copyright 2009-2010 Igor Polevoy 

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


package activejdbc;

import junit.framework.TestCase;

import java.math.BigDecimal;
import java.util.Date;
import java.sql.Timestamp;

/**
 * @author Igor Polevoy
 */
public class ConverterTest extends TestCase {

    public void testDouble(){
        int i = 6;
        assertEquals(6.0, Converter.toDouble(i));
        
        String x = "3";
        assertEquals(3.0, Converter.toDouble(x));
    }

    public void testFloat(){
        int i = 6;
        assertEquals(6.0f, Converter.toFloat(i));

        String x = "3";
        assertEquals(3.0f, Converter.toFloat(x));
    }

    public void testLong(){
        int i = 6;
        assertEquals(6L, (Object)Converter.toLong(i));

        String x = "3";
        assertEquals(3L, (Object)Converter.toLong(x));
    }

    public void testInteger(){
        int i = 6;
        assertEquals(6, (Object)Converter.toInteger(i));

        String x = "3.6";
        assertEquals(3, (Object)Converter.toInteger(x));
    }

    public void testBoolean(){
        assertEquals(true, (Object)Converter.toBoolean("yes"));
        assertEquals(true, (Object)Converter.toBoolean("true"));
        assertEquals(true, (Object)Converter.toBoolean("t"));
        assertEquals(true, (Object)Converter.toBoolean(1));
        assertEquals(true, (Object)Converter.toBoolean(true));
    }

    public void testBigDecimal(){
        int i = 6;
        assertEquals(new BigDecimal(i), Converter.toBigDecimal(i));

        String x = "3.6";
        assertEquals(new BigDecimal(x), Converter.toBigDecimal(x));
    }

    public void testTimeStamp(){

        java.util.Date utilDate = new Date();
        Timestamp ts = new Timestamp(utilDate.getTime());
        assertEquals(ts, Converter.toTimestamp(utilDate));

        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
        assertEquals(ts, Converter.toTimestamp(sqlDate));

        String timeStampStirng = ts.toString();
        assertEquals(ts, Converter.toTimestamp(timeStampStirng));
    }

    //TODO: finish with the rest of methods
    
}
