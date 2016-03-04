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
import static io.devcon5.pageobjects.tx.TransactionHelper.getClassTxName;

import java.util.Optional;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.google.common.base.Predicate;

import io.devcon5.pageobjects.tx.Transactional;

/**
 * Interface to declare a page of an application
 */
public interface Page extends ElementGroup {

    /**
     * Default implementation navigates to the url of the page specified by locator annotation. If the page as another
     * mechanism of navigating to it, this method must be overriden.
     */
    default void loadPage() {
        currentDriver().map(driver -> {
            Optional.ofNullable(this.getClass().getAnnotation(Locator.class))
                    .flatMap(l -> l.by().locate(l.value()))
                    .ifPresent(WebElement::click);
            new WebDriverWait(driver, 150, 50).until((Predicate<WebDriver>) d -> ((JavascriptExecutor) d).executeScript(
                    "return document.readyState").equals("complete"));
            return Void.TYPE;
        }).orElseThrow(() -> new IllegalStateException("Context not initialized"));
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

        final T page = PageLoader.loadPage(pageType);
        final Optional<Transactional> tx = Optional.ofNullable(Transactional.class.isAssignableFrom(pageType)
                                                               ? (Transactional) page
                                                               : null);
        tx.ifPresent(ts -> getClassTxName(pageType).ifPresent(ts::txBegin));
        try {
            page.loadPage();
        } finally {
            tx.ifPresent(ts -> getClassTxName(pageType).ifPresent(ts::txEnd));
        }

        page.locateElements();
        return page;

    }

}
