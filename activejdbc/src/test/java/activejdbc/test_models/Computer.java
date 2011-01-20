package activejdbc.test_models;

import activejdbc.Model;
import activejdbc.annotations.BelongsTo;
import activejdbc.annotations.BelongsToParents;

@BelongsToParents({
	@BelongsTo(foreignKeyName="key_id",parent=Keyboard.class),
	@BelongsTo(foreignKeyName="mother_id",parent=Motherboard.class)
})
public class Computer extends Model {

}
