package org.javalite.activejdbc;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import static org.javalite.test.jspec.JSpec.a;

import org.javalite.activejdbc.validation.AttributePresenceValidator;

public class ErrorsTest {

	Errors errors;
	
	@Before
	public void before() { 
		errors = new Errors();
		errors.addValidator("name", new AttributePresenceValidator("name"));
		errors.addValidator("description", new AttributePresenceValidator("description"));
	}
	
	@Test
	public void shouldReturnCollectionsWithAllValuesOfTheErrors() { 
		Collection<String> expected = Arrays.asList("value is missing","value is missing");
		a(expected).shouldBeEqual(errors.values());
	}
}
