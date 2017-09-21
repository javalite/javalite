package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.PartitionIDs;

/**
 * @author igor on 9/20/17.
 */
@PartitionIDs({"user_id", "vehicle"})
public class Passenger extends Model{
}
