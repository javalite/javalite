package org.javalite.activejdbc;

import static org.javalite.activejdbc.test.JdbcProperties.driver;
import static org.javalite.activejdbc.test.JdbcProperties.password;
import static org.javalite.activejdbc.test.JdbcProperties.url;
import static org.javalite.activejdbc.test.JdbcProperties.user;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Animal;
import org.javalite.activejdbc.test_models.Room;
import org.junit.Test;

public class SqlQueryCacheTest extends ActiveJDBCTest{

	@Override
	protected void openDB() {
		new DB("default").enableSqlCache().enableConnectionStatistics().open(driver(), url(), user(), password());
	}
	
	/**
     * Test for sql query cache.
     */
    @Test
    public void FindAllShouldBeCached() {
        deleteAndPopulateTables("rooms");
        //produces a cache miss
        Room.findAll().toMaps();
        ConnectionStatistics stats = ConnectionsAccess.getStatisticsForConnection("default");
        a(stats.getCountQueries()).shouldBeEqual(1);
        a(stats.getCountHits()).shouldBeEqual(0);
        a(stats.getCountMisses()).shouldBeEqual(1);
        //print(rooms);
        //produces a cache hit
        Room.findAll().toMaps();
        a(stats.getCountQueries()).shouldBeEqual(2);
        a(stats.getCountHits()).shouldBeEqual(1);
        a(stats.getCountMisses()).shouldBeEqual(1);
        Room d1 = new Room();
        //purges rooms cache
        d1.set("name", "Sunjay").saveIt();

        //produces a miss
        Room.findAll().toMaps();
        a(stats.getCountQueries()).shouldBeEqual(3);
        a(stats.getCountHits()).shouldBeEqual(1);
        a(stats.getCountMisses()).shouldBeEqual(2);
        //produces a hit
        Room.findAll().toMaps();
        a(stats.getCountQueries()).shouldBeEqual(4);
        a(stats.getCountHits()).shouldBeEqual(2);
        a(stats.getCountMisses()).shouldBeEqual(2);
    }
    
    @Test
    public void FindAllWithParametersShouldBeCached() {
    	 deleteAndPopulateTables("rooms", "animals");
         //produces a cache miss
         Room.find("id =?", 1).toMaps();
         ConnectionStatistics stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(1);
         a(stats.getCountHits()).shouldBeEqual(0);
         a(stats.getCountMisses()).shouldBeEqual(1);
         
         //produces a hit
         Room.find("id =?", 1).toMaps();
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(2);
         a(stats.getCountHits()).shouldBeEqual(1);
         a(stats.getCountMisses()).shouldBeEqual(1);
         boolean b = ((Room)Room.findById(1)).isValid();
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(3);
         a(stats.getCountHits()).shouldBeEqual(2);
         a(stats.getCountMisses()).shouldBeEqual(1);
         Room.findBySQL("SELECT  * FROM rooms WHERE  id = ?", 1).toMaps();
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(4);
         a(stats.getCountHits()).shouldBeEqual(3);
         a(stats.getCountMisses()).shouldBeEqual(1);
         ModelListener<Room> modelListener = new ModelListener<Room>() {
             public void onModel(Room person) {
                 //counter ++;
             }
         };
		 Room.findWith(modelListener, "id = ?", 1);
		 stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(5);
         a(stats.getCountHits()).shouldBeEqual(4);
         a(stats.getCountMisses()).shouldBeEqual(1);
         Room room = (Room)Room.findFirst("id = ?", 1);
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(6);
         a(stats.getCountHits()).shouldBeEqual(5);
         a(stats.getCountMisses()).shouldBeEqual(1);
         Room.findBySQL("SELECT  * FROM rooms order by name").toMaps();
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(7);
         a(stats.getCountHits()).shouldBeEqual(5);
         a(stats.getCountMisses()).shouldBeEqual(2);
         Room.findBySQL("SELECT  * FROM rooms limit 1 offset 1 order by name").toMaps();
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(8);
         a(stats.getCountHits()).shouldBeEqual(6);
         a(stats.getCountMisses()).shouldBeEqual(2);
         boolean isExist = Room.exists(1);
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(9);
         a(stats.getCountHits()).shouldBeEqual(6);
         a(stats.getCountMisses()).shouldBeEqual(3);
         room.refresh();
         room.get("name");
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(10);
         a(stats.getCountHits()).shouldBeEqual(7);
         a(stats.getCountMisses()).shouldBeEqual(3);
         room = (Room)Room.findFirst("id = ?", 1L);
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(11);
         a(stats.getCountHits()).shouldBeEqual(8);
         a(stats.getCountMisses()).shouldBeEqual(3);
         room = (Room)Room.findFirst("id = ?", 1.0d);
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(12);
         a(stats.getCountHits()).shouldBeEqual(9);
         a(stats.getCountMisses()).shouldBeEqual(3);
         room = (Room)Room.findFirst("id = ?", 1.0f);
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(13);
         a(stats.getCountHits()).shouldBeEqual(10);
         a(stats.getCountMisses()).shouldBeEqual(3);
         //For now I didn't try to determine if string and number parameters is the same? Need to think
         room = (Room)Room.findFirst("id = ?", "1");
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(14);
         a(stats.getCountHits()).shouldBeEqual(10);
         a(stats.getCountMisses()).shouldBeEqual(4);
         Room.delete("id = ?", "100");
         room = (Room)Room.findFirst("id = ?", 1L);
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(15);
         a(stats.getCountHits()).shouldBeEqual(11);
         a(stats.getCountMisses()).shouldBeEqual(4);
         Room.delete("id = ?", 1);
         room = (Room)Room.findFirst("id = ?", 1L);
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(16);
         a(stats.getCountHits()).shouldBeEqual(11);
         a(stats.getCountMisses()).shouldBeEqual(5);
         room = (Room)Room.findFirst("id = ?", 1L);
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(17);
         a(stats.getCountHits()).shouldBeEqual(12);
         a(stats.getCountMisses()).shouldBeEqual(5);
         Animal.delete("animal_id = ?", "1");
         room = (Room)Room.findFirst("id = ?", 1L);
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(18);
         a(stats.getCountHits()).shouldBeEqual(12);
         a(stats.getCountMisses()).shouldBeEqual(6);
         room = (Room)Room.findFirst("id = ?", 1L);
         stats = ConnectionsAccess.getStatisticsForConnection("default");
         a(stats.getCountQueries()).shouldBeEqual(19);
         a(stats.getCountHits()).shouldBeEqual(13);
         a(stats.getCountMisses()).shouldBeEqual(6);
    }
}
