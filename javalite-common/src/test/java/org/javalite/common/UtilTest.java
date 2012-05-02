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


package org.javalite.common;

import org.javalite.test.jspec.JSpecSupport;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Igor Polevoy
 */
public class UtilTest extends JSpecSupport {

    @Test
    public void testJoin(){
        String[] arr = {"first", "second", "third", "fourth"};
        a("first, second, third, fourth").shouldBeEqual(Util.join(Arrays.asList(arr), ", "));
    }


    @Test
    public void testSplit(){

        String[] split = Util.split("Hello, Dolly, my darling ", ',');
        a(split.length).shouldBeEqual(3);
    }
    
    @Test
    public void testCountOccurences(){

        String query = "select * from table where id= ? and name =?";
        a(Util.countOccurrences(query, '?')).shouldBeEqual(2);
        query = "select * from table where id= ?";
        a(Util.countOccurrences(query, '?')).shouldBeEqual(1);
        query = "select * from table";
        a(Util.countOccurrences(query, '?')).shouldBeEqual(0);
    }

    @Test
    public void shouldReadBytesFromStream() throws IOException {

        byte[] bytes = Util.bytes(UtilTest.class.getResourceAsStream("/text.txt"));
        a(new String(bytes)).shouldBeEqual("hello");
    }


    @Test
    public void shouldReadBytesFromResource() throws IOException {

        byte[] bytes = Util.readResourceBytes("/pdf_implementation.pdf");
        a(bytes.length).shouldBeEqual(174230);
    }

    @Test
    public void shouldStreamIntoFile() throws IOException {
        String hello = "hello world";
        ByteArrayInputStream bin = new ByteArrayInputStream(hello.getBytes());

        Util.saveTo("target/test.txt", bin);
        a(Util.readFile("target/test.txt")).shouldBeEqual(hello);
    }
    
