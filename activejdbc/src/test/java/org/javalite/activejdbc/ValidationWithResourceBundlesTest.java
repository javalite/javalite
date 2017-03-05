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
package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.School;
import org.junit.Test;

import java.util.Locale;

/**
 * @author Igor Polevoy
 */
public class ValidationWithResourceBundlesTest extends ActiveJDBCTest{

    @Test
    public void shouldPullCorrectMessageFromResourceBundle(){
        School s = new School();
        s.validate();
        a(s.errors().get("school_name")).shouldBeEqual("School name missing");
    }

    @Test
    public void shouldPullCorrectMessageInGermanFromResourceBundle(){
        School s = new School();
        s.validate();
        a(s.errors(new Locale("de", "DE")).get("school_name")).shouldBeEqual("Name der Schule nicht vorgesehen");
    }

    @Test
    public void shouldReturnProvidedMessageIfNotFoundInResourceBundle(){
        School s = new School();
        s.validate();
        a(s.errors().get("school_type")).shouldBeEqual("school.type.missing");
    }

    @Test
    public void shouldPullCorrectMessageWithParameters(){
        School s = new School();
        s.set("school_name", "School of Computer Science");
        s.set("email", "computer#science.edu");
        s.validate();
        a(s.errors().get("email", s.get("school_name"), s.get("email"), "computer@science.edu"))
                .shouldBeEqual("Email format for school School of Computer Science is incorrect: computer#science.edu, a proper format would be something like this: computer@science.edu");
    }

    @Test
    public void shouldPullCorrectMessageWithParametersGerman(){
        School s = new School();
        s.set("school_name", "School of Computer Science");
        s.set("email", "computer#science.edu");
        s.validate();
        a(s.errors(new Locale("de", "DE")).get("email", s.get("school_name"), s.get("email"), "computer@science.edu"))
                .shouldBeEqual("EMail Format f\u00fcr die Schule School of Computer Science ist falsch: computer#science.edu, ein geeignetes Format w\u00e4re so etwas wie dieses: computer@science.edu");
    }

    @Test
    public void shouldGetAttributesAndErrorsCaseInsensitive() {
        School s = new School();
        s.set("school_name", "School of Computer Science");
        s.set("email", "computer#science.edu");
        s.validate();
        a(s.errors().get("Email", s.get("School_Name"), s.get("Email"), "computer@science.edu")).shouldBeEqual(
                s.errors().get("email", s.get("school_name"), s.get("email"), "computer@science.edu"));
        a(s.errors().get("EMAIL", s.get("SCHOOL_NAME"), s.get("EMAIL"), "computer@science.edu")).shouldBeEqual(
                s.errors().get("email", s.get("school_name"), s.get("email"), "computer@science.edu"));
    }
}
