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

import com.google.common.base.Predicate;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Interface to declare a page of an application
 */
public interface Page extends ElementGroup {

    /**
     * Default implementation navigates to the url of the page specified by locator annotation. If the page as another
     * mechanism of navigating to it, this method must be overriden.
     */
    default Optional<WebElement> navigateTo() {

        return currentDriver().map(WebDriver::navigate)
                              .flatMap(nav -> Optional.ofNullable(this.getClass().getDeclaredAnnotation(Locator.class))
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
            currentDriver().map(d -> new WebDriverWait(d, 150, 50))
                           .orElseThrow(() -> new IllegalStateException("Context not initialized"))
                           .until((Predicate<WebDriver>) d -> "complete".equals(((JavascriptExecutor) d).executeScript(
                                   "return document.readyState")));
            page.locateElements();
            return page;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

}
