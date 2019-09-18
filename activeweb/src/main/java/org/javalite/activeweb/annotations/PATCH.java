package org.javalite.activeweb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark an action of a controller with this annotation to receive an HTTP POST request.
 *
 * @author Mikhail Chachkouski
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PATCH {}
