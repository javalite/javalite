package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.HasMany;
import org.javalite.activejdbc.annotations.IdName;

@HasMany(child = Player.class, foreignKeyName = "team_id")
@IdName("team_id")
public class Team extends Model
{
}
