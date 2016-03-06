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

package io.devcon5.pageobjects.tx;

import java.time.Instant;

import io.devcon5.pageobjects.measure.ResponseTimeCollector;

/**
 * Adds transaction support to a page object. Using transactions, response times of accesses to page objects can be
 * recorded. To automatically record certain actions such as page load or method execution the according elements
 * (Page class or Method) have to be annotated with {@link Transaction}
 */
public interface Transactional {

    /**
     * Method to start a transaction manually. It's important to stop the transaction
     * @param txName
     *  the name of the transaction to start. Use the same annotatio to stop the transaction.
     */
    default void txBegin(String txName) {
        ResponseTimeCollector.current().ifPresent(rtc -> rtc.startTx(txName));
    }

    /**
     * Method to stop the manually started transaction. It's important that started transactions are properly
     * stopped.
     * @param txName
     *  the name of the transaction to stop
     */
    default void txEnd(String txName) {
        Instant now = Instant.now();
        ResponseTimeCollector.current().ifPresent(rtc -> rtc.stopTx(txName, now));
    }
}
