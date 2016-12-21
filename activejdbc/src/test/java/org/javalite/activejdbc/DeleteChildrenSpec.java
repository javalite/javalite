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

import java.util.List;

/**
 * @author igor on 12/20/16.
 */
public class DeleteChildrenSpec extends ActiveJDBCTest {

    @Test
    public void shouldDeleteOne2ManyChildren(){
        deleteAndPopulateTables("users", "addresses");
        User user = User.findById(1);
        List<Address> addresses = user.getAll(Address.class);
        a(3).shouldBeEqual(addresses.size());
        user.deleteChildrenShallow(Address.class);
        a(0).shouldBeEqual(Address.count("user_id = ?", user.getId()));
    }

    @Test
    public void shouldDeletePolymorphicChildren(){
        deleteAndPopulateTables("articles", "posts", "comments");
        Article article = Article.findById(1);
        article.add(Comment.create("author", "ipolevoy", "content", "this is just a test comment text"));
        article.add(Comment.create("author", "miked", "content", "yet another comment"));
        the(article.getAll(Comment.class).size()).shouldBeEqual(2);
        article.deleteChildrenShallow(Comment.class);
        the(article.getAll(Comment.class).size()).shouldBeEqual(0);
    }

    @Test
    public void shouldDeleteMany2ManyLinks(){
        deleteAndPopulateTables("doctors", "patients", "doctors_patients");
        Doctor doctor = Doctor.findById(1);

        the(doctor.getAll(Patient.class).size()).shouldBeEqual(2);

        //before delete:
        the(Patient.count()).shouldBeEqual(3);
        the(DoctorsPatients.count()).shouldBeEqual(4);
        doctor.deleteChildrenShallow(Patient.class);

        //after delete
        the(doctor.getAll(Patient.class).size()).shouldBeEqual(0); // links deleted
        the(DoctorsPatients.count()).shouldBeEqual(2); // links are deleted
        the(Patient.count()).shouldBeEqual(3); // patient count is still the same

    }
}
