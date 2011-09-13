/*
Copyright 2009-2010 Igor Polevoy 

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

package org.javalite.activeweb.freemarker;

import org.junit.Test;

import java.io.StringWriter;

import static org.javalite.common.Collections.map;
import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy
 */
public class ConfirmationTagSpec{

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfTextAttributeNotProvided() throws Exception {
        ConfirmationTag t = new ConfirmationTag();
        StringWriter sw = new StringWriter();
        t.render(map("form", "form1"), "Delete", sw);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfFormAttributeNotProvided() throws Exception {
        ConfirmationTag t = new ConfirmationTag();
        StringWriter sw = new StringWriter();
        t.render(map("text", "are you sure?"), "Delete", sw);
    }


    @Test
    public void shouldGenerateProperLinkWithJS() throws Exception {
        ConfirmationTag t = new ConfirmationTag();
        StringWriter sw = new StringWriter();
        t.render(map("text", "Are you sure?", "form", "123"), "Delete", sw);
        String expected = "<a href=\"#\" onClick=\"if(confirm('Are you sure?')) { $('#' + 123).submit(); return true; } else return false;\">Delete</a>";
        a(sw.toString()).shouldBeEqual(expected);
    }

    @Test
    public void shouldPassArbitraryAttributesThrough() throws Exception {
        ConfirmationTag t = new ConfirmationTag();
        StringWriter sw = new StringWriter();
        t.render(map("text", "Are you sure?", "form", "123", "class", "delete_link"), "Delete", sw);
        String expected = "<a href=\"#\" onClick=\"if(confirm(\'Are you sure?\')) { $('#' + 123).submit(); return true; } else return false;\" class=\"delete_link\">Delete</a>";
        a(sw.toString()).shouldBeEqual(expected);
    }
}
