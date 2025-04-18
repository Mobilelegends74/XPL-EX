package org.luaj.vm2.attributes;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as non-invokable from Lua scripts
 * when using the secure wrapper mechanism.
 */
@Retention(RetentionPolicy.RUNTIME) // Important: Needs to be available at runtime for reflection
@Target(ElementType.METHOD)       // Applicable only to methods
public @interface NonInvokable {
}