    @Test
    public void shouldCorrectlyTransformSQL() throws IOException {
        String expected = "select * from people where id = 1";
        a(Util.toGracefulSQL(" select * from people where id = 1")).shouldBeEqual(expected);
        a(Util.toGracefulSQL("select * from people where id = 1 ")).shouldBeEqual(expected);
        a(Util.toGracefulSQL(" select  *   from       people      where    id   =   1")).shouldBeEqual(expected);
        a(Util.toGracefulSQL("select * from people where id=1")).shouldBeEqual(expected);
        a(Util.toGracefulSQL("SELECT  * FRom peopLE where id=       1")).shouldBeEqual(expected);
        a(Util.toGracefulSQL("select * from people where id =1")).shouldBeEqual(expected);
        a(Util.toGracefulSQL("select * from people where id= 1")).shouldBeEqual(expected);
        expected = "select * from people where id = 1 and name = 'Andrey'";
        a(Util.toGracefulSQL("select * from people where id= 1 and name = 'Andrey'")).shouldBeEqual(expected);
        a(Util.toGracefulSQL("select * from people where id=1 and name = 'Andrey'")).shouldBeEqual(expected);
        a(Util.toGracefulSQL("select * from people where id =1 and name = 'Andrey'")).shouldBeEqual(expected);
        a(Util.toGracefulSQL("select * from people where id = 1 and name= 'Andrey'")).shouldBeEqual(expected);
        a(Util.toGracefulSQL("select * from people where id = 1 and name ='Andrey'")).shouldBeEqual(expected);
        a(Util.toGracefulSQL("select * from people where id = 1 and name='Andrey'")).shouldBeEqual(expected);
        a(Util.toGracefulSQL("select * from people where id=1 and name='Andrey'")).shouldBeEqual(expected);
        a(Util.toGracefulSQL("select * from people where id!=1 and name='Andrey'")).shouldBeEqual("select * from people where id != 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id>=1 and name='Andrey'")).shouldBeEqual("select * from people where id >= 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id<=1 and name='Andrey'")).shouldBeEqual("select * from people where id <= 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id==1 and name='Andrey'")).shouldBeEqual("select * from people where id == 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id>1 and name='Andrey'")).shouldBeEqual("select * from people where id > 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id<1 and name='Andrey'")).shouldBeEqual("select * from people where id < 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id >1 and name='Andrey'")).shouldBeEqual("select * from people where id > 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id <1 and name='Andrey'")).shouldBeEqual("select * from people where id < 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id> 1 and name='Andrey'")).shouldBeEqual("select * from people where id > 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id< 1 and name='Andrey'")).shouldBeEqual("select * from people where id < 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id > 1 and name='Andrey'")).shouldBeEqual("select * from people where id > 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id < 1 and name='Andrey'")).shouldBeEqual("select * from people where id < 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id ==1 and name='Andrey'")).shouldBeEqual("select * from people where id == 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id== 1 and name='Andrey'")).shouldBeEqual("select * from people where id == 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id == 1 and name='Andrey'")).shouldBeEqual("select * from people where id == 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id !=1 and name='Andrey'")).shouldBeEqual("select * from people where id != 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id!= 1 and name='Andrey'")).shouldBeEqual("select * from people where id != 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id != 1 and name='Andrey'")).shouldBeEqual("select * from people where id != 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id >=1 and name='Andrey'")).shouldBeEqual("select * from people where id >= 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id>= 1 and name='Andrey'")).shouldBeEqual("select * from people where id >= 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id >= 1 and name='Andrey'")).shouldBeEqual("select * from people where id >= 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id <=1 and name='Andrey'")).shouldBeEqual("select * from people where id <= 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id<= 1 and name='Andrey'")).shouldBeEqual("select * from people where id <= 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id <= 1 and name='Andrey'")).shouldBeEqual("select * from people where id <= 1 and name = 'Andrey'");
        a(Util.toGracefulSQL("select * from people where id = 1 and name='Andrey' AND total=?")).shouldBeEqual("select * from people where id = 1 and name = 'Andrey' and total = ?");
        a(Util.toGracefulSQL("select * from people where id = 1 and name='Andrey' AND total='HHg'")).shouldBeEqual("select * from people where id = 1 and name = 'Andrey' and total = 'HHg'");
    }
    
    @Test
    public void shouldCorrectlyTrimLimitAndOffset() throws IOException {
        String expected = "select * from people where id = 1";
        a(Util.trimLimitAndOffset(" select * from people where id = 1 limit 1 offset 1")).shouldBeEqual(expected);
        a(Util.trimLimitAndOffset("select * from people where id = 1 limit 10 offset 1")).shouldBeEqual(expected);
        a(Util.trimLimitAndOffset("select * from people where id = 1 limit a offset 1")).shouldBeEqual("select * from people where id = 1 limit a offset 1");
        a(Util.trimLimitAndOffset("select * from people where id = 1 limit 1 offset b")).shouldBeEqual("select * from people where id = 1 limit 1 offset b");
        a(Util.trimLimitAndOffset("select * from people where id = 1 limit 1")).shouldBeEqual(expected);
        a(Util.trimLimitAndOffset("select * from people where id = 1 limit 10")).shouldBeEqual(expected);
    }
    
    @Test
    public void shouldCorrectlyFindLimitAndOffset() throws Exception {
 
    	int[] returned = Util.getLimitAndOffsetFromString("select * from people where id = 1 limit 10 offset 1");
    	arrayShouldContain(10, 1, returned);
    	returned = Util.getLimitAndOffsetFromString("select * from people where id = 1 limit 10 offset 10");
    	arrayShouldContain(10, 10, returned);
    	returned = Util.getLimitAndOffsetFromString("select * from people where id = 1 limit 1 offset 10");
    	arrayShouldContain(1, 10, returned);
    	returned = Util.getLimitAndOffsetFromString("select * from people where id = 1 limit 10");
    	arrayShouldContain(10, 0, returned);
    	returned = Util.getLimitAndOffsetFromString("select * from people where id = 1 limit 1");
    	arrayShouldContain(1, 0, returned);
    	returned = Util.getLimitAndOffsetFromString("select * from people where id = 1 limi 1");
    	arrayShouldContain(0, 0, returned);
    	returned = Util.getLimitAndOffsetFromString("select * from people where id = 1 offset 10");
    	arrayShouldContain(0, 0, returned);
    	returned = Util.getLimitAndOffsetFromString("select * from people where id = a offset 10");
    	arrayShouldContain(0, 0, returned);
    	returned = Util.getLimitAndOffsetFromString("select * from people where id = a offset b");
    	arrayShouldContain(0, 0, returned);
    }


	private void arrayShouldContain(int limit, int offset, int[] returned) throws Exception {
		if(returned.length != 2){
			throw new Exception("Size of array should be 2!!");
		}
		if(returned[0] != limit){
			throw new Exception("Limit is wrong should be " + limit + " but actually is " + returned[0]);
		}
		if(returned[1] != offset){
			throw new Exception("Offset is wrong should be " + offset + " but actually is " + returned[1]);
		}
	}
    
}
