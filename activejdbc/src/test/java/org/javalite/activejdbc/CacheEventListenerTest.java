package org.javalite.activejdbc;

import org.javalite.activejdbc.cache.CacheEvent;
import org.javalite.activejdbc.cache.CacheEventListener;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Doctor;
import org.javalite.activejdbc.test_models.Patient;
import org.javalite.activejdbc.test_models.Person;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Convert.toSqlDate;


/**
 * @author Igor Polevoy
 */
public class CacheEventListenerTest extends ActiveJDBCTest {

    @After
    public void tearDown(){
        Registry.cacheManager().removeAllCacheEventListeners();
    }

    @Test
    public void shouldDelegateCachePurgeEventsToListener(){

        class TestCacheEventListener implements CacheEventListener {
            public int groupCount = 0, allCount = 0;
            public String groupName;

            public void onFlush(CacheEvent event) {
                if (event.getType().equals(CacheEvent.CacheEventType.ALL)) {
                    allCount += 1;
                } else if (event.getType().equals(CacheEvent.CacheEventType.GROUP)) {
                    groupCount += 1;
                    groupName = event.getGroup();
                }
            }
        }
        TestCacheEventListener listener = new TestCacheEventListener();
        Registry.cacheManager().addCacheEventListener(listener);

        //flush people first time:
        Person.createIt("name", "Matt", "last_name", "Diamont", "dob", toSqlDate("1962-01-01"));
        a(listener.groupCount).shouldBeEqual(1);
        a(listener.groupName).shouldBeEqual("people");

        //flush people second time:
        Person.purgeCache();
        a(listener.groupCount).shouldBeEqual(2);

        a(listener.allCount).shouldBeEqual(0);
        //flush all
        Registry.cacheManager().flush(CacheEvent.ALL);
        a(listener.allCount).shouldBeEqual(1);
    }


    @Test
    public void shouldNotBreakIfListenerThrowsException() throws IOException {

        final boolean[] triggered = {false};

        class BadEventListener implements CacheEventListener {
            public void onFlush(CacheEvent event) {
                triggered[0] = true;
                throw new RuntimeException("I'm a bad, baaad listener...."); 
            }
        }
        BadEventListener listener = new BadEventListener();
        Registry.cacheManager().addCacheEventListener(listener);
        Person.createIt("name", "Matt", "last_name", "Diamont", "dob", toSqlDate("1962-01-01"));

        a(triggered[0]).shouldBeTrue();
    }

    @Test
    public void shouldSendSpecificNumberOfCacheEvents() {
        deleteAndPopulateTables("doctors", "patients", "doctors_patients", "prescriptions");
        class TestCacheEventListener implements CacheEventListener {
            Map<String, Integer> events = new HashMap<>();
            public void onFlush(CacheEvent event) {
                String key = event.getType() == CacheEvent.CacheEventType.ALL ? "*" : event.getGroup();
                Integer count = events.get(key);
                if (count == null) {
                    count = 1;
                }
                events.put(key, count);
            }
        }
        TestCacheEventListener listener = new TestCacheEventListener();
        Registry.cacheManager().addCacheEventListener(listener);
        Doctor doctor = Doctor.findFirst("first_name = ?", "John");
        a(doctor).shouldEqual(Doctor.findFirst("first_name = ?", "John")); //cached
        List<Patient> patients = doctor.getAll(Patient.class);
        a(patients).shouldNotBeEqual(doctor.getAll(Patient.class));
        a(patients.size()>1).shouldBeTrue();
        Patient patient = patients.get(0);
        patient.delete();
        a(listener.events.get("patients")).shouldBeNull();
        a(listener.events.get("doctors")).shouldBeEqual(1);
        a(listener.events.get("patient_cards")).shouldBeEqual(1);
        a(listener.events.get("prescriptions")).shouldBeNull();
        a(listener.events.size()).shouldBeEqual(2);

    }

}
