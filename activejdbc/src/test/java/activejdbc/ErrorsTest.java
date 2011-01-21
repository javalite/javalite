package activejdbc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import activejdbc.validation.AttributePresenceValidator;

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
		assertEquals(expected, errors.values());
	}
}
