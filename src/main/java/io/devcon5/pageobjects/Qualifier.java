package io.devcon5.pageobjects;

/**
 *
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Qualifier annotation to define custom qualifiers for ElementGroups. In case a {@link io.devcon5.pageobjects.Page}
 * or an {@link io.devcon5.pageobjects.ElementGroup} declares more than one element group of the same type, it should
 * be qualified in order to access it via the {@link ElementGroup#get(Class, Class[])}  method.
 *
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Qualifier {

}
