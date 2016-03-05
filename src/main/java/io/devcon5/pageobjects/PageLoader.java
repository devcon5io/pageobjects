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

import static io.devcon5.pageobjects.tx.TransactionHelper.addTransactionSupport;

import io.devcon5.pageobjects.tx.Transactional;

/**
 * Loader of a page instance. The page loader adds transaction support to the instance to measure
 * the execution of transactional methods.
 */
public final class PageLoader {

    private PageLoader(){}

    /**
     * Loads a page with optional transaction support.
     * @param pageType
     *  the page type to instantiate
     * @param <T>
     *  the type of the page
     * @return
     *  the page instance of the object model
     */
    public static <T extends Page> T loadPage(Class<T> pageType) {

        try {
            T page = pageType.newInstance();
            if (Transactional.class.isAssignableFrom(pageType)) {
                page = addTransactionSupport((Transactional)page);
            }
            return page;

        } catch (InstantiationException | IllegalAccessException e) {
            throw new AssertionError("Page " + pageType.getName() + " can not be loaded", e);
        }

    }

}
