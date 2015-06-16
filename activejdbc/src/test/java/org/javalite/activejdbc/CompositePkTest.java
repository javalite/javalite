package org.javalite.activejdbc;

import java.util.NoSuchElementException;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Developer;
import org.junit.Test;

/**
 * @author comtel2000
 */
public class CompositePkTest extends ActiveJDBCTest {

    @Test
    public void shouldInsertAndUpdate(){

        Developer dev1 = new Developer();
        dev1.set("first_name", "John", "last_name", "Smith", "email", "smithy@spam.org", "address", "bla");
        dev1.saveIt();

        Developer dev2 = new Developer();
        dev2.set("first_name", "John", "last_name", "Smith2", "email", "smithy@spam.org", "address", "blabla");
        dev2.saveIt();

        the(Developer.count()).shouldBeEqual(2);
        the(Developer.findByCompositeKeys("John", "Smith", "smithy@spam.org").get("address")).shouldBeEqual("bla");
        the(Developer.findByCompositeKeys("John", "Smith2", "smithy@spam.org").get("address")).shouldBeEqual("blabla");
        
        Developer comp3 = Developer.findByCompositeKeys("John", "Smith2", "smithy@spam.org");
        comp3.set("address", "blablabla");
        comp3.saveIt();
        the(Developer.findByCompositeKeys("John", "Smith2", "smithy@spam.org").get("address")).shouldBeEqual("blablabla");
        
    }
    
    @Test(expected = NoSuchElementException.class)
    public void shouldGenerateNoSuchElementFromBlankUpdate() {
    	Developer c = new Developer();
    	c.toUpdate();
    }
    
    @Test
    public void shouldGenerateValidSQL() {
        Developer dev = new Developer();
        dev.set("first_name", "Johnny", "last_name", "Cash", "email", "j.cash@spam.org", "address", "123 Pine St");
        the(Base.exec(dev.toInsert())).shouldBeEqual(1);
        dev.set("address", "updated");
        the(Base.exec(dev.toUpdate())).shouldBeEqual(1);

        dev = Developer.findByCompositeKeys("Johnny", "Cash", "j.cash@spam.org");
        
        the(dev.get("first_name")).shouldBeEqual("Johnny");
        the(dev.get("last_name")).shouldBeEqual("Cash");
        the(dev.get("email")).shouldBeEqual("j.cash@spam.org");
        the(dev.get("address")).shouldBeEqual("updated");
        
        dev = Developer.findByCompositeKeys("Johnny", "WrongName", "j.cash@spam.org");
        the(dev).shouldBeNull();
    }
    
    @Test
    public void shouldReturnNullWithWrongPKs() {
    	Developer.createIt("first_name", "Johnny", "last_name", "Cash", "email", "j.cash@spam.org", "address", "123 Pine St");
    	the(Developer.findByCompositeKeys("Johnny", "Casher", "j.cash@spam.org")).shouldBeNull();
    	the(Developer.findByCompositeKeys("John", "Cash", "j.cash@spam.org")).shouldBeNull();
    	the(Developer.findByCompositeKeys("Johnny", "Cash", "j.cash@spam.com")).shouldBeNull();
    }
    
    @Test
    public void shouldDeleteByPK() {
    	Developer.createIt("first_name", "Johnny", "last_name", "Cash", "email", "j.cash@spam.org", "address", "123 Pine St");
    	Developer dev = Developer.findByCompositeKeys("Johnny", "Cash", "j.cash@spam.org");
		the(dev).shouldNotBeNull();
		the(Developer.count()).shouldBeEqual(1);
		dev.delete();
		the(Developer.count()).shouldBeEqual(0);
		the(dev.isFrozen()).shouldBeTrue();
    }
    
    @Test
    public void shouldBeNew() {
    	
        Developer dev = new Developer();
        dev.set("first_name", "Johnny", "last_name", "Cash", "email", "j.cash@spam.org", "address", "123 Pine St");
        the(dev.isNew()).shouldBeTrue();
        
    	Developer.createIt("first_name", "Johnny", "last_name", "Cash", "email", "j.cash@spam.org", "address", "123 Pine St");
    	dev = Developer.findByCompositeKeys("Johnny", "Cash", "j.cash@spam.org");
		the(dev).shouldNotBeNull();
		the(dev.isNew()).shouldBeFalse();
		
		dev = Developer.createIt("first_name", "Johnny", "last_name", "Cash2", "email", "j.cash@spam.org", "address", "123 Pine St");
		the(dev.isNew()).shouldBeFalse();
    }
}
