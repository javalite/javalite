package org.javalite.activejdbc;

import org.junit.Test;

import java.io.IOException;

import static org.javalite.common.Util.getResourceLines;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy: 4/23/12 12:40 PM
 */
public class Defect153Test {

    @Test
    public void shouldGenerateCorrectDatabaseForDbNameAnnotation() throws IOException {
        for (String line : getResourceLines("/activejdbc_models.properties")) {
            if(line.contains("OtherDbModel")){
                the(line.endsWith("test"));
                return;
            }
        }
        throw new RuntimeException("should not come to this");
    }
}
