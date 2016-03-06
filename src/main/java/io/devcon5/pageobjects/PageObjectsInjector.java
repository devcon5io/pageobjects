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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import io.devcon5.classutils.ClassStreams;
import io.devcon5.pageobjects.tx.TransactionHelper;
import io.devcon5.pageobjects.tx.TransactionSupport;

/**
 * Injector to inject WebElement suppliers to Fields and Methods of a Page
 */
public final class PageObjectsInjector {

    private PageObjectsInjector() {
    }

    /**
     * Injects WebElement suppliers in all setter methods according to the {@link io.devcon5.pageobjects.Locator} annotation on
     * that method
     *
     * @param group
     *         the page into which setter methods should be invoked
     */
    public static void injectMethods(ElementGroup group) {

        ClassStreams.selfAndSupertypes(group.getClass())
                    .flatMap(c -> Stream.of(c.getDeclaredMethods()))
                    .filter(m -> void.class.isAssignableFrom(m.getReturnType())
                            && m.getParameterCount() == 1
                            && Supplier.class.isAssignableFrom(m.getParameterTypes()[0]))
                    .forEach(m -> Optional.ofNullable(m.getDeclaredAnnotation(Locator.class)).ifPresent(
                                    loc -> invokeSetter(m, group, loc)));

    }

    /**
     * Invokes the specified setter method to inject a WebElement supplier
     * @param m
     *  the setter method to invoke
     * @param target
     *  the target element on which to invoke the method and which acts a search context for locating the
     *  elements
     * @param loc
     *  the locator to locate the web element
     */
    private static void invokeSetter(Method m, ElementGroup target, Locator loc) {
        m.setAccessible(true);
        try {
            m.invoke(target, (Supplier<WebElement>) () -> WebElementLocator.locate(target.getSearchContext(), loc));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Could not init " + m, e);
        }
    }

    /**
     * Injects WebElement suppliers in all fields according to the {@link io.devcon5.pageobjects.Locator} annotation on that
     * field
     *
     * @param group
     *         the page into which fields should be injected
     */
    public static void injectFields(ElementGroup group) {
        injectWebElements(group);
        injectElementGroups(group);

    }

    /**
     * Inject WebElement Suppliers into fields of the element group
     * @param group
     *  the element group to inject web elements into
     */
    private static void injectWebElements(ElementGroup group) {
        ClassStreams.selfAndSupertypes(group.getClass())
                    .flatMap(c -> Stream.of(c.getDeclaredFields()))
                    .filter(f -> Supplier.class.isAssignableFrom(f.getType()))
                    .forEach(f -> Optional.ofNullable(f.getDeclaredAnnotation(Locator.class)).ifPresent(
                                    loc -> injectWebElement(group, f, loc)));
    }

    /**
     * Creates and injects element groups into the specified group.
     * @param group
     *  the elmeent group to inject element groups into
     */
    private static void injectElementGroups(ElementGroup group) {
        ClassStreams.selfAndSupertypes(group.getClass())
                    .flatMap(c -> Stream.of(c.getDeclaredFields()))
                    .filter(f -> ElementGroup.class.isAssignableFrom(f.getType()))
                    .forEach(f -> injectElementGroup(f, group));
    }

    /**
     * Injects a WebElement supplier into the specified field of the target.
     * @param target
     *  the target object into which the webelement supplier should be injected
     * @param field
     *  the field declaration into which the instance should be injected
     * @param locator
     *  the locator declaring how the web element should be located
     */
    public static void injectWebElement(ElementGroup target, Field field, Locator locator) {
        field.setAccessible(true);
        try {
            field.set(target, (Supplier<WebElement>) () -> WebElementLocator.locate(target.getSearchContext(), locator));
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not init " + field, e);
        }
    }

    /**
     * Injects a new instance of an element group into the target field.
     * @param target
     *  the target field to inject the new element group into
     * @param parent
     *  the parent element group. It is not just the instance containing the target field but acts also as parent
     *  search context for all elements inside the the group.
     */
    private static void injectElementGroup(Field target, ElementGroup parent) {
        try {
            final Class<? extends ElementGroup> elementGroupType = (Class<? extends ElementGroup>) target.getType();
            ElementGroup nestedGroup =
                    Optional.ofNullable(target.getAnnotation(Locator.class))
                            .map(loc -> createContextualInstance(elementGroupType, loc, parent))
                            .orElseGet(() -> createDefaultInstance(elementGroupType));
            target.setAccessible(true);
            injectFields(nestedGroup);
            if(TransactionSupport.class.isAssignableFrom(target.getDeclaringClass())){
                nestedGroup = TransactionHelper.addTransactionSupport((TransactionSupport)nestedGroup);
            }
            target.set(parent, nestedGroup);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not init element group", e);
        }
    }

    /**
     * Creates a new instance of the specified element group type using the given web context. If the element group type
     * does not declare a constructor accepting a single parameter of type {@link org.openqa.selenium.SearchContext} the
     * provided context is used to create the instance. If the type does not declare such a constructor an instance is
     * created using the default constructor.
     *
     * @param elementGroupType
     *         the type of the elementgroup to create
     * @param loc
     *         the locator declaration to locate elements inside the grouop
     * @param parent
     *         the parent element group the new instance will be nested inside
     *
     * @return a new element group instance
     */
    private static ElementGroup createContextualInstance(Class<? extends ElementGroup> elementGroupType, Locator loc, ElementGroup parent) {
        return loc.by().locate(parent.getSearchContext(), loc.value())
                  .map(context -> createContextualInstance(elementGroupType, context))
                  .orElseGet(() -> Optional.of(createDefaultInstance(elementGroupType)))
                  .get();
    }

    /**
     * Creates a new instance of the specified element group type using the given web context. If the element group type
     * does not declare a constructor accepting a single parameter of type {@link org.openqa.selenium.SearchContext} the
     * provided context is used to create the instance.
     *
     * @param elementGroupType
     *         the type of the elementgroup to create
     * @param context
     *         the search context to locate the elements of the element group to create
     *
     * @return an optional instance of the element group. If no matching constructor exists, the optional is empty.
     */
    private static Optional<ElementGroup> createContextualInstance(Class<? extends ElementGroup> elementGroupType, SearchContext context) {
        return Stream.of(elementGroupType.getConstructors())
                     .filter(c -> c.getParameterCount() == 1
                             && SearchContext.class.isAssignableFrom(c.getParameterTypes()[0]))
                     .map(c -> {
                         try {
                             c.setAccessible(true);
                             return (ElementGroup) c.newInstance(context);
                         } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                             throw new RuntimeException("Could not create element group " + elementGroupType, e);
                         }
                     })
                     .findFirst();
    }

    /**
     * Creates an instance using the default constructor
     *
     * @param elementGroupType
     *         the type of the element group to instantiate
     *
     * @return an element group instance
     */
    public static ElementGroup createDefaultInstance(Class<?> elementGroupType) {
        try {
            return (ElementGroup) elementGroupType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Could not create element group using default constructor", e);
        }
    }

}
