package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsTo;

/**
 * @author Igor Polevoy: 6/4/12 2:54 PM
 */
@BelongsTo(parent = Node.class, foreignKeyName = "parent_id")
public class Node extends Model {

    public Node(){}

    public Node(String name){
        set("name", name);
    }
}
