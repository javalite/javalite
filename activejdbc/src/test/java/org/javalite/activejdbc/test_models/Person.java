/*
Copyright 2009-2019 Igor Polevoy

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


package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Cached;
import org.javalite.conversion.StringToSqlDateConverter;
import org.javalite.conversion.StringToTimestampConverter;
import org.javalite.validation.DateValidator;
import org.javalite.validation.TimestampValidator;


@Cached
public class Person extends Model {
    static{
        validatePresenceOf("name", "last_name");
        validateWith(new DateValidator("dob", "yyyy-MM-dd"));
        validateWith(new TimestampValidator("dob", "yyyy-MM-dd"));
        convertWith(new StringToSqlDateConverter("yyyy-MM-dd"), "dob");

        validateWith(new DateValidator("graduation_date", "yyyy-MM-dd"));
        convertWith(new StringToTimestampConverter("yyyy-MM-dd"), "graduation_date");
    }

    public Person() { }

    @Override
    public void beforeClosingTag(StringBuilder sb, boolean pretty, String indent, String... attributeNames) {
        if (pretty) {
            sb.append(indent);
        }
        sb.append("<test>test content</test>");
        if (pretty) {
            sb.append('\n');
        }
    }

}