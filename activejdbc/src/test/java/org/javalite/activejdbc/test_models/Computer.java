package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsTo;
import org.javalite.activejdbc.annotations.BelongsToParents;

@BelongsToParents({
	@BelongsTo(foreignKeyName="key_id",parent=Keyboard.class),
	@BelongsTo(foreignKeyName="mother_id",parent=Motherboard.class)
})
public class Computer extends Model {}
