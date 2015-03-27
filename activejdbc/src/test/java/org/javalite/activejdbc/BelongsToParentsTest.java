package org.javalite.activejdbc;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Computer;
import org.javalite.activejdbc.test_models.Keyboard;
import org.javalite.activejdbc.test_models.Motherboard;

import static org.javalite.activejdbc.test_models.Computer.Computer;
import static org.javalite.activejdbc.test_models.Motherboard.Motherboard;

public class BelongsToParentsTest extends ActiveJDBCTest {

	@Before
	public void before() throws Exception { 
		super.before();

        deleteFromTable("computers");
        deleteFromTable("motherboards");
        deleteFromTable("keyboards");

        populateTable("motherboards");
        populateTable("keyboards");
        populateTable("computers");

        		
        	
	}
	
	@Test
	public void shouldCheckThatComputerBelongsTo2DifferentParents() { 

		Computer computer = Computer.findById(1);
		a(computer.parent(Motherboard.class)).shouldNotBeNull();
		a(computer.parent(Keyboard.class)).shouldNotBeNull();
	}
}
