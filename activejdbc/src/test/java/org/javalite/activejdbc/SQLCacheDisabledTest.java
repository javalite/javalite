package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Room;
import org.junit.Test;

public class SQLCacheDisabledTest extends ActiveJDBCTest{

	@Test
	public void cacheShouldBeDisabled(){
		deleteAndPopulateTables("rooms");
        //produces a cache miss
        Room.findAll().toMaps();
        ConnectionStatistics stats = ConnectionsAccess.getStatisticsForConnection("default");
        a(stats).shouldBeNull();
	}
}
