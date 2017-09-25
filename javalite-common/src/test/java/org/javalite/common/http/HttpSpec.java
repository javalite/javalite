package org.javalite.common.http;

import org.javalite.http.Http;
import org.junit.Test;

import static org.javalite.common.Collections.map;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy: 2/22/12 1:29 PM
 */
public class HttpSpec {

    @Test
    public void shouldGenerateParametersFromMap(){
        String content = Http.map2URLEncoded(map("first_name", "John", "last_name", "Doe", "ssn", "123-45-6789"));

        the(content).shouldContain("first_name=John");
        the(content).shouldContain("ssn=123-45-6789");
        the(content).shouldContain("last_name=Doe");
    }
}
