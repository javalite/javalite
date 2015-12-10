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

import org.javalite.activejdbc.cache.CacheEvent;
import org.javalite.activejdbc.cache.CacheEventListener;
import org.javalite.activejdbc.cache.QueryCache;
import org.javalite.activejdbc.statistics.QueryStats;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.*;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.map;


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

        QueryCache.instance().purgeTableCache("doctors");
        QueryCache.instance().purgeTableCache("patients");
        QueryCache.instance().purgeTableCache("libraries");
        QueryCache.instance().purgeTableCache("books");
        QueryCache.instance().purgeTableCache("people");
    }


    /**
     * To see the cache in action, see console output for hits and misses.
     */
    @Test
    public void testCache() {

        deleteAndPopulateTables("doctors", "patients", "doctors_patients");

        //*********** RESET STATS ***************************/
        Registry.instance().getStatisticsQueue().reset();

        //produces a cache miss
        Doctor.findAll().size();

        //produces a cache hit
        Doctor.findAll().size();


        //data came from database, so only one trip to database:
        validateQueryStats(1, map("query", "SELECT * FROM doctors", "count", 1));


        //*********** RESET STATS ***************************/
        Registry.instance().getStatisticsQueue().reset();

        Doctor d1 = new Doctor();
        //purges doctor cache
        d1.set("first_name", "Sunjay").set("last_name", "Gupta").set("discipline", "physician").saveIt();

        //produces a miss
        a(Doctor.findAll().size()).shouldBeEqual(4);


        //data came from database:
        validateQueryStats(2,
                map("query", "INSERT INTO doctors (discipline, first_name, last_name) VALUES (?, ?, ?)", "count", 1),
                map("query", "SELECT * FROM doctors", "count", 1));



        //*********** RESET STATS ***************************/
        //produces a hit
        Registry.instance().getStatisticsQueue().reset();
        a(Doctor.findAll().size()).shouldBeEqual(4);
        List<QueryStats> queryStats = Registry.instance().getStatisticsQueue().getReportSortedBy("query");
        the(queryStats.size()).shouldBeEqual(0); // data came from cache
    }

    @Test
    public void testFindFirst(){
        //*********** RESET STATS ***************************/
        Registry.instance().getStatisticsQueue().reset();
        Person.findFirst("name like ?" , "%3%");
        //this comes from cache:
        Person.findFirst("name like ?" , "%3%");
        validateQueryStats(1, map("query", "SELECT * FROM people WHERE name like ? LIMIT 1", "count", 1));
    }


    /**
     * Validates content ot query stats
     *
     * @param numberOfQueries - number of queries expected
     * @param queries maps for each query keys: "query" - text of query, "count" - number of times this query was executed.
     */
    private void validateQueryStats(int numberOfQueries, Map... queries) {
        List<QueryStats> queryStats = Registry.instance().getStatisticsQueue().getReportSortedBy("query");

        the(queryStats.size()).shouldBeEqual(numberOfQueries);
        for (int i = 0; i < queryStats.size(); i++) {
            QueryStats stats = queryStats.get(i);
            the(stats.getQuery()).shouldBeEqual(queries[i].get("query"));
            the(stats.getCount()).shouldBeEqual(queries[i].get("count"));
        }
    }

    @Test
    public void testFindById(){

        //*********** RESET STATS ***************************/
        Registry.instance().getStatisticsQueue().reset();

        Person.findById(1); // from DB
        Person.findById(1); // from cache

        validateQueryStats(1, map("query", "SELECT * FROM people WHERE id = ? LIMIT 1", "count", 1));

        //now, let's save a new person - this will blow away cache. 
        new Person().set("name", "Ron").set("last_name", "Smith").set("dob", "1946-11-04").saveIt();

        //*********** RESET STATS ***************************/
        Registry.instance().getStatisticsQueue().reset();
        Person.findById(1); // from DB again

        List<QueryStats> queryStats = Registry.instance().getStatisticsQueue().getReportSortedBy("query");
        the(queryStats.size()).shouldBeEqual(1); // data came from DB

        //cleanup:
        Person.delete("last_name = ? and name = ?", "Smith", "Ron");
    }

    @Test
    public void testCount(){

        //*********** RESET STATS ***************************/
        Registry.instance().getStatisticsQueue().reset();

        Person.count();
        Person.count();

        validateQueryStats(1, map("query", "SELECT COUNT(*) FROM people", "count", 1));


        //*********** RESET STATS ***************************/
        Registry.instance().getStatisticsQueue().reset();
        Person.purgeCache();
        Person.count();
        validateQueryStats(1, map("query", "SELECT COUNT(*) FROM people", "count", 1));

    }

    @Test
    public void testCountWithParams(){

        //*********** RESET STATS ***************************/
        Registry.instance().getStatisticsQueue().reset();

        Person.count("name like ? ", "%3%");
        Person.count("name like ? ", "%3%");
        validateQueryStats(1, map("query", "SELECT COUNT(*) FROM people WHERE name like ? ", "count", 1));

        //*********** RESET STATS ***************************/
        Registry.instance().getStatisticsQueue().reset();
        Person.purgeCache();        
        Person.count("name like ? ", "%3%");
        validateQueryStats(1, map("query", "SELECT COUNT(*) FROM people WHERE name like ? ", "count", 1));
    }

    @Test
    public void testCachedParent(){
        deleteAndPopulateTables("libraries", "books");

        //*********** RESET STATS ***************************/
        Registry.instance().getStatisticsQueue().reset();
        Book b = Book.findById(1);
        b.parent(Library.class);
        b.parent(Library.class);
        validateQueryStats(2,
                map("query", "SELECT * FROM books WHERE id = ? LIMIT 1", "count", 1),
                map("query", "SELECT * FROM libraries WHERE id = ?", "count", 1)
        );



        //*********** RESET STATS ***************************/
        Registry.instance().getStatisticsQueue().reset();
        //let's blow away cache
        new Library().set("address", "123 Pirate Street").set("city", "Bloomington").set("state", "CA").saveIt();

        b.parent(Library.class);

        validateQueryStats(2,
                map("query", "INSERT INTO libraries (address, city, state) VALUES (?, ?, ?)", "count", 1),
                map("query", "SELECT * FROM libraries WHERE id = ?", "count", 1)
        );

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
        List<QueryStats> queryStats = Registry.instance().getStatisticsQueue().getReportSortedBy("query");
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
