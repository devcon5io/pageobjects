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

package io.devcon5.pageobjects.measure;

import static org.slf4j.LoggerFactory.getLogger;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;

/**
 * A collector for tracking transactions for a single thread. The collctor does not support stacking transaction
 * with the same name. Every transaction should be closed before a new transaction with the same name is started.
 * Upon stopping collecting times, all open transactions are logged out.
 * To start collecting response times, invoke the {@link #startCollecting()}
 * method, to stop recording, the {@link #stopCollecting()}. This will associate the time collection for the current
 * thread with the current instance.
 *
 */
public class ResponseTimeCollector {

    private static final Logger LOG = getLogger(ResponseTimeCollector.class);

    public static ThreadLocal<Optional<ResponseTimeCollector>> CURRENT = ThreadLocal.withInitial(Optional::empty);

    private Map<String, ResponseTime> responseTimes = new ConcurrentHashMap<>();

    public static Optional<ResponseTimeCollector> current() {

        return CURRENT.get();
    }

    /**
     * Starts the recording of response times for the current thread.
     */
    public void startCollecting() {

        CURRENT.set(Optional.of(this));
    }

    /**
     * Stops the recoding of the response times for the current thread.
     */
    public void stopCollecting() {

        CURRENT.set(Optional.empty());
        if (!responseTimes.isEmpty()) {
            LOG.warn("Some Transactions have not been completed:\n{}",
                     responseTimes.values().stream().map(ResponseTime::toString).collect(Collectors.joining("\n")));
            responseTimes.clear();
        }
    }

    /**
     * Captures a completed transaction that has been manually recorded.
     * @param txName
     *  the name of the transaction
     * @param start
     *  the start point
     * @param end
     *  the end point
     */
    public void captureTx(String txName, Instant start, Instant end) {
        captureTx(txName, start, Duration.between(start, end));
    }

    /**
     * Caputes a completed transaction that has been manually recorded.
     * @param txName
     *  the name of the transaction
     * @param start
     *  the start point
     * @param duration
     *  the duration of the execution
     */
    public void captureTx(String txName, Instant start, Duration duration) {
        LOG.trace("TX {} started {} took {}", txName, start, duration);
        ResponseTimes.collect(new ResponseTime(txName, start, duration));
    }

    /**
     * Starts a new transaction time recording
     * @param tx
     *  the name of the transaction
     */
    public void startTx(String tx) {

        final Instant now = Instant.now();
        LOG.trace("TX Start {} at {}", tx, now);
        responseTimes.put(tx, ResponseTimes.startTx(tx, now));
    }

    /**
     * Stops the recording of a transaction time, storing the transaction in the global response time store
     * @param tx
     *  the transaction that has been completd
     */
    public void stopTx(String tx) {

        final Instant now = Instant.now();
        stopTx(tx, now);
    }

    /**
     * Stops the transaction at the specific time point
     * @param tx
     *  the transaction to stop
     * @param now
     *  the manually measured time point when the transaction ended
     */
    public void stopTx(String tx, Instant now) {

        if (!responseTimes.containsKey(tx)) {
            throw new IllegalStateException("Transaction " + tx + " not started");
        }
        LOG.trace("TX End {} at {}", tx, now);
        ResponseTimes.stopTx(responseTimes.remove(tx).finish(now));
    }

}
