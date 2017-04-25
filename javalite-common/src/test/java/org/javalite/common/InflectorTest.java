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

import junit.framework.TestCase;

import static org.javalite.test.jspec.JSpec.*;


public class InflectorTest extends TestCase {

    public void testGsub(){
        a("octopus").shouldBeEqual(Inflector.gsub("octopi", "(octop|vir)i$", "$1us"));
    }

    public void testUnderscore(){
        a("one_two_three_four").shouldBeEqual(Inflector.underscore("OneTwoThreeFour"));
        a("one_two_three_four").shouldBeEqual(Inflector.underscore("oneTwoThreeFour"));
    }

    public void testPluralize(){
        a("octopi").shouldBeEqual(Inflector.pluralize("octopus"));
        a("vertices").shouldBeEqual(Inflector.pluralize("vertex"));
        a("oxen").shouldBeEqual(Inflector.pluralize("ox"));
        a("books").shouldBeEqual(Inflector.pluralize("book"));
        a("people").shouldBeEqual(Inflector.pluralize("Person"));
        a("children").shouldBeEqual(Inflector.pluralize("Child"));
        a("Addresses").shouldBeEqual(Inflector.pluralize("Address"));
        a("money").shouldBeEqual(Inflector.pluralize("money"));
        a("libraries").shouldBeEqual(Inflector.pluralize("library"));
    }

    public void testSingularize(){
        a("prognosis").shouldBeEqual(Inflector.singularize("prognoses"));
        a("Analysis").shouldBeEqual(Inflector.singularize("Analyses"));
        a("book").shouldBeEqual(Inflector.singularize("books"));
        a("person").shouldBeEqual(Inflector.singularize("people"));
        a("money").shouldBeEqual(Inflector.pluralize("money"));
    }

    public void testTableize(){
        a("grey_ducks").shouldBeEqual(Inflector.tableize("GreyDuck"));
        a("people").shouldBeEqual(Inflector.tableize("Person"));
        a("salaries").shouldBeEqual(Inflector.tableize("Salary"));
        a("money").shouldBeEqual(Inflector.pluralize("money"));
    }


    public void testOtherTableName()  {

        String source = "game";
        String target = "player_game";

        String other = Inflector.getOtherName(source, target);
        a("player").shouldBeEqual(other);

        source = "student_body";
        target = "student_body_assignments";
        other = Inflector.getOtherName(source, target);
        a("assignments").shouldBeEqual(other);


        source = "maple";
        target = "syrup";
        other = Inflector.getOtherName(source, target);
        a(other).shouldBeNull();
    }

    public void testCamelize(){
        a(Inflector.camelize("library_book")).shouldBeEqual("LibraryBook");
        a(Inflector.camelize("library_book", false)).shouldBeEqual("libraryBook");
    }
}
