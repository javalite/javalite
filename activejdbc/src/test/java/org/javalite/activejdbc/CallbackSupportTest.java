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
import org.javalite.activejdbc.test_models.*;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class CallbackSupportTest extends ActiveJDBCTest {

    @Test
    public void shouldCallBeforeAfterSaveAndDelete(){
        deleteAndPopulateTable("fruits");
        FruitCallbackChecker cc = new FruitCallbackChecker();
        Fruit f = new Fruit(cc);        

        f.set("fruit_name", "apple");
        f.set("category", "tree fruit");
        f.saveIt();

        a(cc.beforeSaveCalled).shouldBeTrue();
        a(cc.beforeCreateCalled).shouldBeTrue();

        a(cc.afterSaveCalled).shouldBeTrue();
        a(cc.afterCreateCalled).shouldBeTrue();

        a(cc.beforeDeleteCalled).shouldBeFalse();
        a(cc.afterDeleteCalled).shouldBeFalse();

        f.delete();

        a(cc.beforeDeleteCalled).shouldBeTrue();
        a(cc.afterDeleteCalled).shouldBeTrue();
    }


    @Test
    public void shouldCallBeforeAfterValidate(){
        deleteAndPopulateTable("vegetables");
        VegetableCallbackChecker cc = new VegetableCallbackChecker();
        Vegetable v = new Vegetable(cc);

        a(cc.beforeValidationCalled).shouldBeFalse();
        a(cc.afterValidationCalled).shouldBeFalse();
        v.validate();

        a(cc.beforeValidationCalled).shouldBeTrue();
        a(cc.afterValidationCalled).shouldBeTrue();
    }

    @Test
    public void shouldCallbackAllMethodsOnCallbackImplementation(){
        deleteAndPopulateTable("plants");
        Plant plant = new Plant();

        plant.set("plant_name", "Fern");
        plant.saveIt();

        a(PlantCallback.instance().beforeSaveCalled).shouldBeTrue();
        a(PlantCallback.instance().beforeCreateCalled).shouldBeTrue();

        a(PlantCallback.instance().afterCreateCalled).shouldBeTrue();
        a(PlantCallback.instance().afterSaveCalled).shouldBeTrue();

        a(PlantCallback.instance().beforeValidationCalled).shouldBeTrue();
        a(PlantCallback.instance().afterValidationCalled).shouldBeTrue();

        a(PlantCallback.instance().beforeDeleteCalled).shouldBeFalse();
        a(PlantCallback.instance().afterDeleteCalled).shouldBeFalse();

        plant.delete();

        a(PlantCallback.instance().beforeDeleteCalled).shouldBeTrue();
        a(PlantCallback.instance().afterDeleteCalled).shouldBeTrue();
    }
}                          
