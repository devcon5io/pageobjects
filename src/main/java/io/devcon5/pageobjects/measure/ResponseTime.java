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

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * ResponseTime of a Transaction. Is a specific {@link io.devcon5.pageobjects.measure.TimeMeasure} that associates
 * a unique id with the measurement and a specific transaction with the recored times. The ResponseTime is
 * immutable. Upon starting a measurement, the duration is set to ZERO. When finishing the transaction, a new
 * instance is created.
 */
public class ResponseTime extends TimeMeasure {

    private final UUID uuid;
    private final String transaction;

    public ResponseTime(String transaction, Instant start) {
        this(UUID.randomUUID(), transaction, start, NEGATIVE);
    }

    public ResponseTime(final String txName, final Instant start, final Duration duration) {
        this(UUID.randomUUID(), txName, start, duration);
    }

    ResponseTime(UUID uuid, String transaction, Instant start, Duration duration) {
        super(start, duration);
        this.uuid = uuid;
        this.transaction = transaction;
    }

    /**
     * Finishes the response time measurement and creates a new ResponseTime
     * @return
     *  the response time representing the end of the transaction
     */
    public ResponseTime finish(){
        return finish(Instant.now());
    }

    /**
     * Finishes the transaction response time recording on the the specific instant.
     * @param end
     *  the time point when the transaction was finished
     * @return
     *  a new ResponseTime instance representing the measured time of the transaction
     */
    public ResponseTime finish(Instant end){
        if(isFinished()) {
            throw new IllegalStateException("Transaction already finished");
        }
        return new ResponseTime(uuid, transaction, getStart(), Duration.between(getStart(), end));
    }

    /**
     * The unique id of the measure
     * @return
     *  the id of the measure
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * The name of the transaction that was measured
     * @return
     *  the name of the transaction
     */
    public String getTransaction() {
        return transaction;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64)
            .append("ResponseTime{")
            .append("transaction='").append(transaction).append('\'')
            .append(", start=").append(getStart())
            .append(", duration=").append(getDuration())
            .append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResponseTime that = (ResponseTime) o;

        return uuid.equals(that.uuid);

    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
