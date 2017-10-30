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


package org.javalite.activejdbc;

import org.javalite.activejdbc.cache.CacheEvent;
import org.javalite.activejdbc.cache.CacheEventListener;
import org.javalite.activejdbc.statistics.QueryStats;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.*;
import org.javalite.common.JsonHelper;
import org.javalite.common.Util;
import org.javalite.test.SystemStreamUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.javalite.test.jspec.JSpec.$;



/**
 * @author Igor Polevoy
 */
public class CacheTest extends ActiveJDBCTest {

    @Before
    public void setup() throws Exception {
        deleteAndPopulateTable("people");
        for (int i = 0; i < 100; i++) {
            Person p = new Person();
            p.set("name", "name: " + i);
            p.set("last_name", "last_name: " + i);
            p.set("dob", "1935-12-06");
            p.saveIt();
        }
    }


    /**
     * To see the cache in action, see console output for hits and misses.
     */
    @Test
    public void testCache() {
        deleteAndPopulateTables("doctors", "patients", "doctors_patients");
        //produces a cache miss
        List<Doctor> doctors = Doctor.findAll().orderBy("id desc");
        //produces a cache hit
        List<Doctor> doctors1 = Doctor.findAll().orderBy("id desc");

        a(doctors.get(0)).shouldBeTheSameAs(doctors1.get(0));

        Doctor d1 = new Doctor();
        //purges doctor cache
        d1.set("first_name", "Sunjay").set("last_name", "Gupta").set("discipline", "physician").saveIt();

        //produces a miss
        doctors1 = Doctor.findAll().orderBy("id desc");
        a(doctors.get(0)).shouldNotBeTheSameAs(doctors1.get(0));

        //produces a hit
        doctors = Doctor.findAll().orderBy("id desc");
        a(doctors.get(0)).shouldBeTheSameAs(doctors1.get(0));
    }

    @Test
    public void testFindFirst(){
        Person p = Person.findFirst("name like ?" , "%3%");
        //comes from cache
        a(p).shouldBeTheSameAs(Person.findFirst("name like ?" , "%3%"));
    }

    @Test
    public void testFindById(){
        Person p1 = Person.findById(1);

        a(p1).shouldBeTheSameAs(Person.findById(1));

        //now, let's save a new person - this will blow away cache. 
        new Person().set("name", "Ron").set("last_name", "Smith").set("dob", "1946-11-04").saveIt();
        a(p1).shouldNotBeTheSameAs(Person.findById(1));

        //cleanup:
        Person.delete("last_name = ? and name = ?", "Smith", "Ron");
    }

    @Test
    public void testCount(){

        Person.count();
        Person.count();

        Person.purgeCache();
        Person.count();
        //see log output
    }

    @Test
    public void testCountWithParams(){
        Person.count("name like ? ", "%3%");
        Person.count("name like ? ", "%3%");

        Person.purgeCache();        
        Person.count("name like ? ", "%3%");
        //see log output
    }

    @Test
    public void testCachedParent(){
        deleteAndPopulateTables("libraries", "books");
        Book b = Book.findById(1);
        Library l1 = b.parent(Library.class);

        a(l1 == b.parent(Library.class)).shouldBeTrue();

        //let's blow away cache
        new Library().set("address", "123 Pirate Street").set("city", "Bloomington").set("state", "CA").saveIt();
        a(l1 == b.parent(Library.class)).shouldBeFalse();
    }

    @Test
    public void shouldNotAddInfoToStatisticsIfFoundResultInCache(){
        deleteAndPopulateTables("libraries", "books");

        try{
            Thread.sleep(1000);
        }catch(Exception e){
            //e.printStackTrace();
        }
        Registry.instance().getStatisticsQueue().reset();
        
        //calling finder twice, but only one object in the stats queue
        Library.findAll().dump();
        Library.findAll().dump();

        try{
            Thread.sleep(1000);
        }catch(Exception e){
            e.printStackTrace();
        }
        List<QueryStats> queryStats = Registry.instance().getStatisticsQueue().getReportSortedBy("total");
        a(queryStats.get(0).getCount()).shouldEqual(1);
    }

    int count = 0;
    @Test
    public void shouldNotPropagateCacheEventForNonCachedModels(){
        CacheEventListener cl = new CacheEventListener() {
            public void onFlush(CacheEvent event) {
                count++;
            }
        };
        Registry.cacheManager().addCacheEventListener(cl);
        Person.deleteAll();
        a(count).shouldBeEqual(1);
        Account.deleteAll();
        a(count).shouldBeEqual(1);
    }

    int count1 = 0;
    @Test
    public void shouldNotPropagateCacheEventOnFlush(){
        CacheEventListener cl = new CacheEventListener() {
            public void onFlush(CacheEvent event) {
                count1++;
            }
        };
        Registry.cacheManager().addCacheEventListener(cl);
        Registry.cacheManager().flush(new CacheEvent("people", "blah"), false);
        $(count1).shouldBeEqual(0);
    }


    @Test
    public void shouldDropCacheOnRefresh(){
        SystemStreamUtil.replaceOut();
        Person p = Person.create("name", "Sam", "last_name", "Margulis", "dob", "2001-01-07");
        p.saveIt();
        Person.findAll().size();
        p.refresh();
        Person.findAll().size();
        String out = SystemStreamUtil.getSystemOut();
        the(out).shouldNotContain("HIT");
        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void shouldIndicateCachedAnnotation(){
        the(Person.isCached()).shouldBeTrue();
    }


    @Test
    public void shouldGenerateJSONFromQueryCache(){
        SystemStreamUtil.replaceOut();
        Person p = Person.create("name", "Sam", "last_name", "Margulis", "dob", "2001-01-07");
        p.saveIt();
        Person.findAll().size();
        Person.findAll().size();
        List<String> selects = getTwoSelects();

        //select 1, miss, has duration_millis
        Map logMap = JsonHelper.toMap(selects.get(0));
        Map message = (Map) logMap.get("message");
        the(message.get("duration_millis")).shouldNotBeNull();
        the(message.get("sql")).shouldBeEqual("SELECT * FROM people");
        the(message.get("cache")).shouldBeEqual("miss"); //<<-----------------miss

        //select 2, hit, has no duration_millis
        logMap = JsonHelper.toMap(selects.get(1));
        message = (Map) logMap.get("message");
        the(message.get("duration_millis")).shouldBeNull();
        the(message.get("sql")).shouldBeEqual("SELECT * FROM people");
        the(message.get("cache")).shouldBeEqual("hit"); //<<-----------------hit

        SystemStreamUtil.restoreSystemOut();
    }

    private List<String> getTwoSelects() {
        String out = SystemStreamUtil.getSystemOut();
        List<String> lines = Arrays.asList(Util.split(out, System.getProperty("line.separator")));
        return lines.stream().filter(line -> line.contains("SELECT")).collect(Collectors.toList());
    }
}
