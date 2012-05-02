package org.javalite.activejdbc.statistics;

import org.javalite.activejdbc.ConnectionStatistics;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.Test;

public class ConnectionStatisticsTest extends JSpecSupport{

	@Test
	public void shouldReturnCorrectResults(){
		ConnectionStatistics stats = new ConnectionStatistics();
		stats.miss("select * from people");
		stats.hit("select * from people");
		stats.miss("select * from people where id = ?");
		stats.hit("select * from people");
		stats.miss("select * from people where name = ?");
		stats.miss("select * from animals where id = ?");
		stats.hit("select * from people");
		stats.hit("select * from people where id = ?");
		stats.miss("select * from people");
		ConnectionStatistics snapshot = stats.snapshot();
		a(snapshot.getCountQueries()).shouldBeEqual(9);
		a(snapshot.getCountMisses()).shouldBeEqual(5);
		a(snapshot.getCountHits()).shouldBeEqual(4);
		a(snapshot.getCount("select * from people")).shouldBeEqual(5);
		a(snapshot.getHits("select * from people")).shouldBeEqual(3);
		a(snapshot.getMiss("select * from people")).shouldBeEqual(2);
		a(snapshot.getCount("select * from people where id = ?")).shouldBeEqual(2);
		a(snapshot.getHits("select * from people where id = ?")).shouldBeEqual(1);
		a(snapshot.getMiss("select * from people where id = ?")).shouldBeEqual(1);
		a(snapshot.getCount("select * from people where name = ?")).shouldBeEqual(1);
		a(snapshot.getHits("select * from people where name = ?")).shouldBeEqual(0);
		a(snapshot.getMiss("select * from people where name = ?")).shouldBeEqual(1);
		a(snapshot.getCount("select * from animals where id = ?")).shouldBeEqual(1);
		a(snapshot.getHits("select * from animals where id = ?")).shouldBeEqual(0);
		a(snapshot.getMiss("select * from animals where id = ?")).shouldBeEqual(1);
		stats.miss("select * from animals where id = ?");
		stats.hit("select * from people");
		stats.hit("select * from people where id = ?");
		stats.miss("select * from people");
		snapshot = stats.snapshot();
		a(snapshot.getCountQueries()).shouldBeEqual(13);
		a(snapshot.getCountMisses()).shouldBeEqual(7);
		a(snapshot.getCountHits()).shouldBeEqual(6);
		a(snapshot.getCount("select * from people")).shouldBeEqual(7);
		a(snapshot.getHits("select * from people")).shouldBeEqual(4);
		a(snapshot.getMiss("select * from people")).shouldBeEqual(3);
		a(snapshot.getCount("select * from people where id = ?")).shouldBeEqual(3);
		a(snapshot.getHits("select * from people where id = ?")).shouldBeEqual(2);
		a(snapshot.getMiss("select * from people where id = ?")).shouldBeEqual(1);
		a(snapshot.getCount("select * from people where name = ?")).shouldBeEqual(1);
		a(snapshot.getHits("select * from people where name = ?")).shouldBeEqual(0);
		a(snapshot.getMiss("select * from people where name = ?")).shouldBeEqual(1);
		a(snapshot.getCount("select * from animals where id = ?")).shouldBeEqual(2);
		a(snapshot.getHits("select * from animals where id = ?")).shouldBeEqual(0);
		a(snapshot.getMiss("select * from animals where id = ?")).shouldBeEqual(2);
	}
}
