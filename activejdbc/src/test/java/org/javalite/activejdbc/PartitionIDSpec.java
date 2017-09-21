package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Passenger;
import org.javalite.common.Util;
import org.javalite.test.SystemStreamUtil;
import org.junit.Test;

import java.io.IOException;

/**
 * @author igor on 9/20/17.
 */
public class PartitionIDSpec extends ActiveJDBCTest {

    @Test
    public void shouldIncludePartitionColumnsIntoUPDATEAndDELETE() throws IOException {
        SystemStreamUtil.replaceOut();

        Passenger p = new Passenger();
        p.set("user_id", 1, "vehicle", "bike", "mode", "pavement").saveIt(); // create
        p.set("mode", "water").saveIt();                                     // update
        p.delete();                                                          // delete

        String out = SystemStreamUtil.getSystemOut();

        String x = find(out, "UPDATE passengers");
        the(find(out, "UPDATE passengers")).shouldContain("user_id = ?");
        the(find(out, "UPDATE passengers")).shouldContain("vehicle = ?");

        the(find(out, "DELETE FROM passengers")).shouldContain("user_id = ?");
        the(find(out, "DELETE FROM passengers")).shouldContain("vehicle = ?");

        the(Passenger.count()).shouldBeEqual(0);

        SystemStreamUtil.restoreSystemOut();
    }

    private String find(String out, String what){
        String[] lines = Util.split(out, "\n");
        for (String line : lines) {
            String lc = line.toLowerCase();
            if(lc.contains(what.toLowerCase())){
                return lc;
            }
        }
        return null;
    }
}
