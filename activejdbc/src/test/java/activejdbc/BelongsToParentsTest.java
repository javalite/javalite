package activejdbc;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import activejdbc.test.ActiveJDBCTest;
import activejdbc.test_models.Computer;
import activejdbc.test_models.Keyboard;
import activejdbc.test_models.Motherboard;

public class BelongsToParentsTest extends ActiveJDBCTest {

	public void before() throws Exception { 
		super.before();
        resetTable("computers-motherboards-keyboards");
	}
	
	@Test
	public void shouldCheckThatComputerBelongsTo2DifferentParents() { 
		Computer computer = Computer.findById(1);
		a(computer.parent(Motherboard.class)).shouldNotBeNull();
		a(computer.parent(Keyboard.class)).shouldNotBeNull();
	}
}
