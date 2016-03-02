/*
 * Copyright 2015-2016 DevCon5 GmbH, info@devcon5.ch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.devcon5.pageobjects;

import static io.devcon5.classutils.ClassStreams.selfAndSupertypes;
import static io.devcon5.pageobjects.PageObjectsInjector.injectFields;
import static io.devcon5.pageobjects.PageObjectsInjector.injectMethods;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import org.openqa.selenium.SearchContext;

/**
 * An element group is the base class for the entire page object model. Everything - starting from a page - is an
 * element group. Using this interface Java Beans may be defined to define a set of elements that denote the. Groups may
 * be extended and nested. It is a basic
 */
public interface ElementGroup {

    /**
     * Returns the search context for locating elements inside the element group. If this method is not implemented,
     * the default search context is the current webdriver.
     * @return
     *  the search context for the element group.
     */
    default SearchContext getSearchContext() {
        return SeleniumContext.currentDriver()
                              .orElseThrow(() -> new IllegalStateException("Could not obtain current driver outside of test execution"));
    }

    /**
     * Retrieves a nested {@link io.devcon5.pageobjects.ElementGroup} from this group by type. The method assumes there
     * is only one {@link io.devcon5.pageobjects.ElementGroup} of a specific type.
     *
     * @param groupType
     *         the type of the element group to retrieve
     * @param <T>
     *         the type of the {@link io.devcon5.pageobjects.ElementGroup} that should be retrieved
     *
     * @return the element group matching the specified type or an empty optional if no field of the specified type
     * exists on the group.
     */
    @SuppressWarnings("unchecked")
    default <T extends ElementGroup> T get(Class<T> groupType, Class<? extends Annotation>... qualifiers) {
        return (T) selfAndSupertypes(this.getClass())
                .flatMap(c -> Stream.of(c.getDeclaredFields()))
                .filter(f -> groupType.isAssignableFrom(f.getType())
                                        && ( qualifiers.length == 0
                                        || Stream.of(qualifiers).anyMatch(q -> f.getAnnotation(q) != null)))
                .map(f -> {
                    f.setAccessible(true);
                    try {
                        return f.get(this);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Could not retrieve field " + f, e);
                    }
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No element group of type " + groupType + " found"));
    }

    /**
     * Locates all elements specified either by field annotation or method annotation and injects the web element
     * suppliers to each element. To properly inject WebElement Suppliers, the fields must be of type {@code
     * Supplier&lt;WebElement&gt;} and must be annotated with {@link io.devcon5.pageobjects.Locator}. Same applies for setter
     * methods, which must have a return type of void and must accept a single parameter being of type {@code
     * Supplier&lt;WebElement&gt;}
     */
    default void locateElements() {
        //inject fields
        injectFields(this);
        //invoke setters (if present)
        injectMethods(this);
    }

}
