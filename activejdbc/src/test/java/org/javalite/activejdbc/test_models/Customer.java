package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.HasMany;
import org.javalite.activejdbc.annotations.IdName;

@HasMany(child = PostalAddress.class, foreignKeyName = "customer_id")
@HasMany(child = PhoneNumber.class, foreignKeyName = "customer_id")
@IdName("customer_id")
public class Customer extends Model {
}
