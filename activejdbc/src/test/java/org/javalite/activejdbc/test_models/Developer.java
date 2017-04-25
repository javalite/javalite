package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.CompositePK;

@CompositePK({ "first_name", "last_name", "email" })
public class Developer extends Model {
	static {
		validatePresenceOf("first_name", "last_name", "email").message(
				"one or more composite PK's missing!!!");
	}
}