package org.javalite.activejdbc;

import java.util.NoSuchElementException;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Composites;
import org.junit.Test;

/**
 * @author comtel2000
 */
public class CompositePkTest extends ActiveJDBCTest {

    @Test
    public void shouldInsertAndUpdate(){

        Composites comp1 = new Composites();
        comp1.set("first_name", "John", "last_name", "Smith", "email", "smithy@spam.org", "address", "bla");
        comp1.insert();

        Composites comp2 = new Composites();
        comp2.set("first_name", "John", "last_name", "Smith2", "email", "smithy@spam.org", "address", "blabla");
        comp2.insert();

        the(Composites.count()).shouldBeEqual(2);
        the(Composites.findByCompositeKeys("John", "Smith", "smithy@spam.org").get("address")).shouldBeEqual("bla");
        the(Composites.findByCompositeKeys("John", "Smith2", "smithy@spam.org").get("address")).shouldBeEqual("blabla");
        
        Composites comp3 = Composites.findByCompositeKeys("John", "Smith2", "smithy@spam.org");
        comp3.set("address", "blablabla");
        comp3.saveIt();
        the(Composites.findByCompositeKeys("John", "Smith2", "smithy@spam.org").get("address")).shouldBeEqual("blablabla");
        
    }
    
    @Test(expected = NoSuchElementException.class)
    public void shouldGenerateNoSuchElementFromBlankUpdate() {
    	Composites c = new Composites();
    	c.toUpdate();
    }
    
    @Test
    public void shouldGenerateValidSQL() {
        Composites comp = new Composites();
        comp.set("first_name", "Johnny", "last_name", "Cash", "email", "j.cash@spam.org", "address", "inserted");
        the(Base.exec(comp.toInsert())).shouldBeEqual(1);
        comp.set("address", "updated");
        the(Base.exec(comp.toUpdate())).shouldBeEqual(1);

        comp = Composites.findByCompositeKeys("Johnny", "Cash", "j.cash@spam.org");
        
        the(comp.get("first_name")).shouldBeEqual("Johnny");
        the(comp.get("last_name")).shouldBeEqual("Cash");
        the(comp.get("email")).shouldBeEqual("j.cash@spam.org");
        the(comp.get("address")).shouldBeEqual("updated");
        
        comp = Composites.findByCompositeKeys("Johnny", "Casher", "j.cash@spam.org");
        the(Composites.findByCompositeKeys("Johnny", "Casher", "j.cash@spam.org")).shouldBeNull();
    }
    
    @Test
    public void shouldReturnNull() {
    	Composites.createIt("first_name", "Johnny", "last_name", "Cash", "email", "j.cash@spam.org", "address", "inserted");
    	the(Composites.findByCompositeKeys("Johnny", "Casher", "j.cash@spam.org")).shouldBeNull();
    	the(Composites.findByCompositeKeys("John", "Cash", "j.cash@spam.org")).shouldBeNull();
    	the(Composites.findByCompositeKeys("Johnny", "Cash", "j.cash@spam.com")).shouldBeNull();
    }
    
    @Test
    public void shouldDeleted() {
    	Composites.createIt("first_name", "Johnny", "last_name", "Cash", "email", "j.cash@spam.org", "address", "inserted");
    	Composites comp = Composites.findByCompositeKeys("Johnny", "Cash", "j.cash@spam.org");
		the(comp).shouldNotBeNull();
		the(Composites.count()).shouldBeEqual(1);
		comp.delete();
		the(Composites.count()).shouldBeEqual(0);
		the(comp.isFrozen()).shouldBeTrue();
    }
    
    @Test
    public void shouldBeNew() {
    	
        Composites comp = new Composites();
        comp.set("first_name", "Johnny", "last_name", "Cash", "email", "j.cash@spam.org", "address", "inserted");
        the(comp.isNew()).shouldBeTrue();
        
    	Composites.createIt("first_name", "Johnny", "last_name", "Cash", "email", "j.cash@spam.org", "address", "inserted");
    	comp = Composites.findByCompositeKeys("Johnny", "Cash", "j.cash@spam.org");
		the(comp).shouldNotBeNull();
		the(comp.isNew()).shouldBeFalse();
		
		comp = Composites.createIt("first_name", "Johnny", "last_name", "Cash2", "email", "j.cash@spam.org", "address", "inserted");
		the(comp.isNew()).shouldBeFalse();
    }
}
