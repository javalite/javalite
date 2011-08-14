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

package org.javalite.activejdbc;

import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.*;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author Stephane Restani
 */
public class HierarchyTest extends ActiveJDBCTest {

	/**
	 * This test verifies that single-table inheritance is not possible.
	 * Cheese extends Meal which are both non-abstract classes
	 */
    @Ignore
	@Test(expected = org.javalite.activejdbc.DBException.class)
	public void shouldFailSTI() {
		Cheese.count();
	}

	/**
	 * This test verifies that one level of abstract inheritance is possible.
	 * Sword extends (abstract) Weapon
	 */
	@Test
	public void shouldAcceptSingleLevelInheritance() {
		Sword.count();	
	}
	
	/**
	 * This test verifies that multiple levels of abstract inheritance are possible.
	 * Cake extends (abstract) Pastry which extends (abstract) Dessert
	 */
	@Test
	public void shouldAcceptMultipleLevelsInheritance() {
		Cake.count();
	}
}
