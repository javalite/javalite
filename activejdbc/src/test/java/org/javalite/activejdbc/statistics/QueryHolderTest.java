package org.javalite.activejdbc.statistics;

import org.javalite.activejdbc.cache.QueryHolder;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.Test;

public class QueryHolderTest extends JSpecSupport{

	@Test
	public void shouldTreateAsSameIntgerAndLongInParams(){
		QueryHolder q1 = new QueryHolder("select", new Object[]{1});
		QueryHolder q2 = new QueryHolder("select", new Object[]{new Long(1)});
		a(q1).shouldBeEqual(q2);
		q2 = new QueryHolder("select", new Object[]{1L});
		a(q1).shouldBeEqual(q2);
	}
	
	@Test
	public void shouldTreateAsSameIntgerAndDoubleInParams(){
		QueryHolder q1 = new QueryHolder("select", new Object[]{1});
		QueryHolder q2 = new QueryHolder("select", new Object[]{new Double(1)});
		a(q1).shouldBeEqual(q2);
		q2 = new QueryHolder("select", new Object[]{1.0d});
		a(q1).shouldBeEqual(q2);
		q2 = new QueryHolder("select", new Object[]{1d});
		a(q1).shouldBeEqual(q2);
		q2 = new QueryHolder("select", new Object[]{1f});
		a(q1).shouldBeEqual(q2);
		q2 = new QueryHolder("select", new Object[]{1.0d});
		a(q1).shouldBeEqual(q2);
	}
	
	@Test
	public void shouldTreateAsDifferentIntgerAndDoubleInParams(){
		QueryHolder q1 = new QueryHolder("select", new Object[]{1});
		QueryHolder q2 = new QueryHolder("select", new Object[]{new Double(1.2)});
		a(q1).shouldNotBeEqual(q2);
		q2 = new QueryHolder("select", new Object[]{1.1d});
		a(q1).shouldNotBeEqual(q2);
		q2 = new QueryHolder("select", new Object[]{1.1d});
		a(q1).shouldNotBeEqual(q2);
		q2 = new QueryHolder("select", new Object[]{1.1f});
		a(q1).shouldNotBeEqual(q2);
		q2 = new QueryHolder("select", new Object[]{1.1d});
		a(q1).shouldNotBeEqual(q2);
	}
	
	@Test
	public void shouldTreateAsDifferentIntgerAndStringInParams(){
		QueryHolder q1 = new QueryHolder("select", new Object[]{1});
		QueryHolder q2 = new QueryHolder("select", new Object[]{"1"});
		a(q1).shouldNotBeEqual(q2);
	}
	
	@Test
	public void shouldTreateAsDifferentWhenStringsNotTheSame(){
		QueryHolder q1 = new QueryHolder("select", new Object[]{"1"});
		QueryHolder q2 = new QueryHolder("select1", new Object[]{"1"});
		a(q1).shouldNotBeEqual(q2);
	}
	
	@Test
	public void shouldTreateAsSameWhenStringsInDiffCases(){
		QueryHolder q1 = new QueryHolder("select", new Object[]{"1"});
		QueryHolder q2 = new QueryHolder("SELECT", new Object[]{"1"});
		a(q1).shouldBeEqual(q2);
	}
	
	@Test
	public void shouldTreateAsSameWithoutParams(){
		QueryHolder q1 = new QueryHolder("select", null);
		QueryHolder q2 = new QueryHolder("select", null);
		a(q1).shouldBeEqual(q2);
		q2 = new QueryHolder("SELECT", null);
		a(q1).shouldBeEqual(q2);
	}
	
	@Test
	public void shouldTreateAsDifferentWithDifferentCountParams(){
		QueryHolder q1 = new QueryHolder("select", new Object[]{"1"});
		QueryHolder q2 = new QueryHolder("select", new Object[]{"1", "2"});
		a(q1).shouldNotBeEqual(q2);
		q1 = new QueryHolder("select", new Object[]{1});
		q2 = new QueryHolder("select", new Object[]{1, "2"});
		a(q1).shouldNotBeEqual(q2);
		q1 = new QueryHolder("select", new Object[]{1});
		q2 = new QueryHolder("select", new Object[]{1L, "2"});
		a(q1).shouldNotBeEqual(q2);
	}
	
	@Test
	public void shouldBeSameHashcode(){
		QueryHolder q1 = new QueryHolder("select", new Object[]{1});
		int expected = q1.hashCode();
		QueryHolder q2 = new QueryHolder("select", new Object[]{new Long(1)});
		a(expected).shouldBeEqual(q2.hashCode());
		q2 = new QueryHolder("select", new Object[]{new Double(1)});
		a(expected).shouldBeEqual(q2.hashCode());
		q2 = new QueryHolder("select", new Object[]{1.0d});
		a(expected).shouldBeEqual(q2.hashCode());
		q2 = new QueryHolder("select", new Object[]{1d});
		a(expected).shouldBeEqual(q2.hashCode());
		q2 = new QueryHolder("select", new Object[]{1f});
		a(expected).shouldBeEqual(q2.hashCode());
		q2 = new QueryHolder("select", new Object[]{1.0d});
		a(expected).shouldBeEqual(q2.hashCode());
	}
}
