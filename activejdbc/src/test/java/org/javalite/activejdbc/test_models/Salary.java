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
import org.javalite.validation.NumericValidator;

import java.text.NumberFormat;
import java.util.Locale;

public class Salary extends Model {
    static{
        validateWith(new NumericValidator("salary", NumberFormat.getCurrencyInstance(Locale.US)));
        validatePresenceOf("salary").message("salary is missing!!!");
        zeroToNull("salary");
    }
}
