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

import static io.devcon5.pageobjects.SeleniumContext.currentDriver;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import com.google.common.base.Predicate;

/**
 * Helper class to locate {@link org.openqa.selenium.WebElement}s by a {@link Locator} literal
 */
public final class WebElementLocator {

    private WebElementLocator(){}

    /**
     * Locates the element using the current driver as search context.
     * @param loc
     *  the locator to specify the element to locate
     * @return
     *  the web element found by the locator. If the element could not be found a NoSuchElementException is thrown
     */
    public static WebElement locate(Locator loc){
        return waitForElement(loc.by().withSelector(loc.value()), loc.timeout()).get();
    }

    /**
     * Locates the element using the search context and locator, waiting for the timeout specified in the locator.
     * @param context
     *  the search context to locate the element in
     * @param loc
     *  the locator to specify the element
     * @return
     *  the web element found by the locator. If the element could not be found a NoSuchElementException is thrown
     */
    public static WebElement locate(SearchContext context, Locator loc){
        return waitForElement(context, loc.by().withSelector(loc.value()), loc.timeout()).get();
    }

    /**
     * Waits for the presence of a specific web element until a timeout is reached. The method will succeed in any case.
     * If the element is not present, the method waits until the timeout, otherwise it returns as soon as the element is
     * present
     *
     * @param context
     *         the search context in which the element should be located
     * @param by
     *         the locate for the element
     * @param waitSec
     *         the timeout in seconds
     *
     * @return the located element
     */
    public static Optional<WebElement> waitForElement(final SearchContext context, final By by, final int waitSec) {
        return currentDriver()
                .map(driver -> {
                    new FluentWait<>(driver)
                            .ignoring(NoSuchElementException.class)
                            .withTimeout(waitSec, TimeUnit.SECONDS)
                            .until(((Predicate<WebDriver>) d -> context.findElement(by).isDisplayed()));
                    return driver.findElement(by);
                });
    }

    /**
     * Waits for the presence of an element until a timeout is reached.
     *
     * @param by
     *         the locator for the element
     * @param waitSec
     *         the timeout. If the timeout is reached, a {@link org.openqa.selenium.NoSuchElementException} is thrown
     *
     * @return the element found
     */
    public static Optional<WebElement> waitForElement(final By by, final int waitSec) {
        return currentDriver().flatMap(d -> waitForElement(d, by, waitSec));
    }

}
