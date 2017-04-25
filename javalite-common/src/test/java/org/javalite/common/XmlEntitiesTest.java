/*
Copyright 2009-2016 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


package org.javalite.common;

import org.javalite.test.jspec.JSpecSupport;
import org.junit.Test;


/**
 * @author Igor Polevoy
 */
public class XmlEntitiesTest implements JSpecSupport {

    @Test
    public void testEscape() {
        XmlEntities entities = new XmlEntities();
        entities.addEntity("foo", 0xA1);
        a(entities.escape("\u0021")).shouldBeEqual("!");
        a(entities.escape("\u00A1")).shouldBeEqual("&foo;");
        a(entities.escape("\u00BF")).shouldBeEqual("&#191;");
    }

    @Test
    public void testUnescape() {
        XmlEntities entities = new XmlEntities();
        entities.addEntity("foo", 0xA1);
        a(entities.unescape("!")).shouldBeEqual("\u0021");
        a(entities.unescape("&foo;")).shouldBeEqual("\u00A1");
        a(entities.unescape("&#191;")).shouldBeEqual("\u00BF");
        a(entities.unescape("&#65536;")).shouldBeEqual("&#65536;");
    }
}
