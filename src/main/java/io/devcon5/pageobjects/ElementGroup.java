package io.devcon5.pageobjects;

import static io.devcon5.pageobjects.PageObjectsInjector.injectFields;
import static io.devcon5.pageobjects.PageObjectsInjector.injectMethods;

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
