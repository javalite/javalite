package org.javalite.activejdbc.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.javalite.activejdbc.cache.ConnectionCache;
import org.javalite.activejdbc.cache.QueryHolder;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.Before;
import org.junit.Test;

public class ConnectionCacheTest extends JSpecSupport{

	ConnectionCache conCache = new ConnectionCache();
	
	@Before
	public void before(){
		conCache = new ConnectionCache();
		prepareCachedItem();
	}
	
	@Test
	public void shouldProperlyCacheLimitAndOffset(){
		QueryHolder holder = new QueryHolder("select * from members where id = ? limit 1 offset 2", new Object[]{1});
		List<HashMap<String, Object>> c = conCache.getItem(holder);
		a(c.size()).shouldBeEqual(1);
		a(c.get(0).get("name")).shouldBeEqual("Andrey3");
		holder = new QueryHolder("select * from members where id = ? limit 1 offset 1", new Object[]{1});
		c = conCache.getItem(holder);
		a(c.size()).shouldBeEqual(1);
		a(c.get(0).get("name")).shouldBeEqual("Andrey2");
		holder = new QueryHolder("select * from members where id = ? limit 1", new Object[]{1});
		c = conCache.getItem(holder);
		a(c.size()).shouldBeEqual(1);
		a(c.get(0).get("name")).shouldBeEqual("Andrey");
		holder = new QueryHolder("select * from members where id = ? limit 2", new Object[]{1});
		c = conCache.getItem(holder);
		a(c.size()).shouldBeEqual(2);
		a(c.get(0).get("name")).shouldBeEqual("Andrey");
		holder = new QueryHolder("select * from members where id = ? limit 2 offset 2", new Object[]{1});
		c = conCache.getItem(holder);
		a(c.size()).shouldBeEqual(2);
		a(c.get(0).get("name")).shouldBeEqual("Andrey3");
		a(c.get(1).get("name")).shouldBeEqual("Andrey4");
		holder = new QueryHolder("select * from MEMbers  where   id= ? limit  2 offset  2", new Object[]{1});
		c = conCache.getItem(holder);
		a(c.size()).shouldBeEqual(2);
		a(c.get(0).get("name")).shouldBeEqual("Andrey3");
		holder = new QueryHolder("select * from MEMbers  where   id= ? limit  10 offset  10", new Object[]{1});
		c = conCache.getItem(holder);
		a(c).shouldNotBeNull();
		a(c.size()).shouldBeEqual(0);
		holder = new QueryHolder("select * from MEMbers  where   id= ? limit  5 offset  4", new Object[]{1});
		c = conCache.getItem(holder);
		a(c.size()).shouldBeEqual(2);
		a(c.get(0).get("name")).shouldBeEqual("Andrey5");
	}
	
	@Test
	public void shouldProperlyCacheDifferentStrings(){
		QueryHolder holder = new QueryHolder("select * from members where id = ?", new Object[]{1});
		List<HashMap<String, Object>> c = conCache.getItem(holder);
		a(c.size()).shouldBeEqual(6);
		a(c.get(0).get("name")).shouldBeEqual("Andrey");
		holder = new QueryHolder("SELECT * from members where id = ?", new Object[]{1});
		c = conCache.getItem(holder);
		a(c.size()).shouldBeEqual(6);
		a(c.get(0).get("name")).shouldBeEqual("Andrey");
		holder = new QueryHolder("SELECT *   from    members where id = ?", new Object[]{1});
		c = conCache.getItem(holder);
		a(c.size()).shouldBeEqual(6);
		a(c.get(0).get("name")).shouldBeEqual("Andrey");
		holder = new QueryHolder("SELECT *   from    members where id=?", new Object[]{1});
		c = conCache.getItem(holder);
		a(c.size()).shouldBeEqual(6);
		a(c.get(0).get("name")).shouldBeEqual("Andrey");
		holder = new QueryHolder("SELECT *   from    members where id=? order by name", new Object[]{1});
		c = conCache.getItem(holder);
		a(c).shouldBeNull();
	}
	
	private void prepareCachedItem(){
		QueryHolder holder = new QueryHolder("select * from members where id = ?", new Object[]{1});
		List<HashMap<String, Object>> cached = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> item = new HashMap<String, Object>();
		item.put("name", "Andrey");
		cached.add(item);
		item = new HashMap<String, Object>();
		item.put("name", "Andrey2");
		cached.add(item);
		item = new HashMap<String, Object>();
		item.put("name", "Andrey3");
		cached.add(item);
		item = new HashMap<String, Object>();
		item.put("name", "Andrey4");
		cached.add(item);
		item = new HashMap<String, Object>();
		item.put("name", "Andrey5");
		cached.add(item);
		item = new HashMap<String, Object>();
		item.put("name", "Andrey6");
		cached.add(item);
		conCache.putItem(holder, cached);
	}
}
