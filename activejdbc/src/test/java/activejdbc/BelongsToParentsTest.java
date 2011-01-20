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
		resetTables("computers","motherboards","keyboards");
	}
	
	@Test
	public void shouldReturnTwoBelongToRelationWithDifencesKeys() { 
		Computer computer = Computer.findById(1);
		assertNotNull(computer.parent(Motherboard.class));
		assertNotNull(computer.parent(Keyboard.class));
	}
}
