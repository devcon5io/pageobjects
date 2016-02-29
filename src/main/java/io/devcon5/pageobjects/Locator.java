package io.devcon5.pageobjects;

import static io.devcon5.pageobjects.SeleniumContext.currentDriver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

/**
 * A locator annotation to declare how a page or an element can be addressed
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Locator {

    /**
     * The locator string to specify which element should be located
     *
     * @return
     */
    String value();

    /**
     * Specifies the locator type
     *
     * @return
     */
    ByLocator by() default ByLocator.URL;

    /**
     * Timeout in seconds to wait for the element to be present
     *
     * @return
     */
    int timeout() default 60;

    enum ByLocator {
        /**
         * URL are only for pages
         */
        URL(null) {
            @Override
            public Optional<WebElement> locate(String selector) {
                return currentDriver().flatMap(
                        d -> {
                            d.navigate().to(SeleniumContext.resolve(selector));
                            return Optional.empty();
                        });
            }
        },
        ID(org.openqa.selenium.By::id),
        LINK_TEXT(org.openqa.selenium.By::linkText),
        PARTIAL_LINK_TEXT(org.openqa.selenium.By::partialLinkText),
        NAME(org.openqa.selenium.By::name),
        TAG(org.openqa.selenium.By::tagName),
        XPATH(org.openqa.selenium.By::xpath),
        CLASS(org.openqa.selenium.By::className),
        CSS(org.openqa.selenium.By::cssSelector);

        private final Optional<Function<String, org.openqa.selenium.By>> mapper;

        ByLocator(Function<String, org.openqa.selenium.By> mapper) {
            this.mapper = Optional.ofNullable(mapper);
        }

        /**
         * Locates the web element on the current page using the appropriate locator strategy
         *
         * @param selector
         *         the selector for the element to select
         *
         * @return a reference to the element
         */
        public Optional<WebElement> locate(String selector) {
            return currentDriver().flatMap(d -> locate(d, selector));
        }

        /**
         * Locates the web element on the current page using the appropriate locator strategy
         *
         * @param selector
         *         the selector for the element to select
         *
         * @return a reference to the element
         */
        public Optional<WebElement> locate(SearchContext parent, String selector) {
            return mapper.map(by -> parent.findElement(by.apply(selector)));
        }

        /**
         * Transforms the ByLocator to a Selenium {@link org.openqa.selenium.By} literal.
         * @param selector
         *  the selector String
         * @return
         *  the selenium By literal
         */
        public By withSelector(String selector) {
            return mapper.map(by -> by.apply(selector))
                         .orElseThrow(() -> new IllegalArgumentException("Not supported for " + this.name()));
        }
    }
}
