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

package org.javalite.activejdbc.validation;

import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.MetaModel;
import org.javalite.activejdbc.Model;

import static org.javalite.activejdbc.ModelDelegate.metaModelOf;


/**
 * Naive uniqueness validator. If enabled, prevents adding a new record that has the same attribute
 * if one already exists in the corresponding table.
 *
 * <p></p>
 * <strong>Critique</strong>:  in a high-load system, it might be possible to create duplicate records due to bad timing
 * (another thread inserted a record after you tested for uniqueness, but before you inserted).
 * <p></p>
 *
 * <strong>Suggestion</strong>: Use for small non-critical project. For real world projects it is recommended to
 * maintain uniqueness with database indexes and  <a href="https://en.wikipedia.org/wiki/Unique_key">unique keys</a>.
 *
 */
public class UniquenessValidator extends ValidatorAdapter {
    private final String attribute;

    public UniquenessValidator(String attribute) {
        this.attribute = attribute;
        setMessage("should be unique");
    }

    @Override
    public void validate(Model model) {
        MetaModel metaModel = metaModelOf(model.getClass());
        if (new DB(metaModel.getDbName()).count(metaModel.getTableName(), attribute + " = ?", model.get(attribute)) > 0) {
            model.addValidator(this, attribute);
        }
    }
}