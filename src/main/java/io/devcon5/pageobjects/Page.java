package io.devcon5.pageobjects;

import static io.devcon5.pageobjects.SeleniumContext.currentDriver;

import java.util.Optional;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Interface to declare a page of an application
 */
public interface Page extends ElementGroup{

    /**
     * Default implementation navigates to the url of the page specified by locator annotation. If the page as another
     * mechanism of navigating to it, this method must be overriden.
     */
    default Optional<WebElement> navigateTo() {
        return currentDriver()
                .map(WebDriver::navigate)
                .flatMap(nav ->
                        Optional.ofNullable(this.getClass().getDeclaredAnnotation(Locator.class))
                                .flatMap(l -> l.by().locate(l.value())));
    }

    /**
     * Navigates to a specific page of the page object model.
     *
     * @param pageType
     *         the type of the page
     * @param <T>
     *         the class declaring the page
     *
     * @return an instance of the page
     */
    static <T extends Page> T navigateTo(Class<T> pageType) {
        try {
            T page = pageType.newInstance();
            page.navigateTo().ifPresent(WebElement::click);
            page.locateElements();
            return page;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException();
        }

    }

}
