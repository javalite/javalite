package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Many2Many;

/**
 * @author Christof Schablinski
 */
@Many2Many(other=Genre.class,
    join = "bands_genres", sourceFKName = "band_id", targetFKName = "genre_id")
@Many2Many(other=Musician.class,
    join = "bands_musicians", sourceFKName = "band_id", targetFKName = "musician_id")
@IdName("band_id")
public class Band extends Model
{
}
