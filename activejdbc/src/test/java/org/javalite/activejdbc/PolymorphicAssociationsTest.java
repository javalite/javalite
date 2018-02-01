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
import org.javalite.activejdbc.test_models.Article;
import org.javalite.activejdbc.test_models.Comment;

import org.javalite.activejdbc.test_models.Post;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author Igor Polevoy
 */
public class PolymorphicAssociationsTest extends ActiveJDBCTest {

    @Test
    public void shouldAddPolymorphicChild() {
        deleteAndPopulateTables("articles", "posts", "comments");
        Article a = Article.findById(1);
        a.add(Comment.create("author", "ipolevoy", "content", "this is just a test comment text"));
        a(Comment.findAll().get(0).get("author")).shouldBeEqual("ipolevoy");
    }

    @Test
    public void shouldFindAllPolymorphicChildren() {
        deleteAndPopulateTables("articles", "posts", "comments");
        Article a = Article.findById(1);
        a.add(Comment.create("author", "ipolevoy", "content", "this is just a test comment text"));
        a.add(Comment.create("author", "rkinderman", "content", "this is another test comment text"));
        List<Comment> comments = a.getAll(Comment.class).orderBy("id");

        a(comments.size()).shouldBeEqual(2);
        a(comments.get(0).get("author")).shouldBeEqual("ipolevoy");

        Post p = Post.findById(2);
        p.add(Comment.create("author", "jjohnes", "content", "this is just a test comment text"));
        p.add(Comment.create("author", "alapsi", "content", "this is another test comment text"));
        p.add(Comment.create("author", "kmandy", "content", "this is just a test comment text"));
        comments = p.getAll(Comment.class).orderBy("id");

        a(comments.size()).shouldBeEqual(3);
        a(comments.get(0).get("author")).shouldBeEqual("jjohnes");
        a(comments.get(1).get("author")).shouldBeEqual("alapsi");
        a(comments.get(2).get("author")).shouldBeEqual("kmandy");

        a(Comment.findAll().size()).shouldBeEqual(5);
    }

    @Test
    public void shouldFindAllPolymorphicChildrenWithCriteria() {
        deleteAndPopulateTables("articles", "posts", "comments");
        Article a = Article.findById(1);
        a.add(Comment.create("author", "ipolevoy", "content", "this is just a test comment text"));
        a.add(Comment.create("author", "rkinderman", "content", "this is another test comment text"));
        List<Comment> comments = a.get(Comment.class, "author = ?", "ipolevoy");
        a(comments.size()).shouldBeEqual(1);

        a(comments.get(0).getString("content").contains("this is just a test comment text")).shouldBeTrue();
    }

    @Test
    public void shouldRemovePolymorphicChildren() {
        deleteAndPopulateTables("articles", "posts", "comments");
        Article a = Article.findById(1);
        a.add(Comment.create("author", "ipolevoy", "content", "this is just a test comment text"));
        a.add(Comment.create("author", "rkinderman", "content", "this is another test comment text"));
        Comment c = (Comment) Comment.findAll().limit(1).orderBy("id").get(0);
        a(a.remove(c)).shouldBeEqual(1);
        a(Comment.findAll().size()).shouldBeEqual(1);
    }


    @Test
    public void shouldInferPolymorphicNames() {
        deleteAndPopulateTables("articles", "posts", "comments");
        Article a = Article.findById(1);
        a.add(Comment.create("author", "ipolevoy", "content", "this is just a test comment text"));
        a.add(Comment.create("author", "rkinderman", "content", "this is another test comment text"));

        a(a.get("comments")).shouldNotBeNull();
        a(Comment.findAll().limit(1).get(0).get("article")).shouldNotBeNull();
    }

    @Test
    public void shouldFindPolymorphicParent() {
        deleteAndPopulateTables("articles", "posts", "comments");
        Article article = Article.findById(1);
        article.add(Comment.create("author", "ipolevoy", "content", "this is just a test comment text"));
        article.add(Comment.create("author", "rkinderman", "content", "this is another test comment text"));
        Article article1 = Comment.findAll().get(0).parent(Article.class);
        the(article.getId()).shouldBeEqual(article1.getId());
    }


    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfWrongParentTypeRequested() {
        deleteAndPopulateTables("articles", "posts", "comments");
        Article article = Article.findById(1);
        article.add(Comment.create("author", "ipolevoy", "content", "this is just a test comment text"));
        article.add(Comment.create("author", "rkinderman", "content", "this is another test comment text"));

        Comment.findAll().get(0).parent(Post.class);
    }

    @Test
    public void shouldFindPolymorphicParentWithInclude(){

        deleteAndPopulateTables("articles", "posts", "comments");
        Article article = Article.findById(1);
        article.add(Comment.create("author", "ipolevoy", "content", "this is just a test comment text"));
        article.add(Comment.create("author", "rkinderman", "content", "this is another test comment text"));


        LazyList<Article> articles = Article.findAll().include(Comment.class).orderBy("id");
        articles.size();

        //compares references.
        the(articles.get(0).getAll(Comment.class).get(0)).shouldBeTheSameAs(articles.get(0).getAll(Comment.class).get(0));

    }

    @Test
    public void shouldFixDefect553CacheEmptyChildren() {
        deleteAndPopulateTables("articles", "posts", "comments");

        LazyList<Article> articles = Article.findAll().include(Comment.class).orderBy("id");

        LazyList<Comment> comments1 = articles.get(0).getAll(Comment.class);
        LazyList<Comment> comments2 = articles.get(0).getAll(Comment.class);

        a(comments1.isEmpty()).shouldBeTrue();
        a(comments1).shouldBeTheSameAs(comments2);
    }

    /**
     * @author Evan Leonard
     */
    @Test
    public void shouldBeAbleToIncludePolymorphicParent() {
        deleteAndPopulateTables("comments", "articles", "posts");

        Post p = Post.findById(1);
        p.add(Comment.create("author", "eleonard", "content", "this is just a test comment text"));

        Article a = Article.findById(2);
        a.add(Comment.create("author", "eleonard", "content", "this is just a test comment text"));

        final LazyList<Comment> comments = Comment.findAll().orderBy("id").include(Article.class, Post.class);

        final List<Map<String, Object>> commentMaps = comments.toMaps();

        final Map post = (Map) commentMaps.get(0).get("post");
        the(post.get("id")).shouldBeEqual(1);

        final Map article = (Map) commentMaps.get(1).get("article");
        the(article.get("id")).shouldBeEqual(2);

        //ensure we get the same reference
        the(comments.get(0).parent(Post.class)).shouldBeTheSameAs(comments.get(0).parent(Post.class));
    }
}
