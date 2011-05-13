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

package app.controllers;

import activeweb.DBControllerSpec;
import app.models.Post;
import javalite.test.XPathHelper;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author Igor Polevoy
 */
public class PostsControllerSpec extends DBControllerSpec {

    @Test
    public void shouldPullAllPostsFromDb() {

        Post.createIt("author", "John", "title", "post 1", "content", "fake content");
        Post.createIt("author", "John", "title", "post 2", "content", "fake content");
        Post.createIt("author", "John", "title", "post 3", "content", "fake content");

        request().get("index");
        List<Map> postMaps = (List<Map>) assigns().get("posts");

        a(postMaps.get(0).get("title")).shouldEqual("post 1");
        a(postMaps.get(1).get("title")).shouldEqual("post 2");
        a(postMaps.get(2).get("title")).shouldEqual("post 3");
    }

    @Test
    public void shouldCreateNewPost(){

        session().setAttribute("captcha", "ABC");

        request().params("author", "John", "content", "this is a content of a post", "title", "fake title", "captcha", "ABC").post("add");
        Post p = (Post)Post.findAll().get(0);

        a(p.get("title")).shouldBeEqual("fake title");
        a(p.get("author")).shouldBeEqual("John");

        Map flasher = (Map) session().getAttribute("flasher");
        a(flasher.get("post_saved")).shouldEqual("Your post was saved");

        a(redirectValue()).shouldEqual("/test_context/posts");
    }


    @Test
    public void shouldDeleteAPost(){
        Post p = (Post)Post.createIt("author", "John", "title", "post 1", "content", "fake content");

        request().param("id", p.getId()).delete("delete");

        a(Post.count()).shouldEqual(0);
        
        Map flasher = (Map) session().getAttribute("flasher");
        a(flasher.get("post_deleted")).shouldEqual("Your post was deleted");

        a(redirectValue()).shouldEqual("/test_context/posts");
    }

    @Test
    public void shouldViewPost(){
        Post p = (Post)Post.createIt("author", "John", "title", "post 1", "content", "fake content");

        request().param("id", p.getId()).get("view");

        Map post = (Map) assigns().get("post");
        a(post.get("title")).shouldEqual("post 1");
    }

    @Test
    public void shouldEditPost(){
        Post p = (Post)Post.createIt("author", "John", "title", "post 1", "content", "fake content");

        request().param("id", p.getId()).integrateViews().get("edit_post");
        
        XPathHelper h = new XPathHelper(responseContent());
        a(h.attributeValue("//input[@type='hidden']/@value")).shouldBeEqual(p.getId().toString());
        a(h.attributeValue("//input[@name='author']/@value")).shouldBeEqual(p.get("author"));
        a(h.attributeValue("//input[@name='title']/@value")).shouldBeEqual(p.get("title"));
        a(h.selectText("//textarea[@name='content']")).shouldBeEqual(p.get("content"));
    }
}
