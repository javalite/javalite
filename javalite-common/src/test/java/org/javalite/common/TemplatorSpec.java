package org.javalite.common;

import org.junit.Test;

import static org.javalite.common.Collections.map;
import static org.javalite.test.jspec.JSpec.a;

/**
 * @author igor on 9/30/16.
 */
public class TemplatorSpec {

    @Test
    public void shouldMergeTemplateWithTemplatorInstance(){
        Templator t = new Templator("/templator/hello.txt");
        a(t.merge(map("name", "Igor", "greeting", "Hi"))).shouldBeEqual("Hi, Igor");
    }

    @Test
    public void shouldMergeTemplateWithStaticTemplator(){
        a(Templator.mergeFromPath("/templator/hello.txt", map("name", "Igor", "greeting", "Hi"))).shouldBeEqual("Hi, Igor");
    }
}


