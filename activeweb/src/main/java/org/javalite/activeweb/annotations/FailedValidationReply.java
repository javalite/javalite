package org.javalite.activeweb.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation is for automatic reply  to HTTP requests in case validation did not pass.
 * Apply this annotation to classes that serve as arguments to controller action methods (models or POJOs).
 * <p>
 *     The response  will be in the JSON format, since it seems to the linga franka of the Internet.
 * </p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FailedValidationReply {
    int value();
}
