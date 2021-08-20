package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Article;
import org.javalite.json.JSONList;
import org.javalite.json.JSONMap;
import org.junit.Test;

import static org.javalite.common.Collections.li;
import static org.javalite.common.Collections.map;

public class JSONModelSpec extends ActiveJDBCTest {

    @Test
    public void shouldStoreJSONMap(){

        deleteAndPopulateTable("articles");

        Article a = Article.create("title", "just a test");
        JSONMap jsonMap  = new JSONMap(map("size", 234, "description", "tada!"));
        a.setJSONMap("content", jsonMap);
        a.saveIt();

        a.refresh();

        the(a.getJSONMap("content").get("size")).shouldEqual(234);
        the(a.getJSONMap("content").get("description")).shouldEqual("tada!");
    }

    @Test
    public void shouldStoreJSONList(){

        deleteAndPopulateTable("articles");

        Article a = Article.create("title", "just a test");
        JSONList jsonList  = new JSONList(li("one", 2, "three", 4));
        a.setJSONList("content", jsonList);
        a.saveIt();

        a.refresh();

        the(a.getJSONList("content").get(0)).shouldEqual("one");
        the(a.getJSONList("content").get(1)).shouldEqual(2);
        the(a.getJSONList("content").get(2)).shouldEqual("three");
        the(a.getJSONList("content").get(3)).shouldEqual(4);
    }
}
