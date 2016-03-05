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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Global Handler for recording response time. As default setting, the response times are stored in a global collection.
 * This class allows to record response times of transaction across multiple threads. It must be ensured, that the
 * response are cleared after the measured sequence, otherwise the response remain in memory permanently.
 */
public final class ResponseTimes {

    /**
     * Global ResponseTime collection.
     */
    private static final Map<UUID, ResponseTime> responseTimes = new ConcurrentHashMap<>();

    /**
     * Default consumer putting a response time into the global table
     */
    private static final Consumer<ResponseTime> DEFAULT_CONSUMER = rt -> responseTimes.put(rt.getUuid(), rt);

    private static AtomicReference<Consumer<ResponseTime>> START_TX_CONSUMER = new AtomicReference<>(DEFAULT_CONSUMER);

    private static AtomicReference<Consumer<ResponseTime>> STOP_TX_CONSUMER = new AtomicReference<>(DEFAULT_CONSUMER);

    private ResponseTimes(){}

    /**
     * Clears the global response time collection
     */
    public static void clear() {

        responseTimes.clear();
    }

    /**
     * Resets the response time consumer to the default.
     */
    public static void resetResponseTimeHandlers(){
        onMeasureStart(DEFAULT_CONSUMER);
        onMeasureEnd(DEFAULT_CONSUMER);
    }

    /**
     * Overrides the default consumer that handles response times on the beginning of a transaction
     * @param responseTimeConsumer
     *  the consumer to process response times
     */
    public static void onMeasureStart(Consumer<ResponseTime> responseTimeConsumer){
        START_TX_CONSUMER.set(responseTimeConsumer);
    }

    /**
     * Overrides the default consumer that handles response times on the end of a transaction
     * @param responseTimeConsumer
     *  the consumer to process response times
     */
    public static void onMeasureEnd(Consumer<ResponseTime> responseTimeConsumer){
        STOP_TX_CONSUMER.set(responseTimeConsumer);
    }

    /**
     * Starts a transaction on the specified point in time
     *
     * @param transaction
     *         the transaction to start
     *
     * @return the ResponseTime handle to track the time measure
     */
    public static ResponseTime startTx(String transaction) {

        return startTx(transaction, Instant.now());
    }

    /**
     * Starts a transaction on the specified point in time
     *
     * @param transaction
     *         the transaction to start
     * @param start
     *         the point in time when the transaction started
     *
     * @return the ResponseTime handle for this response time measure
     */
    public static ResponseTime startTx(String transaction, Instant start) {
        final ResponseTime trt = new ResponseTime(transaction, start);
        START_TX_CONSUMER.get().accept(trt);
        return trt;
    }

    /**
     * Records the response time as finished.
     *
     * @param finish
     *         the response time of the finished transaction. If the response time denotes no finished transaction, it
     *         is finished now.
     *
     * @return the finished ResponseTime
     */
    public static ResponseTime stopTx(ResponseTime finish) {

        return collect(finish.isFinished() ? finish : finish.finish());
    }

    /**
     * Collects a completed ResponeTime recording.
     *
     * @param responseTime
     *         the responseTime to collect into the global collection.
     */
    public static ResponseTime collect(final ResponseTime responseTime) {

        if (!responseTime.isFinished()) {
            throw new AssertionError("Collecting of unfinished responseTimes is not allowed");
        }
        STOP_TX_CONSUMER.get().accept(responseTime);
        return responseTime;
    }

    /**
     * Collects a completed time measure for a specific transaction.
     * @param transaction
     *  the transaction to be associated with the time measure
     * @param result
     *  a time measure result to associate with the transaction
     * @return
     *  the response time recorded
     */
    public static ResponseTime collect(String transaction, TimeMeasure result) {
        return collect(new ResponseTime(transaction, result.getStart(), result.getDuration()));
    }

    /**
     * Returns all recorded response times.
     *
     * @return a map of the response times. The map contains the transaction names as key, and a list of measured
     * responseTimes for that transaction as value.
     */
    public static Map<String, List<ResponseTime>> getResponseTimes() {

        final Map<String, List<ResponseTime>> result = new HashMap<>();
        responseTimes.values().stream().forEach(trt -> {
            if (!result.containsKey(trt.getTransaction())) {
                result.put(trt.getTransaction(), new ArrayList<>());
            }
            result.get(trt.getTransaction()).add(trt);
        });
        return result;
    }
}
