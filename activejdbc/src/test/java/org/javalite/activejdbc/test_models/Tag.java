package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsToPolymorphic;

/**
 * @author Igor Polevoy on 10/24/14.
 */
@BelongsToPolymorphic(parents = {Article.class, Post.class})
public class Tag extends Model {
}
