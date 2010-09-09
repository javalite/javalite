package activejdbc;

import activejdbc.test.ActiveJDBCTest;
import activejdbc.test_models.*;
import org.junit.Test;

import java.util.List;

/**
 * @author Igor Polevoy
 */
public class DeleteCascadeTest extends ActiveJDBCTest{

    @Test 
    public void shouldDeleteOneToManyChildren(){
        resetTables("users", "addresses");

        //verify total count before delete
        a(Address.findAll().size()).shouldBeEqual(7);

        User.<Model>findById(1).deleteCascade();

        //verify total count after delete
        a(Address.findAll().size()).shouldBeEqual(4);
        
        //verify that no relations left in child table
        a(Address.where("user_id = ?", 1).size()).shouldBeEqual(0);
    }

    @Test
    public void shouldDeletePolymorphicChildren(){

        resetTables("articles", "posts", "comments");
        Article a = (Article) Article.findById(1);
        a.add(Comment.create("author", "ipolevoy", "content", "this is just a test comment text"));
        a.add(Comment.create("author", "rkinderman", "content", "this is another test comment text"));


        Post p = (Post) Post.findById(2);
        p.add(Comment.create("author", "jjohnes", "content", "this is just a test comment text"));
        p.add(Comment.create("author", "alapsi", "content", "this is another test comment text"));
        p.add(Comment.create("author", "kmandy", "content", "this is just a test comment text"));

        a(Comment.findAll().size()).shouldBeEqual(5);
        a.deleteCascade();
        a(Comment.findAll().size()).shouldBeEqual(3);

        a(Comment.where("parent_type = ? and parent_id = ?", "activejdbc.test_models.Article", 1).size()).shouldBeEqual(0);

    }


    @Test
    public void shouldRemoveJoinLinksWHenDeleted() {
        resetTables("doctors", "patients", "doctors_patients");
        Doctor doctorNumberOne = (Doctor)Doctor.findById(1);
        doctorNumberOne.deleteCascade();
        a(Base.findAll("select * from doctors_patients").size()).shouldBeEqual(1);
    }

}
