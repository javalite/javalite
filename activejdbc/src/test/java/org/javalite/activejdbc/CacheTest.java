/*
Copyright 2009-2014 Igor Polevoy

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

import org.javalite.activejdbc.Registry;
import org.javalite.activejdbc.cache.CacheEvent;
import org.javalite.activejdbc.cache.CacheEventListener;
import org.javalite.activejdbc.cache.QueryCache;
import org.javalite.activejdbc.statistics.QueryStats;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.*;
import org.junit.Test;

import java.util.List;


/**
 * @author Igor Polevoy
 */
public class CacheTest extends ActiveJDBCTest {

    @Override
    public void before() throws Exception {
        super.before();
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
        List<Doctor> doctors = Doctor.findAll();
        //produces a cache hit
        List<Doctor> doctors1 = Doctor.findAll();

        a(doctors.hashCode()).shouldBeEqual(doctors1.hashCode());

        Doctor d1 = new Doctor();
        //purges doctor cache
        d1.set("first_name", "Sunjay").set("last_name", "Gupta").set("discipline", "physician").saveIt();

        //produces a miss
        doctors1 = Doctor.findAll();
        a(doctors.hashCode() != doctors1.hashCode()).shouldBeTrue();

        //produces a hit
        doctors = Doctor.findAll();
        a(doctors.hashCode()).shouldBeEqual(doctors1.hashCode());
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

        QueryCache.instance().getCacheManager().addCacheEventListener(cl);
        Person.deleteAll();
        a(count).shouldBeEqual(1);


        Account.deleteAll();

        a(count).shouldBeEqual(1);

    }

}
